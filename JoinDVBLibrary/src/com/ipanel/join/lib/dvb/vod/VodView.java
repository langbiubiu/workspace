package com.ipanel.join.lib.dvb.vod;

import com.ipanel.join.lib.dvb.DVBConfig;

import ipaneltv.toolkit.IPanelLog;
import ipaneltv.toolkit.media.MediaSessionInterface.TsPlayerInetSourceInterface;
import android.app.Activity;
import android.app.FragmentManager;
import android.appwidget.AppWidgetManager;
import android.appwidget.AppWidgetProviderInfo;
import android.appwidget.TeeveeWidgetHost;
import android.appwidget.TeeveeWidgetHostView;
import android.content.Context;
import android.graphics.Rect;
import android.net.telecast.NetworkManager;
import android.util.AttributeSet;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.SurfaceView;
import android.view.View;
import android.widget.FrameLayout;

public class VodView extends FrameLayout implements PlayCallback {
	public static final int STATE_IDLE = 0;
	public static final int STATE_PLAYING = 1;
	public static final int STATE_PAUSE = 2;
	public static final int STATE_STOPPED = 3;

	static final String TAG = VodView.class.getSimpleName();

	protected AppWidgetManager mAppWidgetManager;
	protected TeeveeWidgetHost mAppWidgetHost;
	protected AppWidgetProviderInfo appWidgetInfo;
	protected TeeveeWidgetHostView hostView;
	protected int appWidgetId;

	protected int sourceType = TsPlayerInetSourceInterface.TYPE_VOD;
	protected int sourceStreamType = DVBConfig.getVodStreamType();

	protected PlayInterface playIntf;

	protected boolean contextReady;

	public VodView(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);
		init();
	}

	public VodView(Context context, AttributeSet attrs) {
		super(context, attrs);
		init();
	}

	public VodView(Context context) {
		super(context);
		init();
	}

	private void init() {
		View v = initWidget();
		if (v == null) {
			v = new SurfaceView(getContext());
		}
		addView(v, 0, new LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT));
		initFragments();
	}

	private void initFragments() {
		if (getContext() instanceof Activity) {
			FragmentManager fm = ((Activity) getContext()).getFragmentManager();
			final String tag = "_vod_play_";
			VodFragment pf = (VodFragment) fm.findFragmentByTag(tag);
			if (pf == null) {
				pf = VodFragment.createInstance(DVBConfig.getUUID(), DVBConfig.getVodProvider(),
						DVBConfig.getPlayService(), DVBConfig.getSourceService());
				fm.beginTransaction().add(pf, tag).commit();
			}
			playIntf = pf.getPlayInterface(this);
		}

	}

	public View initWidget() {
		try {
			Context ctx = getContext().getApplicationContext();
			int hostid = 100;
			String netid = DVBConfig.getUUID();
			String wname = NetworkManager.PROPERTY_TEEVEE_WIDGET;
			NetworkManager networkManager = DVBConfig.getNetworkManager();
			mAppWidgetManager = AppWidgetManager.getInstance(ctx);
			mAppWidgetHost = new TeeveeWidgetHost(ctx, hostid);
			mAppWidgetHost.startListening();
			appWidgetId = mAppWidgetHost.allocateAppWidgetId();
			if (networkManager.bindNetworkTeeveeWidgetId(netid, appWidgetId, wname)) {
				appWidgetInfo = mAppWidgetManager.getAppWidgetInfo(appWidgetId);
				hostView = (TeeveeWidgetHostView) mAppWidgetHost.createView(ctx, appWidgetId,
						appWidgetInfo);
				hostView.setId(appWidgetId);
				return hostView;
			} else {
				IPanelLog.i(TAG, "bindNetworkTeeveeWidgetId failed!");
			}
		} catch (Exception e) {
			IPanelLog.e(TAG, "onCreateView error:" + e, e);
		}
		return new SurfaceView(getContext());
	}

	public PlayInterface getPlayInterface() {
		return playIntf;
	}

	protected String playUrl;
	protected int playFlags = 0;
	protected long duration = 0;
	protected int playState = 0;
	protected long playPosition = 0;
	protected long pendingSeek = -1;
	protected float rate = 1;
	protected PTSCalculator ptsCalculator = new PTSCalculator();

	@Override
	public void onServiceReady() {
		Log.d(TAG, "onServiceReady");
		contextReady = true;
		if (playUrl != null)
			playIntf.play(playUrl, sourceType, sourceStreamType, playFlags);

	}

	@Override
	public void onWindowFocusChanged(boolean hasWindowFocus) {
		if(hasWindowFocus)
			setVolumeDisplay();
	}

	@Override
	protected void onLayout(boolean changed, int left, int top, int right, int bottom) {
		super.onLayout(changed, left, top, right, bottom);
		post(new Runnable() {
			
			@Override
			public void run() {
				setVolumeDisplay();
				
			}
		});
	}

	protected void setVolumeDisplay() {
		playIntf.setVolume(0.5f);
		//DisplayMetrics dm = getResources().getDisplayMetrics();
		setAutoVideoBounds();
	}
	
	public void setAutoVideoBounds() {
		Rect r = new Rect();
		getGlobalVisibleRect(r);
		playIntf.setDisplay(r);
	}

	@Override
	public void onPlayErrorId(int string_id) {
		Log.d(TAG, "onPlayErrorId id = " + string_id);

	}

	@Override
	public void onPlayError(String msg) {
		Log.d(TAG, "onPlayError msg = " + msg);
		if(mPlayListener != null)
			mPlayListener.onError(1, msg);
	}

	@Override
	public void onPlayTime(long time) {
		Log.d(TAG, "onPlayTime time=" + time);
		if (time >= 0)
			playPosition = time;
		ptsCalculator.onPlayTime(time);

	}

	@Override
	public void onVodDuration(long d) {
		Log.d(TAG, "onVodDuration d=" + d + ", original duration=" + duration);
		if (d > 0)
			duration = d;
	}

	@Override
	public void onSeeBackPeriod(long s, long e) {
		// TODO Auto-generated method stub

	}

	@Override
	public void onShiftStartTime(long t) {
		// TODO Auto-generated method stub

	}

	@Override
	public void onSourceStart(boolean b) {
		Log.d(TAG, "onSourceStart b=" + b);

	}

	@Override
	public void onPlayStart(boolean b) {
		Log.d(TAG, "onPlayStart b=" + b);
		if (b) {
			playState = STATE_PLAYING;
			setVolumeDisplay();
		}
	}

	@Override
	public void onSourceRate(float r) {
		Log.d(TAG, "onSourceRate r=" + r);
		rate = r;
	}

	@Override
	public void onSourceSeek(long t) {
		Log.d(TAG, "onSourceSeek t=" + t);
		pendingSeek = -1;
		playPosition = t;
		ptsCalculator.onSourceSeek(t);
	}

	@Override
	public void onPlayEnd() {
		Log.d(TAG, "onPlayEnd");
		notifyPlayEnd();
	}

	@Override
	public void onPlayMsgId(int string_id) {
		Log.d(TAG, "onPlayMsgId id = " + string_id);

	}

	@Override
	public void onPlayMsg(String msg) {
		Log.d(TAG, "onPlayMsg msg = " + msg);

	}

	@Override
	public void onSyncMediaTime(long t) {
		playPosition = t;
		ptsCalculator.onSourceSeek(t);
	}

	@Override
	public void onStartPts(long t) {
		ptsCalculator.onStartPts(t);

	}

	public int getSourceType() {
		return sourceType;
	}

	public void setSourceType(int sourceType) {
		this.sourceType = sourceType;
	}

	public int getSourceStreamType() {
		return sourceStreamType;
	}

	public void setSourceStreamType(int sourceStreamType) {
		this.sourceStreamType = sourceStreamType;
	}

	public void play(String url) {
		this.playUrl = url;
		if (contextReady && playIntf != null && url != null) {
			playIntf.play(playUrl, sourceType, sourceStreamType, playFlags);
		}
		resetStates();
	}

	public void seek(long time) {
		Log.d(TAG, "seek time="+time);
		pendingSeek = time;
		playIntf.seek(time);
		playState = STATE_PLAYING;
	}

	public void pause() {
		if (playState == STATE_PLAYING) {
			playIntf.pause();
			playState = STATE_PAUSE;
		}
	}

	public void resume() {
		if (playState == STATE_PAUSE) {
			playIntf.resume();
			playState = STATE_PLAYING;
		}
	}
	
	public void stop(){
		playIntf.stop();
		playState = STATE_STOPPED;
	}

	public void setRate(float rate) {
		if (playState == STATE_PAUSE || playState == STATE_PLAYING) {
			playIntf.setRate(rate);
			playState = STATE_PLAYING;
		}
	}

	public float getRate() {
		return rate;
	}

	private void resetStates() {
		playState = STATE_IDLE;
		duration = 0;
		playPosition = 0;
		pendingSeek = -1;
		rate = 1;
		ptsCalculator.reset();
	}
	
	public int getPlayState(){
		return playState;
	}
	
	public boolean isPlaying(){
		return playState == STATE_PLAYING;
	}

	public long getDuration() {
		return duration;
	}
	
	public void setDuration(long duration){
		Log.d(TAG, "setDuration duration="+duration);
		this.duration = duration;
	}

	public long getCurrentPosition() {
		if(pendingSeek != -1)
			return pendingSeek;
		long elapsed = ptsCalculator.getElapsed();
		if(elapsed > 0 ){
			playPosition = elapsed;
		}
		return playPosition;
	}

	class PTSCalculator {
		boolean isFirstPTSReceive;
		long seekFix;
		long firstPTS;

		long latestPTS;

		int samePTSCount = 0;

		public void onSourceSeek(long seekTime) {
			if (isFirstPTSReceive) {
				latestPTS = firstPTS - seekFix + seekTime;
			} else {
				// seekFix = seekTime;
			}
		}

		public void reset() {
			isFirstPTSReceive = false;
			firstPTS = 0;
			latestPTS = 0;
			seekFix = 0;
			samePTSCount = 0;
		}

		public void onStartPts(long pts) {
			firstPTS = pts;
			isFirstPTSReceive = true;
		}

		public void onPlayTime(long pts) {
			// ignore bad PTS
			if (pts == -1) {
				return;
			}
			if (playState != STATE_PAUSE && pts == latestPTS && duration > 0) {
				samePTSCount++;
				if (samePTSCount > 15 || (samePTSCount > 1 && getElapsed() >= duration - 5000)) {
					Log.d(TAG, "PTS freeze, endPlay");
					notifyPlayEnd();
				}
			} else {
				samePTSCount = 0;
			}
			if (isFirstPTSReceive) {
				if (pts >= firstPTS - seekFix)
					latestPTS = pts;
			} else {
				firstPTS = pts;
				isFirstPTSReceive = true;
			}
		}

		public long getElapsed() {
			if (!isFirstPTSReceive)
				return seekFix;
			if (isFirstPTSReceive && latestPTS >= firstPTS - seekFix) {
				return Math.min(latestPTS - firstPTS + seekFix, duration);
			}
			return 0;
		}
	}

	protected void notifyPlayEnd() {
		playState = STATE_STOPPED;
		if(mPlayListener != null)
			mPlayListener.onComplete();
	}
	
	protected PlayerStateListener mPlayListener;
	
	public void setPlayerStateListener(PlayerStateListener l){
		this.mPlayListener = l;
	}
	
	public interface PlayerStateListener{
		public void onComplete();
		public void onError(int type, String msg);
	}

}
