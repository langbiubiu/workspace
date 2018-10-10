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
 * �����ݻ���������
 */
public final class SectionBuffer extends Natives {

	/**
	 * ���������Ļ�����
	 * 
	 * @param length
	 *            ����
	 * @return ����
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
	 * ��ȡFilter�еĵ�ǰ����
	 * <p>
	 * �ڻص�����SectionRetrieved()��ִ�д˺���
	 * {@link android.net.telecast.SectionFilter.SectionDisposeListener#onSectionRetrieved()}
	 * 
	 * @param filter
	 *            ������
	 * @return ���ݳ��ȣ������ȡʧ�ܷ���-1
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
	 * ��SectionStorage�ж�ȡ����
	 * 
	 * @param storage
	 *            �洢����
	 * @param freq
	 *            Ƶ��
	 * @param pid
	 *            pid
	 * @param tableId
	 *            ��ID
	 * @param sn
	 *            section���
	 * @return ���������ݳ���
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
	 * ���ݸ��Ƶ�ָ����buffer��
	 * 
	 * @param sb
	 *            Ŀ�껺����
	 * @return ���Ƶ����ݳ���
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
	 * ��ȡ����
	 * 
	 * @param b
	 *            ����
	 * @return ��ȡ���ĳ���
	 */
	public int read(byte[] b) {
		return read(0, b, b.length);
	}

	/**
	 * ��ȡ����
	 * 
	 * @param offset
	 *            ƫ����
	 * @param b
	 *            ����
	 * @param len
	 *            Ҫ���Ƶ����ݳ���
	 * @return ��ȡ���ĳ���
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
	 * ���浽�ļ������
	 * 
	 * @param fd
	 *            �ļ����
	 * @param seek
	 *            ƫ
	 * @param len
	 *            �ӱ���������Ҫд�뵽�ļ��е����ݳ���
	 * @throws IOException
	 *             �������IO����
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
	 * д������ݴ洢
	 * 
	 * @param storage
	 *            �洢����
	 * @param freq
	 *            Ƶ��
	 * @param pid
	 *            ��pid
	 * @param tableId
	 *            ��id
	 * @param list
	 *            �б�
	 * @throws IOException
	 *             �������IO����
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
	 * ���ļ��м�������
	 * 
	 * @param fd
	 *            ���
	 * @param seek
	 *            λ��
	 * @param len
	 *            ����
	 * @throws IOException
	 *             �������IO����
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
	 * �ͷŶ���
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
	 * ����������
	 * 
	 * @return ֵ
	 */
	public int getLength() {
		return length;
	}

	/**
	 * �õ�ָ�����Ƶ�����ֵ
	 * <p>
	 * ����Ӧ�ó���ʹ�� <code>ipaneltv.dvbsi.*;</code> ������Ķ�����ʹ�þ���ı��е�����
	 * <p>
	 * ֱ��ʹ����������ȡ���ο�����:<br>
	 * DVB��NIT��Section�е�network_id: "DVBNIT.network_id"<br>
	 * PMT��Section�еĽ�Ŀ�������: "PMT.component_size"<br>
	 * PMT��Section�еĵڶ�����Ŀ�����pid: "PMT.component[1].stream_pid"<br>
	 * 
	 * @param name
	 *            ����
	 * @return ֵ
	 */
	public int getIntByName(String name) {
		synchronized (mutex) {
			check();
			return rsecint(buffer, length, name);
		}
	}

	/**
	 * �õ�ָ�����Ƶ�����ֵ
	 */
	public long getLongByName(String name) {
		synchronized (mutex) {
			check();
			return rseclong(buffer, length, name);
		}
	}

	/**
	 * �õ�ָ�����Ƶ�����ֵ
	 * 
	 * @return RFC3339 ʱ��
	 */
	public String getDateByName(String name) {
		synchronized (mutex) {
			check();

			return rsecdate(buffer, length, name);
		}
	}

	/**
	 * �õ�ָ�����Ƶ�����ֵ
	 */
	public String getTextByName(String name) {
		return getTextByName(name, null);
	}

	/**
	 * �õ�ָ�����Ƶ�����ֵ
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
	 * �ж�����������û���ַ����������ͷֶΣ�
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
	 * �õ�ָ�����Ƶ�����ֵ
	 */
	public byte[] getBlobValue(String name) {
		synchronized (mutex) {
			check();
			return rsecblob(buffer, length, name);
		}
	}

	/**
	 * �õ�ָ�����Ƶ��ֽ�����
	 */
	public byte[] getDescriptorByName(String name) {
		synchronized (mutex) {
			check();
			return rsecblob(buffer, length, name);
		}
	}

	/**
	 * �������ݳ���
	 * 
	 * @param len
	 *            ����
	 */
	public void setDataLength(int len) {
		if (len < 0 || len > length)
			throw new ArrayIndexOutOfBoundsException("bad len:" + len);
		datalen = len;
	}

	/**
	 * �õ����ݳ���
	 * 
	 * @return ֵ
	 */
	public int getDataLength() {
		return datalen;
	}

	/**
	 * �õ���������С
	 * 
	 * @return ֵ
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
	// һ�㲻Ҫʹ�ô˺���
	public int getNativeBufferAddress() {
		return buffer;
	}

	/* @hide */
	@Deprecated
	// һ�㲻Ҫʹ�ô˺���
	public void setNativeBufferAddress(int p, int len) {
		if (!free) {
			this.buffer = p;
			this.datalen = len;
			this.length = len;
		}
	}

	/* @hide */
	@Deprecated
	// һ�㲻Ҫʹ�ô˺���
	public static SectionBuffer createDummy() {
		return new SectionBuffer(0, 0);
	}
}
