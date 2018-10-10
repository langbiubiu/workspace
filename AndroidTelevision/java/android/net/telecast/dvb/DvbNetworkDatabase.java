package android.net.telecast.dvb;

import android.net.telecast.NetworkDatabase;
import android.provider.BaseColumns;

/**
 * DVB传输协议网络的数据库基础表规范
 */
public class DvbNetworkDatabase extends NetworkDatabase {

	protected interface TransportStreamColumns {
		/** 数据类型 int */
		public static final String TRANSPORT_STREAM_ID = "dvb_tsid";
		/** 数据类型 int */
		public static final String NETWORK_ID = "dvb_nid";
		/** 数据类型 int */
		public static final String ORIGINAL_NETWORK_ID = "dvb_onid";
	}

	protected interface ServiceColumns {
		/** 数据类型 int */
		public static final String SERVICE_TYPE = "dvb_service_type";
		/** 数据类型 int */
		public static final String EIT_PF_FLAG = "dvb_eit_pf_flag";
		/** 数据类型 int */
		public static final String EIT_SCHEDULE_FLAG = "dvb_eit_sch_flag";
		/** 数据类型 int */
		public static final String IS_FREE_CA = "dvb_is_free_ca";
		/** 数据类型 String */
		public static final String SHORT_SERVICE_NAME = "dvb_short_service_name";
		/** 数据类型 String */
		public static final String SHORT_PROVIDER_NAME = "dvb_short_provider_name";
	}

	protected interface ServiceEventColumns {
		/** 数据类型 int */
		public static final String EVENT_ID = "dvb_event_id";
		/** 数据类型 String */
		public static final String SHORT_EVENT_NAME = "dvb_short_event_name";
		/** 数据类型 int */
		public static final String IS_FREE_CA = "dvb_is_free_ca";
		/** 数据类型 int */
		public static final String RUNNING_STATUS = "dvb_running_status";
	}

	/** 业务分组 */
	protected interface BouquetColumns {
		/** 数据类型 int */
		public static final String BOUQUET_ID = "dvb_bouquet_id";
		/** 数据类型 String */
		public static final String BOUQUET_NAME = "dvb_bouquet_name";
		/** 数据类型 String */
		public static final String SHORT_BOUQUET_NAME = "dvb_short_bouquet_name";
		/** 数据类型 int */
		public static final String TS_ID = "dvb_ts_id";
		/** 数据类型 int */
		public static final String SERVICE_ID = "dvb_service_id";
	}

	/** 网络 */
	protected interface NetworkColumns {
		/** 数据类型 int */
		public static final String NETWORK_ID = "dvb_network_id";
		/** 数据类型 String */
		public static final String NETWORK_NAME = "dvb_network_name";
		/** 数据类型 String */
		public static final String SHORT_NETWORK_NAME = "dvb_short_network_name";
	}

	/** 基本流 */
	protected interface ElementaryStreamsColumns {
		/** 数据类型 int */
		public static final String COMPONENT_TAG = "component_tag";
	}

	/**
	 * 网络
	 */
	public static final class Networks implements BaseColumns, NetworkColumns {
		/** 表的名称 */
		public static final String TABLE_NAME = "dvb_networks";
	}

	/** 业务组 */
	public static final class Bouquets implements BaseColumns, BouquetColumns {
		/** 表的名称 */
		public static final String TABLE_NAME = "dvb_bouquets";
	}

	/**
	 * 传输流
	 */
	public static final class TransportStreams implements BaseColumns, FrequencyColumns,
			TransportStreamColumns {
		/** 表的名称 */
		public static final String TABLE_NAME = NetworkDatabase.Frequencies.TABLE_NAME;
	}

	/**
	 * 业务
	 */
	public static final class Services implements ServiceColumns, BaseColumns, ChannelColumns {
		/** 表的名称 */
		public static final String TABLE_NAME = NetworkDatabase.Channels.TABLE_NAME;
	}

	/**
	 * 节目事件
	 */
	public static final class ServiceEvents implements BaseColumns, EventColumns,
			ServiceEventColumns {
		/** 表的名称 */
		public static final String TABLE_NAME = NetworkDatabase.Events.TABLE_NAME;
	}

	/**
	 * 节目流
	 */
	public static final class ElementaryStreams implements BaseColumns, StreamColumns,
			ElementaryStreamsColumns {
		/** 表的名称 */
		public static final String TABLE_NAME = NetworkDatabase.Streams.TABLE_NAME;
	}
}
