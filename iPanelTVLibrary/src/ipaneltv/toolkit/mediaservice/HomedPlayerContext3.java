package ipaneltv.toolkit.mediaservice;

import ipaneltv.toolkit.IPanelLog;
import ipaneltv.toolkit.JsonParcelable;
import ipaneltv.toolkit.db.DatabaseObjectification.ChannelKey;
import ipaneltv.toolkit.media.MediaSessionInterface.HomedProgramPlayerInterface3;
import ipaneltv.toolkit.mediaservice.components.CaDescramblingManager.ProgramDescramberCallback;
import ipaneltv.toolkit.mediaservice.components.CaDescramblingManager.ProgramDescrambler;
import ipaneltv.toolkit.mediaservice.components.L10n;
import ipaneltv.toolkit.mediaservice.components.LiveDataManager;
import ipaneltv.toolkit.mediaservice.components.LiveDataManager.LiveDataListener;
import ipaneltv.toolkit.mediaservice.components.PlayResourceScheduler;
import ipaneltv.toolkit.mediaservice.components.PlayResourceScheduler.ResourcesState;
import ipaneltv.toolkit.mediaservice.components.PlayWidgetManager.PlayWidgetControl;
import ipaneltv.toolkit.mediaservice.components.PlayWidgetManager.PlayWidgetControlCallback;
import ipaneltv.toolkit.wardship2.WardshipIndicater;

import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.json.JSONException;
import org.json.JSONObject;
import org.json.JSONStringer;
import org.json.JSONTokener;

import android.media.TeeveePlayer;
import android.media.TeeveePlayer.PlayStateListener;
import android.media.TeeveePlayer.ProgramStateListener;
import android.net.telecast.FrequencyInfo;
import android.net.telecast.ProgramInfo;
import android.net.telecast.SignalStatus;
import android.net.telecast.StreamSelector;
import android.net.telecast.StreamSelector.SelectionStateListener;
import android.os.Bundle;
import android.util.Log;

public class HomedPlayerContext3<T extends MediaPlaySessionService> extends
		TeeveePlayerBaseContext<T> implements HomedProgramPlayerInterface3 {
	abstract class CB implements HomedProgramPlayerInterface3.Callback {
	};

	private static final String TAG = HomedPlayerContext3.class.getSimpleName();
	protected final Object mutex = new Object();
	protected ResourcesState mPlayResource;

	protected PlayWidgetControl mWidgetHandle;
	protected ProgramDescrambler mDescrambler;
	protected WardshipIndicater mWardshipIndicater;
	private boolean contextReady = false;
	protected LiveDataManager mLiveData;
	Set<ChannelKey> unlockset = new HashSet<ChannelKey>();
	ChannelKey key;

	public HomedPlayerContext3(T service) {
		super(service);
	}

	@SuppressWarnings("rawtypes")
	@Override
	public void onCreate() {
		IPanelLog.d(TAG, "onCreate in");
		LiveNetworkApplication app = getSessionService().getApp();
		Bundle b = getBundle();
		int pri = PlayResourceScheduler.PRIORITY_DEFAULT, size = 1;
		boolean soft = false;
		if (b != null) {
			pri = b.getInt("priority", pri);
			soft = b.getBoolean("soft", false);
			size = b.getInt("pipSize", 1);
		}
		IPanelLog.d(TAG, "onCreate size = " + size);
		String root = app.getWardshipRoot();
		mWardshipIndicater = new WardshipIndicater(root);
		mPlayResource = app.getResourceScheduler()
				.createLivePlayState(false, pri, soft, size, 0, 0);
		IPanelLog.d(TAG, "onCreate ret.selectorHandle = " + mPlayResource.getPlayer()
				+ ";ret.playerHandle = " + mPlayResource.getSelector());
		mPlayResource.getSelector().setSelectionStateListener(selectionStateListener);
		mPlayResource.getPlayer().setListener(playStateListener, programStateListener);
		mWidgetHandle = app.getPlayWidgetManager().createControl(widgetCallback);
		mDescrambler = app.getCaDescramblingManager().createDescrambler(descramblerCallback);
		mLiveData = app.getLiveDataManager();
		IPanelLog.d(TAG, "mLiveData = " + mLiveData + ";mLiveDataListener = " + mLiveDataListener);
		mLiveData.addLiveDataListener(mLiveDataListener);
	}

	protected int getStreamType(String type) {
		return -1;
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
		mDescrambler.close();
		mPlayResource = null;
		mWidgetHandle = null;
		mDescrambler = null;
	}

	protected boolean reserveAll() {
		return mPlayResource.reserve() && //
				mDescrambler.reserve() && //
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
		Log.d(TAG, "loosenAll clearState = " + clearState + ";isRelease() = " + isRelease());
		if (clearState && isRelease()) {
			mPlayResource.destroy();
		} else {
			mPlayResource.loosen(clearState);
		}
		mDescrambler.loosen(clearState);
		mWidgetHandle.loosen(clearState);
	}

	/**
	 * 客户端请求获得资源
	 * 
	 * @return
	 */
	@Override
	public boolean reserve() {// 客户端请求
		IPanelLog.d(TAG, "before reserve 22");
		synchronized (mutex) {
			IPanelLog.d(TAG, "reserve in");
			if (contextReady ? false : reserveAllSafe()) {
				mWardshipIndicater.ensureOnload();
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
		IPanelLog.d(TAG, "before loosen 22");
		synchronized (mutex) {
			if (contextReady) {
				IPanelLog.d(TAG, "onLoosen(clearState=" + clearState + ")");
				contextReady = false;
				mWardshipIndicater.stopWatching();
				mWidgetHandle.clearWidgetMessage();
				loosenAll(clearState);
				selection.clearUri();
			}
		}
		IPanelLog.d(TAG, "end loosen");
	}

	@Override
	public void stop(int flag) {
		IPanelLog.d(TAG, "stop flag = " + flag + ";suspend = " + suspend);
		IPanelLog.d(TAG, "before stop");
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
	public final void setVolume(float v) {
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
	public void checkPassword(String pwd) {
		IPanelLog.d(TAG, "before checkPassword pwd = " + pwd);
		Log.d(TAG, "check_PSW_CWJ:" + pwd);
		synchronized (mutex) {
			try {
				if (mWardshipIndicater.checkPwd(pwd)) {
					if (key != null) {
						unlockset.add(key);
					}
					Log.d(TAG, "check_password_CWJ" + "true");
					notifyJson(CB.__ID_onPasswprdChecked, true + "");
				} else {
					Log.d(TAG, "check_password_CWJ" + "false");
					notifyJson(CB.__ID_onPasswprdChecked, false + "");
				}
			} catch (Exception e) {
				IPanelLog.e(TAG, "checkPassword error e = " + e.getMessage());
			}
		}
		IPanelLog.d(TAG, "end checkPassword");
	}

	@Override
	public final void syncSignalStatus() {
		IPanelLog.d(TAG, "before syncSignalStatus");
		synchronized (mutex) {
			if (mPlayResource.isReserved()) {
				SignalStatus ss = mPlayResource.getSelector().getSignalStatus();
				Log.d(TAG, "ss = " + ss);
				if (ss != null) {
					notifyJson(CB.__ID_onSyncSignalStatus, ss.toString());
				} else {
					notifyJson(CB.__ID_onSyncSignalStatus, "0,0,0,0,0");
				}
			}
		}
		IPanelLog.d(TAG, "end syncSignalStatus");
	}

	@Override
	public final void solveProblem() {
		IPanelLog.d(TAG, "before solveProblem");
		synchronized (mutex) {
			mDescrambler.solveProblem();
		}
		IPanelLog.d(TAG, "end solveProblem");
	}

	@Override
	public final void enterCaApp(String uri) {
		IPanelLog.d(TAG, "before enterCaApp");
		synchronized (mutex) {
			mDescrambler.enterCaApp(uri);
		}
		IPanelLog.d(TAG, "end enterCaApp");
	}

	@Override
	public void setTeeveeWidget(int flags) {
		IPanelLog.d(TAG, "before setTeeveeWidget");
		synchronized (mutex) {
			mWidgetHandle.setTeeveeWidget(flags);
		}
		IPanelLog.d(TAG, "end setTeeveeWidget");
	}

	@Override
	public void checkTeeveeWidget(int flags) {
		IPanelLog.d(TAG, "before checkTeeveeWidget");
		synchronized (mutex) {
			mWidgetHandle.checkTeeveeWidget(flags);
		}
		IPanelLog.d(TAG, "end checkTeeveeWidget");
	}

	@Override
	public void setProgramFlags(int flags) {
		IPanelLog.d(TAG, "before setProgramFlags");
		synchronized (mutex) {
			if (suspend) {
				IPanelLog.w(TAG, "is suspend, start first!");
				return;
			}
			ProgramInfo fi = ProgramInfo.fromString(selection.puri);
			onSelectProgram(fi, flags, false);
		}
		IPanelLog.d(TAG, "end setProgramFlags");
	}

	@Override
	public void syncMediaTime() {
		IPanelLog.d(TAG, "before syncMediaTime");
		synchronized (mutex) {
			if (suspend) {
				IPanelLog.w(TAG, "is suspend, start first!");
				return;
			}
			long t = mPlayResource.getPlayer().getPlayTime();
			notifyJson(CB.__ID_onSyncMediaTime, t + "");
		}
		IPanelLog.d(TAG, "end syncMediaTime");
	}

	@Override
	public void observeProgramGuide(ChannelKey ch, long focusTime) {
		IPanelLog.d(TAG, "before observeProgramGuide");
		synchronized (mutex) {
			mLiveData.observeProgramGuide(ch, focusTime);
		}
		IPanelLog.d(TAG, "end observeProgramGuide");
	}

	private final void notifyWidgetSwitchEnd(String err) {
		try {
			int code = err == null ? 0 : L10n.getErrorCode(err, 100000);
			mWidgetHandle.notifySwitchingEnd(code, err);
		} catch (Exception e) {
			Log.e(TAG, "notifyWidgetSwitchEnd e = " + e.toString());
		}
	}

	@Override
	public void select(String uri, long freq, int fflags, int pn, int pflags, int delay) {
		FrequencyInfo fi = null;
		ProgramInfo pi = null;
		ChannelKey channelKey;
		if (freq == -1) {
			channelKey = mLiveData.getChannelKeyByPn(pn);
		} else {
			channelKey = ChannelKey.obten(freq, pn);
		}
		IPanelLog.d(TAG, "before select 3 freq = " + freq + ";pn = " + pn);
		synchronized (mutex) {
			@SuppressWarnings("unused")
			String err = null;
			boolean done = false;
			try {
				if ((freq == 0 && pn == 0) || channelKey == null) {
					mWidgetHandle.showMessage();
					return;
				}
				mWidgetHandle.clearWidgetMessage(fflags);
				if (key == null || !key.equals(channelKey)) {
					unlockset.clear();
				}
				key = channelKey;
				fi = mLiveData.getFrequencyInfo(key.getFrequency());
				pi = mLiveData.getProgramInfo(key);
				IPanelLog.d(TAG, "before select 1 fi = " + fi);
				if (fi != null) {
					onSelect(fi, fflags, pi, pflags, true);
					monitorProgramStream(freq, pn);
					selection.pnset(pn, pflags);
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
					if (freq != 0 && pn != 0) {
						stop(TeeveePlayer.FLAG_VIDEO_FRAME_BLACK);
					}
				}
				notifyJson(CB.__ID_onResponseSelect, done + "");
			}
		}
		IPanelLog.d(TAG, "end select");
	}

	@Override
	public void startShift(String uri, int fflags) {
		// TODO
	}

	@Override
	public void seek(long millis) {
		// TODO
	}

	protected ProgramInfo checkProgramInfo(long f, int p, int aPid) {
		ProgramInfo pi = null;
		if (p <= 0) {
			int pn = mLiveData.getProgramNum(f, aPid);
			Log.d(TAG, "select pn = " + pn);
			if (pn > 0) {
				pi = mLiveData.getProgramInfo(ChannelKey.obten(f, pn));
				Log.d(TAG, "select pi = " + pi);
			}
		} else {
			ProgramInfo info = mLiveData.getProgramInfo(ChannelKey.obten(f, p));
			if (info != null) {
				pi = info;
			}
		}
		return pi;
	}

	@Override
	public long getPlayTime() {
		return -1;
	}

	// =====================
	protected Selection selection = new Selection();
	private boolean playerStopped = false;
	private boolean suspend = false;
	private float volumeSelect = 0.5f;

	// private String pendingSolveUri = null;

	private void onSelect(FrequencyInfo fi, int fflags, ProgramInfo pi, int pflags, boolean notify) {
		IPanelLog.d(TAG, "onSelect:(" + fi + "," + fflags + "," + pi + "," + pflags + ")");
		if (fi == null) {
			return;
		}
		String furi = fi.toString();
		String puri = null;
		if (pi != null) {
			puri = pi.toString();
		}
		IPanelLog.d(TAG, "onSelect aaa");
		if (selection.furi == null || !selection.isEquals(selection.furi, furi)
				|| selection.fforce(fflags)) {
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
		monitorProgramStream(furi, puri);
		IPanelLog.d(TAG, "onSelect 222");
		if (pi == null) {
			return;
		}
		IPanelLog.d(TAG, "onSelect 33 pi.getVideoPID() = " + pi.getVideoPID());
		mWidgetHandle.notifySwitchingStart(pi.getVideoPID() < 0);
		if (pi.getAudioPID() == ProgramInfo.PID_UNDEFINED) {
			pi.setAudioPID(0x1ffe);
			pi.setAudioStreamType("audio_mpeg2");
		}
		if (pi.getVideoPID() == ProgramInfo.PID_UNDEFINED) {
			pi.setVideoPID(0x1ffe);
			pi.setVideoStreamType("video_mpeg2");
		}
		playAndDescramble(fi, pi, pflags, notify);
		IPanelLog.d(TAG, "onSelect 444");
		notifyWidgetSwitchEnd(null);
		// callback
	}

	// 给子类重写，以适应某些项目需要先启动解扰再播放。
	protected void playAndDescramble(FrequencyInfo fi, ProgramInfo pi, int pflags, boolean notify) {
		if (!onSelectProgram(pi, pflags, notify)) {
			notifyError(L10n.SELECT_ERR_431);
			notifyWidgetSwitchEnd(L10n.SELECT_ERR_431);
			return;
		}
		if (suspend)
			suspend = false;
		IPanelLog.d(TAG, "onSelect 333");
		ChannelKey key = ChannelKey.obten(fi.getFrequency(), pi.getProgramNumber());
		IPanelLog.d(TAG, "before mDescrambler.start()");
		mDescrambler.stop();
		mWidgetHandle.notifyDescramblingState(0, null);
		boolean d = mDescrambler.start(key, pi.getAudioPID(), pi.getVideoPID());
		IPanelLog.d(TAG, "after mDescrambler.start()");
		if (!d) {
			notifyError("select descrambling failed");
			notifyWidgetSwitchEnd(L10n.SELECT_ERR_432);
			return;
		}
	}

	protected boolean doPlay(String puri, int pflags) {
		if (!onSelectProgram(puri, pflags, false)) {
			notifyError(L10n.SELECT_ERR_431);
			notifyWidgetSwitchEnd(L10n.SELECT_ERR_431);
			return false;
		}
		if (suspend)
			suspend = false;
		return true;
	}

	protected boolean doDescramble(FrequencyInfo fi, ProgramInfo pi) {
		ChannelKey key = ChannelKey.obten(fi.getFrequency(), pi.getProgramNumber());
		IPanelLog.d(TAG, "before mDescrambler.start()");
		mDescrambler.stop();
		boolean d = mDescrambler.start(key, pi.getAudioPID(), pi.getVideoPID());
		IPanelLog.d(TAG, "after mDescrambler.start()");
		if (!d) {
			notifyError("select descrambling failed");
			notifyWidgetSwitchEnd(L10n.SELECT_ERR_432);
			return false;
		}
		return true;
	}

	private boolean onSelectProgram(ProgramInfo pi, int flags, boolean notify) {
		ensurePlayerStarted();
		if (pi.getVideoPID() == ProgramInfo.PID_UNDEFINED) {
			flags = flags & TeeveePlayer.FLAG_VIDEO_FRAME_BLACK;
		}
		if (checkcardState() && !isLocked(notify)) {
			if (mPlayResource.getPlayer().selectProgram(pi, flags)) {
				selection.pset(pi.toString(), flags);
				onSetVolume(volumeSelect);
				return true;
			}
		} else {
			stopPlayer(TeeveePlayer.FLAG_VIDEO_FRAME_BLACK);
			return true;
		}
		return false;
	}

	protected boolean isLocked(boolean notify) {
		// 检查节目是否枷锁
		Log.d(TAG, "isLocked notify= " + notify);
		try {
			boolean locked = mWardshipIndicater.chack(key);
			if (locked && !unlockset.contains(key)) {
				stopPlayer(0);
				if (notify) {
					JSONStringer str = new JSONStringer();
					str.object();
					str.key("freq").value(key.getFrequency());
					str.key("pn").value(key.getProgram());
					str.endObject();
					Log.d(TAG, "check_onChannelLocked_in_CWJ");
					notifyJson(CB.__ID_onChannelLocked, str.toString());
				}
				return true;
			}
		} catch (Exception e) {
			Log.e(TAG, "isLocked e = " + e.toString());
		}
		return false;
	}

	private boolean onSelectProgram(String puri, int flags, boolean notyfy) {
		try {
			return onSelectProgram(ProgramInfo.fromString(puri), flags, notyfy);
		} catch (Exception e) {
			return false;
		}
	}

	private boolean onSelectProgram(long freq, String puri, int flags, boolean notyfy) {
		IPanelLog.d(TAG, "onSelectProgram freq = " + freq + ";freq = " + freq);
		ProgramInfo pi = ProgramInfo.fromString(puri);
		mLiveData.updateStreamPids(new ChannelKey(freq, pi.getProgramNumber()), pi);
		if (!onSelectProgram(pi, flags, notyfy)) {
			notifyError(L10n.SELECT_ERR_431);
			notifyWidgetSwitchEnd(L10n.SELECT_ERR_431);
			return false;
		}
		if (suspend)
			suspend = false;
		IPanelLog.d(TAG, "onSelectProgram 333");
		ChannelKey key = ChannelKey.obten(freq, pi.getProgramNumber());
		IPanelLog.d(TAG, "onSelectProgram before mDescrambler.start()");
		mDescrambler.stop();
		boolean d = mDescrambler.start(key, pi.getAudioPID(), pi.getVideoPID());
		IPanelLog.d(TAG, "onSelectProgram after mDescrambler.start()");
		if (!d) {
			notifyError("onSelectProgram select descrambling failed");
			notifyWidgetSwitchEnd(L10n.SELECT_ERR_432);
			return false;
		}
		return true;
	}

	private void reselectProgram() {
		IPanelLog.d(TAG, "reselectProgram");
		synchronized (mutex) {
			selection.reselectProgram();
		}
	}

	// 检查CA状态。一般不需要检查，在某些项目中清流也需要依赖卡状态的时候实现该接口
	protected boolean checkcardState() {
		return true;
	}

	protected int getcardState() {
		synchronized (mutex) {
			return mDescrambler.getCardState();
		}
	}

	protected void reselectProgram(final int program_number, final String newuri) {
		new Thread(new Runnable() {

			@Override
			public void run() {
				IPanelLog.d(TAG, "reselectProgram newuri = " + newuri);
				synchronized (mutex) {
					selection.preselect(program_number, newuri);
				}
			}
		}).start();
	}

	private void reselect() {
		synchronized (mutex) {
			stopPlayer(0);
			selection.reselect();
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

	/**
	 * 监控当前节目cable中si信息的变化，子类可重写该方法进行处理
	 * 
	 * @param uri
	 *            当前正在播放节目的furi
	 * @param ts2
	 */
	protected void monitorProgramStream(String furi, String puri) {

	}

	/**
	 * 监控当前节目cable中si信息的变化，子类可重写该方法进行处理
	 * 
	 * @param freq
	 *            当前正在播放节目的频点
	 * @param pn
	 */
	protected void monitorProgramStream(long freq, int pn) {

	}

	protected void channelUndefine(final long freq, final int pn) {
		new Thread(new Runnable() {

			@Override
			public void run() {
				synchronized (mutex) {
					try {
						Log.d(TAG, "channelUndefine 1 freq" + freq + ";selection.freq = "
								+ selection.freq + ";pn = " + pn + ";selection.pn = "
								+ selection.pn);
						if (selection.freq == freq && selection.pn == pn) {
							notifyWidgetSwitchEnd(L10n.SELECT_ERR_433);
							stop(TeeveePlayer.FLAG_VIDEO_FRAME_BLACK);
							JSONObject jo = new JSONObject();
							jo.put("f", selection.freq);
							jo.put("pn", selection.pn);
							jo.put("code", 433);
							jo.put("err", L10n.SELECT_ERR_433);
							notifyJson(CB.__ID_onDescramError, jo.toString());
						}
					} catch (Exception e) {
						Log.e(TAG, "channelUndefine e = " + e);
					}

				}
			}
		}).start();
	}

	protected void onSelectSuc(StreamSelector s) {
		notifyJson(CB.__ID_onStreamResumed);
		boolean locked = isLocked(true);
		Log.d(TAG, "onSelectSuc locked = " + locked);
	}

	private void stopPlayer(int flag) {
		IPanelLog.d(TAG, "stopPlayer flag" + flag);
		synchronized (mutex) {
			if ((flag & TeeveePlayer.FLAG_VIDEO_FRAME_FREEZE) == TeeveePlayer.FLAG_VIDEO_FRAME_BLACK) {
				mPlayResource.getPlayer().setFreeze(true, TeeveePlayer.FLAG_VIDEO_FRAME_BLACK);
				mPlayResource.getPlayer().stop();
				mPlayResource.getPlayer().setFreeze(true, TeeveePlayer.FLAG_VIDEO_FRAME_BLACK);
				playerStopped = true;
			} else if ((flag & TeeveePlayer.FLAG_VIDEO_FRAME_FREEZE) == TeeveePlayer.FLAG_VIDEO_FRAME_FREEZE) {
				mPlayResource.getPlayer().setFreeze(true, TeeveePlayer.FLAG_VIDEO_FRAME_FREEZE);
				mPlayResource.getPlayer().stop();
				mPlayResource.getPlayer().setFreeze(true, TeeveePlayer.FLAG_VIDEO_FRAME_FREEZE);
				playerStopped = true;
			}
		}
	}

	private boolean ensurePlayerStarted() {
		synchronized (mutex) {
			if (mPlayResource.isReserved()) {
				boolean b = true;
				IPanelLog.d(TAG, "ensurePlayerStarted playerStopped" + playerStopped);
				if (playerStopped) {
					b = mPlayResource.getPlayer().start();
					if (b) {
						mPlayResource.getPlayer().setFreeze(false, 0);
						onSetVolume(volumeSelect);
						playerStopped = false;
					}
				}
				return b;
			}
			return false;
		}
	}

	protected void onSetVolume(float f) {
		if (mPlayResource.isReserved()) {
			IPanelLog.d(TAG, "onSetVolume f= " + f);
			mPlayResource.getPlayer().setVolume(volumeSelect);
		}
	}

	protected String getCurrentFuri() {
		return selection.furi;
	}

	protected void descramblingState(int code, String err) {

		IPanelLog.d(TAG, "descramblingState  11 err code = " + code + "err = " + err);
		try {
			JSONObject jo = new JSONObject();
			jo.put("f", selection.freq);
			jo.put("pn", selection.pn);
			if (err == null) {
				if (playerStopped) {
					reselectProgram();
				}
				jo.put("code", 0);
				jo.put("err", "null");
				notifyJson(CB.__ID_onDescramError, jo.toString());
			} else if (code == 452) {
				stopPlayer(TeeveePlayer.FLAG_VIDEO_FRAME_BLACK);
				// reselectProgram();
				jo.put("code", code);
				jo.put("err", err);
				notifyJson(CB.__ID_onDescramError, jo.toString());
			} else if (code != 821 && code != 822) {
				if (code == 0) {
					jo.put("code", 1008);
					jo.put("err", err);
				} else {
					jo.put("code", code);
					jo.put("err", err);
				}
				notifyJson(CB.__ID_onDescramError, jo.toString());
				stopPlayer(TeeveePlayer.FLAG_VIDEO_FRAME_BLACK);
			} else if (err.equals("PROGRAM_LOCKED")) {
				IPanelLog.i(TAG, "err.equals(\"PROGRAM_LOCKED\")");
				stopPlayer(TeeveePlayer.FLAG_VIDEO_FRAME_BLACK);
			} else {
				// jo.put("code", 1008);
				// notifyJson(CB.__ID_onDescramError, jo.toString());
			}
		} catch (Exception e) {
			Log.e(TAG, "descramblingState e = " + e.getMessage());
		}

	}

	class Selection {
		public final String FSCHEME = "frequency://";
		protected String furi = null, puri = null;
		@SuppressWarnings("unused")
		private int fflags = 0, pflags = 0, pn = 0;
		private long freq = 0;

		void clearUri() {
			puri = furi = null;
			pflags = fflags = pn = 0;
			freq = 0;
		}

		void fset(String uri, long freq, int flags) {
			furi = uri;
			fflags = flags;
			this.freq = freq;
		}

		void pnset(int pn, int flags) {
			this.pn = pn;
			pflags = flags;
		}

		void pset(String uri, int flags) {
			puri = uri;
			pflags = flags;
		}

		void reselect() {
			if (furi != null && puri != null) {
				// 重新检查参数
				FrequencyInfo fi = FrequencyInfo.fromString(furi);
				fi = mLiveData.getFrequencyInfo(fi.getFrequency());
				ProgramInfo pinfo = ProgramInfo.fromString(puri);
				if (fi != null) {
					onSelect(fi, StreamSelector.SELECT_FLAG_FORCE, pinfo, pflags, false);
				}
			}
		}

		void reselectProgram() {
			IPanelLog.d(TAG, "Selection reselectProgram puri = " + puri);
			if (puri != null) {
				onSelectProgram(puri, pflags, false);
			}
		}

		boolean preselect(int program, String npuri) {
			IPanelLog.d(TAG, "preselect program = " + program + "; npuri = " + npuri);
			IPanelLog.d(TAG, "preselect pn = " + pn);
			if (pn == program) {
				mWidgetHandle.notifyDescramblingState(0, null);
				return onSelectProgram(freq, npuri, pflags, false);
			}
			return true;
		}

		boolean fforce(int flags) {
			return (flags & StreamSelector.SELECT_FLAG_FORCE) != 0;
		}

		boolean isEquals(String ofuri, String nfuri) {
			if (ofuri == null) {
				return true;
			}
			if (!ofuri.startsWith(FSCHEME) || !nfuri.startsWith(FSCHEME))
				return false;
			int ot = ofuri.indexOf('?');
			int nt = nfuri.indexOf('?');
			if (ot < 0 || nt < 0)
				return false;
			if (!ofuri.substring(FSCHEME.length(), ot).endsWith(
					nfuri.substring(FSCHEME.length(), nt))) {
				return false;
			}
			ofuri = ofuri.substring(ot + 1);
			nfuri = nfuri.substring(nt + 1);
			List<String> oparams = Arrays.asList(ofuri.split("&"));
			List<String> nparams = Arrays.asList(nfuri.split("&"));
			if (oparams.size() != nparams.size()) {
				return false;
			}
			if (!oparams.containsAll(nparams) || !nparams.containsAll(oparams)) {
				return false;
			}
			return true;
		}
	}

	final void notifyError(String msg) {
		notifyJson(CB.__ID_onPlayError, msg);
	}

	SelectionStateListener selectionStateListener = new SelectionStateListener() {

		@Override
		public void onSelectStart(StreamSelector selector) {/*- ignore, ResourceScheduler done */
			IPanelLog.d(TAG, "onSelectStart");
		}

		@Override
		public void onSelectFailed(StreamSelector s) {
			IPanelLog.d(TAG, "onSelectFailed 44 selection.furi = " + selection.furi);
			notifyJson(CB.__ID_onStreamLost);
			if (selection.furi != null) {
				new Thread(new Runnable() {

					@Override
					public void run() {
						reselect();
					}
				}).start();
				IPanelLog.d(TAG, "onSelectFailed 11");
				String err = L10n.TRANSPORT_ERR_401;
				mWidgetHandle.notifyTransportState(err);
				IPanelLog.d(TAG, "onSelectFailed 22");
				notifyError(err);
				IPanelLog.d(TAG, "onSelectFailed 33");
			}
		}

		@Override
		public void onSelectSuccess(StreamSelector s) {
			IPanelLog.d(TAG, "onSelectSuccess: 1" + s.getSelectUri());
			onSelectSuc(s);
			mWidgetHandle.notifyTransportState(null);
			SignalStatus ss = s.getSignalStatus();
			if (ss != null) {
				notifyJson(CB.__ID_onSyncSignalStatus, ss.toString());
			}
			IPanelLog.d(TAG, "onSelectSuccess end ");
		}

		@Override
		public void onSelectionLost(StreamSelector s) {
			IPanelLog.d(TAG, "onSelectionLost");
			long f = s.getCurrentFrequency();
			int pn = -1;
			if (s.getCurrentFrequency() == selection.freq) {
				pn = selection.pn;
			}
			doSelectLost(f, pn);
		}

		@Override
		public void onSelectionResumed(StreamSelector s) {
			IPanelLog.d(TAG, "onSelectionResumed");
			long f = s.getCurrentFrequency();
			int pn = -1;
			if (s.getCurrentFrequency() == selection.freq) {
				pn = selection.pn;
			}
			doSelectResume(f, pn);
			SignalStatus ss = s.getSignalStatus();
			if (ss != null) {
				notifyJson(CB.__ID_onSyncSignalStatus, ss.toString());
			}
		}
	};

	protected void doSelectLost(long f, int pn) {
		stopPlayer(0);
		String err = L10n.TRANSPORT_ERR_402;
		mWidgetHandle.notifyTransportState(err);
		notifyJson(CB.__ID_onStreamLost);
	}

	protected void doSelectResume(long f, int pn) {
		new Thread(new Runnable() {

			@Override
			public void run() {
				synchronized (mutex) {
					selection.reselect();
				}
			}
		}).start();
		mWidgetHandle.notifyTransportState(null);
		notifyJson(CB.__ID_onStreamResumed);
		boolean locked = isLocked(true);
		Log.d(TAG, "onSelectSuc locked = " + locked);
	}

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
			onProgramInfoChanged(program_number, newuri);
		}

		@Override
		public void onProgramDiscontinued(int program_number) {
			IPanelLog.d(TAG, "onProgramDiscontinued program_number = " + program_number);

		}

	};

	LiveDataListener mLiveDataListener = new LiveDataListener() {
		public void onLiveInfoUpdated(int mask) {
			IPanelLog.d(TAG, "onLiveInfoUpdated mask = " + mask);
			notifyJson(CB.__ID_homed_onLiveInfoUpdated, mask + "");
		};
	};
	PlayWidgetControlCallback widgetCallback = new PlayWidgetControlCallback() {

		@Override
		public void onWidgetChecked(int flags) {
			notifyJson(CB.__ID_onWidgetChecked, flags + "");
		}

	};

	ProgramDescramberCallback descramblerCallback = new ProgramDescramberCallback() {
		@Override
		public void onCaModuleDispatched(int moduleId) {
			onCaModule(moduleId);
			notifyJson(CB.__ID_homed_onCaModuleDispatched, moduleId + "");
		}

		@Override
		public void onDescramblingState(int code, String err) {
			IPanelLog.d(TAG, "onDescramblingState err code = " + code + "err = " + err);
			if (code == 51) {
				mWidgetHandle.notifyDescramblingState(code, err, key);
			} else {
				mWidgetHandle.notifyDescramblingState(code, err);
			}
			descramblingState(code, err);
		}

		@Override
		public void onCaCardState(int code, String msg) {
			mWidgetHandle.notifySmartcardState(code, msg);
			IPanelLog.d(TAG, "onCaCardState err code = " + code + "msg = " + msg);
			try {
				JSONObject jo = new JSONObject();
				jo.put("f", selection.freq);
				jo.put("pn", selection.pn);
				if (msg == null) {
					jo.put("code", 0);
					jo.put("err", "null");
				} else {
					jo.put("code", 452);
					jo.put("err", msg);
				}
				notifyJson(CB.__ID_onDescramError, jo.toString());
			} catch (Exception e) {
				Log.e(TAG, "onCaCardState err = " + e.getMessage());
			}
			if (msg == null) {
				if (playerStopped) {
					reselectProgram();
				}
			} else {
				stopPlayer(TeeveePlayer.FLAG_VIDEO_FRAME_BLACK);
			}
		}
	};

	protected void onCaModule(int moduleId) {

	}

	@Override
	public String onTransmit(int code, String json, JsonParcelable p, Bundle b)
			throws JSONException {
		IPanelLog.i(TAG, "2015------>navigaton transmit json is=" + json + " code = " + code);
		switch (code) {
		case __ID_homed_select: {
			JSONObject o = (JSONObject) new JSONTokener(json).nextValue();
			select(o.getString("uri"), o.getLong("freq"), o.getInt("fflags"), o.getInt("program"),
					o.getInt("pflags"), o.getInt("delay"));
			break;
		}
		case __ID_homed_startShift: {
			JSONObject o = (JSONObject) new JSONTokener(json).nextValue();
			startShift(o.getString("uri"), o.getInt("fflags"));
			break;
		}
		case __ID_homed_seek: {
			JSONObject o = (JSONObject) new JSONTokener(json).nextValue();
			seek(o.getLong("millis"));
			break;
		}
		case __ID_checkPassword: {
			checkPassword(json);
			break;
		}
		case __ID_syncSignalStatus:
			syncSignalStatus();
			break;
		default:
			Log.i(TAG, "default......");
			return super.onTransmit(code, json, p, b);
		}
		return null;
	}

	public void clearWidgetMessage() {
		mWidgetHandle.clearWidgetMessage();
	}

	@Override
	public void clearCache(int flags) {
		// TODO Auto-generated method stub

	}

}
