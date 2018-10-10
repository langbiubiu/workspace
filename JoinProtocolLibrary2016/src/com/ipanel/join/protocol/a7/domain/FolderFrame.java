package com.ipanel.join.protocol.a7.domain;

import java.io.Serializable;
import java.util.List;

import org.simpleframework.xml.Attribute;
import org.simpleframework.xml.Element;
import org.simpleframework.xml.ElementList;
import org.simpleframework.xml.Root;

@Root(name = "FolderFrame",strict=false)
public class FolderFrame implements Serializable {

	/**
	 * 
	 */
	private static final long serialVersionUID = 8533067452769921360L;
	@Attribute(required=false)
	/** 提供商ID，预留*/
	private String providerld;
	@Attribute(required=false)
	/** 该栏目的ID*/
	private String assetId;
	@Attribute(required=false)
	/**父栏目的资产ID，如果是一级栏目，则该属性不出现*/
	private String parentAssetId;
	@Attribute(required=false)
	/**该栏目显示在页面上的名字*/
	private String displayName;
	@Attribute(required=false)
	/**栏目的类型。0:非叶节点类,1:电影类,2:电视剧类,3:综艺类,4:卡拉OK类*/
	private String folderType;
	@Element(required=false ,name="FolderDetails")
	/**如果是电视剧，描述电视剧的详情*/
	private FolderDetails folderDetails;
	
	@ElementList(name = "Image", inline = true,required=false)
	private List<Image> listImages;
	public String getProviderld() {
		return providerld;
	}
	public void setProviderld(String providerld) {
		this.providerld = providerld;
	}
	
	public String getAssetId() {
		return assetId;
	}
	public void setAssetId(String assetId) {
		this.assetId = assetId;
	}
	public String getParentAssetId() {
		return parentAssetId;
	}
	public void setParentAssetId(String parentAssetId) {
		this.parentAssetId = parentAssetId;
	}
	public String getDisplayName() {
		return displayName;
	}
	public void setDisplayName(String displayName) {
		this.displayName = displayName;
	}
	public String getFolderType() {
		return folderType;
	}
	public void setFolderType(String folderType) {
		this.folderType = folderType;
	}
	public FolderDetails getFolderDetails() {
		return folderDetails;
	}
	public void setFolderDetails(FolderDetails folderDetails) {
		this.folderDetails = folderDetails;
	}
	
	public List<Image> getListImages() {
		return listImages;
	}
	public void setListImages(List<Image> listImages) {
		this.listImages = listImages;
	}
	@Override
	public String toString() {
		return "FolderFrame [providerld=" + providerld + ", assetId=" + assetId + ", parentAssetId=" + parentAssetId
				+ ", displayName=" + displayName + ", folderType=" + folderType + ", folderDetails=" + folderDetails
				+ ", listImages=" + listImages + "]";
	}
	
	
	


}
