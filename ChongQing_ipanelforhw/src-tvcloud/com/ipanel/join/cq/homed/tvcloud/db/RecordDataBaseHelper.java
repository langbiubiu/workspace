package com.ipanel.join.cq.homed.tvcloud.db;

import java.util.ArrayList;
import java.util.List;

import cn.ipanel.android.LogHelper;

import android.content.Context;
import android.database.Cursor;

public class RecordDataBaseHelper {
	
	static RecordDataBaseHelper mDBHelper;
	Context context;
	RecordDataBase mRecordDB;
	Cursor mCursor;
	
	List<RecordEvent> events = new ArrayList<RecordEvent>();
	List<String> eventIds = new ArrayList<String>();
	
	private RecordDataBaseHelper() {
		
	}
	
	public static RecordDataBaseHelper getInstance() {
		if (mDBHelper == null)
			mDBHelper = new RecordDataBaseHelper();
		return mDBHelper;
	}
	
//	public void setContext(Context ctx) {
//		this.context = ctx;
//	}
	
	public void init(Context ctx) {
		this.context = ctx;
		mRecordDB = new RecordDataBase(context);
		mCursor = mRecordDB.select();
		updateData();
	}
	
	public void updateData() {
		synchronized (events) {
			eventIds.clear();
			events.clear();
			mCursor = mRecordDB.select();
	        for (int i = 0; i < mCursor.getCount(); i++) {
	        	mCursor.moveToPosition(i);
	        	eventIds.add(mCursor.getString(1));
	        	RecordEvent event = new RecordEvent();
        		event.setEvent_id(mCursor.getLong(1));
        		event.setEvent_name(mCursor.getString(2));
        		event.setStart_time(mCursor.getLong(3));
        		event.setEvent_size(mCursor.getInt(4));
        		event.setChannel_name(mCursor.getString(5));
        		LogHelper.i(event.toString());
        		events.add(event);
	        }
		}
	}
	
	/**
	 * 删除一条数据，并更新list
	 * @param event
	 */
	public void delete(RecordEvent event) {
		String event_id = event.getEvent_id() +"";
		mRecordDB.delete(event_id);
		updateData();
	}
	
	/**
	 * 删除一条数据，并更新list
	 * @param event
	 */
	public void delete(String event_id) {
		mRecordDB.delete(event_id);
		updateData();
	}
	
	/**
     * 插入一条数据，并更新list
     * @param event_id
     */
	public void insert(String event_id, String event_name, long start_time, int size, String channel_name) {
		// TODO Auto-generated method stub
		mRecordDB.insert(event_id, event_name, start_time, size, channel_name);
		updateData();
	}
	
	public List<RecordEvent> getEvents() {
		return events;
	}
	
	public List<String> getEventIds() {
		return eventIds;
	}

}
