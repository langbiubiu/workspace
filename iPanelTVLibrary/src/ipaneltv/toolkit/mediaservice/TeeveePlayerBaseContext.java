package ipaneltv.toolkit.mediaservice;

import ipaneltv.toolkit.IPanelLog;
import ipaneltv.toolkit.JsonParcelable;
import ipaneltv.toolkit.media.MediaSessionInterface.TeeveePlayerBaseInterface;

import org.json.JSONException;
import org.json.JSONObject;
import org.json.JSONTokener;

import android.os.Bundle;

abstract class TeeveePlayerBaseContext<T extends MediaPlaySessionService> extends
		MediaSessionContext<T> implements TeeveePlayerBaseInterface {
	public static final String TAG = TeeveePlayerBaseContext.class.getSimpleName();

	public TeeveePlayerBaseContext(T service) {
		super(service);
	}

	@Override
	public String onTransmit(int code, String json, JsonParcelable p, Bundle b)
			throws JSONException {
		IPanelLog.d(TAG, "onTransmit code=" + code + ",json=" + json);
		switch (code) {
		case __ID_stop:
			stop(Integer.parseInt(json));
			break;
		case __ID_setVolume:
			setVolume(Float.parseFloat(json));
			break;
		case __ID_setDisplay: {
			JSONObject o = (JSONObject) new JSONTokener(json).nextValue();
			setDisplay(o.getInt("x"), o.getInt("y"), o.getInt("w"), o.getInt("h"));
			break;
		}
		case __ID_setProgramFlags:
			setProgramFlags(Integer.parseInt(json));
			break;
		case __ID_setTeeveeWidget:
			setTeeveeWidget(Integer.parseInt(json));
			break;
		case __ID_checkTeeveeWidget:
			checkTeeveeWidget(Integer.parseInt(json));
			break;
		case __ID_syncMediaTime:
			syncMediaTime();
			break;
		default:
			return super.onTransmit(code, json, p, b);
		}
		return null;
	}

}
