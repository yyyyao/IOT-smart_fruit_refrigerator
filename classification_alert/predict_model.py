#!/usr/bin/env python
# -*- coding: utf-8 -*-
# Author: Tingyu Li (tl2861)
# ---------------------------

#!/usr/bin/env python
# encoding: utf-8
# ******************************************************
# Author       : LIU JIAQI
# Last modified: 2019-04-07 16:58
# Email        : liujq1313@gmail.com
# Filename     : train.py
# Description  :
# ******************************************************
import os
import time
import datetime
import pickle
import argparse
import numpy as np
import tensorflow as tf
import tensorflow.keras as keras
from load import load
import matplotlib
matplotlib.use('agg')
import matplotlib.pyplot as plt



class Args():
    def __init__(self):
        parser = argparse.ArgumentParser()
        parser.add_argument('--baseDir', type=str, default="")
        parser.add_argument('--dataDir', type=str, default="")
        parser.add_argument('--saveDir', type=str, default="")
        parser.add_argument('--epoch', type=int, default=10)
        parser.add_argument('--batchSize', type=int, default=32)
        parser.add_argument('--width', type=int, default=96)
        parser.add_argument('--lr', type=float, default=0.001)
        parser.add_argument('--drop', type=float, default=0.6)
        parser.add_argument('--restore', type=bool, default=True)
        parser.add_argument('--train_num', type=int, default=0)
        parser.add_argument('--test_num', type=int, default=0)
        parser.add_argument('--plot', type=bool, default=True)
        parser.add_argument('--argspath', type=str, default='')
        parser.add_argument('--img', type=str, default=None)
        self.args = parser.parse_args()

    def restore(self, filename):
        with open(filename, 'r') as f:
            self.args = pickle.load(f)

class Models():
    def __init__(self, args):
        self.epoch = args.epoch
        self.width = args.width
        self.lr = args.lr
        self.drop = args.drop
        self.buildModel()

    def buildModel(self):
        """ build model """
        # (96, 96, 3)
        base_model = keras.applications.MobileNetV2(input_shape=(self.width,
                                                                 self.width,
                                                                 3),
                                                    include_top=False)
        base_model.trainable = False
        global_average_layer = keras.layers.GlobalAveragePooling2D()
        dense1 = keras.layers.Dense(256, activation='relu')
        #dense2 = keras.layers.Dense(256,activation='relu')
        dense3 = keras.layers.Dense(80, activation='softmax')
        self.model = keras.Sequential([
            base_model,
            global_average_layer,
            dense1,
            #dense2,
            dense3
        ])

        self.model.compile(optimizer=tf.keras.optimizers.RMSprop(1e-3),
                           loss='sparse_categorical_crossentropy',
                           metrics=['accuracy'])
        self.model.summary()

def parser_function(filename):
    image_string = tf.read_file(filename)
    image_decoded = tf.image.decode_jpeg(image_string, channels=3)
    image = tf.cast(image_decoded, tf.float32)
    image = image/255.0
    image = tf.image.resize(image, (96, 96))
    return image
