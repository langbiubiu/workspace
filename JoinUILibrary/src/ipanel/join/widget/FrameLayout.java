package ipanel.join.widget;

import ipanel.join.configuration.Bind;
import ipanel.join.configuration.View;

import org.json.JSONObject;

import android.content.Context;
import android.util.AttributeSet;

public class FrameLayout extends android.widget.FrameLayout implements
		IConfigViewGroup {
	private View mData;

	public FrameLayout(Context context, AttributeSet attrs) {
		super(context, attrs);
		// TODO Auto-generated constructor stub
	}

	public FrameLayout(Context context) {
		super(context);
		// TODO Auto-generated constructor stub
	}

	public FrameLayout(Context context, View data) {
		super(context);
		this.mData = data;
		PropertyUtils.setCommonProperties(this, data);
	}

	@Override
	public android.view.ViewGroup.LayoutParams genConfLayoutParams(View data) {
		Bind bd = data.getBindByName(PROPERTY_LAYOUT_PARAMS);
		if (bd != null) {
			try {
				JSONObject jobj = bd.getValue().getJsonValue();
				LayoutParams lp = new FrameLayout.LayoutParams(
						PropertyUtils.getScaledSize(jobj.optInt("width",
								LayoutParams.WRAP_CONTENT)),
						PropertyUtils.getScaledSize(jobj.optInt("height",
								LayoutParams.WRAP_CONTENT)));
				lp.gravity = PropertyUtils.parseGravity(jobj
						.optString("gravity"));
				if(jobj.has("margin")){
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
}
