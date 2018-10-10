package com.ipanel.join.protocol.homed.domain;

import java.io.Serializable;

import com.google.gson.annotations.Expose;

public class VideoPlayData implements Serializable {

	/**
	 * 
	 */
	private static final long serialVersionUID = 7924092624833310873L;
	@Expose
	private String ret;
	@Expose
	private String ret_msg;
	@Expose
	private String video_id;
	@Expose
	private String video_name;
	@Expose
	private String video_info_url;
	@Expose
	private String type;
	@Expose
	private String offset;
	@Expose
	private String play_time;
	@Expose
	private Pic pic;

	public String getRet() {
		return ret;
	}

	public void setRet(String ret) {
		this.ret = ret;
	}

	public String getRet_msg() {
		return ret_msg;
	}

	public void setRet_msg(String ret_msg) {
		this.ret_msg = ret_msg;
	}

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

	public String getOffset() {
		return offset;
	}

	public void setOffset(String offset) {
		this.offset = offset;
	}

	public String getPlay_time() {
		return play_time;
	}

	public void setPlay_time(String play_time) {
		this.play_time = play_time;
	}

	public Pic getPic() {
		return pic;
	}

	public void setPic(Pic pic) {
		this.pic = pic;
	}

}
