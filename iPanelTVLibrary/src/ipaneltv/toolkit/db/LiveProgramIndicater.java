package ipaneltv.toolkit.db;

import ipaneltv.toolkit.IPanelLog;
import ipaneltv.toolkit.db.DatabaseCursorHandler.EcmCursorHandler;
import ipaneltv.toolkit.db.DatabaseCursorHandler.FrequencyCursorHandler;
import ipaneltv.toolkit.db.DatabaseCursorHandler.StreamCursorHandler;
import ipaneltv.toolkit.db.DatabaseObjectification.ChannelKey;
import ipaneltv.toolkit.db.DatabaseObjectification.Ecm;
import ipaneltv.toolkit.db.DatabaseObjectification.Frequency;
import ipaneltv.toolkit.db.DatabaseObjectification.Stream;

import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map.Entry;
import java.util.Set;

import android.content.Context;
import android.net.Uri;
import android.net.telecast.NetworkDatabase;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.Looper;
import android.os.Message;
import android.util.Log;
import android.util.SparseArray;

public abstract class LiveProgramIndicater {
	static final String TAG = LiveProgramIndicater.class.getSimpleName();
	static final int MASK_FREQ = 0x01, MASK_STREAM = 0x02, MASK_ECM = 0x04;
	private Context context;
	private boolean preloadShotted = false;
	private HandlerThread freqThread = new HandlerThread("navi-freq");
	private HandlerThread streamThread = new HandlerThread("navi-stream");
	private HandlerThread ecmThread = new HandlerThread("navi-ecm");
	private HandlerThread callbackThread = new HandlerThread("navi-proc");
	private Handler freqHandler, streamHandler, ecmHandler, callbackHandler;
	private Uri uri, freqsUri, streamsUri, ecmsUri;
	private final Object loadMutex = new Object();
	private int loaddedCount = 0;
	private SparseArray<Frequency> loaddedFreqs;
	private HashMap<ChannelKey, List<Stream>> loaddedStreams;
	private HashMap<ChannelKey, List<Ecm>> loaddedEcms;
	private boolean loaddedFlag = false;
	private int updateVersion = 0, updatedMask = 0;

	// private Object updateMutex = new Object();

	public LiveProgramIndicater(Context context, Uri uri) {
		this.context = context;
		this.uri = uri;
		callbackThread.start();
		callbackHandler = new Handler(callbackThread.getLooper(), callbackHandleMessage);
	}

	protected void close() {
		Looper lp;
		callbackThread.getLooper().quit();
		if ((lp = freqThread.getLooper()) != null)
			lp.quit();
		if ((lp = streamThread.getLooper()) != null)
			lp.quit();
		if ((lp = ecmThread.getLooper()) != null)
			lp.quit();
	}

	public void postUpdateInfo(long[] freqs) {
		if (freqs == null) {
			postLoadFreqs(0);
			postLoadStreams(0);
			postLoadEcms(0);
		} else {
			for (int i = 0; i < freqs.length; i++) {
				if (freqs[i] != 0) {
					postLoadFreqs(freqs[i]);
					postLoadStreams(freqs[i]);
					postLoadEcms(freqs[i]);
				}
			}
		}
	}

	private Listener lis, loaddedNotified = null;

	public void setListener(Listener l) {
		lis = l;
		loaddedNotified = null;
	}

	public boolean isLoaded(){
		synchronized (loadMutex) {
			return loaddedFlag;
		}
	}
	
	public static interface Listener {
		void onLoadFinished();

		void onFrequencyUpdated(SparseArray<Frequency> freqs);

		void onStreamUpdated(HashMap<ChannelKey, List<Stream>> streams);

		void onEcmUpdated(HashMap<ChannelKey, List<Ecm>> ecms);
	}

	public void queryState() {
		callbackHandler.sendEmptyMessage(CB_ON_QUERIED);
	}

	protected HashMap<ChannelKey, List<Ecm>> getLoaddedEcms() {
		return loaddedEcms;
	}

	protected HashMap<ChannelKey, List<Stream>> getLoaddedStreams() {
		return loaddedStreams;
	}

	protected SparseArray<Frequency> getLoaddedFrequencies() {
		return loaddedFreqs;
	}

	protected Object getLockMutex() {
		return loadMutex;
	}

	protected abstract FrequencyCursorHandler getLoadFrequencyCursorHandler(Context context,
			Uri uri, Handler handler);

	protected abstract StreamCursorHandler getLoadStreamCursorHandler(Context context, Uri uri,
			Handler handler);

	protected abstract EcmCursorHandler getLoadEcmCursorHandler(Context context, Uri uri,
			Handler handler);

	protected abstract FrequencyCursorHandler getUpdateFrequencyCursorHandler(Context context,
			Uri uri, long freq, Handler handler);

	protected abstract StreamCursorHandler getUpdateStreamCursorHandler(Context context, Uri uri,
			long freq, Handler handler);

	protected abstract EcmCursorHandler getUpdateEcmCursorHandler(Context context, Uri uri,
			long freq, Handler handler);

	protected void onLoadFinished() {
		callbackHandler.sendEmptyMessage(CB_ON_LOADDED);
	}

	protected void onFrequenciesUpdated() {
		notifyUpdated(MASK_FREQ);
	}

	protected void onStreamsUpdated() {
		notifyUpdated(MASK_STREAM);
	}

	protected void onEcmsUpdated() {
		notifyUpdated(MASK_ECM);
	}

	private void notifyUpdated(int mask) {
		synchronized (loadMutex) {
			int v = ++updateVersion;
			Message msg = callbackHandler.obtainMessage(CB_ON_UPDATED, v, mask);
			callbackHandler.sendMessageDelayed(msg, 300);
		}
	}
	private void procQueryEnd(int mask) {
		if (++loaddedCount == 3)
			onLoadFinished();
		else if (loaddedCount > 3) {
			switch (mask) {
			case MASK_FREQ:
				onFrequenciesUpdated();
				break;
			case MASK_STREAM:
				onStreamsUpdated();
				break;
			case MASK_ECM:
				onEcmsUpdated();
				break;
			
			}
		}
	}
	public final synchronized void preload() {
		if (preloadShotted)
			return;
		preloadShotted = true;
		freqThread.start();
		freqHandler = new Handler(freqThread.getLooper());
		streamThread.start();
		streamHandler = new Handler(streamThread.getLooper());
		ecmThread.start();
		ecmHandler = new Handler(ecmThread.getLooper());
		freqsUri = Uri.withAppendedPath(uri, NetworkDatabase.Frequencies.TABLE_NAME);
		streamsUri = Uri.withAppendedPath(uri, NetworkDatabase.Streams.TABLE_NAME);
		ecmsUri = Uri.withAppendedPath(uri, NetworkDatabase.Ecms.TABLE_NAME);
		postLoadFreqs(0);
		postLoadStreams(0);
		postLoadEcms(0);
	}

	private void postLoadFreqs(final long freq) {
		FrequencyCursorHandler h = null;
		if (freq == 0) {
			h = getLoadFrequencyCursorHandler(context, freqsUri, freqHandler);
		} else {
			h = getUpdateFrequencyCursorHandler(context, freqsUri, freq, freqHandler);
		}
		final FrequencyCursorHandler lf = h;

		lf.setQueryHandler(new QueryHandler() {
			@Override
			public void onQueryEnd() {
				synchronized (loadMutex) {
					IPanelLog.d(TAG, "postLoadFreqs onCursorEnd loaddedCount = "+ loaddedCount+";freq = "+ freq);
					if(loaddedFreqs != null && freq!=0){
						int key = Frequency.getSparseKey(freq);
						Log.d(TAG, lf.freqs.get(key).getTuneParams());
						loaddedFreqs.append(key,lf.freqs.get(key));
					}else{
						loaddedFreqs = lf.freqs;
					}
					procQueryEnd(MASK_FREQ);
				}
			}
		});
		lf.postQuery();
	}

	private void postLoadStreams(final long freq) {
		StreamCursorHandler h = null;
		if (freq == 0) {
			h = getLoadStreamCursorHandler(context, streamsUri, streamHandler);
		} else {
			h = getUpdateStreamCursorHandler(context, streamsUri, freq, streamHandler);
		}
		final StreamCursorHandler ls = h;
		ls.setQueryHandler(new QueryHandler() {
			@Override
			public void onQueryEnd() {
				synchronized (loadMutex) {
					IPanelLog.d(TAG, "postLoadStreams onCursorEnd loaddedCount = "+ loaddedCount+";freq = "+ freq);
					if(loaddedStreams != null && freq!= 0){
						loaddedStreams.putAll(ls.streams);
					}else{
						loaddedStreams = ls.streams;
					}
					Iterator iterator = loaddedStreams.keySet().iterator();
					while(iterator.hasNext()) {
					 List<Stream> list = loaddedStreams.get(iterator.next());
					  for(int i =0;i<list.size();i++){
						  Log.i(TAG,"typeName = "+list.get(i).typeName+" pid= "+list.get(i).pid+" type = "+list.get(i).type);
					  }
					}
					procQueryEnd(MASK_STREAM);
				}
			}
		});
		ls.postQuery();
	}

	private void postLoadEcms(final long freq) {
		EcmCursorHandler h = null;
		if (freq == 0) {
			h = getLoadEcmCursorHandler(context, ecmsUri, ecmHandler);
		} else {
			h = getUpdateEcmCursorHandler(context, ecmsUri, freq, ecmHandler);
		}
		final EcmCursorHandler le = h;
		le.setQueryHandler(new QueryHandler() {
			@Override
			public void onQueryEnd() {
				synchronized (loadMutex) {
					IPanelLog.d(TAG, "postLoadEcms onCursorEnd loaddedCount = "+ loaddedCount+";freq = "+ freq);
					if(loaddedEcms != null && freq != 0){
						loaddedEcms.putAll(le.ecms);
					}else{
						loaddedEcms = le.ecms;
					}
					procQueryEnd(MASK_ECM);
				}
			}
		});

		le.postQuery();
	}

	private void procUpdateSync(int mask) {
		Listener l = lis;
		if (l != null) {
			if ((mask & MASK_FREQ) != 0) {
				IPanelLog.d(TAG, "procUpdateSync loaddedFreqs.size()= "+ loaddedFreqs.size());
				l.onFrequencyUpdated(loaddedFreqs);
			}
			if ((mask & MASK_ECM) != 0) {
				l.onEcmUpdated(loaddedEcms);
			}
			if ((mask & MASK_STREAM) != 0) {
				l.onStreamUpdated(loaddedStreams);
			}
		}
	}

	private void procLoadFinished() {
		Listener l = lis;
		IPanelLog.d(TAG, "procLoadFinished l = "+ l);
		if (l != null) {
			synchronized (loadMutex) {
				loaddedNotified = l;
				procUpdateSync(MASK_FREQ | MASK_ECM | MASK_STREAM);
				l.onLoadFinished();
			}
		}
	}

	private Handler.Callback callbackHandleMessage = new Handler.Callback() {

		@Override
		public boolean handleMessage(Message msg) {
			try {
				synchronized (loadMutex) {
					switch (msg.what) {
					case CB_ON_LOADDED:
						loaddedFlag = true;
						procLoadFinished();
						break;
					case CB_ON_UPDATED: {
						int mask = 0;
						updatedMask |= msg.arg2;
						if (msg.arg1 == updateVersion) {
							mask = updatedMask;// 避免重复的过多发送消息
							updatedMask = 0;
						}
						if (mask != 0)
							procUpdateSync(mask);
						break;
					}
					case CB_ON_QUERIED:
						if (loaddedFlag && loaddedNotified == null)
							procLoadFinished();
						break;
					}
				}
			} catch (Exception e) {
				e.printStackTrace();
			}
			return true;
		}
	};

	static final int CB_ON_LOADDED = 1;
	static final int CB_ON_UPDATED = 2;
	static final int CB_ON_QUERIED = 3;
}
