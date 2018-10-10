package com.ipanel.join.protocol.sihua.cqvod;

import java.io.Serializable;

import org.simpleframework.xml.Attribute;
import org.simpleframework.xml.Element;
import org.simpleframework.xml.Root;
@Root(name="message")
public class GetUserBind implements Serializable {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1841602555140832713L;
	@Attribute(required=false)
	private String module;
	@Attribute(required=false)
	private String version;
	@Element(required=false)
	private Header header;
	@Element(name="body")
	private BindUserBody  bindUserBody;
	@Root(name="bindUserQuery")
	public static class BindUserQuery  implements Serializable{

		/**
		 * 
		 */
		private static final long serialVersionUID = 1211601865316794664L;
		@Attribute(required=false)
		private String ICcard;
		@Attribute(required=false)
		private String srcSystemId;
		public String getICcard() {
			return ICcard;
		}
		public void setICcard(String iCcard) {
			ICcard = iCcard;
		}
		public String getSrcSystemId() {
			return srcSystemId;
		}
		public void setSrcSystemId(String srcSystemId) {
			this.srcSystemId = srcSystemId;
		}
		@Override
		public String toString() {
			return "BindUserQuery [ICcard=" + ICcard + ", srcSystemId=" + srcSystemId + "]";
		}
		
	}
	@Root(name="body")
	public static class BindUserBody implements Serializable{

		/**
		 * 
		 */
		private static final long serialVersionUID = 5694295571786834660L;
		@Element(name="bindUserQuery")
		private BindUserQuery bindUserQuery;
		public BindUserQuery getBindUserQuery() {
			return bindUserQuery;
		}
		public void setBindUserQuery(BindUserQuery bindUserQuery) {
			this.bindUserQuery = bindUserQuery;
		}
		@Override
		public String toString() {
			return "BindUserBody [bindUserQuery=" + bindUserQuery + "]";
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
	public BindUserBody getBindUserBody() {
		return bindUserBody;
	}
	public void setBindUserBody(BindUserBody bindUserBody) {
		this.bindUserBody = bindUserBody;
	}
	
	

}
