package com.ipanel.join.cq.vod.player.impl;

import com.ipanel.chongqing_ipanelforhw.Config;

import ipaneltv.toolkit.UriToolkit;
import ipaneltv.toolkit.media.IpQamTsPlayer;
import ipaneltv.toolkit.media.LocalSockTsPlayer;
import ipaneltv.toolkit.media.MediaSessionFragment;
import ipaneltv.toolkit.media.TeeveePlayerBase;
import ipaneltv.toolkit.media.TsLocalSockSourceBase;
import ipaneltv.toolkit.media.TsPlayerInetSource;
import android.graphics.Rect;
import android.media.TeeveePlayer;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.Message;
import android.util.Log;

public class VodFragment extends MediaSessionFragment {
	static final String TAG = VodFragment.class.getName();
	private WasuPlayerSource source;
	private IpQamPlayer qamPlayer;
	private LocalSockPlayer localsockPlayer;
	private VodPlayManager playManager;

	/**
	 * 创建实例
	 * 
	 * @param playServiceName
	 *            播放服务名称
	 * @param sourceServiceName
	 *            原服务名称
	 * @return
	 */
	public static VodFragment createInstance(String playServiceName, String sourceServiceName) {
		Bundle b = MediaSessionFragment.createArguments(Config.UUID, playServiceName,
				sourceServiceName);
		VodFragment f = new VodFragment();
		f.setArguments(b);
		return f;
	}

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		playManager = new VodPlayManager(this);
		source = new WasuPlayerSource(playManager);
		qamPlayer = new IpQamPlayer(playManager);
		localsockPlayer = new LocalSockPlayer(playManager);
		// 交由Fragment托管
		// 1 当Activity隐藏或退出时自动Loosen
		// 2 发生同组资源竞争时，自动Loosen
		entrustSession("source", source);
		entrustSession("player", qamPlayer);
		entrustSession("player", localsockPlayer);
		playManager.prepare();// 启动异步线程
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

	WasuPlayerSource getSource() {
		if (chooseSession(source))
			return source;
		return null;
	}

	IpQamPlayer getIpQamPlayer() {
		if (chooseSession(qamPlayer))
			return qamPlayer;
		return null;
	}

	LocalSockPlayer getLocalSockPlayer() {
		if (chooseSession(localsockPlayer))
			return localsockPlayer;
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
	public void onStop() {
		Log.d(TAG, "onStop");
		super.onStop();
	}
	
	@Override
	public void onPause() {
		Log.d(TAG, "onPause");
		super.onPause();
	}

	@Override
	public void onResume() {
		super.onResume();
	}
}

class VodPlayManager implements PlayInterface {
	static final String TAG = VodPlayManager.class.getName();
	VodFragment host;
	HandlerThread handlerThread;
	Handler sendHandler, callbackHandler;
	private Object playCountMutex = new Object();
	private volatile int playCount = 0, playResponseCount = 0;

	VodPlayManager(VodFragment f) {
		host = f;
	}

	void prepare() {
		handlerThread = new HandlerThread("VodPlayManager");
		handlerThread.start();
		sendHandler = new Handler(handlerThread.getLooper(), handlerProc);
	}

	void dispose() {
		handlerThread.getLooper().quit();
		release();
	}

	void release() {
		synchronized (TAG) {
			if (source != null) {
				source.stop();
				source = null;
			}
			if (player != null) {
				player.stop(0);
				player = null;
			}
		}
	}

	final void postProc(Runnable r) {
		sendHandler.post(r);
	}

	@Override
	public void play(final String uri, final int type, final int streamType, final int flags) {
		playCount++;
		postProc(new Runnable() {
			@Override
			public void run() {
				procClearPlayState();
				boolean b = true;
				try {
					WasuPlayerSource source = host.getSource();
					source.start(uri, type, streamType, flags);
					Log.d(TAG, "play uri=" + uri);
					Log.d(TAG, "type=" + type + ",streamType=" + streamType + ",flags=" + flags);
				} catch (Exception e) {
					Log.d(TAG, "play error:" + e);
					b = false;
				} finally {
					Log.i(TAG, "b="+b);
					if (!b && callbackHandler != null) {
						callbackHandler.obtainMessage(PlayCallback.onSourceStart, Boolean.FALSE)
								.sendToTarget();
					}
				}
			}
		});
	}

	@Override
	public void stop() {
		sendHandler.obtainMessage(PlayInterface.stop).sendToTarget();
	}

	@Override
	public void pause() {
		sendHandler.obtainMessage(PlayInterface.pause).sendToTarget();
	}

	@Override
	public void setDisplay(Rect rect) {
		Message msg = sendHandler.obtainMessage();
		msg.what = PlayInterface.setDisplay;
		msg.obj = rect;
		msg.sendToTarget();
	}

	@Override
	public void resume() {
		sendHandler.obtainMessage(PlayInterface.resume).sendToTarget();
	}

	@Override
	public void seek(long time) {
		sendHandler.obtainMessage(PlayInterface.seek, Long.valueOf(time)).sendToTarget();
	}

	@Override
	public void setRate(float rate) {
		sendHandler.obtainMessage(PlayInterface.setRate, Float.valueOf(rate)).sendToTarget();
	}

	@Override
	public void setVolume(float v) {
		sendHandler.obtainMessage(PlayInterface.setVolume, Float.valueOf(v)).sendToTarget();
	}

	@Override
	public void setProgramFlag(int flags) {
		sendHandler.obtainMessage(PlayInterface.setProgramFlag, Integer.valueOf(flags))
				.sendToTarget();
	}

	/** 在此定义内部使用的消息值，基于负值定义 **/
	public static final int CALLBACK_SERVIE_READY = -1;
	final int CALLBACK_PLAYED = -2;

	void listen(final PlayCallback callback) {
		callbackHandler = new Handler() {
			@Override
			public void handleMessage(Message msg) {
				boolean drop = false;
				synchronized (playCountMutex) {
					/** 保证两次start之间的消息有效，否则drop掉 **/
					if (msg.what == PlayCallback.onSourceStart)
						playResponseCount++;
					drop = (playResponseCount != playCount);
				}
				Log.d(TAG, "listen handleMessage what =" + msg.what + ",drop=" + drop);
				if (drop) {
					dropMessage(msg);
					return;
				}
				switch (msg.what) {
				case PlayCallback.onPlayErrorId:
					callback.onPlayErrorId((Integer) msg.obj);
					break;
				case PlayCallback.onPlayError:
					callback.onPlayError((String) msg.obj);
					break;
				case PlayCallback.onPlayTime:
					callback.onPlayTime((Long) msg.obj);
					break;
				case PlayCallback.onSourceStart:
					callback.onSourceStart((Boolean) msg.obj);
					break;
				case PlayCallback.onPlayEnd:
					callback.onPlayEnd();
					break;
				case PlayCallback.onPlayMsgId:
					callback.onPlayMsgId((Integer) msg.obj);
					break;
				case PlayCallback.onPlayMsg:
					callback.onPlayMsg((String) msg.obj);
					break;
				case PlayCallback.onVodDuration:
					callback.onVodDuration((Long) msg.obj);
					break;
				case PlayCallback.onSeeBackPeriod:
					callback.onSeeBackPeriod(((Long[]) msg.obj)[0], ((Long[]) msg.obj)[1]);
					break;
				case PlayCallback.onShiftStartTime:
					callback.onShiftStartTime((Long) msg.obj);
					break;
				case PlayCallback.onSourceSeek:
					Log.i(TAG, "onSourceSeek----");
					callback.onSourceSeek((Long) msg.obj);
					break;
				case PlayCallback.onSyncMediaTime:
					callback.onSyncMediaTime((Long) msg.obj);
					break;
				case PlayCallback.onSourceRate:
					callback.onSourceRate((Float) msg.obj);
					break;
				case PlayCallback.onPlayStart:
					callback.onPlayStart((Boolean) msg.obj);
					break;
				case CALLBACK_PLAYED:
					sendHandler.obtainMessage(CALLBACK_PLAYED, msg.obj).sendToTarget();
					break;
				case CALLBACK_SERVIE_READY:
					callback.onServiceReady();
					break;
				default:
					break;
				}
			}
		};
	}

	void dropMessage(Message msg) {
	}

	static final int IPQAM_MODE = 1;
	static final int INET_MODE = 2;
	private int play_mode = IPQAM_MODE;
	private TeeveePlayerBase player = null;
	private TsLocalSockSourceBase source = null;

	void procClearPlayState() {
		if (player != null) {
			player.stop(0);
		}
		if (source != null) {
			source.stop();
		}
		this.puri = null;
		this.localSock = null;
		this.furi = null;
		this.fflags = 0;
		this.pflags = 0;
		this.player = null;
		this.playeed = false;
	}

	void procPlayIpqam(String furi, String puri) {
		this.furi = furi;
		this.puri = puri;
		play_mode = IPQAM_MODE;
		player = host.getIpQamPlayer();
		host.getIpQamPlayer().start(furi, 0, puri, 0);
		host.getIpQamPlayer().setVolume(1f);
	}

	private String localSock = null;
	private String puri = null;
	@SuppressWarnings("unused")
	private String furi = null;
	@SuppressWarnings("unused")
	private int fflags = 0, pflags = 0;
	private boolean playeed = false;

	private int checkPlayedUri(String f, String p) {
		if ((localSock != null && puri == null) || (localSock == null && puri != null))
			throw new RuntimeException("impl err");
		if (localSock == null)
			return 1;
		boolean fb = localSock.equals(f), pb = puri.equals(p);
		if (!fb && !pb) { // 源与节目都不相同
			localSock = null;
			puri = null;
			return 1;
		} else if (fb && pb) { // 源与节目相同
			return 0;
		} else if (!fb && pb) { // 源不同而节目相同
			return 2;
		} else if (fb && !pb) { // 源相同而节目不同
			return 3;
		} else {
			throw new RuntimeException("impl err2");
		}
	}

	void procPlayLocalSock(String localSock, String puri) {
		Uri uri = Uri.parse(localSock);
		localSock = UriToolkit.makeLocalSockUri(uri.getAuthority(), uri.getQuery(), -259000000);

		play_mode = INET_MODE;
		player = host.getLocalSockPlayer();
		int result = checkPlayedUri(localSock, puri);
		Log.d(TAG, "procPlayLocalSock>localSock=" + localSock + ",puri=" + puri);
		switch (result) {
		case 1:
			this.localSock = localSock;
			this.puri = puri;
			host.getLocalSockPlayer().start(localSock, 0, puri, pflags);
			break;
		case 2:
			this.localSock = localSock;
			host.getLocalSockPlayer().redirect(localSock, 0);
			break;
		case 3:
			this.puri = puri;
			host.getLocalSockPlayer().start(localSock, 0, puri, pflags);
			break;
		case 0:
		default:
			break;
		}
		host.getLocalSockPlayer().setVolume(1f);
	}

	void onSourcePlayed(String furi, String puri) {
		String msg = "onSourcePlayed success";
		switch (UriToolkit.getSchemaId(furi)) {
		case UriToolkit.FREQUENCY_INFO_SCHEMA_ID:
			if (UriToolkit.getSchemaId(puri) == UriToolkit.DVB_SERVICE_SCHEMA_ID
					|| UriToolkit.getSchemaId(puri) == UriToolkit.PMT_SCHEMA_ID) {
				procPlayIpqam(furi, puri);
			} else {
				// getCallback().obtainMessage(PlayCallback.onPlayErrorId,
				// Integer.valueOf(R.string.errformat)).sendToTarget();
				// return;
			}
			break;
		case UriToolkit.LOCALSOCK_SCHEMA_ID:
			if (UriToolkit.getSchemaId(furi) == UriToolkit.LOCALSOCK_SCHEMA_ID) {
				procPlayLocalSock(furi, puri);
			} else {
				// getCallback().obtainMessage(PlayCallback.onPlayErrorId,
				// Integer.valueOf(R.string.errformat)).sendToTarget();
				// return;
			}
			break;
		default:
			msg = "onSourcePlayed failed";
			getCallback().obtainMessage(PlayCallback.onPlayError, msg).sendToTarget();
			return;
		}
		playeed = true;
		getCallback().obtainMessage(PlayCallback.onPlayMsg, msg).sendToTarget();
	}

	void procPlayStart(Message msg) {
		onSourcePlayed(((String[]) msg.obj)[0], ((String[]) msg.obj)[1]);
	}

	private boolean seeking = false;
	private Long seekpos = null;
	
	Handler.Callback handlerProc = new Handler.Callback() {
		@Override
		public boolean handleMessage(Message msg) {
			Log.d(TAG, "Handler.Callback handleMessage what=" + msg.what);
			try {
				switch (msg.what) {
				case CALLBACK_PLAYED:
					procPlayStart(msg);
					return true;
				case setVolume:
					if (playeed && player != null)
						player.setVolume((Float) msg.obj);
					return true;
				case setProgramFlag:
					if (playeed && player != null)
						player.setProgramFlags((Integer) msg.obj);
					return true;
				default:
					break;
				}
			} catch (Exception e) {
				Log.d(TAG, "on internal message error:" + e);
				e.printStackTrace();
				return true;
			}

			try {
				WasuPlayerSource src = host.getSource();
				switch (msg.what) {
				case stop:
					src.stop();
					if (playeed && player != null)
						player.stop(TeeveePlayer.FLAG_VIDEO_FRAME_BLACK);
					break;
				case pause:
					src.pause();
					if (playeed && player != null)
						player.pause();
					break;
				case resume:
					src.resume();
					if (playeed && player != null){
							player.resume();
							if (play_mode == IPQAM_MODE){
								Log.i(TAG, "play_mode == IPQAM_MODE");
								host.getIpQamPlayer().setRate(1);
							}
							else if (play_mode == INET_MODE){
								Log.i(TAG, "play_mode == INET_MODE");
								host.getLocalSockPlayer().setRate(1);
							}
						}
					break;
				case seek:
					Log.d(TAG, "seek t=" + msg.obj + ", seeking = " + seeking);
					if (seeking) {
						seekpos = (Long) msg.obj;
					} else {
						seeking = true;
						src.seek((Long) msg.obj);
					}
					break;
				case setRate:
					Log.d(TAG, "setRate r=" + msg.obj);
					src.setRate((Float) msg.obj);
					Log.i(TAG, "play_mode =" + play_mode+",playeed:" + playeed+",player == null？"+(player == null));
//					if (playeed || player == null){
//						break;
//					}
					if (player == null){
						break;
					}
					if (play_mode == IPQAM_MODE){
						Log.i(TAG, "play_mode == IPQAM_MODE");
						host.getIpQamPlayer().setRate((Float) msg.obj);
					}
					else if (play_mode == INET_MODE){
						Log.i(TAG, "play_mode == INET_MODE");
						host.getLocalSockPlayer().setRate((Float) msg.obj);
					}
					break;
				case setDisplay:
					Log.d(TAG, "setDisplay t=" + msg.obj);
					Rect rect = (Rect) msg.obj;
					if (playeed && player != null)
						player.setDisplay(rect);
					break;
				case PlayCallback.onSourceSeek:
					Log.d(TAG, "proc PlayCallback.onSourceSeek:seeking = " + seeking
							+ ", seekpos = " + seekpos);
					if (seeking)
						seeking = false;
					if (seekpos != null) {
						src.seek(seekpos);
						seeking = true;
						seekpos = null;
					}
					break;
				default:
					break;
				}
			} catch (Exception e) {
				Log.d(TAG, "on activity message error:" + e);
				e.printStackTrace();
				return false;
			}

			return true;
		}
	};

	Handler getCallback() {
		return callbackHandler;
	}
}

class WasuPlayerSource extends TsPlayerInetSource {
	VodPlayManager manager;

	public WasuPlayerSource(VodPlayManager manager) {
		super(manager.host.getActivity(), manager.host.getSourceServiceName(), Provider.Huawei.getName());
		Log.d(this.toString(), "WasuPlayerSource");
		this.manager = manager; 
	}

	@Override
	public void onResponseStart(boolean b) {
		manager.getCallback().obtainMessage(PlayCallback.onSourceStart, Boolean.valueOf(b))
				.sendToTarget();
	}

	@Override
	public void onSourceError(String msg) {
		manager.getCallback().obtainMessage(PlayCallback.onPlayError, msg).sendToTarget();
	}

	@Override
	public void onSourceMessage(String msg) {
		manager.getCallback().obtainMessage(PlayCallback.onPlayMsg, msg).sendToTarget();
	}

	@Override
	public void onEndOfSource(float rate) {
		manager.getCallback().obtainMessage(PlayCallback.onPlayEnd, Float.valueOf(rate))
				.sendToTarget();
	}

	@Override
	public void onSourcePlayed(String streamUri, String programUri) {
		/** 必须这样处理 ,避免处理过时消息 **/
		manager.getCallback()
				.obtainMessage(manager.CALLBACK_PLAYED, new String[] { streamUri, programUri })
				.sendToTarget();
	}

	@Override
	public void onSeeBackPeriod(long start, long end) {
		manager.getCallback()
				.obtainMessage(PlayCallback.onSeeBackPeriod, new Long[] { start, end })
				.sendToTarget();
	}

	@Override
	public void onShiftStartTime(long start) {
		manager.getCallback().obtainMessage(PlayCallback.onShiftStartTime, Long.valueOf(start))
				.sendToTarget();
	}

	@Override
	public void onSourceRate(float r) {
		manager.getCallback().obtainMessage(PlayCallback.onSourceRate, Float.valueOf(r))
				.sendToTarget();
	}

	@Override
	public void onVodDuration(long d) {
		manager.getCallback().obtainMessage(PlayCallback.onVodDuration, Long.valueOf(d))
				.sendToTarget();
	}

	@Override
	public void onSourceSeek(long t) {
		Log.i("WasuPlayerSource", "onSourceSeek-----t = "+t);
		manager.getCallback().obtainMessage(PlayCallback.onSourceSeek, Long.valueOf(t))
				.sendToTarget();
		manager.sendHandler.obtainMessage(PlayCallback.onSourceSeek, Long.valueOf(t))
				.sendToTarget();
	}

	@Override
	public void onSyncMediaTime(long t) {
		manager.getCallback().obtainMessage(PlayCallback.onSyncMediaTime, Long.valueOf(t))
				.sendToTarget();
	}
}

class IpQamPlayer extends IpQamTsPlayer {
	VodPlayManager manager;

	public IpQamPlayer(VodPlayManager manager) {
		super(manager.host.getActivity(), manager.host.getPlayServiceName());
		this.manager = manager;
	}

	@Override
	public void onResponseStart(boolean succ) {
		super.onResponseStart(succ);
		manager.getCallback().obtainMessage(PlayCallback.onPlayStart, Boolean.valueOf(succ))
				.sendToTarget();
	}
}

class LocalSockPlayer extends LocalSockTsPlayer {
	VodPlayManager manager;

	public LocalSockPlayer(VodPlayManager manager) {
		super(manager.host.getActivity(), manager.host.getPlayServiceName());
		this.manager = manager;
	}

	@Override
	public void onResponseStart(boolean succ) {
		super.onResponseStart(succ);
		manager.getCallback().obtainMessage(PlayCallback.onPlayStart, Boolean.valueOf(succ))
				.sendToTarget();
	}
}