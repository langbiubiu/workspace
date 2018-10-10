package cn.ipanel.android.widget;

import android.content.Context;
import android.graphics.Rect;
import android.text.TextPaint;
import android.util.AttributeSet;
import android.view.animation.LinearInterpolator;
import android.widget.Scroller;
import android.widget.TextView;

public class ScrollTextView extends TextView {
	public interface OnScrollEndListener {
		public void onScrollEnd();
	}

	public enum ScrollDirection {
		RIGHT_TO_LEFT, BOTTOM_TO_TOP
	}

	private ScrollDirection mScrollDirection = ScrollDirection.RIGHT_TO_LEFT;

	// scrolling feature
	private Scroller mSlr;

	// milliseconds per pixel
	private float mSpeed = 1f;

	// the X offset when paused
	private int mXPaused = 0;

	private int mYPaused = 0;

	// whether it's being paused
	private boolean mPaused = true;

	/**
	 * repeat count, -1 for infinite
	 */
	private int mRepeat = -1;

	private OnScrollEndListener mScrollEndListener;

	public void setOnScrollEndListener(OnScrollEndListener l) {
		this.mScrollEndListener = l;
	}

	public void setScrollDirection(ScrollDirection direction) {
		this.mScrollDirection = direction;
		switch (mScrollDirection) {
		case RIGHT_TO_LEFT:
			setSingleLine();
			break;
		case BOTTOM_TO_TOP:
			setSingleLine(false);
			break;

		}
	}

	/*
	 * constructor
	 */
	public ScrollTextView(Context context) {
		this(context, null);
		// customize the TextView
		setSingleLine();
		setEllipsize(null);
		setVisibility(INVISIBLE);
	}

	/*
	 * constructor
	 */
	public ScrollTextView(Context context, AttributeSet attrs) {
		this(context, attrs, android.R.attr.textViewStyle);
		// customize the TextView
		setSingleLine();
		setEllipsize(null);
		setVisibility(INVISIBLE);
	}

	/*
	 * constructor
	 */
	public ScrollTextView(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);
		// customize the TextView
		setSingleLine();
		setEllipsize(null);
		setVisibility(INVISIBLE);
	}

	/**
	 * begin to scroll the text from the original position
	 */
	public void startScroll(int count) {
		mRepeat = count;
		// begin from the very right side
		mXPaused = -1 * getWidth();
		mYPaused = -1 * getHeight();
		// assume it's paused
		mPaused = true;
		resumeScroll();
	}

	@Override
	protected void onLayout(boolean changed, int left, int top, int right, int bottom) {
		super.onLayout(changed, left, top, right, bottom);
		if (changed) {
			mXPaused = -1 * getWidth();
			mYPaused = -1 * getHeight();
			if (!mPaused)
				mPaused = true;
			resumeScroll();
		}
	}

	/**
	 * resume the scroll from the pausing point
	 */
	public void resumeScroll() {

		if (!mPaused)
			return;

		// Do not know why it would not scroll sometimes
		// if setHorizontallyScrolling is called in constructor.
		switch (mScrollDirection) {
		case RIGHT_TO_LEFT:
			setHorizontallyScrolling(true);
			break;
		case BOTTOM_TO_TOP:
			setHorizontallyScrolling(false);
			break;

		}

		// use LinearInterpolator for steady scrolling
		mSlr = new Scroller(this.getContext(), new LinearInterpolator());
		setScroller(mSlr);

		switch (mScrollDirection) {
		case RIGHT_TO_LEFT:
			int scrollingLen = calculateScrollingLen();
			int distance = scrollingLen - (getWidth() + mXPaused);
			int duration = (int) (distance * 50 / mSpeed);
			mSlr.startScroll(mXPaused, 0, distance, 0, duration);
			break;
		case BOTTOM_TO_TOP:
			scrollingLen = calculateVertialScrollingLen();
			distance = scrollingLen - (getHeight() + mYPaused);
			duration = (int) (distance * 100 / mSpeed);
			mSlr.startScroll(0, mYPaused, 0, distance, duration);
			break;
		}

		setVisibility(VISIBLE);
		mPaused = false;
	}

	private int calculateVertialScrollingLen() {
		int h = getLineCount() * getLineHeight();
		return h + getHeight();
	}

	/**
	 * calculate the scrolling length of the text in pixel
	 * 
	 * @return the scrolling length in pixels
	 */
	private int calculateScrollingLen() {
		TextPaint tp = getPaint();
		Rect rect = new Rect();
		String strTxt = getText().toString();
		tp.getTextBounds(strTxt, 0, strTxt.length(), rect);
		int scrollingLen = rect.width() + getWidth();
		rect = null;
		return scrollingLen;
	}

	/**
	 * pause scrolling the text
	 */
	public void pauseScroll() {
		if (null == mSlr)
			return;

		if (mPaused)
			return;

		mPaused = true;

		// abortAnimation sets the current X to be the final X,
		// and sets isFinished to be true
		// so current position shall be saved
		mXPaused = mSlr.getCurrX();
		mYPaused = mSlr.getCurrY();

		mSlr.abortAnimation();
	}

	/*
	 * override the computeScroll to restart scrolling when finished so as that the text is scrolled
	 * forever
	 */
	@Override
	public void computeScroll() {
		super.computeScroll();

		if (null == mSlr)
			return;

		if (mSlr.isFinished() && (!mPaused)) {
			if (mRepeat == -1)
				startScroll(mRepeat);
			else if (mRepeat > 0)
				startScroll(mRepeat - 1);
			else if (mRepeat == 0 && mScrollEndListener != null)
				mScrollEndListener.onScrollEnd();

		}
	}

	public float getSpeed() {
		return mSpeed;
	}

	public void setSpeed(float speed) {
		if (speed <= 0)
			speed = 1f;
		this.mSpeed = speed;
	}

	public boolean isPaused() {
		return mPaused;
	}
}
