package ipaneltv.toolkit.media;

import ipaneltv.toolkit.IPanelLog;
import ipaneltv.toolkit.JsonParcelable;
import ipaneltv.toolkit.media.MediaSessionInterface.LiveCaModuleSessionBaseInterface;
import android.content.Context;
import android.os.Bundle;

public class LiveCaModuleSession extends MediaSessionClient implements
		LiveCaModuleSessionBaseInterface, LiveCaModuleSessionBaseInterface.Callback {

	public LiveCaModuleSession(Context context, String serviceName, String sessionName) {
		super(context, serviceName, sessionName);
	}

	public LiveCaModuleSession(Context context, String serviceName) {
		this(context, serviceName, LiveCaModuleSessionBaseInterface.class.getName());
	}

	@Override
	public final void queryNextScrollMessage() {
		channel.transmit(__ID_queryNextScrollMessage);
	}

	@Override
	public final void queryUnreadMailSize() {
		channel.transmit(__ID_queryUnreadMailSize);
	}

	@Override
	public void queryUnreadMail(String token) {
		channel.transmit(__ID_queryUnreadMail);
	}
	
	@Override
	public void checkEntitlementUpdate() {
		channel.transmit(__ID_checkEntitlementUpdate);
	}

	@Override
	protected void onCallback(int code, final String json, JsonParcelable p, Bundle b) {
		IPanelLog.d(this.toString(), "onCallback code = "+ code +";json = "+ json);
		switch (code) {
		case __ID_onScrollMessage:
			onScrollMessage(json);
			break;
		case __ID_onUnreadMailSize:
			onUnreadMailSize(Integer.parseInt(json));
			break;
		case __ID_UrgencyMails:
			onUrgencyMails(json,b);
			break;
		default:
			break;
		}
	}

	@Override
	public void onScrollMessage(String msg) {
	}

	@Override
	public void onUnreadMailSize(int n) {
	}

	@Override
	public void onUrgencyMails(String token ,Bundle b) {
		
	}
	
}
