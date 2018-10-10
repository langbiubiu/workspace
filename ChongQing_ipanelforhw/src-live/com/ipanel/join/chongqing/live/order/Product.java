package com.ipanel.join.chongqing.live.order;

import java.io.Serializable;
import java.util.List;

import com.google.gson.annotations.Expose;

/**
 * Product（产品明细）
 * @author Administrator
 *
 */
public class Product implements Serializable {
	@Expose
	private String productId; // 产品ID
	@Expose
	private String productInstId; // 产品实例ID
	@Expose
	private String productName; // 产品名称
	@Expose
	private String productStatus; // 产品状态
	@Expose
	private String productPrice; // 产品价格
	@Expose
	private String productDesc; // 产品描述
	@Expose
	private String validDate; // 生效日期
	@Expose
	private String expireDate; // 失效日期
	@Expose
	private List<PPV> ppvList; // 产品包含的PPV列表，可根据实际情况进行使用
	public String getProductId() {
		return productId;
	}
	public void setProductId(String productId) {
		this.productId = productId;
	}
	public String getProductInstId() {
		return productInstId;
	}
	public void setProductInstId(String productInstId) {
		this.productInstId = productInstId;
	}
	public String getProductName() {
		return productName;
	}
	public void setProductName(String productName) {
		this.productName = productName;
	}
	public String getProductStatus() {
		return productStatus;
	}
	public void setProductStatus(String productStatus) {
		this.productStatus = productStatus;
	}
	public String getProductPrice() {
		return productPrice;
	}
	public void setProductPrice(String productPrice) {
		this.productPrice = productPrice;
	}
	public String getProductDesc() {
		return productDesc;
	}
	public void setProductDesc(String productDesc) {
		this.productDesc = productDesc;
	}
	public String getValidDate() {
		return validDate;
	}
	public void setValidDate(String validDate) {
		this.validDate = validDate;
	}
	public String getExpireDate() {
		return expireDate;
	}
	public void setExpireDate(String expireDate) {
		this.expireDate = expireDate;
	}
	public List<PPV> getPpvList() {
		return ppvList;
	}
	public void setPpvList(List<PPV> ppvList) {
		this.ppvList = ppvList;
	}
	@Override
	public String toString() {
		return "Product [productId=" + productId + ", productInstId="
				+ productInstId + ", productName=" + productName
				+ ", productStatus=" + productStatus + ", productPrice="
				+ productPrice + ", productDesc=" + productDesc
				+ ", validDate=" + validDate + ", expireDate=" + expireDate
				+ ", ppvList=" + ppvList + "]";
	}
	
}
