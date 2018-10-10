# Copyright 2011, The Android Open Source Project
#
# Licensed under the Apache License, Version 2.0 (the "License");
# you may not use this file except in compliance with the License.
# You may obtain a copy of the License at
#
#     http://www.apache.org/licenses/LICENSE-2.0
#
# Unless required by applicable law or agreed to in writing, software
# distributed under the License is distributed on an "AS IS" BASIS,
# WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
# See the License for the specific language governing permissions and
# limitations under the License.

LOCAL_PATH := $(call my-dir)

include $(CLEAR_VARS)

LOCAL_MODULE := JoinUILibrary
#LOCAL_MODULE_TAGS := optional
LOCAL_MODULE_TAGS := eng

LOCAL_STATIC_JAVA_LIBRARIES := JoinUILibrary.annotations JoinUILibrary.support-v4 JoinUILibrary.otto JoinUILibrary.ftp JoinUILibrary.nineold
LOCAL_STATIC_JAVA_LIBRARIES += JoinUILibrary.zip
LOCAL_STATIC_JAVA_LIBRARIES += JoinUILibrary.httpclient
#LOCAL_STATIC_JAVA_LIBRARIES += JoinUILibrary.recyclerview

LOCAL_SRC_FILES := $(call all-java-files-under, src)
LOCAL_JAVACFLAGS := -encoding GBK 
#LOCAL_SDK_VERSION := current

include $(BUILD_STATIC_JAVA_LIBRARY)

include $(CLEAR_VARS)

# Local prebuild static jar 
LOCAL_PREBUILT_STATIC_JAVA_LIBRARIES := JoinUILibrary.annotations:extlib/annotations.jar JoinUILibrary.support-v4:libs/android-support-v4.jar JoinUILibrary.otto:libs/otto-1.3.4.jar JoinUILibrary.ftp:libs/commons-net-3.3.jar JoinUILibrary.nineold:libs/nineoldandroids-2.4.0.jar
LOCAL_PREBUILT_STATIC_JAVA_LIBRARIES += JoinUILibrary.zip:libs/zip4j_1.3.1.jar
LOCAL_PREBUILT_STATIC_JAVA_LIBRARIES += JoinUILibrary.httpclient:libs/commons-httpclient-3.1.jar
#LOCAL_PREBUILT_STATIC_JAVA_LIBRARIES += JoinUILibrary.recyclerview:libs/android-support-v7-recyclerview.jar
include $(BUILD_MULTI_PREBUILT)
