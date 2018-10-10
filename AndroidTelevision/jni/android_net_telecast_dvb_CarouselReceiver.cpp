#define LOG_TAG "[jni]CarouselReceiver"
#include <utils/Log.h>
#include <dlfcn.h>
#include <string.h>
#include <assert.h>
#include <tvs/tvsdex.h>
#include <androidtv/carousel_receiver.h>
#include "native_init.h"

using namespace android;

#define  MSG_GW_UP  		1
#define  MSG_MOD_RECV 		2
#define  MSG_MOD_UP  		3
#define  MSG_RECV_STATE  	4

static struct carouselrecv_info {
	jclass clazz;
	jmethodID proc;
	jfieldID peer;
} g_carouselrecv;

static void jniCarouselReceiverCallback(void*o, int msg, int p1, int p2) {
	JNIEnv *env = attach_java_thread("carousel_receiver");
	jobject wo = (jobject) o;
	assert(wo);
	env->CallStaticVoidMethod(g_carouselrecv.clazz, g_carouselrecv.proc, wo, msg, p1, p2);
	if (msg == 0) { // CLOSED MESSAGE
		env->DeleteGlobalRef(wo);
	}
}

static void my_CarouselReceiverCallback(ACarouselReceiver*f, void*o, int msg, int p1, void*p2) {
	LOGD("my_CarouselReceiverCallback msg = %d,%d,%d", msg, p1, (int) p2);
	if (o == NULL)
		return;
	switch (msg) {
	case ACAROUSELRECEIVER_CB_CLOSED:
		jniCarouselReceiverCallback(o, MSG_RECV_STATE, 0, 0);
		break;
	case ACAROUSELRECEIVER_CB_PAUSED:
		jniCarouselReceiverCallback(o, MSG_RECV_STATE, 1, 0);
		break;
	case ACAROUSELRECEIVER_CB_RESUMED:
		jniCarouselReceiverCallback(o, MSG_RECV_STATE, 2, 0);
		break;
	case ACAROUSELRECEIVER_CB_DSI_GOT:
		//TODO
		break;
	case ACAROUSELRECEIVER_CB_DII_GOT:
		jniCarouselReceiverCallback(o, MSG_GW_UP, p1, 0); //TODO -- 1,0,SUCC  1,1,FAILED
		break;
	case ACAROUSELRECEIVER_CB_MOD_CHANGED: {
		AModuleParam* p = (AModuleParam*) p2;
		LOGD("ACAROUSELRECEIVER_CB_MOD_CHANGED mid=%d,version=%d.\n", p1, (int) p->result);
		jniCarouselReceiverCallback(o, MSG_MOD_UP, p1, p->result);
		break;
	}
	case ACAROUSELRECEIVER_CB_MOD_RESULT: {
		AModuleParam* p = (AModuleParam*) p2;
		LOGD("ACAROUSELRECEIVER_CB_MOD_RESULT mid=%d,result=%d.\n", p1, (int) p->result);
		jniCarouselReceiverCallback(o, MSG_MOD_RECV, p1, p->result);
		break;
	}
	default:
		LOGD("proc> invalid message(%d)!", msg);
		break;
	}
}

static ACarouselReceiver* jniGetCarouseReceiverPeer(JNIEnv *e, jobject thiz) {
	ACarouselReceiver*p = (ACarouselReceiver*) e->GetIntField(thiz, g_carouselrecv.peer);
	if (p == NULL) {
		LOGD("CarouselReceiver peer is null");
		throw_runtime_exception(e, "peer is null");
	}
	return p;
}

extern "C" ACarouselReceiver* JavaCarouselReceiver_getNativePeer(JNIEnv *e, jobject thiz) {
	return (ACarouselReceiver*) e->GetIntField(thiz, g_carouselrecv.peer);
}

static jboolean native_open(JNIEnv *e, jobject thiz, jobject wo, jlong idm, jlong idl, jint fs,
		jint bs, jint flags) {
	jobject obj = NULL;
	ACarouselReceiver *f = NULL;
	AUUID uuid;
	if (wo == NULL) {
		return throw_runtime_exception(e, "null pointer");
	}
	if ((obj = e->NewGlobalRef(wo)) == NULL) {
		return throw_runtime_exception(e, "null pointer2");
	}
	AUUID_fromMostLeast(&uuid, idm, idl);
	if ((f = ACarouselReceiver_new(&uuid, fs, bs, flags, (void*) obj, my_CarouselReceiverCallback))
			== NULL) {
		if (obj != NULL)
			e->DeleteGlobalRef(obj);
		return throw_runtime_exception(e, "can't open carousel filter");
	}
	LOGD("native_open idm=%lld,idl=%lld", idm, idl);
	e->SetIntField(thiz, g_carouselrecv.peer, (int) f);
	return JNI_TRUE;
}

static int native_attach(JNIEnv *e, jobject thiz, jlong freq, jint pid) {
	ACarouselReceiver *peer = jniGetCarouseReceiverPeer(e, thiz);
	if (peer != NULL) {
		return ACarouselReceiver_start(peer, freq, pid);
	}
	return -1;
}

static int native_detach(JNIEnv *e, jobject thiz) {
	ACarouselReceiver *peer = jniGetCarouseReceiverPeer(e, thiz);
	if (peer != NULL) {
		return ACarouselReceiver_stop(peer);
	}
	return -1;
}

static int native_release(JNIEnv *e, jobject thiz) {
	ACarouselReceiver *peer = jniGetCarouseReceiverPeer(e, thiz);
	if (peer != NULL) {
		ACarouselReceiver_delete(peer);
		e->SetIntField(thiz, g_carouselrecv.peer, 0);
	}
	return 0;
}

static int native_recvm(JNIEnv *e, jobject thiz, jint mid, jint fi, jlong off) {
	ACarouselReceiver *peer = jniGetCarouseReceiverPeer(e, thiz);
	if (peer != NULL) {
		return ACarouselReceiver_saveMod(peer, mid, -1, fi, off, 0);
	}
	return -1;
}

static int native_obsm(JNIEnv *e, jobject thiz, jint mid, jint v) {
	ACarouselReceiver *peer = jniGetCarouseReceiverPeer(e, thiz);
	if (peer != NULL) {
		return ACarouselReceiver_monitorMod(peer, mid, v);
	}
	return -1;
}

static int native_cancm(JNIEnv *e, jobject thiz, jint mid) {
	ACarouselReceiver *peer = jniGetCarouseReceiverPeer(e, thiz);
	if (peer != NULL) {
		return ACarouselReceiver_cancelMod(peer, mid);
	}
	return -1;
}

static int native_syncg(JNIEnv *e, jobject thiz) {
	ACarouselReceiver *peer = jniGetCarouseReceiverPeer(e, thiz);
	if (peer != NULL) {
		return ACarouselReceiver_getDII(peer);
	}
	return -1;
}

static int native_setf(JNIEnv *e, jobject thiz, jint i, jobject jfd) {
	int fd = -1;
	ACarouselReceiver *peer = jniGetCarouseReceiverPeer(e, thiz);
	if (peer == NULL)
		return -1;

	LOGD("native_setf jfd = %p", jfd);
	if ((fd = jni_native_get_fd(e, jfd)) < 0) {
		LOGD("native_setf>invalid FileDescriptor");
		return -1;
	}
	return ACarouselReceiver_setFd(peer, i, fd);
}

// ----------------------------------------------------------------------------
static const char* g_class_name = "android/net/telecast/dvb/CarouselReceiver";

static JNINativeMethod g_class_methods[] = { //
	{ "native_open", "(Ljava/lang/ref/WeakReference;JJIII)Z", (void*) native_open }, //
	{ "native_attach", "(JI)I", (void*) native_attach }, //
	{ "native_detach", "()I", (void*) native_detach }, //
	{ "native_release", "()I", (void*) native_release }, //
	{ "native_recvm", "(III)I", (void*) native_recvm }, //
	{ "native_obsm", "(II)I", (void*) native_obsm }, //
	{ "native_cancm", "(I)I", (void*) native_cancm }, //
	{ "native_syncg", "()I", (void*) native_syncg }, //
	{ "native_setf", "(ILjava/io/FileDescriptor;)I", (void*) native_setf } //
};

int register_carousel_receiver_natives(JNIEnv *e) {
	jclass cls;
	if (tvsd_ensure_service()) {
		LOGE("section_filter tvsd_ensure_service failed");
		return -1;
	}
	if ((cls = e->FindClass(g_class_name)) == NULL) {
		LOGE("Can't find class : %s", g_class_name);
		return -1;
	}
	g_carouselrecv.clazz = (jclass) e->NewGlobalRef(cls);
	if ((g_carouselrecv.peer = e->GetFieldID(cls, "peer", "I")) == NULL) {
		LOGE("no such field: peer");
		return -1;
	}

	if ((g_carouselrecv.proc = e->GetStaticMethodID(g_carouselrecv.clazz, "native_callback",
			"(Ljava/lang/Object;III)V")) == NULL) {
		LOGE("no such method: native_callback");
		return -1;
	}

	return AndroidRuntime::registerNativeMethods(e, g_class_name, g_class_methods,
			NELEM(g_class_methods));
}

