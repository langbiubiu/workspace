#define LOG_TAG "[jni]SectionInjector"
#include <utils/Log.h>
#include <dlfcn.h>
#include <string.h>
#include <androidtv/section_injector.h>
#include "native_init.h"


using namespace android;

static struct injector_info {
	jclass clazz;
	jfieldID peer;
	jfieldID buffer;
} g_injector;

static ASectionInjector* jniGetSectionInjectorPeer(JNIEnv *e, jobject thiz) {
	ASectionInjector*p = (ASectionInjector*) e->GetIntField(thiz, g_injector.peer);
	if (p == NULL) {
		LOGD("SectionInjector peer is null");
		throw_runtime_exception(e, "peer is null");
	}
	return p;
}

extern "C" ASectionInjector* JavaSectionInjector_getNativePeer(JNIEnv *e, jobject thiz) {
	return (ASectionInjector*) e->GetIntField(thiz, g_injector.peer);
}

static jboolean native_open(JNIEnv *e, jobject thiz, jlong idm, jlong idl,
		jboolean bmain) {
	ASectionInjector *f = NULL;
	AUUID uuid;
	if (bmain == JNI_TRUE) {
		AUUID_fromMostLeast(&uuid, idm, idl);
		if ((f = ASectionInjector_new(&uuid)) == NULL) {
			return throw_runtime_exception(e, "can't open injector");
		}
		e->SetIntField(thiz, g_injector.peer, (int) f);
	} else {
		if ((f = (ASectionInjector *) e->GetIntField(thiz, g_injector.peer)) == NULL) {
			return throw_runtime_exception(e, "invalid main injector");
		}
		int sid = ASectionInjector_addSource(f, 0);
		if (sid <= 0) {
			return throw_runtime_exception(e, "open task injector failed!");
		}
		e->SetIntField(thiz, g_injector.buffer, (int) sid);
	}
	return JNI_TRUE;
}

static void native_release(JNIEnv *e, jobject thiz) {
	ASectionInjector*peer = jniGetSectionInjectorPeer(e, thiz);
	if (peer == NULL)
		return;
	int sid = e->GetIntField(thiz, g_injector.buffer);
	if (sid == 0) {
		ASectionInjector_delete(peer);
		e->SetIntField(thiz, g_injector.peer, 0);
	} else {
		ASectionInjector_removeSource(peer, sid);
	}
}

static jboolean native_start(JNIEnv *e, jobject thiz) {
	ASectionInjector*peer = jniGetSectionInjectorPeer(e, thiz);
	if (peer == NULL)
		return JNI_FALSE;
	int sid = e->GetIntField(thiz, g_injector.buffer);
	if (sid == 0) //only main injector need stop
		return ASectionInjector_start(peer, 0) == 0 ? JNI_TRUE : JNI_FALSE;
	return JNI_FALSE;
}

static void native_stop(JNIEnv *e, jobject thiz) {
	ASectionInjector*peer = jniGetSectionInjectorPeer(e, thiz);
	if (peer == NULL)
		return;
	int sid = e->GetIntField(thiz, g_injector.buffer);
	if (sid == 0) //only main injector need stop
		ASectionInjector_stop(peer);
}

static jboolean native_set_sec(JNIEnv *e, jobject thiz, jlong freq, jint pid, jbyteArray jb) {
	int ret = -1, len;
	ASectionInjector*peer = jniGetSectionInjectorPeer(e, thiz);
	if (peer == NULL)
		return JNI_FALSE;
	int sid = e->GetIntField(thiz, g_injector.buffer);
	jbyte buf[512], *p = buf;
	if (jb == NULL)
		return throw_runtime_exception(e, "buffer is null");
	if ((len = e->GetArrayLength(jb)) <= 4)
		return throw_runtime_exception(e, "invalid section len");
	if (sid == 0)
		return throw_runtime_exception(e, "invalid injector for set section");
	if (len > 512) {
		if ((p = (jbyte*) malloc(len)) == NULL)
			return throw_runtime_exception(e, "out of memory");
	}
	e->GetByteArrayRegion(jb, 0, len, p);
	ret = ASectionInjector_setSource(peer, sid, freq, pid, (uint8_t*) p, len);
	if (p != buf)
		free(p);
	return ret == 0 ? JNI_TRUE : JNI_FALSE;
}

static jboolean native_inject(JNIEnv *e, jobject thiz) {
	int ret = -1;
	ASectionInjector*peer = jniGetSectionInjectorPeer(e, thiz);
	if (peer == NULL)
		return JNI_FALSE;
	int sid = e->GetIntField(thiz, g_injector.buffer);
	if (sid > 0) //only task injector need inject
		ret = ASectionInjector_sendSource(peer, sid);
	return ret == 0 ? JNI_TRUE : JNI_FALSE;
}

// ----------------------------------------------------------------------------
static const char* g_class_name = "android/net/telecast/SectionInjector";
static JNINativeMethod g_class_methods[] = { //
	{ "native_open", "(JJZ)Z", (void*) native_open },//
	{ "native_release", "()V", (void*) native_release },//
	{ "native_set_sec", "(JI[B)Z", (void*) native_set_sec },//
	{ "native_start", "()Z", (void*) native_start },//
	{ "native_stop", "()V", (void*) native_stop },//
	{ "native_inject", "()Z", (void*) native_inject }//
};

//do not throw any exception in this fun
int register_section_injector_natives(JNIEnv *e) {
	jclass cls;
	if ((cls = e->FindClass(g_class_name)) == NULL) {
		LOGE("Can't find class : %s", g_class_name);
		return -1;
	}
	g_injector.clazz = (jclass) e->NewGlobalRef(cls);
	if ((g_injector.peer = e->GetFieldID(cls, "peer", "I")) == NULL) {
		LOGE("no such field: peer");
		return -1;
	}
	if ((g_injector.buffer = e->GetFieldID(cls, "buffer", "I")) == NULL) {
		LOGE("no such field: buffer");
		return -1;
	}
	return AndroidRuntime::registerNativeMethods(e, g_class_name, g_class_methods,
			NELEM(g_class_methods));
}


