package com.ipanel.join.cq.vod.jsondata;

import org.simpleframework.xml.Attribute;
import org.simpleframework.xml.Element;
import org.simpleframework.xml.ElementList;
import org.simpleframework.xml.Path;
import org.simpleframework.xml.Root;

@Root(name = "message")
public class ContentUrl {
	@Element(name = "file-index")
	@Path("body/contents/content")
	private String file_index;// 内容集数序号——整数，如果内容类型是连续剧该值代表真正的集数；其他类型的内容，该值为0。

	private String url;// 内容详细页面访问地址
	private String play_url; // 直接请求CDN的内容播放地址。未来将不再支持，请从auth-play-urls中获取。

	private String format;// 媒体文件格式
	@ElementList(name = "play-urls", inline = true)
	@Path("body/contents/content/")
	private PlayUrls playUrls;
	@Attribute
	@Path("body/result ")
	private String code ;
	@Attribute
	@Path("body/result ")
	private String description;

	public String getFile_index() {
		return file_index;
	}

	public void setFile_index(String file_index) {
		this.file_index = file_index;
	}

	public PlayUrls getPlayUrls() {
		return playUrls;
	}

	public void setPlayUrls(PlayUrls playUrls) {
		this.playUrls = playUrls;
	}

	public String getUrl() {
		return url;
	}

	public void setUrl(String url) {
		this.url = url;
	}

	public String getPlay_url() {
		return play_url;
	}

	public void setPlay_url(String play_url) {
		this.play_url = play_url;
	}

	public String getFormat() {
		return format;
	}

	public void setFormat(String format) {
		this.format = format;
	}

	@Override
	public String toString() {
		return "ContentUrl [file_index=" + file_index + ", url=" + url + ", play_url=" + play_url + ", format="
				+ format + ", playUrls=" + playUrls + ", code=" + code + ", description=" + description + "]";
	}

	public String getDescription() {
		return description;
	}

	public void setDescription(String description) {
		this.description = description;
	}

	public String getCode() {
		return code;
	}

	public void setCode(String code) {
		this.code = code;
	}

	
	
	
}
