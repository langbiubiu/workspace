package com.ipanel.join.protocol.huawei.yn;

import java.io.Serializable;


import org.simpleframework.xml.Attribute;
import org.simpleframework.xml.Element;
import org.simpleframework.xml.Root;

@Root(name = "Message", strict = false)
public class Request_GET_PROGRAM_LIST implements Serializable{

	/**
	 * 
	 */
	private static final long serialVersionUID = -1159853388936227338L;
	@Element(name = "Header", required = false)
	private Header header;
	@Element(name = "Body", required = false)
	private Body body;
	@Attribute(required = false)
	private String version="1.0";
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



	public String getModule() {
		return module;
	}



	public void setModule(String module) {
		this.module = module;
	}



	public static class Body implements Serializable{

		/**
		 * 
		 */
		private static final long serialVersionUID = -8400374180089169605L;

		@Element(name = "TSChannel", required = false)
		private TSChannel tsChannel;

		@Element(name = "Range", required = false)
		private Range range;

		public TSChannel getTsChannel() {
			return tsChannel;
		}

		public void setTsChannel(TSChannel tsChannel) {
			this.tsChannel = tsChannel;
		}

		public Range getRange() {
			return range;
		}

		public void setRange(Range range) {
			this.range = range;
		}
		
				
	}
	
	public static class TSChannel implements Serializable{

		/**
		 * 
		 */
		private static final long serialVersionUID = -5313236390841964212L;
		@Attribute(required = false)
		private String serviceid;
		@Attribute(required = false)
		private String tsid;
		public String getServiceid() {
			return serviceid;
		}
		public void setServiceid(String serviceid) {
			this.serviceid = serviceid;
		}
		public String getTsid() {
			return tsid;
		}
		public void setTsid(String tsid) {
			this.tsid = tsid;
		}

		
	}
	
	public static class Range implements Serializable{
		/**
		 * 
		 */
		private static final long serialVersionUID = 2349172396950441449L;
		@Attribute(required = false)
		private String starttime;
		@Attribute(required = false)
		private String endtime;
		public String getStarttime() {
			return starttime;
		}
		public void setStarttime(String starttime) {
			this.starttime = starttime;
		}
		public String getEndtime() {
			return endtime;
		}
		public void setEndtime(String endtime) {
			this.endtime = endtime;
		}
		
		
	}

}
