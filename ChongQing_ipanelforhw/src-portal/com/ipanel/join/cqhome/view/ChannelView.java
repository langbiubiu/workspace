package com.ipanel.join.cqhome.view;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.List;

import ipanel.join.configuration.Bind;
import ipanel.join.configuration.ConfigState;
import ipanel.join.configuration.View;
import ipanel.join.widget.AbsLayout;
import ipanel.join.widget.FrameLayout;
import ipanel.join.widget.TxtView;

import org.json.JSONException;
import org.json.JSONObject;

import com.ipanel.chongqing_ipanelforhw.CQApplication;
import com.ipanel.chongqing_ipanelforhw.R;
import com.ipanel.chongqing_ipanelforhw.hwstruct.GetHwResponse;
import com.ipanel.join.chongqing.live.Constant;
import com.ipanel.join.cq.vod.utils.BlurBitmap;
import com.squareup.otto.Subscribe;

import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.drawable.BitmapDrawable;
import android.net.Uri;
import android.text.TextUtils;
import android.text.TextUtils.TruncateAt;
import android.util.Log;
import android.widget.ImageView;
import android.widget.TextView;
import cn.ipanel.android.LogHelper;
import cn.ipanel.android.net.imgcache.BaseImageFetchTask;
import cn.ipanel.android.net.imgcache.ImageFetchListener;
import cn.ipanel.android.net.imgcache.ImageFetchTask.TaskType;
import cn.ipanel.android.otto.OttoUtils;

public class ChannelView extends AbsLayout {
	/**
	 * XML中配置的属性
	 * */
	public static final String PROP_CHANNEL_DATA = "channel";
	/**
	 * 频道名
	 * */
	public static final String CHANNEL_DATA_NAME = "name";
	/**
	 * 频率
	 * */
	public static final String CHANNEL_DATA_FREQUENCE = "freq";
	/**
	 * ServiceID
	 * */
	public static final String CHANNEL_DATA_PROGRAAM = "prog";
	/**
	 * 频道类型：用户自定义频道一
	 * */
	public static final String CHANNEL_TYPE_CONFIG_ONE = "_one";
	/**
	 * 频道类型：用户自定义频道二
	 * */
	public static final String CHANNEL_TYPE_CONFIG_TWO = "_two";
	/**
	 * 频道类型：用户历史频道
	 * */
	public static final String CHANNEL_TYPE_HISTORY = "_history";
	/**
	 * 频道类型：本地频道
	 * */
	public static final String CHANNEL_TYPE_LOCAL = "_local";

	private String name;
	private String freq;
	private String prog;
	private int type = -1;
	public GetHwResponse.Program program;
	ImageView imgPoster;
	TextView txtProgram,pf_bgtv,pf_bg;
	TextView txtChannel;
	TextView txtProgress;

	public ChannelView(Context context, View data) {
		super(context, data);
		OttoUtils.getBus().register(this);
		this.setFocusable(true);
		this.setShowFocusFrame(true);
		Bind bind = data.getBindByName(PROP_CHANNEL_DATA);
		if (bind != null) {
			try {
				JSONObject jobj = bind.getValue().getJsonValue();
				if (CHANNEL_TYPE_CONFIG_ONE.equals(jobj.opt(CHANNEL_DATA_NAME))) {
					type = 0;
//					loadDataFromShare(type);
				} else if (CHANNEL_TYPE_CONFIG_TWO.equals(jobj
						.opt(CHANNEL_DATA_NAME))) {
					type = 1;
//					loadDataFromShare(type);
				} else if (CHANNEL_TYPE_HISTORY.equals(jobj.opt("CHANNEL_DATA_NAME"))){
//					loadDataFromProvider();
				}
			} catch (JSONException e) {
				e.printStackTrace();
			}
		}
	}

//	private void loadDataFromProvider() {
//		try {
//			Uri uri = Uri.parse("content://ipaneltv.chongqing.live/history");
//			Cursor cursor = getContext().getContentResolver().query(uri, null,
//					null, null, null);
//			while (cursor != null && cursor.moveToNext()) {
//				name = cursor.getString(0);
//				freq = cursor.getString(2);
//				prog = cursor.getString(3);
//				break;
//			}
//			if (cursor != null) {
//				cursor.close();
//			}
//			LogHelper.i("history chyannel ", name + "");
//			if (TextUtils.isEmpty(name) || "null".equals(name)
//					|| "".equals(name) || name.length() < 2) {
//				LogHelper.i("history is null ");
//				name = getResources().getString(R.string.channel_1);
//				LogHelper.i("load from default channel for history channel  :", name);
//				freq = "427000000";
//				prog = "1104";
//			}
//		} catch (Exception e) {
//			e.printStackTrace();
//		}
//	}

//	private void loadDataFromShare(int type) {
//		SharedPreferences share = getContext().getSharedPreferences(
//				"user_channel", Context.MODE_PRIVATE);
//		switch (type) {
//		case 0:
//			name = share.getString("ch_nm1",
//					getResources().getString(R.string.channel_1));
//			LogHelper.i("load from share :", name);
//			freq = share.getString("ch_freq1", "427000000");
//			prog = share.getString("ch_pnum1", "1104");
//			break;
//		case 1:
//			name = share.getString("ch_nm2",
//					getResources().getString(R.string.channel_2));
//			freq = share.getString("ch_freq2", "435000000");
//			prog = share.getString("ch_pnum2", "1202");
//			break;
//		case 2:
//			name = share.getString("ch_nm3",
//					getResources().getString(R.string.channel_3));
//			freq = share.getString("ch_freq3", "307000000");
//			prog = share.getString("ch_pnum3", "2402");
//			break;
//		default:
//			break;
//		}
//	}

	public void caculateShow(GetHwResponse.Program program) {
		if(program == null)
			return;
		this.program = program;
		Log.i("caculateShow", "channelName:" + program.getChannelName());
		Log.i("caculateShow", "programName:" + program.getTitle());
		Log.i("caculateShow", "postUrl:" + program.getCover());
		freq = program.getFrequency();
		prog = program.getServiceId();
		Log.i("caculateShow", "freq:" + freq);
		Log.i("caculateShow", "prog:" + prog);
		imgPoster = (ImageView) getChildAt(0);
		txtProgress = (TextView) getChildAt(2);
		pf_bg = (TextView) getChildAt(3);//作为高斯模糊处理控件
		pf_bgtv = (TxtView) this.getChildAt(4);
		txtProgram = (TextView) getChildAt(5);
		txtChannel = (TextView) getChildAt(6);
		if(imgPoster != null){
			imgPoster.setDrawingCacheEnabled(true);
			BaseImageFetchTask ivdtask = ConfigState.getInstance()
					.getImageFetcher(getContext()).getBaseTask(program.getCover());
			ivdtask.setTaskType(TaskType.IMAGE);
			ivdtask.setListener(new ImageFetchListener() {
				
				@Override
				public void OnComplete(int status) {
					Log.i("ChannelView", "fetch OnComplete");
					if(imgPoster.getDrawingCache() != null)
						BlurBitmap.blur(imgPoster.getDrawingCache(), pf_bg,8);
				}
			});
			ConfigState.getInstance().getImageFetcher(getContext())
					.loadImage(ivdtask, imgPoster);
		}
		if(txtProgress != null){
			SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd hh:mm:ss");
			int progress = 0;
			txtProgress.setBackgroundColor(Color.parseColor("#74F118"));
			android.view.ViewGroup.LayoutParams params = txtProgress.getLayoutParams();
			try {
				long starttime = format.parse(program.getStartTime()).getTime();
				long endtime = format.parse(program.getEndTime()).getTime();
				long currenttime = System.currentTimeMillis();
				progress = (int) (100.0f * (currenttime - starttime) / (endtime - starttime));
			} catch (ParseException e) {
				e.printStackTrace();
			}
			params.width = (int) (375 * progress / 100.0f);
			txtProgress.setLayoutParams(params);
		}
		
		if(txtChannel != null){
			txtChannel.setText(program.getChannelName());
		}
		
		if(txtProgram != null){
			txtProgram.setSingleLine(true);
			txtProgram.setHorizontallyScrolling(true);
			txtProgram.setMarqueeRepeatLimit(6);
			txtProgram.setEllipsize(TruncateAt.MARQUEE);
			txtProgram.setText(program.getTitle());
		}
		if(pf_bgtv != null)
			pf_bgtv.setBackgroundColor(Color.parseColor("#80ffffff"));
//		if(pf_bg != null){
//			pf_bg.post(new Runnable() {
//				
//				@Override
//				public void run() {
//					BitmapDrawable bd = (BitmapDrawable) imgPoster.getDrawable();
//					Bitmap bmp = bd.getBitmap();
//					BlurBitmap.blur(bmp, pf_bg , 8); 
//				}
//			});
//		}
		
		setOnFocusChangeListener(new OnFocusChangeListener() {
			
			@Override
			public void onFocusChange(android.view.View arg0, boolean arg1) {
				if(txtProgram == null)
					return;
				if(arg1){
					txtProgram.setSelected(true);
				}else{
					txtProgram.setSelected(false);
				}
			}
		});
		setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(android.view.View v) {
				try {
					Log.d("ChannelView", "freq:" + freq + "-prog:" + prog);
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

	@Override
	protected void onAttachedToWindow() {
		super.onAttachedToWindow();
		if(CQApplication.getInstance().hwRecResponseLive != null)
			caculateShow(CQApplication.getInstance().hwRecResponseLive.getPrograms().get(type));
	}
	
	@Subscribe
	public void onPortalLiveHwResponse(GetHwResponse resp){
		if(resp != null && resp.getPrograms() != null && resp.getPrograms().size() > 0){
			program = resp.getPrograms().get(type);
			caculateShow(program);
		}
	}

	@Override
	protected void onDetachedFromWindow() {
		super.onDetachedFromWindow();
//		OttoUtils.getBus().unregister(this);
	}
}
