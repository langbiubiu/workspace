package com.ipanel.join.protocol.sihua.cqvod;

import java.io.Serializable;

import com.google.gson.annotations.Expose;

public class GetWeiboRequest2 implements Serializable {

	/**
	 * 
	 */
	private static final long serialVersionUID = 5492236463986144999L;
	@Expose
	private String systemCode;//系统分配的固定参数，调用接口前申请
	@Expose
	private String  operation;//0:查询微博帐号 1：绑定微博帐号 2：解绑帐号
	@Expose
	private String userid;//用户标识
	@Expose
	private String iccard;//用户卡号，userid和iccard必须填写一个
	@Expose
	private String weiboAuthorizeCode;//微博授权码
	@Expose
	private String weiboUid;//微博账号的uid
	private String token;//(userId)登录的token标识（暂无用）
	public String getSystemCode() {
		return systemCode;
	}
	public void setSystemCode(String systemCode) {
		this.systemCode = systemCode;
	}
	public String getOperation() {
		return operation;
	}
	public void setOperation(String operation) {
		this.operation = operation;
	}
	public String getUserid() {
		return userid;
	}
	public void setUserid(String userid) {
		this.userid = userid;
	}
	public String getIccard() {
		return iccard;
	}
	public void setIccard(String iccard) {
		this.iccard = iccard;
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
	public String getToken() {
		return token;
	}
	public void setToken(String token) {
		this.token = token;
	}
	@Override
	public String toString() {
		return "GetWeiboRequest2 [systemCode=" + systemCode + ", operation=" + operation + ", userid=" + userid
				+ ", iccard=" + iccard + ", weiboAuthorizeCode=" + weiboAuthorizeCode + ", weiboUid=" + weiboUid
				+ ", token=" + token + "]";
	}
	
	

	
}
