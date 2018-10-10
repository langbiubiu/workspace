package ipaneltv.toolkit.dvb;

import ipaneltv.toolkit.Section;
import ipaneltv.toolkit.SectionBuffer;

import java.util.UUID;

import android.content.Context;
import android.net.telecast.SectionFilter;
import android.net.telecast.SectionFilter.SectionMonitorListener;
import android.net.telecast.TransportManager;
import android.util.SparseArray;

/**
 * 用于全频点section数据监控
 */
public class DvbMonitorFilter implements SectionMonitorListener {
	static final String TAG = DvbMonitorFilter.class.getSimpleName();
	private MonitorListener tml;
	private SparseArray<TableMonitor> tables = new SparseArray<TableMonitor>();
	private boolean fMonitoring = false;
	private Object fMutex = new Object();
	private String uuid = null;
	private TransportManager tsManager = null;
	Context mContext = null;

	public DvbMonitorFilter createInstance(Context context, String netid) {
		return new DvbMonitorFilter(context, netid);
	}

	DvbMonitorFilter(Context context, String netid) {
		this.mContext = context;
		this.uuid = UUID.fromString(netid).toString();
		tsManager = TransportManager.getInstance(context);
	}

	public static interface MonitorListener {
		void onSectionGot(int pid, Section sb);

		void onSelectFrequency(long freqs[]);
	}

	/**
	 * 设置监控器
	 */
	public void setMonitorListener(MonitorListener l) {
		tml = l;
	}

	/**
	 * 加入需要监控的的参数
	 * 
	 * @param bs
	 *            section缓冲区大小
	 * @param pid
	 *            pid值
	 * @param tableId
	 *            tableid值
	 * @return
	 */
	public boolean addTableMonitor(int bs, int pid, int tableId) {
		int key = getTableKey(pid, tableId);
		synchronized (tables) {
			if (tables.indexOfKey(key) < 0) {
				TableMonitor tm = new TableMonitor(bs, pid, tableId);
				tables.append(key, tm);
				return true;
			}
		}
		return false;
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

	/**
	 * 启动监控
	 */
	public int start() {
		int n = 0;
		synchronized (fMutex) {
			if (!fMonitoring) {
				fMonitoring = true;
				int size = tables.size();
				for (int i = 0; i < size; i++) {
					TableMonitor tb = tables.valueAt(i);
					tb.setDelay(100);
					n += tb.start() ? 1 : 0;
				}
			}
		}
		return n;
	}

	/**
	 * 停止监控
	 */
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

	private int getTableKey(int pid, int tableId) {
		if (tableId < 0 || tableId > 256 || pid < 0 || pid > 8192)
			throw new IllegalArgumentException();
		return (pid << 16) | (tableId & 0xffff);
	}

	/**
	 * 监控表数目
	 */
	public int tableMonitorSize() {
		return tables.size();
	}

	private class TableMonitor implements SectionFilter.SectionDisposeListener {
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

//		public int getTableId() {
//			return tableid;
//		}
//
//		public int getPID() {
//			return pid;
//		}

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
					int flags = SectionFilter.FLAG_MONITOR_MODE
							| SectionFilter.FLAG_MONITOR_ALL_FREQ;
					if ((f = tsManager.createFilter(uuid, 0, flags)) != null) {
						if (delay > 0)
							f.setTimeout(delay);
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

//		public boolean isMonitoring() {
//			return tMonitoring;
//		}

		@Override
		public void onSectionRetrieved(SectionFilter f, int len) {
			MonitorListener l = tml;
			if (l != null) {
				sbuffer.copyFrom(f);
				l.onSectionGot(pid, section);
			}
		}

		@Override
		public void onStreamLost(SectionFilter f) {

		}

		@Override
		public void onReceiveTimeout(SectionFilter f) {
		}
	}

	@Override
	public int onSelectFrequency(long[] freqs) {
		MonitorListener l = tml;
		if (l != null) {
			l.onSelectFrequency(freqs);
		}
		return 0;
	}
}
