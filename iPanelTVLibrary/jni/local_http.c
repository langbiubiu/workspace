#define LOAG_TAG "[jni]ipaneltvlib-local_http"
#include "local_http.h"

/* 对URL进行编码 */
int local_http_xurl_encode(char* url, int urllen, char *buf, int size) {
	static char table[] = { '0', '1', '2', '3', '4', '5', '6', '7', '8', '9',
			'A', 'B', 'C', 'D', 'E', 'F' };
	int i = 0, n = 0;
	LOGD("local_http_xurl_encode  url = %s  , size = %d \n", n);
	while (url[i] && n < size) {
		if (' ' < url[i] && url[i] <= '~') {
			buf[n++] = url[i];
		} else if (url[i] == 13) {
			break;
		} else {
			buf[n++] = '%';
			buf[n++] = table[((unsigned char) url[i]) >> 4];
			buf[n++] = table[((unsigned char) url[i]) & 0xf];
		}
		i++;
	}
	buf[n++] = '\0';
	LOGD("local_http_xurl_encode  n = %d \n", n);
	return n - 1;
}

struct HttpMgr *http_create(int flags) {
	struct HttpMgr *mgr = (struct HttpMgr *) malloc(sizeof(*mgr));
	if (!mgr) {
		LOGE("http_create mgr malloc failed\n");
		goto FAIL;
	}
	mgr->bufsize = HTTP_BUF_SIZE;
	if (!(mgr->buf = (char*) malloc(mgr->bufsize))) {
		LOGE("http_create httpbuf malloc failed!");
		goto FAIL;
	}
	pipe(mgr->fds);
	LOGD("http_create httpbuf fds = %d:%d \n", mgr->fds[0], mgr->fds[0]);
	memset(mgr->buf, 0, mgr->bufsize);
	memset(mgr->ip_host, 0, sizeof(mgr->ip_host));
	memset(mgr->url, 0, sizeof(mgr->url));
	mgr->seek_pos = 0;
	return mgr;
	FAIL: {
		if (mgr) {
			mgr->run_flag = 0;
			if (mgr->buf)
				free(mgr->buf);
			free(mgr);
		}
		return NULL;
	}
}

int http_connect(struct HttpMgr *mgr, char *url) {
	int ret = -1, recv_buf_size = 64 * 1024, keepalive = 1;
	struct hostent *h;
	struct sockaddr_in servaddr;
	if (!mgr) {
		LOGD("http_connect mgr = %p", mgr);
		goto FAIL;
	}
	char *hostname, *temp, *p;
	memcpy(mgr->url, url, strlen(url));
	LOGD("http_connect url url = %s ; mgr->url = %s", url, mgr->url);
	if (strncmp(mgr->url, SCHEMA, strlen(SCHEMA)) != 0) {
		LOGD("http_connect url isn't start with \"http://\" url = %s",
				mgr->url);
		goto FAIL;
	}
	temp = strstr(url, "://");
	temp += 3;
	if (!(hostname = strtok(temp, ":"))) {
		LOGD("http_connect get hostname failed!");
		goto FAIL;
	}
	LOGD("http_connect url url = %s ; mgr->url = %s", url, mgr->url);
	if (!(p = strtok(NULL, ":"))) {
		LOGD("http_connect get port failed!");
		goto FAIL;
	}
	mgr->port = atoi(p);
	mgr->timeout = 50;
	mgr->run_flag = 1;
	memcpy(mgr->ip_host, hostname, strlen(hostname));
	int len = strlen(hostname);
	LOGD("http_connect hostname =%s, port =%d,len = %d,mgr->ip_host = %s.",
			hostname, mgr->port, len, mgr->ip_host);
	if ((h = gethostbyname(hostname)) == NULL) {
		LOGD("http_connect gethostbyname failed strerror(%d) =%s\n", errno,
				strerror(errno));
		goto FAIL;
	}
	LOGD("http_connect name = %s,IP Address :%s\n", h->h_name,
			inet_ntoa(*((struct in_addr * ) h->h_addr)));
	if ((mgr->sock_fd = socket(AF_INET, SOCK_STREAM, 0)) < 0) {
		LOGD("main socket create failed  strerror(%d) =%s\n", errno,
				strerror(errno));
		goto FAIL;
	}
	fcntl(mgr->sock_fd, F_SETFL, O_NONBLOCK);
	setsockopt(mgr->sock_fd, SOL_SOCKET, SO_RCVBUF, &recv_buf_size,
			sizeof(int));
	setsockopt(mgr->sock_fd, SOL_SOCKET, SO_KEEPALIVE, &keepalive,
			sizeof(keepalive));

	memset(&servaddr, 0, sizeof(servaddr));
	servaddr.sin_family = AF_INET;
	servaddr.sin_port = htons(mgr->port);
	if (inet_pton(AF_INET, inet_ntoa(*((struct in_addr *) h->h_addr)),
			&servaddr.sin_addr) <= 0) {
		LOGD("http_connect inet_pton error for %s\n",
				inet_ntoa(*((struct in_addr * ) h->h_addr)));
		goto FAIL;
	}

	int i = connect(mgr->sock_fd, (struct sockaddr*) &servaddr,
			sizeof(servaddr));
	LOGD("http_connect connect i = %d ;mgr->url = %s \n", i, mgr->url);
	ret = 0;
	FAIL: return ret;
}

int http_request(struct HttpMgr *mgr, int timeout) {
	int ret = -1, size = 0, retry = 0, i = 0, maxfd;
	char *http_url = NULL, *p = NULL, *end = NULL, *ptr = NULL, *str = NULL,
			tmp[256], request[1024];
	if (!mgr) {
		goto FAIL;
	}
	mgr->timeout = timeout;
	fd_set readset, writeset, exceptset, readfd;
	struct timeval tv = { mgr->timeout / 1000, mgr->timeout * 1000 };
	time_t time_now;
	http_url = mgr->url;
	LOGD("http_request 11 http_url = %s ; mgr->url=%s \n", http_url, mgr->url);
	memset(tmp, 0, sizeof(tmp));
	p = mgr->buf;
	p += sprintf(p, "GET ");
	/* 去掉http请求前缀*/
	/* 去掉http请求前缀*/
	http_url = strstr(http_url, "://");
	http_url += 3;
	http_url = strstr(http_url, "/");
	LOGD("http_request http_url = %s \n", http_url);
	/* url encode */
	if (http_url && http_url[0]) {
		LOGD("http_request local_http_xurl_encode \n");
		size = mgr->buf + mgr->bufsize - p;
		p += local_http_xurl_encode(http_url, strlen(http_url), p, size);
	} else {
		p += sprintf(p, "/");
	}
	/* 版本 */
	p += sprintf(p, " HTTP/1.1");

	/* host information */
	p += sprintf(p, "\r\nHost: ");
	LOGD("http_request mgr->ip_host = %s \n", mgr->ip_host);
	p += sprintf(p, "%s:%d", mgr->ip_host, mgr->port);
	/* 接收类型 */
	p += sprintf(p, "\r\nAccept: */*");

	/* 开始位置 */
	//	p += sprintf( p, "\r\nAccept-Ranges: bytes=0-");
	/* 断点续传 */
	p += sprintf(p, "\r\nRange: bytes=%llu-", mgr->seek_pos);

	if (mgr->ipanel_server == 1 && mgr->seek_time != (unsigned int) (-1)) {
		p += sprintf(p, "\r\nRangeTime: %u", mgr->seek_time);
	}
	p += sprintf(p, "\r\nUser-Agent: IPANEL AVPLAYER");
	/* 接收语言以及编码 */

	p += sprintf(p,
			"\r\nAccept-language: zh-cn\r\nAccept-Encoding: gzip, deflate");

	/* user-agent */
	//p += sprintf( p, "\r\nUser-Agent: Mozilla/4.0 (compatible; MSIE 6.0; Windows NT 5.1 ; Broadcom)");
	p += sprintf(p, "\r\nPragma: no-cache");
	p += sprintf(p, "\r\nCache-Control: no-cache");

//	if (mgr->cookie && mgr->cookie[0])
//		p += sprintf(p, "\r\nCookie: %s", mgr->cookie);
	p += sprintf(p, "\r\nConnection: Keep-Alive");
	p += sprintf(p, "\r\nProxy-Connection: Keep-Alive\r\n\r\n");
	*p = 0;
	LOGD("http_request mgr->buf = %s\n", mgr->buf);
	time_now = time(NULL);

	for (i = 0; i < HTTP_SEND_TIMES; i++) {

		if ((!mgr) || (mgr && mgr->run_flag == 0)) {
			ret = -1;
			LOGD("http_request me==null or player closing quit send!\n");
			goto FAIL;
		}

		maxfd = mgr->sock_fd < mgr->fds[0] ? mgr->fds[0] : mgr->sock_fd;

		FD_ZERO(&writeset);
		FD_SET(mgr->sock_fd, &writeset);

		FD_SET(mgr->fds[0], &writeset);
		ret = select(maxfd + 1, NULL, &writeset, NULL, &tv);
		LOGD("http_request select writeset 22 ret = %d\n", ret);
		if (ret > 0) {
			if (FD_ISSET(mgr->fds[0], &writeset)) {
				//TODO
				LOGD("http_request send mgr->fds[0] have data!");
			}

			if (FD_ISSET(mgr->sock_fd, &writeset)) {
				/* 发送Http请求 */
				ret = send(mgr->sock_fd, mgr->buf, p - mgr->buf, 0);
				LOGD("http_request send ret = %d\n", ret);
				if (ret == -1) {
					sleep(5);
				} else {
					LOGD("http_request send(%d) %s\n", ret, mgr->buf);
					break;
				}
			}
		}
	}
	LOGD("http_request socket request send cost time:%u(ms),at time = %d\n",
			time(NULL) - time_now, time(NULL));
	if (i == HTTP_SEND_TIMES && ret <= 0) {
		LOGD("http_request request send failed\n");
		ret = -1;
		goto FAIL;
	}
	/* 接收响应头部 */
	end = NULL;
	if ((!mgr) || (mgr && mgr->run_flag == 0)) {
		LOGD("http_request me==null or player closing \n");
		ret = -1;
		goto FAIL;
	}
	mgr->datasize = 0;
	/* 未超时，且接收的数据没有超过缓冲区大小*/
	retry = 0;
	memset(mgr->buf, 0, HTTP_BUF_SIZE);
//HTTP_RESERVED至少为1
#define HTTP_RESERVED  8
	time_now = time(NULL);
	int len = -1;
	char buf[1];
	int index = 0;
	int check = 0;
	while (retry < HTTP_REQUEST_TIMES
			&& mgr->datasize + HTTP_RESERVED < mgr->bufsize) {
		if ((!mgr) || (mgr && mgr->run_flag == 0)) {
			ret = -1;
			LOGD("http_request me==null or player closing quit recv 3\n");
			goto FAIL;
		}
		maxfd = mgr->sock_fd < mgr->fds[0] ? mgr->fds[0] : mgr->sock_fd;
		FD_ZERO(&readset);
		FD_SET(mgr->sock_fd, &readset);
		FD_SET(mgr->fds[0], &readset);
		ret = select(maxfd + 1, &readset, NULL, NULL, &tv);
		LOGD("http_request select 22 ret =  %d \n", ret);
		if (ret > 0) {

			if (FD_ISSET(mgr->fds[0], &writeset)) {
				//TODO
				LOGD("http_request recv mgr->fds[0] have data!");
			}

			if (FD_ISSET(mgr->sock_fd, &readset)) {
				len = recv(mgr->sock_fd, buf, 1, 0);
				//			LOGD("http_request len = %d ;", len);
				retry++;
				if (ret < 0) {
					// 由于是非阻塞的模式,所以当errno为EAGAIN时,表示当前缓冲区已无数据可读
					// 在这里就当作是该次事件已处理
					LOGD("http_request errno = %d ;", errno);
				} else if (len == 0) {
					// 这里表示对端的socket已正常关闭.
					return -1;
				} else {
					if (buf[0] == '\r') {
						//					LOGD("http_request 1111");
					} else if (buf[0] == '\n') {
						//					LOGD("http_request 2222 check = %d \n", check);
						if (check == 0) {
							check = 1;
							LOGD("http_request mgr->buf = %s;index= %d \n",
									mgr->buf, index);
							memset(mgr->buf, 0, index);
							index = 0;
						} else {
							return mgr->sock_fd;
						}
					} else {
						//					LOGD("http_request 3333");
						check = 0;
						memcpy(mgr->buf + index, buf, 1);
						index = index + 1;
					}
				}
			}
		}
	}
	FAIL: {
		return ret;
	}
}

int http_interrupt(struct HttpMgr *mgr) {

}

int http_destroy(struct HttpMgr *mgr) {
	if (!mgr) {
		return -1;
	}
	mgr->run_flag = 0;
	close(mgr->fds[0]);
	close(mgr->fds[0]);
	if (mgr->buf)
		free(mgr->buf);
	free(mgr);
	return 0;
}
