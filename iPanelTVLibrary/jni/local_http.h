#ifndef __LOCAL_HTTP_H_
#define __LOCAL_HTTP_H_

#include <netdb.h>
#include <errno.h>
#include <fcntl.h>
#include <sys/types.h>
#include <sys/socket.h>
#include <arpa/inet.h>
#include <netinet/in.h>

#include "common_include.h"

#ifdef __cplusplus
extern "C" {
#endif

#define SCHEMA  "http://"
#define HTTP_BUF_SIZE    (64*1024)
#define HTTP_RECV_SIZE   (2560*1024)
#define HTTP_SEND_TIMES        (10)
#define HTTP_REQUEST_TIMES     (1000) //20s

struct HttpMgr{
	jobject wo;
	char *buf;
	int bufsize;
	int datasize;
	int fds[2];
	char ip_host[64];
	char url[1024];
	int sock_fd;
	int port;
	int run_flag;
	unsigned int timeout;
	unsigned int ipanel_server;
	unsigned int seek_time;
	int64_t seek_pos;
};

int local_http_xurl_encode(char* url, int urllen, char *buf, int size);
struct HttpMgr *http_create(int flags);
int http_connect(struct HttpMgr *handle, char *url);
int http_destroy(struct HttpMgr *handle);
int http_interrupt(struct HttpMgr *mgr);
int http_request(struct HttpMgr *handle,int timeout);
#ifdef __cplusplus
}
#endif
#endif//__LOCAL_HTTP_H_
