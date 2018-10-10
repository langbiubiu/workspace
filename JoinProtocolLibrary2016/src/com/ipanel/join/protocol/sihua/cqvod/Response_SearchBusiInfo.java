package com.ipanel.join.protocol.sihua.cqvod;

import java.io.Serializable;
import java.util.List;

import org.simpleframework.xml.Attribute;
import org.simpleframework.xml.Element;
import org.simpleframework.xml.ElementList;
import org.simpleframework.xml.Root;

@Root(name = "message")
public class Response_SearchBusiInfo implements Serializable {

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

	@Root(name = "busiInfo")
	public static class BusiInfo implements Serializable {

		/**
		 * 
		 */
		private static final long serialVersionUID = -6265978695981709554L;
		@Attribute(required = false)
		private String info_id;
		@Attribute(required = false)
		private String info_type;
		@Attribute(required = false)
		private String info_content;
		@Attribute(required = false)
		private String save_date;

		public String getInfo_id() {
			return info_id;
		}

		public void setInfo_id(String info_id) {
			this.info_id = info_id;
		}

		public String getInfo_type() {
			return info_type;
		}

		public void setInfo_type(String info_type) {
			this.info_type = info_type;
		}

		public String getInfo_content() {
			return info_content;
		}

		public void setInfo_content(String info_content) {
			this.info_content = info_content;
		}

		public String getSave_date() {
			return save_date;
		}

		public void setSave_date(String save_date) {
			this.save_date = save_date;
		}

	}

	@Root(name = "searchBusiInfo")
	public static class SearchBusiInfo implements Serializable {

		/**
		 * 
		 */
		private static final long serialVersionUID = 6312601474614650357L;

		@ElementList(name = "busiInfo", inline = true, required = false)
		private List<BusiInfo> busiInfos;
		@Attribute
		private String return_code;
		@Attribute
		private String return_message;

		public List<BusiInfo> getBusiInfos() {
			return busiInfos;
		}

		public void setBusiInfos(List<BusiInfo> busiInfos) {
			this.busiInfos = busiInfos;
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
		@Element(name = "searchBusiInfo")
		private SearchBusiInfo searchBusiInfo;

		public SearchBusiInfo getSearchBusiInfo() {
			return searchBusiInfo;
		}

		public void setSearchBusiInfo(SearchBusiInfo searchBusiInfo) {
			this.searchBusiInfo = searchBusiInfo;
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
