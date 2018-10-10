package ipaneltv.toolkit.wardship;

import ipaneltv.toolkit.IPanelLog;
import ipaneltv.toolkit.wardship.ProgramWardshipDatebase.ProgramWardships;

import java.util.HashMap;

import android.content.ContentValues;
import android.content.Context;
import android.content.UriMatcher;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteQueryBuilder;
import android.net.Uri;
import android.text.TextUtils;

public class ProgramWardshipProviderBase {
	static final String TAG = "ProgramWardshipProviderBase";
	public UriMatcher mUriMatcher;
	public String authority, contentType, contentItemType;
	private static HashMap<String, String> tsMap = new HashMap<String, String>();
	public static final int WARDSHIP = 101;
	public static final int WARDSHIP_ID = 102;
	private boolean enableProjection = false;

	public ProgramWardshipProviderBase(String authority, String contentType, String contentItemType) {
		this.authority = authority;
		this.contentType = contentType;
		this.contentItemType = contentItemType;
		init();
	}

	void init() {
		mUriMatcher = new UriMatcher(UriMatcher.NO_MATCH);
		mUriMatcher.addURI(authority, ProgramWardships.TABLE_NAME, WARDSHIP);
		mUriMatcher.addURI(authority, ProgramWardships.TABLE_NAME + "/#", WARDSHIP_ID);

		tsMap.put(ProgramWardships._ID, ProgramWardships._ID);
		tsMap.put(ProgramWardships.FREQUENCY, ProgramWardships.FREQUENCY);
		tsMap.put(ProgramWardships.PROGRAM_NUMBER, ProgramWardships.PROGRAM_NUMBER);
		tsMap.put(ProgramWardships.CHANNEL_NUMBER, ProgramWardships.CHANNEL_NUMBER);
		tsMap.put(ProgramWardships.CHANNEL_NAME, ProgramWardships.CHANNEL_NAME);
		tsMap.put(ProgramWardships.WARDSHIP, ProgramWardships.WARDSHIP);
	}

	public void setProjectionEnable(boolean b) {
		enableProjection = b;
	}

	public String getUriType(Uri uri) {
		switch (mUriMatcher.match(uri)) {
		case WARDSHIP:
			return contentType;
		case WARDSHIP_ID:
			return contentItemType;
		default:
			throw new SQLException("no match uri:" + uri);
		}
	}

	public Uri insert(SQLiteDatabase db, Uri uri, ContentValues values, Context context) {
		return null;// TODO
	}

	public int delete(SQLiteDatabase db, Uri uri, String where, String[] whereArgs, Context context) {
		int count;
		switch (mUriMatcher.match(uri)) {
		case WARDSHIP:
			count = db.delete(ProgramWardships.TABLE_NAME, where, whereArgs);
			break;
		case WARDSHIP_ID:
			String appId = uri.getPathSegments().get(1);
			count = db.delete(ProgramWardships.TABLE_NAME, ProgramWardships._ID + "=" + appId
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
		case WARDSHIP:
			qb.setTables(ProgramWardships.TABLE_NAME);
			if (enableProjection)
				qb.setProjectionMap(tsMap);
			break;
		case WARDSHIP_ID:
			qb.setTables(ProgramWardships.TABLE_NAME);
			if (enableProjection)
				qb.setProjectionMap(tsMap);
			qb.appendWhere(ProgramWardships._ID + "=" + uri.getPathSegments().get(1));
			break;
		default:
			throw new IllegalArgumentException("Unknown URI=" + uri);
		}
		return qb.query(db, projection, selection, selectionArgs, null, null, null);
	}

	private static String createProgramWardShipTableSqlString;

	public static void createTables(SQLiteDatabase db) {
		IPanelLog.i(TAG, "createTables:========");
		IPanelLog.i(TAG, createProgramWardShipTableSqlString);
		db.execSQL(createProgramWardShipTableSqlString);
	}

	public static void dropTables(SQLiteDatabase db) {
		IPanelLog.i(TAG, "dropTables ============== ");
		db.execSQL("DROP TABLE IF EXISTS " + ProgramWardships.TABLE_NAME);
	}

	static void makeCreateProgramWardShipTableSqlString() {
		StringBuffer sb = new StringBuffer();
		sb.append("CREATE TABLE IF NOT EXISTS ");
		sb.append(ProgramWardships.TABLE_NAME);
		sb.append(" (");
		sb.append(ProgramWardships._ID).append(" INTEGER PRIMARY KEY,");
		sb.append(ProgramWardships.FREQUENCY).append(" INT,");
		sb.append(ProgramWardships.PROGRAM_NUMBER).append(" INT,");
		sb.append(ProgramWardships.CHANNEL_NUMBER).append(" INT,");
		sb.append(ProgramWardships.CHANNEL_NAME).append(" TEXT,");
		sb.append(ProgramWardships.WARDSHIP).append(" INT);");
		createProgramWardShipTableSqlString = sb.toString();
	}

	static {
		makeCreateProgramWardShipTableSqlString();
	}
}
