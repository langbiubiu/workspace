package com.ipanel.join.cq.homed.tvcloud;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.apache.http.Header;
import org.apache.http.message.BasicHeader;

import android.annotation.SuppressLint;
import android.app.Fragment;
import android.content.Context;
import android.content.Intent;
import android.database.DataSetObserver;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnFocusChangeListener;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import cn.ipanel.android.LogHelper;

import com.ipanel.chongqing_ipanelforhw.CQApplication;
import com.ipanel.chongqing_ipanelforhw.R;
import com.ipanel.join.cq.homed.tvcloud.db.RecordDataBaseHelper;
import com.ipanel.join.cq.homed.tvcloud.db.RecordEvent;
import com.ipanel.join.cq.sihua.data.OrderResponse;
import com.ipanel.join.cq.sihua.data.SpaceRequest;
import com.ipanel.join.cq.vod.player.SimplePlayerActivity;
import com.ipanel.join.cq.vod.utils.Tools;
import com.ipanel.join.protocol.a7.ServiceHelper;
import com.ipanel.join.protocol.a7.ServiceHelper.ResponseHandlerT;
import com.ipanel.join.protocol.a7.ServiceHelper.SerializerType;
import com.ipanel.join.protocol.sihua.cqvod.space.Content;
import com.ipanel.join.protocol.sihua.cqvod.space.ContentDeleteResponse;
import com.ipanel.join.protocol.sihua.cqvod.space.SpaceContent;
import com.ipanel.join.protocol.sihua.cqvod.space.SpacePlayUrl;
import com.ipanel.join.protocol.sihua.cqvod.space.SpaceTask;
import com.ipanel.join.protocol.sihua.cqvod.space.TaskInfo;

@SuppressLint("NewApi") 
public class RecordedFragment extends Fragment {
	
	TVCloudActivity mActivity;
//	RecordDataBaseHelper mDBHelper;
	int totalNum;
	
	ListView record_list;
	RecordAdapter adapter;
	List<TaskInfo> taskInfolist = new ArrayList<TaskInfo>();//查询正在录制中的节目
	
	private TextView current_page,total_page;
	
	SimpleDateFormat sdf = new SimpleDateFormat("yyyy年MM月dd日");
	
	private final static int LIST_MOVE_FOCUS = 0;
	private final static int GET_SPACE_CONTENT = 1;
	
	Handler mHandler = new Handler() {
		public void handleMessage(Message msg) {
			int position = 0;
			switch (msg.what) {
			case LIST_MOVE_FOCUS:
				position = (Integer) msg.obj;
				if (record_list.getChildAt(position) != null){
					record_list.getChildAt(position).findViewById(R.id.record_delete).requestFocus();
				}
				if(isAdded()){
					getActivity().findViewById(R.id.logo).setFocusable(false);
				}
				break;
			case GET_SPACE_CONTENT:
				getSpaceContent();
				break;
			}
		};
	};
	
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		if (mActivity == null)
			mActivity = (TVCloudActivity) getActivity();
		ViewGroup vg = (ViewGroup) inflater.inflate(R.layout.tvcloud_record_fragment, container, false);
//		mDBHelper = RecordDataBaseHelper.getInstance();
		current_page = mActivity.current_page;
		total_page = mActivity.total_page;
		current_page.setText("1");
		record_list = (ListView) vg.findViewById(R.id.record_list);
		record_list.setItemsCanFocus(true);
//		record_list.setAdapter(new RecordAdapter());
		record_list.setOnFocusChangeListener(new OnFocusChangeListener(){
			@Override
			public void onFocusChange(View arg0, boolean hasFocus) {
				if(hasFocus){
					record_list.setSelection(0);	
				}
			}
			
		});
		getMyRecord();
		return vg;
	}
	//获得我的录制
	private void getMyRecord() {
		ServiceHelper service = ServiceHelper.getHelper();
		service.setRootUrl(TVCloudActivity.SiHuaUrl);
		service.setSerializerType(SerializerType.XML);//获得任务状态
		service.callServiceAsync(getActivity(), SpaceRequest.setTaskRequest("1"), SpaceTask.class,new ResponseHandlerT<SpaceTask>(){

			@Override
			public void onResponse(boolean success, SpaceTask result) {
				if(!success)
					return;
				if(result != null && result.getBody() != null &&
						result.getBody().getTasks() != null && 
						result.getBody().getTasks().getTaskInfoList() != null &&
						result.getBody().getTasks().getTaskInfoList().size() > 0){
					taskInfolist = result.getBody().getTasks().getTaskInfoList();
				}else if(result != null && result.getBody() != null &&
						result.getBody().getTasks() != null){
//					Tools.showToastMessage(getActivity(), "[" + result.getBody().getTasks().getResultCode()
//							+ "]" + result.getBody().getTasks().getDescription());
				}
				mHandler.sendEmptyMessage(GET_SPACE_CONTENT);
			}
		});
	}
	//获得内容
	private void getSpaceContent() {
		ServiceHelper service = ServiceHelper.getHelper();
		service.setRootUrl(TVCloudActivity.SiHuaUrl);
		service.setSerializerType(SerializerType.XML);
		service.callServiceAsync(getActivity(), SpaceRequest.setContentRequest("1", "1|3|4", "1", "1"),SpaceContent.class,new ResponseHandlerT<SpaceContent>(){

			@Override
			public void onResponse(boolean success, SpaceContent result) {
				if(!success){
					Tools.showToastMessage(getActivity(), getResources().getString(R.string.tvclound_tip7));
					return;
				}
				if(result != null && result.getBody() != null){
					if(adapter == null){
						adapter = new RecordAdapter(result.getBody().getContents().getContentList());
					}else{
						adapter.setTaskList(result.getBody().getContents().getContentList());
						adapter.notifyDataSetChanged();
					}
					record_list.setAdapter(adapter);
				}else{
					Tools.showToastMessage(getActivity(), getResources().getString(R.string.tvclound_tip8));
				}
			}
		});
	}

	class RecordAdapter extends BaseAdapter {
		
		public List<Content> contentList = new ArrayList<Content>();

		public RecordAdapter(List<Content> list) {
			if(list == null)
				list = new ArrayList<Content>();
			for(int i = list.size()-1;i>=0;i--){
				if(list.get(i).getStatus().equals("6")){//内容彻底删除
					list.remove(i);
				}
			}
			this.contentList = list;
			if(contentList.size() == 0 && taskInfolist.size() == 0){
				Tools.showToastMessage(getActivity(), "暂无录制节目");
			}
		}

		public void setTaskList(List<Content> list) {
			if(list == null)
				list = new ArrayList<Content>();
			for(int i = list.size()-1;i>=0;i--){
				if(list.get(i).getStatus().equals("6")){
					list.remove(i);
				}
			}
			this.contentList = list;
			if(contentList.size() == 0 && taskInfolist.size() == 0){
				Tools.showToastMessage(getActivity(), "暂无录制节目");
			}
		}

		@Override
		public int getCount() {
//			return mDBHelper.getEvents() == null ? 0 : mDBHelper.getEvents().size();
			return contentList.size() + taskInfolist.size();
		}

		@Override
		public Object getItem(int position) {
//			return mDBHelper.getEvents() == null ? null : mDBHelper.getEvents().get(position);
			if(position < taskInfolist.size()){
				return taskInfolist.get(position);
			}else
				return contentList.get(position);
		}

		@Override
		public long getItemId(int position) {
			return position;
		}

		@Override
		public View getView(final int position, View convertView, ViewGroup parent) {
			final Holder holder;
			if(convertView == null){
				convertView = LayoutInflater.from(mActivity).
						inflate(R.layout.tvcloud_item_record_list, null, false);
				holder = new Holder();
				holder.rl = (RelativeLayout) convertView.findViewById(R.id.record_event);
				holder.record_name = (TextView) convertView.findViewById(R.id.record_name);
				holder.record_mark = (TextView) convertView.findViewById(R.id.record_mark);
				holder.record_time = (TextView) convertView.findViewById(R.id.record_time);
				holder.record_size = (TextView) convertView.findViewById(R.id.record_size);
				holder.record_delete = (TextView) convertView.findViewById(R.id.record_delete);
				convertView.setTag(holder);
			}else{
				holder = (Holder) convertView.getTag();
			}
			totalNum = getCount();
			total_page.setText(totalNum+""); 
			if(position < taskInfolist.size()){//任务查询接口返回
				final TaskInfo event = taskInfolist.get(position);
				holder.record_name.setText(event.getContentName());//任务查询接口返回
				if(event.getStatus().equals("1")){//录制中
					holder.record_mark.setText("录制中");
				}else if(event.getStatus().equals("2")){//成功
					holder.record_mark.setText("成功");
				}else if(event.getStatus().equals("3")){//失败
					holder.record_mark.setText("失败");
				}else if(event.getStatus().equals("4")){//任务清除
					holder.record_mark.setText("任务清除");
				}else if(event.getStatus().equals("5")){//内容删除
					holder.record_mark.setText("内容删除");
				}else if(event.getStatus().equals("6")){//内容彻底删除
					holder.record_mark.setText("内容彻底删除");
				}
				if(event.getCreateTime() != null && !event.getCreateTime().equals("")){
					holder.record_time.setText(event.getCreateTime().substring(0,10));//任务查询返回
				}
				holder.record_size.setText(event.getSize() + "M");
				holder.record_delete.setOnClickListener(new OnClickListener() {
					
					@Override
					public void onClick(View arg0) {
//						mDBHelper.delete(event);
//						deleteRecord(position,event.getContentID());
						ServiceHelper service = ServiceHelper.getHelper();
						service.setRootUrl(TVCloudActivity.SiHuaUrl);
						service.setSerializerType(SerializerType.XML);
						service.callServiceAsync(getActivity(), SpaceRequest.setOrderRequest(event.getChannelID(), event.getProgramID(), event.getBeginTime(),"1"), OrderResponse.class,new ResponseHandlerT<OrderResponse>(){

							@Override
							public void onResponse(boolean success, OrderResponse result) {
								if(!success)
									return;
								if(result != null && result.getBody().getResult().getCode().equals("0")){
									hideFocus();
									taskInfolist.remove(position);
									adapter.notifyDataSetChanged();
									//删除当前行后，焦点移至上一行
									int later_position = position == taskInfolist.size() ? position-1:position;
									if(taskInfolist.size() == 0)
										later_position = 0;
									mHandler.obtainMessage(LIST_MOVE_FOCUS, later_position).sendToTarget();
									Tools.showToastMessage(getActivity(), result.getBody().getResult().getDescription());
								}else{
									Tools.showToastMessage(getActivity(), result.getBody().getResult().getDescription());
								}
							}
						});
					}
				});
				holder.rl.setOnClickListener(new OnClickListener() {
					
					@Override
					public void onClick(View arg0) {
						/*
						 * start to back play
						 */
						getPlayUrl(event.getContentID(),event.getContentName());
					}
				});
			}else{//内容查询接口返回
				final Content event = contentList.get(position - taskInfolist.size());
				holder.record_name.setText(event.getContentName());
				if(event.getStatus().equals("1")){//可用
					holder.record_mark.setText("成功");
				}
				if(event.getCreateTime() != null && !event.getCreateTime().equals("")){
					holder.record_time.setText(event.getCreateTime().substring(0,10));
				}
				holder.record_size.setText(event.getSize() + "M");
				holder.record_delete.setOnClickListener(new OnClickListener() {
					
					@Override
					public void onClick(View arg0) {
//						mDBHelper.delete(event);
						deleteRecord(position - taskInfolist.size(),event.getContentID());
					}
				});
				holder.rl.setOnClickListener(new OnClickListener() {
					
					@Override
					public void onClick(View arg0) {
						/*
						 * start to back play
						 */
						getPlayUrl(event.getContentID(),event.getContentName());
					}
				});
			}
//			final RecordEvent event = mDBHelper.getEvents().get(position);
//			LogHelper.i("RecordFragment event name = " + event.getEvent_name() + ", start time = " + event.getStart_time());
//			totalNum = mDBHelper.getEvents().size();
//			record_name.setText(event.getEvent_name());
//			if (TimeHelper.isPlayed(event.getStart_time(), 0, System.currentTimeMillis()/1000)) {
//				record_mark.setText("已录制");
//			} else {
//				record_mark.setText("已订录");
//			}
			
			holder.rl.setOnFocusChangeListener(new OnFocusChangeListener(){

				@Override
				public void onFocusChange(View arg0, boolean hasFocus) {
					current_page.setText(position+1+"");
					if(hasFocus){
						holder.record_name.setSelected(true);
					}else{
						holder.record_name.setSelected(false);
					}
				}
				
			});
			holder.record_delete.setOnFocusChangeListener(new OnFocusChangeListener(){

				@Override
				public void onFocusChange(View arg0, boolean hasFocus) {
					current_page.setText(position+1+"");					
				}
			});
			
			return convertView;
		}
	}
	//获得播放地址
	private void getPlayUrl(String contentID,final String programName) {
		ServiceHelper service = ServiceHelper.getHelper();
		service.setRootUrl(TVCloudActivity.SiHuaUrl);
		service.setSerializerType(SerializerType.XML);
		service.callServiceAsync(getActivity(), SpaceRequest.setPlayUrlRequest(contentID), SpacePlayUrl.class,new ResponseHandlerT<SpacePlayUrl>(){

			@Override
			public void onResponse(boolean success, SpacePlayUrl result) {
				if(!success)
					return;
				if(result != null && result.getBody() != null){
					if(result.getBody().getResult().getCode().equals("0")){
//						ConnectivityManager cm = (ConnectivityManager) getActivity().getSystemService(Context.CONNECTIVITY_SERVICE);
//						NetworkInfo info = cm.getActiveNetworkInfo();
						String rtspUrl = result.getBody().getResult().getPlayUrlList().getContentList().get(0).getUrl().getValue();
						String playUrl = rtspUrl;
						playUrl = rtspUrl + "&ServerType=192&HW_Version=1.0&HW_lang=1&HW_decodemode=H.264HD;MPEG-2HD;H.265HD&HW_CA=1&"
												 + "HW_User="   + CQApplication.getInstance().subscriber_id
												 + "&HW_Ip="    + CQApplication.getInstance().getIP()
												 + "&HW_NTID="  + CQApplication.getInstance().getMAC()
												 + "&HW_STBID=" + CQApplication.getInstance().hsSTBID
												 + "&HW_CARDID="+ CQApplication.getInstance().getCardID()
												 + "&HW_ServiceGroupID=" + CQApplication.getInstance().getServicegroup()
//												 + "&HW_supportnet=" + CQApplication.getInstance().netState.type
												 + "&HW_supportnet=" + "Cable"
												 + "&HW_getipqamurl=" + CQApplication.getInstance().getEpgUrl() + "/Preplay4VMS.jsp";
//												 + "HW_User="   + CQApplication.getInstance().subscriber_id
//												 + "&HW_Ip="    + "10.239.166.38"
//												 + "&HW_NTID="  + "68-B6-FC-7B-98-B7"
//												 + "&HW_STBID=" + "06258871235198260"
//												 + "&HW_CARDID="+ CQApplication.getInstance().getCardID()
//												 + "&HW_ServiceGroupID=" + CQApplication.getInstance().getServicegroup()
//												 + "&HW_supportnet=" + "Cable"
//												 + "&HW_getipqamurl=" + CQApplication.getInstance().getEpgUrl() + "/Preplay4VMS.jsp";
//						playUrl = "rtsp://192.168.203.10:8100/00003000000121000000000001419040.ts?token=123456&Contentid=00003000000121000000000001419040&isHD=1&isIpqam=1&mode=2&servicetype=2&spId=11000&playSequenceNo=6CAE8B2C3FA014563005306977270&playSource=100000&ServerType=192&HW_Version=1.0&HW_lang=1&HW_decodemode=H.264HD;MPEG-2HD;H.265HD&HW_CA=1&HW_User=12413618&HW_Ip=10.239.183.36&HW_NTID=68-B6-FC-7B-98-B7&HW_STBID=06258871235198260&HW_CARDID=9950000000946804&HW_ServiceGroupID=36875&HW_supportnet=Cable&HW_getipqamurl=http://192.168.14.114:8082/EPG/jsp/Preplay4VMS.jsp";
						Intent intent = new Intent(getActivity(), SimplePlayerActivity.class);
						intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
						intent.putExtra("playType", "tvCloud");
						intent.putExtra("name", programName);
						intent.putExtra("params", playUrl);
						intent.putExtra("tag", 1);//从电视云录制进入播放，是思华的接口
						startActivity(intent);
					}else{
						Tools.showToastMessage(getActivity(),"[" + result.getBody().getResult().getCode() + "]" 
								+ result.getBody().getResult().getDescription());
					}
				}
			}
		});
	}
	//删除我的录制
	private void deleteRecord(final int position, String contentID){
		ServiceHelper service = ServiceHelper.getHelper();
		service.setRootUrl(TVCloudActivity.SiHuaUrl);
		service.setSerializerType(SerializerType.XML);
		service.callServiceAsync(getActivity(), SpaceRequest.setContentDeleteRequest(contentID), ContentDeleteResponse.class,new ResponseHandlerT<ContentDeleteResponse>(){

			@Override
			public void onResponse(boolean success, ContentDeleteResponse result) {
				if(!success)
					return;
				if(result != null && result.getBody().getResult().getCode().equals("0")){
					hideFocus();
					adapter.contentList.remove(position);
					adapter.notifyDataSetChanged();
					//删除当前行后，焦点移至上一行
					int later_position = position == adapter.contentList.size() ? taskInfolist.size() + adapter.contentList.size() - 1 : position + taskInfolist.size();
					mHandler.obtainMessage(LIST_MOVE_FOCUS, later_position).sendToTarget();
					Tools.showToastMessage(getActivity(), result.getBody().getResult().getDescription());
				}else{
					Tools.showToastMessage(getActivity(),"[" + result.getBody().getResult().getCode() + "]" 
							+ result.getBody().getResult().getDescription());
				}
			}
		});
	}
	
	private void hideFocus(){//删除时隐藏焦点，防止焦点乱跳
		if(isAdded()){
			getActivity().findViewById(R.id.logo).setFocusable(true);
			getActivity().findViewById(R.id.logo).requestFocus();
		}
	}
	
	public boolean onKeyDown(int keyCode, KeyEvent event){
		switch(keyCode){
		case KeyEvent.KEYCODE_DPAD_RIGHT:
			record_list.requestFocus();
			return true;
		}
		return false;		
	}
	
	class Holder{
		RelativeLayout rl;
		TextView record_name;
		TextView record_mark;
		TextView record_time;
		TextView record_size;
		TextView record_delete;
	}
}
