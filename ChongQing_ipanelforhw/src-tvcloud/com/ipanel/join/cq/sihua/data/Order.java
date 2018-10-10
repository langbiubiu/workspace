package com.ipanel.join.cq.sihua.data;

import java.io.Serializable;

import org.simpleframework.xml.Attribute;

public class Order implements Serializable{

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	@Attribute(required=false)
	private String AccessToken;
	@Attribute(required=false)
	private String Action;
	@Attribute(required=false)
	private String UUID;
	@Attribute(required=false)
	private String SPID;
	@Attribute(required=false)
	private String AppID;
	@Attribute(required=false)
	private String Code;
	@Attribute(required=false)
	private String ChannelID;
	@Attribute(required=false)
	private String ProgramID;
	@Attribute(required=false)
	private String RecordType;
	@Attribute(required=false)
	private String BeginTime;
	@Attribute(required=false)
	private String EndTime;
	@Attribute(required=false)
	private String OrderTime;
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
	public String getAction() {
		return Action;
	}
	public void setAction(String action) {
		Action = action;
	}
	public String getCode() {
		return Code;
	}
	public void setCode(String code) {
		Code = code;
	}
	public String getChannelID() {
		return ChannelID;
	}
	public void setChannelID(String channelID) {
		ChannelID = channelID;
	}
	public String getProgramID() {
		return ProgramID;
	}
	public void setProgramID(String programID) {
		ProgramID = programID;
	}
	public String getRecordType() {
		return RecordType;
	}
	public void setRecordType(String recordType) {
		RecordType = recordType;
	}
	public String getBeginTime() {
		return BeginTime;
	}
	public void setBeginTime(String beginTime) {
		BeginTime = beginTime;
	}
	public String getEndTime() {
		return EndTime;
	}
	public void setEndTime(String endTime) {
		EndTime = endTime;
	}
	public String getOrderTime() {
		return OrderTime;
	}
	public void setOrderTime(String orderTime) {
		OrderTime = orderTime;
	}
	public String getAccessToken() {
		return AccessToken;
	}
	public void setAccessToken(String accessToken) {
		AccessToken = accessToken;
	}
	
	
}
