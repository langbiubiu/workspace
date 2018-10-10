package ipaneltv.toolkit.media;

import ipaneltv.toolkit.IPanelLog;
import ipaneltv.toolkit.JsonParcelable;
import ipaneltv.toolkit.media.MediaSessionInterface.LiveStreamSelectorInterface;

import org.json.JSONException;
import org.json.JSONObject;
import org.json.JSONStringer;
import org.json.JSONTokener;

import android.content.Context;
import android.os.Bundle;
import android.util.Log;

public class LiveStreamSelector extends MediaSessionClient implements LiveStreamSelectorInterface,
		LiveStreamSelectorInterface.Callback {

	final static String TAG = LiveStreamSelector.class.getSimpleName();

	public LiveStreamSelector(Context context, String serviceName) {
		super(context, serviceName, LiveStreamSelectorInterface.class.getName());
	}

	public final void syncSignalStatus() {
		channel.transmit(__ID_syncSignalStatus);
	}

	@Override
	public final void select(String furi, int flags) {
		try {
			JSONStringer s = new JSONStringer();
			s.object();
			s.key("furi").value(furi);
			s.key("flags").value(flags);
			s.endObject();
			channel.transmit(__ID_select, s.toString());
			Log.d(TAG, "select url=" + s.toString() + ",__ID_select=" + __ID_select);
		} catch (JSONException e) {
			throw new RuntimeException(e.getMessage());
		}
	}

	@Override
	public final void startStream(String localsock, int pid, int flags) {
		try {
			JSONStringer s = new JSONStringer();
			s.object();
			s.key("localsock").value(localsock);
			s.key("pid").value(pid);
			s.key("flags").value(flags);
			s.endObject();
			channel.transmit(__ID_startStream, s.toString());
			Log.d(TAG, "startStream url=" + s.toString());
		} catch (JSONException e) {
			e.printStackTrace();
			throw new RuntimeException(e.getMessage());
		}
	}

	@Override
	public final void stopStream(String localsock, int pid) {
		try {
			JSONStringer s = new JSONStringer();
			s.object();
			s.key("localsock").value(localsock);
			s.key("pid").value(pid);
			s.endObject();
			channel.transmit(__ID_stopStream, s.toString());
		} catch (JSONException e) {
			e.printStackTrace();
			throw new RuntimeException(e.getMessage());
		}

	}

	@Override
	protected final void onCallback(int code, String json, JsonParcelable p, Bundle b) {
		try {
			IPanelLog.d(TAG, "onCallback code=" + code + ",json=" + json);
			switch (code) {
			case __ID_onResponseSelect:
				onResponseSelect(Boolean.parseBoolean(json));
				break;
			case __ID_onStreamLost:
				onStreamLost();
				break;
			case __ID_onStreamResumed:
				onStreamResumed();
				break;
			case __ID_onSyncSignalStatus:
				onSyncSignalStatus(json);
				break;
			case __ID_onStreamStart: {
				JSONObject o = (JSONObject) new JSONTokener(json).nextValue();
				onStreamStart(o.getString("furi"), o.getBoolean("succ"));
				break;
			}
			case __ID_onStreamStop:
				onStreamStop(json);
				break;
			default:
				break;
			}
		} catch (Exception e) {
			IPanelLog.d(TAG, "onCallback error");
			if (p != null)
				p.clean();
		}
	}

	@Override
	public void onResponseSelect(boolean b) {
	}

	@Override
	public void onStreamLost() {
	}

	@Override
	public void onStreamResumed() {
	}

	@Override
	public void onSyncSignalStatus(String signalStatus) {
	}

	@Override
	public void onStreamStart(String puri, boolean succ) {
	}

	@Override
	public void onStreamStop(String puri) {
	}
}
