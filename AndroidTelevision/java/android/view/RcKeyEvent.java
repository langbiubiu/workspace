package android.view;

import android.net.telecast.TransportManager;
import android.util.Log;

/**
 * ң��������
 */
public class RcKeyEvent {
	/** ��ɫ��-�� , ͬandroid.view.KeyEvent.KEYCODE_PROG_RED */
	public static final int KEYCODE_RED = KeyEvent.KEYCODE_PROG_RED;
	/** ��ɫ��-�� , ͬandroid.view.KeyEvent.KEYCODE_PROG_GREEN */
	public static final int KEYCODE_GREEN = KeyEvent.KEYCODE_PROG_GREEN;
	/** ��ɫ��-�� , ͬandroid.view.KeyEvent.KEYCODE_PROG_YELLOW */
	public static final int KEYCODE_YELLOW = KeyEvent.KEYCODE_PROG_YELLOW;
	/** ��ɫ��-�� , ͬandroid.view.KeyEvent.KEYCODE_PROG_BLUE */
	public static final int KEYCODE_BLUE = KeyEvent.KEYCODE_PROG_BLUE;
	/** 3dģʽ , ͬandroid.view.KeyEvent.KEYCODE_3D_MODE */
	public static final int KEYCODE_3D = KeyEvent.KEYCODE_3D_MODE;
	/** ֹͣ , ͬandroid.view.KeyEvent.KEYCODE_MEDIA_STOP */
	public static final int KEYCODE_MEDIA_STOP = KeyEvent.KEYCODE_MEDIA_STOP;
	/** ��� , ͬandroid.view.KeyEvent.KEYCODE_MEDIA_FAST_FORWARD */
	public static final int KEYCODE_MEDIA_FAST_FORWARD = KeyEvent.KEYCODE_MEDIA_FAST_FORWARD;
	/** ��һ�� , ͬandroid.view.KeyEvent.KEYCODE_MEDIA_NEXT */
	public static final int KEYCODE_MEDIA_NEXT = KeyEvent.KEYCODE_MEDIA_NEXT;
	/** ��һ��, ͬandroid.view.KeyEvent.KEYCODE_MEDIA_PREVIOUS */
	public static final int KEYCODE_MEDIA_PREVIOUS = KeyEvent.KEYCODE_MEDIA_PREVIOUS;
	/** ��ͣ , ͬandroid.view.KeyEvent.KEYCODE_MEDIA_PAUSE */
	public static final int KEYCODE_MEDIA_PAUSE = KeyEvent.KEYCODE_MEDIA_PAUSE;
	/** ����, ͬandroid.view.KeyEvent.KEYCODE_MEDIA_PLAY */
	public static final int KEYCODE_MEDIA_PLAY = KeyEvent.KEYCODE_MEDIA_PLAY;
	/** ��ͣ/���� , ͬandroid.view.KeyEvent.KEYCODE_MEDIA_PLAY_PAUSE */
	public static final int KEYCODE_MEDIA_PLAY_PAUSE = KeyEvent.KEYCODE_MEDIA_PLAY_PAUSE;
	/** ¼�� , ͬandroid.view.KeyEvent.KEYCODE_MEDIA_RECORD */
	public static final int KEYCODE_MEDIA_RECORD = KeyEvent.KEYCODE_MEDIA_RECORD;
	/** �ع� , ͬandroid.view.KeyEvent.KEYCODE_MEDIA_REWIND */
	public static final int KEYCODE_MEDIA_REWIND = KeyEvent.KEYCODE_MEDIA_REWIND;
	/** ����+ , ͬandroid.view.KeyEvent.KEYCODE_VOLUME_UP */
	public static final int KEYCODE_VOLUME_UP = KeyEvent.KEYCODE_VOLUME_UP;
	/** ����- , ͬandroid.view.KeyEvent.KEYCODE_VOLUME_DOWN */
	public static final int KEYCODE_VOLUME_DOWN = KeyEvent.KEYCODE_VOLUME_DOWN;
	/** ����, ͬandroid.view.KeyEvent.KEYCODE_VOLUME_MUTE */
	public static final int KEYCODE_MUTE = KeyEvent.KEYCODE_VOLUME_MUTE;
	/** �˳� , ͬandroid.view.KeyEvent.KEYCODE_ESCAPE */
	public static final int KEYCODE_QUIT = KeyEvent.KEYCODE_ESCAPE;
	/** ����(����) , ͬandroid.view.KeyEvent.KEYCODE_BACK */
	public static final int KEYCODE_BACK = KeyEvent.KEYCODE_BACK;
	/** ��ҳ , ͬandroid.view.KeyEvent.KEYCODE_PAGE_UP */
	public static final int KEYCODE_PAGE_UP = KeyEvent.KEYCODE_PAGE_UP;
	/** ��ҳ , ͬandroid.view.KeyEvent.KEYCODE_PAGE_DOWN */
	public static final int KEYCODE_PAGE_DOWN = KeyEvent.KEYCODE_PAGE_DOWN;
	/** ����0 , ͬandroid.view.KeyEvent.KEYCODE_0 */
	public static final int KEYCODE_0 = KeyEvent.KEYCODE_0;
	/** ����1 , ͬandroid.view.KeyEvent.KEYCODE_1 */
	public static final int KEYCODE_1 = KeyEvent.KEYCODE_1;
	/** ����2 , ͬandroid.view.KeyEvent.KEYCODE_2 */
	public static final int KEYCODE_2 = KeyEvent.KEYCODE_2;
	/** ����3 , ͬandroid.view.KeyEvent.KEYCODE_3 */
	public static final int KEYCODE_3 = KeyEvent.KEYCODE_3;
	/** ����4 , ͬandroid.view.KeyEvent.KEYCODE_4 */
	public static final int KEYCODE_4 = KeyEvent.KEYCODE_4;
	/** ����5 , ͬandroid.view.KeyEvent.KEYCODE_5 */
	public static final int KEYCODE_5 = KeyEvent.KEYCODE_5;
	/** ����6 , ͬandroid.view.KeyEvent.KEYCODE_6 */
	public static final int KEYCODE_6 = KeyEvent.KEYCODE_6;
	/** ����7 , ͬandroid.view.KeyEvent.KEYCODE_7 */
	public static final int KEYCODE_7 = KeyEvent.KEYCODE_7;
	/** ����8 , ͬandroid.view.KeyEvent.KEYCODE_8 */
	public static final int KEYCODE_8 = KeyEvent.KEYCODE_8;
	/** ����9 , ͬandroid.view.KeyEvent.KEYCODE_9 */
	public static final int KEYCODE_9 = KeyEvent.KEYCODE_9;
	/** ���ܼ�(Fn), ͬandroid.view.KeyEvent.KEYCODE_FUNCTION */
	public static final int KEYCODE_FUNCTION = KeyEvent.KEYCODE_FUNCTION;
	/** ���ܼ�1 , ͬandroid.view.KeyEvent.KEYCODE_F1 */
	public static final int KEYCODE_F1 = KeyEvent.KEYCODE_F1;
	/** ���ܼ�2 , ͬandroid.view.KeyEvent.KEYCODE_F2 */
	public static final int KEYCODE_F2 = KeyEvent.KEYCODE_F2;
	/** ���ܼ�3 , ͬandroid.view.KeyEvent.KEYCODE_F3 */
	public static final int KEYCODE_F3 = KeyEvent.KEYCODE_F3;
	/** ���ܼ�4 , ͬandroid.view.KeyEvent.KEYCODE_F4 */
	public static final int KEYCODE_F4 = KeyEvent.KEYCODE_F4;
	/** ����-�� , ͬandroid.view.KeyEvent.KEYCODE_DPAD_LEFT */
	public static final int KEYCODE_LEFT = KeyEvent.KEYCODE_DPAD_LEFT;
	/** ����-��, ͬandroid.view.KeyEvent.KEYCODE_DPAD_UP */
	public static final int KEYCODE_UP = KeyEvent.KEYCODE_DPAD_UP;
	/** ����-��, ͬandroid.view.KeyEvent.KEYCODE_DPAD_RIGHT */
	public static final int KEYCODE_RIGHT = KeyEvent.KEYCODE_DPAD_RIGHT;
	/** ����-��, ͬandroid.view.KeyEvent.KEYCODE_DPAD_DOWN */
	public static final int KEYCODE_DOWN = KeyEvent.KEYCODE_DPAD_DOWN;
	/** ȷ��, ͬandroid.view.KeyEvent.KEYCODE_DPAD_CENTER */
	public static final int KEYCODE_OK = KeyEvent.KEYCODE_DPAD_CENTER;
	/** ɾ��, ͬandroid.view.KeyEvent.KEYCODE_DEL */
	public static final int KEYCODE_DELETE = KeyEvent.KEYCODE_DEL;
	/** ��Դ, ͬandroid.view.KeyEvent.KEYCODE_POWER */
	public static final int KEYCODE_POWER = KeyEvent.KEYCODE_POWER;
	/** �˵�, ͬandroid.view.KeyEvent.KEYCODE_MENU */
	public static final int KEYCODE_MENU = KeyEvent.KEYCODE_MENU;
	/** Home, ͬandroid.view.KeyEvent.KEYCODE_HOME */
	public static final int KEYCODE_HOME = KeyEvent.KEYCODE_HOME;
	
	/** ��ʾģʽ */
	public static final int KEYCODE_DISPLAY_MODE = 0x200;
	/** ͼ��ģʽ */
	public static final int KEYCODE_VIDEO_MODE = 0x201;
	/** ����ģʽ */
	public static final int KEYCODE_SOUND_MODE = 0x202;
	/** �ź�Դ */
	public static final int KEYCODE_SIGNAL_SOURCE = 0x203;
	/** Ƶ��+ */
	public static final int KEYCODE_CH_UP = 0x204;
	/** Ƶ��- */
	public static final int KEYCODE_CH_DOWN = 0x205;
	/** ���ֵ���(ֱ��) */
	public static final int KEYCODE_TV = 0x206;
	/** ��̨(�㲥) */
	public static final int KEYCODE_RADIO = 0x207;
	/** ����/��̨ */
	public static final int KEYCODE_TV_RADIO = 0x208;
	/** ��Ѷ */
	public static final int KEYCODE_INFO = 0x209;
	/** �㲥 */
	public static final int KEYCODE_VOD = 0x20a;
	/** ����(��Ŀָ��) */
	public static final int KEYCODE_EPG = 0x20b;
	/** Ƶ�� */
	public static final int KEYCODE_CHANNEL = 0x20c;
	/** ���� */
	public static final int KEYCODE_CLASSIFY = 0x20d;
	/** ӰԺ */
	public static final int KEYCODE_CINEMA = 0x20e;
	/** ��Ʊ(����) */
	public static final int KEYCODE_STOCK = 0x20f;
	/** ���� */
	public static final int KEYCODE_SMS = 0x210;
	/** ����(�ʼ�) */
	public static final int KEYCODE_MAIL = 0x211;
	/** ��Ϸ */
	public static final int KEYCODE_GAME = 0x212;
	/** ���� */
	public static final int KEYCODE_SOUND_TRACK = 0x213;
	/** ��Ϣ */
	public static final int KEYCODE_INFORMATION = 0x214;
	/** 3D��ʽ(����,���µ�),��ο�{@link #KEYCODE_3D} */
	public static final int KEYCODE_3D_FORMAT = 0x215;
	/** ����(��ʾ) */
	public static final int KEYCODE_OSD_DISPLAY = 0x216;
	/** ���� */
	public static final int KEYCODE_INTERLACE_MODE = 0x217;
	/** ֤ȯ */
	public static final int KEYCODE_BOND = 0x218;
	/** ʱ�� */
	public static final int KEYCODE_TIME_SHIFT = 0x219;
	/** �ؿ� */
	public static final int KEYCODE_SEE_BACK = 0x21a;
	/** ���� */
	public static final int KEYCODE_NAVIGATE = 0x21b;
	/** ѡʱ(��λ) */
	public static final int KEYCODE_TIME_SELECTE = 0x21c;
	/** ϲ�� */
	public static final int KEYCODE_FAVORITE = 0x21d;
	/** �ղ� */
	public static final int KEYCODE_COLLECT = 0x21e;
	/** ���� */
	public static final int KEYCODE_HELP = 0x21f;
	/** ���뷨 */
	public static final int KEYCODE_IME = 0x220;
	/** ����(����) */
	public static final int KEYCODE_OSD_CLEAR = 0x221;
	/** ��Ļ */
	public static final int KEYCODE_SUBTITLE = 0x222;
	/** ����(Web/�����/������) */
	public static final int KEYCODE_WEB = 0x223;
	/** �б� */
	public static final int KEYCODE_LIST = 0x224;
	/** ���� */
	public static final int KEYCODE_SETTING = 0x225;
	/** ˢ�� */
	public static final int KEYCODE_REFRESH = 0x226;
	/** ���� */
	public static final int KEYCODE_INTERACT = 0x227;
	/** ���� */
	public static final int KEYCODE_SEARCH = 0x228;
	/** Ƶ������(-/--) */
	public static final int KEYCODE_CHANNEL_INPUT = 0x229;
	/** �ֲ�(���ݹ㲥) */
	public static final int KEYCODE_OC = 0x22a;
	/** �̳� */
	public static final int KEYCODE_MALL = 0x22b;
	/** Ӧ���̵� */
	public static final int KEYCODE_APP_STORE = 0x22c;
	/** ֧�� */
	public static final int KEYCODE_PAY = 0x22d;
	/** ����ok */
	public static final int KEYCODE_KALAOK = 0x22e;
	/** �ƾ� */
	public static final int KEYCODE_FINANCE = 0x22f;
	/** ͶƱ */
	public static final int KEYCODE_VOTE = 0x230;
	/** �л� */
	public static final int KEYCODE_SWITCH = 0x231;
	/** ״̬ */
	public static final int KEYCODE_STATUS = 0x232;
	/** ��Ӣ�л� */
	public static final int KEYCODE_ZH_EN = 0x233;
	/** ��ƵԴ-�ƺ�� */
	public static final int KEYCODE_SIGNAL_SOURCE_CVBS = 0x234;
	/** ��ƵԴ-HDMI */
	public static final int KEYCODE_SIGNAL_SOURCE_HDMI = 0x235;
	/** ��ƵԴ-����(VGA) */
	public static final int KEYCODE_SIGNAL_SOURCE_VGA = 0x236;
	/** ��ƵԴ-����(��Ƶ) */
	public static final int KEYCODE_SIGNAL_SOURCE_RF = 0x237;
	/** ���� */
	public static final int KEYCODE_BREADTH = 0x238;
	/** ���� */
	public static final int KEYCODE_ASSISTANT = 0x239;
	/** ������ */
	public static final int KEYCODE_MOSAIC = 0x23a;
	/** ��Ƶ����(16:9,4:3������Ӧ) */
	public static final int KEYCODE_VIDEO_RATIO = 0x23b;
	/** Ԥ�� */
	public static final int KEYCODE_FORCAST = 0x23c;
	/** ͼ��Ч��(����,����,��͵�) */
	public static final int KEYCODE_IMAGE_EFFECT = 0x23d;
	/** ��Ŀ��(��Ŀ�б�) */
	public static final int KEYCODE_PROGRAM_LIST = 0x23e;
	/** ��(����) */
	public static final int KEYCODE_CLOUD = 0x23f;
	/** �Ӳ˵� */
	public static final int KEYCODE_SUB_MENU = 0x240;
	/** ���� */
	public static final int KEYCODE_MEDIA_FAST_BACKWARD = 0x241;
	/** ��ҳ�� */
	public static final int KEYCODE_HOME_PAGE = 0x242;
	/** Ԥ�� */
	public static final int KEYCODE_BOOKING = 0x243;
	/** ���� */
	public static final int KEYCODE_RELEVANCE = 0x244;
	/** ���ȼ� */
	public static final int KEYCODE_PRIORITY = 0x245;
	/** Ӫҵ�� */
	public static final int KEYCODE_BUSINESS_HALL = 0x246;
	/** ����Ч�� */
	public static final int KEYCODE_SOUND_EFFECT = 0x247;
	/** TV+ */
	public static final int KEYCODE_TV_ADD = 0x248;
	/** �ͷ�(����) */
	public static final int KEYCODE_CUSTOMER_SERVICE = 0x249;
	
	/**
	 * �õ���Ӧ��KeyCode ���û�ж�Ӧ��ֵ��������ԭʼ��keyCode
	 * 
	 * @param e
	 *            �¼�
	 * @return ֵ
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
