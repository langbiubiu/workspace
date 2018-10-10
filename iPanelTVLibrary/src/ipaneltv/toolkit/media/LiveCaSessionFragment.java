package ipaneltv.toolkit.media;

import ipaneltv.toolkit.IPanelLog;
import android.net.telecast.ca.CAManager;
import android.os.Bundle;

/**
 * 函数和回调都在UI线程中进行
 */
public class LiveCaSessionFragment extends CaSessionFragment {
	static final String TAG = LiveCaSessionFragment.class.getSimpleName();

	public LiveCaSessionFragment() {
	}

	protected void onNativeSessionReady(Session s) {
		IPanelLog.d(TAG, "onSessionReady 2 s = " + s);
	}

	protected final void onSessionReady(CaSessionFragment.Session s) {
		IPanelLog.d(TAG, "onSessionReady 1111 s = " + s);
		onNativeSessionReady((Session) s);
	}

	protected void onScrollMessage(String msg) {
		IPanelLog.d(TAG, "onScrollMessage msg = " + msg);
	}

	protected void onUnreadMailSize(int size) {
	}

	protected void onUrgencyMails(String token, Bundle b) {
		IPanelLog.d(TAG, "onImportantMail b = " + b);
	}

	public class Session extends CaSessionFragment.Session {
		LiveCaModuleSession cas;

		public Session(String serviceName, int mid, int sn) {
			super(mid, sn);
			cas = new LiveCaModuleSession(getActivity().getApplicationContext(), serviceName) {
				@Override
				public void onScrollMessage(final String msg) {
					IPanelLog.d(this.toString(), "onScrollMessage msg = " + msg);
					super.onScrollMessage(msg);
					postToUi(new Runnable() {
						@Override
						public void run() {
							LiveCaSessionFragment.this.onScrollMessage(msg);
						}
					});
				}

				@Override
				public void onUnreadMailSize(final int n) {
					super.onUnreadMailSize(n);
					postToUi(new Runnable() {
						@Override
						public void run() {
							LiveCaSessionFragment.this.onUnreadMailSize(n);
						}
					});
				}

				@Override
				public void onUrgencyMails(final String token, final Bundle b) {
					super.onUrgencyMails(token, b);
					postToUi(new Runnable() {
						@Override
						public void run() {
							LiveCaSessionFragment.this.onUrgencyMails(token, b);
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
			cas.setArguments(bu);
			connect();
		}

		public void queryNextScrollMessage() {
			postToProc(new Runnable() {
				@Override
				public void run() {
					cas.queryNextScrollMessage();
				}
			});
		}

		public void queryUnreadMailSize() {
			postToProc(new Runnable() {
				@Override
				public void run() {
					cas.queryUnreadMailSize();
				}
			});
		}

		public final void checkEntitlementUpdate() {
			postToProc(new Runnable() {
				@Override
				public void run() {
					cas.checkEntitlementUpdate();
				}
			});
		}

		@Override
		public boolean reserve() {
			return cas.reserve();
		}

		@Override
		public void loosen(boolean clearState) {
			cas.loosen(clearState);
		}

		@Override
		public boolean isReserved() {
			return cas.isReserved();
		}

		@Override
		protected void connect() {
			cas.connectToService();
		}

		@Override
		protected void close() {
			cas.close();
		}
	}

	@Override
	protected CaSessionFragment.Session createSession(String serviceName, int mid, int sn) {
		return new Session(serviceName, mid, sn);
	}

}
