package android.net.telecast.ca;

import java.lang.ref.WeakReference;
import java.util.UUID;

import android.content.Context;
import android.net.telecast.TransportManager;
import android.util.Log;

/**
 * CA管理器
 */

public class CAManager {
	static final String TAG = "[java]CAManager";
	/** 会话服务的名称 */
	public static final String PROP_NAME_SESSION_SERVICE = "p_s_serv_n";
	/** 模块制造者名称 */
	public static final String PROP_NAME_VENDER_NAME = "p_vender_n";
	/** 模块序号, 可用于区分共享数据库内的各模块记录 */
	public static final String PROP_NAME_MODULE_SN = "p_module_sn";
	/** 授权信息数据库的URI */
	public static final String PROP_NAME_ENTITLEMENT_URI = "p_ent_uri";
	/** 卡号 */
	public static final String PROP_NAME_CARD_NUMNER = "p_card_number";
	/** 区域码 */
	public static final String PROP_NAME_AREA_CODE = "p_area_code";
	/** 关联的BouquetID */
	public static final String PROP_NAME_ASSOCIATED_BOUQUET_ID = "p_a_bqt_id";
	/** 最大的解扰通道数量 */
	public static final String PROP_NAME_MAX_DESCRAMBLING_SIZE = "p_max_dnum";
	
	/** 卡ID-本地设备最大值 */
	public static final int ACAMODULEMANAGER_MAX_LOCAL_READER_ID = 999;
	/** 卡ID-远程1 */
	public static final int ACAMODULEMANAGER_REMOTE_READER_ID1 = 1001;
	/** 卡ID-远程2 */
	public static final int ACAMODULEMANAGER_REMOTE_READER_ID2 = 1002;
	
	/**
	 * 得到对象实例
	 * 
	 * @param ctx
	 *            上下文环境
	 * @return 对象
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
	 * 释放对象资源
	 */
	public void release() {
		native_release();
	}

	/**
	 * 得到读卡器的数量
	 * 
	 * @return 数量
	 */
	public int getCardReaderSize() {
		return readerSize;
	}

	/**
	 * 得到所有模块的ID的数组
	 * 
	 * @param networkUUID
	 *            指定网络，如果为null则返回所有网络的
	 * @return ID的数组,如果没有则返回null
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
	 * 根据指定的CA System ID查找匹配的CA模块,并返回其ID
	 * 
	 * @param caSysIDs
	 *            CA System ID值
	 * @return 模块ID,如果没有则返回-1
	 */
	public int findCAModuleID(String neworkUUID, int caSysIDs) {
		UUID id = UUID.fromString(neworkUUID);
		return native_find_module(id.getMostSignificantBits(), id.getLeastSignificantBits(),
				caSysIDs);
	}

	/**
	 * 得到关联的CA System ID的数组
	 * 
	 * @param moduleId
	 *            模块ID
	 * @return 数组，如果没有则返回null
	 */
	public int[] getCAModuleCASystemIDs(int moduleId) {
		return native_casys_ids(moduleId);
	}

	/**
	 * 得到CA模块的网络UUID
	 * 
	 * @param moduleID
	 *            模块ID
	 * @return uuid字符串.
	 */
	public String getCAModuleNetworkUUID(int moduleID) {
		long[] ret = native_get_uuid(moduleID);
		if (ret != null)
			return new UUID(ret[0], ret[1]).toString();
		return null;
	}

	/**
	 * 得到CA模块的属性
	 * 
	 * @param moduleID
	 *            模块ID
	 * @param name
	 *            属性名称
	 * @return 属性值，如果没有则返回null
	 */
	public String getCAModuleProperty(int moduleID, String name) {
		return native_get_prop(moduleID, name);
	}

	/**
	 * 购买CA模块的授权
	 * 
	 * @param moduleId
	 *            模块ID
	 * @param productUri
	 *            频率
	 */
	public void buyCAModuleEntitlement(int moduleId, String productUri) {
		native_buy(moduleId, productUri);
	}

	/**
	 * 进入CA模块相关的应用,引导用户处理由参数resolveUri所指定的问题
	 * 
	 * @param moduleId
	 *            模块ID
	 * @param solveUri
	 *            解决方案参数
	 * @see android.net.telecast.ca.StreamDescrambler.OnDescrambleringListener#onDescramblingError(android.net.telecast.ca.StreamDescrambler
	 *      sd, String resolveUri, String msg)
	 */
	public void enterCAModuleApplication(int moduleId, String solveUri) {
		native_enter(moduleId, solveUri);
	}

	/**
	 * 进入CA模块相关的应用
	 * 
	 * @param moduleId
	 *            模块ID
	 */
	public void enterCAModuleApplication(int moduleId) {
		native_enter(moduleId, null);
	}

	/**
	 * 设置CA卡监听器
	 * 
	 * @param l
	 *            对象
	 */
	public void setCACardStateListener(CACardStateListener l) {
		cacsl = l;
	}

	/**
	 * 设置CA模块监听器
	 * 
	 * @param l
	 *            对象
	 */
	public void setCAModuleStateListener(CAModuleStateListener l) {
		camsl = l;
	}

	/**
	 * 查询当前CA状态消息
	 * <p>
	 * 参考CAManagerListener
	 */
	public void queryCurrentCAState() {
		native_query_state();
	}

	/**
	 * CA模块状态监听器
	 */
	public static interface CAModuleStateListener {
		/**
		 * 当模块添加
		 * 
		 * @param moduleId
		 *            模块ID
		 */
		void onModuleAdd(int moduleId);

		/**
		 * 当模块移除
		 * 
		 * @param moduleId
		 *            模块ID
		 */
		void onModuleRemove(int moduleId);

		/**
		 * 模块转换到当前态
		 * 
		 * @param moduleId
		 *            模块ID
		 * @param readerId
		 *            读卡器ID
		 */
		void onModulePresent(int moduleId, int readerId);

		/**
		 * 模块与读卡器已解除绑定
		 * 
		 * @param moduleId
		 *            模块ID
		 */
		void onModuleAbsent(int moduleId);

		/**
		 * CA模块有变化
		 * 
		 * @param moduleId
		 *            模块ID
		 */
		void onCAChange(int moduleId);
	}

	/**
	 * CA监听器对象
	 */
	public static interface CACardStateListener {
		/**
		 * 卡插入
		 * 
		 * @param readerId
		 *            读卡器ID
		 */
		void onCardPresent(int readerId);

		/**
		 * 卡拔出
		 * 
		 * @param readerId
		 *            读卡器ID
		 */
		void onCardAbsent(int readerId);

		/**
		 * 卡无电气信号或插反
		 * 
		 * @param readerId
		 *            读卡器ID
		 */
		void onCardMuted(int readerId);

		/**
		 * 卡就绪
		 * 
		 * @param readerId
		 *            读卡器ID
		 */
		void onCardReady(int readerId);

		/**
		 * 卡验证结果
		 * 
		 * @param readerId
		 *            读卡器ID
		 * @param moduleId
		 *            验证的模块ID,如果为-1则表示暂未被任何模块所验证
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
