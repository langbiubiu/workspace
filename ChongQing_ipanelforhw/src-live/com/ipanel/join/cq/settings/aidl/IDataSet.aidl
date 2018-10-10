package com.ipanel.join.cq.settings.aidl;
interface IDataSet {
  	
  	/**
	 * 设置指定频道的音量
	 * @param vol 音量值 0~1
	 * @param channel 对应的频道
	 */
  	void setChannelVolume(float vol,String channel);
  	
  	/**
	 * 获取指定频道的音量
	 * @param channel 对应的频道信息
	 * @return
	 */
  	float getChannelVolume(String channel);
  	
  	/**
	 * 设置全局音量
	 * @param vol 音量值 0~1
	 */
  	void setGlobalVolume(float vol);
  	
  	/**
	 * 获取全局音量
	 * @return
	 */
  	float getGlobalVolume();
  	
  	/**
	 * 设置显示比例
	 * @return
	 */
  	void setDisplayRatio(int ratio);
  	
	/**
	 * 获取显示比例
	 * @return
	 */
  	int getDisplayRatio();

     
        /**
	 * 获取声道
	 * @return
	 */
  	int getSound_Track();
	/**
	 * 设置声道
	 * @return
	 */
  	void setSound_Track(int sound_track);
  	
  	 /**
	 * 设置声音
	 * @return
	 */
  	void setChannelVoluome(String key,boolean live);
  	
  	 /**
	 * 获取声音
	 * @return
	 */
  	String getChannelVoluome(String key,boolean live);
  	
  	  	 /**
	 * 设置声音
	 * @return
	 */
  	String changeChannelVoluome(String key,boolean add,boolean live);
  	 /**
	 * 设置声音
	 * @return
	 */
  	void saveChannelVolume(String key,boolean live);
  	 /**
	 * 是否是全局音量
	 * @return
	 */
  	boolean isGlobalVolumeControl();
}
