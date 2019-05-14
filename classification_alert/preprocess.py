#!/usr/bin/env python
# encoding: utf-8
# ******************************************************
# Author       : LIU JIAQI
# Last modified: 2019-04-07 19:42
# Email        : liujq1313@gmail.com
# Filename     : preprocess.py
# Description  :
# ******************************************************
import os


temp = []
with open('/home/liujiaqi/fruits-360/class2.txt', 'r') as f:
    for val in f.readlines():
        if val != '\n':
            temp.append(val.strip())
labels = {}
for i in range(len(temp)):
   labels[temp[i]] = i

TRAIN_BASE_DIR = '/home/liujiaqi/fruits-360/Training'
TEST_BASE_DIR = '/home/liujiaqi/fruits-360/Test'

def create(BASE_DIR):
    files = []
    label = []
    for k, v in labels.items():
        dir = BASE_DIR + '/' + k
        filename = [dir + '/' + val for val in os.listdir(dir)]

        label = label + [v]*len(filename)
        files = files + filename
    print len(label)
    print len(files)
    return files, label

train_files, train_labels = create(TRAIN_BASE_DIR)
test_files, test_labels = create(TEST_BASE_DIR)

with open('filename2.txt', 'w') as f:
    for name in train_files:
        f.write(name + '#')
with open('labels2.txt', 'w') as f:
    for val in train_labels:
        f.write(str(val) + ' ')

with open('filename_test2.txt', 'w') as f:
    for name in train_files:
        f.write(name + '#')
with open('labels_test2.txt', 'w') as f:
    for val in train_labels:
        f.write(str(val) + ' ')
