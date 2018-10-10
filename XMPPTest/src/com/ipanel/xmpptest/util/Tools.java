package com.ipanel.xmpptest.util;

import com.example.xmpptest.R;

import android.content.Context;
import android.content.DialogInterface.OnKeyListener;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

public class Tools {
	
	public static void showToastMessage(Context context, String msg) {
		View v = View.inflate(context, R.layout.toast_view, null);
		TextView mShowInfo = (TextView) v.findViewById(R.id.showinfo);
		if (mShowInfo != null) {
			mShowInfo.setText(msg);
//			mShowInfo.setOnKeyListener(new View.OnKeyListener() {
//				
//				@Override
//				public boolean onKey(View arg0, int arg1, KeyEvent arg2) {
//					// TODO Auto-generated method stub
//					return true;
//				}
//			});
		}
		Toast toast = new Toast(context);
		toast.setGravity(Gravity.CENTER_HORIZONTAL, 0, 0);
		toast.setDuration(3500);
		toast.setView(v);
		toast.show();
	}

}
