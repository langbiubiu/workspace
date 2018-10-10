package com.ipanel.join.protocol.a7.domain;

import java.io.Serializable;

import org.simpleframework.xml.Attribute;
import org.simpleframework.xml.Root;

@Root(name = "ChannelSelectionStart")
public class ChannelSelectionStart implements Serializable {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1670046135317287820L;

	@Attribute(required=false)
	private String clientId;

	@Attribute(required=false)
	private String deviceId;

	@Attribute(required=false)
	private String account;

	@Attribute(required = false)
	private String assetId;

	@Attribute(required=false)
	private String channelId;

	@Attribute(required = false)
	private String startDateTime;

	@Attribute(required=false)
	private String titleAssetId;
	@Attribute(required=false)
	private String folderAssetId;
	@Attribute(required=false)
	private String serviceId;
	@Attribute(required=false)
	private String portalId;
	@Attribute(required=false)
	private String client;
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

	public String getAccount() {
		return account;
	}

	public void setAccount(String account) {
		this.account = account;
	}

	public String getChannelId() {
		return channelId;
	}

	public void setChannelId(String channelId) {
		this.channelId = channelId;
	}

	public String getStartDateTime() {
		return startDateTime;
	}

	public void setStartDateTime(String startDateTime) {
		this.startDateTime = startDateTime;
	}

	public String getAssetId() {
		return assetId;
	}

	public void setAssetId(String assetId) {
		this.assetId = assetId;
	}

	public String getTitleAssetId() {
		return titleAssetId;
	}

	public void setTitleAssetId(String titleAssetId) {
		this.titleAssetId = titleAssetId;
	}

	public String getFolderAssetId() {
		return folderAssetId;
	}

	public void setFolderAssetId(String folderAssetId) {
		this.folderAssetId = folderAssetId;
	}

	public String getServiceId() {
		return serviceId;
	}

	public void setServiceId(String serviceId) {
		this.serviceId = serviceId;
	}

	public String getPortalId() {
		return portalId;
	}

	public void setPortalId(String portalId) {
		this.portalId = portalId;
	}

	public String getClient() {
		return client;
	}

	public void setClient(String client) {
		this.client = client;
	}
	

}
