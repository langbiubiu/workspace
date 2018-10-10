package com.ipanel.join.cq.huawei.data;

import java.io.Serializable;

import com.google.gson.annotations.Expose;

public class VodProgram implements Serializable{
	
	@Expose
	private String vodID; // 影片ID
	@Expose
	private String name; // 影片名
	@Expose
	private String img; // 影片海报
	@Expose
	private	String icon; // 影片右上角icon
	@Expose
	private String HD; // 是否高清
	@Expose
	private String tags; // 影片标签类型
	@Expose
	private String playType; // 影片类型
	
	private String typeID; // 栏目id
	
	public String getVodID() {
		return vodID;
	}
	public void setVodID(String vodID) {
		this.vodID = vodID;
	}
	public String getName() {
		return name;
	}
	public void setName(String name) {
		this.name = name;
	}
	public String getImg() {
		return img;
	}
	public void setImg(String img) {
		this.img = img;
	}
	public String getIcon() {
		return icon;
	}
	public void setIcon(String icon) {
		this.icon = icon;
	}
	public String getHD() {
		return HD;
	}
	public void setHD(String hD) {
		HD = hD;
	}
	public String getTags() {
		return tags;
	}
	public void setTags(String tags) {
		this.tags = tags;
	}
	public String getPlayType() {
		return playType;
	}
	public void setPlayType(String playType) {
		this.playType = playType;
	}
	public String getTypeID() {
		return typeID;
	}
	public void setTypeID(String typeID) {
		this.typeID = typeID;
	}
	@Override
	public String toString() {
		return "VodProgram2 [vodID=" + vodID + ", name=" + name + ", img="
				+ img + ", icon=" + icon + ", HD=" + HD + ", tags=" + tags
				+ ", playType=" + playType + ", typeID=" + typeID + "]";
	}
	
}
