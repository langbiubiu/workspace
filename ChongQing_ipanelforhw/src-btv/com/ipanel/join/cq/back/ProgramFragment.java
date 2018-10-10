package com.ipanel.join.cq.back;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

import org.apache.http.Header;
import org.apache.http.message.BasicHeader;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemSelectedListener;
import android.widget.BaseAdapter;
import android.widget.ListView;
import android.widget.TextView;
import cn.ipanel.android.LogHelper;
import cn.ipanel.android.net.http.RequestParams;

import com.ipanel.chongqing_ipanelforhw.R;
import com.ipanel.chongqing_ipanelforhw.hwstruct.GetHwRequest;
import com.ipanel.chongqing_ipanelforhw.hwstruct.GetHwResponse;
import com.ipanel.chongqing_ipanelforhw.hwstruct.GetHwResponse.Program;
import com.ipanel.join.chongqing.live.util.TimeHelper;
import com.ipanel.join.cq.data.ReplayProgram;
import com.ipanel.join.cq.vod.HWDataManager;
import com.ipanel.join.cq.vod.jsondata.GlobalFilmData;
import com.ipanel.join.cq.vod.player.SimplePlayerActivity;
import com.ipanel.join.cq.vod.utils.Logger;
import com.ipanel.join.cq.vod.utils.Tools;
import com.ipanel.join.protocol.a7.ServiceHelper;
import com.ipanel.join.protocol.a7.ServiceHelper.ResponseHandlerT;
import com.ipanel.join.protocol.a7.ServiceHelper.SerializerType;
//节目列表
public class ProgramFragment extends BaseFragment{
	
	public static final String TAG = ProgramFragment.class.getSimpleName();
	public static final int GET_PROGRAMS = 0x01;
	public static final int GOTOPLAY = 0x02;//播放
	private ListView weekListView;//日期列表
	private ListView programView;//节目列表
	private TextView current_column;//当前行数
	private TextView total_columns;//总行数
	private String channelId;//频道号
	private int timeIndex = 0;
	private ServiceHelper serviceHelper;
	private List<Program> programList;
	private String EPG_COOKIE;//登录的cookie
	private String playType = "4";
	private String replayName;
	List<ReplayProgram> programs = new ArrayList<ReplayProgram>();
	
	Handler mHandler = new Handler(){
		@Override
		public void handleMessage(Message msg){
			switch (msg.what) {
			case GET_PROGRAMS:
				String date = DateUitl.getYear2(generateList().get(timeIndex));
				Logger.d("date-->"+date);
				//getDayProgramsByChannel(date,channelId);
				requestReplayProgram(date, channelId);
				break;
			case GOTOPLAY:
				String url = (String) msg.obj;
				Log.d(TAG, "url-->"+url);
				startToBackPlay(url);
			default:
				break;
			}
		}

	};
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		View view = inflater.inflate(R.layout.back_program_fragment, container, false);
		initViews(view);
		initControl();
		serviceHelper = ServiceHelper.getHelper();
		weekListView.setAdapter(new TimeListAdapter());
		weekListView.requestFocus();
		return view;
	}
	
	public ProgramFragment(String channelId){
		this.channelId = channelId;
	}
	
	//获取某天的节目for 欢网
	private void getDayProgramsByChannel(String date,String channelId) {
		Logger.d(TAG, "date:"+date +",channelId:"+channelId);
		GetHwRequest request = HWDataManager.getHwRequest();
		request.setAction(HWDataManager.ACTION_GET_DAY_PROGRAMS_BY_CHANNEL);
		request.getDeveloper().setApikey(HWDataManager.APIKEY);
		request.getDeveloper().setSecretkey(HWDataManager.SECRETKEY);
		request.getParam().setPage(1);
		request.getParam().setPagesize(30);
		request.getParam().setChannelId(channelId);
		request.getParam().setDate(date);
		
		//返回
		serviceHelper.setRootUrl(HWDataManager.ROOT_URL);
		serviceHelper.setSerializerType(SerializerType.JSON);
		serviceHelper.callServiceAsync(getActivity(), request, GetHwResponse.class, new ResponseHandlerT<GetHwResponse>(){

			@Override
			public void onResponse(boolean success, GetHwResponse result) {
				if (!success) {
					Log.i(TAG, "request detail data failed");
					return;
				}
				if (result == null) {
					Log.i(TAG, "failed to parse JSON data");
					return;
				}
				programList = result.getPrograms();
				if(programList != null && programList.size() > 0){
					//programView.setAdapter(new ProgramListAdapter(programList));
				}
			}
		});
	}

	/**
	 * 请求回看节目列表
	 * */
	public void requestReplayProgram(final String time,String channelId) {
		LogHelper.i(String.format("request channel %s,and time %s", channelId,time));
		String url = GlobalFilmData.getInstance().getEpgUrl() + "/defaultHD/en/datajspHD/android_tvod_getProgramInfo.jsp?timestemp="
				+ time + "&curChanId=" + channelId;
		Log.d(TAG, "url=" + url);
		EPG_COOKIE = GlobalFilmData.getInstance().getCookieString();
		serviceHelper.setRootUrl(url);
		serviceHelper.setSerializerType(SerializerType.TEXT);
		serviceHelper.setHeaders(new Header[] { new BasicHeader("Cookie", EPG_COOKIE) });
		RequestParams requestParams = new RequestParams();
		requestParams.put("timestemp", time);
		requestParams.put("curChanId", channelId);
		serviceHelper.callServiceAsync(getActivity(), requestParams,
				String.class, new ResponseHandlerT<String>() {
					@Override
					public void onResponse(boolean success, String result) {
						if (success && result != null && result.trim().length() > 0) {
							try {
								JSONArray ja = new JSONArray(result);
								int N = ja.length();
								programs.clear();
								for (int i = 0; i < N; i++) {
									JSONObject jo = ja.getJSONObject(i);
									ReplayProgram entry = new ReplayProgram();
									entry.setName(jo.optString("tvodName"));
									entry.setUrl(jo.optString("jumpUrl"));
									entry.setTime(jo.optString("timeStr"));
									entry.setDate(time);
									programs.add(entry);
								}
							} catch (JSONException e) {
								e.printStackTrace();
							}
							if(programs!=null && programs.size()>0){
								programView.setAdapter(new ProgramListAdapter(programs));
							}else{
								Tools.showToastMessage(getActivity(), "获取数据失败");
							}
						}
						LogHelper.i(String.format("success put %d program into time : %s map",programs.size(), time));
					}
				});
	}
	
	private void initControl() {
		weekListView.setOnItemSelectedListener(new OnItemSelectedListener() {

			@Override
			public void onItemSelected(AdapterView<?> parent, View view,
					int position, long id) {
				timeIndex = position;
				mHandler.removeMessages(GET_PROGRAMS);
				mHandler.sendEmptyMessageDelayed(GET_PROGRAMS, 100);
			}

			@Override
			public void onNothingSelected(AdapterView<?> parent) {
				
			}
		});
		
		programView.setOnItemSelectedListener(new OnItemSelectedListener() {

			@Override
			public void onItemSelected(AdapterView<?> arg0, View arg1,
					int position, long arg3) {
				refreshColumn(position+1);
			}

			@Override
			public void onNothingSelected(AdapterView<?> arg0) {
				
			}
		});
	}
	
	private void refreshColumn(int index) {
		current_column.setText(""+index);
		//total_columns.setText("/"+programList.size()+"行");
		total_columns.setText("/"+programs.size()+"行");
	}
	
	public void initData() {
		List<String> programList = new ArrayList<String>();
		for (int i = 0; i < 30; i++) {
			programList.add((i+1)+"");
		}
	}

	private void initViews(View view) {
		weekListView = (ListView)view.findViewById(R.id.time_zone);
		programView = (ListView)view.findViewById(R.id.program_list);
		weekListView.setItemsCanFocus(true);
		programView.setItemsCanFocus(true);
		current_column = (TextView)view.findViewById(R.id.program_column);
		total_columns = (TextView)view.findViewById(R.id.program_total_column);
	}

	@Override
	public void onResume() {
		super.onResume();
	}
	
	static List<Calendar> generateList() {
		List<Calendar> list = new ArrayList<Calendar>();
		for (int i = 0; i <= 6; i++) {
			Calendar c = Calendar.getInstance();
			c.set(Calendar.HOUR_OF_DAY, 0);
			c.set(Calendar.MINUTE, 0);
			c.set(Calendar.SECOND, 0);
			c.add(Calendar.DAY_OF_YEAR, -i);
			list.add(c);
		}
		return list;
	}
	//日期
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
				convertView = LayoutInflater.from(getActivity()).inflate(R.layout.btv_time_item, null);
			}
			TextView date = (TextView)convertView.findViewById(R.id.time_date);//日期
			TextView day = (TextView)convertView.findViewById(R.id.time_day);//星期
			if(position <= 2){
				day.setText(getActivity().getResources().getStringArray(R.array.day_of_week)[position]);
			}else{
				day.setText(DateUitl.getWeekDay(list.get(position)));
			}
			date.setText(DateUitl.getDate(list.get(position)));
			return convertView;
		}
	}
	//节目列表
	class ProgramListAdapter extends BaseAdapter{
//		private List<Program> program_list;
//		
//		public ProgramListAdapter(List<Program> program_list){
//			this.program_list = program_list;
//		}
//		
//		public void setEventList(List<Program> program_list){
//			this.program_list = program_list;
//			notifyDataSetChanged();
//		}
		private List<ReplayProgram> program_list;
		
		public ProgramListAdapter(List<ReplayProgram> program_list){
			this.program_list = program_list;
		}
		
		public void setEventList(List<ReplayProgram> program_list){
			this.program_list = program_list;
			notifyDataSetChanged();
		}
		
		@Override
		public int getCount() {
			return program_list.size();
		}

		@Override
		public Object getItem(int position) {
			return program_list.get(position);
		}

		@Override
		public long getItemId(int position) {
			return position;
		}

		@Override
		public View getView(final int position, View convertView, ViewGroup parent) {
			final ReplayProgram program = program_list.get(position);
			if(convertView == null){
				convertView = LayoutInflater.from(getActivity()).inflate(R.layout.btv_program_item, null);
			}
			TextView eventTime = (TextView)convertView.findViewById(R.id.program_time);
			TextView eventName = (TextView)convertView.findViewById(R.id.program_name);
			//String start_time = program.getStart_time();
			//eventTime.setText(start_time.substring(start_time.indexOf(" ") + 1, start_time.length()));
			String start_time = program.getTime();
			eventTime.setText(start_time);
			eventName.setText(program.getName());
			convertView.setOnClickListener(new OnClickListener() {
				
				@Override
				public void onClick(View v) {
					//TODO 点击获取播放地址
					replayName = program.getName();
					requestDetailURL(program.getUrl());
				}
			});
			convertView.setTag(position);
			return convertView;
		}
	}
	
	/**
	 * 请求具体的播放地址
	 * */
	private void requestDetailURL(String url) {
		
		LogHelper.i("requestDetailURL");
		url = GlobalFilmData.getInstance().getEpgUrl() + "/defaultHD/en/" + url;
		serviceHelper.setRootUrl(url);
		serviceHelper.setSerializerType(SerializerType.TEXT);
		serviceHelper.setHeaders(new Header[] { new BasicHeader("Cookie", EPG_COOKIE) });
		serviceHelper.callServiceAsync(getActivity(),
				new RequestParams(), String.class,
				new ResponseHandlerT<String>() {
					@Override
					public void onResponse(boolean success, String result) {
						if (success && result != null
								&& result.trim().length() > 0) {
							noticeURL(result);
						}
					}
				});
	}
	
	/**
	 * 加工获得真正的播放地址
	 * */
	private void noticeURL(String raw) {
		LogHelper.i("get play url is url:" + raw);
		String url = "";
		if (raw.length() > 0 && raw.contains("playFlag")) {
			try {
				JSONObject rtsp = new JSONObject(raw);
				String mFlag = rtsp.getString("playFlag");
				LogHelper.v("playFlag =" + mFlag);
				if (Integer.parseInt(mFlag) == 1) {
					url = rtsp.getString("playUrl");
					LogHelper.v("playUrl =" + url);
					String[] mid = url.split("rtsp");
					String URL = mid[1];
					String MISS = "rrsip=192.168.14.60&";
					URL = URL.replace(MISS, "");
					url = "rtsp" + URL;
					LogHelper.i("get play url 2 is url:" + url);
					String playurl = url;// 获得url
					playurl+="&baseFlag=0";
//					boolean url_ready = true;
					LogHelper.i("get play url 3 is url:" + playurl);
					Message msg = new Message();
					msg.what = GOTOPLAY;
					msg.obj = playurl;
					mHandler.sendMessage(msg);
				} else if (Integer.parseInt(mFlag) == 0) {
					Tools.showToastMessage(getActivity(), rtsp.getString("message"));
				} else {
					
				}
			} catch (JSONException e) {
				e.printStackTrace();
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
	}
	/**
	 * 跳转到播放
	 * @param url
	 */
	private void startToBackPlay(String url) {
		Intent intent = new Intent(getActivity(), SimplePlayerActivity.class);
		intent.putExtra("params", url);
		intent.putExtra("name", replayName);
		intent.putExtra("playType",playType);
		startActivity(intent);
	}
}
