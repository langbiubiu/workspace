package com.ipanel.join.chongqing.live.order;

import java.io.Serializable;

import com.google.gson.annotations.Expose;

/***
 * Balance���˱���
 * @author Administrator
 *
 */
public class Balance implements Serializable {
	@Expose
	private String balanceId; // �˱�����0��ͨ���˱�;1002�����ֻ���;1003�����;1004�����ָ���;1005����������
	@Expose
	private String balanceName; // �˱�����
	@Expose
	private String balance; // �˻����(��λ��Ԫ)
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
