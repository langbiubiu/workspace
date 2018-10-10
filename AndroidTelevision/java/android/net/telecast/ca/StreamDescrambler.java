package android.net.telecast.ca;

import java.lang.ref.WeakReference;
import java.util.UUID;

import android.util.Log;

/**
 * ��������
 */
public final class StreamDescrambler {
	/** ECM PID ����PSI��Ϣ */
	public static final int ECM_PID_FOLLOW_PSI = -1;
	/** �����������һ·����ŵ���(Ŀ����)��������Ž���ֹ. */
	public static final int FLAG_NECESSARY_TARGET_STREAM = 0x01;
	/** ���ö�ECM PID�ĵľ���. ���ڲ�һ�µ�����£���PSI��ָ��ECM PIDΪ׼ */
	public static final int FLAG_CORRECT_ECM_BY_PSI = 0x02;
	/** Ŀ������ECM������Ϣ */
	public static final int FLAG_STREAM_WITH_NO_ECM = 0x04;
	/** �����ecmpid Ϊ pmtpid */
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
	 * @deprecated �ǹ����������������
	 */
	public StreamDescrambler(String netid) {
		this.netid = netid;
		UUID id = UUID.fromString(netid);
		if (!native_open(new WeakReference<StreamDescrambler>(this), id.getMostSignificantBits(),
				id.getLeastSignificantBits()))
			throw new RuntimeException("native init failed");
	}

	/**
	 * ��ȡ����id
	 * 
	 * @return IDֵ
	 */
	public String getNetworkUUID() {
		return netid;
	}

	/**
	 * ���ٽ�����
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
	 * ��ȡ��ǰ���ڽ��ŵ�Ƶ��id
	 * 
	 * @return
	 */
	public int getProgramNumber() {
		return program_number;
	}

	/**
	 * ��ȡ��ǰ���ڽ�������Ƶ��
	 * 
	 * @return
	 */
	public long getFrequency() {
		return frequency;
	}

	/**
	 * ��ȡ��ǰ���ڽ�������pid
	 * 
	 * @return
	 */
	public int getStreamPID() {
		return stream_pid;
	}

	/**
	 * ����ʼ����һ��������
	 * <p>
	 * ���ô˺�����ϵͳ���Զ����PSI��Ϣ��ȡECM����
	 * 
	 * @param f
	 *            Ƶ��
	 * @param program_number
	 *            ��Ŀ��
	 * @param stream_pid
	 *            Ҫ���ŵ�����PID
	 * @param flags
	 *            ��־��Ĭ�ϴ���0
	 * @return �ɹ�����true��ʧ�ܷ���false
	 */
	public boolean start(long f, int program_number, int stream_pid, int flags) {
		this.frequency = f;
		this.program_number = program_number;
		this.stream_pid = stream_pid;
		this.ecm_pid = ECM_PID_FOLLOW_PSI;
		return native_start(f, program_number, stream_pid, ECM_PID_FOLLOW_PSI, flags);
	}

	/**
	 * ����ʼ����һ��������
	 * <p>
	 * ϵͳ��ʹ��ָ����ECM����
	 * 
	 * @param f
	 *            Ƶ��
	 * @param program_number
	 *            ��Ŀ��
	 * @param stream_pid
	 *            Ҫ���ŵ�����PID
	 * @param ecmpid
	 *            Ҫ��ݵ���ECM PID
	 * @param flags
	 *            ��־��Ĭ�ϴ���0
	 * @return �ɹ�����true��ʧ�ܷ���false
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
	 * ֹͣ��ǰ����
	 * <p>
	 * ���������е�ǰ״̬,��֮ǰstart��������CAģ��,���������ϵ
	 */
	public void stop() {
		native_stop();
		this.frequency = 0;
		this.program_number = -1;
		this.stream_pid = -1;
		this.ecm_pid = ECM_PID_FOLLOW_PSI;
	}

	/**
	 * ����Ҫʹ�õ�CAģ���ID�����Ž�ʹ�ô�ģ�����
	 * 
	 * @param id
	 *            idֵ
	 */
	public void setCAModuleID(int id) {
		this.caModuleId = id;
		native_set_camodid(id);
	}

	/**
	 * �õ���ǰCAģ��ID,��ָ����ģ��ID
	 * 
	 * @return IDֵ�����û���򷵻�-1
	 */
	public int getCAModuleID() {
		if (caModuleId != -1)
			return caModuleId;
		return native_get_camodid();
	}

	/**
	 * ���Խ���CA��ص�Ӧ�ó���,�������
	 * 
	 * @param solveUri
	 *            ȷ��������������URI,��ͨ�����ɽ��Ŵ�����Ϣ�лش���Ӧ�ó���
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
	 * ���ż�����
	 */
	public static interface DescramblingListener {
		/**
		 * �����ſ�ʼ
		 * 
		 * @param descrambler
		 *            ������
		 */
		void onDescramblingStart(StreamDescrambler descrambler);

		/**
		 * ���Ź����г���
		 * <p>
		 * ����һ�����������ָ��ģ�Ӧ�ó���ѡ��ѡ��ȴ�����ֹͣ
		 * 
		 * @param descrambler
		 *            ������
		 * @param solveUri
		 *            ��������Uri �����Ϊnull���Դ�Ϊ������ת��CAӦ��ȥ���������
		 * @param msg
		 * @see StreamDescrambler#enterApplication(String)
		 */
		void onDescramblingError(StreamDescrambler descrambler, String solveUri, String msg);

		/**
		 * ���Ŵ����ѻָ�
		 * 
		 * @param descrambler
		 *            ������
		 */
		void onDescramblingResumed(StreamDescrambler descrambler);

		/**
		 * ����������ֹ
		 * <P>
		 * ��ֹ�Ľ������ǲ��ɻָ��ģ���Ϣ��ɺ��Զ�ֹͣ
		 * 
		 * @param descrambler
		 *            ������
		 * @param msg
		 *            �ɹ���ʾ����Ϣ
		 */
		void onDescramblingTerminated(StreamDescrambler descrambler, String msg);

		/**
		 * ����������������CA�����仯
		 * <p>
		 * Ӧ�ÿ�������Ϣ�����´������Խ���,�������Ϣ
		 */
		void onNetworkCAChange(StreamDescrambler descrambler);
	}

	/**
	 * ���ü�����
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
