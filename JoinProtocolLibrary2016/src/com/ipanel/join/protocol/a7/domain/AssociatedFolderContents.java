package com.ipanel.join.protocol.a7.domain;

import java.io.Serializable;
import java.util.List;

import org.simpleframework.xml.Attribute;
import org.simpleframework.xml.Element;
import org.simpleframework.xml.ElementList;
import org.simpleframework.xml.Root;

@Root
public class AssociatedFolderContents implements Serializable {

	/**
	 * 
	 */
	private static final long serialVersionUID = -3530590377748585702L;
	@Attribute(required = false)
	private String restartAtToken;
	@Attribute(required = false)
	private String totalResults;
	@Attribute(required = false)
	private String currentResults;
	@Element(required = false, name = "FolderFrame")
	private FolderFrame folderFrame;
	@ElementList(required = false, name = "ChildFolder", inline = true)
	private List<ChildFolder> listChildFolder;
	@ElementList(required = false, name = "SelectableItem", inline = true)
	private List<SelectableItem> listSelectableItems;

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
		return "FolderContents [restartAtToken=" + restartAtToken
				+ ", totalResults=" + totalResults + ", currentResults="
				+ currentResults + ", folderFrame=" + folderFrame
				+ ", listChildFolder=" + listChildFolder
				+ ", listSelectableItems=" + listSelectableItems + "]";
	}

}
