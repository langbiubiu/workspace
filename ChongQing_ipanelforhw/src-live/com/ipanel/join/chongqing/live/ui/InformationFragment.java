package com.ipanel.join.chongqing.live.ui;

import ipaneltv.toolkit.db.DatabaseObjectification.Program;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import cn.ipanel.android.LogHelper;

import com.ipanel.chongqing_ipanelforhw.R;
import com.ipanel.join.chongqing.live.Constant;
import com.ipanel.join.chongqing.live.base.BaseFragment;
import com.ipanel.join.chongqing.live.manager.ADManager;
import com.ipanel.join.chongqing.live.navi.DatabaseObjects.LiveChannel;
import com.ipanel.join.chongqing.live.util.ShowHelper;
import com.ipanel.join.chongqing.live.util.TimeHelper;
import com.ipanel.join.chongqing.live.view.LiveSeekBar;

public class InformationFragment extends BaseFragment {
	
	TextView channelName, current_event, next_event, event_time;
	LiveSeekBar progress;
	ImageView ad;

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup root) {
		// TODO Auto-generated method stub
		ViewGroup container = (ViewGroup) inflater.inflate(R.layout.live_fragment_information, root, false);
		channelName = (TextView) container.findViewById(R.id.channelName);
		current_event = (TextView) container.findViewById(R.id.event);
		next_event = (TextView) container.findViewById(R.id.next_event);
		event_time = (TextView) container.findViewById(R.id.event_time);
		progress = (LiveSeekBar) container.findViewById(R.id.watch_progress);
		
		current_event.setSelected(true);
		next_event.setSelected(true);
		return container;
	}

	@Override
	public void onShow() {
		// TODO Auto-generated method stub
		getLiveActivity().getADManager().onShowAD(ADManager.AD_FOR_LIVE_INFO, ad);
		current_event.setVisibility(View.VISIBLE);
		progress.setVisibility(View.VISIBLE);
		caculateShow();
	}
	
	private void caculateShow() {
		// TODO Auto-generated method stub
		LiveChannel channel = getLiveActivity().getStationManager()
				.getPlayChannel();
		if (channel == null) {
			LogHelper.e("error state of empty channel");
			return;
		}
		channelName.setText(channel.getName());
		Program present = getLiveActivity().getDataManager()
				.getChannelCurrentProgram(channel);
		Program follow = getLiveActivity().getDataManager()
				.getChannelNextProgram(channel);
		if (ShowHelper.isPFValid(present, follow)) {
			event_time.setText(TimeHelper.getNowTime(present.getStart()) + "-"
					+ TimeHelper.getNowTime(present.getEnd()));
			progress.setLiveProgress(TimeHelper
					.getCurretnEventProgress(present));
		} else {
			event_time.setText("");
			progress.setLiveProgress(0);
			getLiveActivity().getDataManager().requestChannelPF(channel.getChannelKey());
		}
		current_event.setText(present == null ? "" : present.getName());
		next_event.setText("下一节目：" + (follow == null ? "" : follow.getName()));
	}

	@Override
	public void onRefresh() {
		// TODO Auto-generated method stub
		caculateShow();
	}

	@Override
	public void onHide() {
		// TODO Auto-generated method stub

	}

	@Override
	public void onDataChange(int type, Object data) {
		// TODO Auto-generated method stub
		Log.i(TAG,"onDataChange type = " + type);
		switch (type) {
		case Constant.DATA_CHANGE_OF_PF:
			LiveChannel channel = getLiveActivity().getStationManager()
					.getPlayChannel();
			Program present = getLiveActivity().getDataManager()
					.getChannelCurrentProgram(channel);
			Program follow = getLiveActivity().getDataManager()
					.getChannelNextProgram(channel);
			if (present!=null) {
				progress.setVisibility(View.VISIBLE);
				event_time.setText(TimeHelper.getNowTime(present.getStart()) + "-"
						+ TimeHelper.getNowTime(present.getEnd()));
				progress.setLiveProgress(TimeHelper
						.getCurretnEventProgress(present));
				current_event.setText(present.getName());
				if(follow !=null){
					next_event.setText(String.format("下一节目：%s",follow.getName()));
				}else{
					next_event.setText("");
				}
			} else {
				event_time.setText("");
				progress.setLiveProgress(0);
				progress.setVisibility(View.INVISIBLE);
				current_event.setText(getResources().getString(R.string.no_pf_data));
				if(follow !=null){
					next_event.setText(String.format("下一节目：%s",follow.getName()));
				}else{
					next_event.setText("");
				}
			}
			break;
		case Constant.DATA_CHANGE_OF_PF_WITH_KEY:
			channel = getLiveActivity().getStationManager().getPlayChannel();
			if (channel.getChannelKey().equals(data)) {
				present = getLiveActivity().getDataManager()
						.getChannelCurrentProgram(channel);
				follow = getLiveActivity().getDataManager()
						.getChannelNextProgram(channel);
				if (present!=null) {
					event_time.setText(TimeHelper.getNowTime(present.getStart()) + "-"
							+ TimeHelper.getNowTime(present.getEnd()));
					progress.setVisibility(View.VISIBLE);
					progress.setLiveProgress(TimeHelper
							.getCurretnEventProgress(present));
					current_event.setText(present.getName());
					if(follow !=null){
						next_event.setText(String.format("下一节目：%s",follow.getName()));
					}else{
						next_event.setText("");
					}

				} else {
					event_time.setText("");
					progress.setVisibility(View.INVISIBLE);
					progress.setLiveProgress(0);
					current_event.setText(getResources().getString(R.string.no_pf_data));
					if(follow !=null){
						next_event.setText(String.format("下一节目：%s",follow.getName()));
					}else{
						next_event.setText("");
					}
				}
//				current_event.setText(present == null ? getResources().getString(R.string.no_pf_data) : present.getName());
//				next_event.setText(follow == null ? "" : follow.getName());
			} else {
				LogHelper.e("not match key for pf refresh");
			}
			break;
		}
	}

}
