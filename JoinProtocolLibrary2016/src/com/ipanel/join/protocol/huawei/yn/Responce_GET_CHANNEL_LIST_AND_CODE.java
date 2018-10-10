package com.ipanel.join.protocol.huawei.yn;

import java.io.Serializable;
import java.util.List;

import org.simpleframework.xml.Attribute;
import org.simpleframework.xml.Element;
import org.simpleframework.xml.ElementList;
import org.simpleframework.xml.Root;

@Root(name = "Message", strict = false)
public class Responce_GET_CHANNEL_LIST_AND_CODE implements Serializable {

	/**
	 * 
	 */
	private static final long serialVersionUID = -4349264923271470841L;

	@Element(name = "Header", required = false)
	private Header header;
	@Element(name = "Body", required = false)
	private Body body;

	@Attribute(required = false)
	private String version = "1.0";
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
	public static class Body implements Serializable {

		/**
		 * 
		 */
		private static final long serialVersionUID = -6194122587014609090L;

		@Element(name = "Code", required = false)
		private Code code;
		@Element(name = "Channels", required = false)
		private Channels channels;
		@Attribute(required = false)
		private String errcode;
		@Attribute(required = false)
		private String errstring;

		public Code getCode() {
			return code;
		}

		public void setCode(Code code) {
			this.code = code;
		}

		public Channels getChannels() {
			return channels;
		}

		public void setChannels(Channels channels) {
			this.channels = channels;
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

	public static class Channels implements Serializable {

		/**
		 * 
		 */
		private static final long serialVersionUID = -6464594446610822860L;

		@ElementList(inline = true, required = false, entry = "Channel")
		private List<Channel> channels;

		public List<Channel> getChannels() {
			return channels;
		}

		public void setChannels(List<Channel> channels) {
			this.channels = channels;
		}

	}

	public static class Channel implements Serializable {

		/**
		 * 
		 */
		private static final long serialVersionUID = 2719264223545296995L;

		@Attribute(required = false)
		private String tsid;
		@Attribute(required = false)
		private String serviceid;
		@Attribute(required = false)
		private String TStime;
		@Attribute(required = false)
		private String mode;
		@Attribute(required = false)
		private String url;
		@Attribute(required = false)
		private String desc;
		@Attribute(required = false)
		private String ispurchased;

		public String getTsid() {
			return tsid;
		}

		public void setTsid(String tsid) {
			this.tsid = tsid;
		}

		public String getServiceid() {
			return serviceid;
		}

		public void setServiceid(String serviceid) {
			this.serviceid = serviceid;
		}

		public String getTStime() {
			return TStime;
		}

		public void setTStime(String tStime) {
			TStime = tStime;
		}

		public String getMode() {
			return mode;
		}

		public void setMode(String mode) {
			this.mode = mode;
		}

		public String getUrl() {
			return url;
		}

		public void setUrl(String url) {
			this.url = url;
		}

		public String getDesc() {
			return desc;
		}

		public void setDesc(String desc) {
			this.desc = desc;
		}

		public String getIspurchased() {
			return ispurchased;
		}

		public void setIspurchased(String ispurchased) {
			this.ispurchased = ispurchased;
		}

	}

	public static class Code implements Serializable {

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
