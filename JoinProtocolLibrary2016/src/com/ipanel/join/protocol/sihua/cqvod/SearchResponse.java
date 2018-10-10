package com.ipanel.join.protocol.sihua.cqvod;

import java.io.Serializable;
import java.util.List;

import org.simpleframework.xml.Attribute;
import org.simpleframework.xml.Element;
import org.simpleframework.xml.ElementList;
import org.simpleframework.xml.Root;
@Root(name="message",strict=false)
public class SearchResponse implements Serializable {

	/**
	 * 
	 */
	private static final long serialVersionUID = 8213977579298788145L;
	@Attribute(required = false)
	private String module;
	@Attribute(required = false)
	private String version;
	@Element(required=false)
	private Header header;
	@Element(required=false,name="body")
	private SearchResponseBody searchResponseBody;
	
	
	
	@Override
	public String toString() {
		return "SearchResponse [module=" + module + ", version=" + version + ", header=" + header
				+ ", searchResponseBody=" + searchResponseBody + "]";
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
	
	public SearchResponseBody getSearchResponseBody() {
		return searchResponseBody;
	}
	public void setSearchResponseBody(SearchResponseBody searchResponseBody) {
		this.searchResponseBody = searchResponseBody;
	}

	@Root(name="body")
	public static class SearchResponseBody implements Serializable{

		/**
		 * 
		 */
		private static final long serialVersionUID = 4909523776092484446L;
		@Element(required=false,name="contents")
		private SearchResponseContents searchResponseContents;
		@Element(required=false,name="result")
		private SearchResponseResult result;
		public SearchResponseContents getSearchResponseContents() {
			return searchResponseContents;
		}
		public void setSearchResponseContents(SearchResponseContents searchResponseContents) {
			this.searchResponseContents = searchResponseContents;
		}
		public SearchResponseResult getResult() {
			return result;
		}
		public void setResult(SearchResponseResult result) {
			this.result = result;
		}
		@Override
		public String toString() {
			return "SearchResponseBody [searchResponseContents=" + searchResponseContents + ", result=" + result + "]";
		}
		
		
	}
	@Root(name="content")
	public static class SearchResponseContent implements Serializable{
		/**
		 * 
		 */
		private static final long serialVersionUID = 1959264588748243834L;
		@Element(required=false)
		private String code;
		@Element(required=false)
		private String type;
		@Element(required=false)
		private String name;
		@Element(required=false)
		private String url;
		@Element(required=false,name="sort-index")
		private String sortIndex;
		@Element(required=false,name="folder-codes")
		private String folderCodes;
		@Element(required=false,name="creat-time")
		private String createTime;
		public String getCode() {
			return code;
		}
		public void setCode(String code) {
			this.code = code;
		}
		public String getType() {
			return type;
		}
		public void setType(String type) {
			this.type = type;
		}
		public String getName() {
			return name;
		}
		public void setName(String name) {
			this.name = name;
		}
		public String getUrl() {
			return url;
		}
		public void setUrl(String url) {
			this.url = url;
		}
		public String getSortIndex() {
			return sortIndex;
		}
		public void setSortIndex(String sortIndex) {
			this.sortIndex = sortIndex;
		}
		public String getFolderCodes() {
			return folderCodes;
		}
		public void setFolderCodes(String folderCodes) {
			this.folderCodes = folderCodes;
		}
		public String getCreateTime() {
			return createTime;
		}
		public void setCreateTime(String createTime) {
			this.createTime = createTime;
		}
		@Override
		public String toString() {
			return "SearchResponseContent [code=" + code + ", type=" + type + ", name=" + name + ", url=" + url
					+ ", sortIndex=" + sortIndex + ", folderCodes=" + folderCodes + ", createTime=" + createTime + "]";
		}
		
		
	}
	@Root(name="contents")
	public static class SearchResponseContents  implements Serializable{

		/**
		 * 
		 */
		private static final long serialVersionUID = -4633254833614702340L;
		@ElementList(inline=true,required=false)
		private List<SearchResponseContent> listSearchResponseContents;
		
		@Attribute(required=false)
		private String items;
		@Attribute(required=false,name="total-pages")
		private String totalPages;
		@Attribute(required=false,name="page-index")
		private String pageIndex;
		public List<SearchResponseContent> getListSearchResponseContents() {
			return listSearchResponseContents;
		}
		public void setListSearchResponseContents(List<SearchResponseContent> listSearchResponseContents) {
			this.listSearchResponseContents = listSearchResponseContents;
		}
		public String getItems() {
			return items;
		}
		public void setItems(String items) {
			this.items = items;
		}
		public String getTotalPages() {
			return totalPages;
		}
		public void setTotalPages(String totalPages) {
			this.totalPages = totalPages;
		}
		public String getPageIndex() {
			return pageIndex;
		}
		public void setPageIndex(String pageIndex) {
			this.pageIndex = pageIndex;
		}
		@Override
		public String toString() {
			return "SearchResponseContents [listSearchResponseContents=" + listSearchResponseContents + ", items="
					+ items + ", totalPages=" + totalPages + ", pageIndex=" + pageIndex + "]";
		}
	}
	
	@Root(name = "result")
	public static class SearchResponseResult implements Serializable {

		/**
		 * 
		 */
		private static final long serialVersionUID = -5626140161063076170L;
		@Attribute(required = false)
		private String code;
		@Attribute(required = false)
		private String description;

		public String getCode() {
			return code;
		}

		public void setCode(String code) {
			this.code = code;
		}

		public String getDescription() {
			return description;
		}

		public void setDescription(String description) {
			this.description = description;
		}

		@Override
		public String toString() {
			return "Result [code=" + code + ", description=" + description + "]";
		}

	}
	

}
