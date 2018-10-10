package com.ipanel.join.protocol.a7.domain;

import java.io.Serializable;

import org.simpleframework.xml.Attribute;
import org.simpleframework.xml.Root;

@Root(strict = false, name = "NavCheck")
public class NavCheck implements Serializable {

	/**
	 * 
	 */
	private static final long serialVersionUID = -4702261288419687355L;
	@Attribute(required = false)
	private String portalId; // 1 �ַ� 10 Portal ��Ӧ�� ID Ĭ��ȡ 1
	@Attribute(required = false)
	private String client; // 1 �ַ� 40 ���ܿ���
	@Attribute(required = false)
	private String deviceId; // ? �ַ� 32 �ն�Ψһ��ʶ��stbSN��
	@Attribute(required = false)
	private String userType; // ? �ַ� 1�û����ͣ�ö��ֵ��0��VOD �û���2��OTT �û�

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

	public String getDeviceId() {
		return deviceId;
	}

	public void setDeviceId(String deviceId) {
		this.deviceId = deviceId;
	}

	public String getUserType() {
		return userType;
	}

	public void setUserType(String userType) {
		this.userType = userType;
	}

	@Override
	public String toString() {
		return "NavCheck [portalId=" + portalId + ", client=" + client + ", deviceId=" + deviceId + ", userType="
				+ userType + "]";
	}

}
