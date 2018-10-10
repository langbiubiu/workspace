package android.net.telecast;

import java.lang.ref.WeakReference;
import java.util.HashMap;
import java.util.List;

import android.content.Context;
import android.net.Uri;
import android.os.Binder;
import android.os.IBinder;
import android.os.RemoteException;
import android.util.Log;

/**
 * 网络管理
 */
@SuppressWarnings("deprecation")
public class NetworkManager {
	static final String TAG = "[java]NetworkManager";

	/** @hide */
	public static final String SERVICE_NAME = "android.net.telecast.NetworkManager";
	private static HashMap<String, WeakReference<NetworkManager>> sCache = new HashMap<String, WeakReference<NetworkManager>>();

	private INetworkServiceManager mService = null;
	private Object mMuex = new Object();
	private String pkgname;
	static {
		TransportManager.ensure();
	}
	/** 属性名- 运营商名称 */
	public static final String PROPERTY_OPERATOR_NAME = "android.net.telecast.NetworkManager.operator_name";
	/** 属性名- 传输协议 比如 dvb-c,dvb-t,dvb-s等 */
	public static final String PROPERTY_NETWORK_PROTOCOL = "android.net.telecast.NetworkManager.transport_protocol";
	/** 属性名- 数据库URI信息 */
	public static final String PROPERTY_DATABASE_AUTHORITIES = "android.net.telecast.NetworkManager.database_authorities";
	/** 属性名- 电视组件 */
	public static final String PROPERTY_TEEVEE_WIDGET = "android.net.telecast.NetworkManager.teevee_widget";
	/** 属性名- 电视组件-小号 */
	public static final String PROPERTY_TEEVEE_WIDGET_SMALL = "android.net.telecast.NetworkManager.teevee_widget_small";
	/** 属性名- 富电视组件(可选) */
	public static final String PROPERTY_RICH_TEEVEE_WIDGET = "android.net.telecast.NetworkManager.rich_teevee_widget";
	/** 属性名- 段数据存储 */
	public static final String PROPERTY_SECTION_STORAGE = "android.net.telecast.NetworkManager.section_storage";
	/** 属性名- DSMCC协议服务 */
	public static final String PROPERTY_DSMCC_SERVICE = "android.net.telecast.NetworkManager.dsmcc_service";

	/**
	 * 得到实例
	 * 
	 * @param ctx
	 *            上下文对象
	 * @return 对象
	 */
	public static NetworkManager getInstance(Context ctx) {
		ctx = ctx.getApplicationContext();
		String pkgname = ctx.getPackageName();
		WeakReference<NetworkManager> wo = null;
		NetworkManager nm = null;
		synchronized (sCache) {
			if ((wo = sCache.get(pkgname)) != null) {
				if ((nm = wo.get()) != null) {
					return nm;
				}
			}
			nm = new NetworkManager(pkgname);
			wo = new WeakReference<NetworkManager>(nm);
			sCache.put(pkgname, wo);
		}
		return nm;
	}

	/** @hide */
	NetworkManager(String pkgname) {
		this.pkgname = pkgname;
	}

	IBinder.DeathRecipient dcb = new IBinder.DeathRecipient() {
		@Override
		public void binderDied() {
			synchronized (mMuex) {
				mService = null;
				Log.w(TAG, "service died : " + SERVICE_NAME);
			}
		}
	};

	/**
	 * 释放资源
	 * <p>
	 * 对象不再可用
	 */
	public void release() {
		synchronized (sCache) {
			if (pkgname != null) {
				sCache.remove(pkgname);
				pkgname = null;
			}
		}
	}

	private boolean ensureService() {
		synchronized (mMuex) {
			int tryCount = 0;
			if (mService != null)
				return true;
			IBinder ret = null;
			while ((ret = TransportManager.getServiceByServiceManager(SERVICE_NAME)) == null) {
				if (tryCount++ > 50)// 5 second
					return false;
				try {
					Thread.sleep(100);
				} catch (InterruptedException e) {
					break;
				}
			}
			mService = INetworkServiceManager.Stub.asInterface(ret);
			try {
				ret.linkToDeath(dcb, 0);
			} catch (RemoteException e) {
				Log.e(TAG, "error:" + e);
			}
			return true;
		}
	}

	/**
	 * 得到系统中有效网络的UUID
	 * 
	 * @return UUID的列表
	 */
	public List<String> getNetworkUUIDs() {
		try {
			if (!ensureService())
				return null;
			return mService.getNetworkUUIDs();
		} catch (Exception e) {
			Log.e(TAG, "getNetworkUUIDs err:" + e);
			return null;
		}
	}

	/**
	 * 得到内容提供URI
	 * <p>
	 * 一般情况下，需要声明权限:android.net.telecast.permission.READ_NETWORK_DATABASE
	 * 
	 * @param netUUID
	 *            网络ID。 参考<code>java.util.UUID.toString();</code>
	 * @return 对象
	 */
	public Uri getNetworkDatabaseUri(String netUUID) {
		try {
			if (!ensureService())
				return null;
			return mService.getNetworkDatabaseUri(netUUID);
		} catch (Exception e) {
			Log.e(TAG, "getNetworkDatabaseUri err:" + e);
			return null;
		}
	}

	/**
	 * 得到网络属性
	 * 
	 * @param netUUID
	 *            网络ID。 参考<code>java.util.UUID.toString();</code>
	 * @return 值
	 */
	public String getNetworkProperty(String netUUID, String name) {
		try {
			if (!ensureService())
				return null;
			return mService.getNetworkProperty(netUUID, name);
		} catch (Exception e) {
			Log.e(TAG, "getNetworkProperty err:" + e);
			return null;
		}
	}

	/**
	 * 得到首选网络的UUID
	 * 
	 * @return id字符串。 参考<code>java.util.UUID.toString();</code>
	 */
	public String getPreferredNetworkUUID() {
		try {
			if (!ensureService())
				return null;
			return mService.getPreferredNetworkUUID();
		} catch (Exception e) {
			Log.e(TAG, "getPreferredNetworkUUID err:" + e);
			return null;
		}
	}

	/**
	 * 设置偏好的网络
	 * <p>
	 * 偏好的网络将优先使用资源,当存在多个网络的情况下，应用程序如果需要使用网 络管理应用提供的功能，那么在此时前一般应该进行设定.
	 * 以确保网络管理应用能有资源完成对应用的操作
	 * 
	 * @param uuid
	 *            网络ID
	 * @return 成功则返回true,否则返回false
	 */
	public boolean setPreferredNetworkByUUID(String uuid) {
		try {
			if (!ensureService())
				return false;
			return mService.setPreferredNetworkByUUID(uuid);
		} catch (Exception e) {
			Log.e(TAG, "setPreferredNetworkUUID err:" + e);
			return false;
		}
	}

	/**
	 * 注册当前进程为网络应用进程
	 * <p>
	 * 如果操作成功，比较于未注册进程，将可优先使用资源<br>
	 * 注意:setPreferredNetworkByUUID所对应用的应用所述的进程将具有更高的优先级
	 * <p>
	 * 本函数调用需要声明权限:android.net.telecast.permission.ACCESS_TEEVEE_RESOURCE
	 * 
	 * @return 成功则返回true,否则返回false
	 */
	public boolean registerNetworkAppProcess() {
		try {
			if (!ensureService())
				return false;
			Binder b = new Binder();
			return mService.registerNetworkAppProcess(b);
		} catch (Exception e) {
			Log.e(TAG, "registerNetworkAppProcess err:" + e);
			return false;
		}
	}

	/**
	 * 注册网络CA进程
	 * <p>
	 * 对于未注册的，将无法参与系统的CA调度服务体系的运作
	 * <p>
	 * 需要声明权限:android.net.telecast.permission.ACCESS_CA_RESOURCE
	 * 
	 * @return 成功则返回true,否则返回false
	 */
	public boolean registerNetworkCAProcess() {
		try {
			if (!ensureService())
				return false;
			Binder b = new Binder();
			return mService.registerNetworkCAProcess(b);
		} catch (Exception e) {
			Log.e(TAG, "registerNetworkCAProcess err:" + e);
			return false;
		}
	}

	/**
	 * 得到网络提供的基础电视播放组件
	 * <p>
	 * 此组件仅显示视频和其上的Tips信息(CA相关和错误信息等)
	 * 
	 * 
	 * @param uuid
	 *            网络UUID
	 * @param widgetId
	 *            组件ID
	 * @return 成功返回true,否则返回false
	 */
	public boolean bindNetworkTeeveeWidgetId(String uuid, int widgetId) {
		return bindNetworkTeeveeWidgetId(uuid, widgetId, PROPERTY_TEEVEE_WIDGET);
	}

	/**
	 * 得到网络提供的基础电视播放组件
	 * <p>
	 * 此组件仅显示视频和其上的Tips信息(CA相关和错误信息等)
	 * 
	 * @param uuid
	 *            网络UUID
	 * @param widgetId
	 *            组件ID
	 * @param name
	 *            名称
	 * @return 成功返回true,否则返回false
	 */
	public boolean bindNetworkTeeveeWidgetId(String uuid, int widgetId, String name) {
		try {
			if (!ensureService())
				return false;
			return mService.bindNetworkTeeveeWidgetId(uuid, widgetId, name);
		} catch (Exception e) {
			Log.e(TAG, "bindNetworkTeeveeWidgetId(" + uuid + "," + name + ") err:" + e);
			return false;
		}
	}

	/**
	 * 得到可写的网络段数据对象
	 * <p>
	 * 对象为只读类型
	 * 
	 * @param uuid
	 *            网络UUID
	 * @return 对象
	 */
	public SectionStorage openNetworkSectionStorage(String uuid) {
		String s = null;
		try {
			if ((s = getNetworkSectionStorageDir(uuid)) != null)
				return new SectionStorage(uuid, s, true);
		} catch (Exception e) {
			Log.e(TAG, "open openNetworkSectionStorage(" + s + ") error:" + e);
		}
		return null;
	}

	/**
	 * 得到指定网络的DSMCC服务名称
	 * 
	 * @param uuid
	 *            网络UUID
	 * @return 如果网络提供了则返回名称，否则返回null
	 */
	public String getNetworkDsmccServiceName(String uuid) {
		try {
			if (!ensureService())
				return null;
			return mService.getNetworkDsmccServiceName(uuid);
		} catch (Exception e) {
			Log.e(TAG, "getNetworkDsmccServiceName err:" + e);
			return null;
		}
	}

	/**
	 * 得到段数据存储路径
	 * 
	 * @param uuid
	 *            网络UUID
	 * @return 路径，如果没有返回null
	 */
	public String getNetworkSectionStorageDir(String uuid) {
		try {
			if (!ensureService())
				return null;
			return mService.getNetworkSectionbaseDir(uuid);
		} catch (Exception e) {
			Log.e(TAG, "broadcastSectionbaseChange err:" + e);
			return null;
		}
	}

	public static interface OnNetworkStateListener {
		/**
		 * 网络服务添加
		 * 
		 * @param nettworkUUID
		 *            网络UUID
		 */
		void onNetworkServiceAdd(String nettworkUUID);

		/**
		 * 网络服务移除
		 * 
		 * @param nettworkUUID
		 *            网络UUID
		 */
		void onNetworkServiceRemove(String nettworkUUID);

		/**
		 * 网络服务变更
		 * 
		 * @param nettworkUUID
		 *            网络UUID
		 */
		void onNetworkServiceChange(String nettworkUUID);
	}

	private OnNetworkStateListener lis;

	/**
	 * 设置网络状态监听器
	 * 
	 * @param l
	 *            对象
	 */
	public void setOnNetworkStateListener(OnNetworkStateListener l) {
		if (!ensureService())
			return;
		lis = l;
		try {
			mService.setCallback(l == null ? null : callback);
		} catch (RemoteException e) {
			Log.e(TAG, "add listener failed");
		}
	}

	INetworkServiceManagerCallback callback = new INetworkServiceManagerCallback.Stub() {

		@Override
		public void onNetworkRemove(String netid) throws RemoteException {
			Log.d(TAG, "onNetworkRemove");
			OnNetworkStateListener l = lis;
			if (l == null)
				return;
			try {
				l.onNetworkServiceRemove(netid);
			} catch (Exception e) {
				Log.e(TAG, "onNetworkServiceRemove error" + e);
			}
		}

		@Override
		public void onNetworkAdd(String netid) throws RemoteException {
			Log.d(TAG, "onNetworkAdd");
			OnNetworkStateListener l = lis;
			if (l == null)
				return;
			try {
				l.onNetworkServiceAdd(netid);
			} catch (Exception e) {
				Log.e(TAG, "onNetworkServiceAdd error" + e);
			}
		}

		@Override
		public void onNetworkChange(String netid) throws RemoteException {
			Log.d(TAG, "onNetworkChange");
			OnNetworkStateListener l = lis;
			if (l == null)
				return;
			try {
				l.onNetworkServiceChange(netid);
			} catch (Exception e) {
				Log.e(TAG, "onNetworkServiceChange error" + e);
			}
		}

	};

}
