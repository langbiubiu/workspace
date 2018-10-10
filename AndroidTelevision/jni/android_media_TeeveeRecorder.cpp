#define LOG_TAG "[jni]TeeveeRecorder"
#include <utils/Log.h>
#include <tvs/tvsdex.h>
#include <androidtv/teevee_recorder.h>
#include <dlfcn.h>
#include <string.h>

#include "native_init.h"

using namespace android;

#define MSG_ON_PLAYER_ERROR	       1

static struct {
	jclass clazz;
	jfieldID peer;
	jmethodID proc;
} g_rec;

static void my_ATeeveeRecorderCallback(ATeeveeRecorder*ss, void*o, int msg, int p1, void*p2) {
	JNIEnv *e = attach_java_thread("tvrecorder-jni");
	jobject wo = (jobject) o;
	assert(wo);
	switch (msg) {
	case ATEEVEERECORDER_CB_RECORD_START: {
		jstring s = p2 ? e->NewStringUTF((const char*) p2) : NULL;
		e->CallStaticVoidMethod(g_rec.clazz, g_rec.proc, wo, 3, p1, s);
		if (s != NULL)
			e->DeleteLocalRef(s);
		break;
	}
	case ATEEVEERECORDER_CB_RECORD_ERROR: {
		jstring s = p2 ? e->NewStringUTF((const char*) p2) : NULL;
		e->CallStaticVoidMethod(g_rec.clazz, g_rec.proc, wo, 1, p1, s);
		if (s != NULL)
			e->DeleteLocalRef(s);
		break;
	}
	case ATEEVEERECORDER_CB_RECORD_END: {
		jstring s = p2 ? e->NewStringUTF((const char*) p2) : NULL;
		e->CallStaticVoidMethod(g_rec.clazz, g_rec.proc, wo, 2, p1, s);
		if (s != NULL)
			e->DeleteLocalRef(s);
		break;
	}
	case ATEEVEERECORDER_CB_PROGRAM_RESELECT: {
		jstring s = p2 ? e->NewStringUTF((const char*) p2) : NULL;
		e->CallStaticVoidMethod(g_rec.clazz, g_rec.proc, wo, 8, p1, s);
		if (s != NULL)
			e->DeleteLocalRef(s);
		break;
	}
	case ATEEVEERECORDER_CB_PROGRAM_REMOVED:
		e->CallStaticVoidMethod(g_rec.clazz, g_rec.proc, wo, 9, p1, NULL);
		break;
	case MSG_HANDLE_CLOSED:
		e->CallStaticVoidMethod(g_rec.clazz, g_rec.proc, wo, 0, 0, NULL);
		e->DeleteGlobalRef(wo);
		break;
	default:
		LOGV("my_ATeeveeRecorderCallback invalid message!");
		break;
	}
}

static ATeeveeRecorder* jniGetTeeveeRecorderPeer(JNIEnv *e, jobject thiz) {
	ATeeveeRecorder*p = (ATeeveeRecorder*) e->GetIntField(thiz, g_rec.peer);
	if (p == NULL) {
		LOGD("TeeveeRecorder peer is null");
		throw_runtime_exception(e, "peer is null");
	}
	return p;
}

extern "C" ATeeveeRecorder* JavaTeeveeRecorder_getNativePeer(JNIEnv *e, jobject thiz){
	return (ATeeveeRecorder*) e->GetIntField(thiz, g_rec.peer);
}

static jboolean native_init(JNIEnv *e, jobject thiz, jobject wo, int flags) {
	jobject obj = NULL;
	LOGD("create tv recorder...");
	ATeeveeRecorder*peer = (ATeeveeRecorder*) e->GetIntField(thiz, g_rec.peer);
	if (wo == NULL)
		return throw_runtime_exception(e, "null pointer");
	if (peer != NULL)
		return throw_runtime_exception(e, "recorder has been opened!");
	if ((obj = e->NewGlobalRef(wo)) == NULL)
		return throw_runtime_exception(e, "out of memory");
	if ((peer = ATeeveeRecorder_new(obj, my_ATeeveeRecorderCallback,flags)) == NULL) {
		if (obj != NULL)
			e->DeleteGlobalRef(obj);
		LOGE("tvrecorder create failed!");
		return JNI_FALSE;
	}
	e->SetIntField(thiz, g_rec.peer, (int) peer);
	return JNI_TRUE;
}

static jboolean native_prepare(JNIEnv *e, jobject thiz) {
	ATeeveeRecorder* peer = jniGetTeeveeRecorderPeer(e, thiz);
	if (peer)
		return ATeeveeRecorder_prepare(peer) == 0 ? JNI_TRUE : JNI_FALSE;
	return JNI_FALSE;
}

static void native_release(JNIEnv *e, jobject thiz) {
	ATeeveeRecorder* peer = jniGetTeeveeRecorderPeer(e, thiz);
	if (peer){
		ATeeveeRecorder_release(peer);
		ATeeveeRecorder_delete(peer);
		e->SetIntField(thiz, g_rec.peer, 0);
	}
}

extern AStreamSelector* jniGetStreamSelectorPeer(JNIEnv *e, jobject selector);

static jboolean native_set_data_source(JNIEnv *e, jobject thiz, jobject selector, jint flags) {
	ATeeveeRecorder* peer = jniGetTeeveeRecorderPeer(e, thiz);
	if (peer) {
		if (selector) {
			AStreamSelector*ss = jniGetStreamSelectorPeer(e, selector);
			if (ss)
				return ATeeveeRecorder_setDataSource2(peer, ss, flags) == 0 ? JNI_TRUE : JNI_FALSE;
		} else {
			return ATeeveeRecorder_setDataSource2(peer, NULL, flags) == 0 ? JNI_TRUE : JNI_FALSE;
		}
	}
	LOGD("setDataSrc> StreamSelector is invalid");
	return JNI_FALSE;
}

static jboolean native_start(JNIEnv *e, jobject thiz) {
	ATeeveeRecorder* peer = jniGetTeeveeRecorderPeer(e, thiz);
	if (peer)
		return ATeeveeRecorder_start(peer) == 0 ? JNI_TRUE : JNI_FALSE;
	return JNI_FALSE;
}

static void native_stop(JNIEnv *e, jobject thiz) {
	ATeeveeRecorder* peer = jniGetTeeveeRecorderPeer(e, thiz);
	if (peer)
		 ATeeveeRecorder_stop(peer) ;
}

static jboolean native_pause(JNIEnv *e, jobject thiz) {
	ATeeveeRecorder* peer = jniGetTeeveeRecorderPeer(e, thiz);
	if (peer)
		return ATeeveeRecorder_pause(peer) == 0 ? JNI_TRUE : JNI_FALSE;
	return JNI_FALSE;
}

static void native_resume(JNIEnv *e, jobject thiz) {
	ATeeveeRecorder* peer = jniGetTeeveeRecorderPeer(e, thiz);
	if (peer)
		ATeeveeRecorder_resume(peer);
}

static jboolean native_select_program(JNIEnv *e, jobject thiz, jobject jfd, jlong off, jlong len,
		jstring program, jintArray jpids, jint jflags) {
	jboolean ret = JNI_FALSE;
	int pids[64] = { 0 }, n = 0, fd = -1;
	const char*p = NULL;
	ATeeveeRecorder* peer = jniGetTeeveeRecorderPeer(e, thiz);
	if (peer == NULL)
		return throw_runtime_exception(e, "object released");
	if (program != NULL)
		if (!(p = e->GetStringUTFChars(program, NULL)))
			return throw_runtime_exception(e, "Get UTFChars failed");
	fd = jni_native_get_fd(e, jfd);
	if (jpids != NULL) {
		n = e->GetArrayLength(jpids);
		assert(n < 64);
		e->GetIntArrayRegion(jpids, 0, n, (jint*) pids);
	}
	ret = ATeeveeRecorder_selectProgramWithPids(peer, fd, off, len, p, pids, n, jflags) == 0 ?
			JNI_TRUE : JNI_FALSE;
	if (p != NULL)
		e->ReleaseStringUTFChars(program, p);
	return ret;
}

static jboolean native_select_program2(JNIEnv *e, jobject thiz, jobject jfd, jstring program,
		jintArray jpids, jint jflags) {
	jboolean ret = JNI_FALSE;
	const char*p = NULL;
	int pids[64] = { 0 }, n = 0, fd = -1;
	ATeeveeRecorder* peer = jniGetTeeveeRecorderPeer(e, thiz);
	if (peer == NULL)
		return throw_runtime_exception(e, "object released");
	if (program != NULL)
		if (!(p = e->GetStringUTFChars(program, NULL)))
			return throw_runtime_exception(e, "Get UTFChars failed");
	fd = jni_native_get_fd(e, jfd);
	if (jpids != NULL) {
		n = e->GetArrayLength(jpids);
		assert(n < 64);
		e->GetIntArrayRegion(jpids, 0, n, (jint*) pids);
	}
	ret = ATeeveeRecorder_selectProgramWithPids(peer, fd, -1, -1, p, pids, n, jflags) == 0 ?
			JNI_TRUE : JNI_FALSE;
	if (p != NULL)
		e->ReleaseStringUTFChars(program, p);
	return ret;
}
// ----------------------------------------------------------------------------
static const char* g_class_name = "android/media/TeeveeRecorder";
static JNINativeMethod	g_class_methods[] = { //
	{ "native_init", "(Ljava/lang/ref/WeakReference;I)Z", (void*) native_init },//
	{ "native_prepare", "()Z", (void*) native_prepare },//
	{ "native_release", "()V", (void*) native_release },//
	{ "native_set_data_source", "(Landroid/net/telecast/StreamSelector;I)Z",	(void*) native_set_data_source },//
	{ "native_start", "()Z", (void*) native_start },//
	{ "native_stop", "()V", (void*) native_stop },//
	{ "native_pause", "()Z", (void*) native_pause },//
	{ "native_resume", "()V", (void*) native_resume },//
	{ "native_select", "(Ljava/io/FileDescriptor;JJLjava/lang/String;[II)Z",(void*) native_select_program },//
	{ "native_select", "(Ljava/io/FileDescriptor;Ljava/lang/String;[II)Z",(void*) native_select_program2 },//
};

int register_teevee_recorder_natives(JNIEnv *e) {
	jclass cls = e->FindClass(g_class_name);
	if (cls == NULL) {
		LOGE("can't find class : %s", g_class_name);
		return -1;
	}
	g_rec.clazz = (jclass) e->NewGlobalRef(cls);
	if (!(g_rec.peer = e->GetFieldID(g_rec.clazz, "peer", "I"))) {
		LOGE("no such int field : peer");
		return -1;
	}

	LOGI("register_teevee_recorder_natives 1");
	if (!(g_rec.proc = e->GetStaticMethodID(g_rec.clazz, "native_proc",
			"(Ljava/lang/Object;IILjava/lang/String;)V"))) {
		LOGE("no such static method : native_proc");
		return -1;
	}
	LOGI("register_teevee_recorder_natives 2");
	if (!(cls = e->FindClass("android/view/Surface"))) {
		LOGE("Can't find class : android.view.Surface");
		return -1;
	}

	LOGI("register_teevee_recorder_natives 3");
	return AndroidRuntime::registerNativeMethods(e, g_class_name, g_class_methods,
			NELEM(g_class_methods));
}

