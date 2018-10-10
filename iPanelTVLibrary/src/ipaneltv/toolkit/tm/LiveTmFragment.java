package ipaneltv.toolkit.tm;

import org.json.JSONException;
import org.json.JSONStringer;

import android.app.Fragment;
import android.os.Bundle;
import android.os.Handler;
import android.os.HandlerThread;

public class LiveTmFragment extends Fragment {

	public static final String ARG_TM_SERVICE_NAME = "tmservicename";
	LiveTmSession session;
	LiveTmManager manager;

	public static Bundle createArguments(String tmServiceNmae) {
		Bundle b = new Bundle();
		b.putString(ARG_TM_SERVICE_NAME, tmServiceNmae);
		return b;
	}

	public static LiveTmFragment createInstance(String tmServiceNmae) {
		Bundle b = createArguments(tmServiceNmae);
		LiveTmFragment f = new LiveTmFragment();
		f.setArguments(b);
		return f;
	}

	public LiveTmFragment() {
		manager = new LiveTmManager();
	}

	public LiveTmInterface getLiveTmInterface(LiveTmCallback cbk) {
		manager.setCallback(cbk);
		return manager;
	}

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		session = new LiveTmSession(getActivity(), getArguments().getString(ARG_TM_SERVICE_NAME)) {
			protected void onServiceConnected() {
				manager.postUI(new Runnable() {

					@Override
					public void run() {
						manager.callback.onContextReady();

					}
				});
			}

			public void onQueryCurrentChannelInfo() {
				manager.postUI(new Runnable() {

					@Override
					public void run() {
						manager.callback.onQueryCurrentChannelInfo();

					}
				});
			}

//			public void onQueryChannelRatings(String json) {
//				// TODO json to args
//				manager.callback.onQueryChannelRatings(0);
//			}
//
//			public void onQueryProgramRatings(String json) {
//				// TODO json to args
//				manager.callback.onQueryProgramRatings(null);
//			}
		};
	}

	@Override
	public void onDestroy() {
		manager.release();
		session.close();
		super.onDestroy();
	}

	public static interface LiveTmCallback {
		//服务端已经准备好，可以开上报信息
		void onContextReady();
		//服务端请求上报当前直播业务信息
		void onQueryCurrentChannelInfo();

//		void onQueryChannelRatings(int channelNumber);
//
//		void onQueryProgramRatings(String programName);
	}

	public static interface LiveTmInterface {
		//上报当前直播业务信息
		void uploadCurrentChannelInfo(String serviceType, int channelNumber, int tsid, int onid,
				int serviceid, String serviceName);
//
//		void uploadChannelRatings(String json);//TODO args
//
//		void uploadProgramRatings(String json);//TODO args
	}

	class LiveTmManager implements LiveTmInterface {
		private HandlerThread procThread = new HandlerThread(LiveTmManager.class.getName());
		private Handler procHandler, callbackHandler;
		LiveTmCallback callback;

		LiveTmManager() {
			procThread.start();
			procHandler = new Handler(procThread.getLooper());
			callbackHandler = new Handler();
		}

		void setCallback(final LiveTmCallback callback) {
			this.callback = callback;
		}

		void release() {
			callback = null;
			procThread.getLooper().quit();
		}

		final void postProc(Runnable r) {
			procHandler.post(r);
		}

		final void postUI(Runnable r) {
			callbackHandler.post(r);
		}

		@Override
		public void uploadCurrentChannelInfo(String serviceType, int channelNumber, int tsid,
				int onid, int serviceid, String serviceName) {
			final JSONStringer s = new JSONStringer();
			try {
				s.object().key("st").value(serviceType);
				s.key("cn").value(channelNumber);
				s.key("tsid").value(tsid);
				s.key("onid").value(onid);
				s.key("sid").value(serviceid);
				s.key("sn").value(serviceName);
				s.endObject();
				postProc(new Runnable() {
					@Override
					public void run() {
						session.uploadCurrentChannelInfo(s.toString());
					}
				});
			} catch (JSONException e) {
				e.printStackTrace();
			}

		}

//		@Override
//		public void uploadChannelRatings(String json) {
//			// TODO Auto-generated method stub
//
//		}
//
//		@Override
//		public void uploadProgramRatings(String json) {
//			// TODO Auto-generated method stub
//
//		}
	}
}
