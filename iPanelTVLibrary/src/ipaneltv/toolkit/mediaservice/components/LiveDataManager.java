package ipaneltv.toolkit.mediaservice.components;

import ipaneltv.toolkit.IPanelLog;
import ipaneltv.toolkit.ProgramMoniterFilter;
import ipaneltv.toolkit.db.DatabaseObjectification.ChannelKey;
import ipaneltv.toolkit.db.DatabaseObjectification.Ecm;
import ipaneltv.toolkit.db.DatabaseObjectification.Frequency;
import ipaneltv.toolkit.mediaservice.LiveNetworkApplication;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map.Entry;
import java.util.Set;
import java.util.Vector;

import android.net.telecast.FrequencyInfo;
import android.net.telecast.ProgramInfo;
import android.util.Log;
import android.util.SparseArray;
import android.util.SparseIntArray;

public abstract class LiveDataManager extends LiveNetworkApplication.AppComponent {
	static final String TAG = LiveDataManager.class.getSimpleName();
	private List<LiveDataListener> listeners = new ArrayList<LiveDataListener>();
	private final Object liveInfoMutex = new Object();
	private SparseArray<FrequencyInfo> freqs = new SparseArray<FrequencyInfo>();
	private HashMap<ChannelKey, ProgramInfo> programs = new HashMap<ChannelKey, ProgramInfo>();
	private HashMap<ChannelKey, Ecm[]> ecms = new HashMap<ChannelKey, Ecm[]>();
	private SparseArray<SparseIntArray> streamPids = new SparseArray<SparseIntArray>();
	private SparseArray<ChannelKey> keys = new SparseArray<ChannelKey>();
	private SparseIntArray tsids = new SparseIntArray();
	private boolean loaded = false;

	@SuppressWarnings("rawtypes")
	public LiveDataManager(LiveNetworkApplication app) {
		super(app);
		// TODO 监控锁频状态?
	}

	protected abstract void onLoad();

	public abstract void observeProgramGuide(ChannelKey key, long focusTime);

	public synchronized void ensureOnload() {
		if (!loaded) {
			loaded = true;
			onLoad();
		}
	}

	public void addLiveDataListener(LiveDataListener lis) {
		ensureOnload();
		synchronized (listeners) {
			if (!listeners.contains(lis))
				listeners.add(lis);
		}
	}

	public void removeLiveDataListener(LiveDataListener lis) {
		synchronized (listeners) {
			listeners.remove(lis);
		}
	}

	protected void notifyLoadFinished() {
		int mask = LiveDataListener.MASK_ECM | LiveDataListener.MASK_FREQ
				| LiveDataListener.MASK_STREAM;
		notifyLiveInfoUpdated(mask);
	}

	protected void notifyLiveInfoUpdated(int mask) {
		synchronized (listeners) {
			for (LiveDataListener h : listeners) {
				h.onLiveInfoUpdated(mask);
			}
		}
	}

	protected void updateFrequencies(SparseArray<Frequency> fs) {
		synchronized (liveInfoMutex) {
			freqs.clear();
			int n = fs.size();
			for (int i = 0; i < n; i++) {
				Frequency f = fs.valueAt(i);
				try {
					FrequencyInfo fi = FrequencyInfo.fromString(f.getTuneParams());
					freqs.put(f.getSparseKey(), fi);
					tsids.append(f.getSparseKey(), f.getTsid());
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		}
	}

	protected void updatePrograms(HashMap<ChannelKey, ProgramInfo> ss) {
		synchronized (liveInfoMutex) {
			programs.clear();
			programs.putAll(ss);
			if (ss != null) {
				Set<ChannelKey> set = ss.keySet();
				keys.clear();
				for (ChannelKey channelKey : set) {
					keys.append(channelKey.getProgram(), channelKey);
				}
			}
		}
	}

	protected void addStreamPids(HashMap<ChannelKey, ProgramInfo> ss) {
		synchronized (liveInfoMutex) {
			streamPids.clear();
			Iterator<Entry<ChannelKey, ProgramInfo>> iter = ss.entrySet().iterator();
			while (iter.hasNext()) {
				Entry<ChannelKey, ProgramInfo> entry = iter.next();
				ChannelKey key = entry.getKey();
				ProgramInfo val = entry.getValue();
				int freq = Frequency.getSparseKey(key.getFrequency());
				SparseIntArray array = streamPids.get(freq);
				if (array == null) {
					array = new SparseIntArray();
					streamPids.append(freq, array);
				}
				array.append(val.getAudioPID(), val.getProgramNumber());
			}
		}
	}

	protected void updateEcms(HashMap<ChannelKey, List<Ecm>> ss) {
		synchronized (liveInfoMutex) {
			ecms.clear();
			for (Entry<ChannelKey, List<Ecm>> e : ss.entrySet()) {
				List<Ecm> elist = e.getValue();
				Ecm[] ea = new Ecm[elist.size()];
				elist.toArray(ea);
				ecms.put(e.getKey(), ea);
			}
		}
	}

	public void updateEcm(ChannelKey key, Vector<ProgramMoniterFilter.Stream.Ecm> es) {
		synchronized (liveInfoMutex) {
			if (es != null && es.size() > 0) {
				Ecm[] e = new Ecm[es.size()];
				for (int i = 0; i < es.size(); i++) {
					Ecm ecm = new Ecm();
					ecm.setChannelKey(key);
					ecm.setCaSystemId(es.get(i).ca_system_id);
					ecm.setEcmpid(es.get(i).ecm_pid);
					ecm.setStreamPId(es.get(i).stream_pid);
					e[i] = ecm;
				}
				ecms.put(key, e);
			}
		}
	}

	public void updateStreamPids(ChannelKey key, ProgramInfo info) {
		Log.d(TAG, "updateStreamPids 11 key = " + key + ";info = " + info);
		synchronized (liveInfoMutex) {
			programs.put(key, info);
		}
	}

	public Ecm[] getEcmInfo(ChannelKey key) {
		synchronized (liveInfoMutex) {
			return ecms.get(key);
		}
	}

	public FrequencyInfo getFrequencyInfo(long freq) {
		synchronized (liveInfoMutex) {
			return freqs.get(Frequency.getSparseKey(freq));
		}
	}

	public int getTsid(long freq) {
		synchronized (liveInfoMutex) {
			return tsids.get(Frequency.getSparseKey(freq));
		}
	}

	public ChannelKey getChannelKeyByPn(int pn) {
		synchronized (liveInfoMutex) {
			return keys.get(pn);
		}
	}

	/**
	 * 通过freq以及音频pid获取到对应的prmgranNumber。
	 * 上海ocn项目3.0tv标签播放需求，前端没有配置频道号，但是艾迪德的解扰必须要有频道号。
	 * 子类需要重写updatePrograms方法并在其中调用addStreamPids才能生效。
	 * 
	 * @param freq
	 *            频点
	 * @param apid
	 *            音频pid
	 * @return
	 */
	public int getProgramNum(long freq, int apid) {
		synchronized (liveInfoMutex) {
			SparseIntArray array = streamPids.get(Frequency.getSparseKey(freq));
			if (array != null) {
				return array.get(apid);
			}
			return -1;
		}
	}

	public ProgramInfo getProgramInfo(ChannelKey key) {
		synchronized (liveInfoMutex) {
			return programs.get(key);
		}
	}

	public int getChannelStreamCoEcmPid(ChannelKey key, int spid, int casysids[]) {
		IPanelLog.d(TAG, "getChannelStreamCoEcmPid spid = " + spid + ";key = " + key
				+ ";casysids[0] = " + (casysids == null ? null : casysids[0]));
		if (spid < 0)
			return -1;
		synchronized (liveInfoMutex) {
			Ecm[] ecm = ecms.get(key);
			if (ecm != null) {
				for (int i = 0; i < ecm.length; i++) {
					Ecm e = ecm[i];
					// if (e.getStreamPId() == spid) {
					for (int j = 0; j < casysids.length; j++) {
						if (e.getCaSystemId() == casysids[j])
							return e.getEcmPId();
					}
					// }
				}
			}
		}
		return -1;
	}

	public int getChannelStreamCoPmtPid(ChannelKey key) {
		IPanelLog.d(TAG, "getChannelStreamCoPmtPid 2 key = " + key);
		synchronized (liveInfoMutex) {
			Ecm[] ecm = ecms.get(key);
			if (ecm != null && ecm.length > 0) {
				return ecm[0].getPmtpid();
			}
		}
		return -1;
	}

	public boolean isCaRequired(ChannelKey key) {
		synchronized (liveInfoMutex) {
			Ecm[] ecm = ecms.get(key);
			IPanelLog.d(TAG, "isCaRequired key = " + key + "; ecm = " + ecm);
			if (ecm != null) {
				return true;
			}
			return false;
		}
	}

	public boolean isCaRequired2(ChannelKey key) {
		synchronized (liveInfoMutex) {
			Ecm[] ecm = ecms.get(key);
			IPanelLog.d(TAG, "isCaRequired key = " + key + "; ecm = " + ecm);
			if (ecm != null) {
				return true;
			}
			return false;
		}
	}

	public static interface LiveDataListener {
		public static final int MASK_FREQ = 0x01, MASK_STREAM = 0x02, MASK_ECM = 0x04;

		void onLiveInfoUpdated(int mask);
	}
}
