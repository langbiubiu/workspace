package android.net.telecast;

import android.provider.BaseColumns;

/**
 * 网络数据信息数据库
 */
public class NetworkDatabase {

	protected interface FrequencyColumns {
		/** 数据类型 long [key] */
		public static final String FREQUENCY = "frequency";
		/** 数据类型 int */
		public static final String DEVLIVERY_TYPE = "delivery";
		/** 数据类型 String */
		public static final String TUNE_PARAM = "tune_param";
		/** 数据类型 int */
		public static final String MPEG_TRANSPORT_STREAM_ID = "pat_tsid";
		/** 数据类型 int,频率下的数据记录发生任何变化，数据库的维护者需在最后一步更新此字段 */
		public static final String INFO_VERSION = "info_version";
	}

	protected interface ChannelColumns {
		/**
		 * 数据类型 int<br>
		 * 取值参考: {@link android.net.telecast.ProgramInfo.ChannelTypeEnum}
		 */
		public static final String CHANNEL_TYPE = "channel_type";
		/** 数据类型 String */
		public static final String CHANNEL_NAME = "channel_name";
		/** 数据类型 String */
		public static final String CHANNEL_NAME_EN = "channel_name_en";
		/** 数据类型 String */
		public static final String PROVIDER_NAME = "provider_name";
		/** 数据类型 int */
		public static final String CHANNEL_NUMBER = "channel_number";
		/** 数据类型 long */
		public static final String FREQUENCY = "frequency";
		/** 数据类型 int */
		public static final String PROGRAM_NUMBER = "program_number";
	}

	protected interface StreamColumns {
		/**
		 * 数据类型 String<br>
		 * 取值参考:{@link android.net.telecast.ProgramInfo.StreamTypeNameEnum}
		 */
		public static final String STREAM_TYPE_NAME = "stream_type_name";
		/** 数据类型 int */
		public static final String PMT_PID = "pmt_pid";
		/** 数据类型 int */
		public static final String STREAM_PID = "stream_pid";
		/** 数据类型 int */
		public static final String STREAM_TYPE = "stream_type";
		/** 数据类型 long */
		public static final String FREQUENCY = "frequency";
		/** 数据类型 int */
		public static final String PROGRAM_NUMBER = "program_number";
		/** 数据类型 int */
		public static final String ASSOCIATION_TAG = "association_tag";
		/**
		 * 数据类型 int<br>
		 * 取值参考:<br>{@link android.net.telecast.ProgramInfo.VideoPictureFormEnum}<br>
		 * {@link android.net.telecast.ProgramInfo.AudioTrackFormEnum}
		 */
		public static final String PRESENTING_FORM = "presenting_form";
	}

	protected interface EventColumns {
		/** 数据类型 long */
		public static final String FREQUENCY = "frequency";
		/** 数据类型 int */
		public static final String PROGRAM_NUMBER = "program_number";
		/** 数据类型 String */
		public static final String EVENT_NAME = "event_name";
		/** 数据类型 String */
		public static final String EVENT_NAME_EN = "event_name_en";
		/** 数据类型 long */
		public static final String START_TIME = "start_time";
		/** 数据类型 long */
		public static final String END_TIME = "end_time";
		/** 数据类型 int */
		public static final String DURATION = "duration";
	}

	protected interface GroupColumns {
		/** 数据类型 String */
		public static final String GROUP_NAME = "group_name";
		/** 数据类型 long */
		public static final String FREQUENCY = "frequency";
		/** 数据类型 int */
		public static final String PROGRAM_NUMBER = "program_number";
		/** 数据类型 int */
		public static final String GROUP_ID = "group_id";
	}

	protected interface EcmColumns {
		/** 数据类型 int */
		public static final String CA_SYSTEM_ID = "ca_system_id";
		/** 数据类型 long */
		public static final String FREQUENCY = "frequency";
		/** 数据类型 int */
		public static final String PROGRAM_NUMBER = "program_number";
		/** 数据类型 int */
		public static final String STREAM_PID = "stream_pid";
		/** 数据类型 int */
		public static final String ECM_PID = "ecm_pid";
	}

	protected interface GuideColumns {
		/** 数据类型 long */
		public static final String FREQUENCY = "frequency";
		/** 数据类型 int */
		public static final String PROGRAM_NUMBER = "program_number";
		/** 数据类型 int */
		public static final String VERSION = "version";
	}

	/**
	 * 指南线索
	 */
	public static final class Guides implements BaseColumns, GuideColumns {
		/** 表的名称 */
		public static final String TABLE_NAME = "guides";
	}

	/**
	 * 传送频率
	 */
	public static final class Frequencies implements BaseColumns, FrequencyColumns {
		/** 表的名称 */
		public static final String TABLE_NAME = "frequencies";
	}

	/**
	 * 节目频道
	 */
	public static final class Channels implements BaseColumns, ChannelColumns {
		/** 表的名称 */
		public static final String TABLE_NAME = "channels";
	}

	/**
	 * 节目事件
	 */
	public static final class Events implements BaseColumns, EventColumns {
		/** 表的名称 */
		public static final String TABLE_NAME = "events";
	}

	/**
	 * 节目流
	 */
	public static final class Streams implements BaseColumns, StreamColumns {
		/** 表的名称 */
		public static final String TABLE_NAME = "streams";
	}

	/**
	 * 授权控制信息
	 */
	public static final class Ecms implements BaseColumns, EcmColumns {
		/** 表的名称 */
		public static final String TABLE_NAME = "ecms";
	}

	/**
	 * 节目分组
	 */
	public static final class Groups implements BaseColumns, GroupColumns {
		/** 表的名称 */
		public static final String TABLE_NAME = "groups";
	}

}
