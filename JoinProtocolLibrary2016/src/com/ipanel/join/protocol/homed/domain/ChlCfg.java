package com.ipanel.join.protocol.homed.domain;

import java.io.Serializable;

import com.google.gson.annotations.Expose;

public class ChlCfg implements Serializable {

	/**
	 * 
	 */
	private static final long serialVersionUID = -7601561507813669277L;
	@Expose
	private String isHigh;
	@Expose
	private String videoFormat;
	@Expose
	private String channelPoint;
	@Expose
	private String serviceID;

	public String getIsHigh() {
		return isHigh;
	}

	public void setIsHigh(String isHigh) {
		this.isHigh = isHigh;
	}

	public String getVideoFormat() {
		return videoFormat;
	}

	public void setVideoFormat(String videoFormat) {
		this.videoFormat = videoFormat;
	}

	public String getChannelPoint() {
		return channelPoint;
	}

	public void setChannelPoint(String channelPoint) {
		this.channelPoint = channelPoint;
	}

	public String getServiceID() {
		return serviceID;
	}

	public void setServiceID(String serviceID) {
		this.serviceID = serviceID;
	}

}
