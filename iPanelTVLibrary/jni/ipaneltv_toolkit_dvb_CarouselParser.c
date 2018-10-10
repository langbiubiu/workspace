#include "stdio.h"
#include <string.h>
#include <jni.h>
#include <assert.h>
#include "common_include.h"

#ifdef LOG_TAG
#undef LOG_TAG
#define LOG_TAG "dsmcc_native"
#endif

JNIEXPORT jint Java_ipaneltv_toolkit_dvb_CarouselParser_nParseDiiSection(JNIEnv* env, jclass clazz,
		jint section, jint len, jint meta, int mlen, int type) {
//	return ACarouselUtil_readDiiSection((char *) section, len, (char *) meta, mlen, type);
	return -1;
}

JNIEXPORT jint Java_ipaneltv_toolkit_dvb_CarouselParser_nParseDcModuleSection(JNIEnv* env,
		jclass clazz, jint section, jint len, jint fd) {
//	int mlen = 4096;
//	char* meta = (char*) malloc(sizeof(char *) * mlen);
//	int rmlen = ACarouselUtil_readDcModuleSection((char *) section, len, (char*) meta, mlen);
//	LOGI("parseModule  mlen =%d,rmlen=%d.\n", mlen, rmlen);
//	if (rmlen > 0) {
//		int ret = write(fd, meta, rmlen);
//		LOGI("parseModule  write ret =%d,strlen=%d\n", ret, rmlen);
//	}
//	free(meta);

	return 0;
}

JNIEXPORT jint Java_ipaneltv_toolkit_dvb_CarouselParser_nParseOcModuleSection(JNIEnv* env,
		jclass clazz, int section, int len, int mpos) {
//	int mlen = 4096;
//	int clen = 4096;
//	char* meta = (char*) malloc(sizeof(char *) * mlen);
//	char* content = (char*) malloc(sizeof(char *) * clen);
//	int rmlen = ACarouselUtil_readOcModuleSection((char *) section, len, (char*) meta, mlen,
//			(char*) content, clen, mpos);
//	LOGI("parseModule  mlen =%d,rmlen=%d.\n", mlen, rmlen);
////	if (rmlen > 0) {
//////		int ret = write(fd, meta, rmlen);
////		LOGI("parseModule  write ret =%d,strlen=%d\n", ret, rmlen);
////	}
//	free(meta);
//	free(content);
	return 0;
}

