package com.ipanel.join.cq.back;

import android.app.Activity;
import android.graphics.drawable.ColorDrawable;
import android.view.KeyEvent;
import android.view.RcKeyEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnKeyListener;
import android.widget.PopupWindow;
import android.widget.RelativeLayout;

import com.ipanel.chongqing_ipanelforhw.R;
import com.ipanel.join.chongqing.portal.PortalDataManager;
import com.ipanel.join.cq.vod.utils.Tools;

public class TVAddPopupWindow implements OnClickListener {
	private Activity mActivity;
	private PopupWindow popwindow;
	private RelativeLayout mRecBtn,mMineBtn,mJstBtn;
	
	ButtonClickListener listener;
	public interface ButtonClickListener{
		public void onRecommend();
	}
	public TVAddPopupWindow(Activity mActivity,ButtonClickListener listener) {
		this.mActivity = mActivity;
		this.listener = listener;
		View v = mActivity.getLayoutInflater().inflate(
				R.layout.btv_tvadd_layout, null);
		popwindow = new PopupWindow(v);
		popwindow.setFocusable(true);
		ColorDrawable dw = new ColorDrawable(0x00000000);
		popwindow.setBackgroundDrawable(dw);
		initViews(v);
	}

	private void initViews(View v) {
		mRecBtn = (RelativeLayout) v.findViewById(R.id.btv_recommend);
		mMineBtn = (RelativeLayout) v.findViewById(R.id.btv_mine);
		mJstBtn = (RelativeLayout) v.findViewById(R.id.btv_jst);
		mRecBtn.setOnClickListener(this);
		mMineBtn.setOnClickListener(this);
		mJstBtn.setOnClickListener(this);
		
		mJstBtn.setOnKeyListener(mkl);
		mRecBtn.setOnKeyListener(mkl);
		mMineBtn.setOnKeyListener(mkl);
	}

	public PopupWindow getPop() {
		return popwindow;
	}

	@Override
	public void onClick(View v) {
		switch(v.getId()){
		case R.id.btv_recommend:
			popwindow.dismiss();
			listener.onRecommend();//ÍÆ¼ö
			break;
		case R.id.btv_mine:
			PortalDataManager.startMineActivity(mActivity);
			break;
		case R.id.btv_jst:
			Tools.showToastMessage(mActivity, mActivity.getResources().getString(R.string.is_developing));
			break;
		}
	}
	View.OnKeyListener mkl = new OnKeyListener() {
		
		@Override
		public boolean onKey(View v, int keyCode, KeyEvent event) {
			if (event.getAction() == KeyEvent.ACTION_DOWN) {
				keyCode = RcKeyEvent.getRcKeyCode(event);
				if (keyCode == RcKeyEvent.KEYCODE_QUIT
						|| keyCode == RcKeyEvent.KEYCODE_BACK
						|| keyCode == RcKeyEvent.KEYCODE_MENU
						|| keyCode == RcKeyEvent.KEYCODE_TV_ADD) {
					if (popwindow.isShowing()) {
						popwindow.dismiss();
					}
					return true;
				}
			}
			return false;
		}
	};
}
