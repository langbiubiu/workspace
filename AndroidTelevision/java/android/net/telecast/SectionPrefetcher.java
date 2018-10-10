package android.net.telecast;

import java.io.FileDescriptor;
import java.util.UUID;

import android.os.ParcelFileDescriptor;
import android.util.Log;

/**
 * ����ʱSectionԤȡ����
 * <p>
 * Ԥȡ�������´�(����һ��)����ʱ��Ч����ǰ����Ϊ���ο������õ���Ԥȡ���ݡ�
 * <p>
 * Ӧ�ó����ͨ���������ڿ���ʱ��ȡ���ݣ��Դ˼����û��ĵȴ�ʱ�䣬������ȡEIT����.
 * <p>
 * Ӧ�ó������ע�⣬����Ԥȡ�������ǲ���Ԥ���(�������ݳ�����Ч)����˱������걸�Դ���
 * ���ܽ�ĳ���ܵ�ʵ����ȫ�����ڴˣ���Ӧ��Ԥȡ��Ϊ����Ч�ʵ��ֶΡ�
 */
public class SectionPrefetcher {
	static final String TAG = "[java]SectionPrefetcher";

	/** ������־-Ĭ�� */
	public static final int FLAG_CREATE_DEFAULT = 0;
	/** ������־-�����Զ����� */
	public static final int FLAG_CREATE_OPEN_EXIST = 0x01;
	/** ������־-�ؼ���,�����Դ��ͻ����ռ��ͨԤȡ������Դ,��ҪϵͳȨ�� */
	public static final int FLAG_CREATE_CRITICAL = 0x02;
	/** ������־-ģ����̣����ڵ��� */
	public static final int FLAG_CREATE_SIMULATE = 0x04;

	/** �ƻ���־-Ĭ�� */
	public static final int FLAG_SCHEDULE_DEFAULT = 0;
	/** �ƻ���־-������Ч,��ҪϵͳȨ�� */
	public static final int FLAG_SCHEDULE_PERSISTENT = 0x80000000;

	/** �ڴ��־-Ĭ�� */
	public static final int FLAG_MEMORY_DEFAULT = 0;
	/** �ڴ��־-�����ڷ�����̱������ */
	public static final int FLAG_MEMORY_NO_KEEP_IN_SERVER = 0x1;

	TransportManager tmanager;
	int peer;
	String uuid, name;

	int flags;

	SectionPrefetcher(TransportManager tm, String uuid, String name, int flags) {
		this.tmanager = tm;
		UUID id = UUID.fromString(uuid);
		this.uuid = id.toString();
		this.flags = flags;
		if (name.length() > 31)
			name = name.substring(0, 31);
		native_create(id.getMostSignificantBits(), id.getLeastSignificantBits(), name, flags);
		if (peer == 0)
			throw new RuntimeException("open prefetcher failed!");
	}

	/**
	 * �õ�����
	 * 
	 * @return �ַ���
	 */
	public String getName() {
		return name;
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
	 * �����´ο���Ԥȡ�ƻ�
	 * <p>
	 * ʹ���봫������ƥ���Ĭ�ϵ�����ӿ�
	 * <p>
	 * �����ڴ��ļ���ͨ���������ַ������ {@link #getMemoryFile()}
	 * @param fi
	 *            Ƶ�ʲ���
	 * @param bufSize
	 *            ��������С
	 * @param pid
	 *            ��PID
	 * @param tableid
	 *            ��ID
	 * @param flags
	 *            ��־��Ĭ��Ϊ0
	 * @return �ɹ��򷵻�true,���򷵻�false
	 */
	public boolean schedule(FrequencyInfo fi, int bufSize, int pid, int tableid, int flags) {
		NetworkInterface ni = tmanager.getDefaultNetworkInterfaceByType(fi.getDeliveryType());
		if (ni == null)
			return false;
		byte coef[] = new byte[] { (byte) tableid };
		byte mask[] = new byte[] { (byte) 0xff };
		byte excl[] = new byte[] { (byte) 0 };
		return schedule(ni.getId(), fi, bufSize, pid, coef, mask, excl, 1, flags);
	}

	/**
	 * �����´ο���Ԥȡ�ƻ�
	 * 
	 * @param interfaceId
	 *            ����ӿ�ID
	 * @param fi
	 *            Ƶ�ʲ���
	 * @param bufSize
	 *            ��������С
	 * @param pid
	 *            ��PID
	 * @param coef
	 *            �Ƚϲ���
	 * @param mask
	 *            �������
	 * @param excl
	 *            �ǱȲ���
	 * @param depth
	 *            ���(0-16)
	 * @param flags
	 *            ��־��Ĭ��Ϊ0
	 * @return �ɹ��򷵻�true,���򷵻�false
	 */
	public boolean schedule(int interfaceId, FrequencyInfo fi, int bufSize, int pid, byte[] coef,
			byte[] mask, byte[] excl, int depth, int flags) {
		String sfi = fi.toString();
		if (fi.getFrequency() == FrequencyInfo.INVALID_FREQUENCY) {
			Log.d(TAG, "frequncy invalid!");
			return false;
		}
		NetworkInterface ni = tmanager.getNetworkInterfaceById(interfaceId);
		if (ni != null) {
			if (ni.getDevliveryType() != fi.getDeliveryType())
				return false;
		} else {
			if ((ni = tmanager.getDefaultNetworkInterfaceByType(fi.getDeliveryType())) == null)
				return false;
			interfaceId = ni.getId();
		}
		if (pid < 0 || pid >= 8192) {
			Log.d(TAG, "invalid pid:" + pid);
			return false;
		}
		if (depth < 0 || depth >= 16)
			throw new IllegalArgumentException("invalid depth");
		if (coef.length < depth || mask.length < depth || excl.length < depth)
			throw new IndexOutOfBoundsException();
		if (bufSize < 0)
			bufSize = 0;
		return native_sched(interfaceId, sfi, bufSize, pid, coef, mask, excl, depth, flags) == 0;
	}

	/**
	 * �ƻ�ͨ��lua�ű�ִ��Ԥȡ����
	 * 
	 * @param fd
	 *            �ű����ļ����
	 * @param flags
	 *            ��־��Ĭ��Ϊ0
	 * @return �ɹ��򷵻�true,���򷵻�false
	 */
	public boolean schedule(FileDescriptor fd, int flags) {
		if (fd == null)
			throw new NullPointerException("fd is null");
		return native_fsched(fd, flags) == 0;
	}

	/**
	 * �ƻ�ͨ��lua�ű�ִ��Ԥȡ����
	 * 
	 * @param lua
	 *            �ű��ı�
	 * @param flags
	 *            ��־��Ĭ��Ϊ0
	 * @return �ɹ��򷵻�true,���򷵻�false
	 */
	public boolean schedule(String lua, int flags) {
		if (lua == null)
			throw new NullPointerException("buf is null");
		return native_bsched(lua, flags) == 0;
	}

	/**
	 * ȡ��Ԥȡ�ƻ�
	 */
	public void cancelScheduling() {
		native_cancel();
	}

	/**
	 * �Ƿ������üƻ�
	 * 
	 * @return �Ƿ���true,���򷵻�false
	 */
	public boolean isShceduled() {
		return native_is_sche() == 0;
	}

	/**
	 * �ͷŶ�������Դ
	 */
	public void close() {
		close(false);
	}

	/**
	 * �رղ��ƶ��Ƿ���������
	 * 
	 * @param bufRetain
	 *            ��������Ϊtrue,��������Ϊfalse
	 */
	public void close(boolean bufRetain) {
		synchronized (TAG) {
			if (peer != 0)
				native_release(bufRetain);
		}
	}

	/**
	 * �õ����������ڴ�(ashmem)�ľ��,���Ҫӳ�䵽����������Ҫʹ��mmap����
	 * 
	 * @param name
	 *            ����
	 * @return ���
	 */
	public ParcelFileDescriptor getMemoryFile(String name, int flags) {
		int fd = native_getsmf(name, flags);
		if (fd <= 0)
			throw new NullPointerException("fd is null or invalid");
		return ParcelFileDescriptor.adoptFd(fd);
	}

	/**
	 * �õ����������ڴ�(ashmem)�ľ��.
	 * 
	 * @return ���
	 */
	public ParcelFileDescriptor getMemoryFile() {
		return getMemoryFile(null, 0);
	}

	@Override
	protected void finalize() throws Throwable {
		close();
		super.finalize();
	}

	native int native_create(long m, long l, String name, int flags);

	native int native_getsmf(String name, int flags);

	native int native_sched(int ifid, String fi, int bs, int pid, byte[] c, byte[] m, byte[] e,
			int d, int flags);

	native int native_fsched(FileDescriptor fd, int flags);

	native int native_bsched(String buf, int flags);

	native int native_is_sche();

	native int native_cancel();

	native void native_release(boolean b);
}
