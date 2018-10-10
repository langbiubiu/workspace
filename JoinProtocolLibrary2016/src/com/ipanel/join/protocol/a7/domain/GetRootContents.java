package com.ipanel.join.protocol.a7.domain;

import java.io.Serializable;

import org.simpleframework.xml.Attribute;
import org.simpleframework.xml.Root;

/**
 * 获取一级栏目的信息
 * 
 * @author dzwillpower
 * 
 */
@Root(name = "GetRootContents")
public class GetRootContents implements Serializable {

	/**
	 * 
	 */
	private static final long serialVersionUID = -7032292116481827905L;
	@Attribute(name = "client")
	private String clientId;
	@Attribute(required = false)
	private String deviceId;
	@Attribute(required = false)
	private String account;
	@Attribute(required = false)
	private String languageCode;
	@Attribute(required = false)
	private String regionCode;
	@Attribute(required = false)
	private String startAt;
	@Attribute(required = false)
	private String maxItems;
	@Attribute(required = false)
	private String serviceType;
	@Attribute(required = false)
	private String profile;
	@Attribute(required=false)
	private String titleAssetId;
	@Attribute(required=false)
	private String portalId;
	public String getClientId() {
		return clientId;
	}
	public void setClientId(String clientId) {
		this.clientId = clientId;
	}
	public String getDeviceId() {
		return deviceId;
	}
	public void setDeviceId(String deviceId) {
		this.deviceId = deviceId;
	}
	public String getAccount() {
		return account;
	}
	public void setAccount(String account) {
		this.account = account;
	}
	public String getLanguageCode() {
		return languageCode;
	}
	public void setLanguageCode(String languageCode) {
		this.languageCode = languageCode;
	}
	public String getRegionCode() {
		return regionCode;
	}
	public void setRegionCode(String regionCode) {
		this.regionCode = regionCode;
	}
	public String getStartAt() {
		return startAt;
	}
	public void setStartAt(String startAt) {
		this.startAt = startAt;
	}
	public String getMaxItems() {
		return maxItems;
	}
	public void setMaxItems(String maxItems) {
		this.maxItems = maxItems;
	}
	public String getServiceType() {
		return serviceType;
	}
	public void setServiceType(String serviceType) {
		this.serviceType = serviceType;
	}
	public String getProfile() {
		return profile;
	}
	public void setProfile(String profile) {
		this.profile = profile;
	}
	
	public String getTitleAssetId() {
		return titleAssetId;
	}
	public void setTitleAssetId(String titleAssetId) {
		this.titleAssetId = titleAssetId;
	}
	public String getPortalId() {
		return portalId;
	}
	public void setPortalId(String portalId) {
		this.portalId = portalId;
	}
	@Override
	public String toString() {
		return "GetRootContents [clientId=" + clientId + ", deviceId=" + deviceId + ", account=" + account
				+ ", languageCode=" + languageCode + ", regionCode=" + regionCode + ", startAt=" + startAt
				+ ", maxItems=" + maxItems + ", serviceType=" + serviceType + ", profile=" + profile
				+ ", titleAssetId=" + titleAssetId + ", portalId=" + portalId + "]";
	}
	
	

}
