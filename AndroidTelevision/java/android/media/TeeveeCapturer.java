package android.media;

import java.io.IOException;
import java.lang.ref.WeakReference;

import android.content.Context;
import android.graphics.Bitmap;
import android.util.Log;

/**
 * ��Ƶ����ץȡ��
 */
public class TeeveeCapturer {
	static final String TAG = "[java]TeeveeCapturer";

	/** ���ͱ�־-Ĭ��(ƽ̨ʵ��ץ�������ݴ�С) */
	public static final int TYPE_DEFAULT = 0;
	/** ���ͱ�־-С���� */
	public static final int TYPE_SMALL = 1;
	/** ���ͱ�־-�л��� */
	public static final int TYPE_MEDIUM = 2;
	/** ���ͱ�־-�󻭷� */
	public static final int TYPE_LARGE = 3;
	/** ���ͱ�־-ȫ���� */
	public static final int TYPE_FULL = 4;

	/** ������־-Ĭ��(ƽ̨Ĭ��ץ�������ݸ�ʽ) */
	public static final int FLAG_DEFAULT = 0;
	/** ������־-ƫ��RGB���� */
	public static final int FLAG_PREPERRED_RGB = 0x01;
	/** ������־-ƫ��YUV���� */
	public static final int FLAG_PREPERRED_YUV = 0x02;
	/** ������־-ƫ��JPG�������� */
	public static final int FLAG_PREPERRED_JPG = 0x04;
	/** ������־-ƫ��PNG�������� */
	public static final int FLAG_PREPERRED_PNG = 0x08;

	/**
	 * ������Ƶ֡ץȡ��
	 * 
	 * @param context
	 *            ������
	 * @param type
	 *            ����
	 * @return �������ʧ�ܷ���null
	 */
	public static TeeveeCapturer createInstance(Context context, int type) {
		return createInstance(context, type, 0);
	}

	/**
	 * ������Ƶ֡ץȡ��
	 * 
	 * @param context
	 *            ������
	 * @param type
	 *            ����
	 * @param flags
	 *            ��־��Ĭ��Ϊ0
	 * @return �������ʧ�ܷ���null
	 */
	public synchronized static TeeveeCapturer createInstance(Context context, int type, int flags) {
		return new TeeveeCapturer(context, type, flags);
	}

	private Context context;
	private int type = 0, flags = 0;
	private CaptureStateListener csl;
	private Object mutex = new Object();
	private int id = -1, peer = 0;
	private boolean released = false;

	/**
	 * �õ�framerץ��ʵ��
	 * 
	 * @return ����
	 */
	TeeveeCapturer(Context context, int type, int flags) {
		this.context = context;
		this.type = type;
		this.flags = flags;
		if (!native_init(new WeakReference<TeeveeCapturer>(this), type, flags))
			throw new RuntimeException("native init failed");
		if (peer == 0)
			throw new RuntimeException("impl error");
	}

	protected void finalize() throws Throwable {
		try {
			release();
		} catch (Throwable e) {
			e.printStackTrace();
		}
		super.finalize();
	}

	/**
	 * �ͷ���Դ
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
	 * �Ƿ����ͷ�
	 * 
	 * @return �Ƿ���true,���򷵻�false
	 */
	public boolean isReleased() {
		return !released;
	}

	/**
	 * �����ץȡframe��idֵ
	 * <p>
	 * 
	 * @return ����Ψһ��ʶidֵ, ���Ϊ0���ʾ����ʧ��
	 */
	public int allocId() {
		if(id != 0)
			return id;
		return (id = native_alloc_id());
	}



	/**
	 * �ͷſ�ץȡframe��idֵ
	 */
	public void cancelId() {
		native_cancel_id();
		id = 0;
	}

	/**
	 * ��ȡ��ץȡframe��idֵ
	 * <p>
	 * 
	 * @return ����Ψһ��ʶidֵ
	 */
	public int getAllocedId() {
		return id;
	}

	/**
	 * ��ץȡ��frame������ת����bitmapλͼ
	 * <p>
	 * 
	 * @param bmp
	 *            λͼ����
	 * @return �Ƿ�ת���ɹ�
	 */
	public boolean dup(Bitmap bmp) {
		return native_dup_bitmap(bmp);
	}

	/**
	 * ����ǰץͼ����ΪͼƬ
	 * 
	 * @param dir
	 *            Ŀ¼
	 * @param name
	 *            �ļ����ƣ���������չ��
	 * @return �ļ���ȫ����
	 * @throws IOException
	 *             �������O�쳣
	 */
	public String save(String dir, String name) throws IOException {
		String ret = native_save_file(dir + "/" + name);
		if (ret == null)
			throw new IOException("save error!");
		return name + "." + ret;
	}

	/**
	 * ����ץȡ��frame��״̬������
	 * 
	 * @param l
	 *            ����
	 */
	public void setCaptureStateListener(CaptureStateListener l) {
		csl = l;
	}

	/**
	 * ״̬������
	 */
	public static interface CaptureStateListener {
		/**
		 * ��ʼʱ�ص�
		 * 
		 * @param id
		 *            ���漰��ID
		 */
		void onCaptureStart(int id);

		/**
		 * ��ʱʱ�ص�
		 * 
		 * @param id
		 *            IDֵ
		 */
		void onCaptureIdTimeout(int id);

		/**
		 * ����ʱ�ص�
		 * 
		 * @param id
		 *            IDֵ
		 * @param err
		 *            ������Ϣ
		 */
		void onCaptureOver(int id, String err);
	}

	static final int MSG_START = 1;
	static final int MSG_TIMEOUT = 2;
	static final int MSG_END = 3;

	void onMessage(int msg, int p1, String p2) {
		CaptureStateListener l = csl;
		Log.d(TAG, "onMessage msg=" + msg + ",p1=" + p1 + ",p2=" + p2);
		if (l == null)
			return;
		switch (msg) {
		case MSG_START:
			l.onCaptureStart((int) p1);
			break;
		case MSG_TIMEOUT:
			l.onCaptureIdTimeout((int) p1);
			break;
		case MSG_END:
			l.onCaptureOver(p1, p2);
			break;
		default:
			break;
		}
	}

	@SuppressWarnings("unchecked")
	static void native_proc(Object o, int msg, int p1, String p2) {
		WeakReference<TeeveeCapturer> wo = null;
		TeeveeCapturer m = null;
		if (o == null)
			return;
		try {
			wo = (WeakReference<TeeveeCapturer>) o;
			if ((m = wo.get()) == null)
				return;
			m.onMessage(msg, p1, p2);
		} catch (Exception e) {
			Log.d(TAG, e.toString());
		}
	}

	private native boolean native_init(WeakReference<TeeveeCapturer> wo, int type, int flags);

	private native void native_release();

	private native int native_alloc_id();

	private native void native_cancel_id();

	private native boolean native_dup_bitmap(Bitmap b);

	private native String native_save_file(String pathWithoutExtName);
}
