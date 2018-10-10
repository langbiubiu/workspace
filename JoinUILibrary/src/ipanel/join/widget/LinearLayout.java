package ipanel.join.widget;

import ipanel.join.configuration.Bind;
import ipanel.join.configuration.View;

import org.json.JSONObject;

import android.content.Context;
import android.util.AttributeSet;
import android.util.Log;
import android.view.FocusFinder;
import android.view.KeyEvent;

public class LinearLayout extends android.widget.LinearLayout implements
		IConfigViewGroup {
	private View mData;
	public static final String PROP_GRAVITY = "gravity";

	/**
	 * 当焦点是第一个与最后一个时，false为不响应左右键<br/>
	 * 当LinearLayout为水平排列时有效。<br/>
	 */
	private Boolean firstLastBoolean = true;

	public LinearLayout(Context context, AttributeSet attrs) {
		super(context, attrs);
	}

	public LinearLayout(Context context) {
		super(context);
	}

	public LinearLayout(Context context, View data) {
		super(context);
		this.mData = data;
		Bind bind = data.getBindByName("orientation");
		if (bind != null) {
			if ("vertical".equalsIgnoreCase(bind.getValue().getvalue())) {
				setOrientation(VERTICAL);
			}
		}
		bind = data.getBindByName(PROP_GRAVITY);
		if(bind != null){
			setGravity(PropertyUtils.parseGravity(bind.getValue().getvalue()));
		}
		PropertyUtils.setCommonProperties(this, data);
	}

	public void setFirstLastBoolean(Boolean firstLastBoolean) {
		this.firstLastBoolean = firstLastBoolean;
	}

	@Override
	public android.view.ViewGroup.LayoutParams genConfLayoutParams(View data) {
		Bind bd = data.getBindByName(PROPERTY_LAYOUT_PARAMS);
		if (bd != null) {
			try {
				JSONObject jobj = bd.getValue().getJsonValue();
				LayoutParams lp = new LinearLayout.LayoutParams(
						PropertyUtils.getScaledSize(jobj.optInt("width",
								LayoutParams.WRAP_CONTENT)),
						PropertyUtils.getScaledSize(jobj.optInt("height",
								LayoutParams.WRAP_CONTENT)),
						(float) jobj.optDouble("weight"));
				lp.gravity = PropertyUtils.parseGravity(jobj
						.optString("gravity"));
				if (jobj.has("margin")) {
					JSONObject margin = jobj.getJSONObject("margin");
					lp.leftMargin = PropertyUtils.getScaledLeft(margin);
					lp.rightMargin = PropertyUtils.getScaledRight(margin);
					lp.topMargin = PropertyUtils.getScaledTop(margin);
					lp.bottomMargin = PropertyUtils.getScaledBottom(margin);
				}
				return lp;
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
		return generateDefaultLayoutParams();
	}

	@Override
	public View getViewData() {
		return mData;
	}

	@Override
	public void onAction(String type) {
		ActionUtils.handleAction(this, mData, type);

	}

	private boolean mShowFocusFrame = false;

	@Override
	public boolean showFocusFrame() {
		return mShowFocusFrame;
	}

	@Override
	public void setShowFocusFrame(boolean show) {
		mShowFocusFrame = show;
	}

	@Override
	public boolean dispatchKeyEvent(KeyEvent event) {
		boolean handled = false;
		if (event.getAction() == KeyEvent.ACTION_DOWN) {
			switch (event.getKeyCode()) {
			case KeyEvent.KEYCODE_DPAD_LEFT:
				handled = arrowScroll(FOCUS_LEFT);
				break;
			case KeyEvent.KEYCODE_DPAD_RIGHT:
				handled = arrowScroll(FOCUS_RIGHT);
				break;
			case KeyEvent.KEYCODE_DPAD_UP:
				// handled = arrowScroll(FOCUS_UP);
				break;
			case KeyEvent.KEYCODE_DPAD_DOWN:
				// handled = arrowScroll(FOCUS_DOWN);
				break;
			}
		}
		Log.v("LinearLayout", "dispatchKeyEvent()" + event.getKeyCode());
		if (handled)
			return true;
		return super.dispatchKeyEvent(event);
	}

	public boolean arrowScroll(int direction) {
		android.view.View currentFocused = findFocus();
		if (currentFocused == this) {
			currentFocused = null;
		}
		boolean handled = false;
		android.view.View nextFocused = FocusFinder.getInstance()
				.findNextFocus(this, currentFocused, direction);
		if (nextFocused == null) {
			handled = true;
		}
		Log.v("LinearLayout", "arrowScroll(" + direction + ")" + handled + ","
				+ firstLastBoolean);
		if (firstLastBoolean) {
			return false;
		} else {
			return handled;
		}
	}

}
