package com.ipanel.join.protocol.sihua.cqvod.space;

import java.io.Serializable;

import org.simpleframework.xml.Attribute;
import org.simpleframework.xml.Element;
import org.simpleframework.xml.Root;

//<message  version="1.0">
//    <header RequestSystemID ="NPVR" TargetSystemID ="iSpace" CorrelateID ="1000002"  Action ="RESPONSE" Command ="ISPACE_USER_CONTENT_DELETE"  Timestamp ="2014-05-06 14:05:43"/>
//    <body>
//        <result Code="SD8005" Description="用户内容删除失败,指定类型内容不存在">
//            <FaildContents>
//                <Content contentID="000000000000000000000000025901481212"></Content>
//            </FaildContents>
//        </result>
//    </body>
//</message>
@SuppressWarnings("serial")
@Root(name="message")
public class ContentDeleteResponse implements Serializable{
	@Attribute(required=false)
	private String version;
	@Element(required=false)
	private SpaceHeader header;
	@Element(required=false)
	private ContentDeleteResponseBody body;
	
	

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

	public ContentDeleteResponseBody getBody() {
		return body;
	}

	public void setBody(ContentDeleteResponseBody body) {
		this.body = body;
	}

	@Root(name="body",strict=false)
	public static class ContentDeleteResponseBody implements Serializable{
		@Element
		private ContentDeleteResponseResult result;

		public ContentDeleteResponseResult getResult() {
			return result;
		}

		public void setResult(ContentDeleteResponseResult result) {
			this.result = result;
		}

	}
	
	@Root(name="result",strict=false)
	public static class ContentDeleteResponseResult implements Serializable{
		@Attribute(required=false)
		private String Code;
		@Attribute(required=false)
		private String Description;
		@Element
		private ContentDeleteFaildContents FaildContents;
		public String getCode() {
			return Code;
		}
		public void setCode(String code) {
			Code = code;
		}
		public String getDescription() {
			return Description;
		}
		public void setDescription(String description) {
			Description = description;
		}
		public ContentDeleteFaildContents getFaildContents() {
			return FaildContents;
		}
		public void setFaildContents(ContentDeleteFaildContents faildContents) {
			FaildContents = faildContents;
		}
		
	}
	
	@Root(name="FaildContents",strict=false)
	public static class ContentDeleteFaildContents implements Serializable{
		@Element(required=false)
		private ContentDeleteContent Content;

		public ContentDeleteContent getContent() {
			return Content;
		}

		public void setContent(ContentDeleteContent content) {
			Content = content;
		}
		
	}
	
	@Root(name="Content",strict=false)
	public static class ContentDeleteContent implements Serializable{

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
