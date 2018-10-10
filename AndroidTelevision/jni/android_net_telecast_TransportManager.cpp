#define LOG_TAG "[jni]TransportManager"
#include <utils/Log.h>
#include <dlfcn.h>
#include <tvs/tvsdex.h>
#include <nativehelper/jni.h>
#include <nativehelper/JNIHelp.h>
#include <android_runtime/AndroidRuntime.h>
#include <androidtv/transport_manager.h>

#include "native_init.h"

using namespace android;

static struct {
	jclass clazz;
	jmethodID init;
} g_netif;

static int _convert(int t) {
	switch (t) {
	case ATRANSPORT_DELIVERY_TYPE_CABLE:
		return 'C';
	case ATRANSPORT_DELIVERY_TYPE_SATELLITE:
		return 'S';
	case ATRANSPORT_DELIVERY_TYPE_TERRESTRIAL:
		return 'T';
	case ATRANSPORT_DELIVERY_TYPE_UNKNOWN:
	default:
		return 0;
	}
}

jobject nativeNewNI(JNIEnv *e, ATransportInterfaceInfo*info) {
	assert(info);
	jobject obj = e->NewObject(g_netif.clazz, g_netif.init, //
			info->id, //
			_convert(info->type),//
			info->isRemote, //
			info->isTdma);
	return obj;
}
static int nativeGetNIs(JNIEnv *env, jobject thiz, jobjectArray nis) {
	int size, i;
	ATransportInterfaceInfo info[1];
	assert(env->GetArrayLength(nis) >= 8);
	if ((size = ATransportManager_networkInterfaceSize()) < 0)
		return -1;
	//LOGD("nativeGetNIs %d",size);
	for (i = 0; i < size; i++) {
		if (ATransportManager_networkInterfaceInfo(i, info) == 0) {
			jobject obj = nativeNewNI(env, info);
			//LOGD("nativeGetNIs %d, %p",i,obj);
			env->SetObjectArrayElement(nis, i, obj);
		}
	}
	return size;
}

static int native_set_config(JNIEnv *env, jobject thiz, jstring jname, jstring jvalue) {
	const char*name = NULL;
	const char*value = NULL;

	if (jname == NULL || jvalue == NULL) {
		return throw_runtime_exception(env, "null pointer");
	}

	name = env->GetStringUTFChars(jname, NULL);
	value = env->GetStringUTFChars(jvalue, NULL);
	LOGD("native_set_property name:%s, value:%s", name, value);
	if (name != NULL && value != NULL) {
		return ATransportManager_setConfig(name, value);
	}
	return -1;
}

static jstring native_get_config(JNIEnv *env, jobject thiz,jstring jname){
	const char*name = NULL;
	char value[96];
	int ret = -1;
	jstring jvalue = NULL;

	if (jname == NULL) {
		throw_runtime_exception(env, "null pointer");
		return NULL;
	}

	name = env->GetStringUTFChars(jname, NULL);
	if (name != NULL) {
		ret = ATransportManager_getConfig(name, value, 96);
		LOGI("native_get_property name:%s, value:%s", name, value);
		if (ret > 0) {
			jvalue = (jstring) env->NewStringUTF((char*) value);
			return jvalue;
		}
	}
	return NULL;
}

static int (*ATransportManager_setProcessLevel)(const char*, int, int, int);
static jint native_setProcessLevel(JNIEnv *env, jobject thiz, jstring jserverName, jint pid,
		jint level, jint flags) {
	static void*mod = NULL;
	const char*name = NULL;
	int ret = -1;
	if (mod == NULL) {
		mod = dlopen("libandroidtv_native.so", RTLD_NOW);
		if ((ATransportManager_setProcessLevel = (int(*)(const char*, int, int, int)) dlsym(mod,
				"ATransportManager_setProcessLevel")) == NULL) {
			LOGE("no method found:ATransportManager_setProcessLevel");
			return -1;
		}
	}
	if (ATransportManager_setProcessLevel) {
		if ((name = env->GetStringUTFChars(jserverName, NULL))) {
			ret = ATransportManager_setProcessLevel(name, pid, level, flags);
			env->ReleaseStringUTFChars(jserverName, name);
		}
	}
	return ret;
}

static const char* g_class_name = "android/net/telecast/TransportManager";
static JNINativeMethod g_cls_methods[] = { //
	{ "nativeGetNIs","([Landroid/net/telecast/NetworkInterface;)I",(void*) nativeGetNIs },
	{ "native_setTvConfig","(Ljava/lang/String;Ljava/lang/String;)I",(void*) native_set_config},
	{ "native_getTvConfig","(Ljava/lang/String;)Ljava/lang/String;",(void*) native_get_config},
	{ "native_setProcessLevel","(Ljava/lang/String;III)I",(void*) native_setProcessLevel}
};

int register_transport_manager_natives(JNIEnv *e) {
	const char*niclsnam = "android/net/telecast/NetworkInterface";
	jclass cls = e->FindClass(niclsnam);
	if (cls == NULL) {
		LOGE("can't find class : %s", niclsnam);
		return -1;
	}
	g_netif.clazz = (jclass) e->NewGlobalRef(cls);
	if ((g_netif.init = e->GetMethodID(cls, "<init>", "(IIZZ)V")) == NULL) {
		LOGE("can't find method <init> of class: %s", niclsnam);
		return -1;
	}
	if (ATransportManager_ensureService() != 0) {
		LOGE("tv system service is invalid!");
		return -1;
	}
	return AndroidRuntime::registerNativeMethods(e, g_class_name, g_cls_methods,
			NELEM(g_cls_methods));
}
