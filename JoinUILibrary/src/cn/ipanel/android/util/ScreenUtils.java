package cn.ipanel.android.util;

import android.content.Context;
import android.content.res.Configuration;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.View;

public class ScreenUtils {

	public static int getLeftInWindow(View v) {
		View parent = (View) v.getParent();
		if (parent == v.getRootView())
			return v.getLeft();
		return v.getLeft() + getLeftInWindow(parent);
	}

	public static int getTopInWindow(View v) {
		View parent = (View) v.getParent();
		if (parent == v.getRootView())
			return v.getTop();
		return v.getTop() + getTopInWindow(parent);
	}
	
	public static void fakeScreenSWDP(Context ctx, int sw){
		Configuration conf = ctx.getResources().getConfiguration();
		DisplayMetrics dm = ctx.getResources().getDisplayMetrics();
		int sWidth = Math.min(dm.widthPixels, dm.heightPixels);
		float density = (float) sWidth / sw;
		float delta = dm.density / density;
		Log.d("FAKE", "sw=" + sw + ", sWidth = " + sWidth + ", " + "density=" + density
				+ ", delta=" + delta);
		if (delta > 1.12f || delta < 0.88f) {
			dm.density /= delta;
			dm.densityDpi /= delta;
			dm.scaledDensity /= delta;

			conf.screenHeightDp *= delta;
			conf.screenWidthDp *= delta;
			conf.smallestScreenWidthDp *= delta;
			Log.d("FAKE", "swdp=" + conf.smallestScreenWidthDp + "dp, density=" + dm.density);
			ctx.getResources().updateConfiguration(conf, dm);
		}
		
	}
}
