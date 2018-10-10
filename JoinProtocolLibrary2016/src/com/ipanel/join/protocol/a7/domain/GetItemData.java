package com.ipanel.join.protocol.a7.domain;

import java.io.Serializable;

import org.simpleframework.xml.Attribute;
import org.simpleframework.xml.Root;

@Root(name="GetItemData",strict=false)
public class GetItemData implements Serializable {

	/**
	 * 
	 */
	private static final long serialVersionUID = -1718230688102780881L;
	@Attribute(required =false,name="client")
	private String clientId;
	@Attribute(required =false)
	private String deviceId;
	@Attribute(required =false)
	private String account;
	@Attribute(required =false)
	private String languageCode;
	@Attribute(required =false)
	private String titleProviderIdString;
	@Attribute(required =false)
	private String titleAssetId;
	@Attribute(required =false)
	private String profile;
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
	public String getTitleProviderIdString() {
		return titleProviderIdString;
	}
	public void setTitleProviderIdString(String titleProviderIdString) {
		this.titleProviderIdString = titleProviderIdString;
	}
	public String getTitleAssetId() {
		return titleAssetId;
	}
	public void setTitleAssetId(String titleAssetId) {
		this.titleAssetId = titleAssetId;
	}
	public String getProfile() {
		return profile;
	}
	public void setProfile(String profile) {
		this.profile = profile;
	}
	@Override
	public String toString() {
		return "GetItemData [clientId=" + clientId + ", deviceId=" + deviceId
				+ ", account=" + account + ", languageCode=" + languageCode
				+ ", titleProviderIdString=" + titleProviderIdString
				+ ", titleAssetId=" + titleAssetId + ", profile=" + profile
				+ "]";
	}
	
	


}
