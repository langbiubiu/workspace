package ipaneltv.toolkit.ratingscollect;

import ipaneltv.toolkit.IPanelLog;
import ipaneltv.toolkit.ratingscollect.RatingsCollector2Event.BootEpgAdvEvent;

import java.io.FileOutputStream;
import java.io.IOException;
import java.util.LinkedList;

import android.app.Fragment;
import android.content.Context;
import android.os.Bundle;
import android.os.Handler;
import android.os.HandlerThread;

public class BootEpgAdvCollectFragment extends Fragment {
	public static final String TAG = BootEpgAdvCollectFragment.class.getSimpleName();
	HandlerThread handlerThread = null;
	Handler handler, uiHandler;
	long lastUpdateTime;
	Context context;
	public  static final  String CRLF = "\r\n";
	private static final int UPDATE_PERIOD = 10000; // ms
	private static final String UPDATE_FILE_NAME = "boot_epg_adv.txt";
	LinkedList<Object> eventQueue = new LinkedList<Object>();

	public BootEpgAdvCollectFragment() {

	}

	@Override
	public void onCreate(Bundle savedInstanceState) {
		IPanelLog.i(TAG, "onCreate.....");
		super.onCreate(savedInstanceState);
		context = getActivity().getApplicationContext();
	}

	@Override
	public void onStop() {
		IPanelLog.i(TAG, "onStop.....");
		if (handlerThread != null) {
			handlerThread.getLooper().quit();
			handlerThread = null;
			handler = null;
		}
		super.onStop();
	}

	@Override
	public void onResume() {
		IPanelLog.i(TAG, "onResume.....");
		super.onResume();
		handlerThread = new HandlerThread(TAG);
		handlerThread.start();
		handler = new Handler(handlerThread.getLooper());
		uiHandler = new Handler();
		lastUpdateTime = System.currentTimeMillis();
	}

	@Override
	public void onDestroy() {
		IPanelLog.i(TAG, "onDestroy.....");
		if (handlerThread != null) {
			handlerThread.getLooper().quit();
			handlerThread = null;
			handler = null;
		}
		super.onDestroy();
	}

	void postProc(Runnable r) {
		if (handler != null)
			handler.post(r);
	}

	void postUi(Runnable r) {
		if (uiHandler != null)
			uiHandler.post(r);
	}

	/* wangxu start */
	public synchronized boolean updateBootEpgAdvEvent(int platform, String caID, int key,
			int serviceId, String adID, long startTime, long endTime, long time, String event_Type,
			int groupID) {
		IPanelLog.i(TAG, "updateEpgAdvEvent........");
		final BootEpgAdvEvent e = new BootEpgAdvEvent(platform, caID, key, serviceId, adID,
				startTime, endTime, time, event_Type, groupID);
		postProc(new Runnable() {
			@Override
			public void run() {
				long ct = System.currentTimeMillis();
				eventQueue.add(e);
				if ((ct - lastUpdateTime) > UPDATE_PERIOD && eventQueue.size() > 0) {
					wirteToFile(eventQueue);
					lastUpdateTime = System.currentTimeMillis();
				}
			}
		});
		return true;
	}

	/**
	 * 重载方法
	 * 
	 */
	public synchronized boolean updateBootEpgAdvEvent(int key, int serviceId, String adID,
			long startTime, long endTime, String event_Type, int groupID) {
		IPanelLog.i(TAG, "updateEpgAdvEvent........2");

		// TODO platform 为-1 则为无效值
		String caID = "%caID%";
		int platform = -1;
		long time = System.currentTimeMillis();// 采集时间

		return updateBootEpgAdvEvent(platform, caID, key, serviceId, adID, startTime, endTime,
				time, event_Type, groupID);
	}

	/* wangxu end */
	void wirteToFile(LinkedList<Object> queue) {
		int size = queue.size();
		IPanelLog.i(TAG, "wirteToFile...size:" + size);
		FileOutputStream fos = null;
		try {
			fos = context.openFileOutput(UPDATE_FILE_NAME, Context.MODE_MULTI_PROCESS);
			for (int i = 0; i < size; i++) {
				fos.write(queue.poll().toString().getBytes());
				fos.write(CRLF.getBytes());
			}
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			if (fos != null) {
				try {

					fos.flush();
					fos.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		}
		IPanelLog.i(TAG, "wirteToFile...over:" + queue.size());
	}

}