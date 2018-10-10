package com.ipanel.join.cq.vod.jsondata;

import java.io.Serializable;
import java.util.List;

import com.google.gson.annotations.Expose;

/**
 * 专题对应打开3.0url
 * @author Administrator
 *
 */
public class SpecialResponse  implements Serializable{
	@Expose
	private String code;
	@Expose
	private List<Special> data;
	
	public String getCode() {
		return code;
	}
	
	public void setCode(String code) {
		this.code = code;
	}

	public List<Special> getData() {
		return data;
	}

	public void setData(List<Special> data) {
		this.data = data;
	}
	
	@Override
	public String toString() {
		return "SpecialResponse [code=" + code + ", data=" + data + "]";
	}


	public class Special implements Serializable{
		@Expose
		private String id;
		@Expose
		private String name;
		@Expose
		private String url;
		public String getId() {
			return id;
		}
		public void setId(String id) {
			this.id = id;
		}
		public String getName() {
			return name;
		}
		public void setName(String name) {
			this.name = name;
		}
		public String getUrl() {
//			return GlobalFilmData.getInstance().getEpgUrl()+url;
			return url;
		}
		public void setUrl(String url) {
			this.url = url;
		}
		
		@Override
		public String toString() {
			return "Special [id=" + id + ", name=" + name + ", url=" + url
					+ "]";
		}
		
	}
}
