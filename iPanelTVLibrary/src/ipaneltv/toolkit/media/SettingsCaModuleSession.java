package ipaneltv.toolkit.media;

import ipaneltv.toolkit.IPanelLog;
import ipaneltv.toolkit.JsonParcelable;
import ipaneltv.toolkit.media.MediaSessionInterface.SettingsCaModuleSessionInterface;

import java.util.HashMap;
import java.util.Set;

import org.json.JSONObject;
import org.json.JSONStringer;
import org.json.JSONTokener;

import android.content.Context;
import android.os.Bundle;

public class SettingsCaModuleSession extends MediaSessionClient implements
		SettingsCaModuleSessionInterface, SettingsCaModuleSessionInterface.Callback {

	public SettingsCaModuleSession(Context context, String serviceName, String sessionName) {
		super(context, serviceName, sessionName);

	}

	public SettingsCaModuleSession(Context context, String serviceName) {
		this(context, serviceName, SettingsCaModuleSessionInterface.class.getName());
	}

	public final void querySettings() {
		querySettings(null,null);
	}

	@Override
	public final void querySettings(String token,Bundle b) {
		channel.transmit(__ID_querySettings, token, null, b);
	}

	@Override
	public final void queryReadableEntries() {
		channel.transmit(__ID_queryReadableEntries);
	}

	@Override
	public final void updateSettings(String token, Bundle b) {
		channel.transmit(__ID_updateSettings, token, null, b);
	}

	@Override
	public void checkEntitlementUpdate() {
		channel.transmit(__ID_checkEntitlementUpdate);
	}

	@Override
	public void buyEntitlement(String uri, String ext) {
		try {
			JSONStringer s = new JSONStringer();
			s.object();
			s.key("uri").value(uri).key("ext").value(ext);
			s.endObject();
			channel.transmit(__ID_buyEntitlement, s.toString(), null, null);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	private void notifyUpdateSettingsResult(final String token, final String err) {
		onResponseUpdateSettings(token, err.equals("") ? null : err);
	}

	@Override
	protected void onCallback(int code, String json, JsonParcelable p, Bundle b) {
		try {
			IPanelLog.d(TAG, "onCallback code = " + code + ";json = " + json);
			switch (code) {
			case __ID_onReadableEntries: {
				HashMap<String, String> ret = new HashMap<String, String>();
				Set<String> keys = b.keySet();
				for (String k : keys) {
					String v = b.getString(k);
					if (v != null)
						ret.put(k, v);
				}
				onReadableEntries(ret);
				break;
			}
			case __ID_onResponseQuerySettings:{
				onResponseQuerySettings(json,b);
				break;
				}
			case __ID_onResponseUpdateSettings: {
				JSONObject o = (JSONObject) new JSONTokener(json).nextValue();
				notifyUpdateSettingsResult(o.getString("token"), o.getString("err"));
				break;
			}
			case __ID_onResponseBuyEntitlement:{
				JSONObject o = (JSONObject) new JSONTokener(json).nextValue();
				onResponseBuyEntitlement(o.getString("uri"), o.getString("err"));
				break;
				}
			case __ID_onSettingsUpdated:
				onSettingsUpdated(json);
				break;
			default:
				break;
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	@Override
	public void onReadableEntries(HashMap<String, String> entries) {
	}

	@Override
	public void onResponseQuerySettings(String token, Bundle b) {
		
	}

	@Override
	public void onResponseUpdateSettings(String token, String err) {
	}

	@Override
	public void onResponseBuyEntitlement(String uri, String err) {
	}

	@Override
	public void onSettingsUpdated(String token) {
		
	}

}
