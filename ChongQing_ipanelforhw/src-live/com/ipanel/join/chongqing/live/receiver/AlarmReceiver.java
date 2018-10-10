package com.ipanel.join.chongqing.live.receiver;

import com.ipanel.join.chongqing.live.Constant;
import com.ipanel.join.chongqing.live.book.BookRemindService;
import com.ipanel.join.chongqing.live.data.BookData;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

public class AlarmReceiver extends BroadcastReceiver {

	@Override
	public void onReceive(Context context, Intent intent) {
		// TODO Auto-generated method stub
		System.out.println("receive the broadcast "+intent.getAction());
		    	
        if(Constant.ALARM_ALERT_ACTION.equals(intent.getAction())){
            Intent playAlarm = new Intent(context,BookRemindService.class);
            //防止出现android.os.BadParcelableException: ClassNotFoundException when unmarshalling错误
            //并没有什么卵用，注释
//    		intent.setExtrasClassLoader(BookData.class.getClassLoader());
            playAlarm.putExtra(Constant.ALARM_INTENT_EXTRA,intent.getExtras().getParcelable(Constant.ALARM_INTENT_EXTRA));
            playAlarm.putExtra("flag", 0);
            context.startService(playAlarm);
        }
	}

}
