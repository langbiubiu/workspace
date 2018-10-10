package com.ipanel.join.cq.huawei.data;

import java.io.Serializable;
import java.util.List;

import com.google.gson.annotations.Expose;

public class HuaWeiResponse implements Serializable {
	@Expose
	private String total; // ӰƬ����
	@Expose
	private String posterImg; // ��Ŀ����
	@Expose
	private List<VodProgram> vod; // ӰƬ�б�
	@Expose
	private String typeID; // ��Ŀid
	@Expose
	private String playFlag; // �Ƿ���Բ���,1���Բ��ţ�0�����Բ���
	@Expose
	private String playUrl; // ӰƬRTSP��ַ
	@Expose
	private String message; // ��ʾ����
	public String getTotal() {
		return total;
	}
	public void setTotal(String total) {
		this.total = total;
	}
	public String getPosterImg() {
		return posterImg;
	}
	public void setPosterImg(String posterImg) {
		this.posterImg = posterImg;
	}
	public List<VodProgram> getVod() {
		return vod;
	}
	public void setVod(List<VodProgram> vod) {
		this.vod = vod;
	}
	public String getTypeID() {
		return typeID;
	}
	public void setTypeID(String typeID) {
		this.typeID = typeID;
	}
	public String getPlayFlag() {
		return playFlag;
	}
	public void setPlayFlag(String playFlag) {
		this.playFlag = playFlag;
	}
	public String getPlayUrl() {
		return playUrl;
	}
	public void setPlayUrl(String playUrl) {
		this.playUrl = playUrl;
	}
	public String getMessage() {
		return message;
	}
	public void setMessage(String message) {
		this.message = message;
	}
	@Override
	public String toString() {
		return "HuaWeiResponse2 [total=" + total + ", posterImg=" + posterImg
				+ ", vod=" + vod + ", typeID=" + typeID + ", playFlag="
				+ playFlag + ", playUrl=" + playUrl + ", message=" + message
				+ "]";
	}
	
}
