package com.ipanel.join.chongqing.live;

import ipaneltv.toolkit.db.DatabaseObjectification.ChannelKey;
import ipaneltv.toolkit.tm.LiveTmFragment;

import java.util.List;

import android.app.Activity;
import android.app.FragmentTransaction;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.KeyEvent;
import android.view.PanelViewFragment;
import android.view.View;
import cn.ipanel.android.LogHelper;
import cn.ipanel.android.net.cache.JSONApiHelper;

import com.ipanel.chongqing_ipanelforhw.R;
import com.ipanel.join.chongqing.live.manager.ADManager;
import com.ipanel.join.chongqing.live.manager.BookManager;
import com.ipanel.join.chongqing.live.manager.BookManager.BookEventChangeListener;
import com.ipanel.join.chongqing.live.manager.CAAuthManager;
import com.ipanel.join.chongqing.live.manager.DataManager;
import com.ipanel.join.chongqing.live.manager.DataManager.ShiftChannel;
import com.ipanel.join.chongqing.live.manager.IManager;
import com.ipanel.join.chongqing.live.manager.SettingManager;
import com.ipanel.join.chongqing.live.manager.StationManager;
import com.ipanel.join.chongqing.live.manager.UIManager;
import com.ipanel.join.chongqing.live.manager.impl.BaseStationManagerImpl;
import com.ipanel.join.chongqing.live.navi.DatabaseObjects.LiveChannel;
import com.ipanel.join.chongqing.live.navi.DatabaseObjects.LiveGroup;
//import com.ipanel.join.homed.chongqing.HomedDataManager;
import com.ipanel.join.chongqing.live.util.ShowHelper;

public abstract class LiveActivity extends Activity implements IManager {
	private final static String TAG ="IManager";
	protected UIManager mUIManager;
	protected SettingManager mDataSaveManager;
	protected StationManager mStationManager;
	protected DataManager mDataManager;
	protected CAAuthManager mLiveCAManager;
	protected ADManager mADManager;
	protected BookManager mBookManager;
	/**视频控件*/
	protected WidgetFragment widgetf;
	/** 前端面板 */
	private PanelViewFragment panel;
	/**是否需要同步当前频道*/
	public boolean mNeedSyncChannel = false;
	
	public boolean openMailFlag = false;

	/**
	 * 初始化线程
	 * */
	private Runnable mInitRunnable = new Runnable() {
		@Override
		public void run() {
			long total = 10000;
			long step = 10;
			long count = total / step;
			int i = 0;
			do {
				try {
					Thread.sleep(step);
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
				if (i >= count || isFinishing()) {
					break;
				} else {
					i++;
				}
				Log.d(TAG, "cheack is  ReadyOK "+i);
			} while (!isReadyOK());

			if (!isFinishing()) {
				if (i >= count) {
					// 超时
					showMessage(R.string.toast_message_loading_time_up);
					finish();
				} else {
					Log.d(TAG, "go play channel");
					List<LiveChannel> channels = getDataManager()
							.getAllChannel();
					Log.d(TAG, "go play channel"+channels.size());
					if (channels.size() == 0) {
						// 频道为空
						onChannelDataEmpty();
					} else {
						if(!getIntent().getBooleanExtra("noPlay", false))
							getStationManager().selectInvalidChannel();
						getStationManager().initVideoArea();
						LiveChannel channel = handleIntent();
						if (channel == null) {
							if(openMailFlag){
								openMailFlag = false;
								getUIManager().showUI(UIManager.ID_UI_MAIL, null);
								return;
							}
							channel = getDataManager()
									.getLiveChannelByName(
											getSettingManager()
													.getSaveHistoryChannel()[0]);
						}
						if (channel == null) {
							channel = channels.get(0);
						}
						Log.d(TAG, "go play channel select Program"+channel.getProgram());
						Log.d(TAG, "go play channel select name"+channel.getName());
						Log.d(TAG, "go play channel select ChannelNumber"+channel.getChannelNumber());
//						getStationManager().setMediaRegion(0, 0, 1920, 1080);
						getStationManager().initVideoArea();
						getStationManager().setMediaVolume(0.5f);
//						if(getIntent().getIntExtra(Constant.LIVE_LAUNCH_TAG, Constant.ACTIVITY_LAUNCH_TYPE_DEAULT) == Constant.ACTIVITY_LAUNCH_TYPE_LIVE_PUSH){
//							long startTime = getIntent().getLongExtra(Constant.LIVE_LAUNCH_CHANNEL_START_TIME_TAG, 0);
//							long endTime = getIntent().getLongExtra(Constant.LIVE_LAUNCH_CHANNEL_END_TIME_TAG, 0);
//							long offTime = getIntent().getLongExtra(Constant.LIVE_LAUNCH_CHANNEL_OFF_TIME_TAG, 0);
//							
//							getStationManager().goTimeShift(channel, startTime, endTime, offTime);
//						} else {
//							caculateShowsOnResume(channel);
//						}
						caculateShowsOnResume(channel, hasNewIntent);
						getBookManager().caculateNextBook();
						hasNewIntent = false;
					}
				}
			} else {
				LogHelper.e("error state :activity has died");
			}
		}
	};

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		LogHelper.i(TAG, "onCreate");
		setContentView(R.layout.live_activity_live);
		View root = findViewById(R.id.rootlayout);
		root.setOnKeyListener(new View.OnKeyListener() {
			
			@Override
			public boolean onKey(View v, int keyCode, KeyEvent event) {
				LogHelper.i("root onKey keyCode = " + keyCode + ", action = " + event.getAction());
				return false;
			}
		});
		
		getSettingManager();
		loadFragments();
		IntentFilter mIf1 = new IntentFilter();
		mIf1.addAction("android.net.conn.CONNECTIVITY_CHANGE");
		LogHelper.i("register network receiver");
		registerReceiver(mNetworkReceiver, mIf1);
		IntentFilter mIf2 = new IntentFilter();
		mIf2.addAction(Constant.DP_IN_BROADCAST);
		registerReceiver(mDPReceiver, mIf2);

	}

	@Override
	protected void onResume() {
		// TODO Auto-generated method stub
		super.onResume();
		LogHelper.i(TAG, "onResume");
		getDataManager().checkDataValid();
		getBookManager().setBookEventChangeListener(
				new BookEventChangeListener() {
					@Override
					public void onBookEventChange() {
						// TODO Auto-generated method stub
						getUIManager().dispatchDataChange(
								Constant.DATA_CHANGE_OF_BOOK, null);
					}
				});
		getSettingManager().saveVolumeData();
//		getStationManager().setMediaRegion(0, 0, 1920, 1080);
		getStationManager().initVideoArea();

		new Thread(mInitRunnable).start();
	}

	@Override
	protected void onPause() {
		// TODO Auto-generated method stub
		super.onPause();
		LogHelper.i(TAG, "onPause");
		getUIManager().clearCurrentFragment();
		getStationManager().clearPlayData();
		((BaseStationManagerImpl)getStationManager()).destroyShiftPlayer();
		getSettingManager().restoreVolumeData();
		getBookManager().setBookEventChangeListener(null);

	}

	@Override
	protected void onDestroy() {
		// TODO Auto-generated method stub
		super.onDestroy();
		LogHelper.i(TAG, "onDestroy");
		getSettingManager().destroyData();
		unregisterReceiver(mNetworkReceiver);
		unregisterReceiver(mDPReceiver);
	}

	boolean hasNewIntent = true;
	@Override
	protected void onNewIntent(Intent intent) {
		super.onNewIntent(intent);
		LogHelper.e("onNewIntent");
		hasNewIntent = true;
		setIntent(intent);
	}

	@Override
	public boolean dispatchKeyEvent(KeyEvent event) {
		LogHelper.i(String.format("key press %s ----- %s ", event.getAction(),
				event.getKeyCode()));
		if(!isReadyOK()){
			LogHelper.i("data not ready, discard key event");
			return true;
		}
		getUIManager().resetHideTimer();
		LogHelper.i("LiveActivity return super dispatchKeyEvent");
		return super.dispatchKeyEvent(event);
	}

	@Override
	public void onBackPressed() {

	}

	/**
	 * 绑定功能Fragment
	 * */
	protected void loadFragments() {
		LiveTmFragment tm = LiveTmFragment
				.createInstance(Constant.TM_SERVICE_NAME);
		panel = PanelViewFragment.createInstance();
		widgetf = WidgetFragment.createInstance();// 视频窗口组件
		FragmentTransaction ft = getFragmentManager().beginTransaction();
		ft.add(tm, null);
		ft.add(panel, "PanelViewFragment");
		ft.replace(R.id.tv, widgetf);
		ft.commit();
	}

	public synchronized UIManager getUIManager() {
		if (mUIManager == null) {
			mUIManager = createUIManager();
		}
		if (mUIManager == null) {
			throw new IllegalStateException(String.format("%s is not inited",
					"UIManager"));
		}
		return mUIManager;
	}

	public synchronized SettingManager getSettingManager() {
		if (mDataSaveManager == null) {
			mDataSaveManager = createSettingManager();
		}
		if (mDataSaveManager == null) {
			throw new IllegalStateException(String.format("%s is not inited",
					"SettingManager"));
		}
		return mDataSaveManager;
	}

	public synchronized StationManager getStationManager() {
		if (mStationManager == null) {
			mStationManager = createStationManager();
		}
		if (mStationManager == null) {
			throw new IllegalStateException(String.format("%s is not inited",
					"StationManager"));
		}
		return mStationManager;
	}

	public synchronized DataManager getDataManager() {
		if (mDataManager == null) {
			mDataManager = createDataManager();
		}
		if (mDataManager == null) {
			throw new IllegalStateException(String.format("%s is not inited",
					"DataManager"));
		}
		return mDataManager;
	}

	public synchronized BookManager getBookManager() {
		if (mBookManager == null) {
			mBookManager = createBookManager();
		}
		if (mBookManager == null) {
			throw new IllegalStateException(String.format("%s is not inited",
					"BookManager"));
		}
		return mBookManager;
	}

	public synchronized CAAuthManager getCAAuthManager() {
		if (mLiveCAManager == null) {
			mLiveCAManager = createCAAuthManager();
		}
		if (mLiveCAManager == null) {
			throw new IllegalStateException(String.format("%s is not inited",
					"CAAuthManager"));
		}
		return mLiveCAManager;
	}

	public synchronized ADManager getADManager() {
		if (mADManager == null) {
			mADManager = createADManager();
		}
		if (mADManager == null) {
			throw new IllegalStateException(String.format("%s is not inited",
					"ADManager"));
		}
		return mADManager;
	}

	@Override
	public Context getContext() {
		// TODO Auto-generated method stub
		return this;
	}

	public void noticyDateChange(int type) {
		noticyDateChange(type, null);
	}

	public void noticyDateChange(int type, Object data) {
		getUIManager().dispatchDataChange(type, data);
	}

	public void showMessage(final String msg) {
		LiveApp.getInstance().post(new Runnable() {
			@Override
			public void run() {
				// TODO Auto-generated method stub
				InfoManager.getInstance(getBaseContext())
						.showDefaultDialog(msg);
			}
		});

	}

	public void showMessage(int res) {
		showMessage(getResources().getString(res));
	}
	
	public void startShiftPlay(long time) {
		startShiftPlay(time,StationManager.SHIFT_TYPE_WATCH_TAIL,true,false, 0, 0);
	}
	
	public void startShiftPlayByMode(long time,int shiftMode) {
		Constant.INTO_SHIFT_MODE=shiftMode;
		startShiftPlay(time,shiftMode,true,false, 0, 0);
	}

	public void startShiftPlay(long time,int style,boolean ip,boolean pause, long start, long end) {
		LogHelper.i("startShiftPlay time = " + time + ", style = " + style + ", ip = " + ip
				+ ", pause=" + pause);
		LiveChannel channel = getStationManager().getPlayChannel();
		if (channel == null || getDataManager().isShiftChannel(channel) == null) {
			LogHelper.i("invalid timeshift param channelKey = "+channel.getChannelKey().getProgram());
			showMessage(R.string.toast_message_invalid_timeshift);
			return;
		}
		if (!JSONApiHelper.isOnline(getBaseContext())) {
//		if(!NetConstants.checkNetState()){
			showMessage(R.string.toast_message_invalid_network);
			return;
		}
//		if (!Constant.DEVELOPER_MODE && !getCAAuthManager().isCAValid()) {
//			showMessage(R.string.toast_message_invalid_ca);
//			return;
//		}
//		if (!Constant.DEVELOPER_MODE
//				&& getDataManager().isValidAuthForShift(
//						getStationManager().getPlayChannel())) {
//			showMessage(R.string.error_unauthorized);
//			return;
//		}
		if(style==StationManager.SHIFT_TYPE_WATCH_HEAD&&getDataManager().getChannelCurrentProgram(getStationManager().getPlayChannel())==null){
			LogHelper.e("can't find current program at head shift");
			return ;
		}
		Log.e("lixby", "current program at head shift  is not null");
		Constant.COUNT_DOWN_READY = true;
		Constant.SHIFT_DATA_READY = false;
		getStationManager().startShiftPlay(time,style,ip,pause, start, end);
	}

	private LiveChannel handleIntent() {
		Log.d("lixby","LiveChannel****************************************");
		if(!hasNewIntent){
			return getStationManager().getPlayChannel();
		}
		Intent intent = this.getIntent();
		LiveChannel channel = null;
		int circle = Constant.BIG_CIRCLE_COLUME_ID;

		int live_flag = intent.getIntExtra(Constant.LIVE_LAUNCH_TAG,
				Constant.ACTIVITY_LAUNCH_TYPE_DEAULT);
		LogHelper.e("receive the flag is: " + live_flag);
		Log.e("lixby","receive the flag is: " + live_flag);
		try {
			switch (live_flag) {
			case Constant.ACTIVITY_LAUNCH_TYPE_MEMORY_CHANNEL:
				channel = getDataManager().getLiveChannelByName(
						getSettingManager().getSaveHistoryChannel()[0]);
				break;
			case Constant.ACTIVITY_LAUNCH_TYPE_LIVE_KEY:
				
				break;
			case Constant.ACTIVITY_LAUNCH_TYPE_LIVE_FAVORITE:

				break;
			case Constant.ACTIVITY_LAUNCH_TYPE_FREQUENCE_PROGRAM: {
				@SuppressWarnings("unused")
//				long frequency = Long.parseLong(intent
//						.getStringExtra(Constant.LIVE_LAUNCH_FREQUENCE_TAG));
				int program_number = Integer.parseInt(intent
						.getStringExtra(Constant.LIVE_LAUNCH_PROGRAM_TAG));
				channel = getDataManager().getLiveChannelByService(program_number);
				break;
			}
			case Constant.ACTIVITY_LAUNCH_TYPE_CHANNEL_NUMBER:
				if (intent
						.getStringExtra(Constant.LIVE_LAUNCH_CHANNLE_NUMBER_TAG) != null) {
					int channel_number = Integer
							.parseInt(intent
									.getStringExtra(Constant.LIVE_LAUNCH_CHANNLE_NUMBER_TAG));
					channel = getDataManager().getLiveChannelByNumber(
							channel_number);
				}
				break;
			case Constant.ACTIVITY_LAUNCH_TYPE_LIVE_PUSH:
				String channelId = getIntent().getStringExtra(Constant.LIVE_LAUNCH_CHANNEL_ID_TAG);
				if(channelId != null)
					channel = getDataManager().getLiveChannelByChannelId(channelId);
				break;
			case Constant.ACTIVITY_LAUNCH_TYPE_TIMESHIFT_PUSH:
				channelId = getIntent().getStringExtra(Constant.LIVE_LAUNCH_CHANNEL_ID_TAG);
				if(channelId != null){
					channel = getDataManager().getLiveChannelByChannelId(channelId);
				}
				break;
			case Constant.ACTIVITY_LAUNCH_TYPE_CHANNEL_NAME:
				if (intent
						.getStringExtra(Constant.LIVE_LAUNCH_CHANNLE_NAME_TAG) != null) {
					String channel_name = intent.getStringExtra(Constant.LIVE_LAUNCH_CHANNLE_NAME_TAG);
					Log.e("lixby","33333333333333333   channel_name: " + channel_name);
					channel = getDataManager().getLiveChannelByName(channel_name);
				}
				break;
			case Constant.ACTIVITY_LAUNCH_TYPE_MAIl:
				channel = null;
				openMailFlag = true;
				break;
			default:
				String program_str = (intent.getStringExtra("program") + "").trim();
				String bouquet_str = intent.getStringExtra("bouquet");
				LogHelper.i("intent program_str :" + program_str);
				LogHelper.i("intent bouquet_st :" + bouquet_str);
				int service = -1;
				try {
					service = Integer.parseInt(program_str);
				} catch (NumberFormatException e) {
				}
				channel = getDataManager().getLiveChannelByService(service);
				int circle_id = 0;
				if (bouquet_str != null && !("".equals(bouquet_str))) {
					circle_id = Integer.parseInt(bouquet_str);
				}
				LiveGroup group = getDataManager().getGroupByID(circle_id);
				if (group != null) {
					circle = circle_id;
					if (channel == null) {
						List<LiveChannel> gc = getDataManager()
								.getCircleChannel(circle);
						channel = (gc == null || gc.size() == 0 ? null : gc
								.get(0));
					}
				} else {
					if (circle_id > 0) {
						showMessage(R.string.toast_message_no_group);
					}
					LogHelper.i("no match group ");
				}
				break;
			}

		} catch (Exception e) {
			e.printStackTrace();
		}
		LogHelper.i("intent handle ,and channel is :" + circle);
		LogHelper.i("intent handle ,and channel is :"+ (channel == null ? "null" : channel.getName()));
		Log.e("lixby","intent handle ,and channel is : " + (channel == null ? "null" : channel.getName()));
		getStationManager().setPlayCircle(circle);
		intent.putExtra(Constant.LIVE_LAUNCH_TAG, -1);
		intent.putExtras(new Bundle());
		return channel;
	}

	public boolean isReadyOK() {
		return getDataManager() != null && getDataManager().isReadbyOK()
				&& getStationManager() != null
				&& getStationManager().isReadbyOK();
	}

	public void onChannelDataEmpty() {
		LogHelper.e("error state : channel is empty");
		getDataManager().setWaitingForData();
		showMessage(R.string.toast_message_channel_empty);
		startOtherApk(this, Constant.NETWORK_PACKAGE_NAME,
				Constant.NETWORK_ACTIVITY_NAME, null);
	}

	public void startOtherApk(Context context, String packageChar,
			String classChar, Bundle extras) {// 跳转到其他Activity
		try {
			ComponentName componentName = new ComponentName(packageChar,
					classChar);
			Intent intent = new Intent();
			if (null != extras) {
				intent.putExtras(extras);
			}
			intent.setComponent(componentName);
			context.startActivity(intent);
		} catch (Exception e) {
			showMessage(R.string.toast_message_no_found_app);
			e.printStackTrace();
		}
	};

	SettingManager.CallBack mVMCallBack = new SettingManager.CallBack() {

		@Override
		public void onMuteStateChange(boolean mute) {
			// TODO Auto-generated method stub
//			getUIManager().showUI(UIManager.ID_UI_MUTE, mute);
		}
	};

	CAAuthManager.CallBack mLCMCallBack = new CAAuthManager.CallBack() {

		@Override
		public void onScrollMessage(String msg) {
			// TODO Auto-generated method stub
			LogHelper.i("show ca message :" + msg);

			if (TextUtils.isEmpty(msg) || "".equals(msg.trim())) {
//				getUIManager().hideUI(UIManager.ID_UI_OSD);
				return;
			}
//			getUIManager().showUI(UIManager.ID_UI_OSD, msg);
		}

		@Override
		public void onMailChanged(boolean show) {
			// TODO Auto-generated method stub
			LogHelper.i("mail show :" + show);
//			getUIManager().showUI(UIManager.ID_UI_MAIL, show);
//	        showMessage("有新邮件");
			
			Intent intent = new Intent();
			intent.setAction("com.ipanel.join.py.mail");
			intent.putExtra("mail_state", show);
			
			sendBroadcast(intent);
		}

		@Override
		public void onCAInfoChnaged() {
			// TODO Auto-generated method stub

		}
	};
	public StationManager.CallBack mSMCallBack = new StationManager.CallBack() {

		@Override
		public void onChannelAfterChange(LiveChannel channel) {
			// TODO Auto-generated method stub
			// changeMediaPlayer(null);
			((BaseStationManagerImpl)getStationManager()).getPresentAndFollow(channel.getChannelKey());
			getDataManager().requestChannelPF(channel.getChannelKey());
//			getUIManager().showUI(UIManager.ID_UI_CAPTION, null);
			if (panel != null) {
				panel.setText(ShowHelper.getShowChannel(channel
						.getChannelNumber()));
			}
			if (channel.getServiceType() == 2) {
				LogHelper.e("set bd img");
				widgetf.setVideoMask(ADManager.AD_FOR_BROADCAST);
			} else {
				widgetf.setVideoMask(ADManager.AD_FOR_NONE);
			}
			
//			int vol = getSettingManager().getCurrentVoluome();
//			if(vol == 0){
//				getSettingManager().changeMute(true);
//			}else{
//				getSettingManager().changeMute(false);
//			}
		}

		@Override
		public void onPlayStateChanged(int state) {
			// TODO Auto-generated method stub
			switch (state) {
			case StationManager.PLAY_STATE_LIVE:
				getUIManager().showUI(UIManager.ID_UI_CQ_LIVE_CHANNEL_LIST, null);
				break;
			case StationManager.PLAY_STATE_REQUEST:
				getUIManager().showUI(UIManager.ID_UI_SHIFT_LOADING, null);
				break;
			case StationManager.PLAY_STATE_PLAYING:
				getUIManager().showUI(UIManager.ID_UI_CQ_SHIFT_INFO, null);
				break;
			case StationManager.PLAY_STATE_DRAG:
				getUIManager().showUI(UIManager.ID_UI_CQ_SHIFT_INFO, null);
				break;
			case StationManager.PLAY_STATE_PAUSE:
				getUIManager().showUI(UIManager.ID_UI_SHIFT_QUIT, null);
				break;
			case StationManager.PLAY_STATE_ERROR:
				showMessage("时移出错");
				getStationManager().startLivePlay();
				break;
			}
			if(StationManager.PLAY_STATE_LIVE != state){
				getUIManager().showUI(UIManager.ID_UI_WATCH_STATE, null);
			}else{
				getUIManager().hideUI(UIManager.ID_UI_WATCH_STATE);
			}
		}

		@Override
		public void onShiftError(String msg) {
			// TODO Auto-generated method stub
			LogHelper.e("shift error :" + msg);
			showMessage(msg);
			getStationManager().startLivePlay();
		}

		@Override
		public void onShiftTimeUp() {
			// TODO Auto-generated method stub
			showMessage(R.string.toast_exception_timeshift_fail);
			getStationManager().startLivePlay();
		}

		@Override
		public void onShiftReady() {
			// TODO Auto-generated method stub
			getUIManager().showUI(UIManager.ID_UI_WATCH_STATE, null);
		}

		@Override
		public void onShiftTick() {
			// TODO Auto-generated method stub
			getUIManager().dispatchDataChange(
					Constant.DATA_CHANGE_OF_SHIFT_TICK, null);
		}

		@Override
		public void onShiftPrepareStart(LiveChannel channel) {
			// TODO Auto-generated method stub
			getDataManager().requestShiftURL(channel);
		}

		@Override
		public void onPlayMaxDot() {
			// TODO Auto-generated method stub
			getStationManager().startLivePlay();
			showMessage(R.string.shift_mg_most);
		}

		@Override
		public void onPlayMinDot() {
			// TODO Auto-generated method stub
			showMessage(R.string.shift_mg_least);
		}

		@Override
		public void onChannelBeforeChange(LiveChannel channel) {
			// TODO Auto-generated method stub
			Log.e("lixby"," onChannelBeforeChange");
			getDataManager().saveOffChannel(getStationManager().getPlayChannel());

			// changeMediaPlayer(getDataManager().getLiveWebPlayURL(channel));
		}
	};
	DataManager.CallBack mDMCallBack = new DataManager.CallBack() {

		@Override
		public void onShiftDataResponse(ShiftChannel channel) {
			// TODO Auto-generated method stub
			Constant.SHIFT_DATA_READY = true;
		}

		@Override
		public void onChannelCaptionLoaded() {
			// TODO Auto-generated method stub
			//台标
//			getUIManager().showUI(UIManager.ID_UI_CAPTION, null);
		}

		@Override
		public void onPFDataChanged(ChannelKey key) {
			LogHelper.i("onPFDataChanged channelKey = "+key);
			if (key == null) {
				noticyDateChange(Constant.DATA_CHANGE_OF_PF);
			} else {
				noticyDateChange(Constant.DATA_CHANGE_OF_PF_WITH_KEY, key);
			}
		}

		@Override
		public void onEPGDataChanged(ChannelKey key, String time) {
			// TODO Auto-generated method stub
			noticyDateChange(Constant.DATA_CHANGE_OF_EPG_WITH_TIME, key);

		}

		@Override
		public void onDataAuthChanged() {
			// TODO Auto-generated method stub
			noticyDateChange(Constant.DATA_CHANGE_OF_AUTH);

		}

		@Override
		public void onChannelProgramChanged(ChannelKey key) {
			// TODO Auto-generated method stub
			noticyDateChange(Constant.DATA_CHANGE_OF_EPG_EVENT, key);

		}
		
		@Override
		public void onChannelGroupDataChanged(String group_id) {
			noticyDateChange(Constant.DATA_CHANGE_OF_CHANNEL_GROUP, group_id);
		}
		
		@Override
		public void onFavoriteChannelChanged(int change_mode, boolean success) {
			noticyDateChange(change_mode, success);
		};
	};

	public BroadcastReceiver mNetworkReceiver = new BroadcastReceiver() {

		@Override
		public void onReceive(Context context, Intent intent) {

			if (!isFinishing()) {
				LogHelper.i("network received!");
				getUIManager().dispatchDataChange(
						Constant.DATA_CHANGE_OF_NETWORK, null);
//				getStationManager().handleNetChange(
//						JSONApiHelper.isOnline(getBaseContext()));
				getStationManager().handleNetChange(true); //暂时设为true
				
//				getStationManager().handleNetChange(NetConstants.checkNetState());
				
//				if (NetConstants.checkNetState()) {
				if (JSONApiHelper.isOnline(getBaseContext())) {
					LogHelper.i("-------------------net connected-----------------");
					getDataManager().checkDataValid();
				}
			}
		}
	};
	
	public BroadcastReceiver mDPReceiver = new BroadcastReceiver() {

		@Override
		public void onReceive(Context context, Intent intent) {
			LogHelper.i("receive dp broadcast ");
			LiveChannel currentCh = getStationManager().getPlayChannel();
			if (isFinishing()) {
				LogHelper.i("finished");
				return;
			}
			if (currentCh != null) {
				Intent mIntent = new Intent(Constant.DP_OUT_BROADCAST);
				String value = "{'command':'1','type':'1','parameters':{'freq':'"
						+ currentCh.getChannelKey().getFrequency()
						+ "','serviceId':'"
						+ currentCh.getChannelKey().getProgram()
						+ "','cardId':'" + currentCh.getTsId() + "'}}";
				mIntent.putExtra("params", value);
				sendBroadcast(mIntent);
			} else {
				LogHelper.e("current ch is null");
			}
		}
	};

	@Override
	public void onWindowFocusChanged(boolean hasFocus) {
		// TODO Auto-generated method stub
		super.onWindowFocusChanged(hasFocus);
		LogHelper.i("window focus change hasFocus:" + hasFocus+", currentFocus="+getCurrentFocus());
	}

	@Override
	protected void onSaveInstanceState(Bundle outState) {

	}

	protected void goTimeShift(LiveChannel channel, long startTime, long endTime, long offTime){
		getStationManager().goTimeShift(channel, startTime, endTime, offTime);
	}

	protected abstract void caculateShowsOnResume(LiveChannel channel, boolean newIntent);

	protected abstract ADManager createADManager();

	protected abstract CAAuthManager createCAAuthManager();

	protected abstract BookManager createBookManager();

	protected abstract DataManager createDataManager();

	protected abstract StationManager createStationManager();

	protected abstract SettingManager createSettingManager();

	protected abstract UIManager createUIManager();
}
