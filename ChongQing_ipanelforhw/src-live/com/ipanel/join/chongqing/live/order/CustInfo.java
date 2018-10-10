package com.ipanel.join.chongqing.live.order;

import java.io.Serializable;
import java.util.List;

import com.google.gson.annotations.Expose;

/***
 * CustInfo���û���Ϣ��
 * @author Administrator
 *
 */
public class CustInfo implements Serializable {
	@Expose
	private String accountId; // �˻���ţ����ڲ�ѯ�˵���
	@Expose
	private String custId; // �ͻ���ţ�BOSSϵͳ��ţ�:A
	@Expose
	private String custName; // �û���
	@Expose
	private String totleBalance; // �˻����������˱���ӣ�
	@Expose
	private String custCode; // �˻���
	@Expose
	private String mobile; // �ֻ�
	@Expose
	private String address; // ��ַ
	@Expose
	private String phone; // ��ϵ�绰��������
	@Expose
	private String ownCorpOrg; // �����������ڲ�ѯ�˵�
	@Expose
	private String customerType; // �ͻ�����1:����;3:����;4:����;0\2\5\6:���� 
	@Expose
	private List<Balance> balanceList; // �˻��������˱�
	@Expose
	private List<CA> userList; // �˻��������û��б�
	public String getAccountId() {
		return accountId;
	}
	public void setAccountId(String accountId) {
		this.accountId = accountId;
	}
	public String getCustId() {
		return custId;
	}
	public void setCustId(String custId) {
		this.custId = custId;
	}
	public String getCustName() {
		return custName;
	}
	public void setCustName(String custName) {
		this.custName = custName;
	}
	public String getTotleBalance() {
		return totleBalance;
	}
	public void setTotleBalance(String totleBalance) {
		this.totleBalance = totleBalance;
	}
	public String getCustCode() {
		return custCode;
	}
	public void setCustCode(String custCode) {
		this.custCode = custCode;
	}
	public String getMobile() {
		return mobile;
	}
	public void setMobile(String mobile) {
		this.mobile = mobile;
	}
	public String getAddress() {
		return address;
	}
	public void setAddress(String address) {
		this.address = address;
	}
	public String getPhone() {
		return phone;
	}
	public void setPhone(String phone) {
		this.phone = phone;
	}
	public String getOwnCorpOrg() {
		return ownCorpOrg;
	}
	public void setOwnCorpOrg(String ownCorpOrg) {
		this.ownCorpOrg = ownCorpOrg;
	}
	public String getCustomerType() {
		return customerType;
	}
	public void setCustomerType(String customerType) {
		this.customerType = customerType;
	}
	public List<Balance> getBalanceList() {
		return balanceList;
	}
	public void setBalanceList(List<Balance> balanceList) {
		this.balanceList = balanceList;
	}
	public List<CA> getUserList() {
		return userList;
	}
	public void setUserList(List<CA> userList) {
		this.userList = userList;
	}
	@Override
	public String toString() {
		return "CustInfo [accountId=" + accountId + ", custId=" + custId
				+ ", custName=" + custName + ", totleBalance=" + totleBalance
				+ ", custCode=" + custCode + ", mobile=" + mobile
				+ ", address=" + address + ", phone=" + phone + ", ownCorpOrg="
				+ ownCorpOrg + ", customerType=" + customerType
				+ ", balanceList=" + balanceList + ", userList=" + userList
				+ "]";
	}

}
