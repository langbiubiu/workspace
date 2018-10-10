package com.ipanel.join.lib.dvb.live;

import ipaneltv.toolkit.db.DatabaseObjectification.ChannelKey;
import ipaneltv.toolkit.media.MediaSessionInterface.TsPlayerInetSourceInterface;

/** �������߳�ֱ��ʹ�ýӿ�(�ӿ�ʵ�ֶԵ��ú�ʱ��һ��תΪ��Ϣ�첽����) */
public interface PlayInterface {
	
	public static final int SHIFT_FLAG_IPQAM = TsPlayerInetSourceInterface.STREAM_TYPE_INET;
	public static final int SHIFT_FLAG_IP = TsPlayerInetSourceInterface.STREAM_TYPE_IPQAM;

	/** ѡ��Ƶ������ */
	void select(String httpUrl, long freq, int fflags, int program, int pflags);

	/** ������Ƶ����ʾ��Χ */
	void setDisplay(int x, int y, int w, int h);

	/** �������� */
	void setVolume(float v);

	/**
	 * ����ʱ��,��һ���������-1��ʾ�ӵ�ǰʱ�俪ʼ,0�����ʼʱ�俪ʼ
	 * @param uri
	 * @param offsetOfShiftStartTime
	 * @param flags {@link TsPlayerInetSourceInterface#STREAM_TYPE_IPQAM} {@link TsPlayerInetSourceInterface#STREAM_TYPE_INET}
	 */
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
	
	void loosenAllSession();
	
	/**����ֱ���л�����̨ʱ��loosean״̬*/
	void setLoosen(boolean b);
}