package com.ipanel.join.protocol.a7.domain;

import java.io.Serializable;

import org.simpleframework.xml.Attribute;
import org.simpleframework.xml.Root;
@Root(name="SelectionStart",strict=false)
public class SelectionStart implements Serializable{
	/**
	 * 
	 */
	private static final long serialVersionUID = -2548451656064932823L;
	@Attribute(required=false)
	private String portalId;
	@Attribute(required=false)
	private String clientId	;
	@Attribute(required=false)
	private String client;
	@Attribute(required=false)
	private String account;
	@Attribute(required=false)
	private String deviceId;
	@Attribute(required=false)
	private String titleProviderId;
	@Attribute(required=false)
	private String titleAssetId;
	@Attribute(required=false)
	private String audioLanguage;
	@Attribute(required=false)
	private String folderAssetId;
	@Attribute(required=false)
	private String subtitleLanguage;
	@Attribute(required=false)
	private String format;
	@Attribute(required=false)
	private String indefiniteRental;
	@Attribute(required=false)
	private String rentalPeriod;
	@Attribute(required=false)
	private String price;
	@Attribute(required=false)
	private String playPreview;
	@Attribute(required=false)
	private String serviceId;
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
	public String getTitleProviderId() {
		return titleProviderId;
	}
	public void setTitleProviderId(String titleProviderId) {
		this.titleProviderId = titleProviderId;
	}
	public String getTitleAssetId() {
		return titleAssetId;
	}
	public void setTitleAssetId(String titleAssetId) {
		this.titleAssetId = titleAssetId;
	}
	public String getAudioLanguage() {
		return audioLanguage;
	}
	public void setAudioLanguage(String audioLanguage) {
		this.audioLanguage = audioLanguage;
	}
	public String getFolderAssetId() {
		return folderAssetId;
	}
	public void setFolderAssetId(String folderAssetId) {
		this.folderAssetId = folderAssetId;
	}
	public String getSubtitleLanguage() {
		return subtitleLanguage;
	}
	public void setSubtitleLanguage(String subtitleLanguage) {
		this.subtitleLanguage = subtitleLanguage;
	}
	public String getFormat() {
		return format;
	}
	public void setFormat(String format) {
		this.format = format;
	}
	public String getIndefiniteRental() {
		return indefiniteRental;
	}
	public void setIndefiniteRental(String indefiniteRental) {
		this.indefiniteRental = indefiniteRental;
	}
	public String getRentalPeriod() {
		return rentalPeriod;
	}
	public void setRentalPeriod(String rentalPeriod) {
		this.rentalPeriod = rentalPeriod;
	}
	public String getPrice() {
		return price;
	}
	public void setPrice(String price) {
		this.price = price;
	}
	public String getPlayPreview() {
		return playPreview;
	}
	public void setPlayPreview(String playPreview) {
		this.playPreview = playPreview;
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
	public String getAccount() {
		return account;
	}
	public void setAccount(String account) {
		this.account = account;
	}
	public String getServiceId() {
		return serviceId;
	}
	public void setServiceId(String serviceId) {
		this.serviceId = serviceId;
	}
	@Override
	public String toString() {
		return "SelectionStart [portalId=" + portalId + ", clientId=" + clientId + ", client=" + client + ", account="
				+ account + ", deviceId=" + deviceId + ", titleProviderId=" + titleProviderId + ", titleAssetId="
				+ titleAssetId + ", audioLanguage=" + audioLanguage + ", folderAssetId=" + folderAssetId
				+ ", subtitleLanguage=" + subtitleLanguage + ", format=" + format + ", indefiniteRental="
				+ indefiniteRental + ", rentalPeriod=" + rentalPeriod + ", price=" + price + ", playPreview="
				+ playPreview + ", serviceId=" + serviceId + "]";
	}

}
