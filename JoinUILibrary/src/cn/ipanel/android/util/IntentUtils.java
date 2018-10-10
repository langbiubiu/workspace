package cn.ipanel.android.util;

import java.util.List;

import android.content.Context;
import android.content.Intent;
import android.content.pm.ResolveInfo;
import android.util.Log;

public class IntentUtils {
	static final String TAG = IntentUtils.class.getSimpleName();

	private static final String CATEGORY_IPANEL_LAUNCHER = "android.intent.category.ipanel.LAUNCHER";

	public static Intent getLaunchIntent(Context ctx, String pkg) {
		Intent intent = ctx.getPackageManager().getLaunchIntentForPackage(pkg);
		if (intent != null)
			return intent;

		// fall back to iPanel category launcher
		Log.d(TAG, "Try to resolve ipanel launch intent for: " + pkg);
		intent = new Intent(Intent.ACTION_MAIN);
		intent.addCategory(CATEGORY_IPANEL_LAUNCHER);
		intent.setPackage(pkg);
		List<ResolveInfo> result = ctx.getPackageManager().queryIntentActivities(intent, 0);
		Log.d(TAG, pkg + " resolve size = " + result.size());
		if(result.size() == 0)
			return null;
		
		for (ResolveInfo ri : result) {
			Log.d(TAG, "resolve to: " + ri.activityInfo.packageName + "/" + ri.activityInfo.name);
			// set the resolved package and activity class
			intent.setClassName(ri.activityInfo.packageName, ri.activityInfo.name);
			break;
		}
		intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
		return intent;
	}
}
