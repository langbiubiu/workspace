package com.ipanel.join.chongqing.live.order;

import java.io.Serializable;

import com.google.gson.annotations.Expose;

/**
 * �ײ�������Ӧ
 * @author Administrator
 *
 */
public class OrderResponse implements Serializable {
	@Expose
	private String code; // ������(200��ֱ�Ӷ����ɹ���201����֧�����󶩹����������ͨ�ƷѶ�����202����֧�����󶩹������֧�����ƷѶ���������������ʧ��)
	@Expose
	private String msg; // ��ϸ��Ϣ���ɹ� or ʧ��ԭ��
	@Expose
	private Order order; // ������Ϣ
	@Expose
	private ActivityInfo activeInfo; // �н���Ϣ
	@Expose
	private AliOrder aliOrder; // ֧�����ƷѶ��������code=202ʱ������Ϣ
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
