package com.ipanel.join.chongqing.live.manager.impl;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.media.AudioManager;
import android.os.IBinder;
import android.os.RemoteException;
import cn.ipanel.android.LogHelper;

import com.ipanel.join.cq.settings.aidl.IDataSet;
import com.ipanel.join.chongqing.live.Constant;
import com.ipanel.join.chongqing.live.SharedPreferencesMenager;
import com.ipanel.join.chongqing.live.manager.IManager;
import com.ipanel.join.chongqing.live.manager.SettingManager;
import com.ipanel.join.chongqing.live.manager.StationManager;
import com.ipanel.join.chongqing.live.navi.DatabaseObjects.LiveChannel;

public class BaseSettingManagerImpl extends SettingManager {

	IManager mContext;
	private CallBack mCallBack;
	private IDataSet mIDataSet;
//	private IAuth mIAuth;
	private AudioManager mAudioManager;
	private int enter_vol;
	private SharedPreferencesMenager mSharedPreferencesMenager;
	
	boolean ismute = false;
	
	public BaseSettingManagerImpl(IManager context,CallBack callback){
		this.mContext=context;
		this.mCallBack=callback;
		mAudioManager = (AudioManager) mContext.getContext().getSystemService(Context.AUDIO_SERVICE);
		mSharedPreferencesMenager=SharedPreferencesMenager.getInstance(mContext.getContext());
		initAllService();
	}
	@Override
	public int getMaxVoluome() {
		// TODO Auto-generated method stub
		return mAudioManager.getStreamMaxVolume(AudioManager.STREAM_MUSIC);
	}

	@Override
	public int getCurrentVoluome() {
		if (mIDataSet  != null) {
			try {
				return (int)(mIDataSet.getChannelVolume(getVolumeKey()));
			} catch (NumberFormatException e) {
				e.printStackTrace();
			} catch (RemoteException e) {
				e.printStackTrace();
			}
		}
		return mAudioManager.getStreamVolume(AudioManager.STREAM_MUSIC);
	}

	@Override
	public boolean getMute() {
//		boolean mute = SysUtils.isStreamMute(mAudioManager, AudioManager.STREAM_MUSIC);
		boolean mute=false;
//		int v=mAudioManager.getStreamVolume(AudioManager.STREAM_MUSIC);
		int v = 0;
		if(isGlobalVoiceControl() && mIDataSet!=null){
			try {
				v = (int)mIDataSet.getGlobalVolume();
			} catch (RemoteException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
				v = mAudioManager.getStreamVolume(AudioManager.STREAM_MUSIC);
			}
		}else{
			v = getCurrentVoluome();
		}
		if (v==0) {
			mute=true;
			ismute = mute;
		}
		LogHelper.i("isMute " + mute);
		return mute;
	}

	@Override
	public void changeVolume(boolean add) {
		// TODO Auto-generated method stub
		LogHelper.i("setting is null");
//		int maxVolume = mAudioManager.getStreamMaxVolume(AudioManager.STREAM_MUSIC);
//		int currentVolume = mAudioManager.getStreamVolume(AudioManager.STREAM_MUSIC);
//		if (add) {
//			mAudioManager.setStreamVolume(AudioManager.STREAM_MUSIC,Math.min(maxVolume, ++currentVolume), 0);
//		} else {
//			mAudioManager.setStreamVolume(AudioManager.STREAM_MUSIC,Math.max(0, --currentVolume), 0);
//		}

		try {
			if (mIDataSet == null) {
				LogHelper.i("setting is null");
				int maxVolume = mAudioManager
						.getStreamMaxVolume(AudioManager.STREAM_MUSIC);
				int currentVolume = mAudioManager
						.getStreamVolume(AudioManager.STREAM_MUSIC);
				if(add){
					mAudioManager.setStreamVolume(AudioManager.STREAM_MUSIC,
							Math.min(maxVolume, ++currentVolume), 0);
				}else{
					mAudioManager.setStreamVolume(AudioManager.STREAM_MUSIC,
							Math.max(0, --currentVolume), 0);
				}
	
			}else{
				mIDataSet.changeChannelVoluome(getVolumeKey(), add, true);
			}
		} catch (RemoteException e) {
			e.printStackTrace();
		}
		
		changeMute(false);
	}

	@Override
	public void changeMute(boolean mute) {
		// TODO Auto-generated method stub
		LogHelper.i("setMute " + mute);
		ismute = mute;
		mAudioManager.setStreamMute(AudioManager.STREAM_MUSIC, mute);
		mCallBack.onMuteStateChange(mute);
	}
	@Override
	public int getSoundTrackIndex() {
		// TODO Auto-generated method stub
		int result=0;
		if(mIDataSet!=null){
			try {
				result=mIDataSet.getSound_Track();
			} catch (RemoteException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		return result;
	}
	@Override
	public int getVideoScaleIndex() {
		// TODO Auto-generated method stub
		int result=0;
//		if(mIDataSet!=null){
//			try {
//				result=mIDataSet.getDisplayRatio();
//			} catch (RemoteException e) {
//				// TODO Auto-generated catch block
//				e.printStackTrace();
//			}
//		}
		return result;
	}
	@Override
	public void saveHistoryChannel(LiveChannel channel) {
		// TODO Auto-generated method stub
		String name=channel.getName();
		long freq=channel.getChannelKey().getFrequency();
		int service=channel.getChannelKey().getProgram();
		int number=channel.getChannelNumber();
		mSharedPreferencesMenager.saveChannelData(name,number, freq, service,channel.getTsId());
	}
	@Override
	public void setChannelVolume(LiveChannel channel) {
		// TODO Auto-generated method stub
		if(!ismute){
			if(mIDataSet!=null&&channel!=null){
				try {
					mIDataSet.setChannelVoluome(getVolumeKey(), true);
				} catch (RemoteException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}else{
				mAudioManager.setStreamVolume(AudioManager.STREAM_MUSIC, 
						mAudioManager.getStreamVolume(AudioManager.STREAM_MUSIC), 0);
			}
		}
	}
	@Override
	public String[] getSaveHistoryChannel() {
		// TODO Auto-generated method stub
		return new String[]{mSharedPreferencesMenager.getSaveChannelName(),mSharedPreferencesMenager.getSaveChannel()+"",mSharedPreferencesMenager.getSaveFreq()+"",mSharedPreferencesMenager.getSaveProg()+""};
	}
	
	private String getVolumeKey(){
		LiveChannel channel=mContext.getStationManager().getPlayChannel();
		if(channel!=null){
			return channel.getChannelKey().getProgram()+"";
		}
		return "";
	}
	@Override
	public void saveVolumeData() {
		// TODO Auto-generated method stub
		Intent intentb = new Intent();
		intentb.setAction("com.ipanel.SYSTEM_HIDE_VOLUME_BAR");
		mContext.getContext().sendBroadcast(intentb);
		mCallBack.onMuteStateChange(getMute());
		enter_vol = mAudioManager.getStreamVolume(AudioManager.STREAM_MUSIC);
	}
	@Override
	public void restoreVolumeData() {
		// TODO Auto-generated method stub
		if (!isGlobalVoiceControl()) {
			if (getMute()) {
				mAudioManager.setStreamMute(AudioManager.STREAM_MUSIC, true);
			}
			mAudioManager.setStreamVolume(AudioManager.STREAM_MUSIC, enter_vol, 0);
		} else {
			if (getMute()) {
				mAudioManager.setStreamMute(AudioManager.STREAM_MUSIC, false);
				int vol = mAudioManager.getStreamVolume(AudioManager.STREAM_MUSIC);
				mAudioManager.setStreamMute(AudioManager.STREAM_MUSIC, true);
				mAudioManager.setStreamVolume(AudioManager.STREAM_MUSIC, vol, 0);
			}
		}
	}
	private boolean isGlobalVoiceControl() {
		if (mIDataSet != null) {
			try {
				return mIDataSet.isGlobalVolumeControl();
			} catch (RemoteException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		return true;
	}
	@Override
	public void changeSoundTraceIndex() {
		// TODO Auto-generated method stub
		int index=getSoundTrackIndex();
		index=(index+1)%StationManager.SOUND_FLAGS.length;
		LogHelper.i("set sound track index :"+index);
//		if(mIDataSet!=null){
//			try {
//				mIDataSet.setSound_Track(index);
//			} catch (RemoteException e) {
//				// TODO Auto-generated catch block
//				e.printStackTrace();
//			}
//		}
	}
	@Override
	public void changeVideoScaleIndex() {
		// TODO Auto-generated method stub
		int index=getVideoScaleIndex();
		index=(index+1)%StationManager.SCALE_FLAGS.length;
		LogHelper.i("set sound track index :"+index);
//		if(mIDataSet!=null){
//			try {
//				mIDataSet.setDisplayRatio(index);
//			} catch (RemoteException e) {
//				// TODO Auto-generated catch block
//				e.printStackTrace();
//			}
//		}
	}
	
	public void initAllService(){
		mContext.getContext().bindService(new Intent(Constant.ACTION_SETTING_SERVICE),
				mSettingConnection, Context.BIND_AUTO_CREATE);
//		mContext.getContext().bindService(new Intent(Constant.AUTH_SERVICE_NAME), mAuthConnection,
//				Context.BIND_AUTO_CREATE);
	}
	
	public void destroyData(){
		if (mIDataSet != null) {
			mContext.getContext().unbindService(mSettingConnection);
		}
//		if (mIAuth != null) {
//			mContext.getContext().unbindService(mAuthConnection);
//		}
	}
	@Override
	public String getShiftRequestUrl() {
		// TODO Auto-generated method stub
		String result = "";
		if (Constant.DEVELOPER_MODE) {
			result = "ac";
		} else {
//			if (mIAuth != null) {
//				try {
//					result = mIAuth.getEPGServerURL();
//				} catch (RemoteException e) {
//					// TODO Auto-generated catch block
//					e.printStackTrace();
//				}
//			}
		}
		return result;
	}

	@Override
	public String getShiftRequestCookies() {
		// TODO Auto-generated method stub
		String result = "";
		if (Constant.DEVELOPER_MODE) {
			result = "ac";
		} else {
//			if (mIAuth != null) {
//				try {
//					result = mIAuth.getCookieString();
//				} catch (RemoteException e) {
//					// TODO Auto-generated catch block
//					e.printStackTrace();
//				}
//			}
		}
		return result;
	}

	@Override
	public String getShiftRequestTsString() {
		// TODO Auto-generated method stub
		String result = "";
		if (Constant.DEVELOPER_MODE) {
			result = "ac";
		} else {
//			if (mIAuth != null) {
//				try {
//					result = mIAuth.getTsString();
//				} catch (RemoteException e) {
//					// TODO Auto-generated catch block
//					e.printStackTrace();
//				}
//			}
		}
		return result;
	}
	private ServiceConnection mSettingConnection = new ServiceConnection() {// “Ù¡ø±£¥Êsercice

		public void onServiceConnected(ComponentName className, IBinder service) {
			LogHelper.v("IDataSetService  Connection");
			mIDataSet = IDataSet.Stub.asInterface(service);

		}

		public void onServiceDisconnected(ComponentName className) {
			mIDataSet = null;
		}
	};
//	ServiceConnection mAuthConnection = new ServiceConnection() {
//		@Override
//		public void onServiceConnected(ComponentName name, IBinder service) {
//			mIAuth = IAuth.Stub.asInterface(service);
//			LogHelper.i("mAuthConnection  connected ");
//		}
//		@Override
//		public void onServiceDisconnected(ComponentName name) {
//			mIAuth = null;
//		}
//	};
}
