package com.ipanel.chongqing_ipanelforhw;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

import com.ipanel.join.cq.vod.jsondata.GlobalFilmData;
import com.ipanel.join.cq.vod.utils.Logger;

/**
 * EPG地址广播接收器
 * */
public class EPGReceiver extends BroadcastReceiver {

	@Override
	public void onReceive(Context context, Intent intent) {
			Logger.d("已经接收到更新广播CookieString = " + intent.getExtras().getString("CookieString").toString());
			GlobalFilmData.getInstance().setCookieString(intent.getExtras().getString("CookieString"));
			String epg = intent.getExtras().getString("EPG");
			String serviceGroupId = "" + intent.getExtras().getLong("ServiceGroupId");
			String smartcard = intent.getExtras().getString("smartcard");
			String authToken = intent.getStringExtra("authToken");

			GlobalFilmData.getInstance().setEpgUrl(epg);
			GlobalFilmData.getInstance().setGroupServiceId(serviceGroupId);
			GlobalFilmData.getInstance().setCardID(smartcard);
			GlobalFilmData.getInstance().setIcState(intent.getExtras().getString("icState"));
			GlobalFilmData.getInstance().eds=intent.getBooleanExtra("eds",false);
			GlobalFilmData.getInstance().cardID=intent.getStringExtra("smartcard");
			GlobalFilmData.getInstance().weibo=intent.getStringExtra("weibo");
			GlobalFilmData.getInstance().setAaa_state(intent.getStringExtra("aaa_state"));
			GlobalFilmData.getInstance().uid=intent.getStringExtra("unitUserId");
			Logger.d("已经接收到更新广播intent.getStringExtra(unitUserId) = " + intent.getStringExtra("unitUserId").toString());
			GlobalFilmData.getInstance().setAuthToken(authToken);
			Logger.d("已经接收到更新广播intent.getStringExtra(authToken) = " + intent.getStringExtra("authToken").toString());
			GlobalFilmData.getInstance().setServicegroup(intent.getLongExtra("ServiceGroupId",0)+"");
			
			GlobalFilmData.getInstance().saveEpgUrl(context);
	}

}
