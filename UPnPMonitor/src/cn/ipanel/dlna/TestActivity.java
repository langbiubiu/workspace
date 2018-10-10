package cn.ipanel.dlna;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.view.ViewGroup.LayoutParams;
import android.widget.TextView;

public class TestActivity extends Activity {
	public static final String ACTION_INFO_BROADCAST = "cn.ipanel.upnp.monitor.DEVICE_INFO";

	BroadcastReceiver infoReceiver = new BroadcastReceiver() {

		@Override
		public void onReceive(Context context, Intent intent) {
			showDataIn(intent);
		}

	};

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		Intent intent = registerReceiver(infoReceiver, new IntentFilter(ACTION_INFO_BROADCAST));
		startService(new Intent(this, UPnPService.class));
		showDataIn(intent);
	}

	protected void showDataIn(Intent intent) {
		if (intent != null) {
			String ip = intent.getStringExtra("stbip");
			String caid = intent.getStringExtra("caid");
			TextView tv = new TextView(this);
			tv.setText("CA ID: " + caid + "\n\nSTB IP: " + ip);
			addContentView(tv, new LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT));
		}
	}

}
