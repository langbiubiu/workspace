package ipaneltv.toolkit;

import java.io.IOException;
import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import org.json.JSONException;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.IBinder;
import android.os.RemoteException;

public abstract class JsonChannel {
	protected static final String TAG = "JsonChannel";
	static HashMap<String, Service> services = new HashMap<String, Service>();

	static class Service implements ServiceConnection ,IBinder.DeathRecipient{
		List<WeakReference<JsonChannel>> sesssions = new ArrayList<WeakReference<JsonChannel>>();
		List<JsonChannel> sesssionsWaiting = new ArrayList<JsonChannel>();
		String serviceName;
		boolean connected = false;
		IJsonChannelService mService;
		private Object waitConnectObj = new Object();

		static Service getService(Context context, String name, boolean create) {
			synchronized (services) {
				Service s = services.get(name);
				if (s == null && create) {
					try {
						if ((s = new Service(context, name)) != null)
							services.put(name, s);
					} catch (IOException e) {
						IPanelLog.d(TAG, e.getMessage());
					}
				}
				return s;
			}
		}

		Service(Context context, String name) throws IOException {
			serviceName = name;
			Intent i = new Intent(serviceName);
			if (!context.bindService(i, this, Context.BIND_AUTO_CREATE))
				throw new IOException("bindService failed! serviceName=" + serviceName);
		}

		@Override
		public void onServiceDisconnected(ComponentName name) {
			synchronized (services) {
				services.remove(serviceName);
			}
			IPanelLog.d(TAG, serviceName + ".onServiceDisconnected...");
			synchronized (sesssions) {
				mService = null;
				connected = false;
				for (WeakReference<JsonChannel> wj : sesssions) {
					JsonChannel ch;
					if ((ch = wj.get()) != null)
						ch.onConnection(false);
				}
				sesssions.clear();
				for (JsonChannel ch : sesssionsWaiting)
					ch.onConnection(false);
				sesssionsWaiting.clear();
			}
		}

		@Override
		public void onServiceConnected(ComponentName name, IBinder service) {
			synchronized (sesssions) {
				try {
					IPanelLog.d(TAG, serviceName + ".onServiceConnected...");
					mService = IJsonChannelService.Stub.asInterface(service);
					service.linkToDeath(this, 0);
					IPanelLog.d(TAG, serviceName + ".onServiceConnected ok!");
					connected = true;
					for (JsonChannel ch : sesssionsWaiting) {
						try {
							if ((ch.mSession = mService.createSession(ch.sessionName, ch.mCallback,
									ch.args)) != null) {
								IPanelLog.d(TAG, "onServiceConnected ch.sessionName=" + ch.sessionName);
								sesssions.add(new WeakReference<JsonChannel>(ch));
								ch.onConnection(true);
							}
						} catch (Exception e) {
							IPanelLog.e(TAG, serviceName + ".onCreateSession(" + ch.sessionName
									+ ")2 error:" + e);
							e.printStackTrace();
						}
					}
					sesssionsWaiting.clear();
				} catch (Exception e) {
					IPanelLog.e(TAG, serviceName + ".onServiceConnected proc error:" + e);
				}
				synchronized (waitConnectObj) {
					waitConnectObj.notifyAll();
				}
			}
		}
		
		@Override
		protected void finalize() throws Throwable {
			IPanelLog.d(TAG,"call Json channel finalize....");
			super.finalize();
		}

		boolean scheduleChannel(JsonChannel ch) {
			synchronized (sesssions) {
				if (connected) {
					try {
						if ((ch.mSession = mService.createSession(ch.sessionName, ch.mCallback,
								ch.args)) != null) {
							sesssions.add(new WeakReference<JsonChannel>(ch));
							ch.onConnection(true);
							return true;
						}
					} catch (RemoteException e) {
						IPanelLog.e(TAG, serviceName + ".onCreateSession(" + ch.sessionName + ") error:"
								+ e);
						e.printStackTrace();
					}
				} else {
					sesssionsWaiting.add(ch);
					return true;
				}
			}
			return false;
		}

		void unregisterChannel(JsonChannel ch, boolean cn) {
			synchronized (sesssions) {
				if (cn) {
					sesssionsWaiting.remove(ch);
				} else {
					for (WeakReference<JsonChannel> wj : sesssions) {
						if (wj.get() == ch) {
							wj.clear();
							sesssions.remove(wj);
						}
					}
				}
			}
		}

		void waitService() {
			synchronized (waitConnectObj) {
				try {
					waitConnectObj.wait();
				} catch (InterruptedException e) {
				}
			}
		}

		@Override
		public void binderDied() {
			IPanelLog.d(TAG, serviceName + ":binderDied");
			onServiceDisconnected(null);
		}
	}

	private Context ctx;
	private Service service;
	private String sessionName, serviceName;
	private Object mutex = new Object();
	private Bundle args;
	private IJsonChannelSession mSession;
	private boolean shotted = false, connected = false;
	private volatile int version = 0;
	private HashMap<String, WeakReference<Object>> reflections = new HashMap<String, WeakReference<Object>>();

	private IJsonChannelCallback mCallback = new IJsonChannelCallback.Stub() {
		private volatile int callbackVersion = 0;

		@Override
		public void onCallback(int cmd, String json, JsonParcelable p, Bundle b, boolean nv)
				throws RemoteException {
			try {
				IPanelLog.d(TAG, "onCallback cmd = " + cmd + ";json = " + json + ";nv = " + nv);
				if (callbackVersion == version || nv) {
					IPanelLog.d(TAG, "onCallback 22 cmd = " + cmd + ";json = " + json + ";nv = " + nv);
					JsonChannel.this.onCallback(cmd, json, p, b);
				} else {
					JsonChannel.this.onCallbackOutdated(cmd, json, p, b);
					p = null;
				}
				IPanelLog.d(TAG, "onCallback end cmd =" + cmd);
			} catch (Exception e) {
				if (p != null)
					p.clean();
				IPanelLog.d(TAG, JsonChannel.this + ".onCallback error:" + e);
			}
		}

		@Override
		public void onVersion(int v) throws RemoteException {
			callbackVersion = v;
		}

		@Override
		public void onReflectionCallback(JsonReflectionInvokParcelable p, String json)
				throws RemoteException {
			try {
				Object o = reflections.get(p.objectId).get();
				ReflectionInvoker i = ReflectionInvoker.getInvoker(o.getClass().getCanonicalName(),
						p.methodname, p.argsClassName);
				i.invoke(o, p.argsVaule.toArray());
			} catch (Throwable e) {
				e.printStackTrace();
			}
		}
	};

	public JsonChannel(Context ctx, String serviceName) {
		this(ctx.getApplicationContext(), serviceName, null);
	}

	public JsonChannel(Context ctx, String serviceName, String sessionName) {
		this.ctx = ctx;
		this.serviceName = serviceName;
		this.sessionName = sessionName;
		IPanelLog.d(TAG, "JsonChannel serviceName:" + serviceName + ",sessionName=" + sessionName);
	}

	void onConnection(boolean b) {
		try {
			if (!connected && b) {
				connected = b;
				onChannelConnected();
			} else if (connected && !b) {
				connected = b;
				onChannelDisconnectted();
			}
		} catch (Exception e) {
			IPanelLog.e(TAG, "onConnection" + (b ? "Connected" : "Disconnectted" + ":error" + e));
		}
	}

	public void setArguments(Bundle b) {
		args = b;
	}

	public Bundle getArguments() {
		return args;
	}

	public Context getContext() {
		return ctx;
	}

	public boolean connect() {
		synchronized (mutex) {
			if (!shotted) {
				Service s = Service.getService(ctx, serviceName, true);
				if (s != null) {
					shotted = true;
					if (shotted = s.scheduleChannel(this))
						service = s;
				}
			}
		}
		return shotted;
	}

	public boolean isShotted() {
		return shotted;
	}

	public void disconnect() {
		IPanelLog.d(TAG, "disconnect");
		synchronized (mutex) {
			if (shotted) {
				try {
					if (connected)
						mSession.close();
				} catch (RemoteException e) {
					IPanelLog.e(TAG, "session close error:" + e);
				}
				mSession = null;
				if (service != null) {
					service.unregisterChannel(this, connected);
					service = null;
				}
				connected = false;
			}
		}
	}

	public boolean isConnected() {
		return (mSession != null);
	}

	/**
	 * 本函数不能再应用主线程中调用，否则将产生死锁
	 */
	public void waitConnection() {
		Service s = null;
		synchronized (mutex) {
			if ((s = service) == null)
				return;
		}
		if (s != null)
			s.waitService();
	}

	public void updateCallbackVersion() {
		synchronized (mutex) {
			if (mSession != null) {
				try {
					mSession.updateCallbackVersion(++version);
				} catch (RemoteException e) {
					IPanelLog.e(TAG, "updateCallbackVersion error:" + e);
				}
			}
		}
	}

	public String transmit(int code) {
		return transmit(code, null, null, null, false);
	}

	public String transmit(int code, String json) {
		return transmit(code, json, null, null, false);
	}

	public String transmit(int code, String json, JsonParcelable p) {
		return transmit(code, json, p, null, false);
	}

	public String transmit(int code, String json, JsonParcelable p, Bundle b) {
		return transmit(code, json, p, b, false);
	}

	/**
	 * 存在缺陷
	 * <p>
	 * 因中间过程无法决定最终消息处理的时效性,不建议使用
	 */
	@Deprecated
	public String transmit(int code, String json, JsonParcelable p, Bundle b, boolean updateVersion) {
		synchronized (mutex) {
			if (mSession != null) {
				try {
					IPanelLog.d(TAG, "transmit client ************");
					IPanelLog.d(TAG, "transmit code=" + code + ",json=" + json);
					String s = mSession.transmit(code, json, p, b, ++version);
					IPanelLog.d(TAG, "transmit end code=" + code);
					return s;
				} catch (RemoteException e) {
					IPanelLog.e(TAG, "transmit error:" + e.getMessage());
					e.printStackTrace();
				}
			}
			return null;
		}
	}

	public void transmitAsync(int code) {
		transmitAsync(code, null, null, null);
	}

	public void transmitAsync(int code, String json) {
		transmitAsync(code, json, null, null);
	}

	public void transmitAsync(int code, String json, JsonParcelable p, Bundle b) {
		synchronized (mutex) {
			if (mSession != null) {
				try {
					IPanelLog.d(TAG, "transmitAsync code=" + code + ",json=" + json);
					mSession.atransmit(code, json, p, b);
					IPanelLog.d(TAG, "transmitAsync end code=" + code);
				} catch (RemoteException e) {
					IPanelLog.e(TAG, "atransmit error:" + e);
				}
			}
		}
	}

	public void registerReflectionCallback(String id, Object obj) {
		synchronized (reflections) {
			reflections.put(id, new WeakReference<Object>(obj));
		}
	}

	public void onChannelConnected() {
	}

	public void onChannelDisconnectted() {
	}

	/**
	 * 参看{@link #transmit(int, String, JsonParcelable, boolean)}
	 */
	@Deprecated
	public void onCallbackOutdated(int code, String json, JsonParcelable p, Bundle b) {
		if (p != null)
			p.clean();
	}

	public abstract void onCallback(int code, String json, JsonParcelable p, Bundle b)
			throws JSONException;

}
