package com.ipanel.join.chongqing.live.manager;

import ipaneltv.toolkit.db.DatabaseObjectification.Program;
import android.media.TeeveePlayer;

import com.ipanel.join.chongqing.live.manager.DataManager.ShiftProgram;
import com.ipanel.join.chongqing.live.navi.DatabaseObjects.LiveChannel;

/**
 * 视频播放管理类
 * */
public abstract class StationManager{
	public static final int [] SOUND_FLAGS={0,TeeveePlayer.FLAG_AUDIO_LEFT_TO_MONO,TeeveePlayer.FLAG_AUDIO_RIGHT_TO_MONO};
	public static final int [] SCALE_FLAGS={TeeveePlayer.FLAG_VIDEO_TRANSFORM_RATIO_ADAPTION,TeeveePlayer.FLAG_VIDEO_TRANSFORM_NOCLIP_FILL};
	/**
	 * 播放直播中
	 * */
	public static final int PLAY_STATE_LIVE = -1;
	/**
	 * 播放数据请求中
	 * */
	public static final int PLAY_STATE_REQUEST = 0;
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
	
	

	/** 一般类型的时移类型 */
	public static final int SHIFT_TYPE_WATCH_TAIL = 0;
	/** 从头看的时移方式 */
	public static final int SHIFT_TYPE_WATCH_HEAD = 1;
	
	/**
	 * 设置当前播放频道
	 * */
	public abstract void setPlayChannel(LiveChannel channel);
	/**
	 * 设置当前循环
	 * */
	public abstract void setPlayCircle(int circle);
	/**
	 * 获得当前循环
	 * */
	public abstract int getPlayCircle();
	/**
	 * 获得当前的播放频道
	 * */
	public abstract LiveChannel getPlayChannel();
	/**
	 * 获得上一次的播放频道
	 * */
	public abstract LiveChannel getLastCannel();
	/**
	 * 获得当前的时移节目
	 * */
	public abstract ShiftProgram getPlayingTSProgram();
	/**
	 * 获得当前的播放时间
	 * */
	public abstract long getPlayTime();
	/**
	 * 获得当前的拖拽时间，若不是拖拽状态，则返回-1
	 * */
	public abstract long getDragTime();
	/**
	 * 是否处于时移模式
	 * */
	public abstract boolean isShiftMode();
	/**
	 * 是否处于时移模式
	 * */
	public abstract boolean isShifStart();
	/**
	 * 切换频道
	 * */
	public abstract void switchTVChannel(LiveChannel channel);
	
	public abstract void goTimeShift(LiveChannel channel, long startTime, long endTime, long offTime);
	/**
	 *初始化视频区域
	 * */
	public abstract void initVideoArea();
	
	public abstract void selectInvalidChannel();
	/**
	 * 清楚播放状态
	 * */
	public abstract void clearPlayData();
	/**
	 * 开始时移播放
	 * */
	public abstract void startShiftPlay(long time,int style,boolean ip,boolean pause, long start, long end);
	/**
	 * 开始直播播放
	 * */
	public abstract void startLivePlay();
	/**
	 * 播放或者暂停视频
	 * */
	public abstract void playOrPauseMedia();
	/**
	 * 开始拖扯播放
	 * */
	public abstract void startDragMedia(boolean forward);
	/**
	 * 停止拖扯播放
	 * */
	public abstract void stopDragMedia();
	/**
	 * 更改播放速度
	 * */
	public abstract void changeMediaSpeed(boolean forward);
	/**
	 * Seek到指定时间点
	 * */
	public abstract void seekMedia(long time);
	/**
	 * 处理网络变化
	 * */
	public abstract void handleNetChange(boolean valid);
	/**
	 * 是否准备好
	 * */
	public abstract boolean isReadbyOK();
	/**
	 * 是否是从头放
	 * */
	public abstract boolean isHeadShiftMode();
	/**
	 * 获取从头看的节目
	 * */
	public abstract Program getHeadShiftProgram();
	
	/**
	 * 获取最大的播放时间
	 * */
	public abstract long getMaxShiftTime() ;

	/**
	 * 获取最小的播放时间
	 * */
	public abstract long getMinShiftTime();
	/**
	 * 是否正在播放
	 * */
	public abstract boolean isPlayingMedia();
	/**
	 * 是否是IP播放
	 * */
	public abstract boolean isIPResourceShift();
	
	public abstract void setMediaRegion(int x, int y, int w, int h);
	
	public abstract void setMediaVolume(float value);
	public static interface CallBack{
		/**
		 * 切台完成的回调
		 * */
		public void onChannelAfterChange(LiveChannel channel);
		/**
		 * 切台完成的回调
		 * */
		public void onChannelBeforeChange(LiveChannel channel);
		/**
		 * 播放状态发生变化
		 * */
		public void onPlayStateChanged(int state);
		
		public void onShiftError(String msg);
		
		public void onShiftTimeUp();
		
		public void onShiftReady();
		
		public void onShiftTick();
		
		public void onShiftPrepareStart(LiveChannel channel);
		/**
		 * 播放到最大时间点
		 * */
		public void onPlayMaxDot();
		/**
		 * 播放到最小时间点
		 * */
		public void onPlayMinDot();
	}
}
