package ipanel.join.widget;

import ipanel.join.configuration.Bind;
import ipanel.join.configuration.View;
import android.content.Context;
import android.graphics.Canvas;
import android.graphics.LinearGradient;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.PorterDuffXfermode;
import android.graphics.Shader.TileMode;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.util.AttributeSet;

public class ReflectedImgView extends ImgView {
	public static final String PROP_ALPHA_END = "alphaEnd";
	public static final String PROP_ALPHA_START = "alphaStart";
	private Paint mPaint;
	private Matrix matrix = new Matrix();

	private int mAlphaStart = 0x0;
	private int mAlphaEnd = 0xFF000000;

	public ReflectedImgView(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);
		init();
	}

	public ReflectedImgView(Context context, AttributeSet attrs) {
		super(context, attrs);
		init();
	}

	public ReflectedImgView(Context context) {
		super(context);
		init();
	}

	public ReflectedImgView(Context ctx, View data) {
		super(ctx, data);

		Bind bind = data.getBindByName(PROP_ALPHA_START);
		if (bind != null) {
			mAlphaStart = PropertyUtils.parseColor(bind.getValue().getvalue());
		}
		bind = data.getBindByName(PROP_ALPHA_END);
		if (bind != null) {
			mAlphaEnd = PropertyUtils.parseColor(bind.getValue().getvalue());
		}
		init();
	}

	protected void init() {
		if (isInEditMode())
			return;

		setScaleType(ScaleType.MATRIX);

		// Setup the paint.
		mPaint = new Paint();
		mPaint.setAntiAlias(true);
		mPaint.setColor(0xFFFF0000);
		mPaint.setStrokeWidth(0.0f);

		// Destination (DST) is drawn by the parent, and should be retained.
		mPaint.setXfermode(new PorterDuffXfermode(android.graphics.PorterDuff.Mode.DST_IN));
	}

	@Override
	protected void onLayout(boolean changed, int left, int top, int right, int bottom) {
		super.onLayout(changed, left, top, right, bottom);
		if (isInEditMode())
			return;
		// It's recommended not to create a Shader in the basic layout calls.
		// Linear gradient from Transparent to Black, from top to bottom.
		// Note: We rotated the image using a transformation.
		// Hence the colors will be opposite.

		LinearGradient gradient = new LinearGradient(0, 0, 0, bottom - top, mAlphaStart, mAlphaEnd,
				TileMode.CLAMP);

		// Set the gradient as the shader.
		mPaint.setShader(gradient);

		matrix.reset();
		matrix.postScale(1, -1);
		matrix.postTranslate(0, bottom - top);

		updateImgMatrix();
	}

	public void setAlphaStart(int mAlphaStart) {
		this.mAlphaStart = mAlphaStart;
	}

	public void setAlphaEnd(int mAlphaEnd) {
		this.mAlphaEnd = mAlphaEnd;
	}

	@Override
	public void setImageDrawable(Drawable drawable) {
		super.setImageDrawable(drawable);
		updateImgMatrix();
	}

	private void updateImgMatrix() {
		if (getDrawable() instanceof BitmapDrawable && getWidth() > 0) {
			BitmapDrawable bd = (BitmapDrawable) getDrawable();
			if (bd.getIntrinsicWidth() > 0) {
				float scale = (float) getWidth() / bd.getIntrinsicWidth();
				Matrix matrix = new Matrix();
				// matrix.reset();
				matrix.postScale(scale, scale);
				if (bd.getIntrinsicHeight() * scale > getHeight()) {
					matrix.postTranslate(0, getHeight() - bd.getIntrinsicHeight() * scale);
				}
				setImageMatrix(matrix);
			}
		}

	}

	@Override
	protected void onDraw(Canvas canvas) {
		if (isInEditMode()) {
			super.onDraw(canvas);
			return;
		}

		// Save the canvas. All PorterDuff operations should be done in a
		// offscreen bitmap.
		int count = canvas.saveLayer(0, 0, canvas.getWidth(), canvas.getHeight(), null,
				Canvas.MATRIX_SAVE_FLAG | Canvas.CLIP_SAVE_FLAG | Canvas.HAS_ALPHA_LAYER_SAVE_FLAG
						| Canvas.FULL_COLOR_LAYER_SAVE_FLAG | Canvas.CLIP_TO_LAYER_SAVE_FLAG);

		canvas.concat(matrix);
		// Do a default draw.
		super.onDraw(canvas);

		// Draw the paint (that has a shader set), on top of the image
		// drawn by the parent (ImageView).
		// Note: This works only on ICS. For pre-ICS phones, create a bitmap and
		// draw on it, like mentioned in CanvasDelegate linked below.
		canvas.drawPaint(mPaint);

		// Restore the canvas.
		canvas.restoreToCount(count);
	}

}
