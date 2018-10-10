package ipanel.join.widget;

import ipanel.join.configuration.Bind;
import ipanel.join.configuration.View;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.LinearGradient;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.PorterDuff.Mode;
import android.graphics.PorterDuffXfermode;
import android.graphics.Rect;
import android.graphics.Shader.TileMode;
import android.util.AttributeSet;
import android.view.ViewParent;

/**
 * Create reflecting effect for child views </br> NOTE: it use offline bitmap
 * for reflection image, that requires additional memory
 * 
 * @author Zexu
 * 
 */
public class ReflectingLayout extends LinearLayout {
	public static final String PROP_ALPHA_END = "alphaEnd";
	public static final String PROP_ALPHA_START = "alphaStart";
	public static final String PROP_ALPHA_GAP = "alphaGap";

	/** The maximum ratio of the height of the reflection to the source image. */
	private static final float MAX_REFLECTION_RATIO = 0.9F;

	private int mAlphaStart = 0xAA000000;
	private int mAlphaEnd = 0x0;
	private int mAlphaGap = 0;

	/** The {@link Paint} object we'll use to create the reflection. */
	private Paint paint;

	private Matrix vFlipMatrix;

	public ReflectingLayout(Context context, AttributeSet attrs) {
		super(context, attrs);
		init();
	}

	public ReflectingLayout(Context context) {
		super(context);
		init();
	}

	public ReflectingLayout(Context context, View data) {
		super(context, data);

		Bind bind = data.getBindByName(PROP_ALPHA_START);
		if (bind != null) {
			mAlphaStart = PropertyUtils.parseColor(bind.getValue().getvalue());
		}
		bind = data.getBindByName(PROP_ALPHA_END);
		if (bind != null) {
			mAlphaEnd = PropertyUtils.parseColor(bind.getValue().getvalue());
		}
		bind = data.getBindByName(PROP_ALPHA_GAP);
		if (bind != null) {
			mAlphaGap = PropertyUtils.parseColor(bind.getValue().getvalue());
		}

		init();
	}

	public void setAlphaStart(int mAlphaStart) {
		this.mAlphaStart = mAlphaStart;
		requestLayout();
	}

	public void setAlphaEnd(int mAlphaEnd) {
		this.mAlphaEnd = mAlphaEnd;
		requestLayout();
	}

	public void setAlphaGap(int mAlphaGap){
		this.mAlphaGap = mAlphaGap;
		requestLayout();
	}
	
	
	/**
	 * Initialises the layout.
	 */
	private void init() {
		// Ensures that we redraw when our children are redrawn.
		 setAddStatesFromChildren(true);

		// Important to ensure onDraw gets called.
		setWillNotDraw(false);
		setDrawingCacheEnabled(true);

		// Create the paint object which we'll use to create the reflection
		// gradient
		paint = new Paint();
		paint.setXfermode(new PorterDuffXfermode(Mode.DST_IN));

		// Create a matrix which can flip images vertically
		vFlipMatrix = new Matrix();
	}

	@Override
	protected void onLayout(boolean changed, int l, int t, int r, int b) {
		super.onLayout(changed, l, t, r, b);
		reflectionHeight = getReflectionHeight();
		int width = r - l;
		if (reflectionHeight > 0 && width > 0) {
			if (sourceBitmap == null
					|| sourceBitmap.getWidth() != getMeasuredWidth()
					|| sourceBitmap.getHeight() != reflectionHeight) {
				sourceBitmap = Bitmap.createBitmap(width, reflectionHeight,
						Bitmap.Config.ARGB_8888);
				tempCanvas = new Canvas(sourceBitmap);

				LinearGradient gradient = new LinearGradient(0, 0, 0,
						reflectionHeight, mAlphaStart, mAlphaEnd,
						TileMode.CLAMP);
				paint.setShader(gradient);
			}

			vFlipMatrix.reset();
			vFlipMatrix.postScale(1, -1);
			vFlipMatrix.postTranslate(0, getMaxChildBottom());
		}
	}

	int reflectionHeight;
	Bitmap sourceBitmap;
	Canvas tempCanvas;

	@Override
	protected void onDraw(Canvas canvas) {
		super.onDraw(canvas);

		// Only actually do anything if there is space to actually draw a
		// reflection
		if (reflectionHeight > 0 && tempCanvas != null) {

			// Create a bitmap to hold the drawing of child views and pass this
			// to a temp canvas
			tempCanvas.drawColor(0, Mode.CLEAR);

			int count = tempCanvas.saveLayer(0, 0, canvas.getWidth(),
					canvas.getHeight(), null, Canvas.MATRIX_SAVE_FLAG
							| Canvas.CLIP_SAVE_FLAG
							| Canvas.HAS_ALPHA_LAYER_SAVE_FLAG
							| Canvas.FULL_COLOR_LAYER_SAVE_FLAG
							| Canvas.CLIP_TO_LAYER_SAVE_FLAG);
			// Draw the content of this layout onto our temporary canvas.
			tempCanvas.concat(vFlipMatrix);
			super.dispatchDraw(tempCanvas);
			tempCanvas.setMatrix(null);
			tempCanvas.drawPaint(paint);
			tempCanvas.restoreToCount(count);

			int childBottom = getMaxChildBottom();
			
			// Draw our image onto the canvas
			canvas.drawBitmap(sourceBitmap, 0, childBottom+mAlphaGap, null);
		}
	}

	@Override
	public ViewParent invalidateChildInParent(int[] location, Rect dirty) {
		ViewParent p = super.invalidateChildInParent(location, dirty);
		invalidate();
		return p;
	}

	/**
	 * Finds the bottom of the lowest view contained by this layout.
	 * 
	 * @return the bottom of the lowest view
	 */
	private int getMaxChildBottom() {
		int maxBottom = 0;
		for (int i = 0; i < getChildCount(); i++) {
			int bottom = getChildAt(i).getBottom();
			if (bottom > maxBottom)
				maxBottom = bottom;
		}
		return maxBottom;
	}

	/**
	 * Gets the highest top edge of all contained views.
	 * 
	 * @return the min child top
	 */
	private int getMinChildTop() {
		int minTop = Integer.MAX_VALUE;
		for (int i = 0; i < getChildCount(); i++) {
			int top = getChildAt(i).getTop();
			if (top < minTop)
				minTop = top;
		}
		return minTop;
	}

	/**
	 * Gets the height of the space covered by all children.
	 * 
	 * @return the total child height
	 */
	private int getTotalChildHeight() {
		// The max value of any child's "bottom" minus the minimum of any "top"
		return getMaxChildBottom() - getMinChildTop();
	}

	/**
	 * Gets the height of the reflection to be drawn.
	 * 
	 * <p>
	 * This is the minimum of either:
	 * <ul>
	 * <li>The remaining height between the bottom of this layout and the
	 * bottom-most child view</li>
	 * <li>The maximum reflection ratio</li>
	 * </p>
	 * 
	 * @return the reflection height
	 */
	private int getReflectionHeight() {
		return (int) Math.min(getMeasuredHeight() - getMaxChildBottom(),
				getTotalChildHeight() * MAX_REFLECTION_RATIO);
	}
}