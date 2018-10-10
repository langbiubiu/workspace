package com.ipanel.join.protocol.sihua.cqvod;

import java.io.Serializable;

import com.google.gson.annotations.Expose;

public class SendWeiboRequest implements Serializable {

	/**
	 * 
	 */
	private static final long serialVersionUID = 3933312570965281279L;
	@Expose
	private String systemCode;//	true	string	系统分配的固定参数，调用接口前申请
	@Expose
	private String userid;	//false	string	用户标识
	@Expose
	private String iccard;	//false	string	用户卡号，userid和iccard必须填写一个
	@Expose
	private String content;	//false	string	要发布的微博文本内容，必须做URLencode，内容不超过140个汉字。
	@Expose
	private int visible; //false	int	微博的可见性，0：所有人能看，1：仅自己可见，2：密友可见，3：指定分组可见，默认为0。
	@Expose
	private String list_id;	//false	string	微博的保护投递指定分组ID，只有当visible参数为3时生效且必选。
	@Expose
	private String url;	//false	string	图片的URL地址，必须以http开头。
	@Expose
	private String pic_id;//	false	string	已经上传的图片pid，多个时使用英文半角逗号符分隔，最多不超过9个。
	@Expose
	private float lat;	//false	float	纬度，有效范围：-90.0到+90.0，+表示北纬，默认为0.0。
	@Expose
	private  String annotations;//	false	string	元数据，主要是为了方便第三方应用记录一些适合于自己使用的信息，每条微博可以包含一个或者多个元数据，必须以json字串的形式提交，字串长度不超过512个字符，具体内容可以自定。
	@Expose
	private String rip;//	false	string	开发者上报的操作用户真实IP，形如：211.156.0.1。
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
	public String getContent() {
		return content;
	}
	public void setContent(String content) {
		this.content = content;
	}
	public int getVisible() {
		return visible;
	}
	public void setVisible(int visible) {
		this.visible = visible;
	}
	public String getList_id() {
		return list_id;
	}
	public void setList_id(String list_id) {
		this.list_id = list_id;
	}
	public String getUrl() {
		return url;
	}
	public void setUrl(String url) {
		this.url = url;
	}
	public String getPic_id() {
		return pic_id;
	}
	public void setPic_id(String pic_id) {
		this.pic_id = pic_id;
	}
	public float getLat() {
		return lat;
	}
	public void setLat(float lat) {
		this.lat = lat;
	}
	public String getAnnotations() {
		return annotations;
	}
	public void setAnnotations(String annotations) {
		this.annotations = annotations;
	}
	public String getRip() {
		return rip;
	}
	public void setRip(String rip) {
		this.rip = rip;
	}
	@Override
	public String toString() {
		return "SendWeiboRequest [systemCode=" + systemCode + ", userid=" + userid + ", iccard=" + iccard
				+ ", content=" + content + ", visible=" + visible + ", list_id=" + list_id + ", url=" + url
				+ ", pic_id=" + pic_id + ", lat=" + lat + ", annotations=" + annotations + ", rip=" + rip + "]";
	}
	


}
