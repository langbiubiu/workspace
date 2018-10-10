package com.ipanel.join.chongqing.live.manager;

import ipaneltv.toolkit.entitlement.EntitlementDatabaseObjects.Entitlement;

import java.util.List;

import com.ipanel.join.chongqing.live.data.OperatorData;


/**
 * CA相关管理类
 * */
public abstract class CAAuthManager{

	//CA信息标志
	public final static int CA_INFO_FOR_CARDID= 0;
	public final static int CA_INFO_FOR_CA_VERSION= 1;
	public final static int CA_INFO_FOR_MODULE_VERSION= 2;
	public final static int CA_INFO_FOR_CAS= 3;
	public final static int CA_INFO_FOR_WATCH_LEVEL= 4;
	public final static int CA_INFO_FOR_SERVICE_TIME= 5;
	public final static int CA_INFO_FOR_PIN_STATE= 6;
	public final static int CA_INFO_FOR_MATCH_STATE= 7;
	/**
	 * 获取指定的CA信息
	 * */
	public abstract String getCAInfoByKey(int key);
	/**
	 * 获取所有的运营商信息
	 * */
	public abstract List<OperatorData> getOperatorDatas() ;
	/**
	 * 获取指定运营商的授权信息
	 * */
	public abstract List<Entitlement> getAuthorDatas(String id) ;
	/**
	 * 更新CA模块数据
	 * */
	public abstract void updateCaModule(int moduleId);
	/**
	 * 获得CA模块数据
	 * */
	public abstract int getCAModuleId();
	/**
	 * CA卡是否有效
	 * */
	public abstract boolean isCAValid();
	
	public abstract void chooseSession();
	
	public static interface CallBack{
		/**
		 * OSD消息回调
		 * */
		public void onScrollMessage(String msg);
		/**
		 * 邮件信息回调
		 * */
		public void onMailChanged(boolean empty);
		/**
		 * CA信息更改回调
		 * */
		public void onCAInfoChnaged();
		
	}
}
