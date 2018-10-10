package ipaneltv.toolkit.mediaservice;

import ipaneltv.toolkit.IPanelLog;
import ipaneltv.toolkit.JsonParcelable;
import ipaneltv.toolkit.UriToolkit;
import ipaneltv.toolkit.media.MediaSessionInterface.LiveStreamSelectorInterface;
import ipaneltv.toolkit.mediaservice.components.PlayResourceScheduler;
import ipaneltv.toolkit.mediaservice.components.PlayResourceScheduler.ResourcesState;

import java.io.FileDescriptor;

import org.json.JSONException;
import org.json.JSONObject;
import org.json.JSONTokener;

import android.net.LocalSocket;
import android.net.LocalSocketAddress;
import android.net.Uri;
import android.net.telecast.FrequencyInfo;
import android.net.telecast.SignalStatus;
import android.os.Bundle;
import android.os.HandlerThread;
import android.util.Log;

public class LiveStreamSelectorContext<T extends MediaPlaySessionService> extends
		MediaSessionContext<T> implements LiveStreamSelectorInterface {
	public static final String TAG = LiveStreamSelectorContext.class.getSimpleName();

	abstract class CB implements LiveStreamSelectorInterface.Callback {
	};

	protected ResourcesState mSelectResource;

	private Object mutex = new Object();
	private boolean bReceive = false;
	private boolean contextReady = false;
	private HandlerThread procThread = new HandlerThread("ipqamselector-proc");
//	private Handler procHandler = null;

	public LiveStreamSelectorContext(T service) {
		super(service);
	}

	@SuppressWarnings("rawtypes")
	@Override
	public void onCreate() {
		IPanelLog.i(TAG, "onCreate getSessionService = " + getSessionService());
		procThread.start();
//		procHandler = new Handler(procThread.getLooper());
		LiveNetworkApplication app = getSessionService().getApp();
		Bundle b = new Bundle();
		int pri = PlayResourceScheduler.PRIORITY_DEFAULT;
		boolean soft = false;
		if (b != null) {
			pri = b.getInt("priority", pri);
			soft = b.getBoolean("soft", false);
		}
		PlayResourceScheduler ps = app.getResourceScheduler();
		mSelectResource = ps.createStreamSelectState(false, 1, pri, soft);
		IPanelLog.i(TAG, "onCreate mSelectResource=" + mSelectResource);
	}

	@Override
	public void onClose() {
		Log.d(TAG, "onClose bReceive = "+ bReceive);
		if(bReceive){
			stopStream(null, -1);
		}
		if (mSelectResource != null) {
			mSelectResource.loosen(true);
			mSelectResource = null;
		}
		procThread.quit();
	}

	protected void loosenAll(boolean clearState) {
		if(clearState && isRelease()){
			mSelectResource.destroy();
		}else{
			mSelectResource.loosen(clearState);	
		}
	}

	protected boolean reserveAll() {
		return mSelectResource.reserve();
	}

	private boolean reserveAllSafe() {
		boolean ret = false;
		try {
			return (ret = reserveAll());
		} finally {
			if (!ret)
				loosenAll(true);
			contextReady = ret;
		}
	}

	@Override
	public boolean reserve() {
		synchronized (mutex) {
			if (contextReady ? false : reserveAllSafe()) {
				contextReady = true;
			}
		}
		return contextReady;
	}

	@Override
	public void loosen(boolean clearState) {
		synchronized (mutex) {
			if (contextReady) {
				IPanelLog.d(TAG, "onLoosen(clearState=" + clearState + ")");
				contextReady = false;
				loosenAll(clearState);
			}
		}
	}

	@Override
	public void syncSignalStatus() {
		synchronized (mutex) {
			if (mSelectResource.isReserved()) {
				SignalStatus ss = mSelectResource.getSelector().getSignalStatus();
				notifyJson(CB.__ID_onSyncSignalStatus, ss.toString());
			}
		}
	}

	@Override
	public void select(final String furl, final int flags) {
		synchronized (mutex) {
			if (mSelectResource.reserve()) {
				try {
					IPanelLog.d(TAG, "select furl:" + furl + ",flags=" + flags);
					if (furl == null || furl.equals("")) { // stop select
						mSelectResource.getSelector().select((FileDescriptor) null, 0);
						return;
					}

					if (UriToolkit.getSchemaId(furl) == UriToolkit.FREQUENCY_INFO_SCHEMA_ID) {
						FrequencyInfo fi = FrequencyInfo.fromString(furl);
						if (fi == null) {
							IPanelLog.d(TAG, "selector.fromstring failed");
							return;
						}
						if (!mSelectResource.getSelector().select(fi, flags))
							IPanelLog.d(TAG, "selector.select failed");
					} else {
						IPanelLog.d(TAG, "[select] url failed furl=" + furl);
					}
				} catch (Exception e) {
					IPanelLog.d(TAG, "[select] failed e=" + e);
					e.printStackTrace();
				}

				IPanelLog.d(TAG, "[select] end");
			}
		}
	}

	@Override
	public void startStream(String localsock, int pid, int flags) {
		synchronized (mutex) {
			if (mSelectResource.reserve()) {
				try {
					boolean succ = false;
					IPanelLog.d(TAG, "startStream localsock:" + localsock + ",pid=" + pid);
					if (UriToolkit.getSchemaId(localsock) == UriToolkit.LOCALSOCK_SCHEMA_ID) {
						LocalSocket sock = new LocalSocket();
						Uri uri = Uri.parse(localsock);
						String sockname = uri.getAuthority();
						sock.connect(new LocalSocketAddress(sockname));
						FileDescriptor fd = sock.getFileDescriptor();
						IPanelLog.d(TAG, "startStream fd=" + fd);
						IPanelLog.d(TAG, "mSelectResource.getSelector().select 111");
						succ = mSelectResource.getSelector().receive(pid, fd, flags);
						if (!succ) {
							notifyJson(CB.__ID_onStreamStart, Boolean.FALSE + "");
							return;
						}
						IPanelLog.d(TAG, "mSelectResource.getSelector().select 333");
						bReceive = true;
						notifyJson(CB.__ID_onStreamStart, Boolean.TRUE + "");
					} else {
						throw new RuntimeException("no impl");
					}
				} catch (Exception e) {
					IPanelLog.d(TAG, "startStream error=" + e);
					e.printStackTrace();
				}

				IPanelLog.d(TAG, "[startStream] end");
			}
		}
	}

	@Override
	public void stopStream(String leadUri, final int pid) {
		synchronized (mutex) {
			Log.i(TAG, "mSelectResource.reserve() = " + mSelectResource.reserve());
			if (mSelectResource.reserve()) {
				Log.i(TAG, "bReceive = " + bReceive);
				if (bReceive) {
					//procHandler.post(new Runnable() {
					//	@Override
					//	public void run() {
							Log.i(TAG, "STOP RECEIVE ");
							mSelectResource.getSelector().receive(pid, (FileDescriptor) null, 0);
							bReceive = false;
							Log.i(TAG,"STOP RECEIVE OK");
						//}
					//});
				}
				IPanelLog.d(TAG, "[stopStream] end");
			}
		}
	}

	@Override
	public String onTransmit(int code, String json, JsonParcelable p, Bundle b)
			throws JSONException {
		IPanelLog.d(TAG, "select onTransmit code=" + code + ",json=" + json);
		switch (code) {
		case __ID_select: {
			JSONObject o = (JSONObject) new JSONTokener(json).nextValue();
			select(o.getString("furi"), o.getInt("flags"));
			break;
		}
		case __ID_startStream: {
			JSONObject o = (JSONObject) new JSONTokener(json).nextValue();
			startStream(o.getString("localsock"), o.getInt("pid"), o.getInt("flags"));
			break;
		}
		case __ID_stopStream:
			JSONObject o = (JSONObject) new JSONTokener(json).nextValue();
			stopStream(o.getString("localsock"), o.getInt("pid"));
			break;
		default:
			return super.onTransmit(code, json, p, b);
		}
		return null;
	}
}
