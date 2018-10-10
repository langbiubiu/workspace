package ipaneltv.toolkit.entitlement;

import ipaneltv.toolkit.IPanelLog;

import java.util.HashMap;

import android.content.ContentUris;
import android.content.ContentValues;
import android.content.Context;
import android.content.UriMatcher;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteQueryBuilder;
import android.net.Uri;
import android.net.telecast.ca.EntitlementDatabase.Entitlements;
import android.text.TextUtils;

public class EntitlementProviderBase {
	static final String TAG = "EntitlementProviderBase";
	public UriMatcher mUriMatcher;
	public String authority, contentType, contentItemType;
	private static HashMap<String, String> tsMap = new HashMap<String, String>();
	public static final int Entitlement = 401;
	public static final int Entitlement_ID = 402;
	private boolean enableProjection = false;

	public EntitlementProviderBase(String authority, String contentType, String contentItemType) {
		this.authority = authority;
		this.contentType = contentType;
		this.contentItemType = contentItemType;
		init();
	}

	void init() {
		mUriMatcher = new UriMatcher(UriMatcher.NO_MATCH);
		mUriMatcher.addURI(authority, Entitlements.TABLE_NAME, Entitlement);
		mUriMatcher.addURI(authority, Entitlements.TABLE_NAME + "/#", Entitlement_ID);

		tsMap.put(Entitlements._ID, Entitlements._ID);
		tsMap.put(Entitlements.PRODUCT_ID, Entitlements.PRODUCT_ID);
		tsMap.put(Entitlements.PRODUCT_TYPE, Entitlements.PRODUCT_TYPE);
		tsMap.put(Entitlements.PRODUCT_URI, Entitlements.PRODUCT_URI);
		tsMap.put(Entitlements.ENTITLEMENT, Entitlements.ENTITLEMENT);
		tsMap.put(Entitlements.MODULE_SN, Entitlements.MODULE_SN);
		tsMap.put(Entitlements.START_TIME, Entitlements.START_TIME);
		tsMap.put(Entitlements.END_TIME, Entitlements.END_TIME);
		tsMap.put(Entitlements.NETWORK_OPERATOR_ID, Entitlements.NETWORK_OPERATOR_ID);
	}

	public void setProjectionEnable(boolean b) {
		enableProjection = b;
	}

	public String getUriType(Uri uri) {
		switch (mUriMatcher.match(uri)) {
		case Entitlement:
			return contentType;
		case Entitlement_ID:
			return contentItemType;
		default:
			throw new SQLException("no match uri:" + uri);
		}
	}

	public Uri insert(SQLiteDatabase db, Uri uri, ContentValues values, Context context) {
		Uri ret = null;
		switch (mUriMatcher.match(uri)) {
		case Entitlement:
			long rowId = db.insert(Entitlements.TABLE_NAME, null, values);
			if (rowId > 0) {
				IPanelLog.d(TAG, "rowId = " + rowId);
				ret = ContentUris.withAppendedId(uri, rowId);
//				context.getContentResolver().notifyChange(ret, null);
//				context.getContentResolver().notifyChange(uri, null);
			}
			break;
		default:
			throw new IllegalArgumentException("Unkown URI =" + uri);
		}
		return ret;
	}

	public int delete(SQLiteDatabase db, Uri uri, String where, String[] whereArgs, Context context) {
		int count;
		switch (mUriMatcher.match(uri)) {
		case Entitlement:
			count = db.delete(Entitlements.TABLE_NAME, where, whereArgs);
			break;
		case Entitlement_ID:
			String appId = uri.getPathSegments().get(1);
			count = db.delete(Entitlements.TABLE_NAME,
					Entitlements._ID + "=" + appId
							+ (!TextUtils.isEmpty(where) ? "AND (" + where + ')' : ""), whereArgs);
			break;
		default:
			throw new IllegalArgumentException("Unkown URI =" + uri);
		}
		if(count>0)
			context.getContentResolver().notifyChange(uri, null);
		return count;
	}

	public Cursor query(SQLiteDatabase db, Uri uri, String[] projection, String selection,
			String[] selectionArgs, String sortOrder) {
		SQLiteQueryBuilder qb = new SQLiteQueryBuilder();
		switch (mUriMatcher.match(uri)) {
		case Entitlement:
			qb.setTables(Entitlements.TABLE_NAME);
			if (enableProjection)
				qb.setProjectionMap(tsMap);
			break;
		case Entitlement_ID:
			qb.setTables(Entitlements.TABLE_NAME);
			if (enableProjection)
				qb.setProjectionMap(tsMap);
			qb.appendWhere(Entitlements._ID + "=" + uri.getPathSegments().get(1));
			break;
		default:
			throw new IllegalArgumentException("Unknown URI=" + uri);
		}
		return qb.query(db, projection, selection, selectionArgs, null, null, null);
	}

	public int update(SQLiteDatabase db, Uri uri, ContentValues values, String selection,
			String[] selectionArgs, Context context) {
		int ret = 0;
		if (mUriMatcher.match(uri) == Entitlement_ID) {
			String appId = uri.getPathSegments().get(1);
			ret = db.update(Entitlements.TABLE_NAME, values, Entitlements._ID + "=" + appId
					+ (!TextUtils.isEmpty(selection) ? "AND (" + selection + ')' : ""),
					selectionArgs);
		} else if (mUriMatcher.match(uri) == Entitlement) {
			ret = db.update(Entitlements.TABLE_NAME, values, selection, selectionArgs);
		}
		if(ret>0)
			context.getContentResolver().notifyChange(uri, null);
		return ret;
	}

	private static String createEntitlementsTableSqlString;

	public static void createTables(SQLiteDatabase db) {
		IPanelLog.i(TAG, "createTables:========");
		IPanelLog.i(TAG, createEntitlementsTableSqlString);
		db.execSQL(createEntitlementsTableSqlString);
	}

	public static void dropTables(SQLiteDatabase db) {
		IPanelLog.i(TAG, "dropTables ============== ");
		db.execSQL("DROP TABLE IF EXISTS " + Entitlements.TABLE_NAME);
	}

	static void makeCreateEntitlementsTableSqlString() {
		StringBuffer sb = new StringBuffer();
		sb.append("CREATE TABLE IF NOT EXISTS ");
		sb.append(Entitlements.TABLE_NAME);
		sb.append(" (");
		sb.append(Entitlements._ID).append(" INTEGER PRIMARY KEY,");
		sb.append(Entitlements.PRODUCT_ID).append(" INT,");
		sb.append(Entitlements.PRODUCT_TYPE).append(" INT,");
		sb.append(Entitlements.PRODUCT_URI).append(" TEXT,");
		sb.append(Entitlements.MODULE_SN).append(" INT,");
		sb.append(Entitlements.NETWORK_OPERATOR_ID).append(" INT,");
		sb.append(Entitlements.START_TIME).append(" INT,");
		sb.append(Entitlements.END_TIME).append(" INT,");
		sb.append(Entitlements.ENTITLEMENT).append(" INT);");
		createEntitlementsTableSqlString = sb.toString();
	}

	static {
		makeCreateEntitlementsTableSqlString();
	}
}
