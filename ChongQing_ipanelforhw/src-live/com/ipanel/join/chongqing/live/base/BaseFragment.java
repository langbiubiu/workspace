package com.ipanel.join.chongqing.live.base;

import android.view.KeyEvent;
import android.view.RcKeyEvent;

import com.ipanel.join.chongqing.live.Constant;
import com.ipanel.join.chongqing.live.LiveActivity;
import com.ipanel.join.chongqing.live.manager.SettingManager;
import com.ipanel.join.chongqing.live.manager.UIManager;


public abstract class BaseFragment extends UIFragment {

	protected final int DEFAULT_ANIMATION_TIME=400;
	public final static int NAV_MESSAGE_TIME_TICK = 7;

	public LiveActivity getLiveActivity() {
		return (LiveActivity) getActivity();
	}

	public UIManager getUIManager() {
		return getLiveActivity().getUIManager();
	}
	
	public SettingManager getVolumeManager(){
		return getLiveActivity().getSettingManager();
	}
	
	public final int SCALE(int value){
		return (int) (Constant.DENSITY*value);
	}
	
	public boolean generalKeyHandle(int keyCode, KeyEvent event) {
		switch(keyCode){
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
		case KeyEvent.KEYCODE_VOLUME_DOWN:
		case KeyEvent.KEYCODE_VOLUME_UP:
		case KeyEvent.KEYCODE_VOLUME_MUTE:
		case KeyEvent.KEYCODE_MUTE:
		case KeyEvent.KEYCODE_DPAD_DOWN:
		case KeyEvent.KEYCODE_DPAD_UP:
		case KeyEvent.KEYCODE_DPAD_LEFT:
		case KeyEvent.KEYCODE_DPAD_RIGHT:
		case KeyEvent.KEYCODE_DPAD_CENTER:
		case KeyEvent.KEYCODE_BACK:
		case RcKeyEvent.KEYCODE_MENU:
		case Constant.KEY_VALUE_OF_SEEK:
		case Constant.KEY_VALUE_OF_SEEK_F3:
		case RcKeyEvent.KEYCODE_TIME_SELECTE:
		case RcKeyEvent.KEYCODE_CH_DOWN:
		case RcKeyEvent.KEYCODE_CH_UP:

			return true;
		}
		return false;
	}
}
