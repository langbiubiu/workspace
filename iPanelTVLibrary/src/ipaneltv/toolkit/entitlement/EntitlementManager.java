package ipaneltv.toolkit.entitlement;

import ipaneltv.toolkit.IPanelLog;
import ipaneltv.toolkit.db.QueryHandler;
import ipaneltv.toolkit.entitlement.EntitlementDatabaseObjects.Entitlement;
import ipaneltv.toolkit.entitlement.EntitlementDatabaseObjects.EntitlementCursorHandler;
import ipaneltv.toolkit.entitlement.EntitlementDatabaseObjects.Product;
import ipaneltv.toolkit.entitlement.EntitlementDatabaseObjects.ProductRecordCursorHandler;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import android.content.ContentProviderOperation;
import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Context;
import android.database.ContentObserver;
import android.net.Uri;
import android.net.telecast.ca.ChannelEntitlementUri;
import android.net.telecast.ca.EntitlementDatabase.EntitlementType;
import android.net.telecast.ca.EntitlementDatabase.Entitlements;
import android.net.telecast.ca.EntitlementDatabase.ProductType;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.Process;
import android.util.SparseArray;

public abstract class EntitlementManager {
	private static final String TAG = EntitlementManager.class.getSimpleName();
	//private static final int SparseArray = 0;
	private HandlerThread procThread = new HandlerThread(TAG);
	private Handler procHandler;
	private Context context;
	private Uri entUri, productUri;

	private SparseArray<SparseArray<Entitlement>> ents = new SparseArray<SparseArray<Entitlement>>();
	public int processId = -1;
	private ContentObserver productObserver = null;
	private ContentResolver resolver;
	private List<Integer> caSystemIds = new ArrayList<Integer>();

	public EntitlementManager(Context context, Uri entTableUri, Uri productTableUri) {
		this.context = context;
		this.entUri = entTableUri;
		this.productUri = productTableUri;
		resolver = context.getContentResolver();
		processId = Process.myPid();
		procThread.start();
		procHandler = new Handler(procThread.getLooper());
	}

	public Context getContext() {
		return context;
	}

	public synchronized void close() {
		procThread.getLooper().quit();
		ents = null;
	}

	protected abstract EntitlementCursorHandler createLoadEntitlementCursorHandler(Context context,
			Uri uri, Handler h);

	protected abstract ProductRecordCursorHandler createLoadProductRecordCursorHandler(
			Context context, Uri uri, Handler h);

	protected List<Integer> getCaSystemIds() {
		return caSystemIds;
	}

	protected abstract int[] getModuleCaSystemIds(int sn);

	/**
	 * 修改变化的entitlements的信息
	 * 
	 * @param moduleSn
	 * @param list
	 *            列表 , Entitlement只需包括 int productId, int ent, long start, long
	 *            end ,opid
	 */
	public void updateEntitlements(final int moduleSn, final List<Entitlement> list) {
		IPanelLog.i(TAG, "updateEntitlements-----moduleSn-----"+moduleSn);
		procHandler.post(new Runnable() {
			@Override
			public void run() {
				//拿到变化的Entitlement集合 key:product_id：
				SparseArray<Entitlement> es = checkEntitlementsChanged(moduleSn, list);
				if (es != null)
					postLoadProduct(moduleSn, es);
			}
		});
	}

	/**
	 * 对于ent manager的我做了检查，如果给出来的和上次是一样的，那么就不再发起数据库更改了
	 * @param sn
	 * @param list
	 * @return
	 */
	private SparseArray<Entitlement> checkEntitlementsChanged(int sn, List<Entitlement> list) {
		IPanelLog.i(TAG, "checkEntitlementsChanged------list.size:"+list.size());
		SparseArray<Entitlement> ret = new SparseArray<Entitlement>();
		for (Entitlement e : list) {
			ret.put(e.productId, e);
		}
		SparseArray<Entitlement> es = ents.get(sn);
		if(es==null) return ret;
		if (es.size() != ret.size())
			return ret;
		int n = es.size();
		for (int i = 0; i < n; i++) {
			Entitlement e1 = ret.valueAt(i);
			Entitlement e2 = es.get(ret.keyAt(i));
			if (e1 != e2)
				return ret;
			if (e1 == null)
				continue;
			if (e1.start != e2.start || e1.end != e2.end || e1.opid != e2.opid
					|| e1.value != e2.value)
				return ret;
		}
		return null;
	}

	private void procUpdateEntOneModule(ArrayList<ContentProviderOperation> operations,
			int moduleSn, SparseArray<Entitlement> es, SparseArray<List<Product>> pds) {
		IPanelLog.i(TAG, "procUpdateEntOneModule-----");
		if(pds==null)//表示ecms表数据空了
			return;
		ContentProviderOperation op;
		String selection = Entitlements.MODULE_SN + "=?";
		String[] selectionArgs = new String[] { String.valueOf(moduleSn) };
		op = ContentProviderOperation.newDelete(entUri).withSelection(selection, selectionArgs)
				.build();
		operations.add(op);// 先删除原来的

		for (int k = 0; k < pds.size(); k++) {
			int pid = pds.keyAt(k);
			List<Product> plist = pds.valueAt(k);
			Entitlement e = es.get(pid);
			if (e != null) {// 已授权
				for (Product p : plist) {
					ContentValues v = new ContentValues();
					v.put(Entitlements.PRODUCT_ID, e.productId);
					v.put(Entitlements.PRODUCT_TYPE, ProductType.TYPE_CHANNEL);
					// v.put(Entitlements.ENTITLEMENT, e.value);
					v.put(Entitlements.ENTITLEMENT, EntitlementType.TYPE_AVAILABLE);
					String enturi = ChannelEntitlementUri.createUriString(p.key.getFrequency(),
							p.key.getProgram());
					v.put(Entitlements.PRODUCT_URI, enturi);
					v.put(Entitlements.START_TIME, e.start);
					v.put(Entitlements.END_TIME, e.end);
					v.put(Entitlements.NETWORK_OPERATOR_ID, e.opid);
					v.put(Entitlements.MODULE_SN, moduleSn);
					op = ContentProviderOperation.newInsert(entUri).withYieldAllowed(true)
							.withValues(v).build();
					operations.add(op);
				}
			} else {// 未授权
				for (Product p : plist) {
					ContentValues v = new ContentValues();
					v.put(Entitlements.PRODUCT_ID, p.productId);
					v.put(Entitlements.PRODUCT_TYPE, ProductType.TYPE_CHANNEL);
					v.put(Entitlements.ENTITLEMENT, EntitlementType.TYPE_DIALOG_REQUIRED);
					String enturi = ChannelEntitlementUri.createUriString(p.key.getFrequency(),
							p.key.getProgram());
					v.put(Entitlements.PRODUCT_URI, enturi);
					v.put(Entitlements.MODULE_SN, moduleSn);
					op = ContentProviderOperation.newInsert(entUri).withYieldAllowed(true)
							.withValues(v).build();
					operations.add(op);
				}
			}

		}

	}

	private SparseArray<List<Product>> getModuleProducts(int moduleSn,
			SparseArray<SparseArray<List<Product>>> pdss) {
		int[] ids = getModuleCaSystemIds(moduleSn);
		IPanelLog.i(TAG, "ids.length----"+ids.length);
		for (int i = 0; i < ids.length; i++) {
			IPanelLog.i(TAG, "casystemId----"+ids[i]);
			SparseArray<List<Product>> pds = pdss.get(ids[i]);
			if (pds != null) {
				return pds;
			}
		}
		return null;
	}

	/**
	 * 
	 * @param moduleSn
	 * @param es
	 * @param pdss
	 */
	private void procUpdateEnt(int moduleSn, SparseArray<Entitlement> es,
			SparseArray<SparseArray<List<Product>>> pdss) {
		IPanelLog.i(TAG, "procUpdateEnt-----moduleSn--------: "+moduleSn);
		ArrayList<ContentProviderOperation> operations = new ArrayList<ContentProviderOperation>();
		if (moduleSn >= 0) {
			ents.put(moduleSn, es);
			SparseArray<List<Product>> pds = getModuleProducts(moduleSn, pdss);
			procUpdateEntOneModule(operations, moduleSn, es, pds);
		} else {
			IPanelLog.i(TAG, "update action from ecm--------: ");
			int n = ents.size();
			for (int i = 0; i < n; i++) {
				es = ents.valueAt(i);
				moduleSn = ents.keyAt(i);
				SparseArray<List<Product>> pds = getModuleProducts(moduleSn, pdss);
				procUpdateEntOneModule(operations, moduleSn, es, pds);
			}
		}
		try {
			resolver.applyBatch(entUri.getAuthority(), operations);
			resolver.notifyChange(entUri, null);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	private void postLoadProduct(final int moduleSn, final SparseArray<Entitlement> es) {
		IPanelLog.i(TAG, "postLoadProduct----");
		final ProductRecordCursorHandler ch = createLoadProductRecordCursorHandler(context,
				productUri, procHandler);
		ch.setQueryHandler(new QueryHandler() {
			@Override
			public void onQueryStart() {
				setProductObserver();
			}

			@Override
			public void onQueryEnd() {
				//外层key:casystemid,内层key:product_id
				SparseArray<SparseArray<List<Product>>> pdss = new SparseArray<SparseArray<List<Product>>>();
				SparseArray<List<Product>> pds;
				IPanelLog.i(TAG, "data from ecms table product——size():"+ ch.products.size());//219
				for (Product p : ch.products.values()) {
					if ((pds = pdss.get(p.caSystemId)) == null) {
						pds = new SparseArray<List<Product>>();
						pdss.put(p.caSystemId, pds);
					}
					List<Product>  lt = pds.get(p.productId);
					if(lt==null){
						lt = new ArrayList<Product>();
						pds.put(p.productId, lt);
						lt.add(p);
					}else{
						if(!lt.contains(lt))
							lt.add(p);
					}
					
//					pds.put(p.productId, p);//这里会从219条变到117。
				}
				procUpdateEnt(moduleSn, es, pdss);
			}
		});
		ch.postQuery();
	}

	private void setProductObserver() {
		IPanelLog.i(TAG, " setProductObserver");
		if (productObserver != null)
			return;
		productObserver = new ContentObserver(procHandler) {
			public void onChange(boolean selfChange) {
				IPanelLog.i(TAG, "product table changed.");
				postLoadProduct(-1, null);
			};
		};
		context.getContentResolver().registerContentObserver(productUri, false, productObserver);
	}

	public interface EntitlementManagerListener {
		public void productIdChange(HashMap<String, Entitlement> changeMap);
	}
	
}
