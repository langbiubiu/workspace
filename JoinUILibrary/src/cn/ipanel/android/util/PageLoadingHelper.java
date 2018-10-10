package cn.ipanel.android.util;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicInteger;

import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.util.Log;
import android.util.SparseArray;

/**
 * Simple helper utility to fetch data by page
 * 
 * @author Zexu
 *
 * @param <T>
 */
public abstract class PageLoadingHelper<T> {
	static final String TAG = PageLoadingHelper.class.getSimpleName();
	
	SparseArray<List<T>> itemsArray = new SparseArray<List<T>>();

	ExecutorService mPool = Executors.newSingleThreadExecutor();

	final Object lock = new Object();

	static final int MSG_DATA_UPDATE = 1;
	
	AtomicInteger taskVersion = new AtomicInteger();
	
	Handler uiHandler = new Handler(Looper.getMainLooper()) {

		@Override
		public void handleMessage(Message msg) {
			switch (msg.what) {
			case MSG_DATA_UPDATE:
				onDataUpdate(msg.arg1, itemsArray.get(msg.arg1));
				break;
			}
		}

	};

	public List<T> getAllItems() {
		List<T> list = new ArrayList<T>();
		for (int i = 0; i < itemsArray.size(); i++) {
			list.addAll(itemsArray.valueAt(i));
		}
		return list;
	}

	@Override
	protected void finalize() throws Throwable {
		if (mPool != null)
			mPool.shutdownNow();
		super.finalize();
	}

	public void requestPage(int page, int count) {
		for (int i = 0; i < count; i++) {
			requestPage(page + i);
		}
	}

	public void requestPage(int page) {
		synchronized (lock) {
			if (itemsArray.get(page) != null)
				return;
			itemsArray.put(page, new ArrayList<T>());
			Log.d(TAG, "requestPage taskVersion="+taskVersion.get());
			mPool.submit(new RequestTask(page, taskVersion.get()));
		}
	}
	
	public void reset(){
		synchronized (lock) {
			taskVersion.getAndIncrement();
			mPool.shutdownNow();
			itemsArray.clear();
			mPool = Executors.newSingleThreadExecutor();
			Log.d(TAG, "reset taskVersion="+taskVersion.get());
		}
	}

	/**
	 * This method runs in background thread to fetch data for a specific page
	 * 
	 * @param pa
	 * @return
	 */
	public abstract List<T> syncGetPageData(int page) throws Exception;

	/**
	 * This method runs in UI¡¡thread to insert data into specific adapter.
	 * 
	 * @param page
	 * @param list
	 */
	public abstract void onDataUpdate(int page, List<T> list);

	private class RequestTask implements Runnable {
		int page;
		int version;

		public RequestTask(int page, int version) {
			this.page = page;
			this.version = version;
		}

		@Override
		public void run() {
			try {
				List<T> items = syncGetPageData(page);
				Log.d(TAG, "after getPageData, page = "+page+", version = "+version+", taskVersion="+taskVersion.get());
				if (items != null && version == taskVersion.get()) {
					itemsArray.put(page, items);
					uiHandler.obtainMessage(MSG_DATA_UPDATE, page, 0).sendToTarget();
				}
			} catch (Exception e) {
				e.printStackTrace();
			}

		}

	}
}
