package android.view;

import android.net.telecast.TransportManager;
import android.util.Log;

/**
 * 遥控器按键
 */
public class RcKeyEvent {
	/** 颜色键-红 , 同android.view.KeyEvent.KEYCODE_PROG_RED */
	public static final int KEYCODE_RED = KeyEvent.KEYCODE_PROG_RED;
	/** 颜色键-绿 , 同android.view.KeyEvent.KEYCODE_PROG_GREEN */
	public static final int KEYCODE_GREEN = KeyEvent.KEYCODE_PROG_GREEN;
	/** 颜色键-黄 , 同android.view.KeyEvent.KEYCODE_PROG_YELLOW */
	public static final int KEYCODE_YELLOW = KeyEvent.KEYCODE_PROG_YELLOW;
	/** 颜色键-蓝 , 同android.view.KeyEvent.KEYCODE_PROG_BLUE */
	public static final int KEYCODE_BLUE = KeyEvent.KEYCODE_PROG_BLUE;
	/** 3d模式 , 同android.view.KeyEvent.KEYCODE_3D_MODE */
	public static final int KEYCODE_3D = KeyEvent.KEYCODE_3D_MODE;
	/** 停止 , 同android.view.KeyEvent.KEYCODE_MEDIA_STOP */
	public static final int KEYCODE_MEDIA_STOP = KeyEvent.KEYCODE_MEDIA_STOP;
	/** 快进 , 同android.view.KeyEvent.KEYCODE_MEDIA_FAST_FORWARD */
	public static final int KEYCODE_MEDIA_FAST_FORWARD = KeyEvent.KEYCODE_MEDIA_FAST_FORWARD;
	/** 下一首 , 同android.view.KeyEvent.KEYCODE_MEDIA_NEXT */
	public static final int KEYCODE_MEDIA_NEXT = KeyEvent.KEYCODE_MEDIA_NEXT;
	/** 上一首, 同android.view.KeyEvent.KEYCODE_MEDIA_PREVIOUS */
	public static final int KEYCODE_MEDIA_PREVIOUS = KeyEvent.KEYCODE_MEDIA_PREVIOUS;
	/** 暂停 , 同android.view.KeyEvent.KEYCODE_MEDIA_PAUSE */
	public static final int KEYCODE_MEDIA_PAUSE = KeyEvent.KEYCODE_MEDIA_PAUSE;
	/** 播放, 同android.view.KeyEvent.KEYCODE_MEDIA_PLAY */
	public static final int KEYCODE_MEDIA_PLAY = KeyEvent.KEYCODE_MEDIA_PLAY;
	/** 暂停/播放 , 同android.view.KeyEvent.KEYCODE_MEDIA_PLAY_PAUSE */
	public static final int KEYCODE_MEDIA_PLAY_PAUSE = KeyEvent.KEYCODE_MEDIA_PLAY_PAUSE;
	/** 录制 , 同android.view.KeyEvent.KEYCODE_MEDIA_RECORD */
	public static final int KEYCODE_MEDIA_RECORD = KeyEvent.KEYCODE_MEDIA_RECORD;
	/** 回滚 , 同android.view.KeyEvent.KEYCODE_MEDIA_REWIND */
	public static final int KEYCODE_MEDIA_REWIND = KeyEvent.KEYCODE_MEDIA_REWIND;
	/** 音量+ , 同android.view.KeyEvent.KEYCODE_VOLUME_UP */
	public static final int KEYCODE_VOLUME_UP = KeyEvent.KEYCODE_VOLUME_UP;
	/** 音量- , 同android.view.KeyEvent.KEYCODE_VOLUME_DOWN */
	public static final int KEYCODE_VOLUME_DOWN = KeyEvent.KEYCODE_VOLUME_DOWN;
	/** 静音, 同android.view.KeyEvent.KEYCODE_VOLUME_MUTE */
	public static final int KEYCODE_MUTE = KeyEvent.KEYCODE_VOLUME_MUTE;
	/** 退出 , 同android.view.KeyEvent.KEYCODE_ESCAPE */
	public static final int KEYCODE_QUIT = KeyEvent.KEYCODE_ESCAPE;
	/** 返回(回退) , 同android.view.KeyEvent.KEYCODE_BACK */
	public static final int KEYCODE_BACK = KeyEvent.KEYCODE_BACK;
	/** 上页 , 同android.view.KeyEvent.KEYCODE_PAGE_UP */
	public static final int KEYCODE_PAGE_UP = KeyEvent.KEYCODE_PAGE_UP;
	/** 下页 , 同android.view.KeyEvent.KEYCODE_PAGE_DOWN */
	public static final int KEYCODE_PAGE_DOWN = KeyEvent.KEYCODE_PAGE_DOWN;
	/** 数字0 , 同android.view.KeyEvent.KEYCODE_0 */
	public static final int KEYCODE_0 = KeyEvent.KEYCODE_0;
	/** 数字1 , 同android.view.KeyEvent.KEYCODE_1 */
	public static final int KEYCODE_1 = KeyEvent.KEYCODE_1;
	/** 数字2 , 同android.view.KeyEvent.KEYCODE_2 */
	public static final int KEYCODE_2 = KeyEvent.KEYCODE_2;
	/** 数字3 , 同android.view.KeyEvent.KEYCODE_3 */
	public static final int KEYCODE_3 = KeyEvent.KEYCODE_3;
	/** 数字4 , 同android.view.KeyEvent.KEYCODE_4 */
	public static final int KEYCODE_4 = KeyEvent.KEYCODE_4;
	/** 数字5 , 同android.view.KeyEvent.KEYCODE_5 */
	public static final int KEYCODE_5 = KeyEvent.KEYCODE_5;
	/** 数字6 , 同android.view.KeyEvent.KEYCODE_6 */
	public static final int KEYCODE_6 = KeyEvent.KEYCODE_6;
	/** 数字7 , 同android.view.KeyEvent.KEYCODE_7 */
	public static final int KEYCODE_7 = KeyEvent.KEYCODE_7;
	/** 数字8 , 同android.view.KeyEvent.KEYCODE_8 */
	public static final int KEYCODE_8 = KeyEvent.KEYCODE_8;
	/** 数字9 , 同android.view.KeyEvent.KEYCODE_9 */
	public static final int KEYCODE_9 = KeyEvent.KEYCODE_9;
	/** 功能键(Fn), 同android.view.KeyEvent.KEYCODE_FUNCTION */
	public static final int KEYCODE_FUNCTION = KeyEvent.KEYCODE_FUNCTION;
	/** 功能键1 , 同android.view.KeyEvent.KEYCODE_F1 */
	public static final int KEYCODE_F1 = KeyEvent.KEYCODE_F1;
	/** 功能键2 , 同android.view.KeyEvent.KEYCODE_F2 */
	public static final int KEYCODE_F2 = KeyEvent.KEYCODE_F2;
	/** 功能键3 , 同android.view.KeyEvent.KEYCODE_F3 */
	public static final int KEYCODE_F3 = KeyEvent.KEYCODE_F3;
	/** 功能键4 , 同android.view.KeyEvent.KEYCODE_F4 */
	public static final int KEYCODE_F4 = KeyEvent.KEYCODE_F4;
	/** 方向-左 , 同android.view.KeyEvent.KEYCODE_DPAD_LEFT */
	public static final int KEYCODE_LEFT = KeyEvent.KEYCODE_DPAD_LEFT;
	/** 方向-上, 同android.view.KeyEvent.KEYCODE_DPAD_UP */
	public static final int KEYCODE_UP = KeyEvent.KEYCODE_DPAD_UP;
	/** 方向-右, 同android.view.KeyEvent.KEYCODE_DPAD_RIGHT */
	public static final int KEYCODE_RIGHT = KeyEvent.KEYCODE_DPAD_RIGHT;
	/** 方向-下, 同android.view.KeyEvent.KEYCODE_DPAD_DOWN */
	public static final int KEYCODE_DOWN = KeyEvent.KEYCODE_DPAD_DOWN;
	/** 确认, 同android.view.KeyEvent.KEYCODE_DPAD_CENTER */
	public static final int KEYCODE_OK = KeyEvent.KEYCODE_DPAD_CENTER;
	/** 删除, 同android.view.KeyEvent.KEYCODE_DEL */
	public static final int KEYCODE_DELETE = KeyEvent.KEYCODE_DEL;
	/** 电源, 同android.view.KeyEvent.KEYCODE_POWER */
	public static final int KEYCODE_POWER = KeyEvent.KEYCODE_POWER;
	/** 菜单, 同android.view.KeyEvent.KEYCODE_MENU */
	public static final int KEYCODE_MENU = KeyEvent.KEYCODE_MENU;
	/** Home, 同android.view.KeyEvent.KEYCODE_HOME */
	public static final int KEYCODE_HOME = KeyEvent.KEYCODE_HOME;
	
	/** 显示模式 */
	public static final int KEYCODE_DISPLAY_MODE = 0x200;
	/** 图像模式 */
	public static final int KEYCODE_VIDEO_MODE = 0x201;
	/** 声音模式 */
	public static final int KEYCODE_SOUND_MODE = 0x202;
	/** 信号源 */
	public static final int KEYCODE_SIGNAL_SOURCE = 0x203;
	/** 频道+ */
	public static final int KEYCODE_CH_UP = 0x204;
	/** 频道- */
	public static final int KEYCODE_CH_DOWN = 0x205;
	/** 数字电视(直播) */
	public static final int KEYCODE_TV = 0x206;
	/** 电台(广播) */
	public static final int KEYCODE_RADIO = 0x207;
	/** 电视/电台 */
	public static final int KEYCODE_TV_RADIO = 0x208;
	/** 咨讯 */
	public static final int KEYCODE_INFO = 0x209;
	/** 点播 */
	public static final int KEYCODE_VOD = 0x20a;
	/** 导视(节目指南) */
	public static final int KEYCODE_EPG = 0x20b;
	/** 频道 */
	public static final int KEYCODE_CHANNEL = 0x20c;
	/** 分类 */
	public static final int KEYCODE_CLASSIFY = 0x20d;
	/** 影院 */
	public static final int KEYCODE_CINEMA = 0x20e;
	/** 股票(股情) */
	public static final int KEYCODE_STOCK = 0x20f;
	/** 短信 */
	public static final int KEYCODE_SMS = 0x210;
	/** 邮箱(邮件) */
	public static final int KEYCODE_MAIL = 0x211;
	/** 游戏 */
	public static final int KEYCODE_GAME = 0x212;
	/** 声道 */
	public static final int KEYCODE_SOUND_TRACK = 0x213;
	/** 信息 */
	public static final int KEYCODE_INFORMATION = 0x214;
	/** 3D格式(左右,上下等),另参考{@link #KEYCODE_3D} */
	public static final int KEYCODE_3D_FORMAT = 0x215;
	/** 屏显(显示) */
	public static final int KEYCODE_OSD_DISPLAY = 0x216;
	/** 交替 */
	public static final int KEYCODE_INTERLACE_MODE = 0x217;
	/** 证券 */
	public static final int KEYCODE_BOND = 0x218;
	/** 时移 */
	public static final int KEYCODE_TIME_SHIFT = 0x219;
	/** 回看 */
	public static final int KEYCODE_SEE_BACK = 0x21a;
	/** 导航 */
	public static final int KEYCODE_NAVIGATE = 0x21b;
	/** 选时(定位) */
	public static final int KEYCODE_TIME_SELECTE = 0x21c;
	/** 喜爱 */
	public static final int KEYCODE_FAVORITE = 0x21d;
	/** 收藏 */
	public static final int KEYCODE_COLLECT = 0x21e;
	/** 帮助 */
	public static final int KEYCODE_HELP = 0x21f;
	/** 输入法 */
	public static final int KEYCODE_IME = 0x220;
	/** 隐藏(清屏) */
	public static final int KEYCODE_OSD_CLEAR = 0x221;
	/** 字幕 */
	public static final int KEYCODE_SUBTITLE = 0x222;
	/** 上网(Web/浏览器/互联网) */
	public static final int KEYCODE_WEB = 0x223;
	/** 列表 */
	public static final int KEYCODE_LIST = 0x224;
	/** 设置 */
	public static final int KEYCODE_SETTING = 0x225;
	/** 刷新 */
	public static final int KEYCODE_REFRESH = 0x226;
	/** 互动 */
	public static final int KEYCODE_INTERACT = 0x227;
	/** 搜索 */
	public static final int KEYCODE_SEARCH = 0x228;
	/** 频道输入(-/--) */
	public static final int KEYCODE_CHANNEL_INPUT = 0x229;
	/** 轮播(数据广播) */
	public static final int KEYCODE_OC = 0x22a;
	/** 商城 */
	public static final int KEYCODE_MALL = 0x22b;
	/** 应用商店 */
	public static final int KEYCODE_APP_STORE = 0x22c;
	/** 支付 */
	public static final int KEYCODE_PAY = 0x22d;
	/** 卡拉ok */
	public static final int KEYCODE_KALAOK = 0x22e;
	/** 财经 */
	public static final int KEYCODE_FINANCE = 0x22f;
	/** 投票 */
	public static final int KEYCODE_VOTE = 0x230;
	/** 切换 */
	public static final int KEYCODE_SWITCH = 0x231;
	/** 状态 */
	public static final int KEYCODE_STATUS = 0x232;
	/** 中英切换 */
	public static final int KEYCODE_ZH_EN = 0x233;
	/** 视频源-黄红白 */
	public static final int KEYCODE_SIGNAL_SOURCE_CVBS = 0x234;
	/** 视频源-HDMI */
	public static final int KEYCODE_SIGNAL_SOURCE_HDMI = 0x235;
	/** 视频源-电脑(VGA) */
	public static final int KEYCODE_SIGNAL_SOURCE_VGA = 0x236;
	/** 视频源-电视(射频) */
	public static final int KEYCODE_SIGNAL_SOURCE_RF = 0x237;
	/** 幅宽 */
	public static final int KEYCODE_BREADTH = 0x238;
	/** 助手 */
	public static final int KEYCODE_ASSISTANT = 0x239;
	/** 马赛克 */
	public static final int KEYCODE_MOSAIC = 0x23a;
	/** 视频比例(16:9,4:3，自适应) */
	public static final int KEYCODE_VIDEO_RATIO = 0x23b;
	/** 预告 */
	public static final int KEYCODE_FORCAST = 0x23c;
	/** 图像效果(鲜艳,明亮,柔和等) */
	public static final int KEYCODE_IMAGE_EFFECT = 0x23d;
	/** 节目单(节目列表) */
	public static final int KEYCODE_PROGRAM_LIST = 0x23e;
	/** 云(分享) */
	public static final int KEYCODE_CLOUD = 0x23f;
	/** 子菜单 */
	public static final int KEYCODE_SUB_MENU = 0x240;
	/** 快退 */
	public static final int KEYCODE_MEDIA_FAST_BACKWARD = 0x241;
	/** 主页面 */
	public static final int KEYCODE_HOME_PAGE = 0x242;
	/** 预定 */
	public static final int KEYCODE_BOOKING = 0x243;
	/** 关联 */
	public static final int KEYCODE_RELEVANCE = 0x244;
	/** 优先级 */
	public static final int KEYCODE_PRIORITY = 0x245;
	/** 营业厅 */
	public static final int KEYCODE_BUSINESS_HALL = 0x246;
	/** 声音效果 */
	public static final int KEYCODE_SOUND_EFFECT = 0x247;
	/** TV+ */
	public static final int KEYCODE_TV_ADD = 0x248;
	/** 客服(服务) */
	public static final int KEYCODE_CUSTOMER_SERVICE = 0x249;
	
	/**
	 * 得到对应的KeyCode 如果没有对应键值，将返回原始的keyCode
	 * 
	 * @param e
	 *            事件
	 * @return 值
	 */
	public static int getRcKeyCode(KeyEvent e) {
		if (obj == null)
			createInstane();
		return obj.getRcKeyCode(e.getKeyCode());
	}

	/** @hide */
	protected int getRcKeyCode(int code) {
		return code;
	}

	private static RcKeyEvent obj = null;
	static final String TAG = "RcKeyEvent";

	static void createInstane() {
		synchronized (TAG) {
			if (obj != null)
				return;
			String clsname = TransportManager.getSystemProperty("android.view.RcKeyEvent");
			if (clsname != null) {
				try {
					obj = (RcKeyEvent) Class.forName(clsname).newInstance();
					return;
				} catch (Exception e) {
					Log.e(TAG, "class :" + clsname + " is not valid!");
				}
			}
			obj = new RcKeyEvent();
		}
	}
}
