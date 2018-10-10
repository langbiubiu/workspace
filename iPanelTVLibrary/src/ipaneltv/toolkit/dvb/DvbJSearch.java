package ipaneltv.toolkit.dvb;

import ipaneltv.toolkit.IPanelLog;
import ipaneltv.toolkit.dvb.DvbNetworkMapping.TransportStream;
import ipaneltv.toolkit.dvb.DvbObjectification.SiCabelFrequencyInfo;
import ipaneltv.toolkit.dvb.DvbObjectification.SiNetwork;
import ipaneltv.toolkit.dvb.DvbObjectification.SiTransportStream;
import ipaneltv.toolkit.dvb.DvbSearchToolkit.FreqSearchListener;
import ipaneltv.toolkit.dvb.DvbSearchToolkit.TableReceiveTerminitor;
import ipaneltv.toolkit.mediaservice.components.L10n;

import java.util.List;

import android.content.Context;
import android.net.telecast.FrequencyInfo;
import android.net.telecast.JStreamSelector;
import android.net.telecast.NetworkInterface;
import android.net.telecast.SignalStatus;
import android.net.telecast.TransportManager;

/**
 * 
 * @description <p>
 *              针对JStreamSelector的搜索，以注入JStreamSelector的频点为单位
 */
public class DvbJSearch {
	static final String TAG = DvbJSearch.class.getSimpleName();
	private Object mutex = new Object();
	private boolean prepared = false, running = false;
	DvbSearchToolkit toolkit;
	TransportManager tsManager;
	JSearchListener asl;
	DvbNetworkMapping netMap;
	TransportStream curTransportStream;
	JStreamSelector jSelector = null;
	private List<Long> freqs;
	FrequencyInfo fi;
	SiNetwork siNetwork;

	public static interface JSearchListener {
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

	public static DvbJSearch createInstance(Context context, String SEARCH_UUID, List<Long> freqs) {
		try {
			return new DvbJSearch(context, SEARCH_UUID, freqs);
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

	public void setJSearchListener(JSearchListener l) {
		asl = l;
	}

	DvbJSearch(Context context, String SEARCH_UUID, List<Long> freqs) {
		this.freqs = freqs;
		if ((toolkit = DvbSearchToolkit.createInstance(SEARCH_UUID)) == null)
			throw new RuntimeException("create DvbJSearchToolkit failed");
		if ((tsManager = TransportManager.getInstance(context)) == null)
			throw new RuntimeException("create TransportManager failed");
		toolkit.setTransportManager(tsManager);
		toolkit.setFreqSearchListener(freqListener);
	}

	FreqSearchListener freqListener = new FreqSearchListener() {

		@Override
		public void onSearchStopped(FrequencyInfo fi) {
			IPanelLog.d(TAG, "onSearchStopped running=" + running);
			asl.onSearchStopped();
			if (running) {
				if (!start()) {
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
			// TODO Auto-generated method stub

		}

		@Override
		public void onSearchError(FrequencyInfo fi, String msg) {
			// TODO Auto-generated method stub

		}
	};

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
			IPanelLog.i(TAG, " netMap 2= " + netMap);
			asl.onSearchFinished(isSuccess);
		} catch (Exception e) {
			e.printStackTrace();
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

	void clear() {

	}

	public boolean prepare(int DELIVERY) {
		int interfaceid = 1001;
		NetworkInterface ni = toolkit.getDefaultInterfaceId(DELIVERY);
		if (ni != null ? ni.getId() > 0 : false) {
			interfaceid = ni.getId();
		}
		IPanelLog.d(TAG, "prepare interface id = " + interfaceid);
		
		synchronized (mutex) {
			try {
				if (!prepared) {
					jSelector = new JStreamSelector();
					toolkit.setStreamSelector(jSelector);
					toolkit.setFreqFullSearchedTimeout(2 * 60 * 1000);// 2m
					toolkit.setTableFullReceivedTimeout(30 * 1000);// 30s
					siNetwork = new SiNetwork();
					netMap = new DvbNetworkMapping();
					netMap.setSiNetwork(siNetwork);
					setAllFrequencyTransportStream();
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
			if (jSelector != null) {
				jSelector.release();
				jSelector = null;
			}
			prepared = false;
		}
	}

	void startFreqSearching(FrequencyInfo fInfo) {
		// fInfo.setFrequency(0 - fInfo.getFrequency());
		if (!toolkit.startFreqSearch(fInfo)) {
			IPanelLog.d(TAG, "startFreqSearch failed");
			notifyMessage(L10n.code_913);
			stop(true);
			return;
		}
		freqs.remove(fInfo.getFrequency());
		notifyMessage("开始搜索频点:" + fInfo.getFrequency());
		onTableSearch(fInfo);
		IPanelLog.d(TAG, "startFreqSearching continue search");
	}

	public boolean start() {
		if (fi == null) {
			fi = new FrequencyInfo(NetworkInterface.DELIVERY_CABLE);
			fi.setFrequency(0);
			fi.setParameter(FrequencyInfo.MODULATION, "qam64");
			fi.setParameter(FrequencyInfo.SYMBOL_RATE, 6875000);
		}
		IPanelLog.i(TAG, "Jsearch start " + freqs.size() + "  prepared =" + prepared + "running = "
				+ running);
		synchronized (mutex) {
			if (prepared) {
				IPanelLog.i(TAG, " netMap = " + netMap);
				if (freqs.size() <= 0) {
					stop(running);
					return running;
				}
				running = true;
				IPanelLog.i(TAG, "freqs size = " + freqs.size());
				Long freq = freqs.get(0);

				fi.setFrequency(freq);
				curTransportStream = netMap.findTransportStreamByFreq(Math.abs(freq));
				IPanelLog.i(TAG, " curTransportStream = " + curTransportStream);
				toolkit.setCurrentTransportStream(curTransportStream);
				startFreqSearching(fi);
				return true;
			}
			return false;
		}
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

	public void setAllFrequencyTransportStream() {
		FrequencyInfo fi = new FrequencyInfo(NetworkInterface.DELIVERY_CABLE);
		for (Long freq : freqs) {
			SiTransportStream sits = siNetwork.addTransportStream();
			fi.setFrequency(Math.abs(freq));
			fi.setParameter(FrequencyInfo.MODULATION, "qam64");
			fi.setParameter(FrequencyInfo.SYMBOL_RATE, 6875000);
			SiCabelFrequencyInfo sifi = setSiFrequencyInfo(fi);
			sits.frequency_info = sifi;

		}
		netMap.buildTransportStream(siNetwork);
	}

	public boolean receiveTable(int bs, TableReceiver r, int program) {
		return toolkit.addTableSearching(bs, r.pid, r.tid, r, program);
	}

	public boolean receiveTable(int bs, TableReceiver r, int program, int flags) {
		return toolkit.addTableSearching(bs, r.pid, r.tid, r, program, flags);
	}

	public void stop(boolean flag) {
		IPanelLog.i(TAG, "stop flag = " + flag);
		synchronized (mutex) {
			if (running) {
				running = false;
				notifyStop(flag);
				toolkit.stopFreqSearch();
				release();
			}
		}
	}

	public void pause() {

	}

	public void resume() {

	}

}
