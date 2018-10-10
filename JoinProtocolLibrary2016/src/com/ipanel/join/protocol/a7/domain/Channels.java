package com.ipanel.join.protocol.a7.domain;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import org.simpleframework.xml.Attribute;
import org.simpleframework.xml.ElementList;
import org.simpleframework.xml.Root;

import cn.ipanel.android.LogHelper;

import com.ipanel.join.protocol.a7.domain.Programs.Program;

@Root(strict=false)
public class Channels implements Serializable {
	/**
	 * 
	 */
	private static final long serialVersionUID = 8966637987435946664L;

	@ElementList(inline = true, entry = "Channel")
	private List<Channel> channelList;

	@Attribute(required =false)
	private int totalResults;

	@Attribute(required = false)
	private String timeShiftURL;

	@Attribute(required = false)
	private String restartAtToken;

	public List<Channel> getChannelList() {
		return channelList;
	}

	public void setChannelList(List<Channel> channelList) {
		this.channelList = channelList;
	}

	public int getTotalResults() {
		return totalResults;
	}

	public void setTotalResults(int totalResults) {
		this.totalResults = totalResults;
	}

	public String getTimeShiftURL() {
		return timeShiftURL;
	}

	public void setTimeShiftURL(String timeShiftURL) {
		this.timeShiftURL = timeShiftURL;
	}

	public String getRestartAtToken() {
		return restartAtToken;
	}

	public void setRestartAtToken(String restartAtToken) {
		this.restartAtToken = restartAtToken;
	}

	@Root(strict=false)
	public static class Channel implements Serializable {

		/**
		 * 
		 */
		private static final long serialVersionUID = -6474807337706380939L;
		@Attribute(required=false)
		private String channelID;
		@Attribute(required = false)
		private String channelCode;
		@Attribute(required=false)
		private String channelName;
		@Attribute(required = false)
		private String channelNumber;
		@Attribute(required = false)
		private String logo;
		@Attribute(required = false)
		private String isStartOver;
		@Attribute(required = false)
		private String isTVAnyTime;
		@Attribute(required = false)
		private String isStandardChannel;
		@Attribute(required = false)
		private String isNPVR;

		@Attribute(required = false)
		private String npvrOrderFlag;
		@Attribute(required = false)
		private String tVAnyTimeOrderFlag;
		@Attribute(required = false)
		private String startOverOrderFlag;

		@ElementList(inline = true, entry = "Parameter")
		private List<Paramter> paramters;

		private List<Programs.Program> programList;

		@Attribute(name="categoryId",required=false)
		private String categoryId;
		@Attribute(required=false)
		private String ChannelSpec;
		@Attribute(required=false)
		private String serviceId;
		@Attribute(required=false)
		private String tsId;
		
		public String getChannelID() {
			return channelID;
		}

		public void setChannelID(String channelID) {
			this.channelID = channelID;
		}

		public String getIsStandardChannel() {
			return isStandardChannel;
		}

		public void setIsStandardChannel(String isStandardChannel) {
			this.isStandardChannel = isStandardChannel;
		}

		public String getCategoryId() {
			return categoryId;
		}

		public void setCategoryId(String categoryId) {
			this.categoryId = categoryId;
		}

		public String getChannelSpec() {
			return ChannelSpec;
		}

		public void setChannelSpec(String channelSpec) {
			ChannelSpec = channelSpec;
		}

		public String getServiceId() {
			return serviceId;
		}

		public void setServiceId(String serviceId) {
			this.serviceId = serviceId;
		}

		public String getTsId() {
			return tsId;
		}

		public void setTsId(String tsId) {
			this.tsId = tsId;
		}

		public String getChannelId() {
			return channelID;
		}

		public void setChannelId(String channelId) {
			this.channelID = channelId;
		}

		public String getChannelCode() {
			return channelCode;
		}

		public void setChannelCode(String channelCode) {
			this.channelCode = channelCode;
		}

		public String getChannelName() {
			return channelName;
		}

		public void setChannelName(String channelName) {
			this.channelName = channelName;
		}

		public String getChannelNumber() {
			return channelNumber;
		}

		public void setChannelNumber(String channelNumber) {
			this.channelNumber = channelNumber;
		}

		public String getLogo() {
			return logo;
		}

		public void setLogo(String logo) {
			this.logo = logo;
		}

		public String getIsStartOver() {
			return isStartOver;
		}

		public void setIsStartOver(String isStartOver) {
			this.isStartOver = isStartOver;
		}

		public String getIsTVAnyTime() {
			return isTVAnyTime;
		}

		public void setIsTVAnyTime(String isTVAnyTime) {
			this.isTVAnyTime = isTVAnyTime;
		}

		public String getIsStanrdardChannel() {
			return isStandardChannel;
		}

		public void setIsStanrdardChannel(String isStanrdardChannel) {
			this.isStandardChannel = isStanrdardChannel;
		}

		public String getIsNPVR() {
			return isNPVR;
		}

		public void setIsNPVR(String isNPVR) {
			this.isNPVR = isNPVR;
		}

		public String getNpvrOrderFlag() {
			return npvrOrderFlag;
		}

		public void setNpvrOrderFlag(String npvrOrderFlag) {
			this.npvrOrderFlag = npvrOrderFlag;
		}

		public String gettVAnyTimeOrderFlag() {
			return tVAnyTimeOrderFlag;
		}

		public void settVAnyTimeOrderFlag(String tVAnyTimeOrderFlag) {
			this.tVAnyTimeOrderFlag = tVAnyTimeOrderFlag;
		}

		public String getStartOverOrderFlag() {
			return startOverOrderFlag;
		}

		public void setStartOverOrderFlag(String startOverOrderFlag) {
			this.startOverOrderFlag = startOverOrderFlag;
		}

		public List<Paramter> getParamters() {
			return paramters;
		}

		public void setParamters(List<Paramter> paramters) {
			this.paramters = paramters;
		}

		public List<Programs.Program> getProgramList() {
			return programList;
		}

		public void setProgramList(List<Programs.Program> programList) {
			this.programList = programList;
		}

		public List<Programs.Program> getProgramsOfDay(Calendar day) {
			List<Programs.Program> list = new ArrayList<Programs.Program>();
			Calendar pDay = Calendar.getInstance();
			for (Programs.Program p : programList) {
				pDay.setTimeInMillis(p.getStartTimeUTC());
				if (pDay.get(Calendar.YEAR) == day.get(Calendar.YEAR)
						&& pDay.get(Calendar.DAY_OF_YEAR) == day.get(Calendar.DAY_OF_YEAR)) {
					list.add(p);
				}
			}
			Collections.sort(list, new Comparator<Programs.Program>() {

				@Override
				public int compare(Program lhs, Program rhs) {
					return (int) (lhs.getStartTimeUTC() / 1000 - rhs.getStartTimeUTC() / 1000);
				}
			});
			LogHelper.d("dzwillpower", "list.size(): "+list.size());
			return list;
		}

		public Programs.Program findProgramByDate(long utctime) {
			for (Programs.Program p : programList) {
				if (utctime >= p.getStartTimeUTC() && utctime < p.getEndTimeUTC()) {
					return p;
				}
			}
			return null;
		}

	}

	@Root(strict=false,name="Parameter")
	public static class Paramter implements Serializable {

		/**
		 * 
		 */
		private static final long serialVersionUID = -4179352444264125683L;

		@Attribute(required = false)
		private String serviceId;
		@Attribute(required=false)
		private String tsid;

		@Attribute(required = false)
		private String frequency;

		@Attribute(required = false)
		private String Qam;

		@Attribute(required = false)
		private String symbolRate;

		@Attribute(required = false)
		private String bitRate;

		@Attribute(required = false)
		private String customerGroup;

		@Attribute(required = false)
		private String onId;

		@Attribute(required = false)
		private String broadcastIp;

		@Attribute(required = false)
		private String broadcastPort;

		public String getServiceId() {
			return serviceId;
		}

		public void setServiceId(String serviceId) {
			this.serviceId = serviceId;
		}

		public String getTsId() {
			return tsid;
		}

		public void setTsId(String tsId) {
			this.tsid = tsId;
		}

		public String getFrequency() {
			return frequency;
		}

		public void setFrequency(String frequency) {
			this.frequency = frequency;
		}

		public String getQam() {
			return Qam;
		}

		public void setQam(String qam) {
			this.Qam = qam;
		}

		public String getSymbolRate() {
			return symbolRate;
		}

		public void setSymbolRate(String symbolRate) {
			this.symbolRate = symbolRate;
		}

		public String getBitRate() {
			return bitRate;
		}

		public void setBitRate(String bitRate) {
			this.bitRate = bitRate;
		}

		public String getCustomerGroup() {
			return customerGroup;
		}

		public void setCustomerGroup(String customerGroup) {
			this.customerGroup = customerGroup;
		}

		public String getOnId() {
			return onId;
		}

		public void setOnId(String onId) {
			this.onId = onId;
		}

		public String getBroadcastIp() {
			return broadcastIp;
		}

		public void setBroadcastIp(String broadcastIp) {
			this.broadcastIp = broadcastIp;
		}

		public String getBroadcastPort() {
			return broadcastPort;
		}

		public void setBroadcastPort(String broadcastPort) {
			this.broadcastPort = broadcastPort;
		}

	}
}
