package com.ipanel.join.protocol.sihua.cqvod.space;

import java.io.Serializable;
import java.util.List;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;


public class MailList implements Serializable{

	/**
	 * 
	 */
	private static final long serialVersionUID = 9194706131931480224L;
	@Expose
	@SerializedName("number")
	private List<String> numList;
	@Expose
	@SerializedName("array")
	private List<MailData> mailList;
	
	public List<String> getNumList() {
		return numList;
	}

	public void setNumList(List<String> numList) {
		this.numList = numList;
	}

	public List<MailData> getMailList() {
		return mailList;
	}

	public void setMailList(List<MailData> mailList) {
		this.mailList = mailList;
	}

	public class MailData implements Serializable {
		/**
		 * 
		 */
		private static final long serialVersionUID = 9124579254266825466L;
		@Expose
		private String mState;
		@Expose
		private String title;
		@Expose
		private String time;
		@Expose
		private String id;
		public String getmState() {
			return mState;
		}
		public void setmState(String mState) {
			this.mState = mState;
		}
		public String getTitle() {
			return title;
		}
		public void setTitle(String title) {
			this.title = title;
		}
		public String getTime() {
			return time;
		}
		public void setTime(String time) {
			this.time = time;
		}
		public String getId() {
			return id;
		}
		public void setId(String id) {
			this.id = id;
		}
			
	}
	
}
