package com.ipanel.join.protocol.sihua.cqvod.space;

import java.io.Serializable;

import org.simpleframework.xml.Attribute;
import org.simpleframework.xml.Element;
import org.simpleframework.xml.Root;

//<message version="1.0">
//<header CorrelateID="1000002" RequestSystemID="iSpace" TargetSystemID="NPVR" Command="ISPACE_USER_CONTENT_DELETE" Action="REQUEST" Timestamp="2014-01-29 01:12:22" />
//<body>
//  <request Action="LogicDelete" UUID="1200005546" SPID="sp_00001" AppID="app_00001" AccessToken="">
//    <Contents>
//      <Content ContentID="000000000000000000000000025901481212" />
//    </Contents>
//  </request>
//</body>
//</message>
@SuppressWarnings("serial")
@Root(name="message")
public class ContentDeleteRequest implements Serializable{
	@Attribute(required=false)
	private String version;
	@Element(required=false)
	private SpaceHeader header;
	@Element(required=false)
	private ContentDeleteRequestBody body;
	
	
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

	public ContentDeleteRequestBody getBody() {
		return body;
	}

	public void setBody(ContentDeleteRequestBody body) {
		this.body = body;
	}

	@Root(name="body",strict=false)
	public static class ContentDeleteRequestBody implements Serializable{
		@Element
		private ContentDeleteRequestRequest request;

		public ContentDeleteRequestRequest getRequest() {
			return request;
		}

		public void setRequest(ContentDeleteRequestRequest request) {
			this.request = request;
		}
		
	}
	
	@Root(name="request",strict=false)
	public static class ContentDeleteRequestRequest implements Serializable{
		@Attribute(required=false)
		private String Action;
		@Attribute(required=false)
		private String UUID;
		@Attribute(required=false)
		private String SPID;
		@Attribute(required=false)
		private String AppID;
		@Attribute(required=false)
		private String AccessToken;
		@Element
		private ContentDeleteRequestContents Contents;
		public String getAction() {
			return Action;
		}
		public void setAction(String action) {
			Action = action;
		}
		public String getUUID() {
			return UUID;
		}
		public void setUUID(String uUID) {
			UUID = uUID;
		}
		public String getSPID() {
			return SPID;
		}
		public void setSPID(String sPID) {
			SPID = sPID;
		}
		public String getAppID() {
			return AppID;
		}
		public void setAppID(String appID) {
			AppID = appID;
		}
		public String getAccessToken() {
			return AccessToken;
		}
		public void setAccessToken(String accessToken) {
			AccessToken = accessToken;
		}
		public ContentDeleteRequestContents getContents() {
			return Contents;
		}
		public void setContents(ContentDeleteRequestContents contents) {
			Contents = contents;
		}
		
	}
	
	@Root(name="Contents",strict=false)
	public static class ContentDeleteRequestContents implements Serializable{
		@Element
		private ContentDeleteRequestContent Content;

		public ContentDeleteRequestContent getContent() {
			return Content;
		}

		public void setContent(ContentDeleteRequestContent content) {
			Content = content;
		}
		
	}
	
	@Root(name="Content",strict=false)
	public static class ContentDeleteRequestContent implements Serializable{

		@Attribute(required=false)
		private String ContentID;

		public String getContentID() {
			return ContentID;
		}

		public void setContentID(String contentID) {
			ContentID = contentID;
		}
		
	}
}
