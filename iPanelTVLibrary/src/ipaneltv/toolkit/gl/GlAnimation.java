package ipaneltv.toolkit.gl;

import javax.microedition.khronos.opengles.GL10;

public abstract class GlAnimation {

	protected GlAnimationCallback mCallback;
	private boolean started = false;
	private boolean firsted = false;
	private boolean hasMore = false;

	public void setCallback(GlAnimationCallback callback) {
		mCallback = callback;
	}

	protected final void startAnimation() {
		started = true;
		firsted = true;
		hasMore = true;
	}

	protected final void stopAnimation() {
		started = false;
		firsted = false;
		hasMore = false;
	}

	protected final boolean hasMoreFrame() {
		return hasMore;
	}

	protected final void onUpdate(GL10 gl) {
		if (started) {
			if (firsted) {
				if (mCallback != null) {
					mCallback.onAnimationStart();
				}
				firsted = false;
			}
			hasMore = onDrawFrame(gl);
			if (!hasMore) {
				if (mCallback != null) {
					mCallback.onAnimationFinish();
				}
				started = false;
			}
		}
	}

	protected abstract boolean onDrawFrame(GL10 gl);

	public interface GlAnimationCallback {

		public void onAnimationStart();

		public void onAnimationFinish();
	}
}