package com.ipanel.join.chongqing.live;

import ipaneltv.uuids.ChongqingUUIDs;
import ipaneltv.uuids.DalianUUIDs;
import ipaneltv.uuids.PingyaoUUIDs;
import ipaneltv.uuids.ShangHaiOcnUUIDs;
import android.view.RcKeyEvent;

public class Constant {
	// ----------------------START 静态变量-------------------
	/**
	 * 统一ID
	 * */
//	public static final String UUID = DalianUUIDs.ID;
	public static final String UUID = ChongqingUUIDs.ID;

	/**
	 * dvb库是否存在
	 * */
	public static boolean DTV_LIB_EXIST = false;
	/**
	 * 屏幕密度
	 * */
	public static float DENSITY = 1.0f;
	/**
	 * 是否是一体机
	 * */
	public static boolean AIO = false;
	/**
	 * 是否是开发模式
	 * */
	public static boolean DEVELOPER_MODE = false;
	/**
	 * 项目名全称
	 * */
	public static final String PROJECT_T = "dalian";
	/**
	 * 项目名简写
	 * */
	public static final String PROJECT_S = "py";
	/**
	 * contentProvider的AUTHORITY
	 * */
	public static final String AUTHORITY = "com.ipanel.join.apps.chongqing.LiveTVProvider";

	public static boolean IP_REROURCE=false;
	
	public static boolean COUNT_DOWN_READY=false;

	public static boolean SHIFT_DATA_READY=false;

	public static boolean LOOSE_LIVE_PLAYER=false;

	// ----------------------END 静态变量-------------------
	// ----------------------START 界面风格---------------------
	/**
	 * 
	 * */
	public static final int UI_STYLE_FOR_HOMED = 0;
	/**
	 * 
	 * */
	public static final int UI_STYLE_FOR_JIANGXI = 0;
	
	public static int CURRENT_UI_STYLE = UI_STYLE_FOR_HOMED;
	// ----------------------END 界面风格--------------------

	// ----------------------START 数据请求地址---------------------

	public final static String CAPTION_URL = "http://10.1.2.9/dl/stationcaption/caption.json";

	// ----------------------END 数据请求地址---------------------

	// ----------------------START 数据更新消息---------------------

	/**
	 * 数据更新消息：PF
	 * */
	public static final int DATA_CHANGE_OF_PF = 0;
	/**
	 * 数据更新消息：EPG中的节目单
	 * */
	public static final int DATA_CHANGE_OF_EPG_EVENT = 1;
	/**
	 * 数据更新消息：CA授权消息
	 * */
	public static final int DATA_CHANGE_OF_AUTH = 2;
	/**
	 * 数据更新消息：网络
	 * */
	public static final int DATA_CHANGE_OF_NETWORK = 3;
	/**
	 * 数据更新消息：时移频道
	 * */
	public static final int DATA_CHANGE_OF_SHIFT_CHANNEL = 4;
	
	/**进入时移模式**/
	public static int INTO_SHIFT_MODE = -1;
	/**
	 * 数据更新消息：节目预约
	 * */
	public static final int DATA_CHANGE_OF_BOOK = 5;
	/**
	 * 数据更新消息：时移心跳
	 * */
	public static final int DATA_CHANGE_OF_SHIFT_TICK = 6;
	/**
	 * 数据更新消息：PF
	 * */
	public static final int DATA_CHANGE_OF_PF_WITH_KEY = 7;
	/**
	 * 数据更新消息：EPG
	 * */
	public static final int DATA_CHANGE_OF_EPG_WITH_TIME = 8;
	/**
	 * 数据更新消息：back EPG
	 * */
	public static final int DATA_CHANGE_OF_BACK_EPG_WITH_TIME = 9;
	
	/**
	 * 数据更新消息：fav
	 * */
	public static final int DATA_CHANGE_OF_FAVOURITE = 10;
	
	/**
	 * 数据更新消息：fre info
	 * */
	public static final int DATA_CHANGE_OF_FRE_INFO = 11;
	
	/**
	 * 数据更新消息：mail
	 * */
	public static final int DATA_CHANGE_OF_MAIL = 12;
	
	/**
	 * 数据更新消息：mail content
	 * */
	public static final int DATA_CHANGE_OF_MAIL_CONTENT = 13;
	
	/**
	 * 数据更新消息：homed频道分组
	 */
	public static final int DATA_CHANGE_OF_CHANNEL_GROUP = 14;
	
	/**
	 * 数据更新消息：添加收藏频道
	 */
	public static final int DATA_CHANGE_OF_SET_FAVORITE_CHANNEL = 15;
	
	/**
	 * 数据更新消息：取消收藏频道
	 */
	public static final int DATA_CHANGE_OF_CANCEL_FAVORITE_CHANNEL = 16;

	// ----------------------END 数据更新消息---------------------

	// ----------------------START 栏目ID---------------------
	public static final int BIG_CIRCLE_COLUME_ID = Integer.MAX_VALUE - 5;
	public static final int TV_COLUME_ID = Integer.MAX_VALUE - 4;
	public static final int BROADCAST_COLUME_ID = Integer.MAX_VALUE - 3;
	public static final int FAVORITE_COLUME_ID = Integer.MAX_VALUE - 2;
	public static final int SETTING_COLUME_ID = Integer.MAX_VALUE - 1;
	public static final int QUIT_COLUME_ID = Integer.MAX_VALUE;
	public final static int SETTING_ID_SEARCH = -101;
	public final static int SETTING_ID_CA_SET = -102;
	public final static int SETTING_ID_CA_INFO = -103;
	// ----------------------END 栏目ID---------------------

	// ----------------------START 按键值---------------------
	public final static int KEY_VALUE_OF_SEEK = 1180;
	public final static int KEY_VALUE_OF_SEEK_F3 = 133;
	public final static int KEY_VALUE_OF_SOUND_TRACK = RcKeyEvent.KEYCODE_SOUND_TRACK;
	public final static int KEY_VALUE_OF_PAGE_UP = 92;
	public final static int KEY_VALUE_OF_PAGE_DOWN = 93;
	public final static int KEY_VALUE_OF_BACKWORD_PLAY = 89;
	public final static int KEY_VALUE_OF_FORWARD_PLAY = 125;
	// ----------------------END 按键值---------------------

	// ----------------------START 进入直播的一些参数---------------------

	public static final String LIVE_LAUNCH_TAG = "live_tag";
	public static final int ACTIVITY_LAUNCH_TYPE_DEAULT = -2;
	public static final int ACTIVITY_LAUNCH_TYPE_MEMORY_CHANNEL = -1;
	public static final int ACTIVITY_LAUNCH_TYPE_CHANNEL_ID = 0;
	public static final int ACTIVITY_LAUNCH_TYPE_FREQUENCE_PROGRAM = 1;
	public static final int ACTIVITY_LAUNCH_TYPE_CHANNEL_NUMBER = 2;
	public static final int ACTIVITY_LAUNCH_TYPE_CHANNEL_NAME = 3;
	public static final int ACTIVITY_LAUNCH_TYPE_LIVE_KEY = 4;
	public static final int ACTIVITY_LAUNCH_TYPE_LIVE_FAVORITE = 5;
	public static final int ACTIVITY_LAUNCH_TYPE_LIVE_PUSH = 6;
	public static final int ACTIVITY_LAUNCH_TYPE_TIMESHIFT_PUSH = 7;
	public static final int ACTIVITY_LAUNCH_TYPE_MAIl = 8;
	
	
	public static final String LIVE_LAUNCH_CIRCLE_TAG = "live_circle_tag";
	public static final String LIVE_LAUNCH_FREQUENCE_TAG = "live_frequence_tag";
	public static final String LIVE_LAUNCH_PROGRAM_TAG = "live_program_tag";
	public static final String LIVE_LAUNCH_CHANNEL_ID_TAG = "live_channel_id_tag";
	public static final String LIVE_LAUNCH_CHANNLE_NUMBER_TAG = "live_channel_number_tag";
	public static final String LIVE_LAUNCH_CHANNLE_NAME_TAG = "live_channel_name_tag";
	public static final String LIVE_LAUNCH_CHANNEL_START_TIME_TAG = "live_channel_startime_tag";
	public static final String LIVE_LAUNCH_CHANNEL_END_TIME_TAG = "live_channel_endtime_tag";
	public static final String LIVE_LAUNCH_CHANNEL_OFF_TIME_TAG = "live_channel_offtime_tag";

	// ----------------------END 进入直播的一些参数---------------------
	// ----------------------START 广播、应用名---------------------
	/**
	 * 设置服务的action
	 * */
	public static final String ACTION_SETTING_SERVICE = "com.ipanel.join.cq.settings.IDataSetService";
	/**
	 * Auth服务的action
	 * */
	public static final String AUTH_SERVICE_NAME = "com.ipanel.join.vodauth.dl.IAuthService";
	/**
	 * 搜索应用的包名
	 * */
	public static final String NETWORK_PACKAGE_NAME = "com.ipanel.join.network."
			+ Constant.PROJECT_S;
	/**
	 * 搜索应用的类名
	 * */
	public static final String NETWORK_ACTIVITY_NAME = "cn."
			+ Constant.PROJECT_T + ".tvapps.network.SearchActivity";

	/**
	 * 播放服务
	 * */
	public static String PLAY_SERVICE_NAME = "cn.ipanel.tvapps.network.NcPlayService";;
	public static String SRC_SERVICE_NAME = "com.ipanel.apps.common.tsvodsrcservice";

	public static final String TM_SERVICE_NAME = "com.ipanel.apps.jx.tm.TmSessionService";

	public static final String IP_TV_EVENT_UPDATE_BROADCAST_NAME = "cn.tvapps.network.IP_TV_EVENT_UPDATE";
	public static final String IP_TV_VERSION_UPDATE_BROADCAST_NAME = "cn.tvapps.network.IP_TV_VERSION_UPDATE";
	public static final String DVB_TV_VERSION_UPDATE_BROADCAST_NAME = "cn.tvapps.network.DVB_TV_VERSION_UPDATE";
	public static final String ALARM_ALERT_ACTION = "com.wasu.join.live.ALARM_ALERT";
	public static final String ALARM_INTENT_EXTRA = "intent.extra.alarm";
	public static final String ALARM_RMIND_REFRESH = "com.wasu.join.live.REFRESH_EPG";
	public static final String DP_IN_BROADCAST = "com.ipanel.join.service.StbSendToAPPBroadcastPull";
	public static final String DP_OUT_BROADCAST = "com.ipanel.join.service.APPSendToSTBBroadcast";
	// ----------------------END 广播、应用名---------------------

	// ----------------------START 时间常量--------------------
	/**
	 * 提示消息显示时间
	 * */
	public final static int DEFAULT_TOAST_SHOW_TIME = 8000;
	// ----------------------END 时间常量--------------------

}
