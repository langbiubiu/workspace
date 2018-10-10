package android.net.telecast;

import android.provider.BaseColumns;

/**
 * ����������Ϣ���ݿ�
 */
public class NetworkDatabase {

	protected interface FrequencyColumns {
		/** �������� long [key] */
		public static final String FREQUENCY = "frequency";
		/** �������� int */
		public static final String DEVLIVERY_TYPE = "delivery";
		/** �������� String */
		public static final String TUNE_PARAM = "tune_param";
		/** �������� int */
		public static final String MPEG_TRANSPORT_STREAM_ID = "pat_tsid";
		/** �������� int,Ƶ���µ����ݼ�¼�����κα仯�����ݿ��ά�����������һ�����´��ֶ� */
		public static final String INFO_VERSION = "info_version";
	}

	protected interface ChannelColumns {
		/**
		 * �������� int<br>
		 * ȡֵ�ο�: {@link android.net.telecast.ProgramInfo.ChannelTypeEnum}
		 */
		public static final String CHANNEL_TYPE = "channel_type";
		/** �������� String */
		public static final String CHANNEL_NAME = "channel_name";
		/** �������� String */
		public static final String CHANNEL_NAME_EN = "channel_name_en";
		/** �������� String */
		public static final String PROVIDER_NAME = "provider_name";
		/** �������� int */
		public static final String CHANNEL_NUMBER = "channel_number";
		/** �������� long */
		public static final String FREQUENCY = "frequency";
		/** �������� int */
		public static final String PROGRAM_NUMBER = "program_number";
	}

	protected interface StreamColumns {
		/**
		 * �������� String<br>
		 * ȡֵ�ο�:{@link android.net.telecast.ProgramInfo.StreamTypeNameEnum}
		 */
		public static final String STREAM_TYPE_NAME = "stream_type_name";
		/** �������� int */
		public static final String PMT_PID = "pmt_pid";
		/** �������� int */
		public static final String STREAM_PID = "stream_pid";
		/** �������� int */
		public static final String STREAM_TYPE = "stream_type";
		/** �������� long */
		public static final String FREQUENCY = "frequency";
		/** �������� int */
		public static final String PROGRAM_NUMBER = "program_number";
		/** �������� int */
		public static final String ASSOCIATION_TAG = "association_tag";
		/**
		 * �������� int<br>
		 * ȡֵ�ο�:<br>{@link android.net.telecast.ProgramInfo.VideoPictureFormEnum}<br>
		 * {@link android.net.telecast.ProgramInfo.AudioTrackFormEnum}
		 */
		public static final String PRESENTING_FORM = "presenting_form";
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
		/** �������� long */
		public static final String END_TIME = "end_time";
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
		public static final String GROUP_ID = "group_id";
	}

	protected interface EcmColumns {
		/** �������� int */
		public static final String CA_SYSTEM_ID = "ca_system_id";
		/** �������� long */
		public static final String FREQUENCY = "frequency";
		/** �������� int */
		public static final String PROGRAM_NUMBER = "program_number";
		/** �������� int */
		public static final String STREAM_PID = "stream_pid";
		/** �������� int */
		public static final String ECM_PID = "ecm_pid";
	}

	protected interface GuideColumns {
		/** �������� long */
		public static final String FREQUENCY = "frequency";
		/** �������� int */
		public static final String PROGRAM_NUMBER = "program_number";
		/** �������� int */
		public static final String VERSION = "version";
	}

	/**
	 * ָ������
	 */
	public static final class Guides implements BaseColumns, GuideColumns {
		/** ������� */
		public static final String TABLE_NAME = "guides";
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
	 * ��Ȩ������Ϣ
	 */
	public static final class Ecms implements BaseColumns, EcmColumns {
		/** ������� */
		public static final String TABLE_NAME = "ecms";
	}

	/**
	 * ��Ŀ����
	 */
	public static final class Groups implements BaseColumns, GroupColumns {
		/** ������� */
		public static final String TABLE_NAME = "groups";
	}

}
