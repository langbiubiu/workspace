package com.ipanel.join.protocol.sihua.cqvod;

import org.simpleframework.xml.Element;
import org.simpleframework.xml.Root;

import com.ipanel.join.protocol.sihua.cqvod.ImageFiles;

//栏目上的电影海报
@Root(name="content",strict=false)
public class VodFilm
{
	@Element(required = false)
	String vodId;
	@Element(required = false)
	String vodName;
	@Element(required = false)
	String picPath;
	@Element(required = false)
	String playType;
	
	/**思华接口*/
	@Element
	private String  code;//内容代码
	@Element
	private String name;//内容名称
	@Element
	private String type; //内容了类型枚举
	@Element (name="sort-index")
	private String sortIndex;//内容序号，当内容要在页面展示时，以此序号为排列依据
	@Element
	private String url;//内容访问地址
	@Element
	private String thumbnail;//缩略图地址，如果资产没有关联图片资产，取系统配置的默认缩略图
	@Element(name="image-files",required=false)
	private ImageFiles imageFiles;

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
	public String getCode() {
		return code;
	}
	public void setCode(String code) {
		this.code = code;
	}
	public String getName() {
		return name;
	}
	public void setName(String name) {
		this.name = name;
	}
	public String getType() {
		return type;
	}
	public void setType(String type) {
		this.type = type;
	}
	public String getSortIndex() {
		return sortIndex;
	}
	public void setSortIndex(String sortIndex) {
		this.sortIndex = sortIndex;
	}
	public String getUrl() {
		return url;
	}
	public void setUrl(String url) {
		this.url = url;
	}
	public String getThumbnail() {
		return thumbnail;
	}
	public void setThumbnail(String thumbnail) {
		this.thumbnail = thumbnail;
	}
	public ImageFiles getImageFiles() {
		return imageFiles;
	}
	public void setImageFiles(ImageFiles imageFiles) {
		this.imageFiles = imageFiles;
	}
	@Override
	public String toString() {
		return "VodFilm [vodId=" + vodId + ", vodName=" + vodName + ", picPath=" + picPath + ", playType=" + playType
				+ ", code=" + code + ", name=" + name + ", type=" + type + ", sortIndex=" + sortIndex + ", url=" + url
				+ ", thumbnail=" + thumbnail + ", imageFiles=" + imageFiles + "]";
	}
	
}
