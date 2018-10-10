package com.ipanel.join.chongqing.live.play;

import com.ipanel.join.chongqing.live.play.AllPFDataGetter.PresentAndFollow;


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

	/** ���Ʒ���ʱ��ʱ��ms */
	void onShiftDuration(long duration);

	/** ͬ��ʱ��ʱ�� */
	void onShiftMediaTimeSync(long t);

	/** ʱ�Ʋ���״̬ true��ʾ���ųɹ���false��ʾ���ų��� */
	void onShiftPlay(boolean succ);

	/** pf ��Ϣ�ص�����������Ϊ�� */
	void onPfInfoUpdated(PresentAndFollow pf);
	
	/**ʱ�ƴ���*/
	void onShiftError(String msg);
}