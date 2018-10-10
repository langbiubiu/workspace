package android.net.telecast.dvb;

import android.net.telecast.NetworkDatabase;
import android.provider.BaseColumns;

/**
 * DVB����Э����������ݿ������淶
 */
public class DvbNetworkDatabase extends NetworkDatabase {

	protected interface TransportStreamColumns {
		/** �������� int */
		public static final String TRANSPORT_STREAM_ID = "dvb_tsid";
		/** �������� int */
		public static final String NETWORK_ID = "dvb_nid";
		/** �������� int */
		public static final String ORIGINAL_NETWORK_ID = "dvb_onid";
	}

	protected interface ServiceColumns {
		/** �������� int */
		public static final String SERVICE_TYPE = "dvb_service_type";
		/** �������� int */
		public static final String EIT_PF_FLAG = "dvb_eit_pf_flag";
		/** �������� int */
		public static final String EIT_SCHEDULE_FLAG = "dvb_eit_sch_flag";
		/** �������� int */
		public static final String IS_FREE_CA = "dvb_is_free_ca";
		/** �������� String */
		public static final String SHORT_SERVICE_NAME = "dvb_short_service_name";
		/** �������� String */
		public static final String SHORT_PROVIDER_NAME = "dvb_short_provider_name";
	}

	protected interface ServiceEventColumns {
		/** �������� int */
		public static final String EVENT_ID = "dvb_event_id";
		/** �������� String */
		public static final String SHORT_EVENT_NAME = "dvb_short_event_name";
		/** �������� int */
		public static final String IS_FREE_CA = "dvb_is_free_ca";
		/** �������� int */
		public static final String RUNNING_STATUS = "dvb_running_status";
	}

	/** ҵ����� */
	protected interface BouquetColumns {
		/** �������� int */
		public static final String BOUQUET_ID = "dvb_bouquet_id";
		/** �������� String */
		public static final String BOUQUET_NAME = "dvb_bouquet_name";
		/** �������� String */
		public static final String SHORT_BOUQUET_NAME = "dvb_short_bouquet_name";
		/** �������� int */
		public static final String TS_ID = "dvb_ts_id";
		/** �������� int */
		public static final String SERVICE_ID = "dvb_service_id";
	}

	/** ���� */
	protected interface NetworkColumns {
		/** �������� int */
		public static final String NETWORK_ID = "dvb_network_id";
		/** �������� String */
		public static final String NETWORK_NAME = "dvb_network_name";
		/** �������� String */
		public static final String SHORT_NETWORK_NAME = "dvb_short_network_name";
	}

	/** ������ */
	protected interface ElementaryStreamsColumns {
		/** �������� int */
		public static final String COMPONENT_TAG = "component_tag";
	}

	/**
	 * ����
	 */
	public static final class Networks implements BaseColumns, NetworkColumns {
		/** ������� */
		public static final String TABLE_NAME = "dvb_networks";
	}

	/** ҵ���� */
	public static final class Bouquets implements BaseColumns, BouquetColumns {
		/** ������� */
		public static final String TABLE_NAME = "dvb_bouquets";
	}

	/**
	 * ������
	 */
	public static final class TransportStreams implements BaseColumns, FrequencyColumns,
			TransportStreamColumns {
		/** ������� */
		public static final String TABLE_NAME = NetworkDatabase.Frequencies.TABLE_NAME;
	}

	/**
	 * ҵ��
	 */
	public static final class Services implements ServiceColumns, BaseColumns, ChannelColumns {
		/** ������� */
		public static final String TABLE_NAME = NetworkDatabase.Channels.TABLE_NAME;
	}

	/**
	 * ��Ŀ�¼�
	 */
	public static final class ServiceEvents implements BaseColumns, EventColumns,
			ServiceEventColumns {
		/** ������� */
		public static final String TABLE_NAME = NetworkDatabase.Events.TABLE_NAME;
	}

	/**
	 * ��Ŀ��
	 */
	public static final class ElementaryStreams implements BaseColumns, StreamColumns,
			ElementaryStreamsColumns {
		/** ������� */
		public static final String TABLE_NAME = NetworkDatabase.Streams.TABLE_NAME;
	}
}
