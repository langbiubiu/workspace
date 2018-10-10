package com.ipanel.join.chongqing.live.manager.impl.hw;

import ipaneltv.toolkit.TimeToolkit.Weekday;
import ipaneltv.toolkit.db.DatabaseObjectification.ChannelKey;
import ipaneltv.toolkit.db.DatabaseObjectification.Program;
import ipaneltv.toolkit.entitlement.EntitlementObserver.EntitlementsState;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;

import org.apache.http.Header;
import org.apache.http.message.BasicHeader;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.text.TextUtils;
import android.util.Log;
import cn.ipanel.android.LogHelper;
import cn.ipanel.android.net.cache.JSONApiHelper;
import cn.ipanel.android.net.http.RequestParams;

import com.google.gson.Gson;
import com.ipanel.chongqing_ipanelforhw.CQApplication;
import com.ipanel.chongqing_ipanelforhw.R;
import com.ipanel.chongqing_ipanelforhw.hwstruct.GetHwRequest;
import com.ipanel.chongqing_ipanelforhw.hwstruct.GetHwResponse;
import com.ipanel.join.chongqing.live.Constant;
import com.ipanel.join.chongqing.live.manager.DataManager.ShiftProgram;
import com.ipanel.join.chongqing.live.manager.IManager;
import com.ipanel.join.chongqing.live.manager.DataManager.ShiftChannel;
import com.ipanel.join.chongqing.live.manager.impl.BaseDataManagerImpl;
import com.ipanel.join.chongqing.live.navi.DatabaseObjects.LiveChannel;
import com.ipanel.join.chongqing.live.navi.DatabaseObjects.LiveGroup;
import com.ipanel.join.chongqing.live.navi.DatabaseObjects.LiveProgramEvent;
import com.ipanel.join.chongqing.live.navi.NaviCallback;
import com.ipanel.join.chongqing.live.navi.NaviFragment;
import com.ipanel.join.chongqing.live.navi.NaviInterface;
import com.ipanel.join.chongqing.live.util.Task;
import com.ipanel.join.chongqing.live.util.TaskManager;
import com.ipanel.join.chongqing.live.util.TaskManager.TaskHandler;
import com.ipanel.join.chongqing.live.util.TimeHelper;
import com.ipanel.join.chongqing.live.manager.impl.BaseStationManagerImpl;
import com.ipanel.join.chongqing.portal.PortalDataManager;
import com.ipanel.join.protocol.a7.ServiceHelper;
import com.ipanel.join.protocol.a7.ServiceHelper.ResponseHandlerT;
import com.ipanel.join.protocol.a7.ServiceHelper.SerializerType;

public class HWDataManagerImpl extends BaseDataManagerImpl implements
		NaviCallback {
	
	public final HashMap<String, HWShiftChannel> mShiftMap = new HashMap<String, HWShiftChannel>();
	/**
	 * key:serviceId value:欢网channelId
	 */
	public final HashMap<String, String> mHWMap = new HashMap<String, String>(); 
	public final HashMap<ChannelKey, HashMap<String, List<LiveProgramEvent>>> mEPGMap = new HashMap<ChannelKey, HashMap<String, List<LiveProgramEvent>>>();
	/** 收藏频道列表：serviceId*/
	public List<String> fav_channels = new ArrayList<String>();
	
	/** 时移节目是否返回 */
	protected boolean flag = true;
	
	/** 回看节目是否返回 */
	protected boolean back_flag = true;

	public int client_step_ok = 0;
	
	protected NaviInterface navi;
	private CQApplication mAppInstance;
	private ServiceHelper mServiceHelper;
	private TaskManager mTaskManager;
	
	public HWDataManagerImpl(IManager context, NaviFragment navf,
			CallBack callback) {
		super(context, callback);
		this.navi = navf.getNaviInterface(this);
		mAppInstance = CQApplication.getInstance();
		mServiceHelper = ServiceHelper.getHelper();
		mTaskManager = TaskManager.getInstance();
		mTaskManager.setTaskHandler(new TaskHandler() {
			
			@Override
			public void doTask(Task task) {
				// TODO Auto-generated method stub
				LogHelper.i("do one task: " + task.id);
				((HWStationManagerImpl)activity.getStationManager()).getPresentAndFollow((ChannelKey)task.obj1);
			}
		});
		mTaskManager.resetManager();
	}
	
	@Override
	public void checkDataValid() {
		// TODO Auto-generated method stub
		LogHelper.i("checkDataValid");
		super.checkDataValid();
		if (JSONApiHelper.isOnline(activity.getContext())) {
		}
		requestShiftChannel();
		requestHWChannel();
	}

	@Override
	public void onNaviUpdated() {
		// TODO Auto-generated method stub
		initChannelData();
		if(mTotalChannels != null && mTotalChannels.size()>0)
			client_step_ok = 0;
		client_step_ok++;
		LogHelper.i("init step client done client_step_ok = "+client_step_ok);
	}

	@Override
	public void onGuideUpdated(ChannelKey key) {
		// TODO Auto-generated method stub
		LogHelper.i("noticy event change of :"
				+ (key == null ? "null" : key.getProgram()));
		mCallBack.onChannelProgramChanged(key);
	}

	@Override
	public void onPresentFollowUpdated() {
		// TODO Auto-generated method stub
		LogHelper.i("noticy pf change");
		mCallBack.onPFDataChanged(null);
	}

	@Override
	public void onCommitGroupError(String name) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void onShiftUpdated() {
		// TODO Auto-generated method stub
		
	}
	
	@Override
	public List<LiveChannel> getAllChannel() {
		// TODO Auto-generated method stub
		List<LiveChannel> returns = new ArrayList<LiveChannel>();
		/*
		 * 隐藏部分频道
		 */
		for (int i = 0; i < mTotalChannels.size(); i++) {
			LiveChannel ch = mTotalChannels.get(i);
			if (ch.getType() != 4083 && 					//广播频道
				ch.getChannelNumber() != -1 &&				//无效频道号
				!ch.getName().equals("UNKNOW_SERVICE") &&	//无效频道名
				ch.getChannelKey().getProgram() != 701 &&	//党员教育
				ch.getChannelKey().getProgram() != 1307 &&	//DVN LOADER1
				ch.getChannelKey().getProgram() != 1308 &&	//TF LOADER
				ch.getChannelKey().getProgram() != 1309 &&	//DVN loader2
				ch.getChannelKey().getProgram() != 1310 ) {	//TF loader2
				returns.add(ch);
			}
		}
		return returns;
	}

	@Override
	public List<LiveGroup> getGroups() {
		// TODO Auto-generated method stub
		Log.d("channel", "-----***---getGroups---*****--");
		return navi.getGroups();
	}

	@Override
	public void commitOwnedGroup(LiveGroup g, List<ChannelKey> list) {
		// TODO Auto-generated method stub
		navi.commitOwnedGroup(g, list);
	}

	@Override
	public List<LiveChannel> getGroupedChannels(LiveGroup g) {
		// TODO Auto-generated method stub
		return navi.getGroupedChannels(g);
	}

	@Override
	public List<LiveChannel> getNumberedChannels() {
		// TODO Auto-generated method stub
		return navi.getNumberedChannels();
	}

	@Override
	public List<LiveProgramEvent> getDailyPrograms(LiveChannel ch, Weekday d) {
		// TODO Auto-generated method stub
		return navi.getDailyPrograms(ch, d);
	}

	@Override
	public List<LiveProgramEvent> getDailyPrograms(LiveChannel ch,
			int offsetOfToday) {
		// TODO Auto-generated method stub
		return navi.getDailyPrograms(ch, offsetOfToday);
	}

	@Override
	public EntitlementsState getEntitlements(int moduledID) {
		// TODO Auto-generated method stub
		return navi.getEntitlements(moduledID);
	}

	@Override
	public HWShiftChannel isShiftChannel(LiveChannel channel) {
		// TODO Auto-generated method stub
		if(channel == null){
			Log.d("isShiftChannel","channel is null");
			return null;
		}
		if(mShiftMap == null || mShiftMap.get(channel.getChannelKey().getProgram() + "") == null){
			return null;
		}
		HWShiftChannel shift = mShiftMap.get(channel.getChannelKey().getProgram() + ""
				);
		return shift;
	}

	@Override
	public List<LiveChannel> getAllShiftChannel() {
		// TODO Auto-generated method stub
		List<LiveChannel> shiftChannels = new ArrayList<LiveChannel>();
		for (LiveChannel ch : getAllChannel()) {
			if (isShiftChannel(ch) != null){
				shiftChannels.add(ch);
			}
		}
		return shiftChannels;
	}

	@Override
	public List<LiveProgramEvent> getShowProgramlist(LiveChannel ch,
			int offset, boolean again) {
		// TODO Auto-generated method stub
		if (offset > 0) { //EPG
			List<LiveProgramEvent> list_dvb = getDailyPrograms(ch, offset);
			if (list_dvb == null || list_dvb.size() == 0) {
				((HWStationManagerImpl) activity.getStationManager()).observeProgramGuide(ch.getChannelKey(), 0);
				return null;
			} else {
				return list_dvb;
			}
		} else if (offset < 0) { //回看
			String time = "";
			Date mDate = new Date();
			Calendar mCalendar = Calendar.getInstance();
			mCalendar.setTime(mDate);
			mCalendar.add(Calendar.DAY_OF_MONTH, offset);
			SimpleDateFormat formatter = new SimpleDateFormat("yyyyMMdd");
			time = formatter.format(mCalendar.getTime());
			HashMap<String, List<LiveProgramEvent>> epg = mEPGMap.get(ch.getChannelKey());
			if (epg != null) {
				List<LiveProgramEvent> list = epg.get(time);
				if (list != null && list.size() > 0) {
//					return caculate(list, ch.getChannelKey(), time);
					return list;
				} else {
					epg.put(time, new ArrayList<LiveProgramEvent>());
				}
			} else {
				mEPGMap.put(ch.getChannelKey(),
						new HashMap<String, List<LiveProgramEvent>>());
			}
			requestReplayProgram(ch, time);
		} else {
			List<LiveProgramEvent> list_dvb = getDailyPrograms(ch, offset);
			if (list_dvb == null || list_dvb.size() == 0) {
				((HWStationManagerImpl) activity.getStationManager()).observeProgramGuide(ch.getChannelKey(), 0);
			}
			String time = "";
			Date mDate = new Date();
			Calendar mCalendar = Calendar.getInstance();
			mCalendar.setTime(mDate);
			mCalendar.add(Calendar.DAY_OF_MONTH, offset);
			SimpleDateFormat formatter = new SimpleDateFormat("yyyyMMdd");
			time = formatter.format(mCalendar.getTime());
			List<LiveProgramEvent> list_vod = new ArrayList<LiveProgramEvent>();
			HashMap<String, List<LiveProgramEvent>> epg = mEPGMap.get(ch.getChannelKey());
			if (epg != null) {
				list_vod = epg.get(time);
				if (list_vod != null && list_vod.size() > 0) {
//					return caculate(list, ch.getChannelKey(), time);
//					return list_vod;
				} else {
					epg.put(time, new ArrayList<LiveProgramEvent>());
					requestReplayProgram(ch, time);
				}
			} else {
				mEPGMap.put(ch.getChannelKey(),
						new HashMap<String, List<LiveProgramEvent>>());
				requestReplayProgram(ch, time);
			}
			List<LiveProgramEvent> list = new ArrayList<LiveProgramEvent>();
			list.addAll(list_vod);
			list.addAll(list_dvb);
			return list;
		}
		return null;
		
//		List<LiveProgramEvent> list_dvb = getDailyPrograms(ch, offset);
//		if (list_dvb == null || list_dvb.size() == 0) {
//			((HWStationManagerImpl) activity.getStationManager()).observeProgramGuide(ch.getChannelKey(), 0);
//			return null;
//		} else {
//			return list_dvb;
//		}
		
		/*
		 * 机顶盒离线状态下获取 在线状态下调用欢网接口（不使用）
		 */
//		if (!JSONApiHelper.isOnline(activity.getContext())) { 
//			List<LiveProgramEvent> list_dvb = getDailyPrograms(ch, offset);
//			if (list_dvb == null || list_dvb.size() == 0) {
//				((HWStationManagerImpl) activity.getStationManager()).observeProgramGuide(ch.getChannelKey(), 0);
//				return null;
//			} else {
//				return list_dvb;
//			}
//		}
//		LogHelper.i("offset------>" + offset);
//		final String key = TimeHelper.changeOffsetToKey(offset);
//		HashMap<String, List<Program>> epg = mEPGMap.get(ch.getChannelKey());
//		if (epg != null) {
//			List<Program> list = epg.get(key);
//			if (list != null && list.size() > 0) {
//				return caculate(list, ch.getChannelKey(), offset, key);
//			} else {
//				epg.put(key, new ArrayList<Program>());
//			}
//		} else {
//			mEPGMap.put(ch.getChannelKey(),
//					new HashMap<String, List<Program>>());
//		}
//		String id = mHWMap.get(ch.getChannelKey().getProgram()+"");
//		LogHelper.i("欢网id = " + id );
//		GetHwRequest req = new GetHwRequest();
//		req.getDevice().setDnum("123");
//		req.getUser().setUserid("123");
//		req.setAction("GetDayProgramsByChannel");
//		req.getParam().setChannelId(id);
//		req.getParam().setDate(key);
//		req.getParam().setPage(1);
//		req.getParam().setPagesize(100);
//		mServiceHelper.setRootUrl(PortalDataManager.url);
//		mServiceHelper.setSerializerType(SerializerType.JSON);
//		mServiceHelper.callServiceAsync(activity.getContext(), req, GetHwResponse.class, new ResponseHandlerT<GetHwResponse>() {
//			
//			@Override
//			public void onResponse(boolean success, GetHwResponse result) {
//				// TODO Auto-generated method stub
//				final int N =  result == null || result.getPrograms() == null ? 0 : result.getPrograms().size();
//				LogHelper.i("fill channel epg program lenght :" + N);
//				List<Program> list = new ArrayList<Program>();
//				SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm");
//				for (int i = 0; i < N; i++) {
//					GetHwResponse.Program res = result.getPrograms().get(i);
//					Program p = new Program();
//					p.setName(res.getName());
//					long start = 0, end = 0;
//					try {
//						start = sdf.parse(res.getStart_time()).getTime();
//						end = sdf.parse(res.getEnd_time()).getTime();
//					} catch (ParseException e) {
//						// TODO Auto-generated catch block
//						e.printStackTrace();
//					}
//					p.setStart(start);
//					p.setEnd(end);
//					p.setTag(res.getWiki_id()); //暂设为wikiId，等欢网提供华为Id后更换
//					LogHelper.i(p.getName() + "  " + TimeHelper.getDetailTime(p.getStart()));
//					list.add(p);
//				}
//				mEPGMap.get(ch.getChannelKey()).put(key, list);
//				mCallBack.onEPGDataChanged(ch.getChannelKey(), key);
//			}
//		});
//		return null;
	}
	
	private List<LiveProgramEvent> caculate(List<Program> list, ChannelKey key, String time) {
		List<LiveProgramEvent> result = new ArrayList<LiveProgramEvent>();
		final int N = list == null ? 0 : list.size();
		for (int i = 0; i < N; i++) {
			Program p = list.get(i);
			LiveProgramEvent entry = new LiveProgramEvent();
			entry.setName(p.getName());
			entry.setEnd(p.getEnd());
			entry.setStart(p.getStart());
			entry.setChannelKey(key);
			entry.setTag(p.getTag());
			entry.status = -1;
			if (TimeHelper.isPlayed(p.getStart(), p.getEnd(),
					System.currentTimeMillis())) {
				entry.status = -1;
			} else if (TimeHelper.isPlaying(p.getStart(), p.getEnd(),
					System.currentTimeMillis())) {
				entry.status = 0;
			} else {
				if (activity.getBookManager().isProgramBooked(p.getStart(),
						key.getProgram())) {
					entry.status = 1;
				} else {
					entry.status = -1;
				}
			}
			result.add(entry);
		}
		return result;
	}

	@Override
	public Program getChannelCurrentProgram(LiveChannel channel) {
		// TODO Auto-generated method stub
		Program p = channel.getPresent();
		if(p != null){
			return p;
		}
		return null;
	}

	@Override
	public Program getChannelNextProgram(LiveChannel channel) {
		// TODO Auto-generated method stub
		Program p = channel.getFollow();
		if(p != null){
			return p;
		}
		return null;
	}
	
	@Override
	public void requestChannelPF(ChannelKey key) {
		// TODO Auto-generated method stub
//		((HWStationManagerImpl)activity.getStationManager()).getPresentAndFollow(key);
		LiveChannel ch = getLiveChannelByService(key.getProgram());
		if (ch != null)
			mTaskManager.insertTask(new Task(ch.getChannelNumber(), key, 0));
	}

	@Override
	public void requestShiftURL(final LiveChannel channel) {
		// TODO Auto-generated method stub
		LogHelper.i("request shift data ");
		HWShiftChannel shift = isShiftChannel(channel);
		if (shift != null) {
			String pre_url = mAppInstance.getEpgUrl();
			String ipaddr = mAppInstance.getIP();
			String macaddr = mAppInstance.getMAC();
			String caId = mAppInstance.getCardID();
			String ServiceGroupID = mAppInstance.getServicegroup();
			String url = pre_url + "/tstvresouce.jsp?User=&pwd=&ip=" + ipaddr
					+ "&NTID=" + macaddr + "&CARDID=" + caId
					+ "&Version=1.0&lang=1&supportnet=" + mAppInstance.getNetYype(activity.getContext())
					+ "&decodemode=H.264HD;MPEG-2HD&CA=1&ServiceGroupID="
					+ ServiceGroupID + "&encrypt=0&Prognum=30&ChannelID="
					+ shift.getShiftId();
			mServiceHelper.setRootUrl(url);
			mServiceHelper.setSerializerType(SerializerType.TEXT);
			mServiceHelper.setHeaders(new Header[] { new BasicHeader("Cookie", mAppInstance.getCookieString()) });
			mServiceHelper.callServiceAsync(activity.getContext(),
					new RequestParams(), String.class,
					new ResponseHandlerT<String>() {
						@Override
						public void onResponse(boolean success, String result) {
							if (success&&!TextUtils.isEmpty(result)) {
								result = result.trim();
								noticeURL(result, channel);
							} else {
								LogHelper
										.i("faild to get url for bad responce");
							}
						}
					});
		}
	}
	
	private void noticeURL(String result, LiveChannel channel) {
		if (isValidResponse(result)) {
			result=result.replace("&nbsp;", "");
			String tstv_str = "", proList = "";
			HWShiftChannel shift = isShiftChannel(channel);
			int start = Math.min(result.indexOf("PlayUrl="),
					result.indexOf("SessionId="));
			int end = Math.max(result.indexOf("PlayUrl="),
					result.indexOf("SessionId="));
			tstv_str = result.substring(start, end);
			tstv_str += ",type = TSTV";

			shift.setPlayUrl(tstv_str.split("\\^")[4]);
			LogHelper.i(String.format("get url : %s", shift.getPlayUrl()));
			
			proList = result.substring(result.indexOf("Proglist=") + 9,
					result.indexOf("MaxShiftWindow") - 3);
			String[] infoarr = proList.split("&");
			int objNum = (int) Math.ceil(infoarr.length / 3);
			int j = 0;
			LogHelper.i("get the channel timeshift detail data:");
			try{
				for (int i = 0; i < objNum; i++) {
					j = i * 3;
					HWShiftProgram entry = new HWShiftProgram();
					entry.setName(infoarr[j]);
//					entry.setStart(Long.parseLong(infoarr[j + 1].substring(10)));
//					entry.setEnd(Long.parseLong(infoarr[j + 2].substring(8,22)));
					try {
						//从yyyyMMddHHmmss格式转为毫秒
						long startTime = TimeHelper.formatter_c.parse(infoarr[j + 1].substring(10)).getTime();
						long endTime = TimeHelper.formatter_c.parse(infoarr[j + 2].substring(8,22)).getTime();
						entry.setStart(startTime);
						entry.setEnd(endTime);
					} catch (ParseException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
					shift.getShiftPrograms().add(entry);
					LogHelper.i(String.format("shift event name: %s  .start ; %s  ,end :  %s", infoarr[j],infoarr[j + 1].substring(10),infoarr[j + 2].substring(8,22)));
				}
				mCallBack.onShiftDataResponse(shift);
			}catch(IndexOutOfBoundsException e){
				e.printStackTrace();
				LogHelper.i("faild to get program for bad str");
				return ;
			}
			LogHelper.i("get the channel timeshift detail data:");
		} else {
			LogHelper.i("faild to get url for bad str");
		}
	}
	
	private boolean isValidResponse(String response) {
		String s="response Result=";
		try{
			int start=response.indexOf(s)+s.length();
			String code=response.substring(start,start+1);
			LogHelper.i("response code is: "+code);
//			return "0".equals(code);
			return response.length()>s.length()*3;
		}catch(Exception e){
			e.printStackTrace();
		}
		return false;
	}

	@Override
	public void requestShiftProgram(LiveChannel channel) {
		// TODO Auto-generated method stub

	}

	@Override
	public void requestReplayProgram(final LiveChannel channel, final String time) {
		// TODO Auto-generated method stub
		HWShiftChannel shift = getShiftChannel(channel);
		String url = mAppInstance.getEpgUrl()
				+ "/defaultHD/en/datajspHD/android_tvod_getProgramInfo.jsp?timestemp="
				+ time + "&curChanId=" + shift.getShiftId();
		mServiceHelper.setRootUrl(url);
		mServiceHelper.setSerializerType(SerializerType.TEXT);
		mServiceHelper.setHeaders(new Header[] { new BasicHeader("Cookie",
				mAppInstance.getCookieString()) });
		mServiceHelper.callServiceAsync(activity.getContext(), new RequestParams(), 
				String.class, new ResponseHandlerT<String>() {              
			
			@Override
			public void onResponse(boolean success, String result) {
				// TODO Auto-generated method stub
				List<LiveProgramEvent> programs = new ArrayList<LiveProgramEvent>();
				if (success && result != null
						&& result.trim().length() > 0) {
					try {
						JSONArray ja = new JSONArray(result);
						SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMddHH:mm");
						int N = ja.length();
						for (int i = 0; i < N; i++) {
							JSONObject jo = ja.getJSONObject(i);
							LiveProgramEvent entry = new LiveProgramEvent();
							entry.setName(jo.optString("tvodName"));
							entry.setJumpUrl(jo.optString("jumpUrl"));
//							entry.setTime(jo.optString("timeStr"));
							long start = sdf.parse(time + jo.optString("timeStr")).getTime();
							long end = sdf.parse(time + jo.optString("timeEnd")).getTime();
							entry.setStart(start);
							entry.setEnd(end);
							programs.add(entry);
						}
						mEPGMap.get(channel.getChannelKey()).put(time, programs);
						mCallBack.onEPGDataChanged(channel.getChannelKey(), time);
					} catch (JSONException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					} catch (ParseException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
				}
			}
		});
	}

	@Override
	public void observeProgramGuide(ChannelKey key, long time) {
		// TODO Auto-generated method stub
		BaseStationManagerImpl mStationManager = (BaseStationManagerImpl) activity.getStationManager();
		mStationManager.observeProgramGuide(key, time);
	}

	@Override
	public void getFavouriteChannels() {
		// TODO Auto-generated method stub
		LogHelper.i("requestFavoriteChannel");
		GetHwRequest req = new GetHwRequest();
		req.setAction("GetFavChannelsByUser");
		req.getDevice().setDnum("123");
		req.getUser().setUserid("123");
		req.getParam().setShowlive(false);
		req.getParam().setPage(1);
		req.getParam().setPagesize(200);
		
		mServiceHelper.setRootUrl(PortalDataManager.url);
		mServiceHelper.setSerializerType(SerializerType.JSON);
		mServiceHelper.callServiceAsync(activity.getContext(), req, GetHwResponse.class, new ResponseHandlerT<GetHwResponse>() {
			
			@Override
			public void onResponse(boolean success, GetHwResponse result) {
				// TODO Auto-generated method stub
				if (!success) {
					LogHelper.i("request detail data failed");
					return;
				}
				if (result == null) {
					LogHelper.i("failed to parse JSON data");
					return;
				}
				for (int i = 0; i < result.getChannels().size(); i++) {
					String serviceId = result.getChannels().get(i).getServiceId();
					fav_channels.add(serviceId);
				}
				activity.getUIManager().dispatchDataChange(Constant.DATA_CHANGE_OF_FAVOURITE,
						null);
			}
		});
	}

	@Override
	public boolean isFavoriteChannel(LiveChannel channel) {
		// TODO Auto-generated method stub
//		if (mFavorites.contains(channel.getChannelKey()))
//			return true;
//		return false;
		
		if (fav_channels!=null && fav_channels.size()>0) {
			for (String service_id : fav_channels) {
				if (service_id.equals(channel.getChannelKey().getProgram()+""))
					return true;
			}
		}
		
		return false;
	}

	@Override
	public String getShiftPlayURL(LiveChannel channel, long startTime,
			long endTime) {
		// TODO Auto-generated method stub
		HWShiftChannel shift = isShiftChannel(channel);
		if (shift != null) {
			return shift.getPlayUrl();
		}
		return null;
	}

	@Override
	public ShiftProgram getShiftProgramAtTime(LiveChannel channel, long time) {
		// TODO Auto-generated method stub
		HWShiftChannel shift = isShiftChannel(channel);
		if (shift != null && shift.getShiftPrograms() != null && shift.getShiftPrograms().size() > 0) {
//			long t = Long.parseLong(TimeHelper.getStartModeShiftTime(time));
			for (HWShiftProgram program : shift.getShiftPrograms()) {
				if (program.getStart() < time && program.getEnd() > time) {
					return program;
				}
			}
		}
		return null;
	}

	@Override
	public boolean isReadbyOK() {
		// TODO Auto-generated method stub
		return client_step_ok > 0;
	}

	@Override
	public void setWaitingForData() {
		// TODO Auto-generated method stub
		client_step_ok = -1;
	}
	
	@Override
	public HWShiftChannel getShiftChannel(LiveChannel channel) {
		// TODO Auto-generated method stub
		if(channel != null)
			return mShiftMap.get(channel.getChannelKey().getProgram() + "");
		return null;
	}
	
	@Override
	public void updateChannelFavorite(final LiveChannel channel) {
		// TODO Auto-generated method stub
		if (JSONApiHelper.isOnline(activity.getContext())) {
			String hw_id = "";
			if(mHWMap!=null&&mHWMap.get(channel.getChannelKey().getProgram()+"")!=null)
				hw_id = mHWMap.get(channel.getChannelKey().getProgram()+"");
			
			if (isFavoriteChannel(channel)) {
				GetHwRequest req = new GetHwRequest();
				req.setAction("DelFavChannelByUser");
				req.getDevice().setDnum("123");
				req.getUser().setUserid("123");
				req.getParam().setChannelId(hw_id);
				mServiceHelper.setRootUrl(PortalDataManager.url);
				mServiceHelper.setSerializerType(SerializerType.JSON);
				mServiceHelper.callServiceAsync(activity.getContext(), req, GetHwResponse.class, new ResponseHandlerT<GetHwResponse>() {
					
					@Override
					public void onResponse(boolean success, GetHwResponse result) {
						// TODO Auto-generated method stub
						if (!success) {
							LogHelper.i("request detail data failed");
							return;
						}
						if (result == null) {
							LogHelper.i("failed to parse JSON data");
							return;
						}
						if (result.getError().getCode() == 0) {
							LogHelper.i("updateChannelFavorite cancel success");
							fav_channels.remove(channel.getChannelKey().getProgram()+"");
							mCallBack.onFavoriteChannelChanged(Constant.DATA_CHANGE_OF_CANCEL_FAVORITE_CHANNEL, true);
						} else {
							LogHelper.i("updateChannelFavorite cancel failure");
							mCallBack.onFavoriteChannelChanged(Constant.DATA_CHANGE_OF_CANCEL_FAVORITE_CHANNEL, false);
						}
					}
				});
			} else {
				GetHwRequest req = new GetHwRequest();
				req.setAction("SetFavChannelByUser");
				req.getDevice().setDnum("123");
				req.getUser().setUserid("123");
				req.getParam().setChannelId(hw_id);
				mServiceHelper.setRootUrl(PortalDataManager.url);
				mServiceHelper.setSerializerType(SerializerType.JSON);
				mServiceHelper.callServiceAsync(activity.getContext(), req, GetHwResponse.class, new ResponseHandlerT<GetHwResponse>() {
					
					@Override
					public void onResponse(boolean success, GetHwResponse result) {
						// TODO Auto-generated method stub
						if (!success) {
							LogHelper.i("request detail data failed");
							return;
						}
						if (result == null) {
							LogHelper.i("failed to parse JSON data");
							return;
						}
						if (result.getError().getCode() == 0) {
							LogHelper.i("updateChannelFavorite set success");
							fav_channels.add(channel.getChannelKey().getProgram()+"");
							mCallBack.onFavoriteChannelChanged(Constant.DATA_CHANGE_OF_SET_FAVORITE_CHANNEL, true);
						} else {
							LogHelper.i("updateChannelFavorite set failure");
							mCallBack.onFavoriteChannelChanged(Constant.DATA_CHANGE_OF_SET_FAVORITE_CHANNEL, false);
						}
					}
				});
			}
		}
	}
	
	private void requestShiftChannel() {
		LogHelper.i("requestShiftChannel");
		String ip = mAppInstance.getIP();
		String mac = mAppInstance.getMAC();
		if (ip == "" || mac == "") {
			mAppInstance.loadNetworkInfo();
			ip = mAppInstance.getIP();
			mac = mAppInstance.getMAC();
		}
		String epgUrl = mAppInstance.getEpgUrl();
		String caId = mAppInstance.getCardID();
		
		String url = epgUrl
				+ "/tstvlist.jsp?User=&pwd=&ip=" + ip
				+ "&NTID=" + mac
				+ "&CARDID="+ caId
				+ "&Version=1.0&lang=1&supportnet=" + mAppInstance.getNetYype(activity.getContext())
				+"&decodemode=H.264HD;MPEG-2HD&CA=1&&encrypt=0";
		LogHelper.i("requeset shift channel url:" + url);
		mServiceHelper.setRootUrl(url);
		mServiceHelper.setSerializerType(SerializerType.TEXT);
		mServiceHelper.setHeaders(new Header[] { new BasicHeader("Cookie",
				mAppInstance.getCookieString()) });
		mServiceHelper.callServiceAsync(activity.getContext(), new RequestParams(),
				String.class, new ResponseHandlerT<String>() {
					@Override
					public void onResponse(boolean success, String result) {
						if (success && result != null
								&& result.trim().length() > 0) {
							mShiftMap.clear();
							String str = result.substring(9,
									result.indexOf(";END"));
							String[] s = str.split(";");
							for (String ts : s) {
								HWShiftChannel entry = new HWShiftChannel();
								String[] ids = ts.split(",");
								entry.setShiftId(ids[1]);
								entry.setServiceId(ids[0]);
								entry.setTag(ids[2]);
								mShiftMap.put(entry.getServiceId(), entry);
							}
							LogHelper.i(String
									.format("request replay channels success,and size : %d",
											mShiftMap.size()));
						}
					}
				});
	}
	
	/**
	 * 获取欢网频道列表
	 */
	private void requestHWChannel() {
		LogHelper.i("requestHWChannel");
		GetHwRequest req = new GetHwRequest();
		req.getDevice().setDnum("123");
		req.getUser().setUserid("123");
		req.setAction("GetChannels");
		req.getParam().setType("");
		req.getParam().setShowlive(false);
		req.getParam().setPage(1);
		req.getParam().setPagesize(200);
		
		mServiceHelper.setRootUrl(PortalDataManager.url);
		mServiceHelper.setSerializerType(SerializerType.JSON);
		mServiceHelper.callServiceAsync(activity.getContext(), req, GetHwResponse.class, new ResponseHandlerT<GetHwResponse>() {
			
			@Override
			public void onResponse(boolean success, GetHwResponse result) {
				// TODO Auto-generated method stub
				if (!success) {
					LogHelper.i("request detail data failed");
					return;
				}
				if (result == null) {
					LogHelper.i("failed to parse JSON data");
					return;
				}
				for (int i = 0; i < result.getChannels().size(); i++) {
					String serviceId = result.getChannels().get(i).getServiceId();
					String channelId = result.getChannels().get(i).getChannelId();
					mHWMap.put(serviceId, channelId);
				}
				getFavouriteChannels();
			}
		});
	}
	
	public class HWShiftChannel extends ShiftChannel {
		
		private String shiftId;
		private String serviceId;
		private String channelName;
		private String playUrl;
		private String tag;
		private List<HWShiftProgram> programs = new ArrayList<HWShiftProgram>();

		@Override
		public String getMatchKey() {
			// TODO Auto-generated method stub
			return null;
		}

		@Override
		public String getId() {
			// TODO Auto-generated method stub
			return shiftId;
		}
		
		public String getShiftId() {
			return shiftId;
		}

		public void setShiftId(String shiftId) {
			this.shiftId = shiftId;
		}

		public String getServiceId() {
			return serviceId;
		}

		public void setServiceId(String serviceId) {
			this.serviceId = serviceId;
		}

		public String getChannelName() {
			return channelName;
		}

		public void setChannelName(String channelName) {
			this.channelName = channelName;
		}

		public String getPlayUrl() {
			return playUrl;
		}

		public void setPlayUrl(String playUrl) {
			this.playUrl = playUrl;
		}

		public List<HWShiftProgram> getShiftPrograms() {
			return programs;
		}
		
		public void setShiftPrograms(List<HWShiftProgram> programs) {
			this.programs = programs;
		}
		
		public String getTag() {
			return tag;
		}
		
		public void setTag(String tag) {
			this.tag = tag;
		}
	}

	public class HWShiftProgram extends ShiftProgram {
		
	}
}

