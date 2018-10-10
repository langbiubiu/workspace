package com.ipanel.join.chongqing.widget;

import java.text.SimpleDateFormat;
import java.util.Date;
import android.annotation.SuppressLint;
import android.content.Context;
import android.os.Handler;
import android.os.Message;
import android.util.AttributeSet;
import android.widget.TextView;

public class TimeTextView2 extends TextView {
	
	private MyHandler mHandler;
	private TimeRunable mTimeRunable;
    private Context cext;
	public TimeTextView2(Context context) {
		super(context);
		cext=context;
		initTimeView();
	}
		

	public TimeTextView2(Context context, AttributeSet attrs) {
		super(context, attrs);
		cext=context;
		initTimeView();
	}

	public TimeTextView2(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);
		cext=context;
		initTimeView();
	}
	
	private void initTimeView(){
		mHandler=new MyHandler();
		mTimeRunable=new TimeRunable();
		mHandler.post(mTimeRunable);
		
	}
	
	@Override
	protected void onAttachedToWindow() {
		mHandler.removeCallbacks(mTimeRunable);
		mHandler.post(mTimeRunable);
		super.onAttachedToWindow();
	}
	
	
	@Override
	protected void onDetachedFromWindow() {
		mHandler.removeCallbacks(mTimeRunable);
		super.onDetachedFromWindow();
	}
	
	
	@SuppressLint("HandlerLeak")
	private class MyHandler extends Handler{
		@Override
		public void handleMessage(Message msg) {
			super.handleMessage(msg);
			
		}
	}
	
	@SuppressLint("SimpleDateFormat")
	private class TimeRunable implements Runnable{

		@Override
		public void run() {
			SimpleDateFormat dateformat = new SimpleDateFormat("HH:mm");
			String a = dateformat.format(new Date());
			TimeTextView2.this.setText(a);
			mHandler.postDelayed(mTimeRunable, 1000);

		}
		
	} 

}
