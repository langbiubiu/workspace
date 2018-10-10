package android.net.telecast.dvb;

import java.io.FileDescriptor;
import java.lang.ref.WeakReference;
import java.util.UUID;

import android.util.Log;

/**
 * �ֲ�������
 * <p>
 * Ӧ�ó���Ӧʹ�ô˶�������ֲ����ݽ��ա����Ӧ�ó���ʹ��SectionFilter����ʵ������߼���<br>
 * �������·ͬ����SecionFilter���������µ�Ч�ʵ������⡣����������ĳЩ��Ҫ�������������صĳ����������ġ�
 * 
 * @hide
 */
public class CarouselReceiver {
	static final String TAG = "[java]CarouselReceiver";

	/** ������־ - Ĭ�� */
	public static final int CREATE_FLAG_DEFAULT = 0;
	/** ������־-ȫ����أ����� DII �� DSI ���� */
	public static final int CREATE_FLAG_ALL_MONITOR = 0;
	/** ������־ - ������DII�仯��ֻ����������Module����ʽ */
	public static final int CREATE_FLAG_NOT_OBSERVE_DII = 0x1;

	/** ����Gateway���ļ������� */
	public static final int GATEWAY_FILE_INDEX = 0;

	private ReceiverStateListener rsl;
	private String uuid = null;
	boolean running = false;
	private int maxFileSize = 2, peer = 0;
	private boolean isAttached = false, isRelased = false;;

	/**
	 * @deprecated ˽�к����������
	 * @hide �ǹ�������������� ��ͨ��TransportManager.createCarouselReceiver()��ȡ��
	 * 
	 * @param UUID
	 *            ����UUID
	 * @param maxFileSize
	 *            �ļ������������е�DII��Module�Ĵ洢����ֱ�Ӵ�����ļ��С�
	 * 
	 * @param bufsize
	 *            filter�Ļ����С
	 * @param flags
	 *            0:�Զ����DSI/DII�İ汾�仯; 1:������DII�仯��ֻ����������Module����ʽ��
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
	 * �õ�����UUID
	 * 
	 * @return id�ַ���
	 */
	public String getNetworkUUID() {
		return this.uuid.toString();
	}

	protected void finalize() throws Throwable {
		release();
		super.finalize();
	}

	/**
	 * �õ����Ľ����ļ�����
	 * 
	 * @return ֵ
	 */
	public int getMaxReceivingFileSize() {
		return maxFileSize;
	}

	/**
	 * �ͷ���Դ
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
	 * ��������Դ
	 * 
	 * @param freq
	 *            Ƶ��
	 * @param pid
	 *            ��PID
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
	 * �������Դ�Ĺ��� �ر�����
	 */
	public void detach() {
		synchronized (uuid) {
			isAttached = false;
			native_detach();
		}
	}

	/**
	 * �Ƿ��ѹ��������ط���
	 * 
	 * @return �Ƿ���true,���򷵻�false
	 */
	public boolean isAttached() {
		return isAttached;
	}

	/**
	 * ����Gateway�Ľ����ļ�--DII��section��Ϣ����section_number������롣
	 * 
	 * @param fd
	 *            �ļ����
	 */
	public void setGatewayFile(FileDescriptor fd) {
		synchronized (uuid) {
			setReceivingFile(GATEWAY_FILE_INDEX, fd);
		}
	}

	/**
	 * ���ý����ļ��ľ��
	 * 
	 * @param index
	 *            �ļ�����
	 * @param fd
	 *            ��Ӧ���ļ����
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
	 * ͬ��Gateway���ݵ�����
	 * <p>
	 * �����µ�DII��Ϣͬ����setGatewayFile(FileDescriptor fd) ָ����fd�С�
	 */
	public void syncGateway() {
		synchronized (uuid) {
			native_syncg();
		}
	}

	/**
	 * ����ģ������
	 * 
	 * @param moduleId
	 *            ģ��ID
	 * @param fileIndex
	 *            �ļ�����
	 * @param offset
	 *            д���ļ���ƫ����
	 * @param offset
	 *            ��ȡģʽ�� ��������ǰ���module�����еģ�һ��module����һ���ļ���
	 *            ���Զ��module����ͬһ���ļ���ʹ��offset������
	 * 
	 */
	public void receiveModule(int moduleId, int fileIndex, int offset) {
		synchronized (uuid) {
			native_recvm(moduleId, fileIndex, offset);
		}
	}

	/**
	 * ���ģ��İ汾
	 * 
	 * @param moduleId
	 *            ģ��ID
	 * @param currentVersion
	 *            ��ǰ�İ汾
	 */
	public void observeModule(int moduleId, int currentVersion) {
		synchronized (uuid) {
			native_obsm(moduleId, currentVersion);
		}
	}

	/**
	 * ȡ����ģ��Ĳ���
	 * 
	 * @param moduleId
	 *            ģ��ID
	 */
	public void cancelModule(int moduleId) {
		native_cancm(moduleId);
	}

	/**
	 * ���ü�����
	 * 
	 * @param l
	 *            ����
	 */
	public void setReceiverStateListener(ReceiverStateListener l) {
		rsl = l;
	}

	/**
	 * ����״̬������
	 */
	public static interface ReceiverStateListener {
		/**
		 * Gateway�����仯--DII�����仯����֪ͨ�����仯��
		 * ���ȡdii��Ϣ������syncGateway�ӿڡ���DII���е�section��Ϣд���ļ��С� �������DII������Ҫ��һ���Ľ���
		 */
		void onGatewayChanged(int version);

		/**
		 * ģ�鷢���仯
		 * 
		 * @param moduleId
		 *            ģ��ID
		 */
		void onModuleChanged(int moduleId, int version);

		/**
		 * ����ģ�����
		 * 
		 * @param moduleId
		 *            ģ��ID
		 * @param success
		 *            �ɹ�Ϊtrue,ʧ��Ϊfalse Module���ճɹ�
		 *            <p>
		 *            ��Ҫ������һ���Ľ������������õ��������ļ���Ϣ��
		 */
		void onModuleReceiveOver(int moduleId, boolean success);

		/**
		 * �ֲ�����������ͣ����
		 */
		void onReceiverPaused();

		/**
		 * �ֲ��������ѻָ�����
		 */
		void onReceiverResumed();

		/**
		 * �ֲ��������ѹر�
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
