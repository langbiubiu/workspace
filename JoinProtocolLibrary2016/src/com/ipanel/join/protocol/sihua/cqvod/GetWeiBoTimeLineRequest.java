package com.ipanel.join.protocol.sihua.cqvod;

import java.io.Serializable;

import com.google.gson.annotations.Expose;

public class GetWeiBoTimeLineRequest implements Serializable {

	/**
	 * 
	 */
	private static final long serialVersionUID = 7288994599114565249L;
	
	@Expose
	private String systemCode;	//true	string	ϵͳ����Ĺ̶����������ýӿ�ǰ����
	@Expose
	private String userid;	//false	string	�û���ʶ
	@Expose
	private String iccard; //	false	string	�û����ţ�userid��iccard������дһ��
	@Expose
	private String timeline_type; //false	string	��ϸ�μ�����������ͣ�Ĭ��home_timeline
	@Expose
	private String since_id;	//false	int64	��ָ���˲������򷵻�ID��since_id���΢��������since_idʱ�����΢������Ĭ��Ϊ0��
	@Expose
	private String max_id;	//false	int64	��ָ���˲������򷵻�IDС�ڻ����max_id��΢����Ĭ��Ϊ0��
	@Expose
	private String count;	//false	int	��ҳ���صļ�¼��������󲻳���100��Ĭ��Ϊ20��
	@Expose
	private String page;	//false	int	���ؽ����ҳ�룬Ĭ��Ϊ1��
	@Expose
	private String base_app;	//false	int	�Ƿ�ֻ��ȡ��ǰӦ�õ����ݡ�1Ϊ�ǣ�����ǰӦ�ã���
	@Expose
	private String feature;	//false	int	��������ID��0��ȫ����1��ԭ����2��ͼƬ��3����Ƶ��4�����֣�Ĭ��Ϊ0��
	@Expose
	private String trim_user;	//false	int	����ֵ��user�ֶο��أ�0����������user�ֶΡ�1��user�ֶν�����user_id��Ĭ��Ϊ0��
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
