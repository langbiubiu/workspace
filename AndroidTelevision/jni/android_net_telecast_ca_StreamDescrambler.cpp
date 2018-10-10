#define LOG_TAG "[jni]StreamDescrambler"
#include <utils/Log.h>

#include <tvs/tvsdex.h>
#include <ipanel/section_reader.h>
#include <dlfcn.h>
#include <string.h>
#include <androidtv/stream_descrambler.h>

#include "native_init.h"

using namespace android;

#define STR_CLOSED "stream descrambler has been closed"

static struct {
	jclass clazz;
	jfieldID peer;
	jmethodID proc;
} g_desc;

static void JNI_StreamDescramblerCallback(AStreamDescrambler*sd, void*o, int msg, void* p1, void*p2) {
	JNIEnv *env = attach_java_thread("stream-descrambler");
	jobject wo = (jobject) o;
	jstring juri = NULL, jmsg = NULL;
	LOGD("JNI_StreamDescramblerCallback msg = %d",msg);
	switch (msg) {
	case ASTREAMDESCRAMBLER_CB_DESCRAMBLING_TERMINATED:
		if (p1 != NULL)
			jmsg = env->NewStringUTF((char*) p1);
		env->CallStaticVoidMethod(g_desc.clazz, g_desc.proc, wo, 1, jmsg, NULL);
		if (jmsg != NULL)
			env->DeleteLocalRef(jmsg);
		break;
	case ASTREAMDESCRAMBLER_CB_DESCRAMBLING_ERROR:
		jmsg = p1 != NULL ? env->NewStringUTF((char*) p1) : NULL;
		juri = p2 != NULL ? env->NewStringUTF((char*) p2) : NULL;
		env->CallStaticVoidMethod(g_desc.clazz, g_desc.proc, wo, 2, jmsg, juri);
		if (jmsg != NULL)
			env->DeleteLocalRef(jmsg);
		if (juri != NULL)
			env->DeleteLocalRef(juri);
		break;
	case ASTREAMDESCRAMBLER_CB_DESCRAMBLING_RESUMED:
		env->CallStaticVoidMethod(g_desc.clazz, g_desc.proc, wo, 3, NULL, NULL);
		break;
	case ASTREAMDESCRAMBLER_CB_NETWORK_CA_CHANGE:
		env->CallStaticVoidMethod(g_desc.clazz, g_desc.proc, wo, 4, NULL, NULL);
		break;
	case ASTREAMDESCRAMBLER_CB_DESCRAMBLING_START:
		env->CallStaticVoidMethod(g_desc.clazz, g_desc.proc, wo, 5, NULL, NULL);
		break;
	case MSG_HANDLE_CLOSED:
		env->CallStaticVoidMethod(g_desc.clazz, g_desc.proc, wo, 0, NULL, NULL);
		env->DeleteGlobalRef(wo);
		break;
	default:
		LOGW("JNI_StreamDescramblerCallback msg invalid");
		break;
	}
}

static AStreamDescrambler* jniGetStreamDescramblerPeer(JNIEnv *e, jobject thiz) {
	AStreamDescrambler*p = (AStreamDescrambler*) e->GetIntField(thiz, g_desc.peer);
	if (p == NULL) {
		LOGD("StreamDescrambler peer is null");
		throw_runtime_exception(e, "peer is null");
	}
	return p;
}

extern "C" AStreamDescrambler* JavaStreamDescrambler_getNativePeer(JNIEnv *e, jobject thiz) {
	return (AStreamDescrambler*) e->GetIntField(thiz, g_desc.peer);
}

static jboolean native_open(JNIEnv *e, jobject thiz, jobject wo, jlong idm, jlong idl) {
	jobject obj = NULL;
	AUUID id ;
	AStreamDescrambler* d = NULL;
	if (wo == NULL) {
		return throw_runtime_exception(e, "null pointer");
	}
	if ((obj = e->NewGlobalRef(wo)) == NULL) {
		return throw_runtime_exception(e, "null pointer2");
	}
	AUUID_fromMostLeast(&id, idm, idl);
	if ((d = AStreamDescrambler_new(&id, obj, JNI_StreamDescramblerCallback)) == NULL) {
		if (obj != NULL)
			e->DeleteGlobalRef(obj);
		return throw_runtime_exception(e, "can't open stream descrambler");
	}
	e->SetIntField(thiz, g_desc.peer, (int) d);
	return JNI_TRUE;
}

static void native_close(JNIEnv *e, jobject thiz) {
	AStreamDescrambler* peer = jniGetStreamDescramblerPeer(e, thiz);
	if (peer) {
		AStreamDescrambler_delete(peer);
		e->SetIntField(thiz, g_desc.peer, 0);
	}
}

static jboolean native_start(JNIEnv *e, jobject thiz, jlong freq, int pn, int streampid,
		int ecmpid, int flags) {
	AStreamDescrambler* peer = jniGetStreamDescramblerPeer(e, thiz);
	if (peer == NULL)
		return JNI_FALSE;
	return AStreamDescrambler_start(peer, freq, pn, streampid, ecmpid, flags) == 0 ? JNI_TRUE
			: JNI_FALSE;
}

static void native_stop(JNIEnv *e, jobject thiz) {
	AStreamDescrambler* peer = jniGetStreamDescramblerPeer(e, thiz);
	if (peer == NULL)
		return;
	AStreamDescrambler_stop(peer);
}

static jint native_get_camodid(JNIEnv *e, jobject thiz) {
	AStreamDescrambler* peer = jniGetStreamDescramblerPeer(e, thiz);
	if (peer == NULL)
		return -1;
	return AStreamDescrambler_getCAModuleID(peer);
}

static jint native_set_camodid(JNIEnv *e, jobject thiz, jint mid) {
	AStreamDescrambler* peer = jniGetStreamDescramblerPeer(e, thiz);
	if (peer == NULL)
		return -1;
	return AStreamDescrambler_setCAModuleID(peer, mid);
}

static void native_enter(JNIEnv *e, jobject thiz, jstring uri) {
	const char*s = NULL;
	AStreamDescrambler* peer = jniGetStreamDescramblerPeer(e, thiz);
	if (peer == NULL)
		return;
	s = uri != NULL ? e->GetStringUTFChars(uri, NULL) : NULL;
	AStreamDescrambler_enterApplication(peer, s);
	if (s != NULL)
		e->ReleaseStringUTFChars(uri, s);
}

static jint native_set_brk(JNIEnv *e, jobject thiz, jboolean b) {
	AStreamDescrambler* peer = jniGetStreamDescramblerPeer(e, thiz);
	if (peer == NULL)
		return -1;
	AStreamDescrambler_setBreakable(peer, b);
	return 0;
}


// ----------------------------------------------------------------------------
static const char* g_class_name = "android/net/telecast/ca/StreamDescrambler";
static JNINativeMethod g_class_methods[] = { //
	{ "native_open", "(Ljava/lang/ref/WeakReference;JJ)Z", (void*) native_open },//
	{ "native_close", "()V", (void*) native_close },//
	{ "native_get_camodid", "()I", (void*) native_get_camodid },//
	{ "native_set_camodid", "(I)I", (void*) native_set_camodid },//
	{ "native_enter", "(Ljava/lang/String;)V", (void*) native_enter },//
	{ "native_start", "(JIIII)Z", (void*) native_start },//
	{ "native_stop", "()V", (void*) native_stop },//
	{ "native_set_brk", "(Z)I", (void*) native_set_brk }//
};

//do not throw any exception in this fun,return <0 for err
int register_stream_descrambler_natives(JNIEnv *e) {
	jclass cls;
	if ((cls = e->FindClass(g_class_name)) == NULL) {
		LOGE("Can't find class : %s", g_class_name);
		return -1;
	}
	g_desc.clazz = (jclass) e->NewGlobalRef(cls);
	if ((g_desc.proc = e->GetStaticMethodID(g_desc.clazz, "native_callback",
			"(Ljava/lang/Object;ILjava/lang/String;Ljava/lang/String;)V"))
			== NULL) {
		LOGE("no such method: native_callback");
		return -1;
	}
	if ((g_desc.peer = e->GetFieldID(g_desc.clazz, "peer", "I")) == NULL) {
		LOGE("no such field: peer");
		return -1;
	}
	LOGD("register_stream_descrambler_natives>4");
	return AndroidRuntime::registerNativeMethods(e, g_class_name,
			g_class_methods, NELEM(g_class_methods));
}

