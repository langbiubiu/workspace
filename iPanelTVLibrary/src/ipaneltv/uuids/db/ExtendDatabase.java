package ipaneltv.uuids.db;

import android.net.telecast.NetworkDatabase;
import android.net.telecast.dvb.DvbNetworkDatabase;
import android.provider.BaseColumns;

public class ExtendDatabase extends DvbNetworkDatabase {
	protected interface ExtendServiceColumns {
		/** 数据类型 int */
		public static final String HIDED = "hided";
		public static final String MOSAIC = "mosaic";
		public static final String VOLUME = "volume";
		public static final String AUDIO_TRACK = "audio_track";
		public static final String DVB_IS_FAVORITE = "dvb_is_favorite";
		public static final String URL_ASICC = "url_asicc";
	}

	protected interface ExtendStreamColumns {
		
		public static final String CELL_ID = "cell_id";
	}

	protected interface ExtendEcmColumns {
	}

	protected interface ExtendGroupColumns {
		/** 数据类型 int */
		public static final String BOUQUET_ID = GroupColumns.GROUP_ID;
		/**	数据类型int */
		public static final String USER_ID = "user_id";
	}

	public static final class ExtendServices implements ServiceColumns, BaseColumns, ChannelColumns,
	ExtendServiceColumns {
		public static final String TABLE_NAME = NetworkDatabase.Channels.TABLE_NAME;
	}

	public static final class ExtendGroups implements BaseColumns, GroupColumns, ExtendGroupColumns {
		public static final String TABLE_NAME = NetworkDatabase.Groups.TABLE_NAME;
	}

	public static final class ExtendProgramEvents implements BaseColumns, EventColumns,
			ServiceEventColumns {
		public static final String TABLE_NAME = DvbNetworkDatabase.ServiceEvents.TABLE_NAME;
	}

	public static final class ExtendStreams implements BaseColumns, StreamColumns,
			ElementaryStreamsColumns, ExtendStreamColumns {
		public static final String TABLE_NAME = DvbNetworkDatabase.ElementaryStreams.TABLE_NAME;
	}

	public static final class ExtendEcms implements BaseColumns, EcmColumns, ExtendEcmColumns {
		public static final String TABLE_NAME = NetworkDatabase.Ecms.TABLE_NAME;
	}

	public static final class ExtendBouquets implements BaseColumns, BouquetColumns {
		public static final String TABLE_NAME = DvbNetworkDatabase.Bouquets.TABLE_NAME;
	}

	public static final class ExtendNetworks implements BaseColumns, NetworkColumns {
		public static final String TABLE_NAME = DvbNetworkDatabase.Networks.TABLE_NAME;
	}

	public static final class ExtendTransportStreams implements BaseColumns, FrequencyColumns,
			TransportStreamColumns {
		public static final String TABLE_NAME = DvbNetworkDatabase.TransportStreams.TABLE_NAME;
	}

}
