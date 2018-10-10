package com.ipanel.join.protocol.a7.domain;

import java.io.Serializable;

import org.simpleframework.xml.Attribute;
import org.simpleframework.xml.Element;
import org.simpleframework.xml.Root;

@Root(name = "ItemData",strict=false)
public class ItemData implements Serializable {

	/**
	 * 
	 */
	private static final long serialVersionUID = 6250989921164430766L;
	@Element(name = "SelectableItem", required = false)
	private SelectableItem selectableItem;
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

	public SelectableItem getSelectableItem() {
		return selectableItem;
	}

	public void setSelectableItem(SelectableItem selectableItem) {
		this.selectableItem = selectableItem;
	}

	

	@Override
	public String toString() {
		return "ItemData [selectableItem=" + selectableItem + ", playedTime=" + playedTime + ", totalTime=" + totalTime
				+ "]";
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

	
  
}
