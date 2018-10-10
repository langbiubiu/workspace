package com.ipanel.join.chongqing.live.order;

import java.io.Serializable;

import com.google.gson.annotations.Expose;

/**
 * 套擦订购响应
 * @author Administrator
 *
 */
public class OrderResponse implements Serializable {
	@Expose
	private String code; // 返回码(200：直接订购成功；201：先支付，后订购，针对银视通计费订购；202：先支付，后订购，针对支付宝计费订购；其他：订购失败)
	@Expose
	private String msg; // 详细信息（成功 or 失败原因）
	@Expose
	private Order order; // 订购信息
	@Expose
	private ActivityInfo activeInfo; // 中奖信息
	@Expose
	private AliOrder aliOrder; // 支付宝计费订购；针对code=202时返回信息
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
	public Order getOrder() {
		return order;
	}
	public void setOrder(Order order) {
		this.order = order;
	}
	public ActivityInfo getActiveInfo() {
		return activeInfo;
	}
	public void setActiveInfo(ActivityInfo activeInfo) {
		this.activeInfo = activeInfo;
	}
	public AliOrder getAliOrder() {
		return aliOrder;
	}
	public void setAliOrder(AliOrder aliOrder) {
		this.aliOrder = aliOrder;
	}
	@Override
	public String toString() {
		return "OrderResponse [code=" + code + ", msg=" + msg + ", order="
				+ order + ", activeInfo=" + activeInfo + ", aliOrder="
				+ aliOrder + "]";
	}

	
}
