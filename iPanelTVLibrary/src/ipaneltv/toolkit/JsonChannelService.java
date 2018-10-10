package ipaneltv.toolkit;

import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.Map;

import org.json.JSONException;

import android.app.Service;
import android.content.Intent;
import android.os.Binder;
import android.os.Bundle;
import android.os.IBinder;
import android.os.Parcelable;
import android.os.RemoteException;

public abstract class JsonChannelService extends Service {

	public static final String SERVICE_INTERFACE = "ipaneltv.toolkit.JsonChannelService";
	static final String TAG = "JsonChannelService";

	private final JsonChannelServiceBinder mBinder = new JsonChannelServiceBinder(this);

	@Override
	public final IBinder onBind(Intent intent) {
		return mBinder;

	}

	public Session createSession() {
		return null;
	}

	public Session createSession(String name) {
		return createSession();
	}

	public static abstract class Session {
		private InternalIJsonChannelSession mInternalSession;

		void setSession(InternalIJsonChannelSession session) {
			mInternalSession = session;
		}

		public int getCallingUID() {
			return mInternalSession.callingUID;
		}

		public Bundle getBundle() {
			return mInternalSession.getBundle();
		}

		public void notifyJson(int cmd) {
			notifyJson(cmd, null, null, null, true);
		}

		public void notifyJson(int cmd, String json) {
			notifyJson(cmd, json, null, null, true);
		}

		public void notifyJson(int cmd, String json, JsonParcelable p) {
			notifyJson(cmd, json, p, null, true);
		}

		public void notifyJson(int cmd, String json, Bundle b) {
			notifyJson(cmd, json, null, b, true);
		}

		@Deprecated
		public void notifyJson(int cmd, String json, JsonParcelable p, Bundle b, boolean versionless) {
			try {
				IPanelLog.d(TAG, "notifyJson service cmd = " + cmd + ",json=" + json);
				mInternalSession.mCallback.onCallback(cmd, json, p, b, versionless);
				IPanelLog.d(TAG, "notifyJson end cmd =" + cmd);
			} catch (RemoteException e) {
				IPanelLog.d(TAG, "notifyJson error:" + e);
			}
		}

		@Override
		protected void finalize() throws Throwable {
			IPanelLog.d(TAG, "call Json channel Service finalize....");
			super.finalize();
			IPanelLog.d(TAG, "call Json channel Service finalize.... out");
		}

		/**
		 * 无法调用初级数据类型参数的方法(int,long等)
		 */
		public void callReflection(String json, String objId, String method, Object... args) {
			try {
				mInternalSession.callReflection(json, objId, method, args);
			} catch (Exception e) {
				IPanelLog.d(TAG, "callReflection error:" + e);
			}
		}

		/**
		 * 支持初级数据类型
		 */
		@SuppressWarnings("rawtypes")
		public void callReflection(String json, String objId, String method,
				Map.Entry<Class, Object>... args) {
			try {
				mInternalSession.callReflection(json, objId, method, args);
			} catch (Exception e) {
				IPanelLog.d(TAG, "callReflection error:" + e);
			}
		}

		public abstract String onTransmit(int code, String json, JsonParcelable p, Bundle b)
				throws JSONException;

		public abstract void onCreate();

		public abstract void onClose();

	}

	private static class JsonChannelServiceBinder extends IJsonChannelService.Stub {
		private final WeakReference<JsonChannelService> mInternalServiceRef;

		public JsonChannelServiceBinder(JsonChannelService service) {
			mInternalServiceRef = new WeakReference<JsonChannelService>(service);
		}

		@Override
		public IJsonChannelSession createSession(String name, IJsonChannelCallback cb, Bundle bundle)
				throws RemoteException {
			IPanelLog.d(TAG, "createSession in");
			final JsonChannelService service = mInternalServiceRef.get();

			final Session session = service.createSession(name);
			final InternalIJsonChannelSession internalSession = new InternalIJsonChannelSession(cb,
					bundle, session);
			IPanelLog.d(TAG, "createSession 11");
			session.onCreate();
			IPanelLog.d(TAG, "createSession end");
			return internalSession;
		}

	}

	private static class InternalIJsonChannelSession extends IJsonChannelSession.Stub implements
			IBinder.DeathRecipient {
		private final IJsonChannelCallback mCallback;
		private final Session mSession;
		private final Bundle mBundle;
		final int callingUID;

		public InternalIJsonChannelSession(IJsonChannelCallback cb, Bundle bundle, Session session)
				throws RemoteException {
			mCallback = cb;
			mSession = session;
			mBundle = bundle;
			session.setSession(this);
			callingUID = Binder.getCallingUid();
			cb.asBinder().linkToDeath(this, 0);
		}

		public Bundle getBundle() {
			return mBundle;
		}

		@Override
		public void close() {
			try {
				IPanelLog.d(TAG, "close in");
				mSession.onClose();
				IPanelLog.d(TAG, "close out");
			} catch (Exception e) {
				IPanelLog.d(TAG, "onClose error:" + e);
			}
		}

		@Override
		public void binderDied() {
			IPanelLog.d(TAG, "binderDied in");
			close();
			IPanelLog.d(TAG, "binderDied out");
		}

		@Override
		public String transmit(int code, String json, JsonParcelable p, Bundle b, int nv)
				throws RemoteException {
			IPanelLog.d(TAG, "transmit in code = " + code + ",json=" + json);
			if (nv != -1)
				updateCallbackVersion(nv);
			try {
				IPanelLog.d(TAG, "transmit code = " + code + ",json=" + json);
				String s = mSession.onTransmit(code, json, p, b);
				IPanelLog.d(TAG, "transmit out end code = " + code);
				return s;
			} catch (Exception e) {
				if (p != null)
					p.clean();
				IPanelLog.d(TAG, "onTransmit(" + code + ") error:" + e);
				throw new RemoteException();
			}
		}

		@Override
		public void atransmit(int code, String json, JsonParcelable p, Bundle b)
				throws RemoteException {
			try {
				IPanelLog.d(TAG, "atransmit code = " + code + ",json=" + json);
				mSession.onTransmit(code, json, p, b);
				IPanelLog.d(TAG, "atransmit end code = " + code);
			} catch (Exception e) {
				if (p != null)
					p.clean();
				IPanelLog.d(TAG, "onTransmit(" + code + ") error:" + e);
				throw new RemoteException();
			}
		}

		@Override
		public void updateCallbackVersion(int v) throws RemoteException {
			try {
				IPanelLog.d(TAG, "updateCallbackVersion v=" + v);
				if (v != -1)
					mSession.mInternalSession.mCallback.onVersion(v);
				IPanelLog.d(TAG, "updateCallbackVersion end v=" + v);
			} catch (Exception e) {
			}
		}

		@SuppressWarnings("rawtypes")
		public void callReflection(String json, String objId, String method,
				Map.Entry<Class, Object>... args) throws RemoteException {
			JsonReflectionInvokParcelable p = new JsonReflectionInvokParcelable();
			p.objectId = objId;
			p.methodname = method;
			p.argsClassName = new ArrayList<String>();
			p.argsVaule = new ArrayList<Object>();
			IPanelLog.d(TAG, "callReflection111 json = " + json);
			for (int i = 0; i < args.length; i++) {
				Class clazz = args[i].getKey();
				Object o = args[i].getValue();
				if (clazz.isPrimitive() || o instanceof Parcelable || o instanceof String
						|| o instanceof Integer || o instanceof Short || o instanceof Float
						|| o instanceof Double || o instanceof Character || o instanceof Boolean) {
					p.argsClassName.add(clazz.getCanonicalName());
					p.argsVaule.add(o);
				} else {
					throw new RuntimeException("args only support primitives and String");
				}
			}
			mCallback.onReflectionCallback(p, json);
			IPanelLog.d(TAG, "callReflection111 end json = " + json);
		}

		public void callReflection(String json, String objId, String method, Object... args)
				throws RemoteException {
			JsonReflectionInvokParcelable p = new JsonReflectionInvokParcelable();
			p.objectId = objId;
			p.methodname = method;
			p.argsClassName = new ArrayList<String>();
			p.argsVaule = new ArrayList<Object>();
			IPanelLog.d(TAG, "callReflection222 json=" + json);
			for (int i = 0; i < args.length; i++) {
				Object o = args[i];
				@SuppressWarnings("rawtypes")
				Class clazz = o.getClass();
				if (clazz.isPrimitive() || o instanceof Parcelable || o instanceof String
						|| o instanceof Integer || o instanceof Short || o instanceof Float
						|| o instanceof Double || o instanceof Character) {
					p.argsClassName.add(clazz.getCanonicalName());
					p.argsVaule.add(o);
				} else {
					throw new RuntimeException("args only support primitives and String");
				}
			}
			mCallback.onReflectionCallback(p, json);
			IPanelLog.d(TAG, "callReflection222 end json=" + json);
		}
	}
}
