package com.ipanel.join.chongqing.portal;

import java.io.Serializable;

public class MailData implements Serializable {
	private String emergencyMailTheme;
	private String emergencyMailContent;
	private String emergencyMailId;
	private String notReadNum;

	public String getEmergencyMailTheme() {
		return emergencyMailTheme;
	}

	public void setEmergencyMailTheme(String emergencyMailTheme) {
		this.emergencyMailTheme = emergencyMailTheme;
	}

	public String getEmergencyMailContent() {
		return emergencyMailContent;
	}

	public void setEmergencyMailContent(String emergencyMailContent) {
		this.emergencyMailContent = emergencyMailContent;
	}

	public String getEmergencyMailId() {
		return emergencyMailId;
	}

	public void setEmergencyMailId(String emergencyMailId) {
		this.emergencyMailId = emergencyMailId;
	}

	public String getNotReadNum() {
		return notReadNum;
	}

	public void setNotReadNum(String notReadNum) {
		this.notReadNum = notReadNum;
	}
}
