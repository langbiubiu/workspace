package com.ipanel.join.protocol.sihua.cqvod;

import java.io.Serializable;

import com.google.gson.annotations.Expose;

public class WeiboResponse2 implements Serializable {

	/**
	 * 
	 */
	private static final long serialVersionUID = -3780181076728565777L;
	@Expose
	private String result;//	0：请求成功1：失败
	@Expose
	private String resultDesc; //操作结果描述（失败原因详细描述）
	@Expose
	private String weiboAuthorizeCode;//当请求的operation=0时有效，微博授权码
	@Expose
	private String weiboUid;//微博账号的uid
	public String getResult() {
		return result;
	}
	public void setResult(String result) {
		this.result = result;
	}
	public String getResultDesc() {
		return resultDesc;
	}
	public void setResultDesc(String resultDesc) {
		this.resultDesc = resultDesc;
	}
	public String getWeiboAuthorizeCode() {
		return weiboAuthorizeCode;
	}
	public void setWeiboAuthorizeCode(String weiboAuthorizeCode) {
		this.weiboAuthorizeCode = weiboAuthorizeCode;
	}
	public String getWeiboUid() {
		return weiboUid;
	}
	public void setWeiboUid(String weiboUid) {
		this.weiboUid = weiboUid;
	}
	@Override
	public String toString() {
		return "WeiboResponse2 [result=" + result + ", resultDesc=" + resultDesc + ", weiboAuthorizeCode="
				+ weiboAuthorizeCode + ", weiboUid=" + weiboUid + "]";
	}
	
	

	
}
