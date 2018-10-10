package com.ipanel.join.cq.vod.jsondata;

public class VodRecommend {

	String vodId;
	String playType;
	String picPath;
	String name;
	
	public String getName() {
		return name;
	}
	public void setName(String name) {
		this.name = name;
	}
	public String getVodId() {
		return vodId;
	}
	public void setVodId(String vodId) {
		this.vodId = vodId;
	}
	public String getPlayType() {
		return playType;
	}
	public void setPlayType(String playType) {
		this.playType = playType;
	}
	public String getPicPath() {
		return picPath;
	}
	public void setPicPath(String picPath) {
		this.picPath = picPath;
	}
	public VodRecommend(){}
	public VodRecommend(String vodId, String playType, String picPath) {
		super();
		this.vodId = vodId;
		this.playType = playType;
		this.picPath = picPath;
	}
	
	

}
