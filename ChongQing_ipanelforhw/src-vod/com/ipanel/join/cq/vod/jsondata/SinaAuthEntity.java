package com.ipanel.join.cq.vod.jsondata;

import org.simpleframework.xml.Attribute;
import org.simpleframework.xml.Path;
import org.simpleframework.xml.Root;

@Root(name="message")
public class SinaAuthEntity {
	@Attribute(name="result")
	@Path("body/weiboBind")
	private String result;
	@Attribute(name="resultDesc")
	@Path("body/weiboBind")
	private String resultDesc;
	@Attribute(name="weiboAccount")
	@Path("body/weiboBind")
	private String weiboAccount;
	@Attribute(name="weiboPassword")
	@Path("body/weiboBind")
	private String weiboPassword;
	@Attribute(name="weiboAuthorizeCode")
	@Path("body/weiboBind")
	private String weiboAuthorizeCode;
	public String getResult() {
		return result;
	}
	public void setResult(String result) {
		this.result = result;
	}
	public String getResultDesc() {
		return resultDesc;
	}
	public void setResultDesc(String resultDesc) {
		this.resultDesc = resultDesc;
	}
	public String getWeiboAccount() {
		return weiboAccount;
	}
	public void setWeiboAccount(String weiboAccount) {
		this.weiboAccount = weiboAccount;
	}
	public String getWeiboPassword() {
		return weiboPassword;
	}
	public void setWeiboPassword(String weiboPassword) {
		this.weiboPassword = weiboPassword;
	}
	public String getWeiboAuthorizeCode() {
		return weiboAuthorizeCode;
	}
	public void setWeiboAuthorizeCode(String weiboAuthorizeCode) {
		this.weiboAuthorizeCode = weiboAuthorizeCode;
	}
	

}
