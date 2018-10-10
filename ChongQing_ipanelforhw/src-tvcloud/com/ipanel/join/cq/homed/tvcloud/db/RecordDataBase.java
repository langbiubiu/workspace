package com.ipanel.join.cq.homed.tvcloud.db;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

public class RecordDataBase extends SQLiteOpenHelper {
	
	private final static String DATABASE_NAME = "record.db";
	private final static String TABLE_NAME = "record_table";
	private final static int DATABASE_VERSION = 1;
	public final static String RECORD_ID = "id";
	public final static String RECORD_EVENT_ID = "event_id";
	public final static String RECORD_EVENT_NAME = "event_name";
	public final static String RECORD_EVENT_START = "event_start";
	public final static String RECORD_EVENT_SIZE = "event_size";
	public final static String RECORD_CHANNEL_NAME= "channel_name";

	public RecordDataBase(Context context) {
		super(context, DATABASE_NAME, null, DATABASE_VERSION);
		// TODO Auto-generated constructor stub
	}

	@Override
	public void onCreate(SQLiteDatabase db) {
		// TODO Auto-generated method stub
		String sql = "CREATE TABLE " + TABLE_NAME + " (" + RECORD_ID
				+ " INTEGER primary key autoincrement, " + RECORD_EVENT_ID + " text, "
				+ RECORD_EVENT_NAME + " text, " + RECORD_EVENT_START + " LONG, "
				+ RECORD_EVENT_SIZE + " INTEGER, " + RECORD_CHANNEL_NAME + " text);";
		db.execSQL(sql);
	}

	@Override
	public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
		// TODO Auto-generated method stub
		String sql = "DROP TABLE IF EXISTS " + TABLE_NAME;
		db.execSQL(sql);
		onCreate(db);
	}
	
	public Cursor select() {
		SQLiteDatabase db = this.getReadableDatabase();
		Cursor cursor = db
		.query(TABLE_NAME, null, null, null, null, null, null);
		return cursor;
	}
		
	//增加操作
	public long insert(String event_id, String event_name, long start_time, int size, String channel_name) {
		SQLiteDatabase db = this.getWritableDatabase();
		/* ContentValues */
		ContentValues cv = new ContentValues();
		cv.put(RECORD_EVENT_ID, event_id);
		cv.put(RECORD_EVENT_NAME, event_name);
		cv.put(RECORD_EVENT_START, start_time);
		cv.put(RECORD_EVENT_SIZE, size);
		cv.put(RECORD_CHANNEL_NAME, channel_name);
		long row = db.insert(TABLE_NAME, null, cv);
		return row;
	}
		
	//删除操作
	public void delete(String event_id) {
		SQLiteDatabase db = this.getWritableDatabase();
		String where = RECORD_EVENT_ID + " = ?";
		String[] whereValue ={ event_id };
		db.delete(TABLE_NAME, where, whereValue);
	}
		
	//修改操作
	public void update(int id, String event_id){
//		SQLiteDatabase db = this.getWritableDatabase();
//		String where = RECORD_ID + " = ?";
//		String[] whereValue = { Integer.toString(id) };
//		ContentValues cv = new ContentValues();
//		cv.put(RECORD_EVENT_ID, event_id);
//		db.update(TABLE_NAME, cv, where, whereValue);
	}
	
	public Cursor query(String event_id) {
		SQLiteDatabase db = this.getReadableDatabase();
		String[] columns = {RECORD_EVENT_ID, RECORD_EVENT_NAME, RECORD_EVENT_START, RECORD_EVENT_SIZE, RECORD_CHANNEL_NAME};
		String where = RECORD_EVENT_ID +"=?";
		String[] whereValue = {event_id};
		Cursor cur = db.query(TABLE_NAME, columns, where, whereValue, null, null, null);
		return cur;
	}

}
