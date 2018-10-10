package com.ipanel.join.protocol.a7.domain;

import java.io.Serializable;

import org.simpleframework.xml.Attribute;
import org.simpleframework.xml.Root;

@Root(name="GetAssociatedFolderContents")
public class GetAssociatedFolderContents implements Serializable {

	/**
	 * 
	 */
	private static final long serialVersionUID = -3638022740234968163L;
	@Attribute(required=false)
	private String clientId;
	@Attribute(required=false)
	private String deviceId;
	@Attribute(required=false)
	private String account;
	@Attribute(required=false)
	private String languageCode;
	@Attribute(required=false)
	private String quickId;
	@Attribute(required=false)
	private String startAt;
	@Attribute(required=false)
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
	public String getQuickId() {
		return quickId;
	}
	public void setQuickId(String quickId) {
		this.quickId = quickId;
	}
	public String getStartAt() {
		return startAt;
	}
	public void setStartAt(String startAt) {
		this.startAt = startAt;
	}
	public String getProfile() {
		return profile;
	}
	public void setProfile(String profile) {
		this.profile = profile;
	}
	@Override
	public String toString() {
		return "GetAssociatedFolderContents [clientId=" + clientId
				+ ", deviceId=" + deviceId + ", account=" + account
				+ ", languageCode=" + languageCode + ", quickId=" + quickId
				+ ", startAt=" + startAt + ", profile=" + profile + "]";
	}
	
	


}
