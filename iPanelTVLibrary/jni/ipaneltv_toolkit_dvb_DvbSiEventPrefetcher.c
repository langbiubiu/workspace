#define LOAG_TAG "[jni]ipaneltvlib-eitprefetch"

#include <sys/stat.h>
#include <sys/types.h>
#include <sys/mman.h>
#include <unistd.h>
#include <cutils/ashmem.h>
#include <androidtv/section_prefetcher.h>
#include <androidtv/section_utils.h>
#include <androidtv/native_utils.h>
#include <androidtv/section_filter.h>
#include <errno.h>
#include "common_include.h"

#define SERVICEID_LENGTH			10240
#define SECTION_NUMBER_LENGTH		512
#define TABLE_ID                 16

static struct prefecher_info {
	jclass clazz;
	jfieldID peer;
} g_prefetcher;

//static struct filter_info {
//	jclass clazz;
//	jfieldID peer;
//} g_dvb_prefetcher;

struct crccache_t {
	int service[TABLE_ID][SERVICEID_LENGTH][SECTION_NUMBER_LENGTH];
};
struct serviceid_info {
	int count;
	int buffer[300][2];
};
uint8_t *spArray = NULL;
jint jdata[2];

void sparse_callback(int key, void* p) {
	if (p) {
		LOGD("free");
		free(p);
		p = NULL;
	}
}

//int ashmem_get_size_region(int fd)
//{
//        struct stat buf;
//        int result;
//
//        result = fstat(fd, &buf);
//        if (result == -1) {
//                return -1;
//        }
//
//        // Check if this is an "ashmem" region.
//        // TODO: This is very hacky, and can easily break. We need some reliable indicator.
//        if (!(buf.st_nlink == 0 && S_ISREG(buf.st_mode))) {
//                errno = ENOTTY;
//                return -1;
//        }
//
//        return (int)buf.st_size;  // TODO: care about overflow (> 2GB file)?
//}

JNIEXPORT jint Java_ipaneltv_toolkit_dvb_DvbSiEventPrefetcher_ncreate(JNIEnv *e,
		jobject thiz) {
	LOGD("ncreate 111191");
	return 0;
}

JNIEXPORT jint Java_ipaneltv_toolkit_dvb_DvbSiEventPrefetcher_nrelease(
		JNIEnv *e, jobject thiz) {
	if (spArray != NULL) {
		AShortSparse_clear(spArray, sparse_callback);
		AShortSparse_delete(spArray);
		spArray = NULL;
	}
	return 0;
}

JNIEXPORT jint Java_ipaneltv_toolkit_dvb_DvbSiEventPrefetcher_nprepare(
		JNIEnv *e, jobject thiz, jobject sp) {
	LOGD("nprepare in 1");
	uint8_t*pbuf = NULL;
	int fd, ffd;
	int length = 0;
	int blen = 0;
	int ret = -1;
	struct crccache_t* crcs = (struct crccache_t*) calloc(
			sizeof(struct crccache_t), 1);
	LOGD("nprepare in 11");
	if (spArray != NULL) {
		goto BAIL;
	}
	if (sp == NULL) {
		goto BAIL;
	}
	ASectionPrefetcher*f = (ASectionPrefetcher*) (*e)->GetIntField(e, sp,
			g_prefetcher.peer);
	if (f == NULL) {
		goto BAIL;
	}
	if (AShortSparse_new((void**) &spArray) == -1) {
		LOGE("AShortSparse_new failed");
		goto BAIL;
	}
	if ((fd = ASectionPrefetcher_getMemoryFile(f, "eit", 0)) > 0) {
		if ((blen = ashmem_get_size_region(fd)) < 0) {
			LOGE("eit: ashmem_get_size_region 1 failed:%s", strerror(errno));
			goto BAIL;
		}
		LOGD("eit:nprepare blen = %d ", blen);
		if ((ffd = dup(fd)) < 0) {
			LOGE("new: dup fd(%d) failed, err:%d, %s", fd, errno,
					strerror(errno));
			goto BAIL;
		}
		if (fd >= 0) {
			close(fd);
			fd = -1;
		}
		LOGD("eit:nprepare ffd = %d ", ffd);
		if ((pbuf = mmap(0, blen, PROT_READ, MAP_SHARED, ffd, 0))
				== (void*) -1) {
			LOGD("eit:nprepare mmap faled");
			goto BAIL;
		}
		LOGD("eit:nprepare pbuf = %d ", pbuf);
	} else {
		LOGD("eit:nprepare fd = %d ", fd);
	}

	LOGD("eit:nprepare length:blen = %d:%d ", length, blen);
	unsigned short tableid = 0;
	int flen = 0;
	unsigned short serviceid = 0;
	unsigned short section_number = 0;
	struct serviceid_info *buf = NULL;
	while (length < blen - 8) {
		tableid = pbuf[0];
		flen = (((pbuf[1] & 0x0f) << 8) | (pbuf[2] & 0xff)) + 3;
		serviceid = pbuf[3] << 8 | pbuf[4];
		section_number = pbuf[6];
		buf = NULL;
		LOGD(
				"eit:nprepare tableid:flen:serviceid:section_number = %d:%d:%d:%d ",
				tableid, flen, serviceid, section_number);
		if (flen < 8) {
			break;
		}
		if (serviceid < SERVICEID_LENGTH
				&& section_number < SECTION_NUMBER_LENGTH) {

			if (crcs->service[tableid - 96][serviceid][section_number] == 0) {
				crcs->service[tableid - 96][serviceid][section_number] = 1;
			} else {
				length += flen;
				pbuf += flen;
				continue;
			}
		} else {
			length += flen;
			pbuf += flen;
			continue;
		}
		if (!(buf = (struct serviceid_info*) AShortSparse_get(spArray,
				serviceid))) {
			buf = (struct serviceid_info*) calloc(1,
					sizeof(struct serviceid_info));
			AShortSparse_set(spArray, serviceid, buf);
		}
		if (buf->count >= 100) {
			LOGD("eit:nprepare 4 length = %d ", length);
			length += flen;
			pbuf += flen;
			continue;
		}
		buf->buffer[buf->count][0] = (int) pbuf;
		buf->buffer[buf->count][1] = flen;
		buf->count++;
		/*LOGD("eit:nprepare 004 buf->count = %d,length = %d ,flen = %d,section_number = %d,serviceid = %d", buf->count,length,flen,section_number,serviceid);*/
		pbuf += flen;
		length += flen;
		tableid = 0;
		flen = 0;
		serviceid = 0;
		section_number = 0;
		buf = NULL;
		usleep(1000);
	}
	if (ffd >= 0) {
		close(ffd);
		ffd = -1;
	}

	pbuf = NULL;
	fd = -1;
	ffd = -1;
	length = 0;
	blen = 0;

	LOGE("eit-actual:nprepare: crcs = %p ", crcs);

	memset(crcs, 0, sizeof(struct crccache_t));

	if ((fd = ASectionPrefetcher_getMemoryFile(f, "eit-actual", 0)) > 0) {
		if ((blen = ashmem_get_size_region(fd)) < 0) {
			LOGE("eit-actual: ashmem_get_size_region 1 failed:%s",
					strerror(errno));
			goto BAIL;
		}
		if ((ffd = dup(fd)) < 0) {
			LOGE("eit-actual: dup fd(%d) failed, err:%d, %s", fd, errno,
					strerror(errno));
			goto BAIL;
		}
		if (fd >= 0) {
			close(fd);
			fd = -1;
		}
		LOGD("eit-actual:nprepare ffd = %d ", ffd);
		if ((pbuf = mmap(0, blen, PROT_READ, MAP_SHARED, ffd, 0))
				== (void*) -1) {
			LOGD("eit-actual:nprepare mmap faled");
			goto BAIL;
		}
	} else {
		LOGD("eit-actual:nprepare fd = %d ", fd);
	}

	LOGD("eit-actual:nprepare length:blen = %d:%d ", length, blen);

	while (length < blen - 8) {
		unsigned short tableid = pbuf[0];
		int flen = (((pbuf[1] & 0x0f) << 8) | (pbuf[2] & 0xff)) + 3;
		unsigned short serviceid = pbuf[3] << 8 | pbuf[4];
		unsigned short section_number = pbuf[6];
		struct serviceid_info *buf = NULL;
		LOGD(
				"eit-actual:nprepare 413 tableid:flen:serviceid:section_number = %d:%d:%d:%d ",
				tableid, flen, serviceid, section_number);
		if (flen < 8) {
			break;
		}
		if (serviceid < SERVICEID_LENGTH
				&& section_number < SECTION_NUMBER_LENGTH) {
			if (crcs->service[tableid - 80][serviceid][section_number] == 0) {
				crcs->service[tableid - 80][serviceid][section_number] = 1;
			} else {
				length += flen;
				pbuf += flen;
				continue;
			}
		} else {
			length += flen;
			pbuf += flen;
			continue;
		}
		if (!(buf = (struct serviceid_info*) AShortSparse_get(spArray,
				serviceid))) {
			buf = (struct serviceid_info*) calloc(1,
					sizeof(struct serviceid_info));
			AShortSparse_set(spArray, serviceid, buf);
		}
		if (buf->count >= 100) {
			length += flen;
			pbuf += flen;
			continue;
		}
		buf->buffer[buf->count][0] = (int) pbuf;
		buf->buffer[buf->count][1] = flen;
		buf->count++;
		LOGD("eit-actual:nprepare 004 buf->count = %d ", buf->count);
		pbuf += flen;
		length += flen;
	}
	if (ffd >= 0) {
		close(ffd);
		ffd = -1;
	}
	if (crcs) {
		LOGD("eit-actual:nprepare free1");
		free(crcs);
	}
	ret = 0;
	return 0;
	BAIL: {
		ret = -1;
		if (fd >= 0) {
			close(fd);
			fd = -1;
		}
		if (ffd >= 0) {
			close(ffd);
			ffd = -1;
		}
		if (crcs) {
			LOGD("eit-actual:nprepare free2");
			free(crcs);
		}

	}
	LOGD("Java_ipaneltv_toolkit_dvb_DvbSiEventPrefetcher_nprepare 22");
	LOGD(
			"Java_ipaneltv_toolkit_dvb_DvbSiEventPrefetcher_nprepare end ret = %d.",
			ret);
	return ret;
}

JNIEXPORT jint Java_ipaneltv_toolkit_dvb_DvbSiEventPrefetcher_nseek(JNIEnv *e,
		jobject thiz, int pn, int index, jintArray b) {
	LOGD("Java_ipaneltv_toolkit_dvb_DvbSiEventPrefetcher_nseek in");
	if (spArray == NULL) {
		return -1;
	}
	struct serviceid_info *buf = NULL;
	if ((buf = (struct serviceid_info*) AShortSparse_get(spArray, pn))) {
		if (index >= buf->count) {
			return -1;
		}
		jdata[0] = buf->buffer[index][0];
		jdata[1] = buf->buffer[index][1];
		int idlen = (*e)->GetArrayLength(e, b);
		idlen = idlen > 2 ? 2 : idlen;
		(*e)->SetIntArrayRegion(e, b, 0, idlen, jdata);
		return 0;
	}
	return -1;
}

JNIEXPORT jint Java_ipaneltv_toolkit_dvb_DvbSiEventPrefetcher_ntell(JNIEnv *e,
		jobject thiz, int pn) {
	LOGD("Java_ipaneltv_toolkit_dvb_DvbSiEventPrefetcher_ntell");
	if (spArray == NULL) {
		return -1;
	}
	struct serviceid_info *buf = NULL;
	if ((buf = (struct serviceid_info*) AShortSparse_get(spArray, pn))) {
		return buf->count;
	}
	return -1;
}

JNIEXPORT jint Java_ipaneltv_toolkit_dvb_DvbSiEventPrefetcher_ngetKeys(
		JNIEnv *e, jobject thiz, jintArray b) {
	LOGD("Java_ipaneltv_toolkit_dvb_DvbSiEventPrefetcher_ngetKeys");
	int idlen = (*e)->GetArrayLength(e, b);
	LOGD("Java_ipaneltv_toolkit_dvb_DvbSiEventPrefetcher_ngetKeys idlen = %d",
			idlen);
	int keys[idlen];
	int len = AShortSparse_list(spArray, (uint16_t*) keys, idlen);
	(*e)->SetIntArrayRegion(e, b, 0, len, keys);
	LOGD("Java_ipaneltv_toolkit_dvb_DvbSiEventPrefetcher_ngetKeys len = %d", len);
	return len;
}

int initJavaEitPrefetcher(JNIEnv *e) {
	LOGD("initJavaEitPrefetcher");
	jclass cls;
	LOGD("initJavaEitPrefetcher 11");
	static const char* g_class_name = "android/net/telecast/SectionPrefetcher";
	if ((cls = (*e)->FindClass(e, g_class_name)) == NULL) {
		LOGE("Can't find class : %s", g_class_name);
		return -1;
	}
	g_prefetcher.clazz = (jclass)(*e)->NewGlobalRef(e, cls);
	if ((g_prefetcher.peer = (*e)->GetFieldID(e, g_prefetcher.clazz, "peer",
			"I")) == NULL) {
		LOGE("no such field: peer");
		return -1;
	}
//	jclass dvb_cls;
//	static const char* g_dvb_class_name = "ipaneltv/toolkit/dvb/DvbSiEventPrefetcher";
//	if ((dvb_cls = (*e)->FindClass(e, g_dvb_class_name)) == NULL) {
//		LOGE("Can't find class : %s", g_dvb_class_name);
//		return -1;
//	}
//	g_dvb_prefetcher.clazz = (jclass)(*e)->NewGlobalRef(e, dvb_cls);
//	if ((g_dvb_prefetcher.peer = (*e)->GetFieldID(e, g_dvb_prefetcher.clazz, "peer", "I")) == NULL) {
//		LOGE("no such field: peer");
//		return -1;
//	}

	return 0;
}
