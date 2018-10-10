package com.ipanel.join.cq.data;

import java.util.HashMap;
import java.util.List;

public class ReplayChannel {
	private String channelID;
	private String name;
	private String logo;
	private String type;
	private HashMap<String, List<ReplayProgram>> programs = new HashMap<String, List<ReplayProgram>>();

	public String getChannelID() {
		return channelID;
	}

	public void setChannelID(String channelID) {
		this.channelID = channelID;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public HashMap<String, List<ReplayProgram>> getPrograms() {
		return programs;
	}

	public void setPrograms(HashMap<String, List<ReplayProgram>> programs) {
		this.programs = programs;
	}

	public String getLogo() {
//		if(logo==null||"".equals(logo)){
//			String n=getName();
//			if(getName().endsWith("Œ¿ ”")){
//				n=n.replace("Œ¿ ”", "");
//			}
//			logo=Constant.getChannelUrl(n, BackApplication.getInstance());
//		}
		return logo;
	}

	public void setLogo(String logo) {
		this.logo = logo;
	}

	public String getType() {
		return type;
	}

	public void setType(String type) {
		this.type = type;
	}
	

}