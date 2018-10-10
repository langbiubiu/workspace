package ipaneltv.uuids.db;

import android.net.telecast.NetworkDatabase;
import android.net.telecast.dvb.DvbNetworkDatabase;
import android.provider.BaseColumns;

public class FujianDatabase extends DvbNetworkDatabase {
	protected interface FujianServiceColumns {
		/** 数据类型 int */
		public static final String CHANNEL_ID = "wasu_channel_id";
		
		public static final String MOSAIC = "mosaic";
	}

	protected interface FujianStreamColumns {
		/** 数据类型 int */
		public static final String CHANNEL_ID = "wasu_channel_id";
		
		public static final String CELL_ID = "cell_id";
	}

	protected interface FujianEcmColumns {
		/** 数据类型 int */
		public static final String PRODUCT_ID = "wasu_product_id";
	}

	protected interface FujianGroupColumns {
		/** 数据类型 int */
		public static final String BOUQUET_ID = GroupColumns.GROUP_ID;
		/**	数据类型int */
		public static final String USER_ID = "user_id";
	}

	public static final class FujianServices implements ServiceColumns, BaseColumns, ChannelColumns,
	FujianServiceColumns {
		public static final String TABLE_NAME = NetworkDatabase.Channels.TABLE_NAME;
	}

	public static final class FujianGroups implements BaseColumns, GroupColumns, FujianGroupColumns {
		public static final String TABLE_NAME = NetworkDatabase.Groups.TABLE_NAME;
	}

	public static final class FujianProgramEvents implements BaseColumns, EventColumns,
			ServiceEventColumns {
		public static final String TABLE_NAME = DvbNetworkDatabase.ServiceEvents.TABLE_NAME;
	}

	public static final class FujianStreams implements BaseColumns, StreamColumns,
			ElementaryStreamsColumns, FujianStreamColumns {
		public static final String TABLE_NAME = DvbNetworkDatabase.ElementaryStreams.TABLE_NAME;
	}

	public static final class FujianEcms implements BaseColumns, EcmColumns, FujianEcmColumns {
		public static final String TABLE_NAME = NetworkDatabase.Ecms.TABLE_NAME;
	}

	public static final class FujianBouquets implements BaseColumns, BouquetColumns {
		public static final String TABLE_NAME = DvbNetworkDatabase.Bouquets.TABLE_NAME;
	}

	public static final class FujianNetworks implements BaseColumns, NetworkColumns {
		public static final String TABLE_NAME = DvbNetworkDatabase.Networks.TABLE_NAME;
	}

	public static final class FujianTransportStreams implements BaseColumns, FrequencyColumns,
			TransportStreamColumns {
		public static final String TABLE_NAME = DvbNetworkDatabase.TransportStreams.TABLE_NAME;
	}

}
