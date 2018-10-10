package com.ipanel.join.protocol.sihua.cqvod.space;

import java.io.Serializable;

import org.simpleframework.xml.Attribute;
import org.simpleframework.xml.Element;
import org.simpleframework.xml.Root;



@Root(name="message")
public class ContentRequest implements Serializable{

	/**
	 * 
	 */
	private static final long serialVersionUID = 1222251507645510732L;
	@Attribute(required=false)
	private String version;
	@Element(required=false)
	private SpaceHeader header;
	@Element(required=false)
	private ContentDetailRequestBody body;
	
	
	public String getVersion() {
		return version;
	}

	public void setVersion(String version) {
		this.version = version;
	}

	public SpaceHeader getHeader() {
		return header;
	}

	public void setHeader(SpaceHeader header) {
		this.header = header;
	}

	public ContentDetailRequestBody getBody() {
		return body;
	}

	public void setBody(ContentDetailRequestBody body) {
		this.body = body;
	}

	@Root(name="body",strict=false)
	public static class ContentDetailRequestBody implements Serializable{
		
		/**
		 * 
		 */
		private static final long serialVersionUID = -3421865371359247618L;
		@Element
		private ContentDetailRequestQuery query;
		public ContentDetailRequestQuery getQuery() {
			return query;
		}
		public void setQuery(ContentDetailRequestQuery query) {
			this.query = query;
		}
		
	}
	
	@Root(name="query",strict=false)
	public static class ContentDetailRequestQuery implements Serializable{
		
		/**
		 * 
		 */
		private static final long serialVersionUID = -2081909021549135104L;
		@Attribute(required=false)
		private String UUID;
		@Attribute(required=false)
		private String SPID;
		@Attribute(required=false)
		private String AppID;
		@Attribute(required=false)
		private String AccessToken;
		@Attribute(required=false)
		private String Type;
		@Attribute(required=false)
		private String FileType;
		@Attribute(required=false)
		private String sortType;
		@Attribute(required=false)
		private String Status;
		@Attribute(required=false)
		private String PageNo;
		@Attribute(required=false)
		private String PageSize;
		public String getUUID() {
			return UUID;
		}
		public void setUUID(String uUID) {
			UUID = uUID;
		}
		public String getSPID() {
			return SPID;
		}
		public void setSPID(String sPID) {
			SPID = sPID;
		}
		public String getAppID() {
			return AppID;
		}
		public void setAppID(String appID) {
			AppID = appID;
		}
		public String getAccessToken() {
			return AccessToken;
		}
		public void setAccessToken(String accessToken) {
			AccessToken = accessToken;
		}
		public String getType() {
			return Type;
		}
		public void setType(String type) {
			Type = type;
		}
		public String getFileType() {
			return FileType;
		}
		public void setFileType(String fileType) {
			FileType = fileType;
		}
		
		
		public String getSortType() {
			return sortType;
		}
		public void setSortType(String sortType) {
			this.sortType = sortType;
		}
		public String getStatus() {
			return Status;
		}
		public void setStatus(String status) {
			Status = status;
		}
		public String getPageNo() {
			return PageNo;
		}
		public void setPageNo(String pageNo) {
			PageNo = pageNo;
		}
		public String getPageSize() {
			return PageSize;
		}
		public void setPageSize(String pageSize) {
			PageSize = pageSize;
		}
				
		
	}
	
}
