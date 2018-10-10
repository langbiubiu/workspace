package com.ipanel.join.chongqing.live.ui;

import ipaneltv.toolkit.db.DatabaseObjectification.Program;
import android.util.Log;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.RcKeyEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RelativeLayout;
import android.widget.SeekBar;
import android.widget.TextView;

import com.ipanel.chongqing_ipanelforhw.R;
import com.ipanel.join.chongqing.live.Constant;
import com.ipanel.join.chongqing.live.base.BaseFragment;
import com.ipanel.join.chongqing.live.manager.impl.hw.HWDataManagerImpl.HWShiftChannel;
import com.ipanel.join.chongqing.live.manager.impl.hw.HWDataManagerImpl.HWShiftProgram;
import com.ipanel.join.chongqing.live.navi.DatabaseObjects.LiveChannel;
import com.ipanel.join.chongqing.live.util.TimeHelper;

public class ShiftFragment extends BaseFragment{
	
	TextView start_time, end_time, current_event, current_position, current_position2;
	SeekBar seekBar;
	int progress_margin_left;
	int progress_width;
	
	LiveChannel cur_channel;

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup root) {
		// TODO Auto-generated method stub
		ViewGroup container = (ViewGroup) inflater.inflate(R.layout.live_fragment_shift, root, false);
		start_time = (TextView) container.findViewById(R.id.shift_start_time);
		end_time = (TextView) container.findViewById(R.id.shift_end_time);
		current_event = (TextView) container.findViewById(R.id.current_event);
		current_position = (TextView) container.findViewById(R.id.current_position);
		seekBar = (SeekBar) container.findViewById(R.id.shift_progress);
		
		progress_margin_left = 150;
		progress_width = 1620;
		return container;
	}

	@Override
	public void onShow() {
		// TODO Auto-generated method stub
		cur_channel = getLiveActivity().getStationManager().getPlayChannel();
	}

	@Override
	public void onRefresh() {
		// TODO Auto-generated method stub
		long start = 0;
		long end = 0;
		long play = getLiveActivity().getStationManager().getPlayTime();
//		if (getLiveActivity().getStationManager().isHeadShiftMode()) {
//			start = getLiveActivity().getStationManager().getHeadShiftProgram().getStart();
//			end = getLiveActivity().getStationManager().getHeadShiftProgram().getEnd();
//		} else {
//			start = getLiveActivity().getStationManager().getMinShiftTime();
//			end = getLiveActivity().getStationManager().getMaxShiftTime();
//		}
		start = getLiveActivity().getStationManager().getMinShiftTime();
		end = getLiveActivity().getStationManager().getMaxShiftTime();
		Log.e(TAG,"start:"+start+" play:"+play+" end:"+end);
		start_time.setText(TimeHelper.getHourTime(start));
	    end_time.setText(TimeHelper.getHourTime(end));
	    current_position.setText(TimeHelper.getHourTime(play));
	    HWShiftProgram event = (HWShiftProgram) getLiveActivity().getDataManager().getShiftProgramAtTime(cur_channel, play);
		current_event.setText(event.getName());
		int p = (int) ((play - start + 0.0f) * seekBar.getMax() / (end - start + 0.0f) );
		p= Math.max(1, p);
//		if(getLiveActivity().getStationManager().isHeadShiftMode()){
//			Log.e(TAG,"head shift mode true");
//			seekBar.setProgress(p);// 正在播放的进度
//			long now = System.currentTimeMillis() / 1000;
//			int p2 = (int) ((now - start + 0.0f) * seekBar.getMax() / (end - start + 0.0f) );
//			p2 = Math.max(1, p2);
//			seekBar.setSecondaryProgress(p2);
//		}else{
//			Log.e(TAG,"head shift mode false, progress:"+p);
//			seekBar.setProgress(p);
//		}
		seekBar.setProgress(p);
		RelativeLayout.LayoutParams para = (RelativeLayout.LayoutParams) current_position
				.getLayoutParams();
		para.leftMargin = (int) ( progress_margin_left + ((p + 0.0f)
				/ (seekBar.getMax() + 0.0f) * (progress_width)) - para.width / 2);
		current_position.requestLayout();
	}

	@Override
	public void onHide() {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void onDataChange(int type, Object data) {
		// TODO Auto-generated method stub
		switch (type) {
		case Constant.DATA_CHANGE_OF_SHIFT_TICK:
			onRefresh();
			break;
		default:
			break;
		}
	}
	
	@Override
	public boolean onKeyDown(int keyCode, KeyEvent event) {
		// TODO Auto-generated method stub
		switch (keyCode) {
		case KeyEvent.KEYCODE_BACK:
		case RcKeyEvent.KEYCODE_QUIT:
//			if (input.getVisibility() == View.VISIBLE) {
//				input.setVisibility(View.INVISIBLE);
//				return true;
//			}
			return false;
//		case KeyEvent.KEYCODE_ENTER:
//		case KeyEvent.KEYCODE_DPAD_CENTER:
//			if (input.getVisibility() == View.VISIBLE) {
//				long seek_time = input.parseIntArrayTOLong();
//				if (seek_time < 0) {
//					Toast.makeText(getLiveActivity(), R.string.timeout, Toast.LENGTH_SHORT).show();
//					input.setVisibility(View.INVISIBLE);
//				} else {
//					getLiveActivity().getStationManager().seekMedia(seek_time);
//				}
//			}
//			return true;
		case KeyEvent.KEYCODE_DPAD_LEFT:
		case KeyEvent.KEYCODE_DPAD_RIGHT:
			return false;
		case KeyEvent.KEYCODE_MENU:
		case RcKeyEvent.KEYCODE_TV_ADD:
			getUIManager().hideFragment(this);
			return false;
		}
		return true;
	}

}
