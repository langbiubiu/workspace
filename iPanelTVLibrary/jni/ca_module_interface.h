#ifndef CA_MODULE_INTERFACE__H_
#define CA_MODULE_INTERFACE__H_

#include <stdint.h>
#include <androidtv/uuid.h>

#ifdef __cplusplus
extern "C" {
#endif

typedef enum {
	/** ģ���ѹر� */
	CAMODULE_CLOSED = 0,
	/** ģ��δ��ÿ���ʧȥ�� */
	CAMODULE_PAUSED = 1,
	/** ģ���ѵõ��� */
	CAMODULE_RESUMED = 2,
	//�������ص�JSON
	CAMODULE_MANAGER = 3,
	//�ͻ��˻Ự�ص�JSON
	CAMODULE_SESSION = 4
} ca_module_io_type;

/** �ص����ϲ����ģ��� */
typedef int (*ca_module_callback)(void*m, void*cbo, ca_module_io_type type, const char*json,
		char*ret, int len);

struct ca_module_interface {
	/** ��ģ�� */
	void*(*open)(AUUID*uuid, const char*args, void*cbo, ca_module_callback f);
	/** �ر�ģ�� */
	void (*close)(void*o);
	/** ���ñ��ػ�����Ϣ�ַ��� code [0,255] */
	int (*localize)(void*o, int code, const char*name);
	/** ����ģ��,return moduleId */
	int (*start)(void*o);
	/** ֹͣģ�� */
	void (*stop)(void*o);
	/** �������� */
	int (*setprop)(void*o, const char*name, const char*value);
	/** �õ����� */
	int (*getprop)(void*o, const char*name, char*buf, int len);
	/** ���������� */
	int (*manageTransmit)(void*o, const char*json, char*ret, int len);
	/** �ͻ��Ự�������� */
	int (*sessionTransmit)(void*o, const char*json, char*ret, int len);
};

//��̬����뵼���˺���
extern int ca_module_interface_get(struct ca_module_interface**cami);

#ifdef __cplusplus
}
;
#endif

#endif /*  CA_MODULE_INTERFACE__H_*/

