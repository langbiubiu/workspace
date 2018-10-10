package com.ipanel.join.cq.vod.jsondata;

//电影详情数据
public class VodMovie {
	
	String vodId;
	String typeId;
	String vodName;
	String director;
	String actor;
	String intr;
	String picPath;
	String playType;
	String elapsetime;
	
	//播放的相关信息
	int isOver; //0为已播放完毕，1为未播放完
	String url;
	long time=0;//播放开始的实际时间
	String showTime;//显示的字符串时间
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
	public String getElapsetime() {
		return elapsetime;
	}
	public void setElapsetime(String elapsetime) {
		this.elapsetime = elapsetime;
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
	
	public VodMovie(){}
	public VodMovie(String vodId, String typeId, String vodName, String director, String actor, String intr,
			String picPath, String playType, String elapsetime) {
		super();
		this.vodId = vodId;
		this.typeId = typeId;
		this.vodName = vodName;
		this.director = director;
		this.actor = actor;
		this.intr = intr;
		this.picPath = picPath;
		this.playType = playType;
		this.elapsetime = elapsetime;
	}
	
	public VodMovie(String vodId, String typeId, String vodName, String director, String actor, String intr,
			String picPath, String playType, String elapsetime, int isOver, String url, long time, String showTime) {
		super();
		this.vodId = vodId;
		this.typeId = typeId;
		this.vodName = vodName;
		this.director = director;
		this.actor = actor;
		this.intr = intr;
		this.picPath = picPath;
		this.playType = playType;
		this.elapsetime = elapsetime;
		this.isOver = isOver;
		this.url = url;
		this.time = time;
		this.showTime = showTime;
	}
	@Override
	public String toString() {
		return "VodMovie [vodId=" + vodId + ", typeId=" + typeId + ", vodName=" + vodName + ", director=" + director
				+ ", actor=" + actor + ", intr=" + intr + ", picPath=" + picPath + ", playType=" + playType
				+ ", elapsetime=" + elapsetime + "]";
	}
	public int getIsOver() {
		return isOver;
	}
	public void setIsOver(int isOver) {
		this.isOver = isOver;
	}
	
	

}
