package com.ipanel.join.chongqing.live.play;

import ipaneltv.toolkit.media.MediaSessionFragment;
import android.os.Bundle;
import android.util.Log;

import com.ipanel.join.chongqing.live.Constant;

public class PlayFragment extends MediaSessionFragment {
	private final static String TAG = PlayFragment.class.getName();
	private DvbLivePlayer live;
	private LocalShiftPlayer shift;
	private IpqamShiftPlayer qamshift;
	private NgodPlayManager manager;
	private NgodShiftSource source;
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
	public static PlayFragment createInstance() {
		Bundle b = MediaSessionFragment.createArguments(Constant.UUID,
				Constant.PLAY_SERVICE_NAME, Constant.SRC_SERVICE_NAME);
		PlayFragment f = new PlayFragment();
		f.setLoosenState(false);
		f.setArguments(b);
		return f;
	}

	public PlayFragment() {
		manager = new NgodPlayManager(this);
	}

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		live = new DvbLivePlayer(manager);
		shift = new LocalShiftPlayer(manager);
		qamshift = new IpqamShiftPlayer(manager);
		source = new NgodShiftSource(manager);
		// 将播放器托管给Fragment，进行资源回收管理
		// 加入同一组(play),当选择播放时，只有一个播放器实例处于当前，达到切换资源的目的
		entrustSession("play", live);
		entrustSession("play", shift);
		entrustSession("play", qamshift);
		entrustSession("source", source);
		manager.prepare();
	}

	public void onPause() {
		manager.setVolume(0.0f);
		manager.suspend();
		manager.setLoosen(Constant.LOOSE_LIVE_PLAYER);
		// manager.stop(TeeveePlayer.FLAG_VIDEO_FRAME_BLACK);
		super.onPause();
		isHold = false;
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
		Log.d(TAG, "resume");
	}

	public void onDestroy() {
		manager.release();
		super.onDestroy();
	}

	DvbLivePlayer getLivePlayer() {
		Log.d(TAG, "getLivePlayer 22 isHold="+isHold+";live.isLose()="+live.isLose());
		if(live.isLose()){
			removeSession("play", live);
			live = new DvbLivePlayer(manager);
			Log.d(TAG, "getLivePlayer 22 live = "+ live);
			entrustSession("play", live);
			connectSeeeions();
		}
		if (isHold) {
			if (chooseSession(live))
				return live;
		}else{
			Log.d(TAG, "getLivePlayer 22 isHold="+isHold);
		}
		return null;
	}

	DvbLivePlayer tryGetLivePlayer() {
		if (isSessionChoosed(live))
			return live;
		return null;
	}

	LocalShiftPlayer tryGetShiftPlayer() {
		if (isSessionChoosed(shift))
			return shift;
		return null;
	}

	IpqamShiftPlayer tryGetQamShiftPlayer() {
		if (isSessionChoosed(qamshift))
			return qamshift;
		return null;
	}

	NgodShiftSource tryGetShiftSource() {
		if (isSessionChoosed(source))
			return source;
		return null;
	}

	LocalShiftPlayer getShiftPlayer() {
		if (chooseSession(shift))
			return shift;
		return null;
	}

	IpqamShiftPlayer getQamShiftPlayer() {
		if (chooseSession(qamshift))
			return qamshift;
		return null;
	}

	NgodShiftSource getShiftSource() {
		if (chooseSession(source))
			return source;
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
		Log.d(TAG, "onAllEntrusteeConnected :" + group);
	}
}
