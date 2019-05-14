from picamera import PiCamera
import time
import sys
import threading
#-------dynamodb------
import boto3
# from boto3.dynamodb.conditions import Key,Attr

import aws
from datetime import datetime
import calendar
from PIL import Image
# postgresql

# dynamoDB
dynamodb = aws.getResource('dynamodb', "us-east-1")

# S3
s3 = aws.getResource('s3', 'us-east-1')
# s3 = aws.getClient('s3', 'us-east-1')

# sns
snsClient = aws.getClient("sns","us-east-1")
# topic_arn = "arn:aws:sns:us-east-1:810588570945:fruit-alert"

import sys
MACHINE = sys.argv[1]
print('machine{}'.format(MACHINE))


def create_topic():
    table = dynamodb.Table('fruit-topic')
    response = table.get_item(Key={'machine' : MACHINE})
    try:
        response['Item']
    except:
        topic = snsClient.create_topic(
                Name = str(MACHINE),
            )
        print topic['TopicArn']
        table.put_item(Item={
            'machine' : MACHINE,
            'topicArn' : topic['TopicArn']
            })



class dynamoMethods:
    def __init__(self, dbName):
        # dynamobd = aws.getResource('dynamobd','us-east-1')
        try:
            self.table = dynamodb.Table(dbName)
        except Exception as e:
            print(e)

    def upload_img(self, valdic):
        """
        valdic: dictionary
        machine string
        time_now timestamp
        """
        # imgId = MACHINE + '_' + str(valdic['time_now'])
        try:
            self.table.put_item(Item={
                'imgId' : valdic['filename'],
                'machine' : MACHINE,
                'time_now' : valdic['time_now'],
                'weight' : valdic['weight'],
                'fruit_name' : 'NOT',
                'expire_date' : 'NOT',
                'eatendate' : 0,
                'amount' : 1
            })
        except Exception as e:
            print(e)


# class

#------hx711--------
EMULATE_HX711=False

if not EMULATE_HX711:
    import RPi.GPIO as GPIO
    from hx711 import HX711
else:
    from emulated_hx711 import HX711

def cleanAndExit():
    print "Cleaning..."

    if not EMULATE_HX711:
        GPIO.cleanup()

    print "BYE!"
    sys.exit()
hx = HX711(5,6)

hx.set_reading_format("MSB", "MSB")
hx.set_reference_unit(490)
hx.reset()
hx.tare()
print "Tare done! Add weight now..."

#------camera------
camera = PiCamera()
i = 0

# pic_flag = 0
# val = 0

# pic_name = None

# time_now = None

# valdic = {}

lock = threading.Lock()

TH = 10
def take_photo(my_db):
    # global pic_flag
    global i
    # global val
    # global pic_name
    # global time_now
    # global valdic
    flag = 0
    valdic = {}
    try:
        while True:
            print '----time{}------'.format(i)
            val = hx.get_weight(5)
            # val = -3000
            print val
            if val > TH and flag == 0:
                flag = 1
                time.sleep(1)
                if val > TH:
                    camera.start_preview()
                    time.sleep(2)
                    # lock.acquire()
                    # time_now = datetime.now().strftime("%Y-%m-%d %H:%M:%S")
                    time_now = calendar.timegm(time.gmtime())
                    # lock.release()
                    filename = MACHINE + '_' + str(time_now) + '.jpg'
                    valdic['time_now'] = time_now
                    valdic['weight'] = val
                    valdic['filename'] = filename
                    camera.capture(filename)
                    i = i + 1
                    camera.stop_preview()
                    # pic_name = filename
                    img = Image.open(filename)
                    cropped = img.crop((315,0,1365,1050))
                    cropped.save(filename)
                    # lock.acquire()
                    # pic_flag = 1
                    # lock.release()
                    print "photo taken"
                    print "start upload"
                    s3.meta.client.upload_file(filename, 'iotfruit', filename)
                    my_db.upload_img(valdic)
                    print "success upload"
            # elif pic_flag == 1:
                # print("haven't deal with the former pic, please wait")
            if val < TH and flag == 1:
                flag = 0
            time.sleep(2)
    except (KeyboardInterrupt):
        cleanAndExit()

def push_to_db(dynamoMethods, pic_name, valdic):
    """push to dynamodb and s3"""
    # global pic_flag
    # global val
    # global pic_name
    # global time_now
    try:
        # while True:
            #print '--------push run--------'
            #if pic_flag == 1:
                print '-------start push-----'
                # print val
                # fruit = 'apple'
                # s3.Bucket('fruit').upload_file(filename, filename)
                s3.meta.client.upload_file(pic_name, 'iotfruit', pic_name)
                my_db.upload_img(valdic)
                # lock.acquire()
                # pic_flag = 0
                # lock.release()
                print '-------push success-------'
            # time.sleep(1)
    except (KeyboardInterrupt):
        exit


if __name__ == "__main__":
    # thread1 = threading.Thread(target = take_photo)
    # thread2 = threading.Thread(target = push_to_db, args=(my_db,))
    # thread1.setDaemon(True)
    # thread2.setDaemon(True)
    # thread1.start()
    # thread2.start()
    create_topic()
    my_db = dynamoMethods('fruit-img')
    take_photo(my_db)
    #try:
    #    while True:
    #        take_photo(my_db)
    #        time.sleep(1)
    #except KeyboardInterrupt:
    #    exit
