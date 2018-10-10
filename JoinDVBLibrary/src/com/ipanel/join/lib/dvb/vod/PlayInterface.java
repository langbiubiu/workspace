package com.ipanel.join.lib.dvb.vod;

import android.graphics.Rect;

//---------------------------------------------
/** Ӧ�ó��ֽ�����ʹ�ýӿ� **/
public interface PlayInterface {
	public static final int play = 1;
	public static final int stop = 2;
	public static final int pause = 3;
	public static final int resume = 4;
	public static final int seek = 5;
	public static final int setRate = 6;
	public static final int setVolume = 7;
	public static final int setProgramFlag = 8;
	public static final int setDisplay = 9;
	public static final int getPlayTime = 10;

	void play(String url, int type, int streamType, int flags);

	void stop();

	void pause();

	void resume();

	void setDisplay(Rect rect);

	void seek(long time);

	void setRate(float rate);

	void setVolume(float v);

	void setProgramFlag(int flags);
	
	void getPlayTime();
}