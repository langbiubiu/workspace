package com.ipanel.join.chongqing.live.ui;

import java.util.ArrayList;
import java.util.List;

import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import cn.ipanel.android.LogHelper;

import com.ipanel.chongqing_ipanelforhw.R;
import com.ipanel.join.chongqing.live.Constant;
import com.ipanel.join.chongqing.live.base.BaseFragment;
import com.ipanel.join.chongqing.live.manager.UIManager;
import com.ipanel.join.chongqing.live.navi.DatabaseObjects.LiveChannel;
import com.ipanel.join.chongqing.live.util.TaskManager;
import com.ipanel.join.chongqing.live.view.JChannelListView;
import com.ipanel.join.chongqing.live.view.JListView;
import com.ipanel.join.chongqing.live.view.ListViewListenerSet.ListFocusChangeListener;
import com.ipanel.join.chongqing.live.view.ListViewListenerSet.ListOnEnterListener;

public class ChannelListFragment extends BaseFragment {
	
	List<LiveChannel> datas = new ArrayList<LiveChannel>();
	
	JChannelListView channel_list;


	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup root) {
		// TODO Auto-generated method stub
		LogHelper.i("ChannelListFragment", "onCreateView");
		ViewGroup container = (ViewGroup) inflater.inflate(R.layout.live_fragment_channel, root, false);
		channel_list = (JChannelListView) container.findViewById(R.id.channel_list);
//		channel_list.setEnteristener(new ListOnEnterListener() {
//			
//			@Override
//			public void onEnter(int focus) {
//				// TODO Auto-generated method stub
//				getLiveActivity().getStationManager().switchTVChannel(datas.get(focus));
//			}
//		});
		channel_list.setListFocusChangeListener(new ListFocusChangeListener() {
			
			@Override
			public void onFocusChange(int focus) {
				// TODO Auto-generated method stub
				getLiveActivity().getStationManager().switchTVChannel(datas.get(focus));
			}
		});
		return container;
	}

	@Override
	public void onShow() {
		// TODO Auto-generated method stub
		LogHelper.i("ChannelListFragment", "onShow");
		TaskManager.getInstance().clearTask();
		channel_list.clearAnim();
		datas = getLiveActivity().getDataManager().getAllChannel();
		LiveChannel current = getLiveActivity().getStationManager().getPlayChannel();
		channel_list.setShows(datas);
		channel_list.calCurrentIndex(current);
	}

	@Override
	public void onRefresh() {
		// TODO Auto-generated method stub
		LogHelper.i("ChannelListFragment", "onRefresh");
	}

	@Override
	public void onHide() {
		// TODO Auto-generated method stub
		LogHelper.i("ChannelListFragment", "onHide");
	}

	@Override
	public void onDataChange(int type, Object data) {
		// TODO Auto-generated method stub
		LogHelper.i("ChannelListFragment", "onDataChange type = " + type);
		switch (type) {
		case Constant.DATA_CHANGE_OF_PF:
			channel_list.listLayout();
			break;
		default:
			break;
		}
	}
	
	@Override
	public boolean onKeyDown(int keyCode, KeyEvent event) {
		// TODO Auto-generated method stub
		LogHelper.i("ChannelListFragment", "onKeyDown keycode = " + keyCode);
		if (channel_list.onKeyDown(keyCode, event))
			return true;
		switch (keyCode) {
		case KeyEvent.KEYCODE_DPAD_LEFT:
			if (channel_list.getCurrentState() != JListView.STATE_OF_OUT_ANIMING) {
				getLiveActivity().getUIManager().showUI(UIManager.ID_UI_CQ_LIVE_CHANNEL_GROUP, null);
			}
			return true;
		case KeyEvent.KEYCODE_DPAD_RIGHT:
			if (channel_list.getCurrentState() != JListView.STATE_OF_OUT_ANIMING) {
				getLiveActivity().getUIManager().showUI(UIManager.ID_UI_CQ_LIVE_EVENT_LIST, channel_list.shows.get(channel_list.getCurrentIndex()));
			}
			return true;
		default:
			break;
		}
		return super.onKeyDown(keyCode, event);
	}

}
