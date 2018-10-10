#define LOG_TAG "[jni]CAModuleManager"
#include <utils/Log.h>
#include <tvs/tvsdex.h>
#include <androidtv/camodule_manager.h>
#include <dlfcn.h>
#include <string.h>

#include "native_init.h"

using namespace android;

static struct {
	jclass clazz;
	jfieldID peer;
	jmethodID cbf;
} g_camm;

struct sec_buf {
	int len;
	char *p;
};

static void jniCAModuleManagerCallback(void *o, int msg, jint p1, jlong p2,
		jint p3, jint p4,
		jint p5, jint p6, jobject p7) {
	JNIEnv *env = attach_java_thread("camodule-manager");
	jobject wo = (jobject) o;
	assert(wo);
	env->CallStaticVoidMethod(g_camm.clazz, g_camm.cbf, wo, msg, p1, p2, p3, p4, p5, p6, p7);
	if (msg == 0) { // CLOSED MESSAGE
		env->DeleteGlobalRef(wo);
	}
}

static void my_ACAModuleInterface(ACAModuleManager*camm, void*o, int msg,
		int p1, void*p2) {
	JNIEnv *env = attach_java_thread("camodule-manager");
	switch (msg) {
	case ACAMODULEMANAGER_CB_ON_PRESEND:
		jniCAModuleManagerCallback(o, 1, p1, 0, 0, 0, 0, 0, NULL);
		break;
	case ACAMODULEMANAGER_CB_ON_ABSENT:
		jniCAModuleManagerCallback(o, 2, 0, 0, 0, 0, 0, 0, NULL);
		break;
	case ACAMODULEMANAGER_CB_START_DESCRAMBLING: {
		ADescramblingParam*p = (ADescramblingParam*) p2;
		assert(p);
		jniCAModuleManagerCallback(o, 3, p1, p->freq, p->program_number, p->stream_pid, p->ecmpid,
				p->flags, NULL);
		break;
	}
	case ACAMODULEMANAGER_CB_STOP_DESCRAMBLING:
		jniCAModuleManagerCallback(o, 4, p1, 0, 0, 0, 0, 0, NULL);
		break;
	case ACAMODULEMANAGER_CB_DESCRAMBLING_ECM: {
		struct sec_buf*buf = (struct sec_buf*) p2;
		jbyteArray eb = env->NewByteArray(buf->len);
		if (eb && buf->len > 0) {
			env->SetByteArrayRegion(eb, 0, buf->len, (jbyte*) buf->p);
			jniCAModuleManagerCallback(o, 5, p1, 0, 0, 0, 0, 0, eb);
		}
		break;
	}
	case ACAMODULEMANAGER_CB_BUY_ENTITLEMENT: {
		jstring uri = p2 != NULL ? env->NewStringUTF((char*) p2) : NULL;
		jniCAModuleManagerCallback(o, 6, 0, 0, p1, 0, 0, 0, uri);
		if (uri != NULL)
			env->DeleteLocalRef(uri);
		break;
	}
	case ACAMODULEMANAGER_CB_ENTER_APPLICATION: {
		jstring uri = p2 != NULL ? env->NewStringUTF((char*) p2) : NULL;
		jniCAModuleManagerCallback(o, 7, 0, 0, 0, 0, 0, 0, uri);
		if (uri != NULL)
			env->DeleteLocalRef(uri);
		break;
	}
	case ACAMODULEMANAGER_CB_REMOTE_SESSION_FD: {
		jniCAModuleManagerCallback(o, 8, p1, 0, 0, 0, 0, 0, NULL);
		break;
	}
	case MSG_HANDLE_CLOSED:
		jniCAModuleManagerCallback(o, 0, 0, 0, 0, 0, 0, 0, 0);
		break;
	}
}

static ACAModuleManager* jniGetCAModuleManagerPeer(JNIEnv *e, jobject thiz) {
	ACAModuleManager*p = (ACAModuleManager*) e->GetIntField(thiz, g_camm.peer);
	if (p == NULL) {
		LOGD("CAModuleManager peer is null");
		throw_runtime_exception(e, "peer is null");
	}
	return p;
}

extern "C" ACAModuleManager* JavaCAModuleManager_getNativePeer(JNIEnv *e, jobject thiz) {
	return (ACAModuleManager*) e->GetIntField(thiz, g_camm.peer);
}

static void native_create(JNIEnv *e, jobject thiz, jlong most, jlong least, jint cs) {
	AUUID id;
	if (e->GetIntField(thiz, g_camm.peer) != 0) {
		throw_runtime_exception(e, "peer has been initd");
		return;
	}
	AUUID_fromMostLeast(&id,most,least);
	ACAModuleManager*m = ACAModuleManager_new(&id, cs);
	if (m == NULL) {
		throw_runtime_exception(e, "create camodule manager failed");
		return;
	}

	e->SetIntField(thiz, g_camm.peer, (int) m);
}

static void native_release(JNIEnv *e, jobject thiz) {
	ACAModuleManager* peer = jniGetCAModuleManagerPeer(e, thiz);
	if (peer) {
		ACAModuleManager_delete(peer);
		e->SetIntField(thiz, g_camm.peer, 0);
	}
}

static jint native_setif(JNIEnv *e, jobject thiz, jobject wo, jintArray jids) {
	jobject obj = NULL;
	int ids[32] = { 0 }, ret = -1;
	ACAModuleManager* peer = jniGetCAModuleManagerPeer(e, thiz);
	if (peer == NULL)
		return -1;
	if (wo == NULL) {
		throw_runtime_exception(e, "null pointer");
		goto FAIL;
	}
	if ((obj = e->NewGlobalRef(wo)) == NULL) {
		throw_runtime_exception(e, "out of memory");
		goto FAIL;
	}
	if (jids) {
		int idlen = e->GetArrayLength(jids);
		idlen = idlen > 32 ? 32 : idlen;
		e->GetIntArrayRegion(jids, 0, idlen, ids);
		if (ACAModuleManager_setCASystemIDs(peer, ids, idlen) != 0) {
			LOGW("setCASystemIDs> failed!");
		}
	}
	if ((ret = ACAModuleManager_setModuleInterface(peer, obj, my_ACAModuleInterface)) > 0) {
		return ret;
	}
	throw_runtime_exception(e, "setInterface failed");
	FAIL: if (obj)
		e->DeleteGlobalRef(obj);
	return -1;
}

static void native_set_ecmf(JNIEnv *e, jobject thiz, jbyte coef, jbyte mask, jbyte excl,
		jbyte depth, jint tag, jint flags) {
	ACAModuleManager* peer = jniGetCAModuleManagerPeer(e, thiz);
	if (peer == NULL)
		return;
	ACAModuleManager_setEcmFilter(peer, coef, mask, excl, depth, tag, flags);
}

static void native_update_cw(JNIEnv *e, jobject thiz, jint taskindex, jbyteArray jodd,
		jbyteArray jeven, jint len) {
	uint8_t odd[64] = { 0 }, even[64] = { 0 };
	ACAModuleManager* peer = jniGetCAModuleManagerPeer(e, thiz);
	if (peer == NULL)
		return;
	if (len <= 0 || len > 64) {
		throw_runtime_exception(e, "bad length");
		return;
	}
	if (jodd == NULL && jeven == NULL) {
		throw_runtime_exception(e, "both key is null");
		return;
	}
	if (jodd)
		e->GetByteArrayRegion(jodd, 0, len, (jbyte*) odd);
	if (jeven)
		e->GetByteArrayRegion(jeven, 0, len, (jbyte*) even);
	ACAModuleManager_updateControlWord(peer, taskindex, jodd ? odd : NULL, jeven ? even : NULL, len);
}

static void native_notify_veri(JNIEnv *e, jobject thiz, jboolean b) {
	ACAModuleManager* peer = jniGetCAModuleManagerPeer(e, thiz);
	if (peer == NULL)
		return;
	ACAModuleManager_notifyVerification(peer, b == JNI_TRUE ? 1 : 0, "");
}

static jint native_writecard(JNIEnv *e, jobject thiz, jbyteArray b, jint off, jint len, jint flags) {
	char buf[256];
	ACAModuleManager* peer = jniGetCAModuleManagerPeer(e, thiz);
	if (peer == NULL)
		return -1;
	if (len <= 0 || len > 256 || off < 0) {
		throw_runtime_exception(e, "illegal position or len");
		return -1;
	}
	if (b == NULL) {
		throw_runtime_exception(e, "null pointer");
		return -1;
	}
	int alen = e->GetArrayLength(b);
	if (off + len > alen) {
		throw_runtime_exception(e, "out of bounds");
		return -1;
	}
	e->GetByteArrayRegion(b, off, len, (jbyte*) buf);
	return ACAModuleManager_writeCard2(peer, (uint8_t*) buf, len, flags);
}

static jint native_readcard(JNIEnv *e, jobject thiz, jbyteArray b, jint off, jint len, jint flags) {
	int ret = -1;
	char buf[256];
	ACAModuleManager* peer = jniGetCAModuleManagerPeer(e, thiz);
	if (peer == NULL)
		return -1;
	if (len <= 0 || len > 256 || off < 0) {
		throw_runtime_exception(e, "illegal position or len");
		return -1;
	}
	if (b == NULL) {
		throw_runtime_exception(e, "null pointer");
		return -1;
	}
	int alen = e->GetArrayLength(b);
	if (off + len > alen) {
		throw_runtime_exception(e, "out of bounds");
		return -1;
	}
	if ((ret = ACAModuleManager_readCard2(peer, (uint8_t*) buf, len, flags)) > 0) {
		e->SetByteArrayRegion(b, off, len, (jbyte*) buf);
	}
	return ret;
}

static jbyteArray native_getatr(JNIEnv *e, jobject thiz) {
	int ret = -1;
	char buf[80];
	ACAModuleManager* peer = jniGetCAModuleManagerPeer(e, thiz);
	if (peer == NULL)
		return NULL;
	if ((ret = ACAModuleManager_getATR(peer, (uint8_t*) buf, 64)) > 0) {
		jbyteArray b = e->NewByteArray(ret);
		if (b && ret > 0)
			e->SetByteArrayRegion(b, 0, ret, (jbyte*) buf);
		return b;
	}
	return NULL;
}

static jboolean native_resetcard(JNIEnv *e, jobject thiz, jint flags) {
	ACAModuleManager* peer = jniGetCAModuleManagerPeer(e, thiz);
	if (peer == NULL)
		return JNI_FALSE;
	return ACAModuleManager_resetCard(peer, flags) == 0 ? JNI_TRUE : JNI_FALSE;
}

static jboolean native_start(JNIEnv *e, jobject thiz, jint flags) {
	ACAModuleManager* peer = jniGetCAModuleManagerPeer(e, thiz);
	if (peer == NULL)
		return JNI_FALSE;
	return ACAModuleManager_start(peer, flags) == 0 ? JNI_TRUE : JNI_FALSE;
}

static void native_stop(JNIEnv *e, jobject thiz) {
	ACAModuleManager* peer = jniGetCAModuleManagerPeer(e, thiz);
	if (peer == NULL)
		return;
	ACAModuleManager_stop(peer);
}

static void native_set_prop(JNIEnv *e, jobject thiz, jstring name, jstring value) {
	const char*n = NULL, *v = NULL;
	ACAModuleManager* peer = jniGetCAModuleManagerPeer(e, thiz);
	if (peer == NULL)
		return;
	if (name == NULL || value == NULL) {
		throw_runtime_exception(e, "null pointer");
		goto BAIL;
	}
	if ((n = e->GetStringUTFChars(name, NULL)) == NULL) {
		throw_runtime_exception(e, "out of memory");
		goto BAIL;
	}
	if ((v = e->GetStringUTFChars(value, NULL)) == NULL) {
		throw_runtime_exception(e, "out of memory");
		goto BAIL;
	}
	ACAModuleManager_setCAProperty(peer, n, v);
	BAIL: if (n)
		e->ReleaseStringUTFChars(name, n);
	if (v)
		e->ReleaseStringUTFChars(value, v);
}

static void native_descerr(JNIEnv *e, jobject thiz, jint taskindex, jstring ruri, jstring emsg) {
	const char*us = NULL, *es = NULL;
	ACAModuleManager* peer = jniGetCAModuleManagerPeer(e, thiz);
	if (peer == NULL)
		return;
	us = ruri != NULL ? e->GetStringUTFChars(ruri, NULL) : NULL;
	es = emsg != NULL ? e->GetStringUTFChars(emsg, NULL) : NULL;
	ACAModuleManager_notifyDescramblingError(peer, taskindex, us, es);
	if (us != NULL)
		e->ReleaseStringUTFChars(ruri, us);
	if (es != NULL)
		e->ReleaseStringUTFChars(emsg, es);
}

static void native_resumed(JNIEnv *e, jobject thiz, jint i) {
	ACAModuleManager* peer = jniGetCAModuleManagerPeer(e, thiz);
	if (peer == NULL)
		return;
	ACAModuleManager_notifyDescramblingResumed(peer, i);
}

static void native_descramblable(JNIEnv *e, jobject thiz) {
	ACAModuleManager* peer = jniGetCAModuleManagerPeer(e, thiz);
	if (peer == NULL)
		return;
	ACAModuleManager_notifyDescramblableChange(peer);
}

// ----------------------------------------------------------------------------
static const char* g_class_name = "android/net/telecast/ca/CAModuleManager";
static JNINativeMethod g_class_methods[] = { //
	{ "native_create", "(JJI)V", (void*) native_create },//
	{ "native_release", "()V", (void*) native_release },//
	{ "native_setif", "(Ljava/lang/Object;[I)I", (void*) native_setif },//
	{ "native_set_ecmf", "(BBBBII)V", (void*) native_set_ecmf },//
	//{ "native_update_ent", "(JII)V", (void*) native_update_ent },//·ÏÆú
	{ "native_update_cw", "(I[B[BI)V", (void*) native_update_cw },//
	{ "native_notify_veri", "(Z)V", (void*) native_notify_veri },//
	{ "native_writecard", "([BIII)I", (void*) native_writecard },//
	{ "native_readcard", "([BIII)I", (void*) native_readcard },//
	{ "native_getatr", "()[B", (void*) native_getatr },//
	{ "native_resetcard", "(I)Z", (void*) native_resetcard },//
	{ "native_start", "(I)Z", (void*) native_start },//
	{ "native_stop", "()V", (void*) native_stop },//
	{ "native_set_prop", "(Ljava/lang/String;Ljava/lang/String;)V",(void*) native_set_prop },//
	{ "native_notify_descerr", "(ILjava/lang/String;Ljava/lang/String;)V",(void*) native_descerr }, //
	{ "native_notify_resume", "(I)V",(void*) native_resumed }, //
	{ "native_notify_descramblable", "()V",(void*) native_descramblable }, //
};

//do not throw any exception in this fun
int register_ca_module_manager_natives(JNIEnv *e) {
	jclass cls;
	if ((cls = e->FindClass(g_class_name)) == NULL) {
		LOGE("Can't find class : %s", g_class_name);
		return -1;
	}
	g_camm.clazz = (jclass) e->NewGlobalRef(cls);

	if ((g_camm.peer = e->GetFieldID(cls, "peer", "I")) == NULL) {
		LOGE("no such field: peer");
		return -1;
	}
	if ((g_camm.cbf = e->GetStaticMethodID(cls, "native_callback",
			"(Ljava/lang/Object;IIJIIIILjava/lang/Object;)V")) == NULL) {
		LOGE("can't find method nativeCallback of class: %s", g_class_name);
		return -1;
	}
	LOGI("register_ca_module_manager_natives g_camm.peer=%p",g_camm.peer);
	return AndroidRuntime::registerNativeMethods(e, g_class_name, g_class_methods,
			NELEM(g_class_methods));
}

