package com.ipanel.join.cq.vod.jsondata;

/*
 * ���ڻؿ��Ĳ�����Ϣ��
 */
public class VodPlayInfo {
	
//	private int VodId;
	private int playType;//0Ϊ��Ӱ��1Ϊ���Ӿ�	
	private VodTeleplay vodTeleplay;//�洢��ǰ���ص�ȫ�ֵĵ��Ӿ���ߵ�Ӱ
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
