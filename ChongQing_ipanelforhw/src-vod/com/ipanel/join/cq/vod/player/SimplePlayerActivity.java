package com.ipanel.join.cq.vod.player;

import android.app.Activity;
import android.app.Fragment;
import android.app.FragmentTransaction;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.DialogInterface;
import android.content.DialogInterface.OnKeyListener;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.IBinder;
import android.text.TextUtils;
import android.util.Log;
import android.view.KeyEvent;
import android.view.RcKeyEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.widget.ImageView;
import cn.ipanel.android.LogHelper;
import cn.ipanel.android.net.cache.JSONApiHelper;

import com.google.gson.Gson;
import com.ipanel.chongqing_ipanelforhw.Config;
import com.ipanel.chongqing_ipanelforhw.R;
import com.ipanel.chongqing_ipanelforhw.hwstruct.GetHwResponse;
import com.ipanel.chongqing_ipanelforhw.hwstruct.GetHwResponse.Wiki;
import com.ipanel.join.cq.settings.aidl.IDataSet;
import com.ipanel.join.cq.vod.player.VodDataManager.ResponseCallBack;
import com.ipanel.join.cq.vod.player.impl.PlayInterface;
import com.ipanel.join.cq.vod.player.impl.VodFragment;
import com.ipanel.join.cq.vod.player.impl.WidgetFragment;
import com.ipanel.join.cq.vod.utils.GlobalContext;
import com.ipanel.join.cq.vod.utils.Logger;
import com.ipanel.join.cq.vod.utils.PromptFragment;
import com.ipanel.join.cq.vod.utils.TipDialog;
import com.ipanel.join.cq.vod.utils.TipDialog.TipDialogListener;
import com.ipanel.join.cq.vod.utils.Tools;
import com.ipanel.join.cq.vod.vodhome.MainMenuDialog;
import com.ipanel.join.cq.vod.vodhome.MainMenuDialog.MainDialogGetDataListener;
import com.ipanel.join.protocol.a7.LogcatUtils;
import com.ipanel.join.protocol.huawei.cqvod.RTSPResponse;

public class SimplePlayerActivity extends Activity {
	private static final String TAG = SimplePlayerActivity.class.getSimpleName();
	ViewGroup root;
	FragmentFactory mFragmentFactory;
	/**
	 * 解析华为IDaction
	 * */
	public static final String GIVE_HUAWEI_BROADCAST_ACTION = "com.ipanel.join.cq.xmpp.GIVE_HUAWEI_ID";
	/**
	 * 解析华为IDaction
	 * */
	public static final String GET_HUAWEI_BROADCAST_ACTION = "com.ipanel.join.cq.xmpp.GET_HUAWEI_ID";
	/**
	 * 网络是否正常
	 * */
	public boolean isOnLine = true;
	/**
	 * Activity是否在前台
	 * */
	public boolean threadRunFlag = false;
	/**
	 * 消息提示fragment
	 * */
	public PromptFragment prompt;
	/**
	 * 播放fragment
	 * */
	//public TVFragment tvFragment;
	
	public PlayInterface playIf;
	
	/**
	 * 暂停图标
	 * */
	private ImageView imgPause; // 屏幕中心暂停图标
	private ImageView play_speed_1,play_speed_2;
	private ImageView back_icon;//从直播进入回看，显示该icon
	
	/** 当前播放的电视剧的集数 */
	public String num = "1";
	/** 播放的rtsp地址 */
	private String url;
	/** 电影或电视剧的实体类 */
//	private MovieDetailResponse movieDetailResponse;
	private Gson gson;
//	private String jsonString;
	private String playType;
	private long historyTime = 0;
	private String name;//影片名称
	boolean isPushScreen = false;
	
	/** 是否播放完毕 */
	private int isOver = 0;
	private Intent intent;
	MainMenuDialog mainDialog;
	private boolean showBackIcon = false;
	private GetHwResponse hwResponse;//电视剧实体类
	private String wikiId;//电影的id，用于获取相似影片
	/**
	 * 解析华为ID
	 * */
	BroadcastReceiver mHuaweiReceiver = new BroadcastReceiver() {

		@Override
		public void onReceive(Context context, Intent intent) {
			LogHelper.i("receive a broadcast of sync screem");
			Intent i=new Intent(GIVE_HUAWEI_BROADCAST_ACTION);
			String id="";
//			if(movieDetailResponse==null){
//				id="";
//			}else{
//				if("0".equals(flag)){
//					id=movieDetailResponse.getVodId();
//				}else{
//					id=movieDetailResponse.getVodIdList().get(Integer.parseInt(num)-1);
//				}
//			}
			i.putExtra("id", id);
			i.putExtra("type", playType);
//			i.putExtra("name", movieDetailResponse==null?"":movieDetailResponse.getVodName());
			i.putExtra("num", num);
			i.putExtra("time", VodPlayerManager.getInstance(getBaseContext()).getElapsed()+"");
			sendBroadcast(i);
		}
	};
	
//	ProgressBar progressBar;
//	TextView tvTip;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		requestWindowFeature(Window.FEATURE_NO_TITLE);
		getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
				WindowManager.LayoutParams.FLAG_FULLSCREEN);
		threadRunFlag = true;
		this.setContentView(R.layout.vod_activity_simpleplayer);
		root = (ViewGroup) this.findViewById(R.id.root_view);
		imgPause = (ImageView) findViewById(R.id.img_pause);
		play_speed_1 = (ImageView) findViewById(R.id.play_speed_1);
		play_speed_2 = (ImageView) findViewById(R.id.play_speed_2);
		back_icon = (ImageView)findViewById(R.id.back_play_icon);
//		GlobalContext.init(this);
		loadFragments();
		mFragmentFactory = new FragmentFactory(root);
		VodPlayerManager.getInstance(getBaseContext()).setPlayCallBack(mPlayControlCallBack);
		VodDataManager.getInstance(getBaseContext()).setResponseCallBack(responseCallBack);//获取应答回调
		bindService(new Intent("com.ipanel.join.cq.settings.IDataSetService"), mIDataSetConnection, Context.BIND_AUTO_CREATE);
	
		registerReceiver(mHuaweiReceiver, new IntentFilter(GET_HUAWEI_BROADCAST_ACTION));
		IntentFilter mIf1 = new IntentFilter();
		mIf1.addAction("android.net.conn.CONNECTIVITY_CHANGE");
		registerReceiver(mNetworkReceiver, mIf1);
	}
	@Override
	protected void onResume() {
		super.onResume();
		threadRunFlag = true;
		getExtraData();
		mFragmentFactory.showFragment(FragmentFactory.FRAGMENT_ID_LOADING,
				null);
		
	};
	
	public void startPreparePlay(){
		LogHelper.i("startPreparePlay");
		playIf = VodPlayerManager.getInstance(this).preparePlayer(SimplePlayerActivity.this,vodF, url,historyTime);
	} 
	@Override
	protected void onPause() {
		super.onPause();
		VodPlayerManager.getInstance(SimplePlayerActivity.this).setDrag_factor(0);
//		finish();
		VodPlayerManager.getInstance(SimplePlayerActivity.this).destroyPlayer();
	};
	
	
	PlayCallBack mPlayControlCallBack = new PlayCallBack() {
		private String tag = "mPlayControlCallBack";
		@Override
		public void onPrepareStart() {
			Log.i(tag, "onPrepareStart");
			resetTVFragment();
		}

		@Override
		public void onPrepareSuccess() {
			BaseFragment nav = mFragmentFactory.getCurrentFragment();
			if (nav != null) {
				Log.i(tag, "onPrepareSuccess");
				nav.onDataChange(Constant.DATA_CHANGE_PREPARE_SUCCESS, null);
			}
			resetTVFragment();
		}

		@Override
		public void onMediaDestroy() {
			Log.i(tag, "onMediaDestroy");
			despathTVFragment();
		}

		@Override
		public void onPlayStateChange(int state) {
			Logger.d("play state change msg receive :" + state);
			switch (state) {
			case VodPlayerManager.PLAY_STATE_READY:
				mFragmentFactory.showFragment(
						FragmentFactory.FRAGMENT_ID_READY, null);
				break;
			case VodPlayerManager.PLAY_STATE_PLAYING:
			case VodPlayerManager.PLAY_STATE_PAUSE:
			case VodPlayerManager.PLAY_STATE_DRAG:
				mFragmentFactory.showFragment(FragmentFactory.FRAGMENT_ID_PLAY,
						null);
				break;
			}
			showByState();
			
		}

		@Override
		public void onPlayTick() {
			BaseFragment nav = mFragmentFactory.getCurrentFragment();
			if (nav != null) {
				nav.onDataChange(Constant.DATA_CHANGE_TIME_TICK, null);
			}
		}

		@Override
		public void onPlayEnd() {
			Logger.d("onPlayEnd()");
			VodPlayerManager.getInstance(SimplePlayerActivity.this).destroyPlayer();
			root.post(runnableEnd);
		}

		@Override
		public void onPlayFailed(String msg) {
			Log.i(tag, "onPlayFailed");
			if(TextUtils.isEmpty(msg)){
				msg=getResources().getString(R.string.preparefail);
			}
			prompt.showDialogAndPostDelayed(msg, new Runnable() {
				
				@Override
				public void run() {
					finish();
				}
			}, 1500);
		}

		@Override
		public void setVolume(Object value) {
			float volume = Float.parseFloat(value + "");
			Log.i(tag, "setVolume:"+volume);
			playIf.setVolume(volume);
		}

		@Override
		public String getVolomeKey() {
			return VodPlayerManager.DEFAULT_VOLUME_KEY;
		}

		@Override
		public void onMuteStateChange(boolean mute) {
			Log.i(tag, "onMuteStateChange:" + mute);
		}

		@Override
		public void onCleanCache() {
		}

		@Override
		public void fastReverseStart() {
			prompt.showDefaultDialog(R.string.fastreverseend);
		}

		@Override
		public void onPauseMedia() {
		}

		@Override
		public void onResumeMedia() {
		}

		@Override
		public void onNeedPlayMedia(String freq, String prog,int flag) {
			if (playIf!=null) {
				playIf.setProgramFlag(flag);
			}
		}

		@Override
		public void onSpeedChange(int speed) {
			playIf.setRate(speed);
		}
	};
	//选集and续播下一集
	ResponseCallBack responseCallBack = new ResponseCallBack() {
		
		@Override
		public void onResponse(boolean success, RTSPResponse result,String name,int num) {
			Log.i("ResponseCallBack", "success:"+success+",num"+num+",result:"+result.toString());
			if (success) {
				LogcatUtils.splitAndLog("wuhd",String.format("rtspResponse: %s name:%s", result.toString(),name));
				if(result.getPlayFlag().equals("1")){
					isOver = 0;
					Logger.d("start to prepare next episode");
					SimplePlayerActivity.this.num = num+"";
					SimplePlayerActivity.this.name = name + "";
					String url = result.getPlayUrl();
					SimplePlayerActivity.this.url = url.substring(url.indexOf("rtsp"), url.length());
					historyTime = 0;
					//获取应答实体
					Logger.d(String.format("url: %s num: %s historyTime: %d", url,num,historyTime));
					threadRunFlag = true;
					mFragmentFactory.showFragment(FragmentFactory.FRAGMENT_ID_LOADING,
							null);
				}else{
					SimplePlayerActivity.this.num = String.valueOf(Integer.parseInt(SimplePlayerActivity.this.num)-1);
					Tools.showToastMessage(SimplePlayerActivity.this, result.getMessage());
					finish();
					return;
				}
			}else{
				SimplePlayerActivity.this.num = String.valueOf(Integer.parseInt(SimplePlayerActivity.this.num)-1);
				Tools.showToastMessage(SimplePlayerActivity.this, getResources().getString(R.string.loaddataerror));
				finish();
			}
		}

		@Override
		public void onPlayDataReady(String url, String time, String flag, String num) {
			
		}
	};
	
	public void resetTVFragment() {
		Logger.d("resetTVFragment");
		getFragmentManager().beginTransaction().replace(R.id.fl_tvfragment, widget)
				.commitAllowingStateLoss();

	}
	public void despathTVFragment() {
		Logger.d("despathTVFragment");
		if (widget != null && widget.isAdded()) {
			getFragmentManager().beginTransaction().remove(widget).commit();
		}
	}
	/**
	 * 音量保存sercice
	 */
	private ServiceConnection mIDataSetConnection = new ServiceConnection() {
		@Override
		public void onServiceConnected(ComponentName className, IBinder service) {
			Logger.d(" volumeService   Connection");
			VodPlayerManager.getInstance(getBaseContext()).setIDataSet(
					IDataSet.Stub.asInterface(service));
		}

		@Override
		public void onServiceDisconnected(ComponentName className) {
			VodPlayerManager.getInstance(getBaseContext()).setIDataSet(null);
		}
	};
	
	WidgetFragment widget;
	VodFragment vodF;
	private void loadFragments() {
		prompt = PromptFragment.createFragemtn(root, R.layout.vod_toast_view, R.style.popwin_anim_style);
		widget = WidgetFragment.createInstance();
		vodF = VodFragment.createInstance(Config.PLAY_SERVICE_NAME, Config.SRC_SERVICE_NAME);
		Fragment f = getFragmentManager().findFragmentById(R.id.root_view);
		FragmentTransaction ft = getFragmentManager().beginTransaction();
		ft.add(vodF, "vod");
		ft.add(prompt, null);
		if (f != null) {
			ft.remove(f);
		}
		ft.commit();
		getFragmentManager().executePendingTransactions();
		
	}
	
	private void showByState() {
		int state = VodPlayerManager.getInstance(getBaseContext())
				.getPlayState();
		Log.i("showByState", "state:"+state);
		switch (state) {
		case VodPlayerManager.PLAY_STATE_PLAYING:
		case VodPlayerManager.PLAY_STATE_SPEED:
			imgPause.setVisibility(View.INVISIBLE);
			break;
		case VodPlayerManager.PLAY_STATE_PAUSE:
			imgPause.setVisibility(View.VISIBLE);
			break;
		}
		int []res=VodPlayerManager.getInstance(getBaseContext()).getCurrentSpeedImage();
		if(play_speed_1!=null){
			play_speed_1.setImageResource(res[0]);
		}
		if(play_speed_2!=null){
			play_speed_2.setImageResource(res[1]);
		}
	}
	/**
	 * name:影片名称
	 * url:播放地址
	 * playType：播放类型
	 * historyTime：历史播放时间
	 * 保留关键字切屏：isPushScreen
	 */
	
	private void getExtraData(){
		gson = new Gson();
		name = getIntent().getStringExtra("name");
		playType = getIntent().getStringExtra("playType");
		wikiId = getIntent().getStringExtra("wikiId");//电影的wikiId
		hwResponse = (GetHwResponse) getIntent().getSerializableExtra("hwResponse");//电视剧传入；用于电视剧的选集
		url = getIntent().getStringExtra("params");//播放地址
		historyTime =this.getIntent().getLongExtra("historyTime", 0);
		isPushScreen = getIntent().getBooleanExtra("isPush" , false);
		Logger.d(TAG, "name:"+name+",playType="+playType +",wiki="+gson.toJson(hwResponse));
		Logger.d(String.format("url: %s num: %s historyTime: %d", url,num,historyTime));
		if(TextUtils.isEmpty(num)){
			num = "1";
		}
		if(playType.equals("4")){
			showBackIcon = true;//回看
		}
		if(showBackIcon){
			back_icon.setVisibility(View.VISIBLE);
		}else{
			back_icon.setVisibility(View.INVISIBLE);
		}
	}
	
	
	@Override
	protected void onNewIntent(Intent intent) {
		super.onNewIntent(intent);
		LogHelper.e("onNewIntent");
		setIntent(intent);
	}
	@Override
	public boolean onKeyDown(int keyCode, KeyEvent event) {
//		keyCode = RcKeyEvent.getRcKeyCode(event);
		if (keyCode == RcKeyEvent.KEYCODE_SIGNAL_SOURCE) {
			return super.onKeyDown(keyCode, event);
		}
		BaseFragment current = mFragmentFactory.getCurrentFragment();
		if (current != null) {
			LogHelper.v("fragment =" + current.getClass().getName()
					+ "  key down :" + keyCode);
			if("PlayFragment".equals(current.getClass().getSimpleName())){
				if (keyCode==RcKeyEvent.KEYCODE_QUIT||keyCode == RcKeyEvent.KEYCODE_BACK) {
					onBackPressed();
					return true;
				}
			}
			if("PlayFragment".equals(current.getClass().getSimpleName())
					||"EmptyFragment".equals(current.getClass().getSimpleName())){
				//菜单键弹出TV+
				if(keyCode == RcKeyEvent.KEYCODE_MENU || keyCode== RcKeyEvent.KEYCODE_TV_ADD){
					mFragmentFactory.showFragment(FragmentFactory.FRAGMENT_ID_EMPTY,
							null);
					MainDialogGetDataListener mainDialogGetDataListener = new MainDialogGetDataListener() {
						@Override
						public void tvshift() {
							VodPlayerManager.getInstance(getBaseContext()).seekTo(0);
						}

						@Override
						public void showRecommend() {
							mFragmentFactory.showFragment(FragmentFactory.FRAGMENT_ID_RECOMMEND, null);
						}

						@Override
						public void anthology() {
							mFragmentFactory.showFragment(FragmentFactory.FRAGMENT_ID_Anthology, null);
						}
					};
					
					mainDialog = new MainMenuDialog(this, R.style.Dialog_Collect,
							mainDialogGetDataListener, getPlayType().equals("1"));
					mainDialog.show();
					return true;
				}
			}
			return current.onKeyDown(keyCode, event);
		} else {
			return super.onKeyDown(keyCode, event);
		}
	}
	@Override
	public boolean onKeyUp(int keyCode, KeyEvent event) {
//		keyCode = RcKeyEvent.getRcKeyCode(event);
		if (keyCode == KeyEvent.KEYCODE_VOLUME_DOWN
				|| keyCode == KeyEvent.KEYCODE_VOLUME_MUTE
				|| keyCode == KeyEvent.KEYCODE_VOLUME_UP
				|| keyCode == KeyEvent.KEYCODE_MUTE) {
			return super.onKeyUp(keyCode, event);
		}
		if (keyCode == RcKeyEvent.KEYCODE_SIGNAL_SOURCE) {
			return super.onKeyUp(keyCode, event);
		}
		BaseFragment current = mFragmentFactory.getCurrentFragment();
		if (current != null) {
			LogHelper.v("fragment =" + current.getClass().getName()
					+ "  key up :" + keyCode);
			return current.onKeyUp(keyCode, event);
		} else {
			return super.onKeyUp(keyCode, event);
		}
	}
	
	@Override
	public boolean dispatchKeyEvent(KeyEvent event) {
		if (event.getAction() == KeyEvent.ACTION_DOWN) {
			BaseFragment current = mFragmentFactory.getCurrentFragment();
			if (current != null) {
				current.resetHideTimer();
			}
		}
		return super.dispatchKeyEvent(event);
	}
		
	@Override
	protected void onDestroy() {
		super.onDestroy();
		threadRunFlag = false;
		try {
			if (VodPlayerManager.getInstance(getBaseContext()).getIDataSet() != null) {
				unbindService(mIDataSetConnection);
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		unregisterReceiver(mHuaweiReceiver);
		unregisterReceiver(mNetworkReceiver);

	}
	
	public String getNum() {
		return num;
	}
	
	@Override
	public void onBackPressed() {
		if(showBackIcon){
			SimplePlayerActivity.this.finish();//直接退出
		}else{
			mFragmentFactory.showFragment(FragmentFactory.FRAGMENT_ID_EXIT,
					null);
		}
	}
	/**
	 * 按确认键
	 */
	public void onEnterPressed(){
		showDialog();
		mFragmentFactory.showFragment(FragmentFactory.FRAGMENT_ID_EMPTY,
				null);
		VodPlayerManager.getInstance(SimplePlayerActivity.this).doPressBackKey(false);//暂停
	}
	
	private TipDialog tipDialog;
	private void showDialog() {
		tipDialog = new TipDialog(SimplePlayerActivity.this,
				R.style.Dialog_Collect_Dark, tipDialogListener,
				R.string.tvtip,showBackIcon);
		tipDialog.setOnKeyListener(new OnKeyListener() {
			
			@Override
			public boolean onKey(DialogInterface dialog, int keyCode, KeyEvent event) {
				if(event.getAction()==KeyEvent.ACTION_DOWN){
					if(keyCode==KeyEvent.KEYCODE_BACK||keyCode == KeyEvent.KEYCODE_ESCAPE){
//						VodPlayerManager.getInstance(SimplePlayerActivity.this).doPressBackKey(true);
//						tipDialog.dismiss();
//						return true;
						SimplePlayerActivity.this.finish();
					}
					if(keyCode==KeyEvent.KEYCODE_DPAD_CENTER||keyCode==KeyEvent.KEYCODE_ENTER){
						if(tipDialog.isAdHide()){//广告被隐藏
							tipDialog.dismiss();
							VodPlayerManager.getInstance(SimplePlayerActivity.this).doPressBackKey(true);
							return true;
						}
					}
					if(keyCode == KeyEvent.KEYCODE_VOLUME_DOWN
						|| keyCode == KeyEvent.KEYCODE_VOLUME_MUTE
						|| keyCode == KeyEvent.KEYCODE_VOLUME_UP
						|| keyCode == KeyEvent.KEYCODE_MUTE){
						return true;
						//TODO　音量控制
					}
				}
				return false;
			}
		});
		tipDialog.show();
	}

	TipDialogListener tipDialogListener = new TipDialogListener() {

		@Override
		public void sure() {
			//继续看
			VodPlayerManager.getInstance(SimplePlayerActivity.this).doPressBackKey(true);//继续播放
		}

		@Override
		public void cancel() {
			SimplePlayerActivity.this.finish();
		}
	};
	
	Runnable runnableEnd = new Runnable() {
		@Override
		public void run() {
			VodPlayerManager.getInstance(SimplePlayerActivity.this).setDrag_factor(0);
			Log.i("wuhd", "flag="+playType);
			if (playType.equals("playType"))// 如果是电视剧，那么播放完毕后看是否还有下一集，有则继续播放，否则跳回详情页面,1为播放完
			{/*
				Logger.d("runnableEnd start teleplay next");
				if (Integer.parseInt(num) < Integer.parseInt(movieDetailResponse.getTotalNum() ))// 当前不是最后一集
				{
//					showDialog();
				} else {
					Logger.d("runnableEnd no teleplay next");
					isOver = 1;
					prompt.showDialogAndPostDelayed(getResources().getString(R.string.watchout), new Runnable() {

						@Override
						public void run() {
							finish();
						}
					}, 2000);
				}
			*/} else// 如果是电影，那么播放完毕后直接关闭播放器，回到详情页面
			{
				isOver = 1;
				prompt.showDialogAndPostDelayed(getResources().getString(R.string.watchout), new Runnable() {

					@Override
					public void run() {
						finish();
					}
				}, 2000);
				Logger.d("runnableEnd movie");

			}
		}
	};

	public BroadcastReceiver mNetworkReceiver = new BroadcastReceiver() {

		@Override
		public void onReceive(Context context, Intent intent) {
			isOnLine=JSONApiHelper.isOnline(getApplicationContext());
			VodPlayerManager.getInstance(getApplicationContext()).onNetworkChange(isOnLine);
		}
	};
	
	public long getHistoryTime() {
		return historyTime;
	}
	
	public String getName(){
		return name;
	}
	
	public String getPlayType(){
		return playType;
	}
	
	public GetHwResponse getHwResponse(){
		return hwResponse;
	}
	
	public String getWikiId(){
		if(playType.equals("1") && hwResponse != null){
			return hwResponse.getWiki().getId();
		}
		return wikiId;
	}
}
