package com.ipanel.join.protocol.sihua.cqvod;

import java.io.Serializable;

import org.simpleframework.xml.Attribute;
import org.simpleframework.xml.Element;
import org.simpleframework.xml.Root;

@Root(name = "message")
public class GetDoubanCoreNumRequest implements Serializable {

	/**
	 * 
	 */
	private static final long serialVersionUID = -4552206079769623619L;
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

	@Root(name = "body")
	public static class Body implements Serializable {

		/**
		 * 
		 */
		private static final long serialVersionUID = -5466094989113280160L;
		@Element(required=false)
		private doubanRating doubanRating;
		public doubanRating getDoubanRating() {
			return doubanRating;
		}
		public void setDoubanRating(doubanRating doubanRating) {
			this.doubanRating = doubanRating;
		}
		

	}

	@Root(name = "DoubanBind")
	public static class doubanRating implements Serializable {

		/**
		 * 
		 */
		private static final long serialVersionUID = -6135595491876083979L;
		@Attribute(required = false)
		private String contentId;
		
		public String getContentId() {
			return contentId;
		}
		public void setContentId(String contentId) {
			this.contentId = contentId;
		}
		public String getContentName() {
			return contentName;
		}
		public void setContentName(String contentName) {
			this.contentName = contentName;
		}
		public String getUuid() {
			return uuid;
		}
		public void setUuid(String uuid) {
			this.uuid = uuid;
		}
		@Attribute(required = false)
		private String contentName;
		@Attribute(required = false)
		private String uuid;
		@Attribute(required = false)
		private String token;
	
		public String getToken() {
			return token;
		}
		public void setToken(String token) {
			this.token = token;
		}
		@Override
		public String toString() {
			return "doubanRating [contentId=" + contentId + ", contentName=" + contentName + ", uuid=" + uuid
					+ ", token=" + token +"]";
		}
		

	}

}
