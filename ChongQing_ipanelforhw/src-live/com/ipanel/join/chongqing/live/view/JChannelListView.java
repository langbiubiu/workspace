package com.ipanel.join.chongqing.live.view;

import java.util.ArrayList;
import java.util.List;

import android.content.Context;
import android.graphics.Color;
import android.util.AttributeSet;
import android.view.View;
import android.widget.TextView;
import cn.ipanel.android.LogHelper;

import com.ipanel.chongqing_ipanelforhw.R;
import com.ipanel.join.chongqing.live.LiveActivity;
import com.ipanel.join.chongqing.live.manager.impl.hw.HWStationManagerImpl;
import com.ipanel.join.chongqing.live.navi.DatabaseObjects.LiveChannel;

public class JChannelListView extends JListView {
	
	public List<LiveChannel> shows = new ArrayList<LiveChannel>();
	
	int mFocusColor = Color.parseColor("#000000");
	int mNormalColor = Color.parseColor("#f0f0f0");
	int mNormalColor_1 = Color.parseColor("#999999");
	private boolean hasFocus = false;
	
	LiveActivity mActivity;

	public JChannelListView(Context context) {
		super(context);
		mActivity = (LiveActivity)context;
	}

	public JChannelListView(Context context, AttributeSet attrs) {
		super(context, attrs);
		mActivity = (LiveActivity)context;
	}
	
	@Override
	public void init() {
		// TODO Auto-generated method stub
		item_resourceId = R.layout.live_item_channel_01;
		list_item_width = 746;
		list_item_height = 105;
		show_count = 9;
		defaultCenter=3;
		move_flag=true;
		super.init();
	}
	
	public void setShows(List<LiveChannel> shows) {
		this.shows = shows;
		init();
	}

	@Override
	public View getListView(int position, View convertView) {
		// TODO Auto-generated method stub
		if (convertView == null) {
			convertView = tmpView;
		}
		LiveChannel channel = shows.get(position % getDataCount());
		TextView channel_name = (TextView) convertView.findViewById(R.id.channel_name);
		TextView current_event = (TextView) convertView.findViewById(R.id.current_event);
		channel_name.setText(channel.getName());
		if (channel.getPresent() == null) {
			current_event.setText("");
			mActivity.getDataManager().requestChannelPF(channel.getChannelKey());
		} else {
			current_event.setText(channel.getPresent().getName());
		}
		LogHelper.i("channel list view channel name is " + channel.getName());
		if(position==this.getCurrentIndex()){
			LogHelper.i("channel list view is current");
			convertView.setBackgroundResource(R.color.list_bg_color);
			channel_name.setTextColor(mFocusColor);
			current_event.setTextColor(mFocusColor);
			channel_name.setSelected(true);
			current_event.setSelected(true);
		} else{
			LogHelper.i("channel list view is not current");
			convertView.setBackgroundResource(android.R.color.transparent);
			channel_name.setTextColor(mNormalColor);
			current_event.setTextColor(mNormalColor_1);
			channel_name.setSelected(false);
			current_event.setSelected(false);
		}
		return convertView;
	}

	@Override
	public int getDataCount() {
		// TODO Auto-generated method stub
		return shows == null ? 0 : shows.size();
	}
	
	public void calCurrentIndex(LiveChannel p) {
		int length = shows.size();
		for (int i = 0; i < length; i++) {
			if (p.getChannelKey().getFrequency() == shows.get(i).getChannelKey().getFrequency()
					&& p.getChannelKey().getProgram() == shows.get(i).getChannelKey().getProgram()) {
				this.moveSelector(defaultCenter);
				this.setCurrentIndex(i);
				return;
			}
		}
	}
	
	public LiveChannel getCurrentProgram(){
		return shows.get(this.getCurrentIndex());
	}

	public boolean isHasFocus() {
		return hasFocus;
	}

	public void setHasFocus(boolean hasFocus) {
		this.hasFocus = hasFocus;
	}

}
