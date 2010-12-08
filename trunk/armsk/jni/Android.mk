# date: Summer, 2010 
# author: Ethan Rublee
# contact: ethan.rublee@gmail.com
#
LOCAL_PATH := $(call my-dir)

include $(CLEAR_VARS)

#pass in OPENCV_ROOT or define it here
#OPENCV_ROOT := ~/android-opencv/opencv



#define OPENCV_INCLUDES
include $(OPENCV_ROOT)/includes.mk
#include /Developer/Android_Projekt/android-opencv/opencv/android/build/android-opencv.mk
#define OPENCV_LIBS
include $(OPENCV_ROOT)/libs.mk

LOCAL_LDLIBS += $(OPENCV_LIBS) $(ANDROID_OPENCV_LIBS) -llog -lGLESv2
    
LOCAL_C_INCLUDES +=  $(OPENCV_INCLUDES) $(ANDROID_OPENCV_INCLUDES)

LOCAL_MODULE    := armskdemoapp

LOCAL_SRC_FILES := ARmsk.cpp gen/armskdemoapp_swig.cpp

include $(BUILD_SHARED_LIBRARY)

