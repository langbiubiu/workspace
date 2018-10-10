package com.ipanel.join.chongqing.live;

import java.util.List;

import ipanel.join.ad.widget.TextAdView;

import android.app.FragmentTransaction;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.KeyEvent;
import android.view.RcKeyEvent;
import android.view.View;

import cn.ipanel.android.LogHelper;

import com.ipanel.chongqing_ipanelforhw.R;
import com.ipanel.join.chongqing.live.base.UIFragment;
import com.ipanel.join.chongqing.live.ca.LiveLiveCaFragment;
import com.ipanel.join.chongqing.live.ca.LiveSettingsCaFragment;
import com.ipanel.join.chongqing.live.manager.ADManager;
import com.ipanel.join.chongqing.live.manager.BookManager;
import com.ipanel.join.chongqing.live.manager.CAAuthManager;
import com.ipanel.join.chongqing.live.manager.DataManager;
import com.ipanel.join.chongqing.live.manager.SettingManager;
import com.ipanel.join.chongqing.live.manager.StationManager;
import com.ipanel.join.chongqing.live.manager.UIManager;
import com.ipanel.join.chongqing.live.manager.impl.CAAuthManagerImpl;
import com.ipanel.join.chongqing.live.manager.impl.hw.HWADManagerImpl;
import com.ipanel.join.chongqing.live.manager.impl.hw.HWDataManagerImpl;
import com.ipanel.join.chongqing.live.manager.impl.hw.HWSettingManagerImpl;
import com.ipanel.join.chongqing.live.manager.impl.hw.HWStationManagerImpl;
import com.ipanel.join.chongqing.live.manager.impl.hw.HWUIManagerImpl;
import com.ipanel.join.chongqing.live.navi.DatabaseObjects.LiveChannel;
import com.ipanel.join.chongqing.live.navi.NaviFragment;
import com.ipanel.join.chongqing.live.play.PlayFragment;
import com.ipanel.join.chongqing.live.ui.TVADDFragment;
import com.ipanel.join.chongqing.live.util.ShowHelper;
import com.ipanel.join.chongqing.portal.PortalActivity;
import com.ipanel.join.chongqing.portal.VolumePanel;

public class LiveForHWActivity extends LiveActivity {
	
	TextAdView ad;
	
	private final static int MSG_SHIFT_SEEK = 0;
	
	Handler mHandler = new Handler() {
		
		@Override
		public void handleMessage(Message msg) {
			// TODO Auto-generated method stub
			switch (msg.what) {
			case MSG_SHIFT_SEEK:
				getStationManager().stopDragMedia();
				return;
			}
		}
	};
	
	VolumePanel volPanel;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onCreate(savedInstanceState);
		ad = (TextAdView)findViewById(R.id.ad);
		volPanel = new VolumePanel(this);
	}
	
	@Override
	protected void onResume() {
		super.onResume();
//		Constant.LOOSE_LIVE_PLAYER = true;
//		((HomedADManagerImpl)getADManager()).onShowAD(ADManager.AD_FOR_LIVE_TXT, ad);
		ad.setVisibility(View.GONE);
		Constant.LOOSE_LIVE_PLAYER = !getIntent().getBooleanExtra("noPlay", false);
		PortalActivity.hasEnteredLive = true;
//		((CAAuthManagerImpl)getCAAuthManager()).queryCAMail();
	}

	@Override
	protected void caculateShowsOnResume(final LiveChannel channel, boolean newIntent) {
		// TODO Auto-generated method stub
		if(newIntent && getIntent().getBooleanExtra("noPlay", false)){
			((HWStationManagerImpl)getStationManager()).currentCh = channel;
			LiveApp.getInstance().post(new Runnable() {
				
				@Override
				public void run() {
					mSMCallBack.onChannelAfterChange(channel);
					
				}
			});
			return;
		}
		getStationManager().switchTVChannel(channel);
	}
	
	@Override
	protected void onPause() {
		// TODO Auto-generated method stub
//		getStationManager().setMediaVolume(0.0f);
		volPanel.onResume();
		PortalActivity.returnFromLive = true;
		super.onPause();
	}
	
	protected LiveSettingsCaFragment settingsf;
	protected LiveLiveCaFragment liveFragmaent;
	protected NaviFragment navif;
	protected PlayFragment playf;
	
	@Override
	protected void loadFragments() {
		super.loadFragments();
		LogHelper.i("LiveForHomed", "loadFragments");
		playf = PlayFragment.createInstance();
		settingsf = LiveSettingsCaFragment.createInstance();
		liveFragmaent = LiveLiveCaFragment.createInstance();
		navif = NaviFragment.createInstance();
		FragmentTransaction ft = getFragmentManager().beginTransaction();
		ft.add(playf, null);
		ft.add(navif, null);
		ft.add(settingsf, null);
		ft.add(liveFragmaent, null);
		ft.commit();
	}
	
	@Override
	public boolean onKeyDown(int keyCode, KeyEvent event) {
		if(volPanel.onKeyDown(keyCode, event)){
			getUIManager().detatchExclusiveFagment();
			return true;
		}
		int code = ShowHelper.changeGlobalKeyCode(event);
		LogHelper.i("LiveForCQHomedActivity onKeyDown keycode = " + keyCode);
		LogHelper.i("LiveForCQHomedActivity onKeyDown rcKeyCode = " + code);
		
		if (keyCode == KeyEvent.KEYCODE_VOLUME_MUTE
				|| keyCode == KeyEvent.KEYCODE_MUTE) {
			if (getSettingManager().getMute()) {
				getSettingManager().changeMute(false);
			} else {
				getSettingManager().changeMute(true);
			}
			return true;
		}
		if (getUIManager().handleKeyEvent(code, event)) {
			return true;
		}
		switch (keyCode) {
		case KeyEvent.KEYCODE_DPAD_UP:
		case RcKeyEvent.KEYCODE_CH_DOWN:
			LogHelper.d("KEYCODE_DPAD_UP");
			if (!getStationManager().isShiftMode()) {
				List<LiveChannel> channels = getDataManager().getAllChannel();
				LiveChannel current = getStationManager().getPlayChannel();
				int position = channels.indexOf(current);
				LiveChannel next = channels.get((position - 1 + channels.size()) % channels.size());
				getStationManager().switchTVChannel(next);
				getUIManager().showUI(UIManager.ID_UI_CQ_LIVE_PF, null);
			}
			return true;
		case KeyEvent.KEYCODE_DPAD_DOWN:
		case RcKeyEvent.KEYCODE_CH_UP:
			LogHelper.d("KEYCODE_DPAD_DOWN");
			if (!getStationManager().isShiftMode()) {
				List<LiveChannel> channels = getDataManager().getAllChannel();
				LiveChannel current = getStationManager().getPlayChannel();
				int position = channels.indexOf(current);
				LiveChannel next = channels.get((position + 1) % channels.size());
				getStationManager().switchTVChannel(next);
				getUIManager().showUI(UIManager.ID_UI_CQ_LIVE_PF, null);
			}
			return true;
		case KeyEvent.KEYCODE_DPAD_LEFT:
			if (getStationManager().isShiftMode() && !(getUIManager().getCurrentFragment() instanceof TVADDFragment)) {
				getStationManager().startDragMedia(false);
			} else {
				if (getUIManager().getCurrentFragment() == null)
					startShiftPlayByMode(-1, StationManager.SHIFT_TYPE_WATCH_TAIL);
			}
			return true;
		case KeyEvent.KEYCODE_DPAD_RIGHT:
			if (getStationManager().isShiftMode()) {
				getStationManager().startDragMedia(true);
			} else {
				Log.d("lixby", "KEYCODE_DPAD_RIGHT");
//				getUIManager().showUI(UIManager.ID_UI_CQ_LIVE_CHANNEL_LIST, null);
			}
			return true;
		case KeyEvent.KEYCODE_BACK:
		case RcKeyEvent.KEYCODE_QUIT:
			UIFragment ui = getUIManager().getCurrentFragment();
			if (ui != null) {
				getUIManager().hideFragment(ui);
			} else {
				if (getStationManager().isShiftMode()) {
//					getUIManager().showUI(UIManager.ID_UI_SHIFT_QUIT, null);
					getStationManager().startLivePlay();
				} else {
//					moveTaskToBack(true);
					getUIManager().showUI(UIManager.ID_UI_CQ_LIVE_QUIT, null);
				}
			}
			return true;
		case KeyEvent.KEYCODE_ENTER:
		case KeyEvent.KEYCODE_DPAD_CENTER:
			if (getStationManager().isShiftMode()) {
				getStationManager().playOrPauseMedia();
			} else {
//				getUIManager().showUI(UIManager.ID_UI_CQ_LIVE_PF, null);
				getUIManager().showUI(UIManager.ID_UI_CQ_LIVE_CHANNEL_LIST, null);
			}
			return true;
		case KeyEvent.KEYCODE_0:
		case KeyEvent.KEYCODE_1:
		case KeyEvent.KEYCODE_2:
		case KeyEvent.KEYCODE_3:
		case KeyEvent.KEYCODE_4:
		case KeyEvent.KEYCODE_5:
		case KeyEvent.KEYCODE_6:
		case KeyEvent.KEYCODE_7:
		case KeyEvent.KEYCODE_8:
		case KeyEvent.KEYCODE_9:
			if (!getStationManager().isShiftMode()) {
				getUIManager().showUI(UIManager.ID_UI_CQ_LIVE_PF, keyCode - 7);
			}
			return true;
		case KeyEvent.KEYCODE_MENU:
		case RcKeyEvent.KEYCODE_TV_ADD:
			if (getUIManager().getCurrentFragment() instanceof TVADDFragment) {
				getUIManager().clearCurrentFragment();
			} else {
				getUIManager().showUI(UIManager.ID_UI_CQ_TV_ADD, 0);
			}
			return true;
		default:
			break;
		}
		return false;
	}
	
	@Override
	public boolean onKeyUp(int keyCode, KeyEvent event) {
		// TODO Auto-generated method stub
		int code = ShowHelper.changeGlobalKeyCode(event);
		if (getUIManager().handleKeyEvent(code, event)) {
			return true;
		}

		switch (code) {
		case KeyEvent.KEYCODE_DPAD_DOWN:
		case RcKeyEvent.KEYCODE_CH_DOWN:
		case KeyEvent.KEYCODE_DPAD_UP:
		case RcKeyEvent.KEYCODE_CH_UP:
			if (getStationManager().isShiftMode()) {
				LiveChannel play = getStationManager().getPlayChannel();
				LiveChannel next = getDataManager().caculateChannelChange(
						keyCode == KeyEvent.KEYCODE_DPAD_DOWN ? 1 : -1,
						getStationManager().getPlayCircle(), play);
				getStationManager().setPlayChannel(next);
				getStationManager().startLivePlay();
			} else {
				// if
				// (getUIManager().isFragmentAdded(UIManager.ID_UI_LIVE_INFO)) {
				//
				// }
				if (mNeedSyncChannel) {
					getStationManager().switchTVChannel(
							getStationManager().getPlayChannel());
					mNeedSyncChannel = false;
				}
			}
			return true;
		case KeyEvent.KEYCODE_DPAD_LEFT:
		case KeyEvent.KEYCODE_DPAD_RIGHT:
			if (getStationManager().isShiftMode()) {
				mHandler.removeMessages(MSG_SHIFT_SEEK);
				mHandler.sendEmptyMessageDelayed(MSG_SHIFT_SEEK, 500);
				return true;
			}
		}
		return false;
	}

	@Override
	protected ADManager createADManager() {
		// TODO Auto-generated method stub
		return new HWADManagerImpl(this);
	}

	@Override
	protected CAAuthManager createCAAuthManager() {
		// TODO Auto-generated method stub
		return new CAAuthManagerImpl(this, settingsf, liveFragmaent,
				mLCMCallBack);
	}

	@Override
	protected BookManager createBookManager() {
		// TODO Auto-generated method stub
		return LiveApp.getInstance().getBookManager();
	}

	@Override
	protected DataManager createDataManager() {
		// TODO Auto-generated method stub
		return new HWDataManagerImpl(this, navif, mDMCallBack);
	}

	@Override
	protected StationManager createStationManager() {
		// TODO Auto-generated method stub
		return new HWStationManagerImpl(this, playf, mSMCallBack);
	}

	@Override
	protected SettingManager createSettingManager() {
		// TODO Auto-generated method stub
		return new HWSettingManagerImpl(this, mVMCallBack);
	}

	@Override
	protected UIManager createUIManager() {
		// TODO Auto-generated method stub
		return new HWUIManagerImpl(this, R.id.first_level, R.id.second_level);
	}

}
