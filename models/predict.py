#!/usr/bin/env python
# encoding: utf-8
# ******************************************************
# Author       : LIU JIAQI
# Last modified: 2019-04-18 17:09
# Email        : liujq1313@gmail.com
# Filename     : predict.py
# Description  :
# ******************************************************

import tensorflow as tf
import tensorflow.keras as keras
import numpy as np

from train import Models, Args, parser_function

class TrainedModel():
    def __init__(self):
        self.args = None
        self.model = None
        self.labels = None
        self.classfile = ''
        self.to_label()

    def load_args(self):
        args = Args()
        args.restore(args.args.baseDir+args.args.argspath)
        args = args.args
        args.restore = True
        self.args = args

    def load_model(self):
        model = Models(self.args).model
        model.load_weights(self.args.baseDir + self.args.saveDir + 'model')
        self.model = model

    def predict(self, filenames):
        imgs = tf.stack(list(map(parser_function, filenames)))
        r = np.argmax(self.model.predict(imgs, steps=1), axis=1)
        results = [self.labels[val] for val in r]
        return results

    def to_label(self):
        temp = []
        with open(self.classfile, 'r') as f:
            for val in f.readlines():
                if val != '\n':
                    temp.append(val.strip())
        self.labels = temp

if __name__ == '__main__':
    model = TrainedModel()
    model.load_args()
    model.load_model()
    print model.predict(['3_100.jpg', '4_100.jpg'])

