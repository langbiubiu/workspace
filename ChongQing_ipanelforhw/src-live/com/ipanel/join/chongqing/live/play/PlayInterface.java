package com.ipanel.join.chongqing.live.play;

import ipaneltv.toolkit.db.DatabaseObjectification.ChannelKey;

/** �������߳�ֱ��ʹ�ýӿ�(�ӿ�ʵ�ֶԵ��ú�ʱ��һ��תΪ��Ϣ�첽����) */
public interface PlayInterface {

	/** ѡ��Ƶ������ */
	void select(long freq, int fflags, int program, int pflags);

	/** ������Ƶ����ʾ��Χ */
	void setDisplay(int x, int y, int w, int h);

	/** �������� */
	void setVolume(float v);

	/** ����ʱ��,��һ���������-1��ʾ�ӵ�ǰʱ�俪ʼ,0�����ʼʱ�俪ʼ */
	void shift(String uri, int offsetOfShiftStartTime, int flags);

	/** ʱ��seek�������� */
	void shiftSeek(long t);

	/** ֹͣʱ�� */
	void shiftStop();

	/** ��ͣʱ�� */
	void shiftPause(String uri);

	/** ���ý�Ŀ���ŵı�־ */
	void setProgramFlags(int flags);

	/** ��ע��Ŀָ�����ݣ�focusTime���ʱ��������ȴ���,0��ʾ����עʱ�� */
	void observeProgramGuide(ChannelKey ch, long focusTime);

	/** ��ע�Ľ�pf��Ŀ��Ϣ */
	void getPresentAndFollow(ChannelKey ch);
	
	/**����ֱ���л�����̨ʱ��loosean״̬*/
	void setLoosen(boolean b);
	
	void loosenAllSession();

	void syncSignalStatus();
}