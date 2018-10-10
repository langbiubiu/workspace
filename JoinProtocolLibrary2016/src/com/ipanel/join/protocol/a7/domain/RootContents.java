package com.ipanel.join.protocol.a7.domain;

import java.io.Serializable;
import java.util.List;

import org.simpleframework.xml.Attribute;
import org.simpleframework.xml.ElementList;
import org.simpleframework.xml.Root;

@Root(name = "RootContents",strict=false)
public class RootContents implements Serializable {

	/**
	 * 
	 */
	private static final long serialVersionUID = 798159382781166228L;
	@Attribute(required = false)
	private String totalResults;
	@Attribute(required = false)
	private String displayName;
	@Attribute(required = false)
	private String restartAtToken;
	@ElementList(name = "ChildFolder", inline = true,required=false)
	private List<ChildFolder> listChildFolders;
	@ElementList(name = "SelectableItem", inline = true,required=false)
	private List<SelectableItem> listSelectableItems;

	public List<ChildFolder> getListChildFolders() {
		return listChildFolders;
	}

	public void setListChildFolders(List<ChildFolder> listChildFolders) {
		this.listChildFolders = listChildFolders;
	}

	public List<SelectableItem> getListSelectableItems() {
		return listSelectableItems;
	}

	public void setListSelectableItems(List<SelectableItem> listSelectableItems) {
		this.listSelectableItems = listSelectableItems;
	}

	public String getTotalResults() {
		return totalResults;
	}

	public void setTotalResults(String totalResults) {
		this.totalResults = totalResults;
	}

	public String getDisplayName() {
		return displayName;
	}

	public void setDisplayName(String displayName) {
		this.displayName = displayName;
	}

	public String getRestartAtToken() {
		return restartAtToken;
	}

	public void setRestartAtToken(String restartAtToken) {
		this.restartAtToken = restartAtToken;
	}

	@Override
	public String toString() {
		return "RootContents [totalResults=" + totalResults + ", displayName="
				+ displayName + ", restartAtToken=" + restartAtToken
				+ ", listChildFolders=" + listChildFolders
				+ ", listSelectableItems=" + listSelectableItems + "]";
	}

}
