package ipaneltv.toolkit.mediaservice;

import ipaneltv.toolkit.IPanelLog;
import ipaneltv.toolkit.JsonParcelable;
import ipaneltv.toolkit.ProgramInfoFilter;
import ipaneltv.toolkit.UriToolkit;
import ipaneltv.toolkit.media.MediaSessionInterface.IpQamTsPlayerInterface;
import ipaneltv.toolkit.mediaservice.components.L10n;
import ipaneltv.toolkit.mediaservice.components.PlayResourceScheduler;
import ipaneltv.toolkit.mediaservice.components.PlayResourceScheduler.ResourcesState;
import ipaneltv.toolkit.mediaservice.components.PlayWidgetManager.PlayWidgetControl;
import ipaneltv.toolkit.mediaservice.components.PlayWidgetManager.PlayWidgetControlCallback;

import org.json.JSONException;
import org.json.JSONObject;
import org.json.JSONStringer;
import org.json.JSONTokener;

import android.media.TeeveePlayer;
import android.media.TeeveePlayer.PlayStateListener;
import android.media.TeeveePlayer.ProgramStateListener;
import android.net.telecast.FrequencyInfo;
import android.net.telecast.ProgramInfo;
import android.net.telecast.ProgramInfo.StreamTypeNameEnum;
import android.net.telecast.SignalStatus;
import android.net.telecast.StreamSelector;
import android.net.telecast.StreamSelector.SelectionStateListener;
import android.os.Bundle;
import android.os.Handler;
import android.os.HandlerThread;
import android.util.Log;

public class IpQamTsPlayerContext<T extends MediaPlaySessionService> extends
		TeeveePlayerBaseContext<T> implements IpQamTsPlayerInterface {
	abstract class CB implements IpQamTsPlayerInterface.Callback {
	};

	public static final String TAG = IpQamTsPlayerContext.class.getSimpleName();

	protected final Object mutex = new Object();
	protected ResourcesState mResourceState;
	protected PlayWidgetControl mWidget;
	private boolean contextReady = false;
	private boolean paused = false;

	protected int fflags = 0, pflags = 0;
	protected FrequencyInfo freqUri = null;
	protected String pString = null;
	protected ProgramInfo progUri = null;
	protected ProgramInfoFilter piFilter;
	protected volatile int selecVersion = 0;
	private HandlerThread procThread = new HandlerThread("ipqamtsplayer-proc");
	private Handler procHandler = null;

	public IpQamTsPlayerContext(T service) {
		super(service);
	}

	@SuppressWarnings("rawtypes")
	@Override
	public void onCreate() {
		IPanelLog.i(TAG, "onCreate getSessionService = " + getSessionService());
		procThread.start();
		procHandler = new Handler(procThread.getLooper());
		LiveNetworkApplication app = getSessionService().getApp();
		Bundle b = getBundle();
		int pri = PlayResourceScheduler.PRIORITY_DEFAULT;
		boolean soft = false;
		if (b != null) {
			pri = b.getInt("priority", pri);
			soft = b.getBoolean("soft", false);
		}
		IPanelLog.i(TAG, "onCreate 11");
		PlayResourceScheduler ps = app.getResourceScheduler();
		mResourceState = ps.createIpQamTsPlayState(false, pri, soft);
		mResourceState.getPlayer().setListener(playerListener, playerListener);
		mResourceState.getSelector().setSelectionStateListener(selectorListener);
		mWidget = app.getPlayWidgetManager().createControl(widgetCallback);
		IPanelLog.i(TAG, "onCreate 22");
		piFilter = new ProgramInfoFilter(ps.getUUID(), ps.getTransportManager()) {
			@Override
			protected String getStreamTypeName(int stream_type) {
				IPanelLog.d(TAG, "getStreamTypeName stream_type = " + stream_type);
				return getStreamType(stream_type);
			}
		};
		IPanelLog.i(TAG, "onCreate end");
	}

	/**
	 * 客户端连接已断开
	 */
	@Override
	public void onClose() {
		if (piFilter != null) {
			piFilter.release();
			piFilter = null;
		}
		if (mResourceState != null) {
			mResourceState.loosen(true);
			mResourceState = null;
		}
		if (mWidget != null) {
			mWidget.close();
			mWidget = null;
		}
		procThread.quit();
	}

	void ensureLiveStateListeners() {
		if (mResourceState.getTag() == null) {
			mResourceState.setTag(new Object());// 标记已经添加过监听器
			mResourceState.getSelector().setSelectionStateListener(selectorListener);
			IPanelLog.d(TAG, "playerListener = " + playerListener);
			mResourceState.getPlayer().setListener(playerListener, playerListener);
		}
	}

	protected boolean reserveAll() {
		return mResourceState.reserve() && mWidget.reserve();
	}

	private boolean reserveAllSafe() {
		boolean ret = false;
		try {
			return (ret = reserveAll());
		} finally {
			if (!ret)
				loosenAll(true);
			contextReady = ret;
		}
	}

	protected void loosenAll(boolean clearState) {
		if(clearState && isRelease()){
			mResourceState.destroy();
		}else{
			mResourceState.loosen(clearState);	
		}
		mWidget.loosen(clearState);

	}

	protected String getStreamType(int type) {
		String name = ProgramInfo.getMpegAVStreamTypeName(type);
		IPanelLog.d(TAG, "ProgramInfoFilter onPmt stream_type=" + type);
		if (name == null) {
			switch (type) {
			case 0x1b:
				name = StreamTypeNameEnum.VIDEO_H264;
				break;
			case 0x0f:
				name = StreamTypeNameEnum.AUDIO_AAC;
				break;
			case 0x81:
				name = StreamTypeNameEnum.AUDIO_AC3;
				break;
			case 0x91:
				name = StreamTypeNameEnum.AUDIO_AC3_PLUS;
				break;
			case 0x8a:
				name = StreamTypeNameEnum.AUDIO_DTS;
				break;
			default:
				name = "";
				break;
			}
		}
		return name;
	}

	/**
	 * 客户端请求获得资源
	 * 
	 * @return
	 */
	@Override
	public final boolean reserve() {// 客户端请求
		synchronized (mutex) {
			if (contextReady ? false : reserveAllSafe()) {
				ensureLiveStateListeners();
				mResourceState.getPlayer().start();
				// 能保留成功，则资源一定是准备好的且已经绑定,比如player的prepare不需要调用
				try {
					contextReady = true;
					IPanelLog.e(TAG, "onReserve ok.");
				} catch (Exception e) {
					IPanelLog.e(TAG, "onReserve error:" + e);
					mResourceState.loosen(true);
				}
			}
		}
		return contextReady;
	}

	/**
	 * 客户端放开资源控制，除非因别的客户端请求资源而发生抢占,服务端对资源尽量保留
	 */
	@Override
	public void loosen(boolean clearState) {
		synchronized (mutex) {
			if (contextReady) {
				IPanelLog.d(TAG, "onLoosen");
				paused = false;
				mWidget.clearWidgetMessage();
				contextReady = false;
				loosenAll(clearState);
				freqUri = null;
				progUri = null;
			}
		}
	}

	@Override
	public void stop(int flag) {
		synchronized (mutex) {
			if (mResourceState.isReserved()) {
				freqUri = null;
				progUri = null;
				stopPlayer(flag);
			}
		}
	}

	@Override
	public void pause() {
		synchronized (mutex) {
			if (mResourceState.isReserved()) {
				paused = true;
				mResourceState.getPlayer().setFreeze(true, TeeveePlayer.FLAG_VIDEO_FRAME_FREEZE);
				mResourceState.getPlayer().pause();
			}
		}
	}

	@Override
	public void resume() {
		synchronized (mutex) {
			if (mResourceState.isReserved()) {
				paused = false;
				mResourceState.getPlayer().resume();
			}
		}
	}

	/**
	 * 得到当前节目的播放时间
	 * 
	 * @return 毫秒时间值
	 */
	public long getPlayTime() {
		synchronized (mutex) {
			if (mResourceState.isReserved()) {
				IPanelLog.d(TAG, "getPlayTime");
				return mResourceState.getPlayer().getPlayTime();
			}
			return -1;
		}
	}

	@Override
	public void setVolume(float v) {
		synchronized (mutex) {
			if (mResourceState.isReserved()) {
				volumeSelect = v < 0f ? 0f : v > 1f ? 1f : v;
				IPanelLog.d(TAG, "setVolume volumeSelect= " + volumeSelect);
				mResourceState.getPlayer().setVolume(volumeSelect);
			}
		}
	}

	@Override
	public void setDisplay(int x, int y, int w, int h) {
		synchronized (mutex) {
			if (mResourceState.isReserved()) {
				mResourceState.getPlayer().setDisplay(x, y, w, h);
			}
		}
	}

	@Override
	public void setProgramFlags(int flags) {
		synchronized (mutex) {
			boolean succ = false;
			if (mResourceState.isReserved()) {
				try {
					this.pflags = flags;
					succ = onSelect(this.freqUri, this.fflags, this.progUri, this.pflags);
				} catch (Exception e) {
					notifyError("error:" + e.getMessage());
					e.printStackTrace();
				} finally {
					// notifyJson(CB.__ID_onResponseStart, succ + "");
					IPanelLog.d(TAG, "setProgramFlags 1 __ID_onResponseStart succ=" + succ);
				}
			}
		}
	}

	@Override
	public void setTeeveeWidget(int flags) {
		synchronized (mutex) {
			if (mWidget != null)
				mWidget.setTeeveeWidget(flags);
		}
	}

	@Override
	public void checkTeeveeWidget(int flags) {
		synchronized (mutex) {
			if (mWidget != null)
				mWidget.checkTeeveeWidget(flags);
		}
	}

	@Override
	public void syncMediaTime() {
		synchronized (mutex) {
			if (mResourceState.isReserved()) {
				notifyJson(CB.__ID_onSyncMediaTime, null);
			}
		}
	}

	class ProgFilter implements ProgramInfoFilter.ProgramHandler {
		int flags = 0;
		int progFlags = 0;

		public ProgFilter(int progFlags, int flags) {
			this.flags = flags;
			this.progFlags = progFlags;
		}

		@Override
		public void onProgramFound(final int ver, final String err, final ProgramInfo info) {
			IPanelLog.d(TAG, "onProgramFound info=" + info);
			// 通过procHandler 发送消息播放，以免出现在其他的地方关闭filter时卡主的情况。
			procHandler.post(new Runnable() {
				@Override
				public void run() {
					onProgramFoundPlay(ver, err, info, progFlags, flags);
				}
			});
			IPanelLog.d(TAG, "onProgramFound end");
		}
	}

	private void onProgramFoundPlay(int ver, String err, ProgramInfo info, int progFlags, int flags) {
		synchronized (mutex) {
			boolean succ = false;
			IPanelLog.d(TAG, "onProgramFoundPlay info=" + info + "; flags = " + flags
					+ ";played = " + played);
			try {
				if (selecVersion == ver && err == null && info != null) {
					IPanelLog.d(TAG, "onProgramFoundPlay paused = " + paused);
					if (paused) {
						if (mResourceState.isReserved()) {
							if (!playerStopped) {
								mResourceState.getPlayer().resume();
							}

						}
					}
					if (!selectProgram(info, progFlags)) {
						IPanelLog.e(TAG, "start>selectProgram failed");
						return;
					}
					onSetVolume(volumeSelect);
					succ = true;
				}
			} catch (Exception e) {
				e.printStackTrace();
			} finally {
				if (paused) {
					if (mResourceState.isReserved()) {
						if (!playerStopped) {
							mResourceState.getPlayer().pause();
						}

					}
				}
				if (flags != NO_RESPONSE || !played) {
					played = succ;
					notifyJson(CB.__ID_onResponseStart, succ + "");
					IPanelLog.d(TAG, "onProgramFoundPlay __ID_onResponseStart succ=" + succ);
				}
			}
		}
	}
	
	protected void onSetVolume(float f){
		mResourceState.getPlayer().setVolume(f);
	}

	private boolean checkSameParam(String furi, String puri) {
		if (this.freqUri != null && this.progUri != null) {
			if (this.freqUri.equals(furi) && this.progUri.equals(puri))
				return true;
			else
				return false;
		}
		return false;
	}

	@Override
	public void start(String furi, int fflags, String puri, int pflags) {
		synchronized (mutex) {
			boolean succ = false;
			if (mResourceState.isReserved()) {
				if (furi == null || puri == null)
					throw new RuntimeException("invalid param");
				played = false;
				paused = false;
				IPanelLog.d(TAG, "start 1 furi=" + furi + ",puri=" + puri);

				// 重新播放参数相同则直接播放（不需要重新搜索）
				if (checkSameParam(furi, puri)) {
					try {
						succ = onSelect(freqUri, fflags, progUri, pflags);
					} catch (Exception e) {
						e.printStackTrace();
					} finally {
						played = succ;
						notifyJson(CB.__ID_onResponseStart, succ + "");
						IPanelLog.d(TAG, "start __ID_onResponseStart succ=" + succ);
					}
				} else {
					succ = selectProgramForSearch(furi, puri, pflags, DEFAULT);
				}
			} else {
				notifyJson(CB.__ID_onResponseStart, succ + "");
				IPanelLog.d(TAG, "start __ID_onResponseStart succ=" + succ);
			}
		}
	}

	@Override
	public void setRate(float r) {
		synchronized (mutex) {
			try {
				if (mResourceState.isReserved()) {
					ProgramInfo pi = this.progUri;
					pi.setVideoSourceRate(r);
					int flag;
					if (r > 1) {
						flag = pflags | TeeveePlayer.FLAG_VIDEO_IFRAME_ONLY
								| TeeveePlayer.FLAG_VIDEO_FRAME_FREEZE;
					} else {
						flag = pflags | TeeveePlayer.FLAG_VIDEO_FRAME_FREEZE;
					}
					IPanelLog.d(TAG, "setRate 2 pi=" + pi.toString() + ";flag = " + flag);
					onSelectProgram(pi, flag);
					onSetVolume(volumeSelect);
				}
			} catch (Exception e) {
				IPanelLog.e(TAG, "set rate error e" + e.getMessage());
			} finally {
				notifyJson(CB.__ID_onRateChange, "" + r);
			}
		}
	}

	@Override
	public void syncSignalStatus() {
		synchronized (mutex) {
			if (mResourceState.isReserved()) {
				SignalStatus ss = mResourceState.getSelector().getSignalStatus();
				notifyJson(CB.__ID_onSyncSignalStatus, ss.toString());
			}
		}
	}

	@Override
	public void clearCache(int flags) {
		synchronized (mutex) {
			if (mResourceState.isReserved()) {
				mResourceState.getSelector().clear();
				mResourceState.getPlayer().clearCache();
			}
		}
	}

	@Override
	public String onTransmit(int code, String json, JsonParcelable p, Bundle b)
			throws JSONException {
		IPanelLog.d(TAG, "onTransmit> code=" + code + ",json=" + json);
		switch (code) {
		case __ID_getPlayTime:
			return getPlayTime()+"";
		case __ID_start:
			JSONObject o = (JSONObject) new JSONTokener(json).nextValue();
			start(o.getString("furi"), o.getInt("fflags"), o.getString("puri"), o.getInt("pflags"));
			break;
		case __ID_pause:
			pause();
			break;
		case __ID_resume:
			resume();
			break;
		case __ID_setRate:
			setRate(Float.parseFloat(json));
			break;
		case __ID_syncSignalStatus:
			syncSignalStatus();
			break;
		case __ID_clearCache:
			clearCache(Integer.parseInt(json));
			break;
		default:
			return super.onTransmit(code, json, p, b);
		}
		return null;
	}

	void notifyError(String msg) {
		notifyJson(CB.__ID_onPlayError, msg);
	}

	private final void notifyWidgetSwitchEnd(String err) {
		int code = err == null ? 0 : L10n.getErrorCode(err, 100000);
		if (mWidget != null)
			mWidget.notifySwitchingEnd(code, err);
	}

	private boolean selectFrequency(FrequencyInfo fi, int fFlags) {
		boolean fSelect = true;
		IPanelLog.d(TAG, "curr fi = " + fi);
		if (freqUri != null && freqUri.toString().equals(fi.toString())) {
			IPanelLog.d(TAG, "last freqInfo = " + freqUri);
			if ((fFlags & StreamSelector.SELECT_FLAG_FORCE) == 0) {
				IPanelLog.d(TAG, "selectFrequency>not force same freqUri");
				fSelect = false;
			}
		}
		if (fSelect) {
			IPanelLog.d(TAG, "selector.select");
			if (!mResourceState.getSelector().select(fi, fflags)) {
				IPanelLog.e(TAG, "StreamSelector select failed");
				/*
				 * notifyError(L10n.SELECT_ERR_430);
				 * notifyWidgetSwitchEnd(L10n.SELECT_ERR_430);
				 */
				return false;
			}
			this.freqUri = fi;
			this.fflags = fFlags;
		}
		return true;
	}

	private boolean selectProgram(ProgramInfo pi, int pFlags) {
		IPanelLog.d(TAG, "selectProgram pi=" + pi.toString());
		ensurePlayerStarted();
		if (!mResourceState.getPlayer().selectProgram(pi, pFlags)) {
			IPanelLog.e(TAG, "TeeveePlayer select failed");
			// 注释掉，快速切换有时候会由于切换
			// 过快使上一次播放失败的消息显示在后面的正常抱播放上
			// notifyError(L10n.SELECT_ERR_431);
			notifyWidgetSwitchEnd(L10n.SELECT_ERR_431);
			return false;
		}
		this.progUri = pi;
		this.pflags = pFlags;
		return true;
	}

	private static final int DEFAULT = 0;
	private static final int NO_RESPONSE = 1;
	private boolean played = false;

	private boolean selectProgramForSearch(String furi, String puri, int progFlags, int flag) {
		IPanelLog.d(TAG, "freqUri> diff furi=" + furi + ";& puri=" + puri);
		final FrequencyInfo fi = FrequencyInfo.fromString(furi);
		if (fi == null)
			return false;

		if (!selectFrequency(fi, fflags)) {
			IPanelLog.d(TAG, "selector.select failed");
			return false;
		}

		piFilter.stop();
		if (UriToolkit.getSchemaId(puri) == UriToolkit.PMT_SCHEMA_ID) {
			// PMT PID
			final int pmtpid = UriToolkit.getProgramPmtId(puri);
			piFilter.start2(fi.getFrequency(), pmtpid, ++selecVersion, new ProgFilter(progFlags,
					flag));
		} else if (UriToolkit.getSchemaId(puri) == UriToolkit.DVB_SERVICE_SCHEMA_ID) {
			// SERVICE ID
			int pn = UriToolkit.getProgramServiceId(puri);
			pn = (pn == 0 ? -1 : pn);
			piFilter.start(fi.getFrequency(), pn, ++selecVersion, new ProgFilter(progFlags, flag));
		}
		pString = puri;
		return true;
	}

	private boolean onSelect(FrequencyInfo fi, int fflags, ProgramInfo pi, int pflags) {
		IPanelLog.d(TAG, "onSelect:(" + fi.toString() + "," + fflags + "," + pi.toString() + ","
				+ pflags + ")");
		if (mWidget != null) {
			mWidget.clearWidgetMessage();
			mWidget.notifySwitchingStart(pi.getVideoPID() < 0);
		}
		if (!selectFrequency(fi, fflags)) {
			/*
			 * notifyError(L10n.SELECT_ERR_430);
			 * notifyWidgetSwitchEnd(L10n.SELECT_ERR_430);
			 */
			return false;
		}

		if (!onSelectProgram(pi, pflags)) {
			notifyError(L10n.SELECT_ERR_431);
			notifyWidgetSwitchEnd(L10n.SELECT_ERR_431);
			return false;
		}

		this.freqUri = fi;
		this.progUri = pi;
		this.fflags = fflags;
		this.pflags = pflags;
		return true;
	}

	private boolean ensurePlayerStarted() {
		if (mResourceState.isReserved()) {
			boolean b = false;
			if (playerStopped) {
				b = mResourceState.getPlayer().start();
				if (b) {
					mResourceState.getPlayer().setFreeze(false, 0);
					onSetVolume(volumeSelect);
					IPanelLog.d(TAG, "ensurePlayerStarted volumeSelect=" + volumeSelect);
					playerStopped = false;
				}
			}
			return b;
		}
		return false;
	}

	private boolean onSelectProgram(ProgramInfo pi, int flags) {
		ensurePlayerStarted();
		if (mResourceState.getPlayer().selectProgram(pi, flags)) {
			return true;
		}
		return false;
	}

	private void stopPlayer(int flag) {
		IPanelLog.d(TAG, "stopPlayer flag=" + flag);
		if ((flag & TeeveePlayer.FLAG_VIDEO_FRAME_FREEZE) == TeeveePlayer.FLAG_VIDEO_FRAME_BLACK) {
			mResourceState.getPlayer().setFreeze(true, TeeveePlayer.FLAG_VIDEO_FRAME_BLACK);
			mResourceState.getPlayer().stop();
			mResourceState.getPlayer().setFreeze(true, TeeveePlayer.FLAG_VIDEO_FRAME_BLACK);
			onSetVolume(0);
			playerStopped = true;
		} else if ((flag & TeeveePlayer.FLAG_VIDEO_FRAME_FREEZE) == TeeveePlayer.FLAG_VIDEO_FRAME_FREEZE) {
			mResourceState.getPlayer().setFreeze(true, TeeveePlayer.FLAG_VIDEO_FRAME_FREEZE);
			mResourceState.getPlayer().stop();
			mResourceState.getPlayer().setFreeze(true, TeeveePlayer.FLAG_VIDEO_FRAME_FREEZE);
			onSetVolume(0);
			playerStopped = true;
		}
	}

	protected void reselectProgram(int program_number, String newuri) {
		synchronized (mutex) {
			IPanelLog.d(TAG, "reselectProgram newuri = " + newuri + ";progUri = " + progUri);
			if (newuri != null && progUri != null) {
				ProgramInfo p = ProgramInfo.fromString(newuri);
				if (p.getProgramNumber() == progUri.getProgramNumber()
						&& (p.getAudioPID() != progUri.getAudioPID() || p.getVideoPID() != progUri
								.getVideoPID())) {
					selectProgram(p, pflags);
				}
			}
		}
	}

	/**
	 * 回调频道信息变化，子类可重写该方法进行处理。
	 * 
	 * @param program_number
	 *            频道号
	 * @param newuri
	 *            新的节目信息uri
	 */
	protected void onProgramInfoChanged(int program_number, String newuri) {

	}

	protected void onSelectSuc(StreamSelector s) {

	}

	// =====================
	private boolean playerStopped = false;
	private float volumeSelect = 0.5f;

	PlayWidgetControlCallback widgetCallback = new PlayWidgetControlCallback() {

		@Override
		public void onWidgetChecked(int flags) {
			notifyJson(CB.__ID_onWidgetChecked, flags + "");
		}

	};

	SelectionStateListener selectorListener = new SelectionStateListener() {

		@Override
		public void onSelectStart(StreamSelector selector) {
			// TODO Auto-generated method stub

		}

		@Override
		public void onSelectSuccess(StreamSelector selector) {
			onSelectSuc(selector);
			mWidget.notifyTransportState(null);
			SignalStatus ss = mResourceState.getSelector().getSignalStatus();
			if(ss != null){
				notifyJson(CB.__ID_onSyncSignalStatus, ss.toString());	
			}
		}

		@Override
		public void onSelectFailed(StreamSelector selector) {
			Log.d(TAG, "onSelectFailed selector = "+ selector);
			String err = L10n.TRANSPORT_ERR_401;
			mWidget.notifyTransportState(err);
			if (freqUri != null && pString != null) {
				selectProgramForSearch(freqUri.toString(), pString, pflags
						| TeeveePlayer.FLAG_VIDEO_FRAME_FREEZE, NO_RESPONSE);
			}
			notifyJson(CB.__ID_onStreamLost);
		}

		@Override
		public void onSelectionLost(StreamSelector selector) {
			String err = L10n.TRANSPORT_ERR_402;
			mWidget.notifyTransportState(err);
			notifyJson(CB.__ID_onStreamLost);
		}

		@Override
		public void onSelectionResumed(StreamSelector selector) {
			IPanelLog.d(TAG, "onSelectionResumed pString = " + pString + ";freqUri = " + freqUri);
			mWidget.notifyTransportState(null);
			if (freqUri != null && pString != null) {
				selectProgramForSearch(freqUri.toString(), pString, pflags
						| TeeveePlayer.FLAG_VIDEO_FRAME_FREEZE, NO_RESPONSE);
			}
			notifyJson(CB.__ID_onStreamResumed);
		}

	};

	PlayerListener playerListener = new PlayerListener();

	class PlayerListener implements PlayStateListener, ProgramStateListener {

		@Override
		public void onProgramReselect(int program_number, String newuri) {
			IPanelLog.d(TAG, "onProgramReselect program_number = " + program_number + ";newuri = "
					+ newuri);
			onProgramInfoChanged(program_number, newuri);
		}

		@Override
		public void onProgramDiscontinued(int program_number) {
			// TODO Auto-generated method stub

		}

		@Override
		public void onSelectionStart(TeeveePlayer player, int program_number) {
			// TODO Auto-generated method stub

		}

		@Override
		public void onPlayProcessing(int program_number) {
			// try {
			// IPanelLog.d(TAG, "onPlayProcessing program_number = " +
			// program_number);
			// JSONStringer str = new JSONStringer();
			// str.object();
			// str.key("pn").value(program_number);
			// long pts_time =
			// mResourceState.getPlayer().getPlayProcessPtsTime();
			// IPanelLog.d(TAG, "pts_time = " + pts_time);
			// str.key("pts_time").value(pts_time);
			// notifyJson(CB.__ID_onPlayProcessing);
			// str.endObject();
			// } catch (Exception e) {
			// IPanelLog.e(TAG, "onPlayProcessing failed e=" + e);
			// e.printStackTrace();
			// }
		}

		@Override
		public void onPlaySuspending(int program_number) {
			// IPanelLog.d(TAG, "onPlaySuspending program_number=" +
			// program_number);
			// notifyJson(CB.__ID_onPlaySuspending, program_number + "");
		}

		@Override
		public void onPlayError(int program_number, String msg) {
			// TODO Auto-generated method stub

		}

	}

	PlayerPTSListener playerptslistener = new PlayerPTSListener();

	class PlayerPTSListener implements PlayResourceScheduler.PlayerProcessPTSListener {

		@Override
		public void onPlayerPTSChange(int program_number, long process_pts_time, int state) {
			// TODO Auto-generated method stub
			try {
				IPanelLog.d(TAG, "onPlayerPTSChange program_number = " + program_number);
				JSONStringer str = new JSONStringer();
				str.object();
				str.key("pn").value(program_number);
				IPanelLog.d(TAG, "process_pts_time = " + process_pts_time);
				str.key("pts_time").value(process_pts_time);
				str.key("state").value(state);// 1代表process消息
				str.endObject();
				// notifyJson(CB.__ID_onPlayProcessing,str.toString());
				notifyJson(CB.__ID_onPlayerPTSChange, str.toString());
			} catch (Exception e) {
				IPanelLog.e(TAG, "onPlayProcessing failed e=" + e);
				e.printStackTrace();
			}
		}

		@Override
		public void onPlayProcess(int program_number, long process_pts_time) {
			// TODO Auto-generated method stub

		}

		@Override
		public void onPlaySuspend(int program_number) {
			// TODO Auto-generated method stub

		}
	}
	
	@Override
	public void checkPassword(String pwd) {
		// TODO Auto-generated method stub

	}

}
