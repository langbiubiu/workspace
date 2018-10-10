package ipaneltv.toolkit.dvb;

import ipaneltv.dvbsi.EIT;
import ipaneltv.toolkit.IPanelLog;
import ipaneltv.toolkit.Section;
import ipaneltv.toolkit.SectionBuffer;

import java.util.HashSet;
import java.util.LinkedList;
import java.util.Timer;
import java.util.TimerTask;
import java.util.UUID;

import android.net.telecast.FrequencyInfo;
import android.net.telecast.SectionFilter;
import android.net.telecast.TransportManager;

public class DvbSiEventReceiveToolkit {
	private static final String TAG = DvbSiEventReceiveToolkit.class.getSimpleName();
	private final String uuid;
	private TransportManager tsManager;
	private long freq = 0;
	Timer timer = new Timer();
	boolean started = false;
	boolean startable = false;
	private HashSet<SiEventReceiveTask> tasks = new HashSet<SiEventReceiveTask>();
	private long tableFullReceivedTimeout = 2 * 60 * 1000;
	private RetrieveSearchListener rsl;
	private Object mutex = new Object();

	public static DvbSiEventReceiveToolkit createInstance(String netiUUID) {
		netiUUID = UUID.fromString(netiUUID).toString();
		return new DvbSiEventReceiveToolkit(netiUUID);
	}

	DvbSiEventReceiveToolkit(String uuid) {
		this.uuid = uuid;
	}

	public String getNetworkUUID() {
		return uuid;
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

	public TransportManager getTransportManager() {
		return tsManager;
	}

	public boolean start(long frequency) {
		synchronized (TAG) {
			if (!started) {
				started = true;
				freq = frequency;
				return true;
			}
			return false;
		}
	}

	public boolean isFreqUnderSearch(long frequency) {
		synchronized (TAG) {
			IPanelLog.d(TAG, "started = " + started + ";frequency = " + frequency + ";freq = " + freq);
			if (started && frequency == freq) {
				return true;
			}
			return false;
		}
	}

	/**
	 * 设置搜索监听器
	 * 
	 * @param l
	 *            监听对象
	 */
	public void setRetrieveSearchListener(RetrieveSearchListener l) {
		rsl = l;
	}

	// 停止所有的filter
	public void stop(boolean flag) {
		boolean stopped = false;
		IPanelLog.d(TAG, "stop in");
		synchronized (tasks) {
			if (started) {
				started = false;
				for (SiEventReceiveTask task : tasks) {
					try {
						task.stop();
					} catch (Exception e) {
						e.printStackTrace();
					}
				}
				tasks.clear();
				stopped = true;
			}
		}
		IPanelLog.d(TAG, "stop end");
		if (stopped) {
			RetrieveSearchListener l = rsl;
			if (l != null) {
				l.onSearchStopped(flag);
			}
		}
	}

	/**
	 * 设置表收取完成超时时间
	 * 
	 * @param millis
	 *            毫秒
	 */
	public void setTableFullReceivedTimeout(long millis) {
		if (millis <= 0)
			tableFullReceivedTimeout = 0;
		else
			tableFullReceivedTimeout = millis;
	}

	/**
	 * 频率搜索监听器
	 */
	public static interface RetrieveSearchListener {
		/** 当搜索开始 */
		void onSearchStopped(boolean flag);

		/** 当搜索需要显示消息 */
		void onSearchMessage(FrequencyInfo fi, String msg);

		/** 当搜索错误 */
		void onSearchError(FrequencyInfo fi, String msg);
	}

	// section监听接口
	public static interface OnRetrieveStateListener {
		boolean onSectionRetrieved(int pid, int tableid, Section s, boolean finished);

		void onTimeout(int pid, int tableid);

		void onReceiveFailed(int pid, int tableid);
	}

	// 调用filter对象启动filter
	public boolean addRetrieveTask(int timeOut, int pid, int tableId,
			OnRetrieveStateListener listener) {
		IPanelLog.d(TAG, "addRetrieveTask");
		synchronized (tasks) {
			SiEventReceiveTask task = new SiEventReceiveTask(timeOut, pid, tableId, listener);
			if (task.start()) {
				IPanelLog.d(TAG, "addRetrieveTask start success");
				tasks.add(task);
				if (tableFullReceivedTimeout > 0)
					timer.schedule(task, tableFullReceivedTimeout);
				return true;
			}
			return false;
		}
	}

	// 调用filter对象启动filter
	public boolean addPFTask(int timeOut, int pid, int tableId, OnRetrieveStateListener listener) {
		IPanelLog.d(TAG, "addPFTask");
		synchronized (tasks) {
			SiEventReceiveTask task = new SiEventReceiveTask(timeOut, pid, tableId, listener);
			if (task.start()) {
				IPanelLog.d(TAG, "addPFTask start success");
				tasks.add(task);
				if (tableFullReceivedTimeout > 0)
					timer.schedule(task, tableFullReceivedTimeout);
				return true;
			}
			return false;
		}
	}

	void removeTableTask(SiEventReceiveTask task) {
		boolean stopit = false;
		synchronized (tasks) {
			task.stop();
			tasks.remove(task);
			task.cancel();
			stopit = (tasks.size() == 0);
		}
		if (stopit)
			stop(true);
	}

	class SiEventReceiveTask extends TimerTask implements SectionFilter.SectionDisposeListener {
		byte[] coef = new byte[] { 0, 0, 0, 0 };
		byte[] mask = new byte[] { (byte) 0xFF, 0, 0, 0 };
		byte[] excl = new byte[] { 0, 0, 0, 0 };
		private int pid = -1;
		int tableId = 0;
		int timeOut = 0;
		SectionFilter f = null;
		OnRetrieveStateListener lis;
		DvbSiTable table = new DvbSiTable();
		private boolean running = false;

		SiEventReceiveTask(int timeOut, int pid, int tableid, OnRetrieveStateListener lis) {
			IPanelLog.d(TAG, "SiEventReceiveTask timeOut = " + timeOut + ";pid = " + pid + ";tableid = "
					+ tableid);
			this.pid = pid;
			this.tableId = tableid;
			this.timeOut = timeOut;
			coef[0] = (byte) tableid;
			this.lis = lis;
			IPanelLog.d(TAG, "SiEventReceiveTask end");
		}

		boolean openFilter() {
			if (f == null) {
				if ((f = tsManager.createFilter(uuid, 128 * 1024)) == null)
					return false;

				IPanelLog.d(TAG, "openFilter freq = " + freq + "; timeOut = " + timeOut);
				f.setFrequency(freq);
				f.setAcceptionMode(SectionFilter.ACCEPT_UPDATED);
				f.setCARequired(false);
				f.setTimeout(timeOut);
				f.setSectionDisposeListener(this);

				return true;
			}
			return false;
		}

		public void release() {
			IPanelLog.d(TAG, "release in");
			if (f != null) {
				f.release();
				f = null;
			}
			IPanelLog.d(TAG, "release end");
		}

		public boolean start() {
			try {
				if (openFilter()) {
					IPanelLog.d(TAG, "pid = " + pid + ";coef = " + coef[0] + ";mask = " + mask[0]
							+ ";excl = " + excl[0]);
					running = f.start(pid, coef, mask, excl, 1);
				}
			} finally {
				if (!running)
					release();
			}
			return running;
		}

		public void stop() {
			IPanelLog.d(TAG, "stop2 in");
			running = false;
			table = null;
			super.cancel();
			IPanelLog.d(TAG, "stop2 in 222");
			release();
			IPanelLog.d(TAG, "stop2 end");
		}

		@Override
		public void onStreamLost(SectionFilter f) {
			IPanelLog.d(TAG, "onStreamLost in");
			boolean removeit = false;
			synchronized (f) {
				if (started && running) {
					try {
						lis.onReceiveFailed(f.getStreamPID(), tableId);
					} catch (Exception e) {
						e.printStackTrace();
					} finally {
						removeit = true;
					}
				}
			}
			if (removeit)
				removeTableTask(this);
		}

		@Override
		public void onReceiveTimeout(SectionFilter f) {
			IPanelLog.d(TAG, "onReceiveTimeout +tableId = " + tableId);
			boolean removeit = false;
			synchronized (f) {
				if (started && running) {
					try {
						lis.onTimeout(f.getStreamPID(), tableId);
					} catch (Exception e) {
						e.printStackTrace();
					} finally {
						removeit = true;
					}
				}
			}
			if (removeit)
				removeTableTask(this);
		}

		public void onTableFullReceivedTimeout() {
			IPanelLog.d(TAG, "onTableFullReceivedTimeout");
			boolean removeit = false;
			synchronized (f) {
				if (started && running) {
					try {
						lis.onTimeout(f.getStreamPID(), tableId);
					} catch (Exception e) {
					} finally {
						removeit = true;
					}
				}
			}
			if (removeit)
				removeTableTask(this);
		}

		@Override
		public void onSectionRetrieved(SectionFilter f, int len) {
			IPanelLog.d(TAG, "onSectionRetrieved  in started = " + started + ";running = " + running);
			boolean removeit = false;
			boolean full = false;
			synchronized (f) {
				if (started && running) {
					IPanelLog.d(TAG, "--------------------------1111111111111");
					Section section = getSection();
					section.getSectionBuffer().copyFrom(f);
					section.reset();
					IPanelLog.d(TAG, "onSectionRetrieved  section = " + section
							+ ";section.getSectionBuffer() = " + section.getSectionBuffer());
					if (table == null ? false
							: table.addSections(section, EIT.service_id(section)) < 0) {
						IPanelLog.e(TAG, " ----------");
						return;
					}
					if (table != null && table.isReady()) {
						full = true;
						IPanelLog.d(TAG, "onSectionRetrieved full = " + full + ",pid=" + f.getStreamPID());
					}
					try {
						lis.onSectionRetrieved(f.getStreamPID(), tableId, section, full);
					} catch (Exception e) {
						e.printStackTrace();
					} finally {
						removeit = full;
					}
				}
			}
			IPanelLog.d(TAG, "onSectionRetrieved removeit = " + removeit + ";tableId = " + tableId);
			if (tableId != DvbConst.TID_EIT_ACTUAL_PF) {
				if (removeit)
					removeTableTask(this);
			}
			IPanelLog.d(TAG, "onSectionRetrieved end");
		}

		@Override
		public void run() {
			onTableFullReceivedTimeout();
		}
	}
	
	private LinkedList<Section> freedSection = new LinkedList<Section>();
	private LinkedList<Section> workedSection = new LinkedList<Section>();

	public Section getSection() {
		synchronized (mutex) {
			IPanelLog.d(TAG, "getSection freedSection.size()=" + freedSection.size());
			if (freedSection.isEmpty()) {
				SectionBuffer sb = SectionBuffer.createSectionBuffer(4096);
				Section s = new Section(sb);
				workedSection.add(s);
				return s;
			} else {
				Section s = freedSection.removeFirst();
				workedSection.add(s);
				return s;
			}
		}
	}

	public boolean FreeSection(Section s) {
		IPanelLog.d(TAG, "FreeSection s = " + s);
		synchronized (mutex) {
			if (workedSection.remove(s)) {
				IPanelLog.d(TAG, "FreeSection remove s = " + s);
				s.reset();
				freedSection.add(s);
				return true;
			}
		}
		return false;
	}

	public void releaseSections() {
		synchronized (mutex) {
			while (!freedSection.isEmpty()) {
				Section s = freedSection.removeFirst();
				s.getSectionBuffer().release();
			}
			while (!workedSection.isEmpty()) {
				Section s = workedSection.removeFirst();
				s.getSectionBuffer().release();
			}
			IPanelLog.d(TAG, "releaseSections freedSection.size()=" + freedSection.size()
					+ ";workedSection.size()=" + workedSection.size());
		}
	}

	public int getTaskNum() {
		synchronized (tasks) {
			return tasks.size();
		}
	}
}
