package com.ipanel.join.chongqing.widget;

import ipanel.join.widget.LinearLayout;

import android.content.Context;
import android.graphics.Color;
import android.os.Handler;
import android.os.Message;
import android.util.AttributeSet;
import android.view.Gravity;
import android.widget.TextView;
import cn.ipanel.android.net.http.RequestParams;

import com.ipanel.join.cqhome.view.WeatherView.Weather;
import com.ipanel.join.protocol.a7.ServiceHelper;
import com.ipanel.join.protocol.a7.ServiceHelper.ResponseHandlerT;
import com.ipanel.join.protocol.a7.ServiceHelper.SerializerType;

public class WeatherView2 extends LinearLayout{
	
	public static final String WEATHER_URL="http://125.62.46.202:8080/ChongQingWeatherReport_sj/ConnectWeather?version=3.0";
	
	private static final long HEART_BEAT = 2*60*1000;
	private TextView txt;
	
	private Context mContext;
	
	private Handler mHandler = new Handler(){
		@Override
		public void handleMessage(Message msg) {
			
			if(msg.what == 0){
				requestWeatherData();
			}
		}
	};

	public WeatherView2(Context context) {
		super(context);
		init(context);
	}
	public WeatherView2(Context context, AttributeSet attrs) {
		super(context, attrs);
		init(context);
	}

	@Override
	protected void onAttachedToWindow() {
		super.onAttachedToWindow();
		requestWeatherData();
		
		mHandler.sendEmptyMessageDelayed(0, HEART_BEAT);
	}
	
	@Override
	protected void onDetachedFromWindow() {
		super.onDetachedFromWindow();
		mHandler.removeMessages(0);
	}
	
	private void init(Context context){
		this.mContext = context;
		txt = new TextView(context);
		txt.setTextSize(21);
		txt.setTextColor(Color.parseColor("#FFb4b4b4"));
		LayoutParams tlp = new LayoutParams(android.view.ViewGroup.LayoutParams.WRAP_CONTENT,android.view.ViewGroup.LayoutParams.WRAP_CONTENT);
		tlp.gravity = Gravity.CENTER_VERTICAL|Gravity.LEFT;
		this.addView(txt, tlp);

	}
	
	
	private void requestWeatherData(){
		ServiceHelper helper=ServiceHelper.getHelper();
		helper.setSerializerType(SerializerType.JSON);
		helper.setRootUrl(WEATHER_URL);
		helper.callServiceAsync(WeatherView2.this.getContext(), new RequestParams(), Weather[].class, new ResponseHandlerT<Weather[]>() {
			@Override
			public void onResponse(boolean success, Weather[] result) {
				updateWeaView(result[0].getEstate() + " " + result[0].getTemperature());
				mHandler.sendEmptyMessageDelayed(0, HEART_BEAT);
			}
		});
	}
	
	private void updateWeaView(String weather) {
		txt.setText(weather);
		txt.setVisibility(VISIBLE);
		txt.requestLayout();
	}
}
