package com.ipanel.join.chongqing.live;

import ipanel.join.ad.widget.ImageAdView;
import ipaneltv.toolkit.media.TeeveeWidgetFragment;
import android.app.Activity;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.net.telecast.NetworkManager;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.SurfaceView;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewGroup.LayoutParams;

import com.ipanel.chongqing_ipanelforhw.R;
import com.ipanel.join.chongqing.live.manager.ADManager;

public class WidgetFragment extends TeeveeWidgetFragment {
	Drawable default_bg;
	ImageAdView ad_view;

	public static WidgetFragment createInstance() {
		Bundle b = createArguments(Constant.UUID, NetworkManager.PROPERTY_TEEVEE_WIDGET, 100);
		WidgetFragment f = new WidgetFragment();
		f.setArguments(b);
		return f;
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		View v = super.onCreateView(inflater, container, savedInstanceState);
		if (v == null) {
			v = new SurfaceView(getActivity());
//			v.setBackgroundColor(Color.WHITE);// 没有找到视频装口Widget，深蓝色背景替代
		}else{


		}
		return v;
	}

	public void setVideoMask(Drawable d) {
		if (hostView != null) {
			hostView.setVideoMask(d);
		}
	}
	
	public void setVideoMask(int url) {
		if(url==ADManager.AD_FOR_NONE){
			if(ad_view!=null){
				ViewGroup vg=(ViewGroup) this.getView();
				if(vg!=null){
					vg.removeView(ad_view);
					ad_view=null;
				}
			}
		}else{
			if(ad_view==null){
				createAdView();
			}
			ad_view.setVisibility(View.VISIBLE);
			Activity activity=getActivity();
			if(activity instanceof LiveActivity){
				LiveActivity live=(LiveActivity) activity;
				live.getADManager().onShowAD(url, ad_view);
			}
		}
	}
	private void createAdView(){
		ad_view=new ImageAdView(this.getActivity());
		ad_view.setBackgroundColor(Color.TRANSPARENT);
		if(default_bg==null){
			default_bg=this.getResources().getDrawable(R.drawable.live_broadcast_bg);
		}
		ad_view.setBackgroundDrawable(default_bg);
		ViewGroup vg=(ViewGroup) this.getView();
		vg.addView(ad_view, 1, new LayoutParams(LayoutParams.MATCH_PARENT,LayoutParams.MATCH_PARENT));
	}
}
