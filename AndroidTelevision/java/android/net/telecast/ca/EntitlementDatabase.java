package android.net.telecast.ca;

import android.provider.BaseColumns;

/**
 * 授权信息数据库规格
 */
public class EntitlementDatabase {

	/** 授权状态 */
	public static interface EntitlementType {
		/** 未知 */
		public static final int TYPE_UNKNOWN = 0;
		/** 可用 */
		public static final int TYPE_AVAILABLE = 1;
		/** 不可用 */
		public static final int TYPE_NOT_AVAILABL = 2;
		/** 可以发起购买会话 */
		public static final int TYPE_DIALOG_REQUIRED = 3;
	}

	/** 产品类型 */
	public static interface ProductType {
		/** 其他 */
		public static final int TYPE_OTHER = 0;
		/** 频道 */
		public static final int TYPE_CHANNEL = 1;
		/** 应用 */
		public static final int TYPE_APP = 2;
		/** 电影 */
		public static final int TYPE_MOVIE = 3;
	}

	/** 产品Uri的Schema */
	public static interface ProductUriSchema {
		/** 产品Authority-频道 */
		public static final String CHANNEL = "ch://";
		/** 产品Authority-应用 */
		public static final String APP = "app://";
		/** 产品Authority-影片 */
		public static final String MOVIE = "movie://";
	}

	protected interface EntitlementColumns {
		/** 数据类型int */
		public static final String PRODUCT_ID = "product_id";
		/** 数据类型 int */
		public static final String PRODUCT_TYPE = "product_type";
		/** 数据类型 int */
		public static final String MODULE_SN = "module_sn";
		/** 数据类型 int */
		public static final String NETWORK_OPERATOR_ID = "net_operator_id";
		/** 数据类型 String */
		public static final String PRODUCT_URI = "product_uri";
		/** 数据类型 int 参考 {@link android.net.telecast.ca.EntitlementDatabase.EntitlementType} */
		public static final String ENTITLEMENT = "entitlement";
		/** 数据类型 long */
		public static final String START_TIME = "start_time";
		/** 数据类型 long */
		public static final String END_TIME = "end_time";
	}

	/** 授权信息表 */
	public static final class Entitlements implements BaseColumns, EntitlementColumns {
		/** 表的名称 */
		public static final String TABLE_NAME = "entitlements";
	}

}
