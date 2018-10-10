package com.ipanel.join.chongqing.portal;


import com.ipanel.chongqing_ipanelforhw.R;

import android.app.Activity;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;

public class BaseActivity extends Activity {

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setTheme(R.style.AppTheme);
	}

	@Override
	protected void onResume() {
		_handler.removeMessages(_MSG_HIDE);
		super.onResume();
		getWindow().getDecorView().setAlpha(1);
	}

	@Override
	protected void onPause() {
		super.onPause();
		if(useDelayedHide())
			delayedHide();
		else
			getWindow().getDecorView().setAlpha(0);
	}
	
	protected boolean useDelayedHide(){
		return true;
	}
	
	private void delayedHide(){
		_handler.removeMessages(_MSG_HIDE);
		_handler.sendEmptyMessageDelayed(_MSG_HIDE, 400);
	}
	
	private static final int _MSG_HIDE = 1;
	Handler _handler = new Handler(){

		@Override
		public void handleMessage(Message msg) {
			switch (msg.what) {
			case _MSG_HIDE:
				getWindow().getDecorView().setAlpha(0);
				break;
			}
		}
		
	};

}
