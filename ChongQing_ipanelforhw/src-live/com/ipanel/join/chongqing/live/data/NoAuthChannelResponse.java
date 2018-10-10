package com.ipanel.join.chongqing.live.data;

import java.io.Serializable;
import java.util.List;

import com.google.gson.annotations.Expose;

public class NoAuthChannelResponse implements Serializable{

	/**
	 * 
	 */
	private static final long serialVersionUID = 3502113106698508543L;
	
	@Expose
	private String code;
	
	@Expose
	private String message;
	
	@Expose
	private List<NoAuthChannel> data;

	public String getCode() {
		return code;
	}

	public void setCode(String code) {
		this.code = code;
	}

	public String getMessage() {
		return message;
	}

	public void setMessage(String message) {
		this.message = message;
	}

	public List<NoAuthChannel> getData() {
		return data;
	}

	public void setData(List<NoAuthChannel> data) {
		this.data = data;
	}
	
}
