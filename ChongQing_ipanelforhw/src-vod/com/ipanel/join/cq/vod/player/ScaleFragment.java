package com.ipanel.join.cq.vod.player;

import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import cn.ipanel.android.LogHelper;

import com.ipanel.chongqing_ipanelforhw.R;
import com.ipanel.join.cq.vod.utils.Logger;

public class ScaleFragment extends BaseFragment {
	public final static int SETTING_SOUND_TRACK = 1;
	public final static int SETTING_DISPLAY_SCALE = 2;
	public int current_setting;
	String[] sound_track_shows;
	String[] display_scale_shows;

	TextView setting_item;
	TextView setting_value;
	ImageView setting_left;
	ImageView setting_right;

	@Override
	public void showFragment() {
		super.showFragment();
		display_scale_shows = mActivity.getResources().getStringArray(
				R.array.display_scale);
	}
	
	@Override
	public void refreshFragment() {
		
		super.refreshFragment();
		current_setting = VodPlayerManager.getInstance(getActivity()).getScaleValue();
		LogHelper.v("handleShow  current_setting=" + current_setting);
		setting_item.setText(R.string.scale_setting);
		setting_value.setText(display_scale_shows[current_setting]);
		setting_left.setImageResource(R.drawable.vod_setting_left_normal);
		setting_right.setImageResource(R.drawable.vod_setting_right_normal);
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup root) {
		
		ViewGroup container = createContainer(inflater, root,
				R.layout.vod_fragment_setting_view);
		setting_item = (TextView) container.findViewById(R.id.setting_item);
		setting_value = (TextView) container.findViewById(R.id.setting_value);
		setting_left = (ImageView) container.findViewById(R.id.setting_left_btn);
		setting_right = (ImageView) container.findViewById(R.id.setting_right_btn);
		return container;
	}

	@Override
	protected int getHideDelay() {
		return 3000;
	}

	@Override
	public boolean onKeyDown(int keyCode, KeyEvent event) {// 界面按键处理
		Logger.d("SettingNavigation  keyCode=" + keyCode);
		resetHideTimer();
		switch (keyCode) {
		case KeyEvent.KEYCODE_DPAD_LEFT:
		case KeyEvent.KEYCODE_DPAD_RIGHT:
			VodPlayerManager.getInstance(getActivity()).changeScale();
			mHandler.sendEmptyMessage(MessageColection.NAV_MESSAGE_UPDATE_SELF);
			return true;

		default:
			break;
		}
		return super.onKeyDown(keyCode, event);
	}

}
