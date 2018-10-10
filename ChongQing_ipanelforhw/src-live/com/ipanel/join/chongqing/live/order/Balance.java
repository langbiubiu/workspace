package com.ipanel.join.chongqing.live.order;

import java.io.Serializable;

import com.google.gson.annotations.Expose;

/***
 * Balance（账本）
 * @author Administrator
 *
 */
public class Balance implements Serializable {
	@Expose
	private String balanceId; // 账本类型0：通用账本;1002：数字基本;1003：宽带;1004：数字付费;1005：互动基本
	@Expose
	private String balanceName; // 账本名称
	@Expose
	private String balance; // 账户余额(单位：元)
	public String getBalanceId() {
		return balanceId;
	}
	public void setBalanceId(String balanceId) {
		this.balanceId = balanceId;
	}
	public String getBalanceName() {
		return balanceName;
	}
	public void setBalanceName(String balanceName) {
		this.balanceName = balanceName;
	}
	public String getBalance() {
		return balance;
	}
	public void setBalance(String balance) {
		this.balance = balance;
	}
	@Override
	public String toString() {
		return "Balance [balanceId=" + balanceId + ", balanceName="
				+ balanceName + ", balance=" + balance + "]";
	}
	
	
}
