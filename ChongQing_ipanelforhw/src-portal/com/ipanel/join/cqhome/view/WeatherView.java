package com.ipanel.join.cqhome.view;

import ipanel.join.configuration.View;
import ipanel.join.widget.LinearLayout;

import java.io.Serializable;
import java.util.Timer;
import java.util.TimerTask;

import android.annotation.TargetApi;
import android.content.Context;
import android.graphics.Color;
import android.os.Build;
import android.util.AttributeSet;
import android.view.Gravity;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.LinearLayout.LayoutParams;
import cn.ipanel.android.LogHelper;
import cn.ipanel.android.net.cache.JSONApiHelper;
import cn.ipanel.android.net.http.RequestParams;
import cn.ipanel.android.net.imgcache.SharedImageFetcher;

import com.ipanel.chongqing_ipanelforhw.R;
import com.ipanel.join.protocol.a7.ServiceHelper;
import com.ipanel.join.protocol.a7.ServiceHelper.ResponseHandlerT;
import com.ipanel.join.protocol.a7.ServiceHelper.SerializerType;

public class WeatherView extends LinearLayout{
	
	public static final String WEATHER_URL="http://125.62.46.202:8080/ChongQingWeatherReport_sj/ConnectWeather?version=3.0";
	
	public static final String WEATHER_ICON_URL="http://125.62.46.202:8080/ChongQingWeatherReport_sj/weather/";
	
	private Timer mTimer;
	private TimerTask mTickTask;
	private long last_request_time;
	private static final long HEART_BEAT=10*1000;
	private static final long UPDATE_PERIOD=60*1000;
	private static final int WEATHER_STATE_TODAY=0;
	private static final int WEATHER_STATE_TOMORROW=1;
	private int state=WEATHER_STATE_TODAY;
	private Weather[] weathers;
	private ImageView icon;
	private TextView txt;
	private TextView txtWea;

	public WeatherView(Context context, View data) {
		super(context, data);
		init(context);
	}
	public WeatherView(Context context, AttributeSet attrs) {
		super(context, attrs);
		init(context);
	}

	@Override
	protected void onAttachedToWindow() {
		super.onAttachedToWindow();
		if(mTickTask!=null){
			mTickTask.cancel();
		}
		mTimer.schedule(createTask(), 0, HEART_BEAT);
	}
	
	@Override
	protected void onDetachedFromWindow() {
		super.onDetachedFromWindow();
		if(mTickTask!=null){
			mTickTask.cancel();
		}
	}
	
	private void init(Context context){
		txtWea = new TextView(context);
		txtWea.setTextSize(17);
		txtWea.setTextColor(getResources().getColor(R.color.live_color));
		LayoutParams ilp=new LayoutParams(android.view.ViewGroup.LayoutParams.WRAP_CONTENT,android.view.ViewGroup.LayoutParams.WRAP_CONTENT);
		ilp.gravity=Gravity.CENTER_VERTICAL|Gravity.LEFT;
		this.addView(txtWea, ilp);
		txt=new TextView(context);
		txt.setTextSize(17);
		txt.setTextColor(getResources().getColor(R.color.live_color));
		LayoutParams tlp=new LayoutParams(android.view.ViewGroup.LayoutParams.WRAP_CONTENT,android.view.ViewGroup.LayoutParams.WRAP_CONTENT);
		tlp.gravity=Gravity.CENTER_VERTICAL|Gravity.RIGHT;
		tlp.topMargin = -4;
		this.addView(txt, tlp);
		this.setVisibility(GONE);
		mTimer = new Timer();
	}
	
	private TimerTask createTask(){

		mTickTask = new TimerTask() {
			@Override
			public void run() {
				state=(state+1)%2;
				if(System.currentTimeMillis()-last_request_time>UPDATE_PERIOD&&JSONApiHelper.isOnline(getContext())){
					LogHelper.i("----- 1");
					requestWeatherData();
				}else{
					LogHelper.i("----- 2");
					post(new Runnable() {
						@Override
						public void run() {
							caculateShow();
						}
					});
				}

			}
		};
		return mTickTask;
	}
	
	private void requestWeatherData(){
		ServiceHelper helper=ServiceHelper.getHelper();
		helper.setSerializerType(SerializerType.JSON);
		helper.setRootUrl(WEATHER_URL);
		helper.callServiceAsync(WeatherView.this.getContext(), new RequestParams(), Weather[].class, new ResponseHandlerT<Weather[]>() {
			@Override
			public void onResponse(boolean success, Weather[] result) {
				if(success&&result!=null){
					last_request_time=System.currentTimeMillis();
				}
				weathers=result;
				caculateShow();
			}
		});
	}
	
	@TargetApi(Build.VERSION_CODES.HONEYCOMB_MR1)
	private void caculateShow(){
		if(weathers!=null&&weathers.length==2&&weathers[state]!=null&&JSONApiHelper.isOnline(getContext())){
			LogHelper.i("----- 3");
			this.setVisibility(VISIBLE);
			this.setAlpha(0f);
			this.animate().alpha(1f);
			txt.setText((state==0?"今天  ":"明天  ") +" "+weathers[state].getTemperature());
			txtWea.setText(weathers[state].getEstate());
			SharedImageFetcher.getSharedFetcher(getContext()).loadImage(WEATHER_ICON_URL+weathers[state].getImg(), icon);
		}else{
			LogHelper.i("----- 4");
			this.setVisibility(GONE);
		}

	}
	
	public static class Weather implements Serializable{

		/**
		 * 
		 */
		private static final long serialVersionUID = -5512753579781733258L;
		
		private String img; 
		private String date; 
		private String temperature; 
		private String estate;
		public String getImg() {
			return img;
		}
		public void setImg(String img) {
			this.img = img;
		}
		public String getDate() {
			return date;
		}
		public void setDate(String date) {
			this.date = date;
		}
		public String getTemperature() {
			return temperature;
		}
		public void setTemperature(String temperature) {
			this.temperature = temperature;
		}
		public String getEstate() {
			return estate;
		}
		public void setEstate(String estate) {
			this.estate = estate;
		} 

		

		
	}

	
     
	
	
}
