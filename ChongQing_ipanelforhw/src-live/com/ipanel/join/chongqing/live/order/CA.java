package com.ipanel.join.chongqing.live.order;

import java.io.Serializable;
import java.util.List;

import com.google.gson.annotations.Expose;

/**
 * CA（用户）
 * @author Administrator
 *
 */
public class CA implements Serializable {
	@Expose
	private String prodInstId; // 用户编号
	@Expose
	private String caId; // 终端编号（智能卡号 or MAC）
	@Expose
	private String caType; // 终端类型1:数字电视;2:宽带
//	@Expose
//	private int status; // 终端状态1：正常;2：异常
	@Expose
	private String status; // 终端状态1：正常;2：异常
	@Expose
	private String caModel; // ca是高清还是标清HD：高清;SD：标清
	@Expose
	private int isMain; // 主机副机标识（订购直播套餐时传入）0：主机;1：副机
	@Expose
	private List<ProductOrder> productOrderList; // 已购明细
	public String getProdInstId() {
		return prodInstId;
	}
	public void setProdInstId(String prodInstId) {
		this.prodInstId = prodInstId;
	}
	public String getCaId() {
		return caId;
	}
	public void setCaId(String caId) {
		this.caId = caId;
	}
	public String getCaType() {
		return caType;
	}
	public void setCaType(String caType) {
		this.caType = caType;
	}
//	public int getStatus() {
//		return status;
//	}
//	public void setStatus(int status) {
//		this.status = status;
//	}
	public String getStatus() {
		return status;
	}
	public void setStatus(String status) {
		this.status = status;
	}
	public String getCaModel() {
		return caModel;
	}
	public void setCaModel(String caModel) {
		this.caModel = caModel;
	}
	public int getIsMain() {
		return isMain;
	}
	public void setIsMain(int isMain) {
		this.isMain = isMain;
	}
	public List<ProductOrder> getProductOrderList() {
		return productOrderList;
	}
	public void setProductOrderList(List<ProductOrder> productOrderList) {
		this.productOrderList = productOrderList;
	}
	@Override
	public String toString() {
		return "CA [prodInstId=" + prodInstId + ", caId=" + caId + ", caType="
				+ caType + ", status=" + status + ", caModel=" + caModel
				+ ", isMain=" + isMain + ", productOrderList="
				+ productOrderList + "]";
	}

}
