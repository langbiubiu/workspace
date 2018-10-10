package ipaneltv.toolkit.dvb;

import ipaneltv.toolkit.IPanelLog;
import ipaneltv.toolkit.Section;
import android.content.Context;
import android.net.telecast.FrequencyInfo;
import android.net.telecast.StreamObserver;
import android.net.telecast.TransportManager;

public class DvbSiEventReceiveSearch {
	static final String TAG = DvbSiEventReceiveSearch.class.getSimpleName();
	public static final int SEARCH_EPG = 0;
	public static final int SEARCH_PF = 1;
	public static final int SEARCH_EPG_OTHER = 2;
	final int maxfilterSize = 12;
	final int bufferSize = 1024 * 512;
	DvbSiEventReceiveToolkit toolkit;
	TransportManager tsManager;
	DvbNetworkMapping netMap;
	Context context;
	boolean FreqChanged = false;
	private StreamObserver streamOb = null;
	private Object mutex = new Object();
	private DvbSiEventReceiveListener drl;

	public static DvbSiEventReceiveSearch createInstance(Context context, String SEARCH_UUID) {
		try {
			return new DvbSiEventReceiveSearch(context, SEARCH_UUID);
		} catch (Exception e) {
			e.printStackTrace();
			return null;
		}
	}

	DvbSiEventReceiveSearch(Context context, String SEARCH_UUID) {
		this.context = context;
		if (tsManager == null) {
			if ((tsManager = TransportManager.getInstance(context)) == null)
				throw new RuntimeException("create TransportManager failed");
			streamOb = tsManager.createObserver(SEARCH_UUID);
		}

		if (toolkit == null) {
			if ((toolkit = DvbSiEventReceiveToolkit.createInstance(SEARCH_UUID)) == null)
				throw new RuntimeException("create DvbSiEventReceiveToolkit failed");
		}
		toolkit.setRetrieveSearchListener(listener);
		toolkit.setTransportManager(tsManager);
	}

	DvbSiEventReceiveToolkit.RetrieveSearchListener listener = new DvbSiEventReceiveToolkit.RetrieveSearchListener() {

		@Override
		public void onSearchStopped(boolean flag) {
			// TODO Auto-generated method stub
			if (drl != null) {
				drl.onSearchFinished(flag);
			}
		}

		@Override
		public void onSearchMessage(FrequencyInfo fi, String msg) {
			// TODO Auto-generated method stub

		}

		@Override
		public void onSearchError(FrequencyInfo fi, String msg) {
			// TODO Auto-generated method stub

		}
	};

	public void StreamStateListener(StreamObserver.StreamStateListener ssl) {
		if (streamOb != null) {
			IPanelLog.d(TAG, "StreamStateListener");
			streamOb.setStreamStateListener(ssl);
			streamOb.queryStreamState();
			IPanelLog.d(TAG, "queryStreamState");
		}
	}

	public void setDvbSiEventReceiveListener(DvbSiEventReceiveListener l) {
		drl = l;
	}

	public static interface DvbSiEventReceiveListener {
		void onSearchFinished(boolean flag);
	}

	/**
	 * 
	 * @param freq
	 *            所要搜去的频点，该频点必须为当前锁频的频点
	 * @param flag
	 *            SEARCH_EPG和SEARCH_PF分别代表搜EPG和PF
	 * @param l
	 *            搜索的监听
	 * @return
	 */
	public boolean startsearch(long freq, int flag,
			DvbSiEventReceiveToolkit.OnRetrieveStateListener l) {
		int ret = 0;
		String tables[] = null;
		synchronized (mutex) {
			IPanelLog.i(TAG, "startsearch curFreq = " + freq + ";flag = " + flag);
			if (toolkit.start(freq)) {
				if (flag == SEARCH_EPG) {
					tables = new String[] { "schedule_actual" };
				} else if (flag == SEARCH_EPG_OTHER) {
					IPanelLog.d(TAG, "schedule_other search");
					tables = new String[] { "schedule_other" };
				} else {
					IPanelLog.d(TAG, "pf search & pf_other");
					tables = new String[] { "pf_actual", "pf_other" };
				}
				IPanelLog.d(TAG, "tables.length = " + tables.length);
				for (int i = 0; i < tables.length; i++) {
					IPanelLog.d(TAG, "tables[i] = " + tables[i]);
					if (tables[i].equals("pf_actual")) {
						IPanelLog.d(TAG, "pf_actual");
						if (startPfActual(l))
							ret++;
					} else if (tables[i].equals("pf_other")) {
						if (startPfOther(l))
							ret++;
					} else if (tables[i].equals("schedule_actual")) {
						IPanelLog.d(TAG, "schedule_actual");
						if (startScheduleActual(l))
							ret++;
					} else if (tables[i].equals("schedule_other")) {
						if (startScheduleOther(l))
							ret++;
					}
				}
				if (ret == tables.length) {
					IPanelLog.d(TAG, "start success");
					return true;
				}
			}
			IPanelLog.d(TAG, "start failed");
			return false;
		}
	}

	public boolean isFreqUnderSearch(long freq) {
		synchronized (mutex) {
			return toolkit.isFreqUnderSearch(freq);
		}
	}

	private boolean startScheduleOther(DvbSiEventReceiveToolkit.OnRetrieveStateListener l) {
		IPanelLog.d(TAG, "startScheduleOther");
		if (!toolkit.addRetrieveTask(5000, DvbConst.PID_EIT, DvbConst.TID_EIT_OTHER_FIRST, l)) {
			IPanelLog.e(TAG, "Start PfActual filter fail!");
			return false;
		}
		return true;
	}

	private boolean startScheduleActual(DvbSiEventReceiveToolkit.OnRetrieveStateListener l) {
		IPanelLog.d(TAG, "startScheduleActual");
		if (!toolkit.addRetrieveTask(8000, DvbConst.PID_EIT, DvbConst.TID_EIT_ACTUAL_FIRST, l)) {
			IPanelLog.e(TAG, "Start ScheduleActual 0x50 filter fail!");
			return false;
		}
		if (!toolkit.addRetrieveTask(8000, DvbConst.PID_EIT, DvbConst.TID_EIT_ACTUAL_FIRST + 1, l)) {
			IPanelLog.e(TAG, "Start ScheduleActual 0x51 filter fail!");
			return false;
		}
		if (!toolkit.addRetrieveTask(8000, DvbConst.PID_EIT, DvbConst.TID_EIT_ACTUAL_FIRST + 2, l)) {
			IPanelLog.e(TAG, "Start ScheduleActual 0x52 filter fail!");
			return false;
		}
		if (!toolkit.addRetrieveTask(8000, DvbConst.PID_EIT, DvbConst.TID_EIT_ACTUAL_FIRST + 3, l)) {
			IPanelLog.e(TAG, "Start ScheduleActual 0x53 filter fail!");
			return false;
		}
		IPanelLog.d(TAG, "startScheduleActual end");
		return true;
	}

	private boolean startPfOther(DvbSiEventReceiveToolkit.OnRetrieveStateListener l) {
		IPanelLog.d(TAG, "startPfOther");
		if (!toolkit.addRetrieveTask(6000, DvbConst.PID_EIT, DvbConst.TID_EIT_OTHER_PF, l)) {
			IPanelLog.e(TAG, "Start PfActual filter fail!");
			return false;
		}
		return true;
	}

	private boolean startPfActual(DvbSiEventReceiveToolkit.OnRetrieveStateListener l) {
		IPanelLog.d(TAG, "startPfActual");
		if (!toolkit.addPFTask(5000, DvbConst.PID_EIT, DvbConst.TID_EIT_ACTUAL_PF, l)) {
			IPanelLog.e(TAG, "Start PfActual filter fail!");
			return false;
		}
		IPanelLog.d(TAG, "startPfActual end");
		return true;
	}

	public boolean FreeSection(Section s) {
		return toolkit.FreeSection(s);
	}

	public void stop() {
		IPanelLog.d(TAG, "stop in");
		synchronized (mutex) {
			IPanelLog.d(TAG, "stop in 11");
			toolkit.stop(false);
		}
		IPanelLog.d(TAG, "stop in 22");
	}

	public void release() {
		synchronized (mutex) {
			toolkit.releaseSections();
		}
	}

	public int getTaskNum() {
		return toolkit.getTaskNum();
	}

}
