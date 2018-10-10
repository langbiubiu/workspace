package com.ipanel.join.chongqing.live.order;

import java.io.Serializable;
import java.util.List;

import com.google.gson.annotations.Expose;

/**
 * ProductOrder（已购明细）
 * @author Administrator
 *
 */
public class ProductOrder implements Serializable {
	@Expose
	private String offerId; // 套餐编号
	@Expose
	private String offerName; // 套餐名称
	@Expose
	private String lineOrPoint; // 是直播还是点播line:直播;point:点播
	@Expose
	private String offerInstId; // 套餐实例ID，（用于调用订购直播套餐接口）
	@Expose
	private List<Product> productList; // 产品列表
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
