package com.ipanel.join.chongqing.live.manager;

import android.view.KeyEvent;

import com.ipanel.join.chongqing.live.base.UIFragment;
import com.ipanel.join.chongqing.live.manager.impl.BaseUIManagerImpl.UIConfig;

/**
 * 界面管理类
 * */
public abstract class UIManager{
	
	/**音量调节界面*/
	public static final int ID_UI_VOLUME = 11;
	/**静音状态界面*/
	public static final int ID_UI_MUTE = 12;
	/**频道信息界面1*/
	public static final int ID_UI_LIVE_INFO = 13;
	/**频道信息界面2*/
	public static final int ID_UI_LIVE_CHANNEL_INFO = 31;
	/**频道号界面*/
	public static final int ID_UI_LIVE_NUMBER = 14;
	/**退出时移界面*/
	public static final int ID_UI_SHIFT_QUIT = 15;
	/**声道界面*/
	public static final int ID_UI_SOUND_TRACK=16;
	/**推荐界面*/
	public static final int ID_UI_RECOMEND=17;
	/**EPG界面*/
	public static final int ID_UI_LIVE_EPG=18;
	/**频道分组界面*/
	public static final int ID_UI_LIVE_CHANNEL_GROUP=32;
	
	public static final int ID_UI_FAVOROT=33;
	/**菜单提示界面*/
	public static final int ID_UI_TOOL_SHIP=19;
	/**时移加载界面*/
	public static final int ID_UI_SHIFT_LOADING=20;
	/**时移信息界面*/
	public static final int ID_UI_SHIFT_INFO=21;
	/**时移暂停界面*/
	public static final int ID_UI_SHIFT_PAUSE=22;
	/**时移选时界面*/
	public static final int ID_UI_SEEK=23;
	/**时移选时界面*/
	public static final int ID_UI_SHIFT_PAUSE_NOAD=24;
	/**时移选时界面*/
	public static final int ID_UI_SHIFT_ERROR=25;
	/**时移标识界面*/
	public static final int ID_UI_WATCH_STATE=26;
	/**时移选时界面*/
	public static final int ID_UI_CAPTION=27;
	/**时移选时界面*/
	public static final int ID_UI_MAIL=100;
	/**时移选时界面*/
	public static final int ID_UI_OSD=28;
	/**时移选时界面*/
	public static final int ID_UI_CROSS=29;
	/**时移选时界面*/
	public static final int ID_UI_PF=30;
	/**back EPG界面*/
	public static final int ID_UI_LIVE_BACK_EPG=40;
	/**book list界面*/
	public static final int ID_UI_LIVE_BOOK_LIST=41;
	/** 频点信息查看*/
	public static final int ID_UI_LIVE_FRE_INFO=42;
	
	/** 
	 * 重庆新UI 
	 * 
	 * */
	/** 频道+节目列表界面*/
	public static final int ID_UI_CQ_LIVE_CHANNEL_LIST = 50;
	/** 节目+日期列表界面*/
	public static final int ID_UI_CQ_LIVE_EVENT_LIST = 51;
	/** 节目分组列表界面*/
	public static final int ID_UI_CQ_LIVE_CHANNEL_GROUP = 52;
	/** 直播PFbar界面*/
	public static final int ID_UI_CQ_LIVE_PF = 53;
	/** 时移信息界面*/
	public static final int ID_UI_CQ_SHIFT_INFO = 54;
	/** 时移选时界面*/
	public static final int ID_UI_CQ_SHIFT_SEEK = 55;
	/** 退出挽留界面*/
	public static final int ID_UI_CQ_LIVE_QUIT = 56;
	/** 选集看详情界面*/
	public static final int ID_UI_CQ_SERIES = 57;
	/** TV+界面*/
	public static final int ID_UI_CQ_TV_ADD = 58;
	
	/**
	 * 默认的界面显示时间
	 * */
	public final static int DEFAULT_DURATION_OF_SHOW_UI = 5 * 1000;
	
	/**
	 * 界面常显的 标志
	 * */
	public final static int FOREVER_DURATION_OF_SHOW_UI = -1;
	
	/** 根据id获得配置数据 */
	public abstract UIConfig getConfigUI(int id);

	/** 根据id显示界面 */
	public abstract void showUI(int id, Object o);

	/** 根据id隐藏界面 
	 * @param  */
	public abstract void hideUI(int id);

	/** 隐藏界面 */
	public abstract boolean detatchExclusiveFagment();

	/** 重置界面的隐藏Timer */
	public abstract void resetHideTimer();

	/** 界面的按键派发 */
	public abstract boolean handleKeyEvent(int keyCode, KeyEvent event);

	/** activity onPause时清除当前的界面 */
	public abstract void clearCurrentFragment();

	/** 隐藏界面 */
	public abstract void hideFragment(UIFragment f);

	/** 获得当前界面 */
	public abstract UIFragment getCurrentFragment();

	/*** 派发数据变化 */
	public abstract void dispatchDataChange(final int type, final Object data);

	/** * 判断某个界面是否正在显示 */
	public abstract boolean isFragmentAdded(int id);
}
