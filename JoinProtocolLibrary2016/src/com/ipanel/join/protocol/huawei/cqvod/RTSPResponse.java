package com.ipanel.join.protocol.huawei.cqvod;

import java.io.Serializable;

import com.google.gson.annotations.Expose;

public class RTSPResponse implements Serializable{

	/**
	 * 
	 */
	private static final long serialVersionUID = 5007549919295103136L;
	@Expose
	private String   playFlag;
	@Expose
	private String playUrl;
	@Expose
	private String message;
	private String anCiFlag; // 当 playFlag = 0 ，然后如果 anCiFlag  = 1 则表示是按此点播 
	private String confirmUrl; // 按次购买链接
	private String price; // 按次购买价格
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
	public String getAnCiFlag() {
		return anCiFlag;
	}
	public void setAnCiFlag(String anCiFlag) {
		this.anCiFlag = anCiFlag;
	}
	public String getConfirmUrl() {
		return confirmUrl;
	}
	public void setConfirmUrl(String confirmUrl) {
		this.confirmUrl = confirmUrl;
	}
	public String getPrice() {
		return price;
	}
	public void setPrice(String price) {
		this.price = price;
	}
	@Override
	public String toString() {
		return "RTSPResponse [playFlag=" + playFlag + ", playUrl=" + playUrl + ", anCiFlag=" + anCiFlag
				+ ", confirmUrl=" + confirmUrl + ", message=" + message + ", price=" + price + "]";
	}
	
	

}
