package com.ipanel.join.lib.dvb.live;

import com.ipanel.join.lib.dvb.DVBConfig;

import ipaneltv.toolkit.media.MediaSessionFragment;
import android.os.Bundle;
import android.util.Log;

public class PlayFragment extends MediaSessionFragment {
	private final static String TAG = PlayFragment.class.getName();
	private DvbLivePlayer live;
	private LocalShiftPlayer shift;
	private IpqamShiftPlayer qamshift;
	private NgodPlayManager manager;
	private NgodShiftSource source;
	private boolean isHold = false;

	/**
	 * ����ʵ��
	 * 
	 * @param playServiceName
	 *            ���ŷ��������
	 * @param shiftServiceName
	 *            ʱ�Ʒ��������
	 * @return ����ʵ��
	 */
	public static PlayFragment createInstance() {
		Bundle b = MediaSessionFragment.createArguments(DVBConfig.getUUID(),
				DVBConfig.getPlayService(), DVBConfig.getSourceService());
		PlayFragment f = new PlayFragment();
		f.setArguments(b);
		return f;
	}

	public PlayFragment() {
		manager = new NgodPlayManager(this);
		setRetainInstance(true);
	}

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		live = new DvbLivePlayer(manager);
		shift = new LocalShiftPlayer(manager);
		qamshift = new IpqamShiftPlayer(manager);
		source = new NgodShiftSource(manager, DVBConfig.getVodProvider());
		// ���������йܸ�Fragment��������Դ���չ���
		// ����ͬһ��(play),��ѡ�񲥷�ʱ��ֻ��һ��������ʵ�����ڵ�ǰ���ﵽ�л���Դ��Ŀ��
		entrustSession("play", live);
		entrustSession("play", shift);
		entrustSession("play", qamshift);
		entrustSession("source", source);
		manager.prepare();
	}

	public void onPause() {
		isHold = false;
		manager.suspend();
		// manager.stop(TeeveePlayer.FLAG_VIDEO_FRAME_BLACK);
		super.onPause();
	}

	@Override
	public void onStop() {
		isHold = false;
		super.onStop();
		Log.d(TAG, "onStop");
	}

	public void onResume() {
		super.onResume();
		isHold = true;
		manager.resume();
	}

	public void onDestroy() {
		manager.release();
		super.onDestroy();
	}

	DvbLivePlayer getLivePlayer() {
		Log.d(TAG, "getLivePlayer isHold = " + isHold);
		if (isHold) {
			if (chooseSession(live))
				return live;
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
		Log.d(TAG, "getShiftPlayer isHold = " + isHold);
		if (isHold) {
			if (chooseSession(shift))
				return shift;
		}
		return null;
	}

	IpqamShiftPlayer getQamShiftPlayer() {
		Log.d(TAG, "getQamShiftPlayer isHold = " + isHold);
		if (isHold) {
			if (chooseSession(qamshift))
				return qamshift;
		}
		return null;
	}

	NgodShiftSource getShiftSource() {
		Log.d(TAG, "getShiftSource isHold = " + isHold);
		if (isHold) {
			if (chooseSession(source))
				return source;
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
		Log.d(TAG, "onAllEntrusteeConnected :" + group);
	}
}
