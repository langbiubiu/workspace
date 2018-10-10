package com.ipanel.join.protocol.sihua.cqvod.space;

import java.io.Serializable;
import java.util.List;

import org.simpleframework.xml.Attribute;
import org.simpleframework.xml.Element;
import org.simpleframework.xml.ElementList;
import org.simpleframework.xml.Root;
import org.simpleframework.xml.Text;

@Root(name="message")
public class SpacePlayUrl implements Serializable {

	/**
	 * 
	 */
	private static final long serialVersionUID = -6710101331121441782L;
	@Attribute(required=false)
	private String version;
	@Element(required=false)
	private SpaceHeader header;
	@Element(required=false)
	private PlayUrlBody body;
	
	
	
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

	public PlayUrlBody getBody() {
		return body;
	}

	public void setBody(PlayUrlBody body) {
		this.body = body;
	}

	@Root(name="body",strict=false)
	public static class PlayUrlBody implements Serializable{
		
		/**
		 * 
		 */
		private static final long serialVersionUID = -5179391759242664143L;
		@Element
		private Result result;
		public Result getResult() {
			return result;
		}
		public void setResult(Result result) {
			this.result = result;
		}
			
	}
	@SuppressWarnings("serial")
	@Root(name="result",strict=false)
	public static class Result implements Serializable{
		@Attribute(required=false)
		private String Code;
		@Attribute(required=false)
		private String Description;
		@Attribute(required=false)
		private String contentID;
		@Element(name="PlayUrlList")
		private PlayUrlList playUrlList;
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
		public String getContentID() {
			return contentID;
		}
		public void setContentID(String contentID) {
			this.contentID = contentID;
		}
		public PlayUrlList getPlayUrlList() {
			return playUrlList;
		}
		public void setPlayUrlList(PlayUrlList playUrlList) {
			this.playUrlList = playUrlList;
		}
		
	}
	
	@SuppressWarnings("serial")
	@Root(name="PlayUrlList",strict=false)
	public static class PlayUrlList implements Serializable{
		@ElementList(inline=true,name="playUrl")
		private List<PlayUrl> contentList;

		public List<PlayUrl> getContentList() {
			return contentList;
		}

		public void setContentList(List<PlayUrl> contentList) {
			this.contentList = contentList;
		}
		
	}
	
	
	@Root(name="playUrl",strict=false)
	public static class PlayUrl implements Serializable{
		/**
		 * 
		 */
		private static final long serialVersionUID = 4358462463263277390L;
		@Attribute(required=false)
		private String codeRate;
		@Attribute(required=false)
		private String codec;

		@Element(name="url")
		private Url url;

		public String getCodeRate() {
			return codeRate;
		}

		public void setCodeRate(String codeRate) {
			this.codeRate = codeRate;
		}

		public String getCodec() {
			return codec;
		}

		public void setCodec(String codec) {
			this.codec = codec;
		}

		public Url getUrl() {
			return url;
		}

		public void setUrl(Url url) {
			this.url = url;
		}
				
	}
	
	@SuppressWarnings("serial")
	@Root(name="url",strict=false)
	public static class Url implements Serializable{
		@Text
		private String value;

		public String getValue() {
			return value;
		}

		public void setValue(String value) {
			this.value = value;
		}
		
	}
}
