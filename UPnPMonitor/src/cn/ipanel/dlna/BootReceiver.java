package cn.ipanel.dlna;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;

public class BootReceiver extends BroadcastReceiver {

	@Override
	public void onReceive(Context context, Intent intent) {
		String action = intent.getAction();
		Logger.d(action);
//		if (WifiManager.NETWORK_STATE_CHANGED_ACTION.equals(action)) {
//			final WifiManager wifiManager = (WifiManager) context.getSystemService(Context.WIFI_SERVICE);
//			NetworkInfo netInfo = intent.getParcelableExtra(WifiManager.EXTRA_NETWORK_INFO);
//			if (wifiManager != null && netInfo != null) {
//				WifiInfo info = wifiManager.getConnectionInfo();
//				Logger.d(netInfo.toString());
//				Logger.d(info.toString());
//				if (netInfo.getState() == NetworkInfo.State.CONNECTED && info.getNetworkId() != -1 && info.getLinkSpeed() > 0) {
//					if(!PersistStore.BoolSetting.DISABLE_AUTO_START.getSetting(context))
//						context.startService(new Intent(context, UPnPService.class));
//				}
//			}
//		}
		if(ConnectivityManager.CONNECTIVITY_ACTION.equals(action)){
			ConnectivityManager cm = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
			if(cm != null){
				NetworkInfo netInfo = cm.getActiveNetworkInfo();
				Logger.d(action+" "+netInfo);
				if (netInfo != null
						&& netInfo.isConnected()
						&& (netInfo.getType() == ConnectivityManager.TYPE_ETHERNET || netInfo
								.getType() == ConnectivityManager.TYPE_WIFI)) {
					Logger.d("network type: "+netInfo.getType());
					if(!PersistStore.BoolSetting.DISABLE_AUTO_START.getSetting(context))
						context.startService(new Intent(context, UPnPService.class));
				}
			}
		}
		
		if(Intent.ACTION_BOOT_COMPLETED.equals(intent.getAction())) {
			if(!PersistStore.BoolSetting.DISABLE_AUTO_START.getSetting(context))
				context.startService(new Intent(context, UPnPService.class));
		}

	}

}
