package com.ipanel.join.protocol.huawei.cqvod;

import java.io.Serializable;
import java.util.Iterator;
import java.util.List;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

public class UniqueSearch implements Serializable {

	/**
	 * 
	 */
	private static final long serialVersionUID = 9110403286180831709L;
	@Expose
	private String total;
	@Expose
	private Status status;
	@Expose
	@SerializedName("data")
	private List<UniqueSearchData> uniqueSearchDataList;
	
	public class UniqueSearchData implements Serializable{

		/**
		 * 
		 */
		private static final long serialVersionUID = -6153725289905042126L;
		@Expose
		private String channelFrequency;
		@Expose
		private String channelName;
		@Expose
		private String dataSource;
		@Expose
		private String endTime;
		@Expose
		private String playDate;
		@Expose
		private String serviceId;
		@Expose
		private String startTime;
		@Expose
		private String videoName;
		@Expose
		private String vodId;
		public String getChannelFrequency() {
			return channelFrequency;
		}
		public void setChannelFrequency(String channelFrequency) {
			this.channelFrequency = channelFrequency;
		}
		public String getChannelName() {
			return channelName;
		}
		public void setChannelName(String channelName) {
			this.channelName = channelName;
		}
		public String getDataSource() {
			return dataSource;
		}
		public void setDataSource(String dataSource) {
			this.dataSource = dataSource;
		}
		public String getEndTime() {
			return endTime;
		}
		public void setEndTime(String endTime) {
			this.endTime = endTime;
		}
		public String getPlayDate() {
			return playDate;
		}
		public void setPlayDate(String playDate) {
			this.playDate = playDate;
		}
		public String getServiceId() {
			return serviceId;
		}
		public void setServiceId(String serviceId) {
			this.serviceId = serviceId;
		}
		public String getStartTime() {
			return startTime;
		}
		public void setStartTime(String startTime) {
			this.startTime = startTime;
		}
		public String getVideoName() {
			return videoName;
		}
		public void setVideoName(String videoName) {
			this.videoName = videoName;
		}
		public String getVodId() {
			return vodId;
		}
		public void setVodId(String vodId) {
			this.vodId = vodId;
		}
		@Override
		public String toString() {
			return "UniqueSearchData [channelFrequency=" + channelFrequency + ", channelName=" + channelName
					+ ", dataSource=" + dataSource + ", endTime=" + endTime + ", playDate=" + playDate + ", serviceId="
					+ serviceId + ", startTime=" + startTime + ", videoName=" + videoName + ", vodId=" + vodId + "]";
		}
		
	}
	
	public class Status implements Serializable{

		/**
		 * 
		 */
		private static final long serialVersionUID = -8937469784608012172L;
		@Expose
		private String code;
		@Expose
		private String msg;
		public String getCode() {
			return code;
		}
		public void setCode(String code) {
			this.code = code;
		}
		public String getMsg() {
			return msg;
		}
		public void setMsg(String msg) {
			this.msg = msg;
		}
		@Override
		public String toString() {
			return "Status [code=" + code + ", msg=" + msg + "]";
		}
		
		
	}

	public String getTotal() {
		return total;
	}

	public void setTotal(String total) {
		this.total = total;
	}

	public Status getStatus() {
		return status;
	}

	public void setStatus(Status status) {
		this.status = status;
	}

	public List<UniqueSearchData> getUniqueSearchDataList() {
		if(uniqueSearchDataList != null && uniqueSearchDataList.size()>0){
			for (Iterator<UniqueSearchData> iterator = uniqueSearchDataList.iterator();iterator.hasNext();) {
				String dataSource = iterator.next().getDataSource();
				if(! dataSource.equals("live")){
					iterator.remove();
				}
			}
		}
		return uniqueSearchDataList;
	}

	public void setUniqueSearchDataList(List<UniqueSearchData> uniqueSearchDataList) {
		this.uniqueSearchDataList = uniqueSearchDataList;
	}

	@Override
	public String toString() {
		return "UniqueSearch [total=" + total + ", status=" + status + ", uniqueSearchDataList=" + uniqueSearchDataList
				+ "]";
	}
	
	

}
