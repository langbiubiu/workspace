package ipanel.join.collectors;

import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.IBinder;
import android.text.TextUtils;
import android.util.Log;

public abstract class AcquisitionService extends Service {
	
	public static final String TAG=AcquisitionService.class.getSimpleName();
	/**用户数据收集的广播接收器*/
	private BroadcastReceiver receiver=new BroadcastReceiver(){
		@Override
		public void onReceive(Context context, Intent intent) {
			Log.i(TAG, "receive a user data colectors broadcast");
			AcquisitionManager.getInstance().handleAcquisitionIntent(intent, context);
		}
	};
	
	public abstract IAcquisitionConfig getConfig();
	
	@Override
	public void onCreate() {
		super.onCreate();
		Log.i(TAG, "service on create");
		AcquisitionManager.getInstance().initContext(getApplicationContext());
		AcquisitionManager.getInstance().setAcquisitionConfig(getConfig());
		String action=AcquisitionManager.getInstance().getAcquisitionConfig().getCollectorBroadcastAction();
		if(TextUtils.isEmpty(action)){
			throw new IllegalStateException("the action of collector broadcast has't defined");
		}
		registerReceiver(receiver, new IntentFilter(action));
	}
	
	@Override
	public void onDestroy() {
		super.onDestroy();
		Log.i(TAG, "service on destroy");
		unregisterReceiver(receiver);
	}
	
	@Override
	public IBinder onBind(Intent arg0) {
		// TODO Auto-generated method stub
		return null;
	}
}
