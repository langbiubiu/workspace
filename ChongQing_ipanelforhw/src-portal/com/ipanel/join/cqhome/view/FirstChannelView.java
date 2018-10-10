package com.ipanel.join.cqhome.view;


import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

import com.ipanel.chongqing_ipanelforhw.R;
import com.ipanel.chongqing_ipanelforhw.hwstruct.GetHwRequest;
import com.ipanel.chongqing_ipanelforhw.hwstruct.GetHwResponse;
import com.ipanel.chongqing_ipanelforhw.hwstruct.GetHwResponse.Channel;
import com.ipanel.join.chongqing.live.Constant;
import com.ipanel.join.chongqing.live.SharedPreferencesMenager;
import com.ipanel.join.cq.vod.HWDataManager;
import com.ipanel.join.cq.vod.utils.BlurBitmap;
import com.ipanel.join.protocol.a7.ServiceHelper;
import com.ipanel.join.protocol.a7.ServiceHelper.ResponseHandlerT;
import com.ipanel.join.protocol.a7.ServiceHelper.SerializerType;

import cn.ipanel.android.net.imgcache.BaseImageFetchTask;
import cn.ipanel.android.net.imgcache.ImageFetcher;
import cn.ipanel.android.net.imgcache.ImageFetchTask.TaskType;
import android.annotation.SuppressLint;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.drawable.BitmapDrawable;
import android.os.Handler;
import android.os.Message;
import android.text.TextUtils;
import android.text.TextUtils.TruncateAt;
import android.util.Log;
import android.view.View.OnFocusChangeListener;
import android.widget.ImageView;
import android.widget.TextView;
import ipanel.join.configuration.ConfigState;
import ipanel.join.configuration.View;
import ipanel.join.widget.AbsLayout;
import ipanel.join.widget.ImgView;
import ipanel.join.widget.TxtView;

public class FirstChannelView extends AbsLayout{
	private static final String TAG = FirstChannelView.class.getSimpleName();
    public static final String RECEIVE_DATA = "com.ipanel.join.channel.update";
    private static final String CQ_URL = "asset:///config-new-1080P/chaLogo_cqws.png";
    private  String default_url = CQ_URL;
    private static final int  RECEIVER_BROADCAST=1;
    private FirstChannelViewReceiver mReceiver;
    private ImageFetcher mFetcher;
    private Handler handler;
    private ImgView channel_logo;
    private TxtView channel_name,program_name,pf_bgtv;
    TextView txtProgress;
    ImageView progressBg,pf_bg;
    private int prog;
    private long freq;
    private String channellogo_url = "";
    private List<Channel> channels;
	public FirstChannelView(Context ctx, View data) {
		super(ctx, data);
		setId(R.id.last_channel);
		handler=new MyHnadler();
		mFetcher = ConfigState.getInstance().getImageFetcher(ctx);
		mReceiver=new FirstChannelViewReceiver();
	}
  
	@Override
	protected void onAttachedToWindow() {
		try {
			channel_logo = (ImgView) this.getChildAt(0);
			progressBg = (ImgView) this.getChildAt(1);
			txtProgress = (TextView) getChildAt(2);
			pf_bg = (ImageView) getChildAt(3);//作为高斯模糊处理控件
			pf_bgtv = (TxtView) this.getChildAt(4);
			program_name = (TxtView) this.getChildAt(5);
			channel_name = (TxtView) this.getChildAt(6);
		} catch (Exception e) {
			e.printStackTrace();
		}
		if(program_name != null){
			program_name.setSingleLine(true);
			program_name.setHorizontallyScrolling(true);
			program_name.setMarqueeRepeatLimit(6);
			program_name.setEllipsize(TruncateAt.MARQUEE);
		}
		setOnFocusChangeListener(new OnFocusChangeListener() {
			
			@Override
			public void onFocusChange(android.view.View arg0, boolean arg1) {
				if(program_name == null)
					return;
				if(arg1){
					program_name.setSelected(true);
				}else{
					program_name.setSelected(false);
				}
			}
		});
		if(pf_bgtv != null)
			pf_bgtv.setBackgroundColor(Color.parseColor("#80ffffff"));
		if(pf_bg != null){
			pf_bg.post(new Runnable() {
				
				@Override
				public void run() {
					BitmapDrawable bd = (BitmapDrawable) getBackground();
					Bitmap bmp = bd.getBitmap();
					BlurBitmap.blur(bmp, pf_bg , 8); 
				}
			});
		}
		updateFirstChannel();
//		SharedPreferences mSharePre=this.getContext().getSharedPreferences("user_channel", Context.MODE_PRIVATE);
//		String picUrl=mSharePre.getString("ch_url1", null);
//		String channenName=mSharePre.getString("ch_nm1", null);
//		String freq=mSharePre.getString("ch_freq1", null);
//		String ch_pnum=mSharePre.getString("ch_pnum1", null);
//		
//		if (picUrl==null&&channenName==null&&freq==null&&ch_pnum==null) {
//			picUrl=CQ_URL;
//			channenName=getResources().getString(R.string.channel_1);
//			freq="427000000";
//			ch_pnum="1104";
//			Editor e=mSharePre.edit();
//			e.putString("ch_url1", picUrl);
//			e.putString("ch_nm1", channenName);
//			e.putString("ch_freq1", freq);
//			e.putString("ch_pnum1", ch_pnum);
//			e.commit();
//
//		}else{
//			if (default_url.equals(picUrl)) {
//				BaseImageFetchTask ivdtask = mFetcher.getBaseTask(picUrl);
//				ivdtask.setTaskType(TaskType.IMAGE);
//				mFetcher.loadImage(ivdtask, iv);
//				tv.setText(channenName);
//			}else{
//				BaseImageFetchTask ivdtask = mFetcher.getBaseTask(picUrl);
//				ivdtask.setTaskType(TaskType.IMAGE);
//				mFetcher.loadImage(ivdtask, iv);
//			}
//		}
//		setOnClick(freq,ch_pnum);
		IntentFilter mIn=new IntentFilter(RECEIVE_DATA);
		getContext().registerReceiver(mReceiver, mIn);
		super.onAttachedToWindow();
	}

	private void updateFirstChannel() {
		if(progressBg != null)
			progressBg.setVisibility(android.view.View.INVISIBLE);
		if(txtProgress != null)
			txtProgress.setVisibility(android.view.View.INVISIBLE);
		SharedPreferencesMenager manager = SharedPreferencesMenager.getInstance(getContext());
		prog = manager.getSaveProg();
		freq = manager.getSaveFreq();
		String channelName = manager.getSaveChannelName();
//		channellogo_url = manager.getValueString("logo_url");
//		channellogo_url = ChannelTools.getChannelUrl(channelName, getContext());//本地台标
		Log.d(TAG,"channel_name:" + channelName);
		Log.d(TAG,"channellogo_url:" + channellogo_url);
		if(channel_name != null && channelName != null)
			channel_name.setText(channelName);
		setOnClick(freq + "",prog + "");
		if(channels != null){
			for(Channel channel:channels){
				if(channel != null && (prog+"").equals(channel.getServiceId())){
					channellogo_url = channel.getLogo();
				}
			}
		}
		if(channel_logo != null && channellogo_url!= null && !channellogo_url.equals(""))
			mFetcher.loadImage(channellogo_url, channel_logo);
		
		getFirstChannelFromNetwork(prog+"");//从网络获取pf相关信息
	}
	
	@Override
	protected void onDetachedFromWindow() {
		getContext().unregisterReceiver(mReceiver);
		super.onDetachedFromWindow();
	}
	
	@SuppressLint("HandlerLeak")
	private class MyHnadler extends Handler{
		@Override
		public void handleMessage(Message msg) {
			super.handleMessage(msg);
			switch (msg.what) {
			case RECEIVER_BROADCAST:
				updateFirstChannel();
				break;
			default:
				break;
			}
		}
	} 
	
	private class FirstChannelViewReceiver extends BroadcastReceiver{
        
		@Override
		public void onReceive(Context context, Intent intent) {
			if (intent != null && intent.getAction().equals(RECEIVE_DATA)) {
				Log.d(TAG, "FirstChannelViewReceiver receive the BroadcastReceiver:" + intent.getAction());
				handler.sendEmptyMessage(RECEIVER_BROADCAST);
			}
		}
	}
	
  private void setOnClick(final String freq,final String prog){
	  
	  FirstChannelView.this.setOnClickListener(new OnClickListener() {
		
		@Override
		public void onClick(android.view.View v) {
			 Log.d(TAG, "freq:" + freq);
			 Log.d(TAG, "prog:" + prog);
			 try {
					ComponentName mCom = new ComponentName(
							"com.ipanel.chongqing_ipanelforhw",
							"com.ipanel.join.chongqing.live.LiveForHWActivity");
					Intent i = new Intent();
					i.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
					i.putExtra(Constant.LIVE_LAUNCH_TAG, 1);
//					i.putExtra("live_activity_freq", freq);
					i.putExtra(Constant.LIVE_LAUNCH_PROGRAM_TAG, prog);
					i.setComponent(mCom);
					getContext().startActivity(i);
				} catch (Exception e) {
					e.printStackTrace();
			}
		}
	});
  }  
  
  public void getFirstChannelFromNetwork(final String serviceId){
		GetHwRequest request = HWDataManager.getHwRequest();
		request.setAction(HWDataManager.ACTION_GET_CHANNELS);
		request.getParam().setShowlive(true);
		request.getParam().setOrder(0);
		request.getParam().setPage(1);
		request.getParam().setPagesize(200);
		ServiceHelper serviceHelper = ServiceHelper.getHelper();
		serviceHelper.setRootUrl(HWDataManager.ROOT_URL);
		serviceHelper.setSerializerType(SerializerType.JSON);
		serviceHelper.callServiceAsync(getContext(), request, GetHwResponse.class, new ResponseHandlerT<GetHwResponse>(){

			@Override
			public void onResponse(boolean success, GetHwResponse result) {
				if (!success) {
					Log.i(TAG, "request channels data failed");
					return;
				}
				if (result == null) {
					Log.i(TAG, "failed to parse JSON data");
					return;
				}
				if(result.getError().getCode() == 0){
					List<Channel> mChannels = result.getChannels();
					if(channels == null){
						channels = mChannels;
					}
					for(Channel channel:mChannels){
						if(channel != null && serviceId.equals(channel.getServiceId())){
							updateFromNetwork(channel);
						}
					}
				}
			}
		});
	}
  //如果网络获取到频道相关信息，则更新网络信息
  private void updateFromNetwork(Channel channel){
//	  SimpleDateFormat format = new SimpleDateFormat("HH:mm");
	  SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd hh:mm:ss");
	  String time = format.format(new Date());
	  if(channellogo_url == null || channellogo_url.equals("")){
		  channellogo_url = channel.getLogo();
		  mFetcher.loadImage(channellogo_url, channel_logo);
	  }
	  if(program_name != null)
		  program_name.setText(channel.getCurName());
	  if(channel_name != null)
		  channel_name.setText(channel.getName());
	  if(txtProgress != null){
		  int progress = 0;
		  txtProgress.setBackgroundColor(Color.parseColor("#74F118"));
		  android.view.ViewGroup.LayoutParams params = txtProgress.getLayoutParams();
		  try {
			  	String stime = time.substring(0, 11) + channel.getStartTime() + ":00";
			  	String etime = time.substring(0, 11) + channel.getEndTime() + ":00";
				long starttime = format.parse(stime).getTime();
				long endtime = format.parse(etime).getTime();
				long currenttime = System.currentTimeMillis();
				progress = (int) (100.0f * (currenttime - starttime) / (endtime - starttime));
				params.width = (int) (375 * progress / 100.0f);
				txtProgress.setLayoutParams(params);
				progressBg.setVisibility(android.view.View.VISIBLE);
				txtProgress.setVisibility(android.view.View.VISIBLE);
			  } catch (ParseException e) {
				e.printStackTrace();
		  }
	  }
  }
}
