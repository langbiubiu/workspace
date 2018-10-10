package ipaneltv.toolkit.mediaservice;

import ipaneltv.toolkit.JsonChannelService;
import ipaneltv.toolkit.JsonParcelable;
import ipaneltv.toolkit.media.MediaSessionInterface;

import org.json.JSONException;

import android.os.Bundle;

public abstract class MediaSessionContext<T extends MediaSessionService> extends
		JsonChannelService.Session implements MediaSessionInterface {
	public static final String TAG = MediaSessionContext.class.getSimpleName();
	private T service;
	private Bundle args;

	public MediaSessionContext(T service) {
		this.service = service;
	}

	public void setArguments(Bundle b) {
		args = b;
	}

	public Bundle getArguments() {
		return args;
	}

	public final T getSessionService() {
		return service;
	}

	@Override
	public final boolean isReserved() {
		return false;// 不会调用,客户端自己实现了
	}
	
	public boolean isRelease(){
		return false;
	}

	@Override
	public String onTransmit(int code, String json, JsonParcelable p, Bundle b)
			throws JSONException {
		switch (code) {
		case __ID_reserve:
			return reserve() ? "true" : null;
		case __ID_loosen:
			// false优先,即不明确的情况下，总是清除状态
			loosen(json == null ? true : !"false".equals(json));
			return "";
		default:
			break;
		}
		return null;
	}
}
