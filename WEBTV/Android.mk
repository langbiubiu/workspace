LOCAL_PATH:= $(call my-dir)

vod_support_mx :=0

include $(CLEAR_VARS)

LOCAL_MODULE_TAGS := optional 

LOCAL_STATIC_JAVA_LIBRARIES := webtv_aync

LOCAL_SRC_FILES := $(call all-subdir-java-files)

LOCAL_PACKAGE_NAME := WEBTV
LOCAL_PROGUARD_ENABLED := disabled
LOCAL_CERTIFICATE := platform

LOCAL_DEX_PREOPT := false

include $(BUILD_PACKAGE)

###############################

include $(CLEAR_VARS)

LOCAL_PREBUILT_STATIC_JAVA_LIBRARIES := webtv_aync:libs/android-async-http-1.4.4.jar 

LOCAL_MODULE_TAGS := optional  
include $(BUILD_MULTI_PREBUILT)

# Use the following include to make our testapk.  
include $(call all-makefiles-under,$(LOCAL_PATH)) 
