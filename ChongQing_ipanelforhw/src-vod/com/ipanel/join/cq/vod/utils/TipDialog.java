package com.ipanel.join.cq.vod.utils;


import android.app.Dialog;
import android.content.Context;
import android.os.Bundle;
import android.util.Log;
import android.view.KeyEvent;
import android.view.RcKeyEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.ipanel.chongqing_ipanelforhw.R;

public class TipDialog extends Dialog implements OnClickListener{

	Context context;
	int contentID;
	boolean showBackIcon = false;
	public TipDialog(Context context) {
		super(context);
	}

	public TipDialog(Context context, boolean cancelable,
			OnCancelListener cancelListener) {
		super(context, cancelable, cancelListener);
	}

	public TipDialog(Context context, int theme,TipDialogListener tipDialogListener,int contentID,boolean showBackIcon) {
		super(context, theme);
		this.context=context;
		this.tipDialogListener=tipDialogListener;
		this.contentID=contentID;
		this.showBackIcon = showBackIcon;
	}
	
	private TextView sureTextView;
	private TextView cancelTextView;
	private RelativeLayout ad_rl;//广告图
	private ImageView back_icon;//回看logo
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.vod_tip_dialog);
		sureTextView=(TextView)findViewById(R.id.tipSure);
		cancelTextView=(TextView)findViewById(R.id.tipCancel);
		ad_rl = (RelativeLayout)findViewById(R.id.ad_rl);
		back_icon = (ImageView)findViewById(R.id.back_pause_icon);
		if(showBackIcon){
			back_icon.setVisibility(View.VISIBLE);
		}else{
			back_icon.setVisibility(View.GONE);
		}
		sureTextView.setOnClickListener(this);
		cancelTextView.setOnClickListener(this);
	}
	
	@Override
	public void onClick(View v) {
		switch(v.getId())
		{
		case R.id.tipSure:
			Log.i("","R.id.tipSure");
			this.dismiss();
            tipDialogListener.sure();
			break;
		case R.id.tipCancel: 
//			this.dismiss();
//			 tipDialogListener.cancel();
			//退出广告
			ad_rl.setVisibility(View.INVISIBLE);
			break;
		}
	}
	
	
	TipDialogListener tipDialogListener;
	public interface TipDialogListener
	{
		public void sure();
		public void cancel();
	}
	private static final int RETURN = 1034;
	@Override
	public boolean onKeyDown(int keyCode,KeyEvent event)
	{
		switch (RcKeyEvent.getRcKeyCode(event)) {
		case RcKeyEvent.KEYCODE_QUIT:
		case RcKeyEvent.KEYCODE_BACK:
			this.dismiss();
//			tipDialogListener.cancel();
			break;

		default:
			break;
		}

	  	return super.onKeyDown(keyCode, event);
	}
	
	public boolean isAdHide(){
		return ad_rl.getVisibility() == View.VISIBLE ? false :  true;
	}
}
