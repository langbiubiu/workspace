package ipaneltv.toolkit.tmservice;

import ipaneltv.toolkit.JsonParcelable;
import ipaneltv.toolkit.tm.TmSessionInterface.VodTmSessionInterface;

import org.json.JSONException;

import android.os.Bundle;

public abstract class VodTmSessionContext<T extends TmSessionService> extends
		TmSessionBaseContext<T> implements VodTmSessionInterface {

	public VodTmSessionContext(T service) {
		super(service);
	}

	@Override
	public String onTransmit(int code, String json, JsonParcelable p, Bundle b)
			throws JSONException {
		switch (code) {
		case __ID_uploadCurrentVodInfo:
			uploadCurrentVodInfo(json);
			break;

		default:
			return super.onTransmit(code, json, p, b);
		}
		return null;
	}
}
