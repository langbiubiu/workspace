package com.ipanel.join.protocol.sihua.cqvod;

import java.io.Serializable;

import org.simpleframework.xml.Attribute;
import org.simpleframework.xml.Element;
import org.simpleframework.xml.Root;


@Root(name = "message")
public class DoubanResponse implements Serializable {

	/**
	 * 
	 */
	private static final long serialVersionUID = -4552206079769623619L;
	@Attribute(required=false)
	private String module;
	@Attribute(required=false)
	private String version;
	@Element(required=false)
	private Header header;
	@Element(required=false)
	private Body body;
	

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

	public Body getBody() {
		return body;
	}

	public void setBody(Body body) {
		this.body = body;
	}

	@Root(name = "body")
	public static class Body implements Serializable {

		/**
		 * 
		 */
		private static final long serialVersionUID = -5466094989113280160L;
		@Element(required=false)
		private doubanRating doubanRating;
		public doubanRating getDoubanRating() {
			return doubanRating;
		}
		public void setDoubanRating(doubanRating doubanRating) {
			this.doubanRating = doubanRating;
		}
		

	}

	@Root(name = "DoubanBind")
	public static class doubanRating implements Serializable {

		/**
		 * 
		 */
		private static final long serialVersionUID = -6135595491876083979L;
		@Attribute(required = false)
		private String return_code;
	
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

		public String getMin() {
			return min;
		}

		public void setMin(String min) {
			this.min = min;
		}

		public String getMax() {
			return max;
		}

		public void setMax(String max) {
			this.max = max;
		}

		public String getStars() {
			return stars;
		}

		public void setStars(String stars) {
			this.stars = stars;
		}

		public String getCollect_count() {
			return collect_count;
		}

		public void setCollect_count(String collect_count) {
			this.collect_count = collect_count;
		}

		public String getAverage() {
			return average;
		}

		public void setAverage(String average) {
			this.average = average;
		}

		@Attribute(required = false)
		private String return_message;
		@Attribute(required = false)
		private String min;
		@Attribute(required = false)
		private String max;
		
		@Attribute(required = false)
		private String stars;
		
		@Attribute(required = false)
		private String collect_count;
		
		@Attribute(required = false)
		private String average;
	
		@Override
		public String toString() {
			return "doubanRating [return_message=" + return_message + ", min=" + min + ", max=" + max
					+ ", stars=" + stars +"average"+average+"collect_count"+collect_count+"]";
		}
		

	}
		

}
