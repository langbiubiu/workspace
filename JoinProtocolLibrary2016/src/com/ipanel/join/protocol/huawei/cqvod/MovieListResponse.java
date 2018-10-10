package com.ipanel.join.protocol.huawei.cqvod;

import java.io.Serializable;
import java.util.Iterator;
import java.util.List;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

/**
 * 
 * @author dzwillpower 华为影片列表
 */
public class MovieListResponse implements Serializable {

	/**
	 * 
	 */
	private static final long serialVersionUID = 8833996578807732694L;

	@Expose
	private String totalNums;
	@Expose
	private String img;
	@Expose
	@SerializedName("array")
	private List<MovieData> movieList;

	public class MovieData implements Serializable {

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
		@Expose
		private String tagType;
		@Expose
		private String icon;
		@Expose
		private String isZt;

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

		public String getTagType() {
			return tagType;
		}

		public void setTagType(String tagType) {
			this.tagType = tagType;
		}

		public String getIcon() {
			return icon;
		}

		public void setIcon(String icon) {
			this.icon = icon;
		}

		public String getIsZt() {
			return isZt;
		}

		public void setIsZt(String isZt) {
			this.isZt = isZt;
		}

		@Override
		public String toString() {
			return "MovieData [vodId=" + vodId + ", vodName=" + vodName + ", picPath=" + picPath + ", playType="
					+ playType + ", isHd=" + isHd + ", tagType=" + tagType + ", icon=" + icon + ", isZt=" + isZt + "]";
		}
	}

	public String getTotalNums() {
		return totalNums;
	}

	public void setTotalNums(String totalNums) {
		this.totalNums = totalNums;
	}

	public String getImg() {
		return img;
	}

	public void setImg(String img) {
		this.img = img;
	}

	public List<MovieData> getMovieList() {
//		if(movieList != null && movieList.size()>0){
//			for (Iterator<MovieData> iterator = movieList.iterator();iterator.hasNext();) {
//				String tagType = iterator.next().getTagType();
//				if(tagType.equals("专题")){
//					iterator.remove();
//				}
//			}
//		}
		return movieList;
	}

	public void setMovieList(List<MovieData> movieList) {
		this.movieList = movieList;
	}

	@Override
	public String toString() {
		return "MovieListResponse [totalNums=" + totalNums + ", img=" + img + ", movieList=" + movieList + "]";
	}

}
