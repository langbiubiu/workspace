package com.ipanel.join.protocol.sihua.cqvod;

import java.io.Serializable;
import java.util.List;

import org.simpleframework.xml.Attribute;
import org.simpleframework.xml.Element;
import org.simpleframework.xml.ElementList;
import org.simpleframework.xml.Root;

@Root(name = "message", strict = false)
public class SubColumnResponse implements Serializable {

	/**
	 * 
	 */
	private static final long serialVersionUID = -1101085220919249487L;

	@Attribute(required = false)
	private String module;
	@Attribute(required = false)
	private String version;
	@Element(required = false)
	private Header header;
	@Element(required = false)
	private SubColumnResponseBody body;
	

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

	public SubColumnResponseBody getBody() {
		return body;
	}

	public void setBody(SubColumnResponseBody body) {
		this.body = body;
	}

	@Override
	public String toString() {
		return "SubColumnResponse [module=" + module + ", version=" + version + ", header=" + header + ", body=" + body
				+ "]";
	}

	public static class SubColumnResponseBody implements Serializable {

		/**
		 * 
		 */
		private static final long serialVersionUID = 7943839439627612981L;
		@Element(required = false, name = "result")
		private SubColumnResponseResult result;
		@Element(required = false, name = "folders")
		private SubColumnResponseFolders subColumnFolders;

		public SubColumnResponseResult getResult() {
			return result;
		}

		public void setResult(SubColumnResponseResult result) {
			this.result = result;
		}

		public SubColumnResponseFolders getSubColumnFolders() {
			return subColumnFolders;
		}

		public void setSubColumnFolders(SubColumnResponseFolders subColumnFolders) {
			this.subColumnFolders = subColumnFolders;
		}

		@Override
		public String toString() {
			return "SubColumnResponseBody [result=" + result + ", subColumnFolders=" + subColumnFolders + "]";
		}
	}

	@Root(name = "result")
	public static class SubColumnResponseResult implements Serializable {

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

	@Root(name = "folders")
	public static class SubColumnResponseFolders implements Serializable {

		/**
		 * 
		 */
		private static final long serialVersionUID = 5064403513165220705L;
		@Attribute(name = "items", required = false)
		private String items;
		@Attribute(name = "total-pages", required = false)
		private String totalPages;
		@Attribute(name = "page-index", required = false)
		private String pageIndex;
		@ElementList(required = false, inline = true)
		List<SubColumnReponseFolder> lisSubColumbFolders;

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

		public List<SubColumnReponseFolder> getLisSubColumbFolders() {
			return lisSubColumbFolders;
		}

		public void setLisSubColumbFolders(List<SubColumnReponseFolder> lisSubColumbFolders) {
			this.lisSubColumbFolders = lisSubColumbFolders;
		}

		@Override
		public String toString() {
			return "SubColumnFolders [items=" + items + ", totalPages=" + totalPages + ", pageIndex=" + pageIndex
					+ ", lisSubColumbFolders=" + lisSubColumbFolders + "]";
		}

	}

	@Root(name = "folder")
	public static class SubColumnReponseFolder implements Serializable {

		/**
		 * 
		 */
		private static final long serialVersionUID = -8534299750840244288L;
		@Element(required = false)
		private String name;
		@Element(required = false)
		private String code;
		@Element(required = false, name = "site-code")
		private String sitecCode;
		@Element(required = false)
		private String type;
		@Element(required = false, name = "parent-folder-code")
		private String parentFolderCode;
		@Element(required = false, name = "with-content")
		private String withContent;
		@Element(required = false, name = "url")
		private String url;
		@Element(required = false, name = "icon-url")
		private String iconUrl;
		@Element(required = false, name = "sort-index")
		private String sortIndex;
		@Element(required = false, name = "description")
		private String description;

		public String getName() {
			return name;
		}

		public void setName(String name) {
			this.name = name;
		}

		public String getCode() {
			return code;
		}

		public void setCode(String code) {
			this.code = code;
		}

		public String getSitecCode() {
			return sitecCode;
		}

		public void setSitecCode(String sitecCode) {
			this.sitecCode = sitecCode;
		}

		public String getType() {
			return type;
		}

		public void setType(String type) {
			this.type = type;
		}

		public String getParentFolderCode() {
			return parentFolderCode;
		}

		public void setParentFolderCode(String parentFolderCode) {
			this.parentFolderCode = parentFolderCode;
		}

		public String getWithContent() {
			return withContent;
		}

		public void setWithContent(String withContent) {
			this.withContent = withContent;
		}

		public String getUrl() {
			return url;
		}

		public void setUrl(String url) {
			this.url = url;
		}

		public String getIconUrl() {
			return iconUrl;
		}

		public void setIconUrl(String iconUrl) {
			this.iconUrl = iconUrl;
		}

		public String getSortIndex() {
			return sortIndex;
		}

		public void setSortIndex(String sortIndex) {
			this.sortIndex = sortIndex;
		}

		public String getDescription() {
			return description;
		}

		public void setDescription(String description) {
			this.description = description;
		}

		@Override
		public String toString() {
			return "SubColumbFolder [name=" + name + ", code=" + code + ", sitecCode=" + sitecCode + ", type=" + type
					+ ", parentFolderCode=" + parentFolderCode + ", withContent=" + withContent + ", url=" + url
					+ ", iconUrl=" + iconUrl + ", sortIndex=" + sortIndex + ", description=" + description + "]";
		}

	}
}
