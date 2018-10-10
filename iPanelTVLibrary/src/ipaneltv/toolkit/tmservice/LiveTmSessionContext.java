package ipaneltv.toolkit.tmservice;

import ipaneltv.toolkit.JsonParcelable;
import ipaneltv.toolkit.tm.TmSessionInterface.LiveTmSessionInterface;

import org.json.JSONException;

import android.os.Bundle;

public abstract class LiveTmSessionContext<T extends TmSessionService> extends
		TmSessionBaseContext<T> implements LiveTmSessionInterface {

	public LiveTmSessionContext(T service) {
		super(service);
	}

	@Override
	public String onTransmit(int code, String json, JsonParcelable p, Bundle b)
			throws JSONException {
		switch (code) {
		case __ID_uploadCurrentChannelInfo:
			uploadCurrentChannelInfo(json);
			break;
		case __ID_uploadChannelRatings:
			uploadChannelRatings(json);
			break;
		case __ID_uploadProgramRatings:
			uploadProgramRatings(json);
			break;
		default:
			return super.onTransmit(code, json, p, b);
		}
		return null;
	}
}
