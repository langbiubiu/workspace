package com.ipanel.join.protocol.a7.domain;

import java.io.Serializable;
import java.util.Iterator;
import java.util.List;

import org.simpleframework.xml.Attribute;
import org.simpleframework.xml.Element;
import org.simpleframework.xml.ElementList;
import org.simpleframework.xml.Root;

@Root(strict=false)
public class FolderContents implements Serializable {

	/**
	 * 
	 */
	private static final long serialVersionUID = -3530590377748585702L;
	@Attribute(required = false)
	/** 如果本次请求没有返回全部的数据，则该属性有值，供后续请求使用,JSON格式*/
	private String restartAtToken;
	@Attribute(required = false)
	private String totalResults;
	/**其值等于请求的栏目资产ID下ChildFolder,SelectableItem这些元素的个数总和。*/
	@Attribute(required = false)
	/**当前响应ChildFolder,SelectableItem这些元素的个数总和*/
	private String currentResults;
	/**这个栏目的基本信息*/
	@Element(required = false, name = "FolderFrame")
	private FolderFrame folderFrame;
	/**子栏目信息 多个*/
	@ElementList(required = false, name = "ChildFolder", inline = true)
	private List<ChildFolder> listChildFolder;
	/**影片信息，多个*/
	@ElementList(required = false, name = "SelectableItem", inline = true)
	private List<SelectableItem> listSelectableItems;

	private String playedTime;
	private String totalTime;
	
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

	public String getRestartAtToken() {
		return restartAtToken;
	}

	public void setRestartAtToken(String restartAtToken) {
		this.restartAtToken = restartAtToken;
	}

	public String getTotalResults() {
		return totalResults;
	}

	public void setTotalResults(String totalResults) {
		this.totalResults = totalResults;
	}

	public String getCurrentResults() {
		return currentResults;
	}

	public void setCurrentResults(String currentResults) {
		this.currentResults = currentResults;
	}

	public FolderFrame getFolderFrame() {
		return folderFrame;
	}

	public void setFolderFrame(FolderFrame folderFrame) {
		this.folderFrame = folderFrame;
	}

	public List<ChildFolder> getListChildFolder() {
		if(listChildFolder != null){
			for (Iterator<ChildFolder> iterator=listChildFolder.iterator();iterator.hasNext();) {
				String displayName = iterator.next().getDisplayName();
				if(displayName.equals("少儿栏目")){
					iterator.remove();
				}else if(displayName.equals("首页图片推荐")){
					iterator.remove();
				}
			}
		}
		return listChildFolder;
	}

	public void setListChildFolder(List<ChildFolder> listChildFolder) {
		this.listChildFolder = listChildFolder;
	}

	public List<SelectableItem> getListSelectableItems() {
		return listSelectableItems;
	}

	public void setListSelectableItems(List<SelectableItem> listSelectableItems) {
		this.listSelectableItems = listSelectableItems;
	}

	@Override
	public String toString() {
		return "FolderContents [restartAtToken=" + restartAtToken + ", totalResults=" + totalResults
				+ ", currentResults=" + currentResults + ", folderFrame=" + folderFrame + ", listChildFolder="
				+ listChildFolder + ", listSelectableItems=" + listSelectableItems + ", playedTime=" + playedTime
				+ ", totalTime=" + totalTime + "]";
	}

	

}
