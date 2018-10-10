#define LOG_TAG "[jni]InportPlayer"
#include <utils/Log.h>
#include <tvs/tvsdex.h>
#include <androidtv/inport_player.h>
#include <dlfcn.h>
#include <string.h>

#include "native_init.h"

using namespace android;

static struct {
	jclass clazz;
	jfieldID peer;
	jmethodID proc;
} g_inportplayer;

static jboolean native_open(JNIEnv *e, jobject thiz, jobject wo, int flags) {
	//jobject obj = NULL;
	LOGD("create inport_player init ...");

	return JNI_FALSE;
}

static void native_close(JNIEnv *e, jobject thiz) {

}

static jboolean native_set_display(JNIEnv *e, jobject thiz, int x, int t, int w, int h) {
	return JNI_FALSE;// TODO
}

static jboolean native_start(JNIEnv *e, jobject thiz) {
	return JNI_FALSE;// TODO
}

static void native_stop(JNIEnv *e, jobject thiz) {

}

static jboolean native_setsrc(JNIEnv *e, jobject thiz, int id, jstring uri, int flags) {
	return JNI_FALSE;// TODO
}

static const char* g_class_name = "android/media/InportPlayer";
static JNINativeMethod g_class_methods[] = { //
	{ "native_open", "(Ljava/lang/ref/WeakReference;I)Z", (void*) native_open },//
	{ "native_close", "()V", (void*) native_close },//
	{ "native_set_display", "(IIII)Z", (void*) native_set_display },//
	{ "native_start", "()Z", (void*) native_start },//
	{ "native_stop", "()V", (void*) native_stop },//
	{ "native_setsrc", "(ILjava/lang/String;I)Z", (void*) native_setsrc },//
};

int register_inport_player_natives(JNIEnv *e) {
	jclass cls = e->FindClass(g_class_name);
	if (cls == NULL) {
		LOGE("can't find class : %s", g_class_name);
		return -1;
	}
	g_inportplayer.clazz = (jclass) e->NewGlobalRef(cls);
	if (!(g_inportplayer.peer = e->GetFieldID(g_inportplayer.clazz, "peer", "I"))) {
		LOGE("no such int field : peer");
		return -1;
	}

	LOGI("register_inport_player_natives ok");
	return AndroidRuntime::registerNativeMethods(e, g_class_name, g_class_methods,
			NELEM(g_class_methods));
}
