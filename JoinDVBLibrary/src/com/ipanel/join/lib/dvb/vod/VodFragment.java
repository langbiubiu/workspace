package com.ipanel.join.lib.dvb.vod;

import ipaneltv.toolkit.media.MediaSessionFragment;
import android.os.Bundle;
import android.util.Log;

public class VodFragment extends MediaSessionFragment {
	static final String TAG = VodFragment.class.getName();
	private PlayerSource source;
	private IpQamPlayer qamPlayer;
	private LocalSockPlayer localsockPlayer;
	private VodPlayManager playManager;
	private HomedPlayer homedPlayer;
	private String provider;
	private boolean paused = false;

	/**
	 * 创建实例
	 * 
	 * @param playServiceName
	 *            播放服务名称
	 * @param sourceServiceName
	 *            原服务名称
	 * @return
	 */
	public static VodFragment createInstance(String uuid,String provider,String playServiceName, String sourceServiceName) {
		Bundle b = MediaSessionFragment.createArguments(uuid, playServiceName,
				sourceServiceName);
		VodFragment f = new VodFragment();
		b.putString("provider", provider);
		f.setArguments(b);
		return f;
	}

	public VodFragment() {
		playManager = new VodPlayManager(this);
	}
	
	@Override
	public void onCreate(Bundle savedInstanceState) {
		this.provider = getArguments().getString("provider");
		super.onCreate(savedInstanceState);
		setRetainInstance(true);
		source = new PlayerSource(playManager,provider);
		qamPlayer = new IpQamPlayer(playManager);
		localsockPlayer = new LocalSockPlayer(playManager);
		homedPlayer = new HomedPlayer(playManager);
		
		// 交由Fragment托管
		// 1 当Activity隐藏或退出时自动Loosen
		// 2 发生同组资源竞争时，自动Loosen
		entrustSession("source", source);
		entrustSession("player", qamPlayer);
		entrustSession("player", localsockPlayer);
		entrustSession("player", homedPlayer);
		playManager.prepare();// 启动异步线程
	}

	public String getProvider(){
		return provider;
	}
	
	@Override
	public void onStart() {
		super.onStart();
	}

	@Override
	public void onDestroy() {
		playManager.dispose();// 退出异步线程
		super.onDestroy();
	}

	private boolean bSourceReady = false, bPlayerReady = false;

	@Override
	public void onAllEntrusteeConnected(String groug) {
		super.onAllEntrusteeConnected(groug);

		if ("source".equals(groug)) {
			bSourceReady = true;
		}

		if ("player".equals(groug)) {
			bPlayerReady = true;
		}

		if (bSourceReady && bPlayerReady) {
			playManager.callbackHandler.obtainMessage(VodPlayManager.CALLBACK_SERVIE_READY)
					.sendToTarget();
		}
	}

	PlayerSource getSource() {
		Log.d(TAG, "getSource paused = "+ paused);
		if(!paused){
			if (chooseSession(source))
				return source;
		}
		return null;
	}

	HomedPlayer getHomedPlayer(){
		if(!paused){
			if(chooseSession(homedPlayer))
				return homedPlayer;
		}
		return null;
	}
	
	HomedPlayer tryGetHomedPlayer(){
		if(isSessionChoosed(homedPlayer))
			return homedPlayer;
		return null;
	}
	
	IpQamPlayer getIpQamPlayer() {
		if(!paused){
			if (chooseSession(qamPlayer))
				return qamPlayer;
		}
		return null;
	}
	

	LocalSockPlayer getLocalSockPlayer() {
		if(!paused){
			if (chooseSession(localsockPlayer))
				return localsockPlayer;	
		}
		return null;
	}

	@Override
	public String getSourceServiceName() {
		String serviceName = getArguments().getString(MediaSessionFragment.ARG_SOURCE_SERVICE_NAME);
		if (serviceName == null)
			throw new RuntimeException("source service name missing!");
		return serviceName;
	}

	@Override
	public String getPlayServiceName() {
		String serviceName = getArguments().getString(MediaSessionFragment.ARG_PLAY_SERVICE_NAME);
		if (serviceName == null)
			throw new RuntimeException("play service name missing!");
		return serviceName;
	}

	public PlayInterface getPlayInterface(PlayCallback callback) {
		playManager.listen(callback);
		return playManager;
	}

	@Override
	public void onPause() {
		paused = true;
		Log.d(TAG, "onPause");
		playManager.suspend();
		super.onPause();
	}

	@Override
	public void onResume() {
		paused = false;
		Log.d(TAG, "onResume 1");
		super.onResume();
		playManager.setVolume(0.5f);
	}
}
