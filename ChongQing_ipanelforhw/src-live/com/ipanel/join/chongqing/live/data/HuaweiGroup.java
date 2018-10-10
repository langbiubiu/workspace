package com.ipanel.join.chongqing.live.data;

import java.io.Serializable;
import java.util.List;

public class HuaweiGroup implements Serializable{

	/**
	 * 
	 */
	private static final long serialVersionUID = -176880420340924167L;
	public String code;
	public String cataName;
	public List<HuaweiChannel> chanelList;
	
	public class HuaweiChannel implements Serializable{
		
		/**
		 * 
		 */
		private static final long serialVersionUID = -2515594746487714793L;
		public int channelID;
		public int service;
		public int frequency;
		public String channelName;
		public int isRec;
	}
}
