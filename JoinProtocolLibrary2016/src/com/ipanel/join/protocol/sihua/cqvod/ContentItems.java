package com.ipanel.join.protocol.sihua.cqvod;

import java.util.List;

import org.simpleframework.xml.Attribute;
import org.simpleframework.xml.ElementList;
import org.simpleframework.xml.Root;
@Root(name="content-items" ,strict=false)
public class ContentItems {

	@Attribute(required=false)
	private String items;// 连续剧集数
	@ElementList(inline=true,required=false)
	List<ContentItem> contentItemList;
	public String getItems() {
		return items;
	}
	public void setItems(String items) {
		this.items = items;
	}
	public List<ContentItem> getContentItemList() {
		return contentItemList;
	}
	public void setContentItemList(List<ContentItem> contentItemList) {
		this.contentItemList = contentItemList;
	}
	@Override
	public String toString() {
		return "ContentItems [items=" + items + ", contentItemList=" + contentItemList + "]";
	}
}
