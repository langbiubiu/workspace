package com.ipanel.join.cq.vod.player;


public class Constant {
	
	/**
	 * ��Ļ�ܶ�
	 * */
	public static float density=1.0f;
	public static int changeScaleByDensity(int value){
		return (int) (value*density);
	}
	
	//-------------------------����ַ----------------------------

	/**
	 * ����ַ
	 * */
	public static final String NO_AD_URI = "no";
	public final static String VOLUME_AD_IMAGE_URL="content://com.ipanel.join.shanxi.admanager.AdInfoProvider/ad/?adId=28";
	public final static String GUAJIAO_AD_IMAGE_URL="content://com.ipanel.join.shanxi.admanager.AdInfoProvider/ad/?adId=24";
	public final static String BROADCAST_AD_IMAGE_URL="content://com.ipanel.join.shanxi.admanager.AdInfoProvider/ad/?adId=22";
	public final static String INFO_AD_IMAGE_URL="content://com.ipanel.join.shanxi.admanager.AdInfoProvider/ad/?adId=21";
	
	public final static int DATA_CHANGE_PREPARE_SUCCESS = 1003;
	public final static int DATA_CHANGE_TIME_TICK = 1004;
	
	//���ٲ��ŷ��
	public final static int SHIFT_PLAY_SPEED_STYLE=0;
	//�ϳ����ŷ��
	public final static int SHIFT_PLAY_DRAG_STYLE=1;
	
	public static boolean isDragShiftStyle(){
		return true;
	}
}
