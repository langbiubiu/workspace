package com.ipanel.join.chongqing.live.book;

import android.app.Service;
import android.content.Intent;
import android.graphics.PixelFormat;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnFocusChangeListener;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.TextView;
import cn.ipanel.android.LogHelper;

import com.ipanel.chongqing_ipanelforhw.R;
import com.ipanel.join.chongqing.live.Constant;
import com.ipanel.join.chongqing.live.data.BookData;
import com.ipanel.join.chongqing.live.util.TimeHelper;

public class BookRemindService extends Service {

	private BookData bookProgram;
	View mOverlay;
	WindowManager.LayoutParams params;
	WindowManager wm;
	Button in, out;
	int flag = 0;
	TextView event_name, event_time, now_time, month_time, count_down,count_down2;

	@Override
	public void onDestroy() {
		bookProgram = null;
		dismissOverlay();
		stopForeground(true);
		super.onDestroy();
	}

	@Override
	public int onStartCommand(Intent intent, int flags, int startId) {
		bookProgram = intent.getExtras().getParcelable(Constant.ALARM_INTENT_EXTRA);
		Log.i("lixby", "BookRemindService ----onStartCommand------"+bookProgram.getChannel_name());
		if (bookProgram != null) {
			Alarms.deleteBook(this,bookProgram);
			showOverlay();
		}
		return Service.START_NOT_STICKY;
	}

	public void dismissOverlay() {
		if (mOverlay != null && wm != null) {
			wm.removeView(mOverlay);
			wm = null;
			mOverlay = null;
		}
		mhandler.removeMessages(1);
		Alarms.setNextAlert(this);
	}

	public void showOverlay() {
		LogHelper.i("show book remind dialog");
		Log.d("lixby", "BookRemindService ----show book remind dialog------");
		dismissOverlay();
		flag = 0;
		params = new WindowManager.LayoutParams(WindowManager.LayoutParams.MATCH_PARENT,
				WindowManager.LayoutParams.MATCH_PARENT,
				WindowManager.LayoutParams.TYPE_SYSTEM_ALERT,
				WindowManager.LayoutParams.FLAG_NOT_TOUCH_MODAL, PixelFormat.TRANSLUCENT);
		params.gravity = Gravity.CENTER;
		mOverlay = LayoutInflater.from(this).inflate(R.layout.live_event_remind, null);
		in = (Button) mOverlay.findViewById(R.id.enter_remind);
		out = (Button) mOverlay.findViewById(R.id.out_remind);

//		channel_name = (TextView) mOverlay.findViewById(R.id.remind_channel);
		event_name = (TextView) mOverlay.findViewById(R.id.remind_event);
		event_time = (TextView) mOverlay.findViewById(R.id.remind_time);
		now_time = (TextView) mOverlay.findViewById(R.id.noow_time);
		month_time = (TextView) mOverlay.findViewById(R.id.am_pm);
		count_down = (TextView) mOverlay.findViewById(R.id.count_down);
		count_down2 = (TextView) mOverlay.findViewById(R.id.count_down2);

		event_time.setText(TimeHelper.getDetailTime(Long.parseLong(bookProgram.getStart_time())));
//		channel_name.setText(bookProgram.getChannel_name());
		event_name.setText(bookProgram.getChannel_name()+":"+bookProgram.getEvent_name());
		event_name.setSelected(true);
//		channel_name.setSelected(true);
		now_time.setText(TimeHelper.getNowTime());
		month_time.setText(TimeHelper.getMonth());
//		mOverlay.setFocusable(true);
//		mOverlay.requestFocus();
//
//		mOverlay.setOnKeyListener(new OnKeyListener() {
//
//			@Override
//			public boolean onKey(View v, int keyCode, KeyEvent event) {
//				if (event.getAction() == KeyEvent.ACTION_DOWN) {
//					resetHideTimer();
//					if (keyCode == KeyEvent.KEYCODE_DPAD_LEFT) {
//						flag = 0;
//						return true;
//					} else if (keyCode == KeyEvent.KEYCODE_DPAD_RIGHT) {
//						flag = 1;
//						return true;
//					}
//				} else if (event.getAction() == KeyEvent.ACTION_UP) {
//					if (keyCode == KeyEvent.KEYCODE_DPAD_CENTER) {
//						sendRefreshBroadcast();
//						dismissOverlay();
//						if (flag == 0) {
//							Intent intent = new Intent(BookRemindService.this, LiveForHomeActivity.class);
//							intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
//							intent.putExtra(Constant.LIVE_LAUNCH_TAG,Constant.ACTIVITY_LAUNCH_TYPE_FREQUENCE_PROGRAM);
//							intent.putExtra(Constant.LIVE_LAUNCH_FREQUENCE_TAG,bookProgram.getFrequency() + "");
//							intent.putExtra(Constant.LIVE_LAUNCH_PROGRAM_TAG,bookProgram.getProgram_number() + "");
//							startActivity(intent);
//						}
//					}
//				}
//				return false;
//			}
//		});
		in.requestFocus();
		in.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				sendRefreshBroadcast();
				dismissOverlay();
//				Intent intent = new Intent(BookRemindService.this, LiveForHomeActivity.class);
//				intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
//				intent.putExtra(Constant.LIVE_LAUNCH_TAG,Constant.ACTIVITY_LAUNCH_TYPE_FREQUENCE_PROGRAM);
//				intent.putExtra(Constant.LIVE_LAUNCH_FREQUENCE_TAG,bookProgram.getFrequency() + "");
//				intent.putExtra(Constant.LIVE_LAUNCH_PROGRAM_TAG,bookProgram.getProgram_number() + "");
//				startActivity(intent);
			}
		});
		out.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				sendRefreshBroadcast();
				dismissOverlay();
			}
		});
		in.setOnFocusChangeListener(new OnFocusChangeListener() {
			
			@Override
			public void onFocusChange(View v, boolean hasFocus) {
				if (hasFocus) {
					resetHideTimer();
				}
				
			}
		});
		out.setOnFocusChangeListener(new OnFocusChangeListener() {
			
			@Override
			public void onFocusChange(View v, boolean hasFocus) {
				if (hasFocus) {
					resetHideTimer();
				}
				
			}
		});

		wm = (WindowManager) getSystemService(WINDOW_SERVICE);
		wm.addView(mOverlay, params);
		resetHideTimer();
		mhandler.sendEmptyMessage(2);
		
	}

	private void resetHideTimer() {
		mhandler.removeMessages(1);
		mhandler.sendEmptyMessageDelayed(1, 30 * 1000);
	}

	private Handler mhandler = new Handler() {
		public void handleMessage(android.os.Message msg) {
			switch (msg.what) {
			case 1:
				sendRefreshBroadcast();
				dismissOverlay();
				break;
			case 2:
				String show=getCountDown();
				count_down.setText(show);

				if(!"".equals(show)){
					count_down2.setVisibility(View.VISIBLE);
					if("0".equals(show)){
						sendRefreshBroadcast();
						dismissOverlay();
					}else{
						mhandler.sendEmptyMessageDelayed(2, 1000);
					}
				}else{
					count_down2.setVisibility(View.INVISIBLE);
				}
			default:
				break;
			}
		}
	};
	private String getCountDown(){
		if (bookProgram != null) {
			long time=(System.currentTimeMillis()-Long.parseLong(bookProgram.getStart_time()))/1000;
			if(time>0){
				return "";
			}else{
				return Math.abs(time)+"";
			}
		}else{
			return "";
		}
	}

	@Override
	public IBinder onBind(Intent intent) {
		// TODO Auto-generated method stub
		return null;
	}

	private void sendRefreshBroadcast() {
		if (bookProgram != null) {
			Intent intent = new Intent();
			intent.setAction(Constant.ALARM_RMIND_REFRESH);
			Bundle bundle = new Bundle();
			bundle.putString("remind_freq", bookProgram.getFrequency() + "");
			bundle.putString("remind_time", bookProgram.getStart_time() + "");
			bundle.putString("remind_prog", bookProgram.getProgram_number() + "");
			intent.putExtras(bundle);
			this.sendBroadcast(intent);
		}
	}
}
