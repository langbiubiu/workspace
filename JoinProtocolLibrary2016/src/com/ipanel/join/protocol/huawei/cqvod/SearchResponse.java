package com.ipanel.join.protocol.huawei.cqvod;

import java.io.Serializable;
import java.util.List;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

public class SearchResponse implements Serializable {

	/**
	 * 
	 */
	private static final long serialVersionUID = -7500407827056952174L;
	
	@Expose
	private String countTotal;
	@Expose
	@SerializedName("array")
	private List<SearchData> searchList;

	public class SearchData implements Serializable {

		/**
		 * 
		 */
		private static final long serialVersionUID = 6780489891711915589L;
		@Expose
		private String vodId;
		@Expose
		private String vodName;
		@Expose
		private String picPath;
		@Expose
		private String playType;
		@Expose
		private String isHd;
		
		public String getVodId() {
			return vodId;
		}

		public void setVodId(String vodId) {
			this.vodId = vodId;
		}

		public String getVodName() {
			return vodName;
		}

		public void setVodName(String vodName) {
			this.vodName = vodName;
		}

		public String getPicPath() {
			return picPath;
		}

		public void setPicPath(String picPath) {
			this.picPath = picPath;
		}

		public String getPlayType() {
			return playType;
		}

		public void setPlayType(String playType) {
			this.playType = playType;
		}

		public String getIsHd() {
			return isHd;
		}

		public void setIsHd(String isHd) {
			this.isHd = isHd;
		}

		@Override
		public String toString() {
			return "SearchData [vodId=" + vodId + ", vodName=" + vodName + ", picPath=" + picPath + ", playType="
					+ playType + ", isHd=" + isHd + "]";
		}

		

	}

	public String getCountTotal() {
		return countTotal;
	}

	public void setCountTotal(String countTotal) {
		this.countTotal = countTotal;
	}

	public List<SearchData> getSearchList() {
		return searchList;
	}

	public void setSearchList(List<SearchData> searchList) {
		this.searchList = searchList;
	}

	@Override
	public String toString() {
		return "SearchResponse [countTotal=" + countTotal + ", searchList=" + searchList + "]";
	}
	
	

}
