package com.ipanel.join.cq.vod.jsondata;

import java.util.List;

import org.simpleframework.xml.Attribute;
import org.simpleframework.xml.ElementList;
import org.simpleframework.xml.Path;
import org.simpleframework.xml.Root;

/**
 * ����Ŀ
 * 
 * @author dzwillpower
 * @time 2013��11��19�� ����10:00:16
 */
@Root(name="message")
public class VodSubMenu {
	@Attribute
	@Path("body/folders")
	private String items;// ����Ŀ�����������������Ŀ��itemsΪ0���Ҳ���ʾfolder��ǩ
	@Attribute(name="total-pages")
	@Path("body/folders")
	private String total_pages;// ��ѯ��ҳ��
	@Attribute(name="page-index")
	@Path("body/folders")
	private String page_index;// ��ǰҳ��
	@ElementList(inline=true)
	@Path("body/folders")
	private List<VodMenu> listvoMenus;
	public String getItems() {
		return items;
	}
	public void setItems(String items) {
		this.items = items;
	}
	public String getTotal_pages() {
		return total_pages;
	}
	public void setTotal_pages(String total_pages) {
		this.total_pages = total_pages;
	}
	public String getPage_index() {
		return page_index;
	}
	public void setPage_index(String page_index) {
		this.page_index = page_index;
	}
	public List<VodMenu> getListvoMenus() {
		return listvoMenus;
	}
	public void setListvoMenus(List<VodMenu> listvoMenus) {
		this.listvoMenus = listvoMenus;
	}
	@Override
	public String toString() {
		return "VodSubMenu [items=" + items + ", total_pages=" + total_pages + ", page_index=" + page_index
				+ ", listvoMenus=" + listvoMenus + "]";
	}
	

}
