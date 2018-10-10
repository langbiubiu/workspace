package com.ipanel.join.protocol.sihua.cqvod;

import org.simpleframework.xml.Element;
import org.simpleframework.xml.Root;

/*
 * 思华接口 内容详细信息
 */
@Root(name="content",strict=false)
public class VodMovieData {

	@Element
	
	private String code;// 内容代码
	@Element
	
	private String name;// 内容名称
	@Element
	
	private String type;// 内容枚举类型
	@Element(name="sort-index")
	
	private String sortIndex;// 内容展示序号，当内容要在页面展示时，以此序号为排列依据
	@Element(name="relative-ppvids",required=false)
	
	private String relativeppvids;// PPVID列表，如有多个用逗号分隔
	@Element(name="standard-price",required= false)
	
	private String standardprice;// 标准资费，单位为分
	@Element
	
	private String director;// 导演
	@Element
	
	private String actors;// 演员，多个用逗号分隔
	@Element
	
	private String items;// 集数，内容类型为连续剧是为有效集数，其他类型该值为0。
	@Element(name="produced-year")
	
	private String producedYear;// 出品年份
	@Element
	
	private String duration;// 时长，单位秒
	@Element
	
	private String url;// 内容访问地址
	@Element
	
	private String description;// 内容简介
	@Element(required =false)
	
	private String duber;// 语种
	@Element(required =false)
	
	private String comment;// 0不支持评论功能（默认）, 1支持评论功能。
	@Element(name="auth-result" ,required = false)
	
	private String authResult;// 鉴权结果（如果鉴权前置），参见返回码定义
	@Element
	
	private String keywords;// 关键字
	@Element
	
	private String viewpoints;// 看点
	@Element(required=false)
	
	private String cp;// 内容提供商
	@Element(name="media-files",required=false)
	
	private MediaFiles mediaFiles;
	@Element(name="image-files",required=false)
	
	private ImageFiles imageFiles;
	@Element(name="content-items",required=false)
	
	private ContentItems contentItems;
//	private AssetsItems> assetItems;

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

	public String getRelativeppvids() {
		return relativeppvids;
	}

	public void setRelativeppvids(String relativeppvids) {
		this.relativeppvids = relativeppvids;
	}

	public String getStandardprice() {
		return standardprice;
	}

	public void setStandardprice(String standardprice) {
		this.standardprice = standardprice;
	}

	public String getDirector() {
		return director;
	}

	public void setDirector(String director) {
		this.director = director;
	}

	public String getActors() {
		return actors;
	}

	public void setActors(String actors) {
		this.actors = actors;
	}

	public String getItems() {
		return items;
	}

	public void setItems(String items) {
		this.items = items;
	}

	public String getProducedYear() {
		return producedYear;
	}

	public void setProducedYear(String producedYear) {
		this.producedYear = producedYear;
	}

	public String getDuration() {
		return duration;
	}

	public void setDuration(String duration) {
		this.duration = duration;
	}

	public String getUrl() {
		return url;
	}

	public void setUrl(String url) {
		this.url = url;
	}

	public String getDescription() {
		return description;
	}

	public void setDescription(String description) {
		this.description = description;
	}

	public String getDuber() {
		return duber;
	}

	public void setDuber(String duber) {
		this.duber = duber;
	}

	public String getComment() {
		return comment;
	}

	public void setComment(String comment) {
		this.comment = comment;
	}

	public String getAuthResult() {
		return authResult;
	}

	public void setAuthResult(String authResult) {
		this.authResult = authResult;
	}

	public String getKeywords() {
		return keywords;
	}

	public void setKeywords(String keywords) {
		this.keywords = keywords;
	}

	public String getViewpoints() {
		return viewpoints;
	}

	public void setViewpoints(String viewpoints) {
		this.viewpoints = viewpoints;
	}

	public String getCp() {
		return cp;
	}

	public void setCp(String cp) {
		this.cp = cp;
	}

	public MediaFiles getMediaFiles() {
		return mediaFiles;
	}

	public void setMediaFiles(MediaFiles mediaFiles) {
		this.mediaFiles = mediaFiles;
	}

	public ImageFiles getImageFiles() {
		return imageFiles;
	}

	public void setImageFiles(ImageFiles imageFiles) {
		this.imageFiles = imageFiles;
	}

	public ContentItems getContentItems() {
		return contentItems;
	}

	public void setContentItems(ContentItems contentItems) {
		this.contentItems = contentItems;
	}

	@Override
	public String toString() {
		return "VodMovieData [code=" + code + ", name=" + name + ", type=" + type + ", sortIndex=" + sortIndex
				+ ", relativeppvids=" + relativeppvids + ", standardprice=" + standardprice + ", director=" + director
				+ ", actors=" + actors + ", items=" + items + ", producedYear=" + producedYear + ", duration="
				+ duration + ", url=" + url + ", description=" + description + ", duber=" + duber + ", comment="
				+ comment + ", authResult=" + authResult + ", keywords=" + keywords + ", viewpoints=" + viewpoints
				+ ", cp=" + cp + ", mediaFiles=" + mediaFiles + ", imageFiles=" + imageFiles + ", contentItems="
				+ contentItems + "]";
	}

	

//	public List<AssetsItems> getAssetsItemList() {
//		return assetsItemList;
//	}
//
//	public void setAssetsItemList(List<AssetsItems> assetsItemList) {
//		this.assetsItemList = assetsItemList;
//	}
}
