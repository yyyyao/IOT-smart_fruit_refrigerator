#!/usr/bin/env python
import boto3
import aws
from boto3.dynamodb.conditions import Key,Attr
import sys
reload(sys)
sys.setdefaultencoding('utf-8')
from sqlalchemy import create_engine
from datetime import datetime
from pytz import timezone
import time
import calendar
import json
import numpy as np
import math
from operator import itemgetter
import pandas as pd
# DATABASEURI = "postgresql://liujq13:kongkong13@localhost/fruit"
# engine = create_engine(DATABASEURI)

snsClient = aws.getClient("sns","us-east-1")

dynamodb = aws.getResource('dynamodb','us-east-1')
expire_table = dynamodb.Table('fruit-expire')
vitamin_table = dynamodb.Table('fruit-vitamin')

TIMEZONE = timezone('America/New_York')
DAY = 3*86400
# DAY = 3

# One day vitamin people need
one_day_vdic = {'A' : 10, 'B1': 14, 'B2' : 16, 'B6' : 2, 'C' : 600}
one_day = [10000,10000,3000,1000,10000000]
vit_dic = {0 : 'A', 1 : 'B1', 2 : 'B2', 3: 'B6', 4 : 'C'}
# def connect_db():
#     return engine.connect()


# def load_vitamin(filename):
#     df =

def check_email(email):
    """True : email exist in table"""
    table = dynamodb.Table('fruit-user')
    response = table.get_item(Key={'email':email})
    try:
        response['Item']
        return True
    except:
        return False


# def check_password(password):
#     """"""

def check_machine(machine):
    """
    If machine exist return True
    not exist return False
    """
    table = dynamodb.Table('fruit-topic')
    response = table.get_item(Key = {'machine': str(machine)})
    try:
        response['Item']
        return True
    except:
        return False

def check_login(email, password):
    """
    check if email and password is correct
    True: email and password correct
    """
    table = dynamodb.Table('fruit-user')
    try:
        response = table.get_item(Key={'email':email})
        return response['Item']['password'] == password
    except:
        return False

def create_user(user_dict):
    """create new user in table"""
    table_user = dynamodb.Table('fruit-user')
    table_user.put_item(Item={'email': user_dict['email'],
                         'password': user_dict['password'],
                         'machine': user_dict['machine']})
    table_topic = dynamodb.Table('fruit-topic')
    topic_arn = table_topic.get_item(Key={'machine':user_dict['machine']})['Item']['topicArn']
    response = snsClient.subscribe(
                        TopicArn = topic_arn,
                        Protocol = 'email',
                        Endpoint = user_dict['email']
                        )

def update_fruit(email):
    """
    return information under this email
    fruit name | expire date | amount
    """
    result = {}
    # fruit_dic = {"fruit_id": [], "fruit_name" : [], "expire_date" : [], "amount" : []}
    # print machine
    machine = get_machine_on_email(email)
    table = dynamodb.Table('fruit-img')
    response = table.scan(FilterExpression=Attr('machine').eq(machine))
    # print response
    for i in response['Items']:
        # print i
        fruit_name = i['fruit_name']
        if fruit_name != 'NOT':
            # putin_date = i['time_now']s
            fruit_id = i['imgId']
            amount = int(i['amount'])
            # expire_period = expire_table.get_item(Key={'fruit_name':fruit_name})['Item']['expire_period']
            # expire_date = putin_date + expire_period
            expire_date = datetime.fromtimestamp(i['expire_date'],TIMEZONE).strftime("%m/%d/%Y, %H:%M:%S")
            # fruit_dic['fruit_id'].append(fruit_id)
            # fruit_dic['fruit_name'].append(fruit_name)
            # fruit_dic['expire_date'].append(expire_date)
            # fruit_dic['amount'].append(amount)
            result[fruit_id] = {'fruit_name': fruit_name, 'expire_date' : expire_date, 'amount': str(amount)}
    print result
    return result

def update_amount(amount_dic):
    """update the update_amount of the fruit"""
    table = dynamodb.Table('fruit-img')
    # item = table.get_item(Key = {'email' : amount_dic.keys()[0]})
    for key, value in amount_dic.items():
        if key != "email":
            old_record = table.get_item(Key = {'imgId' : key})['Item']
            old_amount = int(old_record['amount'])
            if (old_amount > int(value)):
                update_eaten(old_record['fruit_name'], old_record['machine'], old_record['weight'], int(old_amount)-int(value))
            if (int(value) == 0):
                table.delete_item(Key={'imgId':key})
            else:
                table.update_item(
                    Key = {'imgId' : key},
                    UpdateExpression = 'set amount = :amount',
                    ExpressionAttributeValues = {':amount' : value},
                    ReturnValues = 'UPDATED_NEW'
                    )


def get_machine_on_email(email):
    """Get machine code baseds on email"""
    table = dynamodb.Table('fruit-user')
    response = table.get_item(Key={'email':email})
    # print response
    machine = response['Item']['machine']
    return machine


def get_out_of_date_fruit(email, alter_time):
    """get fruit older than curtime - alter_time"""
    # fruit = []
    machine = get_machine_on_email(email)
    curtime = calendar.timegm(time.gmtime())
    table = dynamodb.Table('fruit-img')
    # print curtime
    threhold = curtime+alter_time
    response = table.scan(FilterExpression=Attr('expire_date').lt(threhold) & Attr('machine').eq(machine))
    print response
    return response

def update_eaten(fruit_name, machine, weight, amount):
    """give the fruit a eaten date, imgId is the id of the fruit"""
    # machine = get_machine_on_email(email)
    table = dynamodb.Table('fruit-log')
    eatendate = calendar.timegm(time.gmtime())
    print('eatendate = {}'.format(eatendate))
    table.update_item(
        Key={'machine': machine, 'eaten_time' : eatendate},
        UpdateExpression="set amount = :amount, fruit_name = :fruit_name, weight = :weight",
        ExpressionAttributeValues={':amount': amount, ':fruit_name' : fruit_name, ':weight' : weight},
        ReturnValues="UPDATED_NEW"
        )


def delete_fruit(imgId):
    table = dynamodb.Table('fruit-img')
    response = table.delete_item(Key={'imgId':imgId})


def cal_vitamin(email, day):
    """
    calculate the vitamin the user get in <day>.
    If don't have records up to <day>,
    calculate all records and it related dates
    input :email <str>
          :day <int> the day want to calculate the total vitamin
    output :total : [np.narray(float)]
                    represent ["A", "B1", "B2", "B6" , "C"]
            :cal_period : int
    """
    machine = get_machine_on_email(email)
    print(machine)
    curtime = calendar.timegm(time.gmtime())
    # curtime = time.time()
    threhold = int(curtime - day*86400)
    print threhold
    # table = dynamodb.Table('fruit-img')
    table = dynamodb.Table('fruit-log')
    results = table.scan(FilterExpression=Attr('eaten_time').gt(threhold) & Attr('machine').eq(machine))
    print results
    print results['Count']
    # try: 
    #     # The oldest record is older than cal day
    #     actual_day = DAY
    #     print(results['Items'])
    #     results['Items'][0]['machine']
    #     results = table.scan(FilterExpression=Attr('eaten_time').lt(threhold) & Attr('machine').eq(machine))
    # except Exception as e:
    #     print e
    #     results = table.scan(FilterExpression = Attr('machine').eq(machine))
    #     min_time = float('inf')
    #     for i in results['Items']:
    #         if i['eaten_time'] < min_time:
    #             min_time = i['eaten_time']
    #     actual_day = curtime - min_time
    total = np.array([0,0,0,0,0]).astype(float)
    try:
        for i in results['Items']:
            total_temp = cal_fruit_vitamin(i['fruit_name'],int(i['weight']),int(i['amount']))
            print total_temp
            total += total_temp
            # print i
    except Exception as e:
        print('error{}'.format(e))
        pass
    # transfer actual_day to day
    # cal_period = int(math.ceil(actual_day/86400))
    # print('total tpye = {}'.format(type(list(total))))
    # print ('total_vit = {}'.format(total))
    # print ('days = {}'.format(cal_period))
    total = np.around(total/100, decimals=2)
    total = list(total)
    cal_period = None
    return total, cal_period



def cal_fruit_vitamin(fruit_name, weight, amount):
    """
    calculate the total of each vitamin in one record
    Input :fruit_name <str> the name of the fruit
          :amount <int> the amount of the fruit eaten
    Output :vit_total [np.narray(float)]
                    represent ["A", "B1", "B2", "B6" , "C"]
    """
    vit_total = []
    # response = vitamin_table.get_item(Key={'fruit_name':fruit_name})['Item']
    # vit_total.append(response['A']*amount)
    # vit_total.append(response['B1']*amount)
    # vit_total.append(response['B2']*amount)
    # vit_total.append(response['B6']*amount)
    # vit_total.append(response['C']*amount)
    vitamin_df = pd.read_csv('vitamin.csv', index_col = 0)
    # print vitamin_df
    weight = weight/100.0
    vit_total = np.squeeze((vitamin_df.loc[[fruit_name]] * amount * weight).values)
    # print vit_total
    return vit_total

def recommend_fruit(total, limit):
    """
    get top <limit> number of recommed fruits based on <total>
    Input :total [np.narray(float)]
                represent ["A", "B1", "B2", "B6" , "C"]
          :limit <int>
    Output :results_list [<str>] list of fruit_name
    """
    # total, day = cal_vitamin(email)
    # total.astype(float)
    total = np.array(total).astype(float)
    total_trans = total/one_day
    # print total_trans
    val, min_key = min((val, idx) for (idx, val) in np.ndenumerate(total_trans))
    # print "minkey={}".format(min_key)
    min_vit = vit_dic[min_key[0]]
    # print min_vit
    results = get_fruit_high_in_vit(min_vit, limit)
    return results
    # result = vitamin_df.sort_values(by=)

def get_fruit_high_in_vit(vitamin, limit):
    """
    get the top <limit> fruits that are hign in <vitamin>
    Input :vitamin <str> eg. "A" , "B1" ...
          :limit <int>
    Output :results_list [<str>] list of fruit name
    """
    vitamin_df = pd.read_csv('vitamin.csv', index_col = 0)
    results = vitamin_df.nlargest(limit, vitamin)
    results_list =  results.index.tolist()
    print results_list
    return results_list


if __name__ == '__main__':
    # create_user({'email':'tl2861@columbia.edu', 'password' : '0000', 'machine' : '0001'})
    # print('success crease')
    # print(check_email('904985646@qq.com'))
    # print(check_email('904983646@qq.com'))
    # print(check_login('904985646@qq.com','0000'))
    # print(check_login('904985646@qq.com','0001'))
    # print(check_login('904983646@qq.com','0001'))
    # update_fruit('904985646@qq.com')
    # update_eaten('0001_1557020941.jpg')
    # get_out_of_date_fruit('904985646@qq.com',3)
    # delete_fruit('0001_1557021175.jpg')
    update_amount({'0001_1557258727.jpg':17})
    update_amount({'0001_1557258727.jpg':16})
    total, cal_period = cal_vitamin('904985646@qq.com',1)
    print(total)
    # print(cal_period)
    recommend_fruit(total, 3)
    # vitamin_df = pd.read_csv('vitamin.csv', index_col = 0)
    # print vitamin_df.columns
    # print vitamin_df.nlargest(3, 'A')

