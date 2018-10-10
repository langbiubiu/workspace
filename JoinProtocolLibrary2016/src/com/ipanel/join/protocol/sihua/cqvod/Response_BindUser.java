package com.ipanel.join.protocol.sihua.cqvod;

import java.io.Serializable;

import org.simpleframework.xml.Attribute;
import org.simpleframework.xml.Element;
import org.simpleframework.xml.Root;
@Root(name="message")
public class Response_BindUser implements Serializable {

	/**
	 * 
	 */
	private static final long serialVersionUID = -606972786211305655L;
	@Attribute(required=false)
	private String module;
	@Attribute(required=false)
	private String version;
	@Element(required=false)
	private Header header;
	@Element(name="body")
	private UnbindBody bindBody;
	
	
	
	@Root(name="bindUser")
	public  static class UnBindUnitUser implements Serializable{

		/**
		 * 
		 */
		private static final long serialVersionUID = 6312601474614650357L;
		@Attribute(required=false)
		private String return_code;
		@Attribute(required=false)
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
	@Root(name="body")
	public static class UnbindBody implements Serializable{

		/**
		 * 
		 */
		private static final long serialVersionUID = 1084584913988532690L;
		@Element(name="bindUser")
		private UnBindUnitUser bindUser;
		
		
		public UnBindUnitUser getBindUser() {
			return bindUser;
		}


		public void setBindUser(UnBindUnitUser bindUser) {
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
	public UnbindBody getBindBody() {
		return bindBody;
	}
	public void setBindBody(UnbindBody bindBody) {
		this.bindBody = bindBody;
	}
	@Override
	public String toString() {
		return "UserUnBindResponse [module=" + module + ", version=" + version + ", header=" + header + ", bindBody="
				+ bindBody + "]";
	}
	
	
	

}
