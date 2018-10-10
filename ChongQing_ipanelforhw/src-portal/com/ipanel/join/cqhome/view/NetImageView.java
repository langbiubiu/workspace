package com.ipanel.join.cqhome.view;

import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.SocketException;
import java.util.Enumeration;

import org.apache.http.conn.util.InetAddressUtils;
import org.json.JSONObject;

import com.ipanel.chongqing_ipanelforhw.R;
import com.ipanel.join.cq.vod.utils.Tools;

import cn.ipanel.android.net.imgcache.ImageFetcher;
import cn.ipanel.android.net.imgcache.SharedImageFetcher;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.util.Log;
import ipanel.join.configuration.Bind;
import ipanel.join.configuration.Value;
import ipanel.join.configuration.View;
import ipanel.join.widget.ImgView;

public class NetImageView extends ImgView {
	private static final String TAG = "NetImageView";
	private NetWorkReceiver netReceiver;
	private Context context;
	private String mNetOn, mNetOff;

	public NetImageView(Context ctx, View data) {
		super(ctx, data);
		this.context = ctx;
		netReceiver = new NetWorkReceiver();
		Bind bd = data.getBindByName("drawable");
		if (bd != null) {
			Value v = bd.getValue();
			if (v != null) {
				if (Value.TYPE_JSON.equals(v.getType())) {
					try {
						JSONObject jobj = new JSONObject(v.getvalue());
						if (jobj.has("net1")) {
							mNetOn = jobj.getString("net1");
						}
						if (jobj.has("net2")) {
							mNetOff = jobj.getString("net2");
						}
					} catch (Exception e) {
						e.printStackTrace();
					}
				}
			}
		}
	}

	@Override
	protected void onAttachedToWindow() {
		RegisterNetWorkReceiver(context);
		super.onAttachedToWindow();
		setNetImage();
	}

	@Override
	protected void onDetachedFromWindow() {
		context.unregisterReceiver(netReceiver);
		Log.d(TAG, "lixby :   unregisterReceiver--netReceiver");
		super.onDetachedFromWindow();
	}

	public void RegisterNetWorkReceiver(Context context) {
		Log.d(TAG, "RegisterNetWorkReceiver ");
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
		ImageFetcher mFetcher = SharedImageFetcher.getSharedFetcher(context);
		if(Tools.isOnline(context)){
			mFetcher.loadImage(mNetOn, NetImageView.this);
		}else{
			mFetcher.loadImage(mNetOff, NetImageView.this);
		}
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
