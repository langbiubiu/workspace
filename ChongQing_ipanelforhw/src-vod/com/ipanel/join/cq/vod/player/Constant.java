package com.ipanel.join.cq.vod.player;


public class Constant {
	
	/**
	 * 屏幕密度
	 * */
	public static float density=1.0f;
	public static int changeScaleByDensity(int value){
		return (int) (value*density);
	}
	
	//-------------------------广告地址----------------------------

	/**
	 * 广告地址
	 * */
	public static final String NO_AD_URI = "no";
	public final static String VOLUME_AD_IMAGE_URL="content://com.ipanel.join.shanxi.admanager.AdInfoProvider/ad/?adId=28";
	public final static String GUAJIAO_AD_IMAGE_URL="content://com.ipanel.join.shanxi.admanager.AdInfoProvider/ad/?adId=24";
	public final static String BROADCAST_AD_IMAGE_URL="content://com.ipanel.join.shanxi.admanager.AdInfoProvider/ad/?adId=22";
	public final static String INFO_AD_IMAGE_URL="content://com.ipanel.join.shanxi.admanager.AdInfoProvider/ad/?adId=21";
	
	public final static int DATA_CHANGE_PREPARE_SUCCESS = 1003;
	public final static int DATA_CHANGE_TIME_TICK = 1004;
	
	//倍速播放风格
	public final static int SHIFT_PLAY_SPEED_STYLE=0;
	//拖扯播放风格
	public final static int SHIFT_PLAY_DRAG_STYLE=1;
	
	public static boolean isDragShiftStyle(){
		return true;
	}
}
