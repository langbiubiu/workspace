package com.ipanel.join.chongqing.myapp;

import java.io.Serializable;


public class WebAppResponse implements Serializable{
	
	private static final long serialVersionUID = 1L;
	
	private int id;
	private String name;
	private WebApp[] data;
	
	public String getName() {
		return name;
	}
	public void setName(String name) {
		this.name = name;
	}
	public WebApp[] getData() {
		return data;
	}
	public void setData(WebApp[] data) {
		this.data = data;
	}

	public class WebApp implements Serializable{

		private static final long serialVersionUID = 1L;
		
		private String url;
		private String img;
		private String name;
		private String appType;
		
		public String getUrl(){
			return url;
		}
		public void setUrl(String url){
			this.url=url;
		}
		
		public String getName() {
			return name;
		}
		public void setName(String name) {
			this.name = name;
		}
		public String getImg() {
			return img;
		}
		public void setImg(String img) {
			this.img = img;
		}
		public String getAppType() {
			return appType;
		}
		public void setAppType(String appType) {
			this.appType = appType;
		}
		
	}

}
