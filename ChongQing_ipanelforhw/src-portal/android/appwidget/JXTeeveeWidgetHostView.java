package android.appwidget;

import android.content.Context;
import android.graphics.Rect;

public class JXTeeveeWidgetHostView extends TeeveeWidgetHostView {

	public JXTeeveeWidgetHostView(Context context) {
		super(context);
	}

	@Override
	protected void onLayout(boolean changed, int left, int top, int right, int bottom) {
		super.onLayout(changed, left, top, right, bottom);

		setAutoVideoBounds();
	}

	@Override
	void doSetVideoBoundsRect(Rect r) {
		if (getVisibility() == VISIBLE && getWidth() > 0 && getHeight() > 0)
			super.doSetVideoBoundsRect(r);
	}
	
	@Override
	public void setAutoVideoBounds() {
		Rect r = new Rect();
		getGlobalVisibleRect(r);
		autoVideoBounds = true;
		// ȡ�������������Ļ�����ꡣ��Ҫ��Ϊ��Ƶ�Ŀ�͸�
		r.right = r.right - r.left + 1;
		r.bottom = r.bottom - r.top + 3;
		r.left++;
		r.top++;
		r.top = r.top + 3;
		doSetVideoBoundsRect(r);
	}

}
