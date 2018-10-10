package com.ipanel.join.protocol.sihua.cqvod;

import java.io.Serializable;

import org.simpleframework.xml.Attribute;
import org.simpleframework.xml.Element;
import org.simpleframework.xml.Root;

@Root(name = "message")
public class WeiboResponse implements Serializable {

	/**
	 * 
	 */
	private static final long serialVersionUID = -3366034236596634028L;
	@Attribute(required=false)
	private String module;
	@Attribute(required=false)
	private String version;
	@Element(required=false)
	private Header header;
	@Element(required=false)
	private Body body;
	public String getModule() {
		return module;
	}

	public void setModule(String module) {
		this.module = module;
	}

	public String getVersion() {
		return version;
	}

	public void setVersion(String version) {
		this.version = version;
	}

	public Header getHeader() {
		return header;
	}

	public void setHeader(Header header) {
		this.header = header;
	}

	public Body getBody() {
		return body;
	}

	public void setBody(Body body) {
		this.body = body;
	}

	@Root(name = "body")
	public static class Body implements Serializable {

		/**
		 * 
		 */
		private static final long serialVersionUID = -5466094989113280160L;
		@Element(required=false)
		private WeiboBind weiboBind;
		public WeiboBind getWeiboBind() {
			return weiboBind;
		}
		public void setWeiboBind(WeiboBind weiboBind) {
			this.weiboBind = weiboBind;
		}
		@Override
		public String toString() {
			return "Body [weiboBind=" + weiboBind + "]";
		}
	}

	@Root(name = "weiboBind")
	public static class WeiboBind implements Serializable {

		/**
		 * 
		 */
		private static final long serialVersionUID = -7902397882018801124L;
		@Attribute(required=false)
		private String result;
		@Attribute(required=false)
		private String resultDesc;
		@Attribute(required=false)
		private String weiboAccount;
		@Attribute(required=false)
		private String weiboPassword;
		@Attribute(required=false)
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
		@Override
		public String toString() {
			return "WeiboBind [result=" + result + ", resultDesc=" + resultDesc + ", weiboAccount=" + weiboAccount
					+ ", weiboPassword=" + weiboPassword + ", weiboAuthorizeCode=" + weiboAuthorizeCode + "]";
		}	
		
		
	}

	@Override
	public String toString() {
		return "WeiboResponse [module=" + module + ", version=" + version + ", header=" + header + ", body=" + body
				+ "]";
	}
	
	

}
