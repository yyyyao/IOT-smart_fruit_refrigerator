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
        parser.add_argument('--baseDir', type=str, default="/home/ubuntu/")
        parser.add_argument('--dataDir', type=str, default="~/fruits-360/")
        parser.add_argument('--saveDir', type=str, default="Iot-Fruit/save/1/")
        parser.add_argument('--epoch', type=int, default=10)
        parser.add_argument('--batchSize', type=int, default=32)
        parser.add_argument('--width', type=int, default=96)
        parser.add_argument('--lr', type=float, default=0.001)
        parser.add_argument('--drop', type=float, default=0.6)
        parser.add_argument('--restore', type=bool, default=True)
        parser.add_argument('--train_num', type=int, default=0)
        parser.add_argument('--test_num', type=int, default=0)
        parser.add_argument('--plot', type=bool, default=True)
        parser.add_argument('--argspath', type=str, default='Iot-Fruit/save/1/args.pkl')
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

def get_iter(x, y, batch_size):
    filenames = tf.constant(x)
    labels = tf.constant(y)

    dataset = tf.data.Dataset.from_tensor_slices((filenames, labels))

    def _parse_function(filename, label):
        image = parser_function(filename)
        return image, label

    dataset = dataset.map(_parse_function)
    dataset = dataset.repeat()
    dataset = dataset.shuffle(1000)
    dataset = dataset.batch(batch_size)

    return dataset


if __name__ == "__main__":
    args = Args()
    if args.args.restore:
        args.restore(args.args.baseDir+args.args.argspath)
        args = args.args
        args.restore = True
    x_train, y_train = load('filename2.txt', 'labels2.txt')
    x_test, y_test = load('filename_test2.txt', 'labels_test2.txt')
    args.train_num = len(x_train)
    args.test_num = len(x_test)
    train = get_iter(x_train, y_train, args.batchSize)
    test = get_iter(x_test, y_test, args.batchSize)

    checkpointpath = args.baseDir + args.saveDir
    checkpointdir = checkpointpath + 'model'
    if not os.path.exists(checkpointpath):
        os.makedirs(checkpointpath)

    model = Models(args).model
    if args.restore:
        #latest = tf.trian.latest_checkpoint(checkpointdir)
        model.load_weights(checkpointdir)
        history = model.evaluate(x=test, steps=100)
        print history
    else:
        with open(args.baseDir+args.argspath, 'w') as f:
            pickle.dump(args, f)
        cp_callback = tf.keras.callbacks.ModelCheckpoint(checkpointdir,
                                                         save_weights_only=True,
                                                         verbose=1,save_best_only=True)
        history = model.fit(train,
                            epochs=args.epoch,
                            steps_per_epoch=200,
                            validation_data=test,
                            validation_steps=30,
                            verbose=1,
                            callbacks=[cp_callback])
        keras.utils.plot_model(model, to_file=args.baseDir+args.saveDir+'model.png')
        if args.plot:
            fig, ax = plt.subplots()
            ax.plot(np.arange(args.epoch), history.history['acc'], label='Train')
            ax.plot(np.arange(args.epoch), history.history['val_acc'], label='Test')
            ax.set_title("Model Accuracy")
            ax.set_ylabel("Accuracy")
            ax.set_xlabel("Epoch")
            ax.legend(loc='upper left')
            ts = time.time()
            fig.savefig(args.baseDir+args.saveDir+'acc_{}.png'.format(datetime.datetime.fromtimestamp(ts).strftime('%Y-%m-%d %H:%M:%S')))
            fig2, ax2 = plt.subplots()
            ax2.plot(np.arange(args.epoch), history.history['loss'], label='Train')
            ax2.plot(np.arange(args.epoch), history.history['val_loss'], label='Test')
            ax2.set_title("Model Accuracy")
            ax2.set_ylabel("Loss")
            ax2.set_xlabel("Epoch")
            ax2.legend(loc='upper left')
            fig2.savefig(args.baseDir+args.saveDir+'loss_{}.png'.format(datetime.datetime.fromtimestamp(ts).strftime('%Y-%m-%d %H:%M:%S')))
