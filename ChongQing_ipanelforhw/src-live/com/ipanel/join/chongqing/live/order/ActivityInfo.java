package com.ipanel.join.chongqing.live.order;

import java.io.Serializable;

import com.google.gson.annotations.Expose;

/**
 * ActivityInfo（中奖信息）
 * @author Administrator
 *
 */
public class ActivityInfo implements Serializable {
	@Expose
	private String isActive; // code=200情况下，是否中奖,0:未中奖，1表示已经中奖，2表示中奖机会
	@Expose
	private String accNbr; // code=200情况下，电话号码
	@Expose
	private String activeDesc; // code=200情况下，活动消息
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
