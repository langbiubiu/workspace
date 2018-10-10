package ipaneltv.toolkit.gl;

import javax.microedition.khronos.opengles.GL10;

import android.view.KeyEvent;

public abstract class GlView {

	protected Object mutex = new Object();
	protected boolean visible = true;
	protected boolean focus = false;

	protected GlAnimation mGlAnimation;
	protected GlFrame mGlFrame;

	protected float x;
	protected float y;
	protected float w;
	protected float h;

	protected OnKeyListener mOnKeyListener;
	protected OnFocusChangeListener mOnFocusChangeListener;

	public void setBound(float x, float y, float w, float h) {
		this.x = x;
		this.y = y;
		this.w = w;
		this.h = h;
	}

	public void setBound(int x, int y, int w, int h) {
		this.setBound(GlToolkit.translateX(x), GlToolkit.translateY(y),
				GlToolkit.translateWidth(w), GlToolkit.translateHeight(h));
	}

	public void setVisible(boolean v) {
		visible = v;
	}

	public final boolean isVisible() {
		return visible;
	}

	public void setOnKeyListener(OnKeyListener listener) {
		mOnKeyListener = listener;
	}

	public void setOnFocusChangeListener(OnFocusChangeListener listener) {
		mOnFocusChangeListener = listener;
	}

	public boolean hasFocus() {
		return focus;
	}

	public boolean requestFocus() {

		if (focus || mGlFrame == null)
			return focus;
		return mGlFrame.dispatchRequestFocus(this, true);
	}

	public void startAnimation(GlAnimation animation) {
		synchronized (mutex) {
			mGlAnimation = animation;
			mGlAnimation.startAnimation();
		}
	}

	public void clearAnimation() {
		synchronized (mutex) {
			if (mGlAnimation != null) {
				mGlAnimation.stopAnimation();
				mGlAnimation = null;
			}
		}
	}

	protected GlAnimation getAnimation() {
		return mGlAnimation;
	}

	protected boolean dispatchKeyEvent(KeyEvent event) {
		if (event == null)
			return false;
		if (mOnKeyListener != null) {
			boolean b = mOnKeyListener.onKeyEvent(this, event);
			if (b)
				return b;
		}
		return onKeyEvent(event);
	}

	protected void dispatchFocusEvent(GlView object, boolean focus) {
		if (object != this)
			return;
		this.focus = focus;
		if (mOnFocusChangeListener != null) {
			mOnFocusChangeListener.onFocusChanged(this, focus);
		}
	}

	protected boolean onKeyDown(int keyCode, KeyEvent event) {
		return false;
	}

	protected boolean onKeyUp(int keyCode, KeyEvent event) {
		return false;
	}

	protected boolean onKeyEvent(KeyEvent event) {
		switch (event.getAction()) {
		case KeyEvent.ACTION_DOWN:
			return onKeyDown(event.getKeyCode(), event);
		case KeyEvent.ACTION_UP:
			return onKeyUp(event.getKeyCode(), event);
		default:
			return false;
		}
	}

	/**
	 * 绘制函数
	 * 
	 * 如果返回false，表示当前GlObject，下一帧开始无变化，可以不用再绘制
	 * 如果返回true，表示当前GlObject，下一帧仍然与当前帧有变化，必须绘制
	 * 
	 * @param gl
	 * @return
	 */
	protected boolean onDraw(GL10 gl) {
		return false;
	}

	public float getX() {
		return x;
	}

	public float getY() {
		return y;
	}

	public float getWidth() {
		return w;
	}

	public float getHeight() {
		return h;
	}

	public interface OnKeyListener {

		public boolean onKeyEvent(GlView view, KeyEvent event);
	}

	public interface OnFocusChangeListener {

		public void onFocusChanged(GlView view, boolean focus);
	}
}