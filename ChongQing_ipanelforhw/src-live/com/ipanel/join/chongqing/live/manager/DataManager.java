package com.ipanel.join.chongqing.live.manager;

import ipaneltv.toolkit.db.DatabaseObjectification.ChannelKey;
import ipaneltv.toolkit.db.DatabaseObjectification.Program;

import java.text.SimpleDateFormat;
import java.util.List;

import com.ipanel.join.chongqing.live.navi.DatabaseObjects.LiveChannel;
import com.ipanel.join.chongqing.live.navi.DatabaseObjects.LiveGroup;
import com.ipanel.join.chongqing.live.navi.DatabaseObjects.LiveProgramEvent;

/**
 * ���ݹ�����
 * */
public abstract class DataManager{
	public static final String INVALID = "__invalid";
	public static SimpleDateFormat formatter_b = new SimpleDateFormat("yyyyMMddHHmmss");
	public static SimpleDateFormat formatter_i = new SimpleDateFormat("yyyyMMdd");
	/**
	 * ����Ƶ���Ų�ѯƵ��
	 * */
	public abstract LiveChannel getLiveChannelByNumber(int number);
	
	/**
	 * ����Ƶ��ID��ѯƵ��
	 * @param channelId Ƶ��ID
	 * @return
	 */
	public abstract LiveChannel getLiveChannelByChannelId(String channelId);
	/**
	 * ����Ƶ������ѯƵ��
	 * */
	public abstract LiveChannel getLiveChannelByName(String name);
	/**
	 * ����Ƶ����Ϣ��ѯƵ��
	 * */
	public abstract LiveChannel getLiveChannelByService(int serviceID);
	/**
	 * ��ȡ������ЧƵ��
	 * */
	public abstract List<LiveChannel> getAllChannel();
	/**
	 * ��ȡ��������Ӧ��Ƶ���б�
	 * */
	public abstract List<LiveChannel> getCircleChannel(int circle);
	/**
	 * ��ȡ������Ч����
	 * */
	public abstract List<LiveGroup> getAllGroup();
	/**
	 * ���ݷ���ID��ѯ����
	 * */
	public abstract LiveGroup getGroupByID(int id);
	/**
	 * ����Ƶ���Ƿ���ʱ��Ƶ��
	 * */
	public abstract ShiftChannel isShiftChannel(LiveChannel channel);

	public abstract ShiftChannel getShiftChannel(LiveChannel channel);
	/**
	 * ��ȡ����ʱ��Ƶ��
	 */
	public abstract List<LiveChannel> getAllShiftChannel();
	/**
	 * ��ָ�������ڼ��㵱ǰƵ��������Ƶ��
	 * */
	public abstract LiveChannel caculateChannelChange(int offest,int circle,LiveChannel play);
	/**
	 * ����ָ��Ƶ�����ղ���Ϣ
	 * */
	public abstract void updateChannelFavorite(LiveChannel channel);
	/**
	 * ��ȡָ��Ƶ���Ľ�Ŀ��
	 * */
	public abstract List<LiveProgramEvent> getShowProgramlist(LiveChannel ch) ;
	/**
	 * ��ȡָ��Ƶ��ָ����Ľ�Ŀ�� : IP
	 * */
	public abstract List<LiveProgramEvent> getShowProgramlist(LiveChannel ch,int offset,boolean again) ;
	/**
	 * ��ȡָ��Ƶ����̨��
	 * */
	public abstract String getChannelCaption(ChannelKey key);
	/**
	 * ��ȡָ��Ƶ���ĵ�ǰ���Ž�Ŀ
	 * */
	public abstract Program getChannelCurrentProgram(LiveChannel channel) ;
	/**
	 * ��ȡָ��Ƶ������һ���Ž�Ŀ
	 * */
	public abstract Program getChannelNextProgram(LiveChannel channel) ;
	/**
	 * ������ݵ���Ч��
	 * */
	public abstract void checkDataValid();
	/**
	 * ����Ƶ����ʱ������
	 * */
	public abstract void requestShiftURL(LiveChannel channel);
	/**
	 * ����Ƶ����ʱ������
	 * */
	public abstract void requestShiftProgram(LiveChannel channel);
	/**
	 * ����Ƶ���Ļؿ�����
	 * */
	public abstract void requestReplayProgram(LiveChannel channel, String time);
	/**
	 * ����Ƶ����PF����
	 * */
	public abstract void requestChannelPF(ChannelKey key);
	/**
	 * ����Ƶ���б����Ȩ���
	 * */
	public abstract void caculateAuth();
	/**
	 * ����ָ��Ƶ���Ľ�Ŀ��
	 * */
	public abstract void observeProgramGuide(ChannelKey key,long time);
	/**
	 * Ƶ����ʱ���Ƿ�����Ȩ
	 * */
	public abstract boolean isValidAuthForShift(LiveChannel channel);
	
	/**
	 * ��ȡϲ��Ƶ���б�
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
