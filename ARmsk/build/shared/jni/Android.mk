 # Copyright 2010, 2011 Project ARmsk
 # 
 # This file is part of ARmsk.
 # ARmsk is free software: you can redistribute it and/or modify
 # it under the terms of the GNU General Public License as published by
 # the Free Software Foundation, either version 3 of the License, or
 # (at your option) any later version.
 # ARmsk is distributed in the hope that it will be useful, 
 # but WITHOUT ANY WARRANTY; without even the implied warranty of
 # MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 # GNU General Public License for more details.
 # You should have received a copy of the GNU General Public License
 # along with ARmsk.  If not, see <http://www.gnu.org/licenses/>.

LOCAL_PATH := $(call my-dir)

include $(CLEAR_VARS)

#define OPENCV_INCLUDES
include $(OPENCV_ROOT)/includes.mk
#define OPENCV_LIBS
include $(OPENCV_ROOT)/libs.mk

LOCAL_LDLIBS += $(OPENCV_LIBS) -llog -lGLESv2
    
LOCAL_C_INCLUDES +=  $(OPENCV_INCLUDES) 

LOCAL_MODULE    := android-armsk

LOCAL_SRC_FILES := gen/android_cv_wrap.cpp image_pool.cpp yuv420sp2rgb.c gl_code.cpp Calibration.cpp

include $(BUILD_SHARED_LIBRARY)

