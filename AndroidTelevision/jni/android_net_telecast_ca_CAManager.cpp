#define LOG_TAG "[jni]CAManager"
#include <utils/Log.h>
#include <tvs/tvsdex.h>
#include <androidtv/ca_manager.h>
#include <dlfcn.h>
#include <string.h>
#include "native_init.h"

using namespace android;

static struct {
	jclass clazz;
	jmethodID callback;
	jfieldID peer;
} g_cam;

#define MSG_CARD_PRESENT 	1
#define MSG_CARD_ABSENT 	2
#define MSG_CARD_MUTED 		3
#define MSG_CARD_READY 		4
#define MSG_CARD_VERIFIED	10

#define MSG_MODULE_ADD 		5
#define MSG_MODULE_REMOVED 	6
#define MSG_MODULE_PRESENT 	7
#define MSG_MODULE_ABSENT 	8
#define MSG_CA_CHANGE 		9

static void jniCAManagerCallback(void*o, int msg, int p1,void *p2) {
	JNIEnv *env = attach_java_thread("camanager_jni");
	jobject wo = (jobject) o;
	assert(wo);
	env->CallStaticVoidMethod(g_cam.clazz, g_cam.callback, wo, msg, p1, (int) p2);
	if (msg == 0) { // CLOSED MESSAGE
		env->DeleteGlobalRef(wo);
	}
}

static void my_ACAManagerCallback(ACAManager*m, void*o, int what, int p1,
		void*p2) {
	switch (what) {
	case ACAMANAGER_CB_CARD_PRESENT:
		jniCAManagerCallback(o, MSG_CARD_PRESENT, p1, p2);
		break;
	case ACAMANAGER_CB_CARD_ABSENT:
		jniCAManagerCallback(o, MSG_CARD_ABSENT, p1, p2);
		break;
	case ACAMANAGER_CB_CARD_MUTED:
		jniCAManagerCallback(o, MSG_CARD_MUTED, p1, p2);
		break;
	case ACAMANAGER_CB_CARD_READY:
		jniCAManagerCallback(o, MSG_CARD_READY, p1, p2);
		break;
	case ACAMANAGER_CB_CARD_VERIFIED:
		jniCAManagerCallback(o, MSG_CARD_VERIFIED, p1, p2);
		break;
	case ACAMANAGER_CB_MODULE_ADD:
		jniCAManagerCallback(o, MSG_MODULE_ADD, p1, p2);
		break;
	case ACAMANAGER_CB_MODULE_REMOVED:
		jniCAManagerCallback(o, MSG_MODULE_REMOVED, p1, p2);
		break;
	case ACAMANAGER_CB_MODULE_PRESENT:
		jniCAManagerCallback(o, MSG_MODULE_PRESENT, p1, p2);
		break;
	case ACAMANAGER_CB_MODULE_ABSENT:
		jniCAManagerCallback(o, MSG_MODULE_ABSENT, p1, p2);
		break;
	case ACAMANAGER_CB_CA_CHANGE:
		jniCAManagerCallback(o, MSG_CA_CHANGE, p1, p2);
		break;
	case MSG_HANDLE_CLOSED:
		jniCAManagerCallback(o, 0, 0, NULL);
		break;
	}
}

static ACAManager* jniGetCAManagerPeer(JNIEnv *e, jobject thiz) {
	ACAManager*p = (ACAManager*) e->GetIntField(thiz, g_cam.peer);
	if (p == NULL) {
		LOGD("CAManager peer is null");
		throw_runtime_exception(e, "peer is null");
	}
	return p;
}

extern "C" ACAManager* JavaCAManager_getNativePeer(JNIEnv *e, jobject thiz){
	return (ACAManager*) e->GetIntField(thiz, g_cam.peer);
}

static void native_init(JNIEnv *e, jobject thiz, jobject wo) {
	jobject obj;
	ACAManager*c = NULL;
	if ((c = (ACAManager*) e->GetIntField(thiz, g_cam.peer)) != NULL) {
		throw_runtime_exception(e, "peer has been initd");
		return;
	}
	if ((obj = e->NewGlobalRef(wo)) == NULL)
		throw_runtime_exception(e, "out of memory");
	if ((c = ACAManager_new(obj, my_ACAManagerCallback)) == NULL) {
		if (obj != NULL)
			e->DeleteGlobalRef(obj);
		LOGD("open camanager failed!");
		return;
	}
	e->SetIntField(thiz, g_cam.peer, (int) c);
}

static void native_release(JNIEnv *e, jobject thiz) {
	ACAManager* peer = jniGetCAManagerPeer(e, thiz);
	if (peer) {
		ACAManager_delete(peer);
		e->SetIntField(thiz, g_cam.peer, 0);
	}
}

static jint native_reader_size(JNIEnv *e, jobject thiz) {
	ACAManager* peer = jniGetCAManagerPeer(e, thiz);
	if (peer)
		return ACAManager_cardReaderSize(peer);
	return -1;
}

static jintArray native_casys_ids(JNIEnv *e, jobject thiz, jint mid) {
	jintArray ret = NULL;
	int n = -1, ids[64] = { 0 };
	ACAManager* peer = jniGetCAManagerPeer(e, thiz);
	if (peer == NULL)
		return NULL;
	memset(ids, 0x00, sizeof(ids));
	if ((n = ACAManager_getCAModuleCASystemIDs(peer, mid, ids, 64)) < 0)
		return NULL;
	if ((ret = e->NewIntArray(n)))
		e->SetIntArrayRegion(ret, 0, n, ids);
	return ret;
}

static jstring native_get_prop(JNIEnv *e, jobject thiz, jint mid, jstring name) {
	jstring ret = NULL;
	const char*n = NULL;
	char buf[256] = { 0 };
	ACAManager* peer = jniGetCAManagerPeer(e, thiz);
	if (peer == NULL)
		return NULL;
	if (name == NULL ? 1 : !(n = e->GetStringUTFChars(name, NULL))) {
		throw_runtime_exception(e, "name is null or out of memory");
		goto BAIL;
	}
	if (ACAManager_getCAModuleProperty(peer, mid, n, buf, 256) > 0) {
		ret = e->NewStringUTF(buf);
	}
	BAIL: if (n)
		e->ReleaseStringUTFChars(name, n);
	LOGI("native_get_prop name = %s,buf=%s", n, buf);
	return ret;
}

static jintArray native_module_ids(JNIEnv *e, jobject thiz, jlong most, jlong least) {
	int ids[32] = { 0 }, n = 0;
	jintArray ret = NULL;
	AUUID uuid;
	ACAManager* peer = jniGetCAManagerPeer(e, thiz);
	if (peer == NULL)
		return NULL;
	AUUID_fromMostLeast(&uuid, most, least);
	if ((n = ACAManager_getCAModuleIDs(peer, &uuid, ids, 32)) > 0) {
		if ((ret = e->NewIntArray(n))) {
			e->SetIntArrayRegion(ret, 0, n, (jint*) ids);
		}
	}
	return ret;
}
static jint native_find_module(JNIEnv *e, jobject thiz, jlong m, jlong l, jint casid) {
	AUUID id ;
	ACAManager* peer = jniGetCAManagerPeer(e, thiz);
	if (peer == NULL)
		return -1;
	AUUID_fromMostLeast(&id, m, l);
	return ACAManager_findCAModule(peer, &id, casid);
}

static jlongArray native_get_uuid(JNIEnv *e, jobject thiz, jint mid) {
	jlongArray ret = NULL;
	AUUID uuid;
	ACAManager* peer = jniGetCAManagerPeer(e, thiz);
	if (peer == NULL)
		return NULL;
	AUUID_clear(&uuid);
	if (ACAManager_getCAModuleNetworkUUID(peer, mid, &uuid) == 0) {
		if ((ret = e->NewLongArray(2)))
			e->SetLongArrayRegion(ret, 0, 2, (jlong*) &uuid);
	}
	return ret;
}

static void native_buy(JNIEnv *e, jobject thiz, jint mid, jstring juri) {
	const char*u = NULL;
	ACAManager* peer = jniGetCAManagerPeer(e, thiz);
	if (peer == NULL)
		return;
	u = juri ? e->GetStringUTFChars(juri, NULL) : NULL;
	ACAManager_buyCAModuleEntitlement(peer, mid, u);
	if (u != NULL)
		e->ReleaseStringUTFChars(juri, u);
}

static void native_enter(JNIEnv *e, jobject thiz, jint mid, jstring juri) {
	const char*u = NULL;
	ACAManager* peer = jniGetCAManagerPeer(e, thiz);
	if (peer == NULL)
		return;
	u = juri ? e->GetStringUTFChars(juri, NULL) : NULL;
	ACAManager_enterCAModuleApplication(peer, mid, u);
	if (u != NULL)
		e->ReleaseStringUTFChars(juri, u);
}

static void native_query_state(JNIEnv *e, jobject thiz) {
	ACAManager* peer = jniGetCAManagerPeer(e, thiz);
	if (peer == NULL)
		return;
	ACAManager_queryCurrentCAState(peer);
}

// ----------------------------------------------------------------------------
static const char* g_class_name = "android/net/telecast/ca/CAManager";
static JNINativeMethod g_class_methods[] = { //
	{ "native_init", "(Ljava/lang/ref/WeakReference;)V", (void*) native_init },//
	{ "native_release", "()V", (void*) native_release },//
	{ "native_reader_size", "()I", (void*) native_reader_size },//
	{ "native_module_ids", "(JJ)[I", (void*) native_module_ids },//
	{ "native_casys_ids", "(I)[I", (void*) native_casys_ids },//
	{ "native_get_prop", "(ILjava/lang/String;)Ljava/lang/String;", (void*) native_get_prop },//
	{ "native_find_module","(JJI)I",(void*)native_find_module},//
	{ "native_get_uuid","(I)[J",(void*)native_get_uuid},//
	//{ "native_query","(IJI)I",(void*)native_query},//·ÏÆú
	{ "native_buy","(ILjava/lang/String;)V",(void*)native_buy},//
	{ "native_enter","(ILjava/lang/String;)V",(void*)native_enter},//
	{ "native_query_state","()V",(void*)native_query_state}//
};

//do not throw any exception in this fun
int register_ca_manager_natives(JNIEnv *e) {
	jclass cls;
	if ((cls = e->FindClass(g_class_name)) == NULL) {
		LOGE("Can't find class : %s", g_class_name);
		return -1;
	}
	g_cam.clazz = (jclass) e->NewGlobalRef(cls);

	if ((g_cam.peer = e->GetFieldID(g_cam.clazz, "peer", "I")) == NULL) {
		LOGE("no such int field: peer");
		return -1;
	}
	if ((g_cam.callback
			= e->GetStaticMethodID(g_cam.clazz, "nativeCallback", "(Ljava/lang/Object;III)V")) == NULL) {
		LOGE("can't find method nativeCallback of class: %s", g_class_name);
		return -1;
	}
	return AndroidRuntime::registerNativeMethods(e, g_class_name, g_class_methods,
			NELEM(g_class_methods));
}
