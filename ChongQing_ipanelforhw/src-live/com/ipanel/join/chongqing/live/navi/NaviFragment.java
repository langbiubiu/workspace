package com.ipanel.join.chongqing.live.navi;

import ipaneltv.toolkit.TimeToolkit.Weekday;
import ipaneltv.toolkit.db.DatabaseObjectification.ChannelKey;
import ipaneltv.toolkit.entitlement.EntitlementObserver;
import ipaneltv.toolkit.entitlement.EntitlementObserver.EntitlementsState;
import ipaneltv.uuids.NcWasuUUIDs;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;

import android.annotation.SuppressLint;
import android.app.Fragment;
import android.os.Bundle;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.Message;
import android.util.Log;
import android.util.SparseArray;
import android.view.View;

import cn.ipanel.android.LogHelper;

import com.ipanel.join.chongqing.live.Constant;
import com.ipanel.join.chongqing.live.LiveApp;
import com.ipanel.join.chongqing.live.navi.DatabaseObjects.LiveChannel;
import com.ipanel.join.chongqing.live.navi.DatabaseObjects.LiveGroup;
import com.ipanel.join.chongqing.live.navi.DatabaseObjects.LiveProgramEvent;
import com.ipanel.join.chongqing.live.util.TimeHelper;

public class NaviFragment extends Fragment {
	NaviManager manager;
	LiveApp app = LiveApp.getInstance();

	public static NaviFragment createInstance() {
		NaviFragment f = new NaviFragment();
		Bundle b = new Bundle();
		b.putString("uuid", Constant.UUID);
		f.setArguments(b);
		return f;
	}

	public NaviFragment() {
		manager = new NaviManager(this);
	}

	@Override
	public void onCreate(Bundle savedInstanceState) {
		Log.d(this.toString(), "onCreate");
		super.onCreate(savedInstanceState);
	}

	@Override
	public void onStart() {
		Log.d(this.toString(), "onStart");
		super.onStart();
		manager.start();
	}

	@Override
	public void onStop() {
		Log.d(this.toString(), "onStop");
		manager.stop();
		super.onStop();
	}

	@Override
	public void onDetach() {
		Log.d(this.toString(), "onDetach");
		app = null;
		super.onDetach();
	}

	@Override
	public void onPause() {
		Log.d(this.toString(), "onPause");
		super.onPause();
	}

	@Override
	public void onResume() {
		Log.d(this.toString(), "onResume");
		super.onResume();
	}

	@SuppressLint("NewApi")
	@Override
	public void onViewCreated(View view, Bundle savedInstanceState) {
		Log.d(this.toString(), "onViewCreated");
		super.onViewCreated(view, savedInstanceState);
	}

	@Override
	public void onDestroyView() {
		Log.d(this.toString(), "onDestroyView");
		super.onDestroyView();
	}

	public NaviInterface getNaviInterface(NaviCallback callback) {
		manager.setNaviCallback(callback);
		return manager;
	}
}

class NaviManager implements NaviInterface {
	private final static String TAG = NaviInterface.class.getName();
	NaviCallback callback;
	SparseArray<LiveGroup> groups;
	HashMap<ChannelKey, LiveChannel> channels = new HashMap<ChannelKey, LiveChannel>();
	List<LiveChannel> numberedChannel = new ArrayList<LiveChannel>();
	static List<LiveGroup> emptyGroupList = new ArrayList<LiveGroup>();
	static List<LiveChannel> emptyChannelList = new ArrayList<LiveChannel>();
	static List<LiveProgramEvent> emptyProgramList = new ArrayList<LiveProgramEvent>();
	boolean started = false;
	
	void setNaviCallback(NaviCallback cb) {
		callback = cb;
	}

	void start() {
		Log.d(TAG, "start");
		started = true;
		navi = host.app.getWasuLiveNavigator();
		navi.setListener(new LiveNavigator.Listener() {
			@Override
			public void onLoadFinished() {
				Log.d(this.toString(), "onLoadFinished");
				groups = new SparseArray<LiveGroup>();
				navi.fillGroups(groups);
				navi.fillGroupChannels(null, numberedChannel);
				sortNumberedChannel(numberedChannel);
				for (LiveChannel ch : numberedChannel) {
					Log.i(TAG,"onLoadFinished: channelkey:"+ch.getChannelKey().getProgram()+" name:"+ch.getName());
					if(ch.getType() == 65535){
						continue ;
					}
					channels.put(ch.getChannelKey(), ch);
					navi.fillChannelPrograms(ch);
				}
				navi.fillChannelPresentFollow(numberedChannel);
				if(started){
					callbackHandler.sendEmptyMessage(CB_ON_NAVI_UPDATED);
					callbackHandler.sendEmptyMessage(CB_ON_GUIDE_UPDATED);
					callbackHandler.sendEmptyMessage(CB_ON_PF_UPDATED);	
				}
			}

			@Override
			public void onProgramsUpdated(ChannelKey key) {
				LiveChannel ch = channels.get(key);
				Log.d(TAG, "onProgramsUpdated key = " + key.toString());
				if (ch != null)
					navi.fillChannelPrograms(ch);
				if(started){
					Message msg = callbackHandler.obtainMessage();
					msg.obj = ch.getChannelKey();
					msg.what = CB_ON_GUIDE_UPDATED;
					callbackHandler.sendMessage(msg);	
				}
				// callbackHandler.sendEmptyMessage(CB_ON_GUIDE_UPDATED);
			}

			@Override
			public void onPresentFollowUpdated() {
				Log.d(TAG, "onPresentFollowUpdated started = " + started);
				navi.fillChannelPresentFollow(numberedChannel);
				if(started){
					callbackHandler.sendEmptyMessage(CB_ON_PF_UPDATED);
				}
			}

			@Override
			public void onGroupsUpdate() {
				Log.d(TAG, "onGroupsUpdate");
				navi.fillGroups(groups);
				if(started){
					callbackHandler.sendEmptyMessage(CB_ON_NAVI_UPDATED2);
				}
			}

			@Override
			public void onChannelsUpdated() {
				Log.d(TAG, "onChannelUpdate");
				navi.fillGroupChannels(null, numberedChannel);
				sortNumberedChannel(numberedChannel);
				for (LiveChannel ch : numberedChannel)
					channels.put(ch.getChannelKey(), ch);
				if(started){
					callbackHandler.sendEmptyMessage(CB_ON_NAVI_UPDATED2);
				}
			}
		});
		navi.preload();
		navi.queryState();

		observer = host.app.getEntitlementObserver();
		observer.registerAll(NcWasuUUIDs.ID);
	}

	void sortNumberedChannel(List<LiveChannel> channels) {
		Collections.sort(channels, new Comparator<LiveChannel>() {
			@Override
			public final int compare(LiveChannel lhs, LiveChannel rhs) {
				return lhs.getChannelNumber() - rhs.getChannelNumber();
			}
		});
	}

	void stop() {
		Log.d(TAG, "stop! ");
		started = false;
	}

	@Override
	public EntitlementsState getEntitlements(int moduledID) {
		checkMainThread();
		return observer.getSelectedEntitlementsState(moduledID);
	}

	@Override
	public List<LiveGroup> getGroups() {
		checkMainThread();
		int n = groups == null ? 0 : groups.size();
		if (n == 0)
			return emptyGroupList;
		List<LiveGroup> ret = new ArrayList<LiveGroup>();
		for (int i = 0; i < n; i++) {


			ret.add(groups.valueAt(i));
		}

		return ret;
	}

	@Override
	public List<LiveChannel> getGroupedChannels(LiveGroup g) {
		checkMainThread();
		List<LiveChannel> ret = new ArrayList<LiveChannel>();
		for (ChannelKey key : g.getChannelKeys()) {
			ret.add(channels.get(key));
		}
		sortNumberedChannel(ret);

		return ret;
	}

	@Override
	public List<LiveChannel> getNumberedChannels() {
		checkMainThread();
		return numberedChannel == null ? emptyChannelList : numberedChannel;
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
		 * if (ret == null) { Log.d(TAG, "List<WasuProgramEvent> ret == null");
		 * ret = emptyProgramList; navi.queryChannelGuide(ch.getChannelKey()); }
		 */
		Log.i("EPG", "------>go in  --:" + ch.getChannelKey().toString());
		if (ret != null) {
			for (LiveProgramEvent w : ret) {
				Log.i("EPG", "------>start" + w.getStart());
				Log.i("EPG", "------>end" + w.getEnd());
				String hour = TimeHelper.getHourTime(w.getStart());
				String evnet = TimeHelper.getEventTime(w.getStart(), w.getEnd());
				Log.i("EPG", "------>hour" + hour + "----event:" + evnet);

			}
		}

		return ret;
	}

	// ===================

	NaviFragment host;
	LiveNavigator navi;
	EntitlementObserver observer;
	Thread mainThread;
	Handler callbackHandler;
	HandlerThread groupThread;
	Handler groupHandler;
	FaivoratesCommitor fcommiter;

	NaviManager(NaviFragment fragment) {
		host = fragment;
		mainThread = Thread.currentThread();
		groupThread = new HandlerThread("play_group");
		groupThread.start();
		callbackHandler = new Handler(callbackHandleMessage);
		groupHandler = new Handler(groupThread.getLooper());
		fcommiter = new FaivoratesCommitor() {
			protected void onCommitFailed() {
				if (callbackHandler != null)
					callbackHandler.obtainMessage(CB_ON_OGCMT_ERR, FaivoratesCommitor.NAME);
			}
		};
	}

	void checkMainThread() {
		if (Thread.currentThread() != mainThread)
			throw new RuntimeException("must invoke in main thread!");
	}

	Handler.Callback callbackHandleMessage = new Handler.Callback() {

		@Override
		public boolean handleMessage(Message msg) {
			NaviCallback cb = callback;
			if (cb == null)
				return true;
			try {
				switch (msg.what) {
				case CB_ON_NAVI_UPDATED:
					cb.onNaviUpdated();
					break;
				case CB_ON_NAVI_UPDATED2:
					if(numberedChannel.size()<=0 ||groups.size()<=0){
						Log.i("handleMessage ", "numberedChannel or groups is null" );
						return true;
					}
					cb.onNaviUpdated();
					break;
				case CB_ON_GUIDE_UPDATED:
					cb.onGuideUpdated((ChannelKey) msg.obj);
					break;
				case CB_ON_PF_UPDATED:
					cb.onPresentFollowUpdated();
					break;
				case CB_ON_OGCMT_ERR:
					cb.onCommitGroupError((String) msg.obj);
					break;
				case CB_ON_SHIFT_UPDATED:
					cb.onShiftUpdated();
					break;
				default:
					break;
				}
			} catch (Exception e) {
				e.printStackTrace();
			}
			return true;
		}
	};

	static final int CB_ON_NAVI_UPDATED = 1;
	static final int CB_ON_NAVI_UPDATED2 = 6;
	static final int CB_ON_GUIDE_UPDATED = 2;
	static final int CB_ON_PF_UPDATED = 3;
	static final int CB_ON_OGCMT_ERR = 4;
	static final int CB_ON_SHIFT_UPDATED = 5;

	@Override
	public void commitOwnedGroup(LiveGroup g, List<ChannelKey> list) {
		if (g.getUid() == 0)
			throw new IllegalArgumentException("not group owner");
		if (FaivoratesCommitor.NAME.equals(g.getName())) {
			fcommiter.commitFaivoritesGroup(host.getActivity(), g, list, groupHandler);
		} else {
			throw new RuntimeException("not support yet!");
		}
	}


}
