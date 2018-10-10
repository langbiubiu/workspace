#define LOG_TAG "[jni]TeeveeCapturer"
#include <utils/Log.h>
#include <tvs/tvsdex.h>
#include <dlfcn.h>
#include <string.h>
#include <androidtv/teevee_capturer.h>

#include "native_init.h"

using namespace android;

static struct {
	jclass clazz;
	jfieldID peer;
	jmethodID proc;
} g_capturer;

static struct {
	jclass clazz;
	jfieldID peer;
} g_bitmap;

static void my_ATeeveeCaptuerCallback(ATeeveeCapturer*ss, void*o, int msg, int p1, void *p2) {
	JNIEnv *e = attach_java_thread("capturer-jni");
	LOGD("my_ATeeveeCaptuerCallback msg = %d", msg);
	jobject wo = (jobject) o;
	assert(wo);
	switch (msg) {
	case ATEEVEECAPTURER_CB_CLOSE:
		e->DeleteGlobalRef(wo);
		break;
	case ATEEVEECAPTURER_CB_START:
		e->CallStaticVoidMethod(g_capturer.clazz, g_capturer.proc, wo, 1, p1, NULL);
		break;
	case ATEEVEECAPTURER_CB_TIMEOUT:
		e->CallStaticVoidMethod(g_capturer.clazz, g_capturer.proc, wo, 2, p1, NULL);
		break;
	case ATEEVEECAPTURER_CB_OVER: {
		jstring s = p2 != NULL ? e->NewStringUTF((const char*) p2) : NULL;
		e->CallStaticVoidMethod(g_capturer.clazz, g_capturer.proc, wo, 3, p1, s);
		if (s != NULL)
			e->DeleteLocalRef(s);
		break;
	}
	default:
		break;
	}
}

static ATeeveeCapturer* jniGetTeeveeCapturerPeer(JNIEnv *e, jobject thiz) {
	ATeeveeCapturer*p = (ATeeveeCapturer*) e->GetIntField(thiz, g_capturer.peer);
	if (p == NULL) {
		LOGD("TeeveeCaptuer peer is null");
		throw_runtime_exception(e, "peer is null");
	}
	return p;
}

extern "C" ATeeveeCapturer* JavaTeeveeCapturer_getNativePeer(JNIEnv *e, jobject thiz) {
	return (ATeeveeCapturer*) e->GetIntField(thiz, g_capturer.peer);
}

static jboolean native_init(JNIEnv *e, jobject thiz, jobject wo, jint type, jint flags) {
	jobject obj = NULL;
	LOGD("create capturer framer ...");
	ATeeveeCapturer*peer = (ATeeveeCapturer*) e->GetIntField(thiz, g_capturer.peer);
	if (wo == NULL)
		return throw_runtime_exception(e, "null pointer");
	if (peer != NULL)
		return throw_runtime_exception(e, "capturer has been opened!");
	if ((obj = e->NewGlobalRef(wo)) == NULL)
		return throw_runtime_exception(e, "out of memory");
	if ((peer = ATeeveeCapturer_new(obj, my_ATeeveeCaptuerCallback, type, flags)) == NULL) {
		if (obj != NULL)
			e->DeleteGlobalRef(obj);
		LOGE("native_init create failed!");
		return JNI_FALSE;
	}
	e->SetIntField(thiz, g_capturer.peer, (int) peer);
	return JNI_TRUE;
}

static void native_release(JNIEnv *e, jobject thiz) {
	ATeeveeCapturer* peer = jniGetTeeveeCapturerPeer(e, thiz);
	if (peer) {
		ATeeveeCapturer_delete(peer);
		e->SetIntField(thiz, g_capturer.peer, 0);
	}
}

static jint native_alloc_id(JNIEnv *e, jobject thiz) {
	int id = -1;
	ATeeveeCapturer* peer = jniGetTeeveeCapturerPeer(e, thiz);
	if (peer) {
		if (ATeeveeCapturer_allocId(peer, &id) == 0)
			return id;
	}
	return -1;
}

static void native_cancel_id(JNIEnv *e, jobject thiz) {
	ATeeveeCapturer* peer = jniGetTeeveeCapturerPeer(e, thiz);
	if (peer) {
		ATeeveeCapturer_cancelId(peer);
	}
}

static void* jniGetSkBitmapFromJava(JNIEnv *e, jobject obj) {
	if (g_bitmap.clazz == NULL) {
		jclass cls = e->FindClass("android/graphics/Bitmap");
		if (cls)
			g_bitmap.peer = e->GetFieldID(cls, "mNativeBitmap", "I");
	}
	if (g_bitmap.peer)
		return (void*) e->GetIntField(obj, g_bitmap.peer);
	return NULL;
}

static jboolean native_dup_bitmap(JNIEnv *e, jobject thiz, jobject jb) {
	void* b;
	ATeeveeCapturer* peer = jniGetTeeveeCapturerPeer(e, thiz);
	if (peer && jb ? (b = jniGetSkBitmapFromJava(e, jb)) : 0)
		return ATeeveeCapturer_toBitmap(peer, b) == 0 ? JNI_TRUE : JNI_FALSE;
	return JNI_FALSE;
}

static jstring native_save_file(JNIEnv *e, jobject thiz, jstring pathWithoutExtName) {
	jstring ret = NULL;
	char full_path[256];
	char suffix_name[16] = { 0 };
	const char*path = NULL;
	ATeeveeCapturer* peer = jniGetTeeveeCapturerPeer(e, thiz);
	if (peer) {
		if ((path = e->GetStringUTFChars(pathWithoutExtName, NULL)) == NULL)
			goto BAIL;
		LOGD("native_save_file path = %s", path);
		memset(suffix_name, 0x00, sizeof(suffix_name));
		memset(full_path, 0x00, sizeof(full_path));
		if (ATeeveeCapturer_saveFile(peer, path, suffix_name) == 0) {
			snprintf(full_path, sizeof(full_path), "%s.%s", path, suffix_name);
			LOGD("save_file>path=%s", full_path);
			ret = e->NewStringUTF(full_path);
		}
	}
	BAIL: if (path)
		e->ReleaseStringUTFChars(pathWithoutExtName, path);
	return ret;
}

static const char* g_class_name = "android/media/TeeveeCapturer";

static JNINativeMethod g_class_methods[] = { //
	{ "native_init", "(Ljava/lang/ref/WeakReference;II)Z",(void*) native_init },//
	{ "native_release", "()V", (void*) native_release },//
	{ "native_alloc_id", "()I", (void*) native_alloc_id },//
	{ "native_cancel_id", "()V", (void*) native_cancel_id },//
	{ "native_dup_bitmap", "(Landroid/graphics/Bitmap;)Z",(void*) native_dup_bitmap }, //
	{ "native_save_file", "(Ljava/lang/String;)Ljava/lang/String;",(void*) native_save_file }, //
};

int register_teevee_caputer_natives(JNIEnv *e) {
	jclass cls = e->FindClass(g_class_name);
	if (cls == NULL) {
		LOGE("can't find class : %s", g_class_name);
		return -1;
	}
	g_capturer.clazz = (jclass) e->NewGlobalRef(cls);
	if (!(g_capturer.peer = e->GetFieldID(g_capturer.clazz, "peer", "I"))) {
		LOGE("no such int field : peer");
		return -1;
	}

	if (!(g_capturer.proc = e->GetStaticMethodID(g_capturer.clazz, "native_proc",
			"(Ljava/lang/Object;IILjava/lang/String;)V"))) {
		LOGE("no such static method : native_proc");
		return -1;
	}
	memset(&g_bitmap, 0, sizeof(g_bitmap));
	return AndroidRuntime::registerNativeMethods(e, g_class_name, g_class_methods,
			NELEM(g_class_methods));
}

