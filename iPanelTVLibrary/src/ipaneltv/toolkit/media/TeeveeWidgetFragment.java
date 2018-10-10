package ipaneltv.toolkit.media;

import ipaneltv.toolkit.IPanelLog;
import android.app.Fragment;
import android.appwidget.AppWidgetManager;
import android.appwidget.AppWidgetProviderInfo;
import android.appwidget.TeeveeWidgetHost;
import android.appwidget.TeeveeWidgetHostView;
import android.content.Context;
import android.net.telecast.NetworkManager;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

/**
 * 使用者需要提供三个参数<br>
 * WIDGET_HOST_ID<br>
 * NETWORK_UUID<br>
 * WIDGET_NAME<br>
 * 
 * @see android.net.telecast.NetworkManager
 */
public class TeeveeWidgetFragment extends Fragment {

	/** Host ID */
	public static final String WIDGET_HOST_ID = "widget_host_id";
	/** 提供者网络的UUID */
	public static final String NETWORK_UUID = "network_uuid";
	/** Widget的名字 */
	public static final String WIDGET_NAME = "widget_name";

	static final String TAG = TeeveeWidgetFragment.class.getSimpleName();
	protected NetworkManager networkManager;
	protected AppWidgetManager mAppWidgetManager;
	protected TeeveeWidgetHost mAppWidgetHost;
	protected AppWidgetProviderInfo appWidgetInfo;
	protected TeeveeWidgetHostView hostView;
	protected int appWidgetId;

	public static final Bundle createArguments(String netuuid, String widgetName, int hostId) {
		Bundle b = new Bundle();
		b.putString(NETWORK_UUID, netuuid);
		b.putString(WIDGET_NAME, widgetName);
		b.putInt(WIDGET_HOST_ID, hostId);
		return b;
	}

	public TeeveeWidgetFragment() {
	}

	public NetworkManager getNetworkManager() {
		return networkManager;
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		try {
			Context ctx = getActivity().getApplicationContext();
			int hostid = getArguments().getInt(WIDGET_HOST_ID);
			String netid = getArguments().getString(NETWORK_UUID);
			String wname = getArguments().getString(WIDGET_NAME);
			IPanelLog.d(TAG, "onCreateView(hostid:" + hostid + ",uuid:" + netid + ",name:" + wname + ")");
			networkManager = NetworkManager.getInstance(ctx);
			mAppWidgetManager = AppWidgetManager.getInstance(ctx);
			mAppWidgetHost = new TeeveeWidgetHost(ctx, hostid);
			mAppWidgetHost.startListening();
			appWidgetId = mAppWidgetHost.allocateAppWidgetId();
			if (networkManager.bindNetworkTeeveeWidgetId(netid, appWidgetId, wname)) {
				appWidgetInfo = mAppWidgetManager.getAppWidgetInfo(appWidgetId);
				hostView = (TeeveeWidgetHostView) mAppWidgetHost.createView(ctx, appWidgetId,
						appWidgetInfo);
				hostView.setId(appWidgetId);
				return hostView;
			} else {
				IPanelLog.i(TAG, "bindNetworkTeeveeWidgetId failed!");
			}
		} catch (Exception e) {
			IPanelLog.e(TAG, "onCreateView error:" + e);
		}
		return null;
	}
}
