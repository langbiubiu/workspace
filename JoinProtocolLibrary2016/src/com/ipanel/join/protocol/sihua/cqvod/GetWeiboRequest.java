package com.ipanel.join.protocol.sihua.cqvod;

import java.io.Serializable;

import org.simpleframework.xml.Attribute;
import org.simpleframework.xml.Element;
import org.simpleframework.xml.Root;

@Root(name = "message")
public class GetWeiboRequest implements Serializable {

	/**
	 * 
	 */
	private static final long serialVersionUID = -4552206079769623619L;
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
		

	}

	@Root(name = "weiboBind")
	public static class WeiboBind implements Serializable {

		/**
		 * 
		 */
		private static final long serialVersionUID = -6135595491876083979L;
		@Attribute(required = false)
		private String operation;
		@Attribute(required = false)
		private String srcUserId;
		@Attribute(required = false)
		private String ICcard;
		@Attribute(required = false)
		private String weiboAccount;
		@Attribute(required = false)
		private String weiboPassword;
		@Attribute(required = false)
		private String weiboAuthorizeCode;
		@Attribute(required = false)
		private String token;
		public String getOperation() {
			return operation;
		}
		public void setOperation(String operation) {
			this.operation = operation;
		}
		public String getSrcUserId() {
			return srcUserId;
		}
		public void setSrcUserId(String srcUserId) {
			this.srcUserId = srcUserId;
		}
		public String getICcard() {
			return ICcard;
		}
		public void setICcard(String iCcard) {
			ICcard = iCcard;
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
		public String getToken() {
			return token;
		}
		public void setToken(String token) {
			this.token = token;
		}
		@Override
		public String toString() {
			return "WeiboBind [operation=" + operation + ", srcUserId=" + srcUserId + ", ICcard=" + ICcard
					+ ", weiboAccount=" + weiboAccount + ", weiboPassword=" + weiboPassword + ", weiboAuthorizeCode="
					+ weiboAuthorizeCode + ", token=" + token + "]";
		}
		

	}

}
