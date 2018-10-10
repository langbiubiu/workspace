#ifndef CA_MODULE_INTERFACE__H_
#define CA_MODULE_INTERFACE__H_

#include <stdint.h>
#include <androidtv/uuid.h>

#ifdef __cplusplus
extern "C" {
#endif

typedef enum {
	/** 模块已关闭 */
	CAMODULE_CLOSED = 0,
	/** 模块未获得卡或失去卡 */
	CAMODULE_PAUSED = 1,
	/** 模块已得到卡 */
	CAMODULE_RESUMED = 2,
	//管理程序回调JSON
	CAMODULE_MANAGER = 3,
	//客户端会话回调JSON
	CAMODULE_SESSION = 4
} ca_module_io_type;

/** 回调到上层管理模块的 */
typedef int (*ca_module_callback)(void*m, void*cbo, ca_module_io_type type, const char*json,
		char*ret, int len);

struct ca_module_interface {
	/** 打开模块 */
	void*(*open)(AUUID*uuid, const char*args, void*cbo, ca_module_callback f);
	/** 关闭模块 */
	void (*close)(void*o);
	/** 设置本地化的消息字符串 code [0,255] */
	int (*localize)(void*o, int code, const char*name);
	/** 启动模块,return moduleId */
	int (*start)(void*o);
	/** 停止模块 */
	void (*stop)(void*o);
	/** 设置属性 */
	int (*setprop)(void*o, const char*name, const char*value);
	/** 得到属性 */
	int (*getprop)(void*o, const char*name, char*buf, int len);
	/** 管理传输数据 */
	int (*manageTransmit)(void*o, const char*json, char*ret, int len);
	/** 客户会话传输数据 */
	int (*sessionTransmit)(void*o, const char*json, char*ret, int len);
};

//动态库必须导出此函数
extern int ca_module_interface_get(struct ca_module_interface**cami);

#ifdef __cplusplus
}
;
#endif

#endif /*  CA_MODULE_INTERFACE__H_*/

