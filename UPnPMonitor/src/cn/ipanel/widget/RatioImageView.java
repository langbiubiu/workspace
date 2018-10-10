package cn.ipanel.widget;

import cn.ipanel.upnp.monitor.R;
import android.content.Context;
import android.content.res.TypedArray;
import android.util.AttributeSet;
import android.widget.ImageView;

public class RatioImageView extends ImageView {

	private float mRatio;
	
	public RatioImageView(Context context){
		this(context, null);
	}
	public RatioImageView(Context context, AttributeSet attrs){
		this(context, attrs, 0);
	}

	public RatioImageView(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);

		// reading attributes
		final TypedArray a = getContext().obtainStyledAttributes(attrs,
				R.styleable.RatioImageView);
		mRatio = a.getFloat(R.styleable.RatioImageView_ratio, 0);

	}

	@Override
	protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
		super.onMeasure(widthMeasureSpec, heightMeasureSpec);
		if (mRatio > 0)
			this.setMeasuredDimension(getMeasuredWidth(),
					(int) (getMeasuredWidth() * mRatio));
	}

	public float getRatio() {
		return mRatio;
	}

	public void setRatio(float mRatio) {
		this.mRatio = mRatio;
		this.requestLayout();
	}

}
