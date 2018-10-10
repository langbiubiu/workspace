package com.ipanel.join.chongqing.live.navi;

import ipaneltv.toolkit.TimeToolkit;
import ipaneltv.toolkit.TimeToolkit.Weekday;
import ipaneltv.toolkit.db.DatabaseCursorHandler;
import ipaneltv.toolkit.db.DatabaseObjectification;
import ipaneltv.toolkit.db.DatabaseObjectification.Channel;
import ipaneltv.toolkit.db.DatabaseObjectification.ChannelKey;
import ipaneltv.toolkit.db.DatabaseObjectification.Group;
import ipaneltv.toolkit.db.DatabaseObjectification.Guide;
import ipaneltv.toolkit.db.DatabaseObjectification.Program;
import ipaneltv.toolkit.db.DvbDatabaseCursorHandler;
import ipaneltv.toolkit.db.DvbDatabaseObjectification;
import ipaneltv.uuids.db.FujianDatabase;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.net.telecast.ca.EntitlementDatabase.EntitlementType;
import android.os.Handler;
import android.text.format.Time;
import android.util.Log;
import android.util.SparseArray;

public class DatabaseObjects {

	public static class LiveGroupCursorHandler extends DatabaseCursorHandler.GroupCursorHandler {
		protected int i_uid = -1;
		protected int v_uid = 0;

		public LiveGroupCursorHandler(Context context, Uri uri, String projection[],
				String selection, String[] selectionArgs, String order, Handler handler) {
			super(context, uri, projection, selection, selectionArgs, order, handler);
		}

		@Override
		public void onCursorStart(Cursor c) {
			super.onCursorStart(c);
			v_uid = c.getColumnIndex(FujianDatabase.FujianGroups.USER_ID);
		}

		@Override
		public void onRecordFound(Cursor c) {
			super.onRecordFound(c);
			if (i_uid >= 0)
				v_uid = c.getInt(i_uid);
			appendRecord();// 执行了此函数，才会把记录加入到结果列表，此时可以做过滤选择
		}

		@Override
		protected Group createGroup() {
			return new LiveGroup();
		}

		@Override
		protected Group appendRecord() {
			LiveGroup g = (LiveGroup) super.appendRecord();
			g.uid = v_uid;
			return g;
		}
	}

	public static class LiveChannelCursorHandler extends
			DvbDatabaseCursorHandler.ServiceCursorHandler {
		int i_channel_id;
		String v_channel_id;

		public LiveChannelCursorHandler(Context context, Uri uri, String projection[],
				String selection, String[] selectionArgs, String order, Handler handler) {
			super(context, uri, projection, selection, selectionArgs, order, handler);
		}

		@Override
		public void onCursorStart(Cursor c) {
			super.onCursorStart(c);
			i_channel_id = c.getColumnIndex(FujianDatabase.FujianServices.CHANNEL_ID);
		}

		@Override
		public void onRecordFound(Cursor c) {
			super.onRecordFound(c);
			if (i_channel_id >= 0)
				v_channel_id = c.getString(i_channel_id);
			appendRecord();// 执行了此函数，才会把记录加入到结果列表，此时可以做过滤选择
		}

		@Override
		protected Channel createChannel() {
			return new LiveChannel();
		}

		@Override
		protected Channel appendRecord() {
			LiveChannel s = (LiveChannel) super.appendRecord();
			s.channelId = v_channel_id;
			return s;
		}
	}

	public static class LiveProgramEventCursorHandler extends
			DvbDatabaseCursorHandler.ServiceEventCursorHandler {

		public LiveProgramEventCursorHandler(Context context, Uri uri, String projection[],
				String selection, String[] selectionArgs, String order, Handler handler) {
			super(context, uri, projection, selection, selectionArgs, order, handler);
		}

		@Override
		public void onRecordFound(Cursor c) {
			super.onRecordFound(c);
			// 执行了appendRecord函数，才会把记录加入到结果列表，此时可以做过滤选择
			optAppendPresentFollow(appendRecord());
		}

		@Override
		protected Program createProgram() {
			return new LiveProgramEvent();
		}
	}

	public static class LiveGuideCursorHandler extends DatabaseCursorHandler.GuideCursorHandler {
		public LiveGuideCursorHandler(Context context, Uri uri, String projection[],
				String selection, String[] selectionArgs, String order, Handler handler) {
			super(context, uri, projection, selection, selectionArgs, order, handler);
		}

		@Override
		public void onRecordFound(Cursor c) {
			super.onRecordFound(c);
			// 执行了appendRecord函数，才会把记录加入到结果列表，此时可以做过滤选择
			appendRecord();
		}

		@Override
		protected Guide createGuide() {
			return new LiveGuide();
		}
	}

	public static class LiveChannel extends DvbDatabaseObjectification.Service {
		private final static String TAG = LiveChannel.class.getName();
		protected String channelId;
		protected int order;

		public int favorite;
		public int logNumber;
		public String logName;

		SparseArray<List<LiveProgramEvent>> dailyList;
		SparseArray<List<LiveProgramEvent>> dailyList2;
		
		public void setPresent(Program present){
			this.present=present;
		}
		public void setFollow(Program follow){
			this.follow=follow;
		}
		
		public void setChannelNumber(int number){
			this.channemNumber=number;
		}
		
		public String getChannelId() {
			return getProgram();
		}

		public int getFavorite() {
			return favorite;
		}

		public String getProgram() {
			return key.getProgram()+"";
		}
		
		public String getMathKey(){
			return tsId+":"+key.getProgram();
		}

		public void setFavorite(int favorite) {
			this.favorite = favorite;
		}

		public String getLogName() {
			return logName;
		}

		public void setLogName(String logName) {
			this.logName = logName;
		}

		public void setOrder(int order) {
			this.order = order;
		}

		public int getOrder() {
			return order;
		}

		public boolean isOrder() {
			return order == EntitlementType.TYPE_AVAILABLE || order == EntitlementType.TYPE_UNKNOWN;
		}

		public int getLogNumber() {
			return logNumber;
		}

		public void setLogNumber(int logNumber) {
			this.logNumber = logNumber;
		}

		void setPresent(LiveProgramEvent p) {
			present = p;
		}

		void setFollow(LiveProgramEvent p) {
			follow = p;
		}

		void clearPrograms() {
			dailyList = null;
		}

		List<LiveProgramEvent> getDaily(long startOfDay) {
			Log.i(TAG, "---------------------------->&&&&&&  i am go seconde");
			Log.d(TAG, "getDaily startOfDay = " + startOfDay);
			int key = (int) (startOfDay / TimeToolkit.DURATION_OF_DAY);
			SimpleDateFormat formatter = new SimpleDateFormat("yyyy年MM月dd日    HH:mm:ss      ");
			Date curDate = new Date(startOfDay);// 获取当前时间
			String str = formatter.format(curDate);
			Log.d(TAG, "observe time = "+str+" key = "+key);
			if (dailyList2 != null) {
				Log.d(TAG, "dailyList2 size = " + dailyList2.size());
				for (int i = 0; i < dailyList2.size(); i++) {
					Log.d(TAG, "dailyList2.keyAt(i) = "+dailyList2.keyAt(i));
//					List<WasuProgramEvent> list = dailyList2.get(i);
//					if (list != null) {
//						Log.d(TAG, "list[" + i + "] sise = " + list.size());
//					}
				}
				return dailyList2.get(key);
			} else {
				Log.d(TAG, "dailyList == null");
			}

			/*
			 * if (dailyList != null) { Log.d(TAG, "dailyList != null");
			 * SparseArray<List<WasuProgramEvent>> a = dailyList.get();
			 * Log.i(TAG, "a="+a); if (a != null){ Log.d(TAG,
			 * "SparseArray<List<WasuProgramEvent>> a size = "+a.size()); return
			 * a.get(key); } dailyList = null; }else { Log.d(TAG,
			 * "dailyList == null"); }
			 */
			return null;
		}

		List<LiveProgramEvent> getDaily(int offset) {
			Time t = new Time();
			TimeToolkit.getStartTimeByOffsetOfToday(offset, t);
			return getDaily(t.toMillis(false));
		}

		List<LiveProgramEvent> getDaily(Weekday weekday) {
			int offset = TimeToolkit.getOffsetOfTodayByWeekday(weekday);
			return getDaily(offset);
		}
	}

	public static class LiveGroup extends DatabaseObjectification.Group {
		protected int uid;

		public int getUid() {
			return uid;
		}

		public void setUid(int id) {
			this.uid = id;
		}

		public void setId(int id) {
			this.id = id;
		}

		public void setName(String name) {
			this.name = name;
		}

		void setList(List<ChannelKey> n) {
			list.clear();
			list.addAll(n);
		}
	}

	public static class LiveProgramEvent extends DvbDatabaseObjectification.ProgramEvent {
		public int status;
		
		private String jump_url; // 针对华为回看设置的字段，用于请求回看节目播放地址

		public String getJumpUrl() {
			return jump_url;
		}

		public void setJumpUrl(String jump_url) {
			this.jump_url = jump_url;
		}
		
	}

	public static class LiveGuide extends DatabaseObjectification.Guide {
	}
}
