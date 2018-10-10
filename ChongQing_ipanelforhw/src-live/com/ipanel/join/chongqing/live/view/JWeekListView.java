package com.ipanel.join.chongqing.live.view;

import java.util.Calendar;
import java.util.Date;

import android.content.Context;
import android.graphics.Color;
import android.util.AttributeSet;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.ipanel.chongqing_ipanelforhw.R;

public class JWeekListView extends JListView {
	
	String[] weeks = {"周一", "周二", "周三", "周四", "周五", "周六", "周日"};
	int mFocusColor = Color.parseColor("#000000");
	int mNormalColor = Color.parseColor("#f0f0f0");
	int mSelectColor = Color.parseColor("#ffb400");
	int today_position = 0;
	private boolean hasFocus = false;

	public JWeekListView(Context context) {
		super(context);
		// TODO Auto-generated constructor stub
	}

	public JWeekListView(Context context, AttributeSet attrs) {
		super(context, attrs);
		// TODO Auto-generated constructor stub
	}
	
	@Override
	public void init() {
		// TODO Auto-generated method stub
		item_resourceId = R.layout.live_item_week;
		list_item_width = 210;
		list_item_height = 105;
		show_count = 7;
		defaultCenter = 3;
		move_flag = true;
		circle_flag = false;
		defaultAnimDelay = 20;
		super.init();
	}
	
	public void setShow() {
		Date date = new Date();
		Calendar c = Calendar.getInstance();
		c.setTime(date);
		today_position = c.get(Calendar.DAY_OF_WEEK);
		init();
	}

	@Override
	public View getListView(int position, View convertView) {
		// TODO Auto-generated method stub
		if (convertView == null) {
			convertView = tmpView;
		}
		TextView week = (TextView) convertView.findViewById(R.id.week);
		ImageView flag = (ImageView) convertView.findViewById(R.id.week_flag);
		week.setText(weeks[(today_position + position + 2) % 7]);
		if (position < 3)
			flag.setImageResource(R.drawable.live_icon002);
		if (position == 3) 
			flag.setImageResource(R.drawable.live_icon003);
		if (position > 3)
			flag.setImageResource(R.drawable.live_icon001);
		if(position==this.getCurrentIndex()){
			if (hasFocus) {
				convertView.setBackgroundResource(R.color.list_bg_color);
				week.setTextColor(mFocusColor);
			} else {
				convertView.setBackgroundResource(android.R.color.transparent);
				week.setTextColor(mSelectColor);
			}
			week.setSelected(true);
		}else{
			convertView.setBackgroundResource(android.R.color.transparent);
			week.setTextColor(mNormalColor);
			week.setSelected(false);
		}
		return convertView;
	}

	@Override
	public int getDataCount() {
		// TODO Auto-generated method stub
		return 7;
	}

	public boolean isHasFocus() {
		return hasFocus;
	}

	public void setHasFocus(boolean hasFocus) {
		this.hasFocus = hasFocus;
	}

}
