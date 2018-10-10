package com.ipanel.join.chongqing.portal;

import android.app.Activity;
import android.graphics.drawable.ColorDrawable;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.View;
import android.view.View.OnKeyListener;
import android.widget.ImageView;
import android.widget.PopupWindow;

import com.ipanel.chongqing_ipanelforhw.R;

public class MinePopupWindow implements OnKeyListener{
	private PopupWindow popupWindow;
	private View top_view;// 菜单视图
	private Activity mParent;
	ImageView mFocusImg;
	VolumePanel volPanel;
	
	public MinePopupWindow(Activity mParent,VolumePanel volPanel) {
		this.mParent = mParent;
		initViews(mParent);
		this.volPanel = volPanel;
	}

	private void initViews(Activity mParent) {
		top_view = mParent.getLayoutInflater().inflate(
				R.layout.portal_mine_top_pop, null);
		popupWindow = new PopupWindow(top_view);
		popupWindow.setFocusable(true);
		ColorDrawable dw = new ColorDrawable(0x00000000);
		popupWindow.setBackgroundDrawable(dw);// 设置透明背景，否则返回键popupWindow不会消失
		mFocusImg = (ImageView) top_view.findViewById(R.id.mine_focus);
		mFocusImg.setOnKeyListener(this);
		mFocusImg.requestFocus();
		top_view.setOnKeyListener(this);
	}
	
	public PopupWindow getPop() {
		return popupWindow;
	}

	public void showTopView(Activity parent){
		if(popupWindow != null){
			popupWindow.showAtLocation(parent.getWindow().getDecorView(), Gravity.TOP, 0, 0);
			popupWindow.update(1920, 201);
		}
	}
	
	public void hideTopView(){
		if(popupWindow != null && popupWindow.isShowing())
			popupWindow.dismiss();
	}
	
	public boolean isShowing(){
		if(popupWindow!=null && popupWindow.isShowing())
			return true;
		return false;
	}

	@Override
	public boolean onKey(View v, int keyCode, KeyEvent event) {
		if (volPanel.onKeyDown(keyCode, event)){
			hideTopView();
			return true;
		}
		if(event.getAction() == KeyEvent.ACTION_DOWN){
			switch(keyCode){
			case KeyEvent.KEYCODE_DPAD_DOWN:
			case KeyEvent.KEYCODE_MENU:
				hideTopView();
				return true;
			case KeyEvent.KEYCODE_DPAD_CENTER:
			case KeyEvent.KEYCODE_ENTER:
				PortalDataManager.startMineActivity(mParent);
				return true;
			}
		}
		return false;
	}
}
