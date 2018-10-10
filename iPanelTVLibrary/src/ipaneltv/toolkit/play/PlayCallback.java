package ipaneltv.toolkit.play;

import ipaneltv.toolkit.mediaservice.components.PFDataGetter.PresentAndFollow;

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
	/***/
	void onChannelLocked(long freq, int program_number);
	/***/
	void onDescramError(long f, int pn, int code, String err);
	/***/
	void onPasswordChecked(boolean succ);

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
	
	/** Ƶ����Ϣ�ص� */
	void onSyncSignalStatus(String json);
	
}