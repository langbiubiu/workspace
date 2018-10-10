#define LOG_TAG "[jni]StreamObserver"
#include <utils/Log.h>
#include <tvs/tvsdex.h>
#include <dlfcn.h>
#include <string.h>

#include "native_init.h"
#include <androidtv/stream_observer.h>

using namespace android;

static struct observer_info{
	jclass clazz;
	jmethodID callback;
	jfieldID peer;
} g_obser;

jobject nativeNewNI(JNIEnv *e, ATransportInterfaceInfo*info);

static void my_AStreamObserverCallback(AStreamObserver*m, void*o, int64_t f, int what, int p1,
		void*p2) {
	JNIEnv *e = attach_java_thread("observer-callback");
	jobject wo = (jobject) o;
	if (o == NULL)
		return;
	switch (what) {
	case ASTREAMOBSERVER_CB_CLOSED:
		e->CallStaticVoidMethod(g_obser.clazz, g_obser.callback, wo, 0, 0, 0, 0);
		e->DeleteGlobalRef(wo);
		break;
	case ASTREAMOBSERVER_CB_STREAM_PRESENT:
		e->CallStaticVoidMethod(g_obser.clazz, g_obser.callback, wo, f, 1, p1, (int)p2);
		break;
	case ASTREAMOBSERVER_CB_STREAM_ABSENT:
		e->CallStaticVoidMethod(g_obser.clazz, g_obser.callback, wo, f, 2, 0, 0);
		break;
	default:
		LOGE("my_AStreamObserverCallback > invalid msg = %d.", what);
		break;
	}
}

static AStreamObserver* jniGetSectionObserverPeer(JNIEnv *e, jobject thiz) {
	AStreamObserver*p = (AStreamObserver*) e->GetIntField(thiz, g_obser.peer);
	if (p == NULL) {
		LOGD("StreamObserver peer is null");
		throw_runtime_exception(e, "peer is null");
	}
	return p;
}

extern "C" AStreamObserver* JavaStreamObserver_getNativePeer(JNIEnv *e, jobject thiz) {
	return (AStreamObserver*) e->GetIntField(thiz, g_obser.peer);
}

static jboolean native_open(JNIEnv *e, jobject thiz, jobject wo, jlong most, jlong least, jint flags) {
	AUUID uuid ;
	jobject obj = NULL;
	AStreamObserver*tm = NULL;
	if (wo == NULL)
		return throw_runtime_exception(e, "null pointer");
	if ((obj = e->NewGlobalRef(wo)) == NULL)
		return throw_runtime_exception(e, "null pointer2");
	AUUID_fromMostLeast(&uuid, most, least);
	if ((tm = AStreamObserver_new(&uuid, flags, obj, my_AStreamObserverCallback)) == NULL) {
		if (obj != NULL)
			e->DeleteGlobalRef(obj);
		return throw_runtime_exception(e, "create native observer failed");
	}
	e->SetIntField(thiz, g_obser.peer, (int) tm);
	return JNI_TRUE;
}

static void native_close(JNIEnv *e, jobject thiz) {
	AStreamObserver*peer = jniGetSectionObserverPeer(e, thiz);
	if (peer != NULL) {
		AStreamObserver_delete(peer);
		e->SetIntField(thiz, g_obser.peer, 0);
	}
}

static jboolean native_query(JNIEnv *e, jobject thiz, jint flags) {
	AStreamObserver*peer = jniGetSectionObserverPeer(e, thiz);
	if (peer == NULL)
		return JNI_FALSE;
	return AStreamObserver_query2(peer, flags) == 0 ? JNI_TRUE : JNI_FALSE;
}

//--------
static const char* g_class_name = "android/net/telecast/StreamObserver";

static JNINativeMethod g_cls_methods[] = { //
	{ "native_open", "(Ljava/lang/ref/WeakReference;JJI)Z", (void*) native_open },//
	{ "native_close", "()V", (void*) native_close },//
	{ "native_query", "(I)Z", (void*) native_query },//
};

int register_stream_observer_natives(JNIEnv *e) {
	jclass cls;
	if ((cls = e->FindClass(g_class_name)) == NULL) {
		LOGE("can't find class : %s", g_class_name);
		return -1;
	}
	g_obser.clazz = (jclass) e->NewGlobalRef(cls);
	if ((g_obser.callback = e->GetStaticMethodID(cls, "nativeCallback",
			"(Ljava/lang/Object;JIII)V")) == NULL) {
		LOGE("can't find method callback of class: %s", g_class_name);
		return -1;
	}
	if ((g_obser.peer = e->GetFieldID(cls, "peer", "I")) == NULL) {
		LOGE("can't find ield peer of class: %s", g_class_name);
		return -1;
	}
	return AndroidRuntime::registerNativeMethods(e, g_class_name, g_cls_methods,
			NELEM(g_cls_methods));
}
