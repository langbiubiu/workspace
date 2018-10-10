package com.ipanel.join.chongqing.widget;

import com.ipanel.chongqing_ipanelforhw.R;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.view.View;
import android.widget.ImageView;


public class CheckUsbImageView extends ImageView {
	private String TAG = "CheckUsbImageView";
	private UsbReceiver usbReceiver;
	private String mUsbUrl;
	private Context context;

	public CheckUsbImageView(Context ctx) {
		super(ctx);
		this.context = ctx;
		CheckUsbImageView.this.setVisibility(View.INVISIBLE);
	}

	@Override
	protected void onAttachedToWindow() {
		setUsbReceiver();
		super.onAttachedToWindow();
	}
   
	@Override
	protected void onDetachedFromWindow() {
		context.unregisterReceiver(usbReceiver);
		super.onDetachedFromWindow();
	}
	
	/**
	 * ×¢²áUsb¹ã²¥¼àÌý Register USB Receiver
	 */
	public void setUsbReceiver() {
		IntentFilter fliter = new IntentFilter();
		fliter.addAction(Intent.ACTION_MEDIA_MOUNTED);// ²åÈëUÅÌµÄ¼àÌý
		fliter.addAction(Intent.ACTION_MEDIA_UNMOUNTED);// °Î³öUÅÌµÄ¼àÌý
		fliter.addAction(Intent.ACTION_MEDIA_EJECT);// ÎïÀí°Î³öUÅÌµÄ¼àÌý
		fliter.addAction(Intent.ACTION_MEDIA_REMOVED);// ÒÆ³ýUÅÌµÄ¼àÌý
		fliter.addAction(Intent.ACTION_MEDIA_CHECKING);// ¼ì²éUÅÌµÄ¼àÌý
		fliter.addDataScheme("file");
		usbReceiver = new UsbReceiver();
		context.registerReceiver(usbReceiver, fliter);
	}

	/**
	 * USB×´Ì¬¹ã²¥µÄ¼àÌý£»
	 * 
	 */
	public class UsbReceiver extends BroadcastReceiver {
		@Override
		public void onReceive(Context context, Intent intent) {
			if (intent.getAction().equals(Intent.ACTION_MEDIA_MOUNTED)) {
				CheckUsbImageView.this
						.setBackgroundResource(R.drawable.portal_icon_usb);
				CheckUsbImageView.this.setVisibility(View.VISIBLE);
			} else {
				CheckUsbImageView.this.setVisibility(View.INVISIBLE);
			}
		}
	};

}
