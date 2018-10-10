LOCAL_PATH:= $(call my-dir)
include $(CLEAR_VARS)

LOCAL_MODULE_TAGS := optional
LOCAL_PROGUARD_ENABLED := disabled

LOCAL_STATIC_JAVA_LIBRARIES := dlna.annotation dlna.support-v4 dlna.netty dlna.htmlcleaner
# Local prebuild static jar 

# Build all java files in the src subdirectory
LOCAL_SRC_FILES := $(call all-java-files-under, src)

LOCAL_PACKAGE_NAME := DLNA
#LOCAL_CERTIFICATE := platform
#LOCAL_OVERRIDES_PACKAGES := Home

include $(BUILD_PACKAGE)
include $(CLEAR_VARS)
LOCAL_PREBUILT_STATIC_JAVA_LIBRARIES := dlna.annotation:extlib/annotations.jar dlna.support-v4:libs/android-support-v4.jar dlna.netty:libs/netty-3.6.1.Final.jar dlna.htmlcleaner:libs/htmlcleaner-2.2.jar 
include $(BUILD_MULTI_PREBUILT)
include $(call all-makefiles-under,$(LOCAL_PATH))