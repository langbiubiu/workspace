package android.net.telecast.ca;

import android.provider.BaseColumns;

/**
 * ��Ȩ��Ϣ���ݿ���
 */
public class EntitlementDatabase {

	/** ��Ȩ״̬ */
	public static interface EntitlementType {
		/** δ֪ */
		public static final int TYPE_UNKNOWN = 0;
		/** ���� */
		public static final int TYPE_AVAILABLE = 1;
		/** ������ */
		public static final int TYPE_NOT_AVAILABL = 2;
		/** ���Է�����Ự */
		public static final int TYPE_DIALOG_REQUIRED = 3;
	}

	/** ��Ʒ���� */
	public static interface ProductType {
		/** ���� */
		public static final int TYPE_OTHER = 0;
		/** Ƶ�� */
		public static final int TYPE_CHANNEL = 1;
		/** Ӧ�� */
		public static final int TYPE_APP = 2;
		/** ��Ӱ */
		public static final int TYPE_MOVIE = 3;
	}

	/** ��ƷUri��Schema */
	public static interface ProductUriSchema {
		/** ��ƷAuthority-Ƶ�� */
		public static final String CHANNEL = "ch://";
		/** ��ƷAuthority-Ӧ�� */
		public static final String APP = "app://";
		/** ��ƷAuthority-ӰƬ */
		public static final String MOVIE = "movie://";
	}

	protected interface EntitlementColumns {
		/** ��������int */
		public static final String PRODUCT_ID = "product_id";
		/** �������� int */
		public static final String PRODUCT_TYPE = "product_type";
		/** �������� int */
		public static final String MODULE_SN = "module_sn";
		/** �������� int */
		public static final String NETWORK_OPERATOR_ID = "net_operator_id";
		/** �������� String */
		public static final String PRODUCT_URI = "product_uri";
		/** �������� int �ο� {@link android.net.telecast.ca.EntitlementDatabase.EntitlementType} */
		public static final String ENTITLEMENT = "entitlement";
		/** �������� long */
		public static final String START_TIME = "start_time";
		/** �������� long */
		public static final String END_TIME = "end_time";
	}

	/** ��Ȩ��Ϣ�� */
	public static final class Entitlements implements BaseColumns, EntitlementColumns {
		/** ������� */
		public static final String TABLE_NAME = "entitlements";
	}

}
