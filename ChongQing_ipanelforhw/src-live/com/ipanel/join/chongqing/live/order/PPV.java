package com.ipanel.join.chongqing.live.order;

import java.io.Serializable;

import com.google.gson.annotations.Expose;

public class PPV implements Serializable {
	@Expose
	private String ppvId; // PPV ID
	@Expose
	private String ppvName; // ppvÃû³Æ
	public String getPpvId() {
		return ppvId;
	}
	public void setPpvId(String ppvId) {
		this.ppvId = ppvId;
	}
	public String getPpvName() {
		return ppvName;
	}
	public void setPpvName(String ppvName) {
		this.ppvName = ppvName;
	}
	@Override
	public String toString() {
		return "PPV [ppvId=" + ppvId + ", ppvName=" + ppvName + "]";
	}
	
	
}
