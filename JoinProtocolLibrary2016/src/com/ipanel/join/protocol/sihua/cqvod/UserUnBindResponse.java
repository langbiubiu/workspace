package com.ipanel.join.protocol.sihua.cqvod;

import java.io.Serializable;

import org.simpleframework.xml.Attribute;
import org.simpleframework.xml.Element;
import org.simpleframework.xml.Root;
@Root(name="message")
public class UserUnBindResponse implements Serializable {

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
	
	
	
	@Root(name="bindUnitUser")
	public  static class UnBindUnitUser implements Serializable{

		/**
		 * 
		 */
		private static final long serialVersionUID = 6312601474614650357L;
		@Attribute(required=false)
		private String result;
		@Attribute(required=false)
		private String resultDesc;
		public String getResult() {
			return result;
		}
		public void setResult(String result) {
			this.result = result;
		}
		public String getResultDesc() {
			return resultDesc;
		}
		public void setResultDesc(String resultDesc) {
			this.resultDesc = resultDesc;
		}
		@Override
		public String toString() {
			return "BindUnitUser [result=" + result + ", resultDesc=" + resultDesc + "]";
		}
		
		

	} 
	@Root(name="body")
	public static class UnbindBody implements Serializable{

		/**
		 * 
		 */
		private static final long serialVersionUID = 1084584913988532690L;
		@Element(name="bindUnitUser")
		private UnBindUnitUser bindUnitUser;
		public UnBindUnitUser getBindUnitUser() {
			return bindUnitUser;
		}
		public void setBindUnitUser(UnBindUnitUser bindUnitUser) {
			this.bindUnitUser = bindUnitUser;
		}
		@Override
		public String toString() {
			return "UnbindBody [bindUnitUser=" + bindUnitUser + "]";
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
