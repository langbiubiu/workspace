package com.ipanel.join.cq.vod.player;

import android.app.Instrumentation;
import android.os.Handler;
import android.view.KeyEvent;
import cn.ipanel.android.LogHelper;

/**
 * 点播拷机 在点播状态下随机进入、退出、拖动、选时、暂停、音量等操作
 */
public class PlayerTestCase {
	private Handler handler = new Handler();
	private boolean isVodTest = false;

	public void init() {
		if (isVodTest) {
			setKeyCode();
		}
	}

	public void stop() {
		if (isVodTest) {
			if(testThread != null && testThread.isAlive()){
				testThread.stop();
			}
		}
	}

	private void setKeyCode() {
		handler.postDelayed(new Runnable() {
			
			@Override
			public void run() {
				testThread.start();
			}
		}, 3000);
	}

	// 供拷机测试使用，正式版需注释掉
	private Runnable mTestRunnable = new Runnable() {

		@Override
		public void run() {
			// TODO Auto-generated method stub
			LogHelper.i("test thread start");
			try {
				while (true) {
					Instrumentation inst = new Instrumentation();
					/*
					 * 按LEFT快退
					 */
					inst.sendKeyDownUpSync(KeyEvent.KEYCODE_DPAD_LEFT);
					Thread.sleep(500);
					/*
					 * 按RIGHT键快进
					 */
					inst.sendKeyDownUpSync(KeyEvent.KEYCODE_DPAD_RIGHT);
					Thread.sleep(500);

					/*
					 * 按UP键进入选时
					 */
					inst.sendKeyDownUpSync(KeyEvent.KEYCODE_DPAD_UP);
					Thread.sleep(500);
					/*
					 * 输入300以内的随机频道号，然后按OK键，进行切台
					 */
					int channel = (int)(Math.random() * 300);
					int one = channel % 10;
					int ten = channel / 10 % 10;
					int	hundred = channel / 100;
					inst.sendKeyDownUpSync(KeyEvent.KEYCODE_0 + hundred);
					Thread.sleep(200);
					inst.sendKeyDownUpSync(KeyEvent.KEYCODE_0 + ten);
					Thread.sleep(200);
					inst.sendKeyDownUpSync(KeyEvent.KEYCODE_0 + one);
					Thread.sleep(200);
					inst.sendKeyDownUpSync(KeyEvent.KEYCODE_DPAD_CENTER);
					Thread.sleep(3000);
					/*
					 * 按LEFT键快退
					 */
					inst.sendKeyDownUpSync(KeyEvent.KEYCODE_DPAD_LEFT);
					Thread.sleep(500);
					/*
					 * 按RIGHT键快进
					 */
					inst.sendKeyDownUpSync(KeyEvent.KEYCODE_DPAD_RIGHT);
					Thread.sleep(500);

					inst.sendKeyDownUpSync(KeyEvent.KEYCODE_DPAD_CENTER);
					Thread.sleep(3000);
					
					inst.sendKeyDownUpSync(KeyEvent.KEYCODE_DPAD_CENTER);
					Thread.sleep(3000);
					
				}
			} catch (Exception e) {

			}
		}
	};
	private Thread testThread = new Thread(mTestRunnable);
}
