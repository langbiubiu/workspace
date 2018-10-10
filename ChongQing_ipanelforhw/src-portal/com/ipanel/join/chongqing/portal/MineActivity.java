package com.ipanel.join.chongqing.portal;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.hardware.usb.UsbManager;
import android.os.Bundle;
import android.provider.Settings;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.ImageView;

import com.ipanel.chongqing_ipanelforhw.R;
import com.ipanel.join.chongqing.myapp.MyAppActivity;
import com.ipanel.join.chongqing.wechattv.WechatTVActivity;
import com.ipanel.join.cq.user.UserActivity;

public class MineActivity extends Activity implements OnClickListener{

	ImageView mUserImg,mSettingImg,mUsbImg,mAppImg,mWechatTVImg,mTVCloud;
	
	private BroadcastReceiver usbRecevier;
	private IntentFilter filter;
	private UsbManager usbManager;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.portal_view_top_pop);
		initViews();
		initUsb();
	}
	
	private void initUsb() {
		usbManager = (UsbManager)getSystemService(
				Context.USB_SERVICE);
		filter = new IntentFilter();
		filter.addAction("android.intent.action.MEDIA_UNMOUNTED");
		filter.addAction("android.intent.action.MEDIA_REMOVED");
		filter.addAction("android.intent.action.MEDIA_BAD_REMOVAL");
		filter.addAction("android.intent.action.MEDIA_MOUNTED");
		filter.addDataScheme("file");

		usbRecevier = new BroadcastReceiver() {
			@Override
			public void onReceive(Context context, Intent intent) {
				String action = intent.getAction();
				Log.d("MineActivity", "usbReceiver action==" + action);
				if (action.equals("android.intent.action.MEDIA_MOUNTED")) {
					mUsbImg.setVisibility(View.VISIBLE);
				} else if (action.equals("android.intent.action.MEDIA_UNMOUNTED")) {
					mUsbImg.setVisibility(View.INVISIBLE);
				}
			}
		};
		
	}

	@Override
	protected void onResume() {
		super.onResume();
		if (usbManager.getDeviceList()==null||usbManager.getDeviceList().size()==0) {
			mUsbImg.setVisibility(View.INVISIBLE);
		} else {
			mUsbImg.setVisibility(View.VISIBLE);
		}
		registerReceiver(usbRecevier, filter);
	}

	@Override
	protected void onPause() {
		super.onPause();
		if(usbRecevier != null)
			unregisterReceiver(usbRecevier);
	}

	private void initViews() {
		mUserImg = (ImageView)findViewById(R.id.user);
		mSettingImg = (ImageView)findViewById(R.id.setting);
		mUsbImg = (ImageView)findViewById(R.id.usb);
		mAppImg = (ImageView)findViewById(R.id.app);
		mWechatTVImg = (ImageView)findViewById(R.id.wechat_tv);
		mTVCloud = (ImageView) findViewById(R.id.tv_cloud);
		mUserImg.setOnClickListener(this);
		mSettingImg.setOnClickListener(this);
		mUsbImg.setOnClickListener(this);
		mAppImg.setOnClickListener(this);
		mWechatTVImg.setOnClickListener(this);
		mTVCloud.setOnClickListener(this);
	}

	@Override
	public void onClick(View v) {
		switch(v.getId()){
		//个人中心
		case R.id.user:
			startUserCenter();
			break;
		//设置
		case R.id.setting:
			startSettingApplication();
			break;
		//外接设备
		case R.id.usb:
			startUsbApplication();
			break;
		//最近应用
		case R.id.app:
			startAppManager();
			break;
		//微信电视
		case R.id.wechat_tv:
			startWechatTV();
			break;
		//电视营业厅
		case R.id.tv_business_hall:
			startTVBusinessHall();
			break;
		//电视云
		case R.id.tv_cloud:
			startTVCloud();
			break;
		}
	}
	
	//电视营业厅
	private void startTVBusinessHall() {
		
	}

	//电视云
	private void startTVCloud() {
//		Intent intent = new Intent(this,TVCloudActivity.class);
//		startActivity(intent);
	}

	//微信电视
	private void startWechatTV() {
		Intent intent = new Intent(this,WechatTVActivity.class);
		startActivity(intent);
	}

	//个人中心
	private void startUserCenter() {
		Intent intent = new Intent(this,UserActivity.class);
		startActivity(intent);
	}

	//外接设备
	private void startUsbApplication(){
		try {
			ComponentName com = new ComponentName("com.ipanel.join.cq.player",
					"com.ipanel.join.sx.player.MainFragmentActivity");
			Intent i = new Intent();
			i.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
			i.setComponent(com);
			startActivity(i);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	//设置
	private void startSettingApplication(){
		try {
			Intent intent = getPackageManager().
					getLaunchIntentForPackage(PortalDataManager.getSettingsPkg());
			startActivity(intent);
		} catch (Exception e) {
			e.printStackTrace();
			try {
				Intent intent = new Intent(Settings.ACTION_SETTINGS);
				startActivity(intent);
			} catch (Exception e1) {
			}
		}
	}
	
	//我的应用
	private void startAppManager(){
		Intent intent = new Intent(this,MyAppActivity.class);
		startActivity(intent);
	}
}
