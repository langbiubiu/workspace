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
	private String portalId; // 1 字符 10 Portal 对应的 ID 默认取 1
	@Attribute(required = false)
	private String client; // 1 字符 40 智能卡号
	@Attribute(required = false)
	private String deviceId; // ? 字符 32 终端唯一标识（stbSN）
	@Attribute(required = false)
	private String userType; // ? 字符 1用户类型，枚举值：0：VOD 用户，2：OTT 用户

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
