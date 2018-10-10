package cn.ipanel.android.widget;

import android.content.Context;
import android.util.AttributeSet;
import cn.ipanel.android.widget.WeightGridLayout;

public class MyWeightGridLayout extends WeightGridLayout {

	public MyWeightGridLayout(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);
		// TODO Auto-generated constructor stub
	}

	public MyWeightGridLayout(Context context) {
		super(context);
		// TODO Auto-generated constructor stub
	}

	public MyWeightGridLayout(Context context, AttributeSet attrs) {
		super(context, attrs);
		// TODO Auto-generated constructor stub
	}

	@Override
	public boolean arrowScroll(int direction) {
	//	super.arrowScroll(direction);
		return false;
	}

}
