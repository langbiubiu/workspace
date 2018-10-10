package com.ipanel.join.chongqing.live.ui;

import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.RcKeyEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.View.OnClickListener;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.ipanel.chongqing_ipanelforhw.R;
import com.ipanel.join.chongqing.live.base.BaseFragment;

public class ShiftPauseFragment extends BaseFragment {
	
	RelativeLayout ad_rl;
	ImageView ad;
	TextView ok, cancel;
	
	boolean isAdHide = false;

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup root) {
		// TODO Auto-generated method stub
		ViewGroup vg = (ViewGroup) inflater.inflate(R.layout.live_fragment_shift_quit, root, false);
		ad_rl = (RelativeLayout) vg.findViewById(R.id.ad_rl);
		ad = (ImageView) vg.findViewById(R.id.shift_ad);
		ok = (TextView) vg.findViewById(R.id.btn_ok);
		cancel = (TextView) vg.findViewById(R.id.btn_cancel);
		ad.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View arg0) {
				// TODO Auto-generated method stub
				
			}
		});
		ok.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View arg0) {
				// TODO Auto-generated method stub
				getLiveActivity().getStationManager().playOrPauseMedia();
			}
		});
		cancel.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View arg0) {
				// TODO Auto-generated method stub
				ad_rl.setVisibility(View.INVISIBLE);
				isAdHide = true;
			}
		});
		return vg;
	}

	@Override
	public void onShow() {
		// TODO Auto-generated method stub
		isAdHide = false;
		ok.requestFocus();
	}

	@Override
	public void onRefresh() {
		// TODO Auto-generated method stub
		
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
	public boolean onKeyDown(int keyCode, KeyEvent event) {
		// TODO Auto-generated method stub
		switch (RcKeyEvent.getRcKeyCode(event)) {
		case RcKeyEvent.KEYCODE_QUIT:
		case RcKeyEvent.KEYCODE_BACK:
			getLiveActivity().getStationManager().startLivePlay();
			return true;
		case KeyEvent.KEYCODE_DPAD_CENTER:
		case KeyEvent.KEYCODE_ENTER:
			if (isAdHide) {
				getLiveActivity().getStationManager().playOrPauseMedia();
			}
			return true;
		default:
			break;
		}
		return super.onKeyDown(keyCode, event);
	}

}
