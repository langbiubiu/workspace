package ipaneltv.toolkit.parentslock;

import ipaneltv.toolkit.parentslock.IParentLockDateListener;

interface IRemoteService {
	/**
	*��֤�����Ƿ���ȷ
	*param password ��Ҫ��֤���ַ�������
	*return true��ʾ������ȷ��false���벻��ȷ
	*/
	boolean validatePwd(String password);
	
	
	/**
	*����Ƶ���ż��Ƶ���Ƿ����
	*param ctx ���ڻ����е������Ķ���
	*param channel_number ����Ƶ����
	*return true��ʾƵ���Ѽ�����falseƵ��δ����
	*/
	boolean checkChannelLocked(int channel_number);
	
	
	/**
	*��������
	*
	*return true��ʾ���óɹ���false����ʧ��
	*/
	boolean resetPassword();
	
	
	/**
	*�������Ƶ������
	*
	*return true��ʾ��ճɹ���false���ʧ��
	*/
	boolean clearChannelLock();

	/**
	*�������ݿ��е����ݱ䶯
	*
	*
	*/
	void setLockDateListener(IParentLockDateListener lockDataListener);

	/**
	*���ݿ��е����ݱ䶯
	*
	*
	*/
	boolean onParentLockDataChange();
}