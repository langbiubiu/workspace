package ngbj.ipaneltv.dvb;

interface IDvbSearchListener {
	
	oneway void onResponseStart(boolean succ);
	
	oneway void onFrequencySearch(String fi);
	
	oneway void onFrequencyEnd(String fi);
	
	oneway void onFrequencyNumber(int number);
	
	oneway void onSignalStatus(String ss);
	
	oneway void onServiceFound(String name, int type);
	
	oneway void onSearchFinished(boolean successed);
	
	oneway void onRespWriteDatabase(boolean succ);
	
	oneway void onTipsShow(String msg);
}
