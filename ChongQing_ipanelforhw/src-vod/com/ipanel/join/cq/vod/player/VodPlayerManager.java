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
	 * Ĭ�ϵ�����key
	 * */
	public static final String DEFAULT_VOLUME_KEY = "default_key";
	/**
	 * ����׼����
	 * */
	public static final int PLAY_STATE_READY = 1;
	/**
	 * ���Ž�����
	 * */
	public static final int PLAY_STATE_PLAYING = 2;
	/**
	 * ������ͣ��
	 * */
	public static final int PLAY_STATE_PAUSE = 3;
	/**
	 * ���ſ�����
	 * */
	public static final int PLAY_STATE_TRANSLATE_LEFT = 5;
	/**
	 * ���ſ����
	 * */
	public static final int PLAY_STATE_TRANSLATE_RIGHT = 6;
	/**
	 * ���Ŵ�����
	 * */
	public static final int PLAY_STATE_ERROR = 7;
	/**
	 * ���Źر���
	 * */
	public static final int PLAY_STATE_DESTROY = 8;
	/**
	 * �����ϳ���
	 * */
	public static final int PLAY_STATE_DRAG = 9;
	/**
	 * ���ű�����
	 * */
	public static final int PLAY_STATE_SPEED = 10;
	/**
	 * ϵͳ�����Ͳ����������Ƿ����
	 * */
	private boolean related = true;
	/**
	 * ��ǰ���ŵ�Ƶ��
	 * */
	private String freqN = null;
	/**
	 * ��ǰ���ŵ�service
	 * */
	private String programN = null;
	/**
	 * ��ǰ�Ĳ���״̬
	 * */
	private int play_state = PLAY_STATE_READY;
	/**
	 * ��ǰ�Ĳ��ŵ�ַ
	 * */
	private String url;
	/**
	 * ������Timer
	 * */
	private Timer mTimer;
	/**
	 * ������task
	 * */
	private TimerTask mTickTask;
	/**
	 * ����ֵ
	 * */
	private int HEART_BEAT = 1000;
	/**
	 * ��ǰ���ϳ�ʱ��
	 * */
	private long drag_time = -1;
	/**
	 * ��ǰ���ϳ����ӣ������ϳ��ٶ�
	 * */
	private float drag_factor = 0;

	public float getDrag_factor() {
		return drag_factor;
	}

	public void setDrag_factor(float drag_factor) {
		this.drag_factor = drag_factor;
	}

	/**
	 * ���õķ���
	 * */
	private IDataSet mIDataSet;
	/**
	 * ���ŵķ���
	 * */
	/** ��ʷ����ʱ�� */
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
			elapsed = t/1000;//elapsed��λΪ��
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
						historyTime = 0;//��ǩ���룬��ֹ�ٴ�ѡʱ
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
						//���˻����
						Log.i(TAG, "current1:"+current+",speed"+speed);
						callback.fastReverseStart();
						seekTo(3);
					}else{
						//������յ�
						Log.i(TAG, "current2:"+current+",speed:"+speed);
						callback.onPlayEnd();
					}
				}else{
					Log.i(TAG, "current speed: "+speed);
					//����ֹͣ
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
	 * ���ò��Żص�
	 * */
	public void setPlayCallBack(PlayCallBack cb) {
		this.callback = cb;
	}

	/**
	 * ��õ�ǰ�Ĳ���״̬
	 * */
	public int getPlayState() {
		return play_state;
	}

	/**
	 * ��ȡ��ǰ�Ĳ���ʱ��
	 * */
	public long getDuration() {
		return duration;
	}

	/**
	 * ��ȡ��ǰ�Ĳ��Ž���
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
	 * ��ȡ��ǰ��ʾ�Ĳ���ʱ��
	 * */
	public String getShowDuration() {
		return TimeUtility.formatTime(getDuration());
	}

	/**
	 * ��ȡ��ǰ��ʾ�Ĳ��Ž���
	 * */
	public String getShowElapsed() {
		return TimeUtility.formatTime(getElapsed());
	}

	/**
	 * ����ȷ�ϼ����߼�����
	 * */
	public void doPressEnterKey() {
		if (!isMediaValid()) {
			Logger.d("invalid action for bad time");
			return;
		}
		if (play_state == PLAY_STATE_PAUSE||play_state==PLAY_STATE_SPEED) {
			playIf.resume();
			speed = 1;//���ŵ�ʱ�������ٶȻָ�Ϊ1
			setPlayState(PLAY_STATE_PLAYING);
		} else {
			playIf.pause();
			setPlayState(PLAY_STATE_PAUSE);
		}
	}
	/**
	 * ���ڷ��ؼ����߼�����
	 * resume��������
	 * false����ͣ����
	 * */
	public void doPressBackKey(boolean resume) {
		if (!isMediaValid()) {
			Logger.d("invalid action for bad time");
			return;
		}
		try {
			if (resume) {
				playIf.resume();
				speed = 1;//���ŵ�ʱ�������ٶȻָ�Ϊ1
				setPlayState(PLAY_STATE_PLAYING);
			} else {
				playIf.pause();
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	/**
	 * �����ϳ��е��߼�����
	 * */

	public void dragTime(boolean backforward) {
		if (!isMediaValid()) {
			Logger.d("invalid action for bad time");
			return;
		}
		// drag_factor += 0.25f;
		// drag_factor = Math.min(1, drag_factor);
		// �ϳ���ʼʱ��������ʱ�丳ֵ���ϳ�ʱ��
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
	 * �����ϳ��������߼�����
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
	 * ���ڲ���seek���߼�����
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
						callback.onPlayFailed("�����ѶϿ������Ժ�����");
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
	 * ����������
	 * */
	public void createPlayer() {
		Logger.d("createPlayer");
		setPlayState(PLAY_STATE_READY);
	}

	/**
	 * ���ٲ�����
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
	 * ׼������
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
	 * ���õ�ǰ�Ĳ���״̬
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
	 * ����service�ĳ�ʼ��
	 * */
	public void setIDataSet(IDataSet mDataSet) {
		this.mIDataSet = mDataSet;
	}

	/**
	 * ��ȡ����service
	 * */
	public IDataSet getIDataSet() {
		return mIDataSet;
	}

	/**
	 * ˢ�µ�ǰ����
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
	 * ��������
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
	 * ���ľ���
	 * */
	public void changeMute(boolean mute) {
		mAudioManager.setStreamMute(AudioManager.STREAM_MUSIC, mute);
		updateVolume();
		if (callback != null) {
			callback.onMuteStateChange(mute);
		}
	}

	/**
	 * ������ʾ����
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
	 * ������ʾ����
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
	 * ��������
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
	 * ��������
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
	 * ����3Dģʽ
	 * */
	public void change3DMode(int value) {
		callback.onNeedPlayMedia(freqN, programN, getPlayFlag());
	}

	/**
	 * ��ò��Ų���
	 * */
	public int getPlayFlag() {
		return 1 | getScaleFlag() | getSoundTrackFlag();
	}

	/**
	 * ��þ������õ�ֵ
	 * */
	public boolean getMuteValue() {
		return SysUtils.isStreamMute(mAudioManager, AudioManager.STREAM_MUSIC);
	}

	/**
	 * ���������ֵ
	 * */
	public int getVolumeValue() {
		return mAudioManager.getStreamVolume(AudioManager.STREAM_MUSIC);
	}

	/**
	 * ���������ֵ
	 * */
	public int getMaxVolumeValue() {
		return mAudioManager.getStreamMaxVolume(AudioManager.STREAM_MUSIC);
	}

	/**
	 * ����������õ�ֵ
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
	 * ��ñ������õ�ֵ
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
	 * ���3D���õ�ֵ
	 * */
	public int get3DModeValue() {
		return 0;
	}

	/**
	 * ����������õ�ֵ
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
	 * ��ñ������õ�ֵ
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
	 * ���3D���õ�ֵ
	 * */
	public int get3DModeFlag() {
		return 0;
	}

	/**
	 * �󶨲��ŷ���
	 * */
	private void bindPlayService() {
		Logger.d("bindPlayService");
//		Intent intent = new Intent();
//		intent.setAction("com.ipanel.join.huawei.vod.IControl");
//		mContext.bindService(intent, mVodPlayConnection,
//				Context.BIND_AUTO_CREATE);
	}

	/**
	 * ��������task
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
	 * ��ȡ�ϳ������е�ʱ������
	 * */
	private int getPlayFactor(boolean backforword) {
		int result = 0;
		result = (int) (drag_factor * 5 + 10);
		Log.i("VodPlayerManager", "drag_factor="+drag_factor);
		return backforword ? -result : result;
	}

	/**
	 * \ ����ʱ������
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
	 * \ �ر�ʱ������
	 * */
	private void stopTick() {
		if (mTickTask != null) {
			mTickTask.cancel();
		}
	}

	/**
	 * �������Ƿ�����Ч״̬
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
