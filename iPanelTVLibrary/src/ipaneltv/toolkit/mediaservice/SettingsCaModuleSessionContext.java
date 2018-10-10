package ipaneltv.toolkit.mediaservice;

import ipaneltv.toolkit.IPanelLog;
import ipaneltv.toolkit.JsonParcelable;
import ipaneltv.toolkit.media.MediaSessionInterface.SettingsCaModuleSessionInterface;

import java.util.HashMap;
import java.util.Map.Entry;

import org.json.JSONException;
import org.json.JSONObject;
import org.json.JSONStringer;
import org.json.JSONTokener;

import android.os.Bundle;

public abstract class SettingsCaModuleSessionContext<T extends CaModuleSessionService> extends
		CaModuleSessionContext<T> implements SettingsCaModuleSessionInterface,
		SettingsCaModuleSessionInterface.Callback {

	public SettingsCaModuleSessionContext(T service) {
		super(service);
	}

	@Override
	public String onTransmit(int code, String json, JsonParcelable p, Bundle b)
			throws JSONException {
		try {
			IPanelLog.d(TAG, "onTransmit code=" + code + ",json=" + json);
			switch (code) {
			case __ID_queryReadableEntries:
				queryReadableEntries();
				break;
			case __ID_buyEntitlement:
				JSONObject o = (JSONObject) new JSONTokener(json).nextValue();
				buyEntitlement(o.getString("uri"), o.getString("ext"));
			case __ID_querySettings:
				querySettings(json,b);
				break;
			case __ID_updateSettings:
				updateSettings(json, b);
				break;
			case __ID_checkEntitlementUpdate:
				checkEntitlementUpdate();
				break;
			default:
				return super.onTransmit(code, json, p, b);
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		return null;
	}

	@Override
	public void onReadableEntries(HashMap<String, String> entries) {
		Bundle b = new Bundle();
		for (Entry<String, String> e : entries.entrySet()) {
			b.putString(e.getKey(), e.getValue());
		}
		notifyJson(__ID_onReadableEntries, null, b);
	}

	public void onResponseQuerySettings(Bundle b) {
		notifyJson(__ID_onResponseQuerySettings, null, b);
	}
	
	@Override
	public void onResponseQuerySettings(String token ,Bundle b) {
		notifyJson(__ID_onResponseQuerySettings, token, b);
	}

	@Override
	public void onResponseUpdateSettings(String token, String err) {
		try {
			JSONStringer s = new JSONStringer();
			s.object();
			s.key("token").value(token);
			if (err != null)
				s.key("err").value(err);
			s.endObject();
			notifyJson(__ID_onResponseUpdateSettings, s.toString());
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	@Override
	public void onSettingsUpdated(String token) {
		notifyJson(__ID_onSettingsUpdated, token);
	}

	@Override
	public void onResponseBuyEntitlement(String uri, String err) {
		try {
			JSONStringer s = new JSONStringer();
			s.object();
			s.key("uri").value(uri);
			if (err != null)
				s.key("err").value(err);
			s.endObject();
			notifyJson(__ID_onResponseBuyEntitlement, s.toString());
		} catch (Exception e) {
			e.printStackTrace();
		}

	}

}
