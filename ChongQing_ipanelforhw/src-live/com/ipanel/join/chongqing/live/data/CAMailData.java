package com.ipanel.join.chongqing.live.data;

import java.io.Serializable;

@SuppressWarnings("serial")
public class CAMailData implements Serializable{
	private int number;
	private int status;
	private String title;
	private String sendDate;
	
	public CAMailData() {
		// TODO Auto-generated constructor stub
	}

	public int getNumber() {
		return number;
	}

	public void setNumber(int number) {
		this.number = number;
	}

	public int getStatus() {
		return status;
	}

	public void setStatus(int status) {
		this.status = status;
	}

	public String getTitle() {
		return title;
	}

	public void setTitle(String title) {
		this.title = title;
	}

	public String getSendDate() {
		return sendDate;
	}

	public void setSendDate(String sendDate) {
		this.sendDate = sendDate;
	}

}
