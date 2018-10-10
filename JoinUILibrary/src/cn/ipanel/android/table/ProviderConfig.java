package cn.ipanel.android.table;


public interface ProviderConfig {
	/**
	 * 数据库中表所对应的类
	 * */
	public Class[] createClasses();
	/**
	 * 数据库的名字
	 * */
	public String getDataBaseName();
	/**
	 * 数据库的版本
	 * */
	public int getDataBaseVersion();
	/**
	 * 数据库对应的授权
	 * */
	public String getAuthority();


}
