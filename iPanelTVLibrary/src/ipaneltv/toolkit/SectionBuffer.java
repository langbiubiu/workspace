package ipaneltv.toolkit;

import java.io.FileDescriptor;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.nio.charset.Charset;
import java.util.List;

import android.net.telecast.JSectionFilter;
import android.net.telecast.SectionFilter;
import android.net.telecast.SectionStorage;
import android.util.Log;

/**
 * 段数据缓冲区对象
 */
public final class SectionBuffer extends Natives {

	/**
	 * 创建独立的缓冲区
	 * 
	 * @param length
	 *            长度
	 * @return 对象
	 */
	public static SectionBuffer createSectionBuffer(int length) {
		if (length <= 0)
			throw new IllegalArgumentException();
		if (length < 128)
			length = 128;
		else if (length < 256)
			length = 256;
		else if (length < 512)
			length = 512;
		else if (length < 1024)
			length = 1024;
		else
			length = 4096;
		return new SectionBuffer(length);
	}

	SectionBuffer(int p, int len) {
		this.buffer = p;
		this.length = len;
		free = false;
	}

	SectionBuffer(int len) {
		if ((buffer = calloc(1, len)) == 0)
			throw new OutOfMemoryError();
		length = len;
		free = true;
	}

	/**
	 * 读取Filter中的当前数据
	 * <p>
	 * 在回调函数SectionRetrieved()中执行此函数
	 * {@link android.net.telecast.SectionFilter.SectionDisposeListener#onSectionRetrieved()}
	 * 
	 * @param filter
	 *            过滤器
	 * @return 数据长度，如果读取失败返回-1
	 */
	public int copyFrom(SectionFilter filter) {
		synchronized (mutex) {
			if (filter instanceof JSectionFilter) {
				JSectionFilter jf = (JSectionFilter) filter;
				byte[] buf = jf.peekSection();
				jnmemcpy(buffer, buf, 0, buf.length);
				return buf.length;
			}
			datalen = dupfseca(filter, buffer, length);
			return datalen;
		}
	}

	/**
	 * 从SectionStorage中读取数据
	 * 
	 * @param storage
	 *            存储对象
	 * @param freq
	 *            频率
	 * @param pid
	 *            pid
	 * @param tableId
	 *            表ID
	 * @param sn
	 *            section编号
	 * @return 读到的数据长度
	 * @throws IOException
	 */
	public int copyFrom(SectionStorage storage, long freq, int pid, int tableId, int sn)
			throws IOException {
		String p = storage.getTableStoragePath(freq, pid, tableId);
		if (p == null)
			throw new IOException("no such storage!");
		RandomAccessFile raf = null;
		try {
			raf = new RandomAccessFile(p, "r");
			int ret = lsecstor(getfd(raf.getFD()), sn, buffer, length);
			if (ret > 0) {
				datalen = ret;
				return ret;
			}
			datalen = 0;
			return -1;
		} finally {
			if (raf != null)
				raf.close();
		}
	}

	/**
	 * 数据复制到指定的buffer中
	 * 
	 * @param sb
	 *            目标缓冲区
	 * @return 复制的数据长度
	 */
	public int copyFrom(SectionBuffer sb) {
		int len = 0;
		if (sb != this) {
			synchronized (mutex) {
				check();
				synchronized (sb.mutex) {
					sb.check();
					len = length > sb.datalen ? sb.datalen : length;
					if (len > 0)
						memcpy(this.buffer, sb.buffer, len);
					this.datalen = len;
				}
				return len;
			}
		}
		return 0;
	}

	/**
	 * 读取数据
	 * 
	 * @param b
	 *            数组
	 * @return 读取到的长度
	 */
	public int read(byte[] b) {
		return read(0, b, b.length);
	}

	/**
	 * 读取数据
	 * 
	 * @param offset
	 *            偏移量
	 * @param b
	 *            数组
	 * @param len
	 *            要复制的数据长度
	 * @return 读取到的长度
	 */
	public int read(int offset, byte[] b, int len) {
		if (len < 0 || len > b.length)
			throw new IndexOutOfBoundsException();
		if (len == 0)
			return 0;
		synchronized (mutex) {
			check();
			if (offset < 0 || offset >= length)
				throw new IllegalArgumentException();
			len = datalen - offset > len ? len : datalen - offset;
			if (len > 0)
				njmemcpy(b, 0, buffer + offset, len);
			return len;
		}
	}

	/**
	 * 保存到文件句柄中
	 * 
	 * @param fd
	 *            文件句柄
	 * @param seek
	 *            偏
	 * @param len
	 *            从本缓冲区中要写入到文件中的数据长度
	 * @throws IOException
	 *             如果出现IO错误
	 */
	public void save(FileDescriptor fd, int seek, int len) throws IOException {
		synchronized (mutex) {
			check();
			if (len < 0 || seek < 0)
				throw new IllegalArgumentException();
			if (len == 0)
				return;
			if (llseek(getfd(fd), seek, SEEK_SET) < 0)
				throw new IOException("seek failed!");
			len = len > datalen ? datalen : len;
			if (len > 0)
				if (write(getfd(fd), buffer, len) < 0)
					throw new IOException("write error");
		}
	}

	/**
	 * 写入段数据存储
	 * 
	 * @param storage
	 *            存储对象
	 * @param freq
	 *            频率
	 * @param pid
	 *            流pid
	 * @param tableId
	 *            表id
	 * @param list
	 *            列表
	 * @throws IOException
	 *             如果发生IO错误
	 */
	public static void saveToSectionStorage(SectionStorage storage, long freq, int pid,
			int tableId, List<SectionBuffer> list) throws IOException {
		String p = storage.getTableStoragePath(freq, pid, tableId);
		if (p == null)
			throw new IOException("no such storage!");
		RandomAccessFile raf = null;
		int np[] = new int[list.size()];
		int nl[] = new int[list.size()];
		for (int i = 0; i < nl.length; i++) {
			SectionBuffer sb = list.get(i);
			np[i] = sb.buffer;
			nl[i] = sb.datalen;
		}
		try {
			raf = new RandomAccessFile(p, "rw");
			if (ssecfile(getfd(raf.getFD()), 0, np, nl, nl.length) != 0)
				throw new IOException("write section file failed");
		} finally {
			if (raf != null)
				raf.close();
		}
	}
	
	public static void saveToSectionStorage(String root, long freq, int pid,
			int tableId, List<SectionBuffer> list) throws IOException {
		String p = root + "/" + freq + "-" + pid + "-" + tableId + ".sections";
		RandomAccessFile raf = null;
		int np[] = new int[list.size()];
		int nl[] = new int[list.size()];
		for (int i = 0; i < nl.length; i++) {
			SectionBuffer sb = list.get(i);
			np[i] = sb.buffer;
			nl[i] = sb.datalen;
		}
		try {
			raf = new RandomAccessFile(p, "rw");
			if (ssecfile(getfd(raf.getFD()), 0, np, nl, nl.length) != 0)
				throw new IOException("write section file failed");
		} finally {
			if (raf != null)
				raf.close();
		}
	}

	/**
	 * 从文件中加载数据
	 * 
	 * @param fd
	 *            句柄
	 * @param seek
	 *            位置
	 * @param len
	 *            长度
	 * @throws IOException
	 *             如果出现IO错误
	 */
	public void load(FileDescriptor fd, int seek, int len) throws IOException {
		synchronized (mutex) {
			check();
			if (len < 0 || seek < 0 || len < 0 || len > length)
				throw new IllegalArgumentException();
			if (len == 0)
				return;
			if (llseek(getfd(fd), seek, SEEK_SET) < 0)
				throw new IOException("seek failed!");
			if ((len = read(getfd(fd), buffer, len)) < 0)
				throw new IOException("write error");
			datalen = len;
		}
	}

	/**
	 * 释放对象
	 */
	public void release() {
		synchronized (mutex) {
			if (buffer != 0) {
				int p = buffer;
				buffer = 0;
				if (free)
					free(p);
			}
		}
	}

	/**
	 * 缓冲区长度
	 * 
	 * @return 值
	 */
	public int getLength() {
		return length;
	}

	/**
	 * 得到指定名称的数据值
	 * <p>
	 * 建议应用程序使用 <code>ipaneltv.dvbsi.*;</code> 所定义的对象来使用具体的表中的数据
	 * <p>
	 * 直接使用名称来获取，参考如下:<br>
	 * DVB的NIT表Section中的network_id: "DVBNIT.network_id"<br>
	 * PMT表Section中的节目组件数量: "PMT.component_size"<br>
	 * PMT表Section中的第二个节目组件的pid: "PMT.component[1].stream_pid"<br>
	 * 
	 * @param name
	 *            名称
	 * @return 值
	 */
	public int getIntByName(String name) {
		synchronized (mutex) {
			check();
			return rsecint(buffer, length, name);
		}
	}

	/**
	 * 得到指定名称的数据值
	 */
	public long getLongByName(String name) {
		synchronized (mutex) {
			check();
			return rseclong(buffer, length, name);
		}
	}

	/**
	 * 得到指定名称的数据值
	 * 
	 * @return RFC3339 时间
	 */
	public String getDateByName(String name) {
		synchronized (mutex) {
			check();

			return rsecdate(buffer, length, name);
		}
	}

	/**
	 * 得到指定名称的数据值
	 */
	public String getTextByName(String name) {
		return getTextByName(name, null);
	}

	/**
	 * 得到指定名称的数据值
	 */
	public String getTextByName(String name, String encoding) {
		synchronized (mutex) {
			check();
			byte[] b = rsectext(buffer, length, name);
			if (b == null)
				return null;
			Log.d(TAG, "getTextByName b[0] = "+ (b[0]&0xff));
			Log.d(TAG, "getTextByName encoding "+encoding);
			if (encoding == null && b[0] > 0){
				encoding = new String(b, 1, b[0], mCharsetUTF8);
				Log.d(TAG, "encoding = "+encoding);
				if(encoding.trim().equals("iso8859-1")){
					encoding = "gb2312";
				}
			}
			Charset cset = encoding == null ? mCharsetUTF8 : Charset.forName(encoding);
			return new String(b, 1 + b[0], b.length - 1 - b[0], cset);
		}
	}

	/**
	 * 判断特殊码流（没有字符编码描述和分段）
	 * 
	 * @param b
	 * @return
	 */
	public boolean isHaveEncoding(byte[] b) {
		boolean haveEncoding = false;
		for (byte c : b) {
			if (c == 0) {
				haveEncoding = true;
				break;
			}
		}
		return haveEncoding;
	}

	/**
	 * 得到指定名称的数据值
	 */
	public byte[] getBlobValue(String name) {
		synchronized (mutex) {
			check();
			return rsecblob(buffer, length, name);
		}
	}

	/**
	 * 得到指定名称的字节序列
	 */
	public byte[] getDescriptorByName(String name) {
		synchronized (mutex) {
			check();
			return rsecblob(buffer, length, name);
		}
	}

	/**
	 * 设置数据长度
	 * 
	 * @param len
	 *            长度
	 */
	public void setDataLength(int len) {
		if (len < 0 || len > length)
			throw new ArrayIndexOutOfBoundsException("bad len:" + len);
		datalen = len;
	}

	/**
	 * 得到数据长度
	 * 
	 * @return 值
	 */
	public int getDataLength() {
		return datalen;
	}

	/**
	 * 得到缓冲区大小
	 * 
	 * @return 值
	 */
	public int getBufferLength() {
		return length;
	}

	protected void finalize() throws Throwable {
		try {
			release();
		} catch (Exception e) {
		}
	}

	void check() {
		if (buffer == 0)
			throw new IllegalStateException("object has released");
	}

	static Charset mCharsetUTF8 = Charset.forName("utf-8");
	private Object mutex = new Object();
	private boolean free = false;
	private int buffer = 0;
	private int length = -1;
	private int datalen;

	/* @hide */
	@Deprecated
	// 一般不要使用此函数
	public int getNativeBufferAddress() {
		return buffer;
	}

	/* @hide */
	@Deprecated
	// 一般不要使用此函数
	public void setNativeBufferAddress(int p, int len) {
		if (!free) {
			this.buffer = p;
			this.datalen = len;
			this.length = len;
		}
	}

	/* @hide */
	@Deprecated
	// 一般不要使用此函数
	public static SectionBuffer createDummy() {
		return new SectionBuffer(0, 0);
	}
}
