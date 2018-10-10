package cn.ipanel.smackui;

import org.jivesoftware.smack.packet.Packet;

import cn.ipanel.android.LogHelper;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.ConnectivityManager;
import android.os.IBinder;
import android.os.SystemClock;

public class XMPPService extends Service {

	@Override
	public IBinder onBind(Intent intent) {
		// TODO Auto-generated method stub
		return null;
	}

	private BroadcastReceiver mBroadcastReceiver;
	
	private PendingIntent mHeartBeatIntent;
	
//	private static final int KEEP_ALIVE_INTERVAL = 28 * 60 * 1000;
	private static final int KEEP_ALIVE_INTERVAL = 48 * 1000;


	@Override
	public void onCreate() {
		super.onCreate();
		IntentFilter intentFilter = new IntentFilter();
		intentFilter.addAction(ConnectivityManager.CONNECTIVITY_ACTION);
		registerReceiver(mBroadcastReceiver = new BroadcastReceiver() {

			@Override
			public void onReceive(Context context, Intent intent) {
				String action = intent.getAction();
				if (ConnectivityManager.CONNECTIVITY_ACTION.equals(action)) {
					boolean noConnection = intent.getBooleanExtra(
							ConnectivityManager.EXTRA_NO_CONNECTIVITY, false);
					if (!noConnection) {
						ConnectivityManager cm = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
						if (cm.getActiveNetworkInfo() != null && cm.getActiveNetworkInfo().isConnected()) {
							startXMPPConnection();
						}
					}
				}

			}

		}, intentFilter);

		Intent intent = new Intent("com.chongqing.mobile.XMPPService");
		intent.putExtra("HeartBeat", true);
		mHeartBeatIntent = PendingIntent.getService(this, 1, intent, 0);
	}

	protected void scheduleNextHeartBeat() {
		AlarmManager am = (AlarmManager) getSystemService(Context.ALARM_SERVICE);
		am.set(AlarmManager.ELAPSED_REALTIME_WAKEUP, SystemClock.elapsedRealtime()+KEEP_ALIVE_INTERVAL, mHeartBeatIntent);
	}
	protected void cancelNextHeartBeat(){
		AlarmManager am = (AlarmManager) getSystemService(Context.ALARM_SERVICE);
		am.cancel(mHeartBeatIntent);
	}
	protected void startXMPPConnection() {
		// TODO Auto-generated method stub

	}

	@Override
	public void onDestroy() {
		unregisterReceiver(mBroadcastReceiver);
		cancelNextHeartBeat();
		super.onDestroy();
	}

	@Override
	public int onStartCommand(Intent intent, int flags, int startId) {
		if(intent != null && intent.getBooleanExtra("HeartBeat", false)){
			handleHeartBeat();
			scheduleNextHeartBeat();
		}
		return Service.START_STICKY;
	}
	
	protected void handleHeartBeat(){

	}
	
	protected void sendHeartBeat() {
		XMPPManager.connection.sendPacket(new Packet() {
			
			@Override
			public String toXML() {
				return " ";
			}
		});
		
	}

}
