package android.net.telecast;

import java.io.File;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.UUID;

import android.os.FileObserver;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.Looper;
import android.os.Message;
import android.util.Log;

/**
 * 段数据存储
 * <p>
 * 对象分为<b>只读</b>和<b>可写</b>两种类型<br>
 * <li>只读类型,用来获取网络应用提供的段数据.通过添加监听器,可以实现对文件及目录变化的监控
 * <li>可写类型,可以进行写入删除等操作,此类型对象不提供文件变化的的监听消息
 */
public class SectionStorage {
	static final String TAG = "[java]SectionStorage";

	private Object mutex = new Object();
	private boolean observing = false, aviailable = true;
	private final boolean writable;
	MyFileObserver fobs;
	Handler handler = null;
	HandlerThread th = null;
	StorageStatusListener ssl = null;
	private final String root, uuid;

	/**
	 * 创建可写的段数据存储
	 * <p>
	 * 对象是可写类型
	 * 
	 * @param root
	 *            根目录路径
	 * @return 对象
	 */
	public static SectionStorage createWritableStorage(String root) {
		try {
			return new SectionStorage(null, root, true);
		} catch (Exception e) {
			Log.e(TAG, "create writable failed!");
			return null;
		}
	}

	/**
	 * 创建可读的段数据存储
	 * <p>
	 * 对象是可读类型
	 * 
	 * @param root
	 *            根目录路径
	 * @return 对象
	 */
	public static SectionStorage createReadableStorage(String root) {
		try {
			return new SectionStorage(null, root, false);
		} catch (Exception e) {
			Log.e(TAG, "create readable failed!");
			return null;
		}
	}

	SectionStorage(String uuid, String root, boolean writable) {
		this.writable = writable;
		this.uuid = (uuid == null) ? null : UUID.fromString(uuid).toString();
		File f = new File(root);
		try {
			this.root = f.getCanonicalPath();
		} catch (IOException e1) {
			throw new RuntimeException("invlaid root path:" + root);
		}
		if (writable) {
			initWritable(f);
		} else {
			initReadable(f);
		}
	}

	void initReadable(File f) {
		if (!f.isDirectory())
			throw new RuntimeException("root is not a dir!");
		if (!f.canRead())
			throw new RuntimeException("root can't read!");

		th = new HandlerThread("sectionStorage");
		th.start();
		handler = new Handler(th.getLooper(), callback);

		fobs = new MyFileObserver(root);
	}

	void initWritable(File f) {
		if (!f.exists()) {
			if (!f.mkdirs())
				throw new RuntimeException("create storage dir failed!");
		} else {
			if (!f.isDirectory())
				throw new RuntimeException("root is not a dir!");
		}
		if (!f.canWrite())
			throw new RuntimeException("root can't write!");
	}

	class MyFileObserver extends FileObserver {
		public MyFileObserver(String path) {
			super(path, FileObserver.CLOSE_WRITE | FileObserver.CREATE | FileObserver.DELETE
					| FileObserver.DELETE_SELF);
		}

		@Override
		public void onEvent(int event, String path) {
			synchronized (mutex) {
				int el = event & FileObserver.ALL_EVENTS;
				Log.i(TAG, "onEvent event:" + el + " path:" + path);
				if (aviailable) {
					try {
						handler.obtainMessage(el, path).sendToTarget();
					} catch (Exception e) {
						Log.e(TAG, "Storage FileObserver.onEvent error:" + e);
					}
				}
			}
		}
	};

	Handler.Callback callback = new Handler.Callback() {
		@Override
		public boolean handleMessage(Message msg) {
			Log.i(TAG, "callback msg.what=" + msg.what + " msg.obj:" + msg.obj);
			synchronized (mutex) {
				if (aviailable) {
					try {
						switch (msg.what) {
						case -1:// quit
							Looper.myLooper().quit();
							break;
						case -2:// runnable
							((Runnable) msg.obj).run();
							break;
						case FileObserver.CLOSE_WRITE:
							onFileWriteClosed((String) msg.obj);
							break;
						case FileObserver.CREATE:
							onFileCreate((String) msg.obj);
							break;
						case FileObserver.DELETE:
							onFileDeleted((String) msg.obj);
							break;
						case FileObserver.DELETE_SELF:
							onRootDeleted();
							release();
							break;
						}
					} catch (Throwable e) {
						Log.e(TAG, "handleMessage error:" + e);
					}
				}
			}
			return true;
		}
	};

	class ChangeInfo {
		long freq = -1;
		short tableId = -1;
		short pid = -1;
	}

	ChangeInfo withPath(String path) {
		try {
			ChangeInfo ci = new ChangeInfo();
			int i = path.indexOf('.');
			if (i <= 1)// format is freq-pid-tid.sections
				throw new RuntimeException("bad section file name format!");

			String[] sp = path.substring(0, i).split("-");
			if (sp == null ? true : sp.length < 3)
				throw new RuntimeException("bad file name format!");

			ci.freq = Long.parseLong(sp[0]);
			ci.pid = Short.parseShort(sp[1]);
			ci.tableId = Short.parseShort(sp[2]);
			return ci;
		} catch (Exception e) {
			Log.e(TAG, "path invalid:" + path);
		}
		return null;
	}

	void onFileWriteClosed(String path) {
		StorageStatusListener l = ssl;
		if (l == null)
			return;
		ChangeInfo ci = withPath(path);
		if (ci == null)
			return;
		if (ci.freq > 0 && ci.pid >= 0) {
			l.onSectionsChanged(ci.freq, ci.pid, ci.tableId);
		}
	}

	void onFileCreate(String path) {
		StorageStatusListener l = ssl;
		if (l == null)
			return;
		ChangeInfo ci = withPath(path);
		if (ci == null)
			return;
		l.onTableAdd(ci.freq, ci.pid, ci.tableId);
	}

	void onFileDeleted(String path) {
		StorageStatusListener l = ssl;
		if (l == null)
			return;
		ChangeInfo ci = withPath(path);
		if (ci == null)
			return;
		l.onTableRemoved(ci.freq, ci.pid, ci.tableId);
	}

	void onRootDeleted() {
		StorageStatusListener l = ssl;
		if (l != null) {
			l.onStorageRemoved();
		}
	}

	void checkFreq(long f) {
		if (f == 0)
			throw new IllegalArgumentException("freq is invalid");
	}

	void checkTable(long f, int pid, int tid) {
		if (f == 0 || pid < 0 || pid >= 8192 || tid < 0 || tid > 255)
			throw new IllegalArgumentException("table param is invalid");
	}

	void deleteDiRrecu(File f) {
		if (f.isDirectory()) {
			File[] subs = f.listFiles();
			for (int i = 0; i < subs.length; i++) {
				deleteDiRrecu(subs[i]);
			}
		}
		try {
			f.delete();
		} catch (Exception e) {
			Log.e(TAG, "delete file(" + f.getName() + ") failed:" + e);
		}
	}

	void checkWritable() {
		if (!writable)
			throw new RuntimeException("object is read only!");
	}

	void checkReadable() {
		if (writable)
			throw new RuntimeException("object is read&write !");
	}

	void checkExist(File f, boolean b) {
		if (f.exists() == b)
			throw new RuntimeException("file is " + (b ? "already" : " not") + " exist!");
	}

	/**
	 * 得到存储的根路径
	 * 
	 * @return 路径
	 */
	public String getStorageRoot() {
		return root;
	}

	/**
	 * 得到指定表格数据的文件的路径
	 * 
	 * @param f
	 *            频率
	 * @return 路径
	 */
	public String getTableStoragePath(long f, int pid, int tableId) {
		return root + "/" + f + "-" + pid + "-" + tableId + ".sections";
	}

	/**
	 * 得到网络的UUID
	 * <p>
	 * 如果是私有存储将返回null
	 * 
	 * @return 值
	 */
	public String getNetworkUUID() {
		return uuid;
	}

	/**
	 * 是否为可写的
	 * 
	 * @return 是返回true,否则返回false
	 */
	public boolean isWritable() {
		return writable;
	}

	/**
	 * 提交查询请求
	 * <p>
	 * 对只读类型的对象，数据的查询应采用此函数来完成,否则可能会导致数据不能及时更新或者错误
	 * 
	 * @param r
	 *            执行体对象
	 */
	public void postQuery(Runnable r) {
		synchronized (mutex) {
			if (aviailable) {
				if (writable) {
					r.run();
				} else {
					if (!observing)
						throw new IllegalStateException("must be attached first!");
					handler.obtainMessage(-2, r).sendToTarget();
				}
			}
		}
	}

	/**
	 * 释放资源
	 */
	public void release() {
		synchronized (mutex) {
			detach();
			if (handler != null) {
				handler.sendEmptyMessage(-1);
				try {
					th.join();
				} catch (InterruptedException e) {
					Log.e(TAG, "release failed:" + e);
				} finally {
					handler = null;
					th = null;
					aviailable = false;
				}
			}
		}
	}

	/**
	 * 对于只读类型的对象，应在实际使用前调用此函数，监控数据的变化
	 * 
	 * @return 成功返回true,否则返回false
	 */
	public boolean attach() {
		synchronized (mutex) {
			if (aviailable) {
				if (writable) {
					return true;
				} else {
					if (!observing) {
						fobs.startWatching();
						observing = true;
					}
					return observing;
				}
			}
		}
		throw new IllegalStateException();
	}

	/**
	 * 解除监控
	 */
	public void detach() {
		synchronized (mutex) {
			if (aviailable) {
				if (observing) {
					observing = false;
					fobs.stopWatching();
				}
			}
		}
	}

	/**
	 * 对于非只读类型的对象，此函数无效
	 * <p>
	 * 即非只读没有任何消息会回调给应用程序
	 * 
	 * @param l
	 *            对象
	 */
	public void setStorageStatusListener(StorageStatusListener l) {
		ssl = l;
	}

	/**
	 * 存储状态监听器
	 */
	public static interface StorageStatusListener {
		/**
		 * 当存储被移除时
		 */
		void onStorageRemoved();

		/**
		 * 当增加表存储时
		 * 
		 * @param freq
		 *            频率
		 * @param pid
		 *            表dePID
		 * @param tableId
		 *            表的ID
		 */
		void onTableAdd(long freq, int pid, int tableId);

		/**
		 * 当删除表存储时
		 * 
		 * @param freq
		 *            频率
		 * @param pid
		 *            表dePID
		 * @param tableId
		 *            表的ID
		 */
		void onTableRemoved(long freq, int pid, int tableId);

		/**
		 * 当表存储的段数据发生变化时
		 * 
		 * @param freq
		 *            频率
		 * @param pid
		 *            表dePID
		 * @param tableId
		 *            表的ID
		 */
		void onSectionsChanged(long freq, int pid, int tableId);
	}

	private HashSet<String> listFreqFiles() {
		HashSet<String> ret = new HashSet<String>();
		File f = new File(root);
		String[] list = f.list();
		for (int i = 0; i < list.length; i++) {
			ret.add(list[i].substring(0, list[i].indexOf('-')));
		}
		return ret;
	}

	/**
	 * 列举已存储的频率
	 * 
	 * @return 列表
	 */
	public List<Long> getFrequencies() {
		HashSet<String> list = listFreqFiles();
		List<Long> ret = new ArrayList<Long>(list.size());
		for (String f : list) {
			ret.add(Long.parseLong(f));
		}
		return ret;
	}

	/**
	 * 检查是否存在指定频率的存储
	 * 
	 * @param freq
	 *            频率
	 * @return 是返回true,否则返回false
	 */
	public boolean containsFrequency(long freq) {
		checkFreq(freq);
		HashSet<String> list = listFreqFiles();
		return list.contains(freq + "");
	}

	/**
	 * 检查是否存在指定表的存储
	 * 
	 * @param freq
	 *            频率
	 * @return 是返回true,否则返回false
	 */
	public boolean containsTable(long freq, int pid, int tableId) {
		checkTable(freq, pid, tableId);
		try {
			return new File(getTableStoragePath(freq, pid, tableId)).exists();
		} catch (Exception e) {
			Log.e(TAG, "containsTable error:" + e);
		}
		return false;
	}

	/**
	 * 删除频率存储
	 * <p>
	 * 要求可写类型
	 * 
	 * @param freq
	 *            频率
	 */
	public void removeFrequency(long freq) {
		synchronized (mutex) {
			checkFreq(freq);
			checkWritable();
			File f = new File(root);
			String[] list = f.list();
			for (int i = 0; i < list.length; i++) {
				if (list[i].startsWith(freq + "-")) {
					new File(f, list[i]).delete();
				}
			}
		}
	}

	/**
	 * 添加表存储
	 * 
	 * @param freq
	 *            频率
	 * @param pid
	 *            PID
	 * @param tableId
	 *            表ID
	 * @return 成功翻译true，否则返回false
	 */
	public boolean addTable(long freq, int pid, int tableId) {
		synchronized (mutex) {
			checkTable(freq, pid, tableId);
			checkWritable();
			File f = new File(getTableStoragePath(freq, pid, tableId));
			checkExist(f, true);
			try {
				return f.createNewFile();
			} catch (IOException e) {
				Log.e(TAG, "create table storage file failed:" + e);
				return false;
			}
		}
	}

	/**
	 * 删除表存储
	 * 
	 * @param freq
	 *            频率
	 * @param pid
	 *            PID
	 * @param tableId
	 *            表ID
	 */
	public void removeTable(long freq, int pid, int tableId) {
		synchronized (mutex) {
			checkTable(freq, pid, tableId);
			checkWritable();
			File f = new File(getTableStoragePath(freq, pid, tableId));
			if (f.exists())
				if (!f.delete())
					f.deleteOnExit();
		}
	}

	/**
	 * 写入段数据
	 * <p>
	 * 段数据应一次性写入,多次写入将覆盖之前的数据
	 * 
	 * @param freq
	 *            频率
	 * @param pid
	 *            PID
	 * @param tableId
	 *            表ID
	 * @param b
	 *            缓冲区
	 * @param off
	 *            偏移量
	 * @param len
	 *            长度
	 * @throws IOException
	 *             如果发生IO错误
	 */
	public void writeSections(long freq, int pid, int tableId, byte[] b, int off, int len)
			throws IOException {
		synchronized (mutex) {
			checkTable(freq, pid, tableId);
			checkWritable();
			File f = new File(getTableStoragePath(freq, pid, tableId));
			checkExist(f, false);
			RandomAccessFile raf = null;
			try {
				raf = new RandomAccessFile(f, "rw");
				raf.write(b, off, len);
			} catch (Exception e) {
				Log.e(TAG, "writeSections e:" + e);
			} finally {
				if (raf != null) {
					try {
						raf.close();
					} catch (Exception e) {
					}
				}
			}
		}
	}

	/**
	 * 读取段数据
	 * <p>
	 * 段数据应一次性读出,多次读取将总是从文件的起始位置读取
	 * 
	 * @param freq
	 *            频率
	 * @param pid
	 *            PID
	 * @param tableId
	 *            表ID
	 * @param b
	 *            缓冲区
	 * @param off
	 *            偏移量
	 * @param len
	 *            长度
	 * @throws IOException
	 *             如果发生IO错误
	 */
	public int readSections(long freq, int pid, int tableId, byte[] b, int off, int len)
			throws IOException {
		checkTable(freq, pid, tableId);
		checkReadable();
		File f = new File(getTableStoragePath(freq, pid, tableId));
		checkExist(f, false);
		RandomAccessFile raf = null;
		try {
			raf = new RandomAccessFile(f, "r");
			return raf.read(b, off, len);
		} catch (Exception e) {
			Log.e(TAG, "readSections e:" + e);
		} finally {
			if (raf != null) {
				try {
					raf.close();
				} catch (Exception e) {
				}
			}
		}
		return 0;
	}

	/**
	 * 读取端数据
	 * <p>
	 * 将根据过滤条件排除部分或全部端数据
	 * 
	 * @param freq
	 *            频率
	 * @param pid
	 *            PID 流PID
	 * @param coef
	 *            过滤参数COEF
	 * @param mask
	 *            过滤参数MASK
	 * @param excl
	 *            过滤参数EXCL
	 * @param depth
	 *            过滤深度(字节)
	 * @param b
	 *            缓冲区
	 * @param off
	 *            偏移量
	 * @param len
	 *            长度
	 * @return 读取到的数据长度
	 * @throws IOException
	 */
	public int readSections(long freq, int pid, byte[] coef, byte[] mask, byte[] excl, int depth,
			byte[] b, int off, int len) throws IOException {
		if (depth > 16)
			return 0;
		if (depth <= 0)
			return readSections(freq, pid, coef[0], b, off, len);
		checkTable(freq, pid, coef[0]);
		checkReadable();
		File f = new File(getTableStoragePath(freq, pid, coef[0]));
		checkExist(f, false);
		RandomAccessFile raf = null;
		try {
			raf = new RandomAccessFile(f, "r");
			return readSectionFiltered(raf, coef, mask, excl, depth, b, off, len);
		} catch (Exception e) {
			Log.e(TAG, "readSections e:" + e);
		} finally {
			if (raf != null) {
				try {
					raf.close();
				} catch (Exception e) {
				}
			}
		}
		return 0;
	}

	int readSectionFiltered(RandomAccessFile f, byte[] coef, byte[] mask, byte[] excl, int depth,
			byte[] b, int off, int len) throws IOException {
		int seek = 0, rlen = 0, slen;
		byte[] head = new byte[16];
		while (true) {
			if (f.read(head) < 16)
				return rlen;
			if ((slen = (((head[1] & 0x0f) << 8) | (head[2] & 0xff)) + 3) <= 3)
				return rlen;
			if (softFilter(head, coef, mask, excl, depth)) {
				if (rlen + slen > len)
					return rlen;
				f.seek(seek);
				f.read(b, off + rlen, slen);
				if ((rlen += slen) == len)
					return rlen;
			}
			seek += slen;
		}
	}

	boolean softFilter(byte[] s, byte[] c, byte[] m, byte[] e, int d) {
		for (int i = 0; i < d; ++i) {
			if (e[i] == 0) {
				if ((c[i] & m[i]) != (s[i] & m[i]))
					return false;
			} else {
				if ((c[i] & (m[i] & ~e[i])) != (s[i] & (m[i] & ~e[i])))
					return false;
			}
		}
		return true;
	}

	/**
	 * 读取端数据
	 * <p>
	 * 将根据过滤条件排除部分或全部端数据
	 * 
	 * @param freq
	 *            频率
	 * @param pid
	 *            PID 流PID
	 * @param coef
	 *            过滤参数COEF
	 * @param mask
	 *            过滤参数MASK
	 * @param excl
	 *            过滤参数EXCL
	 * @param depth
	 *            过滤深度(字节)
	 * @param b
	 *            缓冲区
	 * @return 读取到的数据长度
	 * @throws IOException
	 */
	public int readSections(long freq, int pid, byte[] coef, byte[] mask, byte[] excl, int depth,
			byte[] b) throws IOException {
		return readSections(freq, pid, coef, mask, excl, depth, b, 0, b.length);
	}

	/**
	 * 读取段数据
	 * <p>
	 * 段数据应一次性读出,多次读取将总是从文件的起始位置读取
	 * 
	 * @param freq
	 *            频率
	 * @param pid
	 *            PID
	 * @param tableId
	 *            表ID
	 * @param b
	 *            缓冲区
	 * @param len
	 *            长度
	 * @throws IOException
	 *             如果发生IO错误
	 */
	public int readSections(long freq, int pid, int tableId, byte[] b, int len) throws IOException {
		return readSections(freq, pid, tableId, b, 0, len);
	}

	/**
	 * 读取段数据
	 * <p>
	 * 段数据应一次性读出,多次读取将总是从文件的起始位置读取
	 * 
	 * @param freq
	 *            频率
	 * @param pid
	 *            PID
	 * @param tableId
	 *            表ID
	 * @param b
	 *            缓冲区
	 * @throws IOException
	 *             如果发生IO错误
	 */
	public int readSections(long freq, int pid, int tableId, byte[] b) throws IOException {
		return readSections(freq, pid, tableId, b, 0, b.length);
	}

	/**
	 * 写入段数据
	 * <p>
	 * 段数据应一次性写入,多次写入将覆盖之前的数据
	 * 
	 * @param freq
	 *            频率
	 * @param pid
	 *            PID
	 * @param tableId
	 *            表ID
	 * @param b
	 *            缓冲区
	 * @param len
	 *            长度
	 * @throws IOException
	 *             如果发生IO错误
	 */
	public void writeSections(long freq, int pid, int tableId, byte[] b, int len)
			throws IOException {
		writeSections(freq, pid, tableId, b, 0, len);
	}

	/**
	 * 写入段数据
	 * <p>
	 * 段数据应一次性写入,多次写入将覆盖之前的数据
	 * 
	 * @param freq
	 *            频率
	 * @param pid
	 *            PID
	 * @param tableId
	 *            表ID
	 * @param b
	 *            缓冲区
	 * @throws IOException
	 *             如果发生IO错误
	 */
	public void writeSections(long freq, int pid, int tableId, byte[] b) throws IOException {
		writeSections(freq, pid, tableId, b, 0, b.length);
	}
}
