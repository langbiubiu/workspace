package com.ipanel.join.lib.dvb.live.homed;

import com.ipanel.join.lib.dvb.DVBConfig;
import com.ipanel.join.lib.dvb.live.PlayCallback;
import com.ipanel.join.lib.dvb.live.PlayInterface;

import ipaneltv.toolkit.media.MediaSessionFragment;
import android.os.Bundle;
import android.util.Log;

public class HomedPlayFragment extends MediaSessionFragment {
	private final static String TAG = HomedPlayFragment.class.getName();
	DvbLivePlayer live;
	private HttpPlayer httpPlayer;
//	private LocalShiftPlayer shift;
//	private IpqamShiftPlayer qamshift;
	private HomedPlayManager manager;
//	private NgodShiftSource source;
	private HttpSource httpSource;
	private boolean isHold = false;

	/**
	 * 创建实例
	 * 
	 * @param playServiceName
	 *            播放服务的名称
	 * @param shiftServiceName
	 *            时移服务的名称
	 * @return 对象实例
	 */
	public static HomedPlayFragment createInstance() {
		Bundle b = MediaSessionFragment.createArguments(DVBConfig.getUUID(),
				DVBConfig.getPlayService(), DVBConfig.getSourceService());
		HomedPlayFragment f = new HomedPlayFragment();
		f.setArguments(b);
		return f;
	}

	public HomedPlayFragment() {
		manager = new HomedPlayManager(this);
	}

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		live = new DvbLivePlayer(manager);
		httpPlayer = new HttpPlayer(manager);
//		shift = new LocalShiftPlayer(manager);
//		qamshift = new IpqamShiftPlayer(manager);
//		source = new NgodShiftSource(manager);
		httpSource = new HttpSource(manager);
		// 将播放器托管给Fragment，进行资源回收管理
		// 加入同一组(play),当选择播放时，只有一个播放器实例处于当前，达到切换资源的目的
		entrustSession("play", live);
		entrustSession("play", httpPlayer);
//		entrustSession("play", shift);
//		entrustSession("play", qamshift);
//		entrustSession("source", source);
		entrustSession("source", httpSource);
		manager.prepare();
	}

	public void onPause() {
		isHold = false;
		manager.suspend();
		super.onPause();
		Log.d(TAG, "onPause");
	}

	@Override
	public void onStop() {
		super.onStop();
		isHold = false;
		Log.d(TAG, "onStop");
	}

	public void onResume() {
		super.onResume();
		isHold = true;
		manager.resume();
		Log.d(TAG, "resume 11");
	}

	public void onDestroy() {
		manager.release();
		super.onDestroy();
	}

	DvbLivePlayer getLivePlayer() {
		Log.d(TAG, "getLivePlayer 33 isHold=" + isHold + ";live.isLose()="
				+ live.isLose());
		if (live.isLose()) {
			removeSession("play", live);
			live = new DvbLivePlayer(manager);
			Log.d(TAG, "getLivePlayer 33 live = " + live);
			entrustSession("play", live);
			connectSeeeions();
		}
		if (isHold) {
			if (chooseSession(live))
				return live;
		} else {
			Log.d(TAG, "getLivePlayer 33 isHold=" + isHold);
		}
		return null;
	}

	DvbLivePlayer tryGetLivePlayer() {
		if (isSessionChoosed(live))
			return live;
		return null;
	}

//	LocalShiftPlayer tryGetShiftPlayer() {
//		if (isSessionChoosed(shift))
//			return shift;
//		return null;
//	}
//
//	IpqamShiftPlayer tryGetQamShiftPlayer() {
//		if (isSessionChoosed(qamshift))
//			return qamshift;
//		return null;
//	}

//	NgodShiftSource tryGetShiftSource() {
//		if (isSessionChoosed(source))
//			return source;
//		return null;
//	}

	HttpSource tryGetHttpSource() {
		if (isSessionChoosed(httpSource))
			return httpSource;
		return null;
	}

//	LocalShiftPlayer getShiftPlayer() {
//		if (chooseSession(shift))
//			return shift;
//		return null;
//	}
//
//	IpqamShiftPlayer getQamShiftPlayer() {
//		if (chooseSession(qamshift))
//			return qamshift;
//		return null;
//	}

//	NgodShiftSource getShiftSource() {
//		if (chooseSession(source))
//			return source;
//		return null;
//	}

	HttpSource getHttpSource() {
		Log.d(TAG, "getHttpSource 11 isHold=" + isHold);
		if (isHold) {
			if (chooseSession(httpSource))
				return httpSource;
		} else {
			Log.d(TAG, "getHttpSource 33 isHold=" + isHold);
		}

		return null;
	}

	public PlayInterface getPlayInterface(PlayCallback callback) {
		manager.setCallback(callback);
		return manager;
	}

	@Override
	public void onAllEntrusteeConnected(String group) {
		super.onAllEntrusteeConnected(group);
		manager.notifyPlayContextReady(group);
		if("play".equals(group)){
			Log.d(TAG, "onAllEntrusteeConnected  1");
		}
		Log.d(TAG, "onAllEntrusteeConnected 2 :" + group);
	}
}
