package android.net.telecast.ca;

import java.lang.ref.WeakReference;
import java.util.UUID;

import android.content.Context;
import android.net.telecast.TransportManager;
import android.os.ParcelFileDescriptor;
import android.util.Log;

/**
 * CA模块管理器
 */
public class CAModuleManager {
	/** 比较ECM数据更新条件的CRC值，采用软计算方式，而非取自段数据中 */
	public static final int FLAG_ECM_SOFT_CRC = 0x01;
	/** 用于指定智能卡通讯采用 transfer 模式，上层不处理协议逻辑 */
	public static final int FLAG_RESET_MODE_TRANSFER = 0;
	/** 用于指定智能卡通讯按照协议多次read/write，处理协议逻辑 */
	public static final int FLAG_RESET_MODE_READ_WRITE = 0x01;
	/** 智能卡写入指令-超时设定，缓冲区头4个字节为超时时间 */
	public static final int FLAG_WRITE_CMD_TIMEOUT = 0x01;
	/** 启动标志-仅远程设备有效 */
	public static final int FLAG_START_REMOTE_ONLY = 0x01;
	
	static final String TAG = "[java]CAModuleManager";
	private int peer;
	private CAModuleInterface mif = null;
	private Object mutex = new Object();
	private boolean started = false;
	private String netid;
	private Context ctx;
	private int moduleId = -1;

	/**
	 * 创建CA模块对象
	 * 
	 * @param ctx
	 *            上下文环境
	 * @param networkUUID
	 *            网络UUID
	 * @param maxConDesSize
	 *            最大并发解扰数量
	 * @return 对象，失败则返回null
	 */
	public static CAModuleManager createInstance(Context ctx, String networkUUID, int maxConDesSize) {
		if (maxConDesSize < 1 || maxConDesSize > 64) {
			Log.e(TAG, "maxDescTaskSize:" + maxConDesSize + " invalid");
		}
		return new CAModuleManager(networkUUID, maxConDesSize);
	}

	/**
	 * CA模块接口
	 */
	public static interface CAModuleInterface {
		/**
		 * 当ca模块被切换到当前状态
		 * 
		 * @param readerId
		 *            读卡器Id
		 */
		void onPresent(int readerId);

		/**
		 * 当模块被转换为非当前状态时
		 */
		void onAbsent();

		/**
		 * 当要求启动解扰
		 * <p>
		 * 关于解扰索引,其取值的范围是[0-maxDescTaskSize),maxDescTaskSize参数请参考
		 * {@link android.net.telecast.ca.CAModuleManager#setCAModuleInterface(CAModuleInterface, int[])}
		 * 
		 * @param taskIndex
		 *            解扰索引
		 * @param freq
		 *            频率
		 * @param programNumber
		 *            节目号
		 * @param streamPID
		 *            要接绕的流的PID
		 * @param flags
		 *            标志，默认为0
		 */
		void onStartDescrambling(int taskIndex, long freq, int programNumber, int streamPID,
				int ecmpid, int flags);

		/**
		 * 要求停止解扰
		 * 
		 * @param taskIndex
		 *            解扰索引
		 */
		void onStopDescrambling(int taskIndex);

		/**
		 * 收取到ECM数据或者ECM数据发生变化
		 * <p>
		 * 当启用ECM自动接收策略时有效
		 * 
		 * @param taskIndex
		 *            解扰索引
		 * @param buf
		 *            数据缓冲区
		 */
		void onDescramblingEcmUpdate(int taskIndex, byte[] buf);

		/**
		 * 购买授权
		 * 
		 * @param uri
		 *            产品的uri
		 */
		void onBuyEntitlement(String uri);

		/**
		 * 请求进入CA模块所关联的程序
		 * <p>
		 * resolveUri通常由
		 * {@link CAModuleManager#notifyDescramblingError(int, String, String)}
		 * 的参数传递给应用程序，应用程序再依此，解决相关解扰错误的问题
		 * 
		 * @param resolveUri
		 *            如果为空则进入CA模块关联程序的默认界面，否则尝试进入能解决Uri所指代的问题的页面，
		 */
		void onEnterApplication(String resolveUri);

		/**
		 * 远程会话FD
		 * <p>
		 * 如果CA为非本地CA时，会由此消息通知通信的句柄,如果对此通知，则需要使用最新的
		 * <p>句柄需要应用程序自行dup,否则函数返回则句柄失效
		 * @param fd
		 *            句柄
		 */
		void onRemoteSessionFd(ParcelFileDescriptor fd);
	}

	private CAModuleManager(String sid, int cs) {
		UUID id = UUID.fromString(sid);
		netid = id.toString();
		native_create(id.getMostSignificantBits(), id.getLeastSignificantBits(), cs);
	}

	/**
	 * 释放相关资源
	 */
	public void release() {
		native_release();
	}

	/**
	 * 设置管理其所管理的模块的操作接口
	 * 
	 * @param mif
	 *            接口对象
	 * @param caSysIDs
	 *            ca模块的System IDs数组
	 */
	public void setCAModuleInterface(CAModuleInterface mif, int[] caSysIDs) {
		synchronized (mutex) {
			if (started)
				throw new IllegalStateException("already started!");
			this.mif = mif;

			moduleId = native_setif(new WeakReference<CAModuleManager>(this), caSysIDs);
		}
	}
	
	/**@hide*/
	public int getCaModuleId() {
		return moduleId;
	}

	/**
	 * 设置ECM接收策略
	 * <p>
	 * depth 可设置为0或者1,表示禁用/启用自动ECM接收策略 <br>
	 * 如果不启用自动ECM接收，则需要应用程序需自己接收ECM信息 <br>
	 * 标志参考如下:
	 * <li>ACCEPT_UPDATED_SOFTCRC
	 * 
	 * <br>
	 * 不需设置，默认传入0即可.
	 * <p>
	 * 
	 * @param coef
	 *            过滤参数 coef
	 * @param mask
	 *            过滤参数 mask
	 * @param excl
	 *            过滤参数 excl
	 * @param depth
	 *            深度(0、1)
	 * @param caTag
	 *            描述符TAG
	 * @param flags
	 *            默认0,参考SectionFilter的FLAG
	 */
	public void setEcmFilter(int coef, int mask, int excl, int depth, int caTag, int flags) {
		synchronized (mutex) {
			if (started)
				throw new IllegalStateException("already started!");
			native_set_ecmf((byte) coef, (byte) mask, (byte) excl, (byte) depth, caTag, flags);
		}
	}

	/**
	 * 设置CA模块的属性
	 * 
	 * @param name
	 *            名称
	 * @param value
	 *            值
	 */
	public void setProperty(String name, String value) {
		native_set_prop(name, value);
	}

	/**
	 * 启动管理器
	 * 
	 * @param flags
	 *            标志,默认设置为0
	 * @return 成功返回true,否则返回false
	 */
	public boolean start(int flags) {
		synchronized (mutex) {
			if (mif == null) {
				Log.w(TAG, "must call setCAModuleInterface() before start!");
				return false;
			}
			if (native_start(flags)) {
				started = true;
				return true;
			}
		}
		return false;
	}

	/**
	 * 停止管理器
	 */
	public void stop() {
		synchronized (mutex) {
			if (started)
				started = false;
			native_stop();
		}
	}

	/**
	 * 重置智能卡
	 * <p>
	 * 在Present期间调用有效
	 * <p>
	 * 标志参考如下：<br>
	 * <li>FLAG_RESET_MODE_READ_WRITE
	 * <li>FLAG_RESET_MODE_TRANSFER <br>
	 * 如无需设置传入0即可
	 * 
	 * @param flags
	 *            标志，默认传0，
	 * @return 成功返回true,否则返回false
	 */
	public boolean resetCard(int flags) {
		return native_resetcard(flags);
	}

	/**
	 * 得到卡的ATR
	 * 
	 * @return atr数组，如果失败则返回null
	 */
	public byte[] getCardATR() {
		return native_getatr();
	}

	/**
	 * 读卡数据
	 * 
	 * @param b
	 *            缓冲区
	 * @param off
	 *            偏移量
	 * @param len
	 *            长度
	 * @return 实际读取到的长度,如果失败返回-1
	 */
	public int readCard(byte[] b, int off, int len) {
		return readCard(b, off, len, 0);
	}

	/**
	 * 读卡数据
	 * 
	 * @param b
	 *            缓冲区
	 * @param off
	 *            偏移量
	 * @param len
	 *            长度
	 * @param flags
	 *            标志，默认传0
	 * @return 实际读取到的长度,如果失败返回-1
	 */
	public int readCard(byte[] b, int off, int len, int flags) {
		return native_readcard(b, off, len, flags);
	}

	/**
	 * 写卡数据
	 * 
	 * @param b
	 *            缓冲区
	 * @param off
	 *            偏移量
	 * @param len
	 *            长度
	 * @return 实际读取到的长度,如果失败返回-1
	 */
	public int writeCard(byte[] b, int off, int len) {
		return writeCard(b, off, len, 0);
	}

	/**
	 * 写卡数据
	 * <p>
	 * 标志参考如下：
	 * <li>FLAG_WRITE_CMD_TIMEOUT <br>
	 * 如无需设置，传入0即可
	 * <p>
	 * 
	 * @param b
	 *            缓冲区
	 * @param off
	 *            偏移量
	 * @param len
	 *            长度
	 * @param flags
	 *            标志
	 * @return 实际读取到的长度,如果失败返回-1
	 */
	public int writeCard(byte[] b, int off, int len, int flags) {
		return native_writecard(b, off, len, flags);
	}

	/**
	 * 通知验证结果
	 * <p>
	 * 参考{@link CAModuleInterface#onPresent(int)},通过此函数通知管理器卡已被验证
	 * 
	 * @param result
	 *            验证通过则为true,否则则为false
	 */
	public void notifyVerification(boolean result) {
		native_notify_veri(result);
	}

	/**
	 * 更新解扰控制字
	 * 
	 * @param taskIndex
	 *            解扰索引
	 * @param odd
	 *            奇
	 * @param even
	 *            偶
	 * @param len
	 *            长度
	 */
	public void updateControlWord(int taskIndex, byte[] odd, byte[] even, int len) {
		native_update_cw(taskIndex, odd, even, len);
	}

	/**
	 * 通知解扰错误
	 * 
	 * @param taskIndex
	 *            解扰任务索引
	 * @param resolveUri
	 *            解决错误的回传Uri,可以为null,表示无法确定解决方案
	 * @param errTipsMsg
	 *            错误提示信息,可显示给用户
	 */
	public void notifyDescramblingError(int taskIndex, String resolveUri, String errTipsMsg) {
		native_notify_descerr(taskIndex, resolveUri, errTipsMsg);
	}

	/**
	 * 通知解扰错误已恢复
	 * 
	 * @param taskIndex
	 *            解扰任务索引
	 */
	public void notifyDescramblingResumed(int taskIndex) {
		native_notify_resume(taskIndex);
	}

	/**
	 * 通知可解扰状态发生变化
	 * <p>
	 * 应用程序可据此尝试解扰
	 * 
	 */
	public void notifyDescrambableChange() {
		native_notify_descramblable();
	}

	static final int MSG_ON_PRESENT = 1;
	static final int MSG_ON_ABSENT = 2;
	static final int MSG_ON_START_DESC = 3;
	static final int MSG_ON_STOP_DESC = 4;
	static final int MSG_ON_ECM_DESC = 5;
	static final int MSG_ON_BUY_ENT = 6;
	static final int MSG_ON_ENTER_APP = 7;
	static final int MSG_ON_TGD_FD = 8;//telecast gateway device fd
	
	void onCallback(int msg, int p1, long p2, int p3, int p4, int p5, int p6, Object p7) {
		Log.d(TAG, "onCallback msg type:" + msg);
		if (mif == null) {
			Log.e(TAG, "CAModuleManager onCallback NULL pointer");
			return;
		}
		switch (msg) {
		case MSG_ON_PRESENT:
			mif.onPresent(p1);
			break;
		case MSG_ON_ABSENT:
			mif.onAbsent();
			break;
		case MSG_ON_START_DESC:
			mif.onStartDescrambling(p1, p2, p3, p4, p5, p6);
			break;
		case MSG_ON_STOP_DESC:
			mif.onStopDescrambling(p1);
			break;
		case MSG_ON_ECM_DESC:
			mif.onDescramblingEcmUpdate(p1, (byte[]) p7);
			break;
		case MSG_ON_BUY_ENT:
			mif.onBuyEntitlement((String) p7);
			break;
		case MSG_ON_ENTER_APP:
			mif.onEnterApplication((String) p7);
			break;
		case MSG_ON_TGD_FD:
			ParcelFileDescriptor fd =  null;
			try {
				fd = ParcelFileDescriptor.adoptFd(p1);
				mif.onRemoteSessionFd(fd);
			} catch (Exception e) {
				e.printStackTrace();
			} finally {
				if (fd != null)
					fd.detachFd();
			}
			break;
		}
	}

	@SuppressWarnings("unchecked")
	static void native_callback(Object o, int msg, int p1, long p2, int p3, int p4, int p5, int p6,
			Object p7) {
		WeakReference<CAModuleManager> wo;
		CAModuleManager camm;
		Log.i(TAG, "CAModuleManager native_callback");

		if (o == null)
			return;
		try {
			wo = (WeakReference<CAModuleManager>) o;
			camm = wo.get();
			if (camm == null)
				return;
			camm.onCallback(msg, p1, p2, p3, p4, p5, p6, p7);
		} catch (Exception e) {
			Log.d(TAG, "onCallback(" + msg + ") error:" + e);
		}
	}

	protected void finalize() throws Throwable {
		try {
			native_release();
		} catch (Throwable e) {
		}
		super.finalize();
	}

	native void native_create(long m, long l, int s);

	native void native_release();

	native int native_setif(Object o, int[] ids);

	native void native_set_ecmf(byte coef, byte mask, byte excl, byte depth, int tag, int flags);

	// native void native_update_ent(long freq, int streamPId, int entitlement);

	native void native_update_cw(int taskIndex, byte[] odd, byte[] even, int len);

	native void native_notify_veri(boolean b);

	native int native_writecard(byte[] b, int off, int len, int flags);

	native int native_readcard(byte[] b, int off, int len, int flags);

	native byte[] native_getatr();

	native boolean native_resetcard(int flags);

	native boolean native_start(int flags);

	native void native_stop();

	native void native_set_prop(String name, String value);

	native void native_notify_descerr(int taskIndex, String resolveUri, String errTipsMsg);

	native void native_notify_resume(int i);

	native void native_notify_descramblable();

	static {
		init();
	}

	@SuppressWarnings("deprecation")
	static void init() {
		TransportManager.ensure();
	}
}
