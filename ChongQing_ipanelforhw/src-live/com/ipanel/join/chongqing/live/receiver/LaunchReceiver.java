package com.ipanel.join.chongqing.live.receiver;

import com.ipanel.join.chongqing.live.book.Alarms;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import cn.ipanel.android.LogHelper;

public class LaunchReceiver extends BroadcastReceiver {

	@Override
	public void onReceive(Context context, Intent intent) {
		LogHelper.i("receive the boot broadcat");
		Alarms.setNextAlert(context);
	}

}
