package com.ipanel.join.protocol.sihua.cqvod;

import java.io.Serializable;

import org.simpleframework.xml.Attribute;
import org.simpleframework.xml.Element;
import org.simpleframework.xml.Root;

@Root(name = "message", strict = false)
public class getSearchRequest implements Serializable {

	/**
	 * 
	 */
	private static final long serialVersionUID = -7956314728072692657L;
	@Attribute(required = false)
	private String module;
	@Attribute(required = false)
	private String version;
	@Element(required=false)
	private Header header;
	@Element(required=false,name="body")
	private getSearchRequestBody getSearchRequestBody;
	
	
	@Override
	public String toString() {
		return "getSearchRequest [module=" + module + ", version=" + version + ", header=" + header
				+ ", getSearchRequestBody=" + getSearchRequestBody + "]";
	}

	public String getModule() {
		return module;
	}

	public void setModule(String module) {
		this.module = module;
	}

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

	public getSearchRequestBody getGetSearchRequestBody() {
		return getSearchRequestBody;
	}

	public void setGetSearchRequestBody(getSearchRequestBody getSearchRequestBody) {
		this.getSearchRequestBody = getSearchRequestBody;
	}

	@Root(name = "searcher", strict = false)
	public static class Searcher implements Serializable {

		/**
		 * 
		 */
		private static final long serialVersionUID = -4047106335359384056L;
		@Element(name = "site-code", required = false)
		private String siteCode;
		@Element(name = "folder-code", required = false)
		private String folderCode;
		@Element(name = "search-type", required = false)
		private String searchType;
		@Element(name = "search-condition", required = false)
		private String searchCondition;
		@Element(name = "sort-type", required = false)
		private String sortType;
		@Element(name = "page-index", required = false)
		private String pageIndex;
		@Element(name = "page-items", required = false)
		private String pageItems;

		public String getSiteCode() {
			return siteCode;
		}

		public void setSiteCode(String siteCode) {
			this.siteCode = siteCode;
		}

		public String getFolderCode() {
			return folderCode;
		}

		public void setFolderCode(String folderCode) {
			this.folderCode = folderCode;
		}

		public String getSearchType() {
			return searchType;
		}

		public void setSearchType(String searchType) {
			this.searchType = searchType;
		}

		public String getSearchCondition() {
			return searchCondition;
		}

		public void setSearchCondition(String searchCondition) {
			this.searchCondition = searchCondition;
		}

		public String getSortType() {
			return sortType;
		}

		public void setSortType(String sortType) {
			this.sortType = sortType;
		}

		public String getPageIndex() {
			return pageIndex;
		}

		public void setPageIndex(String pageIndex) {
			this.pageIndex = pageIndex;
		}

		public String getPageItems() {
			return pageItems;
		}

		public void setPageItems(String pageItems) {
			this.pageItems = pageItems;
		}

		@Override
		public String toString() {
			return "Searcher [siteCode=" + siteCode + ", folderCode=" + folderCode + ", searchType=" + searchType
					+ ", searchCondition=" + searchCondition + ", sortType=" + sortType + ", pageIndex=" + pageIndex
					+ ", pageItems=" + pageItems + "]";
		}

	}
	
	@Root(name="body")
	public static class getSearchRequestBody implements Serializable{

		/**
		 * 
		 */
		private static final long serialVersionUID = -4621400853859423575L;
		@Attribute(name="user-token",required=false)
		private String userToken;
		@Element(required=false)
		private Searcher searcher;
		public String getUserToken() {
			return userToken;
		}
		public void setUserToken(String userToken) {
			this.userToken = userToken;
		}
		public Searcher getSearcher() {
			return searcher;
		}
		public void setSearcher(Searcher searcher) {
			this.searcher = searcher;
		}
		@Override
		public String toString() {
			return "getSearchRequestBody [userToken=" + userToken + ", searcher=" + searcher + "]";
		}
		
	}

}
