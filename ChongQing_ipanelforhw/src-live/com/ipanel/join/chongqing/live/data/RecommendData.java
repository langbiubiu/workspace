package com.ipanel.join.chongqing.live.data;

public class RecommendData {
	private int type;
	private String name;
	private String layoutCode;
	private String folderId;
	private String contentId;
	private String contentType;
	private String showType="1";
	private String action;
	private Object extra;

	public int getType() {
		return type;
	}

	public void setType(int type) {
		this.type = type;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getLayoutCode() {
		return layoutCode;
	}

	public void setLayoutCode(String layoutCode) {
		this.layoutCode = layoutCode;
	}

	public String getFolderId() {
		return folderId;
	}

	public void setFolderId(String folderId) {
		this.folderId = folderId;
	}

	public String getContentId() {
		return contentId;
	}

	public void setContentId(String contentId) {
		this.contentId = contentId;
	}

	public String getContentType() {
		return contentType;
	}

	public void setContentType(String contentType) {
		this.contentType = contentType;
	}

	public Object getExtra() {
		return extra;
	}

	public void setExtra(Object extra) {
		this.extra = extra;
	}

	public String getShowType() {
		return showType;
	}

	public void setShowType(String showType) {
		this.showType = showType;
	}

	public String getAction() {
		return action;
	}

	public void setAction(String action) {
		this.action = action;
	}

	public RecommendData(int type, String name, String url, Object obj) {
		super();
		this.type = type;
		this.name = name;
		this.extra = obj;
	}
	
	public RecommendData(){
		
	}

}
