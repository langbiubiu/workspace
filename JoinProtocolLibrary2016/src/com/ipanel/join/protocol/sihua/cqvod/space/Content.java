package com.ipanel.join.protocol.sihua.cqvod.space;

import java.io.Serializable;

import org.simpleframework.xml.Attribute;
import org.simpleframework.xml.Root;

@Root(name="content",strict=false)
public class Content implements Serializable{

	/**
	 * 
	 */
	private static final long serialVersionUID = -3264151017069255450L;
	@Attribute(required=false)
	private String OrderCode;
	@Attribute(required=false)
	private String ChannelID;
	@Attribute(required=false)
	private String ChannelName;
	@Attribute(required=false)
	private String ProgramID;
	@Attribute(required=false)
	private String ProgramName;
	@Attribute(required=false)
	private String RecordType;
	@Attribute(required=false)
	private String BeginTime;
	@Attribute(required=false)
	private String EndTime;
	@Attribute(required=false)
	private String CreateTime;
	@Attribute(required=false)
	private String UpdateTime;
	@Attribute(required=false)
	private String Status;
	@Attribute(required=false)
	private String Size;
	@Attribute(required=false)
	private String ContentID;
	@Attribute(required=false)
	private String ContentType;
	@Attribute(required=false)
	private String FileType;
	@Attribute(required=false)
	private String Url;
	@Attribute(required=false)
	private String ImageFormat;
	@Attribute(required=false)
	private String ImageResolution;
	@Attribute(required=false)
	private String ContentName;
	@Attribute(required=false)
	private String ContentSearchType;
	public String getOrderCode() {
		return OrderCode;
	}
	public void setOrderCode(String orderCode) {
		OrderCode = orderCode;
	}
	public String getChannelID() {
		return ChannelID;
	}
	public void setChannelID(String channelID) {
		ChannelID = channelID;
	}
	public String getChannelName() {
		return ChannelName;
	}
	public void setChannelName(String channelName) {
		ChannelName = channelName;
	}
	public String getProgramID() {
		return ProgramID;
	}
	public void setProgramID(String programID) {
		ProgramID = programID;
	}
	public String getProgramName() {
		return ProgramName;
	}
	public void setProgramName(String programName) {
		ProgramName = programName;
	}
	public String getRecordType() {
		return RecordType;
	}
	public void setRecordType(String recordType) {
		RecordType = recordType;
	}
	public String getBeginTime() {
		return BeginTime;
	}
	public void setBeginTime(String beginTime) {
		BeginTime = beginTime;
	}
	public String getEndTime() {
		return EndTime;
	}
	public void setEndTime(String endTime) {
		EndTime = endTime;
	}
	public String getCreateTime() {
		return CreateTime;
	}
	public void setCreateTime(String createTime) {
		CreateTime = createTime;
	}
	public String getUpdateTime() {
		return UpdateTime;
	}
	public void setUpdateTime(String updateTime) {
		UpdateTime = updateTime;
	}
	public String getStatus() {
		return Status;
	}
	public void setStatus(String status) {
		Status = status;
	}
	public String getSize() {
		return Size;
	}
	public void setSize(String size) {
		Size = size;
	}
	public String getContentID() {
		return ContentID;
	}
	public void setContentID(String contentID) {
		ContentID = contentID;
	}
	public String getContentType() {
		return ContentType;
	}
	public void setContentType(String contentType) {
		ContentType = contentType;
	}
	public String getFileType() {
		return FileType;
	}
	public void setFileType(String fileType) {
		FileType = fileType;
	}
	public String getUrl() {
		return Url;
	}
	public void setUrl(String url) {
		Url = url;
	}
	public String getImageFormat() {
		return ImageFormat;
	}
	public void setImageFormat(String imageFormat) {
		ImageFormat = imageFormat;
	}
	public String getImageResolution() {
		return ImageResolution;
	}
	public void setImageResolution(String imageResolution) {
		ImageResolution = imageResolution;
	}
	public String getContentName() {
		return ContentName;
	}
	public void setContentName(String contentName) {
		ContentName = contentName;
	}
	public String getContentSearchType() {
		return ContentSearchType;
	}
	public void setContentSearchType(String contentSearchType) {
		ContentSearchType = contentSearchType;
	}
	
	
	
}
