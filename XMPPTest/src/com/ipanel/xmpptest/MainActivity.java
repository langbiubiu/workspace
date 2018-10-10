package com.ipanel.xmpptest;

import java.io.ByteArrayInputStream;

import org.simpleframework.xml.Serializer;
import org.simpleframework.xml.core.Persister;

import com.example.xmpptest.R;
import com.ipanel.hengyun.message.Event;
import com.ipanel.xmpptest.util.Tools;

import android.os.Bundle;
import android.util.Log;
import android.widget.TextView;
import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;

public class MainActivity extends Activity {

	TextView message;
	
	Serializer mSerializer = new Persister();
	
	BroadcastReceiver mReceiver = new BroadcastReceiver() {
		
		@Override
		public void onReceive(Context ctx, Intent intent) {
			// TODO Auto-generated method stub
			if (intent.getAction().equals("com.ipanel.action.xmpptest")) {
				String msg = intent.getStringExtra("message");
				msg = msg.replaceAll("&", "&amp;");
				try {
					System.out.println("receive msg =\n" + msg);
					Event e = mSerializer.read(Event.class, new ByteArrayInputStream(msg.getBytes()));
					message.setText(e.toString());
				} catch (Exception e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
		}
	};
	
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        message = (TextView) findViewById(R.id.received_message);
        
        Intent intent = new Intent("com.ipanel.testservice");
        startService(intent);
        
        IntentFilter filter = new IntentFilter("com.ipanel.action.xmpptest");
        registerReceiver(mReceiver, filter);
    }
    
    @Override
    protected void onDestroy() {
    	// TODO Auto-generated method stub
    	super.onDestroy();
    	unregisterReceiver(mReceiver);
    	Intent intent = new Intent("com.ipanel.testservice");
    	stopService(intent);
    }
}
