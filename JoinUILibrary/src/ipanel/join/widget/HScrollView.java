package ipanel.join.widget;

import ipanel.join.configuration.Bind;
import ipanel.join.configuration.ConfigState;
import ipanel.join.configuration.View;

import java.util.HashMap;

import org.json.JSONException;
import org.json.JSONObject;

import android.content.Context;
import android.graphics.Rect;
import android.os.Handler;
import android.util.AttributeSet;
import android.util.Log;
import android.view.ViewGroup;
import android.widget.HorizontalScrollView;

public class HScrollView extends HorizontalScrollView implements
		IConfigViewGroup {

	private View mData;
	private String TAG = "HScrollView";
	private static final int MIN_EDGE = 50;

	private int offset = MIN_EDGE;
	private int FBPadding = 0;

	/**
	 * HScrollView布局的孙子控件的个数，因为HScrollView只有一个子控件。
	 */
	private int ChildCount = 0;
	/**
	 * HScrollView布局的孙子控件的位置<br/>
	 * 可能过程中会将某个孙子控件放在最前端显示从而打乱布局顺序，记录位置以便判断是第几个孙子时用。
	 */
	private HashMap<Integer, JSONObject> rectHashMap = new HashMap<Integer, JSONObject>();

	public HScrollView(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);
		// TODO Auto-generated constructor stub
	}

	public HScrollView(Context context, AttributeSet attrs) {
		super(context, attrs);
		// TODO Auto-generated constructor stub
	}

	public HScrollView(Context context) {
		super(context);
		// TODO Auto-generated constructor stub
	}

	public void setEdgeOffset(int offset) {
		this.offset = offset;
	}

	/**
	 * 设置偏移距离，第一个孙子控件距离左边或最后一个孙子控件距离右边的间距。为0则不处理。
	 * 
	 * @param FBPadding
	 */
	public void setFBPadding(int FBPadding) {
		this.FBPadding = FBPadding;
	}

	/**
	 * 设置孙子控件的默认位置
	 * 
	 * @param view
	 */
	public void setAppIndex(android.view.View view) {
		ChildCount = ((ViewGroup) view).getChildCount();
		for (int i = 0; i < ChildCount; i++) {
			android.view.View v = ((ViewGroup) view).getChildAt(i);
			if (v == null) {
				continue;
			}
			Rect rect = new Rect();
			v.getHitRect(rect);
			try {
				JSONObject o = new JSONObject();
				o.put("index", i);
				o.put("rect", rect);
				rectHashMap.put(v.getId(), o);
			} catch (JSONException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
	}

	/**
	 * 设置孙子控件的默认位置
	 * 
	 * @param rectHashMap
	 */
	public void setAppIndexRect(HashMap<Integer, JSONObject> rectHashMap) {
		this.rectHashMap = rectHashMap;
	}

	public HScrollView(Context context, View data) {
		super(context);
		this.mData = data;
		this.setHorizontalScrollBarEnabled(false);
		PropertyUtils.setCommonProperties(this, data);

		Bind bd = data.getBindByName("offset");
		if (bd != null) {
			offset = PropertyUtils.getScaledSize(Integer.parseInt(bd.getValue()
					.getvalue()));
		} else {
			offset = (int) (MIN_EDGE * ConfigState.getInstance()
					.getConfiguration().getScale());
		}
		/**
		 * 前后的间距，即第一个与最后一个时显示边距
		 */
		bd = data.getBindByName("FBPadding");
		if (bd != null) {
			FBPadding = PropertyUtils.getScaledSize(Integer.parseInt(bd
					.getValue().getvalue()));
		} else {
			/**
			 * Author:zyl <br/>
			 * Time:2014-06-06<br/>
			 * Reason:改动后重庆HOME的界面显示异常，恢复默认的逻辑
			 * **/
			FBPadding = (int) (MIN_EDGE * ConfigState.getInstance()
					.getConfiguration().getScale());

			// FBPadding = 0;
		}
	}

	@Override
	protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
		super.onMeasure(widthMeasureSpec, heightMeasureSpec);

		if (ChildCount == 0) {
			android.view.View view = getChildAt(0);
			if (view != null) {
				setAppIndex(view);
			}
		}
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

	Handler mHandler = new Handler();
	Runnable mFrameTask = new Runnable() {

		@Override
		public void run() {
			if (ConfigState.getInstance() == null) {
				return;
			}
			if (ConfigState.getInstance().getFrameListener() == null) {
				return;
			}
			ConfigState.getInstance().getFrameListener().updateFrame();
		}
	};

	@Override
	protected void onScrollChanged(int x, int y, int oldX, int oldY) {
		super.onScrollChanged(x, y, oldX, oldY);
		mHandler.removeCallbacks(mFrameTask);
		mHandler.postDelayed(mFrameTask, 100);
	}

	@Override
	protected int computeScrollDeltaToGetChildRectOnScreen(Rect rect) {
		if (getChildCount() == 0)
			return 0;

		int width = getWidth();
		int screenLeft = getScrollX();
		int screenRight = screenLeft + width;

		int fadingEdge = getHorizontalFadingEdgeLength();

		// leave room for left fading edge as long as rect isn't at very left
		if (rect.left > 0) {
			screenLeft += fadingEdge;
		}

		// leave room for right fading edge as long as rect isn't at very right
		if (rect.right < getChildAt(0).getWidth()) {
			screenRight -= fadingEdge;
		}

		int scrollXDelta = 0;

		if (rect.right + offset > screenRight && rect.left > screenLeft) {
			// need to move right to get it in view: move right just enough so
			// that the entire rectangle is in view (or at least the first
			// screen size chunk).

			if (rect.width() > width) {
				// just enough to get screen size chunk on
				scrollXDelta += (rect.left - screenLeft);
			} else {
				// get entire rect at right of screen
				scrollXDelta += (rect.right + offset - screenRight);
			}

			// make sure we aren't scrolling beyond the end of our content
			int right = getChildAt(0).getRight();
			int distanceToRight = right - screenRight;
			scrollXDelta = Math.min(scrollXDelta, distanceToRight);

		} else if (rect.left - offset < screenLeft && rect.right < screenRight) {
			// need to move right to get it in view: move right just enough so
			// that
			// entire rectangle is in view (or at least the first screen
			// size chunk of it).

			if (rect.width() > width) {
				// screen size chunk
				scrollXDelta -= (screenRight - rect.right);
			} else {
				// entire rect at left
				scrollXDelta -= (screenLeft - rect.left + offset);
			}

			// make sure we aren't scrolling any further than the left our
			// content
			scrollXDelta = Math.max(scrollXDelta, -getScrollX());
		}
		scrollXDelta = updateScrollXDelta(scrollXDelta, rect);
		return scrollXDelta;
	}

	/**
	 * 偏移度。默认第一个距离左边的间距为FBPadding。<br/>
	 * 移到最后一个时显示右边间距FBPadding。<br/>
	 * 当在屏幕内需要滚动时则无FBPadding。
	 * 
	 * @param scrollXDelta
	 * @param rect
	 * @return
	 */
	private int updateScrollXDelta(int scrollXDelta, Rect rect) {
		Log.v(TAG, "FBPadding:" + FBPadding + ",scrollXDelta:" + scrollXDelta
				+ ",getWidth()" + this.getWidth());
		if (FBPadding != 0) {
			int scrollWidth = this.getWidth();
			android.view.View view = getChildAt(0);
			int width = view.getWidth();
			if (width + FBPadding > scrollWidth) {
				Rect leftRect = new Rect();
				Rect rightRect = new Rect();
				String c = String.valueOf(ChildCount - 1);
				for (int i = 0; i < ChildCount; i++) {
					android.view.View v = ((ViewGroup) view).getChildAt(i);
					int id = v.getId();
					if (rectHashMap.get(id) != null) {
						try {
							JSONObject o = rectHashMap.get(id);
							if ("0".equals(o.get("index").toString())) {
								// leftRect = (Rect) o.get("rect");
								v.getHitRect(leftRect);
							} else if (c.equals(o.get("index").toString())) {
								// rightRect = (Rect) o.get("rect");
								v.getHitRect(rightRect);
							}
						} catch (JSONException e) {
							// TODO Auto-generated catch block
							e.printStackTrace();
						}
					}
				}

				Log.v(TAG, "ChildCount:" + ChildCount + ",leftRect.left:"
						+ leftRect.left + ",leftRect.right:" + leftRect.right
						+ ",rect.left:" + rect.left + ",rect.right:"
						+ rect.right + ",rightRect.left:" + rightRect.left
						+ ",rightRect.right:" + rightRect.right + ",rect.left:"
						+ rect.left + ",rect.right:" + rect.right);

				if (rect.right == rightRect.right) {
					scrollXDelta += FBPadding;
				} else if (rect.left == leftRect.left) {
					scrollXDelta -= FBPadding;
				}
			}
		}
		return scrollXDelta;
	}

	@Override
	protected float getLeftFadingEdgeStrength() {
		return (float) 0.0;
	}

	@Override
	protected float getRightFadingEdgeStrength() {
		return (float) 0.0;
	}

}
