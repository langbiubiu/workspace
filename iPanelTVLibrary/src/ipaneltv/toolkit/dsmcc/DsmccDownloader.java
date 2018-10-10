package ipaneltv.toolkit.dsmcc;

import ipaneltv.toolkit.HideExploder;
import ipaneltv.toolkit.IPanelLog;

import java.io.IOException;
import java.util.UUID;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.IBinder;
import android.os.MemoryFile;
import android.os.ParcelFileDescriptor;
import android.os.RemoteException;
import android.util.SparseArray;

/**
 * DSMCC下载器对象
 */
public class DsmccDownloader {
	public static final String SERVICE_NAME = "ipaneltv.toolkit.dsmcc.downloadservice";
	static final String TAG = "[cb]DsmccDownloader";
	private ServiceStateListener ssl;
	final Object mutex = new Object();
	SparseArray<Task> tasks = new SparseArray<Task>();
	IDsmccDownloader mService = null;
	Context ctx;
	int binding = 0;

	/**
	 * 创建实例
	 * 
	 * @param ctx
	 *            上下文
	 * @return 对象
	 */
	public static DsmccDownloader createInstance(Context ctx) {
		try {
			return new DsmccDownloader(ctx);
		} catch (Exception e) {
			IPanelLog.e(TAG, "createInstance error:" + e);
		}
		return null;
	}

	DsmccDownloader(Context ctx) throws IOException {
		this.ctx = ctx;
	}

	/**
	 * 确认服务已发起连接
	 */
	public void ensure() {
		synchronized (mutex) {
			if (binding == 0) {
				Intent i = new Intent(SERVICE_NAME);
				if (ctx.bindService(i, conn, Context.BIND_AUTO_CREATE)) {
					binding = 1;
				}
			}
		}
	}

	/**
	 * 创建下载任务对象
	 * 
	 * @param uuid
	 *            网络UUID
	 * @param freq
	 *            频率
	 * @return 对象
	 */
	public Task createTask(String uuid, long freq) {
		return new Task(uuid, freq);
	}

	/**
	 * 设置服务状态监听器
	 * 
	 * @param ssl
	 *            对象
	 */
	public void setServiceStateListener(ServiceStateListener l) {
		ssl = l;
	}

	/**
	 * 服务可用状态监听器
	 */
	public static interface ServiceStateListener {
		/**
		 * 服务可用状态变化
		 * 
		 * @param b
		 *            true为可用,否则为不可用
		 */
		void onServiceAvailable(boolean b);
	}

	/**
	 * 任务加载处理者
	 * 
	 */
	public static interface TaskLoadingHandler {
		/**
		 * 任务失败
		 * 
		 * @param code
		 *            代码
		 * @param estr
		 *            消息字符串,可能为null
		 */
		void onTaskFailed(int code, String estr);

		/**
		 * 任务已完成
		 * 
		 * @param update
		 *            数据是否有更新,下载时如果传入的版本是-1,则update为true
		 */
		void onTaskFinish(boolean update, int len);
	}

	/**
	 * 任务状态监听器
	 */
	public static interface TaskStateListener {
		/**
		 * 任务挂起
		 * <p>
		 * 这通常是因为流所在频率不可达引起的
		 * 
		 * @param t
		 *            任务
		 */
		void onTaskPaused(Task t);

		/**
		 * 任务恢复
		 * 
		 * @param t
		 *            任务
		 */
		void onTaskResumed(Task t);
	}

	/**
	 * 任务对象
	 */
	public class Task {

		private String uuid;
		private long freq;
		private int taskid = -1;
		private TaskLoadingHandler loadcb = null;
		private TaskStateListener statecb = null;
		private boolean running = false, reserved = false, fdseted = false;
		private MemoryFile data = null, meta = null;
		private int metaBufLen = 0, dataBufLen = 0;
		private int bigfileFd = -1;
		private boolean isBigFile = false;

		Task(String uuid, long freq) {
			if (freq == 0)
				throw new IllegalArgumentException();
			this.uuid = UUID.fromString(uuid).toString();
			this.freq = freq;
		}

		/**
		 * 得到任务所属的下载器对象
		 * 
		 * @return 对象
		 */
		public DsmccDownloader getDownloader() {
			return DsmccDownloader.this;
		}

		/**
		 * 释放任务资源
		 */
		public void release() {
			synchronized (tasks) {
				if (reserved) {
					if (tasks.get(taskid) == this) {
						closeObject(taskid);
						taskid = -1;
					}
				}
				clearFiles();
			}
		}

		void clearFiles() {
			try {
				if (data != null) {
					data.close();
					data = null;
				}
			} catch (Exception e) {
			}
			try {
				if (meta != null) {
					meta.close();
					meta = null;
				}
			} catch (Exception e) {
			}
		}

		/**
		 * 得到稀疏数组的key
		 * 
		 * @return 值
		 */
		public int getSparseKey() {
			return taskid;
		}

		/**
		 * 得到网络UUID
		 * 
		 * @return 值
		 */
		public String getNetworkUUID() {
			return uuid;
		}

		/**
		 * 得到流所在的频率
		 * 
		 * @return 值
		 */
		public long getFrequency() {
			return freq;
		}

		/**
		 * 设置任务加载处理者
		 * 
		 * @param h
		 *            对象
		 */
		public void setLoadingHandler(TaskLoadingHandler h) {
			this.loadcb = h;
		}

		/**
		 * 得到数据内存文件
		 * 
		 * @return 对象
		 */
		public MemoryFile getDataFile() {
			return data;
		}

		/**
		 * 得到元数据内存文件
		 * 
		 * @return 对象
		 */
		public MemoryFile getMetaFile() {
			return meta;
		}

		boolean setupFiles(int mlen, int dlen) {
			try {
				if (mlen > 0)
					meta = new MemoryFile("Meta@" + this.hashCode(), mlen);
				if (dlen > 0)
					data = new MemoryFile("Data@" + this.hashCode(), dlen);
				meta.allowPurging(false);
				data.allowPurging(false);
				metaBufLen = mlen;
				dataBufLen = dlen;
				return true;
			} catch (Exception e) {
				cleanFiles();
			}
			return false;
		}

		void cleanFiles() {
			try {
				metaBufLen = 0;
				if (meta != null)
					meta.close();
			} catch (Exception e) {
			}
			try {
				dataBufLen = 0;
				if (data != null)
					data.close();
			} catch (Exception e) {
			}
		}

		/**
		 * 保留资源准备下载
		 * 
		 * @return 成功返回true,否则返回false
		 */
		public boolean reserve() {
			synchronized (tasks) {
				if (reserved)
					throw new IllegalStateException("already reserved");
				try {
					if ((taskid = openObject(uuid, freq)) >= 0) {
						tasks.put(taskid, this);
						reserved = true;
					}
				} finally {
					if (!reserved)
						clearFiles();
				}
			}
			return reserved;
		}

		/**
		 * 删除缓冲区
		 */
		public void removeBuffers() {
			synchronized (tasks) {
				if (reserved && fdseted) {
					cleanFiles();
					setObjectFds(taskid, null, 0, null, 0);
				}
			}
		}

		/**
		 * 是否已持有缓冲区
		 * 
		 * @return 是返回true,否则返回false
		 */
		public boolean hasBuffers() {
			synchronized (tasks) {
				return fdseted;
			}
		}

		/**
		 * 打开BigFile任务FD
		 * 
		 * @return 是返回-1,否则返回bigfileFd
		 */
		public int openFd(String filePath) {
			synchronized (tasks) {
				if (mService != null)
					try {
						bigfileFd = mService.objectOpenFd(taskid, filePath);
					} catch (RemoteException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
			}
			return bigfileFd;
		}

		/**
		 * 获取BigFile任务FD
		 * 
		 * @return 是返回-1,否则返回bigfileFd
		 */
		public int getFd() {
			return bigfileFd;
		}
		
		/**
		 * 设置当前下载任务是否为大文件下载
		 * 
		 * @return 是返回-1,否则返回bigfileFd
		 */
		public void setIsBigFile(boolean flag) {
			 isBigFile = flag;
		}
		
		/**
		 * 获取当前下载任务的文件类型标志
		 * 
		 * @return 是返回-1,否则返回bigfileFd
		 */
		public boolean getIsBigFile() {
			 return isBigFile ;
		}

		/**
		 * 建立默认大小的缓冲区 meta,data各1M缓冲区
		 * 
		 * @return 成功返回true,否则返回false
		 */
		public boolean setupBuffers() {
			return setupBuffers(1024 * 1024, 1024 * 1024);
		}

		/**
		 * 建立缓冲区
		 * 
		 * @param metaLen
		 *            元数据缓冲区长度
		 * @param dataLen
		 *            数据缓冲区长度
		 * @return 成功返回true,否则返回false
		 */
		public boolean setupBuffers(int metaLen, int dataLen) {
			if (metaLen < 0 || dataLen < 0 || (metaLen == 0 && dataLen == 0))
				throw new IllegalArgumentException();
			try {
				synchronized (tasks) {
					IPanelLog.i(TAG, "setupBuffers reserved=" + reserved + ",fdseted=" + fdseted);
					if (reserved && !fdseted) {
						if (setupFiles(metaLen, dataLen)) {
							ParcelFileDescriptor m = null, d = null;
							if (meta != null) {
								m = ParcelFileDescriptor.dup(HideExploder
										.getFileDescriptorFrom(meta));
							}
							if (data != null) {
								d = ParcelFileDescriptor.dup(HideExploder
										.getFileDescriptorFrom(data));
							}
							fdseted = setObjectFds(taskid, m, metaBufLen, d, dataBufLen);
						}
					}
				}
			} catch (Exception e) {
				IPanelLog.e(TAG, "setupBuffers error:" + e);
			} finally {
				if (!fdseted)
					cleanFiles();
			}
			return fdseted;
		}

		/**
		 * 加载DSMCC PMT数据
		 * 
		 * @param pid
		 *            pmt的PID
		 * @return 无措返回true,否则返回false
		 */
		public boolean loadPmt(int pid) {
			return loadPmt(pid, -1, -1);// no version check
		}

		/**
		 * 加载DSMCC PMT数据
		 * 
		 * @param pid
		 *            pmt的PID
		 * @return 无措返回true,否则返回false
		 */
		public boolean loadPmt(int pid, int ver, int timeout) {
			boolean ret = false;
			synchronized (tasks) {
				try {
					if (reserved && !running && tasks.get(taskid) == this) {
						ret = receivePmt(taskid, pid, ver, timeout);
					}
				} finally {
					if (ret)
						running = true;
				}
			}
			return ret;
		}

		/**
		 * 加载DSMCC Dsi数据
		 * 
		 * @param pid
		 *            dsi的PID
		 * @return 无措返回true,否则返回false
		 */
		public boolean loadDsi(int pid) {
			return loadDsi(pid, -1, -1);// no version check
		}

		/**
		 * 加载DSMCC Dsi数据
		 * 
		 * @param pid
		 *            dsi的PID
		 * @return 无措返回true,否则返回false
		 */
		public boolean loadDsi(int pid, int ver, int timeout) {
			boolean ret = false;
			synchronized (tasks) {
				try {
					IPanelLog.i(TAG, "loadDsi in.");
					IPanelLog.i(TAG, "reserved:" + reserved + ",running:" + running);
					running = false;
					if (reserved && !running && tasks.get(taskid) == this) {
						ret = receiveDsi(taskid, pid, ver, timeout);
					}
				} finally {
					if (ret)
						running = true;
				}
			}
			return ret;
		}

		/**
		 * 加载DSMCC Dii数据
		 * 
		 * @param pid
		 *            dii的PID
		 * @return 无措返回true,否则返回false
		 */
		public boolean loadDii(int pid, int dii_extid) {
			return loadDii(pid, dii_extid, -1, -1);// no verion check
		}

		/**
		 * 加载DSMCC Dii数据
		 * 
		 * @param pid
		 *            dii的PID
		 * @return 无措返回true,否则返回false
		 */
		public boolean loadDii(int pid, int dii_extid, int ver, int timeout) {
			boolean ret = false;
			synchronized (tasks) {
				try {
					if (reserved && !running && tasks.get(taskid) == this) {
						ret = receiveDii(taskid, pid, dii_extid, ver, timeout);
					}
				} finally {
					if (ret)
						running = true;
				}
			}
			return ret;
		}

		/**
		 * 加载DSMCC Module数据
		 * 
		 * @param pid
		 *            module的PID
		 * @return 无措返回true,否则返回false
		 */
		public boolean loadModule(int pid, int moduleid) {
			IPanelLog.i(TAG, "loadModule in");
			return loadModule(pid, moduleid, -1, -1, -1);/*- no verion check*/
		}

		/**
		 * 加载DSMCC Module数据
		 * 
		 * @param pid
		 *            module的PID
		 * @return 无措返回true,否则返回false
		 */
		public boolean loadModule(int pid, int moduleid, int moudle_version, int module_size,
				int timeout) {
			boolean ret = false;
			synchronized (tasks) {
				try {
					if (reserved && !running && tasks.get(taskid) == this)
						ret = receiveModule(taskid, pid, moduleid, moudle_version, module_size,
								timeout);
				} finally {
					if (ret) {
						IPanelLog.i(TAG, "122225running =" + running);
						running = true;
						IPanelLog.i(TAG, "122225running =" + running);
					}
				}
			}
			return ret;
		}

		/**
		 * 加载DSMCC BigFile数据
		 * 
		 * @param pid
		 *            module的PID
		 * @return 无措返回true,否则返回false
		 */
		public boolean loadBigFile(int pid, int mids[], String path) {
			IPanelLog.i(TAG, "loadBigFile in"+","+mids[0]+","+mids[1]+","+mids[2]+","+mids[3]+","+mids[4]+","+mids[5]);
			return loadBigFile(pid, mids, path, -1, -1);/*- no verion check*/
		}

		/**
		 * 加载DSMCC Module数据
		 * 
		 * @param pid
		 *            module的PID
		 * @return 无措返回true,否则返回false
		 */
		public boolean loadBigFile(int pid, int mids[], String path,int mver,int timeout) {
			boolean ret = false;
			synchronized (tasks) {
				try {
					if (reserved && tasks.get(taskid) == this)
						IPanelLog.i(TAG, "receiveBigFile in"+","+mids[0]+","+mids[1]+","+mids[2]+","+mids[3]+","+mids[4]+","+mids[5]);

						ret = receiveBigFile(taskid, pid, mids, path,mver, timeout);
				} finally {
					if (ret) {
						IPanelLog.i(TAG, "122225running =" + running);
						// running = true;
						IPanelLog.i(TAG, "122225running =" + running);
					}
				}
			}
			return ret;
		}

		/**
		 * 查询可达状态
		 * 
		 * @param lis
		 *            任务状态监听器
		 */
		public void queryReachable(TaskStateListener lis) {
			synchronized (tasks) {
				if (reserved && !running) {
					if (lis == null)
						throw new NullPointerException();
					this.statecb = lis;
					receiveQuery(taskid);
				}
			}
		}

		void onLoadFinish(boolean update, int len) {
			IPanelLog.i(TAG, "onLoadFinish call onTaskFinish IN tasks=" + tasks);
			synchronized (tasks) {
				running = false;
			}
			IPanelLog.i(TAG, "onLoadFinish call onTaskFinish IN 1");
			loadcb.onTaskFinish(update, len);
			IPanelLog.i(TAG, "onLoadFinish call onTaskFinish out");
		}

		void onLoadFailed(int code, String es) {
			synchronized (tasks) {
				running = false;
			}
			loadcb.onTaskFailed(code, es);
		}

		void onReachable(boolean b) {
			if (b)
				statecb.onTaskResumed(this);
			else
				statecb.onTaskPaused(this);
		}

		@Override
		protected void finalize() throws Throwable {
			try {
				release();
			} catch (Throwable e) {
			}
			super.finalize();
		}
	}

	int openObject(String netid, long freq) {
		try {
			if (mService != null) {
				int oid = mService.objectOpen(netid, freq);
				return oid;
			}
		} catch (Exception e) {
			IPanelLog.e(TAG, "openObject error:" + e);
		}
		return -1;
	}

	void closeObject(int oid) {
		try {
			if (mService != null) {
				mService.objectClose(oid);
			}
		} catch (Exception e) {
			IPanelLog.e(TAG, "closeObject error:" + e);
		}
	}

	boolean setObjectFds(int oid, ParcelFileDescriptor meta, int mlen, ParcelFileDescriptor data,
			int dlen) {
		try {
			if (mService != null) {
				IPanelLog.i(TAG, "setObjectFds oid=" + oid + " ,meta=" + meta.getFd() + ",mlen=" + mlen
						+ " ,data=" + data.getFd() + ",dlen=" + dlen);
				mService.objectSetFds(oid, meta, mlen, data, dlen);
				return true;
			}
		} catch (Exception e) {
			IPanelLog.e(TAG, "setObjectFds error:" + e);
		}
		return false;
	}

	boolean receivePmt(int oid, int pid, int ver, int tout) {
		try {
			if (mService != null)
				return mService.objectLoadPmt(oid, pid, ver, tout);
		} catch (Exception e) {
			IPanelLog.e(TAG, "receivePmt error:" + e);
		}
		return false;
	}

	boolean receiveDsi(int oid, int pid, int ver, int tout) {
		try {
			if (mService != null)
				return mService.objectLoadDsi(oid, pid, ver, tout);
		} catch (Exception e) {
			IPanelLog.e(TAG, "receiveDsi error:" + e);
		}
		return false;
	}

	boolean receiveDii(int oid, int pid, int dii_extid, int dii_ver, int tout) {
		try {
			if (mService != null)
				return mService.objectLoadDii(oid, pid, dii_extid, dii_ver, tout);
		} catch (Exception e) {
			IPanelLog.e(TAG, "receiveDii error:" + e);
		}
		return false;
	}

	boolean receiveModule(int oid, int pid, int moduleId, int moduleVersion, int moduleSize,
			int tout) {
		try {
			if (mService != null)
				return mService.objectLoadModule(oid, pid, moduleId, moduleVersion, moduleSize,
						tout);
		} catch (Exception e) {
			IPanelLog.e(TAG, "receiveModule error:" + e);
		}
		return false;
	}

	boolean receiveBigFile(int oid, int pid, int mids[], String path, int moduleVersion, int tout) {
		try {
			if (mService != null)
				return mService.objectLoadBigFile(oid, pid, mids, path, moduleVersion,tout);
		} catch (Exception e) {
			IPanelLog.e(TAG, "loadBigFile error:" + e);
		}
		return false;
	}

	boolean receiveQuery(int oid) {
		try {
			if (mService != null)
				mService.objectQueryReachable(oid);
		} catch (Exception e) {
			IPanelLog.e(TAG, "queryReachable error:" + e);
		}
		return false;
	}

	ServiceConnection conn = new ServiceConnection() {

		@Override
		public void onServiceDisconnected(ComponentName name) {
			synchronized (mutex) {
				mService = null;
				binding = 0;
				try {
					ssl.onServiceAvailable(false);
				} catch (Exception e) {
					IPanelLog.e(TAG, "onServiceAvailable(false) error:" + e);
				}
			}
		}

		@Override
		public void onServiceConnected(ComponentName name, IBinder service) {
			synchronized (mutex) {
				try {
					mService = IDsmccDownloader.Stub.asInterface(service);
					mService.setCallback(callback);
					binding = 2;
				} catch (Exception e) {
					IPanelLog.e(TAG, "onServiceConnected error:" + e);
					mService = null;
				}
				try {
					ssl.onServiceAvailable(true);
				} catch (Exception e) {
					IPanelLog.e(TAG, "onServiceAvailable(true) error:" + e);
				}
			}
			IPanelLog.i(TAG, "onServiceConnected service=" + service);
		}
	};

	IDsmccCallback callback = new IDsmccCallback.Stub() {

		@Override
		public void onLoadReachable(int oid, boolean b, int len) throws RemoteException {
			synchronized (tasks) {
				Task t = tasks.get(oid);
				if (t != null) {
					try {
						t.onReachable(b);
					} catch (Exception e) {
						IPanelLog.e(TAG, "onReachable error:" + e);
					}
				}
			}
		}

		@Override
		public void onLoadFailed(int oid, int code, String es) throws RemoteException {
			synchronized (tasks) {
				Task t = tasks.get(oid);
				if (t != null) {
					try {
						t.onLoadFailed(code, es);
					} catch (Exception e) {
						IPanelLog.e(TAG, "onLoadFailed error:" + e);
					}
				}
			}
		}

		@Override
		public void onLoadFinish(int oid, boolean update, int len) throws RemoteException {
			synchronized (tasks) {
				Task t = tasks.get(oid);
				if (t != null) {
					try {
						IPanelLog.i(TAG, "onLoadFinish in.");
						t.onLoadFinish(update, len);
						IPanelLog.i(TAG, "onLoadFinish out.");
					} catch (Exception e) {
						IPanelLog.e(TAG, "onLoadFinish error:" + e);
					}
				} else {
					IPanelLog.e(TAG, "onLoadFinish task==null");
				}
			}
		}
	};

}
