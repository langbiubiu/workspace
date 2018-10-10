package com.ipanel.join.protocol.sihua.cqvod.space;

import java.io.Serializable;

import org.simpleframework.xml.Attribute;
import org.simpleframework.xml.Root;

@Root(name="spaceMessage",strict=false)
public class SpaceMessage implements Serializable{
	/**
	 * 
	 */
	private static final long serialVersionUID = 8533263578729485215L;
	@Attribute(required=false)
	private String UUID;
	@Attribute(required=false)
	private String AppID;
	@Attribute(required=false)
	private String SpaceStatus;
	@Attribute(required=false)
	private String TotalSpace;
	@Attribute(required=false)
	private String UsedSpace;
	@Attribute(required=false)
	private String LockedSpace;
	@Attribute(required=false)
	private String ResultCode;
	@Attribute(required=false)
	private String Description;
	public String getUUID() {
		return UUID;
	}
	public void setUUID(String uUID) {
		UUID = uUID;
	}
	public String getAppID() {
		return AppID;
	}
	public void setAppID(String appID) {
		AppID = appID;
	}
	public String getSpaceStatus() {
		return SpaceStatus;
	}
	public void setSpaceStatus(String spaceStatus) {
		SpaceStatus = spaceStatus;
	}
	public String getTotalSpace() {
		return TotalSpace;
	}
	public void setTotalSpace(String totalSpace) {
		TotalSpace = totalSpace;
	}
	public String getUsedSpace() {
		return UsedSpace;
	}
	public void setUsedSpace(String usedSpace) {
		UsedSpace = usedSpace;
	}
	public String getLockedSpace() {
		return LockedSpace;
	}
	public void setLockedSpace(String lockedSpace) {
		LockedSpace = lockedSpace;
	}
	public String getResultCode() {
		return ResultCode;
	}
	public void setResultCode(String resultCode) {
		ResultCode = resultCode;
	}
	public String getDescription() {
		return Description;
	}
	public void setDescription(String description) {
		Description = description;
	}
	
	
}
