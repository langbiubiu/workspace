package com.ipanel.join.cq.vod.player;

import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.RcKeyEvent;
import android.view.View;
import android.view.ViewGroup;

public class EmptyFragment extends BaseFragment {

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup root) {
		return null;
		
	}

	@Override
	public boolean onKeyDown(int keyCode, KeyEvent event) {
		if (keyCode == KeyEvent.KEYCODE_BACK||keyCode == RcKeyEvent.KEYCODE_QUIT) {
			getActivity().onBackPressed();
			return true;
		}
		return super.onKeyDown(keyCode, event);
	}

}
