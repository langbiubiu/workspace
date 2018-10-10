package com.ipanel.join.chongqing.live.order;

import java.io.Serializable;

import com.google.gson.annotations.Expose;

/**
 * AliOrder��֧�����ƷѶ���������Ϣ��
 * @author Administrator
 *
 */
public class AliOrder implements Serializable {
	@Expose
	private String tradeNo; // code=202���أ�֧������ˮ��
	@Expose
	private String orderNo; // code=202���أ�ҵ�񶩵���
	@Expose
	private String aliOrderNo; // code=202���أ�֧����������
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
