package com.ipanel.join.protocol.a7.domain;

import java.io.Serializable;

import org.simpleframework.xml.Attribute;
import org.simpleframework.xml.Element;
import org.simpleframework.xml.Root;

@Root(name = "BookmarkedItem")
public class BookmarkedItem implements Serializable {
	private static final long serialVersionUID = 9058253738905953554L;
	@Attribute
	private String bookmarkedId;
	@Attribute
	private String markDateTime;
	@Attribute(required = false)
	private String custom;
	@Attribute(required = false)
	private String endDateTime;
	@Element
	private SelectableItem selectableItem;

	public String getBookmarkedId() {
		return bookmarkedId;
	}

	public void setBookmarkedId(String bookmarkedId) {
		this.bookmarkedId = bookmarkedId;
	}

	public String getMarkDateTime() {
		return markDateTime;
	}

	public void setMarkDateTime(String markDateTime) {
		this.markDateTime = markDateTime;
	}

	public String getCustom() {
		return custom;
	}

	public void setCustom(String custom) {
		this.custom = custom;
	}

	public String getEndDateTime() {
		return endDateTime;
	}

	public void setEndDateTime(String endDateTime) {
		this.endDateTime = endDateTime;
	}

	public SelectableItem getSelectableItem() {
		return selectableItem;
	}

	public void setSelectableItem(SelectableItem selectableItem) {
		this.selectableItem = selectableItem;
	}

	@Override
	public String toString() {
		return "BookmarkedItem [bookmarkedId=" + bookmarkedId + ", markDateTime=" + markDateTime
				+ ", custom=" + custom + ", endDateTime=" + endDateTime + ", selectableItem="
				+ selectableItem + "]";
	}

}
