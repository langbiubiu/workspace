package com.ipanel.join.protocol.a7.domain;

import java.io.Serializable;

import org.simpleframework.xml.Attribute;
import org.simpleframework.xml.Root;

/**
 * 获取该栏目节点下子栏目、节目信息。包含间接节点下的信息
 * 
 * @author dzwillpower
 * 
 */
@Root(name="GetFolderContents")
public class GetFolderContents implements Serializable {

	/**
	 * 
	 */
	private static final long serialVersionUID = 9083718948553084966L;
	@Attribute(name="client")
	private String clientId;
	@Attribute(required = false)
	private String zip;
	@Attribute(required = false)
	private String deviceId;
	@Attribute(required = false)
	private String account;
	@Attribute(required = false)
	private String languageCode;
	@Attribute(required = false)
	private String providerId;
	@Attribute(required = false)
	private String assetId;
	@Attribute(required = false)
	private String includeFolderProperties;
	@Attribute(required = false)
	private String includeSubFolder;
	@Attribute(required = false)
	private String includeSelectableItem;
	@Attribute(required = false)
	private String depth;
	@Attribute(required = false)
	private String profile;
	@Attribute(required = false)
	private String maxItems;
	@Attribute(required=false)
	private String startAt;
	@Attribute(required=false)
	private String portalId;
	
	public String getPortalId() {
		return portalId;
	}
	public void setPortalId(String portalId) {
		this.portalId = portalId;
	}
	public String getStartAt() {
		return startAt;
	}
	public void setStartAt(String startAt) {
		this.startAt = startAt;
	}
	public String getClientId() {
		return clientId;
	}
	public void setClientId(String clientId) {
		this.clientId = clientId;
	}
	public String getZip() {
		return zip;
	}
	public void setZip(String zip) {
		this.zip = zip;
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
	public String getProviderId() {
		return providerId;
	}
	public void setProviderId(String providerId) {
		this.providerId = providerId;
	}
	public String getAssetId() {
		return assetId;
	}
	public void setAssetId(String assetId) {
		this.assetId = assetId;
	}
	public String getIncludeFolderProperties() {
		return includeFolderProperties;
	}
	public void setIncludeFolderProperties(String includeFolderProperties) {
		this.includeFolderProperties = includeFolderProperties;
	}
	public String getIncludeSubFolder() {
		return includeSubFolder;
	}
	public void setIncludeSubFolder(String includeSubFolder) {
		this.includeSubFolder = includeSubFolder;
	}
	public String getIncludeSelectableItem() {
		return includeSelectableItem;
	}
	public void setIncludeSelectableItem(String includeSelectableItem) {
		this.includeSelectableItem = includeSelectableItem;
	}
	public String getDepth() {
		return depth;
	}
	public void setDepth(String depth) {
		this.depth = depth;
	}
	public String getProfile() {
		return profile;
	}
	public void setProfile(String profile) {
		this.profile = profile;
	}
	public String getMaxItems() {
		return maxItems;
	}
	public void setMaxItems(String maxItems) {
		this.maxItems = maxItems;
	}
	@Override
	public String toString() {
		return "GetFolderContents [clientId=" + clientId + ", zip=" + zip + ", deviceId=" + deviceId + ", account="
				+ account + ", languageCode=" + languageCode + ", providerId=" + providerId + ", assetId=" + assetId
				+ ", includeFolderProperties=" + includeFolderProperties + ", includeSubFolder=" + includeSubFolder
				+ ", includeSelectableItem=" + includeSelectableItem + ", depth=" + depth + ", profile=" + profile
				+ ", maxItems=" + maxItems + ", startAt=" + startAt + ", portalId=" + portalId + "]";
	}
	
	
	

}
