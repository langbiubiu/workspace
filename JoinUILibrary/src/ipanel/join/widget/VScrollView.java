package ipanel.join.widget;

import ipanel.join.configuration.Bind;
import ipanel.join.configuration.ConfigState;
import ipanel.join.configuration.View;

import org.json.JSONObject;

import android.content.Context;
import android.graphics.Rect;
import android.util.AttributeSet;
import android.util.Log;
import android.widget.ScrollView;

public class VScrollView extends ScrollView implements IConfigViewGroup {

	private String TAG = "VScrollView";
	private View mData;
	private static final int MIN_EDGE_TOP = 0;
	private static final int MIN_EDGE_BOTTOM = 0;
	private int offsetTop = MIN_EDGE_TOP;
	private int offsetBottom = MIN_EDGE_BOTTOM;

	public VScrollView(Context context){
		super(context);		
	}

	public VScrollView(Context context, AttributeSet attrs) {
		super(context);	
	}
	
	public VScrollView(Context context, View data) {
		super(context);
		this.mData = data;
		this.setVerticalScrollBarEnabled(false);
		PropertyUtils.setCommonProperties(this, data);
		//上边距
		Bind bd = data.getBindByName("offsetTop");
		if (bd != null) {
			offsetTop = PropertyUtils.getScaledSize(Integer.parseInt(bd
					.getValue().getvalue()));
		} else {
			offsetTop = (int) (MIN_EDGE_TOP * ConfigState.getInstance()
					.getConfiguration().getScale());
		}
		//下边距
		bd = data.getBindByName("offsetBottom");
		if (bd != null) {
			offsetBottom = PropertyUtils.getScaledSize(Integer.parseInt(bd
					.getValue().getvalue()));
		} else {
			offsetBottom = (int) (MIN_EDGE_BOTTOM * ConfigState.getInstance()
					.getConfiguration().getScale());
		}
	}

	public void setEdgeOffset(int offsetTop, int offsetBottom) {
		this.offsetTop = offsetTop;
		this.offsetBottom = offsetBottom;
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

	/**
	 * 计算X方向滚动的总合，以便在屏幕上显示子视图的完整矩形。
	 */
	@Override
	protected int computeScrollDeltaToGetChildRectOnScreen(Rect rect) {
		if (getChildCount() == 0)
			return 0;
		int height = getHeight();
		int screenTop = getScrollY();
		int screenBottom = screenTop + height;
		int fadingEdge = getHorizontalFadingEdgeLength();
		if (rect.top > 0) {
			screenTop += fadingEdge;
		}
		if (rect.bottom < getChildAt(0).getHeight()) {
			screenBottom -= fadingEdge;
		}
		int scrollXDelta = 0;
		if (rect.bottom + offsetBottom > screenBottom && rect.top > screenTop) {
			if (rect.height() > height) {
				scrollXDelta += (rect.top - screenTop);
			} else {
				scrollXDelta += (rect.bottom + offsetBottom - screenBottom);
			}
			int bottom = getChildAt(0).getBottom();
			int distanceToBottom = bottom - screenBottom;
			scrollXDelta = Math.min(scrollXDelta, distanceToBottom);
		} else if (rect.top - offsetTop < screenTop
				&& rect.bottom < screenBottom) {
			if (rect.height() > height) {
				scrollXDelta -= (screenBottom - rect.bottom);
			} else {
				scrollXDelta -= (screenTop - rect.top + offsetTop);
			}
			scrollXDelta = Math.max(scrollXDelta, -getScrollY());
		}
		Log.v(TAG, screenTop+"|"+screenBottom+"|"+rect.top+"|"+rect.bottom+"|"+scrollXDelta);		
		return scrollXDelta;
	}

	@Override
	protected float getBottomFadingEdgeStrength () {
		return (float) 0.0;
	}

	@Override
	protected float getTopFadingEdgeStrength () {
		return (float) 0.0;
	}

}
