package com.ipanel.join.lib.dvb.live;

import ipaneltv.toolkit.TimeToolkit.Weekday;
import ipaneltv.toolkit.db.DatabaseObjectification.ChannelKey;
import ipaneltv.toolkit.entitlement.EntitlementObserver;
import ipaneltv.toolkit.entitlement.EntitlementObserver.EntitlementsState;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;

import android.os.HandlerThread;
import android.os.Looper;
import android.util.Log;
import android.util.SparseArray;
import cn.ipanel.android.otto.OttoUtils;

import com.ipanel.join.lib.dvb.DVBConfig;
import com.ipanel.join.lib.dvb.OttoEventChannelDataUpdate;
import com.ipanel.join.lib.dvb.OttoEventEPGUpdate;
import com.ipanel.join.lib.dvb.OttoEventPFUpdate;
import com.ipanel.join.lib.dvb.live.DatabaseObjects.LiveChannel;
import com.ipanel.join.lib.dvb.live.DatabaseObjects.LiveProgramEvent;
import com.ipanel.join.lib.dvb.live.DatabaseObjects.LiveuGroup;
import com.squareup.otto.Produce;

public class NaviManager implements NaviInterface {
	private final static String TAG = NaviInterface.class.getName();
	SparseArray<LiveuGroup> groups;
	HashMap<ChannelKey, LiveChannel> channels = new HashMap<ChannelKey, LiveChannel>();
	HashMap<String, LiveChannel> serviceIdChannelMap = new HashMap<String, LiveChannel>();

	List<LiveChannel> numberedChannel = new ArrayList<LiveChannel>();
	static List<LiveuGroup> emptyGroupList = new ArrayList<LiveuGroup>();
	static List<LiveChannel> emptyChannelList = new ArrayList<LiveChannel>();
	static List<LiveProgramEvent> emptyProgramList = new ArrayList<LiveProgramEvent>();
	OttoEventChannelDataUpdate readyEvent;

	@Produce
	public OttoEventChannelDataUpdate getOttoEventChannelDataUpdate() {
		return readyEvent;
	}

	void start() {
		navi = DVBConfig.getLiveNavigator();
		navi.setListener(new LiveNavigator.Listener() {
			@Override
			public void onLoadFinished() {
				Log.d(this.toString(), "onLoadFinished");
				groups = new SparseArray<LiveuGroup>();
				navi.fillGroups(groups);
				navi.fillGroupChannels(null, numberedChannel);
				sortNumberedChannel(numberedChannel);
				for (LiveChannel ch : numberedChannel) {
					channels.put(ch.getChannelKey(), ch);
					serviceIdChannelMap.put(ch.getChannelKey().getProgram() + "", ch);
					navi.fillChannelPrograms(ch);
				}
				navi.fillChannelPresentFollow(numberedChannel);
				OttoUtils.postOnUiThread(readyEvent = new OttoEventChannelDataUpdate());
			}

			@Override
			public void onProgramsUpdated(ChannelKey key) {
				LiveChannel ch = channels.get(key);
				Log.d(TAG, "key = " + key.toString());
				if (ch != null)
					navi.fillChannelPrograms(ch);

				OttoUtils.postOnUiThread(new OttoEventEPGUpdate(ch.getChannelKey()));
			}

			@Override
			public void onPresentFollowUpdated() {
				navi.fillChannelPresentFollow(numberedChannel);
				OttoUtils.postOnUiThread(new OttoEventPFUpdate());
			}

			@Override
			public void onGroupsUpdate() {
				navi.fillGroups(groups);
				OttoUtils.postOnUiThread(new OttoEventChannelDataUpdate());
			}

			@Override
			public void onChannelsUpdated() {
				navi.fillGroupChannels(null, numberedChannel);
				sortNumberedChannel(numberedChannel);
				channels.clear();
				serviceIdChannelMap.clear();
				for (LiveChannel ch : numberedChannel) {
					channels.put(ch.getChannelKey(), ch);
					serviceIdChannelMap.put(ch.getChannelKey().getProgram() + "", ch);
				}
				OttoUtils.postOnUiThread(new OttoEventChannelDataUpdate());
			}
		});
		navi.preload();
		navi.queryState();

		observer = DVBConfig.getEntitlementObserver();
		observer.registerAll(DVBConfig.getUUID());
	}

	void sortNumberedChannel(List<LiveChannel> channels) {
		try {
			if (channels == null || channels.size() == 0)
				return;
			Collections.sort(channels, new Comparator<LiveChannel>() {
				@Override
				public final int compare(LiveChannel lhs, LiveChannel rhs) {
					if (lhs == null || rhs == null)
						return 0;
					return lhs.getChannelNumber() - rhs.getChannelNumber();
				}
			});
		} catch (Exception e) {
			// TODO: handle exception
			Log.d(TAG, "kemm-->sortNumberedChannel fatal Exception");
			e.printStackTrace();
		}

	}

	void stop() {
		navi.setListener(null);
	}

	@Override
	public EntitlementsState getEntitlements(int moduledID) {
		checkMainThread();
		return observer.getSelectedEntitlementsState(moduledID);
	}

	@Override
	public List<LiveuGroup> getGroups() {
		checkMainThread();
		int n = groups == null ? 0 : groups.size();
		if (n == 0)
			return emptyGroupList;
		List<LiveuGroup> ret = new ArrayList<LiveuGroup>();
		for (int i = 0; i < n; i++) {
			ret.add(groups.valueAt(i));
		}

		return ret;
	}

	@Override
	public List<LiveChannel> getGroupedChannels(LiveuGroup g) {
		checkMainThread();
		List<LiveChannel> ret = new ArrayList<LiveChannel>();
		for (ChannelKey key : g.getChannelKeys()) {
			LiveChannel channel = channels.get(key);
			if (channel != null)
				ret.add(channel);
		}
		sortNumberedChannel(ret);
		return ret;
	}

	@Override
	public List<LiveChannel> getNumberedChannels() {
		checkMainThread();
		return numberedChannel == null ? emptyChannelList : numberedChannel;
	}

	public LiveChannel getChannel(ChannelKey key) {
		return channels.get(key);
	}
	
	public LiveChannel getChannel(String serviceId) {
		return serviceIdChannelMap.get(serviceId);
	}

	@Override
	public List<LiveProgramEvent> getDailyPrograms(LiveChannel ch, Weekday d) {
		checkMainThread();
		List<LiveProgramEvent> ret = ch.getDaily(d);
		if (ret == null) {
			ret = emptyProgramList;
			navi.queryChannelGuide(ch.getChannelKey());
		}
		return ret;
	}

	@Override
	public List<LiveProgramEvent> getDailyPrograms(LiveChannel ch, int offsetOfToday) {
		checkMainThread();
		Log.d(TAG, "getDailyPrograms ------ offsetOfToday =" + offsetOfToday);
		Log.i("1202--EPG", "------------step2  获取" + ch.getChannelKey().toString() + "的EPG信息");
		List<LiveProgramEvent> ret = ch.getDaily(offsetOfToday);
		/*
		 * if (ret == null) { Log.d(TAG, "List<WasuProgramEvent> ret == null"); ret =
		 * emptyProgramList; navi.queryChannelGuide(ch.getChannelKey()); }
		 */
		Log.i("EPG", "------>go in  --:" + ch.getChannelKey().toString());

		return ret;
	}

	// ===================

	LiveNavigator navi;
	EntitlementObserver observer;
	Thread mainThread;
	HandlerThread groupThread;

	public NaviManager() {
		OttoUtils.getBus().register(this);
		mainThread = Looper.getMainLooper().getThread();
		groupThread = new HandlerThread("play_group");
		groupThread.start();
		start();
	}

	void checkMainThread() {
		if (Thread.currentThread() != mainThread)
			throw new RuntimeException("must invoke in main thread!");
	}

	@Override
	public void commitOwnedGroup(LiveuGroup g, List<ChannelKey> list) {
		if (g.getUid() == 0)
			throw new IllegalArgumentException("not group owner");
		throw new RuntimeException("not support yet!");
	}

}