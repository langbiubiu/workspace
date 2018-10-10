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
	/** �ṩ��ID��Ԥ��*/
	private String providerld;
	@Attribute(required=false)
	/** ����Ŀ��ID*/
	private String assetId;
	@Attribute(required=false)
	/**����Ŀ���ʲ�ID�������һ����Ŀ��������Բ�����*/
	private String parentAssetId;
	@Attribute(required=false)
	/**����Ŀ��ʾ��ҳ���ϵ�����*/
	private String displayName;
	@Attribute(required=false)
	/**��Ŀ�����͡�0:��Ҷ�ڵ���,1:��Ӱ��,2:���Ӿ���,3:������,4:����OK��*/
	private String folderType;
	@Element(required=false ,name="FolderDetails")
	/**����ǵ��Ӿ磬�������Ӿ������*/
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
