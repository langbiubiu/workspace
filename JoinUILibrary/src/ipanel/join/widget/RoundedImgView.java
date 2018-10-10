package ipanel.join.widget;

import ipanel.join.configuration.Bind;
import ipanel.join.configuration.View;
import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffXfermode;
import android.graphics.RectF;
import android.graphics.Xfermode;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.util.AttributeSet;

public class RoundedImgView extends ImgView {
	public static final String PROP_RADIUS = "radius";
	public static final String PROP_RATIO = "ratio";

	private float mRadius;
	private float mRatio;

	public RoundedImgView(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);
	}

	public RoundedImgView(Context context, AttributeSet attrs) {
		super(context, attrs);
	}

	public RoundedImgView(Context ctx, View data) {
		super(ctx, data);

	}

	public RoundedImgView(Context context) {
		super(context);
	}

	@Override
	protected void bindProperty(Context ctx, View data) {
		super.bindProperty(ctx, data);

		Bind bd = data.getBindByName(PROP_RADIUS);
		if (bd != null) {
			mRadius = PropertyUtils.getScaledSize(Float.parseFloat(bd.getValue().getvalue()));
		}

		bd = data.getBindByName(PROP_RATIO);
		if (bd != null) {
			mRatio = Float.parseFloat(bd.getValue().getvalue());
		}
	}

	@Override
	protected void onDraw(Canvas canvas) {
		// Round some corners betch!
		Drawable maiDrawable = getDrawable();
		if (maiDrawable instanceof BitmapDrawable
				&& ((BitmapDrawable) maiDrawable).getBitmap() != null && mRadius > 0) {
			Paint paint = ((BitmapDrawable) maiDrawable).getPaint();
			final int color = 0xff000000;

			final RectF rectF = new RectF(0, 0, getWidth(), getHeight());
			// Create an off-screen bitmap to the PorterDuff alpha blending to work right
			int saveCount = canvas.saveLayer(rectF, null, Canvas.MATRIX_SAVE_FLAG
					| Canvas.CLIP_SAVE_FLAG | Canvas.HAS_ALPHA_LAYER_SAVE_FLAG
					| Canvas.FULL_COLOR_LAYER_SAVE_FLAG | Canvas.CLIP_TO_LAYER_SAVE_FLAG);

			paint.setAntiAlias(true);
			canvas.drawARGB(0, 0, 0, 0);
			paint.setColor(color);
			canvas.drawRoundRect(rectF, mRadius, mRadius, paint);

			Xfermode oldMode = paint.getXfermode();
			// This is the paint already associated with the BitmapDrawable that super draws
			paint.setXfermode(new PorterDuffXfermode(PorterDuff.Mode.SRC_IN));
			super.onDraw(canvas);
			paint.setXfermode(oldMode);
			canvas.restoreToCount(saveCount);
		} else {
			super.onDraw(canvas);
		}
	}

	@Override
	protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
		if (mRatio > 0) {
			int mode = MeasureSpec.getMode(widthMeasureSpec);
			switch (mode) {
			case MeasureSpec.UNSPECIFIED:
				heightMeasureSpec = widthMeasureSpec;
				break;
			case MeasureSpec.EXACTLY:
				heightMeasureSpec = MeasureSpec.makeMeasureSpec(MeasureSpec.EXACTLY,
						(int) (MeasureSpec.getSize(widthMeasureSpec) * mRatio));
				break;
			case MeasureSpec.AT_MOST:
				heightMeasureSpec = MeasureSpec.makeMeasureSpec(MeasureSpec.AT_MOST,
						(int) (MeasureSpec.getSize(widthMeasureSpec) * mRatio));
				break;
			}
		}
		super.onMeasure(widthMeasureSpec, heightMeasureSpec);
		if (mRatio > 0)
			this.setMeasuredDimension(getMeasuredWidth(), (int) (getMeasuredWidth() * mRatio));
	}

	public float getRatio() {
		return mRatio;
	}

	public void setRatio(float mRatio) {
		this.mRatio = mRatio;
		this.requestLayout();
	}

	public void setRadius(float radius) {
		this.mRadius = radius;
		invalidate();
	}

	public float getRadius() {
		return mRadius;
	}

}
