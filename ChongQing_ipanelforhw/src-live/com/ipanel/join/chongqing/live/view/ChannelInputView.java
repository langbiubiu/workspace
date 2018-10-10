package com.ipanel.join.chongqing.live.view;

import cn.ipanel.android.LogHelper;

import com.ipanel.chongqing_ipanelforhw.R;

import android.content.Context;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.RcKeyEvent;
import android.view.View;
import android.widget.RelativeLayout;
import android.widget.TextView;

public class ChannelInputView extends RelativeLayout {
	
	int[] channelNumber = new int[] { 0, 0, 0 };
	int[] limits = new int[]{ 9, 9, 9 };
	int position = 0; //0~2
	
	TextView  bai, shi, ge;
	Drawable mDrawNor, mDrawSelect;
	int  mColorNor, mColorSelect;
	
	private InputEnterListener listener;
	private InputCancelListener clistener;

	public ChannelInputView(Context context) {
		super(context);
		// TODO Auto-generated constructor stub
		View container = LayoutInflater.from(context).inflate(R.layout.live_channel_input_view, null);
		bai = (TextView) container.findViewById(R.id.timeinputText1);
		shi = (TextView) container.findViewById(R.id.timeinputText2);
		ge = (TextView) container.findViewById(R.id.timeinputText3);
		mDrawNor    = getResources().getDrawable(R.drawable.live_num_bg);
		mDrawSelect = getResources().getDrawable(R.drawable.live_num_focus);
		mColorNor  = Color.parseColor("#FFF0F5");
		mColorSelect = Color.parseColor("#0A0A0A");
		addView(container);
	}
	
	@Override
	public boolean onKeyDown(int keyCode, KeyEvent event) {
		// TODO Auto-generated method stub
		LogHelper.i("ChannelInputView onKeyDown " + keyCode);
		switch (keyCode) {
		case KeyEvent.KEYCODE_DPAD_LEFT:
			if (position == 0) {
				position = 2;
			} else {
				position--;
			}
			updateShows();
			break;
		case KeyEvent.KEYCODE_DPAD_UP:
			if(channelNumber[position] == limits[position]){
				channelNumber[position] = 0;
			}else{
				channelNumber[position] ++;
			}
			updateShows();
			break;
		case KeyEvent.KEYCODE_DPAD_DOWN://ÉÏÏÂ¼ü£¬ÇÐ»»Êý×Ö
			if(channelNumber[position] == 0){
				channelNumber[position] = limits[position];
			} else {
				channelNumber[position] --;
			}
			updateShows();
			break;
			
		case KeyEvent.KEYCODE_DPAD_RIGHT:
			if (position == 2) {
				position = 0;
			} else {
				position++;
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
		case KeyEvent.KEYCODE_0:
		case KeyEvent.KEYCODE_1:
		case KeyEvent.KEYCODE_2:
		case KeyEvent.KEYCODE_3:
		case KeyEvent.KEYCODE_4:
		case KeyEvent.KEYCODE_5:
		case KeyEvent.KEYCODE_6:
		case KeyEvent.KEYCODE_7:
		case KeyEvent.KEYCODE_8:
		case KeyEvent.KEYCODE_9:
			int value = keyCode - KeyEvent.KEYCODE_0;
			channelNumber[position] = value;
			if (position != 2) {
				position++;
			}
			updateShows();
			break;
		default:
			break;
		}
		return true;
	}
	
	public int parseIntFromArray() {
		return channelNumber[0] * 100 + channelNumber[1] * 10 + channelNumber[2];
	}

	public void clear() {
		position = 0;
		channelNumber = new int[] { 0, 0, 0 };
	}
	
	public void setEnterListener(InputEnterListener listener) {
		this.listener = listener;
	}

	public void setCancelListener(InputCancelListener listener) {
		this.clistener = listener;
	}

	public interface InputCancelListener {
		public void onCancel();
	}

	public interface InputEnterListener {
		public void onEnter();
	}
	
	public void updateShows(){
		bai.setText(""+channelNumber[0]);
		bai.setTextColor(mColorNor);
		shi.setText(""+channelNumber[1]);
		shi.setTextColor(mColorNor);
		ge.setText(""+channelNumber[2]);
		ge.setTextColor(mColorNor);
		
		bai.setBackgroundDrawable(mDrawNor);
		shi.setBackgroundDrawable(mDrawNor);
		ge.setBackgroundDrawable(mDrawNor);
	
		switch ( position) {
		case 0:
			bai.setTextColor(mColorSelect);
			bai.setBackgroundDrawable(mDrawSelect);
			break;
		case 1:
			shi.setTextColor(mColorSelect);
			shi.setBackgroundDrawable(mDrawSelect);
			break;
		case 2:
			ge.setTextColor(mColorSelect);
			ge.setBackgroundDrawable(mDrawSelect);
			break;
		default:
			break;
		}
	 }
}
