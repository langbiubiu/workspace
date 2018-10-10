package ipanel.join.collectors;

import android.content.Intent;

public interface IAcquisitionConfig {
	
	/**
	 * 解析原始行为数据
	 * */
	public Collector parseAcquistitionMessage(Intent intent);
	/**
	 * 获得FTP服务器地址
	 * */
	public String getServerAddress();
	/**
	 * 获得存储文件的最大容积
	 * */
	public long getMaxSaveFileSize();
	/**
	 * 获得存储文件的上传容积
	 * */
	public long getMaxUploadFileSize();
	/**
	 * 获得zip文件有效时间范围
	 * */
	public long getZipSaveDuration();
	/**
	 * 获得行为收集广播的Action
	 * */
	public String getCollectorBroadcastAction();
	/**
	 * 上传检测的时间周期
	 * */
	public long getUploadHearter() ;
	/**
	 * 获得用户标识
	 * */
	public String getUserToken();
	/**
	 * 获得压缩包密码
	 * */
	public String getZipPassword();

}
