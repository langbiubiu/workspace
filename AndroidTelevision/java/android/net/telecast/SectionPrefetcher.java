package android.net.telecast;

import java.io.FileDescriptor;
import java.util.UUID;

import android.os.ParcelFileDescriptor;
import android.util.Log;

/**
 * 开机时Section预取对象
 * <p>
 * 预取操作在下次(仅下一次)开机时生效。当前数据为本次开机所得到的预取数据。
 * <p>
 * 应用程序可通过本对象在开机时收取数据，以此减少用户的等待时间，比如收取EIT数据.
 * <p>
 * 应用程序必须注意，开机预取的最坏结果是不可预测的(比如数据彻底无效)，因此必须做完备性处理。
 * 不能将某功能的实现完全依托于此，而应将预取作为提升效率的手段。
 */
public class SectionPrefetcher {
	static final String TAG = "[java]SectionPrefetcher";

	/** 创建标志-默认 */
	public static final int FLAG_CREATE_DEFAULT = 0;
	/** 创建标志-仅打开自动创建 */
	public static final int FLAG_CREATE_OPEN_EXIST = 0x01;
	/** 创建标志-关键的,如果资源冲突会抢占普通预取器的资源,需要系统权限 */
	public static final int FLAG_CREATE_CRITICAL = 0x02;
	/** 创建标志-模拟过程，用于调试 */
	public static final int FLAG_CREATE_SIMULATE = 0x04;

	/** 计划标志-默认 */
	public static final int FLAG_SCHEDULE_DEFAULT = 0;
	/** 计划标志-持续生效,需要系统权限 */
	public static final int FLAG_SCHEDULE_PERSISTENT = 0x80000000;

	/** 内存标志-默认 */
	public static final int FLAG_MEMORY_DEFAULT = 0;
	/** 内存标志-无需在服务进程保留句柄 */
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
	 * 得到名称
	 * 
	 * @return 字符串
	 */
	public String getName() {
		return name;
	}

	/**
	 * 得到网络UUID
	 * 
	 * @return 值
	 */
	public String getNetworkUUID() {
		return uuid;
	}

	/**
	 * 安排下次开机预取计划
	 * <p>
	 * 使用与传输类型匹配的默认的网络接口
	 * <p>
	 * 数据内存文件可通过空名称字符串获得 {@link #getMemoryFile()}
	 * @param fi
	 *            频率参数
	 * @param bufSize
	 *            缓冲区大小
	 * @param pid
	 *            流PID
	 * @param tableid
	 *            表ID
	 * @param flags
	 *            标志，默认为0
	 * @return 成功则返回true,否则返回false
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
	 * 安排下次开机预取计划
	 * 
	 * @param interfaceId
	 *            网络接口ID
	 * @param fi
	 *            频率参数
	 * @param bufSize
	 *            缓冲区大小
	 * @param pid
	 *            流PID
	 * @param coef
	 *            比较参数
	 * @param mask
	 *            掩码参数
	 * @param excl
	 *            非比参数
	 * @param depth
	 *            深度(0-16)
	 * @param flags
	 *            标志，默认为0
	 * @return 成功则返回true,否则返回false
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
	 * 计划通过lua脚本执行预取数据
	 * 
	 * @param fd
	 *            脚本的文件句柄
	 * @param flags
	 *            标志，默认为0
	 * @return 成功则返回true,否则返回false
	 */
	public boolean schedule(FileDescriptor fd, int flags) {
		if (fd == null)
			throw new NullPointerException("fd is null");
		return native_fsched(fd, flags) == 0;
	}

	/**
	 * 计划通过lua脚本执行预取数据
	 * 
	 * @param lua
	 *            脚本文本
	 * @param flags
	 *            标志，默认为0
	 * @return 成功则返回true,否则返回false
	 */
	public boolean schedule(String lua, int flags) {
		if (lua == null)
			throw new NullPointerException("buf is null");
		return native_bsched(lua, flags) == 0;
	}

	/**
	 * 取消预取计划
	 */
	public void cancelScheduling() {
		native_cancel();
	}

	/**
	 * 是否已设置计划
	 * 
	 * @return 是返回true,否则返回false
	 */
	public boolean isShceduled() {
		return native_is_sche() == 0;
	}

	/**
	 * 释放对象及其资源
	 */
	public void close() {
		close(false);
	}

	/**
	 * 关闭并制定是否保留缓冲区
	 * 
	 * @param bufRetain
	 *            是则设置为true,否则设置为false
	 */
	public void close(boolean bufRetain) {
		synchronized (TAG) {
			if (peer != 0)
				native_release(bufRetain);
		}
	}

	/**
	 * 得到匿名共享内存(ashmem)的句柄,如果要映射到本进程则需要使用mmap函数
	 * 
	 * @param name
	 *            名称
	 * @return 句柄
	 */
	public ParcelFileDescriptor getMemoryFile(String name, int flags) {
		int fd = native_getsmf(name, flags);
		if (fd <= 0)
			throw new NullPointerException("fd is null or invalid");
		return ParcelFileDescriptor.adoptFd(fd);
	}

	/**
	 * 得到匿名共享内存(ashmem)的句柄.
	 * 
	 * @return 句柄
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
