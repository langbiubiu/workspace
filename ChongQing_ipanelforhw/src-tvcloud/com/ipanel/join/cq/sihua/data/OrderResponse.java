package com.ipanel.join.cq.sihua.data;

import java.io.Serializable;

import org.simpleframework.xml.Attribute;
import org.simpleframework.xml.Element;
import org.simpleframework.xml.Root;

import com.ipanel.join.protocol.sihua.cqvod.space.SpaceHeader;


@Root(name="result",strict=false)
public class OrderResponse implements Serializable{
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	@Attribute(required=false)
	private String version;
	@Element(required=false)
	private SpaceHeader header;
	@Element(required=false)
	private OrderResponseBody body;
	
	
	public String getVersion() {
		return version;
	}

	public void setVersion(String version) {
		this.version = version;
	}

	public SpaceHeader getHeader() {
		return header;
	}

	public void setHeader(SpaceHeader header) {
		this.header = header;
	}

	public OrderResponseBody getBody() {
		return body;
	}

	public void setBody(OrderResponseBody body) {
		this.body = body;
	}

	@Root(name="body",strict=false)
	public static class OrderResponseBody implements Serializable{
		/**
		 * 
		 */
		private static final long serialVersionUID = 1L;
		@Element
		private OrderResponseResult result;

		public OrderResponseResult getResult() {
			return result;
		}

		public void setResult(OrderResponseResult result) {
			this.result = result;
		}

	}
	
	@Root(name="result",strict=false)
	public static class OrderResponseResult implements Serializable{
		/**
		 * 
		 */
		private static final long serialVersionUID = 1L;
		@Attribute(required=false)
		private String Code;
		@Attribute(required=false)
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
