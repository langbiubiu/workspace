package com.ipanel.join.chongqing.live.play;

import ipaneltv.dvbsi.EIT;
import ipaneltv.toolkit.ASSERT;
import ipaneltv.toolkit.IPanelLog;
import ipaneltv.toolkit.Section;
import ipaneltv.toolkit.SectionBuffer;
import ipaneltv.toolkit.TimerFormater;
import ipaneltv.toolkit.db.DatabaseObjectification.ChannelKey;
import ipaneltv.toolkit.db.DatabaseObjectification.Program;
import ipaneltv.toolkit.dvb.DvbConst;
import ipaneltv.toolkit.dvb.DvbObjectification;
import ipaneltv.toolkit.dvb.DvbObjectification.SiEITEvents;
import ipaneltv.toolkit.dvb.DvbSiTable;

import java.util.HashMap;
import java.util.LinkedList;

import com.ipanel.join.chongqing.live.play.AllPFDataGetter.PresentAndFollow;

import android.content.Context;
import android.net.telecast.SectionFilter;
import android.net.telecast.StreamObserver;
import android.net.telecast.TransportManager;
import android.os.Handler;
import android.os.HandlerThread;
import android.util.Log;


public class OtherPFDataGetter {
	public static final String TAG = OtherPFDataGetter.class.getSimpleName();
	Context context;
	TransportManager manager;
	StreamObserver observer;
	String uuid;
	Handler procHandler;
	HandlerThread thread = new HandlerThread("thread_other");
	Object mutex = new Object();
	TransprotStreamOther ts;
	HashMap<ChannelKey, PresentAndFollow> map = new HashMap<ChannelKey, PresentAndFollow>();
	LinkedList<SectionBuffer> sbPool = new LinkedList<SectionBuffer>();
	int sbPoolOutSideSize = 0;
	ChannelKey focusChannelOther = null;
	private DvbObjectification mObjectf = null;

	public OtherPFDataGetter(Context context, String uuid, int flags) {
		mObjectf = new DvbObjectification();
		this.context = context;
		this.uuid = uuid;
		IPanelLog.d(TAG, "PFDataGetter 111");
		thread.start();
		IPanelLog.d(TAG, "PFDataGetter 223");
		procHandler = new Handler(thread.getLooper());
		IPanelLog.d(TAG, "PFDataGetter 333");
		manager = TransportManager.getInstance(context);
		observer = manager.createObserver(uuid);
		observer.setStreamStateListener(new listener());
		observer.queryStreamState();
		IPanelLog.d(TAG, "PFDataGetter 444");
	}

	/**
	 * 设置解析时所用的编码格式
	 * 
	 * @param eocod
	 *            一般为gbk或者utf-8
	 */
	public void setEncoding(String eocod) {
		mObjectf.setBouquetNameEncoding(eocod);
		mObjectf.setEventNameEncoding(eocod);
		mObjectf.setNetworkNameEncoding(eocod);
		mObjectf.setServiceNameEncoding(eocod);
		mObjectf.setServiceProviderNameEncoding(eocod);
	}

	/**
	 * 获取pf信息
	 * 
	 * @param freq
	 *            频点
	 * @param pro
	 *            频道号
	 * @param flags
	 *            默认为0
	 */
	public void getPresentAndFollow(final long freq, final int pro, final int flags) {
		postProcOther(new Runnable() {
			@Override
			public void run() {
				Log.d(TAG, "getPresentAndFollow freq = " + freq + ";pro = " + pro);
				ChannelKey key = ChannelKey.obten(freq, pro);
				PresentAndFollow pf = map.get(key);
				if (pf == null) {
					pf = new PresentAndFollow();
					pf.setChannelKey(key);
					map.put(key, pf);
				} else {
					Program program = pf.getPresentProgram();
					long currentTime = System.currentTimeMillis();
					if (program != null) {
						long startTime = program.getStart();
						long endTime = program.getEnd();
						if (startTime < currentTime && endTime > currentTime) {
							Log.d(TAG, "getPresentAndFollow pf = " + pf);
							onProgramsFound(pf, flags);
							return;
						}
					}
				}
				if (ts == null) {
					ts = new TransprotStreamOther();
				}
				ts.trySrart(freq);
				focusChannelOther = ChannelKey.obten(freq, pro);
			}
		});
	}

	/**
	 * 子类可重写以拿到pf数据，该方法为主线程调用 可直接更新界面。
	 * 
	 * @param pf
	 *            pf信息
	 */
	protected void onProgramsFound(PresentAndFollow pf, int flags) {

	}

	/**
	 * 子类可以覆写以改变收取数据的超时时间
	 * 
	 * @return 值
	 */
	protected int getSectinRetrieveTimeout() {
		return 5000;
	}

	/**
	 * 子类可覆写
	 * 
	 * @return
	 */
	protected int getFilterMaskByte() {
		return 0xff;
	}

	/**
	 * 可以子类覆写以改变最大缓冲区数量
	 * 
	 * @return
	 */
	protected int getMaxSectionBufferNumber() {
		return 400;
	}

	/**
	 * 可以子类覆写以改变常驻缓冲区数量
	 * 
	 * @return
	 */
	protected int getResidentSectionBufferNumber() {
		return 5;
	}

	protected void postProcOther(Runnable r) {
		postProcOther(r, false);
	}

	protected void postProcOther(Runnable r, boolean head) {
		if (head)
			procHandler.postAtFrontOfQueue(r);
		else
			procHandler.post(r);
	}

	SectionBuffer obtenSectionBuffer() {
		SectionBuffer ret = null;
		try {
			synchronized (sbPool) {
				if (sbPool.size() > 0)
					return ret = sbPool.pop();
				if (sbPoolOutSideSize >= getMaxSectionBufferNumber())
					return null;
			}
			return ret = SectionBuffer.createSectionBuffer(4096);
		} finally {
			if (ret != null)
				sbPoolOutSideSize++;
		}
	}

	void recyleSectionBuffer(SectionBuffer sb) {
		ASSERT.assertTrue(sb != null);
		synchronized (sbPool) {
			if (sbPool.size() >= getResidentSectionBufferNumber()) {
				sb.release();
			} else {
				sbPool.push(sb);
			}
			sbPoolOutSideSize--;
		}
	}

	class listener implements StreamObserver.StreamStateListener {

		@Override
		public void onStreamPresent(StreamObserver o, final long freq, int size, int prevSize) {
			IPanelLog.d(TAG, "onStreamPresent freq = " + freq);
			postProcOther(new Runnable() {

				@Override
				public void run() {
					if (ts == null) {
						ts = new TransprotStreamOther();
					}
					ts.setEnable(true);
					ts.setFrequency(freq);
				}
			});
		}

		@Override
		public void onStreamAbsent(StreamObserver o, final long freq) {
			IPanelLog.d(TAG, "onStreamAbsent freq = " + freq);
			postProcOther(new Runnable() {

				@Override
				public void run() {
					if (ts == null) {
						ts = new TransprotStreamOther();
					}
					ts.setEnable(false);
					ts.setFrequency(-1);
				}
			});
		}
	}

	class TransprotStreamOther implements SectionFilter.SectionDisposeListener {
		long freq = -1;
		long cFreq = -1;
		boolean enable = false;
		boolean filtering = false;
		SectionFilter filter;
		DvbSiTable table;
		HashMap<ChannelKey, PresentAndFollow> tsmap = null;

		public TransprotStreamOther() {
		}

		public void setEnable(boolean b) {
			enable = b;
		}

		public void setFrequency(long f) {
			cFreq = f;
			if (focusChannelOther != null && focusChannelOther.getFrequency() == cFreq) {
				Log.d(TAG, "setFrequency f = "+ f);
				start(f);
			}
		}

		private synchronized boolean start(long f) {
			if (!filtering || freq != f) {
				if (filter == null) {
					filter = manager.createFilter(uuid, 4 * 1024 * 1024);
					if (filter == null) {
						return false;
					}
					filter.setFrequency(f);
					filter.setAcceptionMode(SectionFilter.ACCEPT_UPDATED);
					filter.setSectionDisposeListener(this);
					filter.setTimeout(getSectinRetrieveTimeout());
				} else {
					filter.stop();
					filter.setFrequency(f);
				}
				freq = f;
				byte[] coef = new byte[] { (byte) DvbConst.TID_EIT_OTHER_PF };
				byte[] mask = new byte[] { (byte) getFilterMaskByte() };
				byte[] excl = new byte[] { (byte) 0x00 };
				table = new DvbSiTable();
				tsmap = new HashMap<ChannelKey, PresentAndFollow>();
				filtering = filter.start(DvbConst.PID_EIT, coef, mask, excl, 1);
			}
			return filtering;
		}

		public void trySrart(long f) {
			Log.d(TAG, "trySrart f = " + f + ";cFreq = " + cFreq);
			if (enable && cFreq == f) {
				start(f);
			}
		}

		public synchronized void stop() {
			if (filtering) {
				table = null;
				filtering = false;
				tsmap = null;
				freq = -1;
				filter.stop();
			}
		}

		@Override
		public void onStreamLost(SectionFilter f) {
			postProcOther(new Runnable() {

				@Override
				public void run() {
					stop();
				}
			});
		}

		@Override
		public void onReceiveTimeout(SectionFilter f) {
			postProcOther(new Runnable() {

				@Override
				public void run() {
					stop();
				}
			});
		}

		@Override
		public void onSectionRetrieved(SectionFilter f, int len) {
			final SectionBuffer sb = obtenSectionBuffer();
			if (sb == null) {
				IPanelLog.w(TAG, "SectionBuffer insufficient ,data drop!");
				return;
			}
			sb.copyFrom(f);
			postProcOther(new Runnable() {

				@Override
				public void run() {
					procSection(sb);
				}
			});
		}

		synchronized void procSection(SectionBuffer sb) {
			Section s = new Section(sb);
			IPanelLog.i(TAG, "onSectionRetrieved pf start");
			try {
				int j = 0;
				boolean full = false;
				if (table == null || tsmap == null
						|| (j = table.addSections(s, EIT.service_id(s))) == -1) {
					return;
				} else if (j == -2) {
					if (table.isReady()) {
						Log.d(TAG, "procSection full");
						full = true;
					} else {
						return;
					}
				}
				DvbObjectification.SiEITEvents se = mObjectf.parseEITEvents(s, null);
				if (se == null ? true : se.event == null) {
					IPanelLog.d(TAG, "OnPfInfoLisener se=" + se + ",se.event=" + se.event);
					return;
				}

				IPanelLog.d(TAG, "pn = " + se.service_id + "sn =" + s.section_number());
				PresentAndFollow pf = tsmap.get(ChannelKey.obten(freq, se.service_id));
				if (pf == null) {
					pf = new PresentAndFollow();
					pf.setChannelKey(ChannelKey.obten(freq, se.service_id));
					tsmap.put(ChannelKey.obten(freq, se.service_id), pf);
				}

				IPanelLog.d(TAG, "onSectionRetrieved event.size()=" + se.event.size());
				for (int i = 0; i < se.event.size(); i++) {
					SiEITEvents.Event sss = se.event.get(i);
					IPanelLog.d(TAG, "pn=" + se.service_id + ";start_time=" + sss.start_time
							+ ";duration = " + sss.duration + ";event_name = " + sss.event_name);

					long start_time = TimerFormater.rfc3339tolong(sss.start_time);
					long duration = formatDuration(sss.duration);
					long end_time = start_time + duration;
					long now = System.currentTimeMillis();
					IPanelLog.d(TAG, "serviceid 1=" + se.service_id + ",start_time=" + start_time
							+ ",end_time=" + end_time + ";now = " + now);
					if (s.section_number() == 0) { // present
						Program present = new Program();
						present.setName(sss.event_name);
						present.setStart(start_time);
						present.setEnd(end_time);
						pf.setPresentProgram(present);
					} else if (s.section_number() == 1) { // follow
						Program follow = new Program();
						follow.setName(sss.event_name);
						follow.setStart(start_time);
						follow.setEnd(end_time);
						pf.setFollowProgram(follow);
					}
				}
				IPanelLog.d(TAG, " focusChannelOther 1 = " + focusChannelOther);
				if (focusChannelOther != null) {
					if (table.isFull(focusChannelOther.getProgram())) {
						try {
							IPanelLog.d(TAG, "noticyPF receive full focusChannelOther.getProgram()="
									+ focusChannelOther.getProgram());
							PresentAndFollow programs = tsmap.get(focusChannelOther);
							if (programs != null) {
								onProgramsFound(programs, 0);
								focusChannelOther = null;
							}
						} catch (Exception e) {
							IPanelLog.d(TAG, "noticyPF failed e=" + e);
							e.printStackTrace();
						}
					}
				}

				if (full) {
					map.putAll(tsmap);
					tsmap.clear();
					stop();
				}
			} catch (Exception e) {
				Log.e(TAG, "procSection e = " + e.getMessage());
			} finally {
				recyleSectionBuffer(sb);
			}
		}
	}

	public int getSparseKey(long freq) {
		return (int) freq / 1000;
	}

	public long formatDuration(long duration) {
		int flag = 1;
		long j = 0;
		String s = Long.toHexString(duration);
		int i = s.length();
		while (i > 0) {
			if (flag > 3600) {
				break;
			}
			String s1 = s.substring(i - 2 > 0 ? i - 2 : 0, i);
			j = j + Integer.parseInt(s1) * flag;
			i = i - 2;
			flag = flag * 60;
		}
		j = j * 1000;
		return j;
	}

	@Override
	protected void finalize() throws Throwable {
		if (thread != null) {
			thread.getLooper().quit();
			thread = null;
		}
		super.finalize();
	}

}
