package com.ipanel.join.cq.vod.player.impl;

import com.ipanel.chongqing_ipanelforhw.Config;

import ipaneltv.toolkit.media.TeeveeWidgetFragment;
import android.net.telecast.NetworkManager;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.SurfaceView;
import android.view.View;
import android.view.ViewGroup;

public class WidgetFragment extends TeeveeWidgetFragment {

	public static WidgetFragment createInstance() {
		Bundle b = createArguments(Config.UUID,
				NetworkManager.PROPERTY_TEEVEE_WIDGET, 101);
		WidgetFragment f = new WidgetFragment();
		f.setArguments(b);
		return f;
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {

		View v = super.onCreateView(inflater, container, savedInstanceState);
		Log.d(this.toString(), "onCreateView v = "+ v);
		if (v == null) {
			v = new SurfaceView(getActivity());
			// v.setBackgroundColor(0x000055);// 没有找到视频装口Widget，深蓝色背景替代
		}
		return v;
	}
}
