package com.ipanel.join.cq.vod.jsondata;

import org.simpleframework.xml.Attribute;
import org.simpleframework.xml.Element;
import org.simpleframework.xml.Root;

/**
 * ֱ������CDN�����ݲ��ŵ�ַ
 * 
 * @author dzwillpower
 * @time 2013��11��19�� ����1:46:49
 */
@Root(name = "play-urls")
public class PlayUrls {
	@Attribute
	private String code;// ���������ʶ
	@Element(name = "play-url")
	private String playurl;// ֱ������CDN�����ݲ��ŵ�ַ

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
