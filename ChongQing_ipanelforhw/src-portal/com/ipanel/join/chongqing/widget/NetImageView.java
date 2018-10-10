package com.ipanel.join.chongqing.widget;

import com.ipanel.chongqing_ipanelforhw.R;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.database.Cursor;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.util.Log;
import android.widget.ImageView;
import cn.ipanel.android.LogHelper;
import cn.ipanel.android.net.cache.JSONApiHelper;
import cn.ipanel.android.net.imgcache.ImageFetcher;
import cn.ipanel.android.net.imgcache.SharedImageFetcher;


public class NetImageView extends ImageView {
	private static final String TAG = "NetImageView";
	private NetWorkReceiver netReceiver;
	private Context contex;
	private String sWifiUrl, sMboleUrl, nNet;

	public NetImageView(Context ctx) {
		super(ctx);
		this.contex = ctx;
		netReceiver = new NetWorkReceiver();
	}

	@Override
	protected void onAttachedToWindow() {
		super.onAttachedToWindow();
		RegisterNetWorkReceiver(contex);
		Log.d(TAG, "onAttachedToWindow--RegisterNetWorkReceiver");
		setNetImage();
	}

	@Override
	protected void onDetachedFromWindow() {
		super.onDetachedFromWindow();
		contex.unregisterReceiver(netReceiver);
		Log.d(TAG, "onDetachedFromWindow--unregisterReceiver netReceiver");
	}

	public void RegisterNetWorkReceiver(Context context) {
		IntentFilter netfilter = new IntentFilter();
		netfilter.addAction(ConnectivityManager.CONNECTIVITY_ACTION);
		netReceiver = new NetWorkReceiver();
		context.registerReceiver(netReceiver, netfilter);
	}

	public class NetWorkReceiver extends BroadcastReceiver {

		@Override
		public void onReceive(Context context, Intent intent) {
			if (intent.getAction() == ConnectivityManager.CONNECTIVITY_ACTION) {
				setNetImage();
			}
		}
	}

	private void setNetImage() {
		ImageFetcher mFetcher = SharedImageFetcher.getSharedFetcher(contex);
		boolean iswf = isWifiContected(contex);
		boolean islocal = isOnline(contex);
		Log.d(TAG, "is have net  isWifi" + iswf);
		Log.d(TAG, "is have net  islocal" + islocal);
		if (iswf) {
			Log.d(TAG, "sWifiUrl " + sWifiUrl);
			mFetcher.loadImage(sMboleUrl, NetImageView.this);
		} else if (islocal) {
			Log.d(TAG, "snetUrl " + sMboleUrl);
			mFetcher.loadImage(sMboleUrl, NetImageView.this);
		} else {
			Log.d(TAG, "nNet " + nNet);
			NetImageView.this.setImageResource(R.drawable.portal_icon_net_02);
		}
	}
	
	/**
	 * 有线网络是否链接正常
	 * */
	public static boolean isOnline(Context context) {
		boolean connected = JSONApiHelper.isOnline(context);
		boolean ipValid = getValidIP(context).startsWith("10.");
		LogHelper.i("is connected :" + connected);
		LogHelper.i("is ipValid :" + ipValid);

		return connected && ipValid;

	}
	public static String getValidIP(Context context) {
		String result = "";
		try {
			Uri uri = Uri.parse("content://ipaneltv.chongqing.settings/ip");
			Cursor cursor = context.getContentResolver().query(uri, null, null,
					null, null);
			while (cursor.moveToNext()) {
				result = cursor.getString(0);
				break;
			}
			cursor.close();
		} catch (Exception e) {
			e.printStackTrace();
		}
		LogHelper.i("get valid ip : " + result);
		return result;
	}

	/**
	 * 判断wifi是否连接成功
	 * 
	 * @param context
	 * @return
	 * */
	public static boolean isWifiContected(Context context) {
		ConnectivityManager connectivityManager = (ConnectivityManager) context
				.getSystemService(Context.CONNECTIVITY_SERVICE);
		NetworkInfo wifiNetworkInfo = connectivityManager
				.getNetworkInfo(ConnectivityManager.TYPE_WIFI);
		if (wifiNetworkInfo != null && wifiNetworkInfo.isConnected()) {
			Log.d(TAG, "isWifiContected" + wifiNetworkInfo.isAvailable());
			return true;
		}
		Log.d(TAG, "isWifiDisconnected");
		return false;
	}

}
