package cn.ipanel.android.util;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import android.os.Handler;
import android.os.Looper;
import android.os.SystemClock;

public abstract class AbsDataCache<K, V> {
	protected Map<K, V> mDataCache = new HashMap<K, V>();
	protected Map<K, Long> mTimeCache = new HashMap<K, Long>();
	
	protected Set<K> mRunningTask = new HashSet<K>();

	protected long mDataValidTime = 15 * 60 * 1000;

	protected final Object mMutex = new Object();

	protected ExecutorService mPool = Executors.newCachedThreadPool();

	protected Handler uiHandler = new Handler(Looper.getMainLooper());

	@Override
	protected void finalize() throws Throwable {
		mPool.shutdownNow();
		uiHandler.removeCallbacksAndMessages(null);
		super.finalize();
	}

	public void setDataValidTime(long time) {
		this.mDataValidTime = time;
	}
	
	public void clearCache(){
		synchronized (mMutex) {
			mDataCache.clear();
			mTimeCache.clear();
			mPool.shutdownNow();
			mRunningTask.clear();
			mPool = Executors.newCachedThreadPool();
		}
	}
	
	public void removeCacheData(K key){
		synchronized (mMutex) {
			mDataCache.remove(key);
			mTimeCache.remove(key);
		}
	}

	public V getData(K key) {
		V data = mDataCache.get(key);
		if (data != null) {
			Long time = mTimeCache.get(key);
			if (time != null && SystemClock.elapsedRealtime() - time > mDataValidTime) {
				requestData(key);
			}
		} else {
			requestData(key);
		}
		return data;
	}

	protected void requestData(K key) {
		synchronized (mMutex) {
			if(mRunningTask.contains(key))
				return;
			
			mRunningTask.add(key);
			mPool.submit(new RequestTask(key));
			
		}

	}

	protected void notifyDataReady(final K key, final V data) {
		synchronized (mMutex) {
			mDataCache.put(key, data);
			mTimeCache.put(key, SystemClock.elapsedRealtime());
		}
		uiHandler.post(new Runnable() {

			@Override
			public void run() {
				try {
					onDataReady(key, data);
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		});
	}

	public abstract void onDataReady(K key, V data);

	public abstract V loadDataInBackground(K key) throws Exception;

	class RequestTask implements Runnable {
		K key;

		public RequestTask(K key) {
			this.key = key;
		}

		@Override
		public void run() {
			try {
				final V v = loadDataInBackground(key);
				if (v != null)
					notifyDataReady(key, v);
			} catch (Exception e) {
				e.printStackTrace();
			} finally {
				synchronized (mMutex) {
					mRunningTask.remove(key);
				}
			}

		}
	}
}
