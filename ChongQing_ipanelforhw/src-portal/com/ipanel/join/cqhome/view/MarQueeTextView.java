package com.ipanel.join.cqhome.view;

import android.content.Context;
import android.text.TextUtils.TruncateAt;
import android.view.ViewDebug.ExportedProperty;
import ipanel.join.configuration.View;
import ipanel.join.widget.TxtView;

public class MarQueeTextView extends TxtView {
    
	public MarQueeTextView(Context context, View data) {
		super(context, data);
		
		MarQueeTextView.this.setEllipsize(TruncateAt.MARQUEE);
		MarQueeTextView.this.setSingleLine(true);
		MarQueeTextView.this.setHorizontallyScrolling(true);
		MarQueeTextView.this.setMarqueeRepeatLimit(-1);
		
	}
	
	@Override
	@ExportedProperty(category = "focus")
	public boolean isFocused() {
		return true;
	}

}
