package ipaneltv.toolkit.tm;

import ipaneltv.toolkit.tm.LiveTmFragment.LiveTmManager;

import org.json.JSONException;
import org.json.JSONStringer;

import android.app.Fragment;
import android.os.Bundle;
import android.os.Handler;
import android.os.HandlerThread;

public class VodTmFragment extends Fragment {

	public static final String ARG_TM_SERVICE_NAME = "tmservicename";
	VodTmManager manager;
	VodTmSession session;

	public static Bundle createArguments(String tmServiceNmae) {
		Bundle b = new Bundle();
		b.putString(ARG_TM_SERVICE_NAME, tmServiceNmae);
		return b;
	}

	public static VodTmFragment createInstance(String tmServiceNmae) {
		Bundle b = createArguments(tmServiceNmae);
		VodTmFragment f = new VodTmFragment();
		f.setArguments(b);
		return f;
	}

	public VodTmFragment() {
		manager = new VodTmManager();
	}

	public VodTmInterface getVodTmInterface(VodTmCallback cbk) {
		manager.setCallback(cbk);
		return manager;
	}

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		session = new VodTmSession(getActivity(), getArguments().getString(ARG_TM_SERVICE_NAME)) {
			protected void onServiceConnected() {
				manager.postUI(new Runnable() {

					@Override
					public void run() {
						manager.callback.onContextReady();
					}

				});
			}

			public void onQueryCurrentVodInfo() {
				manager.postUI(new Runnable() {
					@Override
					public void run() {
						manager.callback.onQueryCurrentVodInfo();
					}
				});
			}
		};
	}

	@Override
	public void onDestroy() {
		manager.release();
		session.close();
		super.onDestroy();
	}

	public static interface VodTmInterface {
		// 上报当前点播信息
		void uploadCurrentVodInfo(int serviceType, String videoName, String url);
	}

	public static interface VodTmCallback {
		// 服务端已经准备好，可以上报了
		void onContextReady();

		// 服务端请求上报当前点播信息
		void onQueryCurrentVodInfo();
	}

	class VodTmManager implements VodTmInterface {
		VodTmCallback callback;
		private HandlerThread procThread = new HandlerThread(LiveTmManager.class.getName());
		private Handler procHandler, callbackHandler;

		public VodTmManager() {
			procThread.start();
			procHandler = new Handler(procThread.getLooper());
			callbackHandler = new Handler();
		}

		public void postUI(Runnable task) {
			callbackHandler.post(task);
		}

		public void postProc(Runnable task) {
			procHandler.post(task);
		}

		public void setCallback(VodTmCallback cbk) {
			this.callback = cbk;
		}

		void release() {
			callback = null;
			procThread.getLooper().quit();
		}

		@Override
		public void uploadCurrentVodInfo(int serviceType, String videoName, String url) {
			final JSONStringer s = new JSONStringer();
			try {
				s.object();
				s.key("st").value(serviceType);
				s.key("vn").value(videoName);
				s.key("url").value(url);
				s.endObject();
			} catch (JSONException e) {
				e.printStackTrace();
			}
			procHandler.post(new Runnable() {

				@Override
				public void run() {
					session.uploadCurrentVodInfo(s.toString());

				}
			});

		}
	}
}
