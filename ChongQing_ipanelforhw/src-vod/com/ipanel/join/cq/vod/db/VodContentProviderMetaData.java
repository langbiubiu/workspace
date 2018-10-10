package com.ipanel.join.cq.vod.db;

import android.net.Uri;
import android.provider.BaseColumns;
public class VodContentProviderMetaData {
	public static final String AUTHORITY = "vod.test.contentprovider";
	public static final String DATABASE_NAME = "vod.db";
	// ���ݿ�İ汾
	public static final int DATABASE_VERSION = 2;

	/** �ղ���Ϣ�� */
	public static final class AllfavoritesInfo implements BaseColumns {
		// ����
		public static final String ALLFAVORITESINFO_TABLE_NAME = "allfavoritesinfo";
		// ���ʸ�ContentProvider��URI
		public static final Uri CONTENT_URI = Uri.parse("content://" + AUTHORITY + "/allfavoritesinfo");

		// ��ContentProvider�����ص��������͵Ķ���
		public static final String CONTENT_TYPE = "vnd.android.cursor.dir/vnd.firstprovider.allfavoritesinfo";
		public static final String CONTENT_TYPE_ITEM = "vnd.android.cursor.item/vnd.firstprovider.allfavoritesinfo";

		public static final String VODID = "vodId";

		public static final String TYPEID = "typeId";

		public static final String VODNAME = "vodName";

		public static final String DIRECTOR = "director";

		public static final String ACTOR = "actor";

		public static final String INTR = "intr";

		public static final String PLAYTYPE = "playType";

		public static final String PICPATH = "picPath";

		public static final String ELAPSETIME = "elapsetime";

		public static final String TOTALNUM = "totalNum";

		public static final String VODIDLIST = "vodIdList";
		
		public static final String ISAFTERPLAY = "isafterplay";
		
		public static final String CODE = "code";
		public static final String JSONSTRING = "jsonstring";

	}

	/** ��ʷ�ۿ���¼�� */
	public static final class PlayedInfo implements BaseColumns {
		// ����
		public static final String PLAYEDINFO_TABLE_NAME = "playedinfo";
		// ���ʸ�ContentProvider��URI

		public static final Uri CONTENT_URI = Uri.parse("content://" + AUTHORITY + "/playedinfo");

		// ��ContentProvider�����ص��������͵Ķ���
		public static final String CONTENT_TYPE = "vnd.android.cursor.dir/vnd.firstprovider.playedinfo";
		public static final String CONTENT_TYPE_ITEM = "vnd.android.cursor.item/vnd.firstprovider.playedinfo";

		public static final String VODID = "vodId";

		public static final String TYPEID = "typeId";

		public static final String VODNAME = "vodName";

		public static final String DIRECTOR = "director";

		public static final String ACTOR = "actor";

		public static final String INTR = "intr";

		public static final String PLAYTYPE = "playType";

		public static final String PICPATH = "picPath";

		public static final String ELAPSETIME = "elapsetime";

		public static final String TOTALNUM = "totalNum";

		public static final String VODIDLIST = "vodIdList";

		public static final String ISOVER = "isOver";

		public static final String LASTEPOSDIDE = "lastEposdide";

		public static final String URL = "url";

		public static final String TIME = "time";

		public static final String SHOWTIME = "showTime";

		public static final String WATCHTIME = "watchTime";

		public static final String TIMERATIO = "timeRatio";
		public static final String CODE = "code";
		public static  final String JSONSTRING ="jsonstring";

	}

}
