package ipaneltv.toolkit;

import java.io.FileDescriptor;

import android.net.telecast.SectionFilter;
import android.os.MemoryFile;

/*@hide*/
public class Natives {
	protected static final int SEEK_SET = 0;
	protected static final int SEEK_CUR = 1;
	protected static final int SEEK_END = 2;

	protected static final int PROT_NONE = 0;
	protected static final int PROT_READ = 1;
	protected static final int PROT_WRITE = 2;
	protected static final int PROT_EXEC = 4;

	protected static final int MF_PURGED_NOT = 0;
	protected static final int MF_PURGED_WAS = 1;
	protected static final int MF_PIN_UNPINNED = 0;
	protected static final int MF_PIN_PINNED = 1;

	protected static final int MAP_FILE = 0;
	protected static final int MAP_SHARED = 1;
	protected static final int MAP_PRIVATE = 2;
	protected static final int MAP_TYPE = 0xF;
	protected static final int MAP_FIXED = 0x10;
	protected static final int MAP_ANONYMOUS = 0x20;
	protected static final int MAP_ANON = MAP_ANONYMOUS;
	protected static final int MAP_NORESERVE = 0x4000;
	protected static final int MAP_AUTOGROW = 0x8000;

	protected static final int FT_FILE = 1;// 常规文件
	protected static final int FT_DIR = 2;// 目录
	protected static final int FT_CHAR = 3;// 字符设备
	protected static final int FT_BLOCK = 4;// 块设备
	protected static final int FT_FIFO = 5;// 流式文件(管道)
	protected static final int FT_LINK = 6;// 符号链接
	protected static final int FT_SOCK = 7;// 套接字

	protected static final int FO_RDONLY = 0;
	protected static final int FO_WRONLY = 1; 
	protected static final int FO_RDWR = 2; 
	protected static final int FO_APPEND = 0x0008;
	protected static final int FO_CREAT = 0x0200;
	protected static final int FO_TRUNC = 0x0400;
	protected static final int FO_EXCL = 0x0800;
	protected static final int FO_SYNC = 0x2000;

	protected native static final int malloc(int len);

	protected native static final int calloc(int b, int len);

	protected native static final void free(int p);

	protected native static final void memsetb(int p, int v);

	protected native static final void memsetf(int p, float v);

	protected native static final void memsetd(int p, double v);

	protected native static final void memseti(int p, int v);
	
	protected native static final void memsetl(int p, long v);

	protected native static final void memsetutf(int p, String s, int len);																			

	protected native static final int memcpy(int dst, int src, int len);

	protected native static final int memmove(int dst, int src, int len);

	protected native static final void njmemcpy(byte[] dst, int off, int src, int len);/*- native to java memcpy */

	protected native static final void jnmemcpy(int dst, byte[] src, int off, int len);/*- java to native memcpy */

	protected native static final int ftype(String path);

	protected native static final int flength(String path);

	protected native static final int fopen(String path, String mode);

	protected native static final int fclose(int f);

	protected native static final int fread(int f, int p, int len);

	protected native static final int fwrite(int f, int p, int len);

	protected native static final int fseek(int f, long offset, int where);

	protected native static final long ftell(int f);

	protected native static final int fflush(int f);

	protected native static final int fileno(int f);

	protected native static final int fcopy(int dst, int src, int len);/*- file copy */

	protected native static final int getfd(FileDescriptor fd);

	protected native static final void setfd(FileDescriptor fd, int v);

	protected native static final int open(String p, int mode);

	protected native static final int close(int fd);

	protected native static final long llseek(int fd, long off, int where);

	protected native static final int read(int fd, int p, int len);

	protected native static final int write(int fd, int p, int len);

	protected native static final int mmap(int fd, int len, int prot);

	protected native static final int munmap(int fd, int addr, int len);

	protected native static final int amfaddr(MemoryFile mf); /*- memory addr */

	protected native static final int mfopen(String name, int len, int prot); /*- memory open */

	protected native static final int mfprot(int fd, int prot); /*- memory prot */

	protected native static final int mfpin(int fd, boolean b); /*- memory pin */

	protected native static final int mflen(int fd); /*- memory len */

	protected native static final int remove(String path);/*- remove file */

	protected native static final int rename(String path, String newpath);/*- rename file */

	protected native static final int mkdir(String path, int mode);

	protected native static final int mkdirs(String path, int mode);

	protected native static final int cleardir(String path);

	protected native static final int memgetb(int p);

	protected native static final int memgeti(int p);
	
	protected native static final long memgetl(int p);

	protected native static final float memgetf(int p);

	protected native static final double memgetd(int p);

	protected native static final String memgetutf(int p, int len);

	static native final int rsecint(int p, int len, String name);/*- read section int by name*/

	static native final long rseclong(int p, int len, String name);/*- read section long by name*/

	static native final String rsecdate(int p, int len, String name);/*- read section time(RFC 3339) by name*///

	static native final byte[] rsectext(int p, int len, String name);/*- read section text by name*/

	static native final byte[] rsecblob(int p, int len, String name);/*- read section blob by name*/

	static native final int lsecfile(int fd, int seek, int[] p, int[] len, int size);

	static native final int lsecfile2(int fd, int seek, int[] p, int[] len, int size, byte[] c,
			byte[] m, byte[] r, int d);

	static native final int ssecfile(int fd, int seek, int[] p, int[] len, int size);

	static native final int lsecstor(int fd, int sn, int p, int len);

	static native final int dupfseca(SectionFilter f, int p, int len);// dup all

	/** vn_sn为标识section的version_number(高16位)+section_number(低16位)，返回crc个数, */
	static native final int getcrcs(int fd, int[] crcs, int[] vn_sn);//

	static native final int getbatcrcs(int fd, int[] crcs, int[] bouquet_ids);

	static {
		try {
			System.loadLibrary("ipaneltvlibrary-jni");
		} catch (Exception e) {
			IPanelLog.e("NATIVES", "loadLibrary 'ipaneltvlibrary' failed: " + e);
		}
	}
	static final String TAG = "[java]Natives";

	/**
	 * @hide
	 * @deprecated
	 */
	public static void ensure() {
	}
}
