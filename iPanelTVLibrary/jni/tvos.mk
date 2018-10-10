
LOCAL_PATH := $(call my-dir)

include $(CLEAR_VARS)
LOCAL_MODULE_TAGS := eng
LOCAL_MODULE    := libipaneltvlibrary-jni
LOCAL_PRELINK_MODULE:=false
LOCAL_SRC_FILES := \
	ipaneltv_toolkit_Natives.c \
	ipaneltv_toolkit_dvb_CarouselParser.c \
	ipaneltv_toolkit_dvb_CarouselFilter.c \
	ipaneltv_toolkit_dvb_DvbSiEventPrefetcher.c \
	ipaneltv_toolkit_camodule_CaNativeModule.c  \	

ANDROID_INCLUDE_TOP :=

LOCAL_C_INCLUDES += \
	$(ANDROID_INCLUDE_TOP)frameworks/tv/native/include \
	$(ANDROID_INCLUDE_TOP)frameworks/tv/include \

LOCAL_SHARED_LIBRARIES += liblog libandroidtv_native libdl libcutils libutils 
LOCAL_LDLIBS += -llog -landroidtv_native -ldl

include $(BUILD_SHARED_LIBRARY)