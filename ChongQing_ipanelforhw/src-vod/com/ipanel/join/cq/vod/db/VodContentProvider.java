package com.ipanel.join.cq.vod.db;

import java.util.HashMap;

import android.content.ContentProvider;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.UriMatcher;
import android.content.res.Resources;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteQueryBuilder;
import android.net.Uri;
import android.provider.BaseColumns;
import android.text.TextUtils;

import com.ipanel.join.cq.vod.db.VodContentProviderMetaData.AllfavoritesInfo;
import com.ipanel.join.cq.vod.db.VodContentProviderMetaData.PlayedInfo;

public class VodContentProvider extends ContentProvider {

	private SQLiteHelper dh;
	public static final UriMatcher uriMatcher;
	public static final int INCOMING_ALLFAVORITESINFO_COLLECTION = 101;
	public static final int INCOMING_ALLFAVORITESINFO_SINGLE = 201;

	public static final int INCOMING_PLAYEDINFO_COLLECTION = 102;
	public static final int INCOMING_PLAYEDINFO_SINGLE = 202;
	static {
		uriMatcher = new UriMatcher(UriMatcher.NO_MATCH);
		uriMatcher.addURI(VodContentProviderMetaData.AUTHORITY, "allfavoritesinfo",
				INCOMING_ALLFAVORITESINFO_COLLECTION);
		uriMatcher.addURI(VodContentProviderMetaData.AUTHORITY, "allfavoritesinfo/#",
				INCOMING_ALLFAVORITESINFO_COLLECTION);
		uriMatcher.addURI(VodContentProviderMetaData.AUTHORITY, "playedinfo", INCOMING_PLAYEDINFO_COLLECTION);
		uriMatcher.addURI(VodContentProviderMetaData.AUTHORITY, "playedinfo/#", INCOMING_PLAYEDINFO_SINGLE);
	}

	public static HashMap<String, String> allfavoritesinfoProjectionMap;
	public static HashMap<String, String> playedinfoProjectionMap;
	static {
		allfavoritesinfoProjectionMap = new HashMap<String, String>();
		allfavoritesinfoProjectionMap.put(BaseColumns._ID, BaseColumns._ID);
		allfavoritesinfoProjectionMap.put(AllfavoritesInfo.VODID, AllfavoritesInfo.VODID);
		allfavoritesinfoProjectionMap.put(AllfavoritesInfo.TYPEID, AllfavoritesInfo.TYPEID);
		allfavoritesinfoProjectionMap.put(AllfavoritesInfo.VODNAME, AllfavoritesInfo.VODNAME);
		allfavoritesinfoProjectionMap.put(AllfavoritesInfo.DIRECTOR, AllfavoritesInfo.DIRECTOR);
		allfavoritesinfoProjectionMap.put(AllfavoritesInfo.ACTOR, AllfavoritesInfo.ACTOR);
		allfavoritesinfoProjectionMap.put(AllfavoritesInfo.INTR, AllfavoritesInfo.INTR);
		allfavoritesinfoProjectionMap.put(AllfavoritesInfo.PLAYTYPE, AllfavoritesInfo.PLAYTYPE);
		allfavoritesinfoProjectionMap.put(AllfavoritesInfo.PICPATH, AllfavoritesInfo.PICPATH);
		allfavoritesinfoProjectionMap.put(AllfavoritesInfo.ELAPSETIME, AllfavoritesInfo.ELAPSETIME);
		allfavoritesinfoProjectionMap.put(AllfavoritesInfo.TOTALNUM, AllfavoritesInfo.TOTALNUM);
		allfavoritesinfoProjectionMap.put(AllfavoritesInfo.VODIDLIST, AllfavoritesInfo.VODIDLIST);
		allfavoritesinfoProjectionMap.put(AllfavoritesInfo.ISAFTERPLAY, AllfavoritesInfo.ISAFTERPLAY);
		allfavoritesinfoProjectionMap.put(AllfavoritesInfo.CODE, AllfavoritesInfo.CODE);

		playedinfoProjectionMap = new HashMap<String, String>();
		playedinfoProjectionMap.put(BaseColumns._ID, BaseColumns._ID);
		playedinfoProjectionMap.put(PlayedInfo.VODID, PlayedInfo.VODID);
		playedinfoProjectionMap.put(PlayedInfo.TYPEID, PlayedInfo.TYPEID);
		playedinfoProjectionMap.put(PlayedInfo.VODNAME, PlayedInfo.VODNAME);
		playedinfoProjectionMap.put(PlayedInfo.DIRECTOR, PlayedInfo.DIRECTOR);
		playedinfoProjectionMap.put(PlayedInfo.ACTOR, PlayedInfo.ACTOR);
		playedinfoProjectionMap.put(PlayedInfo.INTR, PlayedInfo.INTR);
		playedinfoProjectionMap.put(PlayedInfo.PLAYTYPE, PlayedInfo.PLAYTYPE);
		playedinfoProjectionMap.put(PlayedInfo.PICPATH, PlayedInfo.PICPATH);
		playedinfoProjectionMap.put(PlayedInfo.ELAPSETIME, PlayedInfo.ELAPSETIME);
		playedinfoProjectionMap.put(PlayedInfo.TOTALNUM, PlayedInfo.TOTALNUM);
		playedinfoProjectionMap.put(PlayedInfo.VODIDLIST, PlayedInfo.VODIDLIST);
		playedinfoProjectionMap.put(PlayedInfo.ISOVER, PlayedInfo.ISOVER);
		playedinfoProjectionMap.put(PlayedInfo.LASTEPOSDIDE, PlayedInfo.LASTEPOSDIDE);
		playedinfoProjectionMap.put(PlayedInfo.URL, PlayedInfo.URL);
		playedinfoProjectionMap.put(PlayedInfo.TIME, PlayedInfo.TIME);
		playedinfoProjectionMap.put(PlayedInfo.SHOWTIME, PlayedInfo.SHOWTIME);
		playedinfoProjectionMap.put(PlayedInfo.WATCHTIME, PlayedInfo.WATCHTIME);
		playedinfoProjectionMap.put(PlayedInfo.TIMERATIO, PlayedInfo.TIMERATIO);
		playedinfoProjectionMap.put(PlayedInfo.CODE, PlayedInfo.CODE);
	}

	@Override
	public int delete(Uri uri, String selection, String[] selectionArgs) {
		SQLiteDatabase db = dh.getWritableDatabase();
		String TABLE_NAME;
		int count;
		switch (uriMatcher.match(uri)) {
		case INCOMING_ALLFAVORITESINFO_COLLECTION:
			TABLE_NAME = AllfavoritesInfo.ALLFAVORITESINFO_TABLE_NAME;
			break;
		case INCOMING_ALLFAVORITESINFO_SINGLE:
			TABLE_NAME = AllfavoritesInfo.ALLFAVORITESINFO_TABLE_NAME;
			selection = BaseColumns._ID + "=" + uri.getPathSegments().get(1)
					+ (!TextUtils.isEmpty(selection) ? " AND (" + selection + ')' : "");
			break;
		case INCOMING_PLAYEDINFO_COLLECTION:
			TABLE_NAME = PlayedInfo.PLAYEDINFO_TABLE_NAME;
			break;
		case INCOMING_PLAYEDINFO_SINGLE:
			TABLE_NAME = PlayedInfo.PLAYEDINFO_TABLE_NAME;
			selection = BaseColumns._ID + "=" + uri.getPathSegments().get(1)
					+ (!TextUtils.isEmpty(selection) ? " AND (" + selection + ')' : "");
			break;
		default:
			throw new IllegalArgumentException("Unknown URI " + uri);
		}
		count = db.delete(TABLE_NAME, selection, selectionArgs);
		getContext().getContentResolver().notifyChange(uri, null);
		return count;
	}

	@Override
	public String getType(Uri uri) {
		return null;
	}

	@Override
	public Uri insert(Uri uri, ContentValues values) {
		String TABLE_NAME;
		String nullColumnHack;
		Resources r = Resources.getSystem();
		if (values == null) {
			values = new ContentValues();
		} else {
			values = new ContentValues(values);
		}
		switch (uriMatcher.match(uri)) {
		case INCOMING_ALLFAVORITESINFO_COLLECTION:
			TABLE_NAME = AllfavoritesInfo.ALLFAVORITESINFO_TABLE_NAME;
			nullColumnHack = AllfavoritesInfo.VODID;
			if (values.containsKey(AllfavoritesInfo.VODID) == false) {
				values.put(AllfavoritesInfo.VODID, r.getString(android.R.string.untitled));
			}
			if (values.containsKey(AllfavoritesInfo.TYPEID) == false) {
				values.put(AllfavoritesInfo.TYPEID, "");
			}
			if (values.containsKey(AllfavoritesInfo.VODNAME) == false) {
				values.put(AllfavoritesInfo.VODNAME, "");
			}
			if (values.containsKey(AllfavoritesInfo.DIRECTOR) == false) {
				values.put(AllfavoritesInfo.DIRECTOR, "");
			}
			if (values.containsKey(AllfavoritesInfo.ACTOR) == false) {
				values.put(AllfavoritesInfo.ACTOR, "");
			}
			if (values.containsKey(AllfavoritesInfo.INTR) == false) {
				values.put(AllfavoritesInfo.INTR, "");
			}
			if (values.containsKey(AllfavoritesInfo.PLAYTYPE) == false) {
				values.put(AllfavoritesInfo.PLAYTYPE, "");
			}
			if (values.containsKey(AllfavoritesInfo.PICPATH) == false) {
				values.put(AllfavoritesInfo.PICPATH, "");
			}
			if (values.containsKey(AllfavoritesInfo.ELAPSETIME) == false) {
				values.put(AllfavoritesInfo.ELAPSETIME, "");
			}
			if (values.containsKey(AllfavoritesInfo.TOTALNUM) == false) {
				values.put(AllfavoritesInfo.TOTALNUM, "");
			}
			if (values.containsKey(AllfavoritesInfo.VODIDLIST) == false) {
				values.put(AllfavoritesInfo.VODIDLIST, "");
			}
			if (values.containsKey(AllfavoritesInfo.ISAFTERPLAY) == false) {
				values.put(AllfavoritesInfo.ISAFTERPLAY, "");
			}
			if (values.containsKey(AllfavoritesInfo.CODE) == false) {
				values.put(AllfavoritesInfo.CODE, "");
			}
			break;
		case INCOMING_PLAYEDINFO_COLLECTION:
			TABLE_NAME = PlayedInfo.PLAYEDINFO_TABLE_NAME;
			nullColumnHack = PlayedInfo.VODID;
			if (values.containsKey(PlayedInfo.VODID) == false) {
				values.put(PlayedInfo.VODID, r.getString(android.R.string.untitled));
			}
			if (values.containsKey(PlayedInfo.TYPEID) == false) {
				values.put(PlayedInfo.TYPEID, "");
			}
			if (values.containsKey(PlayedInfo.VODNAME) == false) {
				values.put(PlayedInfo.VODNAME, "");
			}
			if (values.containsKey(PlayedInfo.DIRECTOR) == false) {
				values.put(PlayedInfo.DIRECTOR, "");
			}
			if (values.containsKey(PlayedInfo.ACTOR) == false) {
				values.put(PlayedInfo.ACTOR, "");
			}
			if (values.containsKey(PlayedInfo.INTR) == false) {
				values.put(PlayedInfo.INTR, "");
			}
			if (values.containsKey(PlayedInfo.PLAYTYPE) == false) {
				values.put(PlayedInfo.PLAYTYPE, "");
			}
			if (values.containsKey(PlayedInfo.PICPATH) == false) {
				values.put(PlayedInfo.PICPATH, "");
			}
			if (values.containsKey(PlayedInfo.ELAPSETIME) == false) {
				values.put(PlayedInfo.ELAPSETIME, "");
			}
			if (values.containsKey(PlayedInfo.TOTALNUM) == false) {
				values.put(PlayedInfo.TOTALNUM, "");
			}
			if (values.containsKey(PlayedInfo.VODIDLIST) == false) {
				values.put(PlayedInfo.VODIDLIST, "");
			}
			if (values.containsKey(PlayedInfo.ISOVER) == false) {
				values.put(PlayedInfo.ISOVER, "");
			}
			if (values.containsKey(PlayedInfo.LASTEPOSDIDE) == false) {
				values.put(PlayedInfo.LASTEPOSDIDE, "");
			}
			if (values.containsKey(PlayedInfo.URL) == false) {
				values.put(PlayedInfo.URL, "");
			}
			if (values.containsKey(PlayedInfo.TIME) == false) {
				values.put(PlayedInfo.TIME, "");
			}
			if (values.containsKey(PlayedInfo.SHOWTIME) == false) {
				values.put(PlayedInfo.SHOWTIME, "");
			}
			if (values.containsKey(PlayedInfo.WATCHTIME) == false) {
				values.put(PlayedInfo.WATCHTIME, "");
			}
			if (values.containsKey(PlayedInfo.CODE) == false) {
				values.put(PlayedInfo.CODE, "");
			}
			break;

		default:
			throw new IllegalArgumentException("Unknown URI " + uri);
		}
		SQLiteDatabase db = dh.getWritableDatabase();
		long rowId = db.insert(TABLE_NAME, nullColumnHack, values);
		if (rowId > 0) {
			Uri returnUri = ContentUris.withAppendedId(uri, rowId);
			getContext().getContentResolver().notifyChange(returnUri, null);
			return returnUri;
		}
		throw new SQLException("Failed to insert row into " + uri);
	}

	@Override
	public boolean onCreate() {
		dh = new SQLiteHelper(getContext(), VodContentProviderMetaData.DATABASE_NAME, null,
				VodContentProviderMetaData.DATABASE_VERSION);
		return true;
	}

	@Override
	public Cursor query(Uri uri, String[] projection, String selection, String[] selectionArgs, String orderBy) {
		SQLiteQueryBuilder qb = new SQLiteQueryBuilder();
		switch (uriMatcher.match(uri)) {
		case INCOMING_ALLFAVORITESINFO_COLLECTION:
			qb.setTables(AllfavoritesInfo.ALLFAVORITESINFO_TABLE_NAME);
			qb.setProjectionMap(allfavoritesinfoProjectionMap);
			break;
		case INCOMING_ALLFAVORITESINFO_SINGLE:
			qb.setTables(AllfavoritesInfo.ALLFAVORITESINFO_TABLE_NAME);
			qb.setProjectionMap(allfavoritesinfoProjectionMap);
			qb.appendWhere(BaseColumns._ID + "=" + uri.getPathSegments().get(1));
			break;
		case INCOMING_PLAYEDINFO_COLLECTION:
			qb.setTables(PlayedInfo.PLAYEDINFO_TABLE_NAME);
			qb.setProjectionMap(playedinfoProjectionMap);
			break;
		case INCOMING_PLAYEDINFO_SINGLE:
			qb.setTables(PlayedInfo.PLAYEDINFO_TABLE_NAME);
			qb.setProjectionMap(playedinfoProjectionMap);
			qb.appendWhere(BaseColumns._ID + "=" + uri.getPathSegments().get(1));
			break;
		default:
			throw new IllegalArgumentException("Unknown URI " + uri);
		}
		SQLiteDatabase db = dh.getWritableDatabase();
		Cursor c = qb.query(db, projection, selection, selectionArgs, null, null, orderBy);
		c.setNotificationUri(getContext().getContentResolver(), uri);
		return c;
	}

	@Override
	public int update(Uri uri, ContentValues values, String selection, String[] selectionArgs) {
		String TABLE_NAME = null;
		if (values == null) {
			values = new ContentValues();
		} else {
			values = new ContentValues(values);
		}
		switch (uriMatcher.match(uri)) {
		case INCOMING_ALLFAVORITESINFO_COLLECTION:
			TABLE_NAME = AllfavoritesInfo.ALLFAVORITESINFO_TABLE_NAME;
			break;
		case INCOMING_ALLFAVORITESINFO_SINGLE:
			TABLE_NAME = AllfavoritesInfo.ALLFAVORITESINFO_TABLE_NAME;
			selection = BaseColumns._ID + "=" + uri.getPathSegments().get(1)
					+ (!TextUtils.isEmpty(selection) ? " AND (" + selection + ')' : "");
			break;

		case INCOMING_PLAYEDINFO_COLLECTION:
			TABLE_NAME = PlayedInfo.PLAYEDINFO_TABLE_NAME;
			break;
		case INCOMING_PLAYEDINFO_SINGLE:
			TABLE_NAME = PlayedInfo.PLAYEDINFO_TABLE_NAME;
			selection = BaseColumns._ID + "=" + uri.getPathSegments().get(1)
					+ (!TextUtils.isEmpty(selection) ? " AND (" + selection + ')' : "");
			break;
		default:
			break;
		}
		int count;
		SQLiteDatabase db = dh.getWritableDatabase();
		count = db.update(TABLE_NAME, values, selection, selectionArgs);
		getContext().getContentResolver().notifyChange(uri, null);
		return count;
	}
}
