package com.ipanel.join.cq.data;

import java.io.Serializable;
import java.util.List;

import com.google.gson.annotations.Expose;
/**
 * 华为频道列表
 */
public class BtvChannel implements Serializable {

	private static final long serialVersionUID = 1L;
	@Expose
	private String code;
	@Expose
	private String cataName;
	
	public String getCode() {
		return code;
	}

	public void setCode(String code) {
		this.code = code;
	}

	public String getCataName() {
		return cataName;
	}

	public void setCataName(String cataName) {
		this.cataName = cataName;
	}

	public List<HwChannel> getChannelList() {
		return chanelList;
	}

	public void setChannelList(List<HwChannel> channelList) {
		this.chanelList = channelList;
	}

	@Expose
	private List<HwChannel> chanelList;//华为的channel list
	
	public class HwChannel implements Serializable{
		@Expose
		private String  channelID;
		@Expose
		private String channelName;
		@Expose
		private String logo;
		@Expose
		private String introduction;
		@Expose 
		private String picsPath;
		@Expose 
		private int hd;
		
		
		public String getChannelId() {
			return channelID;
		}
		public void setChannelId(String channelId) {
			this.channelID = channelId;
		}
		public String getChannelName() {
			return channelName;
		}
		public void setChannelName(String channelName) {
			this.channelName = channelName;
		}
		public String getLogo() {
			return logo;
		}
		public void setLogo(String logo) {
			this.logo = logo;
		}
		public String getIntroduction() {
			return introduction;
		}
		public void setIntroduction(String introduction) {
			this.introduction = introduction;
		}
		public String getPic_path() {
			return picsPath;
		}
		public void setPic_path(String pic_path) {
			this.picsPath = pic_path;
		}
		public int getHd() {
			return hd;
		}
		public void setHd(int hd) {
			this.hd = hd;
		}
	}

}
