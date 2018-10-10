package com.ipanel.join.cqhome.view;

import com.ipanel.chongqing_ipanelforhw.R;

import android.app.Dialog;
import android.app.Instrumentation;
import android.content.Context;
import android.util.AttributeSet;
import android.view.KeyEvent;
import android.view.View;
import ipanel.join.widget.ImgView;

public class PowerButton extends ImgView {

	public PowerButton(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);
		init();
	}

	public PowerButton(Context context, AttributeSet attrs) {
		super(context, attrs);
		init();
	}

	public PowerButton(Context ctx, ipanel.join.configuration.View data) {
		super(ctx, data);
		init();
	}

	public PowerButton(Context context) {
		super(context);
		init();
	}

	void init(){
		setOnClickListener(new View.OnClickListener() {
			
			@Override
			public void onClick(View v) {
				showPowerOff();
				
			}
		});
		
	}
	
	void showPowerOff(){
		final Dialog dlg = new Dialog(getContext(), R.style.Dialog_Collect_Dark);
		dlg.setContentView(R.layout.portal_frag_qrlogin_confirm);
		dlg.findViewById(R.id.yes_btn).setOnClickListener(new View.OnClickListener() {
			
			@Override
			public void onClick(View v) {
				dlg.dismiss();
				new Thread(){
					public void run(){
						new Instrumentation().sendKeyDownUpSync(KeyEvent.KEYCODE_POWER);
					}
				}.start();
			}
		});
		dlg.findViewById(R.id.no_btn).setOnClickListener(new View.OnClickListener() {
			
			@Override
			public void onClick(View v) {
				dlg.dismiss();
			}
		});
		dlg.show();
		dlg.findViewById(R.id.yes_btn).requestFocus();
	}
}
