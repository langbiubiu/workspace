LOCAL_PATH:= $(call my-dir)

include $(CLEAR_VARS)

# Local prebuild static jar 
#LOCAL_PREBUILT_STATIC_JAVA_LIBRARIES := Homed.jackson:libs/jackson-all-1.9.2.jar
#LOCAL_PREBUILT_STATIC_JAVA_LIBRARIES += Homed.weibosdk:libs/weibosdk.jar
#LOCAL_PREBUILT_STATIC_JAVA_LIBRARIES += Homed.weibosdkcore:libs/weibosdkcore.jar

#LOCAL_PREBUILT_STATIC_JAVA_LIBRARIES += JoinUILibrary.recyclerview:libs/android-support-v7-recyclerview.jar
include $(BUILD_MULTI_PREBUILT)

include $(CLEAR_VARS)

# Only compile source java files in this apk.
LOCAL_MODULE_TAGS := eng
LOCAL_PACKAGE_NAME := ChongQing_ipanelforhw

LOCAL_STATIC_JAVA_LIBRARIES :=JoinUILibrary ipaneltvlibrary JoinProtocolLibrary  \

LOCAL_SRC_FILES := $(call all-java-files-under, src)
LOCAL_SRC_FILES += $(call all-java-files-under, src-btv)
LOCAL_SRC_FILES += $(call all-java-files-under, src-live)
LOCAL_SRC_FILES += $(call all-java-files-under, src-vod)
LOCAL_SRC_FILES += $(call all-java-files-under, src-search)
LOCAL_SRC_FILES += $(call all-java-files-under, src-portal)
LOCAL_SRC_FILES += $(call all-java-files-under, src-user)
LOCAL_SRC_FILES += src-vod/com/ipanel/join/cq/settings/aidl/IDataSet.aidl
LOCAL_SRC_FILES += src-vod/com/ipanel/join/cq/vodauth/IAuthService.aidl

LOCAL_CERTIFICATE := platform
LOCAL_JAVA_LIBRARIES := libandroidtv
LOCAL_JAVACFLAGS := -encoding GBK
LOCAL_PROGUARD_ENABLED := disabled
LOCAL_AAPT_INCLUDE_ALL_RESOURCES := true
#LOCAL_PROGUARD_FLAG_FILES := proguard.flags
include $(BUILD_PACKAGE)
