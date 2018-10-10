package com.ipanel.join.protocol.sihua.cqvod;

import java.io.Serializable;
import java.util.List;

import org.simpleframework.xml.Attribute;
import org.simpleframework.xml.Element;
import org.simpleframework.xml.ElementList;
import org.simpleframework.xml.Root;

@Root(name="message")
public class UserBindResponse implements Serializable {

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
	private bindBody bindBody;
	
	@Root(name="bindInfo")
	public static class BindUser implements Serializable{

		/**
		 * 
		 */
		private static final long serialVersionUID = -6265978695981709554L;
		@Attribute(required=false)
		private String userId;
		public String getUserId() {
			return userId;
		}
		public void setUserId(String userId) {
			this.userId = userId;
		}
		@Override
		public String toString() {
			return "BindUser [userId=" + userId + "]";
		}
		
		
	}
	@Root(name="searchBindInfo")
	public static class BindUserList implements Serializable{

		/**
		 * 
		 */
		private static final long serialVersionUID = 670934834291013457L;
		
		@ElementList(name="bindInfo",inline=true,required=false)
		private List<BindUser> listBindUsers;

		public List<BindUser> getListBindUsers() {
			return listBindUsers;
		}

		public void setListBindUsers(List<BindUser> listBindUsers) {
			this.listBindUsers = listBindUsers;
		}

		@Override
		public String toString() {
			return "BindUserList [listBindUsers=" + listBindUsers + "]";
		}
		
		
	}
	@Root(name="bindUserQuery")
	public  static class BindUserQuery implements Serializable{

		/**
		 * 
		 */
		private static final long serialVersionUID = 6312601474614650357L;
		@Element(name="bindUserList")
		private BindUserList bindUserList;
		@Attribute
		private String result;
		@Attribute
		private String resultDesc;
		
		
		public BindUserList getBindUserList() {
			return bindUserList;
		}
		public void setBindUserList(BindUserList bindUserList) {
			this.bindUserList = bindUserList;
		}
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
			return "BindUserQuery [bindUserList=" + bindUserList + ", result=" + result + ", resultDesc=" + resultDesc
					+ "]";
		}
		
		

	} 
	@Root(name="body")
	public static class bindBody implements Serializable{

		/**
		 * 
		 */
		private static final long serialVersionUID = 1084584913988532690L;
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
			return "bindBody [bindUserQuery=" + bindUserQuery + "]";
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
	public bindBody getBindBody() {
		return bindBody;
	}
	public void setBindBody(bindBody bindBody) {
		this.bindBody = bindBody;
	}
	@Override
	public String toString() {
		return "UserBindResponse [module=" + module + ", version=" + version + ", header=" + header + ", bindBody="
				+ bindBody + "]";
	}
	
	

}
