package android.net.telecast;

import java.io.FileDescriptor;
import java.lang.ref.WeakReference;
import java.util.UUID;

import android.util.Log;

/**
 * 数字信号传输流选择器
 * <p>
 * 本对象兼具调谐器与解复用器的功能(Tuner,Demux)
 * <p>
 * 本对象有两重作用<br>
 * 1 选择频率或者数据流作为数据源,即接收数据<br>
 * 2 为后续输出的数据流赋予网络ID标识,即标识数据<br>
 * 对于第二点，将决定性的影响，后续由网络ID为基础建立的数据流分发体系。比如收取数据的SectionFilter，
 * 解扰数据的StreamDescrambler等
 */
public class StreamSelector {
	final String TAG = "[java]StreamSelector";

	/** 创建-默认 */
	public static final int CREATE_FLAG_DEFAULT = 0;
	/** 创建-在Tuner模式下也支持推送数据 */
	public static final int CREATE_FLAG_PUSHABLE_ALSO = 0x1;
	/** 创建-支持等待模式 */
	public static final int CREATE_FLAG_NO_RETRY = 0x4;
	/** 创建-支持推送大流量数据(要求为push模式或设置CREATE_FLAG_PUSHABLE_ALSO) */
	public static final int CREATE_FLAG_PUSH_HIGH_FLOW = 0x8;

	/** 默认 */
	public static final int SELECT_FLAG_DEFAULT = 0;
	/** 如果锁频失败自动重复尝试 */
	public static final int SELECT_FLAG_RETRY = 0x1;
	/** 强制重新锁频,仅对FrequencyInfo有效 */
	public static final int SELECT_FLAG_FORCE = 0x2;
	/** 循环重复推送数据,仅对文件类型的FileDescriptor有效 */
	public static final int SELECT_FLAG_REPEAT = 0x4;
	/** 建议准备,若资源有空余系统则进行预锁,仅对FrequencyInfo有效 */
	public static final int SELECT_FLAG_ADVISE = 0x8;

	/** 接收流标志-默认 */
	public static final int RECEIVE_FLAG_DEFAULT = 0;
	/** 接收流标志-按组 */
	public static final int RECEIVE_FLAG_GROUPED = 0x1;
	/** 接收流标志-本地加密 */
	public static final int RECEIVE_FLAG_NATIVE_ENCRYPTION = 0x2;
	/** 接收流标志-能力测试，不做实际接收用 */
	public static final int RECEIVE_FLAG_TEST_CAPABILITY = 0x4;

	/** 用于选择一个空流来替换之前的选择(包括频率，文件以及流) {@link #select(FrequencyInfo, int)} */
	public static FileDescriptor EMPTY_STREAM = new FileDescriptor();

	private Object mutex = new Object();
	private int interfaceId;
	private int peer, count = -1;// for native
	private SelectionStateListener ssl;
	private String appname;
	private UUID uuid = null;
	private long freq = 0;
	private boolean weak = false, released = false;

	StreamSelector() {
	}

	StreamSelector(int interfaceId, String appname, int flags) {
		this.interfaceId = interfaceId;
		this.appname = appname;
		boolean succ = native_reserve(new WeakReference<StreamSelector>(this), interfaceId, flags);
		if (!succ || peer == 0) {
			throw new RuntimeException();
		}
	}

	/**
	 * 得到网络接口ID
	 * 
	 * @return 值
	 */
	public int getNetworkInterfaceId() {
		return interfaceId;
	}

	/**
	 * 释放资源
	 */
	public void release() {
		synchronized (mutex) {
			if (!released) {
				Log.v(TAG, "release StreamSelector!");
				released = true;
				native_release();
			}
		}
	}

	/**
	 * 对象是否已释放
	 * 
	 * @return 是返回true,否则返回false
	 */
	public boolean isReleased() {
		return released;
	}

	@Override
	protected void finalize() throws Throwable {
		try {
			release();
		} catch (Throwable e) {
		}
		super.finalize();
	}

	void checkPeer() {
		if (peer == 0)
			throw new IllegalStateException("not reserve!");
	}

	/**
	 * 得到信号状态
	 * 
	 * @return 信号状态
	 */
	public SignalStatus getSignalStatus() {
		synchronized (mutex) {
			checkPeer();
			SignalStatus ss = new SignalStatus();
			if (native_signal_status(ss))
				return ss;
			return null;
		}
	}

	/**
	 * 设置网络ID<br>
	 * 对于数据收取等操作具有关键选择的作用。同网络的后续数据流节点会被激活(比如SectionFilter).
	 * <p>
	 * 对下一次select有效.<br>
	 * 
	 * @param id
	 *            ID , 参考<code>java.util.UUID.toString();</code>
	 */
	public void setNetworkUUID(String id) {
		long most = 0, least = 0;
		UUID uuid = null;
		if (id != null) {
			uuid = UUID.fromString(id);
			most = uuid.getMostSignificantBits();
			least = uuid.getLeastSignificantBits();
			if (this.uuid == null ? false : this.uuid.equals(uuid))
				return;
		}
		boolean b = native_set_network(most, least);
		if (b)
			this.uuid = uuid;
		Log.d(TAG, "setNetworkUUID(" + most + "," + least + ") " + (b ? "ok" : "failed"));
	}

	/**
	 * 设定虚拟频率，0表示不启用
	 * 
	 * @param freq
	 *            为流式数据源或文件数据源,必须为负值
	 */
	public void setVirtualFrequency(long freq) {
		if (freq > 0)
			freq = 0;
		native_set_virtual_frequency(freq);
	}

	public String getSelectUri() {
		return uri;
	}

	String uri;

	/**
	 * 选择频点
	 * 
	 * @param fi
	 *            频率
	 * @param flags
	 *            标志,默认请传入0
	 * @return 发生错误返回false，否则返回true
	 */
	public boolean select(FrequencyInfo fi, int flags) {
		synchronized (mutex) {
			checkPeer();
			return native_select_freq((uri = fi.toString()), flags);
		}
	}

	/**
	 * 设置资源弱引用模式
	 * <p>
	 * <li>参数为true时改为弱引用模式，此时资源可能会被系统收回，除关闭对象或重新改回强引用模式外,不应做其他操作
	 * <li>参数为false时改回强引用模式，若操作失败，则说明资源已被剥夺，应该关闭对象.
	 * 
	 * @return 成功返回true,否则返回false
	 */
	public boolean setWeakMode(boolean b) {
		synchronized (mutex) {
			if (native_set_weak_mode(b)) {
				weak = b;
				return true;
			}
			return false;
		}
	}

	/**
	 * 清理demux的缓存
	 * @return 成功返回true,否则返回false
	 */
	public boolean clear() {
		synchronized (mutex) {
			return native_clear();
		}
	}

	/**
	 * 是否为弱引用模式
	 * 
	 * @return 是则返回true,否则返回false
	 */
	public boolean isWeakMode() {
		return weak;
	}

	/**
	 * 选择文件作为频点数据源
	 * 
	 * @param fd
	 *            文件FD
	 * @param off
	 *            文件偏移地址
	 * @param len
	 *            文件长度
	 * @param flags
	 *            标志,默认请传入0
	 * @return 发生错误返回false，否则返回true
	 */
	public boolean select(FileDescriptor fd, long off, long len, int flags) {
		synchronized (mutex) {
			checkPeer();
			if (fd == EMPTY_STREAM || fd == null) {
				uri = "fd://" + (-1);
				fd = EMPTY_STREAM;
			} else {
				uri = "fd://" + fd.toString();// 顺便做了null pointer检查
			}
			return native_select_file(fd, off, len, flags);
		}
	}

	/**
	 * 选择流作为频点数据源
	 * <p>
	 * 可以是套接字输入流或者管道的输入流
	 * 
	 * @param fd
	 *            文件FD 锁频的方法
	 * @param flags
	 *            标志,默认请传入0
	 * @return 发生错误返回false，否则返回true
	 */
	public boolean select(FileDescriptor fd, int flags) {
		synchronized (mutex) {
			checkPeer();
			if (fd == EMPTY_STREAM || fd == null) {
				uri = "fd://" + (-1);
				fd = EMPTY_STREAM;
			} else {
				uri = "fd://" + fd.toString();// 顺便做了null pointer检查
			}
			return native_select_stream(fd, flags);
		}
	}

	/**
	 * 得到当前正在调谐或者已调谐的频率值，否则返回0
	 * 
	 * @return 频率值
	 */
	public long getCurrentFrequency() {
		return freq;
	}

	/**
	 * 接收PID所对应的流数据
	 * 
	 * @param pid
	 *            流的PID
	 * @param fd
	 *            文件描述符，应传入流式句柄(管道或者套接字)
	 * @param flags
	 *            标志，默认为0
	 * @return 成功则返回true，否则返回false
	 */
	public boolean receive(int pid, FileDescriptor fd, int flags) {
		synchronized (mutex) {
			checkPeer();
			if (fd == EMPTY_STREAM || fd == null) {
				fd = EMPTY_STREAM;
			}
			return native_receive_stream(new int[] { pid }, fd, flags);
		}
	}

	/**
	 * 接收一组PID所对应的流
	 * 
	 * @param pids
	 *            pid数组
	 * @param fd
	 *            文件描述符，应传入流式句柄(管道或者套接字)
	 * @param flags
	 *            标志，默认为0
	 * @return 成功则返回true，否则返回false
	 */
	public boolean receive(int[] pids, FileDescriptor fd, int flags) {
		synchronized (mutex) {
			checkPeer();
			if (fd == EMPTY_STREAM || fd == null) {
				fd = EMPTY_STREAM;
			}
			return native_receive_stream(pids, fd, flags);
		}
	}

	/**
	 * 设置监听器
	 * 
	 * @param l
	 *            对象
	 */
	public void setSelectionStateListener(SelectionStateListener l) {
		ssl = l;
	}

	static final int MSG_TUNNING_OVER = 1;
	static final int MSG_LOCK_STATE = 2;
	static final int MSG_RES_RECYLED = 3;
	static final int MSG_TUNNING_START = 4;

	void onMessage(int msg, int p1, long p2) {
		SelectionStateListener l = ssl;
		if (l == null)
			return;
		switch (msg) {
		case MSG_TUNNING_START:
			freq = p2;
			l.onSelectStart(this);
			break;
		case MSG_TUNNING_OVER:
			if (p1 == 0)
				l.onSelectFailed(this);
			else
				l.onSelectSuccess(this);
			return;
		case MSG_LOCK_STATE:
			if (p1 == 0)
				l.onSelectionLost(this);
			else
				l.onSelectionResumed(this);
			return;
		default:
			return;
		}
	}

	static final Object msgMutex = new Object();

	@SuppressWarnings("unchecked")
	static void native_proc(Object o, int msg, int p1, long p2) {
		WeakReference<StreamSelector> wo;
		StreamSelector ts;
		if (o == null)
			return;
		try {
			wo = (WeakReference<StreamSelector>) o;
			ts = wo.get();
			if (ts == null)
				return;
			synchronized (msgMutex) {
				ts.onMessage(msg, p1, p2);
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	native boolean native_reserve(WeakReference<StreamSelector> owner, int interfaceId, int flags);

	native void native_release();

	native boolean native_set_weak_mode(boolean b);
	
	native boolean native_clear();

	native boolean native_signal_status(SignalStatus ss);

	native boolean native_set_network(long most, long least);

	native void native_set_virtual_frequency(long freq);

	native boolean native_select_freq(String freq, int flags);

	native boolean native_select_file(FileDescriptor fd, long off, long len, int flags);

	native boolean native_select_stream(FileDescriptor fd, int flags);

	native boolean native_receive_stream(int[] pid, FileDescriptor fd, int flags);

	/**
	 * 调谐监听器
	 */
	public static interface SelectionStateListener {
		/**
		 * 给定的频率已开始调谐
		 * <p>
		 * 这意味着后续的消息都是针对此频率的,直到再次收到此消息为止
		 * 
		 * @param selector
		 * 
		 */
		void onSelectStart(StreamSelector selector);

		/**
		 * 选择成功
		 * 
		 * @param selector
		 */
		void onSelectSuccess(StreamSelector selector);

		/**
		 * 选择失败
		 * 
		 * @param selector
		 */
		void onSelectFailed(StreamSelector selector);

		/**
		 * 目标丢失
		 * 
		 * @param selector
		 */
		void onSelectionLost(StreamSelector selector);

		/**
		 * 目标重新获取
		 * 
		 * @param selector
		 */
		void onSelectionResumed(StreamSelector selector);

	}
}
