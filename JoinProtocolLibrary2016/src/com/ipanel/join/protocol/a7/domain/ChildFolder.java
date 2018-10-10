package com.ipanel.join.protocol.a7.domain;

import java.io.Serializable;
import java.util.List;

import org.simpleframework.xml.Attribute;
import org.simpleframework.xml.Element;
import org.simpleframework.xml.ElementList;
import org.simpleframework.xml.Root;

/**
 * 一级栏目信息
 * 
 * @author dzwillpower
 * @time 2013年12月6日 上午11:14:37
 */
@Root(name = "ChildFolder",strict=false)
public class ChildFolder implements Serializable {
	private static final long serialVersionUID = -4195099295682307129L;

	@Attribute(required=false)
	private String assetId;
	@Attribute(required = false)
	private String providerld;
	@Attribute(required = false)
	private String parentAssetId;
	@Attribute(required = false)
	private String displayName;
	@Attribute(required = false)
	private String secondDisplayName;
	@Attribute(required = false)
	private String infoText;
	@Attribute(required = false)
	private String previewProviderld;
	@Attribute(required = false)
	private String folderType;
	@Attribute(required = false)
	private String selectableltemSortby;
	@Attribute(required = false)
	private int selectableltemSortDirection;
	@Attribute(required = false)
	private String childFolderSortby;
	@Attribute(required = false)
	private String childFolderSortDirection;
	@Attribute(required = false)
	private String createDate;
	@Attribute(required = false)
	private String modifyDate;
	@ElementList(name = "Image", inline = true,required=false)
	private List<Image> listImages;
	@Element(name = "RatingControl", required = false)
	private String ratingControl;
	@Attribute(required = false)
	private String serviceId;
	@Element(required=false,name="FolderDetails")
	private FolderDetails folderDetails;
	public String getAssetld() {
		return assetId;
	}
	public void setAssetld(String assetld) {
		this.assetId = assetld;
	}
	public String getProviderld() {
		return providerld;
	}
	public void setProviderld(String providerld) {
		this.providerld = providerld;
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
	public String getSecondDisplayName() {
		return secondDisplayName;
	}
	public void setSecondDisplayName(String secondDisplayName) {
		this.secondDisplayName = secondDisplayName;
	}
	public String getInfoText() {
		return infoText;
	}
	public void setInfoText(String infoText) {
		this.infoText = infoText;
	}
	public String getPreviewProviderld() {
		return previewProviderld;
	}
	public void setPreviewProviderld(String previewProviderld) {
		this.previewProviderld = previewProviderld;
	}
	public String getFolderType() {
		return folderType;
	}
	public void setFolderType(String folderType) {
		this.folderType = folderType;
	}
	public String getSelectableltemSortby() {
		return selectableltemSortby;
	}
	public void setSelectableltemSortby(String selectableltemSortby) {
		this.selectableltemSortby = selectableltemSortby;
	}
	public int getSelectableltemSortDirection() {
		return selectableltemSortDirection;
	}
	public void setSelectableltemSortDirection(int selectableltemSortDirection) {
		this.selectableltemSortDirection = selectableltemSortDirection;
	}
	public String getChildFolderSortby() {
		return childFolderSortby;
	}
	public void setChildFolderSortby(String childFolderSortby) {
		this.childFolderSortby = childFolderSortby;
	}
	public String getChildFolderSortDirection() {
		return childFolderSortDirection;
	}
	public void setChildFolderSortDirection(String childFolderSortDirection) {
		this.childFolderSortDirection = childFolderSortDirection;
	}
	public String getCreateDate() {
		return createDate;
	}
	public void setCreateDate(String createDate) {
		this.createDate = createDate;
	}
	public String getModifyDate() {
		return modifyDate;
	}
	public void setModifyDate(String modifyDate) {
		this.modifyDate = modifyDate;
	}
	
	public String getAssetId() {
		return assetId;
	}
	public void setAssetId(String assetId) {
		this.assetId = assetId;
	}
	public List<Image> getListImages() {
		return listImages;
	}
	public void setListImages(List<Image> listImages) {
		this.listImages = listImages;
	}
	public FolderDetails getFolderDetails() {
		return folderDetails;
	}
	public void setFolderDetails(FolderDetails folderDetails) {
		this.folderDetails = folderDetails;
	}
	public String getRatingControl() {
		return ratingControl;
	}
	public void setRatingControl(String ratingControl) {
		this.ratingControl = ratingControl;
	}
	public String getServiceId() {
		return serviceId;
	}
	public void setServiceId(String serviceId) {
		this.serviceId = serviceId;
	}
	@Override
	public String toString() {
		return "ChildFolder [assetId=" + assetId + ", providerld=" + providerld + ", parentAssetId=" + parentAssetId
				+ ", displayName=" + displayName + ", secondDisplayName=" + secondDisplayName + ", infoText="
				+ infoText + ", previewProviderld=" + previewProviderld + ", folderType=" + folderType
				+ ", selectableltemSortby=" + selectableltemSortby + ", selectableltemSortDirection="
				+ selectableltemSortDirection + ", childFolderSortby=" + childFolderSortby
				+ ", childFolderSortDirection=" + childFolderSortDirection + ", createDate=" + createDate
				+ ", modifyDate=" + modifyDate + ", listImages=" + listImages + ", ratingControl=" + ratingControl
				+ ", serviceId=" + serviceId + ", folderDetails=" + folderDetails + "]";
	}
	

	
}
