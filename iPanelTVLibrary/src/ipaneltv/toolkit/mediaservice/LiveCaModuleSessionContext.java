package ipaneltv.toolkit.mediaservice;

import ipaneltv.toolkit.JsonParcelable;
import ipaneltv.toolkit.media.MediaSessionInterface.LiveCaModuleSessionBaseInterface;

import org.json.JSONException;

import android.os.Bundle;

public abstract class LiveCaModuleSessionContext<T extends CaModuleSessionService> extends
		CaModuleSessionContext<T> implements LiveCaModuleSessionBaseInterface,
		LiveCaModuleSessionBaseInterface.Callback {

	public LiveCaModuleSessionContext(T service) {
		super(service);
	}

	@Override
	public String onTransmit(int code, String json, JsonParcelable p, Bundle b)
			throws JSONException {
		try {
			switch (code) {
			case __ID_queryNextScrollMessage:
				queryNextScrollMessage();
				break;
			case __ID_queryUnreadMailSize:
				queryUnreadMailSize();
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
	public void onScrollMessage(String msg) {
		notifyJson(__ID_onScrollMessage, msg);
	}

	@Override
	public void onUnreadMailSize(int n) {
		notifyJson(__ID_onUnreadMailSize, n + "");
	}
	
	@Override
	public void onUrgencyMails(String token, Bundle b) {
		notifyJson(__ID_UrgencyMails, token, b);
	}
}

abstract class CaModuleSessionContext<T extends CaModuleSessionService> extends
		MediaSessionContext<T> {
	public CaModuleSessionContext(T service) {
		super(service);
	}
}
