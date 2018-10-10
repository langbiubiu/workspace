package com.ipanel.join.protocol.sihua.cqvod;

import org.simpleframework.xml.Element;
import org.simpleframework.xml.Root;

@Root(name="image-file",strict=false)
public class ImageFile {
	@Element
	private String index; //图片文件序列号，当多个图片文件时，用该值标识不同的图片文件，但是该值和图片文件格式无特定关系
	@Element
	private String type;//图片文件类型枚举
	@Element
	private String url;//图片访问地址
	
	public String getIndex() {
		return index;
	}
	public void setIndex(String index) {
		this.index = index;
	}
	public String getType() {
		return type;
	}
	public void setType(String type) {
		this.type = type;
	}
	public String getUrl() {
		return url;
	}
	public void setUrl(String url) {
		this.url = url;
	}
	@Override
	public String toString() {
		return "ImageFile [index=" + index + ", type=" + type + ", url=" + url + "]";
	}
	

}
