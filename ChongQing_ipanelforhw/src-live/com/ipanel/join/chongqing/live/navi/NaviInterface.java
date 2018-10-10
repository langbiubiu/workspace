package com.ipanel.join.chongqing.live.navi;

import ipaneltv.toolkit.TimeToolkit.Weekday;
import ipaneltv.toolkit.db.DatabaseObjectification.ChannelKey;
import ipaneltv.toolkit.entitlement.EntitlementObserver.EntitlementsState;

import java.util.List;

import com.ipanel.join.chongqing.live.navi.DatabaseObjects.LiveChannel;
import com.ipanel.join.chongqing.live.navi.DatabaseObjects.LiveGroup;
import com.ipanel.join.chongqing.live.navi.DatabaseObjects.LiveProgramEvent;


// ========================= ��������� ===========================
// ===============================================================
// 1 ���½ӿ�(Interface)���������߳�(UI�߳�)�е��ã�
// 2 ���»ص�(Callback)�������߳���ʵʩ����ֱ�ӽӲ���UI����������
// ===============================================================
public interface NaviInterface {

	/** �õ���������,�Ӳ�����null */
	public List<LiveGroup> getGroups();

	/** �ύ�µķ�����Ϣ */
	public void commitOwnedGroup(LiveGroup g, List<ChannelKey> list);

	/** �õ�ָ�������Ƶ���б� ,�Ӳ�����null */
	public List<LiveChannel> getGroupedChannels(LiveGroup g);

	/** �õ�Ƶ���������Ƶ���б� ,�Ӳ�����null */
	public List<LiveChannel> getNumberedChannels();

	/** �õ�ָ��Ƶ����ÿ�ս�Ŀָ�� ,�Ӳ�����null */
	public List<LiveProgramEvent> getDailyPrograms(LiveChannel ch, Weekday d);

	/** �õ�ָ��Ƶ����ÿ�ս�Ŀָ��,�Ӳ�����null */
	public List<LiveProgramEvent> getDailyPrograms(LiveChannel ch, int offsetOfToday);

	/** ��ȡƵ������Ȩ��Ϣ */
	public EntitlementsState getEntitlements(int moduledID);
}