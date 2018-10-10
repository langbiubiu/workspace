package com.ipanel.join.lib.dvb.live;

import ipaneltv.toolkit.db.DatabaseObjectification.Program;

/** �ص��߳�Ϊ���߳̽��У���ֱ�ӽ���ui��������������ѭAndroidԭ�� */
public interface PlayCallback {
	/** �������Ѿ��� */
	void onContextReady(String group);

	/** ��ѡ��Ľ�Ŀ�������Ŵ��� */
	void onSelectError(String msg);

	/** ��������ֱ����Ϣ�Ѿ����£���Ҫ�״�/�ٴη��𲥷� */
	void onLiveInfoUpdated();

	/** caģ����ָ�� */
	void onCaModuleDispatched(int moduleId);

	/** ʱ�Ƶ���ʼʱ���Ѹ��£�ʱ�������ڵ�ǰʱ����� �� */
	void onShiftStartTimeUpdated(long start);

	/**Э�齻������*/
	void onSourceError(String err);
	
	/** ���Ʒ���ʱ��ʱ��ms */
	void onShiftDuration(long duration);

	/** ͬ��ʱ��ʱ�� */
	void onShiftMediaTimeSync(long t);

	/** ʱ�Ʋ���״̬ true��ʾ���ųɹ���false��ʾ���ų��� */
	void onShiftPlay(boolean succ);

	/** pf ��Ϣ�ص�����������Ϊ�� */
	void onPfInfoUpdated(Program present, Program follow);
	
	/**ʱ�ƴ���*/
	void onShiftError(String msg);
	
	/**
	 * ��Ƶ�ź�
	 * @param msg
	 */
	void onSyncSignalStatus(String msg);
}