#ifndef JNI_NATIVE_INIT_H_
#define JNI_NATIVE_INIT_H_

#include <nativehelper/jni.h>
#include <nativehelper/JNIHelp.h>
#include <android_runtime/AndroidRuntime.h>

jboolean throw_runtime_exception(JNIEnv *e, const char*msg);
jstring jni_new_string_object(JNIEnv *e, void*p, int len, const char*coding);
JNIEnv* attach_java_thread(const char* threadName);
int jni_native_get_fd(JNIEnv *e, jobject jfd);

int register_teevee_player_natives(JNIEnv *e);
int register_section_filter_natives(JNIEnv *e);
int register_stream_selector_natives(JNIEnv *e);
int register_stream_descrambler_natives(JNIEnv *e);
int register_transport_manager_natives(JNIEnv *e);
int register_ca_manager_natives(JNIEnv *e);
int register_ca_module_manager_natives(JNIEnv *e);
int register_section_injector_natives(JNIEnv *e);
int register_stream_observer_natives(JNIEnv *e);
int register_panel_view_fragment_natives(JNIEnv *e);
int register_section_prefetcher_natives(JNIEnv *e);
int register_teevee_caputer_natives(JNIEnv *e);
int register_teevee_recorder_natives(JNIEnv *e);
int register_carousel_receiver_natives(JNIEnv *e);

#endif /* JNI_NATIVE_INIT_H_ */
