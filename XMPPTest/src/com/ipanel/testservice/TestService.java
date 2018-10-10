package com.ipanel.testservice;

import java.io.ByteArrayInputStream;

import org.jivesoftware.smack.Chat;
import org.jivesoftware.smack.MessageListener;
import org.jivesoftware.smack.packet.Message;

import cn.ipanel.smackui.PersistStore;
import cn.ipanel.smackui.XMPPManager;
import cn.ipanel.smackui.XMPPManager.SimpleResponse;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.os.Handler;
import android.os.IBinder;
import android.util.Log;

import org.simpleframework.xml.Serializer;
import org.simpleframework.xml.core.Persister;

import com.ipanel.hengyun.message.Event;
import com.ipanel.hengyun.message.GatewayMessage;
import com.ipanel.xmpptest.util.Tools;

public class TestService extends Service {
	
	private Context ctx;
	
	private static String TAG = "TestService";
	
	private String userName = "";
	
	private String password = "";
	
	Serializer mSerializer = new Persister();
	
	Handler mHandler = new Handler() {
		
		public void handleMessage(android.os.Message msg) {
			super.handleMessage(msg);
			switch(msg.what) {
			case 0:
				Event event = (Event)msg.obj;
				Log.i(TAG, "event subtype is " + event.subType);
				String toast = GatewayMessage.getMessage(event);
				if (toast != null)
					Tools.showToastMessage(ctx, toast);
				break;
			default:
				break;
			}
		};
	};
	
	@Override
	public void onCreate() {
		super.onCreate();
		ctx = getBaseContext();
		XMPPManager.init(ctx, null); //connect to server
		Log.i(TAG, "XMPP init complete");
		XMPPManager.setMessageListener(pullListener);
		userName = "hmg1001";
		password = "hmg1001";
		if (!PersistStore.isRegistered(ctx, userName)) { // if user is not registered, then register
			XMPPManager.register(userName, password, null);
			PersistStore.setUser(ctx, userName);
			PersistStore.setUserPwd(ctx, password);
			Log.i(TAG, "register completed!");
		} else {
			Log.i(TAG, "already registered!");
			XMPPManager.setIsRegistered(true);
		}
		XMPPManager.login(userName, password, new SimpleResponse() {
			@Override
			public void onResponse(boolean success) {
				if (success) {
					Log.i(TAG, "login success");
				} else {
					Log.i(TAG, "login fail");
				}
			}
		});
	};
	
	@Override
	public void onDestroy() {
		super.onDestroy();
//		XMPPManager.disconnect();
	};

	@Override
	public IBinder onBind(Intent arg0) {
		// TODO Auto-generated method stub
		return null;
	}
	
	MessageListener pullListener = new MessageListener() {

		@Override
		public void processMessage(Chat chat, Message message) {
			Log.i(TAG, "receive message from " + message.getFrom());
			try {
				XMPPManager.addFriend(message.getFrom(), null);
				String msg = message.getBody();
//				handleMsg(msg);
				msg = msg.replaceAll("&", "&amp;");
				Event e = mSerializer.read(Event.class, new ByteArrayInputStream(msg.getBytes()));
				android.os.Message event = new android.os.Message();
				event.obj = e;
				event.what = 0;
				handleMsg(e.subType + "");
				mHandler.sendMessage(event);
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
	};

	private void handleMsg(String msg) {
		Intent intent = new Intent();
		intent.putExtra("message", msg);
		intent.setAction("com.ipanel.action.xmpptest");
		sendBroadcast(intent);
	}
	
}
