package cn.ipanel.android.widget;

import ipanel.join.widget.DarkImgView;
import ipanel.join.widget.ImgView;
import android.animation.TypeEvaluator;
import android.animation.ValueAnimator;
import android.annotation.TargetApi;
import android.app.Activity;
import android.graphics.ColorFilter;
import android.graphics.Rect;
import android.os.Build;
import android.view.View;
import android.view.ViewGroup.LayoutParams;
import android.view.ViewGroup.MarginLayoutParams;
import android.view.ViewParent;
import android.view.animation.Animation;
import android.view.animation.Transformation;
import android.widget.FrameLayout;
import android.widget.ImageSwitcher;
import android.widget.ImageView;
import cn.ipanel.android.Logger;
import cn.ipanel.android.util.ColorFilterGenerator;

/**
 * Used to draw a custom overly over a target view
 * 
 * @author Zexu
 * 
 */
@TargetApi(Build.VERSION_CODES.HONEYCOMB)
public class ViewFrameIndicator2 implements IFrameIndicator {
	private ImageView mImg;

	public ViewFrameIndicator2(Activity context) {
		mImg = new ImageView(context);
		context.addContentView(mImg, new LayoutParams(
				LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT));
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
	private int mAnimationTime = 250;

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

	/**
	 * 
	 * @return Maximum region the focus frame can move, null if no limit
	 */
	public Rect getMaxMoveRect() {
		return mMaxMoveRect;
	}

	float scaleAnimationX = 1f, scaleAnimationY = 1f;

	/**
	 * 缩放的倍数，1为原大小,1.1为放大10%
	 */
	public void setScaleAnimationSize(float x, float y) {
		scaleAnimationX = x;
		scaleAnimationY = y;
		Logger.d("setScaleAnimationSize("+x+","+y+")");
	}

	MarginLayoutParams lastEnd;

	View mLastItem;

	@TargetApi(Build.VERSION_CODES.ICE_CREAM_SANDWICH)
	public synchronized void moveFrameTo(View v, boolean animated,
			boolean hideFrame) {

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

			if (mMaxMoveRect != null) {
				xy[0] = Math.min(xy[0], mMaxMoveRect.right - w);
				xy[0] = Math.max(xy[0], mMaxMoveRect.left);

				xy[1] = Math.min(xy[1], mMaxMoveRect.bottom - h);
				xy[1] = Math.max(xy[1], mMaxMoveRect.top);
			}

			Logger.d(String.format("follower: %d x %d, %f, %f",
					follower.getWidth(), follower.getHeight(),
					follower.getScaleX(), follower.getScaleY()));
			Logger.d(String.format("class: %s, x: %d, y: %d, w:%d,h:%d", v
					.getClass().getName(), xy[0], xy[1], w, h));
			MarginLayoutParams start = (MarginLayoutParams) follower
					.getLayoutParams();
			FrameLayout.LayoutParams end = new FrameLayout.LayoutParams(w
					+ follower.getPaddingLeft() + follower.getPaddingRight(), h
					+ follower.getPaddingTop() + follower.getPaddingBottom());
			end.leftMargin = xy[0] - follower.getPaddingLeft();
			end.topMargin = xy[1] - follower.getPaddingTop();
			boolean wasVisible = follower.getVisibility() == View.VISIBLE;
			follower.setAlpha(1f);

			if (hideFrame) {
				follower.setVisibility(View.INVISIBLE);
			} else {
				follower.setVisibility(View.VISIBLE);
			}

			Logger.d(String.format(
					"End layout left=%d,top=%d,width=%d,height=%d",
					end.leftMargin, end.topMargin, end.width, end.height));

			// 有动画效果
			Logger.d("2====>" + v.getAnimation());
			if (scaleAnimationX != 1 || scaleAnimationY != 1) {
				end.width += v.getWidth() * (scaleAnimationX - 1);
				end.height += v.getWidth() * (scaleAnimationY - 1);
				end.leftMargin -= (v.getWidth() * (scaleAnimationX - 1) / 2);
				end.topMargin -= (v.getWidth() * (scaleAnimationY - 1) / 2);
			}

			Logger.d(String.format(
					"End layout left=%d,top=%d,width=%d,height=%d",
					end.leftMargin, end.topMargin, end.width, end.height));
			if (lastEnd != null)
				Logger.d(String.format(
						"LastEnd layout left=%d,top=%d,width=%d,height=%d",
						lastEnd.leftMargin, lastEnd.topMargin, lastEnd.width,
						lastEnd.height));
			if (lastEnd != null && lastEnd.leftMargin == end.leftMargin
					&& lastEnd.topMargin == end.topMargin
					&& lastEnd.width == end.width
					&& lastEnd.height == end.height)
				return;
			// Logger.d("start animation animated="+animated+", hideFrame="+hideFrame+", wasVisisble"+wasVisible);
			lastEnd = end;
			if (mLastItem != null && mLastItem != v)
				mLastItem.setActivated(false);
			if (animated && !hideFrame && wasVisible) {
				if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB) {
					if (mValueAnimator != null) {
						mValueAnimator.end();
						if (mLastItem != null) {
							mLastItem.clearAnimation();
							mLastItem.animate().cancel();
							mLastItem.setScaleX(1f);
							mLastItem.setScaleY(1f);
						}
						follower.setScaleX(1f);
						follower.setScaleY(1f);
						follower.clearAnimation();
					}
					mValueAnimator = ValueAnimator.ofObject(
							new LayoutEvaluator(follower, v), start, end)
							.setDuration(mAnimationTime);
					mValueAnimator.start();
					mLastItem = v;
				} else {
					LayoutAnimation ani = new LayoutAnimation(follower, start,
							end);
					ani.setDuration(mAnimationTime);
					follower.setAnimation(ani);
				}
			} else {
				v.setActivated(true);
				follower.setLayoutParams(end);
				follower.setAlpha(0f);
				follower.animate().alpha(1f).setDuration(mAnimationTime)
						.start();
			}
		} else {
			follower.setVisibility(View.INVISIBLE);
		}
	}

	class LayoutAnimation extends Animation {
		View follower;
		MarginLayoutParams startValue;
		MarginLayoutParams endValue;

		public LayoutAnimation(View v, MarginLayoutParams start,
				MarginLayoutParams end) {
			this.follower = v;
			this.startValue = start;
			this.endValue = end;
		}

		@Override
		protected void applyTransformation(float fraction, Transformation t) {
			int width = (int) (startValue.width + fraction
					* (endValue.width - startValue.width));
			int height = (int) (startValue.height + fraction
					* (endValue.height - startValue.height));

			FrameLayout.LayoutParams p = new FrameLayout.LayoutParams(width,
					height);
			p.leftMargin = (int) (startValue.leftMargin + fraction
					* (endValue.leftMargin - startValue.leftMargin));
			p.topMargin = (int) (startValue.topMargin + fraction
					* (endValue.topMargin - startValue.topMargin));
			follower.setLayoutParams(p);
		}

	}

	@TargetApi(Build.VERSION_CODES.HONEYCOMB)
	class LayoutEvaluator implements TypeEvaluator<MarginLayoutParams> {
		View follower, item;

		public LayoutEvaluator(View v, View item) {
			this.follower = v;
			this.item = item;
		}

		@Override
		public MarginLayoutParams evaluate(float fraction,
				MarginLayoutParams startValue, MarginLayoutParams endValue) {
			if (fraction <= 0.6f) {
				fraction = fraction / 0.6f;
				float scale = 1 - 0.2f * fraction;
				int width = (int) (startValue.width + fraction
						* (endValue.width - startValue.width));
				int height = (int) (startValue.height + fraction
						* (endValue.height - startValue.height));

				FrameLayout.LayoutParams p = new FrameLayout.LayoutParams(
						width, height);
				p.leftMargin = (int) (startValue.leftMargin + fraction
						* (endValue.leftMargin - startValue.leftMargin));
				p.topMargin = (int) (startValue.topMargin + fraction
						* (endValue.topMargin - startValue.topMargin));
				follower.setScaleX(scale);
				follower.setScaleY(scale);
				follower.setLayoutParams(p);
				if (item instanceof DarkImgView) {
					((DarkImgView) item).setFadeFraction(1 - fraction
							* fraction);
				}
				return p;
			} else if (fraction <= 0.85f) {
				follower.setLayoutParams(endValue);
				float scale = 0.8f + 0.3f * (fraction - 0.6f) / 0.25f;
				follower.setScaleX(scale);
				item.setScaleX(scale);
				follower.setScaleY(scale);
				item.setScaleY(scale);

				int brightness = (int) (50 * (fraction - 0.6f) / 0.25f);
				ColorFilter cf = ColorFilterGenerator.adjustColor(brightness,
						0, 0, 0);
				if (item instanceof ImgView) {

					((ImgView) item).setColorFilter(cf);
				} else if (item.getBackground() != null) {
					item.getBackground().setColorFilter(cf);
				}
				item.setActivated(true);

			} else {
				follower.setLayoutParams(endValue);
				float scale = 1.1f - 0.1f * (fraction - 0.85f) / 0.15f;
				follower.setScaleX(scale);
				item.setScaleX(scale);
				follower.setScaleY(scale);
				item.setScaleY(scale);

				int brightness = (int) (50 * (1 - (fraction - 0.85f) / 0.15f));
				ColorFilter cf = ColorFilterGenerator.adjustColor(brightness,
						0, 0, 0);
				if (item instanceof ImageView) {
					((ImageView) item).setColorFilter(cf);
				} else if (item instanceof ImageSwitcher) {
					View v = ((ImageSwitcher) item).getCurrentView();
					if (v instanceof ImageView) {
						((ImageView) v).setColorFilter(cf);
					} else if (v.getBackground() != null) {
						item.getBackground().setColorFilter(cf);
					}
				} else if (item.getBackground() != null) {
					item.getBackground().setColorFilter(cf);
				}
			}
			return endValue;
		}
	}

	@Override
	public void hideFrame() {
		mImg.setVisibility(View.INVISIBLE);
		mImg.clearAnimation();
		if (mValueAnimator != null)
			mValueAnimator.end();
		if (mLastItem != null) {
			mLastItem.clearAnimation();
			mLastItem.setScaleX(1f);
			mLastItem.setScaleY(1f);
		}
	}

}
