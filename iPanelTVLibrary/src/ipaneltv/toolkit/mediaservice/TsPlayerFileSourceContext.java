package ipaneltv.toolkit.mediaservice;

import ipaneltv.toolkit.JsonParcelable;
import ipaneltv.toolkit.media.MediaSessionInterface.TsPlayerFileSourceInterface;

import org.json.JSONException;
import org.json.JSONObject;
import org.json.JSONTokener;

import android.os.Bundle;

public abstract class TsPlayerFileSourceContext<T extends MediaPlaySessionService> extends
		TsPlayerSourceBaseContext<T> implements TsPlayerFileSourceInterface {
	public static final String TAG = TsPlayerFileSourceContext.class.getSimpleName();

	public TsPlayerFileSourceContext(T service) {
		super(service);
	}

	@Override
	public String onTransmit(int code, String json, JsonParcelable p, Bundle b)
			throws JSONException {
		switch (code) {
		case __ID_start: {
			JSONObject o = (JSONObject) new JSONTokener(json).nextValue();
			start(o.getString("uri"), o.getInt("flags"));
			break;
		}
		default:
			return super.onTransmit(code, json, p, b);
		}
		return null;
	}

}
