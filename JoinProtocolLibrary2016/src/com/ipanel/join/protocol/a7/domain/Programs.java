package com.ipanel.join.protocol.a7.domain;

import java.io.Serializable;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;

import org.simpleframework.xml.Attribute;
import org.simpleframework.xml.ElementList;
import org.simpleframework.xml.Root;

@Root(strict=false)
public class Programs implements Serializable {

	/**
	 * 
	 */
	private static final long serialVersionUID = 8096545376585270231L;

	@ElementList(inline = true, entry = "Program", required = false)
	private List<Program> programs = new ArrayList<Programs.Program>();

	@Attribute
	private int totalResults;

	@Attribute(required = false)
	private String restartAtToken;

	public List<Program> getPrograms() {
		return programs;
	}

	public void setPrograms(List<Program> programs) {
		this.programs = programs;
	}

	public int getTotalResults() {
		return totalResults;
	}

	public void setTotalResults(int totalResults) {
		this.totalResults = totalResults;
	}

	public String getRestartAtToken() {
		return restartAtToken;
	}

	public void setRestartAtToken(String restartAtToken) {
		this.restartAtToken = restartAtToken;
	}

	@Root(strict=false)
	public static class Program implements Serializable {
		static final SimpleDateFormat DF_DATE = new SimpleDateFormat("yyyyMMddHHmmss");
		/**
		 * 
		 */
		private static final long serialVersionUID = -1458784495632300537L;

		@Attribute(required=false)
		private String programId;

		@Attribute(required = false)
		private String assetId;

		@Attribute(required=false)
		private String programName;

		@Attribute(required=false)
		private String channelId;

		@Attribute(required = false)
		private String isNPVR;

		@Attribute(required = false)
		private String isTVAnyTime;

		@Attribute
		private String startDateTime;
		@Attribute
		private String endDateTime;

		@Attribute(required=false)
		private String status;
		@Attribute(required=false)
		private String channelName;
		public String getProgramId() {
			return programId;
		}

		public void setProgramId(String programId) {
			this.programId = programId;
		}

		public String getAssetId() {
			return assetId;
		}

		public void setAssetId(String assetId) {
			this.assetId = assetId;
		}

		public String getProgramName() {
			return programName;
		}

		public void setProgramName(String programName) {
			this.programName = programName;
		}

		public String getChannelId() {
			return channelId;
		}

		public void setChannelId(String channelId) {
			this.channelId = channelId;
		}

		public String getIsNPVR() {
			return isNPVR;
		}

		public void setIsNPVR(String isNPVR) {
			this.isNPVR = isNPVR;
		}

		public String getIsTVAnyTime() {
			return isTVAnyTime;
		}

		public void setIsTVAnyTime(String isTVAnyTime) {
			this.isTVAnyTime = isTVAnyTime;
		}

		public String getStartTime() {
			return startDateTime;
		}

		public void setStartTime(String startTime) {
			this.startDateTime = startTime;
		}

		public String getEndTime() {
			return endDateTime;
		}

		public void setEndTime(String endTime) {
			this.endDateTime = endTime;
		}

		public String getStatus() {
			return status;
		}

		public long getStartTimeUTC() {
			try {
				return DF_DATE.parse(getStartTime()).getTime();
			} catch (ParseException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			return 0;
		}

		public long getEndTimeUTC() {
			try {
				return DF_DATE.parse(getEndTime()).getTime();
			} catch (ParseException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			return 0;
		}

		/**
		 * 录制状态 --未预约:4 --已预约:0 (app中表示新建意义应该跟预约差不多) --开始录制:1 --录制完成:2 --预约失败:5
		 * --录制失败:3
		 */
		public void setStatus(String status) {
			this.status = status;
		}

		@Override
		public String toString() {
			return "Program [programId=" + programId + ", assetId=" + assetId
					+ ", programName=" + programName + ", channelId="
					+ channelId + ", isNPVR=" + isNPVR + ", isTVAnyTime="
					+ isTVAnyTime + ", startDateTime=" + startDateTime
					+ ", endDateTime=" + endDateTime + ", status=" + status
					+ ", channelName=" + channelName + "]";
		}
		
		

	}

	@Override
	public String toString() {
		return "Programs [programs=" + programs + ", totalResults="
				+ totalResults + ", restartAtToken=" + restartAtToken + "]";
	}
	
	
}
