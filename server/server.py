#!/usr/bin/env python
# encoding: utf-8
# ******************************************************
# Author       : LIU JIAQI
# Last modified: 2019-04-28 21:37
# Email        : liujq1313@gmail.com
# Filename     : server.py
# Description  :
# ******************************************************

import os
import json
from flask import Flask, request, g, redirect, Response
from db import check_login, create_user, check_email, update_fruit, update_amount, cal_vitamin, recommend_fruit

app = Flask(__name__)
app.secret_key = b''



@app.route('/login', methods=['POST'])
def login():
    content = json.loads(request.form['PostData'])
    email = content['email']
    password = content['password']

    if(check_login(email, password)):
        return 'true'
    else:
        return 'false'

@app.route('/signup', methods=['POST'])
def signup():
    content = json.loads(request.form['PostData'])
    email = content['email']
    password = content['password']
    machine = content['machine']

    if check_email(email):
        return 'false'
    user_dict = {'email': email,
                 'password': password,
                 'machine': machine}
    try:
        create_user(user_dict)
        return 'true'
    except KeyError:
        return 'false'

@app.route('/fruit', methods=['POST'])
def getFruit():
    content = json.loads(request.form['PostData'])
    print content
    email = content['email']

    if len(content.keys()) == 1:
        method = "GET"
    else:
        method = "POST"

    if method == "GET":
        result = update_fruit(email)
        return json.dumps(result)
    else:
        # update amount
        map = {}
        for key in content.keys():
            if key != 'email':
                map[key] = content[key]
        try:
            update_amount(map)
            return "true"
        except KeyError:
            return "false"


VITAMIN = ['A', 'B1', 'B2', 'B6', 'C']


@app.route('/recommend', methods=['POST'])
def recommend():
    content = json.loads(request.form['PostData'])
    email = content['email']
    day = int(content['day'])
    try:
        total, _ = cal_vitamin(email, day)
        recommend = recommend_fruit(total, 3)
        vitamin = [float(val) for val in total]
        result = {}
        for i in range(len(vitamin)):
            result[VITAMIN[i]] = vitamin[i]

        str = ""
        for fruit in recommend:
            str += fruit + "\n"
        result['recommend'] = str
        return json.dumps(result)
    except ValueError:
        return "false"


if __name__ == "__main__":
    import click

    @click.command()
    @click.option('--debug', is_flag=True)
    @click.option('--threaded', is_flag=True)
    @click.argument('HOST', default='0.0.0.0')
    @click.argument('PORT', default=8000, type=int)
    def run(debug, threaded, host, port):

        HOST, PORT = host, port
        print "running on %s:%d" % (HOST, PORT)
        app.run(host=HOST, port=PORT, debug=debug, threaded=threaded)


    run()


