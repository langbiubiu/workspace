package com.ipanel.join.chongqing.live.manager;

import ipaneltv.toolkit.entitlement.EntitlementDatabaseObjects.Entitlement;

import java.util.List;

import com.ipanel.join.chongqing.live.data.OperatorData;


/**
 * CA��ع�����
 * */
public abstract class CAAuthManager{

	//CA��Ϣ��־
	public final static int CA_INFO_FOR_CARDID= 0;
	public final static int CA_INFO_FOR_CA_VERSION= 1;
	public final static int CA_INFO_FOR_MODULE_VERSION= 2;
	public final static int CA_INFO_FOR_CAS= 3;
	public final static int CA_INFO_FOR_WATCH_LEVEL= 4;
	public final static int CA_INFO_FOR_SERVICE_TIME= 5;
	public final static int CA_INFO_FOR_PIN_STATE= 6;
	public final static int CA_INFO_FOR_MATCH_STATE= 7;
	/**
	 * ��ȡָ����CA��Ϣ
	 * */
	public abstract String getCAInfoByKey(int key);
	/**
	 * ��ȡ���е���Ӫ����Ϣ
	 * */
	public abstract List<OperatorData> getOperatorDatas() ;
	/**
	 * ��ȡָ����Ӫ�̵���Ȩ��Ϣ
	 * */
	public abstract List<Entitlement> getAuthorDatas(String id) ;
	/**
	 * ����CAģ������
	 * */
	public abstract void updateCaModule(int moduleId);
	/**
	 * ���CAģ������
	 * */
	public abstract int getCAModuleId();
	/**
	 * CA���Ƿ���Ч
	 * */
	public abstract boolean isCAValid();
	
	public abstract void chooseSession();
	
	public static interface CallBack{
		/**
		 * OSD��Ϣ�ص�
		 * */
		public void onScrollMessage(String msg);
		/**
		 * �ʼ���Ϣ�ص�
		 * */
		public void onMailChanged(boolean empty);
		/**
		 * CA��Ϣ���Ļص�
		 * */
		public void onCAInfoChnaged();
		
	}
}
