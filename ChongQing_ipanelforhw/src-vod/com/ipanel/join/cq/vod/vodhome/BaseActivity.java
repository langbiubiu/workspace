package com.ipanel.join.cq.vod.vodhome;

import android.app.Activity;
import android.os.Bundle;
import android.util.DisplayMetrics;
import android.view.View;
import android.widget.FrameLayout;
import cn.ipanel.android.widget.ViewFrameZoomIndicator2;

import com.ipanel.chongqing_ipanelforhw.R;
import com.ipanel.join.chongqing.portal.VolumePanel;
import com.ipanel.join.cq.vod.HWDataManager;
import com.ipanel.join.cq.vod.jsondata.GlobalFilmData;
import com.ipanel.join.cq.vod.utils.Tools;
import com.ipanel.join.protocol.a7.ServiceHelper;
import com.ipanel.join.protocol.a7.ServiceHelper.SerializerType;

public abstract class BaseActivity extends Activity {
	protected int dmHeight = 720;
	protected ViewFrameZoomIndicator2 frameIndicator;
	protected VolumePanel volPanel;
	protected ServiceHelper serviceHelper;
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		DisplayMetrics dm = new DisplayMetrics();
		getWindowManager().getDefaultDisplay().getMetrics(dm);
		if (dm.heightPixels == 1080) {
			dmHeight = 1080;
		}
		serviceHelper = ServiceHelper.getHelper();
		serviceHelper.setSerializerType(SerializerType.JSON);
		serviceHelper.setRootUrl(HWDataManager.ROOT_URL);
		frameIndicator = new ViewFrameZoomIndicator2(this);
		frameIndicator.setFrameResouce(R.color.transparent);
		frameIndicator.setScaleAnimationSize(1.1f, 1.1f);
	}
	
	@Override
	protected void onResume() {
		super.onResume();
		if (!Tools.isOnline(this)) {
			Tools.showToastMessage(this, getResources().getString(R.string.networkoff));
			finish();
		}
	}
	/**
	 * 检查各种状态是否正常
	 * */
	public boolean checkValidState(){
		if (!Tools.isOnline(this)) {
			Tools.showToastMessage(this, getResources().getString(R.string.networkoff));
			return false;
		}
		if(!GlobalFilmData.getInstance().eds){
			Tools.showToastMessage(this, getResources().getString(R.string.edserror));
			return false;
		}
		if (!"0".equals(GlobalFilmData.getInstance().getIcState())) {
			Tools.showToastMessage(this, getResources().getString(R.string.icerror));
			return false;
		}
		return true;
	}
	/**
	 * 移动焦点
	 * */
	public void moveIndicatorToFocus() {
		View focus = this.getCurrentFocus();
		if (focus instanceof FrameLayout) {
			frameIndicator.moveFrameTo(focus, true, false);
		} else {
			frameIndicator.hideFrame();
		}
	}
	
	public void showMessage(int resId){
		Tools.showToastMessage(this, getString(resId));
	}
	
	public void showMessage(String msg){
		Tools.showToastMessage(this, msg);
	}
}
