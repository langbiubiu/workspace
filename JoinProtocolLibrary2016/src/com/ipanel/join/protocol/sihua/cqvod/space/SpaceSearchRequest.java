package com.ipanel.join.protocol.sihua.cqvod.space;

import java.io.Serializable;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

public class SpaceSearchRequest implements Serializable{

	/**
	 * 
	 */
	private static final long serialVersionUID = -5806959753381548474L;
	@Expose
	@SerializedName("header")
	private SearchHeader header;
	@Expose
	@SerializedName("body")
	private SearchBody body;
		
	public SearchHeader getHeader() {
		return header;
	}

	public void setHeader(SearchHeader header) {
		this.header = header;
	}

	public SearchBody getBody() {
		return body;
	}

	public void setBody(SearchBody body) {
		this.body = body;
	}

	public static class SearchHeader implements Serializable {
		/**
		 * 
		 */
		private static final long serialVersionUID = -8474468163633324166L;

		public SearchHeader() {
			// TODO Auto-generated constructor stub
		}
	}
	
	public static class SearchBody implements Serializable {
		/**
		 * 
		 */
		private static final long serialVersionUID = -1119335107725610364L;
		@Expose
		@SerializedName("request")
		private SearchRequest request;

		public SearchRequest getRequest() {
			return request;
		}

		public void setRequest(SearchRequest request) {
			this.request = request;
		}
		
	}
	
	public static class SearchRequest implements Serializable {
		/**
		 * 
		 */
		private static final long serialVersionUID = -1065539093563631641L;
		@Expose
		private String uuid;
		@Expose
		private String searchType;
		@Expose
		private String matchType;
		@Expose
		private String searchName;
		@Expose
		private String otherParam;
		@Expose
		private String command;
		public String getUuid() {
			return uuid;
		}
		public void setUuid(String uuid) {
			this.uuid = uuid;
		}
		public String getSearchType() {
			return searchType;
		}
		public void setSearchType(String searchType) {
			this.searchType = searchType;
		}
		public String getMatchType() {
			return matchType;
		}
		public void setMatchType(String matchType) {
			this.matchType = matchType;
		}
		public String getSearchName() {
			return searchName;
		}
		public void setSearchName(String searchName) {
			this.searchName = searchName;
		}
		public String getOtherParam() {
			return otherParam;
		}
		public void setOtherParam(String otherParam) {
			this.otherParam = otherParam;
		}
		public String getCommand() {
			return command;
		}
		public void setCommand(String command) {
			this.command = command;
		}
		
		
	}
	
	
	
}
