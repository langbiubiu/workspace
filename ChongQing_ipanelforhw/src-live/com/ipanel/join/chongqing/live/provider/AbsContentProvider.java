package com.ipanel.join.chongqing.live.provider;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import android.content.ContentProvider;
import android.content.ContentValues;
import android.content.Context;
import android.content.UriMatcher;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteDatabase.CursorFactory;
import android.database.sqlite.SQLiteOpenHelper;
import android.database.sqlite.SQLiteQueryBuilder;
import android.net.Uri;
import android.text.TextUtils;

public abstract class AbsContentProvider extends ContentProvider {
	public static final String CONTENT_TYPE = "vnd.android.cursor.dir/vnd.ipanel.app";
	public static final String CONTENT_ITEM_TYPE = "vnd.android.cursor.item/vnd.ipanel.app";
	private static String DATABASE_NAME;
	protected static String AUTHORITY;
	private static int DATABASE_VERSION;
	protected static UriMatcher mUriMatcher;
	private static final List<Class> classes = new ArrayList<Class>();
	private static HashMap<Integer, Class> map = new HashMap<Integer, Class>();
	private static ProviderConfig config;
	private static int MATCH_ID = 11;

	private DatabaseHelper mDataBaseHelper;

	protected abstract ProviderConfig createProviderConfig();

	@Override
	public boolean onCreate() {
		config=createProviderConfig();
		if (config == null) {
			throw new IllegalArgumentException("don't init provider config");
		}
		MATCH_ID = 11;
		mUriMatcher = new UriMatcher(UriMatcher.NO_MATCH);
		DATABASE_NAME = config.getDataBaseName();
		DATABASE_VERSION = config.getDataBaseVersion();
		AUTHORITY = config.getAuthority();
		Class[] tmp = config.createClasses();
		classes.clear();
		if (tmp != null) {
			for (Class c : tmp) {
				classes.add(c);
			}
		}
		int N = classes.size();
		for (int i = 0; i < N; i++) {
			map.put(MATCH_ID, classes.get(i));
			mUriMatcher.addURI(AUTHORITY,
					TableUtils.getTableName(classes.get(i)), MATCH_ID++);
			map.put(MATCH_ID, classes.get(i));
			mUriMatcher.addURI(AUTHORITY,
					TableUtils.getTableName(classes.get(i)) + "/#", MATCH_ID++);
		}
		mDataBaseHelper = new DatabaseHelper(getContext(), DATABASE_NAME, null,
				DATABASE_VERSION);
		if (mDataBaseHelper != null) {
			SQLiteDatabase db = mDataBaseHelper.getWritableDatabase();
			return true;
		}
		return false;
	}

	@Override
	public String getType(Uri uri) {
		int match = mUriMatcher.match(uri);
		if (!isUriValid(match)) {
			throw new IllegalArgumentException("Unkown URI =" + uri);
		}
		switch (match % 2) {
		case 1:
			return CONTENT_TYPE;
		default:
			return CONTENT_ITEM_TYPE;
		}
	}

	@Override
	public Uri insert(Uri uri, ContentValues initValues) {
		int match = mUriMatcher.match(uri);
		if (!isUriValid(match)) {
			throw new IllegalArgumentException("Unkown URI =" + uri);
		}
		ContentValues values;
		if (initValues != null) {
			values = new ContentValues(initValues);
		} else {
			values = new ContentValues();
		}
		SQLiteDatabase db = mDataBaseHelper.getWritableDatabase();
		Long rowId = db.insert(TableUtils.getTableName(map.get(match)), null,
				values);
		if (rowId > 0) {
			getContext().getContentResolver().notifyChange(uri, null);
			return uri;
		}

		throw new SQLException("Failed to insert row into " + uri);
	}

	@Override
	public int delete(Uri uri, String selection, String[] selectionArgs) {
		SQLiteDatabase db = mDataBaseHelper.getWritableDatabase();
		int count = 0;
		int match = mUriMatcher.match(uri);
		if (!isUriValid(match)) {
			throw new IllegalArgumentException("Unkown URI =" + uri);
		}
		switch (match % 2) {
		case 1:
			count = db.delete(TableUtils.getTableName(map.get(match)),
					selection, selectionArgs);
			break;
		default:
			String appId = uri.getPathSegments().get(1);
			count = db.delete(
					TableUtils.getTableName(map.get(match)),
					TableUtils._ID
							+ "="
							+ appId
							+ (!TextUtils.isEmpty(selection) ? "AND ("
									+ selection + ')' : ""), selectionArgs);
			break;
		}
		getContext().getContentResolver().notifyChange(uri, null);
		return count;
	}

	@Override
	public int update(Uri uri, ContentValues values, String selection,
			String[] selectionArgs) {
		SQLiteDatabase db = mDataBaseHelper.getWritableDatabase();
		int count;
		int match = mUriMatcher.match(uri);
		if(!isUriValid(match)){
			throw new IllegalArgumentException("Unkown URI =" + uri);
		}
		count = db.update(TableUtils.getTableName(map.get(match)), values, selection, selectionArgs);
		getContext().getContentResolver().notifyChange(uri, null);
		return count;
	}
	@SuppressWarnings("unchecked")
	@Override
	public Cursor query(Uri uri, String[] projection, String selection,
			String[] selectionArgs, String sortOrder) {
		SQLiteQueryBuilder qb = new SQLiteQueryBuilder();
		int match = mUriMatcher.match(uri);
		if(!isUriValid(match)){
			throw new IllegalArgumentException("Unkown URI =" + uri);
		}
		qb.setTables(TableUtils.getTableName(map.get(match)));
		qb.setProjectionMap(TableUtils.getKeyMap(map.get(match)));
		SQLiteDatabase db = mDataBaseHelper.getReadableDatabase();
		Cursor c = qb.query(db, projection, selection, selectionArgs, null,
				null, null);
		c.setNotificationUri(getContext().getContentResolver(), uri);
		return c;
	}
	public boolean isUriValid(int match) {
		return match > 0;
	}

	private class DatabaseHelper extends SQLiteOpenHelper {

		public DatabaseHelper(Context context, String name,
				CursorFactory factory, int version) {
			super(context, DATABASE_NAME, null, DATABASE_VERSION);
		}

		@Override
		public void onCreate(SQLiteDatabase db) {
			onDoCreate(db);
		}

		@Override
		public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
			onDoUpgrade(db,oldVersion,newVersion);
		}
	}
	public void onDoUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
		int N = classes.size();
		for (int i = 0; i < N; i++) {
			db.execSQL(TableUtils.getDropSQL(classes.get(i)));
		}
	}
	
	public void onDoCreate(SQLiteDatabase db) {
		int N = classes.size();
		for (int i = 0; i < N; i++) {
			db.execSQL(TableUtils.getCreateSQL(classes.get(i)));
		}
	}
}
