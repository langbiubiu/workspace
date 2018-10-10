package com.ipanel.join.chongqing.live.receiver;

import com.ipanel.join.chongqing.live.book.Alarms;
import com.ipanel.join.chongqing.live.manager.BookManager;
import com.ipanel.join.chongqing.live.provider.TableUtils;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

public class AlarmClearReceiver extends BroadcastReceiver {

	@Override
	public void onReceive(Context context, Intent intent) {
		// TODO Auto-generated method stub
		//删除所有预约记录
		TableUtils.delete(context, BookManager.BOOK_CONTENT_URI, null, null);
		Alarms.setNextAlert(context);
	}

}
