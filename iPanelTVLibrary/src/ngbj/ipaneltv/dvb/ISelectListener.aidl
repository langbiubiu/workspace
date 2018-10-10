package ngbj.ipaneltv.dvb;

interface ISelectListener {
	
			/**
		 * ËøÆµ³É¹¦
		 */
		oneway void onSelectSuccess(long freq);
		
		oneway void onSelectFailed(long freq);
		
		oneway void onSelectionLost(long freq);
		
		oneway void onSelectionResumed(long freq);
		
		oneway void onSignalStatus(String ss);
}
