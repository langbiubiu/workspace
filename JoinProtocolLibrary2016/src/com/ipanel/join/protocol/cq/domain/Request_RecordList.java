package com.ipanel.join.protocol.cq.domain;

import java.io.Serializable;

import org.simpleframework.xml.Attribute;
import org.simpleframework.xml.Element;
import org.simpleframework.xml.Root;


@Root(name = "message")
public class Request_RecordList implements Serializable {

	/**
	 * 
	 */
	private static final long serialVersionUID = -6729508744568496517L;
	
	@Element(required = false)
	private Header header;
	@Element(required = false)
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
	
	public void setBody(Query query){
		Body body=new Body();
		body.setQuery(query);
		setBody(body);
	}

	@Root(name = "boty")
	public static class Body implements Serializable {
		/**
		 * 
		 */
		private static final long serialVersionUID = 6954026763982037355L;
		@Element(required = true)
		private Query query;

		public Query getQuery() {
			return query;
		}

		public void setQuery(Query query) {
			this.query = query;
		}
		
		
	}
	@Root(name = "query")
	public static class Query implements Serializable {
		/**
		 * 
		 */
		private static final long serialVersionUID = 3430342496779488766L;
		@Attribute(required = false)
		private String UUID;
		@Attribute(required = false)
		private String SPID;
		@Attribute(required = false)
		private String AppID;
		@Attribute(required = false)
		private String AccessToken;
		@Attribute(required = false)
		private String Status;
		@Attribute(required = false)
		private String Type;
		@Attribute(required = false)
		private String ContentSearchType;
		@Attribute(required = false)
		private String sortType;
		@Attribute(required = false)
		private String PageNo;
		@Attribute(required = false)
		private String PageSize;
		@Attribute(required = false)
		private String FileType;
		
		public String getFileType() {
			return FileType;
		}
		public void setFileType(String fileType) {
			FileType = fileType;
		}
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
		public String getStatus() {
			return Status;
		}
		public void setStatus(String status) {
			Status = status;
		}
		public String getType() {
			return Type;
		}
		public void setType(String type) {
			Type = type;
		}
		public String getContentSearchType() {
			return ContentSearchType;
		}
		public void setContentSearchType(String contentSearchType) {
			ContentSearchType = contentSearchType;
		}
		public String getSortType() {
			return sortType;
		}
		public void setSortType(String sortType) {
			this.sortType = sortType;
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
