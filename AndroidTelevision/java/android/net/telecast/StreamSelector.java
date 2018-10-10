package android.net.telecast;

import java.io.FileDescriptor;
import java.lang.ref.WeakReference;
import java.util.UUID;

import android.util.Log;

/**
 * �����źŴ�����ѡ����
 * <p>
 * �������ߵ�г����⸴�����Ĺ���(Tuner,Demux)
 * <p>
 * ����������������<br>
 * 1 ѡ��Ƶ�ʻ�����������Ϊ����Դ,����������<br>
 * 2 Ϊ�����������������������ID��ʶ,����ʶ����<br>
 * ���ڵڶ��㣬�������Ե�Ӱ�죬����������IDΪ�����������������ַ���ϵ��������ȡ���ݵ�SectionFilter��
 * �������ݵ�StreamDescrambler��
 */
public class StreamSelector {
	final String TAG = "[java]StreamSelector";

	/** ����-Ĭ�� */
	public static final int CREATE_FLAG_DEFAULT = 0;
	/** ����-��Tunerģʽ��Ҳ֧���������� */
	public static final int CREATE_FLAG_PUSHABLE_ALSO = 0x1;
	/** ����-֧�ֵȴ�ģʽ */
	public static final int CREATE_FLAG_NO_RETRY = 0x4;
	/** ����-֧�����ʹ���������(Ҫ��Ϊpushģʽ������CREATE_FLAG_PUSHABLE_ALSO) */
	public static final int CREATE_FLAG_PUSH_HIGH_FLOW = 0x8;

	/** Ĭ�� */
	public static final int SELECT_FLAG_DEFAULT = 0;
	/** �����Ƶʧ���Զ��ظ����� */
	public static final int SELECT_FLAG_RETRY = 0x1;
	/** ǿ��������Ƶ,����FrequencyInfo��Ч */
	public static final int SELECT_FLAG_FORCE = 0x2;
	/** ѭ���ظ���������,�����ļ����͵�FileDescriptor��Ч */
	public static final int SELECT_FLAG_REPEAT = 0x4;
	/** ����׼��,����Դ�п���ϵͳ�����Ԥ��,����FrequencyInfo��Ч */
	public static final int SELECT_FLAG_ADVISE = 0x8;

	/** ��������־-Ĭ�� */
	public static final int RECEIVE_FLAG_DEFAULT = 0;
	/** ��������־-���� */
	public static final int RECEIVE_FLAG_GROUPED = 0x1;
	/** ��������־-���ؼ��� */
	public static final int RECEIVE_FLAG_NATIVE_ENCRYPTION = 0x2;
	/** ��������־-�������ԣ�����ʵ�ʽ����� */
	public static final int RECEIVE_FLAG_TEST_CAPABILITY = 0x4;

	/** ����ѡ��һ���������滻֮ǰ��ѡ��(����Ƶ�ʣ��ļ��Լ���) {@link #select(FrequencyInfo, int)} */
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
	 * �õ�����ӿ�ID
	 * 
	 * @return ֵ
	 */
	public int getNetworkInterfaceId() {
		return interfaceId;
	}

	/**
	 * �ͷ���Դ
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
	 * �����Ƿ����ͷ�
	 * 
	 * @return �Ƿ���true,���򷵻�false
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
	 * �õ��ź�״̬
	 * 
	 * @return �ź�״̬
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
	 * ��������ID<br>
	 * ����������ȡ�Ȳ������йؼ�ѡ������á�ͬ����ĺ����������ڵ�ᱻ����(����SectionFilter).
	 * <p>
	 * ����һ��select��Ч.<br>
	 * 
	 * @param id
	 *            ID , �ο�<code>java.util.UUID.toString();</code>
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
	 * �趨����Ƶ�ʣ�0��ʾ������
	 * 
	 * @param freq
	 *            Ϊ��ʽ����Դ���ļ�����Դ,����Ϊ��ֵ
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
	 * ѡ��Ƶ��
	 * 
	 * @param fi
	 *            Ƶ��
	 * @param flags
	 *            ��־,Ĭ���봫��0
	 * @return �������󷵻�false�����򷵻�true
	 */
	public boolean select(FrequencyInfo fi, int flags) {
		synchronized (mutex) {
			checkPeer();
			return native_select_freq((uri = fi.toString()), flags);
		}
	}

	/**
	 * ������Դ������ģʽ
	 * <p>
	 * <li>����Ϊtrueʱ��Ϊ������ģʽ����ʱ��Դ���ܻᱻϵͳ�ջأ����رն�������¸Ļ�ǿ����ģʽ��,��Ӧ����������
	 * <li>����Ϊfalseʱ�Ļ�ǿ����ģʽ��������ʧ�ܣ���˵����Դ�ѱ����ᣬӦ�ùرն���.
	 * 
	 * @return �ɹ�����true,���򷵻�false
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
	 * ����demux�Ļ���
	 * @return �ɹ�����true,���򷵻�false
	 */
	public boolean clear() {
		synchronized (mutex) {
			return native_clear();
		}
	}

	/**
	 * �Ƿ�Ϊ������ģʽ
	 * 
	 * @return ���򷵻�true,���򷵻�false
	 */
	public boolean isWeakMode() {
		return weak;
	}

	/**
	 * ѡ���ļ���ΪƵ������Դ
	 * 
	 * @param fd
	 *            �ļ�FD
	 * @param off
	 *            �ļ�ƫ�Ƶ�ַ
	 * @param len
	 *            �ļ�����
	 * @param flags
	 *            ��־,Ĭ���봫��0
	 * @return �������󷵻�false�����򷵻�true
	 */
	public boolean select(FileDescriptor fd, long off, long len, int flags) {
		synchronized (mutex) {
			checkPeer();
			if (fd == EMPTY_STREAM || fd == null) {
				uri = "fd://" + (-1);
				fd = EMPTY_STREAM;
			} else {
				uri = "fd://" + fd.toString();// ˳������null pointer���
			}
			return native_select_file(fd, off, len, flags);
		}
	}

	/**
	 * ѡ������ΪƵ������Դ
	 * <p>
	 * �������׽������������߹ܵ���������
	 * 
	 * @param fd
	 *            �ļ�FD ��Ƶ�ķ���
	 * @param flags
	 *            ��־,Ĭ���봫��0
	 * @return �������󷵻�false�����򷵻�true
	 */
	public boolean select(FileDescriptor fd, int flags) {
		synchronized (mutex) {
			checkPeer();
			if (fd == EMPTY_STREAM || fd == null) {
				uri = "fd://" + (-1);
				fd = EMPTY_STREAM;
			} else {
				uri = "fd://" + fd.toString();// ˳������null pointer���
			}
			return native_select_stream(fd, flags);
		}
	}

	/**
	 * �õ���ǰ���ڵ�г�����ѵ�г��Ƶ��ֵ�����򷵻�0
	 * 
	 * @return Ƶ��ֵ
	 */
	public long getCurrentFrequency() {
		return freq;
	}

	/**
	 * ����PID����Ӧ��������
	 * 
	 * @param pid
	 *            ����PID
	 * @param fd
	 *            �ļ���������Ӧ������ʽ���(�ܵ������׽���)
	 * @param flags
	 *            ��־��Ĭ��Ϊ0
	 * @return �ɹ��򷵻�true�����򷵻�false
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
	 * ����һ��PID����Ӧ����
	 * 
	 * @param pids
	 *            pid����
	 * @param fd
	 *            �ļ���������Ӧ������ʽ���(�ܵ������׽���)
	 * @param flags
	 *            ��־��Ĭ��Ϊ0
	 * @return �ɹ��򷵻�true�����򷵻�false
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
	 * ���ü�����
	 * 
	 * @param l
	 *            ����
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
	 * ��г������
	 */
	public static interface SelectionStateListener {
		/**
		 * ������Ƶ���ѿ�ʼ��г
		 * <p>
		 * ����ζ�ź�������Ϣ������Դ�Ƶ�ʵ�,ֱ���ٴ��յ�����ϢΪֹ
		 * 
		 * @param selector
		 * 
		 */
		void onSelectStart(StreamSelector selector);

		/**
		 * ѡ��ɹ�
		 * 
		 * @param selector
		 */
		void onSelectSuccess(StreamSelector selector);

		/**
		 * ѡ��ʧ��
		 * 
		 * @param selector
		 */
		void onSelectFailed(StreamSelector selector);

		/**
		 * Ŀ�궪ʧ
		 * 
		 * @param selector
		 */
		void onSelectionLost(StreamSelector selector);

		/**
		 * Ŀ�����»�ȡ
		 * 
		 * @param selector
		 */
		void onSelectionResumed(StreamSelector selector);

	}
}
