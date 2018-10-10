package com.ipanel.join.chongqing.live.order;

import java.io.Serializable;
import java.util.List;

import com.google.gson.annotations.Expose;

/***
 * CustInfo（用户信息）
 * @author Administrator
 *
 */
public class CustInfo implements Serializable {
	@Expose
	private String accountId; // 账户编号（用于查询账单）
	@Expose
	private String custId; // 客户编号（BOSS系统编号）:A
	@Expose
	private String custName; // 用户名
	@Expose
	private String totleBalance; // 账户总余额（所以账本相加）
	@Expose
	private String custCode; // 账户号
	@Expose
	private String mobile; // 手机
	@Expose
	private String address; // 地址
	@Expose
	private String phone; // 联系电话（座机）
	@Expose
	private String ownCorpOrg; // 所属区域，用于查询账单
	@Expose
	private String customerType; // 客户类型1:公众;3:集团;4:政企;0\2\5\6:其他 
	@Expose
	private List<Balance> balanceList; // 账户下所有账本
	@Expose
	private List<CA> userList; // 账户下所有用户列表
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
