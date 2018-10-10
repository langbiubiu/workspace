package ipaneltv.toolkit.dvb;

import ipaneltv.toolkit.IPanelLog;

import java.util.HashMap;
import java.util.List;

import android.content.ContentUris;
import android.content.ContentValues;
import android.content.Context;
import android.content.UriMatcher;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteQueryBuilder;
import android.net.Uri;
import android.net.telecast.NetworkDatabase.Ecms;
import android.net.telecast.NetworkDatabase.Events;
import android.net.telecast.NetworkDatabase.Groups;
import android.net.telecast.NetworkDatabase.Guides;
import android.net.telecast.NetworkDatabase.Streams;
import android.net.telecast.dvb.DvbNetworkDatabase;
import android.net.telecast.dvb.DvbNetworkDatabase.ElementaryStreams;
import android.net.telecast.dvb.DvbNetworkDatabase.ServiceEvents;
import android.net.telecast.dvb.DvbNetworkDatabase.Services;
import android.net.telecast.dvb.DvbNetworkDatabase.TransportStreams;
import android.text.TextUtils;
import android.util.Log;

public class DvbProviderBase {
	static final String TAG = "DvbProviderBase";
	public static final int TRANSPORT = 11;
	public static final int TRANSPORT_ID = 12;
	public static final int SERVICE = 21;
	public static final int SERVICE_ID = 22;
	public static final int EVENT = 31;
	public static final int EVENT_ID = 32;
	public static final int GUIDE = 91;
	public static final int GUIDE_ID = 92;
	public static final int STREAM = 41;
	public static final int STREAM_ID = 42;
	public static final int GROUPS = 51;
	public static final int GROUPS_ID = 52;
	public static final int FREQUENCY_EVENT = 61;
	public static final int GROUPS_PROVIDERNAME = 71;
	public static final int ECM = 127;
	public static final int ECM_ID = 128;
	private static String createTSTableSql;
	private static String createServiceTableSql;
	private static String createGroupsTableSql;
	private static String createESTableSql;
	private static String createEventTableSql;
	private static String createGuidesTableSql;
	private static String createEcmTableSql;

	public UriMatcher mUriMatcher;
	public String authority, contentType, contentItemType;

	protected boolean enableProjection = false;
	private static HashMap<String, String> tsMap = new HashMap<String, String>();
	private static HashMap<String, String> serviceMap = new HashMap<String, String>();
	private static HashMap<String, String> eventMap = new HashMap<String, String>();
	private static HashMap<String, String> guidesMap = new HashMap<String, String>();
	private static HashMap<String, String> streamMap = new HashMap<String, String>();
	private static HashMap<String, String> groupMap = new HashMap<String, String>();
	private static HashMap<String, String> ecmMap = new HashMap<String, String>();

	private String[] extraUriPaths;
	private int [] extraUriCodes;
	public DvbProviderBase(String authority, String contentType, String contentItemType) {
		this.authority = authority;
		this.contentType = contentType;
		this.contentItemType = contentItemType;
		init();
		initTableString();
	}

	public void setProjectionEnable(boolean b) {
		enableProjection = b;
	}

	void init() {
		mUriMatcher = new UriMatcher(UriMatcher.NO_MATCH);
		mUriMatcher.addURI(authority, TransportStreams.TABLE_NAME, TRANSPORT);
		mUriMatcher.addURI(authority, TransportStreams.TABLE_NAME + "/#", TRANSPORT_ID);
	
		mUriMatcher.addURI(authority, Services.TABLE_NAME, SERVICE);
		mUriMatcher.addURI(authority, Services.TABLE_NAME + "/#", SERVICE_ID);
		
		mUriMatcher.addURI(authority, ServiceEvents.TABLE_NAME, EVENT);
		mUriMatcher.addURI(authority, ServiceEvents.TABLE_NAME + "/#", EVENT_ID);
		mUriMatcher
				.addURI(authority, ServiceEvents.TABLE_NAME + "/freqency_event", FREQUENCY_EVENT);
		
		mUriMatcher.addURI(authority, Guides.TABLE_NAME, GUIDE);
		mUriMatcher.addURI(authority, Guides.TABLE_NAME + "/#", GUIDE_ID);

		mUriMatcher.addURI(authority, Streams.TABLE_NAME, STREAM);
		mUriMatcher.addURI(authority, Streams.TABLE_NAME + "/#", STREAM_ID);
		
		mUriMatcher.addURI(authority, Groups.TABLE_NAME, GROUPS);
		mUriMatcher.addURI(authority, Groups.TABLE_NAME + "/#", GROUPS_ID);
		mUriMatcher.addURI(authority, Groups.TABLE_NAME + "/grougs_name", GROUPS_PROVIDERNAME);
		
		mUriMatcher.addURI(authority, Ecms.TABLE_NAME, ECM);
		mUriMatcher.addURI(authority, Ecms.TABLE_NAME + "/#", ECM_ID);

		tsMap.put(TransportStreams._ID, TransportStreams._ID);
		tsMap.put(TransportStreams.FREQUENCY, TransportStreams.FREQUENCY);
		tsMap.put(TransportStreams.DEVLIVERY_TYPE, TransportStreams.DEVLIVERY_TYPE);
		tsMap.put(TransportStreams.TUNE_PARAM, TransportStreams.TUNE_PARAM);
		tsMap.put(TransportStreams.TRANSPORT_STREAM_ID, TransportStreams.TRANSPORT_STREAM_ID);
		tsMap.put(TransportStreams.NETWORK_ID, TransportStreams.NETWORK_ID);
		tsMap.put(TransportStreams.ORIGINAL_NETWORK_ID, TransportStreams.ORIGINAL_NETWORK_ID);
		addTSMapPrivateColumn(tsMap);

		serviceMap.put(Services._ID, Services._ID);
		serviceMap.put(Services.PROGRAM_NUMBER, Services.PROGRAM_NUMBER);
		serviceMap.put(Services.CHANNEL_NAME, Services.CHANNEL_NAME);
		serviceMap.put(Services.CHANNEL_NAME_EN, Services.CHANNEL_NAME_EN);
		serviceMap.put(Services.CHANNEL_TYPE, Services.CHANNEL_TYPE);
		serviceMap.put(Services.CHANNEL_NUMBER, Services.CHANNEL_NUMBER);
		serviceMap.put(Services.SERVICE_TYPE, Services.SERVICE_TYPE);
		serviceMap.put(Services.EIT_PF_FLAG, Services.EIT_PF_FLAG);
		serviceMap.put(Services.EIT_SCHEDULE_FLAG, Services.EIT_SCHEDULE_FLAG);
		serviceMap.put(Services.IS_FREE_CA, Services.IS_FREE_CA);
		serviceMap.put(Services.SHORT_PROVIDER_NAME, Services.SHORT_PROVIDER_NAME);
		serviceMap.put(Services.SHORT_SERVICE_NAME, Services.SHORT_SERVICE_NAME);
		addServiceMapPrivateColumn(serviceMap);

		groupMap.put(Groups._ID, Groups._ID);
		groupMap.put(Groups.FREQUENCY, Groups.FREQUENCY);
		groupMap.put(Groups.GROUP_ID, Groups.GROUP_ID);
		groupMap.put(Groups.GROUP_NAME, Groups.GROUP_NAME);
		groupMap.put(Groups.PROGRAM_NUMBER, Groups.PROGRAM_NUMBER);
		addGroupsMapPrivateColumn(groupMap);

		eventMap.put(ServiceEvents._ID, ServiceEvents._ID);
		eventMap.put(ServiceEvents.FREQUENCY, ServiceEvents.FREQUENCY);
		eventMap.put(ServiceEvents.PROGRAM_NUMBER, ServiceEvents.PROGRAM_NUMBER);
		eventMap.put(ServiceEvents.EVENT_NAME, ServiceEvents.EVENT_NAME);
		eventMap.put(ServiceEvents.EVENT_NAME_EN, ServiceEvents.EVENT_NAME_EN);
		eventMap.put(ServiceEvents.START_TIME, ServiceEvents.START_TIME);
		eventMap.put(ServiceEvents.DURATION, ServiceEvents.DURATION);
		eventMap.put(ServiceEvents.EVENT_ID, ServiceEvents.EVENT_ID);
		eventMap.put(ServiceEvents.SHORT_EVENT_NAME, ServiceEvents.SHORT_EVENT_NAME);
		eventMap.put(ServiceEvents.IS_FREE_CA, ServiceEvents.IS_FREE_CA);
		eventMap.put(ServiceEvents.RUNNING_STATUS, ServiceEvents.RUNNING_STATUS);
		addEventMapPrivateColumn(eventMap);

		guidesMap.put(Guides._ID, Guides._ID);
		guidesMap.put(Guides.FREQUENCY, Guides.FREQUENCY);
		guidesMap.put(Guides.PROGRAM_NUMBER, Guides.PROGRAM_NUMBER);
		guidesMap.put(Guides.VERSION, Guides.VERSION);
		addGuidedMapPrivateColumn(guidesMap);

		streamMap.put(ElementaryStreams._ID, ElementaryStreams._ID);
		streamMap.put(ElementaryStreams.FREQUENCY, ElementaryStreams.FREQUENCY);
		streamMap.put(ElementaryStreams.PROGRAM_NUMBER, ElementaryStreams.PROGRAM_NUMBER);
		streamMap.put(ElementaryStreams.STREAM_TYPE, ElementaryStreams.STREAM_TYPE);
		streamMap.put(ElementaryStreams.STREAM_TYPE_NAME, ElementaryStreams.STREAM_TYPE_NAME);
		streamMap.put(ElementaryStreams.STREAM_PID, ElementaryStreams.STREAM_PID);
		streamMap.put(ElementaryStreams.COMPONENT_TAG, ElementaryStreams.COMPONENT_TAG);
		streamMap.put(ElementaryStreams.COMPONENT_TAG, ElementaryStreams.COMPONENT_TAG);
		addESMapPrivateColumn(streamMap);

		ecmMap.put(Ecms._ID, Ecms._ID);
		ecmMap.put(Ecms.FREQUENCY, Ecms.CA_SYSTEM_ID);
		ecmMap.put(Ecms.FREQUENCY, Ecms.FREQUENCY);
		ecmMap.put(Ecms.PROGRAM_NUMBER, Ecms.PROGRAM_NUMBER);
		ecmMap.put(Ecms.STREAM_PID, Ecms.STREAM_PID);
		ecmMap.put(Ecms.STREAM_PID, Ecms.ECM_PID);
		addEcmMapPrivateColumn(ecmMap);
		
		initBackup();
	}

	protected void initBackup() {
		//create bgroups table
		Log.i(TAG, "create backup groups table");
	}
	/**
	 * 用于子类添加新的Uri到UriMatcher中，path和code的length需一样，并且位置一一对应
	 * 
	 * @param path
	 *            子类不可定义父类中已有的path
	 * @param code
	 *            子类不可定义父类中已有的code
	 */
	protected void addExtraUris(String[] paths, int[] codes) {
		this.extraUriPaths = paths.clone();
		this.extraUriCodes = codes.clone();
		if(mUriMatcher== null)
			return;
		if (extraUriPaths != null && extraUriCodes != null) {
			if (extraUriPaths.length != extraUriCodes.length) {
				IPanelLog.d(TAG, "array extraUriPaths and extraUriCodes hava different length!");
				return;
			}
			// TODO check exsited path and code would be better
			for (int i = 0; i < extraUriPaths.length; i++) {
				mUriMatcher.addURI(authority, extraUriPaths[i], extraUriCodes[i]);
			}
		}
	}
	public String getUriType(Uri uri) {
		switch (mUriMatcher.match(uri)) {
		case TRANSPORT:
			return contentType;
		case TRANSPORT_ID:
			return contentItemType;
		case SERVICE:
			return contentType;
		case SERVICE_ID:
			return contentItemType;
		case GROUPS:
			return contentType;
		case GROUPS_ID:
			return contentItemType;
		case STREAM:
			return contentType;
		case STREAM_ID:
			return contentItemType;
		case EVENT:
			return contentType;
		case EVENT_ID:
			return contentItemType;
		case GUIDE:
			return contentType;
		case GUIDE_ID:
			return contentItemType;
		case ECM:
			return contentType;
		case ECM_ID:
			return contentItemType;
		default:
			throw new SQLException("no match uri:" + uri);
		}
	}

	public Uri insert(SQLiteDatabase db, Uri uri, ContentValues values, Context context) {
		Uri insertUri = null;
		long rowId;
		switch (mUriMatcher.match(uri)) {
		case GROUPS:
			IPanelLog.i(TAG, "-----------GROUPS");
			rowId = db.insert(Groups.TABLE_NAME, null, values);
			insertUri = ContentUris.withAppendedId(uri, rowId);
			return insertUri;
		case GROUPS_ID:
			IPanelLog.i(TAG, "-----------GROUPS_id");
			rowId = db.insert(Groups.TABLE_NAME, null, values);
			insertUri = ContentUris.withAppendedId(uri, rowId);
			return insertUri;
		case EVENT:
			rowId = db.insert(ServiceEvents.TABLE_NAME, "event_name_en", values);
			insertUri = ContentUris.withAppendedId(uri, rowId);
			return insertUri;
		case GUIDE:
			IPanelLog.i("EPG--update", "--------------------->go in provider");
			rowId = db.insert(Guides.TABLE_NAME, null, values);
			insertUri = ContentUris.withAppendedId(uri, rowId);
			// context.getContentResolver().notifyChange(uri, null);
			IPanelLog.i("EPG--update", "--------------------->go in provider insertUri=" + insertUri);
			return insertUri;
		case STREAM:
			IPanelLog.i(TAG, "-----------STREAM");
			rowId = db.insert(Streams.TABLE_NAME, null, values);
			insertUri = ContentUris.withAppendedId(uri, rowId);
			return insertUri;
		case STREAM_ID:
			IPanelLog.i(TAG, "-----------STREAM_ID");
			rowId = db.insert(Streams.TABLE_NAME, null, values);
			insertUri = ContentUris.withAppendedId(uri, rowId);
			return insertUri;
		case ECM:
			IPanelLog.i(TAG, "-----------ECM");
			rowId = db.insert(Ecms.TABLE_NAME, null, values);
			insertUri = ContentUris.withAppendedId(uri, rowId);
			return insertUri;
		case ECM_ID:
			IPanelLog.i(TAG, "-----------ECM_ID");
			rowId = db.insert(Ecms.TABLE_NAME, null, values);
			insertUri = ContentUris.withAppendedId(uri, rowId);
			return insertUri;
		default:
			throw new IllegalArgumentException("Unkwon Uri:" + uri.toString());
		}
	}

	public int delete(SQLiteDatabase db, Uri uri, String where, String[] whereArgs, Context context) {
		int count;
		switch (mUriMatcher.match(uri)) {
		case TRANSPORT:
			count = db.delete(TransportStreams.TABLE_NAME, where, whereArgs);
			break;
		case TRANSPORT_ID:
			String appId = uri.getPathSegments().get(1);
			count = db.delete(TransportStreams.TABLE_NAME, TransportStreams._ID + "=" + appId
					+ (!TextUtils.isEmpty(where) ? "AND (" + where + ')' : ""), whereArgs);
			break;
		case SERVICE:
			count = db.delete(Services.TABLE_NAME, where, whereArgs);
			break;
		case SERVICE_ID:
			String channelId = uri.getPathSegments().get(1);
			count = db.delete(Services.TABLE_NAME,
					Services._ID + "=" + channelId
							+ (!TextUtils.isEmpty(where) ? "AND (" + where + ')' : ""), whereArgs);
			break;
		case STREAM:
			count = db.delete(Streams.TABLE_NAME, where, whereArgs);
			break;
		case STREAM_ID:
			String streamsId = uri.getPathSegments().get(1);
			count = db.delete(Streams.TABLE_NAME,
					Streams._ID + "=" + streamsId
							+ (!TextUtils.isEmpty(where) ? "AND (" + where + ')' : ""), whereArgs);
			break;
		case ECM:
			count = db.delete(Ecms.TABLE_NAME, where, whereArgs);
			break;
		case ECM_ID:
			String ecmId = uri.getPathSegments().get(1);
			count = db.delete(Ecms.TABLE_NAME, Ecms._ID + "=" + ecmId
					+ (!TextUtils.isEmpty(where) ? "AND (" + where + ')' : ""), whereArgs);
			break;
		case EVENT:
		case FREQUENCY_EVENT:
			count = db.delete(ServiceEvents.TABLE_NAME, where, whereArgs);
			break;
		case EVENT_ID:
			String app = uri.getPathSegments().get(1);
			count = db.delete(ServiceEvents.TABLE_NAME,
					ServiceEvents._ID + "=" + app
							+ (!TextUtils.isEmpty(where) ? "AND (" + where + ')' : ""), whereArgs);
			break;
		case GUIDE:
			count = db.delete(Guides.TABLE_NAME, where, whereArgs);
			break;
		case GUIDE_ID:
			String app_guide = uri.getPathSegments().get(1);
			count = db.delete(Guides.TABLE_NAME,
					Guides._ID + "=" + app_guide
							+ (!TextUtils.isEmpty(where) ? "AND (" + where + ')' : ""), whereArgs);
			break;
		case GROUPS:
			count = db.delete(Groups.TABLE_NAME, where, whereArgs);
			IPanelLog.i(TAG, "delete where = "+ where);
			if (where.startsWith("1 = 1")){
				IPanelLog.i(TAG, "1 = 1");
				return count;
			}
			break;
		case GROUPS_ID:
			String groupsId = uri.getPathSegments().get(1);
			count = db.delete(Groups.TABLE_NAME,
					Groups._ID + "=" + groupsId
							+ (!TextUtils.isEmpty(where) ? "AND (" + where + ')' : ""), whereArgs);
			break;
		case GROUPS_PROVIDERNAME:
			count = db.delete(Groups.TABLE_NAME, where, whereArgs);
			break;
		default:
			throw new IllegalArgumentException("Unkown URI =" + uri);
		}
		context.getContentResolver().notifyChange(uri, null);
		return count;
	}

	public int update(SQLiteDatabase db, Uri uri, ContentValues values, String where,
			String[] whereArgs, Context context) {
		int count;
		switch (mUriMatcher.match(uri)) {
		case TRANSPORT:
			count = db.update(TransportStreams.TABLE_NAME, values, where, whereArgs);
			break;
		case TRANSPORT_ID:
			String appId = uri.getPathSegments().get(1);
			count = db.update(TransportStreams.TABLE_NAME, values, TransportStreams._ID + "="
					+ appId + (!TextUtils.isEmpty(where) ? "AND (" + where + ')' : ""), whereArgs);
			break;
		case GUIDE:
			count = db.update(Guides.TABLE_NAME, values, where, whereArgs);
			break;
		case GUIDE_ID:
			String app_guide = uri.getPathSegments().get(1);
			count = db.update(Guides.TABLE_NAME, values,
					Guides._ID + "=" + app_guide
							+ (!TextUtils.isEmpty(where) ? "AND (" + where + ')' : ""), whereArgs);
			break;
		case SERVICE:
			count = db.update(Services.TABLE_NAME, values, where, whereArgs);
			break;
		case SERVICE_ID:
			String channelId = uri.getPathSegments().get(1);
			count = db.update(Services.TABLE_NAME, values, Services._ID + "=" + channelId
					+ (!TextUtils.isEmpty(where) ? "AND (" + where + ')' : ""), whereArgs);
			break;
		case GROUPS:
			count = db.update(Groups.TABLE_NAME, values, where, whereArgs);
			break;
		case GROUPS_ID:
			String groupsId = uri.getPathSegments().get(1);
			count = db.update(Groups.TABLE_NAME, values,
					Groups._ID + "=" + groupsId
							+ (!TextUtils.isEmpty(where) ? "AND (" + where + ')' : ""), whereArgs);
			break;
		default:
			throw new IllegalArgumentException("Unkown URI =" + uri);
		}
		context.getContentResolver().notifyChange(uri, null);
		return count;
	}

	public Cursor query(SQLiteDatabase db, Uri uri, String[] projection, String selection,
			String[] selectionArgs, String sortOrder) {
		SQLiteQueryBuilder qb = new SQLiteQueryBuilder();

		switch (mUriMatcher.match(uri)) {
		case TRANSPORT:
			qb.setTables(TransportStreams.TABLE_NAME);
			if (enableProjection)
				qb.setProjectionMap(tsMap);
			break;
		case TRANSPORT_ID:
			qb.setTables(TransportStreams.TABLE_NAME);
			if (enableProjection)
				qb.setProjectionMap(tsMap);
			qb.appendWhere(TransportStreams._ID + "=" + uri.getPathSegments().get(1));
			break;
		case SERVICE:
			qb.setTables(Services.TABLE_NAME);
			if (enableProjection)
				qb.setProjectionMap(serviceMap);
			break;
		case SERVICE_ID:
			qb.setTables(Services.TABLE_NAME);
			if (enableProjection)
				qb.setProjectionMap(serviceMap);
			qb.appendWhere(Services._ID + "=" + uri.getPathSegments().get(1));
			break;
		case ECM:
			qb.setTables(Ecms.TABLE_NAME);
			if (enableProjection)
				qb.setProjectionMap(ecmMap);
			break;
		case ECM_ID:
			qb.setTables(Ecms.TABLE_NAME);
			if (enableProjection)
				qb.setProjectionMap(ecmMap);
			qb.appendWhere(Ecms._ID + "=" + uri.getPathSegments().get(1));
			break;
		case EVENT:
			qb.setTables(ServiceEvents.TABLE_NAME);
			if (enableProjection)
				qb.setProjectionMap(eventMap);
			break;
		case EVENT_ID:
			qb.setTables(ServiceEvents.TABLE_NAME);
			if (enableProjection)
				qb.setProjectionMap(eventMap);
			qb.appendWhere(ServiceEvents._ID + "=" + uri.getPathSegments().get(1));
			break;
		case GUIDE:
			qb.setTables(Guides.TABLE_NAME);
			if (enableProjection)
				qb.setProjectionMap(eventMap);
			break;
		case GUIDE_ID:
			qb.setTables(Guides.TABLE_NAME);
			if (enableProjection)
				qb.setProjectionMap(guidesMap);
			qb.appendWhere(Guides._ID + "=" + uri.getPathSegments().get(1));
			break;
		case STREAM:
			qb.setTables(Streams.TABLE_NAME);
			if (enableProjection)
				qb.setProjectionMap(streamMap);
			break;
		case STREAM_ID:
			qb.setTables(Streams.TABLE_NAME);
			if (enableProjection)
				qb.setProjectionMap(streamMap);
			qb.appendWhere(Streams._ID + "=" + uri.getPathSegments().get(1));
			break;
		case GROUPS:
			qb.setTables(Groups.TABLE_NAME);
			if (enableProjection)
				qb.setProjectionMap(groupMap);
			break;
		case GROUPS_ID:
			qb.setTables(Groups.TABLE_NAME);
			if (enableProjection)
				qb.setProjectionMap(groupMap);
			qb.appendWhere(Groups._ID + "=" + uri.getPathSegments().get(1));
			break;
		default:
			throw new IllegalArgumentException("Unknown URI=" + uri);
		}
		return qb.query(db, projection, selection, selectionArgs, null, null, null);
	}

	public static void createTables(SQLiteDatabase db) {
		IPanelLog.d(TAG, "createTables:========");
		IPanelLog.d(TAG, createTSTableSql);
		IPanelLog.d(TAG, createServiceTableSql);
		IPanelLog.d(TAG, createGroupsTableSql);
		IPanelLog.d(TAG, createESTableSql);
		IPanelLog.d(TAG, createEventTableSql);
		IPanelLog.d(TAG, createGuidesTableSql);
		IPanelLog.d(TAG, createEcmTableSql);
		db.execSQL(createTSTableSql);
		db.execSQL(createServiceTableSql);
		db.execSQL(createGroupsTableSql);
		db.execSQL(createESTableSql);
		db.execSQL(createEventTableSql);
		db.execSQL(createGuidesTableSql);
		db.execSQL(createEcmTableSql);
	}

	public static void dropTables(SQLiteDatabase db) {
		IPanelLog.i(TAG, "dropTables ============== ");
		db.execSQL("DROP TABLE IF EXISTS " + Services.TABLE_NAME);
		db.execSQL("DROP TABLE IF EXISTS " + TransportStreams.TABLE_NAME);
		db.execSQL("DROP TABLE IF EXISTS " + ServiceEvents.TABLE_NAME);
		db.execSQL("DROP TABLE IF EXISTS " + Guides.TABLE_NAME);
		db.execSQL("DROP TABLE IF EXISTS " + Streams.TABLE_NAME);
		db.execSQL("DROP TABLE IF EXISTS " + Groups.TABLE_NAME);
		db.execSQL("DROP TABLE IF EXISTS " + Ecms.TABLE_NAME);
	}

	public static void createNetworkTables(SQLiteDatabase db){
		Log.i(TAG, "createNetworkTables==============");
		db.execSQL(createTSTableSql);
		db.execSQL(createServiceTableSql);
		db.execSQL(createESTableSql);
		db.execSQL(createGuidesTableSql);
		db.execSQL(createEcmTableSql);
	}
	
	public static void dropNetworkTables(SQLiteDatabase db){
		Log.i(TAG, "dropNetworkTables ============== ");
		db.execSQL("DROP TABLE IF EXISTS " + Services.TABLE_NAME);
		db.execSQL("DROP TABLE IF EXISTS " + TransportStreams.TABLE_NAME);
		db.execSQL("DROP TABLE IF EXISTS " + Guides.TABLE_NAME);
		db.execSQL("DROP TABLE IF EXISTS " + Groups.TABLE_NAME);
		db.execSQL("DROP TABLE IF EXISTS " + Streams.TABLE_NAME);
		db.execSQL("DROP TABLE IF EXISTS " + Ecms.TABLE_NAME);
	}
	
	public static void dropBouquetTables(SQLiteDatabase db){
		Log.i(TAG, "dropBouquetTables ============== ");
		db.execSQL("DROP TABLE IF EXISTS " + Groups.TABLE_NAME);
	}
	
	public static void createBouquetTables(SQLiteDatabase db){
		Log.i(TAG, "ready to createBouquetTables");
		db.execSQL(createGroupsTableSql);
	}
	
	public static void dropEittodayTables(SQLiteDatabase db){
		Log.i(TAG, "dropEittodayTables ============== ");
		db.execSQL("DROP TABLE IF EXISTS " + Events.TABLE_NAME);
	}
	
	public static void createEittodayTables(SQLiteDatabase db){
		Log.i(TAG, "ready to createEittodayTables");
		db.execSQL(createEventTableSql);
	}
	
	public static void createNetworkTablesByLevel(SQLiteDatabase db, int val){
		Log.i(TAG, "createNetworkTablesByLevel==========val="+val);
		switch (val) {
		case 0x1 << 1:
			db.execSQL(createTSTableSql);
			break;
		case 0x1 << 2:
			db.execSQL(createServiceTableSql);
			break;
		case 0x1 << 3:
			db.execSQL(createGuidesTableSql);
			break;
		case 0x1 << 4:
			db.execSQL(createESTableSql);
			break;
		case 0x1 << 5:
			db.execSQL(createEcmTableSql);
			break;
		default:
			Log.i(TAG, "---create------no use level:"+val);
			break;
		}
	}
	
	public static void dropNetworkTablesByLevel(SQLiteDatabase db, int val){
		Log.i(TAG, "dropNetworkTablesByLevel ==============val="+val);
		switch (val) {
		case 0x1 << 1:
			db.execSQL("DROP TABLE IF EXISTS " + TransportStreams.TABLE_NAME);
			break;
		case 0x1 << 2:
			db.execSQL("DROP TABLE IF EXISTS " + Services.TABLE_NAME);
			break;
		case 0x1 << 3:
			db.execSQL("DROP TABLE IF EXISTS " + Guides.TABLE_NAME);
			break;
		case 0x1 << 4:
			db.execSQL("DROP TABLE IF EXISTS " + Streams.TABLE_NAME);
			break;
		case 0x1 << 5:
			db.execSQL("DROP TABLE IF EXISTS " + Ecms.TABLE_NAME);
		case 0x1 << 6:
			db.execSQL("DROP TABLE IF EXISTS " + Groups.TABLE_NAME);
			break;
		default:
			Log.i(TAG, "------drop---no use level:"+val);
			break;
		}
	}
	
	public static void deleteEvent(SQLiteDatabase db, long freq) {
		String sql = "DELETE FROM " + ServiceEvents.TABLE_NAME + " WHERE frequency = '" + freq
				+ "';";
		db.execSQL(sql);
	}

	public static void deleteServices(SQLiteDatabase db, long freq) {
		String sql = "DELETE FROM " + Services.TABLE_NAME + " WHERE frequency = '" + freq + "';";
		db.execSQL(sql);
	}

	public static void deleteFrequnecys(SQLiteDatabase db, long freq) {
		String sql = "DELETE FROM " + TransportStreams.TABLE_NAME + " WHERE frequency = '" + freq
				+ "';";
		db.execSQL(sql);
	}

	public static void deleteStreams(SQLiteDatabase db, long freq) {
		String sql = "DELETE FROM " + ElementaryStreams.TABLE_NAME + " WHERE frequency = '" + freq
				+ "';";
		db.execSQL(sql);
	}

	public static void deleteGuides(SQLiteDatabase db, long freq) {
		String sql = "DELETE FROM " + Guides.TABLE_NAME + " WHERE frequency = '" + freq + "';";
		db.execSQL(sql);
	}

	public static void deleteGroups(SQLiteDatabase db, long freq) {
		String sql = "DELETE FROM " + Groups.TABLE_NAME + " WHERE frequency = '" + freq + "';";
		db.execSQL(sql);
	}

	public static void deleteEcms(SQLiteDatabase db, long freq) {
		String sql = "DELETE FROM " + Ecms.TABLE_NAME + " WHERE frequency = '" + freq + "';";
		db.execSQL(sql);
	}
	

	void createTSTableSqlString() {
		StringBuffer sb = new StringBuffer();
		sb.append("CREATE TABLE IF NOT EXISTS ");
		sb.append(DvbNetworkDatabase.TransportStreams.TABLE_NAME);
		sb.append(" (");
		sb.append(TransportStreams._ID).append(" INTEGER PRIMARY KEY,");
		sb.append(TransportStreams.FREQUENCY).append(" INT,");
		sb.append(TransportStreams.DEVLIVERY_TYPE).append(" INT,");
		sb.append(TransportStreams.TRANSPORT_STREAM_ID).append(" INT,");
		onCreateTSTableSqlString(sb);
		sb.append(TransportStreams.MPEG_TRANSPORT_STREAM_ID).append(" INT,");
		sb.append(TransportStreams.NETWORK_ID).append(" INT,");
		sb.append(TransportStreams.ORIGINAL_NETWORK_ID).append(" INT,");
		sb.append(TransportStreams.INFO_VERSION).append(" INT,");
		sb.append(TransportStreams.TUNE_PARAM).append(" TEXT);");
		createTSTableSql = sb.toString();
	}
	/**
	 * 子类实现以向freqency表中添加列
	 * @param sb
	 */
	protected void onCreateTSTableSqlString(StringBuffer sb){
		
	}
	
	protected void addTSMapColumn(List<String> list){
		for (String string : list) {
			tsMap.put(string, string);
		}
	}
	
	protected void addTSMapPrivateColumn(HashMap<String, String> mapValue){
		
	}

	void createServiceTableSqlString() {
		StringBuffer sb = new StringBuffer();
		sb.append("CREATE TABLE IF NOT EXISTS ");
		sb.append(Services.TABLE_NAME);
		sb.append(" (");
		sb.append(Services._ID).append(" INTEGER PRIMARY KEY,");
		sb.append(Services.FREQUENCY).append(" INT,");
		sb.append(Services.PROGRAM_NUMBER).append(" INT,");
		sb.append(TransportStreams.TRANSPORT_STREAM_ID).append(" INT,");
		sb.append(Services.CHANNEL_TYPE).append(" INT,");
		sb.append(Services.CHANNEL_NUMBER).append(" INT,");
		sb.append(Services.CHANNEL_NAME).append(" TEXT,");
		sb.append(Services.CHANNEL_NAME_EN).append(" TEXT,");
		onCreateServiceTableSqlString(sb);
		sb.append(Services.PROVIDER_NAME).append(" TEXT,");
		sb.append(Services.SERVICE_TYPE).append(" INT,");
		sb.append(Services.EIT_PF_FLAG).append(" INT,");
		sb.append(Services.EIT_SCHEDULE_FLAG).append(" INT,");
		sb.append(Services.IS_FREE_CA).append(" INT,");
		sb.append(Streams.PRESENTING_FORM).append(" INT,");
		sb.append(Services.SHORT_PROVIDER_NAME).append(" TEXT,");
		sb.append(Services.SHORT_SERVICE_NAME).append(" TEXT);");
		createServiceTableSql = sb.toString();
	}
	
	/**
	 * 子类实现以向channels表中添加列
	 * @param sb
	 */
	protected void onCreateServiceTableSqlString(StringBuffer sb){
		
	}

	protected void addServiceMapColumn(List<String> list){
		for (String string : list) {
			serviceMap.put(string, string);
		}
	}
	
	protected void addServiceMapPrivateColumn(HashMap<String, String> mapValue){
		
	}
	
	void createGroupsTableSqlString() {
		StringBuffer sb = new StringBuffer();
		sb.append("CREATE TABLE IF NOT EXISTS ");
		sb.append(Groups.TABLE_NAME);
		sb.append(" (");
		sb.append(Groups._ID).append(" INTEGER PRIMARY KEY,");
		sb.append(Groups.FREQUENCY).append(" INT,");
		sb.append(Groups.GROUP_ID).append(" INT,");
		sb.append(Groups.PROGRAM_NUMBER).append(" INT,");
		onCreateGroupsTableSqlString(sb);
		sb.append(Groups.GROUP_NAME).append(" TEXT);");
		createGroupsTableSql = sb.toString();
	}

	/**
	 * 子类实现以向groups表中添加列
	 * @param sb
	 */
	protected void onCreateGroupsTableSqlString(StringBuffer sb){
		
	}
	
	protected void addGroupsMapColumn(List<String> list){
		for (String string : list) {
			groupMap.put(string, string);
		}
	}
	
	protected void addGroupsMapPrivateColumn(HashMap<String, String> mapValue){
		
	}
	
	void createESTableSqlString() {
		StringBuffer sb = new StringBuffer();
		sb.append("CREATE TABLE IF NOT EXISTS ");
		sb.append(Streams.TABLE_NAME);
		sb.append(" (");
		sb.append(ElementaryStreams._ID).append(" INTEGER PRIMARY KEY,");
		sb.append(ElementaryStreams.FREQUENCY).append(" INT,");
		sb.append(ElementaryStreams.PROGRAM_NUMBER).append(" INT,");
		sb.append(ElementaryStreams.STREAM_TYPE).append(" INT,");
		sb.append(ElementaryStreams.STREAM_PID).append(" INT,");
		onCreateESTableSqlString(sb);
		sb.append(ElementaryStreams.ASSOCIATION_TAG).append(" INT,");
		sb.append(ElementaryStreams.COMPONENT_TAG).append(" INT,");
		sb.append(ElementaryStreams.STREAM_TYPE_NAME).append(" TEXT);");
		createESTableSql = sb.toString();
	}
	
	/**
	 * 子类实现以向streams表中添加列
	 * @param sb
	 */
	protected void onCreateESTableSqlString(StringBuffer sb){
		
	}

	protected void addESMapColumn(List<String> list){
		for (String string : list) {
			streamMap.put(string, string);
		}
	}
	
	protected void addESMapPrivateColumn(HashMap<String, String> mapValue){
		
	}
	
	void createEcmTableSqlString() {
		StringBuffer sb = new StringBuffer();
		sb.append("CREATE TABLE IF NOT EXISTS ");
		sb.append(Ecms.TABLE_NAME);
		sb.append(" (");
		sb.append(Ecms._ID).append(" INTEGER PRIMARY KEY,");
		sb.append(Ecms.FREQUENCY).append(" INT,");
		sb.append(Ecms.PROGRAM_NUMBER).append(" INT,");
		onCreateEcmTableSqlString(sb);
		sb.append(Ecms.STREAM_PID).append(" INT,");
		sb.append(Ecms.CA_SYSTEM_ID).append(" INT,");
		sb.append(Ecms.ECM_PID).append(" INT);");
		createEcmTableSql = sb.toString();
	}

	/**
	 * 子类实现以向ecms表中添加列
	 * @param sb
	 */
	protected void onCreateEcmTableSqlString(StringBuffer sb){
		
	}

	protected void addEcmMapColumn(List<String> list){
		for (String string : list) {
			ecmMap.put(string, string);
		}
	}
	
	protected void addEcmMapPrivateColumn(HashMap<String, String> mapValue){
	
	}
	
	void createEventTableSqlString() {
		StringBuffer sb = new StringBuffer();
		sb.append("CREATE TABLE IF NOT EXISTS ");
		sb.append(ServiceEvents.TABLE_NAME);
		sb.append(" (");
		sb.append(ServiceEvents._ID).append(" INTEGER PRIMARY KEY,");
		sb.append(ServiceEvents.FREQUENCY).append(" INT,");
		sb.append(ServiceEvents.PROGRAM_NUMBER).append(" INT,");
		sb.append(ServiceEvents.EVENT_NAME).append(" TEXT,");
		sb.append(ServiceEvents.EVENT_NAME_EN).append(" TEXT,");
		sb.append(ServiceEvents.START_TIME).append(" INT,");
		sb.append(ServiceEvents.END_TIME).append(" INT,");
		sb.append(ServiceEvents.DURATION).append(" INT,");
		onCreateEventTableSqlString(sb);
		sb.append(ServiceEvents.EVENT_ID).append(" INT,");
		sb.append(ServiceEvents.IS_FREE_CA).append(" INT,");
		sb.append(ServiceEvents.RUNNING_STATUS).append(" INT,");
		sb.append(ServiceEvents.SHORT_EVENT_NAME).append(" TEXT);");
		createEventTableSql = sb.toString();
	}
	
	/**
	 * 子类实现以向events表中添加列
	 * @param sb
	 */
	protected void onCreateEventTableSqlString(StringBuffer sb){
		
	}

	protected void addEventMapColumn(List<String> list){
		for (String string : list) {
			eventMap.put(string, string);
		}
	}
	
	protected void addEventMapPrivateColumn(HashMap<String, String> mapValue){
		
	}
	
    void createGuidedTableSqlString() {
		StringBuffer sb = new StringBuffer();
		sb.append("CREATE TABLE IF NOT EXISTS ");
		sb.append(Guides.TABLE_NAME);
		sb.append(" (");
		sb.append(Guides._ID).append(" INTEGER PRIMARY KEY,");
		sb.append(Guides.FREQUENCY).append(" INT,");
		sb.append(Guides.PROGRAM_NUMBER).append(" INT,");
		onCreateGuidedTableSqlString(sb);
		sb.append(Guides.VERSION).append(" INT);");
		createGuidesTableSql = sb.toString();
	}

	/**
	 * 子类实现以向guides表中添加列
	 * @param sb
	 */
	protected void onCreateGuidedTableSqlString(StringBuffer sb){
		
	}
	
	protected void addGuidedMapColumn(List<String> list){
		for (String string : list) {
			guidesMap.put(string, string);
		}
	}
	
	protected void addGuidedMapPrivateColumn(HashMap<String, String> mapValue){
	
	}
	
	void initTableString(){
		createTSTableSqlString();
		createServiceTableSqlString();
		createGroupsTableSqlString();
		createESTableSqlString();
		createEcmTableSqlString();
		createEventTableSqlString();
		createGuidedTableSqlString();
		
		initBackupTableSqlString();
	}
	
	protected void initBackupTableSqlString() {
		
	}
}
