#include <stdlib.h>
#include <stdint.h>
#include <stdio.h>
#include <string.h>
#include <sys/stat.h>
#include <unistd.h>
#include <sys/types.h>
#include <dirent.h>
#include <sys/mman.h>
#include <pthread.h>
#include <assert.h>
#include <android/log.h>
#include <jni.h>
#include <androidtv/section_utils.h>
#include <androidtv/native_utils.h>
#include <androidtv/section_filter.h>

#define LOAG_TAG "[jni]ipaneltvlib"

#ifndef LOGD
#define LOGV(...) ((void)__android_log_print(ANDROID_LOG_VERBOSE, LOAG_TAG,  __VA_ARGS__))
#define LOGD(...) ((void)__android_log_print(ANDROID_LOG_DEBUG, LOAG_TAG,  __VA_ARGS__))
#define LOGI(...) ((void)__android_log_print(ANDROID_LOG_INFO, LOAG_TAG,  __VA_ARGS__))
#define LOGW(...) ((void)__android_log_print(ANDROID_LOG_WARN, LOAG_TAG,  __VA_ARGS__))
#define LOGE(...) ((void)__android_log_print(ANDROID_LOG_ERROR, LOAG_TAG,  __VA_ARGS__))
#endif

#define NBUF_LEN	4096

struct {
	jfieldID descriptor;
} gfdids;

struct {
	jfieldID mAddress;
} gmfids;

struct {
	jfieldID peer;
} gfilterids;

struct string_buf_t {
	struct string_buf_t*next;
	short len;
	char p[2];
};
struct secnamebuf_t {
	pthread_mutex_t mtx[1];
	struct string_buf_t*idle;
	int size;
};
static JavaVM*g_vm = NULL;
static struct secnamebuf_t gsnbuf[1];

JNIEnv* attach_jnienv() {
	jint result;
	JNIEnv *env = NULL;
	JavaVMAttachArgs args;
	args.version = JNI_VERSION_1_4;
	args.name = NULL;
	args.group = NULL;
	assert(g_vm != NULL);
	result = (*g_vm)->AttachCurrentThread(g_vm, &env, (void*) &args);
	if (result != JNI_OK) {
		LOGI("attach_jnienv attach thread failed\n");
		return NULL;
	}
	return env;
}

static int _initsnbuf() {
	memset(gsnbuf, 0, sizeof(gsnbuf));
	pthread_mutex_init(gsnbuf->mtx, NULL);
	return 0;
}
static struct string_buf_t* _getsnbuf() {
	int retry = 1;
	struct string_buf_t*ret = NULL;
	RETRY: pthread_mutex_lock(gsnbuf->mtx);
	if ((ret = gsnbuf->idle)) {
		gsnbuf->idle = ret->next;
	} else {
		if (gsnbuf->size < 10) {
			retry = 0;
			if ((ret = (struct string_buf_t*) malloc(sizeof(*ret) + NBUF_LEN))) {
				gsnbuf->size++;
				ret->len = NBUF_LEN;
				ret->next = NULL;
			}
		}
	}
	pthread_mutex_unlock(gsnbuf->mtx);
	if (ret == NULL && retry) {
		usleep(10);
		goto RETRY;
	}
	return ret;
}

static void _releasesnbuf(struct string_buf_t*p) {
	pthread_mutex_lock(gsnbuf->mtx);
	p->next = gsnbuf->idle;
	gsnbuf->idle = p;
	pthread_mutex_unlock(gsnbuf->mtx);
}

static int _mkdirs(char*p, int mode) {
	int i, len, ret = 0;
	if ((len = strlen(p)) >= 1024)
		return -1;
	for (i = 0; i < len; i++) {
		if (p[i] == '/') {
			p[i] = '\0';
			if (access(p, F_OK) != 0) {
				if (mkdir(p, mode) < 0)
					break;
				else
					ret++;
			}
			p[i] = '/';
		}
	}
	if (len > 0 && access(p, F_OK) != 0)
		mkdir(p, mode);
	return ret;
}

static int _cleardir(const char*fold) {
	DIR *dp;
	struct dirent *dirp;
	struct stat statbuf;
	char c[512] = "/0";
	char *w, *y, *z, *x;
	dp = opendir(fold);

	while ((dirp = readdir(dp)) != NULL) {
		if ((strcmp(dirp->d_name, ".") != 0) && (strcmp(dirp->d_name, "..") != 0)) {
			strcpy(c, "./");
			x = strcat(c, fold);
			w = strcat(x, "/");
			y = dirp->d_name;
			z = strcat(w, y);
			lstat(dirp->d_name, &statbuf);
			if (S_ISREG(statbuf.st_mode))
				remove(z);
			else if (S_ISLNK(statbuf.st_mode))
				remove(z);
			else if (S_ISDIR(statbuf.st_mode)) {
				if (remove(z) > 0)
					; // folder is empty.
				else
					_cleardir(z);
			}
		}
	}
	rmdir(fold);
	return 0;
}

JNIEXPORT jint Java_ipaneltv_toolkit_Natives_malloc(JNIEnv *e, jclass clazz, jint len) {
	return (jint) malloc(len);
}
JNIEXPORT jint Java_ipaneltv_toolkit_Natives_calloc(JNIEnv *e, jclass clazz, jint v, jint len) {
	return (jint) calloc(v, len);
}
JNIEXPORT void Java_ipaneltv_toolkit_Natives_free(JNIEnv *e, jclass clazz, jint p) {
	free((void*) p);
}

JNIEXPORT void Java_ipaneltv_toolkit_Natives_memsetb(JNIEnv *e, jclass clazz, jint p, jint v) {
	*((jbyte*) p) = (jbyte) v;
}

JNIEXPORT jint Java_ipaneltv_toolkit_Natives_memgetb(JNIEnv *e, jclass clazz, jint p) {
	return (jint)(*((jbyte*) p));
}

JNIEXPORT void Java_ipaneltv_toolkit_Natives_memseti(JNIEnv *e, jclass clazz, jint p, jint v) {
	*((jint *) p) = v;
}
JNIEXPORT jint Java_ipaneltv_toolkit_Natives_memgeti(JNIEnv *e, jclass clazz, jint p) {
	return *((jint*) p);
}

JNIEXPORT void Java_ipaneltv_toolkit_Natives_memsetf(JNIEnv *e, jclass clazz, jint p, jfloat v) {
	*((jfloat*) p) = v;
}
JNIEXPORT jfloat Java_ipaneltv_toolkit_Natives_memgetf(JNIEnv *e, jclass clazz, jint p) {
	return *((jfloat*) p);
}

JNIEXPORT void Java_ipaneltv_toolkit_Natives_memsetl(JNIEnv *e, jclass clazz, jint p, jlong v) {
	*((jlong*) p) = v;
}
JNIEXPORT jlong Java_ipaneltv_toolkit_Natives_memgetl(JNIEnv *e, jclass clazz, jint p) {
	return *((jlong *) p);
}

JNIEXPORT void Java_ipaneltv_toolkit_Natives_memsetd(JNIEnv *e, jclass clazz, jint p, jdouble v) {
	*((jdouble *) p) = v;
}
JNIEXPORT jdouble Java_ipaneltv_toolkit_Natives_memgetd(JNIEnv *e, jclass clazz, jint p) {
	return *((jdouble *) p);
}

JNIEXPORT void Java_ipaneltv_toolkit_Natives_memsetutf(JNIEnv *e, jclass clazz, jint p, jstring s,
		int len) {
	(*e)->GetStringUTFRegion(e, s, 0, len, (char*) p);
}

JNIEXPORT jstring Java_ipaneltv_toolkit_Natives_memgetutf(JNIEnv *e, jclass clazz, jint p, jint len) {
	jbyte *pp = (jbyte*) p, v = pp[len];
	jstring s = NULL;
	pp[len] = 0;
	s = (*e)->NewStringUTF(e, (char *)p);
	pp[len] = v;
	return s;
}

JNIEXPORT jint Java_ipaneltv_toolkit_Natives_memcpy(JNIEnv *e, jclass clazz, jint dst, jint src,
		jint len) {
	return (jint) memcpy((void*) dst, (void*) src, len);
}
JNIEXPORT jint Java_ipaneltv_toolkit_Natives_memmove(JNIEnv *e, jclass clazz, jint dst, jint src,
		jint len) {
	return (jint) memmove((void*) dst, (void*) src, len);
}
JNIEXPORT void Java_ipaneltv_toolkit_Natives_njmemcpy(JNIEnv *e, jclass clazz, jbyteArray dst,
		jint off, jint src, jint len) {
	(*e)->SetByteArrayRegion(e, dst, off, len, (jbyte*) src);
}
JNIEXPORT void Java_ipaneltv_toolkit_Natives_jnmemcpy(JNIEnv *e, jclass clazz, jint dst,
		jbyteArray src, jint off, jint len) {
	(*e)->GetByteArrayRegion(e, src, off, len, (jbyte*) dst);
}
JNIEXPORT jint Java_ipaneltv_toolkit_Natives_fopen(JNIEnv *e, jclass clazz, jstring path,
		jstring mode) {
	jint ret = 0;
	const char*p = NULL, *m = NULL;
	if (path == NULL || mode == NULL)
		return 0;
	if (!(p = (*e)->GetStringUTFChars(e, path, NULL)))
		goto BAIL;
	if (!(m = (*e)->GetStringUTFChars(e, mode, NULL)))
		goto BAIL;
	ret = (jint) fopen(p, m);
	BAIL: if (p)
		(*e)->ReleaseStringUTFChars(e, path, p);
	if (m)
		(*e)->ReleaseStringUTFChars(e, mode, m);
	return ret;
}
JNIEXPORT jint Java_ipaneltv_toolkit_Natives_fclose(JNIEnv *e, jclass clazz, jint f) {
	return fclose((FILE*) f);
}
JNIEXPORT jint Java_ipaneltv_toolkit_Natives_fread(JNIEnv *e, jclass clazz, jint f, jint p,
		jint len) {
	return fread((void*) p, 1, len, (FILE*) f);
}
JNIEXPORT jint Java_ipaneltv_toolkit_Natives_fwrite(JNIEnv *e, jclass clazz, jint f, jint p,
		jint len) {
	return fwrite((void*) p, 1, len, (FILE*) f);
}
JNIEXPORT jint Java_ipaneltv_toolkit_Natives_fseek(JNIEnv *e, jclass clazz, jint f, jlong off,
		jint w) {
	return fseek((FILE*) f, off, w);
}
JNIEXPORT jlong Java_ipaneltv_toolkit_Natives_ftell(JNIEnv *e, jclass clazz, jint f) {
	return ftell((FILE*) f);
}
JNIEXPORT jint Java_ipaneltv_toolkit_Natives_fflush(JNIEnv *e, jclass clazz, jint f) {
	return fflush((FILE*) f);
}
JNIEXPORT jint Java_ipaneltv_toolkit_Natives_fileno(JNIEnv *e, jclass clazz, jint f) {
	return fileno((FILE*) f);
}
JNIEXPORT jint Java_ipaneltv_toolkit_Natives_fcopy(JNIEnv *e, jclass clazz, jint dstf, jint srcf,
		jint len) {
	char buf[1024], *p = buf;
	size_t blen = 1024, rl, wl, ret = 0;
	if (len <= 0)
		return len;
	if (len > 1024 * 1024) {
		if ((p = (char*) malloc(1024 * 1024)))
			len = 1024 * 1024;
	}

	while ((rl = fread(p, 1, blen, (FILE*) srcf)) > 0) {
		if ((wl = fwrite(p, 1, rl, (FILE*) dstf)) != rl)
			break;
		ret += rl;
	}

	if (p != buf)
		free(p);
	return ret;
}

JNIEXPORT int Java_ipaneltv_toolkit_Natives_flength(JNIEnv *e, jclass clazz, jstring path) {
	struct stat buf;
	int filesize = -1;
	struct string_buf_t* sb = NULL;
	if (path == NULL)
		return 0;
	sb = _getsnbuf();
	if (sb) {
		(*e)->GetStringUTFRegion(e, path, 0, (*e)->GetStringLength(e, path), (char*) sb->p);
		if(lstat((char*)sb->p, &buf)== 0) {
			filesize = buf.st_size;
		}
		_releasesnbuf(sb);
	}
	return filesize;
}

JNIEXPORT int Java_ipaneltv_toolkit_Natives_ftype(JNIEnv *e, jclass clazz, jstring path) {
	struct stat buf;
	int type = -1;
	struct string_buf_t* sb = NULL;
	if (path == NULL)
		return -1;
	sb = _getsnbuf();
	if (sb) {
		(*e)->GetStringUTFRegion(e, path, 0, (*e)->GetStringLength(e, path), (char*) sb->p);
		type = lstat((char*) sb->p, &buf);
		switch (buf.st_mode & S_IFMT) {
		case S_IFREG:
			type = 1;
			break;
		case S_IFDIR:
			type = 2;
			break;
		case S_IFCHR:
			type = 3;
			break;
		case S_IFBLK:
			type = 4;
			break;
		case S_IFIFO:
			type = 5;
			break;
		case S_IFLNK:
			type = 6;
			break;
		case S_IFSOCK:
			type = 7;
			break;
		default:
			type = -1;
			break;
		}
		_releasesnbuf(sb);
	}
	return type;
}

JNIEXPORT jint Java_ipaneltv_toolkit_Natives_remove(JNIEnv *e, jclass clazz, jstring path) {
	int ret = -1;
	const char*p = NULL;
	if ((p = (*e)->GetStringUTFChars(e, path, NULL))) {
		ret = remove(p);
		(*e)->ReleaseStringUTFChars(e, path, p);
	}
	return ret;
}
JNIEXPORT jint Java_ipaneltv_toolkit_Natives_rename(JNIEnv *e, jclass clazz, jstring path,
		jstring npath) {
	int ret = -1;
	const char*p = NULL, *np = NULL;
	if (path == NULL || npath == NULL)
		return -1;
	if ((p = (*e)->GetStringUTFChars(e, path, NULL)) && (np = (*e)->GetStringUTFChars(e, npath,
			NULL))) {
		ret = rename(p, np);
		(*e)->ReleaseStringUTFChars(e, path, p);
		(*e)->ReleaseStringUTFChars(e, npath, np);
	}
	return ret;
}

JNIEXPORT jint Java_ipaneltv_toolkit_Natives_mkdir(JNIEnv *e, jclass clazz, jstring path, jint mode) {
	int ret = -1;
	const char*p = NULL;
	if ((p = (*e)->GetStringUTFChars(e, path, NULL))) {
		ret = mkdir(p, mode);
		(*e)->ReleaseStringUTFChars(e, path, p);
	}
	return ret;
}

JNIEXPORT jint Java_ipaneltv_toolkit_Natives_mkdirs(JNIEnv *e, jclass clazz, jstring path,
		jint mode) {
	char b[1024];
	(*e)->GetStringUTFRegion(e, path, 0, 1024, b);
	return _mkdirs(b, mode);
}

JNIEXPORT jint Java_ipaneltv_toolkit_Natives_cleardir(JNIEnv *e, jclass clazz, jstring path) {
	int ret = -1;
	const char*p = NULL;
	if ((p = (*e)->GetStringUTFChars(e, path, NULL))) {
		ret = _cleardir(p);
		(*e)->ReleaseStringUTFChars(e, path, p);
	}
	return ret;
}

JNIEXPORT int Java_ipaneltv_toolkit_Natives_getfd(JNIEnv *e, jclass clazz, jobject fd) {
	return (*e)->GetIntField(e, fd, gfdids.descriptor);
}
JNIEXPORT void Java_ipaneltv_toolkit_Natives_setfd(JNIEnv *e, jclass clazz, jobject fd, int v) {
	(*e)->SetIntField(e, fd, gfdids.descriptor, v);
}

JNIEXPORT int Java_ipaneltv_toolkit_Natives_open(JNIEnv *e, jclass clazz, jstring jp, int mode) {
	jint ret = -1;
	const char*p = NULL;
	if (jp == NULL)
		return -1;
	if ((p = (*e)->GetStringUTFChars(e, jp, NULL))) {
		ret = (jint) open(p, mode);
		(*e)->ReleaseStringUTFChars(e, jp, p);
	}
	return ret;
}
JNIEXPORT int Java_ipaneltv_toolkit_Natives_close(JNIEnv *e, jclass clazz, int fd) {
	if (fd < 0)
		return -1;
	return close(fd);
}
JNIEXPORT jlong Java_ipaneltv_toolkit_Natives_llseek(JNIEnv *e, jclass clazz, jint fd, jlong off,
		int where) {
	if (fd < 0)
		return -1;
	return lseek(fd, (int) off, where);
}
JNIEXPORT int Java_ipaneltv_toolkit_Natives_read(JNIEnv *e, jclass clazz, jint fd, int p, int len) {
	int ret = -1;
	if (p == 0 || len <= 0 || fd < 0)
		return -1;
	return read(fd, (void*) p, len);
}

JNIEXPORT int Java_ipaneltv_toolkit_Natives_write(JNIEnv *e, jclass clazz, jint fd, int p, int len) {
	if (fd < 0 || p == 0 || len <= 0 || fd < 0)
		return -1;
	return write(fd, (void*) p, len);
}

JNIEXPORT int Java_ipaneltv_toolkit_Natives_mmap(JNIEnv* e, jobject clazz, jint fd, jint length,
		jint prot) {
	if (fd < 0 || length <= 0 || prot == 0)
		return 0;
	return (int) mmap(NULL, length, prot, MAP_SHARED, fd, 0);
}

JNIEXPORT int Java_ipaneltv_toolkit_Natives_munmap(JNIEnv* e, jobject clazz, jint addr, jint length) {
	munmap((void *) addr, length);
	return 0;
}

JNIEXPORT int Java_ipaneltv_toolkit_Natives_amfaddr(JNIEnv* e, jobject clazz, jobject mf) {
	return (*e)->GetIntField(e, mf, gmfids.mAddress);
}

JNIEXPORT int Java_ipaneltv_toolkit_Natives_mfopen(JNIEnv* e, jobject clazz, jstring name,
		jint len, jint prot) {
	int ret = -1;
	const char* s = (name ? (*e)->GetStringUTFChars(e, name, NULL) : NULL);
	ret = AMemoryFile_open(s, len, prot);
	if (s)
		(*e)->ReleaseStringUTFChars(e, name, s);
	return ret;
}
JNIEXPORT int Java_ipaneltv_toolkit_Natives_mflen(JNIEnv* e, jobject clazz, jint fd) {
	return AMemoryFile_length(fd);
}
JNIEXPORT int Java_ipaneltv_toolkit_Natives_mfprot(JNIEnv* e, jobject clazz, jint fd, jint p) {
	return AMemoryFile_setProt(fd, p);
}
JNIEXPORT int Java_ipaneltv_toolkit_Natives_mfpin(JNIEnv* e, jobject clazz, jint fd, jboolean b) {
	return b ? AMemoryFile_pin(fd) : AMemoryFile_unpin(fd);
}

///=====================package private =========================
JNIEXPORT int Java_ipaneltv_toolkit_Natives_rsecint(JNIEnv *e, jclass clazz, int p, int len,
		jstring name) {
	int ret = -1, v = 0;
	struct string_buf_t* sb = _getsnbuf();
	if (sb) {
		(*e)->GetStringUTFRegion(e, name, 0, (*e)->GetStringLength(e, name), (char*) sb->p);
		ret = ASectionUtil_readInt((void*) p, len, sb->p, &v);
		_releasesnbuf(sb);
	}
	return ret == 0 ? v : -1;
}

JNIEXPORT int64_t Java_ipaneltv_toolkit_Natives_rseclong(JNIEnv *e, jclass clazz, int p, int len,
		jstring name) {
	int ret = -1;
	int64_t v = 0;
	//	struct string_buf_t* sb = _getsnbuf();
	//	if (sb) {
	//		(*e)->GetStringUTFRegion(e, name, 0, (*e)->GetStringLength(e, name), (char*) sb->p);
	//		ret = ASectionUtil_readLong((void*) p, len, sb->p, &v);
	//		_releasesnbuf(sb);
	//	}
	return ret == 0 ? v : -1;
}

JNIEXPORT jstring Java_ipaneltv_toolkit_Natives_rsecdate(JNIEnv *e, jclass clazz, int p, int len,
		jstring name) {
	jstring ret = NULL;
	char buf[80];
	struct string_buf_t* sb = _getsnbuf();
	if (sb) {
		(*e)->GetStringUTFRegion(e, name, 0, (*e)->GetStringLength(e, name), (char*) sb->p);
		if (ASectionUtil_readTime3339((void*) p, len, sb->p, buf, 80) > 0)
			ret = (*e)->NewStringUTF(e, buf);
		_releasesnbuf(sb);
	}
	return ret;
}

JNIEXPORT jbyteArray Java_ipaneltv_toolkit_Natives_rsectext(JNIEnv *e, jclass clazz, int p,
		int len, jstring name) {
	jbyte bt[4];
	jbyteArray ret = NULL;
	int txtlen, enclen, buflen;
	const char*txt = NULL, *enc = NULL;
	struct string_buf_t* sb = _getsnbuf();
	if (sb) {
		(*e)->GetStringUTFRegion(e, name, 0, (*e)->GetStringLength(e, name), (char*) sb->p);
		if ((txtlen = ASectionUtil_peekText((void*) p, len, sb->p, &txt, &enc)) > 0) {
			enclen = enc ? strlen(enc) : 0;
			buflen = 1 + enclen + txtlen;
			if (enclen < 255 && buflen < 65536) {
				if ((ret = (*e)->NewByteArray(e, buflen))) {
					bt[0] = enclen;
					(*e)->SetByteArrayRegion(e, ret, 0, 1, bt);
					if (enclen > 0)
						(*e)->SetByteArrayRegion(e, ret, 1, enclen, (jbyte*) enc);
					(*e)->SetByteArrayRegion(e, ret, 1 + enclen, txtlen, (jbyte*) txt);
				}
			}
		}
		_releasesnbuf(sb);
	}
	return ret;
}

JNIEXPORT jbyteArray Java_ipaneltv_toolkit_Natives_rsecblob(JNIEnv *e, jclass clazz, int p,
		int len, jstring name) {
	jbyteArray ret = NULL;
	int blen;
	const void*b = NULL;
	struct string_buf_t* sb = _getsnbuf();
	if (sb) {
		(*e)->GetStringUTFRegion(e, name, 0, (*e)->GetStringLength(e, name), (char*) sb->p);
		if ((blen = ASectionUtil_peekBytes((void*) p, len, sb->p, &b)) > 0) {
			if ((ret = (*e)->NewByteArray(e, blen)))
				(*e)->SetByteArrayRegion(e, ret, 0, blen, (jbyte*) b);
		}
		_releasesnbuf(sb);
	}
	return ret;
}

//JNIEXPORT int Java_ipaneltv_toolkit_Natives_lsecbase(JNIEnv *e, jclass clazz, jobject nsb, jlong f,
//		int pid, int tid, jintArray p, jintArray len) {
//	return -1;//TODO
//}

#define _BUF_LEN 	(8*1024)
#define _SEC_SIZE	(256)
#define _SEC_LEN(p)	(((p[1] & 0xf)<<8 | p[2]) + 3)
static int _soft_filter(uint8_t*s, int len, char*c/*coef*/, char*m/*mask*/, char*e/*excl*/, int d/*depth*/) {
	int i;
	if (len >= d) {
		for (i = 0; i < d; ++i) {
			if (e[i] == 0) {
				if ((c[i] & m[i]) != (s[i] & m[i]))
					return -1;
			} else {
				if ((c[i] & (m[i] & ~e[i])) != (s[i] & (m[i] & ~e[i])))
					return -1;
			}
		}
		return 0;
	}
	return -1;
}
#if 0
static int _lsecfile_do(uint8_t * p, int len, int *A, int*L, int size, int got,
		char*c, char*m, char*e, int d) {
	int slen, ret = 0;
	do {
		if (size == got)
		return -1;
		slen = _SEC_LEN(p);
		if (slen <= 3 || slen > 4096)
		//	LOGI("[jni]hegang   slen 1.....%d..",slen);
		return -1;
		if (slen > len)
		return ret;
		if (_soft_filter(p, slen, c, m, e, d) == 0) {
			A[got] = p;
			L[got] = slen;
			got++;
		}
		ret += slen;
		p += slen;
		len -= slen;
	}while (1);
	//LOGI("[jni]hegang   got .....%d..",got);
	return ret;
}
#endif

JNIEXPORT int Java_ipaneltv_toolkit_Natives_lsecfile(JNIEnv *e, jclass clazz, jint fd, int seek,
		jintArray jp, jintArray jlen, int size) {
	return Java_ipaneltv_toolkit_Natives_lsecfile2(e, clazz, fd, seek, jp, jlen, size, NULL, NULL,
			NULL, 0);
}

JNIEXPORT int Java_ipaneltv_toolkit_Natives_lsecfile2(JNIEnv *e, jclass clazz, jint fd, int seek,
		jintArray sb, jintArray len, int size, jbyteArray c, jbyteArray m, jbyteArray r, int depth) {
	int ret = 0, doff = 0, dlen = 0, rlen = 0, n = 0, used = 0, slen;
		int _pret[_SEC_SIZE], _lret[_SEC_SIZE], *pret = _pret, *lret = _lret;
		char coef[32], mask[32], excl[32];
		uint8_t * p;
		uint8_t * finalP = NULL;
		uint8_t * tempSection = NULL;
		if (fd < 0 || depth < 0) {
			return -1;
		} else if (depth > 0) {
			if (c == NULL || m == NULL || r == NULL)
				return -1;
			if ((*e)->GetArrayLength(e, c) < depth || (*e)->GetArrayLength(e, m) < depth
					|| (*e)->GetArrayLength(e, r) < depth)
				return -1;
			(*e)->GetByteArrayRegion(e, c, 0, depth, (jbyte*) coef);
			(*e)->GetByteArrayRegion(e, m, 0, depth, (jbyte*) mask);
			(*e)->GetByteArrayRegion(e, r, 0, depth, (jbyte*) excl);
		}
		LOGI("[jni]lsecfile2   size = %d",size);
		if (size > _SEC_SIZE) {
			pret = (int*) malloc(sizeof(int*) * size);
			lret = (int*) malloc(sizeof(int*) * size);
			if (!lret || !pret)
				goto BAIL;
		}
		if ((p = malloc(_BUF_LEN)) == NULL)
			goto fail;
		finalP = p;
		lseek(fd, seek, SEEK_SET);
		do {
			LOGI("[jni]lsecfile2    Adress p = %d",p);
			if (doff > 0) {
	//			LOGI("[jni]lsecfile2    p = %d   doff =%d  dlen = %d", p,doff,dlen);
				p = finalP;
				memmove(p, p + doff, dlen);
	//			LOGI("[jni]lsecfile2    new Adress p = %d",p);
				doff = 0;
			}
			rlen = read(fd, p + dlen, _BUF_LEN - dlen);
	//		LOGI("[jni]lsecfile2    read_length = %d",rlen);
			if (rlen < 0)
				goto fail;
			if (rlen == 0)
				break;
			dlen += rlen;
	//		LOGI("[jni]lsecfile2   totle length = %d",dlen);

			do {
				if (size == n)
					goto SUCC;
				slen = _SEC_LEN(p);
	//			LOGI("[jni]lsecfile2  section_len   %d     remain_len = %d", slen,dlen);
				if (slen <= 3 || slen > 4096)
					break;
				if (slen > dlen)
					break;
				if (_soft_filter(p, slen, coef, mask, excl, depth) == 0) {
					if ((tempSection = malloc(slen)) == NULL)
						goto fail;
					//return -1;
					memmove(tempSection, p, slen);
					pret[n] = tempSection;
					tempSection = NULL;
					lret[n] = slen;
	//				LOGI("[jni]lsecfile2   section_filter   section[%d].len = %d",n, lret[n]);
					n++;
				}
				doff += slen;
				p += slen;
				dlen -= slen;
	//			LOGI("[jni]lsecfile2   buffer_used_len = %d       remain_len = %d",doff,dlen);
			} while (1);
			used += doff;
			LOGI("[jni]lsecfile2   total_used_len = %d ",used);
		} while (1);
		LOGI("[jni]lsecfile2   section_read_finsh   sections.size = %d",n);
		SUCC:
		//拷贝数据
		(*e)->SetIntArrayRegion(e, sb, 0, n, pret);
		(*e)->SetIntArrayRegion(e, len, 0, n, lret);

		BAIL: {
			if (pret != _pret)
				free(pret);
			if (lret != _lret)
				free(lret);
			if (tempSection)
				free(tempSection);
			if (finalP) {
				free(finalP);
				p = NULL;
			}
			if (p)
				free(p);
		}
		LOGI("[jni]lsecfile2   memmary_free_finsh ");
		used += dlen;
		LOGI("[jni]lsecfile2   File_len = %d      sections.size = %d", used,n);
		return n;

		fail:	//内存分配失败
		{
			LOGI("[jni]lsecfile2   memmary_fail_free_menmmary ");
			while (n > 0) {	//释放掉之前为正常读取的section分配的内存
				--n;
				tempSection = pret[n];
				free(tempSection);
				tempSection = NULL;
			}
			if (pret != _pret)
				free(pret);
			if (lret != _lret)
				free(lret);
			if (tempSection)
				free(tempSection);
			if (finalP) {
				free(finalP);
				p = NULL;
			}
			if (p)
				free(p);
		}
		return 0;
}

JNIEXPORT int Java_ipaneltv_toolkit_Natives_ssecfile(JNIEnv *e, jclass clazz, jint fd, int seek,
		jintArray p, jintArray len, int size) {
	int i, writeLen = 0, ret = 0;
	//LOGI("[jni]hegang   come into ssecfile.......");
	if (fd < 0)
		return -1;
	//LOGI("[jni]hegang   fd.....%x..", fd);
	jint * parray = (*e)->GetIntArrayElements(e, p, NULL);
	jint * lenarray = (*e)->GetIntArrayElements(e, len, NULL);

	//LOGI("[jni]hegang   GetIntArrayElements..2.....");
	if (parray == NULL || lenarray == NULL) {
		//	LOGI("[jni]hegang   GetIntArrayElements...3....");
		return -1;
	}

	//LOGI("[jni]hegang   size.....%d..", size);

	if (lseek(fd, seek, SEEK_SET) == -1)
		return -1;
	for (i = 0; i < size; i++) {
		//	LOGI("[jni]hegang   parray...len[i]..%d....%d", parray[i], lenarray[i]);
		ret = write(fd, (void*) parray[i], lenarray[i]);
		writeLen = writeLen + (ret > 0 ? ret : 0);
	}

	//LOGI("[jni]hegang   writeLen.......%d", writeLen);

	if (writeLen > 0)
		return 0;
	return -1;

}
JNIEXPORT int Java_ipaneltv_toolkit_Natives_lsecstor(JNIEnv *e, jclass clazz, jint fd, int sn,
		jint p, jint len) {
	int sz;
	if (fd < 0)
		return -1;
	unsigned char * buff = (unsigned char *) malloc(len);
	//	FILE *fp = fdopen(fd, "r");不要使用FILE* --- by lyre
	//	if (fp == NULL)
	//		return -1;

	while ((sz = read(fd, buff, len)) > 0) {
		unsigned char* buff_end = buff + sz;
		int sec_len = 0, sec_num = -1;
		while (buff < buff_end) {
			if (buff_end > buff && buff_end < (buff + sec_len + 3)) { //section不完整
				//LOGI("[buff tail is not a whole section]=========");
				lseek(fd, buff - buff_end, SEEK_CUR);
				sz = read(fd, buff, len);
				buff_end = buff + sz;
				if (buff_end < (buff + sec_len + 3)) {
					//LOGI(" file tail is not a whole section...skip");
					break;
				}
				continue;
			}
			sec_len = (buff[1] & 0xf) << 8 | buff[2];
			sec_num = buff[6];
			if (sec_num == sn) {
				//p = buff;
				//LOGI(" ----hegang--p-address-1-%d----",buff);
				memcpy((void*) p, buff, sec_len + 3);
				free(buff);
				return sec_len + 3;
			}
			buff = buff + sec_len + 3;
		}
	}
	return -1;
}

JNIEXPORT int Java_ipaneltv_toolkit_Natives_getcrcs(JNIEnv *e, jclass clazz, jint fd,
		jintArray crcs, jintArray vn_sn) {
	//LOGI("[jni]hegang   come into getcrcs.......");
	unsigned char * bf, *buff, *buff_end = NULL;
	int cs[_SEC_SIZE], vsn[_SEC_SIZE]; //crc
	int fileSize = 0, sz = 0, sec_len = 0, sec_num = -1, ver_num = -1, i = 0;
	uint32_t crc = 0;
	struct stat buf;
	if (fd < 0)
		return -1;
	fstat(fd, &buf);
	fileSize = buf.st_size;
	LOGI("[jni]getcrcs   fileSize.....%d..", fileSize);
	if ((bf = (unsigned char *) malloc(fileSize)) == NULL)
		return -1;
	buff = bf;
	sz = read(fd, buff, fileSize);
	LOGI("[jni]getcrcs   sz.....%d..", sz);
	//一个section的长度至少要12。
	if (sz < 12)
		return -1;
	buff_end = buff + sz;
	do{
		sec_len = (buff[1] & 0xf) << 8 | buff[2];
		ver_num = (buff[5] >> 1) & 0x1f;
		sec_num = buff[6];
		LOGI("[jni]getcrcs   sec_len/ver_num/sec_num.....%d/%d/%d..",sec_len, ver_num,sec_num);
		crc = (buff[sec_len - 1] << 24) | (buff[sec_len] << 16) | (buff[sec_len + 1] << 8)
				| buff[sec_len + 2];
		cs[i] = crc;
		vsn[i] = (ver_num << 16) | sec_num;
		LOGI("[jni]getcrcs   cs[i]/vsn[i].....%d/%d..", cs[i],vsn[i]);
		i++;
		buff = buff + sec_len + 3;
		LOGI("[jni]getcrcs   buff/buff_end.....%d/%d..", buff_end,buff_end);
	}while((buff +12)< buff_end);
	LOGI("[jni]getcrcs   i.....%d..", i);
	//拷贝数据
	(*e)->SetIntArrayRegion(e, crcs, 0, i, cs); //?
	(*e)->SetIntArrayRegion(e, vn_sn, 0, i, vsn);

	free(bf);
	return (i > 0) ? i : 0;

}

JNIEXPORT int Java_ipaneltv_toolkit_Natives_getbatcrcs(JNIEnv *e, jclass clazz, jint fd,
		jintArray crcs, jintArray bouquet_ids) {
	//LOGI("[jni]hegang   come into getcrcs.......");
	unsigned char * bf, *buff, *buff_end = NULL;
	int sz, fileSize, sec_len = 0, bouquet_id = 0, i = 0;
	uint32_t crc = 0;
	int cs[_SEC_SIZE]; //crc
	int bouqs[_SEC_SIZE]; //cn_sn
	struct stat buf;
	if (fd < 0)
		return -1;
	fstat(fd, &buf);
	fileSize = buf.st_size;
	//LOGI("[jni]hegang   fileSize.....%d..", fileSize);

	if ((bf = (unsigned char *) malloc(fileSize)) == NULL)
		return -1;
	buff = bf;
	sz = read(fd, buff, fileSize);
	if (sz <= 0)
		return -1;
	buff_end = buff + sz;

	while ((buff + sec_len + 3) <= buff_end) {
		sec_len = (buff[1] & 0xf) << 8 | buff[2];
		//LOGI("[jni]hegang   sec_len.....%d..", sec_len);
		bouquet_id = buff[3] << 8 | buff[4];
		crc = (buff[sec_len - 1] << 24) | (buff[sec_len] << 16) | (buff[sec_len + 1] << 8)
				| buff[sec_len + 2];
		cs[i] = crc;
		bouqs[i] = bouquet_id;
		//LOGI("[jni]hegang   cs[i].....%d..", cs[i]);
		//LOGI("[jni]hegang   vsn[i].....%d..", bouqs[i]);
		i++;
		buff = buff + sec_len + 3;
	}
	//LOGI("[jni]hegang   i.....%d..", i);
	//拷贝数据
	(*e)->SetIntArrayRegion(e, crcs, 0, i, cs); //?
	(*e)->SetIntArrayRegion(e, bouquet_ids, 0, i, bouqs);

	free(bf);
	return (i > 0) ? i : 0;

}

static ASectionFilter* _mfilterpeer(JNIEnv* e, jobject obj) {
	return (ASectionFilter*) (*e)->GetIntField(e, obj, gfilterids.peer);
}
JNIEXPORT int Java_ipaneltv_toolkit_Natives_dupfseca(JNIEnv *e, jclass clazz, jobject f, int p,
		int len) {
	void* addr = NULL;
	ASectionFilter* fi = _mfilterpeer(e, f);
	if (fi != NULL) {
		int flen = ASectionFilter_peek(fi, &addr);
		if (flen > 0) {
			if ((len = (flen > len ? len : flen)) > 0) {
				memcpy((void*) p, addr, len);
				return len;
			}
		}
	}
	return -1;
}

//===========================

static int initJavaFD(JNIEnv *e) {
	const char*clsname = "java/io/FileDescriptor";
	jclass clazz;
	if ((clazz = (*e)->FindClass(e, clsname)) == NULL) {
		LOGE("Can't find class : %s", clsname);
		return -1;
	}
	if ((gfdids.descriptor = (*e)->GetFieldID(e, clazz, "descriptor", "I")) == NULL) {
		LOGE("class %s no such field: descriptor", clsname);
		return -1;
	}
	return 0;
}
static int initJavaMemFile(JNIEnv *e) {
	jclass clazz;
	const char*clsname = "android/os/MemoryFile";
	if ((clazz = (*e)->FindClass(e, clsname)) == NULL) {
		LOGE("Can't find class : %s", clsname);
		return -1;
	}
	if ((gmfids.mAddress = (*e)->GetFieldID(e, clazz, "mAddress", "I")) == NULL) {
		LOGE("class %s no such field: mAddress", clsname);
		return -1;
	}
	return 0;
}
static int initJavaSecFilter(JNIEnv *e) {
	jclass clazz;
	const char*clsname = "android/net/telecast/SectionFilter";
	if ((clazz = (*e)->FindClass(e, clsname)) == NULL) {
		LOGE("Can't find class : %s", clsname);
		return -1;
	}
	if ((gfilterids.peer = (*e)->GetFieldID(e, clazz, "peer", "I")) == NULL) {
		LOGE("class %s no such field: peer", clsname);
		return -1;
	}
	return 0;
}

jint JNI_OnLoad(JavaVM* vm, void* reserved) {
	JNIEnv* env = NULL;
	jint result = -1;
	g_vm = vm;
	_initsnbuf();
	if ((*vm)->GetEnv(vm, (void**) &env, JNI_VERSION_1_4) != JNI_OK)
		goto BAIL;
	if (initJavaFD(env) != 0)
		goto BAIL;
	if (initJavaMemFile(env) != 0)
		goto BAIL;
	if (initJavaSecFilter(env) != 0)
		goto BAIL;
	if (initJavaEitPrefetcher(env) != 0)
		goto BAIL;
	if (initJavaCaNativeModule(env) != 0)
		goto BAIL;
	result = JNI_VERSION_1_4;
	BAIL: return result;
}
