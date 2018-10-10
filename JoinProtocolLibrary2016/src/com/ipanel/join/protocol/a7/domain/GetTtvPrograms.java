package com.ipanel.join.protocol.a7.domain;

import org.simpleframework.xml.Attribute;
import org.simpleframework.xml.Root;

@Root(name = "GetTtvPrograms")
public class GetTtvPrograms {

	@Attribute(required=false)
	private String clientId;

	@Attribute(required = false)
	private String deviceId;

	@Attribute(required = false)
	private String channelIds;

	@Attribute(required = false)
	private String account;

	@Attribute(required = false)
	private String languageCode;

	@Attribute(required = false)
	private String regionCode;

	@Attribute(required = false)
	private int days;

	@Attribute(required = false)
	private int startAt;

	@Attribute(required = false)
	private String profile;

	@Attribute(required = false)
	private int mmaxItems;
	@Attribute(required=false)
	private String client;
	@Attribute(required=false)
	private String portalId;
	@Attribute(required=false)
	private String serviceType;
	@Attribute(required=false)
	private String startDateTime;

	public String getClientId() {
		return clientId;
	}

	public void setClientId(String portalId) {
		this.clientId = portalId;
	}

	public String getDeviceId() {
		return deviceId;
	}

	public void setDeviceId(String client) {
		this.deviceId = client;
	}

	public String getChannelIds() {
		return channelIds;
	}

	public void setChannelIds(String channelIds) {
		this.channelIds = channelIds;
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

	public int getDays() {
		return days;
	}

	public void setDays(int days) {
		this.days = days;
	}

	public int getStartAt() {
		return startAt;
	}

	public void setStartAt(int startAt) {
		this.startAt = startAt;
	}

	public String getProfile() {
		return profile;
	}

	public void setProfile(String profile) {
		this.profile = profile;
	}

	public int getMmaxItems() {
		return mmaxItems;
	}

	public void setMmaxItems(int mmaxItems) {
		this.mmaxItems = mmaxItems;
	}

	public String getClient() {
		return client;
	}

	public void setClient(String client) {
		this.client = client;
	}

	public String getPortalId() {
		return portalId;
	}

	public void setPortalId(String portalId) {
		this.portalId = portalId;
	}

	public String getServiceType() {
		return serviceType;
	}

	public void setServiceType(String serviceType) {
		this.serviceType = serviceType;
	}

	public String getStartDateTime() {
		return startDateTime;
	}

	public void setStartDateTime(String startDateTime) {
		this.startDateTime = startDateTime;
	}
	
	

}
