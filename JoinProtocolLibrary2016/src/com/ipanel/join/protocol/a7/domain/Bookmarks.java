package com.ipanel.join.protocol.a7.domain;

import java.io.Serializable;
import java.util.List;

import org.simpleframework.xml.Attribute;
import org.simpleframework.xml.ElementList;
import org.simpleframework.xml.Root;

@Root(name = "Bookmarks")
public class Bookmarks implements Serializable {
	private static final long serialVersionUID = 7186259504647589965L;
	@Attribute
	private String totalResults;
	@Attribute(required = false)
	private String restartAtToken;
	@ElementList(name = "BookmarkedItem", inline = true)
	private List<BookmarkedItem> listBookmarkedItems;

	public String getTotalResults() {
		return totalResults;
	}

	public void setTotalResults(String totalResults) {
		this.totalResults = totalResults;
	}

	public String getRestartAtToken() {
		return restartAtToken;
	}

	public void setRestartAtToken(String restartAtToken) {
		this.restartAtToken = restartAtToken;
	}

	public List<BookmarkedItem> getListBookmarkedItems() {
		return listBookmarkedItems;
	}

	public void setListBookmarkedItems(List<BookmarkedItem> listBookmarkedItems) {
		this.listBookmarkedItems = listBookmarkedItems;
	}

	@Override
	public String toString() {
		return "Bookmarks [totalResults=" + totalResults + ", restartAtToken=" + restartAtToken
				+ ", listBookmarkedItems=" + listBookmarkedItems + "]";
	}

}
