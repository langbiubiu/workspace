package com.ipanel.join.chongqing.live.navi;

import ipaneltv.toolkit.TimeToolkit.Weekday;
import ipaneltv.toolkit.db.DatabaseObjectification.ChannelKey;
import ipaneltv.toolkit.entitlement.EntitlementObserver.EntitlementsState;

import java.util.List;

import com.ipanel.join.chongqing.live.navi.DatabaseObjects.LiveChannel;
import com.ipanel.join.chongqing.live.navi.DatabaseObjects.LiveGroup;
import com.ipanel.join.chongqing.live.navi.DatabaseObjects.LiveProgramEvent;


// ========================= 放在最后面 ===========================
// ===============================================================
// 1 以下接口(Interface)均可在主线程(UI线程)中调用，
// 2 以下回调(Callback)均在主线程中实施，可直接接操作UI，不能阻塞
// ===============================================================
public interface NaviInterface {

	/** 得到分组数据,从不返回null */
	public List<LiveGroup> getGroups();

	/** 提交新的分组信息 */
	public void commitOwnedGroup(LiveGroup g, List<ChannelKey> list);

	/** 得到指定分组的频道列表 ,从不返回null */
	public List<LiveChannel> getGroupedChannels(LiveGroup g);

	/** 得到频道号排序的频道列表 ,从不返回null */
	public List<LiveChannel> getNumberedChannels();

	/** 得到指定频道的每日节目指南 ,从不返回null */
	public List<LiveProgramEvent> getDailyPrograms(LiveChannel ch, Weekday d);

	/** 得到指定频道的每日节目指南,从不返回null */
	public List<LiveProgramEvent> getDailyPrograms(LiveChannel ch, int offsetOfToday);

	/** 获取频道的授权信息 */
	public EntitlementsState getEntitlements(int moduledID);
}