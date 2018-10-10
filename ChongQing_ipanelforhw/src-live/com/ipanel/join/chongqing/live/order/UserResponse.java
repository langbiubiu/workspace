package com.ipanel.join.chongqing.live.order;

import java.io.Serializable;

import com.google.gson.annotations.Expose;
/**
 * 用户信息响应
 * @author Administrator
 *
 */
public class UserResponse implements Serializable {
	@Expose
	private String code; // 返回码(200：成功，其他：失败)
	@Expose
	private String msg; // 详细信息（成功 or 失败原因）
	@Expose
	private CustInfo custInfo; // 用户信息
	public String getCode() {
		return code;
	}
	public void setCode(String code) {
		this.code = code;
	}
	public String getMsg() {
		return msg;
	}
	public void setMsg(String msg) {
		this.msg = msg;
	}
	public CustInfo getCustInfo() {
		return custInfo;
	}
	public void setCustInfo(CustInfo custInfo) {
		this.custInfo = custInfo;
	}
	@Override
	public String toString() {
		return "UserResponse [code=" + code + ", msg=" + msg + ", custInfo="
				+ custInfo + "]";
	}
	
	
}
