package com.ipanel.join.protocol.huawei.yn;

import java.io.Serializable;

import java.util.List;

import org.simpleframework.xml.Attribute;
import org.simpleframework.xml.Element;
import org.simpleframework.xml.ElementList;
import org.simpleframework.xml.Root;
@Root(name = "Message", strict = false)
public class Responce_GET_PROGRAM_LIST implements Serializable{

	/**
	 * 
	 */
	private static final long serialVersionUID = -4349264923271470841L;
	
	@Element(name = "Header", required = false)
	private Header header;
	@Element(name = "Body", required = false)
	private Body body;
	@Attribute(required = false)
	private String version="1.0";
	@Attribute(required = false)
	private String module;
	
	public String getModule() {
		return module;
	}
	public void setModule(String module) {
		this.module = module;
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
	public String getVersion() {
		return version;
	}
	public void setVersion(String version) {
		this.version = version;
	}
	
	@Root(name = "Body")
	public static class Body implements Serializable{

		/**
		 * 
		 */
		private static final long serialVersionUID = -6194122587014609090L;
		
		@Element(name = "TSChannel", required = false)
		private TSChannel tsChannel;
		@Element(name = "Programs", required = false)
		private Programs programss;
		@Attribute(required = false)
		private String errcode;
		@Attribute(required = false)
		private String errstring;

		public TSChannel getTsChannel() {
			return tsChannel;
		}
		public void setTsChannel(TSChannel tsChannel) {
			this.tsChannel = tsChannel;
		}
		public Programs getProgramss() {
			return programss;
		}
		public void setProgramss(Programs programss) {
			this.programss = programss;
		}
		public String getErrcode() {
			return errcode;
		}
		public void setErrcode(String errcode) {
			this.errcode = errcode;
		}
		public String getErrstring() {
			return errstring;
		}
		public void setErrstring(String errstring) {
			this.errstring = errstring;
		}
		
		
		
	}
	public static class Programs implements Serializable{

		/**
		 * 
		 */
		private static final long serialVersionUID = -6464594446610822860L;
		
		@ElementList(inline = true,required = false, entry = "Program")
		private List<Program> programs;

		public List<Program> getPrograms() {
			return programs;
		}

		public void setPrograms(List<Program> programs) {
			this.programs = programs;
		}

				
		
	}
	
	public static class Program implements Serializable{

		/**
		 * 
		 */
		private static final long serialVersionUID = 2719264223545296995L;
		
		@Attribute(required = false)
		private String programid;
		@Attribute(required = false)
		private String starttime;
		@Attribute(required = false)
		private String duration;
		@Attribute(required = false)
		private String desc;
		public String getProgramid() {
			return programid;
		}
		public void setProgramid(String programid) {
			this.programid = programid;
		}
		public String getStarttime() {
			return starttime;
		}
		public void setStarttime(String starttime) {
			this.starttime = starttime;
		}
		public String getDuration() {
			return duration;
		}
		public void setDuration(String duration) {
			this.duration = duration;
		}
		public String getDesc() {
			return desc;
		}
		public void setDesc(String desc) {
			this.desc = desc;
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

}
