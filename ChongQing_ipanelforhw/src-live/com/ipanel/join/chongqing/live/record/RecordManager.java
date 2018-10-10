package com.ipanel.join.chongqing.live.record;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import cn.ipanel.android.LogHelper;
import cn.ipanel.android.net.cache.JSONApiHelper;
import cn.ipanel.android.net.http.RequestParams;

import com.ipanel.chongqing_ipanelforhw.CQApplication;
import com.ipanel.join.chongqing.live.util.TimeHelper;
import com.ipanel.join.protocol.a7.ServiceHelper;
import com.ipanel.join.protocol.a7.ServiceHelper.ResponseHandlerT;
import com.ipanel.join.protocol.a7.ServiceHelper.SerializerType;
import com.ipanel.join.protocol.cq.domain.Header;
import com.ipanel.join.protocol.cq.domain.Request_OrderRecord;
import com.ipanel.join.protocol.cq.domain.Request_RecordList;
import com.ipanel.join.protocol.cq.domain.Request_RecordTask;
import com.ipanel.join.protocol.cq.domain.Response_OrderRecord;
import com.ipanel.join.protocol.cq.domain.Response_ProgramList;
import com.ipanel.join.protocol.cq.domain.Response_RecordTask;
import com.ipanel.join.protocol.cq.domain.Request_OrderRecord.Body;
import com.ipanel.join.protocol.cq.domain.Request_OrderRecord.Order;
import com.ipanel.join.protocol.cq.domain.Request_RecordList.Query;
import com.ipanel.join.protocol.cq.domain.Response_ProgramList.AssetPrograms;
import com.ipanel.join.protocol.cq.domain.Response_ProgramList.Channels;

import android.annotation.SuppressLint;
import android.content.Context;
import android.os.Handler;

public class RecordManager {
	
	public static final int RECORD_STATE_DOING=1;
	public static final int RECORD_STATE_DONE=2;
	public static final int RECORD_STATE_UNDONE=-1;

	private String TAG = RecordManager.class.getSimpleName();
	private static RecordManager mInstance;
	private Context mContext;
	private RecordCallBack mCallBack;
	// 录制频道列表
	private List<Channels> channels = new ArrayList<Channels>();
	// 录制记录map
	private HashMap<String, String> keys = new HashMap<String, String>();
	// 节目记录map
	private HashMap<String, AssetPrograms> program_map = new HashMap<String, AssetPrograms>();
	private long time_period = 1 * 3600 * 1000;
	
	private String url = "http://192.168.203.39:80/interface/communication";
	private ServiceHelper mServiceHelper;
	private boolean order_flag = false;
	private String current_key = "";

	private  RecordManager(Context context) {
		// TODO Auto-generated constructor stub
		this.mContext = context.getApplicationContext();
		mServiceHelper=ServiceHelper.createOneHelper();
	}

	public static synchronized RecordManager getInstance(Context context) {
		if (mInstance == null) {
			mInstance = new RecordManager(context);
		}
		return mInstance;
	}
	
	static Thread loopThread=null;
	
	public void doRecordStateCheck(final Context cxt){
		if (loopThread != null) {
			loopThread.interrupt();
			return;
		}
		loopThread = new Thread() {
			public void run() {
				while (true) {
					try {
						if(JSONApiHelper.isOnline(cxt)){
							requestRecordTask();
						}else{
							LogHelper.i("give up to do get record for bad network");
						}
						Thread.sleep(time_period);
					} catch (Exception e) {
						if (!(e instanceof InterruptedException)) {
							try {
								Thread.sleep(5000);
							} catch (InterruptedException ee) {

							}
						}
					}
				}
			}
		};
		loopThread.start();
	}

	public void start() {
		doRecordStateCheck(mContext);
	}

	public void stop() {

	}
	
	Handler mHandler = new Handler() {

	};

	public void setCallBack(RecordCallBack mCallBack) {
		this.mCallBack = mCallBack;
	}
	
	// 获取节目列表
	public void requestProgramList(String start_time, String end_time,
			final String key, final Request_OrderRecord record) {
		LogHelper.i(TAG, "requestProgramList");
//		mServiceHelper.setRootUrl(
//				"asset:///program_list_response.json");
		mServiceHelper.setRootUrl(url);
		mServiceHelper.setSerializerType(SerializerType.JSON);
		mServiceHelper.callServiceAsync(mContext,
				getRequestProgramListParam(start_time, end_time),
				Response_ProgramList.class,
				new ResponseHandlerT<Response_ProgramList>() {
					@Override
					public void onResponse(boolean success,
							Response_ProgramList result) {
						LogHelper.i(String.format("order result: %s  %s", result
											.getBody().getResult().getCode(),
											result.getBody().getResult()
													.getDescription()));
						if (success&&"0".equals(result.getBody().getResult().getCode())) {
							
							channels = result.getBody()
									.getChannels();
							final int N = channels.size();
							for (int i = 0; i < N; i++) {
								Channels channel = channels.get(i);
								List<AssetPrograms> programs = channel
										.getAssetprograms();
								final int M = programs.size();
								for (int j = 0; j < M; j++) {
									AssetPrograms program = programs.get(j);
									String key = getMapKey(
											channel.getTitleFull(),
											program.getStartDateTime());
									LogHelper.i(String.format(
											"add key value : %s - %s", key,
											program.getProgramName()));
									program_map.put(key, program);
								}
							}
						} else {
							LogHelper.i(result.getBody().getResult().getDescription());
						}
						if (key != null) {
							AssetPrograms program = program_map.get(key);
							LogHelper.i(program + "");
							if (program != null && record != null) {
								requestLocalOrderRecord(record, program, key);
							} else {
								LogHelper.e(String.format(
										"can't get program for %s ", key));
								if (mCallBack != null) {
									mCallBack.onOrderRecordResponse("-1", key);
								}

							}
						}
					}
				});
	}
	
	// 获取录制Task列表
	public void requestRecordTask() {
		LogHelper.i(TAG, "requestRecordTask");
		mServiceHelper
				.setRootUrl("asset:///order_list_response.xml");
		mServiceHelper.setRootUrl(url);
		mServiceHelper.setSerializerType(SerializerType.XML);
		mServiceHelper.callServiceAsync(mContext,
				requestRecordTaskParam(), Response_RecordTask.class,
				new ResponseHandlerT<Response_RecordTask>() {
					@SuppressLint("DefaultLocale")
					@Override
					public void onResponse(boolean success,
							final Response_RecordTask result) {
						if (success&&"0".equals(result.getBody().getTaks()
								.getResultCode())) {
							List<Response_RecordTask.TaskInfo> records = result
									.getBody().getTaks().getTaskinfolist();
							if(records==null){
								return ;
							}
							LogHelper.i(String.format(
									"get record list size : %d", records.size()));
							final int N = records.size();
							keys.clear();
							for (int i = 0; i < N; i++) {
								Response_RecordTask.TaskInfo context = records
										.get(i);
								LogHelper.i(String.format(
										"add record key valde : %s - %s",
										getMapKey(context.getChannelName(),
												context.getBeginTime()),
										context.getStatus()));
								keys.put(
										getMapKey(context.getChannelName(),
												context.getBeginTime()),
										context.getStatus());
							}
							if (mCallBack != null) {
								mHandler.post(new Runnable() {

									@Override
									public void run() {
//										LogHelper.i("onOrderRecordResponse " + order_flag + " " + current_key);
//										mCallBack.onOrderRecordResponse(order_flag, current_key);
										mCallBack.onOrderRecordResponse(result.getBody().getTaks()
												.getResultCode(), current_key);
									}
								});
							}
						} else {
							LogHelper.i(result.getBody().getTaks().getDescription());
						}
					}
				});
	}

	// 请求节目录制
	@SuppressLint("DefaultLocale")
	public void requestOrderRecord(final String channel_name,
			final String start_time, String end_time) {
		LogHelper.i(TAG, "requestOrderRecord");
		if ("-1".equals(end_time)) {
			end_time = Long.parseLong(start_time) + 3600 * 1000 * 5 + "";
		}
		final String recordType = Long.parseLong(start_time) > System
				.currentTimeMillis() ? "1" : "1";
		final String event_start_time = TimeHelper.getEventTime(start_time);
//		final String event_end_time = TimeHelper.getEventTime(end_time);
		final String order_time = TimeHelper.getEventTime(System
				.currentTimeMillis() + "");
		final String key = getMapKey(channel_name, event_start_time);
		current_key = key;
		final boolean recorded = isProgramRecord(key);
		Request_OrderRecord param = new Request_OrderRecord();
		param.setHeader(Header.getOneInstance("NPVR_ORDER_REQUEST"));
		Order order = new Order();
		order.setAction(recorded ? "DELETE" : "REGIST");
		// order.setCode("000011");
		order.setUUID(CQApplication.getInstance().getUid());
		order.setAppID("10000101");
		order.setSPID("100001");
		order.setRecordType(recordType);
		// order.setBeginTime(event_start_time);
		// order.setEndTime(event_end_time);

		order.setBeginTime("");
		order.setEndTime("");
		order.setOrderTime(order_time);

//			order.setCode(authToken);

		AssetPrograms program = program_map.get(key);
		param.setBody(Body.createOneInstace(order));
		if (program == null) {
			requestProgramList(start_time, end_time, key, param);
		} else {
			requestLocalOrderRecord(param, program, key);
		}
	}

	// 查询资产ＩＤ
	public void requestLocalOrderRecord(Request_OrderRecord param,
			AssetPrograms program, final String key) {
		LogHelper.i(TAG, "requestLocalOrderRecord");
		Order order = param.getBody().getOrders().getOrderlist().get(0);
		order.setProgramID(program.getAssetID());
		order.setChannelID(program.getChannelID());
		// order.setEndTime(program.getEndDateTime());
		mServiceHelper.setRootUrl(
				"asset:///order_record_response.xml");
		mServiceHelper.setRootUrl(url);

		mServiceHelper.setSerializerType(SerializerType.XML);
		mServiceHelper.callServiceAsync(mContext, param,
				Response_OrderRecord.class,
				new ResponseHandlerT<Response_OrderRecord>() {
					@Override
					public void onResponse(boolean success,
							final Response_OrderRecord result) {
						LogHelper.i(String
								.format("order result: %s  %s", result
										.getBody().getResult().getCode(),
										result.getBody().getResult()
												.getDescription()));
						// if (isProgramRecord(key)) {
						// keys.remove(key);
						// } else {
						// keys.put(key, 1 + "");
						// }
						order_flag = "0".equals(result.getBody().getResult().getCode());
						if (order_flag)
							doRecordStateCheck(mContext);
						else {
							if (mCallBack != null) {
								mHandler.post(new Runnable() {

									@Override
									public void run() {
										mCallBack.onOrderRecordResponse(result.getBody()
														.getResult().getCode(),key);
									}
								});

							}
						}
					}
				});
	}
	
	public String getMapKey(String channel_name, String time) {
		return channel_name + "___" + time;
	}

	public Request_OrderRecord requestOrderRecordParam() {
		return new Request_OrderRecord();
	}

	public Request_RecordList requestRecordListParam() {
		Request_RecordList request = new Request_RecordList();
		request.setVersion("1.0");
		request.setHeader(Header.getOneInstance("ISPACE_USER_CONTENT_QUERY"));

		Query query = new Query();
		query.setUUID(CQApplication.getInstance().getUid());
		query.setAppID("10000101");
		query.setSPID("100001");
		query.setAccessToken(CQApplication.getInstance().getAuthToken());
		// query.setContentSearchType("1");
		query.setPageNo("1");
		query.setPageSize("1000");
		// query.setSortType("");
		query.setStatus("0");
		query.setType("9");
		query.setFileType("1");
		request.setBody(query);
		return request;
	}

	public Request_RecordTask requestRecordTaskParam() {
		Request_RecordTask request = new Request_RecordTask();
		request.setVersion("1.0");
		request.setHeader(Header.getOneInstance("ISPACE_USER_TASK_QUERY"));

		Request_RecordTask.Query query = new Request_RecordTask.Query();
		query.setUUID(CQApplication.getInstance().getUid());
		query.setAppID("10000101");
		query.setSPID("100001");
		query.setAccessToken(CQApplication.getInstance().getAuthToken());
		query.setPageNo("1");
		query.setPageSize("1000");
		query.setStatus("");
		query.setType("1");
		request.setBody(query);
		return request;
	}

	public RequestParams getRequestProgramListParam(String start, String end) {
		RequestParams params = new RequestParams();
		params.put("command", "CHANNEL_SCHEDULE_QUERY");
		params.put("uuid", CQApplication.getInstance().getUid());
		params.put("spId", "100001");
		params.put("appID", "10000101");
		params.put("startTime", TimeHelper.getDayTime(Long.parseLong(start)));
		params.put("endTime", TimeHelper.getDayTime(Long.parseLong(end)));
		return params;
	}
	
	public boolean isRecordChannel(String channel_name) {
		for (Channels ch : channels) {
			if (ch.getTitleFull().equals(channel_name)) {
				LogHelper.i("isRecordChannel " + channel_name + " true");
				return true;
			}
		}
		LogHelper.i("isRecordChannel " + channel_name + " false");
		return false;
	}

	public boolean isProgramRecord(String channel_name, String time) {
		String event_time = TimeHelper.getEventTime(time);
		String key = getMapKey(channel_name, event_time);
		LogHelper.e(String.format("get record state %s : %s", key,
				keys.get(key)));
		return "1".equals(keys.get(key)) || "2".equals(keys.get(key));
	}

	public boolean isProgramRecord(String key) {
		LogHelper.e(String.format("get record state %s : %s", key,
				keys.get(key)));
		return "1".equals(keys.get(key)) || "2".equals(keys.get(key));
	}
	public int getProgramRecord(String channel_name, String time) {
		String event_time = TimeHelper.getEventTime(time);
		String key = getMapKey(channel_name, event_time);
		String state=keys.get(key);
		return state==null||state.equals("")?RECORD_STATE_UNDONE:Integer.parseInt(state);
	}
	public int getProgramRecord(String key) {
		String state=keys.get(key);
		return state==null||state.equals("")?RECORD_STATE_UNDONE:Integer.parseInt(state);
	}
	
	public interface RecordCallBack {
//		public void onOrderRecordResponse(boolean success, Object obj);
		public void onOrderRecordResponse(String code, Object obj);

	}
}
