package android.appwidget;

import android.content.Context;
import android.net.telecast.TransportManager;
import android.util.Log;

/**
 * ���󴴽���View��TeeveeWidgetHostView���͵�
 */

/*- <pre>
 * <code>
 *	void initWidget(Context ctx, String uuid) {
 *		AppWidgetManager mAppWidgetManager = AppWidgetManager.getInstance(ctx.getApplicationContext());
 *		TeeveeWidgetHost mAppWidgetHost = new TeeveeWidgetHost(ctx.getApplicationContext(), XXX);
 *		mAppWidgetHost.startListening();
 *
 *		int mAppWidgetId = mAppWidgetHost.allocateAppWidgetId();
 *		NetworkManager mNetworkManager = NetworkManager.getInstance(ctx);
 *		if (mNetworkManager.bindNetworkTeeveeWidgetId(uuid, mAppWidgetId)) {
 *			AppWidgetProviderInfo appWidgetInfo = mAppWidgetManager.getAppWidgetInfo(mAppWidgetId);
 *			TeeveeWidgetHostView hostView = (TeeveeWidgetHostView) mAppWidgetHost.createView(ctx,
 *			mAppWidgetId, appWidgetInfo);
 *			hostView.setId(mAppWidgetId);			
 *		}	
 *	}
 *	</code>
 *	</pre>
 */
public class TeeveeWidgetHost extends AppWidgetHost {
	static final String TAG = "[java]TeeveeAppWidgetHost";

	public TeeveeWidgetHost(Context context, int hostId) {
		super(context, hostId);
	}

	@Override
	protected AppWidgetHostView onCreateView(Context context, int appWidgetId,
			AppWidgetProviderInfo appWidget) {
		String classname = TransportManager.getSystemProperty("TeeveeWidgetHostView");// ��Ϊϵͳ���Գ����31���ַ�
		TeeveeWidgetHostView ret = null;
		if (classname.equals("") || classname == null) {
			ret = new TeeveeWidgetHostView(context);
		} else {
			try {// ���ƽ̨������ʵ����ʹ������ʵ��.
				ret = (TeeveeWidgetHostView) Class.forName(classname).getConstructor(Context.class)
						.newInstance(context);
			} catch (Exception e) {
				Log.e(TAG, "create instance of '" + classname + "' error:" + e);
			}
		}
		if (ret != null)
			ret.setProviderName(appWidget.provider.getClassName());
		return ret;
	}

}
