package com.ipanel.join.chongqing.live.data;

import android.net.Uri;
import android.provider.BaseColumns;

/**
 * ����������Ϣ���ݿ�
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
		/** �������� long [key] */
		public static final String FREQUENCY = "frequency";
		/** �������� int */
		public static final String DEVLIVERY_TYPE = "delivery";
		/** �������� String */
		public static final String TUNE_PARAM = "tune_param";
		/** �������� String */
		public static final String DVB_TSID = "dvb_tsid";
	}

	protected interface ChannelColumns {
		/**
		 * �������� int<br>
		 * ȡֵ�ο�: {@link android.net.telecast.ProgramInfo.ChannelTypeEnum}
		 */
		public static final String CHANNEL_TYPE = "channel_type";
		/** �������� String */
		public static final String DVB_SERVICE_TYPE = "dvb_service_type";// ���˹��
		/** �������� String */
		public static final String CHANNEL_NAME = "channel_name";
		/** �������� String */
		public static final String CHANNEL_NAME_EN = "channel_name_en";
		/** �������� String */
		public static final String PROVIDER_NAME = "provider_name";
		/** �������� long */
		public static final String FREQUENCY = "frequency";
		/** �������� int */
		public static final String PROGRAM_NUMBER = "program_number";
		/** �������� int */
		public static final String ISCAREQUIRED = "dvb_is_free_ca";
		/** �������� int */
		public static final String CHANNEL_NUMBER = "channel_number";
		/** �������� int */
		public static final String DVB_IS_FAVORITE = "dvb_is_favorite";
		/** �������� int */
		public static final String CHANNEL_ID = "channel_id";
	}

	protected interface StreamColumns {
		/**
		 * �������� String<br>
		 * ȡֵ�ο�:{@link android.net.telecast.ProgramInfo.StreamTypeNameEnum}
		 */
		public static final String STREAM_TYPE_NAME = "stream_type_name";
		/** �������� int */
		public static final String STREAM_PID = "stream_pid";
		/** �������� int */
		public static final String STREAM_TYPE = "stream_type";
		/** �������� long */
		public static final String FREQUENCY = "frequency";
		/** �������� int */
		public static final String PROGRAM_NUMBER = "program_number";
		/** �������� int */
		public static final String COMPONENT_TAG = "component_tag";
	}

	protected interface EventColumns {
		/** �������� long */
		public static final String FREQUENCY = "frequency";
		/** �������� int */
		public static final String PROGRAM_NUMBER = "program_number";
		/** �������� String */
		public static final String EVENT_NAME = "event_name";
		/** �������� String */
		public static final String EVENT_NAME_EN = "event_name_en";
		/** �������� long */
		public static final String START_TIME = "start_time";
		/** �������� String */
		public static final String START_TIME_3339 = "start_time_3339";
		/** �������� int */
		public static final String DURATION = "duration";
	}

	protected interface GroupColumns {
		/** �������� String */
		public static final String GROUP_NAME = "group_name";
		/** �������� long */
		public static final String FREQUENCY = "frequency";
		/** �������� int */
		public static final String PROGRAM_NUMBER = "program_number";
		/** �������� int */
		public static final String BOUQUET_ID = "bouquetId";
	}

	protected interface BookColumns {
		/** �������� String */
		public static final String FREQUENCY = "frequency";
		/** �������� String */
		public static final String PROGRAM_NUMBER = "program_number";
		/** �������� String */
		public static final String EVENT_NAME = "event_name";
		/** �������� String */
		public static final String START_TIME = "start_time";
		/** �������� String */
		public static final String DURATION = "duration";
		/** �������� String */
		public static final String CHANNEL_NAME = "channel_name";
		/** �������� String */
		public static final String CHANNEL_NUMBER = "channel_number";
	}

	/**
	 * ����Ƶ��
	 */
	public static final class Frequencies implements BaseColumns, FrequencyColumns {
		/** ������� */
		public static final String TABLE_NAME = "frequencies";
	}

	/**
	 * ��ĿƵ��
	 */
	public static final class Channels implements BaseColumns, ChannelColumns {
		/** ������� */
		public static final String TABLE_NAME = "channels";
	}

	/**
	 * ��Ŀ�¼�
	 */
	public static final class Events implements BaseColumns, EventColumns {
		/** ������� */
		public static final String TABLE_NAME = "events";
	}

	/**
	 * ��Ŀ��
	 */
	public static final class Streams implements BaseColumns, StreamColumns {
		/** ������� */
		public static final String TABLE_NAME = "streams";
	}

	/**
	 * ��Ŀ����
	 */
	public static final class Groups implements BaseColumns, GroupColumns {
		/** ������� */
		public static final String TABLE_NAME = "groups";
	}

	/**
	 * CA ECM
	 */
	public static final class BookChannels implements BaseColumns, BookColumns {
		/** ������� */
		public static final String TABLE_NAME = "bookchannels";
	}

	/**
	 * CA ECM
	 */
	public static final class BackChannel implements BaseColumns, BookColumns {
		/** ������� */
		public static final String TABLE_NAME = "bookchannels";
	}

	public static final class BackChannels implements BaseColumns, BackChannelColumns {
		/** ������� */
		public static final String TABLE_NAME = "backChannels";
	}

	protected interface BackChannelColumns {
		/** �������� int */
		public static final String CHANNELID = "tVodChannel_id";
		/** �������� String */
		public static final String CHANNELNAME = "tVodChannel_name";
	}

	public static final class BackPrograms implements BaseColumns, ProgramColumns {
		/** ������� */
		public static final String TABLE_NAME = "backPrograms";
	}

	protected interface ProgramColumns {
		/** �������� int */
		public static final String CHANNELID = "tVodChannel_id";
		/** �������� String */
		public static final String CHANNELNAME = "tVodChannel_name";
		/** �������� String */
		public static final String PROGRAMDATE = "tVodPro_Date";
		/** �������� String */
		public static final String PROGRAMNAME = "tVodPro_Name";
		/** �������� String */
		public static final String PROGRAMSTIME = "tVodPro_STime";
		/** �������� String */
		public static final String PROGRAMURL = "tVodPro_Url";

	}
}
