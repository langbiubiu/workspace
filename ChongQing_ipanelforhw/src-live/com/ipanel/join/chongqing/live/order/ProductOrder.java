package com.ipanel.join.chongqing.live.order;

import java.io.Serializable;
import java.util.List;

import com.google.gson.annotations.Expose;

/**
 * ProductOrder���ѹ���ϸ��
 * @author Administrator
 *
 */
public class ProductOrder implements Serializable {
	@Expose
	private String offerId; // �ײͱ��
	@Expose
	private String offerName; // �ײ�����
	@Expose
	private String lineOrPoint; // ��ֱ�����ǵ㲥line:ֱ��;point:�㲥
	@Expose
	private String offerInstId; // �ײ�ʵ��ID�������ڵ��ö���ֱ���ײͽӿڣ�
	@Expose
	private List<Product> productList; // ��Ʒ�б�
	public String getOfferId() {
		return offerId;
	}
	public void setOfferId(String offerId) {
		this.offerId = offerId;
	}
	public String getOfferName() {
		return offerName;
	}
	public void setOfferName(String offerName) {
		this.offerName = offerName;
	}
	public String getLineOrPoint() {
		return lineOrPoint;
	}
	public void setLineOrPoint(String lineOrPoint) {
		this.lineOrPoint = lineOrPoint;
	}
	public String getOfferInstId() {
		return offerInstId;
	}
	public void setOfferInstId(String offerInstId) {
		this.offerInstId = offerInstId;
	}
	public List<Product> getProductList() {
		return productList;
	}
	public void setProductList(List<Product> productList) {
		this.productList = productList;
	}
	@Override
	public String toString() {
		return "ProductOrder [offerId=" + offerId + ", offerName=" + offerName
				+ ", lineOrPoint=" + lineOrPoint + ", offerInstId="
				+ offerInstId + ", productList=" + productList + "]";
	}
}
