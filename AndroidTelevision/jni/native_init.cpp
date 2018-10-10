#define LOG_TAG "[jni]television_init"
#include <utils/Log.h>
#include <tvs/tvsdex.h>

#include "native_init.h"

using namespace android;

static struct {
	jclass clazz;
	jmethodID init;
} g_str;
static JavaVM*g_vm = NULL;

jstring jni_new_string_object(JNIEnv *e, void*p, int len, const char*c) {
	jstring code;
	jbyteArray jba = e->NewByteArray(len);
	if ((jba = e->NewByteArray(len))) {
		if ((code = e->NewStringUTF(c))) {
			e->SetByteArrayRegion(jba, 0, len, (jbyte*) p);
			return (jstring) e->NewObject(g_str.clazz, g_str.init, jba, code);
		}
	}
	return NULL;
}

jboolean throw_runtime_exception(JNIEnv *e, const char*msg) {
	jniThrowException(e, "java/lang/RuntimeException", msg);
	return JNI_FALSE;
}

static jboolean string_common_load(JNIEnv* env) {
	jclass cls;
	if (env == NULL)
		return JNI_FALSE;
	if (!(cls = env->FindClass("java/lang/String")))
		return JNI_FALSE;
	g_str.clazz = (jclass) env->NewGlobalRef(cls);
	if (!(g_str.init = env->GetMethodID(g_str.clazz, "<init>", "([BLjava/lang/String;)V")))
		return JNI_FALSE;
	return JNI_TRUE;
}

JNIEnv* attach_java_thread(const char* threadName) {
	JavaVMAttachArgs args;
	jint result;
	JNIEnv*e = NULL;
	threadName = "tv-native";//override
	args.version = JNI_VERSION_1_4;
	args.name = (char*) threadName;
	args.group = NULL;
	if ((result = g_vm->AttachCurrentThread(&e, (void*) &args)) != JNI_OK) {
		LOGE("NOTE: attach of thread '%s' failed\n", threadName);
		return NULL;
	}
	return e;
}

int jni_native_get_fd(JNIEnv *e, jobject jfd) {
	int fd = -1;
	if (jfd == NULL) {
		LOGD("get_fd>empty FileDescriptor");
		return -1;
	}

	if ((fd = jniGetFDFromFileDescriptor(e, jfd)) < 0) {
		LOGE("get_fd>invalid FileDescriptor");
		return -1;
	}

	LOGD("jni_get_fd > fd = %d", fd);
	return fd;
}

//------------

jint JNI_OnLoad(JavaVM* vm, void* reserved) {
	JNIEnv* env = NULL;
	jint result = -1, step = 0;
	g_vm = vm;
	step = 1;//LOGI("JNI_OnLoad> 1 ");
	if (vm->GetEnv((void**) &env, JNI_VERSION_1_4) != JNI_OK)
		goto bail;
	if (!string_common_load(env))
		goto bail;

	step = 2;//LOGI("JNI_OnLoad> 2 ");
	if (tvsd_ensure_service()) {
		LOGE("TVMS tvsd_ensure_service failed");
		goto bail;
	}

	step = 3;//LOGI("JNI_OnLoad> 3 ");
	if (register_transport_manager_natives(env) < 0) {
		LOGE("register_transport_manager_natives failed\n");
		goto bail;
	}

	step = 4;//LOGI("JNI_OnLoad> 4 ");
	if (register_stream_selector_natives(env) < 0) {
		LOGE("register_stream_selector_natives failed\n");
		goto bail;
	}

	step = 5;//LOGI("JNI_OnLoad> 5");
	if (register_teevee_player_natives(env) < 0) {
		LOGE("register_teevee_player_natives failed\n");
		goto bail;
	}

	step = 6;//LOGI("JNI_OnLoad> 6");
	if (register_section_filter_natives(env) < 0) {
		LOGE("register_section_filter_natives failed\n");
		goto bail;
	}

	step = 7;//LOGI("JNI_OnLoad> 7");
	if (register_stream_descrambler_natives(env) < 0) {
		LOGE("register_stream_descrambler_natives failed\n");
		goto bail;
	}

	step = 8;//LOGI("JNI_OnLoad> 8");
	if (register_ca_manager_natives(env) < 0) {
		LOGE("register_ca_manager_natives failed\n");
		goto bail;
	}

	step = 9;//LOGI("JNI_OnLoad> 9");
	if (register_ca_module_manager_natives(env) < 0) {
		LOGE("register_ca_module_manager_natives failed\n");
		goto bail;
	}

	step = 10;//LOGI("JNI_OnLoad> 10");
	if (register_section_injector_natives(env) < 0) {
		LOGE("register_section_injector_natives failed\n");
		goto bail;
	}

	step = 11;//LOGI("JNI_OnLoad> 11");
	if (register_stream_observer_natives(env) < 0) {
		LOGE("register_stream_observer_natives failed\n");
		goto bail;
	}
	
	step = 12;//LOGI("JNI_OnLoad> 12");
	if (register_panel_view_fragment_natives(env) < 0) {
		LOGE("register_panel_view_fragment_natives failed\n");
		goto bail;
	}
	
	step = 13;//LOGI("JNI_OnLoad> 13");
	if (register_section_prefetcher_natives(env) < 0) {
		LOGE("register_section_prefetcher_natives failed\n");
		goto bail;
	}

	step = 14;////LOGI("JNI_OnLoad> 14");
	if (register_teevee_caputer_natives(env) < 0) {
		LOGE("register_teevee_caputer_natives failed\n");
		goto bail;
	}

	step = 15;////LOGI("JNI_OnLoad> 15");
	if (register_teevee_recorder_natives(env) < 0) {
		LOGE("register_teevee_recorder_natives failed\n");
		goto bail;
	}

	step = 16;////LOGI("JNI_OnLoad> 16");
	if (register_carousel_receiver_natives(env) < 0) {
		LOGE("register_carousel_receiver_natives failed\n");
		goto bail;
	}
	result = JNI_VERSION_1_4;
	bail: if (result == -1) {
		LOGI("JNI_OnLoad> failed by :%d", step);
	} else {
		LOGI("JNI_OnLoad> succeed!");
	}
	return result;
}
