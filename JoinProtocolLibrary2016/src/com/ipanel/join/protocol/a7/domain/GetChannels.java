package com.ipanel.join.protocol.a7.domain;

import java.io.Serializable;

import org.simpleframework.xml.Attribute;
import org.simpleframework.xml.Root;

@Root(name = "GetChannels")
public class GetChannels implements Serializable {

	/**
	 * 
	 */
	private static final long serialVersionUID = -9083251741556217431L;

	@Attribute(required=false)
	private String clientId;

	@Attribute(required = false)
	private String deviceId;

	@Attribute(required=false)
	private String account;
    @Attribute(required=false)
    private String portalId;
    @Attribute(required=false)
    private String client;
    @Attribute(required=false)
    private String customerGroup;
    @Attribute(required=false)
    private String startAt;
    @Attribute(required=false)
    private String maxItems;
    
    
	public String getClient() {
		return client;
	}

	public void setClient(String client) {
		this.client = client;
	}

	public String getClientId() {
		return clientId;
	}

	public void setPortalId(String portalId) {
		this.portalId = portalId;
	}

	public String getPortalId() {
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

	public String getCustomerGroup() {
		return customerGroup;
	}

	public void setCustomerGroup(String customerGroup) {
		this.customerGroup = customerGroup;
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

	@Override
	public String toString() {
		return "GetChannels [clientId=" + clientId + ", deviceId=" + deviceId + ", account=" + account + ", portalId="
				+ portalId + ", client=" + client + ", customerGroup=" + customerGroup + ", startAt=" + startAt
				+ ", maxItems=" + maxItems + "]";
	}



	
}
