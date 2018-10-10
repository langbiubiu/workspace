package com.ipanel.join.chongqing.widget;


import java.text.SimpleDateFormat;
import java.util.Date;

import android.annotation.SuppressLint;
import android.content.Context;
import android.os.Handler;
import android.util.AttributeSet;
import android.widget.TextView;

@SuppressLint("SimpleDateFormat")
public class TimeTextView extends TextView{
	private static final String TAG="TimeTextView";
	TimeRunable timeRunable;
	Handler handler;
	public TimeTextView(Context context) {
		super(context);
		handler=new Handler();
		timeRunable=new TimeRunable();
	}
	
	public TimeTextView(Context context, AttributeSet attrs) {
		super(context, attrs);
		handler=new Handler();
		timeRunable=new TimeRunable();
	}

	public TimeTextView(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);
		handler=new Handler();
		timeRunable=new TimeRunable();
	}
	
	@Override
	protected void onAttachedToWindow() {
		handler.post(timeRunable);
		super.onAttachedToWindow();
	}

	@Override
	protected void onDetachedFromWindow() {
		handler.removeCallbacks(timeRunable);
		super.onDetachedFromWindow();
	}

	public class TimeRunable implements Runnable{

		@Override
		public void run() {
			SimpleDateFormat sd=new SimpleDateFormat("HH:mm");
			String time=sd.format(new Date());
			SimpleDateFormat sd2 = new SimpleDateFormat("MM/dd");
			String day = sd2.format(new Date());
			TimeTextView.this.setText(day+'\n'+time);
			handler.postDelayed(timeRunable, 1000);
		}
	}
	
}
