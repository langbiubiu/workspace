package com.ipanel.join.chongqing.live.manager;

import android.view.KeyEvent;

import com.ipanel.join.chongqing.live.base.UIFragment;
import com.ipanel.join.chongqing.live.manager.impl.BaseUIManagerImpl.UIConfig;

/**
 * ���������
 * */
public abstract class UIManager{
	
	/**�������ڽ���*/
	public static final int ID_UI_VOLUME = 11;
	/**����״̬����*/
	public static final int ID_UI_MUTE = 12;
	/**Ƶ����Ϣ����1*/
	public static final int ID_UI_LIVE_INFO = 13;
	/**Ƶ����Ϣ����2*/
	public static final int ID_UI_LIVE_CHANNEL_INFO = 31;
	/**Ƶ���Ž���*/
	public static final int ID_UI_LIVE_NUMBER = 14;
	/**�˳�ʱ�ƽ���*/
	public static final int ID_UI_SHIFT_QUIT = 15;
	/**��������*/
	public static final int ID_UI_SOUND_TRACK=16;
	/**�Ƽ�����*/
	public static final int ID_UI_RECOMEND=17;
	/**EPG����*/
	public static final int ID_UI_LIVE_EPG=18;
	/**Ƶ���������*/
	public static final int ID_UI_LIVE_CHANNEL_GROUP=32;
	
	public static final int ID_UI_FAVOROT=33;
	/**�˵���ʾ����*/
	public static final int ID_UI_TOOL_SHIP=19;
	/**ʱ�Ƽ��ؽ���*/
	public static final int ID_UI_SHIFT_LOADING=20;
	/**ʱ����Ϣ����*/
	public static final int ID_UI_SHIFT_INFO=21;
	/**ʱ����ͣ����*/
	public static final int ID_UI_SHIFT_PAUSE=22;
	/**ʱ��ѡʱ����*/
	public static final int ID_UI_SEEK=23;
	/**ʱ��ѡʱ����*/
	public static final int ID_UI_SHIFT_PAUSE_NOAD=24;
	/**ʱ��ѡʱ����*/
	public static final int ID_UI_SHIFT_ERROR=25;
	/**ʱ�Ʊ�ʶ����*/
	public static final int ID_UI_WATCH_STATE=26;
	/**ʱ��ѡʱ����*/
	public static final int ID_UI_CAPTION=27;
	/**ʱ��ѡʱ����*/
	public static final int ID_UI_MAIL=100;
	/**ʱ��ѡʱ����*/
	public static final int ID_UI_OSD=28;
	/**ʱ��ѡʱ����*/
	public static final int ID_UI_CROSS=29;
	/**ʱ��ѡʱ����*/
	public static final int ID_UI_PF=30;
	/**back EPG����*/
	public static final int ID_UI_LIVE_BACK_EPG=40;
	/**book list����*/
	public static final int ID_UI_LIVE_BOOK_LIST=41;
	/** Ƶ����Ϣ�鿴*/
	public static final int ID_UI_LIVE_FRE_INFO=42;
	
	/** 
	 * ������UI 
	 * 
	 * */
	/** Ƶ��+��Ŀ�б����*/
	public static final int ID_UI_CQ_LIVE_CHANNEL_LIST = 50;
	/** ��Ŀ+�����б����*/
	public static final int ID_UI_CQ_LIVE_EVENT_LIST = 51;
	/** ��Ŀ�����б����*/
	public static final int ID_UI_CQ_LIVE_CHANNEL_GROUP = 52;
	/** ֱ��PFbar����*/
	public static final int ID_UI_CQ_LIVE_PF = 53;
	/** ʱ����Ϣ����*/
	public static final int ID_UI_CQ_SHIFT_INFO = 54;
	/** ʱ��ѡʱ����*/
	public static final int ID_UI_CQ_SHIFT_SEEK = 55;
	/** �˳���������*/
	public static final int ID_UI_CQ_LIVE_QUIT = 56;
	/** ѡ�����������*/
	public static final int ID_UI_CQ_SERIES = 57;
	/** TV+����*/
	public static final int ID_UI_CQ_TV_ADD = 58;
	
	/**
	 * Ĭ�ϵĽ�����ʾʱ��
	 * */
	public final static int DEFAULT_DURATION_OF_SHOW_UI = 5 * 1000;
	
	/**
	 * ���泣�Ե� ��־
	 * */
	public final static int FOREVER_DURATION_OF_SHOW_UI = -1;
	
	/** ����id����������� */
	public abstract UIConfig getConfigUI(int id);

	/** ����id��ʾ���� */
	public abstract void showUI(int id, Object o);

	/** ����id���ؽ��� 
	 * @param  */
	public abstract void hideUI(int id);

	/** ���ؽ��� */
	public abstract boolean detatchExclusiveFagment();

	/** ���ý��������Timer */
	public abstract void resetHideTimer();

	/** ����İ����ɷ� */
	public abstract boolean handleKeyEvent(int keyCode, KeyEvent event);

	/** activity onPauseʱ�����ǰ�Ľ��� */
	public abstract void clearCurrentFragment();

	/** ���ؽ��� */
	public abstract void hideFragment(UIFragment f);

	/** ��õ�ǰ���� */
	public abstract UIFragment getCurrentFragment();

	/*** �ɷ����ݱ仯 */
	public abstract void dispatchDataChange(final int type, final Object data);

	/** * �ж�ĳ�������Ƿ�������ʾ */
	public abstract boolean isFragmentAdded(int id);
}
