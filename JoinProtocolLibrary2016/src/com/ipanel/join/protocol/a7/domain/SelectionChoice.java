package com.ipanel.join.protocol.a7.domain;

import java.io.Serializable;

import org.simpleframework.xml.Attribute;
import org.simpleframework.xml.Element;
import org.simpleframework.xml.Root;
@Root(name="SelectionChoice",strict=false)
public class SelectionChoice implements Serializable {

	/**
	 * 
	 */
	private static final long serialVersionUID = -5039282987780357526L;
	@Attribute(required=false)
	private String format;
	@Attribute(required = false)
	private String displayPrice;
	@Attribute(required=false)
	private String rentalPeriod;
	@Attribute(required = false)
	private String viewLimit;
	@Attribute(required=false)
	private String screenShape;
	@Attribute(required=false)
	private String audioType;
	@Element(required =false)
	private LanguageSet languageSet;
	

	@Root
	class LanguageSet implements Serializable{

		/**
		 * 
		 */
		private static final long serialVersionUID = -6098814639601893890L;
		@Attribute(required =false)
		private String audio;
		@Attribute(required=false)
		private String subtitle;
		public String getAudio() {
			return audio;
		}
		public void setAudio(String audio) {
			this.audio = audio;
		}
		public String getSubtitle() {
			return subtitle;
		}
		public void setSubtitle(String subtitle) {
			this.subtitle = subtitle;
		}
		@Override
		public String toString() {
			return "LanguageSet [audio=" + audio + ", subtitle=" + subtitle
					+ "]";
		}
		
		
	}


	public String getFormat() {
		return format;
	}


	public void setFormat(String format) {
		this.format = format;
	}


	public String getDisplayPrice() {
		return displayPrice;
	}


	public void setDisplayPrice(String displayPrice) {
		this.displayPrice = displayPrice;
	}


	public String getRentalPeriod() {
		return rentalPeriod;
	}


	public void setRentalPeriod(String rentalPeriod) {
		this.rentalPeriod = rentalPeriod;
	}


	public String getViewLimit() {
		return viewLimit;
	}


	public void setViewLimit(String viewLimit) {
		this.viewLimit = viewLimit;
	}


	public String getScreenShape() {
		return screenShape;
	}


	public void setScreenShape(String screenShape) {
		this.screenShape = screenShape;
	}


	public String getAudioType() {
		return audioType;
	}


	public void setAudioType(String audioType) {
		this.audioType = audioType;
	}


	public LanguageSet getLanguageSet() {
		return languageSet;
	}


	public void setLanguageSet(LanguageSet languageSet) {
		this.languageSet = languageSet;
	}


	@Override
	public String toString() {
		return "SelectionChoice [format=" + format + ", displayPrice="
				+ displayPrice + ", rentalPeriod=" + rentalPeriod
				+ ", viewLimit=" + viewLimit + ", screenShape=" + screenShape
				+ ", audioType=" + audioType + ", languageSet=" + languageSet
				+ "]";
	}
	
	
}

