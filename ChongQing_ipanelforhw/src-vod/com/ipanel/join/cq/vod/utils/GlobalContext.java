package com.ipanel.join.cq.vod.utils;

import android.app.Activity;
import android.content.Context;
import android.content.ContextWrapper;
import android.content.Intent;
import android.content.IntentFilter;
import cn.ipanel.android.LogHelper;
import cn.ipanel.android.net.imgcache.SharedImageFetcher;

import com.ipanel.chongqing_ipanelforhw.EPGReceiver;
import com.ipanel.join.cq.vod.jsondata.GlobalFilmData;

public class GlobalContext extends ContextWrapper {
	public static float  dp;
	private GlobalContext(Context base) {
		super(base);
		dp = getResources().getDisplayMetrics().density;
	}
	
	public static int dp2px(int val){
		return (int)(dp * val + 0.5f);
	}

	private static GlobalContext globalContext = null;
	private Activity activity;

	public static void init(Context context) {
		LogHelper.LOGTAG="vod_zyl";
		globalContext = new GlobalContext(context);
		SharedImageFetcher.clearMemoryCache();
		SharedImageFetcher.clearDiskCache(globalContext);
		SharedImageFetcher.sCacheSize=0.38f;
		Intent intent= globalContext.registerReceiver(new EPGReceiver(), new IntentFilter("com.ipanel.join.cq.vodauth.EPG_URL"));
	    if(intent!=null){
	    	globalContext.calEPGData(intent);
	    }
	}
	
	private void calEPGData(Intent intent){
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
		
		GlobalFilmData.getInstance().saveEpgUrl(globalContext);
	}

	public static GlobalContext getInstance() {
		return globalContext;
	}

	public Activity getActivity() {
		return activity;
	}

	public void setActivity(Activity activity) {
		this.activity = activity;
	}
}
