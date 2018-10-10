package com.ipanel.join.protocol.sihua.cqvod;

import java.io.Serializable;

import org.simpleframework.xml.Attribute;
import org.simpleframework.xml.Element;
import org.simpleframework.xml.Root;
@Root(name="message")
public class GetUserUnBindRequest implements Serializable {

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
	private UnBindUserBody  bindUserBody;
	@Root(name="bindUnitUser")
	public static class BindUnitUser   implements Serializable{

		/**
		 * 
		 */
		private static final long serialVersionUID = 1211601865316794664L;
		@Attribute(required=false)
		private String operation;
		@Attribute(required=false)
		private String bindAccount;
		@Attribute(required=false)
		private String srcUserId;
		@Attribute(required=false)
		private String srcSystemId;
		public String getOperation() {
			return operation;
		}
		public void setOperation(String operation) {
			this.operation = operation;
		}
		public String getBindAccount() {
			return bindAccount;
		}
		public void setBindAccount(String bindAccount) {
			this.bindAccount = bindAccount;
		}
		public String getSrcUserId() {
			return srcUserId;
		}
		public void setSrcUserId(String srcUserId) {
			this.srcUserId = srcUserId;
		}
		public String getSrcSystemId() {
			return srcSystemId;
		}
		public void setSrcSystemId(String srcSystemId) {
			this.srcSystemId = srcSystemId;
		}

	}
	@Root(name="body")
	public static class UnBindUserBody implements Serializable{

		/**
		 * 
		 */
		private static final long serialVersionUID = 5694295571786834660L;
		@Element(name="bindUnitUser")
		private BindUnitUser  bindUserQuery;
		public BindUnitUser getBindUserQuery() {
			return bindUserQuery;
		}
		public void setBindUserQuery(BindUnitUser bindUserQuery) {
			this.bindUserQuery = bindUserQuery;
		}
		@Override
		public String toString() {
			return "UnBindUserBody [bindUserQuery=" + bindUserQuery + "]";
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
	public UnBindUserBody getBindUserBody() {
		return bindUserBody;
	}
	public void setBindUserBody(UnBindUserBody bindUserBody) {
		this.bindUserBody = bindUserBody;
	}
	
	

}
