package com.ipanel.join.protocol.sihua.cqvod;

import java.io.Serializable;

import org.simpleframework.xml.Attribute;
import org.simpleframework.xml.Element;
import org.simpleframework.xml.Root;

@Root(name = "message")
public class Response_DeleteBusiInfo implements Serializable {

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


	@Root(name = "deleteBusiInfo")
	public static class DeleteBusiInfo implements Serializable {

		/**
		 * 
		 */
		private static final long serialVersionUID = 6312601474614650357L;

		@Attribute
		private String return_code;
		@Attribute
		private String return_message;

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
