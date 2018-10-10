package ipaneltv.toolkit;

import java.io.IOException;

import android.net.LocalServerSocket;
import android.net.LocalSocket;
import android.util.Log;

public abstract class LocalSockAcceptor {
	static final String TAG = "LocakSocketAsyncTask";
	Thread t = null;
	int version;
	String name;
	LocalServerSocket lis;
	boolean canceled = false, scheudled = false;;
	LocalSocket sock;
	private Object mutex = new Object();

	public LocalSockAcceptor(String name) {
		this.name = new String(name);
	}

	public LocalSocket getLocalSocket() {
		return sock;
	}

	public void schedule(int ver) {
		IPanelLog.d(TAG, "schedule ver = " + ver + ",name=" + name);
		synchronized (name) {
			if (scheudled)
				throw new RuntimeException("once only!");
			scheudled = true;
		}
		if (t != null)
			throw new RuntimeException("object can use on once!");
		IPanelLog.d(TAG, "schedule t = " + t);
		t = new Thread(new Runnable() {
			@Override
			public void run() {
				LocalSocket sock = null;
				try {
					IPanelLog.d(TAG, "schedule canceled = " + canceled);
					synchronized (name) {
						if (!canceled)
							lis = new LocalServerSocket(name);
					}
					IPanelLog.d(TAG, "schedule lis = " + lis);
					synchronized (mutex) {
						mutex.notify();
					}
					if (lis != null) {
						sock = lis.accept();
						IPanelLog.d(TAG, "socket accept sock = " + sock);
						lis.close();
					}
				} catch (Exception e) {
					IPanelLog.d(TAG, "accept error:" + e);
				}
				try {
					synchronized (name) {
						IPanelLog
								.d(TAG, "socket accept canceled = " + canceled + ";sock = " + sock);
						if (canceled) {
							if (sock != null) {
								sock.close();
								sock = null;
							}
							LocalSockAcceptor.this.sock = null;
							return;
						}
					}
						LocalSockAcceptor.this.sock = sock;
						IPanelLog.d(TAG, "onAcceptOver sock = " + sock + ",version=" + version);
						onAcceptOver(sock, version);

				} catch (Exception e) {
					IPanelLog.d(TAG, "onTaskOver error:" + e);
				}
			}
		});
		IPanelLog.d(TAG, "schedule end");
		version = ver;
		synchronized (mutex) {
			t.start();
			// 添加延时，等localSocket准备好再返回，以免出现客户端已经开始连接 但是localSocket还没有准备好的情况。
			Log.d(TAG, "schedule in");
			try {
				mutex.wait(500);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
			Log.d(TAG, "schedule out");
		}
	}

	public void dropSock() {
		synchronized (name) {
			try {
				if (sock != null)
					sock.close();
			} catch (Exception e) {
			}
			sock = null;
		}
	}

	public abstract void onAcceptOver(LocalSocket ls, int version);

	public void cancel() {
		synchronized (name) {
			dropSock();
			if (!canceled) {
				canceled = true;
				if (lis != null) {
					try {
						lis.close();
					} catch (IOException e) {
					}
				}
			}
		}
	}

}
