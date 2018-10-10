package com.ipanel.join.cq.user.recoding;

public class Recoding {
	String name;
	String tv;
	String state;

	Recoding (String name, String tv, String state){
		this.name = name;
		this.tv = tv;
		this.state = state;

	}
	public String getName() {
		return name;
	}
	public void setName(String name) {
		this.name = name;
	}
	public String getTv() {
		return tv;
	}
	public void setTv(String tv) {
		this.tv = tv;
	}
	public String getState() {
		return state;
	}
	public void setState(String state) {
		this.state = state;
	}

	

}
