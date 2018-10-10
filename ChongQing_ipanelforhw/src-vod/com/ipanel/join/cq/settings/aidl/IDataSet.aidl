package com.ipanel.join.cq.settings.aidl;
interface IDataSet {
  	
  	/**
	 * ����ָ��Ƶ��������
	 * @param vol ����ֵ 0~1
	 * @param channel ��Ӧ��Ƶ��
	 */
  	void setChannelVolume(float vol,String channel);
  	
  	/**
	 * ��ȡָ��Ƶ��������
	 * @param channel ��Ӧ��Ƶ����Ϣ
	 * @return
	 */
  	float getChannelVolume(String channel);
  	
  	/**
	 * ����ȫ������
	 * @param vol ����ֵ 0~1
	 */
  	void setGlobalVolume(float vol);
  	
  	/**
	 * ��ȡȫ������
	 * @return
	 */
  	float getGlobalVolume();
  	
  	/**
	 * ������ʾ����
	 * @return
	 */
  	void setDisplayRatio(int ratio);
  	
	/**
	 * ��ȡ��ʾ����
	 * @return
	 */
  	int getDisplayRatio();

     
        /**
	 * ��ȡ����
	 * @return
	 */
  	int getSound_Track();
	/**
	 * ��������
	 * @return
	 */
  	void setSound_Track(int sound_track);
  	
  	 /**
	 * ��������
	 * @return
	 */
  	void setChannelVoluome(String key,boolean live);
  	
  	 /**
	 * ��ȡ����
	 * @return
	 */
  	String getChannelVoluome(String key,boolean live);
  	
  	  	 /**
	 * ��������
	 * @return
	 */
  	String changeChannelVoluome(String key,boolean add,boolean live);
  	 /**
	 * ��������
	 * @return
	 */
  	void saveChannelVolume(String key,boolean live);
  	 /**
	 * �Ƿ���ȫ������
	 * @return
	 */
  	boolean isGlobalVolumeControl();
}
