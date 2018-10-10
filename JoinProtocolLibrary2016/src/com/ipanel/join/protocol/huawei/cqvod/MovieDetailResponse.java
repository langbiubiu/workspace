package com.ipanel.join.protocol.huawei.cqvod;

import java.io.Serializable;
import java.util.List;

import com.google.gson.annotations.Expose;
/**
 * 电影或电视剧的详情 
 * @author dzwillpower
 *
 */
public class MovieDetailResponse implements Serializable {

	/**
	 * 
	 */
	private static final long serialVersionUID = 891151273711054559L;
	@Expose
	private String vodId;
	@Expose
	private String typeId;
	@Expose
	private String vodName;
	@Expose
	private String director;
	@Expose
	private String actor;
	@Expose
	private String intr;
	@Expose 
	private String picPath;
	@Expose 
	private String playType;
	@Expose
	private String totalNum;
	@Expose
	private List<String> vodIdList;
	@Expose
	private String elapsetime;
	@Expose
	private int isOver;
	
	/** 历史记录 播放的时间和 电视剧播放的是哪一集*/
	private String playedTime;
	private String episodes;
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
	public List<String> getVodIdList() {
		return vodIdList;
	}
	public void setVodIdList(List<String> vodIdList) {
		this.vodIdList = vodIdList;
	}
	public String getElapsetime() {
		return elapsetime;
	}
	public void setElapsetime(String elapsetime) {
		this.elapsetime = elapsetime;
	}
	
	public String getPlayedTime() {
		return playedTime;
	}
	public void setPlayedTime(String playedTime) {
		this.playedTime = playedTime;
	}
	public String getEpisodes() {
		return episodes;
	}
	public void setEpisodes(String episodes) {
		this.episodes = episodes;
	}
	
	public int isOver() {
		return isOver;
	}
	public void setOver(int isOver) {
		this.isOver = isOver;
	}
	@Override
	public String toString() {
		return "MovieDetailResponse [vodId=" + vodId + ", typeId=" + typeId + ", vodName=" + vodName + ", director="
				+ director + ", actor=" + actor + ", intr=" + intr + ", picPath=" + picPath + ", playType=" + playType
				+ ", totalNum=" + totalNum + ", vodIdList=" + vodIdList + ", elapsetime=" + elapsetime + ", isOver="
				+ isOver + ", playedTime=" + playedTime + ", episodes=" + episodes + "]";
	}
	

}
