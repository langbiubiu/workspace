package com.ipanel.join.protocol.sihua.cqvod;

import java.io.Serializable;

import org.simpleframework.xml.Attribute;
import org.simpleframework.xml.Element;
import org.simpleframework.xml.Root;

@Root(name = "message")
public class Request_BindUser implements Serializable {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1841602555140832713L;
	@Attribute(required = false)
	private String module;
	@Attribute(required = false)
	private String version;
	@Element(required = false)
	private Header header;
	@Element(required = false)
	private Body body;

	@Root(name = "bindUser")
	public static class BindUser implements Serializable {

		/**
		 * 
		 */
		private static final long serialVersionUID = 1211601865316794664L;
		@Attribute(required = false)
		private String sp_code;
		@Attribute(required = false)
		private String app_code;
		@Attribute(required = false)
		private String token;
		@Attribute(required = false)
		private String bind_token;
		@Attribute(required = false)
		private String uuid;
		@Attribute(required = false)
		private String operation;
		@Attribute(required = false)
		private String ext_no;

		public String getSp_code() {
			return sp_code;
		}

		public void setSp_code(String sp_code) {
			this.sp_code = sp_code;
		}

		public String getApp_code() {
			return app_code;
		}

		public void setApp_code(String app_code) {
			this.app_code = app_code;
		}

		public String getToken() {
			return token;
		}

		public void setToken(String token) {
			this.token = token;
		}

		public String getUuid() {
			return uuid;
		}

		public void setUuid(String uuid) {
			this.uuid = uuid;
		}


		public String getBind_token() {
			return bind_token;
		}

		public void setBind_token(String bind_token) {
			this.bind_token = bind_token;
		}

		public String getOperation() {
			return operation;
		}

		public void setOperation(String operation) {
			this.operation = operation;
		}

		public String getExt_no() {
			return ext_no;
		}

		public void setExt_no(String ext_no) {
			this.ext_no = ext_no;
		}

	}

	@Root(name = "body")
	public static class Body implements Serializable {

		/**
		 * 
		 */
		private static final long serialVersionUID = 5694295571786834660L;
		@Element(name = "bindUser")
		private BindUser bindUser;
		public BindUser getBindUser() {
			return bindUser;
		}
		public void setBindUser(BindUser bindUser) {
			this.bindUser = bindUser;
		}


	}

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

}
