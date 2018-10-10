package cn.ipanel.android.widget;

import android.animation.TypeEvaluator;
import android.animation.ValueAnimator;
import android.annotation.SuppressLint;
import android.annotation.TargetApi;
import android.app.Activity;
import android.graphics.Rect;
import android.os.Build;
import android.view.View;
import android.view.ViewGroup.LayoutParams;
import android.view.ViewGroup.MarginLayoutParams;
import android.view.ViewParent;
import android.view.animation.Animation;
import android.view.animation.Transformation;
import android.widget.FrameLayout;
import android.widget.ImageView;

/**
 * Used to draw a custom overly over a target view
 * 
 * @author Zexu
 * 
 */
@TargetApi(Build.VERSION_CODES.ICE_CREAM_SANDWICH)
public class ViewFrameZoomIndicator2 implements IFrameIndicator {
	public final static String FOCUS_TAG="f";
	public final static float DEFAULT_SCALE=1.1f;
	public float scale=DEFAULT_SCALE;
	private ImageView mImg;
	private View last;


	public ViewFrameZoomIndicator2(Activity context) {
		mImg = new ImageView(context);
		mImg.setFocusable(false);
		mImg.setVisibility(View.INVISIBLE);
		context.addContentView(mImg, new LayoutParams(LayoutParams.WRAP_CONTENT,
				LayoutParams.WRAP_CONTENT));
	}

	public ViewFrameZoomIndicator2(FrameLayout frameLayout) {
		mImg = new ImageView(frameLayout.getContext());
		mImg.setFocusable(false);
		mImg.setVisibility(View.INVISIBLE);
		frameLayout.addView(mImg, new LayoutParams(LayoutParams.WRAP_CONTENT,
				LayoutParams.WRAP_CONTENT));
	}
	public ViewFrameZoomIndicator2(FrameLayout frameLayout,boolean layer) {
		mImg = new ImageView(frameLayout.getContext());
		mImg.setFocusable(false);
		mImg.setVisibility(View.INVISIBLE);
		if(layer){
			frameLayout.addView(mImg, 0,new LayoutParams(LayoutParams.WRAP_CONTENT,
					LayoutParams.WRAP_CONTENT));
		}else{
			frameLayout.addView(mImg, new LayoutParams(LayoutParams.WRAP_CONTENT,
					LayoutParams.WRAP_CONTENT));
		}

	}
	public void setFrameResouce(int resid) {
		mImg.setBackgroundResource(resid);
	}

	public ImageView getImageView() {
		return mImg;
	}
	
	public void setScale(float scale) {
		this.scale = scale;
	}

	/**
	 * 如果是 .9.png, 会自动设置padding, 调用此方法会覆盖.9.png的padding
	 * 
	 * @param left
	 * @param top
	 * @param right
	 * @param bottom
	 */
	public void setPadding(int left, int top, int right, int bottom) {
		mImg.setPadding(left, top, right, bottom);
	}

	public void setFrameColor(int color) {
		mImg.setBackgroundColor(color);
	}

	public void moveFrmaeTo(View v) {
		moveFrameTo(v, true, false);
	}

	private int getLeftInScrren(View v) {
		ViewParent vp = v.getParent();
		if (vp instanceof View && vp != mImg.getParent()) {
			return (int) (v.getLeft() - v.getScrollX() + getLeftInScrren((View) vp));
		}
		return v.getLeft() - v.getScrollX();
	}

	private int getTopInScrren(View v) {
		ViewParent vp = v.getParent();
		if (vp instanceof View && vp != mImg.getParent()) {
			return v.getTop() - v.getScrollY() + getTopInScrren((View) vp);
		}
		return v.getTop() - v.getScrollY();
	}

	private ValueAnimator mValueAnimator;
	private int mAnimationTime = 100;

	private Rect mMaxMoveRect;

	/**
	 * set animation time, default is 100ms
	 * 
	 * @param animationTime
	 */
	public void setAnimationTime(int animationTime) {
		this.mAnimationTime = animationTime;
	}

	/**
	 * Limit the maximum region the focus frame can move
	 * 
	 * @param rect
	 */
	public void setMaxMoveRect(Rect rect) {
		mMaxMoveRect = rect;
	}
	
	public void setMaxMoveRect(View view) {
		
	}

	/**
	 * 
	 * @return Maximum region the focus frame can move, null if no limit
	 */
	public Rect getMaxMoveRect() {
		return mMaxMoveRect;
	}

	@SuppressWarnings("unused")
	@SuppressLint("NewApi")
	@TargetApi(Build.VERSION_CODES.HONEYCOMB_MR1)
	public void moveFrameTo(View v, boolean animated, boolean hideFrame) {

		if (Build.VERSION.SDK_INT < Build.VERSION_CODES.ICE_CREAM_SANDWICH)
			return;
		View follower = mImg;
		View real=v.findViewWithTag(FOCUS_TAG);
		float pivotX=0.5f;
		float pivotY=0.5f;
		float height=Math.max(1.0f, v.getHeight());
		if(real!=null){
			pivotY=0.5f/(1.0f+(height-real.getHeight())/height);
		}

		if(last!=null){
			last.setPivotX(pivotX*last.getWidth());
			last.setPivotY(pivotY*last.getHeight());
			last.animate().setDuration(mAnimationTime).scaleX(1.0f);
			last.animate().setDuration(mAnimationTime).scaleY(1.0f);
		}
		v.bringToFront();
		v.requestLayout();
		v.setPivotX(pivotX*v.getWidth());
		v.setPivotY(pivotY*v.getHeight());
		v.animate().setDuration(mAnimationTime).scaleX(scale);
		v.animate().setDuration(mAnimationTime).scaleY(scale);
		last=v;
		if(real!=null){
			v=real;
		}
		float add_scale=(scale-1.0f)/2;
		if (v != null) {
			int[] xy = new int[2];
			// v.getLocationOnScreen(xy);
			xy[0] = getLeftInScrren(v);
			xy[1] = getTopInScrren(v);
			Rect r = new Rect();
			v.getFocusedRect(r);
			xy[0] += r.left;
			xy[1] += r.top;
			int w = getNearestValue(r.width()*scale);
			int h = getNearestValue(r.height()*scale);
			if (mMaxMoveRect != null) {
				xy[0] = Math.min(xy[0], mMaxMoveRect.right - w);
				xy[0] = Math.max(xy[0], mMaxMoveRect.left);

				xy[1] = Math.min(xy[1], mMaxMoveRect.bottom - h);
				xy[1] = Math.max(xy[1], mMaxMoveRect.top);
			}

			MarginLayoutParams start = (MarginLayoutParams) follower.getLayoutParams();

			FrameLayout.LayoutParams end = new FrameLayout.LayoutParams(w
					+ follower.getPaddingLeft() + follower.getPaddingRight(), h
					+ follower.getPaddingTop() + follower.getPaddingBottom());
			end.leftMargin = getNearestValue(0.0f+xy[0] - follower.getPaddingLeft()-add_scale*v.getWidth());
			end.topMargin = getNearestValue(0.0f+xy[1] - follower.getPaddingTop()-add_scale*v.getHeight());

			boolean wasVisible = follower.getVisibility() == View.VISIBLE;
			if (hideFrame) {
				follower.setVisibility(View.INVISIBLE);
			}
			if (animated && !hideFrame && wasVisible) {
				follower.setVisibility(View.VISIBLE);
				if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB) {
					if (mValueAnimator != null)
						mValueAnimator.end();
					mValueAnimator = ValueAnimator.ofObject(new LayoutEvaluator(follower), start,
							end).setDuration(mAnimationTime);
					mValueAnimator.start();
				} else {
					LayoutAnimation ani = new LayoutAnimation(follower, start, end);
					ani.setDuration(mAnimationTime);
					follower.setAnimation(ani);
				}
			} else {
				follower.setLayoutParams(end);
				if(!hideFrame)
					follower.setVisibility(View.VISIBLE);
				follower.setAlpha(0f);
				follower.animate().alpha(1f).setDuration(mAnimationTime).start();

			}
		} else {
			follower.setVisibility(View.INVISIBLE);
		}
	}
	
	private int getNearestValue(float value){
//		int number=(int) value;
//		float dot=value-number;
//		if(dot==0.0f){
//			return number;
//		}else{
//			return number+1;
//		}
		return Math.round(value);
	}

	class LayoutAnimation extends Animation {
		View follower;
		MarginLayoutParams startValue;
		MarginLayoutParams endValue;

		public LayoutAnimation(View v, MarginLayoutParams start, MarginLayoutParams end) {
			this.follower = v;
			this.startValue = start;
			this.endValue = end;
		}

		@Override
		protected void applyTransformation(float fraction, Transformation t) {
			int width = (int) (startValue.width + fraction * (endValue.width - startValue.width));
			int height = (int) (startValue.height + fraction
					* (endValue.height - startValue.height));

			FrameLayout.LayoutParams p = new FrameLayout.LayoutParams(width, height);
			p.leftMargin = (int) (startValue.leftMargin + fraction
					* (endValue.leftMargin - startValue.leftMargin));
			p.topMargin = (int) (startValue.topMargin + fraction
					* (endValue.topMargin - startValue.topMargin));
			follower.setLayoutParams(p);
		}

	}

	class LayoutEvaluator implements TypeEvaluator<MarginLayoutParams> {
		View follower;

		public LayoutEvaluator(View v) {
			this.follower = v;
		}

		@Override
		public MarginLayoutParams evaluate(float fraction, MarginLayoutParams startValue,
				MarginLayoutParams endValue) {
			int width = (int) (startValue.width + fraction * (endValue.width - startValue.width));
			int height = (int) (startValue.height + fraction
					* (endValue.height - startValue.height));

			FrameLayout.LayoutParams p = new FrameLayout.LayoutParams(width, height);
			p.leftMargin = (int) (startValue.leftMargin + fraction
					* (endValue.leftMargin - startValue.leftMargin));
			p.topMargin = (int) (startValue.topMargin + fraction
					* (endValue.topMargin - startValue.topMargin));
			follower.setLayoutParams(p);
			return p;
		}
	}

	
	@Override
	public void hideFrame() {
		mImg.setVisibility(View.INVISIBLE);
		mImg.clearAnimation();
		if(last!=null){
			last.animate().setDuration(mAnimationTime).scaleX(1.0f);
			last.animate().setDuration(mAnimationTime).scaleY(1.0f);
		}
		if (mValueAnimator != null)
			mValueAnimator.end();
	}


	float scaleAnimationX = 1f, scaleAnimationY = 1f;

	/**
	 * 缩放的倍数，1为原大小,1.1为放大10%
	 */
	@Override
	public void setScaleAnimationSize(float x, float y) {
		// TODO Auto-generated method stub
		scaleAnimationX = x;
		scaleAnimationY = y;
	}
}
