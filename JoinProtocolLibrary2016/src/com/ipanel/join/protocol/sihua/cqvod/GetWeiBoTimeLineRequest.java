package com.ipanel.join.protocol.sihua.cqvod;

import java.io.Serializable;

import com.google.gson.annotations.Expose;

public class GetWeiBoTimeLineRequest implements Serializable {

	/**
	 * 
	 */
	private static final long serialVersionUID = 7288994599114565249L;
	
	@Expose
	private String systemCode;	//true	string	系统分配的固定参数，调用接口前申请
	@Expose
	private String userid;	//false	string	用户标识
	@Expose
	private String iccard; //	false	string	用户卡号，userid和iccard必须填写一个
	@Expose
	private String timeline_type; //false	string	详细参加下面参数解释，默认home_timeline
	@Expose
	private String since_id;	//false	int64	若指定此参数，则返回ID比since_id大的微博（即比since_id时间晚的微博），默认为0。
	@Expose
	private String max_id;	//false	int64	若指定此参数，则返回ID小于或等于max_id的微博，默认为0。
	@Expose
	private String count;	//false	int	单页返回的记录条数，最大不超过100，默认为20。
	@Expose
	private String page;	//false	int	返回结果的页码，默认为1。
	@Expose
	private String base_app;	//false	int	是否只获取当前应用的数据。1为是（仅当前应用）。
	@Expose
	private String feature;	//false	int	过滤类型ID，0：全部、1：原创、2：图片、3：视频、4：音乐，默认为0。
	@Expose
	private String trim_user;	//false	int	返回值中user字段开关，0：返回完整user字段、1：user字段仅返回user_id，默认为0。
	public String getSystemCode() {
		return systemCode;
	}
	public void setSystemCode(String systemCode) {
		this.systemCode = systemCode;
	}
	public String getUserid() {
		return userid;
	}
	public void setUserid(String userid) {
		this.userid = userid;
	}
	public String getIccard() {
		return iccard;
	}
	public void setIccard(String iccard) {
		this.iccard = iccard;
	}
	public String getTimeline_type() {
		return timeline_type;
	}
	public void setTimeline_type(String timeline_type) {
		this.timeline_type = timeline_type;
	}
	public String getSince_id() {
		return since_id;
	}
	public void setSince_id(String since_id) {
		this.since_id = since_id;
	}
	public String getMax_id() {
		return max_id;
	}
	public void setMax_id(String max_id) {
		this.max_id = max_id;
	}
	public String getCount() {
		return count;
	}
	public void setCount(String count) {
		this.count = count;
	}
	public String getPage() {
		return page;
	}
	public void setPage(String page) {
		this.page = page;
	}
	public String getBase_app() {
		return base_app;
	}
	public void setBase_app(String base_app) {
		this.base_app = base_app;
	}
	public String getFeature() {
		return feature;
	}
	public void setFeature(String feature) {
		this.feature = feature;
	}
	public String getTrim_user() {
		return trim_user;
	}
	public void setTrim_user(String trim_user) {
		this.trim_user = trim_user;
	}
	@Override
	public String toString() {
		return "GetWeiBoTimeLineRequest [systemCode=" + systemCode + ", userid=" + userid + ", iccard=" + iccard
				+ ", timeline_type=" + timeline_type + ", since_id=" + since_id + ", max_id=" + max_id + ", count="
				+ count + ", page=" + page + ", base_app=" + base_app + ", feature=" + feature + ", trim_user="
				+ trim_user + "]";
	}
	
	

}
