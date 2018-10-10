package ngbj.ipaneltv.dvb;

import ngbj.ipaneltv.dvb.INgbJScanListener;
import ngbj.ipaneltv.dvb.INgbJScanEitListener;
import ngbj.ipaneltv.dvb.IDvbSearchListener;
import ngbj.ipaneltv.dvb.ISelectListener;
import ngbj.ipanel.player.iNgbPlayer;

interface INgbJScanManager{
	/**
	 * @return iNgbPlayer对象，表示多媒体播放器。
	 */
	iNgbPlayer createLivePlayer() ;	
	
	/**
	 *
	 * @param searchType--频道搜索方式，取值0~2，其中：
	 * 		  0--手动搜索；
	 * 		  1--自动搜索；
	 * 		  2--区间搜索。
	 * @param  deliveryType 调制方式
	 *        0--cable
	 *        1--satellite
	 *        2--terrestrial
	 *
	 * @param  flags 
	 *        0-----普通搜索，仅保存数据库。
	 *        0x01--ngb搜索，会回调ngb的回调
	 *        0x04--保存文件夹。
	 *        0x08--保存数据库。
	 * 初始化搜索
	 */
	void initScan(int searchType, int deliveryType, int flags);
	
	/**
	 * 添加主频点参数
	 *
	 * @param  mainFrequency 主频点频率
	 * @param  modulation 调制方式
	 * @param  symbolRate 符号率
	 */
	boolean setMainFrequency(long mainFrequency,int modulation,int symbolRate);
	
	/**
	 * 添加手动搜索参数
	 *
	 * @param  start 起始频点
	 * @param  end   终止频点
	 * @param  modulation 调制方式
	 * @param  symbolRate 符号率
	 * @param  polarization 极化方式，未定义类型为0
	 */
	 void addScanManualParam(long start, long end, int modulation, int symbolRate, int polarization);
	
	/**
	*
	*锁频操作，用来查询频点信息。对应ISelectListener回调
	*
	*/
	void lockFreqency(long freq,int modulation,int symbolRate);
	
	/**
	 * 开始频道搜索（搜索消息异步回调）
	 */
	void startScan();
	
	/**
	* 将本次搜索结果保存到NVM中。
 	*
 	* @param  flags
 	*
 	*        0--清除以前的数据，重新保存。
 	*        1--添加式保存
 	*
	* @return boolean型，true表示保存成功，false表示保存失败。
	*/
	boolean saveScanResult(int flags);

	/**
	* 取消频道搜索。
	* <p>
	* 应用在捕获搜索失败(<code>ChannelScanFailureEvent</code>)或搜索结束(
	* <code>ChannelScanFinishEvent</code>) 事件之前，可以通过调用该方法取消本次搜索。
	*/
	void cancel();

	/**
	* 释放搜索使用的资源。
	*/
	void release();
	
	/**
	* 设置普通dvb搜索监听器
	*
	*/
	void setDvbSearchListener(IDvbSearchListener listener);

	/**
	* 设置普通dvb搜索监听器
	*
	*/
	void setSelectListener(ISelectListener listener);

	/**
	* 注册搜索过程状态监听器(ngb规范)。
	* 
	* @param listener
	*            NgbScanListener对象，表示待注册的搜索过程监听器对象。
	*/
	void setChannelScanListener(INgbJScanListener listener) ;

	/**
	* 移除搜索过程状态监听器(ngb规范)。
	* 
	* @param listener
	*            NgbScanListener对象，表示待注销的搜索过程监听器对象。
	*/
	void removeChannelScanListener(INgbJScanListener listener);
	
	/**
	 * 获得UTC时间，从TDT或TOT中获取
	 */
	long getUTCTime(long frequence);	
	
	/**
	 * 设置eit搜索的回调函数(ngb规范)。
	 */
	void setScanEitListener(INgbJScanEitListener pflistener);
	
	/**
	 * 搜索该频点的全部EPG信息，其中Eit_Actual的信息通过eit的回调函数异步回调，
	 * 其他信息存放在SectionStorage中(ngb规范)。
	 * 
	 * @param frequence  频点
	 */
	void scanEitAll(long frequence);
	
	/**
	 * 搜索频点的Eit_Actual，即EPG PF信息(ngb规范)。
	 * 
	 * @param frequence 当前节目的频点信息
	 */
	void scanEitActual(long frequence);	
	
	/**
	 * 释放tune资源，如果正在使用tune则释放失败。
	 * 成功返回1，失败返回0。
	 */
	int releaseTune();
	
}
