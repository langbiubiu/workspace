package com.ipanel.join.cq.huawei.data;

import java.io.Serializable;
import java.util.List;

import com.google.gson.annotations.Expose;

public class HuaWeiResponse implements Serializable {
	@Expose
	private String total; // 影片总数
	@Expose
	private String posterImg; // 栏目海报
	@Expose
	private List<VodProgram> vod; // 影片列表
	@Expose
	private String typeID; // 栏目id
	@Expose
	private String playFlag; // 是否可以播放,1可以播放；0不可以播放
	@Expose
	private String playUrl; // 影片RTSP地址
	@Expose
	private String message; // 提示内容
	public String getTotal() {
		return total;
	}
	public void setTotal(String total) {
		this.total = total;
	}
	public String getPosterImg() {
		return posterImg;
	}
	public void setPosterImg(String posterImg) {
		this.posterImg = posterImg;
	}
	public List<VodProgram> getVod() {
		return vod;
	}
	public void setVod(List<VodProgram> vod) {
		this.vod = vod;
	}
	public String getTypeID() {
		return typeID;
	}
	public void setTypeID(String typeID) {
		this.typeID = typeID;
	}
	public String getPlayFlag() {
		return playFlag;
	}
	public void setPlayFlag(String playFlag) {
		this.playFlag = playFlag;
	}
	public String getPlayUrl() {
		return playUrl;
	}
	public void setPlayUrl(String playUrl) {
		this.playUrl = playUrl;
	}
	public String getMessage() {
		return message;
	}
	public void setMessage(String message) {
		this.message = message;
	}
	@Override
	public String toString() {
		return "HuaWeiResponse2 [total=" + total + ", posterImg=" + posterImg
				+ ", vod=" + vod + ", typeID=" + typeID + ", playFlag="
				+ playFlag + ", playUrl=" + playUrl + ", message=" + message
				+ "]";
	}
	
}
