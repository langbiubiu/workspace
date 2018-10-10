package ipanel.join.collectors;

import android.content.Intent;

public interface IAcquisitionConfig {
	
	/**
	 * ����ԭʼ��Ϊ����
	 * */
	public Collector parseAcquistitionMessage(Intent intent);
	/**
	 * ���FTP��������ַ
	 * */
	public String getServerAddress();
	/**
	 * ��ô洢�ļ�������ݻ�
	 * */
	public long getMaxSaveFileSize();
	/**
	 * ��ô洢�ļ����ϴ��ݻ�
	 * */
	public long getMaxUploadFileSize();
	/**
	 * ���zip�ļ���Чʱ�䷶Χ
	 * */
	public long getZipSaveDuration();
	/**
	 * �����Ϊ�ռ��㲥��Action
	 * */
	public String getCollectorBroadcastAction();
	/**
	 * �ϴ�����ʱ������
	 * */
	public long getUploadHearter() ;
	/**
	 * ����û���ʶ
	 * */
	public String getUserToken();
	/**
	 * ���ѹ��������
	 * */
	public String getZipPassword();

}
