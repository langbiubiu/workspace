package com.ipanel.join.cq.vod.order;

import com.ipanel.chongqing_ipanelforhw.R;

import android.app.Dialog;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.TextView;

public class OrderDialog extends Dialog implements OnClickListener {
	private Context context;
	private TextView ok;
	private TextView sure;
	private TextView cancel;
	private TextView content;
	private String tipContent;
	private String url = "http://192.168.9.120/chongqing_TV/index.htm";
	private boolean buyInSequence = false; // true表示按次购买

	public OrderDialog(Context context, String tipContent) {
//		super(context);
		super(context, R.style.Dialog_Collect_Dark);
		this.context = context;
		this.tipContent = tipContent;
	}
	
	public OrderDialog(Context context, String tipContent, boolean buyInSequence) {
		super(context, R.style.Dialog_Collect_Dark);
		this.context = context;
		this.tipContent = tipContent;
		this.buyInSequence = buyInSequence;
		Log.i("OrderDialog", " buyInSequence1 = "+buyInSequence);
	}

	public OrderDialog(Context context, String tipContent, int theme) {
		super(context, theme);
		this.context = context;
		this.tipContent = tipContent;
	}

	public OrderDialog(Context context, String tipContent, int theme,
			OrderDialogListener orderDialogListener) {
		super(context, theme);
		this.context = context;
		this.tipContent = tipContent;
		this.orderDialogListener = orderDialogListener;
	}

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onCreate(savedInstanceState);
		setContentView(R.layout.order_dialog);
		ok = (TextView) findViewById(R.id.order_dialog_ok);
		sure = (TextView) findViewById(R.id.order_dialog_sure);
		cancel = (TextView) findViewById(R.id.order_dialog_cancel);
		content = (TextView) findViewById(R.id.order_dialog_tip);
		ok.setOnClickListener(this);
		sure.setOnClickListener(this);
		cancel.setOnClickListener(this);
		if (null != tipContent) {
			content.setText(tipContent);
		}
		Log.i("OrderDialog", " =buyInSequence= "+buyInSequence);
		if (buyInSequence) {
			ok.setVisibility(View.VISIBLE);
			sure.setVisibility(View.GONE);
		}
	}

	@Override
	public void onClick(View v) {
		// TODO Auto-generated method stub
		switch (v.getId()) {
		case R.id.order_dialog_sure:
			Log.i("OrderDialog", " =确定= ");
			this.dismiss();
			if (orderDialogListener != null) {
				orderDialogListener.sure();
			} else {
//				gotoIPanel30();
				startTVBusinessHall();
			}
			break;
		case R.id.order_dialog_cancel:
		case R.id.order_dialog_ok:
			Log.i("OrderDialog", " =取消= ");
			this.dismiss();
			if (orderDialogListener != null) {
				orderDialogListener.cancel();
			} else {

			}
			break;
		}

	}

	@Override
	public boolean onKeyDown(int keyCode, KeyEvent event) {
		switch (keyCode) {
		case KeyEvent.KEYCODE_BACK:
			this.dismiss();
			break;
		}
		return super.onKeyDown(keyCode, event);
	}

	OrderDialogListener orderDialogListener;

	public interface OrderDialogListener {
		public void sure();

		public void cancel();
	}

	private void gotoIPanel30() {
		Log.i("OrderDialog", " =gotoIPanel30= ");
		try {
			Intent intent = new Intent();
			intent.putExtra("url", url);
			intent.setClassName("com.ipanel.dtv.chongqing",
					"com.ipanel.dtv.chongqing.IPanel30PortalActivity");
			context.startActivity(intent);
		} catch (Exception e) {
			// TODO Auto-generated catch block
			Log.d("OrderDialog", "gotoIPanel30 e = " + e);
			e.printStackTrace();
		}
	}
	
	/**
	 * 电视营业厅
	 */
	private void startTVBusinessHall(){
		ComponentName com = new ComponentName("com.crunii.ccn.tvhall",
				"com.crunii.ccn.tvhall.activity.MainActivity");
		Intent intent = new Intent();
		intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
		intent.setComponent(com);
		if (intent.resolveActivity(context.getPackageManager()) != null) {
			context.startActivity(intent);
		}
	}
}
