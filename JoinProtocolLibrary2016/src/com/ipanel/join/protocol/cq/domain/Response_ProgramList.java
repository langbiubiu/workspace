package com.ipanel.join.protocol.cq.domain;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

public class Response_ProgramList implements Serializable {

	/**
	 * 
	 */
	private static final long serialVersionUID = 4619447975337012800L;
	@Expose
	@SerializedName("header")
	private Header header;
	@Expose
	@SerializedName("body")
	private Body body;	
	
	public Header getHeader() {
		return header;
	}

	public void setHeader(Header header) {
		this.header = header;
	}

	public Body getBody() {
		return body;
	}

	public void setBody(Body body) {
		this.body = body;
	}


	public static class Header implements Serializable {

		/**
		 * 
		 */
		private static final long serialVersionUID = -9083709618655186075L;
		
		
	
	}

	public static class Body implements Serializable {
		
		/**
		 * 
		 */
		private static final long serialVersionUID = 4370482413540062368L;
		@Expose
		@SerializedName("result")
		private Result result;
		@Expose
		@SerializedName("channels")
		private List<Channels> channels = new ArrayList<Channels>();
		public Result getResult() {
			return result;
		}
		public void setResult(Result result) {
			this.result = result;
		}
		public List<Channels> getChannels() {
			return channels;
		}
		public void setChannels(List<Channels> channels) {
			this.channels = channels;
		}

		
	}

	public class Result implements Serializable {

		/**
		 * 
		 */
		private static final long serialVersionUID = 174550708952618116L;
		
		@Expose
		private String code;
		@Expose
		private String description;
		public String getCode() {
			return code;
		}
		public void setCode(String code) {
			this.code = code;
		}
		public String getDescription() {
			return description;
		}
		public void setDescription(String description) {
			this.description = description;
		}
	}
	
	public static class Channels implements Serializable {

		/**
		 * 
		 */
		private static final long serialVersionUID = 9114096560924830634L;
		@Expose
		private String titleFull;
		@Expose
		private String channelCode;
		@Expose
		private String type;
		@Expose
		private String status;
		@Expose
		@SerializedName("physicalChannels")
		private List<PhysicalChannels> physicalchannels = new ArrayList<PhysicalChannels>();
		@Expose
		@SerializedName("assetPrograms")
		private List<AssetPrograms> assetprograms = new ArrayList<AssetPrograms>();
		public String getTitleFull() {
			return titleFull;
		}
		public void setTitleFull(String titleFull) {
			this.titleFull = titleFull;
		}
		public String getChannelCode() {
			return channelCode;
		}
		public void setChannelCode(String channelCode) {
			this.channelCode = channelCode;
		}
		public String getType() {
			return type;
		}
		public void setType(String type) {
			this.type = type;
		}
		public String getStatus() {
			return status;
		}
		public void setStatus(String status) {
			this.status = status;
		}
		public List<PhysicalChannels> getPhysicalchannels() {
			return physicalchannels;
		}
		public void setPhysicalchannels(List<PhysicalChannels> physicalchannels) {
			this.physicalchannels = physicalchannels;
		}
		public List<AssetPrograms> getAssetprograms() {
			return assetprograms;
		}
		public void setAssetprograms(List<AssetPrograms> assetprograms) {
			this.assetprograms = assetprograms;
		}
		
		


	}
	public static class PhysicalChannels implements Serializable {

		/**
		 * 
		 */
		private static final long serialVersionUID = 8269491289173547717L;
		@Expose
		private String assetID;
		@Expose
		private String encodingProfile;
		@Expose
		private String videoCodec;
		@Expose
		private String audioCodec;
		@Expose
		private String hDFlag;
		@Expose
		private String channelID;
		@Expose
		private String bitRate;
		@Expose
		private String mimeType;
		public String getAssetID() {
			return assetID;
		}
		public void setAssetID(String assetID) {
			this.assetID = assetID;
		}
		public String getEncodingProfile() {
			return encodingProfile;
		}
		public void setEncodingProfile(String encodingProfile) {
			this.encodingProfile = encodingProfile;
		}
		public String getVideoCodec() {
			return videoCodec;
		}
		public void setVideoCodec(String videoCodec) {
			this.videoCodec = videoCodec;
		}
		public String getAudioCodec() {
			return audioCodec;
		}
		public void setAudioCodec(String audioCodec) {
			this.audioCodec = audioCodec;
		}
		public String gethDFlag() {
			return hDFlag;
		}
		public void sethDFlag(String hDFlag) {
			this.hDFlag = hDFlag;
		}
		public String getChannelID() {
			return channelID;
		}
		public void setChannelID(String channelID) {
			this.channelID = channelID;
		}
		public String getBitRate() {
			return bitRate;
		}
		public void setBitRate(String bitRate) {
			this.bitRate = bitRate;
		}
		public String getMimeType() {
			return mimeType;
		}
		public void setMimeType(String mimeType) {
			this.mimeType = mimeType;
		}


	}
	
	public static class AssetPrograms implements Serializable {

		/**
		 * 
		 */
		private static final long serialVersionUID = 5311022803405352742L;
		@Expose
		private String assetID;
		@Expose
		private String startDateTime;
		@Expose
		private String endDateTime;
		@Expose
		private String programName;
		@Expose
		private String description;
		@Expose
		private String duration;
		@Expose
		private String channelID;
		@Expose
		private String category;
		@Expose
		private String status;
		public String getAssetID() {
			return assetID;
		}
		public void setAssetID(String assetID) {
			this.assetID = assetID;
		}
		public String getStartDateTime() {
			return startDateTime;
		}
		public void setStartDateTime(String startDateTime) {
			this.startDateTime = startDateTime;
		}
		public String getEndDateTime() {
			return endDateTime;
		}
		public void setEndDateTime(String endDateTime) {
			this.endDateTime = endDateTime;
		}
		public String getProgramName() {
			return programName;
		}
		public void setProgramName(String programName) {
			this.programName = programName;
		}
		public String getDescription() {
			return description;
		}
		public void setDescription(String description) {
			this.description = description;
		}
		public String getDuration() {
			return duration;
		}
		public void setDuration(String duration) {
			this.duration = duration;
		}
		public String getChannelID() {
			return channelID;
		}
		public void setChannelID(String channelID) {
			this.channelID = channelID;
		}
		public String getCategory() {
			return category;
		}
		public void setCategory(String category) {
			this.category = category;
		}
		public String getStatus() {
			return status;
		}
		public void setStatus(String status) {
			this.status = status;
		}
		


	}
	
}
