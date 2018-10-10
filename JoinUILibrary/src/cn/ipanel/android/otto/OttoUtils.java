package cn.ipanel.android.otto;

import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import cn.ipanel.android.Logger;

import com.squareup.otto.Bus;

public final class OttoUtils {
	private static final Bus bus = new Bus();

	public static Bus getBus() {
		return bus;
	}

	private static final Handler uiHandler = new Handler(Looper.getMainLooper()) {

		@Override
		public void handleMessage(Message msg) {
			try {
				if (msg.what == 0)
					bus.post(msg.obj);
			} catch (Exception e) {
				Logger.d("OttoUtils post message exception " + e);
				e.printStackTrace();
			}
		}

	};

	public static void postOnUiThread(Object obj) {
		uiHandler.obtainMessage(0, obj).sendToTarget();
	}
}
