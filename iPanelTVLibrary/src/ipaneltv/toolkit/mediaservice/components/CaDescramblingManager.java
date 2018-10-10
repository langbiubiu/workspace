package ipaneltv.toolkit.mediaservice.components;

import ipaneltv.toolkit.ASSERT;
import ipaneltv.toolkit.IPanelLog;
import ipaneltv.toolkit.ca.CaCardSlot;
import ipaneltv.toolkit.ca.CaEnvironment;
import ipaneltv.toolkit.ca.CaModule;
import ipaneltv.toolkit.db.DatabaseObjectification.ChannelKey;
import ipaneltv.toolkit.media.ReserveStateInterface;
import ipaneltv.toolkit.mediaservice.LiveNetworkApplication;
import ipaneltv.toolkit.mediaservice.LiveNetworkApplication.AppComponent;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

import android.net.telecast.ca.StreamDescrambler;
import android.os.Handler;
import android.os.HandlerThread;
import android.util.SparseArray;
import android.util.SparseIntArray;

public class CaDescramblingManager extends AppComponent {
	static final String TAG = CaDescramblingManager.class.getSimpleName();
	private SparseArray<ProgramDescrambler> pds = new SparseArray<ProgramDescrambler>();
	private Object mutex = new Object();
	private HandlerThread procThread = new HandlerThread("cam-proc");
	private Handler procHandler = null;
	private int COUNTER = 1;
	private DescramblingScheduler scheduler;

	@SuppressWarnings("rawtypes")
	public CaDescramblingManager(LiveNetworkApplication app) {
		super(app);
	}

	final static int MSG_CLOSE = 1;

	protected synchronized void close() {
		synchronized (pds) {
			int n = pds.size();
			for (int i = 0; i < n; i++)
				pds.valueAt(i).close();
		}
		if (scheduler != null) {
			scheduler.close();
			procHandler.sendEmptyMessage(MSG_CLOSE);
		}
		scheduler = null;
	}

	void ensureScheduler() {
		if (scheduler != null)
			return;
		procThread.start();
		procHandler = new Handler(procThread.getLooper());
		scheduler = new DescramblingScheduler(getApp(), procHandler) {
			@Override
			protected void onCaCardState(int id, int v, int code, String str) {
				try {
					synchronized (mutex) {
						ProgramDescrambler pd = pds.get(id);
						IPanelLog.d(TAG, "onCaCardState id = " + id + ";pd = " + pd + ";str = "
								+ str);
						if (pd != null ? pd.matchVersion(v) : false)
							pd.cb.onCaCardState(code, str);
					}
				} catch (Exception e) {
					e.printStackTrace();
				}
			}

			@Override
			protected void onDescramblingState(int id, int v, int code, String err) {
				try {
					synchronized (mutex) {
						ProgramDescrambler pd = pds.get(id);
						IPanelLog.d(TAG, "onDescramblingState id = " + id + ";pd = " + pd
								+ ";err = " + err);
						if (pd != null ? pd.matchVersion(v) : false) {
							pd.cb.onDescramblingState(code, err);
						}
					}
				} catch (Exception e) {
					e.printStackTrace();
				}
			}

			@Override
			protected void onCaModuleDispatched(int id, int v, int camid) {
				try {
					synchronized (mutex) {
						IPanelLog.d(TAG, "onCaModuleDispatched id  = " + id);
						ProgramDescrambler pd = pds.get(id);
						IPanelLog.d(TAG, "onCaModuleDispatched id = " + id + ";pd = " + pd);
						if (pd != null ? pd.matchVersion(v) : false)
							pd.cb.onCaModuleDispatched(camid);
					}
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		};
	}

	public ProgramDescrambler createDescrambler(ProgramDescramberCallback callback) {
		synchronized (pds) {
			ensureScheduler();
			return new ProgramDescrambler(callback);
		}
	}

	synchronized final int nextCounter() {
		if (COUNTER == Integer.MAX_VALUE)
			throw new RuntimeException();
		return ++COUNTER;
	}

	public boolean queryCACardState() {
		if (scheduler != null) {
			scheduler.queryState();
			return true;
		}
		return false;
	}

	public class ProgramDescrambler implements ReserveStateInterface {
		private ProgramDescramberCallback cb;
		private int stateId = -1, version = 0;
		private boolean loosen = false, started = false;

		ProgramDescrambler(ProgramDescramberCallback callback) {
			cb = callback;
		}

		private final int nextVersion() {
			return ++version;
		}

		final boolean matchVersion(int v) {
			return version == v;
		}

		@Override
		public boolean reserve() {
			synchronized (pds) {
				ASSERT.assertTrue(cb != null);
				IPanelLog.d(TAG, "before reserve loosen = " + loosen + " stateId = " + stateId);
				if (loosen) {
					if (stateId > 0) {
						IPanelLog.d(TAG, "release camoudle");
						pds.remove(stateId);
						scheduler.releaseState(stateId);
						stateId = -1;
					}
					loosen = false;
				}
				int nid = nextCounter();
				scheduler.reserveState(nid);
				stateId = nid;
				pds.put(stateId, this);
				IPanelLog.d(TAG, "after reserve stateId = " + stateId);
				return stateId > 0;
			}
		}

		@Override
		public void loosen(boolean clearState) {
			synchronized (pds) {
				IPanelLog.d(TAG, "before loosen loosen = " + loosen + " stateid = " + stateId
						+ " clearState = " + clearState);
				if (!loosen && stateId > 0) {
					loosen = true;
					if (clearState) {
						IPanelLog.d(TAG, "release camoudle");
						scheduler.releaseState(stateId);
					} else {
						IPanelLog.d(TAG, "scheduler.breakableState");
						scheduler.breakableState(stateId, version);
					}
				}
			}
		}

		public void close() {
			synchronized (pds) {
				loosen = false;
				IPanelLog.d(TAG, "close loosen = " + loosen);
				if (stateId > 0) {
					pds.remove(stateId);
					scheduler.releaseState(stateId);
					stateId = -1;
				}
				cb = null;
			}
		}

		@Override
		public boolean isReserved() {
			return stateId > 0 && cb != null;
		}

		public boolean start(ChannelKey key, int a, int v) {
			IPanelLog.d(TAG, "start stateId = " + stateId);
			synchronized (pds) {
				IPanelLog.d(TAG, "start in");
				if (stateId > 0) {
					scheduler.startState(stateId, nextVersion(), key, new int[] { a, v });
					started = true;
				}
			}
			IPanelLog.d(TAG, "start out");
			return started;
		}

		public void stop() {
			IPanelLog.d(TAG, "stop stateId = " + stateId + ";started = " + started);
			synchronized (pds) {
				IPanelLog.d(TAG, "stop 111");
				if (started) {
					started = false;
					scheduler.stopState(stateId, nextVersion());
				}
			}
			IPanelLog.d(TAG, "stop out");
		}

		public void solveProblem() {
			if (stateId > 0)
				scheduler.solveStateUri(stateId, version);
		}

		public void enterCaApp(String uri) {
			if (stateId > 0) {
				scheduler.enterStateApp(stateId, version, uri);
			}
		}
	}

	public static interface ProgramDescramberCallback {
		void onCaModuleDispatched(int moduleId);

		void onDescramblingState(int code, String err);

		void onCaCardState(int code, String msg);
	}
}

abstract class DescramblingScheduler extends CaEnvironment implements
		LiveDataManager.LiveDataListener {
	static final String TAG = DescramblingScheduler.class.getSimpleName();
	@SuppressWarnings("rawtypes")
	LiveNetworkApplication app;
	private final Object schedulerMutex = new Object();
	private SparseArray<DescState> dispatch = new SparseArray<DescState>();
	private SparseArray<ModState> mods = new SparseArray<ModState>();
	private int rescheduleCounter = 0;
	private int cardState = CaCardSlot.STATE_ABSENT;

	@SuppressWarnings("rawtypes")
	DescramblingScheduler(LiveNetworkApplication app, Handler handler) {
		super(app, handler);
		this.app = app;
		queryState();
		app.getLiveDataManager().addLiveDataListener(this);
	}

	abstract void onCaCardState(int id, int v, int code, String str);

	abstract void onDescramblingState(int id, int v, int code, String err);

	abstract void onCaModuleDispatched(int id, int v, int camid);

	@Override
	public synchronized void close() {
		super.close();
		app.getLiveDataManager().removeLiveDataListener(this);
	}

	private void onCaModuleReschedule() {
		final int postCounter = ++rescheduleCounter;
		getHandler().post(new Runnable() {
			@Override
			public void run() {
				IPanelLog.d(TAG, "onCaModuleReschedule postCounter = " + postCounter
						+ ";rescheduleCounter = " + rescheduleCounter);
				if (postCounter == rescheduleCounter)
					procScheduleScan();
			}
		});
	}

	@Override
	protected void onModuleCardBind(CaCardSlot s, CaModule m) {
		synchronized (schedulerMutex) {
			IPanelLog.d(TAG, "call onModuleCardBind moduleid=" + m.getModuleId());
			ModState ms = mods.get(m.getModuleId());
			IPanelLog.d(TAG, "call onModuleCardBind ms=" + ms);
			if (ms == null) {
				int max = m.getMaxChannelSize(getCaManager(), 4);
				ms = new ModState(m.getModuleId(), max);
				IPanelLog.d(TAG, "ms.maxDescChannelSize = " + ms.maxDescChannelSize);
				ms.availableChannel = ms.maxDescChannelSize;
				if ((ms.casysids = m.getCaSystemIds()) == null)
					ms.casysids = getCaManager().getCAModuleCASystemIDs(m.getModuleId());
				mods.put(m.getModuleId(), ms);
			}
			ms.available = true;
		}
		cardState = CaCardSlot.STATE_VERIFIED;
		onCaModuleReschedule();
		onCaCardState(null);
	}

	@Override
	protected void onModuleCardUnbind(CaCardSlot s, CaModule m) {
		ModState ms = mods.get(m.getModuleId());
		if (ms != null)
			ms.available = false;
		onCaModuleReschedule();
		cardState = CaCardSlot.STATE_ABSENT;
		// onCaCardState(L10n.CAMOD_ERR_451);
	}

	@Override
	protected void onCaModuleRemove(CaModule m) {
		mods.remove(m.getModuleId());
		// onCaCardState(L10n.CAMOD_ERR_450);
	}

	@Override
	protected void onCaCardAbsent(CaCardSlot s) {
		onCaCardState(L10n.CARD_ERR_440);
		cardState = CaCardSlot.STATE_ABSENT;
	}

	@Override
	protected void onCaCardMuted(CaCardSlot s) {
		onCaCardState(L10n.CARD_ERR_441);
		cardState = CaCardSlot.STATE_MUTED;
	}

	@Override
	protected void onCaCardNoMatching(CaCardSlot s) {
		// onCaCardState(L10n.CARD_ERR_442);
	}

	@Override
	protected void onCaModulePresent(CaModule m) {
		super.onCaModulePresent(m);
		IPanelLog.d(TAG, "onCaModulePresent mouldId = " + m.getModuleId());
	}

	@Override
	protected void onCaModuleChange(CaModule m) {
		IPanelLog.d(TAG, "onCaModuleChange");
		super.onCaModuleChange(m);
		IPanelLog.d(TAG, "onCaModuleChange m.CaSystemId = " + m.getCaSystemIds()[0]);
		getHandler().post(new Runnable() {
			@Override
			public void run() {
				procEcmUpdatedScan();
			}
		});
	}

	@Override
	public void onLiveInfoUpdated(int mask) {
		IPanelLog.d(TAG, "onLiveInfoUpdated mask = " + mask);
		if ((mask & LiveDataManager.LiveDataListener.MASK_ECM) != 0) {
			getHandler().post(new Runnable() {
				@Override
				public void run() {
					procEcmUpdatedScan();
				}
			});
		}
	}

	private void onCaCardState(String str) {
		int code = L10n.getErrorCode(str);
		int n = dispatch.size();
		IPanelLog.d(TAG, "onCaCardState n = " + n);
		for (int i = 0; i < n; i++) {
			DescState d = dispatch.valueAt(i);
			IPanelLog.d(TAG, "onCaCardState str = " + str + ";d = " + d);
			if (d != null) {
				IPanelLog.d(TAG, "onCaCardState d.isStarted() = " + d.isStarted());
			}
			if (d != null) {
				if (d.getArgs() != null
						&& d.getLiveDataManager().isCaRequired(d.getArgs().getKey())) {
					onCaCardState(d.getId(), d.getVersion(), code, str);
				}
			}
		}
	}

	private void procEcmUpdatedScan() {
		int n = dispatch.size();
		for (int i = 0; i < n; i++) {
			DescState d = dispatch.valueAt(i);
			int resid = d.getResId();
			if (resid > 0) {
				ModState m = mods.get(resid);
				if (m != null ? m.available : false)
					d.checkEcmUpdated(m.casysids);
			}
		}
	}

	private LinkedList<DescState> getWaittingStates() {
		LinkedList<DescState> ret = new LinkedList<DescState>();
		int n = dispatch.size();
		for (int i = 0; i < n; i++) {
			DescState d = dispatch.valueAt(i);
			if (d.isWaitting())
				ret.add(d);
		}
		return ret;
	}

	private LinkedList<DescState> getWaittingBreakableStates() {
		LinkedList<DescState> ret = new LinkedList<DescState>();
		int n = dispatch.size();
		for (int i = 0; i < n; i++) {
			DescState d = dispatch.valueAt(i);
			if (d.isWaittingBreadable())
				ret.add(d);
		}
		return ret;
	}

	private LinkedList<DescState> getBreakableStates() {
		LinkedList<DescState> ret = new LinkedList<DescState>();
		int n = dispatch.size();
		for (int i = 0; i < n; i++) {
			DescState d = dispatch.valueAt(i);
			if (d.isBreakable())
				ret.add(d);
		}
		return ret;
	}

	private LinkedList<ModState> getLeisuredModules() {
		LinkedList<ModState> ret = new LinkedList<ModState>();
		int n = mods.size();
		IPanelLog.d(TAG, "getLeisuredModules  mods.size() = " + mods.size());
		for (int i = 0; i < n; i++) {
			ModState m = mods.valueAt(i);
			IPanelLog.d(TAG, "m.availableChannel = " + m.availableChannel + ";m.mid = " + m.mid
					+ ";m = " + m);
			if (m.available && m.availableChannel > 1)// 2个及以上.
				ret.add(m);
		}
		return ret;
	}

	// private void procScheduleScan() {
	// synchronized (schedulerMutex) {
	// // 第一趟检查收回资源或更改状态(因module变为无效引起)
	// int n = dispatch.size();
	// IPanelLog.d(TAG, "procScheduleScan step1 n = " + n);
	// List<DescState> dlist = new ArrayList<DescramblingScheduler.DescState>();
	// for (int i = 0; i < n; i++) {
	// DescState d = dispatch.valueAt(i);
	// int mid = d.getResId();
	// IPanelLog.d(TAG, "step1 mid = " + mid);
	// if (mid > 0) {
	// ModState m = mods.get(mid);
	// IPanelLog.d(TAG, "step1 mods.size = " + mods.size() + " m.id = " + mid);
	// IPanelLog.d(TAG, "step11 m = "+m);
	// if (d.isDropping()) {
	// d.setCaModuleId(-1, null);
	// if (m != null) {
	// IPanelLog.d(TAG, "procScheduleScan release camoudle");
	// m.availableChannel += 2;
	// ASSERT.assertTrue(m.availableChannel <= m.maxDescChannelSize);
	// IPanelLog.d(TAG, "m.availableChannel = " + m.availableChannel);
	// }
	// } else {
	// ASSERT.assertTrue(mid > 0);
	// IPanelLog.d(TAG, "step1  m.available = " +
	// m.available+";d.isBreakable() = "+
	// d.isBreakable());
	// if (m == null ? true : !m.available&&!d.isBreakable()) {
	// d.setCaModuleId(-1, null);
	// if (m != null) {
	// m.availableChannel += 2;
	// }
	// }
	// }
	// if (d.isClosed()) {
	// dlist.add(d);
	// }
	// }
	// }
	// for (DescState d : dlist) {
	// dispatch.remove(d.getResId());
	// }
	// // 第二趟分配module资源
	// LinkedList<DescState> user = getWaittingStates();
	// IPanelLog.d(TAG, "step2 user.size() = " + user.size());
	// if (user.size() == 0)
	// return;
	// LinkedList<ModState> res = getLeisuredModules();
	// DescState d = null;
	// IPanelLog.d(TAG, "procScheduleScan step2 res = " + res.size());
	// if (res.size() > 0) {
	// while ((d = user.peek()) != null) {
	// ModState m = null;
	// while ((m = res.peek()) != null) {
	// if (m.availableChannel >= 2)
	// break;
	// res.pop();
	// }
	// if (m == null)
	// break;
	// if (d.setCaModuleId(m.mid, m.casysids)) {
	// m.availableChannel -= 2;
	// // res.pop();
	// }
	// user.pop();
	// }
	// }
	//
	// // 第三趟抢占breakable的资源
	// IPanelLog.d(TAG, "step3 user.size = " + user.size());
	// if (user.size() == 0)
	// return;
	// LinkedList<DescState> brk = getBreakableStates();
	// if (brk.size() == 0)
	// return;
	// IPanelLog.d(TAG, "step3 brk.size = " + brk.size() + " user.size = " +
	// user.size());
	// while ((d = user.peek()) != null) {
	// DescState r = brk.peek();
	// if (r == null)
	// break;
	// if (doRace(d, r))
	// brk.pop();
	// user.pop();
	// }
	//
	// // 第四趟对于没有拿到资源的要予以通知
	// if(user.size() == 0){
	// return;
	// }
	// while ((d = user.pop()) != null) {
	// onDescramblingState(d.getResId(), d.getVersion(),
	// L10n.getErrorCode(L10n.CAMOD_ERR_452), L10n.CAMOD_ERR_452);
	// }
	// }
	// }

	private void procScheduleScan() {
		synchronized (schedulerMutex) {
			// 第一趟检查收回资源或更改状态(因module变为无效引起)
			int n = dispatch.size();
			IPanelLog.d(TAG, "procScheduleScan step1 n = " + n);
			List<DescState> dlist = new ArrayList<DescramblingScheduler.DescState>();
			for (int i = 0; i < n; i++) {
				DescState d = dispatch.valueAt(i);
				int mid = d.getResId();
				IPanelLog.d(TAG, "step1 mid = " + mid);
				if (mid > 0) {
					ModState m = mods.get(mid);
					IPanelLog.d(TAG, "step1 mods.size = " + mods.size() + " m.id = " + mid);
					IPanelLog.d(TAG, "step11 m = " + m);
					if (d.isDropping()) {
						d.setCaModuleId(-1, null);
						if (m != null) {
							IPanelLog.d(TAG, "procScheduleScan release camoudle");
							m.availableChannel += 2;
							ASSERT.assertTrue(m.availableChannel <= m.maxDescChannelSize);
							IPanelLog.d(TAG, "m.availableChannel = " + m.availableChannel);
						}
					} else {
						ASSERT.assertTrue(mid > 0);
						IPanelLog.d(TAG, "step1  m.available = " + m.available
								+ ";d.isBreakable() = " + d.isBreakable());
						if (m == null ? true : !m.available) {
							d.setCaModuleId(-1, null);
							if (m != null) {
								m.availableChannel += 2;
							}
						}
					}
				}
				if (d.isClosed()) {
					dlist.add(d);
				}
			}
			for (DescState d : dlist) {
				dispatch.remove(d.getId());
			}
			// 第二趟分配module资源，优先非breakable的
			LinkedList<DescState> user = getWaittingStates();
			IPanelLog.d(TAG, "step2 user.size() = " + user.size());
			if (user.size() > 0) {
				LinkedList<ModState> res = getLeisuredModules();
				DescState d = null;
				IPanelLog.d(TAG, "procScheduleScan step2 res = " + res.size());
				if (res.size() > 0) {
					while ((d = user.peek()) != null) {
						ModState m = null;
						while ((m = res.peek()) != null) {
							if (m.availableChannel >= 2)
								break;
							res.pop();
						}
						if (m == null)
							break;
						if (d.setCaModuleId(m.mid, m.casysids)) {
							m.availableChannel -= 2;
							// res.pop();
						}
						user.pop();
					}
				}
				// 第三趟抢占breakable的资源
				IPanelLog.d(TAG, "step3 user.size = " + user.size());
				if (user.size() > 0) {
					LinkedList<DescState> brk = getBreakableStates();
					IPanelLog.d(TAG,
							"step3 brk.size = " + brk.size() + " user.size = " + user.size());
					if (brk.size() > 0) {
						while ((d = user.peek()) != null) {
							DescState r = brk.peek();
							if (r == null)
								break;
							boolean b = false;
							if ((b = doRace(d, r)))
								brk.pop();
							user.pop();
							IPanelLog.d(TAG, "step3 b = " + b);
						}
					}
					IPanelLog.d(TAG, "step 4 user.size = " + user.size());
					// 第四趟对于没有拿到资源的要予以通知
					if (user.size() > 0) {
						while ((d = user.peek()) != null) {
							if (d.getArgs() != null
									&& d.getLiveDataManager().isCaRequired(d.getArgs().getKey())) {
								String err = null;
								if(cardState == CaCardSlot.STATE_MUTED){
									err = L10n.CARD_ERR_441;
								}else{
									err = L10n.CAMOD_ERR_452;
								}
								onDescramblingState(d.getId(), d.getVersion(),
										L10n.getErrorCode(err), err);
							}
							user.pop();
						}
						return;
					}
				}
			}
			// 第五趟 尝试给breakable的分配资源
			LinkedList<DescState> breakableuser = getWaittingBreakableStates();
			IPanelLog.d(TAG, "procScheduleScan step5 breakableuser = " + breakableuser.size());
			if (breakableuser.size() > 0) {
				LinkedList<ModState> resList = getLeisuredModules();
				DescState state = null;
				IPanelLog.d(TAG, "procScheduleScan step5 res = " + resList.size());
				if (resList.size() > 0) {
					while ((state = breakableuser.peek()) != null) {
						ModState m = null;
						while ((m = resList.peek()) != null) {
							IPanelLog.d(TAG, "procScheduleScan step5 m.availableChannel = "
									+ m.availableChannel);
							if (m.availableChannel >= 2)
								break;
							resList.pop();
						}
						if (m == null)
							break;
						IPanelLog.d(TAG, "procScheduleScan step5 m = " + m);
						if (state.setCaModuleId(m.mid, m.casysids)) {
							state.setBreakable(state.getVersion());
							m.availableChannel -= 2;
						}
						breakableuser.pop();
					}
				}
			}
		}
	}

	private boolean doRace(DescState d, DescState r) {
		int mid = r.getResId();
		IPanelLog.d(TAG, "doRace mid = " + mid);
		if (mid > 0) {
			ModState m = mods.get(r.getResId());
			IPanelLog.d(TAG, "m = " + m);
			if (m != null) {
				IPanelLog.d(TAG, "m.available = " + m.available);
			}
			if (m != null ? m.available : false) {
				r.setCaModuleId(-1, null);
				r.close();
				if (d.setCaModuleId(mid, m.casysids)) {
					return true;
				}
			}
		}
		return false;
	}

	static final int MSG_ST_RESERVE = 1;
	static final int MSG_ST_RELEASE = 2;
	static final int MSG_ST_START = 3;
	static final int MSG_ST_STOP = 4;
	static final int MSG_ST_BREAKABLE = 5;
	static final int MSG_ST_SOLVE = 6;
	static final int MSG_ST_ENTER = 7;

	void post(final int msg, final int id, final int v, final Object p1, final Object p2) {
		Runnable r = new Runnable() {
			@Override
			public void run() {
				boolean scan = false;
				try {
					IPanelLog.d(TAG, "handleMessage msg = " + msg + ";id = " + id + ";v = " + v);
					DescState ds = dispatch.get(id);
					if (msg == MSG_ST_RESERVE) {
						ASSERT.assertTrue(ds == null);
						IPanelLog.d(TAG, "MSG_ST_RESERVE");
						ds = new DescState(id);
						IPanelLog.d(TAG, "MSG_ST_RESERVE 22");
						dispatch.put(id, ds);
						scan = true;
					} else if (ds != null) {
						switch (msg) {
						case MSG_ST_RELEASE:
							IPanelLog.d(TAG, "MSG_ST_RELEASE");
							if (!ds.isClosed())
								scan = ds.close();
							break;
						case MSG_ST_BREAKABLE:
							IPanelLog.d(TAG, "MSG_ST_BREAKABLE");
							scan = ds.setBreakable(v);
							break;
						case MSG_ST_START: {
							IPanelLog.d(TAG, "handleMessage MSG_ST_START");
							int[] pids = (int[]) p2;
							scan = ds.start(v, (ChannelKey) p1, pids[0], pids.length > 1 ? pids[1]
									: -1);
							break;
						}
						case MSG_ST_STOP:
							scan = ds.stop(v);
							IPanelLog.d(TAG, "handleMessage stop msg = " + msg + ";id = " + id
									+ ";v = " + v);
							break;
						case MSG_ST_SOLVE:
							ds.solveUri(v);
							break;
						case MSG_ST_ENTER:
							ds.enterApp(v, (String) p1);
							break;
						default:
							break;
						}
					}
					IPanelLog.d(TAG, "handleMessage out msg = " + msg + ";id = " + id + ";v = " + v
							+ ";scan = " + scan);
				} catch (Exception e) {
					e.printStackTrace();
				}
				if (scan) {
					onCaModuleReschedule();
				}
			}
		};
		if (msg == MSG_ST_STOP || msg == MSG_ST_RELEASE) {
			IPanelLog.d(TAG, "postAtFrontOfQueue msg = " + msg);
			getHandler().postAtFrontOfQueue(r);
		} else {
			IPanelLog.d(TAG, "post msg = " + msg);
			getHandler().post(r);
		}
	}

	final void reserveState(int id) {
		post(MSG_ST_RESERVE, id, 0, null, null);
	}

	final void releaseState(int id) {
		post(MSG_ST_RELEASE, id, 0, null, null);
	}

	final void startState(int id, int v, ChannelKey key, int[] pids) {
		post(MSG_ST_START, id, v, key, pids);
	}

	final void stopState(int id, int v) {
		post(MSG_ST_STOP, id, v, null, null);
	}

	final void breakableState(int id, int v) {
		post(MSG_ST_BREAKABLE, id, v, null, null);
	}

	final void solveStateUri(int id, int v) {
		post(MSG_ST_SOLVE, id, v, null, null);
	}

	final void enterStateApp(int id, int v, String uri) {
		post(MSG_ST_ENTER, id, v, uri, null);
	}

	class ModState {
		ModState(int mid, int max) {
			this.mid = mid;
			maxDescChannelSize = max;
		}

		final int mid;
		boolean available = false;
		final int maxDescChannelSize;// 最大解扰通道数
		int availableChannel = 0;
		int casysids[];
	}

	class DescState extends DescramblingState {
		DescState(int id) {
			super(id);
		}

		@Override
		void onDescramblingState(String err) {
			int code = L10n.getErrorCode(err);
			IPanelLog.d(TAG, "onDescramblingState err = " + err);
			DescramblingScheduler.this.onDescramblingState(getId(), getVersion(), code, err);
		}

		@Override
		void onCaModuleDispatched(int mid) {
			IPanelLog.d(TAG, "onCaModuleDispatched mid = " + mid);
			DescramblingScheduler.this.onCaModuleDispatched(getId(), getVersion(), mid);
		}

		@Override
		StreamDescrambler createStreamDescrambler() {
			return app.getTransportManager().createDescrambler(app.uuid);
		}

		@Override
		Handler getHandler() {
			return DescramblingScheduler.this.getHandler();
		}

		boolean start(int v, ChannelKey key, int apid, int vpid) {
			if (getVersion() < v) {
				LiveDataManager ldm = app.getLiveDataManager();
				DescramblingArguments args = new DescramblingArguments(key, apid, vpid);
				IPanelLog.d(TAG, "start getResId() = " + getResId());
				if (getResId() > 0) {
					ModState m = mods.get(getResId());
					int ecms[] = new int[] { -1, -1, -1 };
					if (m != null ? m.available : false) {
						if (apid > 0)
							ecms[0] = ldm.getChannelStreamCoEcmPid(key, apid, m.casysids);
						if (vpid > 0)
							ecms[1] = ldm.getChannelStreamCoEcmPid(key, vpid, m.casysids);
						ecms[2] = ldm.getChannelStreamCoPmtPid(key);
						IPanelLog.d(TAG, "ecms[0] = " + ecms[0] + ":" + ecms[1] + ":" + ecms[2]);
					}
					args.setEcmpids(ecms[0], ecms[1], ecms[2]);
				}
				return super.start(v, args);
			}
			return false;
		}

		@Override
		LiveDataManager getLiveDataManager() {
			return app.getLiveDataManager();
		}
	}
}

class DescramblingArguments {
	private ChannelKey key = null;
	private int pid = -1, ecmpid = -1, pmtpid = -1, flags = 0;
	private int /* vice but video */vpid = -1, vecmpid = -1, vflags = 0;

	DescramblingArguments(ChannelKey key, int p1, int p2) {
		this.key = key;
		this.pid = p1;
		this.vpid = p2;
	}

	void setEcmpids(int e1, int e2, int e3) {
		ecmpid = e1;
		vecmpid = e2;
		pmtpid = e3;
	}

	final boolean compareEcmPids(int e1, int e2, int e3) {
		return e1 == ecmpid && e2 == vecmpid && e3 == pmtpid;
	}

	final int getPID() {
		return pid > 0 ? pid : vpid;
	}

	final int getPmtpid() {
		return pmtpid > 0 ? pmtpid : -1;
	}

	final int getEcmPID() {
		return pid > 0 ? ecmpid : vpid > 0 ? vecmpid : -1;
	}

	final int getFlags() {
		return pid > 0 ? flags : vpid > 0 ? vflags : 0;
	}

	final int getVicePID() {
		return pid > 0 && vpid > 0 ? vpid : -1;
	}

	final int getViceEcmPID() {
		return pid > 0 && vpid > 0 ? vecmpid : -1;
	}

	final int getViceFlags() {
		return pid > 0 && vpid > 0 ? vflags : 0;
	}

	final long getf() {
		return key.getFrequency();
	}

	final int getp() {
		return key.getProgram();
	}

	final ChannelKey getKey() {
		return key;
	}
}

abstract class DescramblingState {
	static final String TAG = DescramblingState.class.getSimpleName();
	private DescListener descListener = new DescListener();

	private int vMainStart = 0, vMainMsg = 0, vViceStart = 0, vViceMsg = 0;
	private DescramblingArguments args;
	private StreamDescrambler main, vice;
	private boolean closed = false, started = false, breadable = false, mainr = false,
			vicer = false;
	private int resId = -1, version = -1;
	final int stateId;

	DescramblingState(int id) {
		this.stateId = id;
		IPanelLog.d(TAG, "DescramblingState");
		this.main = createStreamDescrambler();
		IPanelLog.d(TAG, "DescramblingState 11");
		this.vice = createStreamDescrambler();
		IPanelLog.d(TAG, "DescramblingState 22");
		main.setDescramblingListener(descListener);
		vice.setDescramblingListener(descListener);
	}

	abstract StreamDescrambler createStreamDescrambler();

	abstract Handler getHandler();

	abstract void onDescramblingState(String err);

	abstract void onCaModuleDispatched(int mid);

	abstract LiveDataManager getLiveDataManager();

	final boolean matchVersion(boolean main) {
		IPanelLog.d(TAG, "matchVersion main = " + main + ";vMainMsg = " + vMainMsg
				+ ";vMainStart = " + vMainStart + ";vViceMsg = " + vViceMsg + ";vViceStart = "
				+ vViceStart);
		return main ? vMainMsg == vMainStart : vViceMsg == vViceStart;
	}

	final int getVersion() {
		return version;
	}

	final int getId() {
		return stateId;
	}

	final boolean isStarted() {
		return started;
	}

	final DescramblingArguments getArgs() {
		return args;
	}

	final boolean isDropping() {
		IPanelLog.d(TAG, "closed = " + closed + " resId = " + resId);
		return closed && resId > 0;
	}

	final boolean isClosed() {
		return closed;
	}

	final boolean isWaitting() {
		// breakable的不算在waiting里面
		if (started && resId < 0 && !breadable) {
			return true;
		}
		return false;
	}

	final boolean isWaittingBreadable() {
		if (started && resId < 0 && breadable) {
			return true;
		}
		return false;
	}

	final boolean isBreakable() {
		if (breadable && resId > 0) {
			ASSERT.assertTrue(started);
			return true;
		}
		return false;
	}

	void checkEcmUpdated(int casysids[]) {
		if (started && !breadable) {
			ASSERT.assertTrue(args != null);
			LiveDataManager ldm = getLiveDataManager();
			int e = ldm.getChannelStreamCoEcmPid(args.getKey(), args.getPID(), casysids);
			int ve = ldm.getChannelStreamCoEcmPid(args.getKey(), args.getVicePID(), casysids);
			int pe = ldm.getChannelStreamCoPmtPid(args.getKey());
			if (!args.compareEcmPids(e, ve, pe)) {
				args.setEcmpids(e, ve, pe);
				if (mainr)
					main.stop();
				if (vicer)
					vice.stop();
				started = mainr = vicer = false;
				doStart();
			}
		}
	}

	final int getResId() {
		return resId;
	}

	boolean close() {/*- return schedule scan flags */
		ASSERT.assertTrue(!closed);
		main.release();
		vice.release();
		args = null;
		version = -1;
		started = breadable = mainr = vicer = false;
		closed = true;
		IPanelLog.d(TAG, "closed = " + closed);
		return resId > 0;// 这里不清除resid
	}

	boolean stop(int v) {/*- return schedule scan flags */
		IPanelLog.d(TAG, "stop v = " + v + ";version = " + version + ";started =" + started);
		if (v > version) {
			version = v;
			if (started) {
				IPanelLog.d(TAG, "stop 22 v = " + v);
				if (mainr)
					main.stop();
				IPanelLog.d(TAG, "stop 33 v = " + v);
				if (vicer)
					vice.stop();
				IPanelLog.d(TAG, "stop 44 v = " + v);
				started = breadable = mainr = vicer = false;
				args = null;
			}
		}
		return false;
	}

	boolean start(int v, DescramblingArguments args) {/*- return schedule scan flags */
		if (version < v) {
			version = v;
			this.args = args;
			// breadable = false;
			if (resId < 0) {
				started = true;
				return true;
			}
			doStart();
		}
		return false;
	}

	void doStart() {
		if (closed)
			closed = false;
		IPanelLog.d(TAG, "doStart started = " + started + ";args = " + args);
		if (args == null || started)
			return;
		boolean ok = false;
		int pmtpid = args.getPmtpid();
		IPanelLog.d(TAG, "doStart pmtpid = " + pmtpid);
		if (args.getPID() > 0) {
			int ecmpid = args.getEcmPID();
			IPanelLog.d(TAG, "start main ecmpid = " + ecmpid);
			ok = (ecmpid > 0) ? //
			main.start(args.getf(), args.getp(), args.getPID(), ecmpid, args.getFlags()) //
					: pmtpid > 0 ? main.start(args.getf(), args.getp(), args.getPID(), pmtpid,
							args.getFlags()|0x08) : main.start(args.getf(), args.getp(), args.getPID(),
							args.getFlags() | StreamDescrambler.FLAG_STREAM_WITH_NO_ECM);
			mainr = true;
		}
		if (args.getVicePID() > 0) {
			int ecmpid = args.getViceEcmPID();
			IPanelLog.d(TAG, "start vice ecmpid = " + ecmpid);
			ok = ecmpid > 0 ? //
			vice.start(args.getf(), args.getp(), args.getVicePID(), ecmpid, args.getViceFlags())//
					: pmtpid > 0 ? vice.start(args.getf(), args.getp(), args.getVicePID(), pmtpid,
							args.getViceFlags()|0x08) : vice.start(args.getf(), args.getp(),
							args.getVicePID(), args.getViceFlags()
									| StreamDescrambler.FLAG_STREAM_WITH_NO_ECM);
			vicer = true;
		}
		started = true;
		breadable = false;
		if (!ok)
			onDescramblingState(L10n.DESCR_ERR_420);
	}

	boolean setBreakable(int v) {/*- return schedule scan flags */
		if (started && version == v && !breadable) {
			IPanelLog.d(TAG, "setBreakable");
			breadable = true;
			if (mainr)
				main.setBreakable(true);
			if (vicer)
				vice.setBreakable(true);
		}
		return (resId > 0);
	}

	boolean setCaModuleId(int id, int casysids[]) {
		IPanelLog.d(TAG, "setCaModuleId id = " + id + ";casysids = " + casysids);
		if ((resId = id) > 0) {
			if (started) {
				if (mainr)
					main.stop();
				if (vicer)
					vice.stop();
				started = mainr = vicer = false;
				main.setCAModuleID(id);
				vice.setCAModuleID(id);
				LiveDataManager ldm = getLiveDataManager();
				int e = ldm.getChannelStreamCoEcmPid(args.getKey(), args.getPID(), casysids);
				int ve = ldm.getChannelStreamCoEcmPid(args.getKey(), args.getVicePID(), casysids);
				int pmtpid = ldm.getChannelStreamCoPmtPid(args.getKey());
				IPanelLog.d(TAG, "setCaModuleId id = " + id + " e = " + e + " ve = " + ve
						+ ";pmtpid = " + pmtpid);
				args.setEcmpids(e, ve, pmtpid);
				doStart();
			} else {
				main.setCAModuleID(id);
				vice.setCAModuleID(id);
			}
		} else {
			if (started) {
				if (mainr)
					main.stop();
				if (vicer)
					vice.stop();
				mainr = vicer = false;
				// onDescramblingState(L10n.CAMOD_ERR_451);
			}
		}
		IPanelLog.d(TAG, "setCaModuleId version = " + version);
		if (version > 0)
			onCaModuleDispatched(id);
		return true;
	}

	void solveUri(int v) {
		if (started && v == version) {
			synchronized (descListener) {
				String uri = descListener.solveUri;
				if (uri != null)
					main.enterApplication(uri);
			}
		}
	}

	void enterApp(int v, String uri) {
		if (v == version) {
			main.enterApplication(uri);
		}
	}

	class DescListener implements StreamDescrambler.DescramblingListener {
		final int MSG_DESC_START = 1;
		final int MSG_DESC_ERROR = 2;
		final int MSG_DESC_TERMI = 3;
		final int MSG_DESC_RESUM = 4;
		final int MSG_DESC_CACHG = 5;
		SparseIntArray errs = new SparseIntArray();
		String solveUri = null;

		@Override
		public void onDescramblingStart(StreamDescrambler d) {
			IPanelLog.d(TAG, "onDescramblingStart");
			post(MSG_DESC_START, d, null, null);
		}

		@Override
		public void onDescramblingError(StreamDescrambler d, String uri, String msg) {
			IPanelLog.d(TAG, "onDescramblingError uri = " + uri + ";msg = " + msg);
			post(MSG_DESC_ERROR, d, uri, msg);
		}

		@Override
		public void onDescramblingResumed(StreamDescrambler d) {
			IPanelLog.d(TAG, "onDescramblingResumed");
			post(MSG_DESC_RESUM, d, null, null);
		}

		@Override
		public void onDescramblingTerminated(StreamDescrambler d, String msg) {
			IPanelLog.d(TAG, "onDescramblingTerminated msg = " + msg);
			post(MSG_DESC_TERMI, d, null, msg);
		}

		@Override
		public void onNetworkCAChange(StreamDescrambler d) {
			IPanelLog.d(TAG, "onNetworkCAChange");
			post(MSG_DESC_CACHG, d, null, null);
		}

		private void onDescTermi(String err) {
			IPanelLog.d(TAG, "onDescState err = " + err);
			synchronized (errs) {
				int key;
				if (err == null) {
					solveUri = null;
					errs.clear();
					key = -2;
				} else {
					key = L10n.getErrorCode(err, -1);
				}
				for (int i = 0; i < errs.size(); i++) {
					IPanelLog.d(TAG, "onDescState errs.keyAt(i) = " + errs.keyAt(i));
					IPanelLog.d(TAG, "onDescState errs = " + errs.get(errs.keyAt(i)));
				}
				IPanelLog.d(TAG,
						"onDescState key = " + key + ";errs.get(key, -1) = " + errs.get(key, -1));
				if (key != -1) {
					if (errs.get(key, -1) != -1)
						return;
					errs.put(key, 0);
				}
			}
			IPanelLog.d(TAG, "onDescState args = " + args);
			if (args != null) {
				IPanelLog.d(TAG, "onDescState args.getEcmPID() = " + args.getEcmPID()
						+ ";args.getViceEcmPID() = " + args.getViceEcmPID());
			}
			if (args != null && getLiveDataManager().isCaRequired(args.getKey())) {
				onDescramblingState(err);
			}
		}

		private void onDescState(String err) {
			IPanelLog.d(TAG, "onDescState err = " + err);
			synchronized (errs) {
				int key;
				if (err == null) {
					solveUri = null;
					errs.clear();
					key = -2;
				} else {
					key = L10n.getErrorCode(err, -1);
				}
				for (int i = 0; i < errs.size(); i++) {
					IPanelLog.d(TAG, "onDescState errs.keyAt(i) = " + errs.keyAt(i));
					IPanelLog.d(TAG, "onDescState errs = " + errs.get(errs.keyAt(i)));
				}
				IPanelLog.d(TAG,
						"onDescState key = " + key + ";errs.get(key, -1) = " + errs.get(key, -1));
				if (key != -1) {
					if (errs.get(key, -1) != -1)
						return;
					errs.put(key, 0);
				}
			}
			IPanelLog.d(TAG, "onDescState args = " + args);
			if (args != null) {
				IPanelLog.d(TAG, "onDescState args.getEcmPID() = " + args.getEcmPID()
						+ ";args.getViceEcmPID() = " + args.getViceEcmPID());
			}
			if (args != null && getLiveDataManager().isCaRequired2(args.getKey())) {
				onDescramblingState(err);
			}
		}

		private void onDescStateError(String err) {
			IPanelLog.d(TAG, "onDescState err = " + err);
			synchronized (errs) {
				int key;
				if (err == null) {
					solveUri = null;
					errs.clear();
					key = -2;
				} else {
					key = L10n.getErrorCode(err, -1);
				}
				for (int i = 0; i < errs.size(); i++) {
					IPanelLog.d(TAG, "onDescState errs.keyAt(i) = " + errs.keyAt(i));
					IPanelLog.d(TAG, "onDescState errs = " + errs.get(errs.keyAt(i)));
				}
				IPanelLog.d(TAG,
						"onDescState key = " + key + ";errs.get(key, -1) = " + errs.get(key, -1));
				if (key != -1) {
					if (errs.get(key, -1) != -1)
						return;
					errs.put(key, 0);
				}
			}
			IPanelLog.d(TAG, "onDescState args = " + args);
			if (args != null) {
				IPanelLog.d(TAG, "onDescState args.getEcmPID() = " + args.getEcmPID()
						+ ";args.getViceEcmPID() = " + args.getViceEcmPID());
			}
			if (args != null && getLiveDataManager().isCaRequired2(args.getKey())) {
				onDescramblingState(err);
			}
		}

		private void post(final int what, final StreamDescrambler d, final String uri,
				final String msg) {
			getHandler().post(new Runnable() {
				@Override
				public void run() {
					boolean isMain = d == main;
					IPanelLog.d(TAG, "post what = " + what + ";isMain = " + isMain);
					switch (what) {
					case MSG_DESC_START:
						if (isMain) {
							vMainMsg++;
							vMainStart++;
						} else {
							vViceMsg++;
							vViceStart++;
						}
						synchronized (errs) {
							errs.clear();
							solveUri = null;
						}
						break;
					case MSG_DESC_ERROR:
						if (matchVersion(isMain))
							onDescStateError(msg);
						break;
					case MSG_DESC_TERMI:
						boolean b = matchVersion(isMain);
						IPanelLog.d(TAG, "post b" + b);
						if (matchVersion(isMain)) {
							onDescTermi(msg);
							solveUri = uri;
						}
						break;
					case MSG_DESC_RESUM:
						if (matchVersion(isMain))
							onDescState(null);
						break;
					case MSG_DESC_CACHG:
						if (matchVersion(isMain))
							onDescState(null);
						break;
					}
				}
			});
		}
	}
}
