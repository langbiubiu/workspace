package cn.ipanel.android.widget;

import android.graphics.Rect;
import android.view.View;
import android.widget.ImageView;

public interface IFrameIndicator {
	public ImageView getImageView();

	public Rect getMaxMoveRect();

	public void moveFrameTo(View v, boolean animated, boolean hidFrame);

	public void moveFrmaeTo(View v);

	public void hideFrame();

	public void setAnimationTime(int duration);

	public void setFrameColor(int color);

	public void setFrameResouce(int resid);

	public void setMaxMoveRect(Rect r);

	public void setScaleAnimationSize(float x, float y);

	public void setPadding(int left, int top, int right, int bottom);
}
