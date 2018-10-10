LOCAL_PATH := $(call my-dir)

# the library
# ============================================================
include $(CLEAR_VARS)

LOCAL_MODULE:= libandroidtv
LOCAL_MODULE_TAGS := eng 

LOCAL_SRC_FILES := \
	$(call all-subdir-java-files)

LOCAL_SRC_FILES += \
	$(call all-java-files-under, ../gen)
	
$(warning LOCAL_SRC_FILES:$(LOCAL_SRC_FILES))
					
LOCAL_SRC_FILES += \
	android/net/telecast/INetworkServiceManagerCallback.aidl \
	android/net/telecast/INetworkServiceManager.aidl \

LOCAL_STATIC_JAVA_LIBRARIES := libandroidtvext
	
include $(BUILD_JAVA_LIBRARY)


# ====  libandroidtv.xml lib def  ========================
include $(CLEAR_VARS)

LOCAL_MODULE := libandroidtv.xml
LOCAL_MODULE_TAGS := eng 

LOCAL_MODULE_CLASS := ETC

# This will install the file in /system/etc/permissions
#
LOCAL_MODULE_PATH := $(TARGET_OUT_ETC)/permissions

LOCAL_SRC_FILES := $(LOCAL_MODULE)

include $(BUILD_PREBUILT)



include $(CLEAR_VARS)

LOCAL_PREBUILT_STATIC_JAVA_LIBRARIES := libandroidtvext:../libs/libandroidtvext.jar

include $(BUILD_MULTI_PREBUILT)