package com.ipanel.join.cqhome.view;

import java.text.SimpleDateFormat;
import java.util.Date;

import ipanel.join.configuration.View;
import ipanel.join.widget.TxtView;
import android.annotation.SuppressLint;
import android.content.Context;
import android.os.Handler;

@SuppressLint("SimpleDateFormat")
public class TimeTextView extends TxtView{
	private static final String TAG="TimeTextView";
	TimeRunable timeRunable;
	Handler handler;
	public TimeTextView(Context context, View data) {
		super(context, data);
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
			TimeTextView.this.setText(time+'\n'+day);
			handler.postDelayed(timeRunable, 1000);
		}
	}
	
}
