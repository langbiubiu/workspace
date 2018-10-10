package com.ipanel.join.protocol.a7.domain;

import java.io.Serializable;

import org.simpleframework.xml.Attribute;
import org.simpleframework.xml.Root;

@Root(name = "Image",strict=false)
public class Image implements Serializable {
	private static final long serialVersionUID = 8744638215178662011L;
	@Attribute
	private String posterUrl;
	@Attribute
	private String rank;
	@Attribute(required=false)
	private String height;
	@Attribute(required=false)
	private String width;

	public String getHeight() {
		return height;
	}

	public void setHeight(String height) {
		this.height = height;
	}

	public String getWidth() {
		return width;
	}

	public void setWidth(String width) {
		this.width = width;
	}

	public String getPosterUrl() {
		return posterUrl;
	}

	public void setPosterUrl(String posterUrl) {
		this.posterUrl = posterUrl;
	}

	public String getRank() {
		return rank;
	}

	public void setRank(String rank) {
		this.rank = rank;
	}

	@Override
	public String toString() {
		return "Image [posterUrl=" + posterUrl + ", rank=" + rank + ", height=" + height + ", width=" + width + "]";
	}

	

}
