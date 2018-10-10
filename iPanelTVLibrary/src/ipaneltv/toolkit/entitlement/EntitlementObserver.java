package ipaneltv.toolkit.entitlement;

import ipaneltv.toolkit.IPanelLog;
import ipaneltv.toolkit.db.DatabaseObjectification.ChannelKey;
import ipaneltv.toolkit.db.QueryHandler;
import ipaneltv.toolkit.entitlement.EntitlementDatabaseObjects.Entitlement;
import ipaneltv.toolkit.entitlement.EntitlementDatabaseObjects.EntitlementCursorHandler;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map.Entry;

import android.content.Context;
import android.database.ContentObserver;
import android.database.Cursor;
import android.net.Uri;
import android.net.telecast.ca.CAManager;
import android.net.telecast.ca.EntitlementDatabase;
import android.net.telecast.ca.EntitlementDatabase.EntitlementType;
import android.net.telecast.ca.EntitlementDatabase.Entitlements;
import android.net.telecast.ca.EntitlementDatabase.ProductType;
import android.net.telecast.ca.EntitlementDatabase.ProductUriSchema;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.Looper;
import android.util.SparseArray;

public class EntitlementObserver {
	static final String TAG = EntitlementObserver.class.getSimpleName();
	private Context context;
	private Object mutex = new Object();
	private CAManager caManager;
	private SparseArray<EntitlementsState> entStates = new SparseArray<EntitlementsState>();
	private HandlerThread procThread = new HandlerThread(EntitlementObserver.class.getName());
	private Handler procHandler;
	private boolean preloadShotted = false;
	private EntDataReadyLisentener drl;

	public EntitlementObserver(Context context) {
		this.context = context.getApplicationContext();
	}

	public void prepare() {
		synchronized (mutex) {
			if (!preloadShotted) {
				preloadShotted = true;
				caManager = CAManager.createInstance(context);
				procThread.start();
				procHandler = new Handler(procThread.getLooper());
			}
		}
	}

	/** 注册一个网络的所有启用的CA模块的授权信息数据结构 */
	public void registerAll(String uuid) {
		int[] ids = caManager.getCAModuleIDs(uuid);
		if (ids != null) {
			for (int i : ids) {
				registerProvider(i);
			}
		}
	}

	public boolean registerProvider(int moduleId) {
		synchronized (mutex) {
			if (!preloadShotted)
				throw new RuntimeException("call prepare first!");
			if (entStates.get(moduleId) != null)
				return true;
		}
		try {
			String uris = caManager.getCAModuleProperty(moduleId,
					CAManager.PROP_NAME_ENTITLEMENT_URI);
			String msnStr = caManager.getCAModuleProperty(moduleId,CAManager.PROP_NAME_MODULE_SN);
			if(msnStr==null) return false;
			int msn = Integer.parseInt(msnStr);
			if (uris == null) {
				IPanelLog.w(TAG, "entitlement db uri not setted by module:" + moduleId);
				return false;
			}
			Uri uri = Uri.withAppendedPath(Uri.parse(uris), Entitlements.TABLE_NAME);
			IPanelLog.d(TAG, "registerProvider: uri is " + uri);
			EntitlementsState es = new EntitlementsState();
			es.uri = uri;
			es.moduleId = moduleId;
			es.moduleSn = msn;
			es.vender = caManager.getCAModuleProperty(moduleId, CAManager.PROP_NAME_VENDER_NAME);
			synchronized (mutex) {
				entStates.put(moduleId, es);
			}
			IPanelLog.i("hegang", "registerProvider----moduleId---"+moduleId);
			IPanelLog.i("hegang", "entStates---"+entStates.size());
			postLoadEntitlements(es);
			return true;
		} catch (Exception e) {
			e.printStackTrace();
			return false;
		}
	}

	public List<EntitlementsState> getEntitlementsStates() {
		synchronized (mutex) {
			int n = entStates.size();
			List<EntitlementsState> ret = new ArrayList<EntitlementsState>();
			for (int i = 0; i < n; i++) {
				ret.add(entStates.valueAt(i));
			}
			return ret;
		}
	}

	public EntitlementsState getSelectedEntitlementsState(int moduleId) {
		synchronized (mutex) {
			return entStates.get(moduleId);
		}
	}

	public synchronized void close() {
		synchronized (mutex) {
			Looper lp = procThread.getLooper();
			if (lp != null)
				lp.quit();
			procThread = null;
			procHandler = null;
		}
	}

	protected EntitlementCursorHandler createLoadEntitlementCursorHandler(Context context, Uri uri,
			EntitlementsState es, Handler handler) {
		String selection = null;
		String selectionargs[] = null;
		if (es.moduleSn >= 0) {
			selection = EntitlementDatabase.Entitlements.MODULE_SN + "=?";
			selectionargs = new String[] { String.valueOf(es.moduleSn) };
		}
		return new EntitlementCursorHandler(context, uri, null, selection, selectionargs, null,
				handler){
			@Override
			public void onRecordFound(Cursor c) {
				// TODO Auto-generated method stub
				super.onRecordFound(c);
				appendRecord();
			}
		};
	}

	public Context getContext() {
		return context;
	}

	private void postLoadEntitlements(final EntitlementsState es) {
		final EntitlementCursorHandler ch = createLoadEntitlementCursorHandler(context, es.uri, es,
				procHandler);
		ch.setQueryHandler(new QueryHandler() {
			@Override
			public void onQueryStart() {
				super.onQueryStart();
				es.addContentObserver();
			}

			@Override
			public void onQueryEnd() {
				es.resetRecords(ch.ents);
			}
		});
		ch.postQuery();
	}

	/** 非UI线程，需要转接 */
	protected void onEntitlementsUpdated(EntitlementsState es) {
	}

	public class EntitlementsState {
		int moduleId, moduleSn = -1;
		Uri uri;
		String vender;
		HashMap<ChannelKey, Entitlement> channelEnts = new HashMap<ChannelKey, Entitlement>();
		HashMap<String, Entitlement> uriEnts = new HashMap<String, Entitlement>();
		SparseArray<List<Entitlement>> opidEnts = new SparseArray<List<Entitlement>>();
		ContentObserver observer = null;
		
		public int getEntitlement(ChannelKey ch) {
			synchronized (channelEnts) {
				Entitlement e = channelEnts.get(ch);
				if (e != null)
					return e.value;
				return EntitlementType.TYPE_UNKNOWN;
			}
		}

		public int getModuleId() {
			return moduleId;
		}

		public int getVenderId() {
			return moduleSn;
		}

		public String getVenderName() {
			return vender;
		}

		public int getEntitlement(String uri) {
			synchronized (uriEnts) {
				Entitlement e = uriEnts.get(uri);
				if (e != null)
					return e.value;
				return EntitlementType.TYPE_UNKNOWN;
			}
		}

		public Entitlement getEntitlementRecord(ChannelKey ch) {
			synchronized (channelEnts) {
				return channelEnts.get(ch);
			}
		}

		public Entitlement getEntitlementRecord(String uri) {
			synchronized (uriEnts) {
				return uriEnts.get(uri);
			}
		}

		public List<Entitlement> getAllEntitlements() {
			synchronized (uriEnts) {
				List<Entitlement> ret = new ArrayList<Entitlement>();
				ret.addAll(uriEnts.values());
				return ret;
			}
		}
		
		/**根据运营商id获取授权信息*/
		public List<Entitlement> getAllEntitlementsByOpid(int opid) {
			synchronized (opidEnts) {
				return opidEnts.get(opid);
			}
		}

		/** 根据运营商id已授权的信息 */
		public List<Entitlement> getEntitlementsByOpid(int opid) {
			synchronized (opidEnts) {
				List<Entitlement> a = new ArrayList<Entitlement>();
				IPanelLog.i(TAG, "opidEnts.get(opid) = "+opidEnts.get(opid));
				if (opidEnts.get(opid) != null) {
					for (Entitlement e : opidEnts.get(opid)) {
						IPanelLog.i(TAG, "----------go in Entitlement");
						if (e.value == EntitlementType.TYPE_AVAILABLE)
							a.add(e);
					}
				}
				return a;
			}
		}

		public List<Entitlement> getEntitlements(int type) {
			List<Entitlement> ret = new ArrayList<Entitlement>();
			if (type == ProductType.TYPE_CHANNEL) {
				synchronized (channelEnts) {
					ret.addAll(channelEnts.values());
				}
			} else {
				synchronized (uriEnts) {
					for (Entitlement e : uriEnts.values()) {
						if (e.type == type)
							ret.add(e);
					}
				}
			}
			return ret;
		}

		void addContentObserver() {
			if (observer != null)
				return;
			observer = new ContentObserver(procHandler) {
				@Override
				public void onChange(boolean selfChange) {
					super.onChange(selfChange);
					IPanelLog.i("hegang", "ent table has changed....");
					postLoadEntitlements(EntitlementsState.this);
				}
			};
			context.getContentResolver().registerContentObserver(uri, false, observer);
		}

		void removeContentObserver() {
			if (observer != null) {
				context.getContentResolver().unregisterContentObserver(observer);
				observer = null;
			}
		}

		void resetRecords(HashMap<String, Entitlement> map) {
			IPanelLog.i("hegang", "resetRecords---------map.size:"+map.size());
			synchronized (uriEnts) {
				synchronized (channelEnts) {
					uriEnts = map;
					channelEnts = new HashMap<ChannelKey, Entitlement>();
					opidEnts = new SparseArray<List<Entitlement>>();
					
					for (Entry<String, Entitlement> e : map.entrySet()) {
						Entitlement v = e.getValue();
						ChannelKey key = channelUriToChannelKey(e.getKey());
						if (key != null && v.type == ProductType.TYPE_CHANNEL) {
							channelEnts.put(key, v);
							v.ch = key;
						}
						
						List<Entitlement> lt = opidEnts.get(v.opid);
						if(lt==null){
							lt = new ArrayList<Entitlement>();
							lt.add(v);
							opidEnts.put(v.opid, lt);
						}else{
							if(!lt.contains(v))
								lt.add(v);
						}
					}
					IPanelLog.i("hegang", "---------channelEnts.size:"+channelEnts.size());
					try {
						onEntitlementsUpdated(this);
					} catch (Exception e) {
						e.printStackTrace();
					}
				}
			}
			if (opidEnts != null && drl != null)
				drl.onDataReady();
		}

		
		
	}
	public static ChannelKey channelUriToChannelKey(String uri){
		if(uri.startsWith(ProductUriSchema.CHANNEL)){
			String ckStr = uri.replace(ProductUriSchema.CHANNEL, "");
			return ChannelKey.fromString(ckStr);
		}
		return null;
	}
	public void setDataReadyLisentener(EntDataReadyLisentener drl) {
		this.drl = drl;
	}
	
	public static interface EntDataReadyLisentener {
		void onDataReady();
	}
}
