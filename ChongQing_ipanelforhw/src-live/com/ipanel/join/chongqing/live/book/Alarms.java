/*
 * Copyright (C) 2007 The Undried Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.ipanel.join.chongqing.live.book;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.util.Log;
import cn.ipanel.android.LogHelper;

import com.ipanel.join.chongqing.live.Constant;
import com.ipanel.join.chongqing.live.data.BookData;
import com.ipanel.join.chongqing.live.manager.BookManager;
import com.ipanel.join.chongqing.live.provider.TableUtils;

/**
 * The Alarms provider supplies info about Alarm Clock settings
 */
public class Alarms {

	public static void setNextAlert(final Context context) {
		disableAlert(context);
		List<BookData> tmps = TableUtils.query(context, BookManager.BOOK_CONTENT_URI, BookData.class);
		List<BookData> list = new ArrayList<BookData>();
		for (BookData book : tmps) {
			long l1 = Long.parseLong(book.getStart_time());
			long l2 = l1 + Long.parseLong(book.getDuration());
			long l3 = System.currentTimeMillis();
			if (l2 < l3) {
				deleteBook(context,book);
			} else if (l1 > l3) {
				list.add(book);
			}
		}
		Collections.sort(list, new ComparatorBookProgram());
		int length = list.size();
		if (length == 0) {
			LogHelper.i("no valid event to book");
			return;
		}

		BookData tmp = list.get(0);
		long atTimeInMillis = Long.parseLong(tmp.getStart_time()) - 30 * 1000;
		Intent intent = new Intent(Constant.ALARM_ALERT_ACTION);
		intent.putExtra(Constant.ALARM_INTENT_EXTRA, tmp);
		AlarmManager am = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
		PendingIntent sender = PendingIntent.getBroadcast(context, 0, intent,
				PendingIntent.FLAG_CANCEL_CURRENT);
		if(Constant.DEVELOPER_MODE){
			atTimeInMillis = System.currentTimeMillis() + 30 * 1000;
		}
		am.set(AlarmManager.RTC_WAKEUP, atTimeInMillis, sender);
		LogHelper.e("do remind set");
	}
	public static boolean deleteBook(Context context,BookData book) {
		// TODO Auto-generated method stub
		TableUtils.delete(context, BookManager.BOOK_CONTENT_URI, "program_number=" + "'"
				+ book.getProgram_number() + "'" + " AND " + "start_time=" + "'" + book.getStart_time() + "'",
				null);
		Log.e("lixby", "deleteBook------"+book.getChannel_name());
		return true;
	}
	static void disableAlert(Context context) {
		AlarmManager am = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
		PendingIntent sender = PendingIntent.getBroadcast(context, 0, new Intent(
				Constant.ALARM_ALERT_ACTION), PendingIntent.FLAG_CANCEL_CURRENT);
		am.cancel(sender);
	}

	public static class ComparatorBookProgram implements Comparator<Object> {

		public int compare(Object arg0, Object arg1) {
			BookData program0 = (BookData) arg0;
			BookData program1 = (BookData) arg1;

			return (int) (Long.parseLong(program0.getStart_time()) - Long.parseLong(program1
					.getStart_time()));
		}
	}
}
