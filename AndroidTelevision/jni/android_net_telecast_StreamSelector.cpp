#define LOG_TAG "[jni]StreamSelector"
#include <utils/Log.h>
#include <tvs/tvsdex.h>
#include <ipanel/section_reader.h>
#include <dlfcn.h>
#include <string.h>
#include <stdint.h>
#include <androidtv/stream_selector.h>

#include "native_init.h"

using namespace android;

#define MSG_TUNNING_OVER 	1
#define MSG_LOCK_STATE		2
#define MSG_RES_RECYLED		3
#define MSG_LOCK_STARTED	4

static struct {
	jclass clazz;
	jfieldID peer;
	jmethodID proc;
} g_selector;

static struct{
	jclass clazz;
	jfieldID quality;
	jfieldID strength;
	jfieldID level;
	jfieldID snr;
	jfieldID ber;
} g_signal;

static void jniStreamSelectorCallback(void*o, int msg, int p1, int64_t p2) {
	JNIEnv *env = attach_java_thread("stream-selector");
	jobject wo = (jobject) o;
	assert(wo);
	env->CallStaticVoidMethod(g_selector.clazz, g_selector.proc, wo, msg, p1, p2);
	if (msg == 0) { // CLOSED MESSAGE
		env->DeleteGlobalRef(wo);
	}
}

static void my_AStreamSelectorCallback(AStreamSelector*ss, void*o, int msg, int p1, void*p2) {
	LOGI("my_AStreamSelectorCallback msg = %d,%d,%p",msg,p1,p2);
	if (o == NULL)
		return;
	switch (msg) {
	case ASTREAMSELECTOR_CB_RES_RECYCLED:
		jniStreamSelectorCallback(o, MSG_RES_RECYLED, 0, 0);
		break;
	case ASTREAMSELECTOR_CB_SELECT_SUCCESS:
		jniStreamSelectorCallback(o, MSG_TUNNING_OVER, 1, 0);
		break;
	case ASTREAMSELECTOR_CB_SELECT_FAILED:
		jniStreamSelectorCallback(o, MSG_TUNNING_OVER, 0, 0);
		break;
	case ASTREAMSELECTOR_CB_SELECTION_LOST:
		jniStreamSelectorCallback(o, MSG_LOCK_STATE, 0, 0);
		break;
	case ASTREAMSELECTOR_CB_SELECTION_REGAINED:
		jniStreamSelectorCallback(o, MSG_LOCK_STATE, 1, 0);
		break;
	case ASTREAMSELECTOR_CB_SELECTION_START:
		jniStreamSelectorCallback(o, MSG_LOCK_STARTED, p1, *((int64_t*) p2));
		break;
	case MSG_HANDLE_CLOSED:
		jniStreamSelectorCallback(o, 0, 0, 0);
		break;
	}
}

AStreamSelector* jniGetStreamSelectorPeer(JNIEnv *e, jobject thiz) {
	AStreamSelector*ss = (AStreamSelector*) e->GetIntField(thiz, g_selector.peer);
	if (ss == NULL) {
		LOGD("StreamSelector peer is null");
	}
	return ss;
}

extern "C" AStreamSelector* JavaStreamSelector_getNativePeer(JNIEnv *e, jobject thiz) {
	return (AStreamSelector*) e->GetIntField(thiz, g_selector.peer);
}

static jboolean native_reserve(JNIEnv *e, jobject thiz, jobject jwo, jint jiid, int flags) {
	jobject obj;
	AStreamSelector*ss = (AStreamSelector*) e->GetIntField(thiz, g_selector.peer);
	if (jwo == NULL)
		return throw_runtime_exception(e, "null pointer");
	if (ss != NULL)
		return throw_runtime_exception(e, "streamselector has been opened!");
	if ((obj = e->NewGlobalRef(jwo)) == NULL)
		return throw_runtime_exception(e, "out of memory");
	if ((ss = AStreamSelector_new(jiid, flags, obj, my_AStreamSelectorCallback)) == NULL) {
		if (obj != NULL)
			e->DeleteGlobalRef(obj);
		LOGD("open stream selector failed!");
		return JNI_FALSE;
	}
	e->SetIntField(thiz, g_selector.peer, (int) ss);
	return JNI_TRUE;
}

static void native_release(JNIEnv *e, jobject thiz) {
	AStreamSelector*ss = jniGetStreamSelectorPeer(e, thiz);
	if (ss) {
		e->SetIntField(thiz, g_selector.peer, 0);
		AStreamSelector_delete(ss);
	}
}

static jboolean native_clear(JNIEnv *e, jobject thiz) {
	AStreamSelector* ss = jniGetStreamSelectorPeer(e, thiz);
	if (ss)
		return AStreamSelector_clear(ss) == 0 ? JNI_TRUE : JNI_FALSE;
	return JNI_FALSE;
}

static jboolean native_set_weak_mode(JNIEnv *e, jobject thiz,jboolean b) {
	AStreamSelector* ss = jniGetStreamSelectorPeer(e, thiz);
	if (ss)
		return AStreamSelector_setWeakMode(ss, b) == 0 ? JNI_TRUE : JNI_FALSE;
	return JNI_FALSE;
}
static jboolean native_signal_status(JNIEnv *e, jobject thiz, jobject jss) {
	ASignalStatus ass;
	AStreamSelector*ss = jniGetStreamSelectorPeer(e, thiz);
	if (ss == NULL || jss == NULL) {
		return throw_runtime_exception(e, "");
	}
	if (AStreamSelector_signalStatus(ss, &ass) == 0) {
		e->SetIntField(jss, g_signal.quality, ass.quality);
		e->SetIntField(jss, g_signal.level, ass.level);
		e->SetIntField(jss, g_signal.strength, ass.strength);
		e->SetIntField(jss, g_signal.snr, ass.snr);
		e->SetFloatField(jss, g_signal.ber, ass.ber);
		return JNI_TRUE;
	}
	return JNI_FALSE;
}

static jboolean native_set_network(JNIEnv *e, jobject thiz, jlong nidm, jlong nidl) {
	AStreamSelector*ss = jniGetStreamSelectorPeer(e, thiz);
	if (ss == NULL)
		return throw_runtime_exception(e, "object released");
	AUUID uuid;
	AUUID_fromMostLeast(&uuid, nidm, nidl);
	return AStreamSelector_setNetworkUUID(ss, &uuid) == 0 ? JNI_TRUE : JNI_FALSE;
}

static void native_set_virtual_frequency(JNIEnv *e, jobject thiz, jlong freq) {
	AStreamSelector*ss = jniGetStreamSelectorPeer(e, thiz);
	if (ss == NULL)
		throw_runtime_exception(e, "object released");
	AStreamSelector_setVirtualFrequency(ss, freq);
}

static jboolean native_select_freq(JNIEnv *e, jobject thiz, jstring freq, jint flags) {
	jboolean ret = JNI_FALSE;
	const char* cFreq = NULL;
	AStreamSelector*ss = jniGetStreamSelectorPeer(e, thiz);
	if (ss == NULL)
		return throw_runtime_exception(e, "object released");
	if (freq == NULL)
		return throw_runtime_exception(e, "null pointer");

	if ((cFreq = e->GetStringUTFChars(freq, NULL))) {
		ret = AStreamSelector_selectFreq(ss, cFreq, flags) == 0 ? JNI_TRUE : JNI_FALSE;
		e->ReleaseStringUTFChars(freq, cFreq);
	}
	return ret;
}

static jboolean native_select_file(JNIEnv *e, jobject thiz, jobject jfd, jlong off, jlong len,
		jint flags) {
	int fd = -1;
	AStreamSelector*ss = jniGetStreamSelectorPeer(e, thiz);
	if (ss == NULL)
		return throw_runtime_exception(e, "object released");
	fd = jni_native_get_fd(e, jfd);
	LOGD("native_select_file jfd = %p, fd = %d", jfd,fd);
	
	return AStreamSelector_selectFile(ss, fd, off, len, flags) == 0 ? JNI_TRUE : JNI_FALSE;
}

static jboolean native_select_stream(JNIEnv *e, jobject thiz, jobject jfd, jint flags) {
	int fd = -1;
	AStreamSelector*ss = jniGetStreamSelectorPeer(e, thiz);
	if (ss == NULL)
		return throw_runtime_exception(e, "object released");
	fd = jni_native_get_fd(e, jfd);
	LOGD("native_select_stream jfd = %p, fd = %d", jfd,fd);
	return AStreamSelector_selectStream(ss, fd, flags) == 0 ? JNI_TRUE : JNI_FALSE;
}
static jboolean native_receive_stream(JNIEnv *e, jobject thiz, jintArray jpids, jobject jfd,
		jint flags) {
	int pids[64], n = 0, fd = -1;
	AStreamSelector*ss = jniGetStreamSelectorPeer(e, thiz);
	if (ss == NULL)
		return throw_runtime_exception(e, "object released");
	fd = jni_native_get_fd(e, jfd);
	LOGD("native_receive_stream jfd = %p, fd = %d", jfd,fd);
	if (jpids == NULL ? 1 : (n = e->GetArrayLength(jpids)) > 64) {
		LOGD("invalid pids");
		return JNI_FALSE;
	}
	e->GetIntArrayRegion(jpids, 0, n, (jint*) pids);
	return AStreamSelector_receiveStreamBatching(ss, pids, n, fd, flags) == 0 ? JNI_TRUE : JNI_FALSE;
}

// ----------------------------------------------------------------------------
static const char* g_class_name = "android/net/telecast/StreamSelector";
static JNINativeMethod g_class_methods[] = { //
	{ "native_reserve", "(Ljava/lang/ref/WeakReference;II)Z", (void*) native_reserve },//
	{ "native_release", "()V", (void*) native_release },//
	{ "native_set_weak_mode", "(Z)Z", (void*) native_set_weak_mode },//
	{ "native_clear", "()Z", (void*) native_clear },//
	{ "native_signal_status", "(Landroid/net/telecast/SignalStatus;)Z",(void*) native_signal_status },//
	{ "native_set_network", "(JJ)Z", (void*) native_set_network }, //
	{ "native_set_virtual_frequency", "(J)V", (void*) native_set_virtual_frequency }, //
	{ "native_select_freq", "(Ljava/lang/String;I)Z", (void*) native_select_freq }, //
	{ "native_select_file", "(Ljava/io/FileDescriptor;JJI)Z", (void*) native_select_file }, //
	{ "native_select_stream", "(Ljava/io/FileDescriptor;I)Z", (void*) native_select_stream }, //
	{ "native_receive_stream", "([ILjava/io/FileDescriptor;I)Z", (void*) native_receive_stream }, //
};

//do not throw any exception in this fun,return <0 for err
int register_stream_selector_natives(JNIEnv *e) {
	jclass cls;
	LOGI("register_stream_selector_natives>1");
	if ((cls = e->FindClass(g_class_name)) == NULL) {
		LOGE("Can't find class : %s", g_class_name);
		return -1;
	}
	g_selector.clazz = (jclass) e->NewGlobalRef(cls);
	if ((g_selector.proc = e->GetStaticMethodID(cls, "native_proc", "(Ljava/lang/Object;IIJ)V"))
			== NULL) {
		LOGE("no such method: native_proc");
		return -1;
	}
	if ((g_selector.peer = e->GetFieldID(g_selector.clazz, "peer", "I")) == NULL) {
		LOGE("no such field: peer");
		return -1;
	}
	//--
	if ((cls = e->FindClass("android/net/telecast/SignalStatus"))== NULL) {
		LOGE("Can't find class : android/net/telecast/SignalStatus");
		return -1;
	}
	g_signal.clazz = (jclass) e->NewGlobalRef(cls);
	g_signal.quality = e->GetFieldID(g_signal.clazz, "quality", "I");
	g_signal.level = e->GetFieldID(g_signal.clazz, "level", "I");
	g_signal.strength = e->GetFieldID(g_signal.clazz, "strength", "I");
	g_signal.snr = e->GetFieldID(g_signal.clazz, "snr", "I");
	g_signal.ber = e->GetFieldID(g_signal.clazz, "ber", "F");
	if (!(g_signal.quality && g_signal.level && g_signal.strength && g_signal.snr && g_signal.ber)) {
		LOGE("android/net/telecast/SignalStatus some field not found!");
		return -1;
	}
	return AndroidRuntime::registerNativeMethods(e, g_class_name, g_class_methods,
			NELEM(g_class_methods));
}

