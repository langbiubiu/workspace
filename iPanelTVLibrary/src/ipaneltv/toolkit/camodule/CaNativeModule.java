package ipaneltv.toolkit.camodule;

import ipaneltv.toolkit.IPanelLog;
import ipaneltv.toolkit.Natives;

import java.lang.ref.WeakReference;
import java.util.UUID;

import android.os.Handler;
import android.os.HandlerThread;
import android.os.Message;

public class CaNativeModule extends Natives {
	static final String TAG = CaNativeModule.class.getSimpleName();
	private String libName;
	private boolean isLoadded = false, isOpened = false, isRunning = false, isClosed = false;
	private UUID id;
	private int peer = 0;
	private final Object sessionMutex = new Object();
	private CaModuleSession session = null;
	private int attachedNumber = 0;
	private boolean weakSession;
	private HandlerThread handlerThread;
	private Handler procHandler;
	private CaModuleApplication app;
	private int casysids[];
	int moduleSn = -1, moduleId = -1;

	/**
	 * ¹¹½¨ÊµÀý
	 * 
	 * @param app
	 */
	public CaNativeModule(CaModuleApplication app, int casysids[]) {
		this.app = app;
		handlerThread = new HandlerThread(TAG);
		id = UUID.fromString(app.getNetworkUUID());
		this.casysids = casysids.clone();
		IPanelLog.d(TAG, "CaNativeModule id = " + id);
	}

	public int[] getCaSystemIds() {
		return casysids.clone();
	}

	public int getModuleSN() {
		return moduleSn;
	}

	public final String getUUID() {
		return id.toString();
	}

	public CaModuleApplication getApp() {
		return app;
	}

	public final String getLibraryName() {
		return libName;
	}

	public final boolean load(String libName) {
		synchronized (id) {
			if (isClosed)
				throw new IllegalStateException("is released!");
			if (isLoadded)
				throw new IllegalStateException("is loadded!");
			if (libName.charAt(0) != '/') {
//				String files = app.getFilesDir().getAbsoluteFile().toString();
//				int i = files.lastIndexOf('/');
				libName = "/system/lib/" + libName;
			}
			try {
				if (procHandler == null) {
					handlerThread.start();
					procHandler = new Handler(handlerThread.getLooper(), handlerMessage);
				}
				this.libName = libName;
				return isLoadded = (nload(libName, id.getMostSignificantBits(),
						id.getLeastSignificantBits()) == 0);
			} catch (Throwable e) {
				IPanelLog.e(TAG, "load(" + libName + ")failed");
				e.printStackTrace();
				return false;
			}
		}
	}

	public final boolean isLoadded() {
		return isLoadded;
	}

	public final boolean open(String args) {
		synchronized (id) {
			if (isClosed)
				throw new IllegalStateException("is released!");
			if (!isLoadded)
				throw new IllegalStateException("is not loaded!");
			if (isOpened)
				throw new IllegalStateException("is initialized!");
			int ret = nopen(new WeakReference<CaNativeModule>(this), args);
			return isOpened = (ret == 0);
		}
	}

	public final int getCaModuleId() {
		return moduleId;
	}

	public final boolean isOpened() {
		return isOpened;
	}

	public final boolean start() {
		synchronized (id) {
			if (isClosed)
				throw new IllegalStateException("is released!");
			if (!isLoadded)
				throw new IllegalStateException("is not loaded!");
			if (!isOpened)
				throw new IllegalStateException("is initialized!");
			if (isRunning)
				throw new IllegalStateException("is already running!");
			moduleId = nstart();
			return isRunning = (moduleId >= 0);
		}
	}

	public final boolean isRunning() {
		return isRunning;
	}

	public final void stop() {
		synchronized (id) {
			if (isClosed)
				throw new IllegalStateException("is released!");
			if (isRunning && isOpened && isLoadded) {
				nstop();
				isRunning = false;
			}
		}
	}

	public final void close() {
		synchronized (id) {
			if (!isClosed) {
				if (isRunning)
					nstop();
				if (isOpened)
					nclose();
			}
		}
	}

	public final boolean isClosed() {
		return isClosed;
	}

	public final void localizeMessage(int code, String str) {
		synchronized (id) {
			if (isClosed)
				throw new IllegalStateException("is released!");
			if (!isLoadded)
				throw new IllegalStateException("is not loaded!");
			if (!isOpened)
				throw new IllegalStateException("is initialized!");
			if (code < 0 || str == null)
				throw new IllegalArgumentException();
			nlocalize(code, str);
		}
	}

	public String getProperty(String name) {
		if (name == null)
			return null;
		return ngetprop(name);
	}

	public int getIntProperty(String name, int defaultValue) {
		try {
			return Integer.parseInt(ngetprop(name));
		} catch (Exception e) {
		}
		return defaultValue;
	}

	public long getLongProperty(String name, long defaultValue) {
		try {
			return Long.parseLong(ngetprop(name));
		} catch (Exception e) {
		}
		return defaultValue;
	}

	public float getFloatProperty(String name, float defaultValue) {
		try {
			return Float.parseFloat(ngetprop(name));
		} catch (Exception e) {
		}
		return defaultValue;
	}

	public double getDoubleProperty(String name, double defaultValue) {
		try {
			return Double.parseDouble(ngetprop(name));
		} catch (Exception e) {
		}
		return defaultValue;
	}

	public boolean setProperty(String name, String value) {
		if (name == null)
			return false;
		return nsetprop(name, value) == 0;
	}

	public boolean setProperty(String name, int value) {
		if (name == null)
			return false;
		return nsetprop(name, value + "") == 0;
	}

	public boolean setProperty(String name, float value) {
		if (name == null)
			return false;
		return nsetprop(name, value + "") == 0;
	}

	public boolean setProperty(String name, double value) {
		if (name == null)
			return false;
		return nsetprop(name, value + "") == 0;
	}

	public boolean setProperty(String name, long value) {
		if (name == null)
			return false;
		return nsetprop(name, value + "") == 0;
	}

	public final String manageTransmit(String json) {
		synchronized (id) {
			if (isClosed)
				throw new IllegalStateException("is released!");
			if (!isLoadded)
				throw new IllegalStateException("is not loaded!");
			if (!isOpened)
				throw new IllegalStateException("is initialized!");
			return nmtransmit(json);
		}
	}

	protected String onJsonManage(String json) {
		return null;
	}

	protected void onJsonMessage(String json) {
		int number = -1;
		synchronized (json) {
			if (session != null)
				number = sessionCounter;
		}
		if (number >= 0)
			procHandler.obtainMessage(MSG_JSON, number, 0, json).sendToTarget();
		else
			onJsonMessageIgnored(json);
	}

	protected void onJsonMessageIgnored(String json) {
	}

	protected void checkEntitlements() {
	}

	protected void onPaused() {
		procHandler.sendEmptyMessage(MSG_PAUSED);
	}

	protected void onResumed() {
		procHandler.sendEmptyMessage(MSG_RESUMED);
	}

	protected void onClosed() {
	}

	public CaModuleSession createSession(String name, int pri, CaModuleSession.Callback callback) {
		return new CaModuleSession(this, ++sessionCounter, name, pri, callback);
	}

	public CaModuleSession createSession(String name, CaModuleSession.Callback callback) {
		return createSession(name, 5, callback);
	}

	protected static final int CALLBACK_CLOSED = 0;
	protected static final int CALLBACK_PAUSED = 1;
	protected static final int CALLBACK_RESUMED = 2;
	protected static final int CALLBACK_JSON_MANAGE = 3;
	protected static final int CALLBACK_JSON_MESSAGE = 4;

	@SuppressWarnings({ "unchecked", "rawtypes" })
	private final static String native_callback(Object o, int msg, int p1, int p2, String json) {
		try {
			WeakReference<CaNativeModule> wo = (WeakReference) o;
			if (wo != null) {
				CaNativeModule m = wo.get();
				if (m != null) {
					switch (msg) {
					case CALLBACK_CLOSED:
						m.procHandler.sendEmptyMessage(MSG_CLOSED);
						break;
					case CALLBACK_PAUSED:
						m.onPaused();
						return null;
					case CALLBACK_RESUMED:
						m.onResumed();
						return null;
					case CALLBACK_JSON_MANAGE:
						return m.onJsonManage(json);
					case CALLBACK_JSON_MESSAGE:
						m.onJsonMessage(json);
						return null;
					default:
						break;
					}
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		return null;
	}

	native int nload(String libname, long most, long least);

	native int nopen(WeakReference<CaNativeModule> o, String args);

	native int nclose();

	native int nstart();

	native String ngetprop(String name);

	native int nsetprop(String name, String value);

	native int nstop();

	native String nmtransmit(String json);

	native String nstransmit(String json);

	native int nlocalize(int code, String s);

	private static int sessionCounter = 1;

	private final boolean doAttachSession(CaModuleSession s) {
		if (session == s)
			return true;
		if (session == null) {
			session = s;
			return true;
		}
		if (weakSession) {
			weakSession = false;
			session = s;
			return true;
		}
		if (session.pri < s.pri) {
			WeakReference<CaModuleSession> o = new WeakReference<CaModuleSession>(session);
			session = s;
			procHandler.obtainMessage(MSG_RACED, o).sendToTarget();
			return true;
		}
		return false;
	}

	final boolean attachSession(CaModuleSession s) {
		synchronized (sessionMutex) {
			s.attached = false;
			if (doAttachSession(s)) {
				s.attached = true;
				attachedNumber++;
			}
			return s.attached;
		}
	}

	final void loosenSession(CaModuleSession s) {
		synchronized (sessionMutex) {
			if (session != null && session == s) {
				weakSession = true;
			}
		}
	}

	final void checkEntitlements(CaModuleSession s) {
		synchronized (sessionMutex) {
			if (session != null && session == s) {
				checkEntitlements();
			}
		}
	}

	final void detachSession(CaModuleSession s) {
		synchronized (sessionMutex) {
			if (s.attached) {
				s.attached = false;
				if (session == s) {
					session = null;
					weakSession = false;
					attachedNumber++;
				}
			}
		}
	}

	final String sessionTransmit(CaModuleSession s, String json) {
		synchronized (sessionMutex) {
			if (session == s && json != null) {
				if (!isClosed && isLoadded && isOpened)
					return nstransmit(json);
			}
			return null;
		}
	}

	private Handler.Callback handlerMessage = new Handler.Callback() {
		@Override
		public boolean handleMessage(Message msg) {
			try {
				switch (msg.what) {
				case MSG_CLOSED:
					synchronized (sessionMutex) {
						if (session != null) {
							session.callback.onModuleClosed();
							session = null;
						}
					}
					handlerThread.getLooper().quit();
					onClosed();
					break;
				case MSG_PAUSED:
					synchronized (sessionMutex) {
						if (session != null)
							session.callback.onModulePaused();
					}
					break;
				case MSG_RESUMED:
					synchronized (sessionMutex) {
						if (session != null)
							session.callback.onModuleResumed();
					}
					break;
				case MSG_JSON:
					synchronized (sessionMutex) {
						if (session != null && msg.arg1 == sessionCounter)
							session.callback.onJsonMessage((String) msg.obj);
					}
					break;
				case MSG_RACED:
					@SuppressWarnings({ "rawtypes", "unchecked" })
					WeakReference<CaModuleSession> o = (WeakReference) msg.obj;
					if (o.get() != null)
						o.get().callback.onSessionLost();
					o.clear();
					break;
				default:
					break;
				}
			} catch (Exception e) {
				e.printStackTrace();
			}
			return false;
		}
	};
	private static final int MSG_CLOSED = 0;
	private static final int MSG_PAUSED = 1;
	private static final int MSG_RESUMED = 2;
	private static final int MSG_JSON = 3;
	private static final int MSG_RACED = 4;

}
