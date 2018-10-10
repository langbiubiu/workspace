package com.ipanel.join.cq.vod.player.impl;

/** 应用呈现界面所使用接口 **/
public interface PlayCallback {
	public static final int onPlayErrorId = 81;
	public static final int onPlayError = 82;
	public static final int onPlayTime = 83;
	public static final int onPlayStart = 84;
	public static final int onPlayEnd = 85;
	public static final int onPlayMsgId = 86;
	public static final int onPlayMsg = 87;
	public static final int onVodDuration = 88;
	public static final int onSeeBackPeriod = 89;
	public static final int onShiftStartTime = 90;
	public static final int onSourceSeek = 91;
	public static final int onSourceRate = 92;
	public static final int onSyncMediaTime = 93;
	public static final int onSourceStart = 94;

	void onServiceReady();

	void onPlayErrorId(int string_id);

	void onPlayError(String msg);

	void onPlayTime(long time);

	void onVodDuration(long d);

	void onSeeBackPeriod(long s, long e);

	void onShiftStartTime(long t);

	void onSourceStart(boolean b);

	void onPlayStart(boolean b);

	void onSourceRate(float r);

	void onSourceSeek(long t);

	void onPlayEnd();

	void onPlayMsgId(int string_id);

	void onPlayMsg(String msg);

	void onSyncMediaTime(long t);
}