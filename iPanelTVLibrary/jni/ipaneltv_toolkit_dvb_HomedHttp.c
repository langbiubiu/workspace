#define LOAG_TAG "[jni]ipaneltvlib-HomedHttp"

#include <sys/stat.h>
#include <sys/types.h>
#include "local_http.h"

#include "common_include.h"

static struct {
	jclass clazz;
	jfieldID peer;
	jmethodID callback;
} g_homedHttp;


JNIEXPORT jint Java_ipaneltv_toolkit_dvb_HomedHttp_ncreate(JNIEnv *e,
		jobject thiz, jobject wo, int flags) {
	int ret = -1;
	struct HttpMgr*p = (struct HttpMgr*) (*e)->GetIntField(e, thiz,
			g_homedHttp.peer);
	LOGD("ncreate p = %p",p);
	if (p)
		return -1;
	if ((p = http_create(flags)) == NULL) {
		LOGD("ncreate http_create error p = %p",p);
		goto BAIL;
	}
	p->wo = (*e)->NewGlobalRef(e, wo);
	(*e)->SetIntField(e, thiz, g_homedHttp.peer, (jint) p);
	ret =0;
	BAIL: if (ret != 0) {
		if (p) {
			http_destroy((int) p);
		}
	}
	LOGD("ncreate ret = %d ",ret);
	return ret;
}

JNIEXPORT jint Java_ipaneltv_toolkit_dvb_HomedHttp_nmake(JNIEnv *e,
		jobject thiz, jstring uri, int flags) {
	int ret = -1;
	const char* curi = NULL;
	struct HttpMgr*p = (struct HttpMgr*) (*e)->GetIntField(e, thiz,
			g_homedHttp.peer);
	LOGD("nmake p = %p ",p);
	if (p) {
		if ((curi = (*e)->GetStringUTFChars(e,uri, NULL))) {
			LOGD("nmake curi = %s",curi);
				ret = http_connect(p,curi);
				(*e)->ReleaseStringUTFChars(e,uri, curi);
				LOGD("nmake ret 1 = %d",ret);
				if(ret < 0 ){
					return -1;
				}
				ret = http_request(p,5000);
				LOGD("nmake ret 2 = %d",ret);
			}
	}
	return ret;
}

JNIEXPORT jint Java_ipaneltv_toolkit_dvb_HomedHttp_ninterrupt(
		JNIEnv *e, jobject thiz, int flags) {
	int ret = -1;
	struct HttpMgr*p = (struct HttpMgr*) (*e)->GetIntField(e, thiz,
				g_homedHttp.peer);
	if (p) {
		ret = http_interrupt(p);
	}
	return ret;
}
JNIEXPORT void Java_ipaneltv_toolkit_dvb_HomedHttp_nrelease(
		JNIEnv *e, jobject thiz) {
	int ret = -1;
	struct HttpMgr*p = (struct HttpMgr*) (*e)->GetIntField(e, thiz,
				g_homedHttp.peer);
	if (p) {
		http_destroy(p);
	}
	return ret;
}

static void jniHomedHttpCallback(void*o, int code, void* msg) {
	JNIEnv* e = attach_jnienv();
	jstring jmsg = NULL;
	jobject wo = (jobject) o;
	assert(wo);
	jmsg = msg != NULL ? (*e)->NewStringUTF(e, (char*) msg) : NULL;
	(*e)->CallStaticVoidMethod(e, g_homedHttp.clazz, g_homedHttp.callback, wo,
			code, jmsg);
	if (jmsg != NULL)
		(*e)->DeleteLocalRef(e, jmsg);
	if (code == 0) { // CLOSED MESSAGE
		(*e)->DeleteGlobalRef(e, wo);
	}
}

int initJavaHomedHttp(JNIEnv *e) {
	jclass cls;
	LOGD("initJavaHomedHttp");
	static const char* g_class_name = "ipaneltv/toolkit/dvb/HomedHttp";
	if ((cls = (*e)->FindClass(e, g_class_name)) == NULL) {
		LOGE("Can't find class : %s", g_class_name);
		return -1;
	}
	g_homedHttp.clazz = (jclass)(*e)->NewGlobalRef(e, cls);
	if ((g_homedHttp.peer = (*e)->GetFieldID(e, g_homedHttp.clazz, "peer", "I"))
			== NULL) {
		LOGE("no such field: peer");
		return -1;
	}
	if ((g_homedHttp.callback = (*e)->GetStaticMethodID(e, cls,
			"native_callback", "(Ljava/lang/Object;ILjava/lang/String;)V"))
			== NULL) {
		LOGE("can't find method native_callback of class: %s", g_class_name);
		return -1;
	}
	return 0;
}

