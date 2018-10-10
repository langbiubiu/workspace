package com.ipanel.join.chongqing.live.order;

import java.io.Serializable;

import com.google.gson.annotations.Expose;
/**
 * �û���Ϣ��Ӧ
 * @author Administrator
 *
 */
public class UserResponse implements Serializable {
	@Expose
	private String code; // ������(200���ɹ���������ʧ��)
	@Expose
	private String msg; // ��ϸ��Ϣ���ɹ� or ʧ��ԭ��
	@Expose
	private CustInfo custInfo; // �û���Ϣ
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
