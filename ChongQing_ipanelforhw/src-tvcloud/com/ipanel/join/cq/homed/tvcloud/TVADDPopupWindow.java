package com.ipanel.join.cq.homed.tvcloud;

import android.content.Context;
import android.graphics.drawable.ColorDrawable;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.LinearLayout;
import android.widget.PopupWindow;

import com.ipanel.chongqing_ipanelforhw.R;
import com.ipanel.join.cq.back.TVAddPopupWindow.ButtonClickListener;

public class TVADDPopupWindow {
	
	TVCloudActivity mActivity;
	PopupWindow popwindow;
	
	LinearLayout recommend, mine, jst;
	ButtonClickListener listener;
	public interface ButtonClickListener{
		public void onRecommend();
	}

	public TVADDPopupWindow(Context context) {
		this.mActivity = (TVCloudActivity) context;
		View v = mActivity.getLayoutInflater().inflate(
				R.layout.tvcloud_tvadd_layout, null);
		popwindow = new PopupWindow(v);
		popwindow.setFocusable(true);
		ColorDrawable dw = new ColorDrawable(0x00000000);
		popwindow.setBackgroundDrawable(dw);
		
		recommend = (LinearLayout) v.findViewById(R.id.tvcloud_recommend);
		mine = (LinearLayout) v.findViewById(R.id.tvcloud_mine);
		jst = (LinearLayout) v.findViewById(R.id.tvcloud_jst);
		
		recommend.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View view) {
				// TODO Auto-generated method stub
				popwindow.dismiss();
				listener.onRecommend();//ÍÆ¼ö
			}
		});
		mine.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View view) {
				// TODO Auto-generated method stub
				
			}
		});
		jst.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View view) {
				// TODO Auto-generated method stub
				
			}
		});
	}

	public PopupWindow getPop() {
		return popwindow;
	}

}
