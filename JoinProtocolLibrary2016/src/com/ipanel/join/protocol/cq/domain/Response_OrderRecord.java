package com.ipanel.join.protocol.cq.domain;

import java.io.Serializable;

import org.simpleframework.xml.Attribute;
import org.simpleframework.xml.Element;
import org.simpleframework.xml.Root;

@Root(name = "message")
public class Response_OrderRecord implements Serializable {

	/**
	 * 
	 */
	private static final long serialVersionUID = 2770079873441304724L;
	@Element(required = true)
	private Header header;
	@Element(required = true)
	private Body body;
	@Attribute(required = false)
	private String version="1.0";

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
		private static final long serialVersionUID = 2871354045699077639L;
		@Element(required = true)
		private Result result;
		
		public Result getResult() {
			return result;
		}
		public void setResult(Result result) {
			this.result = result;
		}
		
		
	}

	@Root(name = "result")
	public static class Result implements Serializable {

		/**
		 * 
		 */
		private static final long serialVersionUID = -6468415003731196147L;
		@Attribute(required = false)
		private String Code;
		@Attribute(required = false)
		private String Description;
		public String getCode() {
			return Code;
		}
		public void setCode(String code) {
			Code = code;
		}
		public String getDescription() {
			return Description;
		}
		public void setDescription(String description) {
			Description = description;
		}
		
		
	}
}
