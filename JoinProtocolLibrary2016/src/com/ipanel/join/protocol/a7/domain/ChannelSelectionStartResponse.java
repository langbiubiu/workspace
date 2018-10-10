package com.ipanel.join.protocol.a7.domain;

import java.io.Serializable;

import org.simpleframework.xml.Attribute;
import org.simpleframework.xml.Root;

@Root(strict=false)
public class ChannelSelectionStartResponse implements Serializable {

	/**
	 * 
	 */
	private static final long serialVersionUID = -4295777923825372995L;

	@Attribute(name = "access-url",required=false)
	private String accessUrl;

	@Attribute(name="rtsp",required=false)
	private String rtsp;
	@Attribute(required=false)
	private String purchaseToken;
	@Attribute(required=false)
	private String startTime;
	
	public String getRtsp() {
		return rtsp;
	}

	public void setRtsp(String rtsp) {
		this.rtsp = rtsp;
	}

	public String getPurchaseToken() {
		return purchaseToken;
	}

	public void setPurchaseToken(String purchaseToken) {
		this.purchaseToken = purchaseToken;
	}

	public String getStartTime() {
		return startTime;
	}

	public void setStartTime(String startTime) {
		this.startTime = startTime;
	}

	public String getAccessUrl() {
		return accessUrl;
	}

	public void setAccessUrl(String accessUrl) {
		this.accessUrl = accessUrl;
	}

	@Override
	public String toString() {
		return "ChannelSelectionStartResponse [accessUrl=" + accessUrl + ", rtsp=" + rtsp + ", purchaseToken="
				+ purchaseToken + ", startTime=" + startTime + "]";
	}

}
