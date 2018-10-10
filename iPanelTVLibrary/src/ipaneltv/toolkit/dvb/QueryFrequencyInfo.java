package ipaneltv.toolkit.dvb;

import ipaneltv.toolkit.IPanelLog;

import java.util.List;

import android.content.Context;
import android.net.telecast.FrequencyInfo;
import android.net.telecast.NetworkInterface;
import android.net.telecast.SignalStatus;
import android.net.telecast.StreamSelector;
import android.net.telecast.TransportManager;
import android.os.Handler;
import android.os.HandlerThread;

/**
 * 查询频点信息
 */
public class QueryFrequencyInfo {
	static final String TAG = QueryFrequencyInfo.class.getSimpleName();

	private TransportManager tsManager = null;
	private StreamSelector selector = null;
	private QueryFreqListener lsn;
	private Object mutex = new Object();
	private Context mContext = null;
	private String uuid = null;
	private boolean prepared = false;
	private FrequencyInfo fi;
	private HandlerThread queryProc = new HandlerThread("query-finfo");
	private Handler handler;

	public static interface QueryFreqListener {
		void onSignalStatus(FrequencyInfo fi, SignalStatus ss);
	}

	public static QueryFrequencyInfo createInstance(Context context, String SEARCH_UUID) {
		return new QueryFrequencyInfo(context, SEARCH_UUID);
	}

	private NetworkInterface getDefaultInterfaceId(int dtype) {
		List<NetworkInterface> nis = tsManager.getNetworkInterfaces();
		NetworkInterface ni;
		for (int i = 0; i < nis.size(); i++) {
			if ((ni = nis.get(i)).getDevliveryType() == dtype)
				return ni;
		}
		return null;
	}

	private boolean prepare() {
		synchronized (mutex) {
			if (!prepared) {
				if ((tsManager = TransportManager.getInstance(mContext)) == null)
					throw new RuntimeException("create TransportManager failed");

				int interfaceid = 1001;
				NetworkInterface ni = getDefaultInterfaceId(NetworkInterface.DELIVERY_CABLE);
				if (ni != null ? ni.getId() >= 0 : false) {
					interfaceid = ni.getId();
				}
				IPanelLog.d(TAG, "prepare interface id = " + interfaceid);
				if ((selector = tsManager.createSelector(interfaceid)) == null) {
					IPanelLog.d(TAG, "prepare failed: can't create selector for interface:"
							+ interfaceid);
					return false;
				}

				selector.setNetworkUUID(uuid);
				selector.setSelectionStateListener(new Selector());
				prepared = true;
			}
			return prepared;
		}
	}

	private boolean lockFrequency(long freq) {
		IPanelLog.d(TAG, "lockFrequency freq = " + freq);
		FrequencyInfo fi = new FrequencyInfo(NetworkInterface.DELIVERY_CABLE);
		fi.setFrequency(freq);
		fi.setParameter(FrequencyInfo.SYMBOL_RATE, 6875000);
		fi.setParameter(FrequencyInfo.MODULATION, "qam64");
		IPanelLog.d(TAG, "lockFrequency fi=" + fi.toString());

		if (!selector.select(fi, 0)) {
			IPanelLog.e(TAG, "lockFrequency failed");
			return false;
		}
		this.fi = fi;
		return true;
	}

	private boolean lockFrequency(long freq,int symbol_rate,String modulation) {
		IPanelLog.d(TAG, "lockFrequency freq = " + freq);
		FrequencyInfo fi = new FrequencyInfo(NetworkInterface.DELIVERY_CABLE);
		fi.setFrequency(freq);
		fi.setParameter(FrequencyInfo.SYMBOL_RATE, symbol_rate);
		fi.setParameter(FrequencyInfo.MODULATION, modulation);
		IPanelLog.d(TAG, "lockFrequency fi=" + fi.toString());

		if (!selector.select(fi, 0)) {
			IPanelLog.e(TAG, "lockFrequency failed");
			return false;
		}
		this.fi = fi;
		return true;
	}
	
	QueryFrequencyInfo(Context context, String uuid) {
		this.mContext = context;
		this.uuid = uuid;
		queryProc.start();
		handler = new Handler(queryProc.getLooper());
	}

	public boolean setLockFrequency(long freq) {
		if (!prepare())
			return false;
		return lockFrequency(freq);
	}
	
	public boolean setLockFrequency(long freq,int symbol_rate,String modulation){
		if (!prepare())
			return false;
		return lockFrequency(freq,symbol_rate,modulation);
	}

	public boolean setLockFrequecy(FrequencyInfo fi) {
		if (!prepare())
			return false;

		if (!selector.select(fi, 0)) {
			IPanelLog.e(TAG, "setLockFrequecy failed,fi=" + fi);
			return false;
		}
		this.fi = fi;
		return true;
	}

	public void stop() {
		if (prepared) {
			prepared = false;
			if (selector != null) {
				selector.setSelectionStateListener(null);
				selector.release();
				selector = null;
			}
		}
	}

	public void release() {
		IPanelLog.d(TAG, "release selector=" + selector);
		prepared = false;
		if (selector != null) {
			selector.setSelectionStateListener(null);
			selector.release();
			selector = null;
		}
		if (queryProc != null) {
			queryProc.quit();
			queryProc = null;
		}
	}

	public void setListener(QueryFreqListener lis) {
		this.lsn = lis;
	}

	class Selector implements StreamSelector.SelectionStateListener {

		@Override
		public void onSelectStart(StreamSelector selector) {
			// TODO Auto-generated method stub

		}

		@Override
		public void onSelectSuccess(final StreamSelector selector) {
			handler.post(new Runnable() {
				@Override
				public void run() {
					QueryFreqListener l = lsn;
					if (l != null) {
						SignalStatus ss = selector.getSignalStatus();
						if (ss != null)
							l.onSignalStatus(fi, ss);
						stop();
					}
				}
			});
		}

		@Override
		public void onSelectFailed(final StreamSelector selector) {
			handler.post(new Runnable() {
				@Override
				public void run() {
					QueryFreqListener l = lsn;
					if (l != null) {
						l.onSignalStatus(fi, null);
					}
					stop();
				}
			});
		}

		@Override
		public void onSelectionLost(StreamSelector selector) {
		}

		@Override
		public void onSelectionResumed(StreamSelector selector) {
		}
	}
}
