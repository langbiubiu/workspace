package android.net.telecast;

import java.lang.ref.WeakReference;
import java.util.UUID;

import android.util.Log;

/**
 * 段数据过滤器
 */
public class SectionFilter {
	final static String TAG = "[java]SectionFilter";

	/** 对于每个Section仅接收一次 */
	public static final int ACCEPT_ONCE = 1;
	/** 对于每个Section仅接收一次，使用软件计算CRC */
	public static final int ACCEPT_ONCE_SOFTCRC = 2;
	/** 当Section发生变更时接收 (默认) */
	public static final int ACCEPT_UPDATED = 3;
	/** 当Section发生变更时接收(包括第一次),软件计算CRC */
	public static final int ACCEPT_UPDATED_SOFTCRC = 4;
	/** 无论是否发生变更,总是接收Section */
	public static final int ACCEPT_ALWAYS = 5;

	/** 在监控模式下使用过滤器，自动调度资源并收取目标频点的数据 */
	public static final int FLAG_MONITOR_MODE = 0x01;
	/** 监控对所有频率有效(freq > 0) */
	public static final int FLAG_MONITOR_ALL_FREQ = 0x02;

	final static int default_size = 4096;
	private int peer = 0;
	private String uuid;
	private int bufSize, pid, flags, ntc_flag = 0;
	private int timeout = -1, accept = ACCEPT_ONCE;
	private long frequency = 0;
	private boolean stopped = true, caRequired = false;
	SectionDisposeListener sdl = null;

	SectionFilter() {
	}

	SectionFilter(String uuid, int bufSize, int flags) {
		if (bufSize < default_size) {
			bufSize = default_size;
		} else {
			bufSize = ((bufSize / default_size) + ((bufSize % default_size) == 0 ? 0 : 1))
					* default_size;
		}
		this.flags = flags & (FLAG_MONITOR_MODE | FLAG_MONITOR_ALL_FREQ);
		this.uuid = uuid;
		this.bufSize = bufSize;
		UUID id = UUID.fromString(uuid);
		boolean succ = native_open(new WeakReference<SectionFilter>(this),
				id.getMostSignificantBits(), id.getLeastSignificantBits(), bufSize, this.flags);
		if (!succ || peer == 0)
			throw new RuntimeException();
	}

	final boolean isMonitorMode() {
		return (FLAG_MONITOR_MODE & flags) != 0;
	}

	final boolean isMonitorAllFreq() {
		return (FLAG_MONITOR_MODE & flags) != 0 && (FLAG_MONITOR_ALL_FREQ & flags) != 0;
	}

	/**
	 * 关闭对象资源,之后对象将不可再使用
	 */
	public void release() {
		synchronized (uuid) {
			if (peer != 0) {
				stopped = true;
				native_close();
				if (peer != 0)
					throw new RuntimeException("impl error");
			}
		}
	}

	@Deprecated
	public void close() {
		release();
	}

	protected void finalize() throws Throwable {
		try {
			release();
		} catch (Throwable e) {
		}
		super.finalize();
	}

	/**
	 * 设置数据处理监听器
	 * 
	 * @param l
	 *            对象
	 */
	public void setSectionDisposeListener(SectionDisposeListener l) {
		sdl = l;
	}

	/**
	 * 得到缓冲区大小
	 * 
	 * @return 值
	 */
	public int getBufferSize() {
		return bufSize;
	}

	/**
	 * 得到收取数据的目标网络
	 * 
	 * @see {@link java.util.UUID#toString()}
	 * @return 网络ID
	 */
	public String getNetworkUUID() {
		return uuid;
	}

	/**
	 * 设置是否条件接收
	 * <p>
	 * 默认不需要解扰
	 * 
	 * @param b
	 *            true需要解扰，否则不需要解扰
	 */
	public void setCARequired(boolean b) {
		this.caRequired = b;
	}

	/**
	 * 是否需要解扰
	 * 
	 * @return true不需要解扰，否则需要解扰
	 */
	public boolean isCARequired() {
		return caRequired;
	}

	/**
	 * 设置频率值
	 * 
	 * @param f
	 *            频率值
	 */
	public void setFrequency(long f) {
		if ((flags & FLAG_MONITOR_MODE) != 0 && (flags & FLAG_MONITOR_ALL_FREQ) != 0) {
			return;// not to set, will be set by native
		} else {
			if (f == 0)
				throw new IllegalArgumentException("invalid frequency : 0");
		}
		synchronized (uuid) {
			if (stopped) {
				this.frequency = f;
				return;
			}
		}
		throw new RuntimeException("is not stop state");
	}

	/**
	 * 得到频率值
	 * 
	 * @return 频率值
	 */
	public long getFrequency() {
		return frequency;
	}

	/**
	 * 设置超时
	 * 
	 * @param t
	 *            超时时间毫秒,如果为-1，则永不超时
	 */
	public void setTimeout(int t) {
		synchronized (uuid) {
			if (!isMonitorMode()) {
				if (stopped) {
					this.timeout = t;
					return;
				} else {
					throw new RuntimeException("is not stop state");
				}
			}
		}
	}

	/**
	 * 得到超时时间，默认为-1
	 * 
	 * @return 超时时间值
	 */
	public int getTimeout() {
		if (!isMonitorMode())
			return timeout;
		return -1;
	}

	/**
	 * 设置Section数据接收模式 参考<br>
	 * <li>ACCEPT_ONCE 仅收取一次(默认) <li>ACCEPT_UPDATED 当更新时收取，包括第一次<li>
	 * ACCEPT_ALWAYS 总是收取,无论是否发生变化<br>
	 * 等
	 * 
	 * @param mode
	 *            模式
	 */
	public void setAcceptionMode(int mode) {
		switch (mode) {
		case ACCEPT_ONCE:
		case ACCEPT_ONCE_SOFTCRC:
		case ACCEPT_UPDATED:
		case ACCEPT_UPDATED_SOFTCRC:
		case ACCEPT_ALWAYS:
			break;
		default:
			mode = ACCEPT_ONCE;
			break;
		}
		synchronized (uuid) {
			if (stopped) {
				accept = mode;
				return;
			}
		}
		throw new RuntimeException("is not stop state");
	}

	/**
	 * 得到Section数据接收模式
	 * 
	 * @return 模式
	 */
	public int getAcceptionMode() {
		return accept;
	}

	/**
	 * 当前filter不做table_id与pid的匹配检查
	 * <p>
	 * start 后自动失效、仅支持 ACCEPT_ALWAYS收取方式。
	 */
	public void markNoTableCheck() {
		ntc_flag = 1;
	}

	/**
	 * 启动表过滤
	 * <p>
	 * 对于非监控模式，如果目标频率不在当前状态，启动将直接失败.
	 * 
	 * @param pid
	 *            流PID
	 * @param tableId
	 *            表ID
	 * @return 成功返回true，否则返回false
	 */
	public boolean start(int pid, int tableId) {
		if (tableId < 0 || tableId > 255)
			throw new IllegalArgumentException("tableId < 0 || tableId >= 255");
		byte[] coef = new byte[] { (byte) tableId };
		byte[] mask = new byte[] { (byte) 0xff };
		byte[] excl = new byte[] { (byte) 0 };
		return start(pid, coef, mask, excl, 1);
	}

	/**
	 * 启动过滤
	 * <p>
	 * 对于非监控模式，如果目标频率不在当前状态，启动将直接失败.
	 * 
	 * @param pid
	 *            数据流pid
	 * @param coef
	 *            比对数据
	 * @param mask
	 *            掩码
	 * @param excl
	 *            比对数据
	 * @param len
	 *            长度
	 * @return 成功返回true，否则返回false
	 */
	public boolean start(int pid, byte[] coef, byte[] mask, byte[] excl, int len) {
		try {
			if (pid < 0 || pid >= 8192)
				throw new IllegalArgumentException("pid < 0 || pid >= 8192");
			if (len <= 0 || len > 32)
				throw new IllegalArgumentException("len <= 0 || len > 32");
			if (coef.length < len || mask.length < len || excl.length < len)
				throw new IllegalArgumentException("invalid param array");
			if (!isMonitorAllFreq() && frequency == 0)
				throw new IllegalArgumentException("invalid frequency");
			if (ntc_flag == 1 && accept != ACCEPT_ALWAYS)
				throw new IllegalArgumentException("invalid acception while no table check");
			synchronized (uuid) {
				if (!stopped)
					throw new RuntimeException("is not stop state");
				this.pid = pid;
				if (!native_config(frequency, timeout, accept, caRequired, ntc_flag)) {
					Log.e(TAG, "set filter config failed");
					return false;
				}

				if (!native_start(pid, coef, mask, excl, len)) {
					Log.e(TAG, "start filter failed");
					return false;
				}
				stopped = false;
				return true;
			}
		} finally {
			ntc_flag = 0;
		}
	}

	/**
	 * 停止过滤
	 */
	public void stop() {
		synchronized (uuid) {
			if (!stopped) {
				native_stop();
				stopped = true;
			}
		}
	}

	/**
	 * 得到目标的PID
	 * 
	 * @return 值
	 */
	public int getStreamPID() {
		return pid;
	}

	/**
	 * 读取指定索引的段的数据
	 * <p>
	 * 缓冲区长度应该足够获取数据，以免抛出异常,MPEG2规范中定义最大4K字节
	 * 
	 * @param buf
	 *            缓冲区
	 * @param off
	 *            缓冲区偏移量
	 * @param len
	 *            缓冲区长度
	 * @return 数据长度，如果不可用则返回-1
	 * @throws IndexOutOfBoundsException
	 *             如果给出的缓冲区长度不够
	 */
	public int readSection(byte[] buf, int off, int len) throws IndexOutOfBoundsException {
		if (off < 0 || buf.length < off + len)
			throw new IllegalArgumentException("offset of buf param invalid");
		if (len <= 0 || len > 4096)
			throw new IllegalArgumentException("len <= 0 || len > 4096");
		synchronized (TAG) {
			if (!stopped) {
				return native_read(0, buf, off, len);
			}
			return -1;
		}
	}

	static final int MSG_STREAM_LOST = 1;
	static final int MSG_TIMEOUT = 3;
	static final int MSG_SECTION = 2;
	static final int MSG_CLOSE = 0;
	static final int MSG_MPICK = 10;

	private int onMessage(int msg, int p, long f, Object x) {
		SectionDisposeListener l = sdl;
		Log.v(TAG, "onMessage " + l + " " + msg + " " + p + " " + f);
		if (msg == MSG_MPICK) {
			SectionMonitorListener lp = sml;
			if (lp != null)
				return lp.onSelectFrequency((long[]) x);
			return 0;
		}
		if (l != null) {
			switch (msg) {
			case MSG_STREAM_LOST:
				l.onStreamLost(this);
				break;
			case MSG_SECTION:
				if (isMonitorAllFreq())
					frequency = f;
				l.onSectionRetrieved(this, p);
				break;
			case MSG_TIMEOUT:
				l.onReceiveTimeout(this);
				break;
			case MSG_CLOSE:
				break;
			}
		}
		return 0;
	}

	@SuppressWarnings("unchecked")
	static int native_proc(Object o, int msg, int p, long fv, Object x) {
		WeakReference<SectionFilter> wo;
		SectionFilter f;
		if (o == null)
			return 0;
		try {
			wo = (WeakReference<SectionFilter>) o;
			f = wo.get();
			if (f == null)
				return 0;
			Log.d(TAG, "native_proc> info(" + msg + ", " + p + ")");
			return f.onMessage(msg, p, fv, x);
		} catch (Throwable e) {
			e.printStackTrace();
			return 0;
		}
	}

	private native boolean native_open(WeakReference<SectionFilter> wo, long uuidm, long uuidl,
			int bufsize, int flags);

	private native void native_close();

	private native boolean native_config(long freq, int timeout, int accept, boolean caRequired,
			int ntc_flag);

	private native boolean native_start(int pid, byte[] c, byte[] m, byte[] e, int l);

	private native void native_stop();

	private native void native_mquery();

	private native int native_read(int offset, byte[] b, int off, int len);

	/**
	 * 段数据处置监听器
	 */
	public static interface SectionDisposeListener {

		/**
		 * 目标数据流已丢失,无法再获得数据
		 * 
		 * @param f
		 *            过滤器
		 */
		void onStreamLost(SectionFilter f);

		/**
		 * 当启动接收,超时时间内未获得任何数据
		 * 
		 * @param f
		 *            过滤器
		 */
		void onReceiveTimeout(SectionFilter f);

		/**
		 * 当收到数据
		 * 
		 * @param f
		 *            过滤器
		 * @param len
		 *            section长度
		 */
		void onSectionRetrieved(SectionFilter f, int len);

	}

	/**
	 * 段数据监控监听器
	 * <p>
	 * 典型的可以用于如下情况：
	 * <li>
	 * 对于需要在启动监控时做一些处理的
	 * <li>多个频点同时都可以监控的(多Tuner)<br>
	 * 比如希望针对不同的频点设置不同的启动延迟 <br>
	 * 
	 */
	public static interface SectionMonitorListener {
		/**
		 * 当可监控频点发生变化或应用查询时<br>
		 * 需要由应用程序选择一个监控的频率 <br>
		 * 如果未设置次监听器，系统将默认监控0号索引的频率
		 * <p>
		 * 如果当前有频率正处于监控之下，那么将置于参数的的索引0位置
		 * 
		 * @param freqs
		 *            当前可监控的频率列表
		 * @return 要选择的频率的索引,-1则放弃此次监控
		 */
		int onSelectFrequency(long freqs[]);
	}

	SectionMonitorListener sml = null;

	/**
	 * 添加多频点监控选取监听器
	 * 
	 * @param l
	 *            监听器
	 */
	public void setSectionMonitorListener(SectionMonitorListener l) {
		this.sml = l;
	}

	/**
	 * 查询可监控状态
	 * <p>
	 * 对于监控模式的对象，此方法会查询可监控频点信息,并触发回调函数SectionMonitorListener.onSelectFrequency
	 */
	public void queryMonitorable() {
		if (isMonitorMode() && sml != null)
			native_mquery();
	}
}
