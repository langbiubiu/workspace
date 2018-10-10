package com.ipanel.join.chongqing.live.order;

import java.io.Serializable;

import com.google.gson.annotations.Expose;

/***
 * Order（套餐）
 * @author Administrator
 *
 */
public class Order implements Serializable {
	@Expose
	private String orderNo; // code=200情况下，订购订单号
	@Expose
	private String balance; // code=200情况下，套餐选择账本余额信息 
	@Expose
	private String notifyUrl; // code=201情况下，通知地址
	@Expose
	private String rechargeOrderId; // code=201情况下，充值订单号
	@Expose
	private String rechargeBalance; // code=200或code=201情况下，所需支付套餐的金额
	public String getOrderNo() {
		return orderNo;
	}
	public void setOrderNo(String orderNo) {
		this.orderNo = orderNo;
	}
	public String getBalance() {
		return balance;
	}
	public void setBalance(String balance) {
		this.balance = balance;
	}
	public String getNotifyUrl() {
		return notifyUrl;
	}
	public void setNotifyUrl(String notifyUrl) {
		this.notifyUrl = notifyUrl;
	}
	public String getRechargeOrderId() {
		return rechargeOrderId;
	}
	public void setRechargeOrderId(String rechargeOrderId) {
		this.rechargeOrderId = rechargeOrderId;
	}
	public String getRechargeBalance() {
		return rechargeBalance;
	}
	public void setRechargeBalance(String rechargeBalance) {
		this.rechargeBalance = rechargeBalance;
	}
	@Override
	public String toString() {
		return "Order [orderNo=" + orderNo + ", balance=" + balance
				+ ", notifyUrl=" + notifyUrl + ", rechargeOrderId="
				+ rechargeOrderId + ", rechargeBalance=" + rechargeBalance
				+ "]";
	}
	
}
