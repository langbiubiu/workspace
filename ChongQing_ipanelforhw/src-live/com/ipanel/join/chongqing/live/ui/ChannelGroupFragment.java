package com.ipanel.join.chongqing.live.ui;

import java.util.List;

import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import cn.ipanel.android.LogHelper;

import com.ipanel.chongqing_ipanelforhw.R;
import com.ipanel.join.chongqing.live.base.BaseFragment;
import com.ipanel.join.chongqing.live.manager.UIManager;
import com.ipanel.join.chongqing.live.manager.impl.hw.HWDataManagerImpl;
import com.ipanel.join.chongqing.live.navi.DatabaseObjects.LiveChannel;
import com.ipanel.join.chongqing.live.navi.DatabaseObjects.LiveGroup;
import com.ipanel.join.chongqing.live.view.JChannelGroupListView;
import com.ipanel.join.chongqing.live.view.JChannelListView_b;
import com.ipanel.join.chongqing.live.view.JListView;
import com.ipanel.join.chongqing.live.view.ListViewListenerSet.ListFocusChangeListener;

public class ChannelGroupFragment extends BaseFragment{
	
	JChannelGroupListView group_list;
	JChannelListView_b channel_list;
	
	private int current_focus = -1;

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup root) {
		// TODO Auto-generated method stub
		LogHelper.i("ChannelGroupFragment", "onCreateView");
		ViewGroup container = (ViewGroup) inflater.inflate(R.layout.live_fragment_navigation, root, false);
		group_list = (JChannelGroupListView) container.findViewById(R.id.channel_group_list);
		channel_list = (JChannelListView_b) container.findViewById(R.id.channel_list_02);
		group_list.setListFocusChangeListener(new ListFocusChangeListener() {
			
			@Override
			public void onFocusChange(int focus) {
				// TODO Auto-generated method stub
				LiveGroup lg = group_list.shows.get(focus);
				requestChannelsData(lg.getId());
			}
		});
		channel_list.setListFocusChangeListener(new ListFocusChangeListener() {
			
			@Override
			public void onFocusChange(int focus) {
				// TODO Auto-generated method stub
				LiveChannel channel = channel_list.shows.get(focus);
				getLiveActivity().getStationManager().switchTVChannel(channel);
			}
		});
		return container;
	}
	
	private void requestChannelsData(int id) {
		LiveGroup lg = getLiveActivity().getDataManager().getGroupByID(id);
		List<LiveChannel> result = ((HWDataManagerImpl)(getLiveActivity().getDataManager())).getGroupedChannels(lg);
		if (result != null && result.size() > 0) {
			LogHelper.i("request group channel result size is " + result.size());
			channel_list.setVisibility(View.INVISIBLE);
			channel_list.setShows(result);
			channel_list.setVisibility(View.VISIBLE);
		} else {
			LogHelper.i("request group channel is null");
			channel_list.clearData();
			channel_list.setVisibility(View.INVISIBLE);
		}
	}

	@Override
	public void onShow() {
		// TODO Auto-generated method stub
		LogHelper.i("ChannelGroupFragment", "onShow");
		group_list.clearAnim();
		channel_list.clearAnim();
		List<LiveGroup> datas = getLiveActivity().getDataManager().getAllGroup();
		if (datas == null || datas.size() == 0) {
			Toast.makeText(getLiveActivity(), "未获取到频道分组信息", Toast.LENGTH_SHORT).show();
			getUIManager().showUI(UIManager.ID_UI_CQ_LIVE_CHANNEL_LIST, null);
			return;
		}
		group_list.setShow(datas);
		group_list.setHasFocus(false);
		
		channel_list.setHasFocus(true);
		requestChannelsData(datas.get(0).getId());
		group_list.animIn();
		group_list.setVisibility(View.VISIBLE);
		channel_list.animIn();
		channel_list.setVisibility(View.VISIBLE);
		
		if (group_list.isHasFocus()) {
			group_list.focus();
			channel_list.blur();
			current_focus = 1;
		} else {
			group_list.blur();
			channel_list.focus();
			current_focus = 2;
		}
	}

	@Override
	public void onRefresh() {
		// TODO Auto-generated method stub
		LogHelper.i("ChannelGroupFragment", "onRefresh");
	}

	@Override
	public void onHide() {
		// TODO Auto-generated method stub
		LogHelper.i("ChannelGroupFragment", "onHide");
	}

	@Override
	public void onDataChange(int type, Object data) {
		// TODO Auto-generated method stub
		LogHelper.i("ChannelGroupFragment", "onDataChange type = " + type);
	}

	@Override
	public boolean onKeyDown(int keyCode, KeyEvent event) {
		// TODO Auto-generated method stub
		switch (keyCode) {
		case KeyEvent.KEYCODE_DPAD_LEFT:
			if (current_focus == 1) {
				if (group_list.getCurrentState() != JListView.STATE_OF_OUT_ANIMING) {
					
				}
			} else if (current_focus == 2) {
				if (group_list.getCurrentState() == JListView.STATE_OF_VISIBLE
						|| group_list.getCurrentState() == JListView.STATE_OF_NORMAL) {
					current_focus = 1;
					group_list.setHasFocus(true);
					group_list.focus();
					channel_list.setHasFocus(false);
					channel_list.blur();
					group_list.invalidateAll();
					channel_list.invalidateAll();
				}
			}
			return true;
		case KeyEvent.KEYCODE_DPAD_RIGHT:
			if (current_focus == 1) {
				if (group_list.getCurrentState() == JListView.STATE_OF_VISIBLE
						|| group_list.getCurrentState() == JListView.STATE_OF_NORMAL) {
					current_focus = 2;
					group_list.setHasFocus(false);
					group_list.blur();
					channel_list.setHasFocus(true);
					channel_list.focus();
					group_list.invalidateAll();
					channel_list.invalidateAll();
				}
			} else if (current_focus == 2) {
				if (channel_list.getCurrentState() != JListView.STATE_OF_IN_ANIMING && channel_list.shows != null && channel_list.shows.size() > 0) {
					getLiveActivity().getUIManager().showUI(UIManager.ID_UI_CQ_LIVE_CHANNEL_LIST, null);
				}
			}
			return true;
		}
		return super.onKeyDown(keyCode, event);
	}

}
