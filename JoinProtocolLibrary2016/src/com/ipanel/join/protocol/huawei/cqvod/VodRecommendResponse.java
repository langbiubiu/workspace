package com.ipanel.join.protocol.huawei.cqvod;

import java.io.Serializable;

import com.google.gson.annotations.Expose;
/**
 * 推荐的影片列表
 * @author dzwillpower
 *
 */
public class VodRecommendResponse implements Serializable {

	/**
	 * 
	 */
	private static final long serialVersionUID = 2550777763253078471L;
	@Expose
	private String vodId;
	@Expose
	private String picPath;
	@Expose
	private String playType;
	@Expose
	private String vodName;
	public String getVodId() {
		return vodId;
	}
	public void setVodId(String vodId) {
		this.vodId = vodId;
	}
	public String getPicPath() {
		return picPath;
	}
	public void setPicPath(String picPath) {
		this.picPath = picPath;
	}
	public String getPlayType() {
		return playType;
	}
	public void setPlayType(String playType) {
		this.playType = playType;
	}
	public String getVodName() {
		return vodName;
	}
	public void setVodName(String vodName) {
		this.vodName = vodName;
	}
	public static long getSerialversionuid() {
		return serialVersionUID;
	}
	@Override
	public String toString() {
		return "VodRecommendResponse [vodId=" + vodId + ", picPath=" + picPath + ", playType=" + playType
				+ ", vodName=" + vodName + "]";
	}
	
	
	

}
