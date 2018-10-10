package ipaneltv.toolkit.ca;

import ipaneltv.toolkit.IPanelLog;

import java.util.ArrayList;
import java.util.List;

import android.content.Context;
import android.net.telecast.ca.CAManager;
import android.os.Handler;
import android.util.SparseArray;

/**
 * 此对象不应该共用
 */
public class CaEnvironment {
	static final String TAG = CaEnvironment.class.getSimpleName();
	private Context context;
	private CAManager camanager;
	private CaCardSlot slot[] = new CaCardSlot[64];
	private SparseArray<CaModule> mods = new SparseArray<CaModule>();
	private CaModule defaultCurrent;
	private Object mutex = new Object();
	private Handler handler;

	public CaEnvironment(Context context, Handler handler) {
		this.context = context.getApplicationContext();
		this.camanager = CAManager.createInstance(context);
		camanager.setCACardStateListener(cardListener);
		camanager.setCAModuleStateListener(modListener);
		this.handler = handler;
	}

	public synchronized void close() {
		synchronized (mutex) {
			if (camanager != null)
				camanager.release();
			camanager = null;
			context = null;
		}
		mutex = null;
	}

	protected Handler getHandler() {
		return handler;
	}

	public void queryState() {
		camanager.queryCurrentCAState();
	}

	public CAManager getCaManager() {
		return camanager;
	}

	public Context getContext() {
		return context;
	}

	public CaModule getCaModule(int id) {
		synchronized (mutex) {
			return mods.get(id);
		}
	}

	public CaModule findCaModule(int caSystemId) {
		synchronized (mutex) {
			int n = mods.size();
			for (int i = 0; i < n; i++) {
				CaModule m = mods.valueAt(i);
				for (int id : m.casysIds) {
					if (id == caSystemId)
						return m;
				}
			}
		}
		return null;
	}

	public CaModule getDefaultCurrentCaModule() {
		synchronized (mutex) {
			return defaultCurrent;
		}
	}

	public List<CaCardSlot> getCaCardSlot() {
		synchronized (mutex) {
			List<CaCardSlot> ret = new ArrayList<CaCardSlot>();
			for (CaCardSlot s : slot) {
				if (s != null)
					ret.add(s);
			}
			return ret;
		}
	}

	public List<CaModule> getCaModule() {
		synchronized (mutex) {
			List<CaModule> ret = new ArrayList<CaModule>();
			int n = mods.size();
			for (int i = 0; i < n; i++) {
				CaModule m = mods.valueAt(i);
				if (m != null)
					ret.add(m);
			}
			return ret;
		}
	}

	public List<CaModule> getVerifiedCaModule() {
		synchronized (mutex) {
			List<CaModule> ret = new ArrayList<CaModule>();
			int n = mods.size();
			for (int i = 0; i < n; i++) {
				CaModule m = mods.valueAt(i);
				if (m != null)
					if (m.state == CaModule.STATE_VERIFIED)
						ret.add(m);
			}
			return ret;
		}
	}

	protected void onCaModuleAdd(CaModule m) {
	}

	protected void onCaModuleRemove(CaModule m) {
	}

	protected void onCaModuleAbsent(CaModule m) {
	}

	protected void onCaModulePresent(CaModule m) {
	}

	protected void onCaModuleChange(CaModule m) {
	}

	protected void onCaCardPresent(CaCardSlot s) {
	}

	protected void onCaCardAbsent(CaCardSlot s) {
	}

	protected void onCaCardMuted(CaCardSlot s) {
	}

	protected void onCaCardReady(CaCardSlot s) {
	}

	protected void onCaCardNoMatching(CaCardSlot s) {
	}

	protected void onModuleCardBind(CaCardSlot s, CaModule m) {
	}

	protected void onModuleCardUnbind(CaCardSlot s, CaModule m) {
	}

	static final int MSG_onCaModuleAdd = 1;
	static final int MSG_onCaModuleRemove = 2;
	static final int MSG_onCaModuleAbsent = 3;
	static final int MSG_onCaModulePresent = 4;
	static final int MSG_onCaModuleChange = 5;
	static final int MSG_onCaCardPresent = 6;
	static final int MSG_onCaCardAbsent = 7;
	static final int MSG_onCaCardMuted = 8;
	static final int MSG_onCaCardReady = 9;
	static final int MSG_onCaCardNoMatching = 10;
	static final int MSG_onModuleCardBind = 11;
	static final int MSG_onModuleCardUnbind = 12;

	private final void postMsg(final int msg, final CaCardSlot c, final CaModule m) {

		handler.post(new Runnable() {
			@Override
			public void run() {
				try {
					IPanelLog.d(TAG, "postMsg msg = " + msg);
					switch (msg) {
					case MSG_onCaModuleAdd:
						onCaModuleAdd(m);
						break;
					case MSG_onCaModuleRemove:
						onCaModuleRemove(m);
						break;
					case MSG_onCaModuleAbsent:
						onCaModuleAbsent(m);
						break;
					case MSG_onCaModulePresent:
						onCaModulePresent(m);
						break;
					case MSG_onCaModuleChange:
						onCaModuleChange(m);
						break;
					case MSG_onCaCardPresent:
						onCaCardPresent(c);
						break;
					case MSG_onCaCardAbsent:
						onCaCardAbsent(c);
						break;
					case MSG_onCaCardMuted:
						onCaCardMuted(c);
						break;
					case MSG_onCaCardReady:
						onCaCardReady(c);
						break;
					case MSG_onCaCardNoMatching:
						onCaCardNoMatching(c);
						break;
					case MSG_onModuleCardBind:
						onModuleCardBind(c, m);
						break;
					case MSG_onModuleCardUnbind:
						onModuleCardUnbind(c, m);
						break;
					default:
						break;
					}
					IPanelLog.d(TAG, "postMsg end msg = " + msg);
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		});
	}

	private CaCardSlot ensureCaCardSlot(int ri) {
		CaCardSlot ret = slot[ri];
		if (ret == null) {
			slot[ri] = ret = new CaCardSlot();
			ret.id = ri;
			ret.current = null;
			ret.state = CaCardSlot.STATE_ABSENT;
		}
		return ret;
	}

	private CaModule ensureCaModule(int id) {
		CaModule m = mods.get(id);
		if (m == null) {
			m = new CaModule();
			m.slotId = -1;
			m.moduleId = id;
			m.state = CaModule.STATE_ABSENT;
			m.casysIds = camanager.getCAModuleCASystemIDs(id);
			mods.put(id, m);
		}
		return m;
	}

	private void unbindCaModule(CaCardSlot s, CaModule m) {
		if (m != null) {
			m.slotId = -1;
			if (m.state == CaModule.STATE_VERIFIED)
				m.state = CaModule.STATE_PRESENT;
			postMsg(MSG_onModuleCardUnbind, s, m);
			IPanelLog.d(TAG, "unbindCaModule s=" + s + ",m=" + m);
		}
	}

	private CAManager.CACardStateListener cardListener = new CAManager.CACardStateListener() {

		@Override
		public void onCardPresent(int ri) {
			IPanelLog.d(TAG, "onCardPresent ri = " + ri);
			CaCardSlot s = null;
			CaModule unbind = null;
			synchronized (mutex) {
				s = ensureCaCardSlot(ri);
				s.state = CaCardSlot.STATE_PRESENT;
				unbind = s.current;
				s.current = null;
			}
			unbindCaModule(s, unbind);
			postMsg(MSG_onCaCardPresent, s, null);
		}

		@Override
		public void onCardAbsent(int ri) {
			IPanelLog.d(TAG, "onCardAbsent ri = " + ri);
			CaCardSlot s = null;
			CaModule unbind = null;
			synchronized (mutex) {
				s = ensureCaCardSlot(ri);
				s.state = CaCardSlot.STATE_ABSENT;
				unbind = s.current;
				s.current = null;
			}
			IPanelLog.d(TAG, "onCardAbsent unbind = " + unbind);
			unbindCaModule(s, unbind);
			postMsg(MSG_onCaCardAbsent, s, null);
		}

		@Override
		public void onCardMuted(int ri) {
			IPanelLog.d(TAG, "onCardMuted ri = " + ri);
			CaCardSlot s = null;
			CaModule unbind = null;
			synchronized (mutex) {
				s = ensureCaCardSlot(ri);
				s.state = CaCardSlot.STATE_MUTED;
				unbind = s.current;
				s.current = null;
			}
			unbindCaModule(s, unbind);
			postMsg(MSG_onCaCardMuted, s, null);
		}

		@Override
		public void onCardReady(int ri) {
			IPanelLog.d(TAG, "onCardReady ri = " + ri);
			CaCardSlot s = null;
			CaModule unbind = null;
			synchronized (mutex) {
				s = ensureCaCardSlot(ri);
				s.state = CaCardSlot.STATE_READY;
				unbind = s.current;
				s.current = null;
			}
			unbindCaModule(s, unbind);
			postMsg(MSG_onCaCardReady, s, null);
		}

		@Override
		public void onCardVerified(int ri, int mid) {
			IPanelLog.d(TAG, "onCardVerified ri = " + ri + ",mid=" + mid);
			CaCardSlot s = null;
			CaModule m = null;
			synchronized (mutex) {
				if (mid > 0) {
					s = ensureCaCardSlot(ri);
					s.state = CaCardSlot.STATE_VERIFIED;
					s.current = m = ensureCaModule(mid);
					if (s.current != null) {
						s.current.state = CaModule.STATE_VERIFIED;
						s.current.slotId = ri;
					}
					if (m != null)
						postMsg(MSG_onModuleCardBind, s, m);
				} else {
					postMsg(MSG_onCaCardNoMatching, s, null);
				}
			}
		}

	};

	private CAManager.CAModuleStateListener modListener = new CAManager.CAModuleStateListener() {

		@Override
		public void onModuleAdd(int mid) {
			IPanelLog.d(TAG, "onModuleAdd mid = " + mid);
			CaModule m = null;
			synchronized (mutex) {
				m = ensureCaModule(mid);
			}
			postMsg(MSG_onCaModuleAdd, null, m);
		}

		@Override
		public void onModuleRemove(int mid) {
			IPanelLog.d(TAG, "onModuleRemove mid = " + mid);
			CaModule m = null;
			synchronized (mutex) {
				if ((m = mods.get(mid)) != null)
					mods.remove(mid);
			}
			if (m != null)
				postMsg(MSG_onCaModuleRemove, null, m);
		}

		@Override
		public void onModulePresent(int mid, int ri) {
			IPanelLog.d(TAG, "onModulePresent mid = " + mid + ",ri=" + ri);
			CaModule m = null;
			synchronized (mutex) {
				m = ensureCaModule(mid);
				m.state = CaModule.STATE_PRESENT;
			}
			postMsg(MSG_onCaModulePresent, null, m);
		}

		@Override
		public void onModuleAbsent(int mid) {
			IPanelLog.d(TAG, "onModuleAbsent mid = " + mid);
			CaModule m = null;
			synchronized (mutex) {
				m = mods.get(mid);
				if (m != null)
					m.state = CaModule.STATE_ABSENT;
			}
			if (m != null)
				postMsg(MSG_onCaModuleAbsent, null, m);
		}

		@Override
		public void onCAChange(int mid) {
			IPanelLog.d(TAG, "onCAChange mid = " + mid);
			CaModule m = null;
			synchronized (mutex) {
				m = mods.get(mid);
				String id = camanager.getCAModuleProperty(mid, "CA_SYSTEMID");
				IPanelLog.d(TAG, "onCAChange id2 = " + id);
				if (m != null && id != null && !id.equals("") && m.casysIds != null) {
					int caId = Integer.parseInt(id);
					IPanelLog.d(TAG, "onCAChange m.casysIds[0] = " + m.casysIds[0] + ";caId = " + caId);
					if (m.casysIds[0] != caId) {
						m.casysIds[0] = caId;
						postMsg(MSG_onCaModuleChange, null, m);
					}
				}
			}
		}

	};

}
