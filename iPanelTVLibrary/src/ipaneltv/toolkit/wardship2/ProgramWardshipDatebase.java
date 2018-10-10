package ipaneltv.toolkit.wardship2;

import android.provider.BaseColumns;

/**
 * 节目收看监护数据提供者
 */
public class ProgramWardshipDatebase {

	/**
	 * 保护类型
	 */
	public static interface WardshipType {
		/** 开放的-可直接观看 */
		public static final int TYPE_OPEN = 0;
		/** 加锁的-需提示密码输入 */
		public static final int TYPE_LOCKED = 1;
		/** 被否决的-不应予以播放 */
		public static final int TYPE_REFUSED = 2;
	}

	protected interface ProgramWardshipColumns {
		/** 数据类型 long [key] */
		public static final String FREQUENCY = "frequency";
		/** 数据类型 int */
		public static final String PROGRAM_NUMBER = "program_number";
		/**数据类型int */
		public static final String CHANNEL_NUMBER = "channel_number";
		/** 数据类型 String */
		public static final String CHANNEL_NAME = "channel_name";
		/** 数据类型 int */
		public static final String WARDSHIP = "wardship";
	}

	/**
	 * 授权控制信息
	 */
	public static final class ProgramWardships implements BaseColumns, ProgramWardshipColumns {
		/** 表的名称 */
		public static final String TABLE_NAME = "program_wardship";
	}
}
