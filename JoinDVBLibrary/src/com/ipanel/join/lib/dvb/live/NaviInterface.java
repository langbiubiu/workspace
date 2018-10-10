package com.ipanel.join.lib.dvb.live;

import ipaneltv.toolkit.TimeToolkit.Weekday;
import ipaneltv.toolkit.db.DatabaseObjectification.ChannelKey;
import ipaneltv.toolkit.entitlement.EntitlementObserver.EntitlementsState;

import java.util.List;

import com.ipanel.join.lib.dvb.live.DatabaseObjects.LiveChannel;
import com.ipanel.join.lib.dvb.live.DatabaseObjects.LiveProgramEvent;
import com.ipanel.join.lib.dvb.live.DatabaseObjects.LiveuGroup;

// ========================= ��������� ===========================
// ===============================================================
// 1 ���½ӿ�(Interface)���������߳�(UI�߳�)�е��ã�
// 2 ���»ص�(Callback)�������߳���ʵʩ����ֱ�ӽӲ���UI����������
// ===============================================================
public interface NaviInterface {

	/** �õ���������,�Ӳ�����null */
	List<LiveuGroup> getGroups();

	/** �ύ�µķ�����Ϣ */
	void commitOwnedGroup(LiveuGroup g, List<ChannelKey> list);

	/** �õ�ָ�������Ƶ���б� ,�Ӳ�����null */
	List<LiveChannel> getGroupedChannels(LiveuGroup g);

	/** �õ�Ƶ���������Ƶ���б� ,�Ӳ�����null */
	List<LiveChannel> getNumberedChannels();

	/** �õ�ָ��Ƶ����ÿ�ս�Ŀָ�� ,�Ӳ�����null */
	List<LiveProgramEvent> getDailyPrograms(LiveChannel ch, Weekday d);

	/** �õ�ָ��Ƶ����ÿ�ս�Ŀָ��,�Ӳ�����null */
	List<LiveProgramEvent> getDailyPrograms(LiveChannel ch, int offsetOfToday);

	/** ��ȡƵ������Ȩ��Ϣ */
	EntitlementsState getEntitlements(int moduledID);

}