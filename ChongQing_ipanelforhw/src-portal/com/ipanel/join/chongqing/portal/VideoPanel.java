package com.ipanel.join.chongqing.portal;

import android.content.Context;
import android.graphics.Rect;
import android.util.AttributeSet;
import android.view.View;

public class VideoPanel extends TeeveeWidgetHolder{
	
	
//	public VideoPanel(Context context) {
//		super(context);
//	}
	
	public VideoPanel(Context context, AttributeSet attrs) {
		super(context, attrs);
		updateLiveSmallVideoData();
		this.setOnFocusChangeListener(new OnFocusChangeListener() {
			
			@Override
			public void onFocusChange(View v, boolean hasFocus) {
				System.out.println("onFocuschange:---------------" + hasFocus);
				if(hasFocus){
					Rect r = new Rect();
					r.left = 290;
					r.top = 120;
					r.right = 1111;
					r.bottom = 586;
					hostView.setVideoBounds(r);
				}else{
					Rect r = new Rect();
					r.left = 330;
					r.top = 144;
					r.right = 1076;
					r.bottom = 562;
					hostView.setVideoBounds(r);
				}
			}
		});
	}

	@Override
	protected void onDetachedFromWindow() {
		super.onDetachedFromWindow();
		if (hostView != null) {
			hostView.toChannel("channelId://-1");
		}
	}

	@Override
	protected void onAttachedToWindow() {
		super.onAttachedToWindow();
		initRemoteView(getContext());
//		updateLiveSmallVideoData();
	}

	public void updateDisplay() {
		updateTvRect();
	}

}
