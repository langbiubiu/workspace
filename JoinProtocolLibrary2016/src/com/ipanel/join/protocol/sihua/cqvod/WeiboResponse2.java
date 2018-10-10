package com.ipanel.join.protocol.sihua.cqvod;

import java.io.Serializable;

import com.google.gson.annotations.Expose;

public class WeiboResponse2 implements Serializable {

	/**
	 * 
	 */
	private static final long serialVersionUID = -3780181076728565777L;
	@Expose
	private String result;//	0������ɹ�1��ʧ��
	@Expose
	private String resultDesc; //�������������ʧ��ԭ����ϸ������
	@Expose
	private String weiboAuthorizeCode;//�������operation=0ʱ��Ч��΢����Ȩ��
	@Expose
	private String weiboUid;//΢���˺ŵ�uid
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
