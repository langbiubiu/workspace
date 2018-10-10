package ipaneltv.toolkit.tm;

import ipaneltv.toolkit.JsonParcelable;
import ipaneltv.toolkit.tm.TmSessionInterface.LiveTmSessionInterface;

import org.json.JSONException;

import android.content.Context;
import android.os.Bundle;

public class LiveTmSession extends TmSessionBase implements LiveTmSessionInterface,
		LiveTmSessionInterface.Callback {

	public LiveTmSession(Context context, String serviceName) {
		super(context, serviceName, LiveTmSessionInterface.class.getName());
	}

	@Override
	public void uploadCurrentChannelInfo(String json) {
		ch.transmit(__ID_uploadCurrentChannelInfo,json);
	}

	@Override
	public void uploadChannelRatings(String json) {
		ch.transmit(__ID_uploadChannelRatings, json);
	}

	@Override
	public void uploadProgramRatings(String json) {
		ch.transmit(__ID_uploadProgramRatings, json);
	}

	@Override
	public void onQueryCurrentChannelInfo() {
	}

	@Override
	protected void onServiceConnected() {
		super.onServiceConnected();
	}
	@Override
	protected void onServiceDisconnectted() {
		super.onServiceDisconnectted();
	}
	
	@Override
	public void onQueryChannelRatings(String json) {

	}

	@Override
	public void onQueryProgramRatings(String json) {

	}

	@Override
	protected void onCallback(int code, String json, JsonParcelable p, Bundle b)
			throws JSONException {
		switch (code) {
		case __ID_onQueryCurrentChannelInfo:
			onQueryCurrentChannelInfo();
			break;
		case __ID_onQueryChannelRatings:
			onQueryChannelRatings(json);
			break;
		case __ID_onQueryProgramRatings:
			onQueryProgramRatings(json);
			break;
		default:
			super.onCallback(code, json, p, b);
			break;
		}

	}
}
