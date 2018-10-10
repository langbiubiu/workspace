package com.ipanel.join.chongqing.live.manager;

import com.ipanel.join.chongqing.live.navi.DatabaseObjects.LiveChannel;

/**
 * 数据保存管理类
 * */
public abstract class SettingManager{
	/**
	 * 获取最大的音量值
	 * */
	public abstract int getMaxVoluome();
	/**
	 * 获取当前的音量值
	 * */
	public abstract int getCurrentVoluome();
	/**
	 * 获取当前的静音值
	 * */
	public abstract boolean getMute();
	/**
	 * 调节音量
	 * */
	public abstract void changeVolume(boolean add);
	/**
	 * 调节静音
	 * */
	public abstract void changeMute(boolean mute);
	/**
	 * 获取声道的保存值
	 * */
	public abstract int getSoundTrackIndex();
	
	public abstract void changeSoundTraceIndex();
	/**
	 * 获取显示比例的保存值
	 * */
	public abstract int getVideoScaleIndex();
	
	public abstract void changeVideoScaleIndex();

	/**
	 * 保存历史频道
	 * */
	public abstract void saveHistoryChannel(LiveChannel channel);
	/**
	 * 设置指定频道的音量
	 * */
	public abstract void setChannelVolume(LiveChannel channel);
	/**
	 * 获取保存的历史频道数据
	 * */
	public abstract String [] getSaveHistoryChannel();
	
	public abstract void saveVolumeData();
	
	public abstract void restoreVolumeData();
	
	public abstract void destroyData();

	public abstract String getShiftRequestUrl() ;

	public abstract String getShiftRequestCookies() ;

	public abstract String getShiftRequestTsString() ;
	
	public static interface CallBack{
		/**
		 * 静音改变的回调
		 * */
		public void onMuteStateChange(boolean mute);
	}
}
