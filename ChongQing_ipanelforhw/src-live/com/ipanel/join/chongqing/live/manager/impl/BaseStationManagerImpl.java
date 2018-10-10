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
	protected IManager activity;// ���Žӿ�
	protected CallBack mCallBack;
	public LiveChannel currentCh;// ��ǰƵ��
	protected LiveChannel lastCh;// ��һƵ��
	private int state = PLAY_STATE_LIVE;
	/**ʱ���������жϳ�ʱ��ʱ��*/
	protected long mFlagTime = Long.MIN_VALUE;
	/**��ǰ����ʱ��*/
	protected long mPlayTime = Long.MIN_VALUE;
	/**�û�����ʱ��*/
	protected long mSettingTime = Long.MAX_VALUE;
	/**������������С��Чʱ��*/
	protected long mMediaMinTime = 0;
	/**�ϳ�ʱ��*/
	protected long mDragTime = -1;
	protected ShiftProgram mPlayTSProgram;
	protected Program mPlayProgram;
	protected String url = "";
	private int mShiftStyle = SHIFT_TYPE_WATCH_TAIL;
	/**
	 * ������Timer
	 * */
	private Timer mTimer;
	/**
	 * ������task
	 * */
	private TimerTask mTickTask;
	/**
	 * ��ǰ���ϳ����ӣ������ϳ��ٶ�
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
		 * ��ֹƵ��pf��Ϣû�л�ȡ�����ڻص�onShiftPrepareOK�л�ȡ��ͷ��ģʽ�µĽ�Ŀ��ʼʱ��
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
	 * \ ����ʱ������
	 * */
	private void startTick(long start, long end) {
		if (mTickTask != null) {
			mTickTask.cancel();
		}
		createTickTask(start, end);
		mTimer.schedule(mTickTask, TICK_STEP, TICK_STEP);
	}

	/**
	 * \ �ر�ʱ������
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
			mCallBack.onShiftError("��Ч���ŵ�ַ");
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
	 * ��������task
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
								mCallBack.onShiftError("���س�ʱ");
							}
							if (isShiftStateReady()) {
								initPlayData(start, end);
							}
							return;
						}
						if (state == PLAY_STATE_READY) {
							if (SystemClock.uptimeMillis() - mFlagTime > 10 * 1000) {
								mCallBack.onShiftError("���س�ʱ");
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
		// �ϳ���ʼʱ��������ʱ�丳ֵ���ϳ�ʱ��
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
	 * ��ȡ���Ĳ���ʱ��
	 * */
	public long getMaxShiftTime() {
		if (isHeadShiftMode()) {

		} else {

		}
//		return System.currentTimeMillis() - VEDIO_DISTANCE_LIVE;
		return System.currentTimeMillis();
	}

	/**
	 * ��ȡ��С�Ĳ���ʱ��
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
	 * ��ȡ�ϳ������е�ʱ������
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
	 * ʱ�����׼���õĻص�
	 * */
	protected void onShiftPrepareOK() {
		// TODO Auto-generated method stub

	}
	/**
	 * ֪ͨ׼����
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

	/** ѡ��Ƶ������ */
	public abstract void select(String http,long freq, int fflags, int program, int pflags);

	/** ������Ƶ����ʾ��Χ */
	public abstract void setDisplay(int x, int y, int w, int h);

	/** ��ע��Ŀָ�����ݣ�focusTime���ʱ��������ȴ���,0��ʾ����עʱ�� */
	public abstract void observeProgramGuide(ChannelKey ch, long focusTime);

	/** ��ע�Ľ�pf��Ŀ��Ϣ */
	public abstract void getPresentAndFollow(ChannelKey ch);
	/** ��ע�Ľ�pf��Ŀ��Ϣ */
	public abstract void setVoluome(float value);
	
	/** ����Ƶ����Ϣ*/
	public abstract void requestFreInfo();
}
