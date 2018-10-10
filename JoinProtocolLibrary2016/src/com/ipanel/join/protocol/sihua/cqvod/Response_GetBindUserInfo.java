package com.ipanel.join.protocol.sihua.cqvod;

import java.io.Serializable;
import java.util.List;

import org.simpleframework.xml.Attribute;
import org.simpleframework.xml.Element;
import org.simpleframework.xml.ElementList;
import org.simpleframework.xml.Root;

@Root(name = "message")
public class Response_GetBindUserInfo implements Serializable {

	/**
	 * 
	 */
	private static final long serialVersionUID = -606972786211305655L;
	@Attribute(required = false)
	private String module;
	@Attribute(required = false)
	private String version;
	@Element(required = false)
	private Header header;
	@Element(required = false)
	private Body body;

	@Root(name = "bindInfo")
	public static class BindInfo implements Serializable {

		/**
		 * 
		 */
		private static final long serialVersionUID = -6265978695981709554L;
		@Attribute(required = false)
		private String ext_no;
		@Attribute(required = false)
		private String type;
		@Attribute(required = false)
		private String bind_Date;
		@Attribute(required = false)
		private String app_Code;
		@Attribute(required = false)
		private String xmpp_id;

		public String getExt_no() {
			return ext_no;
		}

		public void setExt_no(String ext_no) {
			this.ext_no = ext_no;
		}

		public String getType() {
			return type;
		}

		public void setType(String type) {
			this.type = type;
		}

		public String getBind_Date() {
			return bind_Date;
		}

		public void setBind_Date(String bind_Date) {
			this.bind_Date = bind_Date;
		}

		public String getApp_Code() {
			return app_Code;
		}

		public void setApp_Code(String app_Code) {
			this.app_Code = app_Code;
		}

		public String getXmpp_id() {
			return xmpp_id;
		}

		public void setXmpp_id(String xmpp_id) {
			this.xmpp_id = xmpp_id;
		}

	}

	@Root(name = "searchBindInfo")
	public static class SearchBindInfo implements Serializable {

		/**
		 * 
		 */
		private static final long serialVersionUID = 6312601474614650357L;

		@ElementList(name = "bindInfo", inline = true, required = false)
		private List<BindInfo> bindInfos;
		@Attribute
		private String return_code;
		@Attribute
		private String return_message;

		public List<BindInfo> getBindInfos() {
			return bindInfos;
		}

		public void setBindInfos(List<BindInfo> bindInfos) {
			this.bindInfos = bindInfos;
		}

		public String getReturn_code() {
			return return_code;
		}

		public void setReturn_code(String return_code) {
			this.return_code = return_code;
		}

		public String getReturn_message() {
			return return_message;
		}

		public void setReturn_message(String return_message) {
			this.return_message = return_message;
		}

	}

	@Root(name = "body")
	public static class Body implements Serializable {

		/**
		 * 
		 */
		private static final long serialVersionUID = 1084584913988532690L;
		@Element(name = "searchBindInfo")
		private SearchBindInfo searchBindInfo;
		public SearchBindInfo getSearchBindInfo() {
			return searchBindInfo;
		}
		public void setSearchBindInfo(SearchBindInfo searchBindInfo) {
			this.searchBindInfo = searchBindInfo;
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
