package com.ipanel.join.protocol.huawei.yn;

import java.io.Serializable;

import org.simpleframework.xml.Attribute;
import org.simpleframework.xml.Element;
import org.simpleframework.xml.Root;

@Root(name = "Message", strict = false)
public class Request_GET_CHANNEL_LIST_AND_CODE implements Serializable {

	/**
	 * 
	 */
	private static final long serialVersionUID = -1159853388936227338L;

	@Element(name = "Header", required = false)
	private Header header;
	@Element(name = "Body", required = false)
	private Body body;
	@Attribute(required = false)
	private String version = "1.0";
	@Attribute(required = false)
	private String module = "TSG";

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

	public String getVersion() {
		return version;
	}

	public void setVersion(String version) {
		this.version = version;
	}

	public static class Body implements Serializable {

		/**
		 * 
		 */
		private static final long serialVersionUID = -8400374180089169605L;

		@Element(name = "STB", required = false)
		private STB STB;

		public STB getSTB() {
			return STB;
		}

		public void setSTB(STB sTB) {
			STB = sTB;
		}

	}
	
	public void setBody(String stbId){
		Body body = new Body();
		STB stb = new STB();
		stb.setId(stbId);
		body.setSTB(stb);
		setBody(body);;
	}

	public static class STB implements Serializable {

		/**
		 * 
		 */
		private static final long serialVersionUID = -5313236390841964212L;
		@Attribute(required = false)
		private String id;

		public String getId() {
			return id;
		}

		public void setId(String id) {
			this.id = id;
		}

	}

}
