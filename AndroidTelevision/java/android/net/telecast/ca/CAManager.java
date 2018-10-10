package android.net.telecast.ca;

import java.lang.ref.WeakReference;
import java.util.UUID;

import android.content.Context;
import android.net.telecast.TransportManager;
import android.util.Log;

/**
 * CA������
 */

public class CAManager {
	static final String TAG = "[java]CAManager";
	/** �Ự��������� */
	public static final String PROP_NAME_SESSION_SERVICE = "p_s_serv_n";
	/** ģ������������ */
	public static final String PROP_NAME_VENDER_NAME = "p_vender_n";
	/** ģ�����, ���������ֹ������ݿ��ڵĸ�ģ���¼ */
	public static final String PROP_NAME_MODULE_SN = "p_module_sn";
	/** ��Ȩ��Ϣ���ݿ��URI */
	public static final String PROP_NAME_ENTITLEMENT_URI = "p_ent_uri";
	/** ���� */
	public static final String PROP_NAME_CARD_NUMNER = "p_card_number";
	/** ������ */
	public static final String PROP_NAME_AREA_CODE = "p_area_code";
	/** ������BouquetID */
	public static final String PROP_NAME_ASSOCIATED_BOUQUET_ID = "p_a_bqt_id";
	/** ���Ľ���ͨ������ */
	public static final String PROP_NAME_MAX_DESCRAMBLING_SIZE = "p_max_dnum";
	
	/** ��ID-�����豸���ֵ */
	public static final int ACAMODULEMANAGER_MAX_LOCAL_READER_ID = 999;
	/** ��ID-Զ��1 */
	public static final int ACAMODULEMANAGER_REMOTE_READER_ID1 = 1001;
	/** ��ID-Զ��2 */
	public static final int ACAMODULEMANAGER_REMOTE_READER_ID2 = 1002;
	
	/**
	 * �õ�����ʵ��
	 * 
	 * @param ctx
	 *            �����Ļ���
	 * @return ����
	 */
	public static CAManager createInstance(Context ctx) {
		return new CAManager(ctx);
	}

	private CACardStateListener cacsl;
	private CAModuleStateListener camsl;
	private int readerStatus[] = null;
	private String[] associUUIDs = null;
	private int readerSize = 0;
	private int peer = 0;
	private Context ctx;

	CAManager(Context ctx) {
		this.ctx = ctx;
		native_init(new WeakReference<CAManager>(this));
		int s = native_reader_size();
		readerSize = s;
		readerStatus = new int[s > 0 ? s : 0];
		associUUIDs = new String[readerStatus.length];
		for (int i = 0; i < readerStatus.length; i++) {
			readerStatus[i] = CARD_ABSENT;
		}
	}

	/**
	 * �ͷŶ�����Դ
	 */
	public void release() {
		native_release();
	}

	/**
	 * �õ�������������
	 * 
	 * @return ����
	 */
	public int getCardReaderSize() {
		return readerSize;
	}

	/**
	 * �õ�����ģ���ID������
	 * 
	 * @param networkUUID
	 *            ָ�����磬���Ϊnull�򷵻����������
	 * @return ID������,���û���򷵻�null
	 */
	public int[] getCAModuleIDs(String networkUUID) {
		long m = 0, l = 0;
		if (networkUUID != null) {
			UUID id = UUID.fromString(networkUUID);
			m = id.getMostSignificantBits();
			l = id.getLeastSignificantBits();
		}
		return native_module_ids(m, l);
	}

	/**
	 * ����ָ����CA System ID����ƥ���CAģ��,��������ID
	 * 
	 * @param caSysIDs
	 *            CA System IDֵ
	 * @return ģ��ID,���û���򷵻�-1
	 */
	public int findCAModuleID(String neworkUUID, int caSysIDs) {
		UUID id = UUID.fromString(neworkUUID);
		return native_find_module(id.getMostSignificantBits(), id.getLeastSignificantBits(),
				caSysIDs);
	}

	/**
	 * �õ�������CA System ID������
	 * 
	 * @param moduleId
	 *            ģ��ID
	 * @return ���飬���û���򷵻�null
	 */
	public int[] getCAModuleCASystemIDs(int moduleId) {
		return native_casys_ids(moduleId);
	}

	/**
	 * �õ�CAģ�������UUID
	 * 
	 * @param moduleID
	 *            ģ��ID
	 * @return uuid�ַ���.
	 */
	public String getCAModuleNetworkUUID(int moduleID) {
		long[] ret = native_get_uuid(moduleID);
		if (ret != null)
			return new UUID(ret[0], ret[1]).toString();
		return null;
	}

	/**
	 * �õ�CAģ�������
	 * 
	 * @param moduleID
	 *            ģ��ID
	 * @param name
	 *            ��������
	 * @return ����ֵ�����û���򷵻�null
	 */
	public String getCAModuleProperty(int moduleID, String name) {
		return native_get_prop(moduleID, name);
	}

	/**
	 * ����CAģ�����Ȩ
	 * 
	 * @param moduleId
	 *            ģ��ID
	 * @param productUri
	 *            Ƶ��
	 */
	public void buyCAModuleEntitlement(int moduleId, String productUri) {
		native_buy(moduleId, productUri);
	}

	/**
	 * ����CAģ����ص�Ӧ��,�����û������ɲ���resolveUri��ָ��������
	 * 
	 * @param moduleId
	 *            ģ��ID
	 * @param solveUri
	 *            �����������
	 * @see android.net.telecast.ca.StreamDescrambler.OnDescrambleringListener#onDescramblingError(android.net.telecast.ca.StreamDescrambler
	 *      sd, String resolveUri, String msg)
	 */
	public void enterCAModuleApplication(int moduleId, String solveUri) {
		native_enter(moduleId, solveUri);
	}

	/**
	 * ����CAģ����ص�Ӧ��
	 * 
	 * @param moduleId
	 *            ģ��ID
	 */
	public void enterCAModuleApplication(int moduleId) {
		native_enter(moduleId, null);
	}

	/**
	 * ����CA��������
	 * 
	 * @param l
	 *            ����
	 */
	public void setCACardStateListener(CACardStateListener l) {
		cacsl = l;
	}

	/**
	 * ����CAģ�������
	 * 
	 * @param l
	 *            ����
	 */
	public void setCAModuleStateListener(CAModuleStateListener l) {
		camsl = l;
	}

	/**
	 * ��ѯ��ǰCA״̬��Ϣ
	 * <p>
	 * �ο�CAManagerListener
	 */
	public void queryCurrentCAState() {
		native_query_state();
	}

	/**
	 * CAģ��״̬������
	 */
	public static interface CAModuleStateListener {
		/**
		 * ��ģ�����
		 * 
		 * @param moduleId
		 *            ģ��ID
		 */
		void onModuleAdd(int moduleId);

		/**
		 * ��ģ���Ƴ�
		 * 
		 * @param moduleId
		 *            ģ��ID
		 */
		void onModuleRemove(int moduleId);

		/**
		 * ģ��ת������ǰ̬
		 * 
		 * @param moduleId
		 *            ģ��ID
		 * @param readerId
		 *            ������ID
		 */
		void onModulePresent(int moduleId, int readerId);

		/**
		 * ģ����������ѽ����
		 * 
		 * @param moduleId
		 *            ģ��ID
		 */
		void onModuleAbsent(int moduleId);

		/**
		 * CAģ���б仯
		 * 
		 * @param moduleId
		 *            ģ��ID
		 */
		void onCAChange(int moduleId);
	}

	/**
	 * CA����������
	 */
	public static interface CACardStateListener {
		/**
		 * ������
		 * 
		 * @param readerId
		 *            ������ID
		 */
		void onCardPresent(int readerId);

		/**
		 * ���γ�
		 * 
		 * @param readerId
		 *            ������ID
		 */
		void onCardAbsent(int readerId);

		/**
		 * ���޵����źŻ�巴
		 * 
		 * @param readerId
		 *            ������ID
		 */
		void onCardMuted(int readerId);

		/**
		 * ������
		 * 
		 * @param readerId
		 *            ������ID
		 */
		void onCardReady(int readerId);

		/**
		 * ����֤���
		 * 
		 * @param readerId
		 *            ������ID
		 * @param moduleId
		 *            ��֤��ģ��ID,���Ϊ-1���ʾ��δ���κ�ģ������֤
		 */
		void onCardVerified(int readerId, int moduleId);
	}

	protected void finalize() throws Throwable {
		try {
			release();
		} catch (Exception e) {
		}
		super.finalize();
	}

	void onCallback(int msg, int p1, int p2) {
		CAModuleStateListener l1 = camsl;
		CACardStateListener l2 = cacsl;
		Log.d(TAG, "onCallback msg type:" + msg);
		Log.d(TAG, "onCallback l: " + l1 + " l2: " + l2);
		switch (msg) {
		case CARD_PRESENT:
			if (l2 != null)
				l2.onCardPresent(p1);
			break;
		case CARD_ABSENT:
			if (l2 != null)
				l2.onCardAbsent(p1);
			break;
		case CARD_MUTED:
			if (l2 != null)
				l2.onCardMuted(p1);
			break;
		case CARD_READY:
			if (l2 != null)
				l2.onCardReady(p1);
			break;
		case CARD_VERIFIED:
			if (l2 != null)
				l2.onCardVerified(p1, p2);
			break;
		case CARD_MODULE_ADD:
			if (l1 != null)
				l1.onModuleAdd(p1);
			break;
		case CARD_MODULE_REMOVE:
			if (l1 != null)
				l1.onModuleAdd(p1);
			break;
		case CARD_MODULE_PRESENT:
			if (l1 != null)
				l1.onModulePresent(p1, p2);
			break;
		case CARD_MODULE_ABSENT:
			if (l1 != null)
				l1.onModuleAbsent(p1);
			break;
		case CARD_MODULE_CACHANGE:
			if (l1 != null)
				l1.onCAChange(p1);
			break;
		default:
			Log.w(TAG, "invalid onCallback msg type:" + msg);
			return;
		}
	}

	static final int CARD_PRESENT = 1;
	static final int CARD_ABSENT = 2;
	static final int CARD_MUTED = 3;
	static final int CARD_READY = 4;
	static final int CARD_VERIFIED = 10;
	static final int CARD_MODULE_ADD = 5;
	static final int CARD_MODULE_REMOVE = 6;
	static final int CARD_MODULE_PRESENT = 7;
	static final int CARD_MODULE_ABSENT = 8;
	static final int CARD_MODULE_CACHANGE = 9;

	native void native_init(WeakReference<CAManager> wo);

	native void native_release();

	native int native_reader_size();

	native int[] native_module_ids(long m, long l);

	native int native_find_module(long m, long l, int casid);

	native long[] native_get_uuid(int mid);

	native int[] native_casys_ids(int index);

	native String native_get_prop(int index, String name);

	native void native_query_state();

	native void native_buy(int moduleId, String uri);

	native void native_enter(int moduleId, String resolveUri);

	@SuppressWarnings("unchecked")
	static void nativeCallback(Object o, int msg, int p1, int p2) {
		WeakReference<CAManager> wo;
		CAManager cm;
		Log.i(TAG, "CAManager native_callback");

		if (o == null)
			return;
		try {
			wo = (WeakReference<CAManager>) o;
			cm = wo.get();
			if (cm == null)
				return;
			cm.onCallback(msg, p1, p2);
		} catch (Exception e) {
			Log.e(TAG, "nativeCallback error" + e);
		}
	}

	static {
		init();
	}

	@SuppressWarnings("deprecation")
	static void init() {
		TransportManager.ensure();
	}
}
