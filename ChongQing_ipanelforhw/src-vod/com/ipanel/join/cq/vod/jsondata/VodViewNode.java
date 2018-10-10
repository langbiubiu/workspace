package com.ipanel.join.cq.vod.jsondata;

import java.util.ArrayList;

public class VodViewNode {
	String version="";
	String viewID="";
    String viewX="";
    String viewY="";
    String viewLength="";
    String viewWidth="";
    String viewType="";
    String viewText="";
    String isFocusable="";
    String isClickalbe="";
    String imagePath="";
    
    ArrayList<VodViewNode> viewChildNodes=new ArrayList<VodViewNode>();
    
    public ArrayList<VodViewNode> getViewChildNodes() {
		return viewChildNodes;
	}
	public void setViewChildNodes(ArrayList<VodViewNode> viewChildNodes) {
		this.viewChildNodes = viewChildNodes;
	}
	public String getVersion() {
		return version;
	}
	public void setVersion(String version) {
		this.version = version;
	}
	public ArrayList<VodViewNode> getChildView() {
		return childView;
	}
	public void setChildView(ArrayList<VodViewNode> childView) {
		this.childView = childView;
	}
	ArrayList<VodViewNode> childView=new ArrayList<VodViewNode>();
    
	public String getViewID() {
		return viewID;
	}
	public void setViewID(String viewID) {
		this.viewID = viewID;
	}
	public String getViewX() {
		return viewX;
	}
	public void setViewX(String viewX) {
		this.viewX = viewX;
	}
	public String getViewY() {
		return viewY;
	}
	public void setViewY(String viewY) {
		this.viewY = viewY;
	}
	public String getViewLength() {
		return viewLength;
	}
	public void setViewLength(String viewLength) {
		this.viewLength = viewLength;
	}
	public String getViewWidth() {
		return viewWidth;
	}
	public void setViewWidth(String viewWidth) {
		this.viewWidth = viewWidth;
	}
	public String getViewType() {
		return viewType;
	}
	public void setViewType(String viewType) {
		this.viewType = viewType;
	}
	public String getViewText() {
		return viewText;
	}
	public void setViewText(String viewText) {
		this.viewText = viewText;
	}
	public String getIsFocusable() {
		return isFocusable;
	}
	public void setIsFocusable(String isFocusable) {
		this.isFocusable = isFocusable;
	}
	public String getIsClickalbe() {
		return isClickalbe;
	}
	public void setIsClickalbe(String isClickalbe) {
		this.isClickalbe = isClickalbe;
	}
	public String getImagePath() {
		return imagePath;
	}
	public void setImagePath(String imagePath) {
		this.imagePath = imagePath;
	}

}
