package com.ipanel.join.chongqing.live.order;

import java.io.Serializable;

import com.google.gson.annotations.Expose;

/**
 * ActivityInfo���н���Ϣ��
 * @author Administrator
 *
 */
public class ActivityInfo implements Serializable {
	@Expose
	private String isActive; // code=200����£��Ƿ��н�,0:δ�н���1��ʾ�Ѿ��н���2��ʾ�н�����
	@Expose
	private String accNbr; // code=200����£��绰����
	@Expose
	private String activeDesc; // code=200����£����Ϣ
	public String getIsActive() {
		return isActive;
	}
	public void setIsActive(String isActive) {
		this.isActive = isActive;
	}
	public String getAccNbr() {
		return accNbr;
	}
	public void setAccNbr(String accNbr) {
		this.accNbr = accNbr;
	}
	public String getActiveDesc() {
		return activeDesc;
	}
	public void setActiveDesc(String activeDesc) {
		this.activeDesc = activeDesc;
	}
	@Override
	public String toString() {
		return "ActivityInfo [isActive=" + isActive + ", accNbr=" + accNbr
				+ ", activeDesc=" + activeDesc + "]";
	}
	
	
}
