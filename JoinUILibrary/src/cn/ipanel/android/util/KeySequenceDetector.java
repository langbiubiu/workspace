package cn.ipanel.android.util;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import android.os.Handler;
import android.os.Looper;
import android.os.Message;

/**
 * Helper class for detecting special key sequence, e.g. ¡ü¡ü¡ý¡ý¡û¡û¡ú¡úABAB
 * 
 * @author Zexu
 *
 */
public class KeySequenceDetector {
	public interface OnKeySequenceListener {
		public void onKeySequence(String name);
	}

	int maxLen;

	private List<Integer> buffer;

	private Map<String, int[]> candidates = new HashMap<String, int[]>();

	OnKeySequenceListener mListener;

	private int MSG_CLEAR_BUFFER = 1;

	private Handler mHandler = new Handler(Looper.getMainLooper()) {

		@Override
		public void handleMessage(Message msg) {
			if (msg.what == MSG_CLEAR_BUFFER) {
				synchronized (buffer) {
					buffer.clear();
				}
			}
		}

	};

	private int autoClearDelay = -1;

	public KeySequenceDetector(int maxLen) {
		this(maxLen, null);
	}

	public KeySequenceDetector(int maxLen, OnKeySequenceListener listener) {
		this.maxLen = maxLen;
		this.mListener = listener;
		buffer = new ArrayList<Integer>(maxLen);
	}

	public void setOnKeySequenceListener(OnKeySequenceListener l) {
		this.mListener = l;
	}

	/**
	 * 
	 * @param key
	 *            name of the sequence
	 * @param sequence
	 *            key code sequence array
	 */
	public void addPattern(String key, int[] sequence) {
		synchronized (candidates) {
			candidates.put(key, sequence);
		}
	}

	public void removePattern(String key) {
		synchronized (candidates) {
			candidates.remove(key);
		}
	}

	public void setAutoClearDelay(int miliseconds) {
		this.autoClearDelay = miliseconds;
	}

	public void onKeyDown(int keyCode) {
		mHandler.removeMessages(MSG_CLEAR_BUFFER);
		if (autoClearDelay > 0)
			mHandler.sendEmptyMessageDelayed(MSG_CLEAR_BUFFER, autoClearDelay);
		if (buffer.size() >= maxLen)
			buffer.remove(0);
		buffer.add(keyCode);
		checkMatching();
	}

	private void checkMatching() {
		synchronized (candidates) {
			for (Entry<String, int[]> entry : candidates.entrySet()) {
				if (mListener != null && match(entry.getValue())) {
					mListener.onKeySequence(entry.getKey());
				}
			}
		}

	}

	private boolean match(int[] value) {
		if (value != null && buffer.size() >= value.length && value.length > 0) {
			for (int i = 0; i < value.length; i++) {
				if (value[value.length - 1 - i] != buffer.get(buffer.size() - 1 - i))
					return false;
			}
			return true;
		}
		return false;
	}
}
