package com.ipanel.chongqing_ipanelforhw.hwstruct;

import java.io.Serializable;
import java.util.List;

import com.google.gson.annotations.Expose;

/*
 * 欢网所有接口的请求类结构类似，设置不同参数区分，文档没有的参数不用设置
 */
public class GetHwRequest implements Serializable {

	public Developer getDeveloper() {
		return developer;
	}

	public void setDeveloper(Developer developer) {
		this.developer = developer;
	}

	@Expose
	private String action;
	
	@Expose
	private Developer developer = new Developer();
	
	@Expose
	private Device device = new Device();
	@Expose
	private User user = new User();
	@Expose
	private Param param = new Param();

	public String getAction() {
		return action;
	}

	public void setAction(String action) {
		this.action = action;
	}

	public Device getDevice() {
		return device;
	}

	public void setDevice(Device device) {
		this.device = device;
	}

	public User getUser() {
		return user;
	}

	public void setUser(User user) {
		this.user = user;
	}

	public Param getParam() {
		return param;
	}

	public void setParam(Param param) {
		this.param = param;
	}
	
	
	public class Developer implements Serializable {
		@Expose
		private String apikey;
		
		@Expose
		private String secretkey;

		public String getApikey() {
			return apikey;
		}

		public void setApikey(String apikey) {
			this.apikey = apikey;
		}

		public String getSecretkey() {
			return secretkey;
		}

		public void setSecretkey(String secretkey) {
			this.secretkey = secretkey;
		}



	}


	public class Device implements Serializable {
		@Expose
		private String dnum;

		public String getDnum() {
			return dnum;
		}

		public void setDnum(String dnum) {
			this.dnum = dnum;
		}

	}

	public class User implements Serializable {
		@Expose
		private String userid;

		public String getUserid() {
			return userid;
		}

		public void setUserid(String userid) {
			this.userid = userid;
		}

	}

	public class Param implements Serializable {

		@Expose
		private String type;

		@Expose
		private Boolean showlive;

		@Expose
		private int order;

		@Expose
		private int page;

		@Expose
		private int pagesize;

		@Expose
		private String channelId;

		@Expose
		private String wikiId;

		@Expose
		private String title;

		@Expose
		private int value;

		@Expose
		private int act;

		@Expose
		private int actorid;

		@Expose
		private int sort;

		@Expose
		private String model;

		@Expose
		private String tag;

		@Expose
		private String country;

		@Expose
		private String released;

		@Expose
		private String letter;
		@Expose
		private String id;
		@Expose
		private List<String> ids;
		@Expose
		private String date;
		
		public String getDate() {
			return date;
		}

		public void setDate(String date) {
			this.date = date;
		}

		public String getId() {
			return id;
		}

		public void setId(String id) {
			this.id = id;
		}

		public List<String> getIds() {
			return ids;
		}

		public void setIds(List<String> ids) {
			this.ids = ids;
		}

		public int getAct() {
			return act;
		}

		public void setAct(int act) {
			this.act = act;
		}

		public String getTitle() {
			return title;
		}

		public void setTitle(String title) {
			this.title = title;
		}

		public int getValue() {
			return value;
		}

		public void setValue(int value) {
			this.value = value;
		}

		public String getChannelId() {
			return channelId;
		}

		public void setChannelId(String channelId) {
			this.channelId = channelId;
		}

		public String getWikiId() {
			return wikiId;
		}

		public void setWikiId(String wikiId) {
			this.wikiId = wikiId;
		}

		public String getType() {
			return type;
		}

		public void setType(String type) {
			this.type = type;
		}

		public Boolean getShowlive() {
			return showlive;
		}

		public void setShowlive(Boolean showlive) {
			this.showlive = showlive;
		}

		public int getOrder() {
			return order;
		}

		public void setOrder(int order) {
			this.order = order;
		}

		public int getPage() {
			return page;
		}

		public void setPage(int page) {
			this.page = page;
		}

		public int getPagesize() {
			return pagesize;
		}

		public void setPagesize(int pagesize) {
			this.pagesize = pagesize;
		}

		public int getActorid() {
			return actorid;
		}

		public void setActorid(int actorid) {
			this.actorid = actorid;
		}

		public int getSort() {
			return sort;
		}

		public void setSort(int sort) {
			this.sort = sort;
		}

		public String getModel() {
			return model;
		}

		public void setModel(String model) {
			this.model = model;
		}

		public String getTag() {
			return tag;
		}

		public void setTag(String tag) {
			this.tag = tag;
		}

		public String getCountry() {
			return country;
		}

		public void setCountry(String country) {
			this.country = country;
		}

		public String getReleased() {
			return released;
		}

		public void setReleased(String released) {
			this.released = released;
		}

		public String getLetter() {
			return letter;
		}

		public void setLetter(String letter) {
			this.letter = letter;
		}
		
		
		

	}

}
