package ipaneltv.toolkit.dvb;

import ipaneltv.toolkit.Natives;

import java.lang.ref.WeakReference;

import android.util.Log;

public class HomedHttp {
	private final String TAG = "[java]HomedHttp";
	private int peer = 0;
	private boolean released = false;
	private Object mutex = new Object();

	public HomedHttp() {
		int code = ncreate(new WeakReference<HomedHttp>(this), 0);
		Log.d(TAG, "HomedHttp code = "+ code +";peer = "+ peer);
		if (code != 0 || peer == 0) {
			throw new RuntimeException();
		}
	}

	public int make(String uri, int flags) {
		synchronized (mutex) {
			checkPeer();
			return nmake(uri, flags);
		}
	}

	public int interrupt(int flags) {
		synchronized (mutex) {
			checkPeer();
			return interrupt(flags);
		}
	}

	/**
	 * 释放资源
	 */
	public void release() {
		synchronized (mutex) {
			if (!released) {
				Log.d(TAG, "release StreamSelector!");
				released = true;
				nrelease();
			}
		}
	}

	/**
	 * 对象是否已释放
	 * 
	 * @return 是返回true,否则返回false
	 */
	public boolean isReleased() {
		return released;
	}

	@Override
	protected void finalize() throws Throwable {
		try {
			if (peer != 0)
				release();
			peer = 0;
		} catch (Throwable e) {
			e.printStackTrace();
		}
		super.finalize();
	}

	void checkPeer() {
		if (peer == 0)
			throw new IllegalStateException("not reserve!");
	}

	native int ncreate(WeakReference<HomedHttp> owner, int flags);

	native int nmake(String uri, int flags);

	native int ninterrupt(int flags);

	native void nrelease();

	@SuppressWarnings("unchecked")
	static void native_callback(Object o, int code, String msg) {
		WeakReference<HomedHttp> wo;
		HomedHttp ts;
		if (o == null)
			return;
		try {
			wo = (WeakReference<HomedHttp>) o;
			ts = wo.get();
			if (ts == null)
				return;
			// TODO
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	static {
		init();
	}

	@SuppressWarnings("deprecation")
	static void init() {
		Natives.ensure();
	}
}
