package com.ipanel.join.chongqing.live.manager;

import ipaneltv.toolkit.db.DatabaseObjectification.ChannelKey;
import ipaneltv.toolkit.db.DatabaseObjectification.Program;

import java.text.SimpleDateFormat;
import java.util.List;

import com.ipanel.join.chongqing.live.navi.DatabaseObjects.LiveChannel;
import com.ipanel.join.chongqing.live.navi.DatabaseObjects.LiveGroup;
import com.ipanel.join.chongqing.live.navi.DatabaseObjects.LiveProgramEvent;

/**
 * 数据管理类
 * */
public abstract class DataManager{
	public static final String INVALID = "__invalid";
	public static SimpleDateFormat formatter_b = new SimpleDateFormat("yyyyMMddHHmmss");
	public static SimpleDateFormat formatter_i = new SimpleDateFormat("yyyyMMdd");
	/**
	 * 根据频道号查询频道
	 * */
	public abstract LiveChannel getLiveChannelByNumber(int number);
	
	/**
	 * 根据频道ID查询频道
	 * @param channelId 频道ID
	 * @return
	 */
	public abstract LiveChannel getLiveChannelByChannelId(String channelId);
	/**
	 * 根据频道名查询频道
	 * */
	public abstract LiveChannel getLiveChannelByName(String name);
	/**
	 * 根据频点信息查询频道
	 * */
	public abstract LiveChannel getLiveChannelByService(int serviceID);
	/**
	 * 获取所有有效频道
	 * */
	public abstract List<LiveChannel> getAllChannel();
	/**
	 * 获取分组所对应的频道列表
	 * */
	public abstract List<LiveChannel> getCircleChannel(int circle);
	/**
	 * 获取所有有效分组
	 * */
	public abstract List<LiveGroup> getAllGroup();
	/**
	 * 根据分组ID查询分组
	 * */
	public abstract LiveGroup getGroupByID(int id);
	/**
	 * 给定频道是否是时移频道
	 * */
	public abstract ShiftChannel isShiftChannel(LiveChannel channel);

	public abstract ShiftChannel getShiftChannel(LiveChannel channel);
	/**
	 * 获取所有时移频道
	 */
	public abstract List<LiveChannel> getAllShiftChannel();
	/**
	 * 在指定分组内计算当前频道的相邻频道
	 * */
	public abstract LiveChannel caculateChannelChange(int offest,int circle,LiveChannel play);
	/**
	 * 更新指定频道的收藏信息
	 * */
	public abstract void updateChannelFavorite(LiveChannel channel);
	/**
	 * 获取指定频道的节目单
	 * */
	public abstract List<LiveProgramEvent> getShowProgramlist(LiveChannel ch) ;
	/**
	 * 获取指定频道指定天的节目单 : IP
	 * */
	public abstract List<LiveProgramEvent> getShowProgramlist(LiveChannel ch,int offset,boolean again) ;
	/**
	 * 获取指定频道的台标
	 * */
	public abstract String getChannelCaption(ChannelKey key);
	/**
	 * 获取指定频道的当前播放节目
	 * */
	public abstract Program getChannelCurrentProgram(LiveChannel channel) ;
	/**
	 * 获取指定频道的下一播放节目
	 * */
	public abstract Program getChannelNextProgram(LiveChannel channel) ;
	/**
	 * 检测数据的有效性
	 * */
	public abstract void checkDataValid();
	/**
	 * 请求频道的时移数据
	 * */
	public abstract void requestShiftURL(LiveChannel channel);
	/**
	 * 请求频道的时移数据
	 * */
	public abstract void requestShiftProgram(LiveChannel channel);
	/**
	 * 请求频道的回看数据
	 * */
	public abstract void requestReplayProgram(LiveChannel channel, String time);
	/**
	 * 请求频道的PF数据
	 * */
	public abstract void requestChannelPF(ChannelKey key);
	/**
	 * 计算频道列表的授权情况
	 * */
	public abstract void caculateAuth();
	/**
	 * 搜索指定频道的节目单
	 * */
	public abstract void observeProgramGuide(ChannelKey key,long time);
	/**
	 * 频道的时移是否已授权
	 * */
	public abstract boolean isValidAuthForShift(LiveChannel channel);
	
	/**
	 * 获取喜爱频道列表
	 * */
	public abstract void getFavouriteChannels() ;
	
	public abstract boolean isFavoriteChannel(LiveChannel channel);
	
	public abstract String getLiveWebPlayURL(LiveChannel channel);
	
	//public abstract String getShiftPlayURL(LiveChannel channel);

	public abstract String getShiftPlayURL(LiveChannel channel, long startTime, long endTime);
	
	public abstract ShiftProgram getShiftProgramAtTime(LiveChannel channel,long time);
	
	public abstract void saveOffChannel(LiveChannel channel);
	
	public abstract boolean isReadbyOK();
	
	public abstract void setWaitingForData();
	
	public static interface CallBack{

		public void onShiftDataResponse(ShiftChannel channel);
		
		public void onChannelCaptionLoaded();
		
		public void onPFDataChanged(ChannelKey key);
		
		public void onEPGDataChanged(ChannelKey key,String time);
		
		public void onDataAuthChanged();
		
		public void onChannelProgramChanged(ChannelKey key);
		
		public void onChannelGroupDataChanged(String group_id);
		
		public void onFavoriteChannelChanged(int change_mode, boolean success);
		
	}

	public static abstract class ShiftChannel{
						
		public abstract String getMatchKey();
		
		public abstract String getId();
		
	}
	
	public static abstract class ShiftProgram {
		protected long start;
		protected long end;
		protected String name;
		public long getStart() {
			return start;
		}
		public void setStart(long start) {
			this.start = start;
		}
		public long getEnd() {
			return end;
		}
		public void setEnd(long end) {
			this.end = end;
		}
		public String getName() {
			return name;
		}
		public void setName(String name) {
			this.name = name;
		}
		public long getDuration() {
			return end-start;
		}
		
		
	}

}
