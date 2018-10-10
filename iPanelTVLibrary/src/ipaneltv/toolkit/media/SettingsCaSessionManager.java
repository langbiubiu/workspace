package ipaneltv.toolkit.media;

import ipaneltv.toolkit.IPanelLog;

import java.util.HashMap;

import android.content.Context;
import android.net.telecast.ca.CAManager;
import android.os.Bundle;

public class SettingsCaSessionManager extends CaSessionManager {
	static final String TAG = SettingsCaSessionManager.class.getSimpleName();

	public SettingsCaSessionManager(String uuid, Context ctx) {
		super(uuid, ctx);
		addAllCaModuleSession();
	}

	public class Session extends CaSessionManager.Session {
		SettingsCaModuleSession sms;

		public Session(String serviceName, int mid, int sn) {
			super(mid, sn);
			sms = new SettingsCaModuleSession(getContext(), serviceName) {

				@Override
				public void onResponseBuyEntitlement(final String uri, final String err) {
					postToUi(new Runnable() {
						@Override
						public void run() {
							Session.this.onResponseBuyEntitlement(uri, err);
						}
					});
				}

				@Override
				public void onReadableEntries(final HashMap<String, String> entries) {
					IPanelLog.d(this.toString(), "onReadableEntries entries = " + entries);
					postToUi(new Runnable() {
						@Override
						public void run() {
							Session.this.onReadableEntries(entries);
						}
					});
				}

				@Override
				public void onResponseQuerySettings(final String token, final Bundle b) {
					postToUi(new Runnable() {
						@Override
						public void run() {
							Session.this.onResponseQuerySettings(token, b);
						}
					});
				}

				@Override
				public void onResponseUpdateSettings(final String token, final String err) {
					postToUi(new Runnable() {
						@Override
						public void run() {
							Session.this.onResponseUpdateSettings(token, err);
						}
					});
				}

				@Override
				public void onSettingsUpdated(final String token) {
					postToUi(new Runnable() {
						@Override
						public void run() {
							Session.this.onSettingsUpdated(token);
						}
					});
				}

				@Override
				public void onServiceConnected() {
					Session.this.onServiceConnected();
				}

				@Override
				public void onServiceLost() {
					Session.this.onServiceLost();
				}
			};
			Bundle bu = new Bundle();
			bu.putInt(CAManager.PROP_NAME_MODULE_SN, sn);
			sms.setArguments(bu);
			connect();
		}

		public void onResponseBuyEntitlement(String uri, String err) {
			SettingsCaSessionManager.this.onResponseBuyEntitlement(uri, err);
		}

		public void onReadableEntries(HashMap<String, String> entries) {
			IPanelLog.d(TAG, "onReadableEntries entries = " + entries);
			SettingsCaSessionManager.this.onReadableEntries(entries);
		}

		public void onResponseQuerySettings(String token, Bundle b) {
			if (token == null) {
				SettingsCaSessionManager.this.onResponseQuerySettings(b);
			} else {
				SettingsCaSessionManager.this.onResponseQuerySettings(token, b);
			}
		}

		public void onResponseUpdateSettings(String token, String err) {
			SettingsCaSessionManager.this.onResponseUpdateSettings(token, err);
		}

		public void onSettingsUpdated(String token) {
			SettingsCaSessionManager.this.onSettingsUpdated(token);
		}

		public final void querySettings() {
			postToProc(new Runnable() {
				@Override
				public void run() {
					sms.querySettings();
				}
			});
		}

		public final void querySettings(final String token, final Bundle b) {
			postToProc(new Runnable() {
				@Override
				public void run() {
					sms.querySettings(token, b);
				}
			});
		}

		public final void queryReadableEntries() {
			postToProc(new Runnable() {
				@Override
				public void run() {
					sms.queryReadableEntries();
				}
			});
		}

		public final void updateSettings(final String token, final Bundle b) {
			postToProc(new Runnable() {
				@Override
				public void run() {
					sms.updateSettings(token, b);
				}
			});
		}

		public void buyEntitlement(final String uri, final String ext) {
			postToProc(new Runnable() {
				@Override
				public void run() {
					sms.buyEntitlement(uri, ext);
				}
			});
		}

		public final void checkEntitlementUpdate() {
			postToProc(new Runnable() {
				@Override
				public void run() {
					sms.checkEntitlementUpdate();
				}
			});
		}

		@Override
		public boolean reserve() {
			return sms.reserve();
		}

		@Override
		public void loosen(boolean clearState) {
			sms.loosen(clearState);
		}

		@Override
		public boolean isReserved() {
			return sms.isReserved();
		}

		@Override
		protected void connect() {
			sms.connectToService();
		}

		@Override
		protected void close() {
			sms.close();
		}
	}

	public void onResponseBuyEntitlement(String uri, String err) {
	}

	public void onReadableEntries(HashMap<String, String> entries) {
	}

	public void onResponseQuerySettings(String token, Bundle b) {

	}

	public void onResponseQuerySettings(Bundle b) {

	}

	public void onResponseUpdateSettings(String token, String err) {
	}

	public void onSettingsUpdated(String token) {
	}

	@Override
	protected CaSessionManager.Session createSession(String serviceName, int mid, int sn) {
		return new Session(serviceName, mid, sn);
	}

}
