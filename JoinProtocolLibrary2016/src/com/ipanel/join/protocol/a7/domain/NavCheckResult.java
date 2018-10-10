package com.ipanel.join.protocol.a7.domain;

import java.io.Serializable;

import org.simpleframework.xml.Attribute;
import org.simpleframework.xml.Element;
import org.simpleframework.xml.Root;
@Root(name="NavCheckResult",strict=false)
public class NavCheckResult implements Serializable{

	/**
	 * 
	 */
	public static final long serialVersionUID = 2438395124337586620L;
	@Attribute(required=false)
	private String pushStreamType;
	@Attribute(required=false)
	public String account;//设备账户(UserID,由BOSS/SMS同步的用户的账号信息)
	@Attribute(required=false)
	public String customerGroup;//	属性，String	终端所处的用户组
	@Element(name="ZoneFreqInfo",required=false)
	public ZoneFreqInfo zoneFreqInfo;//	属性，Element	区域频点列表（ODC根据区域频点锁频获取TSID作为区域码，S1规范中的QAM name可使用此TSID）


	@Root(name="ZoneFreqInfo",strict=false)
	public static class ZoneFreqInfo implements Serializable{

		/**
		 * 
		 */
		public static final long serialVersionUID = -9034659318674920925L;
		@Attribute(required=false)
		public String qamMode;//	属性，String	调制模式
		@Attribute(required=false)
		public String symbolRate;	//属性，String	符号率
		@Attribute(required=false)
		public String frequency;	//属性，String	频点频率（Hz）
		public String getQamMode() {
			return qamMode;
		}
		public void setQamMode(String qamMode) {
			this.qamMode = qamMode;
		}
		public String getSymbolRate() {
			return symbolRate;
		}
		public void setSymbolRate(String symbolRate) {
			this.symbolRate = symbolRate;
		}
		public String getFrequence() {
			return frequency;
		}
		public void setFrequence(String frequence) {
			this.frequency = frequence;
		}
		@Override
		public String toString() {
			return "ZoneFreqInfo [qamMode=" + qamMode + ", symbolRate=" + symbolRate + ", frequence=" + frequency + "]";
		}
		

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


	public ZoneFreqInfo getZoneFreqInfo() {
		return zoneFreqInfo;
	}


	public void setZoneFreqInfo(ZoneFreqInfo zoneFreqInfo) {
		this.zoneFreqInfo = zoneFreqInfo;
	}


	public String getPushStreamType() {
		return pushStreamType;
	}


	public void setPushStreamType(String pushStreamType) {
		this.pushStreamType = pushStreamType;
	}


	@Override
	public String toString() {
		return "NavCheckResult [pushStreamType=" + pushStreamType + ", account=" + account + ", customerGroup="
				+ customerGroup + ", zoneFreqInfo=" + zoneFreqInfo + "]";
	}
}
