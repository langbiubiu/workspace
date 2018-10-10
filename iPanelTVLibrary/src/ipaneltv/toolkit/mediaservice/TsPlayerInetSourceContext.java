package ipaneltv.toolkit.mediaservice;

import ipaneltv.toolkit.IPanelLog;
import ipaneltv.toolkit.JsonParcelable;
import ipaneltv.toolkit.media.MediaSessionInterface.TsPlayerInetSourceInterface;
import ipaneltv.toolkit.media.MediaSessionInterface.TsPlayerSourceBaseInterface;

import org.json.JSONException;
import org.json.JSONObject;
import org.json.JSONTokener;

import android.os.Bundle;
import android.os.ParcelFileDescriptor;

public abstract class TsPlayerInetSourceContext<T extends MediaPlaySessionService> extends
		TsPlayerSourceBaseContext<T> implements TsPlayerInetSourceInterface {
	public static final String TAG = TsPlayerInetSourceContext.class.getSimpleName();

	public TsPlayerInetSourceContext(T service) {
		super(service);
	}

	@Override
	public String onTransmit(int code, String json, JsonParcelable p, Bundle b)
			throws JSONException {
		switch (code) {
		case __ID_start: {
			JSONObject o = (JSONObject) new JSONTokener(json).nextValue();
			start(o.getString("uri"), o.getInt("type"), o.getInt("stype"), o.getInt("flags"));
			break;
		}
		case __ID_start_2:{
			JSONObject o = (JSONObject) new JSONTokener(json).nextValue();
			start((ParcelFileDescriptor) p.getParcelable("pfd"),o.getString("uri"), o.getInt("type"), o.getInt("stype"), o.getInt("flags"));
			break;
		}
		case __ID_setRate:
			setRate(Float.parseFloat(json));
			break;
		case __ID_setCache:
			setCache(Integer.parseInt(json));
			break;
		case __ID_playCache:
			playCache();
			break;
		default:
			return super.onTransmit(code, json, p, b);
		}
		return null;
	}
}

abstract class TsPlayerSourceBaseContext<T extends MediaPlaySessionService> extends
		MediaSessionContext<T> implements TsPlayerSourceBaseInterface {
	public TsPlayerSourceBaseContext(T service) {
		super(service);
	}

	@Override
	public String onTransmit(int code, String json, JsonParcelable p, Bundle b)
			throws JSONException {
		IPanelLog.d(TAG, "onTransmit code=" + code + ",json=" + json);
		switch (code) {		
		case __ID_stop:
			stop();
			break;
		case __ID_pause:
			pause();
			break;
		case __ID_resume:
			resume();
			break;
		case __ID_seek:
			seek(Long.parseLong(json));
			break;
		case __ID_seek_fd:
			seek(Long.parseLong(json),(ParcelFileDescriptor) p.getParcelable("pfd"));
			break;
		default:
			return super.onTransmit(code, json, p, b);
		}
		return null;
	}

}
