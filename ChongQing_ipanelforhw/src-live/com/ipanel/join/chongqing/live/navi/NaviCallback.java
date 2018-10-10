package com.ipanel.join.chongqing.live.navi;

import ipaneltv.toolkit.db.DatabaseObjectification.ChannelKey;

public interface NaviCallback {

	/** 导航数据更新,需要再次/首次发起播放 */
	void onNaviUpdated();

	/** 指南数据更新 */
	void onGuideUpdated(ChannelKey key);

	/** pf有更新 */
	void onPresentFollowUpdated();

	/** 提交分组失败 */
	void onCommitGroupError(String name);

	/** 时移信息更新 */
	void onShiftUpdated();
}