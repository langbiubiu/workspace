package cn.ipanel.android.res;

import cn.ipanel.android.graphics.drawable.DrawableCompat;
import android.content.res.ColorStateList;
import android.content.res.Resources;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.StateListDrawable;

public class ResUtils {
	public static StateListDrawable getDrawableList(String focusColor, String normalColor) {
		return getDrawableList(Color.parseColor(focusColor), Color.parseColor(normalColor));
	}

	public static StateListDrawable getDrawableList(int focusColor, int normalColor) {
		StateListDrawable sld = new StateListDrawable();
		sld.addState(new int[] { android.R.attr.state_focused }, new ColorDrawable(focusColor));
		sld.addState(new int[] {}, new ColorDrawable(normalColor));
		return sld;
	}
	
	public static StateListDrawable getDrawableList(Resources res, int focus, int selected,
			int normal) {
		return getDrawableList(res.getDrawable(focus), res.getDrawable(selected),
				res.getDrawable(normal));
	}

	public static StateListDrawable getDrawableList(Resources res, int focus, int normal) {
		return getDrawableList(res.getDrawable(focus), res.getDrawable(normal));
	}
	
	public static StateListDrawable getDrawableList(Drawable focus, Drawable normal) {
		StateListDrawable sld = new StateListDrawable();
		sld.addState(new int[] { android.R.attr.state_focused }, focus);
		sld.addState(new int[] {}, normal);
		return sld;
	}
	
	public static StateListDrawable getDrawableList(Drawable focus, Drawable selected, Drawable normal) {
		StateListDrawable sld = new StateListDrawable();
		sld.addState(new int[] { android.R.attr.state_focused }, focus);
		sld.addState(new int[] { android.R.attr.state_selected }, selected);
		sld.addState(new int[] {}, normal);
		return sld;
	}

	public static StateListDrawable getDrawableList(String focusColor, String selectColor,
			String normalColor) {
		return getDrawableList(Color.parseColor(focusColor), Color.parseColor(selectColor),
				Color.parseColor(normalColor));
	}

	public static StateListDrawable getDrawableList(int focusColor, int selectColor, int normalColor) {
		StateListDrawable sld = new StateListDrawable();
		sld.addState(new int[] { android.R.attr.state_focused }, new ColorDrawable(focusColor));
		sld.addState(new int[] { android.R.attr.state_selected }, new ColorDrawable(selectColor));
		sld.addState(new int[] {}, new ColorDrawable(normalColor));
		return sld;
	}

	public static ColorStateList getColorList(String focusColor, String normalColor) {
		return getColorList(Color.parseColor(focusColor), Color.parseColor(normalColor));
	}

	public static ColorStateList getColorList(int focusColor, int normalColor) {
		ColorStateList csl = new ColorStateList(new int[][] {
				new int[] { android.R.attr.state_focused }, new int[] {} }, new int[] { focusColor,
				normalColor });
		return csl;
	}

	public static ColorStateList getColorList(String focusColor, String selectColor,
			String normalColor) {
		return getColorList(Color.parseColor(focusColor), Color.parseColor(selectColor),
				Color.parseColor(normalColor));
	}

	public static ColorStateList getColorList(int focusColor, int selectColor, int normalColor) {
		ColorStateList csl = new ColorStateList(new int[][] {
				new int[] { android.R.attr.state_focused },
				new int[] { android.R.attr.state_selected }, new int[] {} }, new int[] {
				focusColor, selectColor, normalColor });
		return csl;
	}
	
	public static Drawable tintDrawable(Drawable d, int color){
		Drawable wrapped = DrawableCompat.wrap(d.mutate());
		DrawableCompat.setTint(wrapped, color);
		return wrapped;
	}
	
	public static Drawable tintDrawable(Drawable d, ColorStateList colorList){
		Drawable wrapped = DrawableCompat.wrap(d.mutate());
		DrawableCompat.setTintList(wrapped, colorList);
		return wrapped;
	}
}
