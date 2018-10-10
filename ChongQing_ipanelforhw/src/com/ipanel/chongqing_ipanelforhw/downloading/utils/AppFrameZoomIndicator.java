package com.ipanel.chongqing_ipanelforhw.downloading.utils;

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
import cn.ipanel.android.widget.IFrameIndicator;

public class AppFrameZoomIndicator implements IFrameIndicator {
	private ImageView mImg;
	private View mLastView;
	private ValueAnimator mValueAnimator;
	private int mAnimationTime = 50;
	private int mAnimationTime01 = 50;
	private int mAnimationTime02 = 50;
	private Rect mMaxMoveRect;
	private float mTargetScale = 1.06f;
	private float scaleAnimationX = 1f, scaleAnimationY = 1f;

	public AppFrameZoomIndicator(Activity context) {
		mImg = new ImageView(context);
		context.addContentView(mImg, new LayoutParams(
				LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT));
		mImg.setVisibility(View.INVISIBLE);
	}

	public AppFrameZoomIndicator(FrameLayout frameLayout) {
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

	public void setTargetScale(float scale) {
		this.mTargetScale = scale;
	}

	public void setAnimationTime(int animationTime) {
		this.mAnimationTime = animationTime;
	}

	public void setMaxMoveRect(Rect rect) {
		mMaxMoveRect = rect;
	}

	public Rect getMaxMoveRect() {
		return mMaxMoveRect;
	}

	@TargetApi(Build.VERSION_CODES.HONEYCOMB)
	public void moveFrameTo(View v, boolean animated, boolean hideFrame) {

		if (Build.VERSION.SDK_INT < Build.VERSION_CODES.ICE_CREAM_SANDWICH)
			return;
		View follower = mImg;

		if (v != null) {
			int[] xy = new int[2];
			xy[0] = getLeftInScrren(v);
			xy[1] = getTopInScrren(v);
			Rect r = new Rect();
			v.getFocusedRect(r);
			xy[0] += r.left;
			xy[1] += r.top;
			int w = r.width();
			int h = r.height();

			int vpl = v.getPaddingLeft();
			int vpr = v.getPaddingRight();
			int vpt = v.getPaddingTop();
			int vpb = v.getPaddingBottom();
			Logger.d("v: l:" + vpl + ", r:" + vpr + ", t:" + vpt + ", b:" + vpb);

			int fpl = follower.getPaddingLeft();
			int fpr = follower.getPaddingRight();
			int fpt = follower.getPaddingTop();
			int fpb = follower.getPaddingBottom();
			Logger.d("f: l:" + fpl + ", r:" + fpr + ", t:" + fpt + ", b:" + fpb);

			if (mMaxMoveRect != null) {
				xy[0] = Math.min(xy[0], mMaxMoveRect.right - w);
				xy[0] = Math.max(xy[0], mMaxMoveRect.left);

				xy[1] = Math.min(xy[1], mMaxMoveRect.bottom - h);
				xy[1] = Math.max(xy[1], mMaxMoveRect.top);
			}
			Logger.d("follower: " + follower.getWidth() + "x"
					+ follower.getHeight() + ", " + follower.getScaleX() + ", "
					+ follower.getScaleY());
			Logger.d("class: " + v.getClass().getName() + ", x: " + xy[0]
					+ ", y: " + xy[1] + ", w:" + w + ",h:" + h);

			FrameLayout.LayoutParams end = new FrameLayout.LayoutParams(w + fpl
					+ fpr, h + fpt + fpb);
			end.leftMargin = xy[0] - fpl;
			end.topMargin = xy[1] - fpt;

			if (hideFrame) {
				follower.setVisibility(View.INVISIBLE);
			} else {
				follower.setVisibility(View.VISIBLE);
			}
			Logger.d("v=" + v + ",mLastView=" + mLastView + ",v.getParent()="
					+ v.getParent() + ",v.getAnimation()=" + v.getAnimation());
			if (v == mLastView && v.getAnimation() != null) {
				return;
			}
			follower.setLayoutParams(end);
			follower.clearAnimation();
			if (mLastView != null) {
				ScaleAnimation animation = new ScaleAnimation(mTargetScale, 1f,
						mTargetScale, 1f, Animation.RELATIVE_TO_SELF, 0.5f,
						Animation.RELATIVE_TO_SELF, 0.5f);
				animation.setFillAfter(true);
				animation.setDuration(mAnimationTime01);
				mLastView.startAnimation(animation);
				mLastView = null;
			}

			if (animated && !hideFrame) {
				ScaleAnimation animation = new ScaleAnimation(1f, mTargetScale,
						1f, mTargetScale, Animation.RELATIVE_TO_SELF, 0.5f,
						Animation.RELATIVE_TO_SELF, 0.5f);

				ScaleAnimation animation2 = new ScaleAnimation(1f,
						(mTargetScale * w + vpl + vpr) / (w + vpl + vpr), 1f,
						(mTargetScale * h + vpt + vpb) / (h + vpt + vpb),
						Animation.ABSOLUTE, w / 2f + fpl, Animation.ABSOLUTE, h
								/ 2f + fpt);
				animation.setDuration(mAnimationTime);
				animation.setFillAfter(true);
				animation2.setDuration(mAnimationTime02);
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
		if (mLastView != null)
			mLastView.clearAnimation();
		mLastView = null;
	}

	@Override
	public void setScaleAnimationSize(float x, float y) {
		scaleAnimationX = x;
		scaleAnimationY = y;
		Logger.d("scaleAnimationX=" + scaleAnimationX + ",scaleAnimationY="
				+ scaleAnimationY);
	}

	public void changeFoucsState(boolean focus) {
		if (focus) {
			mImg.setVisibility(View.VISIBLE);
			if (mLastView != null) {
				ScaleAnimation animation = new ScaleAnimation(mTargetScale, 1f,
						mTargetScale, 1f, Animation.RELATIVE_TO_SELF, 0.5f,
						Animation.RELATIVE_TO_SELF, 0.5f);
				animation.setFillAfter(true);
				animation.setDuration(mAnimationTime);
				mLastView.startAnimation(animation);
				mLastView = null;
			}
		} else {
			mImg.setVisibility(View.GONE);
			if (mLastView != null) {
				ScaleAnimation animation = new ScaleAnimation(1f, mTargetScale,
						1f, mTargetScale, Animation.RELATIVE_TO_SELF, 0.5f,
						Animation.RELATIVE_TO_SELF, 0.5f);
				animation.setFillAfter(true);
				animation.setDuration(mAnimationTime);
				mLastView.startAnimation(animation);
				mLastView = null;
			}
		}
	}

}
