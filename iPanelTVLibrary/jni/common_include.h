#ifndef COMMON_INCLUDE__H_
#define COMMON_INCLUDE__H_

#include <stdlib.h>
#include <stdint.h>
#include <stdio.h>
#include <string.h>
#include <unistd.h>
#include <jni.h>
#include <android/log.h>
#include <pthread.h>

#ifndef LOGD
#define LOGV(...) ((void)__android_log_print(ANDROID_LOG_VERBOSE, LOAG_TAG,  __VA_ARGS__))
#define LOGD(...) ((void)__android_log_print(ANDROID_LOG_DEBUG, LOAG_TAG,  __VA_ARGS__))
#define LOGI(...) ((void)__android_log_print(ANDROID_LOG_INFO, LOAG_TAG,  __VA_ARGS__))
#define LOGW(...) ((void)__android_log_print(ANDROID_LOG_WARN, LOAG_TAG,  __VA_ARGS__))
#define LOGE(...) ((void)__android_log_print(ANDROID_LOG_ERROR, LOAG_TAG,  __VA_ARGS__))
#endif

JNIEXPORT int Java_ipaneltv_toolkit_Natives_getfd(JNIEnv *e, jobject fd);
JNIEnv* attach_jnienv();

#endif /* COMMON_INCLUDE__H_ */
