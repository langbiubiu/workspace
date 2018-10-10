package com.ipanel.join.cq.vod.player;

import java.text.SimpleDateFormat;
import java.util.Date;

import com.ipanel.join.cq.vod.utils.TimeUtility;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Paint.Align;
import android.graphics.Paint.Style;
import android.graphics.Path;
import android.util.AttributeSet;
import android.view.View;
import cn.ipanel.android.LogHelper;

public class ProgressView extends View {
	
	float left=20;
	float pleft=25;
	float top=0;
	float view_width=80;
	float view_height=25;
	float width= 960;//40;
	float height=35;
	//Paint p0 = new Paint();
	Paint p1 = new Paint();
	int mFocusColor = Color.parseColor("#ffffff");
	int mNormalColor = Color.parseColor("#88292c38");
	float size = 18;
	float density;
	Path path;
	long total_time;
	long current_time;
	long start_time;
	long end_time;
	public ProgressView(Context context, AttributeSet attrs) {
		super(context, attrs);
		start_time=System.currentTimeMillis();
		density = getResources().getDisplayMetrics().density;
		total_time=360;

		p1.setAntiAlias(true);
		p1.setColor(mFocusColor);
		p1.setStyle(Style.STROKE);
		p1.setTextSize(size);
		p1.setTextAlign(Align.LEFT);
	}
	public ProgressView(Context context) {
		super(context);
		start_time=System.currentTimeMillis();
		density = getResources().getDisplayMetrics().density;
		total_time=360;

		p1.setAntiAlias(true);
		p1.setColor(mFocusColor);
		p1.setStyle(Style.STROKE);
		p1.setTextSize(size);
		p1.setTextAlign(Align.LEFT);
	}
	
	public void init(long l1,long l2){
		this.start_time=l1;
		this.end_time=l2;
		this.total_time=l2-l1;
		if(total_time==0){
			total_time=1;
		}
	}
	
	public void calPosition(long l){

		if(l<start_time){
			current_time=0;
		}else{
			current_time=l;
		}
		
		float rate=(float)((current_time -start_time + 0.0)/(total_time+0.0));
		if(rate>1){
			rate=1;
		}   

		float tmp = width*rate;
		pleft = tmp;
		if(tmp<10){
			pleft=10;
		}else if(tmp>960){
			pleft=960;
		}
		
		LogHelper.v("rate ="+rate);

		
		  left=pleft-view_width/2;
		  left=pleft-10;
		
		if(left < 10){
		   left = 10;
		}else if(left>width){
			left=width-50;
		}
		
	//	Log.v("left= "+left);
		
		this.postInvalidate();
	}
	
	@Override
	protected void onDraw(Canvas canvas) {
		//Log.v("onDraw: left="+ left);
		canvas.drawColor(Color.TRANSPARENT);
		canvas.drawText(TimeUtility.formatTime(start_time+current_time), left, top+size-3, p1);
		super.onDraw(canvas);
	}
	
	public String getTimeFromLong(){
		 long l= (current_time)*1000l;

		SimpleDateFormat f2 = new SimpleDateFormat("HH:mm");
		Date d=new Date(l);
		return f2.format(d);
	}
	
	
	
	
	@Override
	protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
//		width = (int) (1280 * density);
//		height = (int) (35 * density);
//		if (View.MeasureSpec.getMode(widthMeasureSpec) == View.MeasureSpec.EXACTLY) {
//			width = View.MeasureSpec.getSize(widthMeasureSpec);
//		}
//		
//		if (View.MeasureSpec.getMode(heightMeasureSpec) == View.MeasureSpec.EXACTLY) {
//			height = View.MeasureSpec.getSize(heightMeasureSpec);
//		}
//		
//		widthMeasureSpec = View.MeasureSpec.makeMeasureSpec((int) width,
//				View.MeasureSpec.EXACTLY);
//		heightMeasureSpec = View.MeasureSpec.makeMeasureSpec((int) height,
//				View.MeasureSpec.EXACTLY);
//		setMeasuredDimension(widthMeasureSpec, heightMeasureSpec);
		super.onMeasure(widthMeasureSpec, heightMeasureSpec);
	}

}
