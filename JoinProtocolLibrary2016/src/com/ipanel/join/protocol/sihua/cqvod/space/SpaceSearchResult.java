package com.ipanel.join.protocol.sihua.cqvod.space;

import java.io.Serializable;
import java.util.List;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

@SuppressWarnings("serial")
public class SpaceSearchResult implements Serializable{

	@Expose
	@SerializedName("header")
	private SearchResultHeader header;
	@Expose
	@SerializedName("body")
	private SearchResultBody body;
		
	public SearchResultHeader getHeader() {
		return header;
	}

	public void setHeader(SearchResultHeader header) {
		this.header = header;
	}

	public SearchResultBody getBody() {
		return body;
	}

	public void setBody(SearchResultBody body) {
		this.body = body;
	}

	public class SearchResultHeader implements Serializable {
		
	}
	
	public class SearchResultBody implements Serializable {
		@Expose
		@SerializedName("result")
		private SearchResult result;
		@Expose
		@SerializedName("canPlaycontents")
		private List<canPlaycontent> canPlaycontents;
		@Expose
		@SerializedName("needOrdercontents")
		private List<needOrdercontent> needOrdercontents;
		public SearchResult getResult() {
			return result;
		}
		public void setResult(SearchResult result) {
			this.result = result;
		}
		public List<canPlaycontent> getCanPlaycontents() {
			return canPlaycontents;
		}
		public void setCanPlaycontents(List<canPlaycontent> canPlaycontents) {
			this.canPlaycontents = canPlaycontents;
		}
		public List<needOrdercontent> getNeedOrdercontents() {
			return needOrdercontents;
		}
		public void setNeedOrdercontents(List<needOrdercontent> needOrdercontents) {
			this.needOrdercontents = needOrdercontents;
		}

		
	}
	
	public class SearchResult implements Serializable {
		@Expose
		private int code;
		@Expose
		private String description;
		public int getCode() {
			return code;
		}
		public void setCode(int code) {
			this.code = code;
		}
		public String getDescription() {
			return description;
		}
		public void setDescription(String description) {
			this.description = description;
		}
			
	}
	
	public class canPlaycontent implements Serializable {
		@Expose
		private String contentID;
		@Expose
		private String contentName;
		@Expose
		private String imageUrl;
		@Expose
		private String actor;
		@Expose
		private String director;
		@Expose
		private String showType;
		@Expose
		private String summaryMedium;
		@Expose
		private String summaryShort;
		@Expose
		private String category;
		@Expose
		private String keyword;
		public String getContentID() {
			return contentID;
		}
		public void setContentID(String contentID) {
			this.contentID = contentID;
		}
		public String getContentName() {
			return contentName;
		}
		public void setContentName(String contentName) {
			this.contentName = contentName;
		}
		public String getImageUrl() {
			return imageUrl;
		}
		public void setImageUrl(String imageUrl) {
			this.imageUrl = imageUrl;
		}
		public String getActor() {
			return actor;
		}
		public void setActor(String actor) {
			this.actor = actor;
		}
		public String getDirector() {
			return director;
		}
		public void setDirector(String director) {
			this.director = director;
		}
		public String getShowType() {
			return showType;
		}
		public void setShowType(String showType) {
			this.showType = showType;
		}
		public String getSummaryMedium() {
			return summaryMedium;
		}
		public void setSummaryMedium(String summaryMedium) {
			this.summaryMedium = summaryMedium;
		}
		public String getSummaryShort() {
			return summaryShort;
		}
		public void setSummaryShort(String summaryShort) {
			this.summaryShort = summaryShort;
		}
		public String getCategory() {
			return category;
		}
		public void setCategory(String category) {
			this.category = category;
		}
		public String getKeyword() {
			return keyword;
		}
		public void setKeyword(String keyword) {
			this.keyword = keyword;
		}
		
		
	}
	
	public class needOrdercontent implements Serializable {
		@Expose
		private String contentID;
		@Expose
		private String programId;
		@Expose
		private String channelId;
		@Expose
		private String startTime;
		@Expose
		private String endTime;
		@Expose
		private String contentName;
		@Expose
		private String imageUrl;
		@Expose
		private String actor;
		@Expose
		private String director;
		@Expose
		private String showType;
		@Expose
		private String summaryMedium;
		@Expose
		private String summaryShort;
		@Expose
		private String category;
		@Expose
		private String keyword;
		public String getContentID() {
			return contentID;
		}
		public void setContentID(String contentID) {
			this.contentID = contentID;
		}
		public String getProgramId() {
			return programId;
		}
		public void setProgramId(String programId) {
			this.programId = programId;
		}
		public String getChannelId() {
			return channelId;
		}
		public void setChannelId(String channelId) {
			this.channelId = channelId;
		}
		public String getStartTime() {
			return startTime;
		}
		public void setStartTime(String startTime) {
			this.startTime = startTime;
		}
		public String getEndTime() {
			return endTime;
		}
		public void setEndTime(String endTime) {
			this.endTime = endTime;
		}
		public String getContentName() {
			return contentName;
		}
		public void setContentName(String contentName) {
			this.contentName = contentName;
		}
		public String getImageUrl() {
			return imageUrl;
		}
		public void setImageUrl(String imageUrl) {
			this.imageUrl = imageUrl;
		}
		public String getActor() {
			return actor;
		}
		public void setActor(String actor) {
			this.actor = actor;
		}
		public String getDirector() {
			return director;
		}
		public void setDirector(String director) {
			this.director = director;
		}
		public String getShowType() {
			return showType;
		}
		public void setShowType(String showType) {
			this.showType = showType;
		}
		public String getSummaryMedium() {
			return summaryMedium;
		}
		public void setSummaryMedium(String summaryMedium) {
			this.summaryMedium = summaryMedium;
		}
		public String getSummaryShort() {
			return summaryShort;
		}
		public void setSummaryShort(String summaryShort) {
			this.summaryShort = summaryShort;
		}
		public String getCategory() {
			return category;
		}
		public void setCategory(String category) {
			this.category = category;
		}
		public String getKeyword() {
			return keyword;
		}
		public void setKeyword(String keyword) {
			this.keyword = keyword;
		}
		
		
	}
	
	
	
}
