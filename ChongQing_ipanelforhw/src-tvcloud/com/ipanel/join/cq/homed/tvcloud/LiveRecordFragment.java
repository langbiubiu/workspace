package com.ipanel.join.cq.homed.tvcloud;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Comparator;
import java.util.Date;
import java.util.List;

import cn.ipanel.android.LogHelper;
import cn.ipanel.android.net.http.RequestParams;

import com.ipanel.chongqing_ipanelforhw.CQApplication;
import com.ipanel.chongqing_ipanelforhw.R;
import com.ipanel.join.cq.homed.tvcloud.RecordedFragment.RecordAdapter;
import com.ipanel.join.cq.homed.tvcloud.db.RecordDataBaseHelper;
import com.ipanel.join.cq.sihua.data.GetChannelResponse;
import com.ipanel.join.cq.sihua.data.OrderRequest;
import com.ipanel.join.cq.sihua.data.OrderResponse;
import com.ipanel.join.cq.sihua.data.SpaceRequest;
import com.ipanel.join.cq.sihua.data.GetChannelResponse.Body.Channel;
import com.ipanel.join.cq.sihua.data.GetChannelResponse.Body.Channel.Program;
import com.ipanel.join.cq.vod.order.OrderDialog;
import com.ipanel.join.cq.vod.player.SimplePlayerActivity;
import com.ipanel.join.cq.vod.utils.Tools;
import com.ipanel.join.protocol.a7.ServiceHelper;
import com.ipanel.join.protocol.a7.ServiceHelper.ResponseHandlerT;
import com.ipanel.join.protocol.a7.ServiceHelper.SerializerType;
import com.ipanel.join.protocol.sihua.cqvod.space.SpaceTask;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.Fragment;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.View.OnClickListener;
import android.view.View.OnFocusChangeListener;
import android.view.View.OnKeyListener;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemSelectedListener;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.TextView;


@SuppressLint("NewApi") 
public class LiveRecordFragment extends Fragment{
	
	TVCloudActivity mActivity;
	RecordDataBaseHelper mDBHelper;
	
	private ListView channels_listview;//回看频道
	private ListView events_listview;//回看节目
	private ListView time_zone;
	
	private int channels_index = 0;
	private int time_index = 0;
	private int program_index = 0;
	private static final int UPDATE_PROGRAM_DATA = 0;
	private static final int UPDATE_CHANNEL_LIST = 1;
	private static final int UPDATE_TIME_LIST = 3;
	private static final int GET_PROGRAMS = 4;
	private static final int EVENT_LIST_MOVE_FOCUS = 5;
	
	private static final int STATE_RECODR_ENABLE = 0;			//录制
	private static final int STATE_HAS_RECORDED = 1;			//已录制
	private static final int STATE_RECORD_ENABLE_FUTURE = 2;	//订录
	private static final int STATE_WILL_RECORD = 3;				//已订录
	
	private ChannelListAdapter channelAdapter;
	private EventListAdapter eventAdapter;
	
	private TextView current_page,total_page;
	
	Handler mHandler = new Handler() {
		
		public void handleMessage(Message msg) {
			int position = 0;
			switch(msg.what){
			case UPDATE_PROGRAM_DATA:
				events_listview.setVisibility(View.VISIBLE);
				if(eventAdapter == null){
					eventAdapter = new EventListAdapter(event_list);
				}else{
					eventAdapter.setEventList(event_list);
				}
				if(event_list != null)
					total_page.setText(event_list.length+"");
				events_listview.setAdapter(eventAdapter);
				break;
			case UPDATE_CHANNEL_LIST:
				if(channelAdapter == null){
					channelAdapter = new ChannelListAdapter(channel_list);
				}else{
					channelAdapter.setChannelList(channel_list);
				}
				channels_listview.setAdapter(channelAdapter);
				channels_listview.setSelection(0);
				if(channel_list.length > 0){
					showTimeTab();
					time_zone.setAdapter(new TimeListAdapter());
				}else{
					hideTimeTab();
					events_listview.setVisibility(View.INVISIBLE);
				}
				break;
			case GET_PROGRAMS:
				updateProgramData();
				break;
			case EVENT_LIST_MOVE_FOCUS:
				position = (Integer)msg.obj;
				if (events_listview.getChildAt(position) != null)
					events_listview.getChildAt(position).requestFocus();
				break;
			case 10:
//				channels_listview.getChildAt(0).requestFocus();
//				channels_listview.getChildAt(0).setSelected(true);
				if(channels_listview.getSelectedView() != null)
					channels_listview.getSelectedView().requestFocus();
				break;
			case UPDATE_TIME_LIST:
				time_zone.setAdapter(new TimeListAdapter());
				time_zone.setSelection(3);
				break;
			}
		};
	};
	
	protected Program[] event_list;
	protected void updateProgramData() {
		if(channel_list == null || channel_list.length == 0){
			return;
		}
		Channel channel = channel_list[channels_index];
		Calendar day = generateList().get(time_index);
		Calendar end = (Calendar) day.clone();
		end.add(Calendar.DAY_OF_YEAR, 1);
		SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd");
		RequestParams req = new RequestParams();
		req.put("command", "CHANNEL_SCHEDULE_QUERY"); //获取节目单
		req.put("uuid", CQApplication.getInstance().getUid());
		req.put("spId", "sp_00001");
		req.put("appID", "app_00001");
		req.put("accessToken", CQApplication.getInstance().getAuthToken());
		req.put("startTime", "" + format.format(new Date(day.getTimeInMillis())) + " 00:00:00");
		req.put("endTime", "" + format.format(new Date(day.getTimeInMillis())) + " 24:00:00");
		req.put("channelID",channel.channelCode);
		ServiceHelper helper = ServiceHelper.getHelper();
		helper.setSerializerType(SerializerType.JSON);
		helper.setRootUrl(TVCloudActivity.SiHuaUrl);
		helper.callServiceAsync(mActivity, req, GetChannelResponse.class, new ResponseHandlerT<GetChannelResponse>() {

			@Override
			public void onResponse(boolean success, GetChannelResponse result) {
				if(!success){
					Tools.showToastMessage(getActivity(), getResources().getString(R.string.tvclound_tip7));
					return;
				}
				if(result == null || result.body == null || result.body.result == null || result.body.result.code != 0){
					Tools.showToastMessage(getActivity(), "[" + result.body.result.code
							+ "]" + result.body.result.description);
					return;
				}
				event_list = result.body.channels[0].assetPrograms;
				if(event_list != null){
					events_listview.setOnFocusChangeListener(new OnFocusChangeListener(){
						@Override
						public void onFocusChange(View arg0, boolean hasFocus) {
							if(hasFocus){
								total_page.setText(event_list.length + "");
							}						
						}						
					});
					mHandler.sendEmptyMessage(UPDATE_PROGRAM_DATA);
				}else{
					Tools.showToastMessage(getActivity(), "暂无节目信息");
				}
			}
		});				
	}

	private void showTimeTab() {
		if(time_zone != null && time_zone.getVisibility()==View.INVISIBLE){
			time_zone.setVisibility(View.VISIBLE);
		}
	}
	
	private void hideTimeTab() {
		if (time_zone != null && time_zone.isShown()) {
			time_zone.setVisibility(View.INVISIBLE);
		}
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		if (mActivity == null)
			mActivity = (TVCloudActivity) getActivity();
		ViewGroup vg = (ViewGroup) inflater.inflate(R.layout.tvcloud_live_fragment, container, false);
		channels_listview = (ListView) vg.findViewById(R.id.channel_list);			
		events_listview = (ListView) vg.findViewById(R.id.event_list);
		time_zone = (ListView) vg.findViewById(R.id.time_zone);
		time_zone.setOnFocusChangeListener(new OnFocusChangeListener(){
			@Override
			public void onFocusChange(View arg0, boolean hasFocus) {
				if(hasFocus){
					total_page.setText("7");
				}				
			}			
		});
		channels_listview.setItemsCanFocus(true);
		channels_listview.setOnItemSelectedListener(new OnItemSelectedListener() {

			@Override
			public void onItemSelected(AdapterView<?> parent, View view,
					int position, long id) {
				channels_index = position;
				mHandler.removeMessages(UPDATE_TIME_LIST);
				mHandler.sendEmptyMessageDelayed(UPDATE_TIME_LIST, 200);
			}

			@Override
			public void onNothingSelected(AdapterView<?> parent) {
				
			}
		});
		
		time_zone.setItemsCanFocus(true);
		time_zone.setOnItemSelectedListener(new OnItemSelectedListener() {

			@Override
			public void onItemSelected(AdapterView<?> parent, View view,
					int position, long id) {
				time_index = position;
				mHandler.removeMessages(GET_PROGRAMS);
				mHandler.sendEmptyMessageDelayed(GET_PROGRAMS, 200);
			}

			@Override
			public void onNothingSelected(AdapterView<?> parent) {
				
			}
		});
		events_listview.setItemsCanFocus(true);
		events_listview.setOnItemSelectedListener(new OnItemSelectedListener() {

			@Override
			public void onItemSelected(AdapterView<?> parent, View view,
					int position, long id) {
				program_index = position;
			}

			@Override
			public void onNothingSelected(AdapterView<?> parent) {
				
			}
		});
		
		mDBHelper = RecordDataBaseHelper.getInstance();
		mActivity.setHandler(mHandler);
		current_page = mActivity.current_page;
		current_page.setText("1");
		total_page = mActivity.total_page;
		getChannelList();
		
		return vg;
	}
	Channel[] channel_list;
	//请求频道列表
	protected void getChannelList() {
		String uuid = CQApplication.getInstance().getUid();
		String accessToken = CQApplication.getInstance().getAuthToken();
		Log.i("LiveRecordFragment", "uuid = " + uuid + ";accessToken = " + accessToken );
		if (uuid == null || "".equals(uuid) 
				|| accessToken == null || "".equals(accessToken)) {
			Tools.showToastMessage(getActivity(), getResources().getString(R.string.tvclound_tip8));
			return;
		}
		RequestParams req = new RequestParams();
		req.put("command", "CHANNEL_LIVE_QUERY"); //取全部频道
		req.put("uuid", CQApplication.getInstance().getUid());
		req.put("spId", "sp_00001");
		req.put("appID", "app_00001");
		req.put("accessToken", CQApplication.getInstance().getAuthToken());
		ServiceHelper helper = ServiceHelper.getHelper();
		helper.setSerializerType(SerializerType.JSON);
		helper.setRootUrl(TVCloudActivity.SiHuaUrl);
		helper.callServiceAsync(mActivity, req, GetChannelResponse.class, new ResponseHandlerT<GetChannelResponse>() {

			@Override
			public void onResponse(boolean success, GetChannelResponse result) {
				if(!success){
					Tools.showToastMessage(getActivity(), getResources().getString(R.string.tvclound_tip7));
					return;
				}
				if(result == null || result.body == null || result.body.result == null || result.body.result.code != 0){
					Tools.showToastMessage(getActivity(), "[" + result.body.result.code
							+ "]" + result.body.result.description);
					return;
				}
				channel_list = result.body.channels;
				if(channel_list != null){
//					total_page.setText(channel_list.length+"");
					channels_listview.setOnFocusChangeListener(new OnFocusChangeListener(){

						@Override
						public void onFocusChange(View arg0, boolean hasFocus) {
							if(hasFocus){
								total_page.setText(channel_list.length+"");
							}
						}
						
					});
					mHandler.sendEmptyMessage(UPDATE_CHANNEL_LIST);
				}else{
					Tools.showToastMessage(getActivity(), "暂无频道列表");
				}
			}
		});								
	}
	
	
	class ChannelListAdapter extends BaseAdapter {
		private Channel[] channelList;
		
		public ChannelListAdapter(Channel[] channelList) {
			this.channelList = channelList;
		}

		
		public void setChannelList(Channel[] channelList) {
			this.channelList = channelList;
			notifyDataSetChanged();
		}

		@Override
		public int getCount() {
			return channelList.length;
		}

		@Override
		public Object getItem(int position) {
			return channelList[position];
		}

		@Override
		public long getItemId(int position) {
			return position;
		}

		@Override
		public View getView(final int position, View convertView, ViewGroup parent) {
			if (convertView == null)
				convertView = LayoutInflater.from(mActivity).inflate(R.layout.tvcloud_item_channel_list, null);
			Channel item = channel_list[position];
			TextView channel_name = (TextView) convertView.findViewById(R.id.channel_name);
			channel_name.setText(item.channelName);
			convertView.setTag(position);
			convertView.setOnKeyListener(new OnKeyListener() {
				@Override
				public boolean onKey(View v, int keyCode, KeyEvent event) {
					if(event.getAction() == KeyEvent.ACTION_DOWN){
						if(keyCode == KeyEvent.KEYCODE_DPAD_RIGHT){
							if(time_zone != null){
								time_zone.getChildAt(time_index).requestFocus();
								return true;
							}
						}
						else if(keyCode == KeyEvent.KEYCODE_DPAD_DOWN && position == (channelList.length-1)){
							return true;
						}else if(keyCode == KeyEvent.KEYCODE_DPAD_UP && position == 0){
							return true;
						}
					}
					return false;
				}
			});
			convertView.setOnFocusChangeListener(new OnFocusChangeListener(){

				@Override
				public void onFocusChange(View arg0, boolean hasFocus) {
					
				}
			});
		
			return convertView;
		}
	}
	
	class TimeListAdapter extends BaseAdapter{
		List<Calendar> list = new ArrayList<Calendar>();
		
		public TimeListAdapter() {
			list = generateList();
		}

		@Override
		public int getCount() {
			return list.size();
		}

		@Override
		public Object getItem(int position) {
			return list.get(position);
		}

		@Override
		public long getItemId(int position) {
			return position;
		}

		@Override
		public View getView(final int position, View convertView, ViewGroup parent) {
			if(convertView == null){
				convertView = LayoutInflater.from(mActivity).inflate(R.layout.tvcloud_item_time_list, null);
			}
			TextView date = (TextView)convertView.findViewById(R.id.time_date);
			TextView day = (TextView)convertView.findViewById(R.id.time_week);
			date.setText(DateUitl.getDate(list.get(position)));
			day.setText(DateUitl.getWeekDay(list.get(position)));
			convertView.setOnKeyListener(new OnKeyListener() {
				@Override
				public boolean onKey(View v, int keyCode, KeyEvent event) {
					if(event.getAction() == KeyEvent.ACTION_DOWN){
						if(keyCode == KeyEvent.KEYCODE_DPAD_LEFT){
							if(channels_listview != null && channels_listview.getChildAt(channels_index) != null){
								channels_listview.getChildAt(channels_index).requestFocus();
								return true;
							}
						}else if(keyCode == KeyEvent.KEYCODE_DPAD_DOWN && position == (list.size()-1)){
							return true;
						}else if(keyCode == KeyEvent.KEYCODE_DPAD_RIGHT){
							if(event_list != null && event_list.length > 0 && events_listview!= null && events_listview.getChildAt(program_index) != null){
								events_listview.getChildAt(program_index).requestFocus();
								return true;
							}
						}else if(keyCode == KeyEvent.KEYCODE_DPAD_UP && position == 0){
							return true;
						}
					}
					return false;
				}
			});
			convertView.setOnFocusChangeListener(new OnFocusChangeListener(){

				@Override
				public void onFocusChange(View arg0, boolean hasFocus) {
					
				}
			});
			return convertView;
		}
	}
	
	static List<Calendar> generateList() {
		List<Calendar> list = new ArrayList<Calendar>();
		for (int i = -3; i <= 6; i++) {
			Calendar c = Calendar.getInstance();
			c.set(Calendar.HOUR_OF_DAY, 0);
			c.set(Calendar.MINUTE, 0);
			c.set(Calendar.SECOND, 0);
			c.add(Calendar.DAY_OF_YEAR, -i);
			list.add(c);
		}
		return list;
	}
	
	class EventListAdapter extends BaseAdapter{
		private Program[] event_list;
		
		public EventListAdapter(Program[] event_list){
			this.event_list = event_list;
		}
		public void setEventList(Program[] event_list){
			this.event_list = event_list;
			notifyDataSetChanged();
		}
		
		@Override
		public int getCount() {
			return event_list.length;
		}

		@Override
		public Object getItem(int position) {
			return event_list[position];
		}

		@Override
		public long getItemId(int position) {
			return position;
		}

		@Override
		public View getView(final int position, View convertView, ViewGroup parent) {
			final Program event = event_list[position];
			final int record_state;
			if(convertView == null){
				convertView = LayoutInflater.from(mActivity).inflate(R.layout.tvcloud_item_event_list, null);
			}
			TextView eventTime = (TextView)convertView.findViewById(R.id.event_time);
			final TextView eventName = (TextView)convertView.findViewById(R.id.event_name);
			final TextView isRecord = (TextView) convertView.findViewById(R.id.isRecorded);
			final ImageView bg = (ImageView) convertView.findViewById(R.id.event_bg);
			try {
				eventTime.setText(event.startDateTime.substring(11, 16));
			} catch (Exception e) {
				e.printStackTrace();
			}
			eventName.setText(event.programName);
//			if (mDBHelper.getEventIds().contains(event.assetID + "")) { //当前event是否在录制列表中
//				if (TimeHelper.isPlayed(event.start_time, event.end_time, System.currentTimeMillis()/1000)) { //当前event是否已播出
//					record_state = STATE_HAS_RECORDED;
//					isRecord.setText("已录制");
//					bg.setVisibility(View.VISIBLE);
//				} else {
//					record_state = STATE_WILL_RECORD;
//					isRecord.setText("已订录");
//					bg.setVisibility(View.VISIBLE);
//				}
//			} else {
//				if (TimeHelper.isPlayed(event.start_time, event.end_time, System.currentTimeMillis()/1000)) {
//					record_state = STATE_RECODR_ENABLE;
//					isRecord.setText("录制");
//					bg.setVisibility(View.GONE);
//				} else {
//					record_state = STATE_RECORD_ENABLE_FUTURE;
//					isRecord.setText("订录");
//					bg.setVisibility(View.GONE);
//				}
//			}
			isRecord.setText("录制");
			if(event.status.equals("0")){
				try {
					if(TimeHelper.formatter_a.parse(event.endDateTime).before(new Date(System.currentTimeMillis()))){
						isRecord.setText("录制");
					}else{
						isRecord.setText("订录");
					}
				} catch (ParseException e) {
					e.printStackTrace();
				}
			}else if(event.status.equals("1")){
				isRecord.setText("已订录");
			}else if(event.status.equals("2")){
				isRecord.setText("录制中");
			}else if(event.status.equals("3")){//录制成功
				isRecord.setText("已录制");
			}else if(event.status.equals("4")){
				isRecord.setText("录制失败");
			}
			convertView.setOnClickListener(new OnClickListener() {				
				@Override
				public void onClick(View v) {
//					if (record_state == STATE_RECODR_ENABLE || record_state == STATE_RECORD_ENABLE_FUTURE) { //若为可录制状态，将该event的event_id加入数据库，刷新eventlist
//						LogHelper.i("LiveRecordFragment event name = " + event.programName + ", start time = " + event.startDateTime);
////						mDBHelper.insert(event.assetID+"", event.startDateTime, event.startDateTime, 
////								(int)(event.end_time-event.start_time) / 10, channel_list.get(channels_index).chnl_name); //根据时间生成文件大小
//						notifyDataSetChanged();
//						mHandler.obtainMessage(EVENT_LIST_MOVE_FOCUS, position).sendToTarget();
//					} else if (record_state == STATE_HAS_RECORDED) {
//						Intent intent = new Intent(getActivity(), SimplePlayerActivity.class);
//						intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
//						intent.putExtra("vodId", event.assetID+"");
//						intent.putExtra("playType", "4");
//						intent.putExtra("name", event.programName);
//						startActivity(intent);
//					}
					if(event.status.equals("0")){
//						bookOrder(event.channelID,isRecord,event.assetID,
//								TimeHelper.getDetailTime(System.currentTimeMillis()));
						bookOrder(event, isRecord, TimeHelper.getDetailTime(System.currentTimeMillis()));
					}
				}
			});
			convertView.setTag(position);
			convertView.setOnKeyListener(new OnKeyListener() {
				@Override
				public boolean onKey(View v, int keyCode, KeyEvent event) {
					if(event.getAction() == KeyEvent.ACTION_DOWN){
						if(keyCode == KeyEvent.KEYCODE_DPAD_LEFT){
							if(time_zone != null){
								time_zone.getChildAt(time_index).requestFocus();
								return true;
							}
						}else if(keyCode == KeyEvent.KEYCODE_DPAD_DOWN && position == (event_list.length-1)){
							return true;
						}else if(keyCode == KeyEvent.KEYCODE_DPAD_UP && position == 0){
							return true;
						}
					}
					return false;
				}
			});
			convertView.setOnFocusChangeListener(new OnFocusChangeListener(){

				@Override
				public void onFocusChange(View arg0, boolean hasFocus) {
					if(hasFocus){
						current_page.setText(position+1+"");
						eventName.setSelected(true);
					}else{
						eventName.setSelected(false);
					}
					
				}
				
			});
			return convertView;
		}
	}
	//订录节目
	public void bookOrder(final Program event,final TextView isRecord, String orderTime){
		ServiceHelper service = ServiceHelper.getHelper();
		service.setRootUrl(TVCloudActivity.SiHuaUrl);
		service.setSerializerType(SerializerType.XML);
		service.callServiceAsync(getActivity(), SpaceRequest.setOrderRequest(event.channelID, event.assetID, orderTime,"0"), OrderResponse.class,new ResponseHandlerT<OrderResponse>(){

			@Override
			public void onResponse(boolean success, OrderResponse result) {
				if(!success){
					Tools.showToastMessage(getActivity(), getResources().getString(R.string.tvclound_tip7));
					return;
				}
				if(result != null && result.getBody().getResult().getCode().equals("0")){
					if(isRecord.getText().toString().equals("录制")){
						isRecord.setText("已录制");
						event.status = "3";
						Tools.showToastMessage(getActivity(), "录制成功");
					}else if(isRecord.getText().toString().equals("订录")){
						isRecord.setText("已订录");
						event.status = "1";
						Tools.showToastMessage(getActivity(), "订录成功");
					}
				}else{
					if(result.getBody().getResult().getCode().equals("-8")){
//						Tools.showToastMessage(mActivity, mActivity.getResources().getString(R.string.tvclound_tip8));
						new OrderDialog(mActivity, mActivity.getResources().getString(R.string.tvclound_tip8)).show();
					}else if(result.getBody().getResult().getCode().equals("-6")){
						Tools.showToastMessage(mActivity, mActivity.getResources().getString(R.string.tvclound_tip1));
					}else{
						Tools.showToastMessage(getActivity(),/*"[" + result.getBody().getResult().getCode() + "]" 
								+ */result.getBody().getResult().getDescription());
					}
				}
			}
		});
	}
	
	//订录节目
	public void bookOrder(String channelID,final TextView isRecord, String programID,String orderTime){
		ServiceHelper service = ServiceHelper.getHelper();
		service.setRootUrl(TVCloudActivity.SiHuaUrl);
		service.setSerializerType(SerializerType.XML);
		service.callServiceAsync(getActivity(), SpaceRequest.setOrderRequest(channelID, programID, orderTime,"0"), OrderResponse.class,new ResponseHandlerT<OrderResponse>(){

			@Override
			public void onResponse(boolean success, OrderResponse result) {
				if(!success){
					Tools.showToastMessage(getActivity(), getResources().getString(R.string.tvclound_tip7));
					return;
				}
				if(result != null && result.getBody().getResult().getCode().equals("0")){
					if(isRecord.getText().toString().equals("录制")){
						isRecord.setText("已录制");
					}else if(isRecord.getText().toString().equals("订录")){
						isRecord.setText("已订录");
					}
				}else{
					if(result.getBody().getResult().getCode().equals("-8")){
						Tools.showToastMessage(mActivity, mActivity.getResources().getString(R.string.tvclound_tip8));
					}else if(result.getBody().getResult().getCode().equals("-6")){
						Tools.showToastMessage(mActivity, mActivity.getResources().getString(R.string.tvclound_tip1));
					}else{
						Tools.showToastMessage(getActivity(),/*"[" + result.getBody().getResult().getCode() + "]" 
								+ */result.getBody().getResult().getDescription());
					}
				}
			}
		});
	}
	
}
