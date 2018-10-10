package com.ipanel.join.chongqing.widget;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

import com.ipanel.chongqing_ipanelforhw.R;


import android.annotation.SuppressLint;
import android.content.Context;
import android.os.Handler;
import android.os.Message;
import android.util.AttributeSet;
import android.widget.TextView;

public class DateTextView extends TextView {
	
	private MyHandler mHandler;
	private TimeRunable mTimeRunable;
    private Context cext;
	public DateTextView(Context context) {
		super(context);
		cext=context;
		initTimeView();
	}
		

	public DateTextView(Context context, AttributeSet attrs) {
		super(context, attrs);
		cext=context;
		initTimeView();
	}

	public DateTextView(Context context, AttributeSet attrs, int defStyle) {
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
			Date d = new Date();
			SimpleDateFormat dateformat = new SimpleDateFormat("yyyy/MM/dd");
			String date = dateformat.format(d);
			String week[] = getResources().getStringArray(R.array.week);
			Calendar cal = Calendar.getInstance();
			cal.setTime(d);
			int w = cal.get(Calendar.DAY_OF_WEEK) - 1;
			if(w < 0)
				w = 0;
			DateTextView.this.setText(date + " " + week[w]);
			mHandler.postDelayed(mTimeRunable, 1000);

		}
		
	} 

}
