package ipanel.join.widget;

import ipanel.join.configuration.Bind;
import ipanel.join.configuration.View;
import android.annotation.TargetApi;
import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.os.Build;
import android.util.AttributeSet;

@TargetApi(Build.VERSION_CODES.HONEYCOMB)
public class DarkImgView extends ImgView {

	public static final String PROP_FADE_COLOR = "fadeColor";
	int mFadeColor;

	int mOriginFadeColor;

	public DarkImgView(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);
		init();
	}

	public DarkImgView(Context context, AttributeSet attrs) {
		super(context, attrs);
		init();
	}

	public DarkImgView(Context context) {
		super(context);
		init();
	}

	public DarkImgView(Context ctx, View data) {
		super(ctx, data);

		init();

		Bind bind = data.getBindByName(PROP_FADE_COLOR);
		if (bind != null) {
			mFadeColor = PropertyUtils.parseColor(bind.getValue().getvalue());
		}
		mOriginFadeColor = mFadeColor;

		setOnFocusChangeListener(new OnFocusChangeListener() {

			@Override
			public void onFocusChange(android.view.View v, boolean hasFocus) {
				if (!hasFocus) {
					setFadeFraction(1f);
				}

			}
		});
	}

	public void setFadeColor(int color) {
		this.mFadeColor = color;
		invalidate();
	}

	@Override
	public void setActivated(boolean activated) {
		super.setActivated(activated);
		invalidate();
	}

	protected void init() {
		mFadeColor = Color.parseColor("#33000000");
	}

	public void setFadeFraction(float fraction) {
		mFadeColor = Color.argb((int) (Color.alpha(mOriginFadeColor) * fraction),
				Color.red(mOriginFadeColor), Color.green(mOriginFadeColor),
				Color.blue(mOriginFadeColor));
		postInvalidate();
	}

	@Override
	protected void onDraw(Canvas canvas) {
		super.onDraw(canvas);
		if (!hasFocus() || !isActivated())
			canvas.drawColor(mFadeColor);
	}

}
