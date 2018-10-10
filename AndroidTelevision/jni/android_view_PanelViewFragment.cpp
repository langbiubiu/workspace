#define LOG_TAG "[jni]PanelViewFragment"
#include <utils/Log.h>
#include <tvs/tvsdex.h>
#include <nativehelper/jni.h>
#include <nativehelper/JNIHelp.h>
#include <android_runtime/AndroidRuntime.h>
#include <androidtv/teevee_utils.h>

#include "native_init.h"

using namespace android;

static int native_set_text(JNIEnv *env, jobject thiz, jstring jtext, jint flags) {
	int ret = -1;
	const char*text = NULL;
	LOGD("native_set_text is in ");
	if (jtext == NULL)
		return -1;
	if ((text = env->GetStringUTFChars(jtext, NULL)) != NULL) {
		LOGD("native_set_text text = %s, flags = %d", text, flags);
		ret = ATeeveeUtils_setPanelText((char*) text, flags);
		env->ReleaseStringUTFChars(jtext, text);
	}
	LOGD("native_set_text is out ret=%d ", ret);
	return ret;
}

static int native_set_light(JNIEnv *env, jobject thiz, jstring jname, jint color, jint flags) {
	int ret = -1;
	const char*name = NULL;
	if (jname == NULL)
		return -1;
	if ((name = env->GetStringUTFChars(jname, NULL))) {
		LOGD("native_set_text color = %d, flags = %d", color, flags);
		ret = ATeeveeUtils_setPanelLight((char*) name, color, flags);
		env->ReleaseStringUTFChars(jname, name);
	}
	return ret;
}

static const char* g_class_name = "android/view/PanelViewFragment";

static JNINativeMethod g_cls_methods[] = { //
	{"nativeSetText", "(Ljava/lang/String;I)I", (void*) native_set_text},
	{"nativeSetLight","(Ljava/lang/String;II)I", (void*) native_set_light}
};

int register_panel_view_fragment_natives(JNIEnv *e) {
	return AndroidRuntime::registerNativeMethods(e, g_class_name, g_cls_methods,
			NELEM(g_cls_methods));
}
