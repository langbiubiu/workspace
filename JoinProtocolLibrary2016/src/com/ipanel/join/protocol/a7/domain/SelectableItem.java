package com.ipanel.join.protocol.a7.domain;

import java.io.Serializable;
import java.util.List;

import org.simpleframework.xml.Attribute;
import org.simpleframework.xml.Element;
import org.simpleframework.xml.ElementList;
import org.simpleframework.xml.Root;

@Root(name = "SelectableItem",strict=false)
public class SelectableItem implements Serializable {
	/**
	 * 
	 */
	private static final long serialVersionUID = 6729951545021107896L;
	@Attribute(required = false)
	private String folderAssetId;
	@Attribute(required = false)
	private String providerld;
	@Attribute(required=false)
	private String assetId;
	@Attribute(required=false)
	private String titleBrief;
	@Attribute(required=false)
	private String titleFull;
	@Attribute(required=false)
	private String secondTitleFull;
	@Attribute(required=false)
	private String summaryShort;
	@Attribute(required=false)
	private String summarMedium;
	@Attribute(required=false)
	private String year;
	@Attribute(required=false)
	private String actorsDisplay;
	@Attribute(required=false)
	private String startDateTime;
	@Attribute(required=false)
	private String endDateTime;
	@Attribute(required=false)
	private String displayRunTime;
	@Attribute(required=false)
	private String runtime;
	@Attribute(required=false)
	private String previewAssetId;
	@Attribute(name="Rating",required=false)
	private String rating;
	@Attribute(required=false)
	private String languageCode;
	@Attribute(required=false)
	private String format;
	@Attribute(required=false)
	private String chapter;
	@Attribute(required=false)
	private String favorRating;
	@Attribute(required=false)
	private String imageLocation;
	@ElementList(name="Director",required=false,inline=true)
	private List<Director> directorList;
	@ElementList(name = "Image", inline = true,required=false)
	private List<Image> listImages;
	@Element(required=false,name="SelectionChoice")
	private SelectionChoice selectionChoice;
	@Attribute(required=false)
	private String serviceId;
	
	public String getServiceId() {
		return serviceId;
	}
	public void setServiceId(String serviceId) {
		this.serviceId = serviceId;
	}
	public String getFolderAssetId() {
		return folderAssetId;
	}
	public void setFolderAssetId(String folderAssetId) {
		this.folderAssetId = folderAssetId;
	}
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
	public String getTitleBrief() {
		return titleBrief;
	}
	public void setTitleBrief(String titleBrief) {
		this.titleBrief = titleBrief;
	}
	public String getTitleFull() {
		return titleFull;
	}
	public void setTitleFull(String titleFull) {
		this.titleFull = titleFull;
	}
	public String getSecondTitleFull() {
		return secondTitleFull;
	}
	public void setSecondTitleFull(String secondTitleFull) {
		this.secondTitleFull = secondTitleFull;
	}
	public String getSummaryShort() {
		return summaryShort;
	}
	public void setSummaryShort(String summaryShort) {
		this.summaryShort = summaryShort;
	}
	public String getSummarMedium() {
		return summarMedium;
	}
	public void setSummarMedium(String summarMedium) {
		this.summarMedium = summarMedium;
	}
	public String getYear() {
		return year;
	}
	public void setYear(String year) {
		this.year = year;
	}
	public String getActorsDisplay() {
		return actorsDisplay;
	}
	public void setActorsDisplay(String actorsDisplay) {
		this.actorsDisplay = actorsDisplay;
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
	public String getDisplayRunTime() {
		return displayRunTime;
	}
	public void setDisplayRunTime(String displayRunTime) {
		this.displayRunTime = displayRunTime;
	}
	public String getRuntime() {
		return runtime;
	}
	public void setRuntime(String runtime) {
		this.runtime = runtime;
	}
	public String getPreviewAssetId() {
		return previewAssetId;
	}
	public void setPreviewAssetId(String previewAssetId) {
		this.previewAssetId = previewAssetId;
	}
	public String getRating() {
		return rating;
	}
	public void setRating(String rating) {
		this.rating = rating;
	}
	public String getLanguageCode() {
		return languageCode;
	}
	public void setLanguageCode(String languageCode) {
		this.languageCode = languageCode;
	}
	public String getFormat() {
		return format;
	}
	public void setFormat(String format) {
		this.format = format;
	}
	public String getChapter() {
		return chapter;
	}
	public void setChapter(String chapter) {
		this.chapter = chapter;
	}
	public String getFavorRating() {
		return favorRating;
	}
	public void setFavorRating(String favorRating) {
		this.favorRating = favorRating;
	}
	public String getImageLocation() {
		return imageLocation;
	}
	public void setImageLocation(String imageLocation) {
		this.imageLocation = imageLocation;
	}
	
	
	public List<Director> getDirectorList() {
		return directorList;
	}
	public void setDirectorList(List<Director> directorList) {
		this.directorList = directorList;
	}
	public List<Image> getListImages() {
		return listImages;
	}
	public void setListImages(List<Image> listImages) {
		this.listImages = listImages;
	}
	public SelectionChoice getSelectionChoice() {
		return selectionChoice;
	}
	public void setSelectionChoice(SelectionChoice selectionChoice) {
		this.selectionChoice = selectionChoice;
	}
	
	@Root(name="Director",strict=false)
	public static class Director implements Serializable{

		/**
		 * 
		 */
		private static final long serialVersionUID = 5983787990227310890L;
		@Attribute(name="name",required=false)
		private String directorName;
		public String getDirector() {
			return directorName;
		}
		public void setDirector(String director) {
			this.directorName = director;
		}
		@Override
		public String toString() {
			return "Director [director=" + directorName + "]";
		}
		
		
	}

	@Override
	public String toString() {
		return "SelectableItem [folderAssetId=" + folderAssetId + ", providerld=" + providerld + ", assetId=" + assetId
				+ ", titleBrief=" + titleBrief + ", titleFull=" + titleFull + ", secondTitleFull=" + secondTitleFull
				+ ", summaryShort=" + summaryShort + ", summarMedium=" + summarMedium + ", year=" + year
				+ ", actorsDisplay=" + actorsDisplay + ", startDateTime=" + startDateTime + ", endDateTime="
				+ endDateTime + ", displayRunTime=" + displayRunTime + ", runtime=" + runtime + ", previewAssetId="
				+ previewAssetId + ", rating=" + rating + ", languageCode=" + languageCode + ", format=" + format
				+ ", chapter=" + chapter + ", favorRating=" + favorRating + ", imageLocation=" + imageLocation
				+ ", directorList=" + directorList + ", listImages=" + listImages + ", selectionChoice="
				+ selectionChoice + ", serviceId=" + serviceId + "]";
	}

	
	
	

	
}
