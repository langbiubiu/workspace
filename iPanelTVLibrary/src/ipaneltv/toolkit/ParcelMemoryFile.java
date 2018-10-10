package ipaneltv.toolkit;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import android.os.Parcel;
import android.os.ParcelFileDescriptor;
import android.os.Parcelable;

public final class ParcelMemoryFile extends Natives implements Parcelable {
	private ParcelFileDescriptor fd;
	private String name;
	private int length;
	private int addr = 0;
	private int prot, share = PROT_READ;
	private boolean allowPurging = false;

	public ParcelMemoryFile(int length) throws IOException {
		this(null, length, false);
	}

	public ParcelMemoryFile(String name, int length) throws IOException {
		this(name, length, false);
	}

	public ParcelMemoryFile(String name, int length, boolean readOnly) throws IOException {
		if (length < 0)
			throw new IllegalArgumentException("length < 0");
		// 对齐到4K
		// (length / 4096 + ((length % 4096) == 0 ? 1 : 0)) * 4096;
		length = ((length >> 12) + ((length & ((1 << 12) - 1)) == 0 ? 1 : 0)) << 12;
		this.length = length == 0 ? (1 << 12) : length;
		prot = getprot(readOnly);
		int nfd = mfopen(name, this.length, prot);
		if (nfd < 0)
			throw new IOException("open memory file failed");
		fd = ParcelFileDescriptor.adoptFd(nfd);
		map(prot);
	}

	static int getprot(boolean readonly) {
		return readonly ? PROT_READ : PROT_WRITE;
	}

	private void map(int prot) throws IOException {
		if ((addr = mmap(fd.getFd(), length, prot)) == 0) {
			fd.close();
			throw new IOException("mmap failed!");
		}
	}

	public void setShare(boolean readOnly) {
		share = readOnly ? PROT_READ : PROT_WRITE;
	}

	public synchronized boolean allowPurging(boolean b) {
		boolean oldValue = allowPurging;
		if (oldValue != b) {
			mfpin(fd.getFd(), !b);
			allowPurging = b;
		}
		return oldValue;
	}

	public String getName() {
		return name;
	}

	public int length() {
		return length;
	}

	public boolean isReadable() {
		return (prot & PROT_READ) != 0;
	}

	public boolean isWritable() {
		return (prot & PROT_WRITE) != 0;
	}

	public int readBytes(int srcOff, byte[] buf) {
		return readBytes(srcOff, buf, 0, buf.length);
	}

	/**
	 * @return 操作成功返回true，否则返回false，此时 当内存已被回收，需要重建对象
	 */
	public synchronized int readBytes(int srcOff, byte[] buf, int off, int count) {
		if (srcOff < 0 || count < 0 || srcOff + count > length || off < 0
				|| srcOff + count < buf.length)
			throw new IllegalArgumentException();
		if (addr == 0)
			throw new IllegalStateException("addr invalid");
		if (!allowPurging && mfpin(fd.getFd(), true) == MF_PURGED_WAS) {
			mfpin(fd.getFd(), false);
			return -1;
		}
		njmemcpy(buf, off, addr + srcOff, count);
		if (!allowPurging) {
			mfpin(fd.getFd(), false);
		}
		return count;
	}

	public int writeBytes(int dstOff, byte[] buf) {
		return writeBytes(dstOff, buf, 0, buf.length);
	}

	/**
	 * @return 操作成功返回true，否则返回false，此时 当内存已被回收，需要重建对象
	 */
	public synchronized int writeBytes(int dstOff, byte[] buf, int off, int count) {
		if (dstOff < 0 || count < 0 || dstOff + count > length || off < 0
				|| dstOff + count < buf.length)
			throw new IllegalArgumentException();
		if (addr == 0)
			throw new IllegalStateException("addr invalid");
		if (!allowPurging && mfpin(fd.getFd(), true) == MF_PURGED_WAS) {
			mfpin(fd.getFd(), false);
			return -1;
		}
		jnmemcpy(addr + dstOff, buf, off, count);

		if (!allowPurging) {
			mfpin(fd.getFd(), false);
		}
		return count;
	}

	public synchronized void release() {
		if (addr != 0) {
			munmap(fd.getFd(), addr, length);
			try {
				fd.close();
			} catch (IOException e) {
			}
			addr = 0;
		}
	}

	ParcelMemoryFile() {
	}

	@Override
	public int describeContents() {
		return 0;
	}

	@Override
	public void writeToParcel(Parcel dest, int flags) {
		dest.writeInt(length);
		dest.writeString(name);
		dest.writeInt(share);
		dest.writeParcelable(fd, 0);
	}

	public static final Parcelable.Creator<ParcelMemoryFile> CREATOR = new Parcelable.Creator<ParcelMemoryFile>() {
		public ParcelMemoryFile createFromParcel(Parcel in) {
			ParcelMemoryFile mf = new ParcelMemoryFile();
			ParcelFileDescriptor fd = in.readParcelable(getClass().getClassLoader());
			mf.length = in.readInt();
			mf.name = in.readString();
			mf.share = in.readInt();
			try {
				mf.fd = fd.dup();
				mf.map(mf.share);
			} catch (IOException e) {
				IPanelLog.e("ParcelMemoryFile", "create from parcel failed:" + e);
				return null;
			}
			return mf;
		}

		public ParcelMemoryFile[] newArray(int size) {
			return new ParcelMemoryFile[size];
		}
	};

	private class MemoryInputStream extends InputStream {

		private int mMark = 0;
		private int mOffset = 0;
		private byte[] mSingleByte;

		@Override
		public int available() throws IOException {
			if (mOffset >= length) {
				return 0;
			}
			return length - mOffset;
		}

		@Override
		public boolean markSupported() {
			return true;
		}

		@Override
		public void mark(int readlimit) {
			mMark = mOffset;
		}

		@Override
		public void reset() throws IOException {
			mOffset = mMark;
		}

		@Override
		public int read() throws IOException {
			if (mSingleByte == null) {
				mSingleByte = new byte[1];
			}
			int result = read(mSingleByte, 0, 1);
			if (result != 1) {
				return -1;
			}
			return mSingleByte[0];
		}

		@Override
		public int read(byte buffer[], int offset, int count) throws IOException {
			if (offset < 0 || count < 0 || offset + count > buffer.length) {
				throw new IndexOutOfBoundsException();
			}
			count = Math.min(count, available());
			if (count < 1) {
				return -1;
			}
			int result = readBytes(mOffset, buffer, offset, count);
			if (result > 0) {
				mOffset += result;
			}
			return result;
		}

		@Override
		public long skip(long n) throws IOException {
			if (mOffset + n > length) {
				n = length - mOffset;
			}
			mOffset += n;
			return n;
		}
	}

	private class MemoryOutputStream extends OutputStream {

		private int mOffset = 0;
		private byte[] mSingleByte;

		@Override
		public void write(byte buffer[], int offset, int count) throws IOException {
			writeBytes(mOffset, buffer, offset, count);
			mOffset += count;
		}

		@Override
		public void write(int oneByte) throws IOException {
			if (mSingleByte == null) {
				mSingleByte = new byte[1];
			}
			mSingleByte[0] = (byte) oneByte;
			write(mSingleByte, 0, 1);
		}

		public long skip(long n) {
			if (mOffset + n > length) {
				n = length - mOffset;
			}
			mOffset += n;
			return n;
		}
	}

	public InputStream getInputStream() {
		return new MemoryInputStream();
	}

	public OutputStream getOutputStream() {
		return new MemoryOutputStream();
	}

	public static void skipOutputStream(OutputStream os, long n) {
		if (os instanceof MemoryOutputStream)
			((MemoryOutputStream) os).skip(n);
	}
}
