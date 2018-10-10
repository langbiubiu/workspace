package android.appwidget;

import android.content.Context;

public class JXTeeveeWidgetHost extends TeeveeWidgetHost {

	public JXTeeveeWidgetHost(Context context, int hostId) {
		super(context, hostId);
	}

	@Override
	protected AppWidgetHostView onCreateView(Context context, int appWidgetId,
			AppWidgetProviderInfo appWidget) {
		TeeveeWidgetHostView ret = new JXTeeveeWidgetHostView(context);
		if (ret != null)
			ret.setProviderName(appWidget.provider.getClassName());
		return ret;
	}

}
