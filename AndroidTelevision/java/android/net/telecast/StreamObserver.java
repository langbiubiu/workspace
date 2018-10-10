package android.net.telecast;

import java.lang.ref.WeakReference;
import java.util.UUID;

import android.util.Log;

/**
 * 流状态观察对象
 * <p>
 * 用于获取流的可达状态
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
	 * 释放资源
	 */
	public void release() {
		native_close();
	}

	/**
	 * 网络的UUID
	 * 
	 * @return 值
	 */
	public String getNetworkUUID() {
		return uuid;
	}

	/**
	 * 查询当前流可达流的状态
	 */
	public void queryStreamState() {
		if (ssl != null)
			native_query(0);
	}

	/**
	 * 设置监听器
	 * 
	 * @param l
	 *            对象
	 */
	public void setStreamStateListener(StreamStateListener l) {
		ssl = l;
	}

	/**
	 * 流状态监听器
	 */
	public static interface StreamStateListener {
		/**
		 * 流转为当前态
		 * 
		 * @param o
		 *            对象
		 * @param freq
		 *            频率
		 * @param size
		 *            当前可达通道数量
		 * @param prevSize
		 *            之前可达通道数量
		 */
		void onStreamPresent(StreamObserver o, long freq, int size, int prevSize);

		/**
		 * 流转为不可达状态
		 * 
		 * @param o
		 *            对象
		 * @param freq
		 *            频率
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
