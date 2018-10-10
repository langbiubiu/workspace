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
	 * ��start֮ǰ����
	 * <p>
	 * ����б仯������Ҫ��ֹͣ ,���ֹͣ��
	 * 
	 * @param freq
	 *            Ƶ����Ϣ
	 * @param pid
	 *            PIDֵ
	 * @param b
	 *            ����������
	 * @param delay
	 *            �Ӻ�ʱ��(ms)
	 * @param period
	 *            ����ʱ��(ms)
	 * @param count
	 *            ִ�м���
	 * @param periodAfterCounts
	 *            �����������ִ������ʱ��
	 * @return �ɹ�����true��ʧ�ܷ���false
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
	 * ��start֮ǰ����
	 * <p>
	 * ����б仯������Ҫ��ֹͣ ,���ֹͣ��
	 * 
	 * @param freq
	 *            Ƶ����Ϣ
	 * @param pid
	 *            PIDֵ
	 * @param b
	 *            ����������
	 * @param delay
	 *            �Ӻ�ʱ��(ms)
	 * @param period
	 *            ����ʱ��(ms)
	 * @return �ɹ�����true��ʧ�ܷ���false
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
