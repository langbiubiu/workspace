package com.ipanel.join.chongqing.live.navi;

import ipaneltv.toolkit.db.DatabaseObjectification.ChannelKey;

public interface NaviCallback {

	/** �������ݸ���,��Ҫ�ٴ�/�״η��𲥷� */
	void onNaviUpdated();

	/** ָ�����ݸ��� */
	void onGuideUpdated(ChannelKey key);

	/** pf�и��� */
	void onPresentFollowUpdated();

	/** �ύ����ʧ�� */
	void onCommitGroupError(String name);

	/** ʱ����Ϣ���� */
	void onShiftUpdated();
}