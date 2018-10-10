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

LOCAL_MODULE := JoinProtocolLibrary
LOCAL_MODULE_TAGS := optional

LOCAL_STATIC_JAVA_LIBRARIES := JoinUILibrary protocol.gson protocol.simplexml
 
LOCAL_SRC_FILES := $(call all-java-files-under, src)
LOCAL_JAVACFLAGS := -encoding GBK 
#LOCAL_SDK_VERSION := current

include $(BUILD_STATIC_JAVA_LIBRARY)

include $(CLEAR_VARS)
# Local prebuild static jar 
LOCAL_PREBUILT_STATIC_JAVA_LIBRARIES := protocol.gson:libs/gson-2.2.4.jar protocol.simplexml:libs/simple-xml-2.7.1.jar
include $(BUILD_MULTI_PREBUILT)
