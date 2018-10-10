package com.ipanel.join.cq.vod.jsondata;

import java.util.ArrayList;
//电视剧详情
public class VodTeleplay {

	String vodId;
	String typeId;
	String vodName;
	String director;
	String actor;
	String intr;
	String picPath;
	String playType;
	String totalNum;
	
	ArrayList<String> vodIdList;

	//播放的相关信息
	int isOver=0; //0为已播放完毕，1为未播放完
	int lastEposdide=0;//播放的上一集,即播放的时候要记录这个集数，作为播放的本集
    String url;
	long time=0;
	String showTime;
	
	float timeRatio=0;
	
	
	public float getTimeRatio() {
		return timeRatio;
	}
	public void setTimeRatio(float timeRatio) {
		this.timeRatio = timeRatio;
	}
	
	

	public String getVodId() {
		return vodId;
	}

	public void setVodId(String vodId) {
		this.vodId = vodId;
	}

	public String getTypeId() {
		return typeId;
	}

	public void setTypeId(String typeId) {
		this.typeId = typeId;
	}

	public String getVodName() {
		return vodName;
	}

	public void setVodName(String vodName) {
		this.vodName = vodName;
	}

	public String getDirector() {
		return director;
	}

	public void setDirector(String director) {
		this.director = director;
	}

	public String getActor() {
		return actor;
	}

	public void setActor(String actor) {
		this.actor = actor;
	}

	public String getIntr() {
		return intr;
	}

	public void setIntr(String intr) {
		this.intr = intr;
	}

	public String getPicPath() {
		return picPath;
	}

	public void setPicPath(String picPath) {
		this.picPath = picPath;
	}

	public String getPlayType() {
		return playType;
	}

	public void setPlayType(String playType) {
		this.playType = playType;
	}

	public String getTotalNum() {
		return totalNum;
	}

	public void setTotalNum(String totalNum) {
		this.totalNum = totalNum;
	}

	public ArrayList<String> getVodIdList() {
		return vodIdList;
	}

	public void setVodIdList(ArrayList<String> vodIdList) {
		this.vodIdList = vodIdList;
	}

	public VodTeleplay(){}
	public VodTeleplay(String vodId, String typeId, String vodName, String director, String actor, String intr,
			String picPath, String playType, String totalNum, ArrayList<String> vodIdList) {
		super();
		this.vodId = vodId;
		this.typeId = typeId;
		this.vodName = vodName;
		this.director = director;
		this.actor = actor;
		this.intr = intr;
		this.picPath = picPath;
		this.playType = playType;
		this.totalNum = totalNum;
		this.vodIdList = vodIdList;
	}

	@Override
	public String toString() {
		return "VodTeleplay [vodId=" + vodId + ", typeId=" + typeId + ", vodName=" + vodName + ", director=" + director
				+ ", actor=" + actor + ", intr=" + intr + ", picPath=" + picPath + ", playType=" + playType
				+ ", totalNum=" + totalNum + ", vodIdList=" + vodIdList + "]";
	}

	public int getLastEposdide() {
		return lastEposdide;
	}

	public void setLastEposdide(int lastEposdide) {
		this.lastEposdide = lastEposdide;
	}

	public String getUrl() {
		return url;
	}

	public void setUrl(String url) {
		this.url = url;
	}

	public long getTime() {
		return time;
	}

	public void setTime(long time) {
		this.time = time;
	}

	public String getShowTime() {
		return showTime;
	}

	public void setShowTime(String showTime) {
		this.showTime = showTime;
	}

	public int getIsOver() {
		return isOver;
	}

	public void setIsOver(int isOver) {
		this.isOver = isOver;
	}
	
	
}
