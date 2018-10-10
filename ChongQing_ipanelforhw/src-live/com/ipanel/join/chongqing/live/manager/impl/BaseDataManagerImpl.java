package com.ipanel.join.chongqing.live.manager.impl;

import ipaneltv.toolkit.TimeToolkit.Weekday;
import ipaneltv.toolkit.db.DatabaseObjectification.ChannelKey;
import ipaneltv.toolkit.entitlement.EntitlementObserver.EntitlementsState;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;

import android.util.Log;
import cn.ipanel.android.LogHelper;
import cn.ipanel.android.net.http.RequestParams;

import com.ipanel.chongqing_ipanelforhw.R;
import com.ipanel.join.protocol.a7.ServiceHelper;
import com.ipanel.join.chongqing.live.Constant;
import com.ipanel.join.chongqing.live.LiveApp;
import com.ipanel.join.chongqing.live.manager.DataManager;
import com.ipanel.join.chongqing.live.manager.DataManager.ShiftChannel;
import com.ipanel.join.chongqing.live.manager.IManager;
import com.ipanel.join.chongqing.live.manager.StationManager;
import com.ipanel.join.chongqing.live.navi.FaivoratesCommitor;
import com.ipanel.join.chongqing.live.navi.DatabaseObjects.LiveChannel;
import com.ipanel.join.chongqing.live.navi.DatabaseObjects.LiveGroup;
import com.ipanel.join.chongqing.live.navi.DatabaseObjects.LiveProgramEvent;
import com.ipanel.join.chongqing.live.util.TimeHelper;

public abstract class BaseDataManagerImpl extends DataManager {

	protected IManager activity;
	protected CallBack mCallBack;
	/** 所有分组 */
	protected final List<LiveGroup> mGroups = new ArrayList<LiveGroup>();
	/** 所有频道 */
	protected final List<LiveChannel> mTotalChannels = new ArrayList<LiveChannel>();
	/** 大循环频道 */
	protected final List<LiveChannel> mCircleChannels = new ArrayList<LiveChannel>();
	/** 收藏频道 */
	protected final List<ChannelKey> mFavorites = new ArrayList<ChannelKey>();
	/** 台标Map */
	protected final HashMap<ChannelKey, String> caption_map = new HashMap<ChannelKey, String>();

	public BaseDataManagerImpl(IManager context, CallBack callback) {
		this.activity = context;
		this.mCallBack = callback;
	}

	@Override
	public LiveChannel getLiveChannelByNumber(int number) {
		// TODO Auto-generated method stub
		int N = mTotalChannels.size();
		for (int i = 0; i < N; i++) {
			if (mTotalChannels.get(i).getChannelNumber() == number) {
				return mTotalChannels.get(i);
			}
		}
		return null;
	}

	@Override
	public LiveChannel getLiveChannelByChannelId(String channelId) {
		return null;
	}

	@Override
	public ShiftChannel getShiftChannel(LiveChannel channel) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public LiveChannel getLiveChannelByName(String name) {
		// TODO Auto-generated method stub
		int N = mTotalChannels.size();
		for (int i = 0; i < N; i++) {
			if (mTotalChannels.get(i).getName().equals(name)) {
				return mTotalChannels.get(i);
			}
		}
		return null;
	}

	@Override
	public LiveChannel getLiveChannelByService(int serviceID) {
		// TODO Auto-generated method stub
		int N = mTotalChannels.size();
		for (int i = 0; i < N; i++) {
			if (mTotalChannels.get(i).getChannelKey().getProgram() == serviceID) {
				return mTotalChannels.get(i);
			}
		}
		return null;
	}

	@Override
	public List<LiveChannel> getAllChannel() {
		// TODO Auto-generated method stub
		return mTotalChannels;
	}

	@Override
	public List<LiveGroup> getAllGroup() {
		// TODO Auto-generated method stub
		return mGroups;
	}

	@Override
	public LiveGroup getGroupByID(int id) {
		List<LiveGroup> groups = getGroups();
		int N = groups.size();
		for (int i = 0; i < N; i++) {
			if (groups.get(i).getId() == id) {
				return groups.get(i);
			}
		}
		return null;
	}

	@Override
	public void caculateAuth() {
		// TODO Auto-generated method stub
		EntitlementsState es = getEntitlements(activity.getCAAuthManager()
				.getCAModuleId());
		if (es == null) {
			return;
		}
		int N = mTotalChannels.size();
		for (int i = 0; i < N; i++) {
			LiveChannel entry = mTotalChannels.get(i);
			entry.setOrder(es.getEntitlement(entry.getChannelKey()));
		}
		mCallBack.onDataAuthChanged();
	}

	@Override
	public LiveChannel caculateChannelChange(int offset, int circle,
			LiveChannel play) {
		// TODO Auto-generated method stub
		List<LiveChannel> currentChannels = this.getCircleChannel(circle);

		if (currentChannels != null && currentChannels.size() > 0) {
			int lenght = currentChannels.size();
			if (lenght <= 1) {
				return play;
			}
			int index = currentChannels.indexOf(play);
			int i = 0;
			while (i < lenght) {
				index += offset;
				if (index < 0) {
					index = (lenght + index) % lenght;
				}
				if (index > lenght - 1) {
					index = index % lenght;
				}
				if (circle != Constant.BIG_CIRCLE_COLUME_ID) {
					break;
				} else {
					if (currentChannels.get(index).getServiceType() != 2) {
						break;
					}
				}
				if (currentChannels.get(index).isOrder()) {
					break;
				}
				i++;
			}
			return currentChannels.get(index);
		}
		return null;
	}

	@Override
	public List<LiveChannel> getCircleChannel(int circle) {
		// TODO Auto-generated method stub
		if (circle == Constant.FAVORITE_COLUME_ID) {
			return getGroupedChannels(LiveApp.getInstance().favoriteGroup);
		} else if (circle == Constant.SETTING_COLUME_ID) {
			List<LiveChannel> tmp = new ArrayList<LiveChannel>();
			LiveChannel search = new LiveChannel();
			search.setLogNumber(Constant.SETTING_ID_SEARCH);
			search.setLogName(activity.getContext().getString(
					R.string.pindao_search));
			tmp.add(search);
			LiveChannel ca = new LiveChannel();
			ca.setLogNumber(Constant.SETTING_ID_CA_INFO);
			ca.setLogName(activity.getContext()
					.getString(R.string.chineng_info));
			tmp.add(ca);
			return tmp;
		} else if (circle == Constant.BIG_CIRCLE_COLUME_ID) {
			return mCircleChannels;
		} else if (circle == Constant.TV_COLUME_ID) {
			return mTotalChannels;
		} else {
			return caculateList(getGroupedChannels(getGroupByID(circle)));
		}
	}
	
	private List<LiveChannel> caculateList(List<LiveChannel> list){
//		List<String> serviceIds=HomedDataManager.getServiceIds();
		List<LiveChannel> result=new ArrayList<LiveChannel>();
		int N=list.size();
		for (int i = 0; i < N; i++) {
			if(isValidChannel(list.get(i))){
//				LiveChannel enkey=list.get(i);
//				String mServiceId=enkey.getChannelKey().getProgram()+"";
//				boolean isExist=serviceIds.contains(mServiceId);
//				if (!isExist) {
//					continue;
//				}
				result.add(list.get(i));
			}
		}
		return result;
	}

	@Override
	public List<LiveProgramEvent> getShowProgramlist(LiveChannel ch) {

		if (ch == null) {
			LogHelper.i("current ch is null");
			return null;
		}
		List<LiveProgramEvent> datas = new ArrayList<LiveProgramEvent>();
		List<LiveProgramEvent> today_events = getDailyPrograms(ch, 0);
		List<LiveProgramEvent> tomorrow_events = getDailyPrograms(ch, 1);
		if (Constant.DEVELOPER_MODE) {
			today_events = new ArrayList<LiveProgramEvent>();
			tomorrow_events = new ArrayList<LiveProgramEvent>();

			long DAY = 3600 * 1000 * 24;
			long now = System.currentTimeMillis();
			long base = now - now % DAY;
			for (int i = 0; i < 24; i++) {
				LiveProgramEvent entry = new LiveProgramEvent();
				entry.setChannelKey(ch.getChannelKey());
				entry.setEnd(base + (i + 1) * 2600 * 1000);
				entry.setStart(base + i * 3600 * 1000);
				entry.setName(ch.getName() + " today " + i);
				today_events.add(entry);
			}
			base += DAY;
			for (int i = 0; i < 24; i++) {
				LiveProgramEvent entry = new LiveProgramEvent();
				entry.setChannelKey(ch.getChannelKey());
				entry.setEnd(base + (i + 1) * 2600 * 1000);
				entry.setStart(base + i * 3600 * 1000);
				entry.setName(ch.getName() + " tomorrow " + i);
				tomorrow_events.add(entry);
			}
		}

		LogHelper.i("raw today event:"
				+ (today_events == null ? "0" : today_events.size()));
		LogHelper.i("raw tomorrow event:"
				+ (tomorrow_events == null ? "0" : tomorrow_events.size()));

		final long[] times = TimeHelper.getTodaySection();
		long start_time = times[0];
		long end_time = times[1];
		LiveProgramEvent e = null;
		if (today_events != null && today_events.size() > 0) {
			int tody_length = today_events.size();
			if (end_time >= today_events.get(tody_length - 1).getStart()
					&& start_time <= today_events.get(tody_length - 1).getEnd()) {
				int lenght_j = today_events.size();
				e = new LiveProgramEvent();
				e.setTag(-1);
				datas.add(e);
				for (int i = 0; i < lenght_j; i++) {
					if (end_time >= today_events.get(i).getStart()
							&& start_time <= today_events.get(i).getEnd()) {
						if (TimeHelper.isPlaying(
								today_events.get(i).getStart(), today_events
										.get(i).getEnd(), System
										.currentTimeMillis())) {
							today_events.get(i).status = 2;
						} else {
							if (LiveApp
									.getInstance()
									.getBookManager()
									.isProgramBooked(
											today_events.get(i).getStart(),
											today_events.get(i).getChannelKey()
													.getProgram())) {
								today_events.get(i).status = 1;
							} else {
								today_events.get(i).status = 0;
							}
						}
						LogHelper.i(String.format(
								"add today event : %s and stateus %s",
								today_events.get(i).getName(),
								today_events.get(i).status));
						datas.add(today_events.get(i));
					}

				}
			}

		}
		if (tomorrow_events != null && tomorrow_events.size() > 0) {
			if (end_time >= tomorrow_events.get(0).getStart()
					&& start_time <= tomorrow_events.get(0).getEnd()) {
				e = new LiveProgramEvent();
				e.setTag(-2);
				datas.add(e);
				int lenght_m = tomorrow_events.size();
				for (int i = 0; i < lenght_m; i++) {
					if (end_time >= tomorrow_events.get(i).getStart()
							&& start_time <= tomorrow_events.get(i).getEnd()) {
						if (LiveApp
								.getInstance()
								.getBookManager()
								.isProgramBooked(
										tomorrow_events.get(i).getStart(),
										tomorrow_events.get(i).getChannelKey()
												.getProgram())) {
							tomorrow_events.get(i).status = 1;
						} else {
							tomorrow_events.get(i).status = 0;
						}
						LogHelper.i(String.format(
								"add tomorrow event : %s and stateus %s",
								tomorrow_events.get(i).getName(),
								tomorrow_events.get(i).status));
						datas.add(tomorrow_events.get(i));
					}
				}
			}
		}

		int lenght = datas.size();
		long e_start = 0, e_end = 0;
		for (int i = 0; i < lenght; i++) {
			if (datas.get(i).getTag() != null) {
				continue;
			}
			e_start = datas.get(i).getStart();
			break;
		}

		for (int i = lenght - 1; i >= 0; i--) {
			if (datas.get(i).getTag() != null) {
				continue;
			}
			e_end = datas.get(i).getEnd();
			break;
		}
		LogHelper.i("start time :" + TimeHelper.getDetailTime(e_start));
		LogHelper.i("end time :" + TimeHelper.getDetailTime(e_end));

		if (e_end - e_start < 24 * 3600 * 1000) {
			long focus_time = Math.max(System.currentTimeMillis(), e_end);
			LogHelper.i("send time :" + TimeHelper.getDetailTime(focus_time));

			observeProgramGuide(ch.getChannelKey(), focus_time);
		}
		LogHelper.i("final get epg :" + (datas == null ? 0 : datas.size())
				+ ";ch=" + ch.getChannelKey() + "<-->" + ch.getProgram());

		return datas;

	}

	@Override
	public void updateChannelFavorite(LiveChannel channel) {
		// TODO Auto-generated method stub
		Log.e("LiveFavoriteChannelFragment"," 删除前收藏频道数量 ： "+mFavorites.size());
		Log.d("LiveFavoriteChannelFragment"," channelKey");
		if (isFavoriteChannel(channel)) {
			channel.favorite = 0;
			boolean remove = mFavorites.remove(channel.getChannelKey());
			Log.e("LiveFavoriteChannelFragment"," 删除收藏频道 remove"+remove);
			
		} else {
			channel.favorite = 1;
			mFavorites.add(channel.getChannelKey());
			Log.d("LiveFavoriteChannelFragment"," 添加收藏频道");
		}
		
		
		
		Log.e("LiveFavoriteChannelFragment"," 收藏频道数量 ： "+mFavorites.size());
		commitOwnedGroup(LiveApp.getInstance().favoriteGroup, mFavorites);
	}

	@Override
	public void checkDataValid() {
		// TODO Auto-generated method stub
		ServiceHelper.getHelper().cancelAllTasks();
	}

	@Override
	public boolean isValidAuthForShift(LiveChannel channel) {
		// TODO Auto-generated method stub
		return true;
	}

	@Override
	public String getLiveWebPlayURL(LiveChannel channel) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void saveOffChannel(LiveChannel channel) {
		// TODO Auto-generated method stub

	}

	@Override
	public String getChannelCaption(ChannelKey key) {
		// TODO Auto-generated method stub
		return caption_map.get(key);
	}

	@Override
	public void requestChannelPF(ChannelKey key) {
		// TODO Auto-generated method stub
		StationManager mStationManager = activity.getStationManager();
	}

//	@Override
//	public boolean isFavoriteChannel(LiveChannel channel) {
//		// TODO Auto-generated method stub
//		if (channel != null) {
//			return channel.getFavorite() > 0;
//		}
//		return false;
//	}

	protected void initChannelData() {
		Log.d("channel", "--------ooooooooinitChannelDataoooooo----->");
		mTotalChannels.clear();
		mCircleChannels.clear();
		mFavorites.clear();
		mGroups.clear();
		// 初始化收藏频道
		LiveGroup fav = getGroupByID(Constant.FAVORITE_COLUME_ID);
		if (fav != null) {
			LiveApp.getInstance().favoriteGroup = fav;
		}else{
			LiveApp.getInstance().favoriteGroup=new LiveGroup();
			int currentProcessID = android.os.Process.myUid();
			LiveApp.getInstance().favoriteGroup.setUid(currentProcessID);
			LiveApp.getInstance().favoriteGroup.setName(FaivoratesCommitor.NAME);
			LiveApp.getInstance().favoriteGroup.setId(Constant.FAVORITE_COLUME_ID);
		}
		List<LiveChannel> fc = getGroupedChannels(LiveApp.getInstance().favoriteGroup);
		int f_length = fc.size();
		for (int i = 0; i < f_length; i++) {
			if(!mFavorites.contains(fc.get(i).getChannelKey()))
				mFavorites.add(fc.get(i).getChannelKey());
		}
		// 初始化授权频道
		EntitlementsState es = getEntitlements(activity.getCAAuthManager()
				.getCAModuleId());
		// 初始化分组数据
		List<LiveGroup> gs = getGroups();
		int g_lenght = gs.size();
		for (int i = 0; i < g_lenght; i++) {
			LiveGroup entry = gs.get(i);
			if (isValidGroup(entry)) {
				mGroups.add(entry);
			}
		}
		final int GROUP_LENGHT = mGroups.size();
		synchronized (mTotalChannels) {
//			List<String> serviceIds=HomedDataManager.getServiceIds();
			List<LiveChannel> raw = getNumberedChannels();
			Collections.sort(raw, new ServiceComparator());
			final int N = raw.size();
			LogHelper.i("raw channel size :" + N);
			for (int i = 0; i < N; i++) {
				LiveChannel entry = raw.get(i);
				if (isValidChannel(entry)) {
					
					if (es != null) {
						entry.setOrder(es.getEntitlement(entry.getChannelKey()));
					}
					
					//String mServiceId=entry.getChannelKey().getProgram()+"";
					//boolean isExist=serviceIds.contains(mServiceId);
					Log.d("channel", "--------oooooooo频道名oooooo----->"+entry.getName());
					//Log.d("channel", "--------isExist----->"+isExist);
//					if (!isExist) {
//						continue;
//					}
					
					if(entry.getType() == 65535){
						continue;
					}

					
					if (mFavorites.contains(entry.getChannelKey())) {
						Log.d("channel", "--------oooooo喜爱频道oooooo----->"+entry.getChannelKey().getProgram());
						entry.setFavorite(1);
					} else {
						entry.setFavorite(0);
					}
					// 计算分组
					for (int j = 0; j < GROUP_LENGHT; j++) {
						if (mGroups.get(j).getChannelKeys()
								.contains(entry.getChannelKey())) {
							entry.setTag(mGroups.get(j));
							break;
						}
					}
					if (entry.getTag() instanceof LiveGroup) {
//						if (entry.getServiceType() != 2
//								&& (entry.getType() == 4081) {
//						}
						mCircleChannels.add(entry);
					}
					mTotalChannels.add(entry);
				}
			}
		}
//		resortChannel();
		Collections.sort(mCircleChannels, new NumberComparator());
		Collections.sort(mTotalChannels, new NumberComparator());

		LogHelper
				.i("序号              频道号            频率         TsID       ServiceID       频道名");
		for (int i = 0; i < mCircleChannels.size(); i++) {
			LogHelper.i(i + "    " + mCircleChannels.get(i).getChannelNumber()
					+ "   "
					+ mCircleChannels.get(i).getChannelKey().getFrequency()
					+ "   " + mCircleChannels.get(i).getTsId() + "   "
					+ mCircleChannels.get(i).getChannelKey().getProgram()
					+ "   " + mCircleChannels.get(i).getName());
		}

		LogHelper.i("------------------------------>");
		LogHelper.i(mCircleChannels.size() + "");
		LogHelper.i("------------------------------>");
	}
	
	protected void resortChannel(){
		
	}

	protected boolean isValidChannel(LiveChannel channel) {
		return true;
	}

	protected boolean isValidGroup(LiveGroup group) {
		return group != null && group.getName() != null
				&& !group.getName().contains("_");
	}

	public class ServiceComparator implements Comparator<Object> {
		public int compare(Object arg0, Object arg1) {
			LiveChannel channel0 = (LiveChannel) arg0;
			LiveChannel channel1 = (LiveChannel) arg1;
			return channel0.getChannelKey().getProgram()
					- channel1.getChannelKey().getProgram();
		}
	}

	public class NumberComparator implements Comparator<Object> {
		public int compare(Object arg0, Object arg1) {
			LiveChannel channel0 = (LiveChannel) arg0;
			LiveChannel channel1 = (LiveChannel) arg1;
			return channel0.getChannelNumber() - channel1.getChannelNumber();
		}
	}

	/** 得到分组数据,从不返回null */
	public abstract List<LiveGroup> getGroups();

	/** 提交新的分组信息 */
	public abstract void commitOwnedGroup(LiveGroup g, List<ChannelKey> list);

	/** 得到指定分组的频道列表 ,从不返回null */
	public abstract List<LiveChannel> getGroupedChannels(LiveGroup g);

	/** 得到频道号排序的频道列表 ,从不返回null */
	public abstract List<LiveChannel> getNumberedChannels();

	/** 得到指定频道的每日节目指南 ,从不返回null */
	public abstract List<LiveProgramEvent> getDailyPrograms(LiveChannel ch,
			Weekday d);

	/** 得到指定频道的每日节目指南,从不返回null */
	public abstract List<LiveProgramEvent> getDailyPrograms(LiveChannel ch,
			int offsetOfToday);

	/** 获取频道的授权信息 */
	public abstract EntitlementsState getEntitlements(int moduledID);

}
