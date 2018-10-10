package android.net.telecast.ca;

import java.lang.ref.WeakReference;
import java.util.UUID;

import android.util.Log;

/**
 * 流解扰器
 */
public final class StreamDescrambler {
	/** ECM PID 来自PSI信息 */
	public static final int ECM_PID_FOLLOW_PSI = -1;
	/** 必须存在至少一路需解扰的流(目标流)，否则解扰将终止. */
	public static final int FLAG_NECESSARY_TARGET_STREAM = 0x01;
	/** 启用对ECM PID的的纠错. 即在不一致的情况下，以PSI所指定ECM PID为准 */
	public static final int FLAG_CORRECT_ECM_BY_PSI = 0x02;
	/** 目标流无ECM解扰信息 */
	public static final int FLAG_STREAM_WITH_NO_ECM = 0x04;
	/** 跟随表，ecmpid 为 pmtpid */
	public static final int FLAG_FOLLOW_PSI = 0x08;
	
	static final String TAG = "[java]StreamDescrambler";
	private Object mutex = new Object();
	private String netid;
	private long frequency;
	private int program_number;
	private int stream_pid, ecm_pid;
	private int peer;// for native
	private DescramblingListener listener;
	private int caModuleId = -1;

	/**
	 * @hide
	 * @deprecated 非公开函数，请勿调用
	 */
	public StreamDescrambler(String netid) {
		this.netid = netid;
		UUID id = UUID.fromString(netid);
		if (!native_open(new WeakReference<StreamDescrambler>(this), id.getMostSignificantBits(),
				id.getLeastSignificantBits()))
			throw new RuntimeException("native init failed");
	}

	/**
	 * 获取网络id
	 * 
	 * @return ID值
	 */
	public String getNetworkUUID() {
		return netid;
	}

	/**
	 * 销毁解扰器
	 */
	public void release() {
		synchronized (mutex) {
			if (peer != 0)
				native_close();
			peer = 0;
		}
	}

	public boolean isReleased() {
		return peer == 0;
	}

	/**
	 * 获取当前正在解扰的频道id
	 * 
	 * @return
	 */
	public int getProgramNumber() {
		return program_number;
	}

	/**
	 * 获取当前正在解扰流的频率
	 * 
	 * @return
	 */
	public long getFrequency() {
		return frequency;
	}

	/**
	 * 获取当前正在解扰流的pid
	 * 
	 * @return
	 */
	public int getStreamPID() {
		return stream_pid;
	}

	/**
	 * 请求开始解扰一个基本流
	 * <p>
	 * 调用此函数，系统将自动监控PSI信息收取ECM数据
	 * 
	 * @param f
	 *            频率
	 * @param program_number
	 *            节目号
	 * @param stream_pid
	 *            要解扰的流的PID
	 * @param flags
	 *            标志，默认传入0
	 * @return 成功返回true，失败返回false
	 */
	public boolean start(long f, int program_number, int stream_pid, int flags) {
		this.frequency = f;
		this.program_number = program_number;
		this.stream_pid = stream_pid;
		this.ecm_pid = ECM_PID_FOLLOW_PSI;
		return native_start(f, program_number, stream_pid, ECM_PID_FOLLOW_PSI, flags);
	}

	/**
	 * 请求开始解扰一个基本流
	 * <p>
	 * 系统将使用指定的ECM数据
	 * 
	 * @param f
	 *            频率
	 * @param program_number
	 *            节目号
	 * @param stream_pid
	 *            要解扰的流的PID
	 * @param ecmpid
	 *            要解据的流ECM PID
	 * @param flags
	 *            标志，默认传入0
	 * @return 成功返回true，失败返回false
	 */
	public boolean start(long f, int program_number, int stream_pid, int ecmpid, int flags) {
		ecmpid = (ecmpid <= ECM_PID_FOLLOW_PSI || ecmpid >= 8192) ? ECM_PID_FOLLOW_PSI : ecmpid;
		this.frequency = f;
		this.program_number = program_number;
		this.stream_pid = stream_pid;
		this.ecm_pid = ecmpid;
		return native_start(f, program_number, stream_pid, ecmpid, flags);
	}

	/**
	 * 停止当前解扰
	 * <p>
	 * 将清理所有当前状态,与之前start所关联的CA模块,解除关联关系
	 */
	public void stop() {
		native_stop();
		this.frequency = 0;
		this.program_number = -1;
		this.stream_pid = -1;
		this.ecm_pid = ECM_PID_FOLLOW_PSI;
	}

	/**
	 * 设置要使用的CA模块的ID，解扰将使用此模块完成
	 * 
	 * @param id
	 *            id值
	 */
	public void setCAModuleID(int id) {
		this.caModuleId = id;
		native_set_camodid(id);
	}

	/**
	 * 得到当前CA模块ID,或指定的模块ID
	 * 
	 * @return ID值，如果没有则返回-1
	 */
	public int getCAModuleID() {
		if (caModuleId != -1)
			return caModuleId;
		return native_get_camodid();
	}

	/**
	 * 尝试进入CA相关的应用程序,解决问题
	 * 
	 * @param solveUri
	 *            确认问题解决方案的URI,这通常是由解扰错误消息中回传给应用程序
	 * @see OnDescrambleringListener#onDescramblingError(StreamDescrambler,
	 *      String)
	 */
	public void enterApplication(String solveUri) {
		native_enter(solveUri);
	}

	private boolean breakable = false;

	/** @hide */
	public boolean setBreakable(boolean b) {
		if (breakable != b) {
			if (native_set_brk(b) == 0) {
				breakable = b;
				return true;
			}
		}
		return false;
	}

	/** @hide */
	public boolean isBreakable() {
		return breakable;
	}

	protected void finalize() throws Throwable {
		try {
			native_close();
		} catch (Exception e) {
			e.printStackTrace();
		}
		super.finalize();
	}

	/**
	 * 解扰监听器
	 */
	public static interface DescramblingListener {
		/**
		 * 当解扰开始
		 * 
		 * @param descrambler
		 *            解扰器
		 */
		void onDescramblingStart(StreamDescrambler descrambler);

		/**
		 * 解扰过程中出错
		 * <p>
		 * 错误一般是有条件恢复的，应用场景选择选择等待或者停止
		 * 
		 * @param descrambler
		 *            解扰器
		 * @param solveUri
		 *            解决问题的Uri 如果不为null可以此为参数跳转到CA应用去处理此问题
		 * @param msg
		 * @see StreamDescrambler#enterApplication(String)
		 */
		void onDescramblingError(StreamDescrambler descrambler, String solveUri, String msg);

		/**
		 * 解扰错误已恢复
		 * 
		 * @param descrambler
		 *            解扰器
		 */
		void onDescramblingResumed(StreamDescrambler descrambler);

		/**
		 * 解扰器被终止
		 * <P>
		 * 终止的解扰器是不可恢复的，消息完成后将自动停止
		 * 
		 * @param descrambler
		 *            解扰器
		 * @param msg
		 *            可供显示的消息
		 */
		void onDescramblingTerminated(StreamDescrambler descrambler, String msg);

		/**
		 * 解扰器所属的网络CA发生变化
		 * <p>
		 * 应用可依此消息来重新触发尝试解扰,或忽略消息
		 */
		void onNetworkCAChange(StreamDescrambler descrambler);
	}

	/**
	 * 设置监听器
	 * 
	 * @param lis
	 */
	public void setDescramblingListener(DescramblingListener lis) {
		listener = lis;
	}

	static final int MSG_DESCR_TERMINATED = 1;
	static final int MSG_DESCR_ERROR = 2;
	static final int MSG_DESCR_RESUMED = 3;
	static final int MSG_DESCR_CA_CHANGE = 4;
	static final int MSG_DESCR_SELECT_START = 5;

	void onCallback(int msg, String p1, String p2) {
		DescramblingListener l = listener;
		Log.d(TAG, "onCallback msg type:" + msg);
		if (l == null) {
			Log.e(TAG, "StreamDescrambler onCallback NULL pointer");
			return;
		}
		switch (msg) {
		case MSG_DESCR_TERMINATED:
			l.onDescramblingTerminated(this, p1);
			break;
		case MSG_DESCR_ERROR:
			l.onDescramblingError(this, p2, p1);
			break;
		case MSG_DESCR_RESUMED:
			l.onDescramblingResumed(this);
			break;
		case MSG_DESCR_CA_CHANGE:
			l.onNetworkCAChange(this);
			break;
		case MSG_DESCR_SELECT_START:
			l.onDescramblingStart(this);
			break;
		default:
			Log.d(TAG, "onCallback() with invalid msg:" + msg);
			break;
		}
	}

	native boolean native_open(WeakReference<StreamDescrambler> wo, long idm, long idl);

	native void native_close();

	native boolean native_start(long freq, int programnumber, int streampid, int ecmpid, int flags);

	native void native_stop();

	native void native_enter(String uri);

	native int native_get_camodid();

	native int native_set_camodid(int id);

	native int native_set_brk(boolean b);

	@SuppressWarnings("unchecked")
	static void native_callback(Object o, int msg, String p1, String p2) {
		WeakReference<StreamDescrambler> wo;
		StreamDescrambler d;
		Log.i(TAG, "StreamDescrambler native_callback");
		if (o == null)
			return;
		try {
			wo = (WeakReference<StreamDescrambler>) o;
			d = wo.get();
			if (d == null)
				return;
			d.onCallback(msg, p1, p2);
		} catch (Exception e) {
			Log.e(TAG, "StreamDescrambler onCallback error:" + e);
		}
	}
}
