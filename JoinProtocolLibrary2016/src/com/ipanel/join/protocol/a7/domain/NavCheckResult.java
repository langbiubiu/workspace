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
	public String account;//�豸�˻�(UserID,��BOSS/SMSͬ�����û����˺���Ϣ)
	@Attribute(required=false)
	public String customerGroup;//	���ԣ�String	�ն��������û���
	@Element(name="ZoneFreqInfo",required=false)
	public ZoneFreqInfo zoneFreqInfo;//	���ԣ�Element	����Ƶ���б�ODC��������Ƶ����Ƶ��ȡTSID��Ϊ�����룬S1�淶�е�QAM name��ʹ�ô�TSID��


	@Root(name="ZoneFreqInfo",strict=false)
	public static class ZoneFreqInfo implements Serializable{

		/**
		 * 
		 */
		public static final long serialVersionUID = -9034659318674920925L;
		@Attribute(required=false)
		public String qamMode;//	���ԣ�String	����ģʽ
		@Attribute(required=false)
		public String symbolRate;	//���ԣ�String	������
		@Attribute(required=false)
		public String frequency;	//���ԣ�String	Ƶ��Ƶ�ʣ�Hz��
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
