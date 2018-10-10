package ipaneltv.toolkit.dvb;

import ipaneltv.toolkit.IPanelLog;
import ipaneltv.toolkit.dvb.DvbNetworkMapping.TransportStream;
import ipaneltv.toolkit.dvb.DvbSearchToolkit.TableReceiveTerminitor;
import ipaneltv.toolkit.mediaservice.components.L10n;
import ipaneltv.toolkit.mediaservice.components.PlayResourceScheduler.ResourcesState;
import android.content.Context;
import android.net.telecast.FrequencyInfo;
import android.net.telecast.NetworkInterface;
import android.net.telecast.SignalStatus;
import android.net.telecast.TransportManager;

public class DvbAutoSearch {
	static final String TAG = "NewDvbAutoSearch";
	private Object mutex = new Object();
	private boolean prepared = false, running = false;
	DvbSearchToolkit toolkit;
	protected ResourcesState mPlayResource;
	// StreamSelector selector;
	TransportManager tsManager;
	Context context;
	FrequencyInfo mainFreqInfo;
	AutoSearchListener asl;
	DvbNetworkMapping netMap;
	TransportStream curTransportStream;

	public static interface AutoSearchListener {
		void onTipsShow(String msg);

		void onFrequencySearch(FrequencyInfo fi);

		void onSignalStatus(SignalStatus ss);

		void onSearchStopped();

		void onSearchFinished(boolean isSuccess);
	}

	public static abstract class TableReceiver implements DvbSearchToolkit.TableReceiveListener {
		public int pid, tid;
		TableReceiveTerminitor t;

		public TableReceiver(int pid, int tid) {
			this.pid = pid;
			this.tid = tid;
		}

		@Override
		public void onReceiveFailed(String msg) {
			IPanelLog.i(TAG, "" + msg + ",tid=" + tid + ",pid=" + pid);
		}

		@Override
		public void onReceiveStart(TableReceiveTerminitor t) {
			this.t = t;
		}

		public void stopReceive() {
			if (t != null)
				t.terminate();
			t = null;
		}
	}

	DvbSearchToolkit.FreqSearchListener mainFreqListener = new DvbSearchToolkit.FreqSearchListener() {
		@Override
		public void onSearchStopped(FrequencyInfo fi) {
			IPanelLog.d(TAG, "onSearchStopped running=" + running);
			asl.onSearchStopped();
			if (running) {
				if (!startNext()) {
					stop(true);
					notifyMessage("自动搜索结束.");
					return;
				}
			}
			IPanelLog.i(TAG, "fi end" + fi.getFrequency());
			notifyMessage("结束搜索频点:" + fi.getFrequency());
		}

		@Override
		public void onSearchMessage(FrequencyInfo fi, String msg) {
			if (msg != null && fi != null) {
				if (msg.equals(L10n.code_904)) {
					addSignalStatus();
				}else if(msg.equals(L10n.code_905) || msg.equals(L10n.code_906) || msg.equals(L10n.code_907)){
					addNullSignalStatus();
				}
					
			}
			
			notifyMessage(msg);
		}

		@Override
		public void onSearchError(FrequencyInfo fi, String msg) {
			notifyMessage(msg);
		}
	};

	public static DvbAutoSearch createInstance(Context context, String SEARCH_UUID,
			ResourcesState mPlayResource) {
		try {
			return new DvbAutoSearch(context, SEARCH_UUID, mPlayResource);
		} catch (Exception e) {
			e.printStackTrace();
			return null;
		}
	}

	public DvbNetworkMapping getDvbNetworkMapping() {
		return netMap;
	}

	public TransportStream getCurrentTransportStream() {
		return curTransportStream;
	}

	public void setAutoSearchListener(AutoSearchListener l) {
		asl = l;
	}

	DvbAutoSearch(Context context, String SEARCH_UUID, ResourcesState mPlayResource) {
		this.context = context;
		this.mPlayResource = mPlayResource;
		if ((toolkit = DvbSearchToolkit.createInstance(SEARCH_UUID)) == null)
			throw new RuntimeException("create DvbSearchToolkit failed");
		if ((tsManager = TransportManager.getInstance(context)) == null)
			throw new RuntimeException("create TransportManager failed");
		toolkit.setTransportManager(tsManager);
		toolkit.setFreqSearchListener(mainFreqListener);
	}

	public DvbSearchToolkit getToolkit() {
		return toolkit;
	}

	void notifyMessage(String msg) {
		try {
			asl.onTipsShow(msg);
		} catch (Exception e) {
			IPanelLog.e(TAG, "notifySearchFailed error:" + e);
		}
	}

	void notifyStop(boolean isSuccess) {
		try {
			asl.onSearchFinished(isSuccess);
		} catch (Exception e) {
			IPanelLog.e(TAG, "notifyStop error:" + e);
		}
	}

	void onTableSearch(FrequencyInfo fi) {
		try {
			asl.onFrequencySearch(fi);
		} catch (Exception e) {
			IPanelLog.e(TAG, "onTableSearch error:" + e);
		}
	}

//	void addSignalChecker() {// 每次切换频率都需要重新添加
//		TimerTask tt = new TimerTask() {
//			@Override
//			public void run() {
//				try {
//					SignalStatus ss = mPlayResource.getSelector().getSignalStatus();
//					if (ss != null)
//						asl.onSignalStatus(ss);
//				} catch (Exception e) {
//					IPanelLog.e(TAG, "onSignalStatus error:" + e);
//				}
//			}
//		};
//		toolkit.addTimerTask(tt, 300, 1500);
//	}
	
	void addSignalStatus() {// 锁频成功则去获取信号状态
		IPanelLog.d(TAG, "addSignalStatus in....");
		try {
			SignalStatus ss = mPlayResource.getSelector().getSignalStatus();
			if (asl != null)
				asl.onSignalStatus(ss);
		} catch (Exception e) {
			IPanelLog.e(TAG, "onSignalStatus error:" + e);
		}
		IPanelLog.d(TAG, "addSignalStatus out....");
	}
	public void addNullSignalStatus(){
		IPanelLog.d(TAG, "addNullSignalStatus in....");
		if (asl != null){
			IPanelLog.d(TAG, "onNullSingal.....");
			asl.onSignalStatus(null);
		}
	}

	public void setMainFrequencyInfo(FrequencyInfo fi) {
		mainFreqInfo = fi;
	}

	void clear() {

	}

	public boolean prepare(int DELIVERY) {
		int interfaceid = 1001;
		NetworkInterface ni = toolkit.getDefaultInterfaceId(DELIVERY);
		if (ni != null ? ni.getId() >= 0 : false) {
			interfaceid = ni.getId();
		}
		IPanelLog.d(TAG, "prepare interface id = " + interfaceid);
		
		synchronized (mutex) {
			try {
				if (!prepared) {
					toolkit.setStreamSelector(mPlayResource);
					toolkit.setFreqFullSearchedTimeout(3 * 60 * 1000);// 3分钟
					toolkit.setTableFullReceivedTimeout(1 * 60 * 1000);// 1分钟
					prepared = true;
					return true;
				}
			} finally {
				clear();
			}
		}
		return prepared;
	}

	/**
	 * !!!对象将不可在使用
	 */
	public void release() {
		if (prepared) {
			if (mPlayResource != null) {
				mPlayResource.loosen(true);
			}
			prepared = false;
		}
	}

	boolean startNext() {
		TransportStream ts = curTransportStream;
		FrequencyInfo fi = new FrequencyInfo(NetworkInterface.DELIVERY_CABLE);
		if (ts == null)
			return false;
		// 找到下一个可搜索的频点
		int index = netMap.indexOfTransportStream(ts);
		int size = netMap.sizeOfTransportStream();
		IPanelLog.i(TAG, "startNext auto netMap index=" + index + ";size=" + size);
		if(index+1 >= size){
			return false;
		}
		for (int i = index + 1; i < size; i++) {
			if ((ts = netMap.transportStreamAt(i)) != null) {
				if ((fi = ts.getFrequencyInfo()) != null) {
					break;
				}
			}
		}
		if (fi != null) {
			curTransportStream = ts;
			toolkit.setCurrentTransportStream(curTransportStream);
			startFreqSearching(fi);
			return true;
		}
		return false;// 全部搜索完毕
	}

	void startMainFreqSearching(FrequencyInfo fi) {
		if (!toolkit.startFreqSearch(fi)) {
			notifyMessage(L10n.code_913);
			stop(false);
			return;
		}
		notifyMessage("开始搜索频点:" + fi.getFrequency());
		//addSignalChecker();
		onTableSearch(fi);
	}

	void startFreqSearching(FrequencyInfo fi) {
		// 可能多次启动，因此不能直接使用主频点的参数,而是使用curTransportStream
		if (!toolkit.startFreqSearch(fi)) {
			// notifyMessage("启动搜索失败！");
			stop(true);
			return;
		}

		notifyMessage("开始搜索频点:" + fi.getFrequency());
		//addSignalChecker();
		onTableSearch(fi);
		IPanelLog.d(TAG, "startFreqSearching continue search");
	}

	public boolean start() {
		FrequencyInfo fi = null;
		synchronized (mutex) {
			if (prepared && !running) {
				running = true;
				if (curTransportStream == null) {
					netMap = new DvbNetworkMapping();
					fi = FrequencyInfo.fromString(mainFreqInfo.toString());
					curTransportStream = netMap.createTransportStream(fi);
					netMap.addMainTransportStream(curTransportStream);
					toolkit.setCurrentTransportStream(curTransportStream);
				} else {
					fi = curTransportStream.getFrequencyInfo();
				}
				startMainFreqSearching(fi);
				return true;
			}
			return false;
		}
	}

	public boolean receiveTable(int bs, TableReceiver r, int program) {
		return toolkit.addTableSearching(bs, r.pid, r.tid, r, program);
	}

	/**
	 * 
	 * @param bs
	 *            缓冲区大小
	 * @param r
	 *            回调
	 * @param program
	 *            频道号，默认传-1
	 * @param flags
	 *            过滤参数。默认传0;1为所有表采用统一的过滤方式。
	 * @return 成功返回true,否则返回false
	 */
	public boolean receiveTable(int bs, TableReceiver r, int program, int flags) {
		return toolkit.addTableSearching(bs, r.pid, r.tid, r, program, flags);
	}

	/**
	 * 
	 * @param bs
	 *            bufer缓冲区大小
	 * @param bs
	 *            filter缓冲区大小
	 * @param r
	 *            回调
	 * @param program
	 *            频道号，默认传-1
	 * @param flags
	 *            过滤参数。默认传0;1为所有表采用统一的过滤方式。
	 * @return 成功返回true,否则返回false
	 */
	public boolean receiveTable(int bs,int filtersize, TableReceiver r, int program, int flags) {
		return toolkit.addTableSearching(bs,filtersize, r.pid, r.tid, r, program, flags);
	}
	
	public void stop(boolean flag) {
		synchronized (mutex) {
			if (running) {
				running = false;
				notifyStop(flag);
				toolkit.stopFreqSearch();
				release();
			}
		}
	}

	public void suspend(boolean isSuspend) {
		synchronized (mutex) {
			running = false;
			notifyMessage("the search is suspend:"+isSuspend);
			toolkit.stopFreqSearch();
			release();
		}
	}
	
	public void pause() {

	}

	public void resume() {

	}

}
