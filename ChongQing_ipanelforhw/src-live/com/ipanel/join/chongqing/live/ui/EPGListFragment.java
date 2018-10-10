package com.ipanel.join.chongqing.live.ui;

import ipaneltv.toolkit.db.DatabaseObjectification.ChannelKey;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

import org.apache.http.Header;
import org.apache.http.message.BasicHeader;
import org.json.JSONException;
import org.json.JSONObject;

import android.app.Dialog;
import android.content.Intent;
import android.os.Message;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.TextView;

import cn.ipanel.android.LogHelper;
import cn.ipanel.android.net.http.RequestParams;

import com.ipanel.chongqing_ipanelforhw.CQApplication;
import com.ipanel.chongqing_ipanelforhw.R;
import com.ipanel.join.chongqing.live.Constant;
import com.ipanel.join.chongqing.live.base.BaseFragment;
import com.ipanel.join.chongqing.live.book.Alarms;
import com.ipanel.join.chongqing.live.data.BookData;
import com.ipanel.join.chongqing.live.manager.UIManager;
import com.ipanel.join.chongqing.live.navi.DatabaseObjects.LiveChannel;
import com.ipanel.join.chongqing.live.navi.DatabaseObjects.LiveProgramEvent;
import com.ipanel.join.chongqing.live.view.JEventListView_b;
import com.ipanel.join.chongqing.live.view.JListView;
import com.ipanel.join.chongqing.live.view.JWeekListView;
import com.ipanel.join.chongqing.live.view.ListViewListenerSet.ListFocusChangeListener;
import com.ipanel.join.chongqing.live.view.ListViewListenerSet.ListOnEnterListener;
import com.ipanel.join.cq.vod.jsondata.GlobalFilmData;
import com.ipanel.join.cq.vod.player.SimplePlayerActivity;
import com.ipanel.join.cq.vod.utils.Tools;
import com.ipanel.join.protocol.a7.ServiceHelper;
import com.ipanel.join.protocol.a7.ServiceHelper.ResponseHandlerT;
import com.ipanel.join.protocol.a7.ServiceHelper.SerializerType;

public class EPGListFragment extends BaseFragment {
	
	JEventListView_b event_list;
	JWeekListView week_list;
	LiveChannel channel;
	
	int current_focus = -1;
	String replayName = "";
	private ServiceHelper serviceHelper;
	private CQApplication mAppInstance;

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup root) {
		// TODO Auto-generated method stub
		ViewGroup container = (ViewGroup) inflater.inflate(R.layout.live_fragment_program, root, false);
		event_list = (JEventListView_b) container.findViewById(R.id.program_list_02);
		week_list = (JWeekListView) container.findViewById(R.id.week_list);
		week_list.setListFocusChangeListener(new ListFocusChangeListener() {
			
			@Override
			public void onFocusChange(int focus) {
				// TODO Auto-generated method stub
				requestEPGData(channel, focus - 3, true);
			}
		});
		event_list.setEnteristener(new ListOnEnterListener() {
			
			@Override
			public void onEnter(int focus) {
				// TODO Auto-generated method stub
				LiveProgramEvent event = event_list.shows.get(focus);
				Date date = new Date();
				long l1 = event.getStart();
				long l2 = event.getEnd();
				long l3 = date.getTime();
				if(l3 >= l1 && l3 <l2){ //正在播放
					getLiveActivity().showMessage(R.string.channel_playing);
					return ;
				}
				if(l3 < l1){
					boolean isbooked = getLiveActivity().getBookManager().isProgramBooked(event.getStart(), event.getChannelKey().getProgram());
					if(isbooked){
						if(getLiveActivity().getBookManager().deleteBook(event.getStart(), event.getChannelKey().getProgram())){
							Alarms.setNextAlert(getLiveActivity());// 重置预订提醒
							event_list.reSetBookFlag();
						}else{
							getLiveActivity().showMessage(R.string.delete_book_fail);
						}
					}else{
						BookData confitData = getLiveActivity().getBookManager().hasConflictBook(event.getStart());
						
						if(confitData == null){
							if(getLiveActivity().getBookManager().addBook(channel, event)){
								Alarms.setNextAlert(getLiveActivity());// 重置预订提醒
								event_list.reSetBookFlag();
							}else{
								getLiveActivity().showMessage(R.string.book_fail);
							}
						}else{
							showBookConfitDialog(confitData,event);
						}
					}
				} else {
					//跳转至回看
					replayName = event.getName();
					requestDetailURL(event.getJumpUrl());
				}
			}
		});
		
		serviceHelper = ServiceHelper.getHelper();
		mAppInstance = CQApplication.getInstance();
		return container;
	}
	
	/**
	 * 请求具体的播放地址
	 * */
	private void requestDetailURL(String url) {
		
		LogHelper.i("requestDetailURL");
		url = GlobalFilmData.getInstance().getEpgUrl() + "/defaultHD/en/" + url;
		serviceHelper.setRootUrl(url);
		serviceHelper.setSerializerType(SerializerType.TEXT);
		serviceHelper.setHeaders(new Header[] { new BasicHeader("Cookie", mAppInstance.getCookieString()) });
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
					startToBackPlay(playurl);
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
		intent.putExtra("playType","4");
		startActivity(intent);
	}
	
	private void showBookConfitDialog(final BookData conflict, final LiveProgramEvent data) {
		// TODO Auto-generated method stub
		final Dialog dialog = new Dialog(getLiveActivity(), R.style.dialog);
		View view = LayoutInflater.from(getLiveActivity()).inflate(
				R.layout.live_conflict_dia, null);
		final Button ok = (Button) view.findViewById(R.id.ensureButton);

		final Button cancel = (Button) view.findViewById(R.id.cancelButton);
		TextView tv = (TextView) view.findViewById(R.id.showinfo);
//	    String html="<p> 该时间段内您已预订：【<b> {0} - {1} </b>】，是否替换？</p>";
//		String info=MessageFormat.format(html,conflict.getChannel_name(),conflict.getEvent_name());
		String info = getResources().getString(R.string.conflict_info);
		String txt_info = String.format(info, conflict.getChannel_name()+"-"+conflict.getEvent_name());
		tv.setText(txt_info);
		
		ok.setFocusable(true);
		ok.requestFocus();
		
		ok.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				boolean b1 = getLiveActivity().getBookManager().deleteBook(Long.parseLong(conflict.getStart_time()),
						conflict.getProgram_number());
				if(b1){
					boolean b2 = getLiveActivity().getBookManager().addBook(channel, data);
					if(b2){
						event_list.reSetBookFlag();
						getLiveActivity().showMessage(R.string.book_success);
						Alarms.setNextAlert(getLiveActivity());// 重置预订提醒
					}else{
						event_list.reSetBookFlag();
						getLiveActivity().showMessage(R.string.book_fail);
						Alarms.setNextAlert(getLiveActivity());// 重置预订提醒
					}
				}else{
					getLiveActivity().showMessage(R.string.delete_book_fail);
				}
				
				dialog.dismiss();
			}
		});
		
		cancel.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				dialog.dismiss();
			}
		});
		
		dialog.setContentView(view);
		android.view.WindowManager.LayoutParams lay = dialog.getWindow().getAttributes();
		lay.gravity = Gravity.CENTER;
		
		dialog.show();
		return;
	}

	@Override
	public void onShow() {
		// TODO Auto-generated method stub
		channel = (LiveChannel) getValue();
		event_list.clearAnim();
		week_list.clearAnim();
		week_list.setHasFocus(false);
		event_list.setHasFocus(true);
		event_list.calCurrent();
		
		requestEPGData(channel, 0, true);
		week_list.setShow();
		week_list.setCurrentIndexAndSelector(3);
		
		event_list.animIn();
		event_list.setVisibility(View.VISIBLE);
		week_list.animIn();
		week_list.setVisibility(View.VISIBLE);
		
		if (event_list.isHasFocus()) {
			week_list.blur();
			event_list.focus();
			current_focus = 1;
		} else {
			current_focus = 2;
			event_list.blur();
			week_list.focus();
		}
	}
	
	private void requestEPGData(LiveChannel ch, int offset, boolean again) {
		List<LiveProgramEvent> result = getLiveActivity().getDataManager().getShowProgramlist(ch, offset, again);
		if (result != null && result.size()>0) {
			LogHelper.i("requestEPGData result size is " + result.size());
			List<LiveProgramEvent> datas = new ArrayList<LiveProgramEvent>();
			for(LiveProgramEvent event:result){
				long cur_time = System.currentTimeMillis();
				Calendar mCalendar = Calendar.getInstance();
				mCalendar.setTime(new Date(cur_time));
				mCalendar.add(Calendar.DAY_OF_YEAR, offset);
				mCalendar.set(Calendar.HOUR_OF_DAY, 0);
				mCalendar.set(Calendar.MINUTE, 0);
				mCalendar.set(Calendar.SECOND, 0);
				long start = mCalendar.getTimeInMillis();
				long end = mCalendar.getTimeInMillis() + 24 * 60 * 60 * 1000;
				if(event.getStart() < start && event.getEnd() > start){
					datas.add(event);
				}
				if(event.getStart() >= start && event.getStart() <= end){
					datas.add(event);
				}
			}
			if(datas.size() != 0){
				event_list.setVisibility(View.INVISIBLE);
				event_list.setShow(datas, 9, ch.getName());
				event_list.calCurrent();
				event_list.setVisibility(View.VISIBLE);
			} else {
				event_list.clearData();
				event_list.setVisibility(View.INVISIBLE);
			}
		} else {
			LogHelper.i("requestEPGData result is null");
			event_list.clearData();
			event_list.setVisibility(View.INVISIBLE);
		}
	}

	@Override
	public void onRefresh() {
		// TODO Auto-generated method stub

	}

	@Override
	public void onHide() {
		// TODO Auto-generated method stub

	}

	@Override
	public void onDataChange(int type, Object data) {
		// TODO Auto-generated method stub
		ChannelKey key = (ChannelKey) data;
		switch (type) {
		case Constant.DATA_CHANGE_OF_EPG_WITH_TIME:
			requestEPGData(channel, week_list.getCurrentIndex() - 3, false);
			break;
		case Constant.DATA_CHANGE_OF_EPG_EVENT:
			if (key != null && channel.getChannelKey().getProgram() == key.getProgram())
				requestEPGData(channel, week_list.getCurrentIndex() - 3, false);
			break;
		default:
			break;
		}
	}
	
	@Override
	public boolean onKeyDown(int keyCode, KeyEvent event) {
		// TODO Auto-generated method stub
		switch (keyCode) {
		case KeyEvent.KEYCODE_DPAD_LEFT:
			if (current_focus == 1) {
				if (event_list.getCurrentState() != JListView.STATE_OF_OUT_ANIMING) {
					getLiveActivity().getUIManager().showUI(UIManager.ID_UI_CQ_LIVE_CHANNEL_LIST, null);
				}
			} else if (current_focus == 2) {
				if (event_list.getCurrentState() == JListView.STATE_OF_VISIBLE
						|| event_list.getCurrentState() == JListView.STATE_OF_NORMAL) {
					current_focus = 1;
					event_list.setHasFocus(true);
					event_list.focus();
					week_list.setHasFocus(false);
					week_list.blur();
					event_list.invalidateAll();
					week_list.invalidateAll();
				}
			}
			return true;
		case KeyEvent.KEYCODE_DPAD_RIGHT:
			if (current_focus == 1) {
				if (event_list.getCurrentState() == JListView.STATE_OF_VISIBLE
						|| event_list.getCurrentState() == JListView.STATE_OF_NORMAL) {
					current_focus = 2;
					event_list.setHasFocus(false);
					event_list.blur();
					week_list.setHasFocus(true);
					week_list.focus();
					event_list.invalidateAll();
					week_list.invalidateAll();
				}
			} else if (current_focus == 2) {
				if (week_list.getCurrentState() != JListView.STATE_OF_IN_ANIMING) {
					
				}
			}
			return true;
		}
		return super.onKeyDown(keyCode, event);
	}

}
