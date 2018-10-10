package ipaneltv.toolkit.dvb;

import ipaneltv.toolkit.IPanelLog;
import android.content.Context;
import android.net.telecast.TransportManager;

public class DvbSiEventPFSearch {
	static final String TAG = DvbSiEventPFSearch.class.getSimpleName();
	PFSearchToolkit toolkit;
	TransportManager tsManager;
	Context context;
	private Object mutex = new Object();

	public static DvbSiEventPFSearch createInstance(Context context, String SEARCH_UUID) {
		try {
			return new DvbSiEventPFSearch(context, SEARCH_UUID);
		} catch (Exception e) {
			e.printStackTrace();
			return null;
		}
	}

	DvbSiEventPFSearch(Context context, String SEARCH_UUID) {
		this.context = context;
		if (tsManager == null) {
			if ((tsManager = TransportManager.getInstance(context)) == null)
				throw new RuntimeException("create TransportManager failed");
		}

		if (toolkit == null) {
			if ((toolkit = PFSearchToolkit.createInstance(context, SEARCH_UUID, tsManager)) == null)
				throw new RuntimeException("create PFSearchToolkit failed");
		}
	}

	/**
	 * 
	 * @param freq
	 *            所要搜去的频点，该频点必须为当前锁频的频点
	 * @param l
	 *            搜索的监听器
	 * @return
	 */
	public boolean startSearchPF(long freq, PFSearchToolkit.OnRetrieveStateListener l) {
		synchronized (mutex) {
			IPanelLog.i(TAG, "startsearch curFreq = " + freq);
			if (toolkit.start(freq)) {
				IPanelLog.d(TAG, "pf_actual");
				if (startPfActual(l)) {
					return true;
				}
			}
			IPanelLog.d(TAG, "start failed");
			return false;
		}
	}

	public boolean isFreqUnderSearch(long freq){
		synchronized (mutex) {
			return toolkit.isFreqUnderSearch(freq);
		}
	}
	
	private boolean startPfActual(
			PFSearchToolkit.OnRetrieveStateListener l) {
		IPanelLog.d(TAG, "startPfActual");
		if (!toolkit.addRetrieveTask(l, DvbConst.PID_EIT,
				DvbConst.TID_EIT_ACTUAL_PF, 5000)) {
			IPanelLog.e(TAG, "Start PfActual filter fail!");
			return false;
		}
		IPanelLog.d(TAG, "startPfActual end");
		return true;
	}
	
	public void stop() {
		toolkit.stopFilter();
	}
	
	public void release(){
		toolkit.closeFilter();
	}

	public boolean isFull(int programNum) {
		return toolkit.isFull(programNum);
	}

	public boolean isReady() {
		return toolkit.isReady();
	}
}
