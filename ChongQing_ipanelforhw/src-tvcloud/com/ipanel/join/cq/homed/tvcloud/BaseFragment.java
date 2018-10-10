package com.ipanel.join.cq.homed.tvcloud;

import com.ipanel.chongqing_ipanelforhw.R;
import com.ipanel.join.chongqing.portal.VolumePanel;

import cn.ipanel.android.widget.ViewFrameZoomIndicator;

import android.app.Fragment;
import android.os.Bundle;
import android.util.DisplayMetrics;
import android.view.View;
import android.widget.FrameLayout;

public class BaseFragment extends Fragment {
	protected int dmHeight = 720;
	protected ViewFrameZoomIndicator frameIndicator;
	protected VolumePanel volPanel;
	/**
	 * find view in the fragment root view
	 * @param id
	 * @return
	 */
	public View findViewById(int id){
		View root = getView();
		if( root != null)
			return root.findViewById(id);
		return null;
	}
	@Override
	public void onActivityCreated(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		DisplayMetrics dm = new DisplayMetrics();
		getActivity().getWindowManager().getDefaultDisplay().getMetrics(dm);
		if (dm.heightPixels == 1080) {
			dmHeight = 1080;
		}
		frameIndicator = new ViewFrameZoomIndicator(getActivity());
		frameIndicator.setFrameResouce(R.color.transparent);
		frameIndicator.setScaleAnimationSize(1.1f, 1.1f);
	}
	/**
	 * ÒÆ¶¯½¹µã
	 * */
	public void moveIndicatorToFocus() {
		View focus = getActivity().getCurrentFocus();
		if (focus instanceof FrameLayout) {
			frameIndicator.moveFrameTo(focus, true, false);
		} else {
			frameIndicator.hideFrame();
		}
	}
}
