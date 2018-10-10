package com.ipanel.join.lib.dvb.live;

import cn.ipanel.android.otto.OttoUtils;

import com.ipanel.join.lib.dvb.DVBConfig;
import com.ipanel.join.lib.dvb.DVBConfig.LivePlayerType;
import com.ipanel.join.lib.dvb.OttoEventPFUpdate;
import com.ipanel.join.lib.dvb.live.DatabaseObjects.LiveChannel;
import com.ipanel.join.lib.dvb.live.homed.HomedPlayFragment;

import ipaneltv.toolkit.IPanelLog;
import ipaneltv.toolkit.db.DatabaseObjectification.ChannelKey;
import ipaneltv.toolkit.db.DatabaseObjectification.Program;
import android.app.Activity;
import android.app.FragmentManager;
import android.appwidget.AppWidgetManager;
import android.appwidget.AppWidgetProviderInfo;
import android.appwidget.TeeveeWidgetHost;
import android.appwidget.TeeveeWidgetHostView;
import android.content.Context;
import android.media.TeeveePlayer;
import android.net.telecast.NetworkManager;
import android.net.telecast.StreamSelector;
import android.util.AttributeSet;
import android.util.Log;
import android.view.SurfaceView;
import android.view.View;
import android.widget.FrameLayout;

public class TeeveeView extends FrameLayout implements PlayCallback {
	static final String TAG = TeeveeView.class.getSimpleName();

	protected AppWidgetManager mAppWidgetManager;
	protected TeeveeWidgetHost mAppWidgetHost;
	protected AppWidgetProviderInfo appWidgetInfo;
	protected TeeveeWidgetHostView hostView;
	protected int appWidgetId;

	protected PlayInterface playIntf;

	protected boolean contextReady;
	protected boolean liveInfoUpdated;
	protected ChannelKey playChannel;
	protected String httpUrl;

	protected int freqFlags;
	protected int programFlags;
	
	protected long shiftDelta;
	protected long lastPauseTime;
	protected String shiftUrl;
	protected int sourceType;

	public TeeveeView(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);
		init();
	}

	public TeeveeView(Context context, AttributeSet attrs) {
		super(context, attrs);
		init();
	}

	public TeeveeView(Context context) {
		super(context);
		init();
	}

	NaviManager navi;

	private void init() {
		navi = DVBConfig.getNaviManager();
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
			if(DVBConfig.getLivePlayerType() == LivePlayerType.DVB){
				final String tag = "_teevee_play_";
				PlayFragment pf = (PlayFragment) fm.findFragmentByTag(tag);
				if (pf == null) {
					pf = PlayFragment.createInstance();
					fm.beginTransaction().add(pf, tag).commit();
				}
				playIntf = pf.getPlayInterface(this);
			} else {
				final String tag = "_homed_teevee_play_";
				HomedPlayFragment pf = (HomedPlayFragment) fm.findFragmentByTag(tag);
				if (pf == null) {
					pf = HomedPlayFragment.createInstance();
					fm.beginTransaction().add(pf, tag).commit();
				}
				playIntf = pf.getPlayInterface(this);
			}
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
			IPanelLog.e(TAG, "onCreateView error:" + e);
		}
		return null;
	}

	@Override
	public void onContextReady(String group) {
		Log.d(TAG, "onContextReady group=" + group);
		contextReady = true;
		if (liveInfoUpdated && playChannel != null)
			open(httpUrl, playChannel);
	}

	@Override
	public void onSelectError(String msg) {
		Log.d(TAG, "onSelectError msg=" + msg);

	}

	@Override
	public void onLiveInfoUpdated() {
		Log.d(TAG, "onLiveInfoUpdated");
		liveInfoUpdated = true;
		if (contextReady && playChannel != null)
			open(httpUrl, playChannel);
	}

	@Override
	public void onCaModuleDispatched(int moduleId) {
		Log.d(TAG, "onCaModuleDispatched moduleId=" + moduleId);

	}

	@Override
	public void onShiftStartTimeUpdated(long start) {
		Log.d(TAG, "onShiftStartTimeUpdated start=" + start);

	}

	@Override
	public void onSourceError(String err) {
		Log.d(TAG, "OnSourceError err=" + err);

	}

	@Override
	public void onShiftDuration(long duration) {
		Log.d(TAG, "onShiftDuration duration=" + duration);

	}

	@Override
	public void onShiftMediaTimeSync(long t) {
		Log.d(TAG, "onShiftMediaTimeSync t=" + t);
		shiftDelta = System.currentTimeMillis() - t;
	}

	@Override
	public void onShiftPlay(boolean succ) {
		Log.d(TAG, "onShiftPlay succ=" + succ);

	}

	@Override
	public void onPfInfoUpdated(Program present, Program follow) {
		Log.d(TAG, "onPfInfoUpdated present=" + present + ", follow=" + follow);
		if (present != null && present.getChannelKey() != null) {
			LiveChannel lc = navi.getChannel(present.getChannelKey());
			if (lc != null) {
				lc.setPresent(present);
				lc.setFollow(follow);
				OttoEventPFUpdate event = new OttoEventPFUpdate();
				event.channel = lc;
				OttoUtils.postOnUiThread(event);
			}
		}
	}

	@Override
	public void onShiftError(String msg) {
		Log.d(TAG, "onShiftError msg=" + msg);
		
	}

	@Override
	public void onSyncSignalStatus(String msg) {
		Log.d(TAG, "onSyncSignalStatus msg=" + msg);
		
	}

	public PlayInterface getPlayInterface() {
		return playIntf;
	}

	/**
	 * Set the channel to be played, if the resource is ready, it will be played immediately,
	 * otherwise it will play once the resource is ready
	 * 
	 * @param ck
	 *            channel key, frequency/serviceId
	 */
	public void open(ChannelKey ck) {
		open(null, ck);
	}
	
	public void open(String httpUrl, ChannelKey ck) {
		playChannel = ck;
		this.httpUrl = httpUrl;
		if (contextReady && liveInfoUpdated && ck != null) {
			resetShiftState();
			playIntf.select(httpUrl, ck.getFrequency(), freqFlags, ck.getProgram(), programFlags);
		}
	}
	
	public void resetShiftState(){
		shiftDelta = 0;
		lastPauseTime = 0;
		shiftUrl = null;
		sourceType = 0;
	}
	
	public void shiftPlay(String url, int sourceType){
		this.shiftUrl = url;
		this.sourceType = sourceType;
		playIntf.shift(url, -1, sourceType);
	}
	
	public void shiftPause(){
		if(shiftUrl != null && sourceType != 0){
			playIntf.shiftPause(shiftUrl);
			lastPauseTime = System.currentTimeMillis();
		}
	}

	public void shiftResume(){
		if(shiftUrl != null && sourceType != 0){
			playIntf.shift(shiftUrl, -1, sourceType);
			if (lastPauseTime != 0) {
				shiftDelta += (System.currentTimeMillis() - lastPauseTime);
			}
		}
	}
	
	public long shiftGetPlayPosition(){
		return System.currentTimeMillis() - shiftDelta;
	}
	
	public void shiftStop(){
		playIntf.shiftStop();
	}
	
	/**
	 * @see StreamSelector#select(android.net.telecast.FrequencyInfo, int)
	 * @return
	 */
	public int getFreqFlags() {
		return freqFlags;
	}

	/**
	 * @see StreamSelector#select(android.net.telecast.FrequencyInfo, int)
	 * @param freqFlags
	 */
	public void setFreqFlags(int freqFlags) {
		this.freqFlags = freqFlags;
	}

	/**
	 * @see TeeveePlayer#selectProgram(android.net.telecast.ProgramInfo, int)
	 * @return
	 */
	public int getProgramFlags() {
		return programFlags;
	}
	
	public ChannelKey getLastChannelKey(){
		return playChannel;
	}

	/**
	 * @see TeeveePlayer#selectProgram(android.net.telecast.ProgramInfo, int)
	 * @param programFlags
	 */
	public void setProgramFlags(int programFlags) {
		this.programFlags = programFlags;
	}

	public void addProgramFlag(int flag) {
		this.programFlags |= flag;
	}

	public void clearProgramFlag(int flag) {
		this.programFlags &= ~flag;
	}

}
