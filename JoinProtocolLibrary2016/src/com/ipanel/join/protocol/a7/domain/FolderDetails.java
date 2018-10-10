package com.ipanel.join.protocol.a7.domain;

import java.io.Serializable;

import org.simpleframework.xml.Attribute;
import org.simpleframework.xml.Element;
import org.simpleframework.xml.Root;

@Root(name = "FolderDetails",strict=false)
public class FolderDetails {
	@Attribute(required=false)
	/**电视剧的集数*/
	private String  selectableltemCount;// 电视剧的集数
	@Attribute(required=false)
	/**演员的信息*/
	private String  actorsDisplay;//演员的信息
	@Attribute(required =false)
	private String area;//地区
	@Attribute(required =false)
	private String publishDate;//发布日期
	@Element(name="Director",required=false)
	private Director director;//导演
	@Element(required=false)
	private String producter;//制片人	
	@Attribute(required=false)
	private String summarMedium;
	@Attribute(required=false)
	private String summarvShort;
	public String getSelectableltemCount() {
		return selectableltemCount;
	}
	public void setSelectableltemCount(String selectableltemCount) {
		this.selectableltemCount = selectableltemCount;
	}
	public String getActorsDisplay() {
		return actorsDisplay;
	}
	public void setActorsDisplay(String actorsDisplay) {
		this.actorsDisplay = actorsDisplay;
	}
	public String getArea() {
		return area;
	}
	public void setArea(String area) {
		this.area = area;
	}
	public String getPublishDate() {
		return publishDate;
	}
	public void setPublishDate(String publishDate) {
		this.publishDate = publishDate;
	}
	
	public Director getDirector() {
		return director;
	}
	public void setDirector(Director director) {
		this.director = director;
	}
	public String getProducter() {
		return producter;
	}
	public void setProducter(String producter) {
		this.producter = producter;
	}
	
	public String getSummarMedium() {
		return summarMedium;
	}
	public void setSummarMedium(String summarMedium) {
		this.summarMedium = summarMedium;
	}
	public String getSummarvShort() {
		return summarvShort;
	}
	public void setSummarvShort(String summarvShort) {
		this.summarvShort = summarvShort;
	}
	@Root(name="Director",strict=false)
	public static class Director implements Serializable{

		/**
		 * 
		 */
		private static final long serialVersionUID = 5983787990227310890L;
		@Attribute(name="name",required=false)
		private String director;
		public String getDirector() {
			return director;
		}
		public void setDirector(String director) {
			this.director = director;
		}
		@Override
		public String toString() {
			return "Director [director=" + director + "]";
		}
		
		
	}
	@Override
	public String toString() {
		return "FolderDetails [selectableltemCount=" + selectableltemCount + ", actorsDisplay=" + actorsDisplay
				+ ", area=" + area + ", publishDate=" + publishDate + ", director=" + director + ", producter="
				+ producter + ", summarMedium=" + summarMedium + ", summarvShort=" + summarvShort + "]";
	}
	
}
