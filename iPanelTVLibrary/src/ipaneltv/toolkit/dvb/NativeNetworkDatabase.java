package ipaneltv.toolkit.dvb;

import android.provider.BaseColumns;

public class NativeNetworkDatabase {
	/**
	 * regionIds
	 */
	public static final class NativeRegions implements BaseColumns {
		/** 表的名称 */
		public static final String TABLE_NAME = "regionIds";

		public static final String VOD_MAIN_FREQENCY = "vod_main_frequency";

		public static final String REGION_ID = "region_id";
	}

	public static final class NativeGroups {
		public static final String BOUQUET_ID = "bouquetId";
	}

	public static final class NativeServices {
		public static final String DVB_FAVORITE = "dvb_is_favorite";
	}

	public static final class NativeEcms implements BaseColumns {
		/** 数据类型 int */
		public static final String PRODUCT_PID = "product_pid";
	}
}