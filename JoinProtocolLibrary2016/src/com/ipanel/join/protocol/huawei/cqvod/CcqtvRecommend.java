package com.ipanel.join.protocol.huawei.cqvod;

import java.io.Serializable;

import com.google.gson.annotations.Expose;
/***
 * 推荐影片列表
 * @author dzwillpower
 *
 */
public class CcqtvRecommend implements Serializable {

	/**
	 * 
	 */
	private static final long serialVersionUID = -5982262195010024696L;
	@Expose
	private String vodID;
	@Expose
	private String name;
	@Expose
	private String HD;
	@Expose
	private String img;
	@Expose
	private String icon;
	@Expose
	private String tags;
	@Expose
	private String typeID;
	@Expose
	private String playType;
	
	public String getPlayType() {
		return playType;
	}
	public void setPlayType(String playType) {
		this.playType = playType;
	}
	public String getVodId() {
		return vodID;
	}
	public void setVodId(String vodId) {
		this.vodID = vodId;
	}
	public String getName() {
		return name;
	}
	public void setName(String name) {
		this.name = name;
	}
	public String getHD() {
		return HD;
	}
	public void setHD(String hD) {
		HD = hD;
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
	public String getTags() {
		return tags;
	}
	public void setTags(String tags) {
		this.tags = tags;
	}
	public String getTypeId() {
		return typeID;
	}
	public void setTypeId(String typeId) {
		this.typeID = typeId;
	}
	@Override
	public String toString() {
		return "CcqtvRecommend [vodId=" + vodID + ", name=" + name + ", HD=" + HD + ", img=" + img + ", icon=" + icon
				+ ", tags=" + tags + ", typeId=" + typeID + ", playType=" + playType + "]";
	}
	
	
	

}
