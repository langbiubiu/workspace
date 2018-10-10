#define LOAG_TAG "[jni]ipaneltvlib-DvbSubtitle"

#include "common_include.h"

static struct subtitle_info {
	jclass clazz;
	jfieldID peer;
} g_subtitle;

JNIEXPORT jint Java_ipaneltv_toolkit_dvb_DvbSubtitle_ncreate(JNIEnv *e, jobject thiz) {
	LOGD("ncreate");
	return 0;
}

JNIEXPORT void Java_ipaneltv_toolkit_dvb_DvbSubtitle_nrelease(JNIEnv *e,jobject thiz){

}

JNIEXPORT jint Java_ipaneltv_toolkit_dvb_DvbSubtitle_nprepare(JNIEnv *e,
		jobject thiz, jobject sp) {
	return 0;
}

JNIEXPORT jint Java_ipaneltv_toolkit_dvb_DvbSubtitle_nstart(JNIEnv *e,
		jobject thiz, jobject sp) {
	return 0;
}

JNIEXPORT jint Java_ipaneltv_toolkit_dvb_DvbSubtitle_nstop(JNIEnv *e,
		jobject thiz, jobject sp) {
	return 0;
}


int initJavaSubtitle(JNIEnv *e) {
	LOGD("initJavaSubtitle");
	jclass cls;
	LOGD("initJavaSubtitle 11");
	static const char* g_class_name = "ipaneltv/toolkit/dvb/DvbSubtitle";
	if ((cls = (*e)->FindClass(e, g_class_name)) == NULL) {
		LOGE("Can't find class : %s", g_class_name);
		return -1;
	}
	g_subtitle.clazz = (jclass)(*e)->NewGlobalRef(e, cls);
	if ((g_subtitle.peer = (*e)->GetFieldID(e, g_subtitle.clazz, "peer", "I")) == NULL) {
		LOGE("no such field: peer");
		return -1;
	}


	return 0;
}
