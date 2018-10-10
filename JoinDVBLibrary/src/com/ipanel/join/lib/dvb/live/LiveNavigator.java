package com.ipanel.join.lib.dvb.live;

import ipaneltv.toolkit.TimeToolkit;
import ipaneltv.toolkit.db.DatabaseCursorHandler.ChannelCursorHandler;
import ipaneltv.toolkit.db.DatabaseCursorHandler.EventCursorHandler;
import ipaneltv.toolkit.db.DatabaseCursorHandler.GroupCursorHandler;
import ipaneltv.toolkit.db.DatabaseCursorHandler.GuideCursorHandler;
import ipaneltv.toolkit.db.DatabaseObjectification;
import ipaneltv.toolkit.db.DatabaseObjectification.ChannelKey;
import ipaneltv.toolkit.db.LiveProgramNavigator;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import android.content.Context;
import android.net.Uri;
import android.net.telecast.NetworkDatabase.Channels;
import android.os.Handler;
import android.text.format.Time;
import android.util.Log;
import android.util.SparseArray;

import com.ipanel.join.lib.dvb.live.DatabaseObjects.LiveChannel;
import com.ipanel.join.lib.dvb.live.DatabaseObjects.LiveChannelCursorHandler;
import com.ipanel.join.lib.dvb.live.DatabaseObjects.LiveGroupCursorHandler;
import com.ipanel.join.lib.dvb.live.DatabaseObjects.LiveGuideCursorHandler;
import com.ipanel.join.lib.dvb.live.DatabaseObjects.LiveProgramEvent;
import com.ipanel.join.lib.dvb.live.DatabaseObjects.LiveProgramEventCursorHandler;
import com.ipanel.join.lib.dvb.live.DatabaseObjects.LiveuGroup;

public class LiveNavigator extends LiveProgramNavigator {

	static final String TAG = LiveNavigator.class.getName();

	public LiveNavigator(Context context, Uri uri) {
		super(context, uri);
	}

	void fillGroups(SparseArray<LiveuGroup> groups) {
		Object mutex = getLockMutex();
		synchronized (mutex) {
			@SuppressWarnings({ "unchecked", "rawtypes" })
			SparseArray<LiveuGroup> loadedGroups = (SparseArray) getLoadedGroups();
			int n = loadedGroups.size();
			groups.clear();
			for (int i = 0; i < n; i++) {
				LiveuGroup g = loadedGroups.valueAt(i);
				groups.put(g.getId(), g);
			}
		}
	}

	void fillGroupChannels(List<ChannelKey> keys, List<LiveChannel> list) {
		Object mutex = getLockMutex();
		synchronized (mutex) {
			@SuppressWarnings({ "unchecked", "rawtypes" })
			HashMap<ChannelKey, LiveChannel> loaddedChannels = (HashMap) getLoaddedChannels();
			Log.d(TAG, "loaddedChannels.size() = "+ loaddedChannels.size());
			list.clear();
			if (keys == null) {
				list.addAll(loaddedChannels.values());
			} else {
				for (ChannelKey key : keys) {
					LiveChannel ch = loaddedChannels.get(key);
					list.add(ch);
				}
			}
		}
	}

	final void fillChanelProgramsDaily(SparseArray<List<LiveProgramEvent>> ret, LiveProgramEvent p,
			long start) {
		Log.d(TAG, "fillChanelProgramsDaily");
		Log.d(TAG, "fillChanelProgramsDaily-->p=="+p.getName());		
		int key = (int) (start / TimeToolkit.DURATION_OF_DAY);
		List<LiveProgramEvent> d = ret.get(key);
		if (d == null){
			Log.d(TAG, "d == null fillChanelProgramsDaily key = "+key);
			ret.put(key, d = new ArrayList<LiveProgramEvent>());
		}else {
			Log.d(TAG, "d.size() = "+d.size());
		}
		d.add(p);
	}

	void fillChannelPrograms(LiveChannel ch) {
		Log.d(TAG,"fillChannelPrograms ready to fill in and ch="+ch.getChannelKey().toString()+"-------------"+ch.getName());
		Object mutex = getLockMutex();
		Time t = new Time();
		TimeToolkit.getStartTimeByOffsetOfToday(0, t);
		long startOfToday = t.toMillis(false);
		ch.clearPrograms();
		synchronized (mutex) {
			@SuppressWarnings({ "unchecked", "rawtypes" })
			List<LiveProgramEvent> programs = (List) getLoadedPrograms(ch.getChannelKey());
			Log.i(TAG, "----------------------------->getLoadedPrograms  programs= +"+programs);
			if (programs == null)
				return;// soft reference
			int n = programs.size();
			SparseArray<List<LiveProgramEvent>> ret = new SparseArray<List<LiveProgramEvent>>();
			for (int i = 0; i < n; i++) {
				LiveProgramEvent p = programs.get(i);
				long estart = p.getStart();
				long eend = p.getEnd();
				int duration = (int) (eend - estart);
				int offset = ((int) ((estart - startOfToday))) / TimeToolkit.DURATION_OF_DAY;
				offset += estart < startOfToday ? -1 : 0;
				for (; duration > 0; duration -= TimeToolkit.DURATION_OF_DAY, offset++) {
					long startOfday = startOfToday + (long) offset * TimeToolkit.DURATION_OF_DAY;
					fillChanelProgramsDaily(ret, p, startOfday);
				}
			}
			//原先 使用的
			ch.dailyList = ret;
			ch.dailyList2 = ret;
		}
	}

	void fillChannelPresentFollow(List<LiveChannel> list) {
		Object mutex = getLockMutex();
		@SuppressWarnings("unused")
		int i=0;
		synchronized (mutex) {
			for (LiveChannel ch : list) {
				try{
					DatabaseObjectification.Program pf[] = getLoaddedPresentFollows(ch.getChannelKey());
					if (pf != null) {
						ch.setPresent((LiveProgramEvent) pf[0]);
						ch.setFollow((LiveProgramEvent) pf[1]);
					}
				}catch(Exception e ){
					e.printStackTrace();
				}

			}
		}

	}

	@Override
	protected GuideCursorHandler getLoadGuideCursorHandler(Context context, Uri uri, Handler handler) {
		return new LiveGuideCursorHandler(context, uri, null, null, null, null, handler);
	}

	@Override
	protected ChannelCursorHandler getLoadChannelCursorHandler(Context context, Uri uri,
			Handler handler) {
		String selection = Channels.CHANNEL_NUMBER + ">?";
		String[] selectionArgs = { "-1" };
		return new LiveChannelCursorHandler(context, uri, null, selection, selectionArgs, null,
				handler);
	}

	@Override
	protected GroupCursorHandler getLoadGroupCursorHandler(Context context, Uri uri, Handler handler) {
		return new LiveGroupCursorHandler(context, uri, null, null, null, null, handler);
	}

	@Override
	protected EventCursorHandler getLoadEventCursorHandler(Context context, Uri uri, Handler handler) {
		String[][] selections = createEventLoadSelection(0, 0); // 预先加载当天的节目
		return new LiveProgramEventCursorHandler(context, uri, null, selections[0][0],
				selections[1], null, handler);
	}

	@Override
	protected EventCursorHandler createUpdateEventCursorHandler(Context context, Uri uri,
			Handler handler, final ChannelKey ch) {
		String[][] selections = createEventUpdateSelection(ch);
		return new LiveProgramEventCursorHandler(context, uri, null, selections[0][0],
				selections[1], null, handler);
	}

}
