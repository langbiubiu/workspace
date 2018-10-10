package ipaneltv.toolkit.media;

import ipaneltv.toolkit.IPanelLog;
import ipaneltv.toolkit.JsonParcelable;
import ipaneltv.toolkit.media.MediaSessionInterface.TsPlayerSourceBaseInterface;

import org.json.JSONException;
import org.json.JSONObject;
import org.json.JSONStringer;
import org.json.JSONTokener;

import android.content.Context;
import android.os.Bundle;
import android.os.ParcelFileDescriptor;

public abstract class TsLocalSockSourceBase extends MediaSessionClient implements
		TsPlayerSourceBaseInterface, TsPlayerSourceBaseInterface.Callback {

	public TsLocalSockSourceBase(Context context, String serviceName, String sessionName) {
		super(context, serviceName, sessionName);
	}

	public TsLocalSockSourceBase(Context context, String serviceName) {
		this(context, serviceName, TsPlayerSourceBaseInterface.class.getName());
	}

	@Override
	public final void pause() {
		channel.transmit(__ID_pause);
	}

	@Override
	public final void resume() {
		channel.transmit(__ID_resume);
	}

	@Override
	public final void stop() {
		channel.transmit(__ID_stop);
	}

	@Override
	public final void seek(long millis) {
		channel.transmit(__ID_seek, millis + "");
	}

	@Override
	public void seek(long millis, ParcelFileDescriptor pfd) {
		JsonParcelable p = new JsonParcelable();
		IPanelLog.d(TAG, "seek pfd " + pfd);
		p.put("pfd", pfd);
		channel.transmit(__ID_seek_fd, millis + "",p);
	}
	
	@Override
	protected void onCallback(int code, String json, JsonParcelable p, Bundle b)
			throws JSONException {
		IPanelLog.d(TAG, "onCallback> code=" + code + ",json=" + json);
		try {
			switch (code) {
			case __ID_onResponseStart:
				onResponseStart(Boolean.parseBoolean(json));
				break;
			case __ID_onResponseStop:
				onResponseStop();
				break;
			case __ID_onResponsePause:
				onResponsePause(Boolean.parseBoolean(json));
				break;
			case __ID_onResponseResume:
				onResponseResume();
				break;
			case __ID_onSourceSeek:
				onSourceSeek(Long.parseLong(json));
				break;
			case __ID_onEndOfSource:
				onEndOfSource(Float.parseFloat(json));
				break;
			case __ID_onSourceError:
				onSourceError(json);
				break;
			case __ID_onSourceMessage:
				onSourceMessage(json);
				break;
			case __ID_onSourceSinker: {
				JSONObject o = (JSONObject) new JSONTokener(json).nextValue();
				onSourceSinker(o.getString("furi"), o.getString("localsock"), o.getInt("pid"));
				break;
			}
			case __ID_onSourcePlayed: {
				JSONObject o = (JSONObject) new JSONTokener(json).nextValue();
				onSourcePlayed(o.getString("streamUri"), o.getString("programUri"));
				break;
			}
			case __ID_onSyncMediaTime:
				onSyncMediaTime(Long.parseLong(json));
				break;
			case __ID_onSourceMediaChange: {
				JSONObject o = (JSONObject) new JSONTokener(json).nextValue();
				onSourceMediaChange(o.getLong("time"), o.getLong("pts"));
				break;
			}
			default:
				break;
			}

		} catch (Exception e) {
			if (p != null)
				p.clean();
		}
	}

	@Override
	public void onResponseStart(boolean b) {
	}

	@Override
	public void onResponseStop() {
	}

	@Override
	public void onResponsePause(boolean b) {
	}

	@Override
	public void onResponseResume() {
	}

	@Override
	public void onSourceSeek(long t) {
	}

	@Override
	public void onEndOfSource(float rate) {
	}

	@Override
	public void onSourceError(String msg) {
	}

	@Override
	public void onSourceMessage(String msg) {
	}

	@Override
	public void onSourcePlayed(String streamUri, String programUri) {
	}

	@Override
	public void onSyncMediaTime(long time) {
	}

	@Override
	public void onSourceMediaChange(long time, long pts) {
	}

	@Override
	public void onSourceSinker(String furi, String localsock, int pid) {
	}
}
