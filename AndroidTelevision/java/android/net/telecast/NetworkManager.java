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
 * �������
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
	/** ������- ��Ӫ������ */
	public static final String PROPERTY_OPERATOR_NAME = "android.net.telecast.NetworkManager.operator_name";
	/** ������- ����Э�� ���� dvb-c,dvb-t,dvb-s�� */
	public static final String PROPERTY_NETWORK_PROTOCOL = "android.net.telecast.NetworkManager.transport_protocol";
	/** ������- ���ݿ�URI��Ϣ */
	public static final String PROPERTY_DATABASE_AUTHORITIES = "android.net.telecast.NetworkManager.database_authorities";
	/** ������- ������� */
	public static final String PROPERTY_TEEVEE_WIDGET = "android.net.telecast.NetworkManager.teevee_widget";
	/** ������- �������-С�� */
	public static final String PROPERTY_TEEVEE_WIDGET_SMALL = "android.net.telecast.NetworkManager.teevee_widget_small";
	/** ������- ���������(��ѡ) */
	public static final String PROPERTY_RICH_TEEVEE_WIDGET = "android.net.telecast.NetworkManager.rich_teevee_widget";
	/** ������- �����ݴ洢 */
	public static final String PROPERTY_SECTION_STORAGE = "android.net.telecast.NetworkManager.section_storage";
	/** ������- DSMCCЭ����� */
	public static final String PROPERTY_DSMCC_SERVICE = "android.net.telecast.NetworkManager.dsmcc_service";

	/**
	 * �õ�ʵ��
	 * 
	 * @param ctx
	 *            �����Ķ���
	 * @return ����
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
	 * �ͷ���Դ
	 * <p>
	 * �����ٿ���
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
	 * �õ�ϵͳ����Ч�����UUID
	 * 
	 * @return UUID���б�
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
	 * �õ������ṩURI
	 * <p>
	 * һ������£���Ҫ����Ȩ��:android.net.telecast.permission.READ_NETWORK_DATABASE
	 * 
	 * @param netUUID
	 *            ����ID�� �ο�<code>java.util.UUID.toString();</code>
	 * @return ����
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
	 * �õ���������
	 * 
	 * @param netUUID
	 *            ����ID�� �ο�<code>java.util.UUID.toString();</code>
	 * @return ֵ
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
	 * �õ���ѡ�����UUID
	 * 
	 * @return id�ַ����� �ο�<code>java.util.UUID.toString();</code>
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
	 * ����ƫ�õ�����
	 * <p>
	 * ƫ�õ����罫����ʹ����Դ,�����ڶ�����������£�Ӧ�ó��������Ҫʹ���� �����Ӧ���ṩ�Ĺ��ܣ���ô�ڴ�ʱǰһ��Ӧ�ý����趨.
	 * ��ȷ���������Ӧ��������Դ��ɶ�Ӧ�õĲ���
	 * 
	 * @param uuid
	 *            ����ID
	 * @return �ɹ��򷵻�true,���򷵻�false
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
	 * ע�ᵱǰ����Ϊ����Ӧ�ý���
	 * <p>
	 * ��������ɹ����Ƚ���δע����̣���������ʹ����Դ<br>
	 * ע��:setPreferredNetworkByUUID����Ӧ�õ�Ӧ�������Ľ��̽����и��ߵ����ȼ�
	 * <p>
	 * ������������Ҫ����Ȩ��:android.net.telecast.permission.ACCESS_TEEVEE_RESOURCE
	 * 
	 * @return �ɹ��򷵻�true,���򷵻�false
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
	 * ע������CA����
	 * <p>
	 * ����δע��ģ����޷�����ϵͳ��CA���ȷ�����ϵ������
	 * <p>
	 * ��Ҫ����Ȩ��:android.net.telecast.permission.ACCESS_CA_RESOURCE
	 * 
	 * @return �ɹ��򷵻�true,���򷵻�false
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
	 * �õ������ṩ�Ļ������Ӳ������
	 * <p>
	 * ���������ʾ��Ƶ�����ϵ�Tips��Ϣ(CA��غʹ�����Ϣ��)
	 * 
	 * 
	 * @param uuid
	 *            ����UUID
	 * @param widgetId
	 *            ���ID
	 * @return �ɹ�����true,���򷵻�false
	 */
	public boolean bindNetworkTeeveeWidgetId(String uuid, int widgetId) {
		return bindNetworkTeeveeWidgetId(uuid, widgetId, PROPERTY_TEEVEE_WIDGET);
	}

	/**
	 * �õ������ṩ�Ļ������Ӳ������
	 * <p>
	 * ���������ʾ��Ƶ�����ϵ�Tips��Ϣ(CA��غʹ�����Ϣ��)
	 * 
	 * @param uuid
	 *            ����UUID
	 * @param widgetId
	 *            ���ID
	 * @param name
	 *            ����
	 * @return �ɹ�����true,���򷵻�false
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
	 * �õ���д����������ݶ���
	 * <p>
	 * ����Ϊֻ������
	 * 
	 * @param uuid
	 *            ����UUID
	 * @return ����
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
	 * �õ�ָ�������DSMCC��������
	 * 
	 * @param uuid
	 *            ����UUID
	 * @return ��������ṩ���򷵻����ƣ����򷵻�null
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
	 * �õ������ݴ洢·��
	 * 
	 * @param uuid
	 *            ����UUID
	 * @return ·�������û�з���null
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
		 * ����������
		 * 
		 * @param nettworkUUID
		 *            ����UUID
		 */
		void onNetworkServiceAdd(String nettworkUUID);

		/**
		 * ��������Ƴ�
		 * 
		 * @param nettworkUUID
		 *            ����UUID
		 */
		void onNetworkServiceRemove(String nettworkUUID);

		/**
		 * ���������
		 * 
		 * @param nettworkUUID
		 *            ����UUID
		 */
		void onNetworkServiceChange(String nettworkUUID);
	}

	private OnNetworkStateListener lis;

	/**
	 * ��������״̬������
	 * 
	 * @param l
	 *            ����
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
