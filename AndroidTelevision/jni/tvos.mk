#
# Copyright (C) 2011 iPanel Inc.
#


LOCAL_PATH:= $(call my-dir)
include $(CLEAR_VARS)

LOCAL_MODULE_TAGS := eng

LOCAL_SRC_FILES:= \
    native_init.cpp \
    android_net_telecast_StreamSelector.cpp \
    android_net_telecast_SectionFilter.cpp \
    android_net_telecast_SectionInjector.cpp \
    android_net_telecast_StreamObserver.cpp \
    android_media_TeeveePlayer.cpp \
    android_media_TeeveeRecorder.cpp \
    android_net_telecast_TransportManager.cpp \
    android_net_telecast_ca_StreamDescrambler.cpp \
    android_net_telecast_ca_CAManager.cpp \
    android_net_telecast_ca_CAModuleManager.cpp \
    android_view_PanelViewFragment.cpp \
    android_net_telecast_SectionPrefetcher.cpp \
    android_media_TeeveeCapturer.cpp \
    android_net_telecast_dvb_CarouselReceiver.cpp \
    android_media_InportPlayer.cpp \


LOCAL_SHARED_LIBRARIES := \
    libandroidtv_native \
    libandroid_runtime \
    libnativehelper \
    libutils \
    libtvmc \
    libbinder \
    libtvsc \
    libcamc \
    libdl \
    liblog \

LOCAL_STATIC_LIBRARIES :=

LOCAL_C_INCLUDES += \
    $(TOP)/frameworks/base/include/ \
    $(TOP)/frameworks/base/native/include/ \
    $(TOP)/frameworks/tv/include/ \
    $(TOP)/frameworks/tv/native/include \
    $(JNI_H_INCLUDE)

LOCAL_C_INCLUDES += \
	external/skia/include/core \
	external/skia/include/effects \
	external/skia/include/images \
	external/skia/src/ports \
	external/skia/include/utils\
	$(TOP)/frameworks/base/core/jni

LOCAL_MODULE:= libjoin_runtime

LOCAL_PRELINK_MODULE := false

include $(BUILD_SHARED_LIBRARY)

