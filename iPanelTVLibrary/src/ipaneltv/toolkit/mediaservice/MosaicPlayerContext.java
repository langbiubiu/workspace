package ipaneltv.toolkit.mediaservice;

import ipaneltv.toolkit.IPanelLog;
import ipaneltv.toolkit.db.DatabaseObjectification.ChannelKey;
import ipaneltv.toolkit.media.MediaSessionInterface.MosaicPlayerInterface;
import ipaneltv.toolkit.mediaservice.components.CaDescramblingManager.ProgramDescramberCallback;
import ipaneltv.toolkit.mediaservice.components.CaDescramblingManager.ProgramDescrambler;
import ipaneltv.toolkit.mediaservice.components.L10n;
import ipaneltv.toolkit.mediaservice.components.LiveDataManager;
import ipaneltv.toolkit.mediaservice.components.LiveDataManager.LiveDataListener;
import ipaneltv.toolkit.mediaservice.components.PlayResourceScheduler;
import ipaneltv.toolkit.mediaservice.components.PlayResourceScheduler.ResourcesState;
import ipaneltv.toolkit.mediaservice.components.PlayWidgetManager.PlayWidgetControl;
import ipaneltv.toolkit.mediaservice.components.PlayWidgetManager.PlayWidgetControlCallback;

import org.json.JSONException;
import org.json.JSONObject;
import org.json.JSONTokener;

import android.media.TeeveePlayer;
import android.media.TeeveePlayer.PlayStateListener;
import android.media.TeeveePlayer.ProgramStateListener;
import android.net.telecast.FrequencyInfo;
import android.net.telecast.ProgramInfo;
import android.net.telecast.StreamSelector;
import android.net.telecast.StreamSelector.SelectionStateListener;
import android.os.Bundle;
import android.util.SparseArray;

public class MosaicPlayerContext<T extends MediaPlaySessionService> extends
		TeeveePlayerBaseContext<T> implements MosaicPlayerInterface {
	abstract class CB implements MosaicPlayerInterface.Callback {
	};

	private static final String TAG = MosaicPlayerContext.class.getSimpleName();
	protected final Object mutex = new Object();
	protected ResourcesState mPlayResource;

	protected PlayWidgetControl mWidgetHandle;
	protected ProgramDescrambler mDescrambler;
	private boolean contextReady = false;
	private LiveDataManager mLiveData;
	SparseArray<ProgramDescrambler> pipDescramblers = new SparseArray<ProgramDescrambler>();

	public MosaicPlayerContext(T service) {
		super(service);
	}

	@SuppressWarnings("rawtypes")
	@Override
	public void onCreate() {
		LiveNetworkApplication app = getSessionService().getApp();
		Bundle b = getBundle();
		int pri = PlayResourceScheduler.PRIORITY_DEFAULT;
		boolean soft = false;
		if (b != null) {
			pri = b.getInt("priority", pri);
			soft = b.getBoolean("soft", false);
		}
		mPlayResource = app.getResourceScheduler().createLivePlayState(false, pri, soft);
		IPanelLog.d(TAG, "onCreate ret.selectorHandle = " + mPlayResource.getPlayer()
				+ ";ret.playerHandle = " + mPlayResource.getSelector());
		mPlayResource.getSelector().setSelectionStateListener(selectionStateListener);
		mWidgetHandle = app.getPlayWidgetManager().createControl(widgetCallback);
		mPlayResource.getPlayer().setListener(playStateListener, programStateListener);
		mWidgetHandle = app.getPlayWidgetManager().createControl(widgetCallback);
		mDescrambler = app.getCaDescramblingManager().createDescrambler(descramblerCallback);
		mLiveData = app.getLiveDataManager();
		IPanelLog.d(TAG, "mLiveData = " + mLiveData + ";mLiveDataListener = " + mLiveDataListener);
		mLiveData.addLiveDataListener(mLiveDataListener);
	}

	/**
	 * 客户端连接已断开
	 */
	@Override
	public void onClose() {
		loosenAll(true);
		IPanelLog.d(TAG, "onClose");
		mLiveData.removeLiveDataListener(mLiveDataListener);
		mPlayResource.close();
		mWidgetHandle.close();
		mPlayResource = null;
		mWidgetHandle = null;
	}

	protected boolean reserveAll() {
		return mPlayResource.reserve() && //
				mWidgetHandle.reserve();
	}

	private boolean reserveAllSafe() {
		boolean ret = false;
		try {
			return (ret = reserveAll());
		} finally {
			IPanelLog.d(TAG, "ret = " + ret);
			if (!ret)
				loosenAll(true);
			contextReady = ret;
		}
	}

	protected void loosenAll(boolean clearState) {
		if(clearState && isRelease()){
			mPlayResource.destroy();
		}else{
			mPlayResource.loosen(clearState);	
		}
		mWidgetHandle.loosen(clearState);
	}

	/**
	 * 客户端请求获得资源
	 * 
	 * @return
	 */
	@Override
	public boolean reserve() {// 客户端请求
		IPanelLog.d(TAG, "before reserve");
		synchronized (mutex) {
			IPanelLog.d(TAG, "reserve in");
			if (contextReady ? false : reserveAllSafe()) {
				contextReady = true;
				mPlayResource.getPlayer().start();
			}
			IPanelLog.d(TAG, "reserve end");
		}
		IPanelLog.d(TAG, "end reserve");
		return contextReady;
	}

	/**
	 * 客户端放开资源控制，除非因别的客户端请求资源而发生抢占,服务端对资源尽量保留
	 */
	@Override
	public void loosen(boolean clearState) {
		IPanelLog.d(TAG, "before loosen");
		synchronized (mutex) {
			if (contextReady) {
				IPanelLog.d(TAG, "onLoosen(clearState=" + clearState + ")");
				contextReady = false;
				mWidgetHandle.clearWidgetMessage();
				loosenAll(clearState);
				selection.clearUri();
			}
		}
		IPanelLog.d(TAG, "end loosen");
	}

	@Override
	public void select(long freq, int fflags, String puri, int pflags) {
		FrequencyInfo fi = null;
		ProgramInfo pi = null;
		IPanelLog.d(TAG, "before select");
		synchronized (mutex) {
			@SuppressWarnings("unused")
			String err = null;
			boolean done = false;
			try {
				if(puri == null){
					return;
				}
				ProgramInfo pInfo = ProgramInfo.fromString(puri);
				ChannelKey key = ChannelKey.obten(freq, pInfo.getProgramNumber());
				mWidgetHandle.clearWidgetMessage();
				fi = mLiveData.getFrequencyInfo(freq);
				pi = mLiveData.getProgramInfo(key);
				pi.setAudioPID(pInfo.getAudioPID());
				pi.setAudioStreamType(pInfo.getAudioStreamType());
				if (fi != null && pi != null) {
					onSelect(fi, fflags, pi, pflags);
					done = true;
				}
			} catch (Exception e) {
				e.printStackTrace();
				err = "error:" + e.toString();
			} finally {
				if (!done) {
					if (fi == null)
						err = L10n.SELECT_ERR_430;
					else if (pi == null)
						err = L10n.SELECT_ERR_431;
					/*
					 * notifyError(err); notifyWidgetSwitchEnd(err);
					 */
				}
				notifyJson(CB.__ID_onResponseSelect, done + "");
			}
		}
		IPanelLog.d(TAG, "end select");
	}

	public void select(String furi, int fflags, String puri, int pflags) {
		try {
			IPanelLog.d(TAG, "before select with furi");
			synchronized (mutex) {
				mWidgetHandle.clearWidgetMessage();
				FrequencyInfo fi = FrequencyInfo.fromString(furi);
				ProgramInfo pi = ProgramInfo.fromString(puri);
				onSelect(fi, fflags, pi, pflags);
			}
			IPanelLog.d(TAG, "end select with furi");
		} catch (Exception e) {
			String err = "error:" + e.getMessage();
			notifyError(err);
			notifyWidgetSwitchEnd(err);
		}
	}

	private void onSelect(FrequencyInfo fi, int fflags, ProgramInfo pi, int pflags) {
		String furi = fi.toString();
		String puri = pi.toString();
		IPanelLog.d(TAG, "onSelect:(" + furi + "," + fflags + "," + puri + "," + pflags + ")");
		IPanelLog.d(TAG, "onSelect pi.getVideoPID() = " + pi.getVideoPID());
		mWidgetHandle.notifySwitchingStart(pi.getVideoPID() < 0);
		IPanelLog.d(TAG, "onSelect aaa");
		if (selection.furi == null || !selection.furi.equals(furi) || selection.fforce(fflags)) {
			IPanelLog.d(TAG, "onSelect 000");
			if (!mPlayResource.getSelector().select(fi, fflags)) {
				IPanelLog.d(TAG, "onSelect 111");
				/*
				 * notifyError(L10n.SELECT_ERR_430);
				 * notifyWidgetSwitchEnd(L10n.SELECT_ERR_430);
				 */
				return;
			}
			selection.fset(furi, fi.getFrequency(), fflags);
		} else {
			IPanelLog.d(TAG, "ignore select stream again for same uri!");
		}
		IPanelLog.d(TAG, "onSelect 222");
		if (!onSelectProgram(puri, pflags)) {
			notifyError(L10n.SELECT_ERR_431);
			notifyWidgetSwitchEnd(L10n.SELECT_ERR_431);
			return;
		}
		IPanelLog.d(TAG, "onSelect 333");
		ChannelKey key = ChannelKey.obten(fi.getFrequency(), pi.getProgramNumber());
		IPanelLog.d(TAG, "before mDescrambler.start()");
		mDescrambler.stop();
		boolean d = mDescrambler.start(key, pi.getAudioPID(), pi.getVideoPID());
		IPanelLog.d(TAG, "after mDescrambler.start()");
		if (!d) {
			notifyError("select descrambling failed");
			notifyWidgetSwitchEnd(L10n.SELECT_ERR_432);
			return;
		}
		IPanelLog.d(TAG, "onSelect 444");
		notifyWidgetSwitchEnd(null);
		// callback
	}

	private boolean onSelectProgram(ProgramInfo pi, int flags) {
		ensurePlayerStarted();
		if (mPlayResource.getPlayer().selectProgram(pi, flags)) {
			selection.pset(pi.toString(), flags);
			return true;
		}
		return false;
	}

	private boolean onSelectProgram(String puri, int flags) {
		try {
			return onSelectProgram(ProgramInfo.fromString(puri), flags);
		} catch (Exception e) {
			return false;
		}
	}

	private boolean ensurePlayerStarted() {
		if (mPlayResource.isReserved()) {
			boolean b = false;
			if (playerStopped) {
				b = mPlayResource.getPlayer().start();
				if (b) {
					mPlayResource.getPlayer().setFreeze(false, 0);
					setVolume(volumeSelect);
					playerStopped = false;
				}
			}
			return b;
		}
		return false;
	}

	@Override
	public void setVolume(float v) {
		IPanelLog.d(TAG, "before setVolume");
		synchronized (mutex) {
			if (mPlayResource.isReserved()) {
				volumeSelect = v < 0f ? 0f : v > 1f ? 1f : v;
				IPanelLog.d(TAG, "setVolume v= " + v);
				mPlayResource.getPlayer().setVolume(volumeSelect);
			}
		}
		IPanelLog.d(TAG, "end setVolume");
	}

	/**
	 * 设置基底player的framer大小
	 * 
	 * @param x
	 * @param y
	 * @param w
	 * @param h
	 */
	@Override
	public final void setDisplay(int x, int y, int w, int h) {
		IPanelLog.d(TAG, "before setDisplay");
		synchronized (mutex) {
			if (mPlayResource.isReserved()) {
				mPlayResource.getPlayer().setDisplay(x, y, w, h);
			}
		}
		IPanelLog.d(TAG, "end setDisplay");
	}

	@Override
	public void stop(int flag) {
		IPanelLog.d(TAG, "stop flag = " + flag + ";suspend = " + suspend);
		synchronized (mutex) {
			if (!suspend) {
				if (mPlayResource.isReserved()) {
					suspend = true;
					selection.clearUri();
					stopPlayer(flag);
				}
			}
		}
		IPanelLog.d(TAG, "end stop");
	}

	@Override
	public void pause() {
		IPanelLog.d(TAG, "before pause");
		synchronized (mutex) {
			if (mPlayResource.isReserved()) {
				mPlayResource.getPlayer().pause();
			}
		}
		IPanelLog.d(TAG, "end pause");
	}

	@Override
	public void resume() {
		IPanelLog.d(TAG, "before resume");
		synchronized (mutex) {
			if (mPlayResource.isReserved()) {
				mPlayResource.getPlayer().resume();
			}
		}
		IPanelLog.d(TAG, "end resume");
	}

	@Override
	public long getPlayTime() {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public void setProgramFlags(int flags) {
		// TODO Auto-generated method stub

	}

	@Override
	public void setTeeveeWidget(int flags) {
		// TODO Auto-generated method stub

	}

	@Override
	public void checkTeeveeWidget(int flags) {
		// TODO Auto-generated method stub

	}

	@Override
	public void syncMediaTime() {
		// TODO Auto-generated method stub

	}

	@Override
	public void syncSignalStatus() {
		// TODO Auto-generated method stub

	}

	private final void notifyWidgetSwitchEnd(String err) {
		int code = err == null ? 0 : L10n.getErrorCode(err, 100000);
		mWidgetHandle.notifySwitchingEnd(code, err);
	}

	// =====================
	Selection selection = new Selection();
	private boolean playerStopped = false;
	private boolean suspend = false;
	private float volumeSelect = 0.5f;

	@SuppressWarnings("unused")
	private void onSelect(FrequencyInfo fi, int fflags) {
		String furi = fi.toString();
		IPanelLog.d(TAG, "onSelect:(" + furi + "," + fflags);
		if (selection.furi == null || !selection.furi.equals(furi) || selection.fforce(fflags)) {
			IPanelLog.d(TAG, "onSelect 000");
			if (!mPlayResource.getSelector().select(fi, fflags)) {
				IPanelLog.d(TAG, "onSelect 111");
				return;
			}
			selection.fset(furi, fi.getFrequency(), fflags);
		} else {
			IPanelLog.d(TAG, "ignore select stream again for same uri!");
		}
		IPanelLog.d(TAG, "onSelect 333");
		// callback
	}

	private void stopPlayer(int flag) {
		IPanelLog.d(TAG, "stopPlayer flag" + flag);
		if ((flag & TeeveePlayer.FLAG_VIDEO_FRAME_FREEZE) == TeeveePlayer.FLAG_VIDEO_FRAME_BLACK) {
			mPlayResource.getPlayer().stop();
			mPlayResource.getPlayer().setFreeze(true, TeeveePlayer.FLAG_VIDEO_FRAME_BLACK);
			mPlayResource.getPlayer().setVolume(0);
			playerStopped = true;
		} else if ((flag & TeeveePlayer.FLAG_VIDEO_FRAME_FREEZE) == TeeveePlayer.FLAG_VIDEO_FRAME_FREEZE) {
			mPlayResource.getPlayer().stop();
			mPlayResource.getPlayer().setFreeze(true, TeeveePlayer.FLAG_VIDEO_FRAME_FREEZE);
			mPlayResource.getPlayer().setVolume(0);
			playerStopped = true;
		}
	}

	class Selection {
		private String furi = null, puri = null;
		private int fflags = 0, pflags = 0;
		@SuppressWarnings("unused")
		private long freq = 0;

		void clearUri() {
			puri = furi = null;
			pflags = fflags = 0;
		}

		void fset(String uri, long freq, int flags) {
			furi = uri;
			fflags = flags;
			this.freq = freq;
		}

		void pset(String uri, int flags) {
			puri = uri;
			pflags = flags;
		}

		void reselect() {
			select(furi, fflags, puri, pflags);
		}

		void reselectProgram() {
			IPanelLog.d(TAG, "Selection reselectProgram puri = " + puri);
			if (puri != null) {
				onSelectProgram(puri, pflags);
			}
		}

		boolean preselect(int program, String npuri) {
			IPanelLog.d(TAG, "preselect program = " + program + "; npuri = " + npuri);
			if (puri != null) {
				int pn = ProgramInfo.fromString(npuri).getProgramNumber();
				IPanelLog.d(TAG, "preselect pn = " + pn);
				if (pn == program) {
					return onSelectProgram(npuri, pflags);
				}
			}
			return true;
		}

		boolean fforce(int flags) {
			return (flags & StreamSelector.SELECT_FLAG_FORCE) != 0;
		}
	}

	final void notifyError(String msg) {
		notifyJson(CB.__ID_onPlayError, msg);
	}

	private static boolean selectFailed = false;
	private Object selectMutex = new Object();
	SelectionStateListener selectionStateListener = new SelectionStateListener() {

		@Override
		public void onSelectStart(StreamSelector selector) {/*- ignore, ResourceScheduler done */
			IPanelLog.d(TAG, "onSelectStart");
		}

		@Override
		public void onSelectFailed(StreamSelector s) {
			IPanelLog.d(TAG, "onSelectFailed");
			IPanelLog.d(TAG, "onSelectFailed 11");
			synchronized (selectMutex) {
				selectFailed = true;
			}
			reselect();
			String err = L10n.TRANSPORT_ERR_401;
			mWidgetHandle.notifyTransportState(err);
			IPanelLog.d(TAG, "onSelectFailed 22");
			notifyError(err);
			IPanelLog.d(TAG, "onSelectFailed 33");
			notifyJson(CB.__ID_onStreamLost);
		}

		@Override
		public void onSelectSuccess(StreamSelector s) {
			IPanelLog.d(TAG, "onSelectSuccess:" + s.getSelectUri());
			mWidgetHandle.notifyTransportState(null);
			synchronized (selectMutex) {
				if (selectFailed) {
					selectFailed = false;

				}
			}
			IPanelLog.d(TAG, "onSelectSuccess end ");
		}

		@Override
		public void onSelectionLost(StreamSelector s) {
			IPanelLog.d(TAG, "onSelectionLost");
			String err = L10n.TRANSPORT_ERR_402;
			mWidgetHandle.notifyTransportState(err);
			notifyJson(CB.__ID_onStreamLost);
		}

		@Override
		public void onSelectionResumed(StreamSelector s) {
			IPanelLog.d(TAG, "onSelectionResumed");
			mWidgetHandle.notifyTransportState(null);
			notifyJson(CB.__ID_onStreamResumed);
		}
	};

	class playStateListener implements PlayStateListener {
		int index = -1;

		public playStateListener(int i) {
			index = i;
		}

		@Override
		public void onSelectionStart(TeeveePlayer player, int program_number) {

		}

		@Override
		public void onPlayProcessing(int program_number) {

		}

		@Override
		public void onPlaySuspending(int program_number) {

		}

		@Override
		public void onPlayError(int program_number, String msg) {

		}

	}

	class programStateListener implements ProgramStateListener {
		int index = -1;

		public programStateListener(int i) {
			index = i;
		}

		@Override
		public void onProgramReselect(int program_number, String newuri) {

		}

		@Override
		public void onProgramDiscontinued(int program_number) {

		}

	}

	LiveDataListener mLiveDataListener = new LiveDataListener() {
		public void onLiveInfoUpdated(int mask) {
			IPanelLog.d(TAG, "onLiveInfoUpdated mask = " + mask);
			 notifyJson(CB.__ID_onLiveInfoUpdated, mask + "");
		};
	};
	PlayWidgetControlCallback widgetCallback = new PlayWidgetControlCallback() {

		@Override
		public void onWidgetChecked(int flags) {
			notifyJson(CB.__ID_onWidgetChecked, flags + "");
		}

	};

	PlayStateListener playStateListener = new PlayStateListener() {

		@Override
		public void onSelectionStart(TeeveePlayer player, int program_number) {/*-ignored ,ResourceScheduler done */
			IPanelLog.d(TAG, "onSelectionStart");
		}

		@Override
		public void onPlayProcessing(int program_number) {
			notifyWidgetSwitchEnd(null);
		}

		@Override
		public void onPlaySuspending(int program_number) {
		}

		@Override
		public void onPlayError(int program_number, String msg) {
			IPanelLog.d(TAG, "onPlayError :" + msg);
			notifyError(L10n.PROGRAM_ERR_410);
		}
	};

	ProgramStateListener programStateListener = new ProgramStateListener() {

		@Override
		public void onProgramReselect(int program_number, String newuri) {
			IPanelLog.d(TAG, "onProgramReselect program_number = " + program_number + ";newuri = "
					+ newuri);

		}

		@Override
		public void onProgramDiscontinued(int program_number) {
			IPanelLog.d(TAG, "onProgramDiscontinued program_number = " + program_number);

		}

	};

	private void reselect() {
		synchronized (mutex) {
			selection.reselect();
		}
	}

	ProgramDescramberCallback descramblerCallback = new ProgramDescramberCallback() {
		@Override
		public void onCaModuleDispatched(int moduleId) {
			// notifyJson(CB.__ID_onCaModuleDispatched, moduleId + "");
		}

		@Override
		public void onDescramblingState(int code, String err) {
			IPanelLog.d(TAG, "onDescramblingState err code = " + code + "err = " + err);
			mWidgetHandle.notifyDescramblingState(code, err);
		}

		@Override
		public void onCaCardState(int code, String msg) {
			mWidgetHandle.notifySmartcardState(code, msg);
		}
	};

	public String onTransmit(int code, String json, ipaneltv.toolkit.JsonParcelable p, Bundle b)
			throws JSONException {
		IPanelLog.i(TAG, "------>navigaton transmit json is=" + json);
		switch (code) {
		case __ID_select: {
			JSONObject o = (JSONObject) new JSONTokener(json).nextValue();
			String puri = null;
			Object obj = o.get("puri");
			IPanelLog.i(TAG, "------>navigaton transmit obj=" + obj);
			if (obj != null) {
				puri = (String) obj;
				if(puri.equals("no")){
					puri = null;
				}
			}
			select(o.getLong("freq"), o.getInt("fflags"), puri, o.getInt("pflags"));
			break;
		}
		default:
			return super.onTransmit(code, json, p, b);
		}
		return null;
	}

	@Override
	public void checkPassword(String pwd) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void clearCache(int flags) {
		// TODO Auto-generated method stub
		
	}

}
