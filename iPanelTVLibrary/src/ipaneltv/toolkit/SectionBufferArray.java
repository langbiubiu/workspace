package ipaneltv.toolkit;

import ipaneltv.toolkit.dvb.DvbConst;

import java.io.FileDescriptor;
import java.io.FileInputStream;
import java.io.IOException;

import android.net.telecast.SectionStorage;

public final class SectionBufferArray extends Natives{
	private Object mutex = new Object();
	private int buffer;
	private SectionBuffer list[];

	SectionBufferArray(int size, int p) {
		list = new SectionBuffer[size];
		if (p == 0)
			throw new IllegalArgumentException();
		buffer = p;
	}

	void set(int index, int p, int len) {
		list[index] = new SectionBuffer(p, len);
		list[index].setDataLength(len);
	}

	public SectionBuffer get(int index) {
		synchronized (mutex) {
			if (list != null)
				return list[index];
			throw new IllegalStateException();
		}
	}

	public int size() {
		synchronized (mutex) {
			if (list != null)
				return list.length;
			throw new IllegalStateException();
		}
	}

	public void release() {
		synchronized (mutex) {
			if (list != null) {
				for (int i = 0; i < list.length; i++) {
					list[i].release();
				}
				list = null;
				Natives.free(buffer);
				buffer = 0;
			}
		}
	}

	/**
	 * ��ȡ�ļ��е�section����
	 * 
	 * @param fd
	 *            �ļ�
	 * @param seek
	 *            ƫ��
	 * @param sectionSize
	 *            ����ȡ��section����
	 * @return ���
	 * @throws IOException
	 *             �������IO����
	 */
	public static SectionBufferArray readSectionFile(FileDescriptor fd, int seek, int sectionSize)
			throws IOException {
		int n = sectionSize;
		int[] p = new int[n];
		int[] len = new int[n];
		if ((n = lsecfile(getfd(fd), seek, p, len, n)) > 0) {
			SectionBufferArray sba = new SectionBufferArray(n, p[0]);
			for (int i = 0; i < n; i++)
				sba.set(i, p[i], len[i]);
			return sba;
		}
		return null;
	}

	/**
	 * ��ȡ�����ݻ���
	 * 
	 * @param storage
	 *            ��������ݴ洢����
	 * @param f
	 *            Ƶ��
	 * @param pid
	 *            ��PID
	 * @param tableid
	 *            ��ID
	 * @return �������б�
	 * @throws IOException
	 *             �������IO����
	 */
	public static SectionBufferArray readSectionStorage(SectionStorage storage, long f, int pid,
			int tableid) throws IOException {
		String path = storage.getTableStoragePath(f, pid, tableid);
		FileInputStream fis = null;
		FileDescriptor fd;
		try {
			fis = new FileInputStream(path);
			if ((fd = fis.getFD()) == null)
				return null;
			int n = 256;
			int[] p = new int[n];
			int[] len = new int[n];
			if ((n = lsecfile(getfd(fd), 0, p, len, n)) > 0) {
				SectionBufferArray sba = new SectionBufferArray(n, p[0]);
				for (int i = 0; i < n; i++){
					sba.set(i, p[i], len[i]);
				}
				return sba;
			}
		} finally {
			if (fis != null)
				fis.close();
		}
		return null;
	}

	/**
	 * ��ȡ�����ݻ���
	 * 
	 * @param storage
	 *            ��������ݴ洢����
	 * @param f
	 *            Ƶ��
	 * @param pid
	 *            ��PID
	 * @param tableid
	 *            ��ID
	 * @return �������б�
	 * @throws IOException
	 *             �������IO����
	 */
	public static SectionBufferArray readSectionStorage(String root, long f, int pid,
			int tableid) throws IOException {
		String path = root + "/" + f + "-" + pid + "-" + tableid + ".sections";
		FileInputStream fis = null;
		FileDescriptor fd;
		try {
			fis = new FileInputStream(path);
			if ((fd = fis.getFD()) == null)
				return null;
			int n = 256;
			int[] p = new int[n];
			int[] len = new int[n];
			if ((n = lsecfile(getfd(fd), 0, p, len, n)) > 0) {
				SectionBufferArray sba = new SectionBufferArray(n, p[0]);
				for (int i = 0; i < n; i++){
					sba.set(i, p[i], len[i]);
				}
				return sba;
			}
		} finally {
			if (fis != null)
				fis.close();
		}
		return null;
	}
	
	/**
	 * ��ȡ�����ݻ���
	 * 
	 * @param storage
	 *            ��������ݴ洢����
	 * @param f
	 *            Ƶ��
	 * @param pid
	 *            ��PID
	 * @param coef
	 *            ���˲���COEF
	 * @param mask
	 *            ���˲���MASK
	 * @param excl
	 *            ���˲���EXCL
	 * @param depth
	 *            �������(�ֽ�)
	 * @return ���˵��Ķ���������
	 * @throws IOException
	 *             �������IO����
	 */
	public static SectionBufferArray readSectionStorage(SectionStorage storage, long f, int pid,
			byte[] coef, byte[] mask, byte[] excl, int depth) throws IOException {
		String path = storage.getTableStoragePath(f, pid, coef[0]);
		FileInputStream fis = null;
		FileDescriptor fd;
		try {
			fis = new FileInputStream(path);
			if ((fd = fis.getFD()) == null)
				return null;
			int n = 256;
			int[] p = new int[n];
			int[] len = new int[n];
			if ((n = lsecfile2(getfd(fd), 0, p, len, n, coef, mask, excl, depth)) > 0) {
				SectionBufferArray sba = new SectionBufferArray(n, p[0]);
				for (int i = 0; i < n; i++)
					sba.set(i, p[i], len[i]);
				return sba;
			}
		} finally {
			if (fis != null)
				fis.close();
		}
		return null;
	}
	
	/**
	 * ��ȡ��ͨ��crcs
	 * @param storage
	 * @param freq
	 * @param pid
	 * @param tableid
	 * @return 
	 * @throws IOException
	 */
	public static CRCList getCrcs(SectionStorage storage, long freq, int pid,int tableid) 
			throws IOException{
		String path = storage.getTableStoragePath(freq, pid, tableid);
		FileInputStream fis = null;
		FileDescriptor fd;
		int ret = -1;
		CRCList cList = null;
		try {
			fis = new FileInputStream(path);
			if ((fd = fis.getFD()) == null)
				return null;
			int []crcs = new int[256];
			int []vn_sn = new int[256];
			ret =  getcrcs(getfd(fd),crcs, vn_sn);
			if(ret>0)
				cList = new CRCList(crcs, vn_sn, ret);
		}  finally {
			if (fis != null)
				fis.close();
		}
		
		return cList;
		
		
	}
	
	public static CRCList getCrcs(String root, long freq, int pid,int tableid) 
			throws IOException{
		String path = root + "/" + freq + "-" + pid + "-" + tableid + ".sections";
		FileInputStream fis = null;
		FileDescriptor fd;
		int ret = -1;
		CRCList cList = null;
		try {
			fis = new FileInputStream(path);
			if ((fd = fis.getFD()) == null)
				return null;
			int []crcs = new int[256];
			int []vn_sn = new int[256];
			ret =  getcrcs(getfd(fd),crcs, vn_sn);
			if(ret>0)
				cList = new CRCList(crcs, vn_sn, ret);
		}  finally {
			if (fis != null)
				fis.close();
		}
		
		return cList;
		
		
	}
	
	/**
	 * ��ȡBat���µ�crcs
	 * @param storage
	 * @param freq
	 * @return
	 * @throws IOException
	 */
	public static CRCList getBatCrcs(SectionStorage storage, long freq ) 
			throws IOException{
		String path = storage.getTableStoragePath(freq, DvbConst.PID_BAT, DvbConst.TID_BAT);
		FileInputStream fis = null;
		FileDescriptor fd;
		int ret = -1;
		CRCList cList = null;
		try {
			fis = new FileInputStream(path);
			if ((fd = fis.getFD()) == null)
				return null;
			int [] crcs = new int[256];
			int [] bouquet_ids = new int[256];
			ret =  getbatcrcs(getfd(fd),crcs,bouquet_ids);
			if(ret>0)
				cList = new CRCList(ret,crcs, bouquet_ids);
		}  finally {
			if (fis != null)
				fis.close();
		}
		
		return cList;
		
		
	}
	
	public static class CRCList {
		int [] crcs;
		int [] vn_sn;
		int size;
		int [] bouquet_ids;
		

		public CRCList(int[] crcs, int[] vn_sn,int size) {
			this.crcs = crcs;
			this.vn_sn = vn_sn;
			this.size = size;
		}
		

		public CRCList(int size, int[] crcs, int[] bouquet_ids) {
			this.crcs = crcs;
			this.size = size;
			this.bouquet_ids = bouquet_ids;
		}


		public int[] getBouquet_ids() {
			return bouquet_ids;
		}
		public int[] getCrcs() {
			return crcs;
		}

		public int[] getVn_sn() {
			return vn_sn;
		}
		
		public int getSize(){
			return size;
		}
		
	}
}
