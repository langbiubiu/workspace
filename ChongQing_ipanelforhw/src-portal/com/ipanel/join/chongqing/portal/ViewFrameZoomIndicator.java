package com.ipanel.join.chongqing.portal;

import cn.ipanel.android.Logger;
import cn.ipanel.android.widget.IFrameIndicator;
import android.animation.ValueAnimator;
import android.annotation.TargetApi;
import android.app.Activity;
import android.graphics.Rect;
import android.os.Build;
import android.view.View;
import android.view.ViewGroup.LayoutParams;
import android.view.ViewGroup.MarginLayoutParams;
import android.view.ViewParent;
import android.view.animation.Animation;
import android.view.animation.Animation.AnimationListener;
import android.view.animation.AnimationSet;
import android.view.animation.LinearInterpolator;
import android.view.animation.ScaleAnimation;
import android.view.animation.Transformation;
import android.widget.FrameLayout;
import android.widget.ImageView;

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
	private int mAnimationTime = 100;

	private Rect mMaxMoveRect;
	
	float mTargetScale = 1.05f;

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
	public void moveFrameTo(final View v, boolean animated, boolean hideFrame) {

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
			
			boolean skipFrameAnimation = false;
			if (xy[0] <= 0 || xy[0] + w >= v.getResources().getDisplayMetrics().widthPixels) {
				skipFrameAnimation = true;
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
			MarginLayoutParams start = (MarginLayoutParams) follower.getLayoutParams();

			FrameLayout.LayoutParams end = new FrameLayout.LayoutParams(w
					+ follower.getPaddingLeft() + follower.getPaddingRight(), h
					+ follower.getPaddingTop() + follower.getPaddingBottom());
			end.leftMargin = xy[0] - follower.getPaddingLeft();
			end.topMargin = xy[1] - follower.getPaddingTop();
			boolean wasVisible = follower.getVisibility() == View.VISIBLE;
			
			if (end.leftMargin == start.leftMargin && end.topMargin == start.topMargin
					&& end.width == start.width && end.height == start.height)
				skipFrameAnimation = true;
			
			if (hideFrame) {
				follower.setVisibility(View.INVISIBLE);
			} else {
				follower.setVisibility(View.VISIBLE);
			}
			if(v == mLastView && v.getAnimation() != null)
				return;
			if(!wasVisible){
				follower.setLayoutParams(end);
			}
			
			if(!skipFrameAnimation)
				follower.clearAnimation();
			if (mLastView != null){
				ScaleAnimation animation = new ScaleAnimation(mTargetScale, 1f, mTargetScale, 1f,
						Animation.RELATIVE_TO_SELF, 0.5f, Animation.RELATIVE_TO_SELF, 0.5f);
				animation.setFillAfter(true);
				animation.setDuration(mAnimationTime);
				mLastView.startAnimation(animation);
				mLastView = null;
			}
			if (animated && !hideFrame) {
				final ScaleAnimation animation = new ScaleAnimation(1f, mTargetScale, 1f, mTargetScale,
						Animation.RELATIVE_TO_SELF, 0.5f, Animation.RELATIVE_TO_SELF, 0.5f);
				
				ScaleAnimation animation2 = new ScaleAnimation(1f,(mTargetScale * w + v.getPaddingLeft() + v.getPaddingRight())/ (w + v.getPaddingLeft() + v.getPaddingRight()),
						1f,(mTargetScale * h+ v.getPaddingTop() + v.getPaddingBottom())/ (h + v.getPaddingTop() + v.getPaddingBottom()),
						Animation.ABSOLUTE, w / 2f + follower.getPaddingLeft(), 
						Animation.ABSOLUTE, h / 2f+ follower.getPaddingTop());
				LayoutAnimation ani = new LayoutAnimation(follower, start, end);
				ani.setDuration(mAnimationTime);
				animation.setDuration(mAnimationTime);
				animation2.setDuration(mAnimationTime);
				animation.setFillAfter(true);
				animation2.setFillAfter(true);	
				ani.setFillAfter(true);	
				//follower.startAnimation(animation2);
				AnimationSet set=new AnimationSet(true);
				if(wasVisible){
					set.addAnimation(ani);
				}
				set.addAnimation(animation2);
				set.setFillAfter(true);
				if(!skipFrameAnimation)
					follower.startAnimation(set);
				set.setAnimationListener(new AnimationListener() {
					@Override
					public void onAnimationStart(Animation a) {
					}
					@Override
					public void onAnimationRepeat(Animation a) {
					}
					@Override
					public void onAnimationEnd(Animation a) {
						
					}
				});
				v.bringToFront();
				v.startAnimation(animation);
				mLastView = v;
				
			}
		} else {
			follower.setVisibility(View.INVISIBLE);
			mLastView = null;
		}
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
			int width = (int) (startValue.width + fraction* (endValue.width - startValue.width));
			int height = (int) (startValue.height + fraction* (endValue.height - startValue.height));

			FrameLayout.LayoutParams p = new FrameLayout.LayoutParams(width, height);
			p.leftMargin = (int) (startValue.leftMargin + fraction* (endValue.leftMargin - startValue.leftMargin));
			p.topMargin = (int) (startValue.topMargin + fraction* (endValue.topMargin - startValue.topMargin));
			follower.setLayoutParams(p);
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

	@Override
	public void setScaleAnimationSize(float x, float y) {
		// TODO Auto-generated method stub
		
	}

}
