package ipaneltv.toolkit.dvb;

import ipaneltv.dvbsi.EIT;
import ipaneltv.toolkit.IPanelLog;
import ipaneltv.toolkit.Section;
import ipaneltv.toolkit.SectionBuffer;

import java.util.UUID;

import android.content.Context;
import android.net.telecast.SectionFilter;
import android.net.telecast.TransportManager;

public class PFSearchToolkit {
	static final String TAG = PFSearchToolkit.class.getSimpleName();
	private TransportManager tsManager = null;
	private String netid;
	private long freq = 0;
	protected Context context = null;
	private Filter mFilter;
	private DvbSiTable mTable;
	UUID uuid;
	boolean started = false;
	boolean startable = false;

	public static PFSearchToolkit createInstance(Context ctx, String netid, TransportManager ts) {
		return new PFSearchToolkit(ctx, netid, ts);
	}

	public TransportManager getTransportManager() {
		return tsManager;
	}

	PFSearchToolkit(Context ctx, String netid, TransportManager ts) {
		this.context = ctx;
		this.netid = netid;
		uuid = UUID.fromString(netid);
		tsManager = ts;
		mFilter = new Filter(0);
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
			IPanelLog.d(TAG, "started = " + started + ";frequency = " + frequency + ";freq = "
					+ freq);
			if (started && frequency == freq) {
				return true;
			}
			return false;
		}
	}

	// 调用filter对象启动filter
	public boolean addRetrieveTask(OnRetrieveStateListener l, int pid, int tableid, int timeout) {
		if (l == null || pid < 0 || tableid < 0 || tableid > 512)
			throw new IllegalArgumentException();
		synchronized (TAG) {
			if (!started)
				throw new IllegalStateException("start first!");
			if (mFilter == null) {
				IPanelLog.d(TAG, "there has no more filters to proc the task,"
						+ " or you should change the max filter size!");
				return false;
			}
			IPanelLog.d(TAG, "addRetrieveTask pid = " + pid + "tableid = " + tableid);
			if (mFilter.start(l, pid, tableid, timeout)) {
				mTable = new DvbSiTable();
				return true;
			}
			mTable = null;
			return false;
		}
	}

	// 停止所有的filter
	public void stopFilter() {
		IPanelLog.d(TAG, "stopFilter in");
		synchronized (TAG) {
			IPanelLog.d(TAG, "stopFilter in111");
			started = false;
			mTable = null;
			mFilter.stop();
			IPanelLog.d(TAG, "stopFilter in222");
		}
	}

	// 删除所有的filter
	public void closeFilter() {
		IPanelLog.d(TAG, "closeFilter in");
		synchronized (TAG) {
			IPanelLog.d(TAG, "closeFilter");
			started = false;
			mFilter.close();
			mTable = null;
		}
	}

	public boolean isFull(int programNum) {
		if (mTable != null) {
			return mTable.isFull(programNum);
		}
		return false;
	}

	public boolean isReady() {
		if (mTable != null) {
			return mTable.isReady();
		}
		return false;
	}

	// section监听接口
	public static interface OnRetrieveStateListener {
		boolean onSectionRetrieved(int pid, int tableid, Section s, boolean full);

		void onTimeout(int pid, int tableid);
	}

	// filter管理对象
	class Filter implements SectionFilter.SectionDisposeListener {
		boolean runing = false;
		SectionFilter filter;
		OnRetrieveStateListener l;
		SectionBuffer sbuffer = null;
		Section section;
		int tableId = 0;

		Filter(int bufferSize) {
			try {
				filter = tsManager.createFilter(netid, bufferSize);
				sbuffer = SectionBuffer.createSectionBuffer(4096);
				section = new Section(sbuffer);
			} catch (RuntimeException e) {
				e.printStackTrace();
			}
			if (filter == null) {
				throw new RuntimeException();
			}
			filter.setAcceptionMode(SectionFilter.ACCEPT_UPDATED);
			filter.setCARequired(false);// is default no need to set
		}

		// 启动filter
		public boolean start(OnRetrieveStateListener l, int pid, int tableid, int timeout) {
			this.l = l;
			this.tableId = tableid;
			byte[] coef = new byte[] { (byte) tableid, 0, 0, 0 };
			byte[] mask = new byte[] { (byte) 0xFF, 0, 0, 0 };
			byte[] excl = new byte[] { 0, 0, 0, 0 };
			synchronized (filter) {
				filter.setSectionDisposeListener(this);
				filter.setTimeout(timeout);
				filter.setFrequency(freq);
				if (filter.start(pid, coef, mask, excl, 4)) {
					runing = true;
					return true;
				}
				return false;
			}
		}

		// 停止filter
		public boolean stop() {
			if (runing) {
				l = null;
				IPanelLog.d(TAG, "stop in aaaa");
				runing = false;
				filter.stop();
				IPanelLog.d(TAG, "stop out aaaa");
				return true;
			}
			return false;
		}

		// 停止filter
		public boolean close() {
			IPanelLog.d(TAG, "close in");
			l = null;
			filter.release();
			sbuffer.release();
			runing = false;
			IPanelLog.d(TAG, "close out");
			return true;
		}

		@Override
		public void onStreamLost(SectionFilter f) {
			IPanelLog.d(TAG, "onStreamLost");
//			synchronized (TAG) {
//				started = false;
//				mTable = null;
//				stop();
//			}
		}

		@Override
		public void onReceiveTimeout(SectionFilter f) {
			IPanelLog.d(TAG, "onReceiveTimeout");
//			synchronized (TAG) {
//				if (l != null)
//					l.onTimeout(filter.getStreamPID(), tableId);
//				started = false;
//				mTable = null;
//				stop();
//			}
		}

		// section回调数据
		@Override
		public void onSectionRetrieved(SectionFilter f, int len) {
			IPanelLog.d(TAG, "onSectionRetrieved section len = " + len);
			boolean full = false;
			synchronized (filter) {
				if (started) {
					IPanelLog.d(TAG, "onSectionRetrieved 111");
					sbuffer.copyFrom(f);
					IPanelLog.d(TAG, "onSectionRetrieved 2222");
					section.reset();
					IPanelLog.d(TAG, "l=" + l);
					int i;
					if (mTable == null
							|| (i = mTable.addSections(section, EIT.service_id(section))) == -1) {
						return;
					} else if (i == -2) {
						if (mTable.isReady()) {
							full = true;
						} else {
							return;
						}
					}
					if (l != null)
						l.onSectionRetrieved(filter.getStreamPID(), tableId, section, full);
				}
			}
			IPanelLog.d(TAG, "onSectionRetrieved 3333");
			if (full) {
				IPanelLog.d(TAG, "isReady");
				started = false;
				mTable = null;
				stop();
				IPanelLog.d(TAG, "stop out");
			}
			IPanelLog.d(TAG, "onSectionRetrieved out");
		}
	}
}
