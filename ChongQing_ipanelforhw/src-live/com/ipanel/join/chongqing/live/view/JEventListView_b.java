package com.ipanel.join.chongqing.live.view;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import android.content.Context;
import android.graphics.Color;
import android.util.AttributeSet;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.ipanel.chongqing_ipanelforhw.R;
import com.ipanel.join.chongqing.live.LiveActivity;
import com.ipanel.join.chongqing.live.navi.DatabaseObjects.LiveProgramEvent;
import com.ipanel.join.chongqing.live.util.TimeHelper;

public class JEventListView_b extends JListView {

	public List<LiveProgramEvent> shows = new ArrayList<LiveProgramEvent>();
	int mFocusColor = Color.parseColor("#000000");
	int mNormalColor = Color.parseColor("#f0f0f0");
	int mSelectColor = Color.parseColor("#ffb400");
	private String channel_name;
	private int default_show_count = 10;
	private boolean hasFocus = false;
	
	LiveActivity mActivity;

	public JEventListView_b(Context context) {
		super(context);
		// TODO Auto-generated constructor stub
		mActivity = (LiveActivity)context;
	}

	public JEventListView_b(Context context, AttributeSet attrs) {
		super(context, attrs);
		// TODO Auto-generated constructor stub
		mActivity = (LiveActivity)context;
	}

	@Override
	public void init() {
		// TODO Auto-generated method stub
		item_resourceId = R.layout.live_item_program_02;
		list_item_width = 560;
		list_item_height = 105;
		show_count = 8;
		defaultCenter=0;
		move_flag=true;
		circle_flag=false;
		defaultAnimDelay=0;
		super.init();
	}
	
	public void setShow(List<LiveProgramEvent> shows,int show,String channel_name) {
		this.channel_name=channel_name;
		boolean flag=false;
		if(data_count>0&&this.shows .size()==shows.size()){
			for(int i=0;i<shows.size();i++){
				if(!this.shows.get(i).equals(shows.get(i))){
					flag=true;
					break;
				}
			}
		}else{
			flag=true;
		}
		if(!flag&&default_show_count ==show){
			this.resetList();
		}else{
			default_show_count=show;
			this.shows = shows;
			init();
		}
	}

	@Override
	public View getListView(int position, View convertView) {
		// TODO Auto-generated method stub
		if (convertView == null)
			convertView = tmpView;
		ImageView book_mark = (ImageView) convertView.findViewById(R.id.book_mark);
		TextView event = (TextView) convertView.findViewById(R.id.program);
		TextView time = (TextView) convertView.findViewById(R.id.start_time);
		LiveProgramEvent e = shows.get(position % getDataCount());
		boolean isbooked = mActivity.getBookManager().isProgramBooked(e.getStart(), e.getChannelKey().getProgram());
		if (isbooked) {
			book_mark.setImageResource(R.drawable.live_epg_yy_1);
			book_mark.setVisibility(View.VISIBLE);
		} else {
			book_mark.setVisibility(View.INVISIBLE);
		}
		event.setText(e.getName());
		time.setText(TimeHelper.getHourTime(e.getStart()));
		long now = System.currentTimeMillis();
		convertView.setBackgroundResource(android.R.color.transparent);
		if (e.getStart() < now && e.getEnd() >= now) {
			event.setTextColor(mSelectColor);
			time.setTextColor(mSelectColor);
			event.setSelected(true);
			time.setSelected(true);
		} else {
			event.setTextColor(mNormalColor);
			time.setTextColor(mNormalColor);
			event.setSelected(false);
			time.setSelected(false);
		}
		if(position==this.getCurrentIndex() && hasFocus){
			convertView.setBackgroundResource(R.color.list_bg_color);
			event.setTextColor(mFocusColor);
			time.setTextColor(mFocusColor);
			event.setSelected(true);
			time.setSelected(true);
			if (isbooked)
				book_mark.setImageResource(R.drawable.live_epg_yy_2);
		}
		return convertView;
	}

	@Override
	public int getDataCount() {
		// TODO Auto-generated method stub
		return shows==null?0:shows.size();
	}
	
	public boolean isHasFocus() {
		return hasFocus;
	}

	public void setHasFocus(boolean hasFocus) {
		this.hasFocus = hasFocus;
	}

	public void clearData() {
		// TODO Auto-generated method stub
		this.shows = null;
		data_count = 0;
	}
	
	public void reSetBookFlag(){
		postDelayed(new Runnable() {
			
			@Override
			public void run() {
				// TODO Auto-generated method stub
				long time = shows.get(getCurrentIndex()).getStart();
				int program = shows.get(getCurrentIndex()).getChannelKey().getProgram();
				boolean flag = mActivity.getBookManager().isProgramBooked(time, program);
				setBookFlag(flag);
			}
		}, 100);
	}
	
	public void setBookFlag(boolean flag){
		ImageView book_flag = (ImageView) views.get(selector_index).findViewById(R.id.book_mark);
		Log.e(TAG,"setBookFlag flag:"+flag+" view:"+book_flag);
		if(book_flag!=null){
			
			if(flag){
				book_flag.setVisibility(View.VISIBLE);
			}else{
				book_flag.setVisibility(View.INVISIBLE);
			}
		}
		invalidateAll();
	}

	public void calCurrent() {
		// TODO Auto-generated method stub
		if (data_count < 2) {
			return;
		}
		long now = new Date().getTime();
		for (int i = 0; i < data_count; i++) {
			long start = shows.get(i).getStart();
			long end = shows.get(i).getEnd();
			if (now >= start && now < end) {
				this.setCurrentIndexNCircle(i);
				this.invalidateAll();
				return;
			}
		}
	}
}
