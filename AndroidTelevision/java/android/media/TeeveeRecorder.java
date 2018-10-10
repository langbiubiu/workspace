package android.media;

import java.io.FileDescriptor;
import java.lang.ref.WeakReference;

import android.content.Context;
import android.net.telecast.ProgramInfo;
import android.net.telecast.StreamSelector;
import android.net.telecast.TransportManager;
import android.util.Log;

/**
 * ��Ŀ¼����
 */
public class TeeveeRecorder {
	static final String TAG = "[java]TeeveeRecorder";

	/** ������־-Ĭ�� */
	public static final int CREATE_FLAG_DEFAULT = 0;
	/** ������־-¼�Ʊ����Ŀ���ͣ��ο�720*576 */
	public static final int CREATE_FLAG_SD = 0x1;
	/** ������־-¼�Ƹ����Ŀ���ͣ��ο�1280*720 */
	public static final int CREATE_FLAG_HD = 0x2;
	/** ������־-¼��ȫ�����Ŀ���ͣ��ο�1920*1080 */
	public static final int CREATE_FLAG_FHD = 0x4;
	/** ������־-¼�Ƴ������Ŀ���ͣ��ο�3840*2160(4K��2K) */
	public static final int CREATE_FLAG_UHD = 0x8;

	/** ������־-�Ƿ���Ҫ����ת�� */
	public static final int CREATE_FLAG_TRANSCODE = 0x10;
	/** ��־-¼�Ʊ��ؼ�������(��3DES/DES),ϵͳ������Ƶ����Ƶͨ�������ͬʱ���� */
	public static final int CREATE_FLAG_NATIVE_ENCRYPTION = 0x20;
	/** ��־-¼�Ƽ����� */
	public static final int CREATE_FLAG_DESCRAMBLER = 0x40;

	/** ��־-Ĭ�� */
	public static final int SELECT_FLAG_DEFAULT = 0;
	/** ��־-��Ŀ���ż��PSI��Ϣ,����Ĭ�Ͻ�������PSI�ı仯,�Լ���Ӧ�Ĵ��� */
	public static final int SELECT_FLAG_OBSERVE_PSI = 0x01;

	/** ��־-Ĭ�� */
	public static final int SET_FLAG_DEFAULT = 0;

	/** ����ѡ��һ���������滻֮ǰ��ѡ��(����Ƶ�ʣ��ļ��Լ���) {@link #select(FrequencyInfo, int)} */
	public static FileDescriptor EMPTY_STREAM = new FileDescriptor();
	
	Context context;
	private Object mutex = new Object();
	private int peer = 0;
	private boolean released = false;

	/**
	 * �õ�¼����ʵ��
	 * 
	 * @return ����
	 */
	public static TeeveeRecorder createTeeveeRecorder(Context ctx) {
		return createTeeveeRecorder(ctx, 0);
	}

	/**
	 * ����¼����ʵ��
	 * 
	 * @param ctx
	 *            ������
	 * @param flags
	 *            ��־��Ĭ��Ϊ0
	 * @return ����
	 */
	public static TeeveeRecorder createTeeveeRecorder(Context ctx, int flags) {
		TeeveeRecorder tr = createInner(ctx);
		if (tr != null)
			if (tr.initPeer(flags))
				return tr;
		return null;
	}

	static TeeveeRecorder createInner(Context ctx) {
		String className = null;
		try {
			if ((className = TransportManager.getSystemProperty("android.media.TeeveeRecorder"))
					.equals("")) {
				return new TeeveeRecorder(ctx);
			} else {// ���ƽ̨������ʵ����ʹ������ʵ��.
				return (TeeveeRecorder) Class.forName(className)
						.getMethod("createTeeveeRecorder", Context.class).invoke(null, ctx);
			}
		} catch (Exception e) {
			Log.e(TAG, "create createTeeveeRecorder(" + className + ") failed:" + e);
		}
		return null;
	}

	protected TeeveeRecorder(Context ctx) {
		this.context = ctx;
	}

	// ֮���Ե�������һ��initPeer����,��Ϊ���ⷴ��ʽ���������ʧ��(���캯���������Ӳ���)��
	// Ϊ�˴������flags������Ŀ��,������Ϻ��ٵ��ô˺�����
	private boolean initPeer(int flags) {
		if (!native_init(new WeakReference<TeeveeRecorder>(this), flags))
			throw new RuntimeException("native init failed");
		if (peer == 0)
			throw new RuntimeException("impl error");
		return true;
	}

	/**
	 * ׼��¼����������Դ
	 * 
	 * @return �ɹ�����true,���򷵻�false
	 */
	public boolean prepare() {
		return native_prepare();
	}

	/**
	 * �ͷ���Դ���˺�����ٿ���
	 */
	public void release() {
		synchronized (mutex) {
			if (!released) {
				released = true;
				native_release();
			}
		}
	}

	/**
	 * ��ʼ����ȡ�����Դ��׼��¼��
	 * <p>
	 * ����Ѿ�����start״̬��ֱ�ӷ���
	 */
	public boolean start() {
		return native_start();
	}

	/**
	 * ֹͣ���ͷ���Դ��ֹͣ¼��
	 */
	public void stop() {
		native_stop();
	}

	/**
	 * ��ͣ¼��
	 * <p>
	 * 
	 * @return ���򷵻�true�����򷵻�false
	 */
	public boolean pause() {
		return native_pause();
	}

	/**
	 * �ָ�¼��
	 */
	public void resume() {
		native_resume();
	}

	/**
	 * ��������Դ
	 * 
	 * @param selector
	 *            ����Դ����
	 * @return �ɹ�����true,���򷵻�false
	 */
	public boolean setDataSource(StreamSelector selector) {
		return setDataSource(selector, SET_FLAG_DEFAULT);
	}

	/**
	 * ��������Դ
	 * 
	 * @param selector
	 *            ����Դ����
	 * @param flags
	 *            ��־��Ĭ��Ϊ0
	 * @return �ɹ�����true,���򷵻�false
	 */
	public boolean setDataSource(StreamSelector selector, int flags) {
		if ((src != null && selector == null) || (src == null && selector != null)) {
			src = selector; // StreamSelector�������ͷ���native_set_data_sourceʧ�ܣ���֤��ֵ��ȷ
			if (native_set_data_source(selector, flags)) {
				return true;
			}
		}
		return false;
	}

	StreamSelector src = null;

	/**
	 * �õ�֮ǰ���õ�����Դ
	 * 
	 * @return ����,û���򷵻�null
	 */
	public StreamSelector getDataSource() {
		return src;
	}

	void checkPeer() {
		if (peer == 0)
			throw new IllegalStateException("not reserve!");
	}

	/**
	 * ¼��ָ���Ľ�Ŀ��Ϣ�����ļ���
	 * 
	 * @param fd
	 *            ���ļ����(�ܵ����׽���)
	 * @param pi
	 *            ��Ŀ��Ϣ
	 * @param flags
	 *            ��־��Ĭ��Ϊ0
	 * @return �ɹ��򷵻�true,���򷵻�false
	 */
	public boolean selectProgram(FileDescriptor fd, ProgramInfo pi, int flags) {
		synchronized (mutex) {
			checkPeer();
			if (fd == EMPTY_STREAM || fd == null) {
				uri = "fd://" + (-1);
				fd = EMPTY_STREAM;
			} else {
				uri = "fd://" + fd.toString();// ˳������null pointer���
			}
			return selectProgram(fd, pi, null, flags);
		}
	}

	/**
	 * ����¼�Ƶ�ƫ�ò���
	 * 
	 * @param ti
	 *            ƫ�ò���
	 * @param flags
	 *            Ĭ��Ϊ0
	 * @return
	 */
	public void setPreference(TeeveeRecordPreference ti) {
		synchronized (mutex) {
			checkPeer();
			mPreference = ti;
		}
	}

	TeeveeRecordPreference mPreference = null;
	String preference = null;

	/**
	 * ��ȡ¼�Ƶ�ƫ�ò���
	 */
	public TeeveeRecordPreference getPreference() {
		if (preference != null)
			return TeeveeRecordPreference.fromQueryString(preference);
		return null;
	}

	/**
	 * ¼��һ��PID����Ӧ���������ļ���
	 * 
	 * @param fd
	 *            ���ļ����(�ܵ����׽���)
	 * @param pi
	 *            ��Ŀ��Ϣ
	 * @param pids
	 *            pid����
	 * @param flags
	 *            ��־��Ĭ��Ϊ0
	 * @return �ɹ��򷵻�true,���򷵻�false
	 */
	public boolean selectProgram(FileDescriptor fd, ProgramInfo pi, int[] pids, int flags) {
		synchronized (mutex) {
			checkPeer();
			if (pi == null && pids.length <= 0)
				throw new RuntimeException("selectProgram11>pids is null pointer");
			if (fd == EMPTY_STREAM || fd == null) {
				uri = "fd://" + (-1);
				fd = EMPTY_STREAM;
			} else {
				uri = "fd://" + fd.toString();// ˳������null pointer���
			}
			String s = uri = pi.toString();
			s += mPreference != null ? mPreference.toQueryString() : "";
			return native_select(fd, s, pids, flags);
		}
	}

	/**
	 * ¼��ָ���Ľ�Ŀ��Ϣ���������
	 * 
	 * @param fd
	 *            ���ļ����(�ܵ����׽���)
	 * @param pi
	 *            ��Ŀ��Ϣ
	 * @param flags
	 *            ��־��Ĭ��Ϊ0
	 * @return �ɹ��򷵻�true,���򷵻�false
	 */
	public boolean selectProgram(FileDescriptor fd, int off, int len, ProgramInfo pi, int flags) {
		synchronized (mutex) {
			checkPeer();
			return selectProgram(fd, off, len, pi, null, flags);
		}
	}

	/**
	 * ¼��һ��PID����Ӧ�����������
	 * 
	 * @param fd
	 *            ���ļ����(�ܵ����׽���)
	 * @param pi
	 *            ��Ŀ��Ϣ
	 * @param flags
	 *            ��־��Ĭ��Ϊ0
	 * @return �ɹ��򷵻�true,���򷵻�false
	 */
	public boolean selectProgram(FileDescriptor fd, long off, long len, ProgramInfo pi, int[] pids,
			int flags) {
		synchronized (mutex) {
			checkPeer();
			if (pi == null && pids.length <= 0)
				throw new RuntimeException("selectProgram22>pids is null pointer");
			if (fd == EMPTY_STREAM || fd == null) {
				uri = "fd://" + (-1);
				fd = EMPTY_STREAM;
			} else {
				uri = "fd://" + fd.toString();// ˳������null pointer���
			}
			String s = uri = pi.toString();
			s += mPreference != null ? mPreference.toString() : "";
			return native_select(fd, off, len, s, pids, flags);
		}
	}

	public String getRecordUri() {
		return uri;
	}

	String uri;

	OnRecordStateListener rsl = null;

	public void setOnRecordStateListener(OnRecordStateListener l) {
		rsl = l;
	}

	/**
	 * ¼��״̬������
	 */
	public static interface OnRecordStateListener {
		/**
		 * ¼�ƿ�ʼ
		 * 
		 * @param program_number
		 */
		void onRecordStart(int program_number);

		/**
		 * ¼�ƴ���
		 * <p>
		 * ֮��¼�ƽ���ֹͣ�����Ҫȷ������player�ٹ�����Ӧ�ó����Ҫ����ִ��prepare��һϵ�в���.
		 * 
		 * @param program_number
		 *            ��selectProgram�еĲ�����ָ��,���δָ����Ϊ0
		 * @param msg
		 *            ������Ϣ�ַ���
		 */
		void onRecordError(int program_number, String msg);

		/**
		 * ¼�����
		 * <p>
		 * ֮��¼�ƽ���ֹͣ�����Ҫȷ������player�ٹ�����Ӧ�ó����Ҫ����ִ��prepare��һϵ�в���.
		 * 
		 * @param program_number
		 *            ��selectProgram�еĲ�����ָ��,���δָ����Ϊ0
		 */
		void onRecordEnd(int program_number);
	}

	private ProgramStateListener psl;

	/**
	 * ��ӽ�Ŀ״̬������
	 * 
	 * @param l
	 *            ����
	 */
	public void setProgramStateListener(ProgramStateListener l) {
		psl = l;
	}

	/**
	 * ��Ŀ״̬������
	 * <p>
	 * �����ϣ��ϵͳ���Ĳ��ŵ������߽��ô���Ϣ,�ο�
	 * {@link android.media.TeeveePlayer#FLAG_NOT_OBSERVE_PSI}
	 */
	public static interface ProgramStateListener {
		/**
		 * Ҫ������ѡ���Ŀ
		 * <p>
		 * ��һ������Ϊ��Ŀ��������仯�����<br>
		 * uri�ο�{@link android.net.telecast.ProgramInfo#fromString(String)}
		 * 
		 * @param program_number
		 *            ��Ŀ��
		 * @param newuri
		 *            ����ѡ���uri
		 */
		void onProgramReselect(int program_number, String newuri);

		/**
		 * ��Ŀ�Ѿ��Ƴ�
		 * 
		 * @param program_number
		 *            ��Ŀ��
		 */
		void onProgramRemoved(int program_number);
	}

	static final int REC_ERROR = 1;
	static final int REC_END = 2;
	static final int REC_START = 3;
	static final int P_RESELECT = 8;
	static final int P_REMOVED = 9;

	void onMessage(int msg, int p1, String s) {
		OnRecordStateListener l = rsl;
		ProgramStateListener l2 = psl;

		switch (msg) {
		case REC_START:
			preference = s;
			if (l != null)
				l.onRecordStart(p1);
			break;
		case REC_ERROR:
			if (l != null)
				l.onRecordError(p1, s);
			break;
		case REC_END:
			if (l != null)
				l.onRecordEnd(p1);
			break;
		case P_RESELECT:
			if (l2 != null)
				l2.onProgramReselect(p1, s);
			break;
		case P_REMOVED:
			if (l2 != null)
				l2.onProgramRemoved(p1);
			break;
		default:
			return;
		}
	}

	@SuppressWarnings("unchecked")
	static void native_proc(Object o, int msg, int p1, String info) {
		WeakReference<TeeveeRecorder> wo = null;
		TeeveeRecorder m = null;
		if (o == null)
			return;
		try {
			wo = (WeakReference<TeeveeRecorder>) o;
			if ((m = wo.get()) == null)
				return;
			m.onMessage(msg, p1, info);
		} catch (Exception e) {
			Log.d(TAG, e.toString());
		}
	}

	native boolean native_init(WeakReference<TeeveeRecorder> wo, int flags);

	native void native_release();

	native boolean native_prepare();

	native boolean native_start();

	native void native_stop();

	native boolean native_pause();

	native void native_resume();

	native boolean native_set_data_source(StreamSelector src, int flags);

	native boolean native_select(FileDescriptor fd, long off, long len, String s, int[] pids,
			int flags);

	native boolean native_select(FileDescriptor fd, String s, int[] pids, int flags);
}
