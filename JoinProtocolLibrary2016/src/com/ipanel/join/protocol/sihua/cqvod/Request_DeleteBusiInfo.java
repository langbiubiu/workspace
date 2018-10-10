package com.ipanel.join.protocol.sihua.cqvod;

import java.io.Serializable;

import org.simpleframework.xml.Attribute;
import org.simpleframework.xml.Element;
import org.simpleframework.xml.Root;

@Root(name = "message")
public class Request_DeleteBusiInfo implements Serializable {

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

	@Root(name = "deleteBusiInfo")
	public static class DeleteBusiInfo implements Serializable {

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
		private String uuid;
		@Attribute(required = false)
		private String info_type;
		@Attribute(required = false)
		private String user_no;
		@Attribute(required = false)
		private String info_id;
		
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

		public String getInfo_type() {
			return info_type;
		}

		public void setInfo_type(String info_type) {
			this.info_type = info_type;
		}

		public String getUser_no() {
			return user_no;
		}

		public void setUser_no(String user_no) {
			this.user_no = user_no;
		}

		public String getInfo_id() {
			return info_id;
		}

		public void setInfo_id(String info_id) {
			this.info_id = info_id;
		}



	}

	@Root(name = "body")
	public static class Body implements Serializable {

		/**
		 * 
		 */
		private static final long serialVersionUID = 5694295571786834660L;
		@Element(name = "deleteBusiInfo")
		private DeleteBusiInfo deleteBusiInfo;
		public DeleteBusiInfo getDeleteBusiInfo() {
			return deleteBusiInfo;
		}
		public void setDeleteBusiInfo(DeleteBusiInfo deleteBusiInfo) {
			this.deleteBusiInfo = deleteBusiInfo;
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
