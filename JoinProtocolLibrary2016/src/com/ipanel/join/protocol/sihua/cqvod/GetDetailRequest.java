package com.ipanel.join.protocol.sihua.cqvod;

import java.io.Serializable;

import org.simpleframework.xml.Attribute;
import org.simpleframework.xml.Element;
import org.simpleframework.xml.Root;

@Root(name="message")
public class GetDetailRequest implements Serializable {

	/**
	 * 
	 */
	private static final long serialVersionUID = 198312899986594981L;
	@Attribute(required=false)
	private String module;
	@Attribute(required=false)
	private String version;
	@Element(required=false)
	private Header header;
	@Element(required=false)
	private DetailRequestBody body;
	
	
	
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
	public DetailRequestBody getBody() {
		return body;
	}
	public void setBody(DetailRequestBody body) {
		this.body = body;
	}
	
	
	@Override
	public String toString() {
		return "GetPlayUrlRequest [module=" + module + ", version=" + version + ", header=" + header + ", body=" + body
				+ "]";
	}


	@Root(name="content",strict=false)
	public static class DetailRequestContent implements Serializable{

		/**
		 * 
		 */
		private static final long serialVersionUID = -7780041527856904694L;
		@Element(name="site-code",required=false)
		private String siteCode;
		@Element(name="folder-code",required=false)
		private String folderCode;
		@Element(required=false)
		private String code;
		@Element(name="items-index",required=false)
		private String itemIndex;
		@Element(required=false,name="user-id")
		private String userId;
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
		public String getCode() {
			return code;
		}
		public void setCode(String code) {
			this.code = code;
		}
		public String getItemIndex() {
			return itemIndex;
		}
		public void setItemIndex(String itemIndex) {
			this.itemIndex = itemIndex;
		}
		public String getUserId() {
			return userId;
		}
		public void setUserId(String userId) {
			this.userId = userId;
		}
		@Override
		public String toString() {
			return "Content [siteCode=" + siteCode + ", folderCode=" + folderCode + ", code=" + code + ", itemIndex="
					+ itemIndex + ", userId=" + userId + "]";
		}
		
		
	}
	@Root(name="contents")
	public static class DetailRequestContents implements Serializable{

		/**
		 * 
		 */
		private static final long serialVersionUID = 4383070684522159558L;
		@Element
		private DetailRequestContent content;
		public DetailRequestContent getContent() {
			return content;
		}
		public void setContent(DetailRequestContent content) {
			this.content = content;
		}
		@Override
		public String toString() {
			return "Contents [content=" + content + "]";
		}
		
		
	}
	@Root(name="body",strict=false)
	public static class DetailRequestBody implements Serializable{

		/**
		 * 
		 */
		private static final long serialVersionUID = 1474309287395702974L;
		@Attribute(name="user-token",required=false)
		private String userToken;
		@Element
		private DetailRequestContents contents;
		public String getUserToken() {
			return userToken;
		}
		public void setUserToken(String userToken) {
			this.userToken = userToken;
		}
		public DetailRequestContents getContents() {
			return contents;
		}
		public void setContents(DetailRequestContents contents) {
			this.contents = contents;
		}
		@Override
		public String toString() {
			return "Body [userToken=" + userToken + ", contents=" + contents + "]";
		}
		
		
	}

}
