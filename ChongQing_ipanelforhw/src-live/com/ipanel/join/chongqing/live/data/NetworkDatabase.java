package com.ipanel.join.chongqing.live.data;

import android.net.Uri;
import android.provider.BaseColumns;

/**
 * 网络数据信息数据库
 */
public class NetworkDatabase {

	public static final String AUTHORITY = "ipaneltv.chongqing.networksidb";
	public static final String B_AUTHORITY = "com.ipanel.join.cq.backdb";

	public static final String BOOKAUTHORITY = "tv.ipanel.join.app.tv.watchtv.database.LiveTVProvider";
	public static final String CONTENT_TYPE = "vnd.android.cursor.dir/vnd.ipanel.app";
	public static final String CONTENT_ITEM_TYPE = "vnd.android.cursor.item/vnd.ipanel.app";

	public static final String FREQUENCY_DEFAULT_SORT_ORDER = "frequency ASC";// DESC
	public static final String FREQUENCY_CREATEDDATE = "created";
	public static final String FREQUENCY_MODIFIEDDATE = "modified";
	// public static final Uri FREQUENCY_CONTENT_URI = Uri.parse("content://"
	// + AUTHORITY + "/frequencys");
	public static final Uri FREQUENCY_CONTENT_URI = Uri.parse("content://" + AUTHORITY
			+ "/frequencies");
	public static final Uri CHANNEL_CONTENT_URI = Uri.parse("content://" + AUTHORITY + "/channels");
	public static final Uri EVENT_CONTENT_URI = Uri.parse("content://" + AUTHORITY + "/events");
	public static final Uri STREAM_CONTENT_URI = Uri.parse("content://" + AUTHORITY + "/streams");
	public static final Uri GROUP_CONTENT_URI = Uri.parse("content://" + AUTHORITY + "/groups");
	public static Uri BOOK_CONTENT_URI = Uri.parse("content://" + BOOKAUTHORITY + "/bookchannels");

	public static final Uri BACK_CHANNEL_URI = Uri.parse("content://" + B_AUTHORITY
			+ "/backChannels");
	public static final Uri BACK_PROGRAM_URI = Uri.parse("content://" + B_AUTHORITY
			+ "/backPrograms");

	protected interface FrequencyColumns {
		/** 数据类型 long [key] */
		public static final String FREQUENCY = "frequency";
		/** 数据类型 int */
		public static final String DEVLIVERY_TYPE = "delivery";
		/** 数据类型 String */
		public static final String TUNE_PARAM = "tune_param";
		/** 数据类型 String */
		public static final String DVB_TSID = "dvb_tsid";
	}

	protected interface ChannelColumns {
		/**
		 * 数据类型 int<br>
		 * 取值参考: {@link android.net.telecast.ProgramInfo.ChannelTypeEnum}
		 */
		public static final String CHANNEL_TYPE = "channel_type";
		/** 数据类型 String */
		public static final String DVB_SERVICE_TYPE = "dvb_service_type";// 过滤广告
		/** 数据类型 String */
		public static final String CHANNEL_NAME = "channel_name";
		/** 数据类型 String */
		public static final String CHANNEL_NAME_EN = "channel_name_en";
		/** 数据类型 String */
		public static final String PROVIDER_NAME = "provider_name";
		/** 数据类型 long */
		public static final String FREQUENCY = "frequency";
		/** 数据类型 int */
		public static final String PROGRAM_NUMBER = "program_number";
		/** 数据类型 int */
		public static final String ISCAREQUIRED = "dvb_is_free_ca";
		/** 数据类型 int */
		public static final String CHANNEL_NUMBER = "channel_number";
		/** 数据类型 int */
		public static final String DVB_IS_FAVORITE = "dvb_is_favorite";
		/** 数据类型 int */
		public static final String CHANNEL_ID = "channel_id";
	}

	protected interface StreamColumns {
		/**
		 * 数据类型 String<br>
		 * 取值参考:{@link android.net.telecast.ProgramInfo.StreamTypeNameEnum}
		 */
		public static final String STREAM_TYPE_NAME = "stream_type_name";
		/** 数据类型 int */
		public static final String STREAM_PID = "stream_pid";
		/** 数据类型 int */
		public static final String STREAM_TYPE = "stream_type";
		/** 数据类型 long */
		public static final String FREQUENCY = "frequency";
		/** 数据类型 int */
		public static final String PROGRAM_NUMBER = "program_number";
		/** 数据类型 int */
		public static final String COMPONENT_TAG = "component_tag";
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
		/** 数据类型 String */
		public static final String START_TIME_3339 = "start_time_3339";
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
		public static final String BOUQUET_ID = "bouquetId";
	}

	protected interface BookColumns {
		/** 数据类型 String */
		public static final String FREQUENCY = "frequency";
		/** 数据类型 String */
		public static final String PROGRAM_NUMBER = "program_number";
		/** 数据类型 String */
		public static final String EVENT_NAME = "event_name";
		/** 数据类型 String */
		public static final String START_TIME = "start_time";
		/** 数据类型 String */
		public static final String DURATION = "duration";
		/** 数据类型 String */
		public static final String CHANNEL_NAME = "channel_name";
		/** 数据类型 String */
		public static final String CHANNEL_NUMBER = "channel_number";
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
	 * 节目分组
	 */
	public static final class Groups implements BaseColumns, GroupColumns {
		/** 表的名称 */
		public static final String TABLE_NAME = "groups";
	}

	/**
	 * CA ECM
	 */
	public static final class BookChannels implements BaseColumns, BookColumns {
		/** 表的名称 */
		public static final String TABLE_NAME = "bookchannels";
	}

	/**
	 * CA ECM
	 */
	public static final class BackChannel implements BaseColumns, BookColumns {
		/** 表的名称 */
		public static final String TABLE_NAME = "bookchannels";
	}

	public static final class BackChannels implements BaseColumns, BackChannelColumns {
		/** 表的名称 */
		public static final String TABLE_NAME = "backChannels";
	}

	protected interface BackChannelColumns {
		/** 数据类型 int */
		public static final String CHANNELID = "tVodChannel_id";
		/** 数据类型 String */
		public static final String CHANNELNAME = "tVodChannel_name";
	}

	public static final class BackPrograms implements BaseColumns, ProgramColumns {
		/** 表的名称 */
		public static final String TABLE_NAME = "backPrograms";
	}

	protected interface ProgramColumns {
		/** 数据类型 int */
		public static final String CHANNELID = "tVodChannel_id";
		/** 数据类型 String */
		public static final String CHANNELNAME = "tVodChannel_name";
		/** 数据类型 String */
		public static final String PROGRAMDATE = "tVodPro_Date";
		/** 数据类型 String */
		public static final String PROGRAMNAME = "tVodPro_Name";
		/** 数据类型 String */
		public static final String PROGRAMSTIME = "tVodPro_STime";
		/** 数据类型 String */
		public static final String PROGRAMURL = "tVodPro_Url";

	}
}
