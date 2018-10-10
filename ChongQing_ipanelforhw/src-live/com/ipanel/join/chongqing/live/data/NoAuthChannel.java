package com.ipanel.join.chongqing.live.data;

import java.io.Serializable;

import com.google.gson.annotations.Expose;

/**
 * 未授权频道，获取预览频点时的频道类
 */
public class NoAuthChannel implements Serializable {

	/**
	 * 
	 */
	private static final long serialVersionUID = 4023835362354984446L;
	
	@Expose
	private String serviceId;
	
	@Expose
	private String name;
	
	@Expose
	private String info;
	
	@Expose
	private String productid;
	
	@Expose
	private String price;
	
	@Expose
	private String previewid;
	
	@Expose
	private String frequency;
	
	
	public String getServiceId() {
		return serviceId;
	}
	public void setServiceId(String serviceId) {
		this.serviceId = serviceId;
	}
	public String getName() {
		return name;
	}
	public void setName(String name) {
		this.name = name;
	}
	public String getInfo() {
		return info;
	}
	public void setInfo(String info) {
		this.info = info;
	}
	public String getProductid() {
		return productid;
	}
	public void setProductid(String productid) {
		this.productid = productid;
	}
	public String getPrice() {
		return price;
	}
	public void setPrice(String price) {
		this.price = price;
	}
	public String getPreviewid() {
		return previewid;
	}
	public void setPreviewid(String previewid) {
		this.previewid = previewid;
	}
	public String getFrequency() {
		return frequency;
	}
	public void setFrequency(String frequency) {
		this.frequency = frequency;
	}
	
	
}
