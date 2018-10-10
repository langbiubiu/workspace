package com.ipanel.join.chongqing.live;

import ipaneltv.toolkit.entitlement.EntitlementObserver;
import android.content.Context;
import android.net.Uri;
import android.net.telecast.NetworkManager;
import android.net.telecast.TransportManager;
import android.net.telecast.ca.CAManager;
import android.os.Handler;
import android.os.Looper;
import cn.ipanel.android.LogHelper;

import com.ipanel.join.chongqing.live.manager.BookManager;
import com.ipanel.join.chongqing.live.manager.impl.BookManagerImpl;
import com.ipanel.join.chongqing.live.navi.FaivoratesCommitor;
import com.ipanel.join.chongqing.live.navi.LiveNavigator;
import com.ipanel.join.chongqing.live.navi.DatabaseObjects.LiveGroup;

public class LiveApp {
	private static LiveApp mInstance = new LiveApp();
	
	public Context appCtx;
	public final Handler mHandler = new Handler(Looper.getMainLooper()) ;
	private LiveNavigator navigator;
	private NetworkManager netManager;
	private CAManager caManager;
	private EntitlementObserver entObserver;
	private BookManager mBookManager;
	private Uri dbUri = null;
	public LiveGroup favoriteGroup = new LiveGroup();// Ï²°®·Ö×é

	public static LiveApp getInstance() {
		return mInstance;
	}

	public void init(Context appCtx) {
		this.appCtx = appCtx;

		LogHelper.LOGTAG = "live_zyl";
		Constant.DTV_LIB_EXIST = hasTVLib();
		if(hasTVLib())
			TransportManager.getInstance(appCtx);
		Constant.DENSITY = appCtx.getResources().getDisplayMetrics().heightPixels > 720 ? 1.5f
				: 1.0f;
		Constant.AIO = Constant.DENSITY > 1.0f;
		int currentProcessID = android.os.Process.myUid();
		favoriteGroup.setUid(currentProcessID);
		favoriteGroup.setName(FaivoratesCommitor.NAME);
		favoriteGroup.setId(Constant.FAVORITE_COLUME_ID);
	}

	public boolean hasTVLib() {
		try {
			Class.forName("android.net.telecast.FrequencyInfo");
		} catch (ClassNotFoundException e) {
			return false;
		}
		return true;
	}
	
	public synchronized LiveNavigator getWasuLiveNavigator() {
		if (navigator == null) {
			navigator = new LiveNavigator(appCtx, getNetworkDatabaseUri());
		}
		return navigator;
	}
	public synchronized Uri getNetworkDatabaseUri() {
		if (dbUri == null)
			dbUri = getNetworkManager().getNetworkDatabaseUri(Constant.UUID);
		return dbUri;
	}
	public synchronized NetworkManager getNetworkManager() {
		if (netManager == null)
			netManager = NetworkManager.getInstance(appCtx);
		return netManager;
	}
	public synchronized BookManager getBookManager() {
		if (mBookManager == null)
			mBookManager = new BookManagerImpl(appCtx);
		return mBookManager;
	}
	public synchronized CAManager getCAManager() {
		if (caManager == null)
			caManager = CAManager.createInstance(appCtx);
		return caManager;
	}

	public synchronized EntitlementObserver getEntitlementObserver() {
		if (entObserver == null) {
			entObserver = new EntitlementObserver(appCtx);
			entObserver.prepare();
			
		}
		return entObserver;
	}
	
	public void post(Runnable r){
		mHandler.post(r);
	}
	public void postDelayed(Runnable r,long delayMillis){
		mHandler.postDelayed(r, delayMillis);
	}
}
