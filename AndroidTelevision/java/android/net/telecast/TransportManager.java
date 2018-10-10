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
 * 传输管理器
 */
public class TransportManager {
	private static final String TAG = "[java]TransportManager";
	
	private static HashMap<String, WeakReference<TransportManager>> sCache = new HashMap<String, WeakReference<TransportManager>>();
	private static boolean ensured = false;
	private static Hashtable<Integer, NetworkInterface> iftables = new Hashtable<Integer, NetworkInterface>();
	
	private int peer;// for native
	private Context ctx;

	/**
	 * 得到对象的实例
	 * 
	 * @param ctx
	 *            上下文对象
	 * @return 传输管理器
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
	 * 得到所有的网络接口
	 * 
	 * @return 接口对象的列表
	 */
	public List<NetworkInterface> getNetworkInterfaces() {
		ensureNetworkInterfaces();
		synchronized (iftables) {
			Collection<NetworkInterface> c = iftables.values();
			return new ArrayList<NetworkInterface>(c);
		}
	}

	/**
	 * 得到指定ID的网络接口
	 * 
	 * @param interfaceId
	 *            网络接口ID
	 * @return 对象，如果没有返回null
	 */
	public NetworkInterface getNetworkInterfaceById(int interfaceId) {
		ensureNetworkInterfaces();
		return iftables.get(interfaceId);
	}

	/**
	 * 得到指定传输类型的默认传输接口
	 * 
	 * @param deliveryType
	 *            传输类型
	 * @return 对象，如果没有则返回null
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
	 * 创建调谐控制器
	 * 
	 * @param interfaceId
	 *            网络接口
	 * @param flags
	 *            默认为0,具体参考StreamSelector中的FLAG定义
	 * @return 对象
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
	 * 创建调谐控制器
	 * 
	 * @param interfaceId
	 *            网络接口
	 * @return 对象
	 */
	public StreamSelector createSelector(int interfaceId) {
		return createSelector(interfaceId, 0);
	}

	/**
	 * 打开段过滤器对象
	 * <p>
	 * 参考<code>java.util.UUID.toString();</code>
	 * 
	 * @param netid
	 *            网络ID
	 * @param bufsize
	 *            缓冲区大小，如果传入0,将使用默认缓冲区大小
	 * @return 对象
	 */
	public SectionFilter createFilter(String netid, int bufsize) {
		return createFilter(netid, bufsize, 0);
	}

	/**
	 * 打开段过滤器对象
	 * <p>
	 * 参考<code>java.util.UUID.toString();</code>
	 * 
	 * @param netid
	 *            网络ID
	 * @param bufsize
	 *            缓冲区大小，如果传入0,将使用默认缓冲区大小
	 * @param flags
	 *            标志，默认为0,其他参考SectionFilter对象的FLAG静态字段
	 * @return 对象
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
	 * 创建监控类型的段数据过滤器
	 * <p>
	 * 对象会自动处理频点切换相关的逻辑 <br>
	 * 对象默认存在启动延迟，以减轻系统切换频点时的负载
	 * 
	 * @param netid
	 *            网络ID
	 * @param bufsize
	 *            缓冲区大小，如果传入0,将使用默认缓冲区大小
	 * @return 对象
	 */
	public SectionFilter createMonitorFilter(String netid, int bufsize) {
		return createMonitorFilter(netid, bufsize, 0);
	}

	/**
	 * 创建监控类型的段数据过滤器
	 * <p>
	 * 对象会自动处理频点切换相关的逻辑 <br>
	 * 对象默认存在启动延迟，以减轻系统切换频点时的负载
	 * 
	 * @param netid
	 *            网络ID
	 * @param bufsize
	 *            缓冲区大小，如果传入0,将使用默认缓冲区大小
	 * @param flags
	 *            标志，默认为0,其他参考SectionFilter对象的FLAG静态字段 *
	 * @return 对象
	 */
	public SectionFilter createMonitorFilter(String netid, int bufsize, int flags) {
		try {
			flags |= SectionFilter.FLAG_MONITOR_MODE; // 默认带有监控标志
			return new SectionFilter(netid, bufsize, flags);
		} catch (Exception e) {
			Log.d(TAG, "createMonitorFilter error:" + e);
			return null;
		}
	}

	/**
	 * 打开解扰器对象
	 * <p>
	 * 参考<code>java.util.UUID.toString();</code>
	 * 
	 * @param netid
	 *            网络UUID
	 * @return 对象
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
	 * 打开段数据注入器
	 * <p>
	 * 参考<code>java.util.UUID.toString();</code>
	 * 
	 * @param netid
	 *            网络UUID
	 * @return 对象
	 */
	public SectionInjector createInjector(String netid) {
		return SectionInjector.createInjector(netid);
	}

	/**
	 * 打开流观察对象
	 * <p>
	 * 参考<code>java.util.UUID.toString();</code>
	 * 
	 * @param netid
	 *            网络UUID
	 * @return 对象
	 */
	public StreamObserver createObserver(String netid) {
		return createObserver(netid, 0);
	}

	/**
	 * 打开流观察对象
	 * <p>
	 * 参考<code>java.util.UUID.toString();</code>
	 * 
	 * @param netid
	 *            网络UUID
	 * @param flags
	 *            标志 ,默认为0
	 * @return 对象
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
	 * 打开段数据预取器对象
	 * <p>
	 * 如果资源被占用/不足将创建失败
	 * 
	 * @param netid
	 *            网络UUID
	 * @param name
	 *            名称
	 * @param flags
	 *            标志，默认传0
	 * @return 对象,如果失败返回null
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
	 * 创建轮播数据接收器
	 * 
	 * @param netid
	 *            网络ID
	 * @param maxFileSize
	 *            最大缓冲文件句柄的数量
	 * @param bufsize
	 *            过滤器缓冲区大小
	 * @param flags
	 *            标志，默认为0
	 * @return 对象，如果失败返回null
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
	
	// 这里的值一般由应用与时间具体的项目进行约定即可，没有必要定义出来！！原则：系统能不做的就不做的
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
	 * @deprecated 私有函数,请勿调用,除非你知道你在做什么
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
	 * 重启TVSS系统
	 * 
	 * @hide
	 * @deprecated 私有函数,请勿调用,除非你知道你在做什么
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
	 * 设置音频通道输出模式
	 * 
	 * @hide
	 * @deprecated 私有函数,请勿调用,除非你知道你在做什么
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
	 * @deprecated 设置tv专用属性，与系统属性是不同的,此接口成员函数需要Context句柄
	 */
	@Deprecated
	public boolean setTvConfig(String key, String value) {
		return setTvConfig(this.ctx, key, value);
	}

	/**
	 * @hide
	 * @deprecated 获取tv专用属性，与系统属性是不同的,此接口成员函数不需要Context句柄，方便没有Context的使用者
	 */
	@Deprecated
	public static String getTvConfig(String key) {
		return getTvConfig(null, key);
	}

	/**
	 * @hide
	 * @deprecated 获取tv专用属性，与系统属性是不同的,此接口成员函数需要Context句柄
	 * @param key
	 *            主键值
	 * @return
	 */
	@Deprecated
	public String getTvConfig2(String key) {
		return getTvConfig(this.ctx, key);
	}

	/**
	 * @hide 设置tv专用属性，与系统属性是不同的
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
	 * @hide 获取属性
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
	 * 请勿直接调用，需要权限！！！
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

	/** @hide 等价于android.os.SystemProperties() */
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
