package com.ipanel.join.protocol.sihua.cqvod;

import org.simpleframework.xml.Element;
import org.simpleframework.xml.Root;

@Root(name="media-file",strict=false)
public class MediaFile {

	@Element
	private String index;// 媒体文件序列号，当多个媒体文件时，用该值标识不同的媒体文件，但是该值和媒体文件格式无特定关系
	@Element
	private String type;// 媒体文件类型枚举
	@Element(name="bit-rate")
	private String bitRate;// 媒体文件的码率
	@Element(name="relative-ppvids",required=false)
	private String relativeppvids;// PPVID列表，如有多个用逗号分隔，如果存在表示该PPVID绑定在子内容上。
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
	public String getBitRate() {
		return bitRate;
	}
	public void setBitRate(String bitRate) {
		this.bitRate = bitRate;
	}
	public String getRelativeppvids() {
		return relativeppvids;
	}
	public void setRelativeppvids(String relativeppvids) {
		this.relativeppvids = relativeppvids;
	}
	@Override
	public String toString() {
		return "MediaFile [index=" + index + ", type=" + type + ", bitRate=" + bitRate + ", relativeppvids="
				+ relativeppvids + "]";
	}
	
}
