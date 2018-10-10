package android.net.telecast;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map.Entry;
import java.util.Timer;
import java.util.TimerTask;
import java.util.UUID;

import android.util.Log;

/**
 * 段数据注入器
 * <p>
 * 可向网络数据接收点注入段数据，使得本机环境中的数据接收者，可以收取到注入的数据，以此来满足业务上的需求
 * <p>
 * 需要声明权限:android.net.telecast.permission.ACCESS_TEEVEE_RESOURCE
 */
public class SectionInjector {
	final static String TAG = "[java]SectionInjector";
	final static HashMap<String, MainSectionInjector> injectors = new HashMap<String, MainSectionInjector>();

	int peer, buffer, delay = 0, period = 64, pid = 0;
	int counts = 5, secondaryPeriod = 1024;

	final String uuid;
	long freq = 0;

	static SectionInjector createInjector(String uuid) {
		try {
			synchronized (injectors) {
				MainSectionInjector mi = injectors.get(uuid);
				if (mi == null) {
					mi = new MainSectionInjector(uuid);
					injectors.put(uuid, mi);
				}
				return mi.addTaskInjector();
			}
		} catch (Exception e) {
			Log.e(TAG, "create injector error:" + e);
		}
		return null;
	}

	SectionInjector(String uuid) {
		this.uuid = uuid;
	}

	/**
	 * 释放资源
	 */
	public void release() {
		synchronized (injectors) {
			for (Entry<String, MainSectionInjector> e : injectors.entrySet()) {
				injectors.get(e.getKey()).stop();
			}
			injectors.clear();
		}
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
	 * 设置频率
	 * 
	 * @param f
	 *            值
	 */
	public void setFrequency(long f) {
		freq = f;
	}

	/**
	 * 得到频率值
	 * 
	 * @return 值
	 */
	public long getFrequency() {
		return freq;
	}

	/**
	 * 设置要注入的段数据
	 * 
	 * @param pid
	 *            流PID
	 * @param buf
	 *            缓冲区
	 */
	public boolean setSections(int pid, byte[] buf) {
		return false;
	}

	int makeTimeMillis(int t) {
		if (t < 0)
			throw new IllegalArgumentException();
		if ((t & 0x40) != 0)// 最小64ms为单位
			t = (t & 0x40) + 0x40;
		return t;
	}

	/**
	 * 设置调度时间
	 * 
	 * @param delay
	 *            延迟时间
	 * @param period
	 *            执行周期时长
	 */
	public void setSchedule(int delay, int period) {
		this.delay = makeTimeMillis(delay);
		this.period = makeTimeMillis(period);
	}

	/**
	 * 设置第二执行周期
	 * 
	 * @param counts
	 *            指定的执行周期计数后
	 * @param period
	 *            第二周期时长
	 */
	public void setSecondaryPeriod(int counts, int period) {
		if (counts <= 0)
			this.counts = -1;
		else {
			this.counts = counts;
			secondaryPeriod = makeTimeMillis(period);
		}
	}

	/**
	 * 开始
	 * 
	 * @return
	 */
	public boolean start() {
		return false;
	}

	/**
	 * 停止
	 */
	public void stop() {
	}

	native boolean native_open(long uuidm, long uuidl, boolean main);

	native void native_release();

	native boolean native_start();

	native void native_stop();

	native boolean native_set_sec(long freq, int pid, byte[] b);

	native boolean native_inject();
}

class MainSectionInjector extends SectionInjector {
	HashSet<SectionInjector> list = new HashSet<SectionInjector>();
	Timer timer = new Timer("[section injector]" + uuid);
	boolean started = false;

	MainSectionInjector(String uuid) {
		super(uuid);
		UUID id = UUID.fromString(uuid);
		boolean succ = native_open(id.getMostSignificantBits(),
				id.getLeastSignificantBits(), true);
		if (!succ || peer == 0)
			throw new RuntimeException();
	}

	boolean ensureStarted() {
		if (!started)
			started = native_start();
		return started;
	}

	class TaskInjector extends SectionInjector {
		Object mutex = new Object();
		TimerTask task = null;

		TaskInjector() {
			super(MainSectionInjector.this.uuid);
			this.peer = MainSectionInjector.this.peer;
			UUID id = UUID.fromString(uuid);
			boolean succ = native_open(id.getMostSignificantBits(),
					id.getLeastSignificantBits(), false);
			if (!succ || this.peer == 0)
				throw new RuntimeException();
		}

		void schedule() {
			TimerTask t = new TimerTask() {
				@Override
				public void run() {
					try {
						synchronized (mutex) {
							native_inject();
							if (counts > 0) {
								if (--counts == 0) {
									cancel();
									scheduleAfterCounts();
								}
							}
						}
					} catch (Exception e) {
						Log.e(TAG, "inject section error1:" + e);
					}
				}
			};
			timer.schedule(t, delay, period);
			task = t;
		}

		void scheduleAfterCounts() {
			TimerTask t = new TimerTask() {
				@Override
				public void run() {
					try {
						native_inject();
					} catch (Exception e) {
						Log.e(TAG, "inject section error2:" + e);
					}
				}
			};
			timer.schedule(t, secondaryPeriod, secondaryPeriod);
			task = t;
		}

		public void release() {
			synchronized (list) {
				stop();
				list.remove(this);
				native_release();
			}
		}

		void checkParam() {
			if (delay < 0 || period <= 0 || pid < 0 || pid >= 8192 || peer == 0)//可以虚拟频点注入
				throw new IllegalArgumentException("some param is invalid");
		}

		public boolean start() {
			try {
				synchronized (list) {
					if (!ensureStarted())
						return false;
					if (list.contains(this)) {
						synchronized (mutex) {
							checkParam();
							if (task == null) {
								schedule();
								return true;
							}
						}
					}
				}
			} catch (Exception e) {
				Log.e(TAG, "start failed:" + e);
			}
			return false;
		}

		public void stop() {
			try {
				synchronized (list) {
					if (list.contains(this)) {
						synchronized (mutex) {
							if (task != null) {
								task.cancel();
								started = false;
								native_stop();
							}
						}
					}
				}
			} catch (Exception e) {
				Log.e(TAG, "stop failed:" + e);
			} finally {
				task = null;
			}
		}

		public boolean setSections(int pid, byte[] buf) {
			if (pid < 0 || pid >= 8192)
				throw new IllegalArgumentException();
			if (buf.length == 0)
				throw new ArrayIndexOutOfBoundsException();
			this.pid = pid;
			return native_set_sec(freq, pid, buf);
		}
	}

	TaskInjector addTaskInjector() {
		synchronized (list) {
			TaskInjector tj = new TaskInjector();
			list.add(tj);
			return tj;
		}
	}

}
