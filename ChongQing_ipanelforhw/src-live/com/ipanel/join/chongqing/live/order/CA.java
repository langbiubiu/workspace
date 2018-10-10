package com.ipanel.join.chongqing.live.order;

import java.io.Serializable;
import java.util.List;

import com.google.gson.annotations.Expose;

/**
 * CA���û���
 * @author Administrator
 *
 */
public class CA implements Serializable {
	@Expose
	private String prodInstId; // �û����
	@Expose
	private String caId; // �ն˱�ţ����ܿ��� or MAC��
	@Expose
	private String caType; // �ն�����1:���ֵ���;2:���
//	@Expose
//	private int status; // �ն�״̬1������;2���쳣
	@Expose
	private String status; // �ն�״̬1������;2���쳣
	@Expose
	private String caModel; // ca�Ǹ��廹�Ǳ���HD������;SD������
	@Expose
	private int isMain; // ����������ʶ������ֱ���ײ�ʱ���룩0������;1������
	@Expose
	private List<ProductOrder> productOrderList; // �ѹ���ϸ
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
