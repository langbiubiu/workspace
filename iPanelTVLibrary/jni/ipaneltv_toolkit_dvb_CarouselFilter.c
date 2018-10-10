#define LOAG_TAG "[jni]ipaneltvlib-carousel"

#include <sys/stat.h>
#include <sys/types.h>
#include <dirent.h>
#include <sys/mman.h>

#include "common_include.h"

JNIEXPORT jint Java_ipaneltv_toolkit_dvb_CarouselFilter_native_create(JNIEnv *e, jobject thiz,
		jobject wo, long idm, long idl, int bs, int flags) {
	return -1;//TODO
}

JNIEXPORT void Java_ipaneltv_toolkit_dvb_CarouselFilter_native_release(JNIEnv *e, jobject thiz) {

}

JNIEXPORT jint Java_ipaneltv_toolkit_dvb_CarouselFilter_native_start(JNIEnv *e, jobject thiz,
		jlong freq, int pid, int coef, int mask) {
	return -1;//TODO
}
JNIEXPORT void Java_ipaneltv_toolkit_dvb_CarouselFilter_native_stop(JNIEnv *e, jobject thiz) {
}

JNIEXPORT jint Java_ipaneltv_toolkit_dvb_CarouselFilter_native_section(JNIEnv *e, jobject thiz,
		int t, int addr, int len) {
	return -1;//TODO
}
JNIEXPORT jint Java_ipaneltv_toolkit_dvb_CarouselFilter_native_look_mod(JNIEnv *e, jobject thiz,
		int mid, int v) {
	return -1;//TODO
}

JNIEXPORT jint Java_ipaneltv_toolkit_dvb_CarouselFilter_native_load_mod(JNIEnv *e, jobject thiz,
		int mid, int p, int len, int flags) {
	return -1;//TODO
}

JNIEXPORT jint Java_ipaneltv_toolkit_dvb_CarouselFilter_native_save_mod(JNIEnv *e, jobject thiz,
		int mid, jobject jfd, jlong seek, int flags) {
	return -1;//TODO
}

JNIEXPORT jint Java_ipaneltv_toolkit_dvb_CarouselFilter_native_drop_mod(JNIEnv *e, jobject thiz,
		int mid) {
	return -1;//TODO
}

int initJavaCarouselFilter(JNIEnv *e) {
	return 0;
}

