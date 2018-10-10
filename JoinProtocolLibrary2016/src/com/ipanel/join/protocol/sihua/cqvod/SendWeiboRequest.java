package com.ipanel.join.protocol.sihua.cqvod;

import java.io.Serializable;

import com.google.gson.annotations.Expose;

public class SendWeiboRequest implements Serializable {

	/**
	 * 
	 */
	private static final long serialVersionUID = 3933312570965281279L;
	@Expose
	private String systemCode;//	true	string	ϵͳ����Ĺ̶����������ýӿ�ǰ����
	@Expose
	private String userid;	//false	string	�û���ʶ
	@Expose
	private String iccard;	//false	string	�û����ţ�userid��iccard������дһ��
	@Expose
	private String content;	//false	string	Ҫ������΢���ı����ݣ�������URLencode�����ݲ�����140�����֡�
	@Expose
	private int visible; //false	int	΢���Ŀɼ��ԣ�0���������ܿ���1�����Լ��ɼ���2�����ѿɼ���3��ָ������ɼ���Ĭ��Ϊ0��
	@Expose
	private String list_id;	//false	string	΢���ı���Ͷ��ָ������ID��ֻ�е�visible����Ϊ3ʱ��Ч�ұ�ѡ��
	@Expose
	private String url;	//false	string	ͼƬ��URL��ַ��������http��ͷ��
	@Expose
	private String pic_id;//	false	string	�Ѿ��ϴ���ͼƬpid�����ʱʹ��Ӣ�İ�Ƕ��ŷ��ָ�����಻����9����
	@Expose
	private float lat;	//false	float	γ�ȣ���Ч��Χ��-90.0��+90.0��+��ʾ��γ��Ĭ��Ϊ0.0��
	@Expose
	private  String annotations;//	false	string	Ԫ���ݣ���Ҫ��Ϊ�˷��������Ӧ�ü�¼һЩ�ʺ����Լ�ʹ�õ���Ϣ��ÿ��΢�����԰���һ�����߶��Ԫ���ݣ�������json�ִ�����ʽ�ύ���ִ����Ȳ�����512���ַ����������ݿ����Զ���
	@Expose
	private String rip;//	false	string	�������ϱ��Ĳ����û���ʵIP�����磺211.156.0.1��
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
