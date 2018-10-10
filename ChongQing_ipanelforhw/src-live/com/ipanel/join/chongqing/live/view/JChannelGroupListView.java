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
import com.ipanel.join.chongqing.live.navi.DatabaseObjects.LiveGroup;

public class JChannelGroupListView extends JListView {
	
	int mFocusColor = Color.parseColor("#000000");
	int mNormalColor = Color.parseColor("#f0f0f0");
	int mSelectColor = Color.parseColor("#ffb400");
	private boolean hasFocus = false;
	
	public List<LiveGroup> shows = new ArrayList<LiveGroup>();

	public JChannelGroupListView(Context context) {
		super(context);
		// TODO Auto-generated constructor stub
	}

	public JChannelGroupListView(Context context, AttributeSet attrs) {
		super(context, attrs);
		// TODO Auto-generated constructor stub
	}
	
	public void setShow(List<LiveGroup> shows) {
		this.shows = shows;
		init();
	}
	
	@Override
	public void init() {
		// TODO Auto-generated method stub
		item_resourceId = R.layout.live_item_navigation;
		list_item_width = 240;
		list_item_height = 105;
		show_count = 9;
		defaultCenter = 0;
		move_flag = true;
		circle_flag = false;
		defaultAnimDelay = 20;
		super.init();
	}

	@Override
	public View getListView(int position, View convertView) {
		// TODO Auto-generated method stub
		if (convertView == null) {
			convertView = tmpView;
		}
		TextView navi = (TextView) convertView.findViewById(R.id.navi);
		LiveGroup lg = shows.get(position);
		navi.setText(lg.getName());
		LogHelper.i("group list view group name is " + lg.getName());
		if(position==this.getCurrentIndex() && hasFocus){
			LogHelper.i("channel list view is current");
			convertView.setBackgroundResource(R.color.list_bg_color);
			navi.setTextColor(mFocusColor);
			navi.setSelected(true);
		} else if (position == this.getCurrentIndex() && !hasFocus) {
			LogHelper.i("channel list view is current but has no focus");
			convertView.setBackgroundResource(android.R.color.transparent);
			navi.setTextColor(mSelectColor);
			navi.setSelected(false);
		} else{
			LogHelper.i("channel list view is not current");
			convertView.setBackgroundResource(android.R.color.transparent);
			navi.setTextColor(mNormalColor);
			navi.setSelected(false);
		}
		return convertView;
	}

	@Override
	public int getDataCount() {
		// TODO Auto-generated method stub
		return shows == null ? 0 : shows.size();
	}
	
	public void calCurrentIndex(LiveGroup lg) {
		int length = shows.size();
		for (int i = 0; i < length; i++) {
			if (shows.get(i).getId() == lg.getId()) {
				this.setCurrentIndex(i);
				return;
			}
		}
	}
	
	public LiveGroup getCurrentGroup() {
		return shows.get(this.getCurrentIndex());
	}

	public boolean isHasFocus() {
		return hasFocus;
	}

	public void setHasFocus(boolean hasFocus) {
		this.hasFocus = hasFocus;
	}

}
