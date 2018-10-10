package com.ipanel.join.chongqing.live.base;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

public interface IUIControl {
	
	/**
	 * ���洴���Ļص�
	 * */
	public View onCreateView(LayoutInflater inflater, ViewGroup root);
	/**
	 * ������ʾ�Ļص�
	 * */
	public void onShow();
	/**
	 * ����ˢ�µĻص�
	 * */
	public void onRefresh();
	/**
	 * �������صĻص�
	 * */
	public void onHide();
	/**
	 * ���ݱ仯ʱ�Ļص�
	 * */
	public void onDataChange(int type,Object data);


}
