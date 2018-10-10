package com.ipanel.join.chongqing.live.order;

import java.io.Serializable;

import com.google.gson.annotations.Expose;

/**
 * AliOrder（支付宝计费订购返回信息）
 * @author Administrator
 *
 */
public class AliOrder implements Serializable {
	@Expose
	private String tradeNo; // code=202返回；支付宝流水号
	@Expose
	private String orderNo; // code=202返回；业务订单号
	@Expose
	private String aliOrderNo; // code=202返回；支付宝订单号
	public String getTradeNo() {
		return tradeNo;
	}
	public void setTradeNo(String tradeNo) {
		this.tradeNo = tradeNo;
	}
	public String getOrderNo() {
		return orderNo;
	}
	public void setOrderNo(String orderNo) {
		this.orderNo = orderNo;
	}
	public String getAliOrderNo() {
		return aliOrderNo;
	}
	public void setAliOrderNo(String aliOrderNo) {
		this.aliOrderNo = aliOrderNo;
	}
	@Override
	public String toString() {
		return "AliOrder [tradeNo=" + tradeNo + ", orderNo=" + orderNo
				+ ", aliOrderNo=" + aliOrderNo + "]";
	}

	
}
