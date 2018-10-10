package android.net.telecast;

import java.lang.ref.WeakReference;
import java.util.UUID;

import android.util.Log;

/**
 * ��״̬�۲����
 * <p>
 * ���ڻ�ȡ���Ŀɴ�״̬
 */
public class StreamObserver {
	static final String TAG = "[java]StreamObserver";
	String uuid;
	StreamStateListener ssl;
	private int peer;

	StreamObserver(String uuid, int flags) {
		this.uuid = uuid;
		UUID id = UUID.fromString(uuid);
		boolean succ = native_open(new WeakReference<StreamObserver>(this),
				id.getMostSignificantBits(), id.getLeastSignificantBits(), flags);
		if (!succ || peer == 0)
			throw new RuntimeException("create failed");
	}

	/**
	 * �ͷ���Դ
	 */
	public void release() {
		native_close();
	}

	/**
	 * �����UUID
	 * 
	 * @return ֵ
	 */
	public String getNetworkUUID() {
		return uuid;
	}

	/**
	 * ��ѯ��ǰ���ɴ�����״̬
	 */
	public void queryStreamState() {
		if (ssl != null)
			native_query(0);
	}

	/**
	 * ���ü�����
	 * 
	 * @param l
	 *            ����
	 */
	public void setStreamStateListener(StreamStateListener l) {
		ssl = l;
	}

	/**
	 * ��״̬������
	 */
	public static interface StreamStateListener {
		/**
		 * ��תΪ��ǰ̬
		 * 
		 * @param o
		 *            ����
		 * @param freq
		 *            Ƶ��
		 * @param size
		 *            ��ǰ�ɴ�ͨ������
		 * @param prevSize
		 *            ֮ǰ�ɴ�ͨ������
		 */
		void onStreamPresent(StreamObserver o, long freq, int size, int prevSize);

		/**
		 * ��תΪ���ɴ�״̬
		 * 
		 * @param o
		 *            ����
		 * @param freq
		 *            Ƶ��
		 */
		void onStreamAbsent(StreamObserver o, long freq);
	}

	static final int MSG_PRESENT = 1;
	static final int MSG_ABSENT = 2;

	void onCallback(int msg, long f, int p1, int p2) {
		StreamStateListener sl = ssl;
		switch (msg) {
		case MSG_PRESENT:
			if (sl != null)
				sl.onStreamPresent(this, f, p1, p2);
			break;
		case MSG_ABSENT:
			if (sl != null)
				sl.onStreamAbsent(this, f);
			break;
		default:
			break;
		}
	}

	@SuppressWarnings("unchecked")
	static void nativeCallback(Object o, long freq, int msg, int p1, int p2) {
		WeakReference<StreamObserver> wo = (WeakReference<StreamObserver>) o;
		if (wo == null)
			return;
		StreamObserver so = wo.get();
		try {
			if (so != null)
				so.onCallback(msg, freq, p1, p2);
		} catch (Exception e) {
			Log.e(TAG, "onCallback error:" + e);
		}
	}

	native boolean native_open(WeakReference<StreamObserver> wo, long m, long l, int flags);

	native void native_close();

	native boolean native_query(int flags);
}
