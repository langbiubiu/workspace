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
 * DSMCC����������
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
	 * ����ʵ��
	 * 
	 * @param ctx
	 *            ������
	 * @return ����
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
	 * ȷ�Ϸ����ѷ�������
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
	 * ���������������
	 * 
	 * @param uuid
	 *            ����UUID
	 * @param freq
	 *            Ƶ��
	 * @return ����
	 */
	public Task createTask(String uuid, long freq) {
		return new Task(uuid, freq);
	}

	/**
	 * ���÷���״̬������
	 * 
	 * @param ssl
	 *            ����
	 */
	public void setServiceStateListener(ServiceStateListener l) {
		ssl = l;
	}

	/**
	 * �������״̬������
	 */
	public static interface ServiceStateListener {
		/**
		 * �������״̬�仯
		 * 
		 * @param b
		 *            trueΪ����,����Ϊ������
		 */
		void onServiceAvailable(boolean b);
	}

	/**
	 * ������ش�����
	 * 
	 */
	public static interface TaskLoadingHandler {
		/**
		 * ����ʧ��
		 * 
		 * @param code
		 *            ����
		 * @param estr
		 *            ��Ϣ�ַ���,����Ϊnull
		 */
		void onTaskFailed(int code, String estr);

		/**
		 * ���������
		 * 
		 * @param update
		 *            �����Ƿ��и���,����ʱ�������İ汾��-1,��updateΪtrue
		 */
		void onTaskFinish(boolean update, int len);
	}

	/**
	 * ����״̬������
	 */
	public static interface TaskStateListener {
		/**
		 * �������
		 * <p>
		 * ��ͨ������Ϊ������Ƶ�ʲ��ɴ������
		 * 
		 * @param t
		 *            ����
		 */
		void onTaskPaused(Task t);

		/**
		 * ����ָ�
		 * 
		 * @param t
		 *            ����
		 */
		void onTaskResumed(Task t);
	}

	/**
	 * �������
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
		 * �õ���������������������
		 * 
		 * @return ����
		 */
		public DsmccDownloader getDownloader() {
			return DsmccDownloader.this;
		}

		/**
		 * �ͷ�������Դ
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
		 * �õ�ϡ�������key
		 * 
		 * @return ֵ
		 */
		public int getSparseKey() {
			return taskid;
		}

		/**
		 * �õ�����UUID
		 * 
		 * @return ֵ
		 */
		public String getNetworkUUID() {
			return uuid;
		}

		/**
		 * �õ������ڵ�Ƶ��
		 * 
		 * @return ֵ
		 */
		public long getFrequency() {
			return freq;
		}

		/**
		 * ����������ش�����
		 * 
		 * @param h
		 *            ����
		 */
		public void setLoadingHandler(TaskLoadingHandler h) {
			this.loadcb = h;
		}

		/**
		 * �õ������ڴ��ļ�
		 * 
		 * @return ����
		 */
		public MemoryFile getDataFile() {
			return data;
		}

		/**
		 * �õ�Ԫ�����ڴ��ļ�
		 * 
		 * @return ����
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
		 * ������Դ׼������
		 * 
		 * @return �ɹ�����true,���򷵻�false
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
		 * ɾ��������
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
		 * �Ƿ��ѳ��л�����
		 * 
		 * @return �Ƿ���true,���򷵻�false
		 */
		public boolean hasBuffers() {
			synchronized (tasks) {
				return fdseted;
			}
		}

		/**
		 * ��BigFile����FD
		 * 
		 * @return �Ƿ���-1,���򷵻�bigfileFd
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
		 * ��ȡBigFile����FD
		 * 
		 * @return �Ƿ���-1,���򷵻�bigfileFd
		 */
		public int getFd() {
			return bigfileFd;
		}
		
		/**
		 * ���õ�ǰ���������Ƿ�Ϊ���ļ�����
		 * 
		 * @return �Ƿ���-1,���򷵻�bigfileFd
		 */
		public void setIsBigFile(boolean flag) {
			 isBigFile = flag;
		}
		
		/**
		 * ��ȡ��ǰ����������ļ����ͱ�־
		 * 
		 * @return �Ƿ���-1,���򷵻�bigfileFd
		 */
		public boolean getIsBigFile() {
			 return isBigFile ;
		}

		/**
		 * ����Ĭ�ϴ�С�Ļ����� meta,data��1M������
		 * 
		 * @return �ɹ�����true,���򷵻�false
		 */
		public boolean setupBuffers() {
			return setupBuffers(1024 * 1024, 1024 * 1024);
		}

		/**
		 * ����������
		 * 
		 * @param metaLen
		 *            Ԫ���ݻ���������
		 * @param dataLen
		 *            ���ݻ���������
		 * @return �ɹ�����true,���򷵻�false
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
		 * ����DSMCC PMT����
		 * 
		 * @param pid
		 *            pmt��PID
		 * @return �޴뷵��true,���򷵻�false
		 */
		public boolean loadPmt(int pid) {
			return loadPmt(pid, -1, -1);// no version check
		}

		/**
		 * ����DSMCC PMT����
		 * 
		 * @param pid
		 *            pmt��PID
		 * @return �޴뷵��true,���򷵻�false
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
		 * ����DSMCC Dsi����
		 * 
		 * @param pid
		 *            dsi��PID
		 * @return �޴뷵��true,���򷵻�false
		 */
		public boolean loadDsi(int pid) {
			return loadDsi(pid, -1, -1);// no version check
		}

		/**
		 * ����DSMCC Dsi����
		 * 
		 * @param pid
		 *            dsi��PID
		 * @return �޴뷵��true,���򷵻�false
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
		 * ����DSMCC Dii����
		 * 
		 * @param pid
		 *            dii��PID
		 * @return �޴뷵��true,���򷵻�false
		 */
		public boolean loadDii(int pid, int dii_extid) {
			return loadDii(pid, dii_extid, -1, -1);// no verion check
		}

		/**
		 * ����DSMCC Dii����
		 * 
		 * @param pid
		 *            dii��PID
		 * @return �޴뷵��true,���򷵻�false
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
		 * ����DSMCC Module����
		 * 
		 * @param pid
		 *            module��PID
		 * @return �޴뷵��true,���򷵻�false
		 */
		public boolean loadModule(int pid, int moduleid) {
			IPanelLog.i(TAG, "loadModule in");
			return loadModule(pid, moduleid, -1, -1, -1);/*- no verion check*/
		}

		/**
		 * ����DSMCC Module����
		 * 
		 * @param pid
		 *            module��PID
		 * @return �޴뷵��true,���򷵻�false
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
		 * ����DSMCC BigFile����
		 * 
		 * @param pid
		 *            module��PID
		 * @return �޴뷵��true,���򷵻�false
		 */
		public boolean loadBigFile(int pid, int mids[], String path) {
			IPanelLog.i(TAG, "loadBigFile in"+","+mids[0]+","+mids[1]+","+mids[2]+","+mids[3]+","+mids[4]+","+mids[5]);
			return loadBigFile(pid, mids, path, -1, -1);/*- no verion check*/
		}

		/**
		 * ����DSMCC Module����
		 * 
		 * @param pid
		 *            module��PID
		 * @return �޴뷵��true,���򷵻�false
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
		 * ��ѯ�ɴ�״̬
		 * 
		 * @param lis
		 *            ����״̬������
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
