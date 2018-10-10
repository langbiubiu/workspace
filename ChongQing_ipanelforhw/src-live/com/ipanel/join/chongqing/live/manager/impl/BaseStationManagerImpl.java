package com.ipanel.join.chongqing.live.manager.impl;

import ipaneltv.toolkit.db.DatabaseObjectification.ChannelKey;
import ipaneltv.toolkit.db.DatabaseObjectification.Program;
import ipaneltv.toolkit.media.MediaSessionInterface.TsPlayerInetSourceInterface;

import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import android.os.SystemClock;
import android.text.TextUtils;
import android.util.Log;
import cn.ipanel.android.LogHelper;

import com.ipanel.join.protocol.a7.domain.GetAssociatedFolderContents;
import com.ipanel.join.chongqing.live.Constant;
import com.ipanel.join.chongqing.live.LiveActivity;
import com.ipanel.join.chongqing.live.LiveApp;
import com.ipanel.join.chongqing.live.manager.IManager;
import com.ipanel.join.chongqing.live.manager.SettingManager;
import com.ipanel.join.chongqing.live.manager.StationManager;
import com.ipanel.join.chongqing.live.manager.DataManager.ShiftProgram;
import com.ipanel.join.chongqing.live.navi.DatabaseObjects.LiveChannel;
import com.ipanel.join.chongqing.live.util.TimeHelper;

public abstract class BaseStationManagerImpl extends StationManager {

	protected static final long TICK_STEP = 500;
	public static final long VEDIO_DURATION = 1 * 7200 * 1000;
	public static final long VEDIO_DISTANCE_LIVE = 10 * 1000;
	private ScheduledExecutorService mPool = Executors
			.newSingleThreadScheduledExecutor();
	protected Future<?> mTask;
	protected IManager activity;// 播放接口
	protected CallBack mCallBack;
	public LiveChannel currentCh;// 当前频道
	protected LiveChannel lastCh;// 上一频道
	private int state = PLAY_STATE_LIVE;
	/**时移心跳中判断超时的时间*/
	protected long mFlagTime = Long.MIN_VALUE;
	/**当前播放时间*/
	protected long mPlayTime = Long.MIN_VALUE;
	/**用户设置时间*/
	protected long mSettingTime = Long.MAX_VALUE;
	/**服务器返回最小有效时间*/
	protected long mMediaMinTime = 0;
	/**拖扯时间*/
	protected long mDragTime = -1;
	protected ShiftProgram mPlayTSProgram;
	protected Program mPlayProgram;
	protected String url = "";
	private int mShiftStyle = SHIFT_TYPE_WATCH_TAIL;
	/**
	 * 心跳的Timer
	 * */
	private Timer mTimer;
	/**
	 * 心跳的task
	 * */
	private TimerTask mTickTask;
	/**
	 * 当前的拖扯因子，决定拖扯速度
	 * */
	private float drag_factor = 0;
	public int circle = Constant.BIG_CIRCLE_COLUME_ID;
	protected boolean mFirstPlay = true;
	protected boolean mPauseStart=false;

	public BaseStationManagerImpl(IManager context, CallBack callback) {
		this.activity = context;
		this.mCallBack = callback;
		mTimer = new Timer();

	}

	@Override
	public void goTimeShift(LiveChannel channel, long startTime, long endTime, long offTime) {
		if (isChannelValid(channel)) {
			currentCh = channel;
			
			((LiveActivity)activity).startShiftPlay(offTime, StationManager.SHIFT_TYPE_WATCH_HEAD, true, false, startTime, endTime);
		}
	}

	@Override
	public void switchTVChannel(final LiveChannel channel) {
		LogHelper.i("switch tv channel");
		// TODO Auto-generated method stub
		if (isChannelValid(channel)) {
			final long freq = channel.getChannelKey().getFrequency();
			final int prog = channel.getChannelKey().getProgram();
			final int service_type = channel.getServiceType();

			LogHelper.i("---------------S----------------------");
			LogHelper.i("switch channel");
			LogHelper.i("name: " + channel.getName());
			LogHelper.i("number: " + channel.getChannelNumber());
			LogHelper.i("frequencey: " + freq);
			LogHelper.i("tsid: " + channel.getTsId());
			LogHelper.i("Program: " + prog);
			LogHelper.i("tv: " + service_type);
			LogHelper.i("---------------E----------------------");
			
			
//			if(currentCh!=null){
//				activity.getSettingManager().setChannelVolume(currentCh);
//			}
			
			
			if (currentCh == null || !currentCh.equals(channel)) {
				lastCh = currentCh;
				currentCh = channel;
			}
			if (mTask != null && !mTask.isDone())
				mTask.cancel(false);
			mTask = mPool.schedule(new Runnable() {
				@Override
				public void run() {
					LogHelper.i("do tv change");
					LiveApp.getInstance().post(new Runnable() {

						@Override
						public void run() {
							mCallBack.onChannelBeforeChange(channel);
						}
					});
					try {
						SettingManager mDataSaveManager = activity
								.getSettingManager();
						int sound_track = SOUND_FLAGS[mDataSaveManager
								.getSoundTrackIndex()];
						int position = SCALE_FLAGS[mDataSaveManager
								.getVideoScaleIndex()];
						mDataSaveManager.setChannelVolume(channel);
						setMediaVolume(1.0f);
						select(activity.getDataManager().getLiveWebPlayURL(currentCh),freq, 0, prog, 1 | position | sound_track);
						initVideoArea();
						mDataSaveManager.setChannelVolume(channel);
						mDataSaveManager.saveHistoryChannel(channel);
						setMediaVolume(1.0f);

						LiveApp.getInstance().post(new Runnable() {

							@Override
							public void run() {
								mCallBack.onChannelAfterChange(channel);
							}
						});
					} catch (NullPointerException e) {
						e.printStackTrace();
					}

				}
			}, 100, TimeUnit.MILLISECONDS);

		} else {
			LogHelper.e("channel is invalid");
		}

	}

	private boolean isChannelValid(LiveChannel channel) {
		return channel != null && channel.getLogNumber() == 0;
	}

	@Override
	public LiveChannel getPlayChannel() {
		// TODO Auto-generated method stub
		return currentCh;
	}

	@Override
	public LiveChannel getLastCannel() {
		// TODO Auto-generated method stub
		return lastCh;
	}

	@Override
	public boolean isShiftMode() {
		// TODO Auto-generated method stub
		return state != PLAY_STATE_LIVE;
	}

	@Override
	public boolean isShifStart() {
		// TODO Auto-generated method stub
		return state != PLAY_STATE_LIVE&&state!=PLAY_STATE_READY&&state!=PLAY_STATE_REQUEST;
	}
	
	@Override
	public void initVideoArea() {
		if (activity.getContext().getResources().getDisplayMetrics().widthPixels >= 1920) {
			setDisplay(0, 0, 1920, 1080);
		} else {
			setDisplay(0, 0, 1280, 720);
		}
	}

	@Override
	public void selectInvalidChannel() {
		LogHelper.i("selectInvalidChannel  null  0   0   0   0 ");
		select(null, 0, 0, 0, 0);
	}

	@Override
	public void clearPlayData() {
		// TODO Auto-generated method stub
		currentCh = null;
		lastCh = null;
		state = PLAY_STATE_LIVE;
		if (mTask != null && !mTask.isDone())
			mTask.cancel(false);
	}

	@Override
	public void setPlayChannel(LiveChannel channel) {
		// TODO Auto-generated method stub
		if (isChannelValid(channel)) {
			currentCh = channel;
		} else {
			LogHelper.e("error state at set play channel :invalid channel");
		}
	}

	@Override
	public void startShiftPlay(long time, int style, boolean ip,boolean pause, long start, long end) {
		// TODO Auto-generated method stub
		mShiftStyle = style;
		Constant.IP_REROURCE = ip;
		mPauseStart=pause;
//		if (time <= 0) {
//			mSettingTime = System.currentTimeMillis();
//			if (isHeadShiftMode()) {
//				mPlayProgram = activity.getDataManager()
//						.getChannelCurrentProgram(currentCh);
//				if (mPlayProgram != null) {
//					mSettingTime = mPlayProgram.getStart();
//				}
//			}
//		} else {
//			mSettingTime = time;
//		}
		/*
		 * 防止频道pf信息没有获取到，在回调onShiftPrepareOK中获取从头看模式下的节目开始时间
		 */
		if (time <= 0) {
			mSettingTime = System.currentTimeMillis();
		} else {
			mSettingTime = time;
		}
		mPlayTime = mSettingTime;
		mPlayTSProgram = null;
		mFirstPlay = true;
		url = "";
		mFlagTime = SystemClock.uptimeMillis();
		createShiftPlayer(start, end);
		recordStateChange(PLAY_STATE_REQUEST);

	}

	@Override
	public void startLivePlay() {
		// TODO Auto-generated method stub
		LogHelper.i("start live play");
		destroyShiftPlayer();
		switchTVChannel(currentCh);
		recordStateChange(PLAY_STATE_LIVE);
	}

	public void recordStateChange(final int state) {

		LogHelper.i(String.format("play state change from %s to %s",
				this.state, state));
		this.state = state;
		LiveApp.getInstance().post(new Runnable() {

			@Override
			public void run() {
				// TODO Auto-generated method stub
				mCallBack.onPlayStateChanged(state);
			}
		});

	}

	public void createShiftPlayer(long start, long end) {
		startTick(start, end);
	}

	public void destroyShiftPlayer() {
		LogHelper.i("destroy shift player");
		Constant.SHIFT_DATA_READY=false;
		stopTick();
	}

	/**
	 * \ 开启时移心跳
	 * */
	private void startTick(long start, long end) {
		if (mTickTask != null) {
			mTickTask.cancel();
		}
		createTickTask(start, end);
		mTimer.schedule(mTickTask, TICK_STEP, TICK_STEP);
	}

	/**
	 * \ 关闭时移心跳
	 * */
	private void stopTick() {
		if (mTickTask != null) {
			mTickTask.cancel();
		}
	}

	protected void seekTo(long time) {
		LogHelper.i("jump time: " + TimeHelper.getDetailTime(time));
		mPlayTime = time;
		setMediaSeekInteral(changeSeekTime(time));
		recordStateChange(PLAY_STATE_PLAYING);
	}

	protected long changeSeekTime(long time) {
		return time;
	}

	private void initPlayData(long startTime, long endTime) {
		url = activity.getDataManager().getShiftPlayURL(currentCh, startTime, endTime);
		LogHelper.i("shift url :" + url);
		if (TextUtils.isEmpty(url)) {
			mCallBack.onShiftError("无效播放地址");
		} else {
			LogHelper.i("start shift success");
			setMediaURLInteral(url);
//			recordStateChange(PLAY_STATE_READY);
			recordStateChange(PLAY_STATE_PLAYING);
		}
	}

	public int getTSType() {
		return Constant.IP_REROURCE ? TsPlayerInetSourceInterface.STREAM_TYPE_INET
				: TsPlayerInetSourceInterface.STREAM_TYPE_IPQAM;// 2 // ipQam
	}

	public boolean isShiftStateReady() {
		return Constant.COUNT_DOWN_READY && Constant.SHIFT_DATA_READY;
	}

	public ShiftProgram getPlayingTSProgram() {
		if (mPlayTSProgram != null
				&& TimeHelper.isPlaying(mPlayTSProgram.getStart(),
						mPlayTSProgram.getEnd(), getPlayTime())) {
			return mPlayTSProgram;
		}
		ShiftProgram result = activity.getDataManager().getShiftProgramAtTime(
				currentCh, getPlayTime());
		if (result != null) {
			mPlayTSProgram = result;
		} else {
			activity.getDataManager().requestShiftProgram(currentCh);
		}
		return mPlayTSProgram;
	}

	/**
	 * 创建心跳task
	 * */
	private void createTickTask(final long start, final long end) {
		mTickTask = new TimerTask() {
			public void run() {
				LiveApp.getInstance().post(new Runnable() {
					@Override
					public void run() {
						// TODO Auto-generated method stub
						if (state == PLAY_STATE_LIVE) {
							LogHelper.i("stop tick for live state");
							if (mTickTask != null) {
								mTickTask.cancel();
							}
							return;
						}
						if (state == PLAY_STATE_REQUEST) {
							if (SystemClock.uptimeMillis() - mFlagTime > 10 * 1000) {
								mCallBack.onShiftError("加载超时");
							}
							if (isShiftStateReady()) {
								initPlayData(start, end);
							}
							return;
						}
						if (state == PLAY_STATE_READY) {
							if (SystemClock.uptimeMillis() - mFlagTime > 10 * 1000) {
								mCallBack.onShiftError("加载超时");
							}
							return;
						}
						if (state == PLAY_STATE_PLAYING) {
							mPlayTime += TICK_STEP;
							mCallBack.onShiftTick();
						}
					}
				});
			}
		};
	}

	@Override
	public long getPlayTime() {
		// TODO Auto-generated method stub
		if (mDragTime >= 0) {
			return mDragTime;
		} else {
			return mPlayTime;
		}
	}

	@Override
	public void playOrPauseMedia() {
		// TODO Auto-generated method stub
		if (!isInvalidMedia()) {
			LogHelper.e("invalid action for bad time");
			return;
		}
		if (state == PLAY_STATE_PAUSE || state == PLAY_STATE_SPEED
				|| state == PLAY_STATE_ERROR) {
			setMediaPlayInteral(url);
			recordStateChange(PLAY_STATE_PLAYING);
		} else {
			setMediaPauseInteral(url);
			recordStateChange(PLAY_STATE_PAUSE);
		}
	}

	@Override
	public void startDragMedia(boolean forward) {
		// TODO Auto-generated method stub
		if (!isInvalidMedia()) {
			LogHelper.e("invalid action for bad time");
			return;
		}
		if (getPlayTime() < getMinShiftTime() - 60 * 1000) {
			LogHelper.i("drag not ready");
			return;
		}
		drag_factor += 0.05f;
		drag_factor = Math.min(1, drag_factor);
		// 拖扯开始时，将播放时间赋值给拖扯时间
		if (mDragTime < 0) {
			mDragTime = getPlayTime();
		}
		mDragTime = mDragTime + getPlayFactor(forward);
		mDragTime = Math.max(0, mDragTime);
		if (mDragTime > getMaxShiftTime()) {
			mDragTime = getMaxShiftTime();
			mCallBack.onPlayMaxDot();
		} else if (mDragTime < getMinShiftTime()) {
			mDragTime = getMinShiftTime() + 10;
			mCallBack.onPlayMinDot();
		} else {
			recordStateChange(PLAY_STATE_DRAG);
		}
	}

	@Override
	public void stopDragMedia() {
		// TODO Auto-generated method stub
		if (!isInvalidMedia()) {
			LogHelper.e("invalid action for bad time");
			return;
		}
		if (mDragTime >= 0) {
			seekTo(mDragTime);
		}
		mDragTime = -1;
		mRate=1;
		drag_factor = 0;
	}
	
	@Override
	public long getDragTime() {
		return mDragTime;
	}

	@Override
	public void changeMediaSpeed(boolean forward) {
		// TODO Auto-generated method stub

	}

	@Override
	public void handleNetChange(boolean valid) {
		// TODO Auto-generated method stub
		if (valid) {
			if (state == PLAY_STATE_ERROR) {
				playOrPauseMedia();
			}
		} else {
			if (isShiftMode()) {
				recordStateChange(PLAY_STATE_ERROR);
			}
		}
	}

	public void requestChannelPF(ChannelKey key) {
		// TODO Auto-generated method stub
		// play.getPresentAndFollow(key);
	}

	@Override
	public void seekMedia(long time) {
		// TODO Auto-generated method stub
		if (isInvalidMedia()) {
			LogHelper.i("ignore this action for bad media");
			return;
		}
		seekTo(time);
	}

	private boolean isInvalidMedia() {
		return state != PLAY_STATE_LIVE && state != PLAY_STATE_READY
				&& state != PLAY_STATE_REQUEST;
	}

	/**
	 * 获取最大的播放时间
	 * */
	public long getMaxShiftTime() {
		if (isHeadShiftMode()) {

		} else {

		}
//		return System.currentTimeMillis() - VEDIO_DISTANCE_LIVE;
		return System.currentTimeMillis();
	}

	/**
	 * 获取最小的播放时间
	 * */
	public long getMinShiftTime() {
//		if (isHeadShiftMode()) {
//			return mPlayProgram.getStart();
//		} else {
//			long time = System.currentTimeMillis() - VEDIO_DURATION;
//			return Math.max(time, mMediaMinTime);
//		}
		
		long time = System.currentTimeMillis() - VEDIO_DURATION;
		return Math.max(time, mMediaMinTime);

	}

	/**
	 * 获取拖扯过程中的时间增量
	 * */

	private int mRate = 1;

	private long getPlayFactor(boolean backforword) {
		int speed = Math.abs(mRate);
		if (backforword) {
			if (mRate < 0) {
				speed = Math.max(1, speed / 2);
			} else {
				speed = Math.min(8, speed * 2);
			}
			mRate = speed;
		} else {
			if (mRate > 0) {
				speed = Math.max(1, speed / 2);
			} else {
				speed = Math.min(8, speed * 2);
			}
			mRate = -speed;
		}
		return (getMaxShiftTime()-getMinShiftTime())* mRate / 200;
	}
	@Override
	public void setPlayCircle(int circle) {
		// TODO Auto-generated method stub
		this.circle = circle;
	}

	@Override
	public int getPlayCircle() {
		// TODO Auto-generated method stub
		return circle;
	}

	@Override
	public boolean isHeadShiftMode() {
		// TODO Auto-generated method stub
		return mShiftStyle == SHIFT_TYPE_WATCH_HEAD;
	}

	@Override
	public Program getHeadShiftProgram() {
		// TODO Auto-generated method stub
		return mPlayProgram;
	}

	@Override
	public boolean isPlayingMedia() {
		// TODO Auto-generated method stub
		return state == PLAY_STATE_PLAYING;
	}

	@Override
	public boolean isIPResourceShift() {
		// TODO Auto-generated method stub
		return Constant.IP_REROURCE;
	}
	/**
	 * 时移最后准备好的回调
	 * */
	protected void onShiftPrepareOK() {
		// TODO Auto-generated method stub

	}
	/**
	 * 通知准备好
	 * */
	public void noticyShiftPrepareOK(){
		LogHelper.i("noticy shift prepare ok");
		LiveApp.getInstance().postDelayed(new Runnable() {
			
			@Override
			public void run() {
				// TODO Auto-generated method stub
				if(isShiftMode()&&isShiftStateReady()){
					mCallBack.onShiftReady();
					onShiftPrepareOK();
				}else{
					LogHelper.e("noticy shift ok at bad time");
				}
			}
		}, 1000);

	}
	@Override
	public void setMediaRegion(int x, int y, int w, int h) {
		// TODO Auto-generated method stub
		setDisplay(x,y,w,h);
	}
	
	@Override
	public void setMediaVolume(float value) {
		// TODO Auto-generated method stub
		setVoluome(value);
	}

	public abstract void setMediaURLInteral(String url);

	public abstract void setMediaSeekInteral(long time);

	public abstract void setMediaPauseInteral(String uri);

	public abstract void setMediaPlayInteral(String url);

	/** 选择频道播放 */
	public abstract void select(String http,long freq, int fflags, int program, int pflags);

	/** 设置视频的显示范围 */
	public abstract void setDisplay(int x, int y, int w, int h);

	/** 关注节目指南数据，focusTime距此时间近则优先处理,0表示不关注时间 */
	public abstract void observeProgramGuide(ChannelKey ch, long focusTime);

	/** 关注的节pf节目信息 */
	public abstract void getPresentAndFollow(ChannelKey ch);
	/** 关注的节pf节目信息 */
	public abstract void setVoluome(float value);
	
	/** 请求频点信息*/
	public abstract void requestFreInfo();
}
