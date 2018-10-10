LOCAL_PATH := $(my-dir)

########## xml for 'uses-library'
#include $(CLEAR_VARS)
#LOCAL_MODULE_TAGS := optional
#LOCAL_MODULE := libandroidtv.xml

#LOCAL_MODULE_CLASS := ETC
#LOCAL_MODULE_PATH := $(TARGET_OUT_ETC)/permissions
#LOCAL_SRC_FILES := $(LOCAL_MODULE)
#include $(BUILD_PREBUILT)

########## build .jar
#include $(CLEAR_VARS)

#LOCAL_MODULE_TAGS := optional
#LOCAL_MODULE := libandroidtv
#LOCAL_SDK_VERSION := current
#LOCAL_SRC_FILES := $(call all-java-files-under, java)
#LOCAL_SRC_FILES := $(call all-java-files-under, gen)
#LOCAL_SRC_FILES += \
#	java/android/net/telecast/INetworkServiceManagerCallback.aidl \
#	java/android/net/telecast/INetworkServiceManager.aidl \


########## build .so and so on
include $(CLEAR_VARS)
ifneq ($(TARGET_SIMULATOR),true)
  $(warning sdk-only1: javac available.)
  include $(call first-makefiles-under,$(LOCAL_PATH))
else
  $(warning sdk-only2: javac available.)
  include $(addprefix $(LOCAL_PATH)/,$(addsuffix /Android.mk, \
	      libjoin_runtime \
	   ))
endif

