package com.ipanel.join.protocol.sihua.cqvod;

import java.io.Serializable;

import org.simpleframework.xml.Attribute;
import org.simpleframework.xml.Element;
import org.simpleframework.xml.Root;

@Root(name = "message")
public class DoubanPointGoodBadResponse implements Serializable {

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
		private scspRate scspRate;
		public scspRate getScspRate() {
			return scspRate;
		}
		public void setScspRate(scspRate scspRate) {
			this.scspRate = scspRate;
		}
		

	}

	@Root(name = "DoubanBind")
	public static class scspRate implements Serializable {

		/**
		 * 
		 */
		private static final long serialVersionUID = -6135595491876083979L;
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



		public String getLikeNum() {
			return likeNum;
		}



		public void setLikeNum(String likeNum) {
			this.likeNum = likeNum;
		}



		public String getDislikeNum() {
			return dislikeNum;
		}



		public void setDislikeNum(String dislikeNum) {
			this.dislikeNum = dislikeNum;
		}



		public String getIsLike() {
			return isLike;
		}



		public void setIsLike(String isLike) {
			this.isLike = isLike;
		}



		public String getIsDislike() {
			return isDislike;
		}



		public void setIsDislike(String isDislike) {
			this.isDislike = isDislike;
		}



		@Attribute(required = false)
		private String return_code;
	
		@Attribute(required = false)
		private String return_message;
		@Attribute(required = false)
		private String likeNum;
	
		@Attribute(required = false)
		private String dislikeNum;
		
		@Attribute(required = false)
		private String isLike;
		
		@Attribute(required = false)
		private String isDislike;
		
		
	
		@Override
		public String toString() {
			return "scspRate [return_code=" + return_code + "return_message=" + return_message
					+ ", likeNum=" + likeNum + ", dislikeNum=" + dislikeNum + ", isLike=" + isLike+", isDislike=" + isDislike+"]";
		}
		

	}

}
