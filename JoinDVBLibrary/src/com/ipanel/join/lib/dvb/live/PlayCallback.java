package com.ipanel.join.lib.dvb.live;

import ipaneltv.toolkit.db.DatabaseObjectification.Program;

/** 回调线程为主线程进行，可直接进行ui操作，不其他遵循Android原则 */
public interface PlayCallback {
	/** 上下文已就绪 */
	void onContextReady(String group);

	/** 当选择的节目发生播放错误 */
	void onSelectError(String msg);

	/** 播放器的直播信息已经更新，需要首次/再次发起播放 */
	void onLiveInfoUpdated();

	/** ca模块已指定 */
	void onCaModuleDispatched(int moduleId);

	/** 时移的起始时间已更新（时移总是在当前时间结束 ） */
	void onShiftStartTimeUpdated(long start);

	/**协议交互出错*/
	void onSourceError(String err);
	
	/** 移移返回时长时间ms */
	void onShiftDuration(long duration);

	/** 同步时移时间 */
	void onShiftMediaTimeSync(long t);

	/** 时移播放状态 true表示播放成功，false表示播放出错 */
	void onShiftPlay(boolean succ);

	/** pf 信息回调，参数可能为空 */
	void onPfInfoUpdated(Program present, Program follow);
	
	/**时移错误*/
	void onShiftError(String msg);
	
	/**
	 * 锁频信号
	 * @param msg
	 */
	void onSyncSignalStatus(String msg);
}