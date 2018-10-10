package com.ipanel.join.protocol.a7.domain;

import java.io.Serializable;

import org.simpleframework.xml.Attribute;
import org.simpleframework.xml.Root;

/**
 * ��Ҫ���ڸ����û������Ӱ�ӵ㲥��Ŀ��Ż�ȡ��Ӧ��Ŀ��Ӱ�ӵ㲥�����б������� �������ȡ�����������б�
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
	 * ��Ŀ���ͣ�������ֶ�Ϊ�գ�����
	 * �֣�������������
	 * ���������� teleplay��ϵ�о�
	 * Series, �� �� Sports, �� ��
	 * Music,��� Ad,���������̾�
	 * Miniseries,��Ӱ Movie,����
	 * New������ Other
	 * </pre>
	 */
	@Attribute(required = false)
	public String showType;

	/**
	 * ������ĿID��������ֶ�Ϊ�գ��� ȡ����Ŀ���У������ȡ��Ӧ��Ŀ ID�µ�����
	 */
	@Attribute(required = false)
	public String folderAssetId;

	/**
	 * �� �� �� �� Դ �� �� �� �� �� �� SelectableItem �ڵ��У�Ĭ��Ϊ ��1-�ǣ�0-��
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
