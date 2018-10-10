package ipaneltv.toolkit.gl;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import javax.microedition.khronos.opengles.GL10;

import android.view.KeyEvent;

public class GlFrame extends GlView {

	List<GlView> childs;
	GlView mFocusGlObject;
	GlSurfaceWindow mGlSurfaceWindow;

	public GlFrame() {
		childs = new ArrayList<GlView>();
		setBound(0, 0, GlSurfaceWindow.FULL_SCREEN_WIDTH,
				GlSurfaceWindow.FULL_SCREEN_HEIGHT);
	}

	@Override
	public final void setVisible(boolean b) {
	}

	public final void add(GlView obj) {
		if (obj == null)
			return;
		synchronized (mutex) {
			if (obj instanceof GlFrame) {
				return;
			}
			childs.add(obj);
			obj.mGlFrame = this;
			GlSurfaceWindow.setApplicationBusyNow(false);
		}
	}

	public final void remove(GlView obj) {
		if (obj == null)
			return;
		synchronized (mutex) {
			if (childs.contains(obj)) {
				obj.mGlFrame = null;
				childs.remove(obj);
				GlSurfaceWindow.setApplicationBusyNow(false);
			}
		}
	}

	public final int count() {
		synchronized (mutex) {
			return childs.size();
		}
	}

	@Override
	protected boolean onKeyEvent(KeyEvent event) {
		if (event == null)
			return false;
		if (event.getAction() == KeyEvent.ACTION_UP) {
			switch (event.getKeyCode()) {
			case KeyEvent.KEYCODE_BACK:
				if (mGlSurfaceWindow == null)
					return false;
				mGlSurfaceWindow.finish();
				return true;
			}
		}
		return false;
	}

	@Override
	protected boolean dispatchKeyEvent(KeyEvent event) {
		if (mFocusGlObject != null) {
			if (mFocusGlObject.dispatchKeyEvent(event)) {
				return true;
			}
		}
		return super.dispatchKeyEvent(event);
	}

	protected final boolean dispatchRequestFocus(GlView view, boolean focus) {
		if (view == null)
			return false;
		if (childs.contains(view)) {
			if (focus) {
				FocusRunnable focusEvent = new FocusRunnable();
				if (mFocusGlObject != null) {
					focusEvent.mLostFocusObject = mFocusGlObject;
					mFocusGlObject = null;
				}
				mFocusGlObject = view;
				focusEvent.mGainFocusObject = mFocusGlObject;
				mGlSurfaceWindow.queueEvent(focusEvent);
			} else {
				if (mFocusGlObject != null) {
					FocusRunnable focusEvent = new FocusRunnable();
					focusEvent.mLostFocusObject = mFocusGlObject;
					mFocusGlObject = null;
					mGlSurfaceWindow.queueEvent(focusEvent);
				}
			}
			return focus;
		}
		return false;
	}

	protected boolean onUpdate(GL10 gl) {
		boolean end = false;
		if (doAnimation(gl, getAnimation()))
			end = true;
		if (onDraw(gl))
			end = true;
		return end;
	}

	protected boolean onDraw(GL10 gl) {
		return onDrawChilds(gl);
	}

	protected boolean onDrawChilds(GL10 gl) {

		synchronized (mutex) {
			Iterator<GlView> it = childs.iterator();
			boolean end = false;
			while (it.hasNext()) {
				GlView glObject = it.next();
				if (glObject == null || !glObject.isVisible())
					continue;
				gl.glPushMatrix();
				gl.glTranslatef(glObject.x, glObject.y, 0);

				if (doAnimation(gl, glObject.getAnimation()))
					end = true;
				if (glObject.onDraw(gl))
					end = true;
				gl.glPopMatrix();
			}
			return end;
		}
	}

	private boolean doAnimation(GL10 gl, GlAnimation anim) {
		if (anim == null)
			return false;
		anim.onUpdate(gl);
		if (anim.hasMoreFrame())
			return true;
		return false;
	}

	class FocusRunnable implements Runnable {

		GlView mLostFocusObject;
		GlView mGainFocusObject;

		public void run() {
			if (mLostFocusObject != null) {
				mLostFocusObject.dispatchFocusEvent(mLostFocusObject, false);
			}
			if (mGainFocusObject != null) {
				mGainFocusObject.dispatchFocusEvent(mGainFocusObject, true);
			}
		}
	}
}