package cn.ipanel.android.widget;

import android.animation.ValueAnimator;
import android.annotation.TargetApi;
import android.app.Activity;
import android.graphics.Rect;
import android.os.Build;
import android.view.View;
import android.view.ViewGroup.LayoutParams;
import android.view.ViewParent;
import android.view.animation.Animation;
import android.view.animation.ScaleAnimation;
import android.widget.FrameLayout;
import android.widget.ImageView;
import cn.ipanel.android.Logger;

/**
 * Used to draw a custom overly over a target view
 * 
 * @author Zexu
 * 
 */
@TargetApi(Build.VERSION_CODES.HONEYCOMB)
public class ViewFrameZoomIndicator implements IFrameIndicator {
	private ImageView mImg;

	private View mLastView;

	public ViewFrameZoomIndicator(Activity context) {
		mImg = new ImageView(context);
		context.addContentView(mImg, new LayoutParams(LayoutParams.WRAP_CONTENT,
				LayoutParams.WRAP_CONTENT));
		mImg.setVisibility(View.INVISIBLE);
	}
	public ViewFrameZoomIndicator(FrameLayout frameLayout) {
		mImg = new ImageView(frameLayout.getContext());
		mImg.setFocusable(false);
		mImg.setVisibility(View.INVISIBLE);
		frameLayout.addView(mImg, new LayoutParams(LayoutParams.WRAP_CONTENT,
				LayoutParams.WRAP_CONTENT));
	}
	public void setFrameResouce(int resid) {
		mImg.setBackgroundResource(resid);
	}

	public ImageView getImageView() {
		return mImg;
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
		if (vp instanceof View) {
			return (int) (v.getLeft() - v.getScrollX() + getLeftInScrren((View) vp));
		}
		return v.getLeft() - v.getScrollX();
	}

	private int getTopInScrren(View v) {
		ViewParent vp = v.getParent();
		if (vp instanceof View) {
			return v.getTop() - v.getScrollY() + getTopInScrren((View) vp);
		}
		return v.getTop() - v.getScrollY();
	}

	private ValueAnimator mValueAnimator;
	private int mAnimationTime = 200;

	private Rect mMaxMoveRect;
	
	float mTargetScale = 1.1f;
	
	public void setTargetScale(float scale){
		this.mTargetScale=scale;
	}

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

	/**
	 * 
	 * @return Maximum region the focus frame can move, null if no limit
	 */
	public Rect getMaxMoveRect() {
		return mMaxMoveRect;
	}

	@TargetApi(Build.VERSION_CODES.HONEYCOMB)
	public void moveFrameTo(final View v, final boolean animated, final boolean hideFrame) {

		if (Build.VERSION.SDK_INT < Build.VERSION_CODES.ICE_CREAM_SANDWICH)
			return;
		View follower = mImg;

		if (v != null) {
			int[] xy = new int[2];
			// v.getLocationOnScreen(xy);
			xy[0] = getLeftInScrren(v);
			xy[1] = getTopInScrren(v);
			Rect r = new Rect();
			v.getFocusedRect(r);
			xy[0] += r.left;
			xy[1] += r.top;
			int w = r.width();
			int h = r.height();
			
			if(w == 0 && h== 0){
				Logger.d("focus view has a size of zero, try to schedule the move later, view = "+v);
				v.post(new Runnable() {
					
					@Override
					public void run() {
						moveFrameTo(v, animated, hideFrame);
						
					}
				});
				return;
			}

			if (mMaxMoveRect != null) {
				xy[0] = Math.min(xy[0], mMaxMoveRect.right - w);
				xy[0] = Math.max(xy[0], mMaxMoveRect.left);

				xy[1] = Math.min(xy[1], mMaxMoveRect.bottom - h);
				xy[1] = Math.max(xy[1], mMaxMoveRect.top);
			}
			Logger.d(String.format("follower: %d x %d, %f, %f", follower.getWidth(),
					follower.getHeight(), follower.getScaleX(), follower.getScaleY()));
			Logger.d(String.format("class: %s, x: %d, y: %d, w:%d,h:%d", v.getClass().getName(),
					xy[0], xy[1], w, h));
//			MarginLayoutParams start = (MarginLayoutParams) follower.getLayoutParams();

			FrameLayout.LayoutParams end = new FrameLayout.LayoutParams(w
					+ follower.getPaddingLeft() + follower.getPaddingRight(), h
					+ follower.getPaddingTop() + follower.getPaddingBottom());
			end.leftMargin = xy[0] - follower.getPaddingLeft();
			end.topMargin = xy[1] - follower.getPaddingTop();
//			boolean wasVisible = follower.getVisibility() == View.VISIBLE;
			
			if (hideFrame) {
				follower.setVisibility(View.INVISIBLE);
			} else {
				follower.setVisibility(View.VISIBLE);
			}
			if(v == mLastView && v.getAnimation() != null)
				return;
			follower.setLayoutParams(end);
			follower.clearAnimation();
			if (mLastView != null){
				ScaleAnimation animation = new ScaleAnimation(mTargetScale, 1f, mTargetScale, 1f,
						Animation.RELATIVE_TO_SELF, 0.5f, Animation.RELATIVE_TO_SELF, 0.5f);
				animation.setFillAfter(true);
				animation.setDuration(mAnimationTime);
				mLastView.startAnimation(animation);
				mLastView = null;
//				mLastView.clearAnimation();
			}
			if (animated && !hideFrame) {
				ScaleAnimation animation = new ScaleAnimation(1f, mTargetScale, 1f, mTargetScale,
						Animation.RELATIVE_TO_SELF, 0.5f, Animation.RELATIVE_TO_SELF, 0.5f);
				
				ScaleAnimation animation2 = new ScaleAnimation(1f,
						(mTargetScale * w + v.getPaddingLeft() + v.getPaddingRight())
								/ (w + v.getPaddingLeft() + v.getPaddingRight()), 1f, (mTargetScale * h
								+ v.getPaddingTop() + v.getPaddingBottom())
								/ (h + v.getPaddingTop() + v.getPaddingBottom()),
						Animation.ABSOLUTE, w / 2f + follower.getPaddingLeft(), Animation.ABSOLUTE, h / 2f
								+ follower.getPaddingTop());
				animation.setDuration(mAnimationTime);
				animation.setFillAfter(true);
				animation2.setDuration(mAnimationTime);
				animation2.setFillAfter(true);
				follower.startAnimation(animation2);
				v.bringToFront();
				v.startAnimation(animation);
				mLastView = v;
			}
		} else {
			follower.setVisibility(View.INVISIBLE);
			mLastView = null;
		}
	}

	@Override
	public void hideFrame() {
		mImg.setVisibility(View.INVISIBLE);
		mImg.clearAnimation();
		if (mValueAnimator != null)
			mValueAnimator.end();
		if(mLastView != null)
			mLastView.clearAnimation();
		mLastView = null;
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
	
	public void changeFoucsState(boolean focus){
		if(focus){
			mImg.setVisibility(View.VISIBLE);
			if (mLastView != null){
				ScaleAnimation animation = new ScaleAnimation(mTargetScale, 1f, mTargetScale, 1f,
						Animation.RELATIVE_TO_SELF, 0.5f, Animation.RELATIVE_TO_SELF, 0.5f);
				animation.setFillAfter(true);
				animation.setDuration(mAnimationTime);
				mLastView.startAnimation(animation);
				mLastView = null;
//				mLastView.clearAnimation();
			}
		}else{
			mImg.setVisibility(View.GONE);
			if (mLastView != null){
				ScaleAnimation animation = new ScaleAnimation(1f, mTargetScale, 1f, mTargetScale,
						Animation.RELATIVE_TO_SELF, 0.5f, Animation.RELATIVE_TO_SELF, 0.5f);
				animation.setFillAfter(true);
				animation.setDuration(mAnimationTime);
				mLastView.startAnimation(animation);
				mLastView = null;
//				mLastView.clearAnimation();
			}
		}
	}
}
