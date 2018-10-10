package com.ipanel.join.chongqing.live.view;

import java.util.Calendar;
import java.util.Date;

import android.content.Context;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.RcKeyEvent;
import android.view.View;
import android.widget.RelativeLayout;
import android.widget.TextView;
import cn.ipanel.android.LogHelper;

import com.ipanel.chongqing_ipanelforhw.R;


public class TimeInputView extends RelativeLayout {
	int[] seekTime = new int[] { 0, 0, 0, 0 };
	int[] limits = new int[]{2,9,5,9};
	int seekTime_guard = 0;
	
	long start_time;
	long end_time;
	
	TextView  hour1,hour2,minute1,minute2;
	Drawable mDrawNor, mDrawSelect;
	int  mColorNor, mColorSelect;

	private InputEnterListener listener;
	private InputCancelListener clistener;
     
	int startPos = 0;//初始焦点位置。默认从0开始
	public TimeInputView(Context context) {   
		super(context);
		View mView = LayoutInflater.from(context).inflate(R.layout.vod_time_input_view, null);
		addView(mView);
		hour1   = (TextView)mView.findViewById(R.id.timeinputText1);
		hour2   = (TextView)mView.findViewById(R.id.timeinputText2);
		minute1   = (TextView)mView.findViewById(R.id.timeinputText3);
		minute2   = (TextView)mView.findViewById(R.id.timeinputText4);
		mDrawNor    = getResources().getDrawable(R.drawable.live_num_bg);
		mDrawSelect = getResources().getDrawable(R.drawable.live_num_focus);
		mColorNor  = Color.parseColor("#FFF0F5");
		mColorSelect = Color.parseColor("#0A0A0A");
	}
	
	public void init(long l1,long l2){
		LogHelper.v("TimeInputView init: start_time="+l1 +"; end_time="+ l2);
		this.start_time=l1;
		this.end_time=l2;
		getLimits();
	}
	
	public void setEnterListener(InputEnterListener listener) {
		this.listener = listener;
	}

	public void setCancelListener(InputCancelListener listener) {
		this.clistener = listener;
	}

	@Override
	public boolean onKeyDown(int keyCode, KeyEvent event) {
		
		LogHelper.v("TimeInputView onKeyDown keyCode="+keyCode);
		switch (keyCode) {
		case KeyEvent.KEYCODE_DPAD_LEFT:
			if (seekTime_guard == startPos) {
				seekTime_guard = 3;
			} else {
				seekTime_guard--;
			}
			updateShows();
			break;
		case KeyEvent.KEYCODE_DPAD_UP:
			if(seekTime[seekTime_guard] == limits[seekTime_guard]){
				seekTime[seekTime_guard] = 0;
			}else{
				seekTime[seekTime_guard] ++;
			}
			updateShows();
			break;
		case KeyEvent.KEYCODE_DPAD_DOWN://上下键，切换数字
			if(seekTime[seekTime_guard] == 0){
				seekTime[seekTime_guard] = limits[seekTime_guard];
			}else{
				seekTime[seekTime_guard] --;
			}
			updateShows();
			break;
			
		case KeyEvent.KEYCODE_DPAD_RIGHT:
			if (seekTime_guard == 3) {
				seekTime_guard = startPos;
			} else {
				seekTime_guard++;
			}
			updateShows();
			break;
		case KeyEvent.KEYCODE_ENTER:
		case KeyEvent.KEYCODE_DPAD_CENTER:
			if (listener != null) {
				listener.onEnter();
			}
			clear();
			this.setVisibility(View.INVISIBLE);
			break;
			
		case KeyEvent.KEYCODE_BACK:
		case RcKeyEvent.KEYCODE_QUIT:
			if (clistener != null) {
				clistener.onCancel();
			}
			clear();
			this.setVisibility(View.INVISIBLE);
			break;
			
//		case KeyEvent.KEYCODE_0:
//		case KeyEvent.KEYCODE_1:
//		case KeyEvent.KEYCODE_2:
//		case KeyEvent.KEYCODE_3:
//		case KeyEvent.KEYCODE_4:
//		case KeyEvent.KEYCODE_5:
//		case KeyEvent.KEYCODE_6:
//		case KeyEvent.KEYCODE_7:
//		case KeyEvent.KEYCODE_8:
//		case KeyEvent.KEYCODE_9:
//			int value = keyCode - KeyEvent.KEYCODE_0;
//			if(seekTime_guard==1&&seekTime[0]==2&&value>3){
//				SimplePlayerActivity activity = (SimplePlayerActivity) getContext();
//				activity.prompt
//						.showDefaultDialog(R.string.inpout_invaild_msg);
//				return true;
//			}else{
//				if(value>limits[seekTime_guard]){
//					SimplePlayerActivity activity = (SimplePlayerActivity) getContext();
//					activity.prompt
//							.showDefaultDialog(R.string.inpout_invaild_msg);
//					return true;
//				}
//			}
//			seekTime[seekTime_guard] = value;
//			if (seekTime_guard == 3) {
////				if (listener != null) {
////					listener.onEnter();
////				}
////				clear();
////				this.setVisibility(View.INVISIBLE);
//			} else {
//				seekTime_guard++;
//			}
//			updateShows();
   
//			break;
		default:
			break;
		}
		return true;
	}   
	
	public void initValue(int value){
		seekTime_guard = startPos;
		LogHelper.d("wuhd", "initValue"+value);
		if(value>limits[seekTime_guard]){
//			SimplePlayerActivity activity = (SimplePlayerActivity) getContext();
//			activity.prompt.showDefaultDialog(R.string.inpout_invaild_msg);
		}else{
			seekTime[seekTime_guard] = value;
			seekTime_guard++;
		}
		updateShows();
	}

	public Long parseIntArrayTOLong() {
		long result = 0L;
		// 单位为秒, 如单位变化此处也需要变化.
		result = (seekTime[0] * 10 + seekTime[1]) * 60 * 60L;
		result += (seekTime[2] * 10 + seekTime[3]) * 60;
		Date date = new Date();
		Calendar c = Calendar.getInstance();
		c.setTime(date);
		c.set(Calendar.HOUR_OF_DAY, 0);
		c.set(Calendar.MINUTE, 0);
		c.set(Calendar.SECOND, 0);
		
        //result = result + c.getTimeInMillis() / 1000l;  //回看播放服务返回非1970时间
		
	    for(int i=0; i< seekTime.length ; i++){
	    	LogHelper.v("seekTime:="+seekTime[i]);
	    }
	    
	    LogHelper.e("parseIntArrayTOLong: result ="+result  +";end_time= "+end_time);
	    
		if(result >= end_time || result <= start_time){
			result = -1l;
		}
		return result;
	}

	public void clear() {
		LogHelper.e(" TimeInputView clear");
		seekTime_guard = 0;
		seekTime = new int[] { 0, 0, 0, 0 };
	}

	public interface InputCancelListener {
		public void onCancel();
	}

	public interface InputEnterListener {
		public void onEnter();
	}
	
	public void updateShows(){
		hour1.setText(""+seekTime[0]);
		hour1.setTextColor(mColorNor);
		hour2.setText(""+seekTime[1]);
		hour2.setTextColor(mColorNor);
		minute1.setText(""+seekTime[2]);
		minute1.setTextColor(mColorNor);
		minute2.setText(""+seekTime[3]);
		minute2.setTextColor(mColorNor);
		
		hour1.setBackgroundDrawable(mDrawNor);
		hour2.setBackgroundDrawable(mDrawNor);
		minute1.setBackgroundDrawable(mDrawNor);
		minute2.setBackgroundDrawable(mDrawNor);
	
		switch ( seekTime_guard) {
		case 0:
			hour1.setTextColor(mColorSelect);
			hour1.setBackgroundDrawable(mDrawSelect);
			break;
		case 1:
			hour2.setTextColor(mColorSelect);
			hour2.setBackgroundDrawable(mDrawSelect);
			break;
		case 2:
			minute1.setTextColor(mColorSelect);
			minute1.setBackgroundDrawable(mDrawSelect);
			break;
		case 3:   
			minute2.setTextColor(mColorSelect);
			minute2.setBackgroundDrawable(mDrawSelect);
			break;
		default:
			break;
		}
	 }
	
	private void getLimits(){
		//endtime时长为秒
		Date end = new Date(end_time * 1000);
		Calendar c = Calendar.getInstance();
		c.setTime(end);
//		int h = (int) (end_time / 3600);//小时
//		int m = (int) ((end_time % 3600) / 60);//分钟
		int h = c.get(Calendar.HOUR_OF_DAY);
		int m = c.get(Calendar.MINUTE);
		if(h >= 10){
			startPos = 0;
			limits[0] = h / 10;
		}else if(h > 0 && h < 10){
			startPos = 1;
			limits[1] = h;
		}else if(h == 0){
			if(m >= 10){
				startPos = 2;
				limits[2] = m / 10;
			}else{
				startPos = 3;
				limits[3] = m;
			}
		}
	}
}
