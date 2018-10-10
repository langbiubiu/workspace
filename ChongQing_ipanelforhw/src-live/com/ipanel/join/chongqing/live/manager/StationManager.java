package com.ipanel.join.chongqing.live.manager;

import ipaneltv.toolkit.db.DatabaseObjectification.Program;
import android.media.TeeveePlayer;

import com.ipanel.join.chongqing.live.manager.DataManager.ShiftProgram;
import com.ipanel.join.chongqing.live.navi.DatabaseObjects.LiveChannel;

/**
 * ��Ƶ���Ź�����
 * */
public abstract class StationManager{
	public static final int [] SOUND_FLAGS={0,TeeveePlayer.FLAG_AUDIO_LEFT_TO_MONO,TeeveePlayer.FLAG_AUDIO_RIGHT_TO_MONO};
	public static final int [] SCALE_FLAGS={TeeveePlayer.FLAG_VIDEO_TRANSFORM_RATIO_ADAPTION,TeeveePlayer.FLAG_VIDEO_TRANSFORM_NOCLIP_FILL};
	/**
	 * ����ֱ����
	 * */
	public static final int PLAY_STATE_LIVE = -1;
	/**
	 * ��������������
	 * */
	public static final int PLAY_STATE_REQUEST = 0;
	/**
	 * ����׼����
	 * */
	public static final int PLAY_STATE_READY = 1;
	/**
	 * ���Ž�����
	 * */
	public static final int PLAY_STATE_PLAYING = 2;
	/**
	 * ������ͣ��
	 * */
	public static final int PLAY_STATE_PAUSE = 3;
	/**
	 * ���ſ�����
	 * */
	public static final int PLAY_STATE_TRANSLATE_LEFT = 5;
	/**
	 * ���ſ����
	 * */
	public static final int PLAY_STATE_TRANSLATE_RIGHT = 6;
	/**
	 * ���Ŵ�����
	 * */
	public static final int PLAY_STATE_ERROR = 7;
	/**
	 * ���Źر���
	 * */
	public static final int PLAY_STATE_DESTROY = 8;
	/**
	 * �����ϳ���
	 * */
	public static final int PLAY_STATE_DRAG = 9;
	/**
	 * ���ű�����
	 * */
	public static final int PLAY_STATE_SPEED = 10;
	
	

	/** һ�����͵�ʱ������ */
	public static final int SHIFT_TYPE_WATCH_TAIL = 0;
	/** ��ͷ����ʱ�Ʒ�ʽ */
	public static final int SHIFT_TYPE_WATCH_HEAD = 1;
	
	/**
	 * ���õ�ǰ����Ƶ��
	 * */
	public abstract void setPlayChannel(LiveChannel channel);
	/**
	 * ���õ�ǰѭ��
	 * */
	public abstract void setPlayCircle(int circle);
	/**
	 * ��õ�ǰѭ��
	 * */
	public abstract int getPlayCircle();
	/**
	 * ��õ�ǰ�Ĳ���Ƶ��
	 * */
	public abstract LiveChannel getPlayChannel();
	/**
	 * �����һ�εĲ���Ƶ��
	 * */
	public abstract LiveChannel getLastCannel();
	/**
	 * ��õ�ǰ��ʱ�ƽ�Ŀ
	 * */
	public abstract ShiftProgram getPlayingTSProgram();
	/**
	 * ��õ�ǰ�Ĳ���ʱ��
	 * */
	public abstract long getPlayTime();
	/**
	 * ��õ�ǰ����קʱ�䣬��������ק״̬���򷵻�-1
	 * */
	public abstract long getDragTime();
	/**
	 * �Ƿ���ʱ��ģʽ
	 * */
	public abstract boolean isShiftMode();
	/**
	 * �Ƿ���ʱ��ģʽ
	 * */
	public abstract boolean isShifStart();
	/**
	 * �л�Ƶ��
	 * */
	public abstract void switchTVChannel(LiveChannel channel);
	
	public abstract void goTimeShift(LiveChannel channel, long startTime, long endTime, long offTime);
	/**
	 *��ʼ����Ƶ����
	 * */
	public abstract void initVideoArea();
	
	public abstract void selectInvalidChannel();
	/**
	 * �������״̬
	 * */
	public abstract void clearPlayData();
	/**
	 * ��ʼʱ�Ʋ���
	 * */
	public abstract void startShiftPlay(long time,int style,boolean ip,boolean pause, long start, long end);
	/**
	 * ��ʼֱ������
	 * */
	public abstract void startLivePlay();
	/**
	 * ���Ż�����ͣ��Ƶ
	 * */
	public abstract void playOrPauseMedia();
	/**
	 * ��ʼ�ϳ�����
	 * */
	public abstract void startDragMedia(boolean forward);
	/**
	 * ֹͣ�ϳ�����
	 * */
	public abstract void stopDragMedia();
	/**
	 * ���Ĳ����ٶ�
	 * */
	public abstract void changeMediaSpeed(boolean forward);
	/**
	 * Seek��ָ��ʱ���
	 * */
	public abstract void seekMedia(long time);
	/**
	 * ��������仯
	 * */
	public abstract void handleNetChange(boolean valid);
	/**
	 * �Ƿ�׼����
	 * */
	public abstract boolean isReadbyOK();
	/**
	 * �Ƿ��Ǵ�ͷ��
	 * */
	public abstract boolean isHeadShiftMode();
	/**
	 * ��ȡ��ͷ���Ľ�Ŀ
	 * */
	public abstract Program getHeadShiftProgram();
	
	/**
	 * ��ȡ���Ĳ���ʱ��
	 * */
	public abstract long getMaxShiftTime() ;

	/**
	 * ��ȡ��С�Ĳ���ʱ��
	 * */
	public abstract long getMinShiftTime();
	/**
	 * �Ƿ����ڲ���
	 * */
	public abstract boolean isPlayingMedia();
	/**
	 * �Ƿ���IP����
	 * */
	public abstract boolean isIPResourceShift();
	
	public abstract void setMediaRegion(int x, int y, int w, int h);
	
	public abstract void setMediaVolume(float value);
	public static interface CallBack{
		/**
		 * ��̨��ɵĻص�
		 * */
		public void onChannelAfterChange(LiveChannel channel);
		/**
		 * ��̨��ɵĻص�
		 * */
		public void onChannelBeforeChange(LiveChannel channel);
		/**
		 * ����״̬�����仯
		 * */
		public void onPlayStateChanged(int state);
		
		public void onShiftError(String msg);
		
		public void onShiftTimeUp();
		
		public void onShiftReady();
		
		public void onShiftTick();
		
		public void onShiftPrepareStart(LiveChannel channel);
		/**
		 * ���ŵ����ʱ���
		 * */
		public void onPlayMaxDot();
		/**
		 * ���ŵ���Сʱ���
		 * */
		public void onPlayMinDot();
	}
}
