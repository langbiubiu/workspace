package com.ipanel.join.protocol.sihua.cqvod.space;

import java.io.Serializable;

import org.simpleframework.xml.Attribute;
import org.simpleframework.xml.Root;

@Root(name="query",strict=false)
public class CapacityQuery implements Serializable{

	
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	@Attribute(required=false)
	private String UUID;
	@Attribute(required=false)
	private String SPID;
	@Attribute(required=false)
	private String AppID;
	@Attribute(required=false)
	private String AccessToken;
	public String getUUID() {
		return UUID;
	}
	public void setUUID(String uUID) {
		UUID = uUID;
	}
	public String getSPID() {
		return SPID;
	}
	public void setSPID(String sPID) {
		SPID = sPID;
	}
	public String getAppID() {
		return AppID;
	}
	public void setAppID(String appID) {
		AppID = appID;
	}
	public String getAccessToken() {
		return AccessToken;
	}
	public void setAccessToken(String accessToken) {
		AccessToken = accessToken;
	}
	
	
}
