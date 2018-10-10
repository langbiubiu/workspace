package ipaneltv.toolkit.dvb;

import ipaneltv.toolkit.Section;
import ipaneltv.toolkit.SectionBuffer;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.UUID;

import android.net.telecast.SectionFilter;
import android.net.telecast.TransportManager;
import android.util.SparseArray;

public class DvbMonitorToolkit {
	static final String TAG = DvbMonitorToolkit.class.getSimpleName();

	public static DvbMonitorToolkit createInstance(String netuuid) {
		return new DvbMonitorToolkit(netuuid);
	}

	private HashMap<Long, FreqMonitor> freqs = new HashMap<Long, FreqMonitor>();
	private TransportManager tsManager;
	private String uuid;

	public static interface TableMonitorListener {
		void onSectionGot(Section sb);
	}

	/**
	 * 设置传输管理对象
	 * 
	 * @param manager
	 *            对象
	 */
	public void setTransportManager(TransportManager manager) {
		this.tsManager = manager;
	}

	public FreqMonitor addFreqMonitor(long freq) {
		synchronized (freqs) {
			if (!freqs.containsKey(freq)) {
				FreqMonitor fm = new FreqMonitor(freq);
				freqs.put(freq, fm);
				return fm;
			}
		}
		return null;
	}

	public FreqMonitor getFreqMonitor(long freq) {
		synchronized (freqs) {
			return freqs.get(freq);
		}
	}

	public List<Long> getFrequencies() {
		synchronized (freqs) {
			List<Long> ret = new ArrayList<Long>();
			for (Long l : freqs.keySet()) {
				ret.add(l);
			}
			return ret;
		}
	}

	public void removeFreqMonitor(long freq) {
		synchronized (freqs) {
			FreqMonitor fm = freqs.remove(freq);
			if (fm != null) {
				try {
					fm.stop();
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		}
	}

	public boolean startAllFreqnencies() {
		synchronized (freqs) {
			int n = 0;
			if (freqs.size() > 0) {
				for (Long l : freqs.keySet()) {
					FreqMonitor fm = freqs.get(l);
					n += fm.start();
				}
			}
			if (n > 0)
				return true;
			return false;
		}
	}

	public void stopAllFreqnencies() {
		synchronized (freqs) {
			if (freqs.size() > 0) {
				for (Long l : freqs.keySet()) {
					FreqMonitor fm = freqs.get(l);
					fm.stop();
				}
			}
		}
	}

	public class FreqMonitor {

		private SparseArray<TableMonitor> tables = new SparseArray<TableMonitor>();
		private long freq;
		private boolean fMonitoring = false;
		private Object fMutex = new Object();

		public FreqMonitor(long freq) {
			this.freq = freq;
		}

		public long getFrequency() {
			return freq;
		}

		public int start() {
			int n = 0;
			synchronized (fMutex) {
				if (!fMonitoring) {
					fMonitoring = true;
					int size = tables.size();
					for (int i = 0; i < size; i++) {
						TableMonitor tb = tables.valueAt(i);
						n += tb.start() ? 1 : 0;
					}
				}
			}
			return n;
		}

		public void stop() {
			synchronized (fMutex) {
				if (fMonitoring) {
					fMonitoring = false;
					int size = tables.size();
					for (int i = 0; i < size; i++) {
						TableMonitor tb = tables.valueAt(i);
						tb.stop();
					}
				}
			}
		}

		int getTableKey(int pid, int tableId) {
			if (tableId < 0 || tableId > 256 || pid < 0 || pid > 8192)
				throw new IllegalArgumentException();
			return (pid << 16) | (tableId & 0xffff);
		}

		public int tableMonitorSize() {
			return tables.size();
		}

		public TableMonitor addTableMonitor(int bs, int pid, int tableId) {
			int key = getTableKey(pid, tableId);
			synchronized (tables) {
				if (tables.indexOfKey(key) < 0) {
					TableMonitor tm = new TableMonitor(bs, pid, tableId);
					tables.append(key, tm);
					return tm;
				}
			}
			return null;
		}

		public TableMonitor getTableMonitor(int pid, int tableId) {
			int key = getTableKey(pid, tableId);
			synchronized (tables) {
				return tables.get(key);
			}
		}

		public TableMonitor[] getTableMonitors() {
			synchronized (tables) {
				int size = tables.size();
				TableMonitor ret[] = new TableMonitor[size];
				for (int i = 0; i < size; i++)
					ret[i] = tables.valueAt(i);
				return ret;
			}
		}

		public boolean removeTableMonitor(int pid, int tableId) {
			int key = getTableKey(pid, tableId);
			synchronized (tables) {
				TableMonitor tm = tables.get(key);
				if (tm != null) {
					tables.remove(key);
					if (fMonitoring)
						tm.stop();
					return true;
				}
			}
			return false;
		}

		public class TableMonitor implements SectionFilter.SectionDisposeListener {
			private TableMonitorListener tml;
			private SectionFilter f;// 监控类型filter,创建时指定类型
			private SectionBuffer sbuffer = null;
			private boolean tMonitoring = false;
			private Section section = null;
			private int pid = -1, delay = 0;
			private Object tMutex = new Object();
			private int tableid;

			public TableMonitor(int bs, int pid, int tableId) {
				this.pid = pid;
				this.tableid = tableId;
				sbuffer = SectionBuffer.createSectionBuffer(bs);
				section = new Section(sbuffer);
			}

			public FreqMonitor getFreqMonitor() {
				return FreqMonitor.this;
			}

			public int getTableId() {
				return tableid;
			}

			public int getPID() {
				return pid;
			}

			public void setTableMonitorListener(TableMonitorListener l) {
				tml = l;
			}

			public void setDelay(int millis) {
				delay = millis;
			}

			boolean startInner() {
				synchronized (tMutex) {
					if (tMonitoring) {
						// start freq monitor first!
						return false;
					}
					try {
						if ((f = tsManager.createMonitorFilter(uuid, 0)) != null) {
							if (delay > 0)
								f.setTimeout(delay);
							f.setFrequency(freq);
							f.setAcceptionMode(SectionFilter.ACCEPT_UPDATED);
							f.setCARequired(false);
							f.setSectionDisposeListener(this);
							if (f.start(pid, tableid))
								tMonitoring = true;
						}
					} finally {
						if (!tMonitoring && f != null) {
							f.release();
							f = null;
						}
					}
					return tMonitoring;
				}
			}

			void stopInner() {
				synchronized (tMutex) {
					if (tMonitoring) {
						tMonitoring = false;
						f.release();
						sbuffer.release();
						sbuffer = null;
						f = null;
					}
				}
			}

			public boolean start() {
				synchronized (fMutex) {
					if (fMonitoring)
						return startInner();
				}
				return false;
			}

			public void stop() {
				synchronized (fMutex) {
					if (fMonitoring)
						stopInner();
				}
			}

			public boolean isMonitoring() {
				return tMonitoring;
			}

			@Override
			public void onSectionRetrieved(SectionFilter f, int len) {
				TableMonitorListener l = tml;
				if (l != null) {
					sbuffer.copyFrom(f);
					l.onSectionGot(section);
				}
			}

			@Override
			public void onStreamLost(SectionFilter f) {
			}

			@Override
			public void onReceiveTimeout(SectionFilter f) {
			}

		}
	}

	DvbMonitorToolkit(String uuid) {
		this.uuid = UUID.fromString(uuid).toString();
	}
}
