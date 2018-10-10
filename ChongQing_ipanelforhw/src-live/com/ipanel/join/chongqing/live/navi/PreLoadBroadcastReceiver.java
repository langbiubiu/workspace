package com.ipanel.join.chongqing.live.navi;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

import com.ipanel.join.chongqing.live.LiveApp;

/**
 * ����Ԥ����Live Navi���ݵĹ㲥
 */
public class PreLoadBroadcastReceiver extends BroadcastReceiver {

	@Override
	public void onReceive(Context c, Intent i) {
		try {
			LiveNavigator navi = LiveApp.getInstance().getWasuLiveNavigator();
			navi.preload();// ���첽��������
			Log.d("NaviPreLoadBroadcastReceiver", "preload...");
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

}
