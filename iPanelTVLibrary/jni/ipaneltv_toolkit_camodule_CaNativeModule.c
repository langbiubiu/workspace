#define LOAG_TAG "[jni]ipaneltvlib-camodule"

#include <sys/stat.h>
#include <sys/types.h>
#include <dirent.h>
#include <sys/mman.h>
#include <dlfcn.h>

#include "common_include.h"
#include "ca_module_interface.h"

#define _BUF_LEN_		2048
#define _NAME_BUF_LEN_	128

static struct {
	jclass clazz;
	jfieldID peer;
	jmethodID callback;
} g_nmod;

struct module_peer_t {
	AUUID uuid;
	void* lib;
	struct ca_module_interface*mi;
	void*mo;
	jobject wo;
	int started;
	char buf[_BUF_LEN_];
};

static void detach_jnienv() {
	//	assert(g_vm != NULL);
	//(*g_vm)->DetachCurrentThread(g_vm);
}

static int get_java_string_region(JNIEnv* e, jstring js, char*buf, int len) {
	int jlen = (*e)->GetStringUTFLength(e, js);
	if (jlen > len - 1) {
		LOGW("get_java_string_region lost some chars[warn]!!!");
		jlen = len - 1;
	}
	(*e)->GetStringUTFRegion(e, js, 0, (*e)->GetStringLength(e, js), (jbyte*)buf);
	buf[jlen] = 0;
	return jlen;
}
static char* get_java_string_region2(JNIEnv* e, jstring js, char*buf, int len) {
	char*ret = buf;
	int jlen = (*e)->GetStringUTFLength(e, js);
	if (jlen > len - 1)
		ret = (char*) malloc(jlen);
	if (ret) {
		(*e)->GetStringUTFRegion(e, js, 0, jlen, (jbyte*) ret);
		ret[jlen] = 0;
	}
	return ret;
}

static int ca_module_callback_impl(void*m, void*cbo, ca_module_io_type type, const char*json,
		char*ret, int len) {
	struct module_peer_t*peer = (struct module_peer_t*) cbo;
	JNIEnv* e = attach_jnienv();
	if (e && peer) {
		switch (type) {
		case CAMODULE_CLOSED:
			(*e)->CallStaticObjectMethod(e, g_nmod.clazz, g_nmod.callback, peer->wo, 0, 0, 0, NULL);
			(*e)->DeleteGlobalRef(e, peer->wo);
			dlclose(peer->lib);
			free(peer);
			break;
		case CAMODULE_PAUSED:
		case CAMODULE_RESUMED:
			(*e)->CallStaticObjectMethod(e, g_nmod.clazz, g_nmod.callback, peer->wo, type, 0, 0,
					NULL);
			break;
		case CAMODULE_MANAGER:
		case CAMODULE_SESSION: {
			LOGD("ca_module_callback_impl session 00");
			jstring arg = json ? (*e)->NewStringUTF(e, (const char*) json) : NULL;
			LOGD("ca_module_callback_impl session 11 peer = %p",peer);
			LOGD("ca_module_callback_impl session 11 peer->wo = %p",peer->wo);
			jstring jret = (jstring)(*e)->CallStaticObjectMethod(e, g_nmod.clazz, g_nmod.callback,
					peer->wo, type, 0, 0, arg);
			LOGD("ca_module_callback_impl session 22");
			(*e)->DeleteLocalRef(e, arg);
			LOGD("ca_module_callback_impl release localRef"); 
			return jret ? get_java_string_region(e, jret, ret, len) : 0;
		}
		default:
			break;
		}
	}
	return 0;
}
typedef int (*ca_module_interface_get_stub)(struct ca_module_interface**cami);

JNIEXPORT jint Java_ipaneltv_toolkit_camodule_CaNativeModule_nload(JNIEnv *e, jobject thiz,
		jstring jln, jlong idm, jlong idl) {
	const char*mi_name = "ca_module_interface_get";
	int ret = -1;
	void*mi = NULL;
	ca_module_interface_get_stub istub = NULL;
	struct module_peer_t*p = (struct module_peer_t*) (*e)->GetIntField(e, thiz, g_nmod.peer);
	if (p || jln == NULL)
		return -1;
	if ((p = (struct module_peer_t*) calloc(1, sizeof(*p))) == NULL) {
		LOGD("out of memory");
		goto BAIL;
	}
	get_java_string_region(e, jln, p->buf, _BUF_LEN_);
	
	LOGD("dlopen1111 err:%s.\n",dlerror());
	if ((p->lib = dlopen(p->buf, RTLD_NOW)) == NULL) {
		LOGD("load library(%s) failed!", p->buf);
		LOGD("dlopen2222 err:%s.\n",dlerror());
		goto BAIL;
	}

	if ((istub = (ca_module_interface_get_stub)dlsym(p->lib, mi_name)) == NULL) {
		LOGD("get module(%s) interface(%s) failed", p->buf, mi_name);
		goto BAIL;
	}
	if (istub(&p->mi) >= 0 ? p->mi == NULL : 1) {
		LOGD("ca_module(%s)_interface_get(%s) call failed", p->buf, mi_name);
		goto BAIL;
	}
	LOGD("ca_module_interface_get call idm (%lld) idl(%lld)", idm, idl);
	//p->uuid.most = idm;
	//p->uuid.least = idl;
	AUUID_fromMostLeast(&p->uuid,idm,idl);
	//LOGD("ca_module_interface_get call most (%lld) least(%lld)", p->uuid.most, p->uuid.least);
	(*e)->SetIntField(e, thiz, g_nmod.peer, (jint) p);
	ret = 0;
	BAIL: if (ret != 0) {
		if (p) {
			if (p->lib)
				dlclose(p->lib);
			free(p);
		}
	}
	return ret;
}

JNIEXPORT jint Java_ipaneltv_toolkit_camodule_CaNativeModule_nopen(JNIEnv *e, jobject thiz,
		jobject wo, jstring jargs) {
	int ret = -1;
	struct module_peer_t*p = (struct module_peer_t*) (*e)->GetIntField(e, thiz, g_nmod.peer);
	if (p) {
		get_java_string_region(e, jargs, p->buf, _BUF_LEN_);
		if (p->wo == NULL)
			p->wo = (*e)->NewGlobalRef(e, wo);
		if (p->mi && p->mo == NULL) {
			if ((p->mo = p->mi->open(&p->uuid, p->buf, p, ca_module_callback_impl)))
				ret = 0;
		}
	}
	return ret;
}

JNIEXPORT jint Java_ipaneltv_toolkit_camodule_CaNativeModule_nclose(JNIEnv *e, jobject thiz) {
	int ret = -1;
	struct module_peer_t*p = (struct module_peer_t*) (*e)->GetIntField(e, thiz, g_nmod.peer);
	if (p) {
		if (p->mo && p->mi) {
			if (p->started)
				p->mi->stop(p->mo);
			p->mi->close(p->mo);
			p->mo = NULL;
			ret = 0;
		}
	}
	return ret;
}

JNIEXPORT jint Java_ipaneltv_toolkit_camodule_CaNativeModule_nstart(JNIEnv *e, jobject thiz) {
	int ret = -1;
	struct module_peer_t*p = (struct module_peer_t*) (*e)->GetIntField(e, thiz, g_nmod.peer);
	if (p) {
		if (p->mo && p->mi) {
			if (p->started == 0) {
				if ((ret = p->mi->start(p->mo)) >= 0)
					p->started = 1;
			}
		}
	}
	return ret;
}
JNIEXPORT jint Java_ipaneltv_toolkit_camodule_CaNativeModule_nstop(JNIEnv *e, jobject thiz) {
	int ret = -1;
	struct module_peer_t*p = (struct module_peer_t*) (*e)->GetIntField(e, thiz, g_nmod.peer);
	if (p) {
		if (p->mo && p->mi) {
			if (p->started) {
				p->mi->stop(p->mo);
				p->started = 0;
				ret = 0;
			}
		}
	}
	return ret;
}
JNIEXPORT jint Java_ipaneltv_toolkit_camodule_CaNativeModule_nlocalize(JNIEnv *e,
		jobject thiz, int code, jstring jstr) {
	int ret = -1;
	struct module_peer_t*p = (struct module_peer_t*) (*e)->GetIntField(e, thiz, g_nmod.peer);
	if (p && jstr && code >= 0) {
		if (p->mo && p->mi) {
			get_java_string_region(e, jstr, p->buf, _BUF_LEN_);
			p->mi->localize(p->mo, code, p->buf);
		}
	}
	return ret;
}
JNIEXPORT jstring Java_ipaneltv_toolkit_camodule_CaNativeModule_nmtransmit(JNIEnv *e,
		jobject thiz, jstring json) {
	int ret = -1;
	jstring jret = NULL;
	char buf[256], *pbuf;
	struct module_peer_t*p = (struct module_peer_t*) (*e)->GetIntField(e, thiz, g_nmod.peer);
	if (p) {
		if (p->mo && p->mi && p->started) {
			if ((pbuf = get_java_string_region2(e, json, buf, 256))) {
				if ((ret = p->mi->manageTransmit(p->mo, pbuf, p->buf, _BUF_LEN_)) > 0)
					jret = (*e)->NewStringUTF(e, (const char*) p->buf);
			}
			if (pbuf != buf)
				free(pbuf);
		}
	}
	return jret;
}
JNIEXPORT jstring Java_ipaneltv_toolkit_camodule_CaNativeModule_nstransmit(JNIEnv *e,
		jobject thiz, jstring json) {
	int ret = -1;
	jstring jret = NULL;
	char buf[256], *pbuf;
	struct module_peer_t*p = (struct module_peer_t*) (*e)->GetIntField(e, thiz, g_nmod.peer);
	if (p) {
		if (p->mo && p->mi && p->started) {
			if ((pbuf = get_java_string_region2(e, json, buf, 256))) {
				if ((ret = p->mi->sessionTransmit(p->mo, pbuf, p->buf, _BUF_LEN_)) > 0)
					jret = (*e)->NewStringUTF(e, (const char*) p->buf);
			}
			if (pbuf != buf)
				free(pbuf);
		}
	}
	return jret;
}
JNIEXPORT jint Java_ipaneltv_toolkit_camodule_CaNativeModule_nsetprop(JNIEnv *e,
		jobject thiz, jstring jname, jstring jvalue) {
	int ret = -1;
	char namebuf[_NAME_BUF_LEN_];
	struct module_peer_t*p = (struct module_peer_t*) (*e)->GetIntField(e, thiz, g_nmod.peer);
	if (p && jname && jvalue) {
		if (p->mo && p->mi) {
			get_java_string_region(e, jname, namebuf, _NAME_BUF_LEN_);
			get_java_string_region(e, jvalue, p->buf, _BUF_LEN_);
			p->mi->setprop(p->mo, namebuf, p->buf);
		}
	}
	return ret;
}

JNIEXPORT jstring Java_ipaneltv_toolkit_camodule_CaNativeModule_ngetprop(JNIEnv *e,
		jobject thiz, jstring jname) {
	int ret = -1;
	char namebuf[_NAME_BUF_LEN_];
	struct module_peer_t*p = (struct module_peer_t*) (*e)->GetIntField(e, thiz, g_nmod.peer);
	if (p && jname) {
		if (p->mo && p->mi) {
			get_java_string_region(e, jname, namebuf, _NAME_BUF_LEN_);
			ret = p->mi->getprop(p->mo, namebuf, p->buf, _BUF_LEN_);
			if (ret > 0)
				return (*e)->NewStringUTF(e, (const char*) p->buf);
		}
	}
	return NULL;
}

int initJavaCaNativeModule(JNIEnv *e) {
	jclass cls;
	LOGD("initJavaCaNativeModule");
	static const char* g_class_name = "ipaneltv/toolkit/camodule/CaNativeModule";
	if ((cls = (*e)->FindClass(e, g_class_name)) == NULL) {
		LOGE("Can't find class : %s", g_class_name);
		return -1;
	}
	g_nmod.clazz = (jclass)(*e)->NewGlobalRef(e, cls);
	if ((g_nmod.peer = (*e)->GetFieldID(e, g_nmod.clazz, "peer", "I")) == NULL) {
		LOGE("no such field: peer");
		return -1;
	}
	if ((g_nmod.callback = (*e)->GetStaticMethodID(e, cls, "native_callback",
			"(Ljava/lang/Object;IIILjava/lang/String;)Ljava/lang/String;")) == NULL) {
		LOGE("can't find method native_callback of class: %s", g_class_name);
		return -1;
	}
	return 0;
}

