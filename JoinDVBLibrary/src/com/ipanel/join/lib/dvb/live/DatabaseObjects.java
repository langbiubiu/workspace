package com.ipanel.join.lib.dvb.live;

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

		public LiveGroupCursorHandler(Context context, Uri uri, String projection[],
				String selection, String[] selectionArgs, String order, Handler handler) {
			super(context, uri, projection, selection, selectionArgs, order, handler);
		}

		@Override
		public void onCursorStart(Cursor c) {
			super.onCursorStart(c);
		}

		@Override
		public void onRecordFound(Cursor c) {
			super.onRecordFound(c);
			appendRecord();// 执行了此函数，才会把记录加入到结果列表，此时可以做过滤选择
		}

		@Override
		protected Group createGroup() {
			return new LiveuGroup();
		}

		@Override
		protected Group appendRecord() {
			LiveuGroup g = (LiveuGroup) super.appendRecord();
			return g;
		}
	}

	public static class LiveChannelCursorHandler extends
			DvbDatabaseCursorHandler.ServiceCursorHandler {

		public LiveChannelCursorHandler(Context context, Uri uri, String projection[],
				String selection, String[] selectionArgs, String order, Handler handler) {
			super(context, uri, projection, selection, selectionArgs, order, handler);
		}

		@Override
		public void onCursorStart(Cursor c) {
			super.onCursorStart(c);
		}

		@Override
		public void onRecordFound(Cursor c) {
			super.onRecordFound(c);
			appendRecord();// 执行了此函数，才会把记录加入到结果列表，此时可以做过滤选择
		}

		@Override
		protected Channel createChannel() {
			return new LiveChannel();
		}

		@Override
		protected Channel appendRecord() {
			LiveChannel s = (LiveChannel) super.appendRecord();
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
		
		public String getChannelId() {
			return getProgram();
		}

		public int getFavorite() {
			return favorite;
		}

		public String getProgram() {
			return this.getTsId() + ":" + key.getProgram();
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

	public static class LiveuGroup extends DatabaseObjectification.Group {
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
	}

	public static class LiveGuide extends DatabaseObjectification.Guide {
	}
}
