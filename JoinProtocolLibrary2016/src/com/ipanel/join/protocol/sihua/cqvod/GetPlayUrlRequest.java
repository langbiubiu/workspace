package com.ipanel.join.protocol.sihua.cqvod;

import java.io.Serializable;

import org.simpleframework.xml.Attribute;
import org.simpleframework.xml.Element;
import org.simpleframework.xml.Root;

@Root(name="message")
public class GetPlayUrlRequest implements Serializable {

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
	private Body body;
	
	
	
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
	public Body getBody() {
		return body;
	}
	public void setBody(Body body) {
		this.body = body;
	}
	
	
	@Override
	public String toString() {
		return "GetPlayUrlRequest [module=" + module + ", version=" + version + ", header=" + header + ", body=" + body
				+ "]";
	}


	@Root(name="content",strict=false)
	public static class Content implements Serializable{

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
		@Element(required=false)
		private String format;
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
		public String getFormat() {
			return format;
		}
		public void setFormat(String format) {
			this.format = format;
		}
		@Override
		public String toString() {
			return "Content [siteCode=" + siteCode + ", folderCode=" + folderCode + ", code=" + code + ", itemIndex="
					+ itemIndex + ", format=" + format + "]";
		}
		
	}
	@Root(name="contents")
	public static class Contents implements Serializable{

		/**
		 * 
		 */
		private static final long serialVersionUID = 4383070684522159558L;
		@Element
		private Content content;
		public Content getContent() {
			return content;
		}
		public void setContent(Content content) {
			this.content = content;
		}
		@Override
		public String toString() {
			return "Contents [content=" + content + "]";
		}
		
		
	}
	@Root(name="body",strict=false)
	public static class Body implements Serializable{

		/**
		 * 
		 */
		private static final long serialVersionUID = 1474309287395702974L;
		@Attribute(name="user-token",required=false)
		private String userToken;
		@Element
		private Contents contents;
		public String getUserToken() {
			return userToken;
		}
		public void setUserToken(String userToken) {
			this.userToken = userToken;
		}
		public Contents getContents() {
			return contents;
		}
		public void setContents(Contents contents) {
			this.contents = contents;
		}
		@Override
		public String toString() {
			return "Body [userToken=" + userToken + ", contents=" + contents + "]";
		}
		
		
	}

}
