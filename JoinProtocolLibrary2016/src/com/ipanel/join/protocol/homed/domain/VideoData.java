package com.ipanel.join.protocol.homed.domain;

import java.io.Serializable;

import com.google.gson.annotations.Expose;

public class VideoData implements Serializable {

	/**
	 * 
	 */
	private static final long serialVersionUID = -531833283820534556L;
	@Expose
	private String video_id;
	@Expose
	private String video_name;
	@Expose
	private String direct_list;
	@Expose
	private String actor_list;
	@Expose
	private String video_time;
	@Expose
	private String video_info_url;
	@Expose
	private String type;
	@Expose
	private Pic pic;

	public String getVideo_id() {
		return video_id;
	}

	public void setVideo_id(String video_id) {
		this.video_id = video_id;
	}

	public String getVideo_name() {
		return video_name;
	}

	public void setVideo_name(String video_name) {
		this.video_name = video_name;
	}

	public String getDirect_list() {
		return direct_list;
	}

	public void setDirect_list(String direct_list) {
		this.direct_list = direct_list;
	}

	public String getActor_list() {
		return actor_list;
	}

	public void setActor_list(String actor_list) {
		this.actor_list = actor_list;
	}

	public String getVideo_time() {
		return video_time;
	}

	public void setVideo_time(String video_time) {
		this.video_time = video_time;
	}

	public String getVideo_info_url() {
		return video_info_url;
	}

	public void setVideo_info_url(String video_info_url) {
		this.video_info_url = video_info_url;
	}

	public String getType() {
		return type;
	}

	public void setType(String type) {
		this.type = type;
	}

	public Pic getPic() {
		return pic;
	}

	public void setPic(Pic pic) {
		this.pic = pic;
	}

}
