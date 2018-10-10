package com.ipanel.join.chongqing.live.data;

import java.io.Serializable;

import com.google.gson.annotations.Expose;

public class HideChannel implements Serializable{
	
//	public HideChannel(String name, int number, String url) {
//		this.name = name;
//		this.number = number;
//		this.url = url;
//	}
	
	/**
	 * 
	 */
	private static final long serialVersionUID = 2773042699845818174L;

	@Expose
	private int appType;
	
	@Expose
	private int contentType;
	
	@Expose
	private String name;
	
	@Expose
	private int logicChannel;
	
	@Expose
	private int serviceId;
	
	@Expose
	private String url;
	
	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public int getLogicChannel() {
		return logicChannel;
	}

	public void setLogicChannel(int number) {
		this.logicChannel = number;
	}

	public String getUrl() {
		return url;
	}

	public void setUrl(String url) {
		this.url = url;
	}

	public int getAppType() {
		return appType;
	}

	public void setAppType(int appType) {
		this.appType = appType;
	}

	public int getContentType() {
		return contentType;
	}

	public void setContentType(int contentType) {
		this.contentType = contentType;
	}

	public int getServiceId() {
		return serviceId;
	}

	public void setServiceId(int serviceId) {
		this.serviceId = serviceId;
	}
	
}
