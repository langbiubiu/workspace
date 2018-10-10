package ngbj.ipaneltv.dvb;

import ngbj.ipaneltv.dvb.NgbJSITransportStream;
import ngbj.ipaneltv.dvb.NgbJSIService;

interface INgbJScanListener {
	
	/**
	 * 搜台成功解析NIT(自动搜索时?)
	 * 
	 * @param niid
	 * @param siTSs--表示NIT表中包含的传输流NgbJSITransportStream对象数组。
	 */
	void onScanNITSuccess(int niid, in NgbJSITransportStream[] siTSs);
	
	/**
	 * 搜索成功（搜索完一个频点）
	 * 
	 * @param niid
	 * @param siSs --节目信息对象NgbJSIService数组。
	 */
	void onScanSuccess(int niid, in NgbJSIService[] siSs);
	
	/**
	 * 搜索失败
	 * 
	 * @param niid 
	 * @param reason 搜台失败原因，取值为0~5，其中：
	 * 		0--未知原因;
	 * 		1--锁频失败;
	 * 		2--NIT搜索失败;
	 * 		3--BAT搜索失败;
	 * 		4--PAT搜索失败;
	 * 		5--PMT搜索失败。
	 */
	void onScanFailure(int niid, int reason);
	
	/**
	 * 搜索完成
	 * @param niid
	 * @param transportStreamCount--表示传输流个数。
	 * @param serviceCount --表示业务个数
	 */
	void onScanFinish(int niid, int transportStreamCount, int serviceCount);
}
