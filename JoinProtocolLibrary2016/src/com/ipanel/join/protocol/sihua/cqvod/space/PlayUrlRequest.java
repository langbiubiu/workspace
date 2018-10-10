package com.ipanel.join.protocol.sihua.cqvod.space;

import java.io.Serializable;

import org.simpleframework.xml.Attribute;
import org.simpleframework.xml.Element;
import org.simpleframework.xml.Root;



@Root(name="message")
public class PlayUrlRequest implements Serializable{

	/**
	 * 
	 */
	private static final long serialVersionUID = 1222251507645510732L;
	@Attribute(required=false)
	private String version;
	@Element(required=false)
	private SpaceHeader header;
	@Element(required=false)
	private PlayUrlDetailRequestBody body;
	
	
	public String getVersion() {
		return version;
	}

	public void setVersion(String version) {
		this.version = version;
	}

	public SpaceHeader getHeader() {
		return header;
	}

	public void setHeader(SpaceHeader header) {
		this.header = header;
	}

	public PlayUrlDetailRequestBody getBody() {
		return body;
	}

	public void setBody(PlayUrlDetailRequestBody body) {
		this.body = body;
	}

	@Root(name="body",strict=false)
	public static class PlayUrlDetailRequestBody implements Serializable{
		
		/**
		 * 
		 */
		private static final long serialVersionUID = -3421865371359247618L;
		@Element
		private PlayUrlDetailRequest request;
		public PlayUrlDetailRequest getRequest() {
			return request;
		}
		public void setRequest(PlayUrlDetailRequest request) {
			this.request = request;
		}
		
		
	}
	
	@Root(name="request",strict=false)
	public static class PlayUrlDetailRequest implements Serializable{
		
		/**
		 * 
		 */
		private static final long serialVersionUID = -2081909021549135104L;
		@Attribute(required=false)
		private String uuid;
		@Attribute(required=false)
		private String spId;
		@Attribute(required=false)
		private String appId;
		@Attribute(required=false)
		private String accessToken;
		@Attribute(required=false)
		private String playTerminalApp;
		@Attribute(required=false)
		private String contentID;
		@Attribute(required=false)
		private String isHD;
		@Attribute(required=false)
		private String codec;
		@Attribute(required=false)
		private String otherParam;
		@Attribute(required=false)
		private String isIPQAM;
		@Attribute(required=false)
		private String protocol;
		
		public String getUuid() {
			return uuid;
		}
		public void setUuid(String uuid) {
			this.uuid = uuid;
		}
		public String getSpId() {
			return spId;
		}
		public void setSpId(String spId) {
			this.spId = spId;
		}
		public String getAppId() {
			return appId;
		}
		public void setAppId(String appId) {
			this.appId = appId;
		}
		public String getAccessToken() {
			return accessToken;
		}
		public void setAccessToken(String accessToken) {
			this.accessToken = accessToken;
		}
		public String getPlayTerminalApp() {
			return playTerminalApp;
		}
		public void setPlayTerminalApp(String playTerminalApp) {
			this.playTerminalApp = playTerminalApp;
		}
		public String getContentID() {
			return contentID;
		}
		public void setContentID(String contentID) {
			this.contentID = contentID;
		}
		public String getIsHD() {
			return isHD;
		}
		public void setIsHD(String isHD) {
			this.isHD = isHD;
		}
		public String getCodec() {
			return codec;
		}
		public void setCodec(String codec) {
			this.codec = codec;
		}
		public String getOtherParam() {
			return otherParam;
		}
		public void setOtherParam(String otherParam) {
			this.otherParam = otherParam;
		}
		public String getIsIPQAM() {
			return isIPQAM;
		}
		public void setIsIPQAM(String isIPQAM) {
			this.isIPQAM = isIPQAM;
		}
		public String getProtocol() {
			return protocol;
		}
		public void setProtocol(String protocol) {
			this.protocol = protocol;
		}
		
	}
	
}
