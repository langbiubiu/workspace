package com.ipanel.join.cq.vod.jsondata;

import org.simpleframework.xml.Attribute;
import org.simpleframework.xml.Element;
import org.simpleframework.xml.Root;

/**
 * 直接请求CDN的内容播放地址
 * 
 * @author dzwillpower
 * @time 2013年11月19日 下午1:46:49
 */
@Root(name = "play-urls")
public class PlayUrls {
	@Attribute
	private String code;// 服务区域标识
	@Element(name = "play-url")
	private String playurl;// 直接请求CDN的内容播放地址

	public String getCode() {
		return code;
	}

	public void setCode(String code) {
		this.code = code;
	}

	public String getPlayurl() {
		return playurl;
	}

	public void setPlayurl(String playurl) {
		this.playurl = playurl;
	}

	@Override
	public String toString() {
		return "PlayUrls [code=" + code + ", playurl=" + playurl + "]";
	}
	

}
