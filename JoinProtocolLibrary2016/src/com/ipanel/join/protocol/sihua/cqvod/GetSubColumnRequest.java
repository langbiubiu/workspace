package com.ipanel.join.protocol.sihua.cqvod;

import java.io.Serializable;

import org.simpleframework.xml.Attribute;
import org.simpleframework.xml.Element;
import org.simpleframework.xml.Root;
@Root(name="message")
public class GetSubColumnRequest implements Serializable{

	/**
	 * 
	 */
	private static final long serialVersionUID = 6820159534027986935L;
	@Attribute(required=false,name="module")
	private String module;
	@Attribute(required=false,name="version")
	private String version;
	@Element(required=false,name="header")
	private Header header;
	@Element(required=false,name="body")
	private SubColumnBody body;
	
	
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
	public SubColumnBody getBody() {
		return body;
	}
	public void setBody(SubColumnBody body) {
		this.body = body;
	}
	@Root(name="folder")
	public static class SubColumnFolder implements Serializable{

		/**
		 * 
		 */
		private static final long serialVersionUID = -763802310743128870L;
		@Element(required=false)
		private String code ;
		@Element(required=false,name="site-code")
		private String siteCode;
		@Element(required=false,name="page-index")
		private String pageIndex;
		@Element(required=false,name="page-items")
		private String pageItems;
		public String getCode() {
			return code;
		}
		public void setCode(String code) {
			this.code = code;
		}
		public String getSiteCode() {
			return siteCode;
		}
		public void setSiteCode(String siteCode) {
			this.siteCode = siteCode;
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
			return "Folder [code=" + code + ", siteCode=" + siteCode + ", pageIndex=" + pageIndex + ", pageItems="
					+ pageItems + "]";
		}
		
		
	}
	@Root(name="folders")
	public static class SubColumnFolders implements Serializable{

		/**
		 * 
		 */
		private static final long serialVersionUID = -5577854498023954997L;
		@Element(required=false,name="folder")
		private SubColumnFolder folder;
		public SubColumnFolder getFolder() {
			return folder;
		}
		public void setFolder(SubColumnFolder folder) {
			this.folder = folder;
		}
		@Override
		public String toString() {
			return "Folders [folder=" + folder + "]";
		}
		
	}
	@Root(name="body")
	public static  class SubColumnBody implements Serializable{

		/**
		 * 
		 */
		private static final long serialVersionUID = -3611959830038280959L;
		@Attribute(name="user-token",required=false)
		private String userToken;
		@Element(required=false,name="folders")
		private SubColumnFolders folders;
		public String getUserToken() {
			return userToken;
		}
		public void setUserToken(String userToken) {
			this.userToken = userToken;
		}
		public SubColumnFolders getFolders() {
			return folders;
		}
		public void setFolders(SubColumnFolders folders) {
			this.folders = folders;
		}
		@Override
		public String toString() {
			return "Body [userToken=" + userToken + ", folders=" + folders + "]";
		}
		
		
	}
	 

}
