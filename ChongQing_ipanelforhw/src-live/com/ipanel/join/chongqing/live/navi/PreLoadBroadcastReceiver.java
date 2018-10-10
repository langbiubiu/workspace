package com.ipanel.join.chongqing.live.navi;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

import com.ipanel.join.chongqing.live.LiveApp;

/**
 * 接受预加载Live Navi数据的广播
 */
public class PreLoadBroadcastReceiver extends BroadcastReceiver {

	@Override
	public void onReceive(Context c, Intent i) {
		try {
			LiveNavigator navi = LiveApp.getInstance().getWasuLiveNavigator();
			navi.preload();// 会异步加载数据
			Log.d("NaviPreLoadBroadcastReceiver", "preload...");
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

}
