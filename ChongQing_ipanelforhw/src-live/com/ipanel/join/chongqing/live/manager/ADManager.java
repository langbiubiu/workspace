package com.ipanel.join.chongqing.live.manager;

import android.view.View;

/**
 * 广告管理类
 * */
public abstract class ADManager{
	public static final int AD_FOR_NONE=-1;
	public static final int AD_FOR_LIVE_INFO=0;
	public static final int AD_FOR_LIVE_RECOMEND=1;
	public static final int AD_FOR_LIVE_EPG=2;
	public static final int AD_FOR_SHIFT_LOADING=3;
	public static final int AD_FOR_SHIFT_INFO=4;
	public static final int AD_FOR_SHIFT_QUIT=5;
	public static final int AD_FOR_SHIFT_PAUSE=6;
	public static final int AD_FOR_SHIFT_ERROR=7;
	public static final int AD_FOR_SHIFT_EPG=8;
	public static final int AD_FOR_VOLOME=9;
	public static final int AD_FOR_SEEK=10;
	public static final int AD_FOR_BROADCAST=11;

	public static final int AD_FOR_LIVE_CHANNEL_LIST=12;
	public static final int AD_FOR_LIVE_TXT=13;
	public static final int AD_FOR_LIVE_CHANNEL_NUMBER=14;

	/**
	 * 刷新type对应的广告位
	 * */
	public abstract void onShowAD(int type,View view);
}
