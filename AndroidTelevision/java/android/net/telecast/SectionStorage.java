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
 * �����ݴ洢
 * <p>
 * �����Ϊ<b>ֻ��</b>��<b>��д</b>��������<br>
 * <li>ֻ������,������ȡ����Ӧ���ṩ�Ķ�����.ͨ����Ӽ�����,����ʵ�ֶ��ļ���Ŀ¼�仯�ļ��
 * <li>��д����,���Խ���д��ɾ���Ȳ���,�����Ͷ����ṩ�ļ��仯�ĵļ�����Ϣ
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
	 * ������д�Ķ����ݴ洢
	 * <p>
	 * �����ǿ�д����
	 * 
	 * @param root
	 *            ��Ŀ¼·��
	 * @return ����
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
	 * �����ɶ��Ķ����ݴ洢
	 * <p>
	 * �����ǿɶ�����
	 * 
	 * @param root
	 *            ��Ŀ¼·��
	 * @return ����
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
	 * �õ��洢�ĸ�·��
	 * 
	 * @return ·��
	 */
	public String getStorageRoot() {
		return root;
	}

	/**
	 * �õ�ָ��������ݵ��ļ���·��
	 * 
	 * @param f
	 *            Ƶ��
	 * @return ·��
	 */
	public String getTableStoragePath(long f, int pid, int tableId) {
		return root + "/" + f + "-" + pid + "-" + tableId + ".sections";
	}

	/**
	 * �õ������UUID
	 * <p>
	 * �����˽�д洢������null
	 * 
	 * @return ֵ
	 */
	public String getNetworkUUID() {
		return uuid;
	}

	/**
	 * �Ƿ�Ϊ��д��
	 * 
	 * @return �Ƿ���true,���򷵻�false
	 */
	public boolean isWritable() {
		return writable;
	}

	/**
	 * �ύ��ѯ����
	 * <p>
	 * ��ֻ�����͵Ķ������ݵĲ�ѯӦ���ô˺��������,������ܻᵼ�����ݲ��ܼ�ʱ���»��ߴ���
	 * 
	 * @param r
	 *            ִ�������
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
	 * �ͷ���Դ
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
	 * ����ֻ�����͵Ķ���Ӧ��ʵ��ʹ��ǰ���ô˺�����������ݵı仯
	 * 
	 * @return �ɹ�����true,���򷵻�false
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
	 * ������
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
	 * ���ڷ�ֻ�����͵Ķ��󣬴˺�����Ч
	 * <p>
	 * ����ֻ��û���κ���Ϣ��ص���Ӧ�ó���
	 * 
	 * @param l
	 *            ����
	 */
	public void setStorageStatusListener(StorageStatusListener l) {
		ssl = l;
	}

	/**
	 * �洢״̬������
	 */
	public static interface StorageStatusListener {
		/**
		 * ���洢���Ƴ�ʱ
		 */
		void onStorageRemoved();

		/**
		 * �����ӱ�洢ʱ
		 * 
		 * @param freq
		 *            Ƶ��
		 * @param pid
		 *            ��dePID
		 * @param tableId
		 *            ���ID
		 */
		void onTableAdd(long freq, int pid, int tableId);

		/**
		 * ��ɾ����洢ʱ
		 * 
		 * @param freq
		 *            Ƶ��
		 * @param pid
		 *            ��dePID
		 * @param tableId
		 *            ���ID
		 */
		void onTableRemoved(long freq, int pid, int tableId);

		/**
		 * ����洢�Ķ����ݷ����仯ʱ
		 * 
		 * @param freq
		 *            Ƶ��
		 * @param pid
		 *            ��dePID
		 * @param tableId
		 *            ���ID
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
	 * �о��Ѵ洢��Ƶ��
	 * 
	 * @return �б�
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
	 * ����Ƿ����ָ��Ƶ�ʵĴ洢
	 * 
	 * @param freq
	 *            Ƶ��
	 * @return �Ƿ���true,���򷵻�false
	 */
	public boolean containsFrequency(long freq) {
		checkFreq(freq);
		HashSet<String> list = listFreqFiles();
		return list.contains(freq + "");
	}

	/**
	 * ����Ƿ����ָ����Ĵ洢
	 * 
	 * @param freq
	 *            Ƶ��
	 * @return �Ƿ���true,���򷵻�false
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
	 * ɾ��Ƶ�ʴ洢
	 * <p>
	 * Ҫ���д����
	 * 
	 * @param freq
	 *            Ƶ��
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
	 * ��ӱ�洢
	 * 
	 * @param freq
	 *            Ƶ��
	 * @param pid
	 *            PID
	 * @param tableId
	 *            ��ID
	 * @return �ɹ�����true�����򷵻�false
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
	 * ɾ����洢
	 * 
	 * @param freq
	 *            Ƶ��
	 * @param pid
	 *            PID
	 * @param tableId
	 *            ��ID
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
	 * д�������
	 * <p>
	 * ������Ӧһ����д��,���д�뽫����֮ǰ������
	 * 
	 * @param freq
	 *            Ƶ��
	 * @param pid
	 *            PID
	 * @param tableId
	 *            ��ID
	 * @param b
	 *            ������
	 * @param off
	 *            ƫ����
	 * @param len
	 *            ����
	 * @throws IOException
	 *             �������IO����
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
	 * ��ȡ������
	 * <p>
	 * ������Ӧһ���Զ���,��ζ�ȡ�����Ǵ��ļ�����ʼλ�ö�ȡ
	 * 
	 * @param freq
	 *            Ƶ��
	 * @param pid
	 *            PID
	 * @param tableId
	 *            ��ID
	 * @param b
	 *            ������
	 * @param off
	 *            ƫ����
	 * @param len
	 *            ����
	 * @throws IOException
	 *             �������IO����
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
	 * ��ȡ������
	 * <p>
	 * �����ݹ��������ų����ֻ�ȫ��������
	 * 
	 * @param freq
	 *            Ƶ��
	 * @param pid
	 *            PID ��PID
	 * @param coef
	 *            ���˲���COEF
	 * @param mask
	 *            ���˲���MASK
	 * @param excl
	 *            ���˲���EXCL
	 * @param depth
	 *            �������(�ֽ�)
	 * @param b
	 *            ������
	 * @param off
	 *            ƫ����
	 * @param len
	 *            ����
	 * @return ��ȡ�������ݳ���
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
	 * ��ȡ������
	 * <p>
	 * �����ݹ��������ų����ֻ�ȫ��������
	 * 
	 * @param freq
	 *            Ƶ��
	 * @param pid
	 *            PID ��PID
	 * @param coef
	 *            ���˲���COEF
	 * @param mask
	 *            ���˲���MASK
	 * @param excl
	 *            ���˲���EXCL
	 * @param depth
	 *            �������(�ֽ�)
	 * @param b
	 *            ������
	 * @return ��ȡ�������ݳ���
	 * @throws IOException
	 */
	public int readSections(long freq, int pid, byte[] coef, byte[] mask, byte[] excl, int depth,
			byte[] b) throws IOException {
		return readSections(freq, pid, coef, mask, excl, depth, b, 0, b.length);
	}

	/**
	 * ��ȡ������
	 * <p>
	 * ������Ӧһ���Զ���,��ζ�ȡ�����Ǵ��ļ�����ʼλ�ö�ȡ
	 * 
	 * @param freq
	 *            Ƶ��
	 * @param pid
	 *            PID
	 * @param tableId
	 *            ��ID
	 * @param b
	 *            ������
	 * @param len
	 *            ����
	 * @throws IOException
	 *             �������IO����
	 */
	public int readSections(long freq, int pid, int tableId, byte[] b, int len) throws IOException {
		return readSections(freq, pid, tableId, b, 0, len);
	}

	/**
	 * ��ȡ������
	 * <p>
	 * ������Ӧһ���Զ���,��ζ�ȡ�����Ǵ��ļ�����ʼλ�ö�ȡ
	 * 
	 * @param freq
	 *            Ƶ��
	 * @param pid
	 *            PID
	 * @param tableId
	 *            ��ID
	 * @param b
	 *            ������
	 * @throws IOException
	 *             �������IO����
	 */
	public int readSections(long freq, int pid, int tableId, byte[] b) throws IOException {
		return readSections(freq, pid, tableId, b, 0, b.length);
	}

	/**
	 * д�������
	 * <p>
	 * ������Ӧһ����д��,���д�뽫����֮ǰ������
	 * 
	 * @param freq
	 *            Ƶ��
	 * @param pid
	 *            PID
	 * @param tableId
	 *            ��ID
	 * @param b
	 *            ������
	 * @param len
	 *            ����
	 * @throws IOException
	 *             �������IO����
	 */
	public void writeSections(long freq, int pid, int tableId, byte[] b, int len)
			throws IOException {
		writeSections(freq, pid, tableId, b, 0, len);
	}

	/**
	 * д�������
	 * <p>
	 * ������Ӧһ����д��,���д�뽫����֮ǰ������
	 * 
	 * @param freq
	 *            Ƶ��
	 * @param pid
	 *            PID
	 * @param tableId
	 *            ��ID
	 * @param b
	 *            ������
	 * @throws IOException
	 *             �������IO����
	 */
	public void writeSections(long freq, int pid, int tableId, byte[] b) throws IOException {
		writeSections(freq, pid, tableId, b, 0, b.length);
	}
}
