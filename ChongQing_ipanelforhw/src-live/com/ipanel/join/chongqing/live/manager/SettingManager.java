package com.ipanel.join.chongqing.live.manager;

import com.ipanel.join.chongqing.live.navi.DatabaseObjects.LiveChannel;

/**
 * ���ݱ��������
 * */
public abstract class SettingManager{
	/**
	 * ��ȡ��������ֵ
	 * */
	public abstract int getMaxVoluome();
	/**
	 * ��ȡ��ǰ������ֵ
	 * */
	public abstract int getCurrentVoluome();
	/**
	 * ��ȡ��ǰ�ľ���ֵ
	 * */
	public abstract boolean getMute();
	/**
	 * ��������
	 * */
	public abstract void changeVolume(boolean add);
	/**
	 * ���ھ���
	 * */
	public abstract void changeMute(boolean mute);
	/**
	 * ��ȡ�����ı���ֵ
	 * */
	public abstract int getSoundTrackIndex();
	
	public abstract void changeSoundTraceIndex();
	/**
	 * ��ȡ��ʾ�����ı���ֵ
	 * */
	public abstract int getVideoScaleIndex();
	
	public abstract void changeVideoScaleIndex();

	/**
	 * ������ʷƵ��
	 * */
	public abstract void saveHistoryChannel(LiveChannel channel);
	/**
	 * ����ָ��Ƶ��������
	 * */
	public abstract void setChannelVolume(LiveChannel channel);
	/**
	 * ��ȡ�������ʷƵ������
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
		 * �����ı�Ļص�
		 * */
		public void onMuteStateChange(boolean mute);
	}
}
