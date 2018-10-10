package com.ipanel.join.chongqing.live.manager.impl.hw;

import java.util.List;

import ipaneltv.toolkit.db.DatabaseObjectification.ChannelKey;
import ipaneltv.toolkit.db.DatabaseObjectification.Program;

import android.util.Log;
import android.view.View;
import cn.ipanel.android.LogHelper;

import com.ipanel.join.chongqing.live.Constant;
import com.ipanel.join.chongqing.live.manager.IManager;
import com.ipanel.join.chongqing.live.manager.impl.BaseStationManagerImpl;
import com.ipanel.join.chongqing.live.manager.impl.hw.HWDataManagerImpl.HWShiftProgram;
import com.ipanel.join.chongqing.live.navi.DatabaseObjects.LiveChannel;
import com.ipanel.join.chongqing.live.play.PlayCallback;
import com.ipanel.join.chongqing.live.play.PlayFragment;
import com.ipanel.join.chongqing.live.play.PlayInterface;
import com.ipanel.join.chongqing.live.play.AllPFDataGetter.PresentAndFollow;

public class HWStationManagerImpl extends BaseStationManagerImpl implements PlayCallback {
	
	protected PlayInterface play;
	private boolean connect_step_ok = false;
	private boolean server_step_ok = false;

	public HWStationManagerImpl(IManager context, PlayFragment playf, CallBack callback) {
		super(context, callback);
		// TODO Auto-generated constructor stub
		this.play = playf.getPlayInterface(this);
	}
	
	@Override
	protected long changeSeekTime(long time) {
		return time;
	}

	@Override
	public void setMediaURLInteral(String url) {
		// TODO Auto-generated method stub
		LogHelper.i("changeMediaPlayer 111 :" + url);
		play.shift(url, -1, 1);
	}

	@Override
	public void setMediaSeekInteral(long time) {
		// TODO Auto-generated method stub
		play.shiftSeek(time);
	}

	@Override
	public void setMediaPauseInteral(String uri) {
		// TODO Auto-generated method stub
		play.shiftPause(uri);
	}

	@Override
	public void setMediaPlayInteral(String url) {
		// TODO Auto-generated method stub
		play.shift(url, -1, 1);
	}
	
	@Override
	public void startShiftPlay(long time, int style, boolean ip, boolean pause,
			long start, long end) {
		// TODO Auto-generated method stub
		super.startShiftPlay(time, style, ip, pause, start, end);
		play.loosenAllSession();
	}
	
	@Override
	public void clearPlayData() {
		// TODO Auto-generated method stub
		destroyShiftPlayer();
		super.clearPlayData();
	}
	
	@Override
	public void destroyShiftPlayer() {
		super.destroyShiftPlayer();
		Log.d("sy", "destroyShiftPlayer*******************");
		play.shiftStop();
	}
	
	@Override
	protected void onShiftPrepareOK() {
		// TODO Auto-generated method stub
		if (isHeadShiftMode()) {
			HWShiftProgram program = (HWShiftProgram) activity.getDataManager().getShiftProgramAtTime(currentCh, System.currentTimeMillis());
			if (program != null)
				mSettingTime = program.getStart();
		}
		LogHelper.i("mSetting time = " + mSettingTime + ", current time = " + System.currentTimeMillis());
		long time = Math.min(mSettingTime, System.currentTimeMillis());
		mPlayTime = time;
		if (mPauseStart) {
			setMediaPauseInteral(url);
			recordStateChange(PLAY_STATE_PAUSE);
		} else {
			setMediaPlayInteral(url);
			setMediaSeekInteral(mPlayTime);
			recordStateChange(PLAY_STATE_PLAYING);
		}
	}

	@Override
	public void select(String http, long freq, int fflags, int program,
			int pflags) {
		// TODO Auto-generated method stub
		LogHelper.i("live url 11111:"+http);
		play.select(freq, fflags, program, pflags);
	}

	@Override
	public void setDisplay(int x, int y, int w, int h) {
		// TODO Auto-generated method stub
		play.setDisplay(x, y, w, h);
	}

	@Override
	public void observeProgramGuide(ChannelKey ch, long focusTime) {
		// TODO Auto-generated method stub
		play.observeProgramGuide(ch, focusTime);
	}

	@Override
	public void getPresentAndFollow(ChannelKey ch) {
		// TODO Auto-generated method stub
		play.getPresentAndFollow(ch);
	}

	@Override
	public void setVoluome(float value) {
		// TODO Auto-generated method stub
		Log.d("Volume", "setVoluome--------------->"+value);
		play.setVolume(value);
	}

	@Override
	public void requestFreInfo() {
		// TODO Auto-generated method stub
		play.syncSignalStatus();
	}

	@Override
	public boolean isReadbyOK() {
		// TODO Auto-generated method stub
		return connect_step_ok&&server_step_ok;
	}

	@Override
	public void onContextReady(String group) {
		// TODO Auto-generated method stub
		selectInvalidChannel();
		connect_step_ok = true;
		LogHelper.i("init step conect done");
	}

	@Override
	public void onSelectError(String msg) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void onLiveInfoUpdated() {
		// TODO Auto-generated method stub
		selectInvalidChannel();
		server_step_ok = true;
		LogHelper.i("init step server done");
	}

	@Override
	public void onCaModuleDispatched(int moduleId) {
		// TODO Auto-generated method stub
		activity.getCAAuthManager().updateCaModule(moduleId);
	}

	@Override
	public void onShiftStartTimeUpdated(long start) {
		// TODO Auto-generated method stub
		LogHelper.i("shift start :" + start);
		mMediaMinTime = start;
	}

	@Override
	public void onShiftDuration(long duration) {
		// TODO Auto-generated method stub
		LogHelper.i("shift duration :" + duration);
	}

	@Override
	public void onShiftMediaTimeSync(long t) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void onShiftPlay(boolean succ) {
		// TODO Auto-generated method stub
		LogHelper.i("onShiftPlay:" + succ);
		if (succ) {
			if (mFirstPlay) {
				noticyShiftPrepareOK();
				mFirstPlay = false;
			}
		} else {
			mCallBack.onShiftError("≤•∑≈ ß∞‹");
		}
	}

	@Override
	public void onPfInfoUpdated(PresentAndFollow pf) {
		// TODO Auto-generated method stub
		List<LiveChannel> datas = activity.getDataManager().getAllChannel();
		
		for(LiveChannel channel : datas) {
			if (pf != null) {
				if (channel.getChannelKey().equals(pf.getChannelKey())) {
					channel.setPresent(pf.getPresentProgram());
					channel.setFollow(pf.getFollowProgram());
				}
			}
		}
		activity.getUIManager().dispatchDataChange(Constant.DATA_CHANGE_OF_PF, null);
	}

	@Override
	public void onShiftError(String msg) {
		// TODO Auto-generated method stub
		mCallBack.onShiftError(msg);
	}

}
