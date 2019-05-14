#!/usr/bin/env python
# encoding: utf-8
# ******************************************************
# Author       : LIU JIAQI
# Last modified: 2019-04-07 19:30
# Email        : liujq1313@gmail.com
# Filename     : load.py
# Description  :
# ******************************************************

import os

def load(file_dir, label_dir):
    filename = []
    with open(file_dir, 'r') as f:
        for file in f.readline().split('#'):
            if file != '':
                filename.append(file)
    labels = []
    with open(label_dir, 'r') as f:
        for val in f.readline().split(' '):
            if val != '':
                labels.append(int(val))
    return filename, labels

file_dir = load('filename2.txt', 'labels2.txt')

