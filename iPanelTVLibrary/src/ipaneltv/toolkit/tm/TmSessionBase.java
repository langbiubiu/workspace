package ipaneltv.toolkit.tm;

import ipaneltv.toolkit.JsonChannel;
import ipaneltv.toolkit.JsonParcelable;

import org.json.JSONException;

import android.content.Context;
import android.os.Bundle;

public class TmSessionBase implements TmSessionInterface {

	JsonChannel ch;

	public TmSessionBase(Context context, String serviceName, String sessionName) {
		ch = new JsonChannel(context, serviceName, sessionName) {

			@Override
			public void onCallback(int code, String json, JsonParcelable p, Bundle b)
					throws JSONException {
				try {
					TmSessionBase.this.onCallback(code, json, p, b);
				} catch (Exception e) {
					e.printStackTrace();
				}

			}

			public void onChannelConnected() {
				onServiceConnected();
			}

			public void onChannelDisconnectted() {
				onServiceDisconnectted();
			};
		};
		ch.connect();
	}

	protected void onCallback(int code, String json, JsonParcelable p, Bundle b)
			throws JSONException {

	}

	protected void onServiceConnected() {

	}

	protected void onServiceDisconnectted() {

	}

	@Override
	public void close() {
		ch.transmit(__ID_close);
	}
}
