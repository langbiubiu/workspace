package ipaneltv.toolkit.dsmcc;

import ipaneltv.toolkit.IPanelLog;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.LinkedList;

import org.xml.sax.InputSource;

import android.content.Context;
import android.os.Environment;
import android.os.MemoryFile;
import android.util.SparseArray;

public class BigFileDownloader {
	static final String TAG = "[cb]BigFileDownloader";
	DownloadListener dl;
	Context ctx;
	String netUUID;
	long freq;
	int pmtpid;
	String targetPath, targetName;
	DsmccDownloader got;
	MetaBuilder metaBuilder;
	private Object mutex = new Object();
	private boolean ready = false, pathok = false, fileok = false;
	private DsmccDownloader.Task task;
	private FilePathTask pathTask = new FilePathTask();
	private BigFileTask fileTask = new BigFileTask();

	public BigFileDownloader(Context ctx) {
		got = DsmccDownloader.createInstance(ctx);
		got.setServiceStateListener(new DsmccDownloader.ServiceStateListener() {
			@Override
			public void onServiceAvailable(boolean b) {
				synchronized (mutex) {
					if (b) {
						IPanelLog.i(TAG, "onServiceAvailable:" + b);
						resumed();
					} else {
						IPanelLog.e(TAG, "dscmm downloader service lost!");
					}
				}
			}
		});
	}

	public void setSource(String netUUID, long freq, int pmtpid, String url) throws IOException {
		this.netUUID = netUUID;
		this.freq = freq;
		this.pmtpid = pmtpid;
		File f = new File(url);

		targetPath = url.substring(0, url.lastIndexOf('/') + 1);
		targetName = f.getName();
		IPanelLog.i(TAG, "setSource freq=" + freq + ",pmtpid=" + pmtpid + ",url=" + url + "targetPath="
				+ targetPath + ",targetName=" + targetName);
		metaBuilder = new MetaBuilder(netUUID, freq);

		if (metaBuilder.getXmlReader() == null)
			throw new IOException();
	}

	public void start() {
		synchronized (mutex) {
			if (metaBuilder == null)
				throw new IllegalStateException("to setupSource first!");
			got.ensure();
			ready = true;
			resumed();
		}
	}

	public void stop() {
		// TODO
	}

	public void next() {
		if (!pathok) {
			pathTask.start();
		} else if (!fileok) {
			fileTask.start();
		}
	}

	void resumed() {
		try {

			if (!ready)
				return;
			if (task == null) {
				task = got.createTask(netUUID, freq);
				if (task == null)
					return;
			}
			task.reserve();

			if (!task.hasBuffers()) {
				if (!task.setupBuffers())
					return;
			}
			if (task != null) {
				if (!pathok) {
					pathTask.start();
				} else if (!fileok) {
					fileTask.start();
				}
			}
		} catch (Exception e) {
			IPanelLog.e(TAG, "resumed error:" + e);
		}
	}

	void paused() {

	}

	public void setDownloadListener(DownloadListener l) {
		dl = l;
	}

	public static interface DownloadListener {
		/**
		 * 需要删除之前的数据，下载器会重新下载
		 */
		void onDownloadRestart();

		/**
		 * 下载到了一块数据
		 * 
		 * @param fm
		 *            文件
		 * @param off
		 *            偏移量
		 * @param len
		 *            长度
		 */
		void onDownloadBlock(MemoryFile fm, int off, int len);

		/**
		 * 下载已完成
		 */
		void onDownloadFinished();

		/**
		 * 下载致命错误
		 */
		void onDownloadFatalError();
	}

	void onFileFound(int blockSize) {
		IPanelLog.d(TAG, "onFileFound(" + blockSize + ")");
		// TODO
	}

	void onFileBlockFound(int index, MemoryFile mf, int off, int len) {
		IPanelLog.d(TAG, "onFileBlockFound(" + index + "," + off + "," + len + ")");
		if (mf != null) {
			File extDir = Environment.getExternalStorageDirectory();
			String filename = targetName;
			byte[] b = null;
			int ret = -1;
			IPanelLog.d(TAG, "onFileBlockFound extDir=" + extDir + ",filename=" + filename);
			File f = new File(extDir, filename);

			try {
				if (!f.exists())
					f.createNewFile();
				f.setWritable(true);
				FileOutputStream is = new FileOutputStream(f, true);
				int offset = 0;
				int last = len % 10;
				for (int i = 0; i < 10 && offset < (len - last); i++) {
					b = new byte[len / 10];
					ret = mf.readBytes(b, off + offset, 0, len / 10);
					is.write(b);
					offset += ret;
				}

				b = new byte[last];
				ret = mf.readBytes(b, off + offset, 0, last);
				is.write(b);
				offset += ret;
				IPanelLog.d(TAG, "onFileBlockFound(" + offset + ")");
				is.close();

			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		} else {
			IPanelLog.d(TAG, "onFileBlockFound bigfile download Success.len " + len);
		}
		IPanelLog.d(TAG, "onFileBlockFound write finish.");

	}

	void onPathTaskFinished() {

		IPanelLog.i(TAG, "onPathTaskFinished in.");

		MetaInfo.Module.Dir dir = pathTask.getEndDir();
		if (dir == null) {
			onTaskFailed(-2001, "path task is not finished ok!");
			return;
		}
		MetaInfo.Module.Dir.Node n = dir.findNodeByName(targetName);
		if (n == null) {
			onTaskFailed(-2002, "last file name was not found!");
			return;
		}
		IPanelLog.i(TAG, "onPathTaskFinished in targetName=" + targetName + ",key=" + n.key
				+ ",moduleId=" + n.moduleId + ",n.type=" + n.type + ",n.transactionId="
				+ n.transactionId);

		if (!n.isFile()) {
			onTaskFailed(-2003, "taget node type is not file!");
			return;
		}

		MetaInfo.Dii dii = pathTask.getEndDii(n.transactionId);
		if (dii == null) {
			onTaskFailed(-2004, "last file dii missing!");
			return;
		}
		IPanelLog.i(TAG, "onPathTaskFinished in targetName=" + targetName + ",diiPid=" + dii.diiPid
				+ ",transactionTag=" + dii.transactionTag);

		int nextModId = n.moduleId;
		IPanelLog.i(TAG, "nextModId=" + n.moduleId);
		MetaInfo.Pmt pmt = pathTask.getPmt();
		if (pmt == null) {
			onTaskFailed(-2005, "missing pmt of path task!");
			return;
		}
		IPanelLog.i(TAG, "pmt.dsipid=" + pmt.dsipid);
		fileTask.clear();
		IPanelLog.i(TAG, "fileTask.clear();");

		while (nextModId > 0 && nextModId < 65535) {
			MetaInfo.Dii.Mod m = dii.getModById(nextModId);
			if (m == null) {
				onTaskFailed(-2006, "some file module link list missing!");
				return;
			}
			int pid = pmt.getTagMappedPID(m.tag);
			m.setPID(pid);
			m.setAttach(null);
			fileTask.setObjectKey(n.key);
			fileTask.addModule(m);
			nextModId = m.link;
			IPanelLog.i(TAG, "m.id=" + m.id + ", m.link=" + m.link + ",n.key=" + n.key);
		}
		pathTask.clear();
		pathok = true;
		IPanelLog.i(TAG, "ffileTask.moduleSize())=" + fileTask.moduleSize());
		onFileFound(fileTask.moduleSize());
		fileTask.start();
	}

	void onFileTaskFinished() {
		// TODO
		// clear modules...--liusl
		IPanelLog.i(TAG, "onFileTaskFinished");
	}

	void onTaskFailed(int code, String estr) {
		// TODO
		IPanelLog.i(TAG, "code=" + code + ",onTaskFailed estr=" + estr);
	}

	class FilePathTask implements DsmccDownloader.TaskLoadingHandler {

		private boolean pmtld = false, dsild = false, diild = false, parsepmt = false,
				parsedsi = false, parsedii = false, endld = false;
		private Node headNode = null, tailNode = null;
		private String path;
		private MetaInfo.Pmt pmt;
		private SparseArray<MetaInfo.Dii> diis = new SparseArray<MetaInfo.Dii>();
		private Object curTodo = null;
		private boolean started = false;
		private Object mutex = new Object();

		public void start() {
			synchronized (mutex) {
				if (started)
					throw new IllegalStateException("has been started!");
				task.setLoadingHandler(this);

				IPanelLog.i(TAG, "FilePathTask start in.");

				if (!pmtld) {
					curTodo = MetaInfo.Pmt.class;
					if (!task.loadPmt(pmtpid)) {
						onTaskFailed(-1001, "start load pmt failed");
					} else {
						pmtld = true;
						IPanelLog.i(TAG, "FilePathTask start loadPmt end.");
					}
				}
				if (!dsild && parsepmt) {
					curTodo = MetaInfo.Dsi.class;
					if (!task.loadDsi(pmt.dsipid)) {
						onTaskFailed(-1001, "start load dsi failed");
					} else {
						dsild = true;
						IPanelLog.i(TAG, "FilePathTask start loadDsi end..");
					}

				}
				if (!endld && parsepmt && parsedsi) {
					doNextNode();
				}
			}
		}

		MetaInfo.Dii getEndDii(int transId) {
			return diis.get(tailNode.transid);
		}

		MetaInfo.Pmt getPmt() {
			return pmt;
		}

		void doNextNode() {
			synchronized (mutex) {
				try {
					String name = null;
					if (path == null) {
						IPanelLog.i(TAG, "path=null");
						return;
					}
					IPanelLog.i(TAG, "curTodo=" + curTodo);
					if (targetPath.equals(path) && parsedii == true
							&& curTodo == MetaInfo.Module.class) {
						if (endld == false) {
							endld = true;
							IPanelLog.i(TAG, "targetPath=" + targetPath + ",path=" + path);
							onPathTaskFinished();
						}
						return;
					} else if (parsedii == true && diild == true
							&& curTodo == MetaInfo.Module.class) {
						int plen = path.length();
						name = targetPath.substring(plen, targetPath.indexOf('/', plen));
						IPanelLog.i(TAG, "targetPath=" + targetPath + ",path=" + path + "name=" + name);
					}

					MetaInfo.Dii dii = diis.get(tailNode.transid);
					if (dii == null && dsild && parsedsi && !diild) {
						int pid = pmt.getDsipid();
						curTodo = MetaInfo.Dii.class;
						if (!task.loadDii(pid, tailNode.transid)) {
							onTaskFailed(-1001, "start load dii failed");
							return;
						} else {
							diild = true;
							IPanelLog.i(TAG, "doNextNode loadDii sucessd.");
						}
					} else {
						IPanelLog.i(TAG, "doNextNode loadModule in.modid=" + tailNode.modid);
						MetaInfo.Dii.Mod m = dii.getModById(tailNode.modid);
						IPanelLog.i(TAG, "doNextNode in.dii.getModById=" + dii.getModById(tailNode.modid));
						MetaInfo.Module mod = m.getAttach();

						if (mod == null) {
							int pid = pmt.getDsipid();
							curTodo = MetaInfo.Module.class;
							if (!task.loadModule(pid, tailNode.modid)) {
								onTaskFailed(-1001, "start load module(" + name + ") failed");
								return;
							} else {
								IPanelLog.i(TAG, "doNextNode loadModule sucessd.");
							}
						} else {
							MetaInfo.Module.Dir dir = mod.getDir(tailNode.objkey);
							if (dir == null) {
								onTaskFailed(-1001, "path middle node(" + name + ") is not a dir");
								return;
							}
							IPanelLog.i(TAG, "doNextNode name=" + name + "objkey=" + tailNode.objkey);
							MetaInfo.Module.Dir.Node n = dir.findNodeByName(name);
							if (n == null) {
								onTaskFailed(-1012, "path node(" + name + ") not founed");
								return;
							}
							IPanelLog.i(TAG, "doNextNode findNode=" + n.moduleId + "," + n.name + ","
									+ n.key);

							// liusl --add
							Node node = new Node(name);
							node.modid = n.moduleId;
							node.name = n.name;
							node.objkey = n.key;
							node.transid = n.transactionId;
							node.transtag = n.transactionTag;
							path += name + "/";
							tailNode.next = node;
							tailNode = node;
							doNextNode();
						}
					}
				} catch (Exception e) {
					onTaskFailed(-1001, "doNextNode error:" + e);
					return;
				}
			}
		}

		public void stop() {
			synchronized (mutex) {
				if (started) {
					task.setLoadingHandler(null);
					task = null;
				}
			}
		}

		public void clear() {
			synchronized (mutex) {
				if (!started) {
					pmtld = dsild = endld = false;
					path = null;
					pmt = null;
					headNode = tailNode = null;
					diis.clear();
				}
			}
		}

		public boolean isReachEnd() {
			return endld;
		}

		@Override
		public void onTaskFailed(int code, String estr) {
			curTodo = null;
			BigFileDownloader.this.onTaskFailed(code, estr);
		}

		Node getHeadDir() {
			return headNode;
		}

		public MetaInfo.Module.Dir getEndDir() {
			IPanelLog.i(TAG, "getEndDir in .transid=" + tailNode.transid + ",modid=" + tailNode.modid
					+ ",objkey=" + tailNode.objkey);

			MetaInfo.Dii dii = diis.get(tailNode.transid);
			if (dii != null) {
				IPanelLog.i(TAG, "getEndDir in .diiPid=" + dii.diiPid + ",.mid=" + dii.transactionTag);
				MetaInfo.Dii.Mod m = dii.getModById(tailNode.modid);
				if (m != null) {
					MetaInfo.Module mod = m.getAttach();
					if (mod != null)
						return mod.getDir(tailNode.objkey);
				}
			}
			return null;
		}

		@Override
		public void onTaskFinish(boolean update, int len) {
			if (!update) {
				IPanelLog.w(TAG, "path onTaskFinish but no update!!! ignore it!");
				return;
			}
			if (curTodo != null) {
				try {
					MemoryFile mf = task.getMetaFile();
					MemeoryFileInputStream is = new MemeoryFileInputStream();
					is.setAttr(mf, 0, len);
					InputSource src = new InputSource(is);
					if (curTodo == MetaInfo.Pmt.class) {
						if ((pmt = metaBuilder.parsePmt(pmtpid, src, len)) == null) {
							onTaskFailed(-1007, "parsePmt error");
							return;
						} else {
							IPanelLog.i(TAG, "MetaBuilder.parsePmt success.");
							parsepmt = true;
							if (!dsild && parsepmt) {
								curTodo = MetaInfo.Dsi.class;
								if (!task.loadDsi(pmt.getDsipid())) {
									onTaskFailed(-1001, "start load dsi failed");
								}
								dsild = true;
								IPanelLog.i(TAG, "FilePathTask start loadDsi end..");
							}
							return;
						}

					} else if (curTodo == MetaInfo.Dsi.class) {
						MetaInfo.Dsi dsi = metaBuilder.parseDsi(pmt.dsipid, src);
						if (dsi == null) {
							onTaskFailed(-1008, "parseDsi error");
							return;
						} else {
							IPanelLog.i(TAG, "MetaBuilder.parseDsi success.in");
							dsild = true;
							parsedsi = true;
							Node n = new Node("/");
							n.transid = dsi.transactionId;
							n.modid = dsi.moduleId;
							n.objkey = dsi.objectKey;
							n.transtag = dsi.transactionTag;
							IPanelLog.i(TAG, "objkey:" + n.objkey + ",transtag:" + n.transtag);
							IPanelLog.i(TAG, "transid:" + n.transid + ",modid:" + n.modid);

							path = "/";
							headNode = tailNode = n;
							IPanelLog.i(TAG, "MetaBuilder.parseDsi success.out");
							// return;//=--liusl
						}
					} else if (curTodo == MetaInfo.Dii.class) {
						int pid = pmt.dsipid;
						MetaInfo.Dii dii = metaBuilder.parseDii(pid, src);
						if (dii == null) {
							onTaskFailed(-1009, "parseDii error");
							return;
						} else {
							parsedii = true;
							IPanelLog.i(TAG, "diis.put:in." + tailNode.transid);
							diis.put(tailNode.transid, dii);
							IPanelLog.i(TAG, "diis.put: out." + tailNode.transid);

						}
					} else if (curTodo == MetaInfo.Module.class) {
						MetaInfo.Dii dii = diis.get(tailNode.transid);
						MetaInfo.Dii.Mod m = dii.getModById(tailNode.modid);
						int pid = pmt.getTagMappedPID(m.tag);
						MetaInfo.Module mod = metaBuilder.parseModule(pid, src);
						if (mod == null) {
							onTaskFailed(-1010, "parseModule error");
							return;
						}
						m.setAttach(mod);
						IPanelLog.i(TAG, "onTaskFinish parseModule success.");
					}
					IPanelLog.i(TAG, "onTaskFinish call doNextNode:");
					doNextNode();
					return;
				} catch (Exception e) {
					onTaskFailed(-1006, "onTaskFinish error:" + e.getMessage());
					return;
				}
			}
			onTaskFailed(-1005, "impl error for:" + curTodo);
			return;
		}

		class Node {// is dir
			Node next;
			String name;
			int ocid, transid, transtag, modid, objkey;

			Node(String name) {
				this.name = name;
			}
		}
	}

	class BigFileTask implements DsmccDownloader.TaskLoadingHandler {

		LinkedList<MetaInfo.Dii.Mod> modules = new LinkedList<MetaInfo.Dii.Mod>();
		private boolean started = false;
		private int key = -1, index = -1;

		public BigFileTask() {
		}

		public void setObjectKey(int key) {
			this.key = key;
		}

		public void addModule(MetaInfo.Dii.Mod m) {
			if (m.getPID() < 0)
				throw new IllegalArgumentException("set pid first for Mod");
			modules.addLast(m);
		}

		public int moduleSize() {
			return modules.size();
		}

		public void start() {
			synchronized (modules) {
				if (started)
					throw new IllegalStateException("has been started already");
				if (modules.size() == 1) {
					index++;
					MetaInfo.Dii.Mod m = modules.peek();
					task.setLoadingHandler(this);
					IPanelLog.i(TAG, "file:START:m.id=" + m.id);
					task.setIsBigFile(false);
					if (!task.loadModule(m.getPID(), m.id)) {
						onTaskFailed(-3001, "start loadModule for file failed");
						return;
					}
					IPanelLog.i(TAG, "file:END:m.id=" + m.id);
				} else if (modules.size() > 1) {
					int mids[] = new int[modules.size()];
					int mpid = 0;

					File extDir = Environment.getExternalStorageDirectory();
					File bigfile = new File(extDir, targetName);

					IPanelLog.i(TAG, "bigfile.getAbsolutePath()=" + bigfile.getAbsolutePath());
					IPanelLog.i(TAG, "modules.size()=" + modules.size());
					for (index = 0; index < modules.size(); index++) {
						mids[index] = modules.get(index).id;
						mpid = modules.get(index).pid;
					}
					IPanelLog.i(TAG, "mpid=" + mpid + "," + mids[0] + "," + mids[1] + "," + mids[2] + ","
							+ mids[3] + "," + mids[4] + "," + mids[5] + "," + mids[6]);
					task.setLoadingHandler(this);
					task.setIsBigFile(true);
					if (!task.loadBigFile(mpid, mids, bigfile.getAbsolutePath())) {
						onTaskFailed(-3011, "start loadModule for file failed");
						return;
					}
				}
			}
		}

		public void stop() {
			synchronized (modules) {
				if (started) {
					// TODO
				}
			}
		}

		public void clear() {
			synchronized (modules) {
				if (started)
					throw new IllegalStateException("is was started now");
				modules.clear();
				index = key = -1;
			}
		}

		@Override
		public void onTaskFinish(boolean update, int len) {
			if (!update)
				IPanelLog.d(TAG, "file onTaskFinish but no update !!! ignore it!");
			try {
				if (!task.getIsBigFile()) {
					MemoryFile mf = task.getMetaFile();
					MemeoryFileInputStream is = new MemeoryFileInputStream();
					is.setAttr(mf, 0, len);
					InputSource src = new InputSource(is);
					MetaInfo.Dii.Mod m = modules.peek();
					MetaInfo.Module mod = metaBuilder.parseModule(m.getPID(), src);

					if (mod == null) {
						onTaskFailed(-3002, "file parseModule failed");
						return;
					} else {
						IPanelLog.i(TAG, "parseModule success.KEY=" + key);
					}
					MetaInfo.Module.Buf buf = mod.getBuf(key);
					if (buf == null) {
						onTaskFailed(-3003, "file buffer missing");
						return;
					}
					modules.removeFirst();
					mf = task.getDataFile();
					onFileBlockFound(index, mf, buf.off, buf.len);
				} else {
					onFileBlockFound(index, null, 0, len);
				}
				BigFileDownloader.this.onFileTaskFinished();
			} catch (Exception e) {
				onTaskFailed(-3009, "file onTaskFinish error:" + e);
				return;
			}
			IPanelLog.i(TAG, " onTaskFinish: out" + len);
		}

		@Override
		public void onTaskFailed(int code, String estr) {
			BigFileDownloader.this.onTaskFailed(code, estr);
		}
	}

}

class MemeoryFileInputStream extends InputStream {

	MemoryFile mf;
	int offset = 0;
	int mfLen = 0;

	final String TAG = "MemeoryFileInputStream";

	void setAttr(MemoryFile mf, int off, int mfLen) {
		this.mf = mf;
		this.offset = off;
		this.mfLen = mfLen;
		IPanelLog.i(TAG, "setAttr offset:" + offset + ",mfLen:" + mfLen);
	}

	@Override
	public synchronized void reset() throws IOException {
		// TODO Auto-generated method stub
		this.offset = 0;
		super.reset();
	}

	@Override
	public void mark(int readlimit) {
		// TODO Auto-generated method stub
		this.offset = 0;
		super.mark(readlimit);
	}

	@Override
	public int read(byte[] b) throws IOException {
		// TODO Auto-generated method stub
		int len = 0;
		IPanelLog.i(TAG, "read(byte[] b) in：offset:" + offset + ",mfLen:" + mfLen);
		if (offset >= mfLen) {
			return -1;
		}
		IPanelLog.i(TAG, "b.length:" + b.length);
		IPanelLog.i(TAG, "mf.length:" + mf.length());
		if (b.length < mfLen) {
			if ((mfLen - offset) > b.length) {
				len = mf.readBytes(b, offset, 0, b.length);
			} else {
				int templen = mfLen - offset;
				len = mf.readBytes(b, offset, 0, templen);
			}
		} else {
			len = mf.readBytes(b, 0, 0, mfLen);
		}
		this.offset += len;
		IPanelLog.i(TAG, "read(byte[] b)out:offset:" + offset + ",mfLen:" + mfLen);
		return len;
	}

	@Override
	public int read(byte[] buffer, int offset, int length) throws IOException {
		// TODO Auto-generated method stub
		IPanelLog.i(TAG, " read(byte[] buffer, int offset, int length) in:---offset:" + offset
				+ ",mfLen:" + mfLen);
		if (offset >= mfLen) {
			return -1;
		}

		int len = mf.readBytes(buffer, this.offset, offset, length);
		this.offset += len;
		IPanelLog.i(TAG, " read(byte[] buffer, int offset, int length)out:---:offset:" + offset
				+ ",mfLen:" + mfLen);

		return len;
	}

	@Override
	public int read() throws IOException {
		// TODO Auto-generated method stub
		IPanelLog.i(TAG, "read():in:offset:" + offset + ",mfLen:" + mfLen);
		if (offset >= mfLen) {
			return -1;
		}
		byte[] b = new byte[1];
		int len = mf.readBytes(b, this.offset, this.offset, 1);
		this.offset += len;
		len = (len == -1 ? len : b[0]);
		IPanelLog.i(TAG, "read():out:offset:" + offset + ",mfLen:" + mfLen + ",len" + len);
		return len;
	}

}
