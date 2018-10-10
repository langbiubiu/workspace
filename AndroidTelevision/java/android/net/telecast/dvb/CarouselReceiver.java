package android.net.telecast.dvb;

import java.io.FileDescriptor;
import java.lang.ref.WeakReference;
import java.util.UUID;

import android.util.Log;

/**
 * 轮播接收器
 * <p>
 * 应用程序应使用此对象进行轮播数据接收。如果应用程序使用SectionFilter自行实现相关逻辑，<br>
 * 可能因多路同条件SecionFilter并发而导致的效率低下问题。甚至，这在某些需要高性能数据下载的场合是致命的。
 * 
 * @hide
 */
public class CarouselReceiver {
	static final String TAG = "[java]CarouselReceiver";

	/** 创建标志 - 默认 */
	public static final int CREATE_FLAG_DEFAULT = 0;
	/** 创建标志-全部监控，包含 DII 及 DSI 数据 */
	public static final int CREATE_FLAG_ALL_MONITOR = 0;
	/** 创建标志 - 不监听DII变化，只会启动下载Module的形式 */
	public static final int CREATE_FLAG_NOT_OBSERVE_DII = 0x1;

	/** 接收Gateway的文件的索引 */
	public static final int GATEWAY_FILE_INDEX = 0;

	private ReceiverStateListener rsl;
	private String uuid = null;
	boolean running = false;
	private int maxFileSize = 2, peer = 0;
	private boolean isAttached = false, isRelased = false;;

	/**
	 * @deprecated 私有函数请勿调用
	 * @hide 非公开函数请勿调用 请通过TransportManager.createCarouselReceiver()获取。
	 * 
	 * @param UUID
	 *            网络UUID
	 * @param maxFileSize
	 *            文件最大个数，所有的DII，Module的存储都是直接存放在文件中。
	 * 
	 * @param bufsize
	 *            filter的缓存大小
	 * @param flags
	 *            0:自动监控DSI/DII的版本变化; 1:不监听DII变化，只会启动下载Module的形式。
	 * 
	 */
	public CarouselReceiver(String uuid, int maxFileSize, int bufsize, int flags) {
		UUID id = UUID.fromString(uuid);
		this.uuid = id.toString();
		this.maxFileSize = maxFileSize;
		Log.i(TAG, "CarouselReceiver uuid=" + uuid);
		boolean succ = native_open(new WeakReference<CarouselReceiver>(this),
				id.getMostSignificantBits(), id.getLeastSignificantBits(), maxFileSize, bufsize,
				flags);
		if (!succ || peer == 0) {
			throw new RuntimeException("native open failed!");
		}
	}

	/**
	 * 得到网络UUID
	 * 
	 * @return id字符串
	 */
	public String getNetworkUUID() {
		return this.uuid.toString();
	}

	protected void finalize() throws Throwable {
		release();
		super.finalize();
	}

	/**
	 * 得到最大的接收文件数量
	 * 
	 * @return 值
	 */
	public int getMaxReceivingFileSize() {
		return maxFileSize;
	}

	/**
	 * 释放资源
	 */
	public synchronized void release() {
		synchronized (uuid) {
			if (!isRelased) {
				isRelased = true;
				native_release();
			}
		}
	}

	/**
	 * 关联到资源
	 * 
	 * @param freq
	 *            频率
	 * @param pid
	 *            流PID
	 */
	public void attach(long freq, int pid) {
		if (freq == 0 || pid < 0 || pid >= 8192)
			throw new IllegalArgumentException();
		Log.d(TAG, "attach freq=" + freq + ",pid=" + pid);
		synchronized (uuid) {
			if (native_attach(freq, pid) == 0) {
				isAttached = true;
			}
		}
	}

	/**
	 * 解除到资源的关联 关闭下载
	 */
	public void detach() {
		synchronized (uuid) {
			isAttached = false;
			native_detach();
		}
	}

	/**
	 * 是否已关联到下载服务
	 * 
	 * @return 是返回true,否则返回false
	 */
	public boolean isAttached() {
		return isAttached;
	}

	/**
	 * 设置Gateway的接收文件--DII的section信息根据section_number按序存入。
	 * 
	 * @param fd
	 *            文件句柄
	 */
	public void setGatewayFile(FileDescriptor fd) {
		synchronized (uuid) {
			setReceivingFile(GATEWAY_FILE_INDEX, fd);
		}
	}

	/**
	 * 设置接收文件的句柄
	 * 
	 * @param index
	 *            文件索引
	 * @param fd
	 *            对应的文件句柄
	 */
	public void setReceivingFile(int index, FileDescriptor fd) {
		if (index < 0 || index >= maxFileSize)
			throw new IndexOutOfBoundsException();
		synchronized (uuid) {
			Log.i(TAG, "setReceivingFile index =" + index + ",fd=" + fd);
			native_setf(index, fd);
		}
	}

	/**
	 * 同步Gateway数据到最新
	 * <p>
	 * 将最新的DII信息同步到setGatewayFile(FileDescriptor fd) 指定的fd中。
	 */
	public void syncGateway() {
		synchronized (uuid) {
			native_syncg();
		}
	}

	/**
	 * 接收模块数据
	 * 
	 * @param moduleId
	 *            模块ID
	 * @param fileIndex
	 *            文件索引
	 * @param offset
	 *            写入文件的偏移量
	 * @param offset
	 *            收取模式、 广告下载是按照module来排列的，一个module就是一个文件。
	 *            可以多个module公用同一个文件，使用offset来区别
	 * 
	 */
	public void receiveModule(int moduleId, int fileIndex, int offset) {
		synchronized (uuid) {
			native_recvm(moduleId, fileIndex, offset);
		}
	}

	/**
	 * 监控模块的版本
	 * 
	 * @param moduleId
	 *            模块ID
	 * @param currentVersion
	 *            当前的版本
	 */
	public void observeModule(int moduleId, int currentVersion) {
		synchronized (uuid) {
			native_obsm(moduleId, currentVersion);
		}
	}

	/**
	 * 取消对模块的操作
	 * 
	 * @param moduleId
	 *            模块ID
	 */
	public void cancelModule(int moduleId) {
		native_cancm(moduleId);
	}

	/**
	 * 设置监听器
	 * 
	 * @param l
	 *            对象
	 */
	public void setReceiverStateListener(ReceiverStateListener l) {
		rsl = l;
	}

	/**
	 * 接收状态监听器
	 */
	public static interface ReceiverStateListener {
		/**
		 * Gateway发生变化--DII发生变化，仅通知发生变化。
		 * 想获取dii信息，调用syncGateway接口。将DII所有的section信息写入文件中。 具体解析DII数据需要下一步的解析
		 */
		void onGatewayChanged(int version);

		/**
		 * 模块发生变化
		 * 
		 * @param moduleId
		 *            模块ID
		 */
		void onModuleChanged(int moduleId, int version);

		/**
		 * 接收模块结束
		 * 
		 * @param moduleId
		 *            模块ID
		 * @param success
		 *            成功为true,失败为false Module接收成功
		 *            <p>
		 *            需要调用下一步的解析方法才能拿到真正的文件信息。
		 */
		void onModuleReceiveOver(int moduleId, boolean success);

		/**
		 * 轮播接收器已暂停工作
		 */
		void onReceiverPaused();

		/**
		 * 轮播接收器已恢复工作
		 */
		void onReceiverResumed();

		/**
		 * 轮播接收器已关闭
		 */
		void onReceiverClosed();
	}

	native boolean native_open(WeakReference<CarouselReceiver> wo, long most, long least, int mfs,
			int bfs, int flags);

	native int native_attach(long f, int pid);

	native int native_detach();

	native int native_release();

	native int native_recvm(int mid, int fi, int off);

	native int native_obsm(int mid, int v);

	native int native_cancm(int mid);

	native int native_syncg();

	native int native_setf(int i, FileDescriptor fd);

	static final int MSG_GW_UP = 1;
	static final int MSG_MOD_RECV = 2;
	static final int MSG_MOD_UP = 3;
	static final int MSG_RECV_STATE = 4;

	private void onCallback(int msg, int p1, int p2) {
		ReceiverStateListener l = rsl;
		if (l == null)
			return;
		switch (msg) {
		case MSG_GW_UP:
			l.onGatewayChanged(p1);
			break;
		case MSG_MOD_UP:
			l.onModuleChanged(p1, p2);
			return;
		case MSG_MOD_RECV:
			if (p2 == 0)
				l.onModuleReceiveOver(p1, true);
			else
				l.onModuleReceiveOver(p1, false);
			return;
		case MSG_RECV_STATE:
			if (p1 == 0)
				l.onReceiverClosed();
			else if (p1 == 1)
				l.onReceiverPaused();
			else if (p1 == 2)
				l.onReceiverResumed();
			return;
		default:
			return;
		}
	}

	@SuppressWarnings({ "unchecked", "rawtypes" })
	static void native_callback(Object o, int msg, int p1, int p2) {
		try {
			WeakReference<CarouselReceiver> wo = (WeakReference) o;
			if (wo == null)
				return;
			CarouselReceiver r = wo.get();
			if (r == null)
				return;
			r.onCallback(msg, p1, p2);
		} catch (Exception e) {
			Log.d(TAG, "onCallback error:" + e);
		}
	}
}
