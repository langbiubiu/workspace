#define LOG_TAG "[jni]SectionInjector"
#include <utils/Log.h>
#include <dlfcn.h>
#include <string.h>
#include <tvs/tvsdex.h>
#include <androidtv/section_prefetcher.h>
#include "native_init.h"

using namespace android;

static struct injector_info {
	jclass clazz;
	jfieldID peer;
} g_prefetcher;

static ASectionPrefetcher* jniGetSectionPrefectcherPeer(JNIEnv *e, jobject thiz) {
	ASectionPrefetcher*p = (ASectionPrefetcher*) e->GetIntField(thiz, g_prefetcher.peer);
	if (p == NULL) {
		LOGD("SectionPrefetcher peer is null");
		throw_runtime_exception(e, "peer is null");
	}
	return p;
}

extern "C" ASectionPrefetcher* JavaSectionPrefetcher_getNativePeer(JNIEnv *e, jobject thiz) {
	return (ASectionPrefetcher*) e->GetIntField(thiz, g_prefetcher.peer);
}

static jboolean native_create(JNIEnv *e, jobject thiz, jlong idm, jlong idl, jstring jn, jint flags) {
	int ret = -1;
	const char*name = NULL;
	AUUID uuid;
	ASectionPrefetcher*f = (ASectionPrefetcher*) e->GetIntField(thiz, g_prefetcher.peer);
	assert(f == NULL);
	if ((name = e->GetStringUTFChars(jn, NULL))) {
		AUUID_fromMostLeast(&uuid, idm, idl);
		if ((f = ASectionPrefetcher_new(&uuid, name, flags)) != NULL) {
			e->SetIntField(thiz, g_prefetcher.peer, (int) f);
			ret = 0;
		}
		e->ReleaseStringUTFChars(jn, name);
	}
	return ret;
}

static void native_release(JNIEnv *e, jobject thiz, jboolean b) {
	ASectionPrefetcher*peer = jniGetSectionPrefectcherPeer(e, thiz);
	if (peer != NULL) {
		ASectionPrefetcher_delete(peer, b);
		e->SetIntField(thiz, g_prefetcher.peer, 0);
	}
}

static jint native_getsmf(JNIEnv *e, jobject thiz, jstring n, jint flags) {
	int ret = -1;
	const char*sfp = NULL;
	ASectionPrefetcher*peer = jniGetSectionPrefectcherPeer(e, thiz);
	if (peer == NULL)
		return -1;
	if (n)
		if ((sfp = e->GetStringUTFChars(n, NULL)) == NULL)
			return -1;
	ret = ASectionPrefetcher_getMemoryFile(peer, sfp, flags);
	if (sfp != NULL)
		e->ReleaseStringUTFChars(n, sfp);
	return ret;
}

static jint native_schedule(JNIEnv *e, jobject thiz, jint ifid, jstring fp, jint bs, jint pid,
		jbyteArray jcoef, jbyteArray jmask, jbyteArray jexcl, jint depth, jint flags) {
	int ret = -1;
	const char*sfp;
	char coef[32], mask[32], excl[32];
	ASectionPrefetcher*peer = jniGetSectionPrefectcherPeer(e, thiz);
	if (peer == NULL || ifid < 0)
		return -1;
	if (fp == NULL || depth <= 0 || depth >= 32)
		return -1;
	if ((sfp = e->GetStringUTFChars(fp, NULL)) == NULL)
		return -1;
	memset(coef, 0, sizeof(coef));
	memset(mask, 0, sizeof(mask));
	memset(excl, 0, sizeof(excl));
	e->GetByteArrayRegion(jcoef, 0, depth, (jbyte*) coef);
	e->GetByteArrayRegion(jmask, 0, depth, (jbyte*) mask);
	e->GetByteArrayRegion(jexcl, 0, depth, (jbyte*) excl);
	ret = ASectionPrefetcher_scheduleFiltering(peer, ifid, sfp, bs, pid, coef, mask, excl, depth, flags);
	if (sfp != NULL)
		e->ReleaseStringUTFChars(fp, sfp);
	return ret;
}
static jint native_fsched(JNIEnv *e, jobject thiz, jobject jfd, jint flags) {
	int fd = -1;
	ASectionPrefetcher*peer = jniGetSectionPrefectcherPeer(e, thiz);
	if (peer == NULL)
		return -1;
	if ((fd = jni_native_get_fd(e, jfd)) < 0) {
		LOGD("native_fsched>invalid FileDescriptor");
		return JNI_FALSE;
	}
	return ASectionPrefetcher_scheduleFd(peer, fd, flags);
}
static jint native_bsched(JNIEnv *e, jobject thiz, jstring buf, jint flags) {
	int ret = -1;
	const char*sbuf = NULL;
	ASectionPrefetcher*peer = jniGetSectionPrefectcherPeer(e, thiz);
	if (peer && buf) {
		if ((sbuf = e->GetStringUTFChars(buf, NULL)) == NULL)
			return -1;
		ret = ASectionPrefetcher_schedule(peer, sbuf, strlen(sbuf), flags);
		if (sbuf != NULL)
			e->ReleaseStringUTFChars(buf, sbuf);
	}
	return ret;
}
static jint native_is_scheduled(JNIEnv *e, jobject thiz) {
	ASectionPrefetcher*peer = jniGetSectionPrefectcherPeer(e, thiz);
	if (peer == NULL)
		return -1;
	return ASectionPrefetcher_isScheduled(peer);
}
static jint native_cancel(JNIEnv *e, jobject thiz) {
	ASectionPrefetcher*peer = jniGetSectionPrefectcherPeer(e, thiz);
	if (peer == NULL)
		return -1;
	return ASectionPrefetcher_cancel(peer);
}
// ----------------------------------------------------------------------------
static const char* g_class_name = "android/net/telecast/SectionPrefetcher";
static JNINativeMethod g_class_methods[] = { //
	{ "native_create",  "(JJLjava/lang/String;I)I", (void*) native_create },//
	{ "native_release", "(Z)V", (void*) native_release },//
	{ "native_getsmf",    "(Ljava/lang/String;I)I", (void*) native_getsmf },//
	{ "native_sched",   "(ILjava/lang/String;II[B[B[BII)I", (void*) native_schedule },//
	{ "native_fsched",    "(Ljava/io/FileDescriptor;I)I", (void*) native_fsched },//
	{ "native_bsched",    "(Ljava/lang/String;I)I", (void*) native_bsched },//
	{ "native_cancel",    "()I", (void*) native_cancel },//
	{ "native_is_sche", "()I", (void*) native_is_scheduled }//
};

int register_section_prefetcher_natives(JNIEnv *e) {
	jclass cls;
	if ((cls = e->FindClass(g_class_name)) == NULL) {
		LOGE("Can't find class : %s", g_class_name);
		return -1;
	}
	g_prefetcher.clazz = (jclass) e->NewGlobalRef(cls);
	if ((g_prefetcher.peer = e->GetFieldID(cls, "peer", "I")) == NULL) {
		LOGE("no such field: peer");
		return -1;
	}
	return AndroidRuntime::registerNativeMethods(e, g_class_name, g_class_methods,
			NELEM(g_class_methods));
}

