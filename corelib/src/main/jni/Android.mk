CORELIB_LOCAL_PATH := $(call my-dir)

include $(CORELIB_LOCAL_PATH)/libs/libxmp/jni/Android.mk

include $(CLEAR_VARS)

LOCAL_PATH := $(CORELIB_LOCAL_PATH)
include $(CLEAR_VARS)

LOCAL_MODULE    := trax-corelib
LOCAL_SRC_FILES := $(LOCAL_PATH)/modplayer.cpp
LOCAL_STATIC_LIBRARIES := xmp

LOCAL_CFLAGS += -g -Wall -fPIC -I. -I$(LOCAL_PATH)/libs/libxmp/include
LOCAL_LDFLAGS += -llog

ifeq ($(TARGET_ARCH),x86)
#
else
	#LOCAL_CFLAGS +=  -marm -O3 -mfpu=neon
endif 
include $(BUILD_SHARED_LIBRARY)


