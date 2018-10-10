package com.ipanel.join.chongqing.live.base;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

public interface IUIControl {
	
	/**
	 * 界面创建的回调
	 * */
	public View onCreateView(LayoutInflater inflater, ViewGroup root);
	/**
	 * 界面显示的回调
	 * */
	public void onShow();
	/**
	 * 界面刷新的回调
	 * */
	public void onRefresh();
	/**
	 * 界面隐藏的回调
	 * */
	public void onHide();
	/**
	 * 数据变化时的回调
	 * */
	public void onDataChange(int type,Object data);


}
