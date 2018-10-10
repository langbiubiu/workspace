package android.net.telecast.ca;

import java.lang.ref.WeakReference;
import java.util.UUID;

import android.content.Context;
import android.net.telecast.TransportManager;
import android.os.ParcelFileDescriptor;
import android.util.Log;

/**
 * CAģ�������
 */
public class CAModuleManager {
	/** �Ƚ�ECM���ݸ���������CRCֵ����������㷽ʽ������ȡ�Զ������� */
	public static final int FLAG_ECM_SOFT_CRC = 0x01;
	/** ����ָ�����ܿ�ͨѶ���� transfer ģʽ���ϲ㲻����Э���߼� */
	public static final int FLAG_RESET_MODE_TRANSFER = 0;
	/** ����ָ�����ܿ�ͨѶ����Э����read/write������Э���߼� */
	public static final int FLAG_RESET_MODE_READ_WRITE = 0x01;
	/** ���ܿ�д��ָ��-��ʱ�趨��������ͷ4���ֽ�Ϊ��ʱʱ�� */
	public static final int FLAG_WRITE_CMD_TIMEOUT = 0x01;
	/** ������־-��Զ���豸��Ч */
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
	 * ����CAģ�����
	 * 
	 * @param ctx
	 *            �����Ļ���
	 * @param networkUUID
	 *            ����UUID
	 * @param maxConDesSize
	 *            ��󲢷���������
	 * @return ����ʧ���򷵻�null
	 */
	public static CAModuleManager createInstance(Context ctx, String networkUUID, int maxConDesSize) {
		if (maxConDesSize < 1 || maxConDesSize > 64) {
			Log.e(TAG, "maxDescTaskSize:" + maxConDesSize + " invalid");
		}
		return new CAModuleManager(networkUUID, maxConDesSize);
	}

	/**
	 * CAģ��ӿ�
	 */
	public static interface CAModuleInterface {
		/**
		 * ��caģ�鱻�л�����ǰ״̬
		 * 
		 * @param readerId
		 *            ������Id
		 */
		void onPresent(int readerId);

		/**
		 * ��ģ�鱻ת��Ϊ�ǵ�ǰ״̬ʱ
		 */
		void onAbsent();

		/**
		 * ��Ҫ����������
		 * <p>
		 * ���ڽ�������,��ȡֵ�ķ�Χ��[0-maxDescTaskSize),maxDescTaskSize������ο�
		 * {@link android.net.telecast.ca.CAModuleManager#setCAModuleInterface(CAModuleInterface, int[])}
		 * 
		 * @param taskIndex
		 *            ��������
		 * @param freq
		 *            Ƶ��
		 * @param programNumber
		 *            ��Ŀ��
		 * @param streamPID
		 *            Ҫ���Ƶ�����PID
		 * @param flags
		 *            ��־��Ĭ��Ϊ0
		 */
		void onStartDescrambling(int taskIndex, long freq, int programNumber, int streamPID,
				int ecmpid, int flags);

		/**
		 * Ҫ��ֹͣ����
		 * 
		 * @param taskIndex
		 *            ��������
		 */
		void onStopDescrambling(int taskIndex);

		/**
		 * ��ȡ��ECM���ݻ���ECM���ݷ����仯
		 * <p>
		 * ������ECM�Զ����ղ���ʱ��Ч
		 * 
		 * @param taskIndex
		 *            ��������
		 * @param buf
		 *            ���ݻ�����
		 */
		void onDescramblingEcmUpdate(int taskIndex, byte[] buf);

		/**
		 * ������Ȩ
		 * 
		 * @param uri
		 *            ��Ʒ��uri
		 */
		void onBuyEntitlement(String uri);

		/**
		 * �������CAģ���������ĳ���
		 * <p>
		 * resolveUriͨ����
		 * {@link CAModuleManager#notifyDescramblingError(int, String, String)}
		 * �Ĳ������ݸ�Ӧ�ó���Ӧ�ó��������ˣ������ؽ��Ŵ��������
		 * 
		 * @param resolveUri
		 *            ���Ϊ�������CAģ����������Ĭ�Ͻ��棬�����Խ����ܽ��Uri��ָ���������ҳ�棬
		 */
		void onEnterApplication(String resolveUri);

		/**
		 * Զ�̻ỰFD
		 * <p>
		 * ���CAΪ�Ǳ���CAʱ�����ɴ���Ϣ֪ͨͨ�ŵľ��,����Դ�֪ͨ������Ҫʹ�����µ�
		 * <p>�����ҪӦ�ó�������dup,��������������ʧЧ
		 * @param fd
		 *            ���
		 */
		void onRemoteSessionFd(ParcelFileDescriptor fd);
	}

	private CAModuleManager(String sid, int cs) {
		UUID id = UUID.fromString(sid);
		netid = id.toString();
		native_create(id.getMostSignificantBits(), id.getLeastSignificantBits(), cs);
	}

	/**
	 * �ͷ������Դ
	 */
	public void release() {
		native_release();
	}

	/**
	 * ���ù������������ģ��Ĳ����ӿ�
	 * 
	 * @param mif
	 *            �ӿڶ���
	 * @param caSysIDs
	 *            caģ���System IDs����
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
	 * ����ECM���ղ���
	 * <p>
	 * depth ������Ϊ0����1,��ʾ����/�����Զ�ECM���ղ��� <br>
	 * ����������Զ�ECM���գ�����ҪӦ�ó������Լ�����ECM��Ϣ <br>
	 * ��־�ο�����:
	 * <li>ACCEPT_UPDATED_SOFTCRC
	 * 
	 * <br>
	 * �������ã�Ĭ�ϴ���0����.
	 * <p>
	 * 
	 * @param coef
	 *            ���˲��� coef
	 * @param mask
	 *            ���˲��� mask
	 * @param excl
	 *            ���˲��� excl
	 * @param depth
	 *            ���(0��1)
	 * @param caTag
	 *            ������TAG
	 * @param flags
	 *            Ĭ��0,�ο�SectionFilter��FLAG
	 */
	public void setEcmFilter(int coef, int mask, int excl, int depth, int caTag, int flags) {
		synchronized (mutex) {
			if (started)
				throw new IllegalStateException("already started!");
			native_set_ecmf((byte) coef, (byte) mask, (byte) excl, (byte) depth, caTag, flags);
		}
	}

	/**
	 * ����CAģ�������
	 * 
	 * @param name
	 *            ����
	 * @param value
	 *            ֵ
	 */
	public void setProperty(String name, String value) {
		native_set_prop(name, value);
	}

	/**
	 * ����������
	 * 
	 * @param flags
	 *            ��־,Ĭ������Ϊ0
	 * @return �ɹ�����true,���򷵻�false
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
	 * ֹͣ������
	 */
	public void stop() {
		synchronized (mutex) {
			if (started)
				started = false;
			native_stop();
		}
	}

	/**
	 * �������ܿ�
	 * <p>
	 * ��Present�ڼ������Ч
	 * <p>
	 * ��־�ο����£�<br>
	 * <li>FLAG_RESET_MODE_READ_WRITE
	 * <li>FLAG_RESET_MODE_TRANSFER <br>
	 * ���������ô���0����
	 * 
	 * @param flags
	 *            ��־��Ĭ�ϴ�0��
	 * @return �ɹ�����true,���򷵻�false
	 */
	public boolean resetCard(int flags) {
		return native_resetcard(flags);
	}

	/**
	 * �õ�����ATR
	 * 
	 * @return atr���飬���ʧ���򷵻�null
	 */
	public byte[] getCardATR() {
		return native_getatr();
	}

	/**
	 * ��������
	 * 
	 * @param b
	 *            ������
	 * @param off
	 *            ƫ����
	 * @param len
	 *            ����
	 * @return ʵ�ʶ�ȡ���ĳ���,���ʧ�ܷ���-1
	 */
	public int readCard(byte[] b, int off, int len) {
		return readCard(b, off, len, 0);
	}

	/**
	 * ��������
	 * 
	 * @param b
	 *            ������
	 * @param off
	 *            ƫ����
	 * @param len
	 *            ����
	 * @param flags
	 *            ��־��Ĭ�ϴ�0
	 * @return ʵ�ʶ�ȡ���ĳ���,���ʧ�ܷ���-1
	 */
	public int readCard(byte[] b, int off, int len, int flags) {
		return native_readcard(b, off, len, flags);
	}

	/**
	 * д������
	 * 
	 * @param b
	 *            ������
	 * @param off
	 *            ƫ����
	 * @param len
	 *            ����
	 * @return ʵ�ʶ�ȡ���ĳ���,���ʧ�ܷ���-1
	 */
	public int writeCard(byte[] b, int off, int len) {
		return writeCard(b, off, len, 0);
	}

	/**
	 * д������
	 * <p>
	 * ��־�ο����£�
	 * <li>FLAG_WRITE_CMD_TIMEOUT <br>
	 * ���������ã�����0����
	 * <p>
	 * 
	 * @param b
	 *            ������
	 * @param off
	 *            ƫ����
	 * @param len
	 *            ����
	 * @param flags
	 *            ��־
	 * @return ʵ�ʶ�ȡ���ĳ���,���ʧ�ܷ���-1
	 */
	public int writeCard(byte[] b, int off, int len, int flags) {
		return native_writecard(b, off, len, flags);
	}

	/**
	 * ֪ͨ��֤���
	 * <p>
	 * �ο�{@link CAModuleInterface#onPresent(int)},ͨ���˺���֪ͨ���������ѱ���֤
	 * 
	 * @param result
	 *            ��֤ͨ����Ϊtrue,������Ϊfalse
	 */
	public void notifyVerification(boolean result) {
		native_notify_veri(result);
	}

	/**
	 * ���½��ſ�����
	 * 
	 * @param taskIndex
	 *            ��������
	 * @param odd
	 *            ��
	 * @param even
	 *            ż
	 * @param len
	 *            ����
	 */
	public void updateControlWord(int taskIndex, byte[] odd, byte[] even, int len) {
		native_update_cw(taskIndex, odd, even, len);
	}

	/**
	 * ֪ͨ���Ŵ���
	 * 
	 * @param taskIndex
	 *            ������������
	 * @param resolveUri
	 *            �������Ļش�Uri,����Ϊnull,��ʾ�޷�ȷ���������
	 * @param errTipsMsg
	 *            ������ʾ��Ϣ,����ʾ���û�
	 */
	public void notifyDescramblingError(int taskIndex, String resolveUri, String errTipsMsg) {
		native_notify_descerr(taskIndex, resolveUri, errTipsMsg);
	}

	/**
	 * ֪ͨ���Ŵ����ѻָ�
	 * 
	 * @param taskIndex
	 *            ������������
	 */
	public void notifyDescramblingResumed(int taskIndex) {
		native_notify_resume(taskIndex);
	}

	/**
	 * ֪ͨ�ɽ���״̬�����仯
	 * <p>
	 * Ӧ�ó���ɾݴ˳��Խ���
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
