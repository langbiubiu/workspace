package com.ipanel.join.protocol.a7.domain;

import java.io.Serializable;

import org.simpleframework.xml.Attribute;
import org.simpleframework.xml.Root;

/**
 * 主要用于根据用户传入的影视点播栏目编号获取相应栏目的影视点播排行列表，不传此 参数则获取所有总排行列表
 * 
 * @author Zexu
 * 
 */
@Root(name = "GetOnDemandRanking")
public class GetOnDemandRanking implements Serializable {

	/**
	 * 
	 */
	private static final long serialVersionUID = 9083718948553084966L;
	@Attribute(name = "client")
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
	private String profile;
	@Attribute(required = false)
	private String maxItems;
	@Attribute(required = false)
	private String startAt;
	@Attribute(required = false)
	private String portalId;

	/**
	 * <pre>
	 * 节目类型：如果该字段为空，不区
	 * 分，返回所有类型
	 * 电视连续剧 teleplay，系列剧
	 * Series, 体 育 Sports, 音 乐
	 * Music,广告 Ad,电视连续短剧
	 * Miniseries,电影 Movie,新闻
	 * New、其它 Other
	 * </pre>
	 */
	@Attribute(required = false)
	public String showType;

	/**
	 * 请求栏目ID，如果该字段为空，获 取总栏目排行，否则获取对应栏目 ID下的排行
	 */
	@Attribute(required = false)
	public String folderAssetId;

	/**
	 * 是 否 将 资 源 包 数 据 放 置 到 SelectableItem 节点中，默认为 否，1-是，0-否
	 */
	@Attribute(required = false)
	public int mergeTV;

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

}
