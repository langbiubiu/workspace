package com.ipanel.join.chongqing.widget;

import android.content.Context;
import android.text.TextUtils.TruncateAt;
import android.util.AttributeSet;
import android.view.ViewDebug.ExportedProperty;
import android.widget.TextView;

public class MarQueeTextView extends TextView {

	public MarQueeTextView(Context context) {
		super(context);
		inite();
	}

	public MarQueeTextView(Context context, AttributeSet attrs) {
		super(context, attrs);
		inite();
	}

	
	
	public MarQueeTextView(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);
		inite();
	}
	
	
	private void inite(){
		MarQueeTextView.this.setEllipsize(TruncateAt.MARQUEE);
		MarQueeTextView.this.setSingleLine(true);
		MarQueeTextView.this.setHorizontallyScrolling(true);
		MarQueeTextView.this.setMarqueeRepeatLimit(-1);
		MarQueeTextView.this.setSelected(true);
		
	}
	
	@Override
	@ExportedProperty(category = "focus")
	public boolean isFocused() {
		// TODO Auto-generated method stub
		return true;
	}
	
	

}
