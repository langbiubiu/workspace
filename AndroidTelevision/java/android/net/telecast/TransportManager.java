package android.net.telecast;

import java.lang.ref.WeakReference;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.List;

import android.content.Context;
import android.net.telecast.ca.StreamDescrambler;
import android.net.telecast.dvb.CarouselReceiver;
import android.os.IBinder;
import android.util.Log;

/**
 * ���������
 */
public class TransportManager {
	private static final String TAG = "[java]TransportManager";
	
	private static HashMap<String, WeakReference<TransportManager>> sCache = new HashMap<String, WeakReference<TransportManager>>();
	private static boolean ensured = false;
	private static Hashtable<Integer, NetworkInterface> iftables = new Hashtable<Integer, NetworkInterface>();
	
	private int peer;// for native
	private Context ctx;

	/**
	 * �õ������ʵ��
	 * 
	 * @param ctx
	 *            �����Ķ���
	 * @return ���������
	 */
	public static TransportManager getInstance(Context ctx) {
		String pkgname = ctx.getApplicationContext().getPackageName();
		WeakReference<TransportManager> wo = null;
		TransportManager tm = null;
		synchronized (sCache) {
			if ((wo = sCache.get(pkgname)) != null) {
				if ((tm = wo.get()) != null) {
					return tm;
				}
			}
			tm = new TransportManager(ctx);
			wo = new WeakReference<TransportManager>(tm);
			sCache.put(pkgname, wo);
		}
		return tm;
	}

	TransportManager(Context ctx) {
		this.ctx = ctx;
	}

	static void ensureNetworkInterfaces() {
		synchronized (iftables) {
			if (!ensured) {
				ensured = true;
				initNetworkInterfaces();
			}
		}
	}

	/**
	 * �õ����е�����ӿ�
	 * 
	 * @return �ӿڶ�����б�
	 */
	public List<NetworkInterface> getNetworkInterfaces() {
		ensureNetworkInterfaces();
		synchronized (iftables) {
			Collection<NetworkInterface> c = iftables.values();
			return new ArrayList<NetworkInterface>(c);
		}
	}

	/**
	 * �õ�ָ��ID������ӿ�
	 * 
	 * @param interfaceId
	 *            ����ӿ�ID
	 * @return �������û�з���null
	 */
	public NetworkInterface getNetworkInterfaceById(int interfaceId) {
		ensureNetworkInterfaces();
		return iftables.get(interfaceId);
	}

	/**
	 * �õ�ָ���������͵�Ĭ�ϴ���ӿ�
	 * 
	 * @param deliveryType
	 *            ��������
	 * @return �������û���򷵻�null
	 */
	public NetworkInterface getDefaultNetworkInterfaceByType(int deliveryType) {
		List<NetworkInterface> nis = getNetworkInterfaces();
		NetworkInterface ni;
		int size = nis.size();
		for (int i = 0; i < size; i++) {
			if ((ni = nis.get(i)).getDevliveryType() == deliveryType)
				return ni;
		}
		return null;
	}

	/**
	 * ������г������
	 * 
	 * @param interfaceId
	 *            ����ӿ�
	 * @param flags
	 *            Ĭ��Ϊ0,����ο�StreamSelector�е�FLAG����
	 * @return ����
	 */
	public StreamSelector createSelector(int interfaceId, int flags) {
		try {
			return new StreamSelector(interfaceId, "app", flags);
		} catch (Exception e) {
			Log.d(TAG, "createSelector error:" + e);
			return null;
		}
	}

	/**
	 * ������г������
	 * 
	 * @param interfaceId
	 *            ����ӿ�
	 * @return ����
	 */
	public StreamSelector createSelector(int interfaceId) {
		return createSelector(interfaceId, 0);
	}

	/**
	 * �򿪶ι���������
	 * <p>
	 * �ο�<code>java.util.UUID.toString();</code>
	 * 
	 * @param netid
	 *            ����ID
	 * @param bufsize
	 *            ��������С���������0,��ʹ��Ĭ�ϻ�������С
	 * @return ����
	 */
	public SectionFilter createFilter(String netid, int bufsize) {
		return createFilter(netid, bufsize, 0);
	}

	/**
	 * �򿪶ι���������
	 * <p>
	 * �ο�<code>java.util.UUID.toString();</code>
	 * 
	 * @param netid
	 *            ����ID
	 * @param bufsize
	 *            ��������С���������0,��ʹ��Ĭ�ϻ�������С
	 * @param flags
	 *            ��־��Ĭ��Ϊ0,�����ο�SectionFilter�����FLAG��̬�ֶ�
	 * @return ����
	 */
	public SectionFilter createFilter(String netid, int bufsize, int flags) {
		try {
			return new SectionFilter(netid, bufsize, flags);
		} catch (Exception e) {
			Log.d(TAG, "createFilter error:" + e);
			return null;
		}
	}

	/**
	 * ����������͵Ķ����ݹ�����
	 * <p>
	 * ������Զ�����Ƶ���л���ص��߼� <br>
	 * ����Ĭ�ϴ��������ӳ٣��Լ���ϵͳ�л�Ƶ��ʱ�ĸ���
	 * 
	 * @param netid
	 *            ����ID
	 * @param bufsize
	 *            ��������С���������0,��ʹ��Ĭ�ϻ�������С
	 * @return ����
	 */
	public SectionFilter createMonitorFilter(String netid, int bufsize) {
		return createMonitorFilter(netid, bufsize, 0);
	}

	/**
	 * ����������͵Ķ����ݹ�����
	 * <p>
	 * ������Զ�����Ƶ���л���ص��߼� <br>
	 * ����Ĭ�ϴ��������ӳ٣��Լ���ϵͳ�л�Ƶ��ʱ�ĸ���
	 * 
	 * @param netid
	 *            ����ID
	 * @param bufsize
	 *            ��������С���������0,��ʹ��Ĭ�ϻ�������С
	 * @param flags
	 *            ��־��Ĭ��Ϊ0,�����ο�SectionFilter�����FLAG��̬�ֶ� *
	 * @return ����
	 */
	public SectionFilter createMonitorFilter(String netid, int bufsize, int flags) {
		try {
			flags |= SectionFilter.FLAG_MONITOR_MODE; // Ĭ�ϴ��м�ر�־
			return new SectionFilter(netid, bufsize, flags);
		} catch (Exception e) {
			Log.d(TAG, "createMonitorFilter error:" + e);
			return null;
		}
	}

	/**
	 * �򿪽���������
	 * <p>
	 * �ο�<code>java.util.UUID.toString();</code>
	 * 
	 * @param netid
	 *            ����UUID
	 * @return ����
	 */
	@SuppressWarnings("deprecation")
	public StreamDescrambler createDescrambler(String netid) {
		try {
			return new StreamDescrambler(netid);
		} catch (Exception e) {
			Log.d(TAG, "createDescrambler error:" + e);
			return null;
		}
	}

	/**
	 * �򿪶�����ע����
	 * <p>
	 * �ο�<code>java.util.UUID.toString();</code>
	 * 
	 * @param netid
	 *            ����UUID
	 * @return ����
	 */
	public SectionInjector createInjector(String netid) {
		return SectionInjector.createInjector(netid);
	}

	/**
	 * �����۲����
	 * <p>
	 * �ο�<code>java.util.UUID.toString();</code>
	 * 
	 * @param netid
	 *            ����UUID
	 * @return ����
	 */
	public StreamObserver createObserver(String netid) {
		return createObserver(netid, 0);
	}

	/**
	 * �����۲����
	 * <p>
	 * �ο�<code>java.util.UUID.toString();</code>
	 * 
	 * @param netid
	 *            ����UUID
	 * @param flags
	 *            ��־ ,Ĭ��Ϊ0
	 * @return ����
	 */
	public StreamObserver createObserver(String netid, int flags) {
		try {
			return new StreamObserver(netid, flags);
		} catch (Exception e) {
			Log.d(TAG, "createObserver error:" + e);
			return null;
		}
	}

	/**
	 * �򿪶�����Ԥȡ������
	 * <p>
	 * �����Դ��ռ��/���㽫����ʧ��
	 * 
	 * @param netid
	 *            ����UUID
	 * @param name
	 *            ����
	 * @param flags
	 *            ��־��Ĭ�ϴ�0
	 * @return ����,���ʧ�ܷ���null
	 */
	public SectionPrefetcher createPrefetcher(String netid, String name, int flags) {
		try {
			return new SectionPrefetcher(this, netid, name, flags);
		} catch (Exception e) {
			Log.d(TAG, "createPrefetcher error:" + e);
			return null;
		}
	}

	/**
	 * �����ֲ����ݽ�����
	 * 
	 * @param netid
	 *            ����ID
	 * @param maxFileSize
	 *            ��󻺳��ļ����������
	 * @param bufsize
	 *            ��������������С
	 * @param flags
	 *            ��־��Ĭ��Ϊ0
	 * @return �������ʧ�ܷ���null
	 * @hide
	 */
	@SuppressWarnings("deprecation")
	public CarouselReceiver createCarouselReceiver(String netid, int maxFileSize, int bufsize,
			int flags) {
		if (maxFileSize <= 0 || maxFileSize > 1024)
			throw new IndexOutOfBoundsException("maxFileSize <= 0 || maxFileSize > 1024");
		try {
			return new CarouselReceiver(netid, maxFileSize, bufsize, flags);
		} catch (Exception e) {
			Log.d(TAG, "createCarouselReceiver error:" + e);
			return null;
		}
	}
	
	// �����ֵһ����Ӧ����ʱ��������Ŀ����Լ�����ɣ�û�б�Ҫ�����������ԭ��ϵͳ�ܲ����ľͲ�����
	/**
	 * @hide
	 */
	@Deprecated
	public static final String DIGITAL_MODE_KEY = "smarttv.digital.mode";
	/**
	 * @hide
	 */
	@Deprecated
	public static final String AUDIO_TRACK_MODE_KEY = "smarttv.audio.track.mode";

	/**
	 * @hide
	 * @deprecated ˽�к���,�������,������֪��������ʲô
	 */
	@Deprecated
	public boolean switchDtvSourceMode(boolean b) {
		try {
			return setTvConfig(DIGITAL_MODE_KEY, b + "");
		} catch (Exception e) {
			Log.e(TAG, "switchDtvSourceMode(" + b + ") error:" + e);
			return false;
		}
	}

	/**
	 * ����TVSSϵͳ
	 * 
	 * @hide
	 * @deprecated ˽�к���,�������,������֪��������ʲô
	 */
	@Deprecated
	public boolean restartTvss(boolean whenNeed) {
		boolean doit = true;
		if (whenNeed) {
			String s = getTvConfig(DIGITAL_MODE_KEY);
			if (s == null ? true : !s.equals("true"))
				doit = false;
		}
		if (doit)// it will auto restart
			return native_setTvConfig("androidtv.tvss.kill", "0") == 0 ? true : false;
		return false;
	}

	/**
	 * ������Ƶͨ�����ģʽ
	 * 
	 * @hide
	 * @deprecated ˽�к���,�������,������֪��������ʲô
	 */
	@Deprecated
	public boolean setAudioTrackMode(String value) {
		try {
			return setTvConfig(AUDIO_TRACK_MODE_KEY, value);
		} catch (Exception e) {
			Log.e(TAG, "setAudioTrackMode value :" + value);
			return false;
		}
	}

	/**
	 * @hide
	 * @deprecated ����tvר�����ԣ���ϵͳ�����ǲ�ͬ��,�˽ӿڳ�Ա������ҪContext���
	 */
	@Deprecated
	public boolean setTvConfig(String key, String value) {
		return setTvConfig(this.ctx, key, value);
	}

	/**
	 * @hide
	 * @deprecated ��ȡtvר�����ԣ���ϵͳ�����ǲ�ͬ��,�˽ӿڳ�Ա��������ҪContext���������û��Context��ʹ����
	 */
	@Deprecated
	public static String getTvConfig(String key) {
		return getTvConfig(null, key);
	}

	/**
	 * @hide
	 * @deprecated ��ȡtvר�����ԣ���ϵͳ�����ǲ�ͬ��,�˽ӿڳ�Ա������ҪContext���
	 * @param key
	 *            ����ֵ
	 * @return
	 */
	@Deprecated
	public String getTvConfig2(String key) {
		return getTvConfig(this.ctx, key);
	}

	/**
	 * @hide ����tvר�����ԣ���ϵͳ�����ǲ�ͬ��
	 */
	@Deprecated
	private static boolean setTvConfig(Context ctx, String key, String value) {
		boolean ret = false;
		try {
			Method setSouceConfig = Class.forName("androidtv.misc.TvConfigHandler").getMethod(
					"setTvConfig", Context.class, String.class, String.class);
			if (setSouceConfig != null) {
				ret = (Boolean) setSouceConfig.invoke(null, ctx, key, value);
			}
		} catch (Exception e) {
			Log.e(TAG, "call method failed: " + e +" will try tvss method");
		}
		if (!ret)
			return native_setTvConfig(key, value) == 0 ? true : false;
		return ret;
	}

	/**
	 * @hide ��ȡ����
	 */
	private static String getTvConfig(Context ctx, String key) {
		String ret = null;
		try {
			Method getSouceConfig = Class.forName("androidtv.misc.TvConfigHandler").getMethod(
					"getTvConfig", Context.class, String.class);
			if (getSouceConfig != null) {
				ret = (String) getSouceConfig.invoke(null, ctx, key);
			}
		} catch (Exception e) {
			Log.e(TAG, "call method failed: " + e);
		}

		if (ret == null || ret.equals(""))
			return native_getTvConfig(key);
		return ret;
	}

	static List<NetworkInterface> initNetworkInterfaces() {
		List<NetworkInterface> list = new ArrayList<NetworkInterface>();
		NetworkInterface[] nis = new NetworkInterface[8];
		int size = nativeGetNIs(nis);
		for (int i = 0; i < size; i++) {
			list.add(nis[i]);
			iftables.put(nis[i].getId(), nis[i]);
		}
		return list;
	}

	native static int nativeGetNIs(NetworkInterface[] nis);

	native static int native_setTvConfig(String key, String value);

	native static String native_getTvConfig(String key);

	/**
	 * ����ֱ�ӵ��ã���ҪȨ�ޣ�����
	 * 
	 * @hide
	 */
	@Deprecated
	public native static int native_setProcessLevel(String server, int pid, int level, int flags);

	static {
		try {
			System.loadLibrary("join_runtime");
		} catch (Exception e) {
			Log.e(TAG, "loadLibrary 'join_runtime' failed: " + e);
		}
	}

	/**
	 * @hide
	 * @deprecated
	 */
	public static void ensure() {
	}

	private static final Object refMutex = new Object();
	private static HashMap<String, String> props = new HashMap<String, String>();
	static Method systemProperties_get = null;
	static Method serviceManager_getService = null;

	/** @hide */
	public static String getSystemProperty(String name) {
		return getSystemProperty(name, true);
	}

	/** @hide �ȼ���android.os.SystemProperties() */
	public static String getSystemProperty(String name, boolean cachefirst) {
		synchronized (props) {
			String ret = null;
			if (cachefirst) {
				if ((ret = props.get(name)) != null)
					return ret;
			}
			synchronized (refMutex) {
				if (systemProperties_get == null) {
					try {
						systemProperties_get = Class.forName("android.os.SystemProperties")
								.getMethod("get", String.class);
					} catch (Exception e) {
						Log.e(TAG, "get method android.os.SystemProperties.get() error:" + e);
						return null;
					}
				}
			}
			try {
				if ((ret = (String) systemProperties_get.invoke(null, name)) != null)
					props.put(name, ret);
				return ret;
			} catch (Exception e) {
				Log.e(TAG, "invoke method android.os.SystemProperties.get() error:" + e);
				return null;
			}
		}
	}

	/** @hide */
	public static IBinder getServiceByServiceManager(String name) {
		try {
			if (serviceManager_getService == null)
				serviceManager_getService = Class.forName("android.os.ServiceManager")
						.getDeclaredMethod("getService", String.class);
			return (IBinder) serviceManager_getService.invoke(null, name);
		} catch (Exception e) {
			Log.w(TAG, "getServiceByServiceManager error:" + e);
			return null;
		}
	}
}
