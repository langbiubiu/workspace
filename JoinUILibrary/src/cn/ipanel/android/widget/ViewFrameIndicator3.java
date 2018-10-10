package cn.ipanel.android.widget;

import ipanel.join.widget.DarkImgView;
import ipanel.join.widget.ImgView;
import android.animation.TypeEvaluator;
import android.animation.ValueAnimator;
import android.annotation.TargetApi;
import android.app.Activity;
import android.graphics.Bitmap;
import android.graphics.Rect;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.view.View;
import android.view.ViewGroup.LayoutParams;
import android.view.ViewGroup.MarginLayoutParams;
import android.view.ViewParent;
import android.view.animation.Animation;
import android.view.animation.ScaleAnimation;
import android.view.animation.Transformation;
import android.widget.FrameLayout;
import android.widget.ImageSwitcher;
import android.widget.ImageView;
import cn.ipanel.android.Logger;

/**
 * 
 * 焦点框<br/>
 * 焦点所在位置缩放<br/>
 * 
 * @author liuf 20140609
 * 
 */
@TargetApi(Build.VERSION_CODES.HONEYCOMB)
public class ViewFrameIndicator3 implements IFrameIndicator {
	private ImageView mImg;
	private ImageView mView;

	public ViewFrameIndicator3(Activity context) {

		mView = new ImageView(context);
		context.addContentView(mView, new LayoutParams(
				LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT));
		mView.setVisibility(View.INVISIBLE);
		mView.setTag("");

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
		Logger.d("setScaleAnimationSize(" + x + "," + y + ")");
	}

	MarginLayoutParams lastEnd;

	View mLastItem;

	@TargetApi(Build.VERSION_CODES.ICE_CREAM_SANDWICH)
	public synchronized void moveFrameTo(View v, boolean animated,
			boolean hideFrame) {
		boolean focusScale = false;
		boolean viewScale = false;
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
			if (w == 0 || h == 0) {
				return;
			}

			if (mMaxMoveRect != null) {
				xy[0] = Math.min(xy[0], mMaxMoveRect.right - w);
				xy[0] = Math.max(xy[0], mMaxMoveRect.left);

				xy[1] = Math.min(xy[1], mMaxMoveRect.bottom - h);
				xy[1] = Math.max(xy[1], mMaxMoveRect.top);
			}

			int followerW = follower.getWidth();
			int followerH = follower.getHeight();
			float followerX = follower.getScaleX();
			float followerY = follower.getScrollY();
			Logger.d(String.format("follower: %d x %d, %f, %f", followerW,
					followerH, followerX, followerY));
			Logger.d(String.format("id %d,class: %s, x: %d, y: %d, w:%d,h:%d",
					v.getId(), v.getClass().getName(), xy[0], xy[1], w, h));
			MarginLayoutParams start = (MarginLayoutParams) follower
					.getLayoutParams();

			int followerPL = follower.getPaddingLeft();
			int followerPR = follower.getPaddingRight();
			int followerPT = follower.getPaddingTop();
			int followerPB = follower.getPaddingBottom();
			FrameLayout.LayoutParams end = new FrameLayout.LayoutParams(w
					+ followerPL + followerPR, h + followerPT + followerPB);
			end.leftMargin = xy[0] - followerPL;
			end.topMargin = xy[1] - followerPT;
			boolean wasVisible = follower.getVisibility() == View.VISIBLE;
			follower.setAlpha(1f);

			if (hideFrame) {
				follower.setVisibility(View.INVISIBLE);
				mView.setVisibility(View.INVISIBLE);
			} else {
				follower.setVisibility(View.VISIBLE);
				mView.setVisibility(View.VISIBLE);
				setFocusViewParams(v, xy);
				viewScale = true;
			}

			if (!focusScale) {
				FrameLayout.LayoutParams newEnd = getFocusSite(follower, v, xy);
				end = newEnd;
			}
			if (end.leftMargin < 0 || end.topMargin < 0) {
				follower.setVisibility(View.INVISIBLE);
				mView.setVisibility(View.INVISIBLE);
				return;
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
					&& lastEnd.height == end.height) {
				follower.setLayoutParams(end);
				return;
			}
			Logger.d("start animation animated=" + animated + ", hideFrame="
					+ hideFrame + ", wasVisisble=" + wasVisible);
			Logger.d("Build.VERSION.SDK_INT=" + Build.VERSION.SDK_INT
					+ ", Build.VERSION_CODES.HONEYCOMB="
					+ Build.VERSION_CODES.HONEYCOMB + ", mValueAnimator="
					+ mValueAnimator);
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
						// follower.setScaleX(1f);
						// follower.setScaleY(1f);
						// follower.clearAnimation();
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
			// 动画缩放效果
			if (!hideFrame) {
				if (!viewScale) {
					setFocusViewParams(v, xy);
				}
			}
		} else {
			follower.setVisibility(View.INVISIBLE);
			mView.setVisibility(View.INVISIBLE);
			mView.clearAnimation();
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
				// float scale = 1 - 0.2f * fraction;
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
				// follower.setScaleX(scale);
				// follower.setScaleY(scale);
				follower.setLayoutParams(p);
				if (item instanceof DarkImgView) {
					// ((DarkImgView) item).setFadeFraction(1 - fraction
					// * fraction);
				}
				return p;
			} else if (fraction <= 0.85f) {
				follower.setLayoutParams(endValue);
				// float scale = 0.8f + 0.3f * (fraction - 0.6f) / 0.25f;
				// follower.setScaleX(scale);
				// item.setScaleX(scale);
				// follower.setScaleY(scale);
				// item.setScaleY(scale);

				// int brightness = (int) (50 * (fraction - 0.6f) / 0.25f);
				// ColorFilter cf = ColorFilterGenerator.adjustColor(brightness,
				// 0, 0, 0);
				if (item instanceof ImgView) {

					// ((ImgView) item).setColorFilter(cf);
				} else if (item.getBackground() != null) {
					// item.getBackground().setColorFilter(cf);
				}
				item.setActivated(true);

			} else {
				follower.setLayoutParams(endValue);
				// float scale = 1.1f - 0.1f * (fraction - 0.85f) / 0.15f;
				// follower.setScaleX(scale);
				// item.setScaleX(scale);
				// follower.setScaleY(scale);
				// item.setScaleY(scale);

				// int brightness = (int) (50 * (1 - (fraction - 0.85f) /
				// 0.15f));
				// ColorFilter cf = ColorFilterGenerator.adjustColor(brightness,
				// 0, 0, 0);
				if (item instanceof ImageView) {
					// ((ImageView) item).setColorFilter(cf);
				} else if (item instanceof ImageSwitcher) {
					View v = ((ImageSwitcher) item).getCurrentView();
					if (v instanceof ImageView) {
						// ((ImageView) v).setColorFilter(cf);
					} else if (v.getBackground() != null) {
						// item.getBackground().setColorFilter(cf);
					}
				} else if (item.getBackground() != null) {
					// item.getBackground().setColorFilter(cf);
				}
			}
			return endValue;
		}
	}

	@Override
	public void hideFrame() {
		mView.clearAnimation();
		mView.setVisibility(View.INVISIBLE);
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

	/**
	 * 以v控件的中心点进行缩放。
	 * 
	 * @param v
	 *            要缩放的控件 。
	 * @param x
	 *            X轴的缩放倍数 ，1.0为原大小。
	 * @param y
	 *            Y轴的缩放倍数 ，1.0为原大小。
	 */
	public void setViewScaleAnimation(View v, float x, float y) {
		int mode = Animation.RELATIVE_TO_SELF;
		ScaleAnimation animation = new ScaleAnimation(1.0f, x, 1.0f, y, mode,
				0.5f, mode, 0.5f);
		// 从1倍到1.1倍需要的时间（1000=1秒钟）
		animation.setDuration(1000);
		// 动画执行完后是否停留在执行完的状态
		animation.setFillAfter(true);
		animation.setFillEnabled(true);
		v.startAnimation(animation);
	}

	/**
	 * 当前焦点所在view的缩放
	 * 
	 * @param v
	 *            当前焦点所在view
	 * @param x
	 *            X轴的缩放倍数 ，1.0为原大小。
	 * @param y
	 *            Y轴的缩放倍数 ，1.0为原大小。
	 * @param xy
	 *            当前焦点所在view在整个布局中的Margin,xy[0]为leftMargin,xy[1]为topMarign。
	 */
	public void setVScaleAnimation(View v, float x, float y, int[] xy) {
		int w = v.getWidth();
		int h = v.getHeight();
		mView.clearAnimation();
		v.setDrawingCacheEnabled(true);
		Bitmap bitmap = v.getDrawingCache();
		@SuppressWarnings("deprecation")
		Drawable drawable = (Drawable) new BitmapDrawable(bitmap);
		mView.setImageDrawable(null);
		mView.setImageDrawable(drawable);
		FrameLayout.LayoutParams p = new FrameLayout.LayoutParams(w, h);
		p.leftMargin = xy[0];
		p.topMargin = xy[1];
		mView.setLayoutParams(p);
		mView.setVisibility(View.VISIBLE);
		setViewScaleAnimation(mView, scaleAnimationX, scaleAnimationY);
		mView.setTag(v.getId());
	}

	/**
	 * 焦点框或焦点所在view有缩放动画效果
	 * 
	 * @param follower
	 *            焦点框
	 * @param v
	 *            当前焦点所在view
	 * @param x
	 *            X轴的缩放倍数 ，1.0为原大小。
	 * @param y
	 *            Y轴的缩放倍数 ，1.0为原大小。
	 * @param xy
	 *            当前焦点所在view在整个布局中的Margin,xy[0]为leftMargin,xy[1]为topMarign。
	 * @param focusScale
	 *            焦点框是否有缩放效果。
	 * @return
	 */
	public void setFocusViewScaleAnimation(View follower, View v, float x,
			float y, int[] xy, boolean focusScale) {
		if (x == 1.0 && y == 1.0) {
			mView.clearAnimation();
			mView.setVisibility(View.INVISIBLE);
			return;
		}
		Logger.d("有缩放动画效果：" + x + "," + y);
		if (focusScale) {
			setViewScaleAnimation(follower, scaleAnimationX, scaleAnimationY);
		}
		setVScaleAnimation(v, scaleAnimationX, scaleAnimationY, xy);
	}

	/**
	 * 焦点框的位置与高宽。
	 * 
	 * @param follower
	 *            焦点框
	 * @param v
	 *            当前焦点所在view
	 * @param xy
	 *            当前焦点所在view在整个布局中的Margin,xy[0]为leftMargin,xy[1]为topMarign。
	 * @return
	 */
	public FrameLayout.LayoutParams getFocusSite(View follower, View v, int[] xy) {
		int w = v.getWidth();
		int h = v.getHeight();
		FrameLayout.LayoutParams end = new FrameLayout.LayoutParams(w, h);
		int scaleW = (int) (w * scaleAnimationX);
		int scaleH = (int) (h * scaleAnimationY);
		int pleft = follower.getPaddingLeft();
		int pright = follower.getPaddingRight();
		int ptop = follower.getPaddingTop();
		int pbottom = follower.getPaddingBottom();
		int leftMargin = (int) (w * (scaleAnimationX - 1) / 2);
		int topMaigin = (int) (h * (scaleAnimationY - 1) / 2);

		end.width = scaleW + pleft + pright;
		end.height = scaleH + ptop + pbottom;
		end.leftMargin = xy[0] - leftMargin - pleft;
		end.topMargin = xy[1] - topMaigin - ptop;
		return end;
	}

	/**
	 * 设置焦点所在view的Params
	 * 
	 * @param v
	 * @param xy
	 */
	public void setFocusViewParams(View v, int[] xy) {
		int w = v.getWidth();
		int h = v.getHeight();
		v.destroyDrawingCache();
		v.setDrawingCacheEnabled(true);
		Bitmap bitmap = v.getDrawingCache();
		bitmap = Bitmap.createBitmap(bitmap);
		Drawable drawable = (Drawable) new BitmapDrawable(bitmap);
		mView.setImageDrawable(null);
		mView.setImageDrawable(drawable);
		FrameLayout.LayoutParams end = new FrameLayout.LayoutParams(w, h);
		end.width = (int) (w * scaleAnimationX);
		end.height = (int) (h * scaleAnimationY);
		int leftMargin = (int) (w * (scaleAnimationX - 1) / 2);
		int topMaigin = (int) (h * (scaleAnimationY - 1) / 2);
		end.leftMargin = xy[0] - leftMargin;
		end.topMargin = xy[1] - topMaigin;
		
		if (!(end.leftMargin < 0 || end.topMargin < 0)) {
			mView.setVisibility(View.VISIBLE);
			mView.setLayoutParams(end);
			mView.setTag(v.getId());
		}
		Logger.d("end.width:" + end.width + ",end.height:" + end.height
				+ ",end.leftMargin:" + end.leftMargin + ",end.topMargin:"
				+ end.topMargin);
	}
}
