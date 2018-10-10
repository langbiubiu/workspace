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
import com.ipanel.join.chongqing.live.navi.DatabaseObjects.LiveChannel;

public class JChannelListView_b extends JListView {
	
	public List<LiveChannel> shows = new ArrayList<LiveChannel>();
	int mFocusColor = Color.parseColor("#000000");
	int mNormalColor = Color.parseColor("#f0f0f0");
	int mSelectColor = Color.parseColor("#ffb400");
	private boolean hasFocus = false;
	LiveActivity mActivity;

	public JChannelListView_b(Context context) {
		super(context);
		// TODO Auto-generated constructor stub
		mActivity = (LiveActivity)context;
	}

	public JChannelListView_b(Context context, AttributeSet attrs) {
		super(context, attrs);
		// TODO Auto-generated constructor stub
		mActivity = (LiveActivity)context;
	}

	@Override
	public void init() {
		// TODO Auto-generated method stub
		item_resourceId = R.layout.live_item_channel_02;
		list_item_width = 330;
		list_item_height = 105;
		show_count = 9;
		defaultCenter=0;
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
		boolean isCurrentChannel = false;
		if (convertView == null) {
			convertView = tmpView;
		}
		LiveChannel channel = shows.get(position % getDataCount());
		if (mActivity.getStationManager().getPlayChannel().getChannelKey().getProgram() == channel.getChannelKey().getProgram()) {
			isCurrentChannel = true;
		}
		TextView channel_name = (TextView) convertView.findViewById(R.id.channel_02);
		channel_name.setText(channel.getName());
		LogHelper.i("222----channel list view channel name is " + channel.getName());
		if(position==this.getCurrentIndex() && hasFocus){
			LogHelper.i("222----channel list view is current");
			convertView.setBackgroundResource(R.color.list_bg_color);
			channel_name.setTextColor(mFocusColor);
			channel_name.setSelected(true);
		} else if (position == this.getCurrentIndex() && !hasFocus && isCurrentChannel) {
			LogHelper.i("222----channel list view is current but has no focus");
			convertView.setBackgroundResource(android.R.color.transparent);
			channel_name.setTextColor(mSelectColor);
			channel_name.setSelected(false);
		} else{
			LogHelper.i("222----channel list view is not current");
			convertView.setBackgroundResource(android.R.color.transparent);
			channel_name.setTextColor(mNormalColor);
			channel_name.setSelected(false);
		}
		return convertView;
	}

	@Override
	public int getDataCount() {
		// TODO Auto-generated method stub
		return shows == null ? 0 : shows.size();
	}

	public boolean isHasFocus() {
		return hasFocus;
	}

	public void setHasFocus(boolean hasFocus) {
		this.hasFocus = hasFocus;
	}

	public void calCurrentIndex() {
		// TODO Auto-generated method stub
		
	}

	public void clearData() {
		// TODO Auto-generated method stub
		this.shows = null;
		data_count=0;
	}

}
