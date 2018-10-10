package ipaneltv.toolkit.dsmcc;

import ipaneltv.toolkit.IPanelLog;
import ipaneltv.toolkit.Natives;

import java.lang.ref.WeakReference;
import java.util.UUID;

import android.util.SparseArray;

/**
 * DSMCC下载
 */
public class DsmccGot {

	static final String TAG = "[java]DsmccDownloader";

	private int peer;// for native
	private SparseArray<Task> loadings = new SparseArray<DsmccGot.Task>();
	private String uuid;
	private long idm, ldl;
	private Task[] task;

	/**
	 * 创建实例
	 * 
	 * @param uuid
	 *            网络UUID
	 * @param maxTaskSize
	 *            最大任务数量
	 * @return 对象，如果失败返回null
	 */
	public static DsmccGot createDsmccGot(String uuid, int maxTaskSize) {
		try {
			return new DsmccGot(uuid, maxTaskSize);
		} catch (Exception e) {
			IPanelLog.e(TAG, "createDsmccGot error:" + e);
		}
		return null;
	}

	DsmccGot(String uuid, int max) {
		UUID id = UUID.fromString(uuid);
		this.uuid = id.toString();
		idm = id.getMostSignificantBits();
		ldl = id.getLeastSignificantBits();
		if (max <= 0 || max > 32)
			throw new IllegalArgumentException("max consize invalid:" + max);
		task = new Task[max];
		if (init(new WeakReference<DsmccGot>(this), max) == false) {
			throw new RuntimeException("open failed!");
		}
	}

	/**
	 * 释放资源
	 */
	public void release() {
		synchronized (loadings) {
			Task t;
			int n = loadings.size();
			for (int i = 0; i < n; i++) {
				t = loadings.valueAt(i);
				t.cancel();
			}
			loadings.clear();
		}
		synchronized (task) {
			for (int i = 0; i < task.length; i++) {
				Task t = task[i];
				if (t != null) {
					t.release();
				}
			}
			exit();
		}
	}

	/**
	 * 获得网络UUID
	 * 
	 * @return 值
	 */
	public String getNetworkUUID() {
		return uuid;
	}

	/**
	 * 最大任务数量
	 * 
	 * @return 值
	 */
	public int maxTaskSize() {
		return task.length;
	}

	/**
	 * 创建新的Task
	 * 
	 * @param metaBufSize
	 *            元数据缓冲区大小
	 * @param dataBufSize
	 *            数据缓冲区大小
	 * @return 对象,失败的则返回null
	 */
	public Task createTask(int metaBufSize, int dataBufSize) {
		Task t = null;
		synchronized (task) {
			for (int i = 0; i < task.length; i++) {
				if (task[i] == null) {
					if ((t = createTask(i, metaBufSize, dataBufSize)) != null)
						task[i] = t;
					break;
				}
			}
		}
		return t;
	}

	/**
	 * 得到指定索引的任务
	 * 
	 * @param index
	 *            索引
	 * @return 对象，如果没有则返回null
	 */
	public Task getTask(int index) {
		synchronized (task) {
			return task[index];
		}
	}

	@Override
	protected void finalize() throws Throwable {
		try {
			release();
		} catch (Exception e) {
		}
		super.finalize();
	}

	private native boolean init(WeakReference<DsmccGot> wo, int max);

	private native void exit();

	native int ldpmt(long f, int pid, int v, int p, int len, int tout);

	native int lddsi(long f, int pid, int v, int p, int len, int tout);

	native int lddii(long f, int pid, int tid, int v, int p, int len, int tout);

	native int ldmod(long f, int pid, int mid, int v, int p, int len, int buf, int blen, int tout);

	native int ldcan(int handle);

	static final int MSG_OBJ_SUCC = 1;
	static final int MSG_OBJ_FAIL = 2;

	void onCallback(int oid, int msg, int p1, int p2) {
		Task t = null;
		synchronized (loadings) {
			if ((t = loadings.get(oid)) != null) {
				t.onLoadingOver();
			}
		}
		if (t != null) {
			switch (msg) {
			case MSG_OBJ_SUCC:
				t.notifyLoadingSuccess(p1 == 0, p2);
				break;
			case MSG_OBJ_FAIL:
				t.notifyLoadingFailed(p1);
			default:
				break;
			}
		}
	}

	static void native_callback(Object o, int oid, int msg, int p1, int p2) {
		try {
			@SuppressWarnings("unchecked")
			WeakReference<DsmccGot> wo = (WeakReference<DsmccGot>) o;
			if (wo == null)
				return;
			DsmccGot d = wo.get();
			if (d == null)
				return;
			d.onCallback(oid, msg, p1, p2);
		} catch (Exception e) {
			IPanelLog.e(TAG, "native_callback error:" + e);
		}
	}

	private Task createTask(int index, int mbs, int dbs) {
		try {
			return new Task(index, mbs, dbs);
		} catch (Exception e) {
			IPanelLog.e(TAG, "create Task error:" + e);
		}
		return null;
	}

	/**
	 * 加载监听器
	 */
	public static interface LoadingListener {
		/**
		 * 加载失败
		 * 
		 * @param ecode
		 *            错误码
		 */
		void onLoadingFailed(int ecode);

		/**
		 * 加载成功
		 * 
		 * @param updated
		 *            数据是否更新
		 * @param metalen
		 *            元数据长度
		 */
		void onLoadingSuccess(boolean updated, int metalen);
	}

	/**
	 * 加载任务对象
	 */
	public class Task extends Natives {

		private int handle = -1;
		int index;
		private int mbuf, dbuf, mbs, dbs;
		LoadingListener ldl;

		Task(int index, int mbs, int dbs) {
			this.index = index;
			if (mbs == 0 && dbs == 0)
				throw new IllegalArgumentException();
			if (mbs > 0)
				mbuf = Natives.malloc(mbs);
			if (dbs > 0)
				dbuf = Natives.malloc(dbs);
			if ((mbs > 0 && mbuf == 0) || (dbs > 0 && mbuf == 0)) {
				release();
				throw new RuntimeException("not enought memory");
			}
		}

		public void release() {
			try {
				cancel();
				if (mbuf != 0)
					Natives.free(mbuf);
				if (dbuf != 0)
					Natives.free(dbuf);
				mbuf = dbuf = mbs = dbs = 0;
				task[index] = null;
				index = -1;
			} catch (Exception e) {
			}
		}

		void onLoadingOver() {
			if (handle != -1) {
				if (loadings.get(handle) == this)
					loadings.remove(handle);
				handle = -1;
			}
		}

		void notifyLoadingFailed(int ecode) {
			LoadingListener l = ldl;
			if (l != null) {
				try {
					l.onLoadingFailed(ecode);
				} catch (Exception e) {
					IPanelLog.e(TAG, "onLoadingFailed error:" + e);
				}
			}
		}

		void notifyLoadingSuccess(boolean update, int metalen) {
			LoadingListener l = ldl;
			if (l != null) {
				try {
					l.onLoadingSuccess(update, metalen);
				} catch (Exception e) {
					IPanelLog.e(TAG, "onLoadingSuccess error:" + e);
				}
			}
		}

		/**
		 * 设置监听器
		 * 
		 * @param l
		 *            对象
		 */
		public void setLoadingListener(LoadingListener l) {
			this.ldl = l;
		}

		/**
		 * 元数据缓冲区大小
		 * 
		 * @return
		 */
		public int metaBufferSize() {
			return mbs;
		}

		public int dataBufferSize() {
			return dbs;
		}

		public int getIndex() {
			return index;
		}

		public boolean cancel() {
			synchronized (loadings) {
				if (handle != -1) {
					ldcan(handle);
					handle = -1;
					return true;
				}
			}
			return false;
		}

		public boolean isLoading() {
			synchronized (loadings) {
				return handle != -1;
			}
		}

		public boolean loadPmt(long freq, int pid) {
			return loadPmt(freq, pid, -1, -1);
		}

		public boolean loadPmt(long freq, int pid, int version, int timeout) {
			synchronized (loadings) {
				if (handle != -1)
					throw new IllegalStateException("task is loading...");
				if ((handle = ldpmt(freq, pid, version, mbuf, mbs, timeout)) != -1) {
					loadings.put(handle, this);
					return true;
				}
			}
			return false;
		}

		public boolean loadDsi(long freq, int pid) {
			return loadDsi(freq, pid, -1, -1);
		}

		public boolean loadDsi(long freq, int pid, int version, int timeout) {
			synchronized (loadings) {
				if (handle != -1)
					throw new IllegalStateException("task is loading...");
				if ((handle = lddsi(freq, pid, version, mbuf, mbs, timeout)) != -1) {
					loadings.put(handle, this);
					return true;
				}
			}
			return false;
		}

		public boolean loadDii(long freq, int pid, int transid) {
			return loadDii(freq, pid, transid, -1, -1);
		}

		public boolean loadDii(long freq, int pid, int transid, int version, int timeout) {
			synchronized (loadings) {
				if (handle != -1)
					throw new IllegalStateException("task is loading...");
				if ((handle = lddii(freq, pid, transid, version, mbuf, mbs, timeout)) != -1) {
					loadings.put(handle, this);
					return true;
				}
			}
			return false;
		}

		public boolean loadModule(long freq, int pid, int modid) {
			return loadModule(freq, pid, modid, -1, -1);
		}

		public boolean loadModule(long freq, int pid, int modid, int version, int timeout) {
			synchronized (loadings) {
				if (handle != -1)
					throw new IllegalStateException("task is loading...");
				if ((handle = ldmod(freq, pid, modid, version, mbuf, mbs, dbuf, dbs, timeout)) != -1) {
					loadings.put(handle, this);
					return true;
				}
			}
			return false;
		}
	}
}