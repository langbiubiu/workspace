package com.ipanel.join.cq.vod.player;

import android.app.Instrumentation;
import android.os.Handler;
import android.view.KeyEvent;
import cn.ipanel.android.LogHelper;

/**
 * �㲥���� �ڵ㲥״̬��������롢�˳����϶���ѡʱ����ͣ�������Ȳ���
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

	// ����������ʹ�ã���ʽ����ע�͵�
	private Runnable mTestRunnable = new Runnable() {

		@Override
		public void run() {
			// TODO Auto-generated method stub
			LogHelper.i("test thread start");
			try {
				while (true) {
					Instrumentation inst = new Instrumentation();
					/*
					 * ��LEFT����
					 */
					inst.sendKeyDownUpSync(KeyEvent.KEYCODE_DPAD_LEFT);
					Thread.sleep(500);
					/*
					 * ��RIGHT�����
					 */
					inst.sendKeyDownUpSync(KeyEvent.KEYCODE_DPAD_RIGHT);
					Thread.sleep(500);

					/*
					 * ��UP������ѡʱ
					 */
					inst.sendKeyDownUpSync(KeyEvent.KEYCODE_DPAD_UP);
					Thread.sleep(500);
					/*
					 * ����300���ڵ����Ƶ���ţ�Ȼ��OK����������̨
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
					 * ��LEFT������
					 */
					inst.sendKeyDownUpSync(KeyEvent.KEYCODE_DPAD_LEFT);
					Thread.sleep(500);
					/*
					 * ��RIGHT�����
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
