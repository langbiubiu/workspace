package com.ipanel.join.cq.vod.jsondata;

/*
 * 用于回看的播放信息表
 */
public class VodPlayInfo {
	
//	private int VodId;
	private int playType;//0为电影，1为电视剧	
	private VodTeleplay vodTeleplay;//存储当前加载的全局的电视剧或者电影
	private VodMovie vodMovie;
	
	public int getPlayType() {
		return playType;
	}
	public void setPlayType(int playType) {
		this.playType = playType;
	}
	public VodTeleplay getVodTeleplay() {
		return vodTeleplay;
	}
	public void setVodTeleplay(VodTeleplay vodTeleplay) {
		this.vodTeleplay = vodTeleplay;
	}
	public VodMovie getVodMovie() {
		return vodMovie;
	}
	public void setVodMovie(VodMovie vodMovie) {
		this.vodMovie = vodMovie;
	}
	
}
