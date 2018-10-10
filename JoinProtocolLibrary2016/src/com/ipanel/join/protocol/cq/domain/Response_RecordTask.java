package com.ipanel.join.protocol.cq.domain;

import java.io.Serializable;
import java.util.List;

import org.simpleframework.xml.Attribute;
import org.simpleframework.xml.Element;
import org.simpleframework.xml.ElementList;
import org.simpleframework.xml.Root;

@Root(name = "message")
public class Response_RecordTask implements Serializable {

	/**
	 * 
	 */
	private static final long serialVersionUID = -5041654543764212446L;

	@Element(required = true)
	private Header header;
	@Element(required = true)
	private Body body;
	@Attribute(required = false)
	private String version="1.0";

	public String getVersion() {
		return version;
	}

	public void setVersion(String version) {
		this.version = version;
	}

	public Header getHeader() {
		return header;
	}

	public void setHeader(Header header) {
		this.header = header;
	}

	public Body getBody() {
		return body;
	}

	public void setBody(Body body) {
		this.body = body;
	}

	@Root(name = "body")
	public static class Body implements Serializable {

		/**
		 * 
		 */
		private static final long serialVersionUID = 2450443024903188874L;

		@Element(required = false)
		private Tasks tasks;

		public Tasks getTaks() {
			return tasks;
		}

		public void setTaks(Tasks tasks) {
			this.tasks = tasks;
		}



	}

	@Root(name = "tasks")
	public static class Tasks implements Serializable {


		/**
		 * 
		 */
		private static final long serialVersionUID = 1551569179250262398L;
		@Attribute(required = false)
		private String AppID;
		@Attribute(required = false)
		private String Description;
		@Attribute(required = false)
		private String ResultCode;
		@Attribute(required = false)
		private String SPID;
		@Attribute(required = false)
		private String UUID;
		@Attribute(required = false)
		private String TotalCount;
		@Attribute(required = false)
		private String Type;
		@Attribute(required = false)
		private String Count;
		@ElementList(inline = true,required = false, entry = "taskInfo")
		private List<TaskInfo> taskinfolist;

		
		
		public String getUUID() {
			return UUID;
		}

		public void setUUID(String uUID) {
			UUID = uUID;
		}

		public String getAppID() {
			return AppID;
		}

		public void setAppID(String appID) {
			AppID = appID;
		}

		public String getDescription() {
			return Description;
		}

		public void setDescription(String description) {
			Description = description;
		}

		public String getResultCode() {
			return ResultCode;
		}

		public void setResultCode(String resultCode) {
			ResultCode = resultCode;
		}

		public String getSPID() {
			return SPID;
		}

		public void setSPID(String sPID) {
			SPID = sPID;
		}

		public String getTotalCount() {
			return TotalCount;
		}

		public void setTotalCount(String totalCount) {
			TotalCount = totalCount;
		}

		public String getType() {
			return Type;
		}

		public void setType(String type) {
			Type = type;
		}


		public List<TaskInfo> getTaskinfolist() {
			return taskinfolist;
		}

		public void setTaskinfolist(List<TaskInfo> taskinfolist) {
			this.taskinfolist = taskinfolist;
		}

		public String getCount() {
			return Count;
		}

		public void setCount(String count) {
			Count = count;
		}

		
	}


	@Root(name = "taskInfo")
	public static class TaskInfo implements Serializable {
		/**
		 * 
		 */
		private static final long serialVersionUID = -1926746617805846217L;

		@Attribute(required = false)
		private String OrderCode;
		@Attribute(required = false)
		private String ContentType;
		@Attribute(required = false)
		private String ChannelID;
		@Attribute(required = false)
		private String ChannelName;
		@Attribute(required = false)
		private String ProgramID;
		@Attribute(required = false)
		private String ProgramName;
		@Attribute(required = false)
		private String RecordType;
		@Attribute(required = false)
		private String BeginTime;
		@Attribute(required = false)
		private String EndTime;
		@Attribute(required = false)
		private String CreateTime;
		@Attribute(required = false)
		private String UpdateTime;
		@Attribute(required = false)
		private String Status;
		@Attribute(required = false)
		private String Size;
		@Attribute(required = false)
		private String ContentID;
		@Attribute(required = false)
		private String ContentName;

	

		public String getContentType() {
			return ContentType;
		}

		public void setContentType(String contentType) {
			ContentType = contentType;
		}

		public String getBeginTime() {
			return BeginTime;
		}

		public void setBeginTime(String beginTime) {
			BeginTime = beginTime;
		}

		public String getChannelID() {
			return ChannelID;
		}

		public String getChannelName() {
			return ChannelName;
		}

		public void setChannelName(String channelName) {
			ChannelName = channelName;
		}

		public void setChannelID(String channelID) {
			ChannelID = channelID;
		}

		public String getContentID() {
			return ContentID;
		}

		public void setContentID(String contentID) {
			ContentID = contentID;
		}

		public String getContentName() {
			return ContentName;
		}

		public void setContentName(String contentName) {
			ContentName = contentName;
		}

		public String getCreateTime() {
			return CreateTime;
		}

		public void setCreateTime(String createTime) {
			CreateTime = createTime;
		}

		public String getEndTime() {
			return EndTime;
		}

		public void setEndTime(String endTime) {
			EndTime = endTime;
		}

		public String getOrderCode() {
			return OrderCode;
		}

		public void setOrderCode(String orderCode) {
			OrderCode = orderCode;
		}

		public String getProgramID() {
			return ProgramID;
		}

		public void setProgramID(String programID) {
			ProgramID = programID;
		}

		public String getProgramName() {
			return ProgramName;
		}

		public void setProgramName(String programName) {
			ProgramName = programName;
		}

		public String getRecordType() {
			return RecordType;
		}

		public void setRecordType(String recordType) {
			RecordType = recordType;
		}

		public String getSize() {
			return Size;
		}

		public void setSize(String size) {
			Size = size;
		}

		public String getStatus() {
			return Status;
		}

		public void setStatus(String status) {
			Status = status;
		}

		public String getUpdateTime() {
			return UpdateTime;
		}

		public void setUpdateTime(String updateTime) {
			UpdateTime = updateTime;
		}
		
	}
}
