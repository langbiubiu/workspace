package com.ipanel.join.cq.data;

import com.ipanel.join.chongqing.live.util.TimeHelper;

public class ReplayProgram {
	private String name;
	private String url;
	private String time;
	private String date;

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getUrl() {
		return url;
	}

	public void setUrl(String url) {
		this.url = url;
	}

	public String getTime() {
		return time;
	}

	public void setTime(String time) {
		this.time = time;
	}

	public String getDate() {
		return date;
	}

	public void setDate(String date) {
		this.date = date;
	}

	public String getMillisecond() {
		return TimeHelper.getMillisecond(date, time) + "";
	}

}