package ipaneltv.toolkit.tm;

import ipaneltv.toolkit.JsonParcelable;

import org.json.JSONException;

import android.content.Context;
import android.os.Bundle;

public class VodTmSession extends TmSessionBase implements
		TmSessionInterface.VodTmSessionInterface, TmSessionInterface.VodTmSessionInterface.Callback {

	public VodTmSession(Context context, String serviceName) {
		super(context, serviceName, VodTmSession.class.getName());
	}

	
	@Override
	public void onQueryCurrentVodInfo() {

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
	public void uploadCurrentVodInfo(String json) {
		ch.transmit(__ID_uploadCurrentVodInfo,json);

	}

	@Override
	protected void onCallback(int code, String json, JsonParcelable p, Bundle b)
			throws JSONException {
		switch (code) {
		case __ID_onQueryCurrentVodInfo:
			onQueryCurrentVodInfo();
			break;
		default:
			super.onCallback(code, json, p, b);
			break;
		}
		
	}
}
