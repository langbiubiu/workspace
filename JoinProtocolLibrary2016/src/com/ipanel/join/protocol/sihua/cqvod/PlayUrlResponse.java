package com.ipanel.join.protocol.sihua.cqvod;

import java.io.Serializable;
import java.util.List;

import org.simpleframework.xml.Attribute;
import org.simpleframework.xml.Element;
import org.simpleframework.xml.ElementList;
import org.simpleframework.xml.Path;
import org.simpleframework.xml.Root;
import org.simpleframework.xml.Text;

import com.ipanel.join.protocol.sihua.cqvod.PlayUrlResponse.PlayUrl.Content;

@Root(name="message",strict=false)
public class PlayUrlResponse implements Serializable {

	/**
	 * 
	 */
	private static final long serialVersionUID = 3309966895685942910L;
	
	@Attribute 
	@Path("body/result")
	private String code;
	@Attribute 
	@Path("body/result")
	private String description;
	@ElementList(inline=true)
	@Path("body/contents")
	private List<Content> listContents;
	
	
	
	@Override
	public String toString() {
		return "PlayUrlResponse [code=" + code + ", description=" + description + ", listContents=" + listContents
				+ "]";
	}



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



	public List<Content> getListContents() {
		return listContents;
	}



	public void setListContents(List<Content> listContents) {
		this.listContents = listContents;
	}



	@Root(name="play-url",strict=false)
	public static class PlayUrl implements Serializable{

		/**
		 * 
		 */
		private static final long serialVersionUID = -5439791230856246222L;
		@Attribute(required=false)
		private String code;
		@Text
		private String value;
		public String getCode() {
			return code;
		}
		public void setCode(String code) {
			this.code = code;
		}
		public String getValue() {
			return value;
		}
		public void setValue(String value) {
			this.value = value;
		}
		@Override
		public String toString() {
			return "PlayUrl [code=" + code + ", value=" + value + "]";
		}
		
		
		@Root(name="content",strict=false)
		public static class Content implements Serializable{

			/**
			 * 
			 */
			private static final long serialVersionUID = 6479756080475273416L;
			@Element(name="file-index",required=false)
			private String fileIndex;
			@Element(required=false)
			private String format;
			@Element(required=false)
			private String code;
			@Element(required=false,name="items-index")
			private String itemsIndex;
			@Element(required=false)
			private String url;
			@Element(required=false,name="play-url")
			private String playurl;
			@Element(required=false,name="m3u8-url")
			private String m3u8Url;
			@Element(required=false,name="play-urls")
			private PlayUrls playUrls;
			public String getFileIndex() {
				return fileIndex;
			}
			public void setFileIndex(String fileIndex) {
				this.fileIndex = fileIndex;
			}
			public String getFormat() {
				return format;
			}
			public void setFormat(String format) {
				this.format = format;
			}
			public String getCode() {
				return code;
			}
			public void setCode(String code) {
				this.code = code;
			}
			public String getItemsIndex() {
				return itemsIndex;
			}
			public void setItemsIndex(String itemsIndex) {
				this.itemsIndex = itemsIndex;
			}
			public String getUrl() {
				return url;
			}
			public void setUrl(String url) {
				this.url = url;
			}
			public String getPlayurl() {
				return playurl;
			}
			public void setPlayurl(String playurl) {
				this.playurl = playurl;
			}
			public String getM3u8Url() {
				return m3u8Url;
			}
			public void setM3u8Url(String m3u8Url) {
				this.m3u8Url = m3u8Url;
			}
			public PlayUrls getPlayUrls() {
				return playUrls;
			}
			public void setPlayUrls(PlayUrls playUrls) {
				this.playUrls = playUrls;
			}
			@Override
			public String toString() {
				return "Content [fileIndex=" + fileIndex + ", format=" + format + ", code=" + code + ", itemsIndex="
						+ itemsIndex + ", url=" + url + ", playurl=" + playurl + ", m3u8Url=" + m3u8Url + ", playUrls="
						+ playUrls + "]";
			}
			
			
		}
		
		
	}
	
	@Root(name="play-urls",strict=false)
	public static class PlayUrls implements Serializable{

		/**
		 * 
		 */
		private static final long serialVersionUID = -278905407711542215L;
		@ElementList(name="play-url",inline=true,required=false)
		private List<PlayUrl> listPlayUrl;
		public List<PlayUrl> getListPlayUrl() {
			return listPlayUrl;
		}
		public void setListPlayUrl(List<PlayUrl> listPlayUrl) {
			this.listPlayUrl = listPlayUrl;
		}
		@Override
		public String toString() {
			return "PlayUrls [listPlayUrl=" + listPlayUrl + "]";
		}
		
		
	}

}
