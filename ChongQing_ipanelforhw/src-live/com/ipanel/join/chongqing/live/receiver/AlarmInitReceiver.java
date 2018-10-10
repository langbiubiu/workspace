package com.ipanel.join.chongqing.live.receiver;

import com.ipanel.join.chongqing.live.book.Alarms;

import cn.ipanel.android.LogHelper;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

public class AlarmInitReceiver extends BroadcastReceiver {

	@Override
	public void onReceive(Context context, Intent intent) {
		// TODO Auto-generated method stub
		String action = intent.getAction();
        LogHelper.v("AlarmInitReceiver" + action);
        LogHelper.e("set remind after reboot");

        if (context.getContentResolver() == null) {
            LogHelper.e("AlarmInitReceiver: FAILURE unable to get content resolver.  Alarms inactive.");
            return;
        }
        Alarms.setNextAlert(context);
	}

}
