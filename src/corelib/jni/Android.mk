# include $(call all-subdir-makefiles)

LOCAL_PATH := $(call my-dir)

#include $(CLEAR_VARS)

#LOCAL_MODULE := xmp
#LOCAL_SRC_FILES := $(SIMUSPLAYER_PATH)/../obj/local/$(TARGET_ARCH_ABI)/libxmp.a
#include $(PREBUILT_STATIC_LIBRARY)


include $(CLEAR_VARS)

LOCAL_MODULE    := simusplayer-corelib
LOCAL_SRC_FILES := modplayer.cpp
LOCAL_STATIC_LIBRARIES := xmp

LOCAL_CFLAGS += -g -Wall -fPIC -I. -I$(SIMUSPLAYER_PATH)/libxmp/include
LOCAL_LDFLAGS += -llog

ifeq ($(TARGET_ARCH),x86)
#
else
	#LOCAL_CFLAGS +=  -marm -O3 -mfpu=neon
endif 
include $(BUILD_SHARED_LIBRARY)


