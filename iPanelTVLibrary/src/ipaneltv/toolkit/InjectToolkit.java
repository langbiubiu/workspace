package ipaneltv.toolkit;

import java.util.HashMap;

import android.net.telecast.SectionInjector;
import android.net.telecast.StreamObserver;
import android.net.telecast.TransportManager;
import android.util.SparseArray;

public class InjectToolkit {
	public static InjectToolkit createInjectToolkit(TransportManager tsm,
			String uuid) {
		return new InjectToolkit(tsm, uuid);
	}

	TransportManager tsm;
	String uuid;
	HashMap<Long, StreamInjector> streams = new HashMap<Long, StreamInjector>();
	boolean running = false;
	StreamObserver oberver;

	StreamObserver.StreamStateListener streamListener = new StreamObserver.StreamStateListener() {

		@Override
		public void onStreamPresent(StreamObserver so, long freq, int ns, int ps) {
			synchronized (streams) {
				if (running) {
					StreamInjector si = streams.get(freq);
					si.ensureStart();
				}
			}
		}

		@Override
		public void onStreamAbsent(StreamObserver so, long freq) {
			synchronized (streams) {
				if (running) {
					StreamInjector si = streams.get(freq);
					si.ensureStop();
				}
			}
		}
	};

	InjectToolkit(TransportManager tsm, String uuid) {
		this.tsm = tsm;
		this.uuid = uuid;
	}

	/**
	 * 在start之前调用
	 * <p>
	 * 如果有变化，则需要先停止 ,如果停止后
	 * 
	 * @param freq
	 *            频点信息
	 * @param pid
	 *            PID值
	 * @param b
	 *            缓冲区数据
	 * @param delay
	 *            延后时间(ms)
	 * @param period
	 *            周期时间(ms)
	 * @param count
	 *            执行计数
	 * @param periodAfterCounts
	 *            计数次数后的执行周期时间
	 * @return 成功返回true，失败返回false
	 */
	public boolean setSections(long freq, int pid, byte[] b, int delay, int period, int count,
			int periodAfterCounts) {
		if (freq == 0 || pid < 0 || pid > 8192 || b.length < 8 || delay < 0 || period <= 0)
			throw new IllegalArgumentException();
		synchronized (streams) {
			if (running)
				throw new RuntimeException("you must invoke this function when not running");
			StreamInjector si = streams.get(freq);
			if (si == null) {
				si = new StreamInjector(freq);
				streams.put(freq, si);
			}
			return si.setSections(pid, b, delay, period, count, periodAfterCounts);
		}
	}

	/**
	 * 在start之前调用
	 * <p>
	 * 如果有变化，则需要先停止 ,如果停止后
	 * 
	 * @param freq
	 *            频点信息
	 * @param pid
	 *            PID值
	 * @param b
	 *            缓冲区数据
	 * @param delay
	 *            延后时间(ms)
	 * @param period
	 *            周期时间(ms)
	 * @return 成功返回true，失败返回false
	 */
	public boolean setSections(long freq, int pid, byte[] b, int delay, int period) {
		return setSections(freq, pid, b, delay, period, 0, 0);
	}

	public boolean start() {
		synchronized (streams) {
			if (!running) {
				if ((oberver = tsm.createObserver(uuid)) != null) {
					oberver.setStreamStateListener(streamListener);
					oberver.queryStreamState();
					running = true;
					return true;
				}
				return false;
			}
			throw new IllegalStateException();
		}
	}

	public void stop() {
		synchronized (streams) {
			if (running) {
				running = false;
				oberver.release();
				oberver = null;
				for (StreamInjector stramIj : streams.values()) {
					stramIj.ensureStop();
				}
			}
		}
	}

	class StreamInjector {
		SparseArray<SectionInjector> injectors = new SparseArray<SectionInjector>();
		boolean started = false;
		long freq;

		public StreamInjector(long f) {
			freq = f;
		}

		void ensureStart() {
			synchronized (injectors) {
				if (!started) {
					for (int i = 0; i < injectors.size(); i++) {
						injectors.valueAt(i).start();
					}
				}
			}
		}

		void ensureStop() {
			synchronized (injectors) {
				if (started) {
					started = false;
					for (int i = 0; i < injectors.size(); i++)
						injectors.valueAt(i).stop();
				}
			}
		}

		boolean setSections(int pid, byte[] b, int delay, int period, int count, int period2) {
			synchronized (injectors) {
				SectionInjector si = injectors.get(pid);
				if (si == null) {
					si = tsm.createInjector(uuid);
					injectors.put(pid, si);
				}
				if (si != null) {
					si.setFrequency(freq);
					si.setSchedule(delay, period);
					if (count > 0)
						si.setSecondaryPeriod(count, period2);
					return si.setSections(pid, b);
				}
			}
			return false;
		}
	}
}
