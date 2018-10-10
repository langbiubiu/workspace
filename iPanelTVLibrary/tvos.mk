LOCAL_PATH:= $(call my-dir)

# Static library with some common classes for the phone apps.
# To use it add this line in your Android.mk
#  LOCAL_STATIC_JAVA_LIBRARIES := com.android.phone.common
include $(CLEAR_VARS)

LOCAL_MODULE := ipaneltvlibrary

LOCAL_MODULE_TAGS := eng

LOCAL_JAVA_LIBRARIES := libandroidtv

LOCAL_SRC_FILES := \
		src/ipaneltv/toolkit/IJsonChannelCallback.aidl \
		src/ipaneltv/toolkit/IJsonChannelService.aidl \
		src/ipaneltv/toolkit/IJsonChannelSession.aidl \
    src/ipaneltv/toolkit/dsmcc/IDsmccCallback.aidl \
		src/ipaneltv/toolkit/dsmcc/IDsmccDownloader.aidl \
		src/ipaneltv/toolkit/parentslock/IParentLockDateListener.aidl \
		src/ipaneltv/toolkit/parentslock/IRemoteService.aidl \
		src/ngbj/ipanel/player/iNgbPlayerListener.aidl \
		src/ngbj/ipanel/player/iNgbPlayer.aidl \
		src/ngbj/ipaneltv/dvb/IDvbSearchListener.aidl \
		src/ngbj/ipaneltv/dvb/INgbJScanEitListener.aidl \
		src/ngbj/ipaneltv/dvb/INgbJScanListener.aidl \
		src/ngbj/ipaneltv/dvb/INgbJScanManager.aidl \
		src/ngbj/ipaneltv/dvb/ISelectListener.aidl \

LOCAL_SRC_FILES += $(call all-java-files-under, src)

include $(BUILD_STATIC_JAVA_LIBRARY)

# Build the test package
include $(call all-makefiles-under,$(LOCAL_PATH))
