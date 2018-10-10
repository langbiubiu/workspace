package com.ipanel.join.chongqing.live.order;

import java.io.Serializable;
import java.util.List;

import com.google.gson.annotations.Expose;

/**
 * Product����Ʒ��ϸ��
 * @author Administrator
 *
 */
public class Product implements Serializable {
	@Expose
	private String productId; // ��ƷID
	@Expose
	private String productInstId; // ��Ʒʵ��ID
	@Expose
	private String productName; // ��Ʒ����
	@Expose
	private String productStatus; // ��Ʒ״̬
	@Expose
	private String productPrice; // ��Ʒ�۸�
	@Expose
	private String productDesc; // ��Ʒ����
	@Expose
	private String validDate; // ��Ч����
	@Expose
	private String expireDate; // ʧЧ����
	@Expose
	private List<PPV> ppvList; // ��Ʒ������PPV�б��ɸ���ʵ���������ʹ��
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
