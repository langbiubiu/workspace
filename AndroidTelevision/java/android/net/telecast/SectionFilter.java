package android.net.telecast;

import java.lang.ref.WeakReference;
import java.util.UUID;

import android.util.Log;

/**
 * �����ݹ�����
 */
public class SectionFilter {
	final static String TAG = "[java]SectionFilter";

	/** ����ÿ��Section������һ�� */
	public static final int ACCEPT_ONCE = 1;
	/** ����ÿ��Section������һ�Σ�ʹ���������CRC */
	public static final int ACCEPT_ONCE_SOFTCRC = 2;
	/** ��Section�������ʱ���� (Ĭ��) */
	public static final int ACCEPT_UPDATED = 3;
	/** ��Section�������ʱ����(������һ��),�������CRC */
	public static final int ACCEPT_UPDATED_SOFTCRC = 4;
	/** �����Ƿ������,���ǽ���Section */
	public static final int ACCEPT_ALWAYS = 5;

	/** �ڼ��ģʽ��ʹ�ù��������Զ�������Դ����ȡĿ��Ƶ������� */
	public static final int FLAG_MONITOR_MODE = 0x01;
	/** ��ض�����Ƶ����Ч(freq > 0) */
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
	 * �رն�����Դ,֮����󽫲�����ʹ��
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
	 * �������ݴ��������
	 * 
	 * @param l
	 *            ����
	 */
	public void setSectionDisposeListener(SectionDisposeListener l) {
		sdl = l;
	}

	/**
	 * �õ���������С
	 * 
	 * @return ֵ
	 */
	public int getBufferSize() {
		return bufSize;
	}

	/**
	 * �õ���ȡ���ݵ�Ŀ������
	 * 
	 * @see {@link java.util.UUID#toString()}
	 * @return ����ID
	 */
	public String getNetworkUUID() {
		return uuid;
	}

	/**
	 * �����Ƿ���������
	 * <p>
	 * Ĭ�ϲ���Ҫ����
	 * 
	 * @param b
	 *            true��Ҫ���ţ�������Ҫ����
	 */
	public void setCARequired(boolean b) {
		this.caRequired = b;
	}

	/**
	 * �Ƿ���Ҫ����
	 * 
	 * @return true����Ҫ���ţ�������Ҫ����
	 */
	public boolean isCARequired() {
		return caRequired;
	}

	/**
	 * ����Ƶ��ֵ
	 * 
	 * @param f
	 *            Ƶ��ֵ
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
	 * �õ�Ƶ��ֵ
	 * 
	 * @return Ƶ��ֵ
	 */
	public long getFrequency() {
		return frequency;
	}

	/**
	 * ���ó�ʱ
	 * 
	 * @param t
	 *            ��ʱʱ�����,���Ϊ-1����������ʱ
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
	 * �õ���ʱʱ�䣬Ĭ��Ϊ-1
	 * 
	 * @return ��ʱʱ��ֵ
	 */
	public int getTimeout() {
		if (!isMonitorMode())
			return timeout;
		return -1;
	}

	/**
	 * ����Section���ݽ���ģʽ �ο�<br>
	 * <li>ACCEPT_ONCE ����ȡһ��(Ĭ��) <li>ACCEPT_UPDATED ������ʱ��ȡ��������һ��<li>
	 * ACCEPT_ALWAYS ������ȡ,�����Ƿ����仯<br>
	 * ��
	 * 
	 * @param mode
	 *            ģʽ
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
	 * �õ�Section���ݽ���ģʽ
	 * 
	 * @return ģʽ
	 */
	public int getAcceptionMode() {
		return accept;
	}

	/**
	 * ��ǰfilter����table_id��pid��ƥ����
	 * <p>
	 * start ���Զ�ʧЧ����֧�� ACCEPT_ALWAYS��ȡ��ʽ��
	 */
	public void markNoTableCheck() {
		ntc_flag = 1;
	}

	/**
	 * ���������
	 * <p>
	 * ���ڷǼ��ģʽ�����Ŀ��Ƶ�ʲ��ڵ�ǰ״̬��������ֱ��ʧ��.
	 * 
	 * @param pid
	 *            ��PID
	 * @param tableId
	 *            ��ID
	 * @return �ɹ�����true�����򷵻�false
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
	 * ��������
	 * <p>
	 * ���ڷǼ��ģʽ�����Ŀ��Ƶ�ʲ��ڵ�ǰ״̬��������ֱ��ʧ��.
	 * 
	 * @param pid
	 *            ������pid
	 * @param coef
	 *            �ȶ�����
	 * @param mask
	 *            ����
	 * @param excl
	 *            �ȶ�����
	 * @param len
	 *            ����
	 * @return �ɹ�����true�����򷵻�false
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
	 * ֹͣ����
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
	 * �õ�Ŀ���PID
	 * 
	 * @return ֵ
	 */
	public int getStreamPID() {
		return pid;
	}

	/**
	 * ��ȡָ�������Ķε�����
	 * <p>
	 * ����������Ӧ���㹻��ȡ���ݣ������׳��쳣,MPEG2�淶�ж������4K�ֽ�
	 * 
	 * @param buf
	 *            ������
	 * @param off
	 *            ������ƫ����
	 * @param len
	 *            ����������
	 * @return ���ݳ��ȣ�����������򷵻�-1
	 * @throws IndexOutOfBoundsException
	 *             ��������Ļ��������Ȳ���
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
	 * �����ݴ��ü�����
	 */
	public static interface SectionDisposeListener {

		/**
		 * Ŀ���������Ѷ�ʧ,�޷��ٻ������
		 * 
		 * @param f
		 *            ������
		 */
		void onStreamLost(SectionFilter f);

		/**
		 * ����������,��ʱʱ����δ����κ�����
		 * 
		 * @param f
		 *            ������
		 */
		void onReceiveTimeout(SectionFilter f);

		/**
		 * ���յ�����
		 * 
		 * @param f
		 *            ������
		 * @param len
		 *            section����
		 */
		void onSectionRetrieved(SectionFilter f, int len);

	}

	/**
	 * �����ݼ�ؼ�����
	 * <p>
	 * ���͵Ŀ����������������
	 * <li>
	 * ������Ҫ���������ʱ��һЩ�����
	 * <li>���Ƶ��ͬʱ�����Լ�ص�(��Tuner)<br>
	 * ����ϣ����Բ�ͬ��Ƶ�����ò�ͬ�������ӳ� <br>
	 * 
	 */
	public static interface SectionMonitorListener {
		/**
		 * ���ɼ��Ƶ�㷢���仯��Ӧ�ò�ѯʱ<br>
		 * ��Ҫ��Ӧ�ó���ѡ��һ����ص�Ƶ�� <br>
		 * ���δ���ôμ�������ϵͳ��Ĭ�ϼ��0��������Ƶ��
		 * <p>
		 * �����ǰ��Ƶ�������ڼ��֮�£���ô�����ڲ����ĵ�����0λ��
		 * 
		 * @param freqs
		 *            ��ǰ�ɼ�ص�Ƶ���б�
		 * @return Ҫѡ���Ƶ�ʵ�����,-1������˴μ��
		 */
		int onSelectFrequency(long freqs[]);
	}

	SectionMonitorListener sml = null;

	/**
	 * ��Ӷ�Ƶ����ѡȡ������
	 * 
	 * @param l
	 *            ������
	 */
	public void setSectionMonitorListener(SectionMonitorListener l) {
		this.sml = l;
	}

	/**
	 * ��ѯ�ɼ��״̬
	 * <p>
	 * ���ڼ��ģʽ�Ķ��󣬴˷������ѯ�ɼ��Ƶ����Ϣ,�������ص�����SectionMonitorListener.onSelectFrequency
	 */
	public void queryMonitorable() {
		if (isMonitorMode() && sml != null)
			native_mquery();
	}
}
