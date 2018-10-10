package com.ipanel.join.protocol.sihua.cqvod;

import java.io.Serializable;

import com.google.gson.annotations.Expose;

public class GetWeiboRequest2 implements Serializable {

	/**
	 * 
	 */
	private static final long serialVersionUID = 5492236463986144999L;
	@Expose
	private String systemCode;//ϵͳ����Ĺ̶����������ýӿ�ǰ����
	@Expose
	private String  operation;//0:��ѯ΢���ʺ� 1����΢���ʺ� 2������ʺ�
	@Expose
	private String userid;//�û���ʶ
	@Expose
	private String iccard;//�û����ţ�userid��iccard������дһ��
	@Expose
	private String weiboAuthorizeCode;//΢����Ȩ��
	@Expose
	private String weiboUid;//΢���˺ŵ�uid
	private String token;//(userId)��¼��token��ʶ�������ã�
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
