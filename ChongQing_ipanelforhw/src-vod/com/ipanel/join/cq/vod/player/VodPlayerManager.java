package com.ipanel.join.cq.vod.player;

import ipaneltv.toolkit.media.MediaSessionInterface.TsPlayerInetSourceInterface;

import java.util.Timer;
import java.util.TimerTask;

import android.app.Activity;
import android.content.Context;
import android.graphics.Rect;
import android.media.AudioManager;
import android.media.TeeveePlayer;
import android.os.Handler;
import android.os.Message;
import android.os.RemoteException;
import android.util.DisplayMetrics;
import android.util.Log;
import cn.ipanel.android.LogHelper;
import cn.ipanel.android.reflect.SysUtils;

import com.ipanel.chongqing_ipanelforhw.R;
import com.ipanel.join.cq.settings.aidl.IDataSet;
import com.ipanel.join.cq.vod.player.impl.PlayCallback;
import com.ipanel.join.cq.vod.player.impl.PlayInterface;
import com.ipanel.join.cq.vod.player.impl.VodFragment;
import com.ipanel.join.cq.vod.utils.Logger;
import com.ipanel.join.cq.vod.utils.TimeUtility;

public class VodPlayerManager {
	static final String TAG = VodPlayerManager.class.getSimpleName();
	
	public static String KEY_STREAM_FD = "stream_fd";

	/**
	 * 默认的音量key
	 * */
	public static final String DEFAULT_VOLUME_KEY = "default_key";
	/**
	 * 播放准备中
	 * */
	public static final int PLAY_STATE_READY = 1;
	/**
	 * 播放进行中
	 * */
	public static final int PLAY_STATE_PLAYING = 2;
	/**
	 * 播放暂停中
	 * */
	public static final int PLAY_STATE_PAUSE = 3;
	/**
	 * 播放快退中
	 * */
	public static final int PLAY_STATE_TRANSLATE_LEFT = 5;
	/**
	 * 播放快进中
	 * */
	public static final int PLAY_STATE_TRANSLATE_RIGHT = 6;
	/**
	 * 播放错误中
	 * */
	public static final int PLAY_STATE_ERROR = 7;
	/**
	 * 播放关闭中
	 * */
	public static final int PLAY_STATE_DESTROY = 8;
	/**
	 * 播放拖扯中
	 * */
	public static final int PLAY_STATE_DRAG = 9;
	/**
	 * 播放倍速中
	 * */
	public static final int PLAY_STATE_SPEED = 10;
	/**
	 * 系统音量和播放器音量是否关联
	 * */
	private boolean related = true;
	/**
	 * 当前播放的频率
	 * */
	private String freqN = null;
	/**
	 * 当前播放的service
	 * */
	private String programN = null;
	/**
	 * 当前的播放状态
	 * */
	private int play_state = PLAY_STATE_READY;
	/**
	 * 当前的播放地址
	 * */
	private String url;
	/**
	 * 心跳的Timer
	 * */
	private Timer mTimer;
	/**
	 * 心跳的task
	 * */
	private TimerTask mTickTask;
	/**
	 * 心跳值
	 * */
	private int HEART_BEAT = 1000;
	/**
	 * 当前的拖扯时间
	 * */
	private long drag_time = -1;
	/**
	 * 当前的拖扯因子，决定拖扯速度
	 * */
	private float drag_factor = 0;

	public float getDrag_factor() {
		return drag_factor;
	}

	public void setDrag_factor(float drag_factor) {
		this.drag_factor = drag_factor;
	}

	/**
	 * 设置的服务
	 * */
	private IDataSet mIDataSet;
	/**
	 * 播放的服务
	 * */
	/** 历史播放时间 */
	private long historyTime;
	//private IControl mIControl;
	PlayInterface playIf;
	private SimplePlayerActivity mActivity;
	
	PlayCallback playCallback = new PlayCallback() {
		private String tag = "playCallback";
		@Override
		public void onVodDuration(long d) {
			Log.d(tag, "onVodDuration d = "+d);
			duration = d/1000;
		}
		
		@Override
		public void onSyncMediaTime(long t) {
			Log.d(tag, "onSyncMediaTime t = "+t);
			elapsed = t/1000;//elapsed单位为秒
			Log.d(TAG, "onSyncMediaTime elapsed:" + elapsed);
		}
		
		@Override
		public void onSourceStart(boolean b) {
			Log.d(tag, "onSourceStart b = "+b);
		}
		
		@Override
		public void onSourceSeek(long t) {
			Log.d(tag, "onSourceSeek t = "+t);
			elapsed = t/1000;
		}
		
		@Override
		public void onSourceRate(float r) {
			Log.d(tag, "onSourceRate r = "+r);
		}
		
		@Override
		public void onShiftStartTime(long t) {
			Log.d(tag, "onShiftStartTime t = "+t);
		}
		
		@Override
		public void onServiceReady() {
			Log.d(tag, "onServiceReady");
		}
		
		@Override
		public void onSeeBackPeriod(long s, long e) {
			Log.d(tag, "onServiceReady s="+s+", e="+e);
		}
		
		@Override
		public void onPlayTime(long time) {
			Log.d(tag, "onPlayTime time="+time);
		}
		
		@Override
		public void onPlayStart(boolean b) {
			Log.d(tag, "onPlayStart b="+b);
			if (b) {
				DisplayMetrics dm = mActivity.getResources().getDisplayMetrics();
				playIf.setDisplay(new Rect(0,0,dm.widthPixels,dm.heightPixels));
				setPlayState(PLAY_STATE_PLAYING);
				if (callback != null){
					callback.onPrepareSuccess();
					if(historyTime > 0){
						Log.i(TAG, "seek to " + historyTime+",elapsed"+elapsed);
						seekTo(historyTime);
						historyTime = 0;//书签进入，防止再次选时
					}
				}
				startTick();
			}
		}
		
		@Override
		public void onPlayMsgId(int string_id) {
			Log.d(tag, "onPlayMsgId string_id="+string_id);
			
		}
		
		@Override
		public void onPlayMsg(String msg) {
			Log.d(tag, "onPlayMsg msg="+msg);
			
		}
		
		@Override
		public void onPlayErrorId(int string_id) {
			Log.d(tag, "onPlayErrorId id="+string_id);
			
		}
		
		@Override
		public void onPlayError(String msg) {
			Log.d(tag, "onPlayError msg="+msg);
			if(callback != null)
				callback.onPlayFailed(msg);
		}
		
		@Override
		public void onPlayEnd() {
			Log.d(tag, "onPlayEnd");
			if(callback != null){
				//callback.onPlayEnd();
				if (play_state == PLAY_STATE_SPEED) {
					long current = getElapsed();
					Log.i("wuhd", "elapsed" + elapsed);
					if (getCurrentPlaySpeed() < 0) {
						//快退回起点
						Log.i(TAG, "current1:"+current+",speed"+speed);
						callback.fastReverseStart();
						seekTo(3);
					}else{
						//快进到终点
						Log.i(TAG, "current2:"+current+",speed:"+speed);
						callback.onPlayEnd();
					}
				}else{
					Log.i(TAG, "current speed: "+speed);
					//正常停止
					callback.onPlayEnd();
				}
			}
		}
	};
	
	private long duration;
	private long elapsed;
	private int speed = 1;
	
	private boolean bind_flag = false;;
	private Context mContext;
	private PlayCallBack callback;
	private AudioManager mAudioManager;
	private final static int CHANGE_TIME = 555;
	private final Handler mHandler = new Handler() {

		@Override
		public void handleMessage(Message msg) {
			// TODO Auto-generated method stub
			super.handleMessage(msg);
			switch (msg.what) {
			case CHANGE_TIME:
				drag_factor = 6;
				break;
			}
		}

	};

	private static VodPlayerManager mInstance;

	public static synchronized VodPlayerManager getInstance(Context cxt) {
		if (mInstance == null) {
			mInstance = new VodPlayerManager(cxt);
		}
		return mInstance;
	}

	private VodPlayerManager(Context cxt) {
		mContext = cxt.getApplicationContext();
		mTimer = new Timer();
		mAudioManager = (AudioManager) mContext
				.getSystemService(Context.AUDIO_SERVICE);
	}

	/**
	 * 设置播放回调
	 * */
	public void setPlayCallBack(PlayCallBack cb) {
		this.callback = cb;
	}

	/**
	 * 获得当前的播放状态
	 * */
	public int getPlayState() {
		return play_state;
	}

	/**
	 * 获取当前的播放时长
	 * */
	public long getDuration() {
		return duration;
	}

	/**
	 * 获取当前的播放进度
	 * */
	public long getElapsed() {
		Log.i("", "drag_time=" + drag_time);
		Log.i("", "getElapsed()=" + elapsed);
		if (drag_time >= 0) {
			return drag_time;
		} else {
			return elapsed;
		}
	}

	/**
	 * 获取当前显示的播放时长
	 * */
	public String getShowDuration() {
		return TimeUtility.formatTime(getDuration());
	}

	/**
	 * 获取当前显示的播放进度
	 * */
	public String getShowElapsed() {
		return TimeUtility.formatTime(getElapsed());
	}

	/**
	 * 对于确认键的逻辑处理
	 * */
	public void doPressEnterKey() {
		if (!isMediaValid()) {
			Logger.d("invalid action for bad time");
			return;
		}
		if (play_state == PLAY_STATE_PAUSE||play_state==PLAY_STATE_SPEED) {
			playIf.resume();
			speed = 1;//播放的时候，设置速度恢复为1
			setPlayState(PLAY_STATE_PLAYING);
		} else {
			playIf.pause();
			setPlayState(PLAY_STATE_PAUSE);
		}
	}
	/**
	 * 对于返回键的逻辑处理
	 * resume继续播放
	 * false则暂停播放
	 * */
	public void doPressBackKey(boolean resume) {
		if (!isMediaValid()) {
			Logger.d("invalid action for bad time");
			return;
		}
		try {
			if (resume) {
				playIf.resume();
				speed = 1;//播放的时候，设置速度恢复为1
				setPlayState(PLAY_STATE_PLAYING);
			} else {
				playIf.pause();
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	/**
	 * 对于拖扯中的逻辑处理
	 * */

	public void dragTime(boolean backforward) {
		if (!isMediaValid()) {
			Logger.d("invalid action for bad time");
			return;
		}
		// drag_factor += 0.25f;
		// drag_factor = Math.min(1, drag_factor);
		// 拖扯开始时，将播放时间赋值给拖扯时间
		if (drag_time < 0) {
			drag_time = getElapsed();
		}
		drag_time = drag_time + getPlayFactor(backforward);
		Log.i("VodPlayerManager", "drag_time="+drag_time+",backforward:"+backforward+",getPlayFactor="+getPlayFactor(backforward));
		drag_time = Math.max(0, drag_time);
		if (drag_time == 0) {
			callback.fastReverseStart();
		}
		if (drag_time > getDuration()) {
			drag_time = getDuration();
			callback.onPlayEnd();
		} else {
			setPlayState(PLAY_STATE_DRAG);
		}

		mHandler.sendEmptyMessageDelayed(CHANGE_TIME, 5000);

	}

	/**
	 * 对于拖扯结束的逻辑处理
	 * */
	public void dragFinish() {
		if (!isMediaValid()) {
			Logger.d("invalid action for bad time");
			return;
		}
		if (drag_time >= 0) {
			seekTo(drag_time);
		}
		drag_time = -1;
		drag_factor = 0;
		mHandler.removeMessages(CHANGE_TIME);

	}

	/**
	 * 对于播放seek的逻辑处理
	 * */
	public void seekTo(long time) {
		if (!isMediaValid()) {
			Logger.d("invalid action for bad time");
			return;
		}
		Logger.d("to time time: " + TimeUtility.formatTime(time));
		if(playIf!=null){
			playIf.seek(time*1000);
		}
		recoverPlaySpeed();
		callback.onCleanCache();
		setPlayState(PLAY_STATE_PLAYING);
	}
	
	public void recoverPlaySpeed(){
		if(getCurrentPlaySpeed()!=1&&callback!=null){
			callback.onSpeedChange(1);
			speed = 1;
		}
	}
	public void changePlaySpeed(boolean fastforward) {
		if (!isMediaValid()) {
			LogHelper.e("invalid action for bad time");
			return;
		}
		if(play_state==PLAY_STATE_PAUSE){
			playIf.resume();
			callback.onResumeMedia();
		}
		LogHelper.i(String.format("speed is %s before change",
				getCurrentPlaySpeed()));
		Log.i("", "drag_time="+drag_time+",elapsed="+getElapsed());
		if (fastforward) {
			fastForward();
		} else {
			backForward();
		}
		callback.onSpeedChange(speed);
		LogHelper.i(String.format("speed is %s after change",
				getCurrentPlaySpeed()));
		setPlayState(PLAY_STATE_SPEED);
		startTick();
	}
	
	private void backForward() {
		if(speed >= -1)
			speed = -8;
		else if (speed > -32)
			speed *= 2;
		else
			speed = -8;
	}

	private void fastForward() {
		if(speed<=1)
			speed = 8;
		else if (speed < 32)
			speed *= 2;
		else 
			speed = 8;
		
	}

	public void onNetworkChange(boolean valid){
		if(!valid&&isMediaValid()){
			LogHelper.e("play error for network change bad");
			mHandler.post(new Runnable() {
				@Override
				public void run() {
					if (callback != null) {
						callback.onPlayFailed("网络已断开，请稍后再试");
					}
				}
			});
		}
	}
	public int getCurrentPlaySpeed() {
		return speed;
	}

	public boolean isSpeeding() {
		return play_state == PLAY_STATE_SPEED;
	}

	int[] direct_res = { R.drawable.vod_epg_l, R.drawable.vod_epg_r };
	int[] value_res = { R.drawable.vod_epg_8x, R.drawable.vod_epg_16x,
			R.drawable.vod_epg_32x };

	public int[] getCurrentSpeedImage() {
		int speed = getCurrentPlaySpeed();
		int value = (Math.abs(speed) / 8);
		value = Math.min(3, value);
		int direct = speed > 0 ? 1 : 0;
		if (!isSpeeding()) {
			return new int[] { R.drawable.translucent_background,
					R.drawable.translucent_background };
		}
		if (value == 0) {
			return new int[] { R.drawable.translucent_background,
					R.drawable.translucent_background };
		} else {
			if (direct > 0) {
				return new int[] { direct_res[direct], value_res[value - 1] };
			} else {
				return new int[] { value_res[value - 1], direct_res[direct] };
			}
		}
	}

	/**
	 * 创建播放器
	 * */
	public void createPlayer() {
		Logger.d("createPlayer");
		setPlayState(PLAY_STATE_READY);
	}

	/**
	 * 销毁播放器
	 * **/
	public void destroyPlayer() {
		Logger.d("destroyPlayer");
		setPlayState(PLAY_STATE_DESTROY);
		stopTick();
		callback.onMediaDestroy();
		try {
			if (playIf != null) {
				playIf.stop();
				playIf = null;
			}
		} catch (Exception e) {
			e.printStackTrace();
		}

	}

	/**
	 * 准备播放
	 * */
	public PlayInterface preparePlayer(Activity activity,VodFragment vodf, String url, long historyTime) {
		this.mActivity = (SimplePlayerActivity) activity;
		Logger.d("preparePlayer");
		this.url = url;
		this.historyTime = historyTime;
		this.drag_time = -1;
		this.duration = 0;
		this.elapsed = historyTime > 0 ? historyTime : 0;
		
		playIf = vodf.getPlayInterface(playCallback);
		playIf.play(url, TsPlayerInetSourceInterface.TYPE_VOD, TsPlayerInetSourceInterface.STREAM_TYPE_IPQAM, 0);
		
		return playIf;
	}

	/**
	 * 设置当前的播放状态
	 * */
	public void setPlayState(int state) {
		Logger.d("set play state :" + state);
		this.play_state = state;
		mHandler.post(new Runnable() {
			
			@Override
			public void run() {
				callback.onPlayStateChange(play_state);
			}
		});
	}

	/**
	 * 设置service的初始化
	 * */
	public void setIDataSet(IDataSet mDataSet) {
		this.mIDataSet = mDataSet;
	}

	/**
	 * 获取设置service
	 * */
	public IDataSet getIDataSet() {
		return mIDataSet;
	}

	/**
	 * 刷新当前音量
	 * */
	public void updateVolume() {
		if (related) {
			callback.setVolume(1);
		} else {
			int maxVolume = mAudioManager
					.getStreamMaxVolume(AudioManager.STREAM_MUSIC);
			int currentVolume = mAudioManager
					.getStreamVolume(AudioManager.STREAM_MUSIC);
			callback.setVolume((float) currentVolume / maxVolume);
		}
	}

	/**
	 * 更改音量
	 * */
	public void changeVolume(boolean up) {
		int maxVolume = mAudioManager
				.getStreamMaxVolume(AudioManager.STREAM_MUSIC);
		int currentVolume = mAudioManager
				.getStreamVolume(AudioManager.STREAM_MUSIC);
		int value = 0;
		if (up) {
			value = Math.min(maxVolume, ++currentVolume);
		} else {
			value = Math.max(0, --currentVolume);
		}
		if (getMuteValue()) {
			if (value > 0) {
				changeMute(false);
			}
		} else {
			if (value <= 0) {
				changeMute(true);
			}
		}
		mAudioManager.setStreamVolume(AudioManager.STREAM_MUSIC, value, 0);
		updateVolume();
	}

	/**
	 * 更改静音
	 * */
	public void changeMute(boolean mute) {
		mAudioManager.setStreamMute(AudioManager.STREAM_MUSIC, mute);
		updateVolume();
		if (callback != null) {
			callback.onMuteStateChange(mute);
		}
	}

	/**
	 * 更改显示比例
	 * */
	public void changeScale(int value) {
		try {
			mIDataSet.setDisplayRatio(value);
			callback.onNeedPlayMedia(freqN, programN, getPlayFlag());
		} catch (RemoteException e) {
			e.printStackTrace();
		} catch (NullPointerException e) {
			e.printStackTrace();
		}
	}

	/**
	 * 更改显示比例
	 * */
	public void changeScale() {
		try {
			int value = (getScaleValue() + 1) % 2;
			mIDataSet.setDisplayRatio(value);
			callback.onNeedPlayMedia(freqN, programN, getPlayFlag());
		} catch (RemoteException e) {
			e.printStackTrace();
		} catch (NullPointerException e) {
			e.printStackTrace();
		}
	}

	/**
	 * 更改声道
	 * */
	public void changeSoundTrack(int value) {
		try {
			mIDataSet.setSound_Track(value);
			callback.onNeedPlayMedia(freqN, programN, getPlayFlag());
		} catch (RemoteException e) {
			e.printStackTrace();
		} catch (NullPointerException e) {
			e.printStackTrace();
		}
	}

	/**
	 * 更改声道
	 * */
	public void changeSoundTrack() {
		try {
			if (isSystemMute()) {
				getAudioManager().setStreamMute(AudioManager.STREAM_MUSIC,
						false);
				getAudioManager().adjustStreamVolume(AudioManager.STREAM_MUSIC,
						AudioManager.ADJUST_SAME, AudioManager.FLAG_SHOW_UI);
			}
			int value = (getSoundTrackValue() + 1) % 3;
			mIDataSet.setSound_Track(value);
			callback.onNeedPlayMedia(freqN, programN, getPlayFlag());
		} catch (RemoteException e) {
			e.printStackTrace();
		} catch (NullPointerException e) {
			e.printStackTrace();
		}
	}

	public boolean isSystemMute() {
		boolean mute = SysUtils.isStreamMute(getAudioManager(),
				AudioManager.STREAM_MUSIC);
		LogHelper.i(String.format("mute: %s", mute + ""));
		return mute;
	}

	private AudioManager getAudioManager() {
		return (AudioManager) mContext.getSystemService(Context.AUDIO_SERVICE);
	}

	/**
	 * 更改3D模式
	 * */
	public void change3DMode(int value) {
		callback.onNeedPlayMedia(freqN, programN, getPlayFlag());
	}

	/**
	 * 获得播放参数
	 * */
	public int getPlayFlag() {
		return 1 | getScaleFlag() | getSoundTrackFlag();
	}

	/**
	 * 获得静音设置的值
	 * */
	public boolean getMuteValue() {
		return SysUtils.isStreamMute(mAudioManager, AudioManager.STREAM_MUSIC);
	}

	/**
	 * 获得音量的值
	 * */
	public int getVolumeValue() {
		return mAudioManager.getStreamVolume(AudioManager.STREAM_MUSIC);
	}

	/**
	 * 获得音量的值
	 * */
	public int getMaxVolumeValue() {
		return mAudioManager.getStreamMaxVolume(AudioManager.STREAM_MUSIC);
	}

	/**
	 * 获得声道设置的值
	 * */
	public int getSoundTrackValue() {
		try {
			return mIDataSet.getSound_Track();
		} catch (RemoteException e) {
			e.printStackTrace();
		}
		return 0;
	}

	/**
	 * 获得比例设置的值
	 * */
	public int getScaleValue() {
		try {
			return mIDataSet.getDisplayRatio();
		} catch (RemoteException e) {
			e.printStackTrace();
		}
		return 0;
	}

	/**
	 * 获得3D设置的值
	 * */
	public int get3DModeValue() {
		return 0;
	}

	/**
	 * 获得声道设置的值
	 * */
	public int getSoundTrackFlag() {
		int result = 0;
		int value = getSoundTrackValue();
		Logger.d("sound :" + value);
		switch (value) {
		case 1:
			result = TeeveePlayer.FLAG_AUDIO_LEFT_TO_MONO;
			break;
		case 2:
			result = TeeveePlayer.FLAG_AUDIO_RIGHT_TO_MONO;
			break;
		default:
			result = 0;
			break;
		}
		Logger.d(String.format("get sound track flag: %d", result));
		return result;
	}

	/**
	 * 获得比例设置的值
	 * */
	public int getScaleFlag() {
		int result = 0;
		switch (getScaleValue()) {
		case 1:
			result = TeeveePlayer.FLAG_VIDEO_TRANSFORM_NOCLIP_FILL;
			break;
		default:
			result = TeeveePlayer.FLAG_VIDEO_TRANSFORM_RATIO_ADAPTION;
			break;
		}
		Logger.d(String.format("get scale flag: %d", result));
		return result;
	}

	/**
	 * 获得3D设置的值
	 * */
	public int get3DModeFlag() {
		return 0;
	}

	/**
	 * 绑定播放服务
	 * */
	private void bindPlayService() {
		Logger.d("bindPlayService");
//		Intent intent = new Intent();
//		intent.setAction("com.ipanel.join.huawei.vod.IControl");
//		mContext.bindService(intent, mVodPlayConnection,
//				Context.BIND_AUTO_CREATE);
	}

	/**
	 * 创建心跳task
	 * */
	private void createTickTask() {
		mTickTask = new TimerTask() {
			@Override
			public void run() {
				mHandler.post(new Runnable() {

					@Override
					public void run() {
						if (play_state == PLAY_STATE_PLAYING || play_state == PLAY_STATE_SPEED) {
							elapsed += speed;
							callback.onPlayTick();
						}
					}
				});
			}
		};
	}

	/**
	 * 获取拖扯过程中的时间增量
	 * */
	private int getPlayFactor(boolean backforword) {
		int result = 0;
		result = (int) (drag_factor * 5 + 10);
		Log.i("VodPlayerManager", "drag_factor="+drag_factor);
		return backforword ? -result : result;
	}

	/**
	 * \ 开启时移心跳
	 * */
	private void startTick() {
		Log.i(TAG, "startTick--");
		if (mTickTask != null) {
			mTickTask.cancel();
		}
		createTickTask();
		mTimer.schedule(mTickTask, HEART_BEAT, HEART_BEAT);
	}

	/**
	 * \ 关闭时移心跳
	 * */
	private void stopTick() {
		if (mTickTask != null) {
			mTickTask.cancel();
		}
	}

	/**
	 * 播放器是否处于有效状态
	 * */
	private boolean isMediaValid() {
		return getPlayState() != PLAY_STATE_DESTROY
				&& getPlayState() != PLAY_STATE_READY;
	}
	
//	class SpeedThread extends Thread{
//		boolean fastword;
//		
//		public SpeedThread(boolean flag){
//			this.fastword=flag;
//		}
//		
//		@Override
//		public void run() {
//			if(play_state==PLAY_STATE_PAUSE){
//				playIf.resume();
//				callback.onResumeMedia();
//			}
//			if (fastword) {
//				fastForward();
//			} else {
//				backForward();
//			}
//			programN = UriUtils.setVideoSourceRate(programN,
//					Math.abs(speed));
//			callback.onNeedPlayMedia(freqN,  programN, 0x5| getScaleFlag()
//					| getSoundTrackFlag());
//			setPlayState(PLAY_STATE_SPEED);
//		}
//	}
}
