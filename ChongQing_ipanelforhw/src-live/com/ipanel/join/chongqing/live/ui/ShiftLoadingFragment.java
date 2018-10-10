package com.ipanel.join.chongqing.live.ui;

import ipanel.join.ad.widget.ImageAdView;

import java.text.MessageFormat;

import com.ipanel.chongqing_ipanelforhw.R;
import com.ipanel.join.chongqing.live.Constant;
import com.ipanel.join.chongqing.live.base.BaseFragment;
import com.ipanel.join.chongqing.live.manager.ADManager;

import android.animation.Animator;
import android.animation.ObjectAnimator;
import android.os.Message;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.LinearInterpolator;
import android.view.animation.RotateAnimation;
import android.widget.ImageView;
import android.widget.TextView;

/**
 * 时移加载界面
 * */
public class ShiftLoadingFragment extends BaseFragment {

	public static final int MSG_LOADING_TIME_TICK = 10;
	TextView loading_shows;
	ImageView loading_img;
	Animation mRotate;
	public int count_down;
	ImageAdView ad;


	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup root) {
		// TODO Auto-generated method stub
		ViewGroup container = (ViewGroup) (inflater.inflate(
				R.layout.live_frag_homed_shift_loading, root, false));
		ad=null;//(ImageAdView) container.findViewById(R.id.ad);

		loading_shows = (TextView) container.findViewById(R.id.loading_shows);
		loading_img = (ImageView) container.findViewById(R.id.loading_img);
		return container;
	}

	@Override
	public void onShow() {
		// TODO Auto-generated method stub
		getLiveActivity().getADManager().onShowAD(ADManager.AD_FOR_SHIFT_LOADING, ad);
		getView().setVisibility(View.GONE);
		count_down = 6;
		mHandler.sendEmptyMessage(MSG_LOADING_TIME_TICK);
		loading_img.setVisibility(View.GONE);

//		if (mRotate == null) {
//			mRotate = new RotateAnimation(360, 0, Animation.RELATIVE_TO_SELF,
//					0.5f, Animation.RELATIVE_TO_SELF, 0.5f);
//			mRotate.setDuration(1000);
//			mRotate.setRepeatCount(Integer.MAX_VALUE);
//			mRotate.setInterpolator(new LinearInterpolator());
//		} else {
//			mRotate.reset();
//		}
//		loading_img.setAnimation(mRotate);
		getLiveActivity().getDataManager().requestShiftURL(getLiveActivity().getStationManager().getPlayChannel());

	}

	@Override
	public void onRefresh() {
		// TODO Auto-generated method stub

	}

	@Override
	public void onHide() {
		// TODO Auto-generated method stub
		mHandler.removeMessages(MSG_LOADING_TIME_TICK);
	}

	@Override
	public void onDataChange(int type, Object data) {
		// TODO Auto-generated method stub

	}
	
	@Override
	public boolean onKeyDown(int keyCode, KeyEvent event) {
		// TODO Auto-generated method stub
		if(keyCode==KeyEvent.KEYCODE_BACK){
			getLiveActivity().getStationManager().startLivePlay();
		}
		return generalKeyHandle(keyCode,event);
	}
	
	@Override
	public boolean onKeyUp(int keyCode, KeyEvent event) {
		// TODO Auto-generated method stub
		return generalKeyHandle(keyCode,event);
	}

	@Override
	public void handleFragmentMessage(Message msg) {
		// TODO Auto-generated method stub
		if (msg.what == MSG_LOADING_TIME_TICK) {
			count_down--;
			if (count_down >= 0) {
				if (count_down <=4) {
					Constant.COUNT_DOWN_READY=true;
				}
				mHandler.sendEmptyMessageDelayed(MSG_LOADING_TIME_TICK, 1000);
				String show = MessageFormat.format(this.getResources()
						.getString(R.string.count_down), count_down);
				loading_shows.setText(R.string.play_loading);
			}
			return;
		}
		super.handleFragmentMessage(msg);
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
