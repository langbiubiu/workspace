package cn.ipanel.android.table;


public interface ProviderConfig {
	/**
	 * ���ݿ��б�����Ӧ����
	 * */
	public Class[] createClasses();
	/**
	 * ���ݿ������
	 * */
	public String getDataBaseName();
	/**
	 * ���ݿ�İ汾
	 * */
	public int getDataBaseVersion();
	/**
	 * ���ݿ��Ӧ����Ȩ
	 * */
	public String getAuthority();


}
