package ipaneltv.toolkit.dvb;

import ipaneltv.toolkit.IPanelLog;
import ipaneltv.toolkit.dvb.DvbNetworkMapping.TransportStream;
import ipaneltv.toolkit.dvb.DvbObjectification.SiCabelFrequencyInfo;
import ipaneltv.toolkit.dvb.DvbObjectification.SiNetwork;
import ipaneltv.toolkit.dvb.DvbObjectification.SiTransportStream;
import ipaneltv.toolkit.mediaservice.components.L10n;
import ipaneltv.toolkit.mediaservice.components.PlayResourceScheduler.ResourcesState;

import java.util.List;

import android.content.Context;
import android.net.telecast.FrequencyInfo;
import android.net.telecast.NetworkInterface;
import android.net.telecast.SignalStatus;
import android.net.telecast.TransportManager;
import android.util.Log;

public class DvbManualSearch {
	static final String TAG = "NewDvbManualSearch";
	private long startFreq, endFreq;
	private int symbol;
	private String modulation;
	private Object mutex = new Object();
	private boolean prepared = false, running = false;
	DvbSearchToolkit toolkit;
	protected ResourcesState mPlayResource;

	TransportManager tsManager;
	Context context;
	FrequencyInfo startFreqInfo;
	ManualSearchListener asl;
	DvbNetworkMapping netMap;
	TransportStream curTransportStream;
	TransportStream mainTransportStream;
	SiNetwork siNetwork;
	boolean first = true;
	static final long FREQ_STEP = 8 * 1000 * 1000; // 8MHZ

	FrequencyInfo mainFreqInfo;

	public DvbManualSearch(long startFreq, long endFreq, int symbol, int modulation,
			Context context, String SEARCH_UUID, ResourcesState mPlayResource) {
		// this.nowFreq = startFreq;
		this.startFreq = startFreq;
		this.endFreq = endFreq;
		this.context = context;
		this.symbol = symbol * 1000;
		this.modulation = "qam" + modulation;
		this.mPlayResource = mPlayResource;
		IPanelLog.d(TAG, "NewDvbManualSearch startFreq=" + startFreq + ",endFreq=" + endFreq);
		IPanelLog.d(TAG, "NewDvbManualSearch modulation=" + modulation + ",symbol=" + symbol);
		if ((toolkit = DvbSearchToolkit.createInstance(SEARCH_UUID)) == null)
			throw new RuntimeException("create NewDvbSearchToolkit failed");
		if ((tsManager = TransportManager.getInstance(context)) == null)
			throw new RuntimeException("create TransportManager failed");
		toolkit.setTransportManager(tsManager);
		toolkit.setFreqSearchListener(thisFreqListener);
	}

	public static interface ManualSearchListener {
		void onTipsShow(String msg);

		void onFrequencySearch(FrequencyInfo fi);

		void onSignalStatus(SignalStatus ss);

		void onSearchFinished(boolean isSuccess);

		void onSearchStopped();
	}

	public static abstract class TableReceiver implements DvbSearchToolkit.TableReceiveListener {
		public int pid, tid;
		DvbSearchToolkit.TableReceiveTerminitor t;

		public TableReceiver(int pid, int tid) {
			this.pid = pid;
			this.tid = tid;
		}

		@Override
		public void onReceiveFailed(String msg) {
			IPanelLog.i(TAG, "" + msg + ",tid=" + tid + ",pid=" + pid);
		}

		@Override
		public void onReceiveStart(DvbSearchToolkit.TableReceiveTerminitor t) {
			this.t = t;
		}

		public void stopReceive() {
			if (t != null)
				t.terminate();
			t = null;
		}
	}
	
	boolean success = false; 
	
	DvbSearchToolkit.FreqSearchListener thisFreqListener = new DvbSearchToolkit.FreqSearchListener() {
		@Override
		public void onSearchStopped(FrequencyInfo fi) {
			IPanelLog.i(TAG, "thisFreqListener onSearchStopped running = " + running);
			if(running){
				TransportStream ts = curTransportStream;
				Log.d(TAG, "onSearchStopped checkMode = "+ checkMode+";ts.getFrequencyInfo() = "+ ts.getFrequencyInfo());
				if(checkMode == WASU_CHECK && success == false && "6875000".equals(ts.getFrequencyInfo().getParameter(FrequencyInfo.SYMBOL_RATE))){
					fi = ts.getFrequencyInfo();
					fi.setParameter(FrequencyInfo.SYMBOL_RATE, 6900000);
					startFreqSearching(fi);
					return;
				}	
			}
			asl.onSearchStopped();
			if (running) {
				success = false;
				if (!startNext()) {
					stop();
					notifyMessage("手动搜索结束.");
					return;
				}
			}
			notifyMessage("结束搜索频点:" + fi.getFrequency());
		}

		@Override
		public void onSearchMessage(FrequencyInfo fi, String msg) {
			Log.i(TAG, "------>onSearchMessage" + fi.getFrequency()+";msg="+msg +";curTransportStream.getFrequencyInfo().getFrequency() = "+ curTransportStream.getFrequencyInfo().getFrequency());
			if(fi.getFrequency() == curTransportStream.getFrequencyInfo().getFrequency() && ErrorString.code_904.equals(msg)){
				success = true;
			}
			if (msg != null && fi != null) {
				if (msg.equals(L10n.code_904)) {
					addSignalStatus();
				}
			}

			notifyMessage(msg);
		}

		@Override
		public void onSearchError(FrequencyInfo fi, String msg) {
			notifyMessage(msg);
		}
	};

	public static DvbManualSearch createInstance(long startFreq, long endFreq, int symbol,
			int modulation, Context context, String SEARCH_UUID, ResourcesState mPlayResource) {
		try {

			return new DvbManualSearch(startFreq, endFreq, symbol, modulation, context,
					SEARCH_UUID, mPlayResource);
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

	public DvbSearchToolkit getToolkit() {
		return toolkit;
	}

	public void setManualSearchListener(ManualSearchListener l) {
		asl = l;
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
			IPanelLog.e(TAG, "notifyStop error:" + e.toString());
			e.printStackTrace();
		}
	}

	void onTableSearch(FrequencyInfo fi) {
		try {
			asl.onFrequencySearch(fi);
		} catch (Exception e) {
			IPanelLog.e(TAG, "onTableSearch error:" + e);
		}
	}

	// void addSignalChecker() {// 每次切换频率都需要重新添加
	// TimerTask tt = new TimerTask() {
	// @Override
	// public void run() {
	// try {
	// SignalStatus ss = mPlayResource.getSelector().getSignalStatus();
	// if (ss != null)
	// asl.onSignalStatus(ss);
	// } catch (Exception e) {
	// IPanelLog.e(TAG, "onSignalStatus error:" + e);
	// }
	// }
	// };
	// toolkit.addTimerTask(tt, 300, 1500);
	// }

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

	public void setStartInfo(FrequencyInfo fi) {
		startFreqInfo = fi;
	}

	public void setMainFrequencyInfo(FrequencyInfo fi) {
		mainFreqInfo = fi;
		IPanelLog.d(TAG, "main freq = " + fi);
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

	int checkMode = 0;
	/**
	 * 华数版本特殊标志，在手动搜素中 如果6875000搜索失败使用6900000再搜一次。
	 */
	public static final int WASU_CHECK = 1;
	/**
	 * 设置频点检查模式
	 * @param flags 默认为0
	 */
	public void setCheckMode(int flags){
		checkMode = flags;
	}
	
	boolean startNext() {
		IPanelLog.i(TAG, "go in startNext");
		TransportStream ts = curTransportStream;
		FrequencyInfo fi = new FrequencyInfo(NetworkInterface.DELIVERY_CABLE);
		if (ts == null)
			return false;
		fi = null; // 必须加上，否则不进if ((fi = ts.getFrequencyInfo()) != null){也会再搜索一次
		IPanelLog.i(TAG, "start next frequency=" + ts.getFrequencyInfo().getFrequency());// 避免死机
		// 找到下一个可搜索的频点
		int index = netMap.indexOfTransportStream(ts);
		int size = netMap.sizeOfTransportStream();
		IPanelLog.i(TAG, "startNext netMap index=" + index + ";size=" + size);
		if (index + 1 >= size) {
			return false;
		}
		if (index >= 0) { // indexof 失败返回-1
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
		}
		return false;// 全部搜索完毕
	}

	boolean startFreqSearching(FrequencyInfo fi) {
		// 可能多次启动，因此不能直接使用主频点的参数,而是使用curTransportStream
		if (!toolkit.startFreqSearch(fi)) {
			stop();
			return false;
		}
		notifyMessage("开始搜索频点:" + fi.getFrequency());
		// addSignalChecker();
		onTableSearch(fi);
		return true;
	}

	public boolean start() {
		if (!adjustStartFreq()) {
			IPanelLog.i(TAG, "ignoreFreqs contains all freqs between " + startFreq + " and " + endFreq);
			return false;
		}
		FrequencyInfo fi = null;
		synchronized (mutex) {
			IPanelLog.d(TAG, "start prepared = " + prepared + ";running = " + running);
			if (prepared && !running) {
				running = true;
				if (curTransportStream == null) {
					netMap = new DvbNetworkMapping();
					fi = FrequencyInfo.fromString(startFreqInfo.toString());
					siNetwork = new SiNetwork();
					curTransportStream = netMap.createTransportStream(fi);
					SiTransportStream sits = siNetwork.addTransportStream();
					SiCabelFrequencyInfo sifi = new SiCabelFrequencyInfo();
					sifi.frequency = fi.getFrequency();
					sifi.modulation = fi.getParameter(FrequencyInfo.MODULATION);
					sifi.symbol_rate = (Integer.valueOf(fi.getParameter(FrequencyInfo.SYMBOL_RATE)));
					sits.frequency_info = sifi;
					netMap.addMainTransportStream(curTransportStream);
					netMap.setSiNetwork(siNetwork);
					toolkit.setCurrentTransportStream(curTransportStream);
				} else {
					fi = curTransportStream.getFrequencyInfo();
				}
				IPanelLog.d(TAG, "start fi = " + fi + ",curTransportStream=" + curTransportStream);
				setAllFrequencyTransportStream();
				startFreqSearching(fi);
				return true;
			}
			return false;
		}
	}

	public boolean start(int flags) {
		IPanelLog.i(TAG, "---zzq---start freq=" + startFreq);
		FrequencyInfo mfi = null;
		synchronized (mutex) {
			if (prepared && !running) {
				running = true;
				mfi = FrequencyInfo.fromString(mainFreqInfo.toString());
				if (curTransportStream == null) {
					netMap = new DvbNetworkMapping();
					siNetwork = new SiNetwork();
					curTransportStream = netMap.createTransportStream(mfi);
					SiTransportStream sits = siNetwork.addTransportStream();
					SiCabelFrequencyInfo sifi = new SiCabelFrequencyInfo();
					sifi.frequency = mfi.getFrequency();
					sifi.modulation = mfi.getParameter(FrequencyInfo.MODULATION);
					sifi.symbol_rate = (Integer.valueOf(mfi
							.getParameter(FrequencyInfo.SYMBOL_RATE)));
					sits.frequency_info = sifi;
					netMap.addMainTransportStream(curTransportStream);
					netMap.setSiNetwork(siNetwork);
					IPanelLog.d(TAG, "start curTransportStream = " + curTransportStream);
				}
				setManualAllFrequencyTransportStream();
				startFreqSearching(mfi);
				return true;
			}
			return false;
		}
	
	}
	
	public boolean start(long freq) {
		if (!adjustStartFreq()) {
			IPanelLog.i(TAG, "ignoreFreqs contains all freqs between " + startFreq + " and " + endFreq);
			return false;
		}
		IPanelLog.i(TAG, "start freq=" + startFreq);
		FrequencyInfo mfi = null;
		synchronized (mutex) {
			if (prepared && !running) {
				running = true;
				if (isMainFrequencyUnder()) {
					mfi = FrequencyInfo.fromString(mainFreqInfo.toString());
					if (curTransportStream == null) {
						netMap = new DvbNetworkMapping();
						siNetwork = new SiNetwork();
						curTransportStream = netMap.createTransportStream(mfi);
						SiTransportStream sits = siNetwork.addTransportStream();
						SiCabelFrequencyInfo sifi = new SiCabelFrequencyInfo();
						sifi.frequency = mfi.getFrequency();
						sifi.modulation = mfi.getParameter(FrequencyInfo.MODULATION);
						sifi.symbol_rate = (Integer.valueOf(mfi
								.getParameter(FrequencyInfo.SYMBOL_RATE)));
						sits.frequency_info = sifi;
						netMap.addMainTransportStream(curTransportStream);
						netMap.setSiNetwork(siNetwork);
						IPanelLog.d(TAG, "start curTransportStream = " + curTransportStream);
					}
				} else {
					mfi = new FrequencyInfo(NetworkInterface.DELIVERY_CABLE);
					mfi.setFrequency(startFreq);
					mfi.setParameter(FrequencyInfo.MODULATION, modulation);
					mfi.setParameter(FrequencyInfo.SYMBOL_RATE, symbol);
					if (curTransportStream == null) {
						netMap = new DvbNetworkMapping();
						siNetwork = new SiNetwork();
						curTransportStream = netMap.createTransportStream(mfi);
						netMap.addMainTransportStream(curTransportStream);
						netMap.setSiNetwork(siNetwork);
						IPanelLog.d(TAG, "start curTransportStream = " + curTransportStream);
					}
				}

				setManualAllFrequencyTransportStream();
				startFreqSearching(mfi);
				return true;
			}
			return false;
		}
	}

	public boolean isMainFrequencyUnder() {
		boolean isMain = false;
		long sfreq = startFreq;
		do {
			IPanelLog.i(TAG, "setManualAllFrequencyTransportStream sfreq=" + sfreq);
			if (mainFreqInfo.getFrequency() == sfreq) {
				isMain = true;
				break;
			}
		} while ((sfreq = getNextFreqency(sfreq)) <= endFreq);
		IPanelLog.d(TAG, "isMainFrequencyUnder isMain=" + isMain);
		return isMain;
	}

	public void setManualAllFrequencyTransportStream() {
		FrequencyInfo fi = new FrequencyInfo(NetworkInterface.DELIVERY_CABLE);
		long nowFreq = startFreq;
		do {
			if (ignoreFreqs != null && ignoreFreqs.contains(nowFreq))
				continue;
			IPanelLog.i(TAG, "setManualAllFrequencyTransportStream nowFreq=" + nowFreq);
			if (fi != null && mainFreqInfo.getFrequency() != nowFreq) {
				SiTransportStream sits = siNetwork.addTransportStream();
				fi.setFrequency(nowFreq);
				fi.setParameter(FrequencyInfo.MODULATION, modulation);
				fi.setParameter(FrequencyInfo.SYMBOL_RATE, symbol);
				SiCabelFrequencyInfo sifi = setSiFrequencyInfo(fi);
				sits.frequency_info = sifi;
				IPanelLog.d(TAG, "setManualAllFrequencyTransportStream nowfreq=" + nowFreq);
			}
		} while ((nowFreq = getNextFreqency(nowFreq)) <= endFreq);

		first = false;
		netMap.buildTransportStream(siNetwork);
		if (isMainFrequencyUnder())
			netMap.addTransportStream2Head(curTransportStream);
	}

	protected long getNextFreqency(long current) {
		return current + FREQ_STEP;
	}

	public void setAllFrequencyTransportStream() {
		FrequencyInfo fi = null;
		long nowFreq = startFreq;
		do {
			if (ignoreFreqs != null && ignoreFreqs.contains(nowFreq))
				continue;
			fi = new FrequencyInfo(NetworkInterface.DELIVERY_CABLE);
			@SuppressWarnings("unused")
			TransportStream ts = curTransportStream;
			if (fi != null) {
				ts = netMap.createTransportStream(fi);
				SiTransportStream sits = siNetwork.addTransportStream();
				fi.setFrequency(nowFreq);
				fi.setParameter(FrequencyInfo.MODULATION, modulation);
				fi.setParameter(FrequencyInfo.SYMBOL_RATE, symbol);
				SiCabelFrequencyInfo sifi = setSiFrequencyInfo(fi);
				sits.frequency_info = sifi;
				IPanelLog.d(TAG, "setAllFrequencyTransportStream nowfreq=" + nowFreq);
			}
		} while ((nowFreq = getNextFreqency(nowFreq)) <= endFreq);
		netMap.buildTransportStream(siNetwork);
	}

	public FrequencyInfo setFrequencyInfo(long freq) {
		FrequencyInfo fi = null;
		synchronized (mutex) {
			fi = new FrequencyInfo(NetworkInterface.DELIVERY_CABLE);
			fi.setParameter(FrequencyInfo.MODULATION, "qam64");
			fi.setParameter(FrequencyInfo.SYMBOL_RATE, 6875000);
			fi.setFrequency(freq);
		}
		return fi;
	}

	public SiCabelFrequencyInfo setSiFrequencyInfo(FrequencyInfo info) {
		SiCabelFrequencyInfo siInfo = null;
		synchronized (mutex) {
			siInfo = new SiCabelFrequencyInfo();
			siInfo.frequency = info.getFrequency();
			siInfo.modulation = info.getParameter(FrequencyInfo.MODULATION);
			siInfo.symbol_rate = (Integer.valueOf(info.getParameter(FrequencyInfo.SYMBOL_RATE)));
		}
		return siInfo;
	}

	public boolean receiveTable(int bs, TableReceiver r, int program, int flags) {
		IPanelLog.d(TAG, "receiveTable program 111=" + program + ",flags=" + flags);
		return toolkit.addTableSearching(bs, r.pid, r.tid, r, program, flags);
	}

	public boolean receiveTable(int bs, TableReceiver r, int program) {
		IPanelLog.d(TAG, "receiveTable program 222=" + program);
		return toolkit.addTableSearching(bs, r.pid, r.tid, r, program);
	}

	public void stop() {
		synchronized (mutex) {
			if (running) {
				running = false;
				notifyStop(true);
				toolkit.stopFreqSearch();
				release();
			}
		}
	}

	public void stop(boolean flag) {
		synchronized (mutex) {
			running = false;
			notifyStop(flag);
			toolkit.stopFreqSearch();
			release();
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

	private List<Long> ignoreFreqs;

	public void setIgnoreFreqs(List<Long> ignoreFreqs) {
		this.ignoreFreqs = ignoreFreqs;
	}

	private boolean adjustStartFreq() {
		if (ignoreFreqs == null)
			return true;
		long freq = startFreq;
		do {
			if (!ignoreFreqs.contains(freq)) {
				startFreq = freq;
				return true;
			}
		} while ((freq = getNextFreqency(freq)) <= endFreq);
		return false;
	}
}