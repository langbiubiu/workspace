#define LOG_TAG "[jni]TeeveePlayer"
#include <utils/Log.h>
#include <tvs/tvsdex.h>
#include <androidtv/teevee_player.h>
#include <dlfcn.h>
#include <string.h>

#include "native_init.h"

using namespace android;

static struct {
	jclass clazz;
	jfieldID peer;
	jmethodID proc;
} g_player;

static void my_ATeeveePlayerCallback(ATeeveePlayer*ss, void*o, int msg, int p1, void*p2) {
	JNIEnv *e = attach_java_thread("tvplayer-jni");
	jobject wo = (jobject) o;
	assert(wo);
	switch (msg) {
	case ATEEVEEPLAYER_CB_PLAY_PROCESSING: {
		ATeeveePlayerParam *p = (ATeeveePlayerParam*) p2;
		if(p->pts_brust_flag == 1){
			e->CallStaticVoidMethod(g_player.clazz, g_player.proc, wo, 1, p1, NULL, p->pts_time);
		}
		break;
	}
	case ATEEVEEPLAYER_CB_PLAY_SUSPENDING:
		e->CallStaticVoidMethod(g_player.clazz, g_player.proc, wo, 2, p1, NULL, 0);
		break;
	case ATEEVEEPLAYER_CB_PLAY_ERROR: {
		jstring s = p2 ? e->NewStringUTF((const char*) p2) : NULL;
		e->CallStaticVoidMethod(g_player.clazz, g_player.proc, wo, 3, p1, s, 0);
		if (s != NULL)
			e->DeleteLocalRef(s);
		break;
	}
	case ATEEVEEPLAYER_CB_PLAY_SELECT_START:
		e->CallStaticVoidMethod(g_player.clazz, g_player.proc, wo, 4, p1, NULL, 0);
		break;
	case ATEEVEEPLAYER_CB_PROGRAM_RESELECT: {
		jstring s = p2 ? e->NewStringUTF((const char*) p2) : NULL;
		e->CallStaticVoidMethod(g_player.clazz, g_player.proc, wo, 8, p1, s, 0);
		if (s != NULL)
			e->DeleteLocalRef(s);
		break;
	}
	case ATEEVEEPLAYER_CB_PROGRAM_DISCONTINUED:
		e->CallStaticVoidMethod(g_player.clazz, g_player.proc, wo, 9, p1, 0, NULL, 0);
		break;
	case MSG_HANDLE_CLOSED:
		e->CallStaticVoidMethod(g_player.clazz, g_player.proc, wo, 0, 0, 0, NULL, 0);
		e->DeleteGlobalRef(wo);
		break;
	default:
		break;
	}
}

static ATeeveePlayer* jniGetTeeveePlayerPeer(JNIEnv *e, jobject thiz) {
	ATeeveePlayer*p = (ATeeveePlayer*) e->GetIntField(thiz, g_player.peer);
	if (p == NULL) {
		LOGD("TeeveePlayer peer is null");
		throw_runtime_exception(e, "peer is null");
	}
	return p;
}

extern "C" ATeeveePlayer* JavaTeeveePlayer_getNativePeer(JNIEnv *e, jobject thiz) {
	return (ATeeveePlayer*) e->GetIntField(thiz, g_player.peer);
}

static jboolean native_init(JNIEnv *e, jobject thiz, jobject wo, jobject jbase, jint pipsize,
		jint flags) {
	jobject obj = NULL;
	LOGD("create teevee_player init ...");
	ATeeveePlayer*peer = (ATeeveePlayer*) e->GetIntField(thiz, g_player.peer);
	ATeeveePlayer*base = jbase ? (ATeeveePlayer*) e->GetIntField(jbase, g_player.peer) : NULL;
	if (wo == NULL)
		return throw_runtime_exception(e, "null pointer");
	if (peer != NULL)
		return throw_runtime_exception(e, "player has been opened!");
	if ((obj = e->NewGlobalRef(wo)) == NULL)
		return throw_runtime_exception(e, "out of memory");
	if (base == NULL && pipsize >= 0) {
		peer = ATeeveePlayer_new(pipsize, flags, obj, my_ATeeveePlayerCallback);
	} else if (base && pipsize == -1) {
		peer = ATeeveePlayer_newPip(base, flags, obj, my_ATeeveePlayerCallback);
	}

	if (peer) {
		e->SetIntField(thiz, g_player.peer, (int) peer);
		return JNI_TRUE;
	}
	if (obj != NULL)
		e->DeleteGlobalRef(obj);
	LOGE("teevee_player create failed!");
	return JNI_FALSE;
}

static jboolean native_prepare(JNIEnv *e, jobject thiz) {
	ATeeveePlayer* peer = jniGetTeeveePlayerPeer(e, thiz);
	if (peer)
		return ATeeveePlayer_prepare(peer) == 0 ? JNI_TRUE : JNI_FALSE;
	return JNI_FALSE;
}

static jboolean native_clear(JNIEnv *e, jobject thiz) {
	ATeeveePlayer* peer = jniGetTeeveePlayerPeer(e, thiz);
	if (peer)
		return ATeeveePlayer_clear(peer) == 0 ? JNI_TRUE : JNI_FALSE;
	return JNI_FALSE;
}

static void native_release(JNIEnv *e, jobject thiz) {
	ATeeveePlayer* peer = jniGetTeeveePlayerPeer(e, thiz);
	if (peer) {
		ATeeveePlayer_release(peer);
		ATeeveePlayer_delete(peer);
		e->SetIntField(thiz, g_player.peer, 0);
	}
}

static jboolean native_set_weak_mode(JNIEnv *e, jobject thiz, jboolean b) {
	ATeeveePlayer* peer = jniGetTeeveePlayerPeer(e, thiz);
	if (peer)
		return ATeeveePlayer_setWeakMode(peer, b) == 0 ? JNI_TRUE : JNI_FALSE;
	return JNI_FALSE;
}

static jboolean native_set_volume(JNIEnv *e, jobject thiz, jfloat leftVolume, jfloat rightVolume) {
	ATeeveePlayer* peer = jniGetTeeveePlayerPeer(e, thiz);
	if (peer)
		return ATeeveePlayer_setVolume(peer, leftVolume, rightVolume) == 0 ? JNI_TRUE : JNI_FALSE;
	return JNI_FALSE;
}

extern AStreamSelector* jniGetStreamSelectorPeer(JNIEnv *e, jobject selector);

static jboolean native_set_data_source(JNIEnv *e, jobject thiz, jobject selector) {
	ATeeveePlayer* peer = jniGetTeeveePlayerPeer(e, thiz);
	AStreamSelector*ss = NULL;
	if (peer == NULL)
		return JNI_FALSE;
	if (selector == NULL)
		return ATeeveePlayer_setDataSource(peer, NULL) == 0 ? JNI_TRUE : JNI_FALSE;

	if ((ss = jniGetStreamSelectorPeer(e, selector)) != NULL)
		return ATeeveePlayer_setDataSource(peer, ss) == 0 ? JNI_TRUE : JNI_FALSE;
	return JNI_FALSE;
}

static jboolean native_set_display(JNIEnv *e, jobject thiz, jint x, jint y, jint w, jint h) {
	ATeeveePlayer* peer = jniGetTeeveePlayerPeer(e, thiz);
	if (peer)
		return ATeeveePlayer_setDisplay(peer, x, y, w, h) == 0 ? JNI_TRUE : JNI_FALSE;
	return JNI_FALSE;
}

static jboolean native_set_freeze(JNIEnv *e, jobject thiz,jboolean b,jint flags){
	ATeeveePlayer* peer = jniGetTeeveePlayerPeer(e, thiz);
	if (peer)
		return ATeeveePlayer_setFreeze(peer, b == JNI_TRUE ? 1 : 0, flags) == 0 ? JNI_TRUE : JNI_FALSE;
	return JNI_FALSE;
}

static jboolean native_start(JNIEnv *e, jobject thiz) {
	ATeeveePlayer* peer = jniGetTeeveePlayerPeer(e, thiz);
	if (peer)
		return ATeeveePlayer_start(peer) == 0 ? JNI_TRUE : JNI_FALSE;
	return JNI_FALSE;
}

static jboolean native_stop(JNIEnv *e, jobject thiz) {
	ATeeveePlayer* peer = jniGetTeeveePlayerPeer(e, thiz);
	if (peer)
		return ATeeveePlayer_stop(peer) == 0 ? JNI_TRUE : JNI_FALSE;
	return JNI_FALSE;
}

static jboolean native_pause(JNIEnv *e, jobject thiz) {
	ATeeveePlayer* peer = jniGetTeeveePlayerPeer(e, thiz);
	if (peer)
		return ATeeveePlayer_pause(peer) == 0 ? JNI_TRUE : JNI_FALSE;
	return JNI_FALSE;
}

static jboolean native_resume(JNIEnv *e, jobject thiz) {
	ATeeveePlayer* peer = jniGetTeeveePlayerPeer(e, thiz);
	if (peer)
		ATeeveePlayer_resume(peer);
	return JNI_TRUE;
}

static jboolean native_select_program(JNIEnv *e, jobject thiz, jstring program, jobject jfd,
		jint flags) {
	int fd = -1;
	jboolean ret = JNI_FALSE;
	const char*p = NULL;
	ATeeveePlayer* peer = jniGetTeeveePlayerPeer(e, thiz);
	if (peer == NULL)
		return JNI_FALSE;
	if (program != NULL)
		if (!(p = e->GetStringUTFChars(program, NULL)))
			return throw_runtime_exception(e, "");
	if (jfd) {
		if ((fd = jniGetFDFromFileDescriptor(e, jfd)) < 0) {
			LOGD("invalid FileDescriptor");
			return JNI_FALSE;
		} else {
			LOGD("native_select_program > fd = %d",fd);
		}
	}
	ret = ATeeveePlayer_selectProgram(peer, p, flags) == 0 ? JNI_TRUE : JNI_FALSE;
	if (p)
		e->ReleaseStringUTFChars(program, p);
	return ret;
}

static jboolean native_set_profile(JNIEnv *e, jobject thiz, jstring type) {
	jboolean ret = JNI_FALSE;
	const char*p = NULL;
	ATeeveePlayer* peer = jniGetTeeveePlayerPeer(e, thiz);
	if (peer == NULL)
		return JNI_FALSE;
	if (type == NULL) {
		throw_runtime_exception(e, "null pointer");
		return JNI_FALSE;
	}
	if (!(p = e->GetStringUTFChars(type, NULL)))
		return JNI_FALSE;
	ret = ATeeveePlayer_setProfile(peer, p);
	if(p)
		e->ReleaseStringUTFChars(type, p);
	return ret;
}

static jlong native_play_time(JNIEnv *e, jobject thiz) {
	int64_t t = -1;
	ATeeveePlayer* peer = jniGetTeeveePlayerPeer(e, thiz);
	if (peer) {
		if (ATeeveePlayer_getPlayTime(peer, &t) == 0)
			return t;
	}
	return -1;
}

static jboolean native_capture_video(JNIEnv *e, jobject thiz, int id) {
	ATeeveePlayer* peer = jniGetTeeveePlayerPeer(e, thiz);
	if (peer) {
		return ATeeveePlayer_captureVideoFrame(peer, id) == 0 ? JNI_TRUE
				: JNI_FALSE;
	}
	return false;
}

static jint native_load_a(JNIEnv *e, jobject thiz, jobject jfd) {
	int fd = -1;
	ATeeveePlayer* peer = jniGetTeeveePlayerPeer(e, thiz);
	if (peer == NULL)
		return -1;
	if (jfd > 0) { // 如果fd为-1则取消之前加载的脚本
		if ((fd = jni_native_get_fd(e, jfd)) < 0) {
			LOGD("native_load_a>invalid FileDescriptor");
			return JNI_FALSE;
		}
	}
	return ATeeveePlayer_loadAnimation(peer, fd) == 0 ? JNI_TRUE : JNI_FALSE;
}

static void native_act_a(JNIEnv *e, jobject thiz, jint action, jint p1, jint p2, jint flags) {
	ATeeveePlayer* peer = jniGetTeeveePlayerPeer(e, thiz);
	if (peer)
		ATeeveePlayer_actAnimation(peer, action, p1, p2, flags);
}
static jboolean native_recv_subt(JNIEnv *e, jobject thiz, jobject jfd) {
	int fd = -1;
	ATeeveePlayer* peer = jniGetTeeveePlayerPeer(e, thiz);
	if (peer == NULL)
		return JNI_FALSE;
	if (jfd == NULL) {
		if ((fd = jniGetFDFromFileDescriptor(e, jfd)) < 0) {
			LOGD("invalid FileDescriptor");
			return JNI_FALSE;
		}
	}
	return ATeeveePlayer_recvSubtitle(peer, fd) == 0 ? JNI_TRUE : JNI_FALSE;
}
// ----------------------------------------------------------------------------
static const char* g_class_name = "android/media/TeeveePlayer";
static JNINativeMethod g_class_methods[] = { //
	{ "native_init", "(Ljava/lang/ref/WeakReference;Ljava/lang/Object;II)Z", (void*) native_init },//
	{ "native_prepare", "()Z", (void*) native_prepare },//
	{ "native_clear", "()Z", (void*) native_clear },//
	{ "native_release", "()V", (void*) native_release },//
	{ "native_set_weak_mode", "(Z)Z", (void*) native_set_weak_mode },//
	{ "native_set_volume", "(FF)Z",(void*) native_set_volume },//
	{ "native_set_data_source", "(Landroid/net/telecast/StreamSelector;)Z", (void*) native_set_data_source },//
	{ "native_set_display", "(IIII)Z",(void*) native_set_display },//
	{ "native_set_freeze", "(ZI)Z",(void*) native_set_freeze }, //
	{ "native_start", "()Z", (void*) native_start },//
	{ "native_stop", "()Z", (void*) native_stop },//
	{ "native_pause", "()Z", (void*) native_pause },//
	{ "native_resume", "()Z", (void*) native_resume },//
	{ "native_select_program", "(Ljava/lang/String;Ljava/io/FileDescriptor;I)Z",(void*) native_select_program },//
	{ "native_set_profile", "(Ljava/lang/String;)Z", (void*) native_set_profile },//
	{ "native_play_time", "()J", (void*) native_play_time },
	{ "native_capture_video", "(I)Z",(void*)native_capture_video},
	{ "native_recv_subt", "(Ljava/io/FileDescriptor;)Z",(void*)native_recv_subt},
	{ "native_load_a", "(Ljava/io/FileDescriptor;)I", (void*) native_load_a },
	{ "native_act_a", "(IIII)V", (void*) native_act_a }
};

int register_teevee_player_natives(JNIEnv *e) {
	jclass cls = e->FindClass(g_class_name);
	if (cls == NULL) {
		LOGE("can't find class : %s", g_class_name);
		return -1;
	}
	g_player.clazz = (jclass) e->NewGlobalRef(cls);
	if (!(g_player.peer = e->GetFieldID(g_player.clazz, "peer", "I"))) {
		LOGE("no such int field : peer");
		return -1;
	}

	LOGI("register_teevee_player_natives 1");
	if (!(g_player.proc = e->GetStaticMethodID(g_player.clazz, "native_proc",
			"(Ljava/lang/Object;IILjava/lang/String;J)V"))) {
		LOGE("no such static method : native_proc");
		return -1;
	}
	LOGI("register_teevee_player_natives 2");
	if (!(cls = e->FindClass("android/view/Surface"))) {
		LOGE("Can't find class : android.view.Surface");
		return -1;
	}

	LOGI("register_teevee_player_natives 3");
	return AndroidRuntime::registerNativeMethods(e, g_class_name, g_class_methods,
			NELEM(g_class_methods));
}

