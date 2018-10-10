package com.ipanel.join.chongqing.myapp;

import android.graphics.drawable.Drawable;

public class App {
	private Drawable icon;
	private String label;
	private String packageName;
	private int drawableId;
	private String url;
	private long InstallTime;
	private String iconUrl;
	private int isSystem;
	
	public void setIsSystem(int isSystem){
		this.isSystem=isSystem;
	}
	public int getIsSystem(){
		return isSystem;
	}
	public void setInstallTime(long InstallTime){
		this.InstallTime=InstallTime;
	}
	public long getInstallTime(){
		return InstallTime;
	}
	public String getURL(){
		return url;
	}
	public void setURL(String url){
		this.url=url;
	}
	public int getDrawableId() {
		return drawableId;
	}

	public void setDrawableId(int drawableId) {
		this.drawableId = drawableId;
	}

	public Drawable getIcon() {
		return icon;
	}

	public void setIcon(Drawable icon) {
		this.icon = icon;
	}
	public String getIconUrl(){
		return iconUrl;
	}
	public void setIconUrl(String iconUrl){
		this.iconUrl=iconUrl;
	}

	public String getLabel() {
		return label;
	}

	public void setLabel(String label) {
		this.label = label;
	}

	public String getPackageName() {
		return packageName;
	}

	public void setPackageName(String packageName) {
		this.packageName = packageName;
	}

}
