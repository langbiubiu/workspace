package com.ipanel.join.protocol.homed.domain;

import java.io.Serializable;

import com.google.gson.annotations.Expose;

public class DataEntry implements Serializable {

	/**
	 * 
	 */
	private static final long serialVersionUID = -6626354907850012736L;
	@Expose
	private String content_type;
	@Expose
	private String id;
	@Expose
	private String channel_id;
	@Expose
	private String channel_name;
	@Expose
	private String name;
	@Expose
	private String url;
	@Expose
	private ChlCfg chlCfg;
	@Expose
	private String describe;
	@Expose
	private String installcount;
	@Expose
	private String avg_score;
	@Expose
	private String free_in_limit_time;
	@Expose
	private String price;
	@Expose
	private String is_own;
	@Expose
	private String star_num;
	@Expose
	private String play_times;
	@Expose
	private String start_time;
	@Expose
	private String end_time;
	@Expose
	private Ad ad;
	@Expose
	private String subTypeID;

	public String getContent_type() {
		return content_type;
	}

	public void setContent_type(String content_type) {
		this.content_type = content_type;
	}

	public String getId() {
		return id;
	}

	public void setId(String id) {
		this.id = id;
	}

	public String getChannel_id() {
		return channel_id;
	}

	public void setChannel_id(String channel_id) {
		this.channel_id = channel_id;
	}

	public String getChannel_name() {
		return channel_name;
	}

	public void setChannel_name(String channel_name) {
		this.channel_name = channel_name;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getUrl() {
		return url;
	}

	public void setUrl(String url) {
		this.url = url;
	}

	public ChlCfg getChlCfg() {
		return chlCfg;
	}

	public void setChlCfg(ChlCfg chlCfg) {
		this.chlCfg = chlCfg;
	}

	public String getDescribe() {
		return describe;
	}

	public void setDescribe(String describe) {
		this.describe = describe;
	}

	public String getInstallcount() {
		return installcount;
	}

	public void setInstallcount(String installcount) {
		this.installcount = installcount;
	}

	public String getAvg_score() {
		return avg_score;
	}

	public void setAvg_score(String avg_score) {
		this.avg_score = avg_score;
	}

	public String getFree_in_limit_time() {
		return free_in_limit_time;
	}

	public void setFree_in_limit_time(String free_in_limit_time) {
		this.free_in_limit_time = free_in_limit_time;
	}

	public String getPrice() {
		return price;
	}

	public void setPrice(String price) {
		this.price = price;
	}

	public String getIs_own() {
		return is_own;
	}

	public void setIs_own(String is_own) {
		this.is_own = is_own;
	}

	public String getStar_num() {
		return star_num;
	}

	public void setStar_num(String star_num) {
		this.star_num = star_num;
	}

	public String getPlay_times() {
		return play_times;
	}

	public void setPlay_times(String play_times) {
		this.play_times = play_times;
	}

	public String getStart_time() {
		return start_time;
	}

	public void setStart_time(String start_time) {
		this.start_time = start_time;
	}

	public String getEnd_time() {
		return end_time;
	}

	public void setEnd_time(String end_time) {
		this.end_time = end_time;
	}

	public Ad getAd() {
		return ad;
	}

	public void setAd(Ad ad) {
		this.ad = ad;
	}

	public String getSubTypeID() {
		return subTypeID;
	}

	public void setSubTypeID(String subTypeID) {
		this.subTypeID = subTypeID;
	}

}
