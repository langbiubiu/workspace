#define LOG_TAG "[jni]SectionFilter"
#include <utils/Log.h>
#include <dlfcn.h>
#include <string.h>
#include <tvs/tvsdex.h>
#include <androidtv/section_filter.h>
#include "native_init.h"


using namespace android;

static struct filter_info {
	jclass clazz;
	jmethodID proc;
	jfieldID peer;
} g_filter;

static void my_ASectionFilterCallback(ASectionFilter*f, void*o, int msg, int p1, void*p2) {
	JNIEnv *env = attach_java_thread("section_filter");
	jobject wo = (jobject) o;
	jlong fv = 0;
	assert(wo != NULL);
	switch (msg) {
	case ASECTIONFILTER_CB_STREAM_LOST:
		msg = 1; //MSG_STREAM_LOST
		break;
	case ASECTIONFILTER_CB_STREAM_TIMEOUT:
		msg = 3; //MSG_TIMEOUT
		break;
	case ASECTIONFILTER_CB_SECTION_ARRIVE:
		ASectionFilter_getFrequency(f, &fv);
		msg = 2; // MSG_SECTION
		break;
	case ASECTIONFILTER_CB_FILTER_CLOSED:
		msg = 0; // MSG_CLOSE
		break;
	case ASECTIONFILTER_CB_FILTER_MSELECT: {
		jlongArray mf = env->NewLongArray(p1);
		env->SetLongArrayRegion(mf, 0, p1, (jlong*) p2);
		msg = 10;
		msg = env->CallStaticIntMethod(g_filter.clazz, g_filter.proc, wo, msg, p1, fv, mf);
		if (msg > 0 && msg < p1)
			((jlong*) p2)[0] = ((jlong*) p2)[msg];
		if(mf != NULL)
			env->DeleteLocalRef(mf);
		return;
	}
	default:
		LOGD("proc> invalid message(%d)!",msg);
		break;
	}

	env->CallStaticIntMethod(g_filter.clazz, g_filter.proc, wo, msg, p1, fv, NULL);
	if (msg == 0) { // CLOSED Message
		env->DeleteGlobalRef(wo);
	}
}

static jboolean native_open(JNIEnv *e, jobject thiz, jobject wo, jlong idm, jlong idl, jint bs, jint flags) {
	jobject obj = NULL;
	ASectionFilter *f = NULL;
	AUUID uuid;
	if (wo == NULL) {
		return throw_runtime_exception(e, "null pointer");
	}
	if ((obj = e->NewGlobalRef(wo)) == NULL) {
		return throw_runtime_exception(e, "null pointer2");
	}
	AUUID_fromMostLeast(&uuid, idm, idl);
	if ((f = ASectionFilter_new(&uuid, bs, flags, (void*) obj, my_ASectionFilterCallback)) == NULL) {
		if (obj != NULL)
			e->DeleteGlobalRef(obj);
		return throw_runtime_exception(e, "can't open section filter");
	}

	e->SetIntField(thiz, g_filter.peer, (int) f);
	return JNI_TRUE;
}

static ASectionFilter* jniGetSectionFilterPeer(JNIEnv *e, jobject thiz) {
	ASectionFilter*p = (ASectionFilter*) e->GetIntField(thiz, g_filter.peer);
	if (p == NULL) {
		LOGD("SectionFilter peer is null");
		throw_runtime_exception(e, "peer is null");
	}
	return p;
}

extern "C" ASectionFilter* JavaSectionFilter_getNativePeer(JNIEnv *e, jobject thiz) {
	return (ASectionFilter*) e->GetIntField(thiz, g_filter.peer);
}

static void native_close(JNIEnv *e, jobject thiz) {
	ASectionFilter *peer = jniGetSectionFilterPeer(e, thiz);
	if (peer != NULL) {
		ASectionFilter_delete(peer);
		e->SetIntField(thiz, g_filter.peer, 0);
	}
}

static jboolean native_config(JNIEnv *e, jobject thiz, jlong freq, jint tout, jint acc, jboolean ca,
		jint ntc_flag) {
	int ret = 0;
	ASectionFilter *peer = jniGetSectionFilterPeer(e, thiz);
	if (peer == NULL)
		return JNI_FALSE;
	switch (acc) {
	case 1://ACCEPT_ONCE
		acc = ASECTIONFILTER_ACCEPT_ONCE;
		break;
	case 2://ACCEPT_ONCE_SOFTCRC
		acc = ASECTIONFILTER_ACCEPT_ONCE_SOFTCRC;
		break;
	case 3://ACCEPT_UPDATED
		acc = ASECTIONFILTER_ACCEPT_UPDATED;
		break;
	case 4://ACCEPT_UPDATED_SOFTCRC
		acc = ASECTIONFILTER_ACCEPT_UPDATED_SOFTCRC;
		break;
	case 5://ACCEPT_ALWAYS
		acc = ASECTIONFILTER_ACCEPT_ALWAYS;
		break;
	default:
		return throw_runtime_exception(e, "illegal acception");
	}

	ret |= ASectionFilter_setFrequency(peer, freq);
	ret |= ASectionFilter_setTimeout(peer, tout);
	ret |= ASectionFilter_setCARequired(peer, (ca == JNI_TRUE ? 1 : 0));
	ret |= ASectionFilter_setAcception(peer, acc);
	ret |= ASectionFilter_setNoTableCheck(peer, ntc_flag);

	return ret ? JNI_FALSE : JNI_TRUE;
}

static jboolean native_start(JNIEnv *e, jobject thiz, jint pid, jbyteArray jc, jbyteArray jm,
		jbyteArray je, jint depth) {
	int ret = -1;
	char coef[32] = { 0 };
	char mask[32] = { 0 };
	char excl[32] = { 0 };

	ASectionFilter *peer = jniGetSectionFilterPeer(e, thiz);
	if (peer == NULL)
		return JNI_FALSE;

	if (pid < 0 || jc == NULL || jm == NULL || je == NULL)
		return JNI_FALSE;

	if (depth > e->GetArrayLength(jc) || depth > e->GetArrayLength(jm) || depth
			> e->GetArrayLength(je)) {
		throw_runtime_exception(e, "invalid param");
		return JNI_FALSE;
	}

	e->GetByteArrayRegion(jc, 0, depth, (jbyte*) coef);
	e->GetByteArrayRegion(jm, 0, depth, (jbyte*) mask);
	e->GetByteArrayRegion(je, 0, depth, (jbyte*) excl);

	ret = ASectionFilter_start(peer, pid, coef, mask, excl, depth);
	return ret == 0 ? JNI_TRUE : JNI_FALSE;
}

static void native_stop(JNIEnv *e, jobject thiz) {
	ASectionFilter *peer = jniGetSectionFilterPeer(e, thiz);
	if (peer == NULL)
		return;
	ASectionFilter_stop(peer);
}

static void native_mquery(JNIEnv *e, jobject thiz) {
	ASectionFilter *peer = jniGetSectionFilterPeer(e, thiz);
	if (peer == NULL)
		return;
	ASectionFilter_queryMonitorable(peer);
}

static jint native_read(JNIEnv *e, jobject thiz, jint off, jbyteArray jbuf, jint boff, jint len) {
	int blen = 0, slen = 0;
	char*a = NULL;
	ASectionFilter *peer = jniGetSectionFilterPeer(e, thiz);
	if (peer == NULL)
		return -1;

	if (jbuf == NULL || off < 0 || boff < 0 || len < 0) {
		throw_runtime_exception(e, "read filter param error");
		return -1;
	}

	if ((blen = e->GetArrayLength(jbuf)) == 0)
		return 0;
	if (boff + len > blen) {
		throw_runtime_exception(e, "offset add length bigger than buflen");
		return -1;
	}

	if ((slen = ASectionFilter_peek(peer, (void**) &a)) < 0) {
		LOGE("native_read> peek data failed");
		return -1;
	}
	assert(a);
	if (off >= slen)
		return 0;

	a += boff;
	slen -= boff;
	len = slen > len ? len : slen;

	e->SetByteArrayRegion(jbuf, off, len, (jbyte*) a);
	return len;
}

// ----------------------------------------------------------------------------
static const char* g_class_name = "android/net/telecast/SectionFilter";
static JNINativeMethod g_class_methods[] = { //
	{ "native_open", "(Ljava/lang/ref/WeakReference;JJII)Z", (void*) native_open },//
	{ "native_close", "()V", (void*) native_close },//
	{ "native_config", "(JIIZI)Z", (void*) native_config },//
	{ "native_start", "(I[B[B[BI)Z", (void*) native_start },//
	{ "native_stop", "()V", (void*) native_stop },//
	{ "native_mquery", "()V", (void*) native_mquery },//
	{ "native_read", "(I[BII)I", (void*) native_read }//
};

//do not throw any exception in this fun
int register_section_filter_natives(JNIEnv *e) {
	jclass cls;
	if (tvsd_ensure_service()) {
		LOGE("section_filter tvsd_ensure_service failed");
		return -1;
	}
	if ((cls = e->FindClass(g_class_name)) == NULL) {
		LOGE("Can't find class : %s", g_class_name);
		return -1;
	}
	g_filter.clazz = (jclass) e->NewGlobalRef(cls);

	if ((g_filter.peer = e->GetFieldID(cls, "peer", "I")) == NULL) {
		LOGE("no such field: peer");
		return -1;
	}
	if ((g_filter.proc = e->GetStaticMethodID(g_filter.clazz, "native_proc", "(Ljava/lang/Object;IIJLjava/lang/Object;)I"))
			== NULL) {
		LOGE("no such method: native_proc");
		return -1;
	}

	return AndroidRuntime::registerNativeMethods(e, g_class_name, g_class_methods,
			NELEM(g_class_methods));
}


