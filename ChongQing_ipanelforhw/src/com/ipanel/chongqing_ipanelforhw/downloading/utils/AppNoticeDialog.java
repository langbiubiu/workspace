package com.ipanel.chongqing_ipanelforhw.downloading.utils;

import android.app.Dialog;
import android.content.Context;
import android.os.Bundle;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.TextView;

import com.ipanel.chongqing_ipanelforhw.R;

public class AppNoticeDialog extends Dialog implements OnClickListener {
	Context context;
	private NoticeDialogListener listener;

	public interface NoticeDialogListener {
		public void onClick(View view);
	}

	public AppNoticeDialog(Context context) {
		super(context);
		this.context = context;
	}

	public AppNoticeDialog(Context context, int theme) {
		super(context, theme);
		this.context = context;
	}

	public AppNoticeDialog(Context context, int theme,
			NoticeDialogListener listener) {
		super(context, theme);
		this.context = context;
		this.listener = listener;
	}

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		TextView enter = (TextView) findViewById(R.id.dialog_enter);
		TextView cancel = (TextView) findViewById(R.id.dialog_cancle);
		if (enter != null && cancel != null) {
			enter.setOnClickListener(this);
			cancel.setOnClickListener(this);
		}

	}

	@Override
	public void onClick(View v) {
		// TODO Auto-generated method stub
		listener.onClick(v);
	}

	@Override
	public boolean onKeyDown(int keyCode, KeyEvent event) {
		Log.e("NoticeDialog", "####.......onKeyDown.......");
		if (keyCode == KeyEvent.KEYCODE_BACK) {
			// return true;
		}
		return super.onKeyDown(keyCode, event);
	}

}
