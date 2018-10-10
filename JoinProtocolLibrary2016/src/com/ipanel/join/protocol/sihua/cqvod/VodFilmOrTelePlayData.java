package com.ipanel.join.protocol.sihua.cqvod;

import java.util.List;

import org.simpleframework.xml.Attribute;
import org.simpleframework.xml.Element;
import org.simpleframework.xml.ElementList;
import org.simpleframework.xml.Path;
import org.simpleframework.xml.Root;
/**
 * 电影或电视剧详细信息的类  因为 电视剧可能包含多个content 所以该类包含了一个list 
 * @author dzwillpower
 * @time 2013年11月25日 下午3:18:20
 */
@Root(name="message",strict=false)
public class VodFilmOrTelePlayData {

	@Attribute 
	@Path("body/result")
	private String code;
	@Attribute 
	@Path("body/result")
	private String description;
	@ElementList(inline=true,required=false)
	@Path("body/contents")
	private List<VodMovieData> listVodMovieData;
	@Element(required=false)
	/** 这个字段是历史记录跳转到详情 时需要栏目代码*/
	private String folderCode;
	@Element(required=false)
	private String playedTime;
	@Element(required=false)
	private String totalTime;
	public String getCode() {
		return code;
	}
	public void setCode(String code) {
		this.code = code;
	}
	public String getDescription() {
		return description;
	}
	public void setDescription(String description) {
		this.description = description;
	}
	public List<VodMovieData> getListVodMovieData() {
		return listVodMovieData;
	}
	public void setListVodMovieData(List<VodMovieData> listVodMovieData) {
		this.listVodMovieData = listVodMovieData;
	}
	
	public String getFolderCode() {
		return folderCode;
	}
	public void setFolderCode(String folderCode) {
		this.folderCode = folderCode;
	}
	public String getPlayedTime() {
		return playedTime;
	}
	public void setPlayedTime(String playedTime) {
		this.playedTime = playedTime;
	}
	public String getTotalTime() {
		return totalTime;
	}
	public void setTotalTime(String totalTime) {
		this.totalTime = totalTime;
	}
	@Override
	public String toString() {
		return "VodFilmOrTelePlayData [code=" + code + ", description=" + description + ", listVodMovieData="
				+ listVodMovieData + ", folderCode=" + folderCode + ", playedTime=" + playedTime + ", totalTime="
				+ totalTime + "]";
	}
	
	
	
	
}
