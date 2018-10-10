package com.ipanel.join.protocol.sihua.cqvod;

import java.util.ArrayList;

import org.simpleframework.xml.Attribute;
import org.simpleframework.xml.Element;
import org.simpleframework.xml.ElementList;
import org.simpleframework.xml.Path;
import org.simpleframework.xml.Root;
@Root(name="message",strict=false)
public class VodFilmList {
	@Element(required = false)
	String totalNums;
	@ElementList(inline=true)
	@Path("body/contents")
	ArrayList<VodFilm> array;
	@Element(required = false)
	String img;
	
	/** ˼��*/
	@Attribute (name = "items")
	@Path("body/contents")
	private String items; //��Ŀ�����ݵ�����������������ݣ�itemsΪ0���Ҳ���ʾcontent��ǩ
	@Attribute(name ="total-pages")
	@Path("body/contents")
	private String totalPages;//��ѯ��ҳ��
	@Attribute(name = "page-index")
	@Path("body/contents")
	private String pageIndex;//��ǰҳ��
	

	public String getTotalNums() {
		return totalNums;
	}

	public void setTotalNums(String totalNums) {
		this.totalNums = totalNums;
	}

	public ArrayList<VodFilm> getArray() {
		return array;
	}

	public String getImg() {
		return img;
	}

	public void setImg(String img) {
		this.img = img;
	}

	public void setArray(ArrayList<VodFilm> vodFilms) {
		this.array = vodFilms;
	}

	public String getItems() {
		return items;
	}

	public void setItems(String items) {
		this.items = items;
	}

	public String getTotalPages() {
		return totalPages;
	}

	public void setTotalPages(String totalPages) {
		this.totalPages = totalPages;
	}

	public String getPageIndex() {
		return pageIndex;
	}

	public void setPageIndex(String pageIndex) {
		this.pageIndex = pageIndex;
	}

	@Override
	public String toString() {
		return "VodFilmList [totalNums=" + totalNums + ", array=" + array + ", img=" + img + ", items=" + items
				+ ", totalPages=" + totalPages + ", pageIndex=" + pageIndex + "]";
	}
	
	

}
