package com.ipanel.join.chongqing.live.ui;

import com.ipanel.chongqing_ipanelforhw.R;
import com.ipanel.join.chongqing.live.base.BaseFragment;

import android.animation.Animator;
import android.animation.ObjectAnimator;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

/**
 * ²¥·Å×´Ì¬½çÃæ
 * */
public class WatchFragment extends BaseFragment {

	ImageView mWatch;
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup root) {
		// TODO Auto-generated method stub
		mWatch = (ImageView) (inflater.inflate(
				R.layout.live_frag_watch, root, false));
		return mWatch;
	}

	@Override
	public void onShow() {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void onRefresh() {
		// TODO Auto-generated method stub
		mWatch.setVisibility(getLiveActivity().getStationManager().isShifStart()?View.VISIBLE:View.GONE);
	}

	@Override
	public void onHide() {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void onDataChange(int type, Object data) {
		// TODO Auto-generated method stub
		
	}
	@Override
	public Animator onCreateAnimator(int transit, boolean enter, int nextAnim) {
		// TODO Auto-generated method stub
		float from=enter?0.0f:1.0f;
		float to=enter?1.0f:0.0f;
		ObjectAnimator anim = ObjectAnimator.ofFloat(getView(), "alpha", from,
				to);
		anim.setDuration(DEFAULT_ANIMATION_TIME);
		return anim;
	}

}
