package ipaneltv.toolkit.media;

import ipaneltv.toolkit.IPanelLog;
import ipaneltv.toolkit.ca.CaModule;

import java.util.ArrayList;
import java.util.List;

import android.app.Fragment;
import android.net.telecast.ca.CAManager;
import android.os.Bundle;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.Message;
import android.util.Log;
import android.util.SparseArray;

public abstract class CaSessionFragment extends Fragment {

	public static final String ARG_NAME_UUID = "uuid";

	static final String TAG = LiveCaSessionFragment.class.getSimpleName();
	private CAManager cam;
	private SparseArray<Session> sessions = new SparseArray<Session>();
	private Session current;
	private boolean addAll = false;
	private Handler callbackHandler, procHandler;
	private HandlerThread procThread = new HandlerThread(TAG);
	private Handler.Callback procMessage = new Handler.Callback() {
		@Override
		public boolean handleMessage(Message msg) {
			try {
				procMessage(msg);
			} catch (Exception e) {
				e.printStackTrace();
			}
			return false;
		}
	};

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		cam = CAManager.createInstance(getActivity());
		callbackHandler = new Handler();
		procThread.start();
		procHandler = new Handler(procThread.getLooper(), procMessage);
		onCaSessionFragmentCreate();
	}

	protected void onCaSessionFragmentCreate(){
		
	}
	
	@Override
	public void onPause() {
		Log.d(TAG, "onPause in");
		synchronized (sessions) {
			int n = sessions.size();
			for (int i = 0; i < n; i++)
				sessions.valueAt(i).loosen(true);
		}
		Log.d(TAG, "onPause out");
		super.onPause();
	}
	
	@Override
	public void onStop() {
		Log.d(TAG, "onStop in");
		synchronized (sessions) {
			int n = sessions.size();
			for (int i = 0; i < n; i++)
				sessions.valueAt(i).loosen(true);
		}
		Log.d(TAG, "onStop out");
		super.onStop();
	}

	@Override
	public void onDestroy() {
		procThread.getLooper().quit();
		synchronized (sessions) {
			int n = sessions.size();
			for (int i = 0; i < n; i++)
				sessions.valueAt(i).close();
			sessions.clear();
		}
		super.onDestroy();
	}

	protected void addAllCaModuleSession() {
		if (addAll)
			return;
		addAll = true;
		postToProc(new Runnable() {
			@Override
			public void run() {
				String uuid = getArguments().getString(ARG_NAME_UUID);
				IPanelLog.d(TAG, "get network uuid for CaSessionFragment : " + uuid.toString());
				int[] ids = cam.getCAModuleIDs(uuid);
				IPanelLog.d(TAG, "addAllCaModuleSession ids : " + ids);
				synchronized (sessions) {
					if (ids != null) {
						for (int i = 0; i < ids.length; i++) {
							String sn = CaModule.getSessionServiceName(cam, ids[i]);
							int msn = CaModule.getModuleSN(cam, ids[i], -1);
							IPanelLog.d(TAG, "found session " + ids[i] + " (" + sn + "," + msn + ") ");
							if (sn != null && msn != -1) {
								Session s = createSession(sn, ids[i], msn);
								if (s != null) {
									sessions.put(ids[i], s);
									s.connect();
								}
							}
						}
					}
				}
			}
		});
	}

	public List<Session> getSessions() {
		List<Session> ret = new ArrayList<Session>();
		synchronized (sessions) {
			int n = sessions.size();
			for (int i = 0; i < n; i++)
				ret.add(sessions.valueAt(i));
		}
		return ret;
	}

	public void chooseSession(final int caModuleId) {
		postToProc(new Runnable() {
			@Override
			public void run() {
				synchronized (sessions) {
					Session s = sessions.get(caModuleId);
					if (s == null) {
						String sn = CaModule.getSessionServiceName(cam, caModuleId);
						int msn = CaModule.getModuleSN(cam, caModuleId, -1);
						IPanelLog.d(TAG, "choose seesion " + caModuleId + " (" + sn + "," + msn + ")");
						if (sn != null && msn != -1)
							if ((s = createSession(sn, caModuleId, msn)) != null) {
								sessions.put(caModuleId, s);
								s.connect();
							}
					}
					IPanelLog.d(TAG, "chooseSession s = " + s);
					if (s != null) {
						current = s;
						checkReserve(s);
					}
				}
			}
		});
	}

	public void chooseSession(final Session s) {
		postToProc(new Runnable() {
			@Override
			public void run() {
				synchronized (sessions) {
					if (sessions.get(s.camid) != null) {
						current = s;
						checkReserve(s);
					}
				}
			}
		});
	}

	public Session getChoosedSession() {
		return current;
	}

	protected void onSessionReady(Session s) {
	}

	protected final void postToUi(Runnable r) {
		callbackHandler.post(r);
	}

	protected final void postToProc(Runnable r) {
		procHandler.post(r);
	}

	protected final CAManager getCaManager() {
		return cam;
	}

	void checkReserve(Session s) {
		if (s == current) {
			if (s.reserve()) {
				onSessionReady(s);
			}
		}
	}

	protected abstract Session createSession(String serviceName, int mid, int sn);

	public abstract class Session implements ReserveStateInterface {

		final int camid;

		public Session(int camid, int sn) {
			this.camid = camid;
//			 Bundle b = getArguments();
			// if (b == null)
			// b = new Bundle();
			// b.putInt(CAManager.PROP_NAME_MODULE_SN, sn);
			// setArguments(b);
//			connect();
		}

		public int getCaModuleId() {
			return camid;
		}

		public void onServiceConnected() {
			postToProc(new Runnable() {
				@Override
				public void run() {
					checkReserve(Session.this);
				}
			});
		}

		public void onServiceLost() {
			postToProc(new Runnable() {
				@Override
				public void run() {
					synchronized (sessions) {
						sessions.remove(camid);
						close();
					}
				}
			});
		}

		protected abstract void connect();

		protected abstract void close();
	}

	protected void procMessage(Message msg) {
	}
}
