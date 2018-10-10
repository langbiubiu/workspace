package ipaneltv.toolkit.mediaservice;

import ipaneltv.toolkit.IPanelLog;
import ipaneltv.toolkit.JsonParcelable;
import ipaneltv.toolkit.SectionBuilder;
import ipaneltv.toolkit.db.DatabaseObjectification.ChannelKey;
import ipaneltv.toolkit.media.MediaSessionInterface.LiveProgramPlayerInterface;
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

import java.io.FileDescriptor;
import java.io.FileOutputStream;
import java.io.IOException;
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
import android.media.TeeveeRecorder.OnRecordStateListener;
import android.net.telecast.FrequencyInfo;
import android.net.telecast.ProgramInfo;
import android.net.telecast.SignalStatus;
import android.net.telecast.StreamSelector;
import android.net.telecast.StreamSelector.SelectionStateListener;
import android.os.Bundle;
import android.os.ParcelFileDescriptor;
import android.os.Parcelable;
import android.util.Log;
import android.util.SparseArray;

public class LiveProgramPlayerContext<T extends MediaPlaySessionService> extends
		TeeveePlayerBaseContext<T> implements LiveProgramPlayerInterface {
	abstract class CB implements LiveProgramPlayerInterface.Callback {
	};

	private static final String TAG = LiveProgramPlayerContext.class.getSimpleName();
	protected final Object mutex = new Object();
	protected ResourcesState mPlayResource;

	protected PlayWidgetControl mWidgetHandle;
	protected ProgramDescrambler mDescrambler;
	protected WardshipIndicater mWardshipIndicater;
	private boolean contextReady = false;
	protected LiveDataManager mLiveData;
	SparseArray<ProgramDescrambler> pipDescramblers = new SparseArray<ProgramDescrambler>();
	Set<ChannelKey> unlockset = new HashSet<ChannelKey>();
	ChannelKey key;

	public LiveProgramPlayerContext(T service) {
		super(service);
	}

	@SuppressWarnings("rawtypes")
	@Override
	public void onCreate() {
		IPanelLog.d(TAG, "onCreate in");
		LiveNetworkApplication app = getSessionService().getApp();
		Bundle b = getBundle();
		int pri = PlayResourceScheduler.PRIORITY_DEFAULT;
		boolean soft = false;
		if (b != null) {
			pri = b.getInt("priority", pri);
			soft = b.getBoolean("soft", false);
		}
		String root = app.getWardshipRoot();
		mWardshipIndicater = new WardshipIndicater(root);
		mPlayResource = app.getResourceScheduler().createLivePlayState(false, pri, soft);
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
		Log.d(TAG, "loosenAll clearState = "+ clearState +";isRelease() = "+ isRelease());
		if(clearState && isRelease()){
			mPlayResource.destroy();
		}else{
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
		Log.d( TAG, "check_PSW_CWJ:" + pwd );
		synchronized (mutex) {
			try {
				if (mWardshipIndicater.checkPwd(pwd)) {
					if (key != null) {
						unlockset.add(key);
					}
					Log.d( TAG, "check_password_CWJ" + "true" );
					notifyJson(CB.__ID_onPasswprdChecked, true + "");
				} else {
					Log.d( TAG, "check_password_CWJ" + "false" );
					notifyJson(CB.__ID_onPasswprdChecked, false + "");
				}
			} catch (Exception e) {
				IPanelLog.e(TAG, "checkPassword error e = " + e.getMessage());
			}
		}
		IPanelLog.d(TAG, "end checkPassword");
	}

	/**
	 * 打开画中画播放器
	 * 
	 * @param size
	 *            请求打开的画中画路数
	 */
	@Override
	public final boolean pipOpenPlayers(int size, int flags) {
		synchronized (mutex) {
			if (mPlayResource.isReserved()) {
				return mPlayResource.openPipPlayers(size, flags);
			}
			return false;
		}
	}

	/**
	 * 关闭画中画播放器并释放资源。
	 */
	@Override
	public final boolean pipClosePlayers() {
		synchronized (mutex) {
			boolean b = false;
			if (mPlayResource.isReserved()) {
				b = mPlayResource.closePipPlayers();
				for (int i = 0; i < pipDescramblers.size(); i++) {
					pipDescramblers.get(pipDescramblers.keyAt(i)).close();
				}
				pipDescramblers.clear();
			}
			return b;
		}
	}

	@Override
	public final void pipSetFreqency(int index, long freq, int flags) {
		synchronized (mutex) {
			if (mPlayResource.isReserved()) {
				mPlayResource.pipSetFreqency(index, freq, flags);
			}
		}
	}

	/**
	 * 设置画中画的参数，并进行播放。 * @param index 画中画player的索引
	 * 
	 * @param prog
	 *            节目信息
	 * @param flag
	 *            falg数组
	 * @param r
	 *            播放视图大小的数组
	 * @return
	 */
	@Override
	public final void pipSetProgram(int index, int prog, int x, int y, int w, int h, int flags) {
		synchronized (mutex) {
			ProgramInfo pi = null;
			Log.d(TAG, "pipSetProgram index = " + index + ";prog = " + prog + "flags = " + flags
					+ ";x = " + x + ";y = " + y + ";w = " + w + ";h = " + h);
			if (mPlayResource.isReserved()) {
				ChannelKey key = ChannelKey.obten(selection.freq, prog);
				pi = mLiveData.getProgramInfo(key);
				Log.d(TAG, "pipSetProgram pi = " + pi);
				boolean m = mPlayResource.pipSetProgram(index, pi, x, y, w, h, flags);
				Log.d(TAG, "pipSetProgram m = " + m);
				if (m) {
					ProgramDescrambler pipDescrambler;
					if ((pipDescrambler = pipDescramblers.get(index)) == null) {
						pipDescrambler = getSessionService().getApp().getCaDescramblingManager()
								.createDescrambler(descramblerCallback);
						pipDescramblers.append(index, pipDescrambler);
					}
					if (pipDescrambler.reserve()) {
						Log.d(TAG, "before mDescrambler.start()");
						pipDescrambler.stop();
						boolean d = pipDescrambler.start(key, pi.getAudioPID(), pi.getVideoPID());
						Log.d(TAG, "setPipPlayers Descrambler d = " + d);
					}
				}
			}
		}
	}

	@Override
	public void pipLoadAnimation(ParcelFileDescriptor pfd) {
		synchronized (mutex) {
			if (mPlayResource.isReserved()) {
				mPlayResource.getPlayer().loadAnimation(pfd.getFileDescriptor());
			}
		}
	}

	@Override
	public void pipActAnimation(int action, int p1, int p2, int flags) {
		synchronized (mutex) {
			if (mPlayResource.isReserved()) {
				mPlayResource.getPlayer().actAnimation(action, p1, p2, flags);
			}
		}
	}

	@Override
	public boolean openTeeveeRecoder(int flags) {
		Log.i(TAG, TAG + " openTeeveeRecoder");
		synchronized (mutex) {
			boolean succ = false;
			if (mPlayResource.isReserved()) {
				succ = mPlayResource.openTeeveeRecoder(flags);
				mPlayResource.setOnRecordStateListener(onRecordStateListener);
			}
			Log.d(TAG, "openTeeveeRecoder succ = " + succ);
			return succ;
		}
	}

	@Override
	public void closeTeeveeRecoder() {
		synchronized (mutex) {
			if (mPlayResource.isReserved()) {
				mPlayResource.closeTeeveeRecoder();
			}
		}
	}

	@Override
	public boolean setTeeveeRecoder(ParcelFileDescriptor pfd, long freq, int fflags, int pn,
			int pflags) {
		Log.d(TAG, "setTeeveeRecoder fd = " + pfd + ";freq = " + freq + ";fflags = " + fflags
				+ ";pn = " + pn + ";pflags = " + pflags);
		synchronized (mutex) {
			ChannelKey key = null;
			FrequencyInfo finfo = null;
			if (freq == -1) {
				key = ChannelKey.obten(selection.freq, pn);
				finfo = mLiveData.getFrequencyInfo(selection.freq);
			} else {
				key = ChannelKey.obten(freq, pn);
				finfo = mLiveData.getFrequencyInfo(freq);
			}
			ProgramInfo pi = mLiveData.getProgramInfo(key);
			FileOutputStream fos = null;
			Log.d(TAG, "setTeeveeRecoder 2 pi = " + pi);
			if (mPlayResource.isReserved()) {
				try {
					FileDescriptor fd = pfd.getFileDescriptor();
					fos = new FileOutputStream(fd);
					if (fos != null) {
						byte pat[] = new byte[16];
						int pns[] = { pn };
						int pids[] = { 0x81 };
						SectionBuilder.buildPATSection(0, 0, pns, pids, pat);
						int n = 0;
						int audioType = -1;
						int videoType = -1;
						if (pi.getVideoPID() != ProgramInfo.PID_UNDEFINED) {
							n++;
							videoType = getStreamType(pi.getVideoStreamType());
						}
						if (pi.getAudioPID() != ProgramInfo.PID_UNDEFINED) {
							n++;
							audioType = getStreamType(pi.getAudioStreamType());
						}
						int stypes[] = new int[n];
						int spids[] = new int[n];
						if (videoType != -1) {
							stypes[0] = videoType;
							spids[0] = pi.getVideoPID();
						}
						if (audioType != -1) {
							stypes[1] = audioType;
							spids[1] = pi.getAudioPID();
						}
						byte pmt[] = new byte[16 + 5 * n];
						SectionBuilder.buildPMTSection(pn, 0, pi.getPcrPID(), null, null, stypes,
								spids, pmt);
						byte patPackage[] = new byte[188];
						byte pmtPackage[] = new byte[188];
						SectionBuilder.buildTSSection(0, pat, pat.length, 0, patPackage);
						SectionBuilder.buildTSSection(0x81, pmt, pmt.length, 0, pmtPackage);
						fos.write(patPackage);
						fos.write(pmtPackage);
						fos.flush();
						Log.d(TAG, "setTeeveeRecoder 2 fos.getFD()" + fos.getFD());
						return mPlayResource.setTeeveeRecoder(fos.getFD(), 376, -1, finfo, fflags,
								pi, pflags);
					}
				} catch (IOException e) {
					Log.e(TAG, "setTeeveeRecoder err = " + e.getMessage());
				} finally {
					try {
						if (fos != null) {
							fos.close();
						}
						if (pfd != null) {
							pfd.close();
						}
					} catch (IOException e) {
						Log.d(TAG, "setTeeveeRecoder err2 = " + e.getMessage());
					}
				}
			}
			return false;
		}
	}

	@Override
	public final void syncSignalStatus() {
		IPanelLog.d(TAG, "before syncSignalStatus");
		synchronized (mutex) {
			if (mPlayResource.isReserved()) {
				SignalStatus ss = mPlayResource.getSelector().getSignalStatus();
				if (ss != null) {
					notifyJson(CB.__ID_onSyncSignalStatus, ss.toString());
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
			mPlayResource.getPlayer().selectProgram(fi, flags);
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
		int code = err == null ? 0 : L10n.getErrorCode(err, 100000);
		mWidgetHandle.notifySwitchingEnd(code, err);
	}

	@Override
	public void select(long freq, int fflags, int pn, int pflags) {
		FrequencyInfo fi = null;
		ProgramInfo pi = null;
		if (freq == -1) {
			key = mLiveData.getChannelKeyByPn(pn);
		} else {
			key = ChannelKey.obten(freq, pn);
		}
		IPanelLog.d(TAG, "before select 3 freq = " + freq + ";pn = " + pn);
		synchronized (mutex) {
			@SuppressWarnings("unused")
			String err = null;
			boolean done = false;
			try {
				if ((freq == 0 && pn == 0) || key == null) {
					mWidgetHandle.showMessage();
					return;
				}
				//检查节目是否枷锁
				Log.d( TAG, "check_onChannelLocked_out_CWJ 1" );
				boolean locked = mWardshipIndicater.chack(key);
				if (locked && !unlockset.contains(key) ) {
					stop(0);
					mWidgetHandle.clearWidgetMessage(fflags);
					JSONStringer str = new JSONStringer();
					str.object();
					str.key("freq").value(key.getFrequency());
					str.key("pn").value(key.getProgram());
					str.endObject();
					Log.d( TAG, "check_onChannelLocked_in_CWJ" );
					notifyJson(CB.__ID_onChannelLocked, str.toString());
					return;
				}
				unlockset.clear();
				mWidgetHandle.clearWidgetMessage(fflags);
				fi = mLiveData.getFrequencyInfo(key.getFrequency());
				pi = mLiveData.getProgramInfo(key);
				IPanelLog.d(TAG, "before select 1 fi = " + fi);
				if (fi != null) {
					onSelect(fi, fflags, pi, pflags);
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
	public void select(String furi, int fflags, String puri, int pflags) {
		try {
			IPanelLog.d(TAG, "before select with furi 44");
			synchronized (mutex) {
				FrequencyInfo fi = null;
				ProgramInfo pi = null;
				notifyJson(CB.__ID_onDescramError, true + "");
				if (furi != null && !"null".equals(furi)) {
					fi = FrequencyInfo.fromString(furi);
				}
				if (puri != null && !"null".equals(puri)) {
					pi = ProgramInfo.fromString(puri);
					if (pi.getProgramNumber() <= 0) {
						int pn = mLiveData.getProgramNum(fi.getFrequency(), pi.getAudioPID());
						Log.d(TAG, "select pn = " + pn);
						if (pn > 0) {
							pi = mLiveData.getProgramInfo(new ChannelKey(fi.getFrequency(), pn));
							Log.d(TAG, "select pi = " + pi);
						}
					} else {
						ProgramInfo info = mLiveData.getProgramInfo(new ChannelKey(fi
								.getFrequency(), pi.getProgramNumber()));
						if (info != null) {
							pi = info;
						}
					}
				}
				if (!selection.isEquals(selection.furi, fi.toString())) {
					mWidgetHandle.clearWidgetMessage();
				} else {
					IPanelLog.d(TAG, "reselect same furi-------");
				}
				onSelect(fi, fflags, pi, pflags);
				monitorProgramStream(fi.getFrequency(), pi.getProgramNumber());
				selection.pnset(pi.getProgramNumber(), pflags);
			}
			IPanelLog.d(TAG, "end select with furi");
		} catch (Exception e) {
			String err = "error:" + e.getMessage();
			notifyError(err);
			notifyWidgetSwitchEnd(err);
		}
	}

	@Override
	public long getPlayTime() {
		return -1;
	}

	@Override
	public void captureVideoFrame(int id) {
		synchronized (mutex) {
			mPlayResource.getPlayer().captureVideoFrame(id);
		}
	}

	// =====================
	protected Selection selection = new Selection();
	private boolean playerStopped = false;
	private boolean suspend = false;
	private float volumeSelect = 0.5f;

	// private String pendingSolveUri = null;

	private void onSelect(FrequencyInfo fi, int fflags, ProgramInfo pi, int pflags) {
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
		IPanelLog.d(TAG, "onSelect pi.getVideoPID() = " + pi.getVideoPID());
		mWidgetHandle.notifySwitchingStart(pi.getVideoPID() < 0);
		playAndDescramble(puri, fi, pi, pflags);
		IPanelLog.d(TAG, "onSelect 444");
		notifyWidgetSwitchEnd(null);
		// callback
	}

	// 给子类重写，以适应某些项目需要先启动解扰再播放。
	protected void playAndDescramble(String puri, FrequencyInfo fi, ProgramInfo pi, int pflags) {
		if (!onSelectProgram(puri, pflags)) {
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
		if (!onSelectProgram(puri, pflags)) {
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

	private boolean onSelectProgram(ProgramInfo pi, int flags) {
		ensurePlayerStarted();
		if (pi.getVideoPID() == ProgramInfo.PID_UNDEFINED) {
			flags = flags & TeeveePlayer.FLAG_VIDEO_FRAME_BLACK;
		}
		if (mPlayResource.getPlayer().selectProgram(pi, flags)) {
			selection.pset(pi.toString(), flags);
			onSetVolume(volumeSelect);
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

	private boolean onSelectProgram(long freq, String puri, int flags) {
		IPanelLog.d(TAG, "onSelectProgram freq = " + freq + ";freq = " + freq);
		ProgramInfo pi = ProgramInfo.fromString(puri);
		mLiveData.updateStreamPids(new ChannelKey(freq, pi.getProgramNumber()), pi);
		if (!onSelectProgram(pi, flags)) {
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
								+ selection.freq + ";pn = " + pn + ";selection.pn = " + selection.pn);
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
						Log.e(TAG, "channelUndefine e = "+ e);
					}
					
				}
			}
		}).start();
	}
	
	protected void onSelectSuc(StreamSelector s) {
		notifyJson(CB.__ID_onStreamResumed);
	}

	private void stopPlayer(int flag) {
		IPanelLog.d(TAG, "stopPlayer flag" + flag);
		synchronized (TAG) {
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
		synchronized (TAG) {
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

	protected void onSetVolume(float f){
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
				if(code ==0){
					jo.put("code", 1008);
					jo.put("err", err);
				}else{
					jo.put("code", code);
					jo.put("err", err);
				}
				notifyJson(CB.__ID_onDescramError, jo.toString());
				stopPlayer(TeeveePlayer.FLAG_VIDEO_FRAME_BLACK);
			} else if (err.equals("PROGRAM_LOCKED")) {
				IPanelLog.i(TAG, "err.equals(\"PROGRAM_LOCKED\")");
				stopPlayer(TeeveePlayer.FLAG_VIDEO_FRAME_BLACK);
			} else {
//				jo.put("code", 1008);
//				notifyJson(CB.__ID_onDescramError, jo.toString());
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
			if (furi != null && puri != null)
				select(furi, StreamSelector.SELECT_FLAG_FORCE, puri, pflags);
		}

		void reselectProgram() {
			IPanelLog.d(TAG, "Selection reselectProgram puri = " + puri);
			if (puri != null) {
				onSelectProgram(puri, pflags);
			}
		}

		boolean preselect(int program, String npuri) {
			IPanelLog.d(TAG, "preselect program = " + program + "; npuri = " + npuri);
			IPanelLog.d(TAG, "preselect pn = " + pn);
			if (pn == program) {
				return onSelectProgram(freq, npuri, pflags);
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
			IPanelLog.d(TAG, "onSelectFailed selection.furi = " + selection.furi);
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
				notifyJson(CB.__ID_onStreamLost);
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

	OnRecordStateListener onRecordStateListener = new OnRecordStateListener() {

		@Override
		public void onRecordStart(int program_number) {
			IPanelLog.d(TAG, "onRecordStart program_number = " + program_number);
			notifyJson(CB.__ID_onRecordStart, program_number + "");
		}

		@Override
		public void onRecordError(int program_number, String msg) {
			IPanelLog.d(TAG, "onRecordError program_number = " + program_number + ";msg = " + msg);
			try {
				JSONStringer str = new JSONStringer();
				str.object();
				str.key("program_number").value(program_number);
				str.key("msg").value(msg);
				str.endObject();
				notifyJson(CB.__ID_onRecordError, str.toString());
			} catch (JSONException e) {
				throw new RuntimeException(e.getMessage());
			}
		}

		@Override
		public void onRecordEnd(int program_number) {
			IPanelLog.d(TAG, "onRecordEnd program_number = " + program_number);
			notifyJson(CB.__ID_onRecordEnd, program_number + "");
		}
	};

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

	ProgramDescramberCallback descramblerCallback = new ProgramDescramberCallback() {
		@Override
		public void onCaModuleDispatched(int moduleId) {
			onCaModule(moduleId);
			notifyJson(CB.__ID_onCaModuleDispatched, moduleId + "");
		}

		@Override
		public void onDescramblingState(int code, String err) {
			IPanelLog.d(TAG, "onDescramblingState err code = " + code + "err = " + err);
			if(code == 51){
				mWidgetHandle.notifyDescramblingState(code, err, key);
			}else{
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
				if(msg == null){
					jo.put("code", 0);
					jo.put("err", "null");
				}else{
					jo.put("code", 452);
					jo.put("err", msg);
				}
				notifyJson(CB.__ID_onDescramError, jo.toString());
			} catch (Exception e) {
				Log.e(TAG, "onCaCardState err = "+ e.getMessage());
			}
			if(msg == null){
				if (playerStopped) {
					reselectProgram();
				}
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
		case __ID_select: {
			JSONObject o = (JSONObject) new JSONTokener(json).nextValue();
			select(o.getLong("freq"), o.getInt("fflags"), o.getInt("program"), o.getInt("pflags"));
			break;
		}
		case __ID_select_2: {
			JSONObject o = (JSONObject) new JSONTokener(json).nextValue();
			select(o.getString("furi"), o.getInt("fflags"), o.getString("puri"), o.getInt("pflags"));
			break;
		}
		case __ID_checkPassword: {
			checkPassword(json);
			break;
		}
		case __ID_pipOpenPlayers: {
			JSONObject o = (JSONObject) new JSONTokener(json).nextValue();
			pipOpenPlayers(o.getInt("size"), o.getInt("flags"));
			break;
		}
		case __ID_pipClosePlayers: {
			pipClosePlayers();
			break;
		}
		case __ID_pipSetFreqency: {
			JSONObject o = (JSONObject) new JSONTokener(json).nextValue();
			pipSetFreqency(o.getInt("index"), o.getLong("freq"), o.getInt("flags"));
			break;
		}
		case __ID_pipSetProgram: {
			JSONObject o = (JSONObject) new JSONTokener(json).nextValue();
			pipSetProgram(o.getInt("index"), o.getInt("prog"), o.getInt("x"), o.getInt("y"),
					o.getInt("w"), o.getInt("h"), o.getInt("flags"));
			break;
		}
		case __ID_pipLoadAnimation: {
			Parcelable obj = b.getParcelable("pfd");
			Log.i(TAG, "------>navigaton transmit __ID_pipLoadAnimation obj = " + obj);
			ParcelFileDescriptor pfd = (ParcelFileDescriptor) obj;
			try {
				ParcelFileDescriptor dpfd = pfd.dup();
				pfd.close();
				Log.i(TAG, "------>navigaton transmit __ID_pipLoadAnimation dpfd = " + dpfd);
				pipLoadAnimation(dpfd);
			} catch (IOException e) {
				e.printStackTrace();
			}
			break;
		}
		case __ID_pipActAnimation: {
			JSONObject o = (JSONObject) new JSONTokener(json).nextValue();
			pipActAnimation(o.getInt("action"), o.getInt("p1"), o.getInt("p2"), o.getInt("flags"));
			break;
		}
		case __ID_openTeeveeRecoder: {
			Log.i(TAG, "__ID_openTeeveeRecoder");
			return openTeeveeRecoder(Integer.parseInt(json)) + "";
		}
		case __ID_setTeeveeRecoder: {
			Log.i(TAG, "__ID_setTeeveeRecoder 1");
			Parcelable obj = b.getParcelable("pfd");
			Log.i(TAG, "------>navigaton transmit __ID_setTeeveeRecoder obj = " + obj);
			ParcelFileDescriptor pfd = (ParcelFileDescriptor) obj;
			JSONObject o = (JSONObject) new JSONTokener(json).nextValue();
			setTeeveeRecoder(pfd, o.getLong("freq"), o.getInt("fflags"), o.getInt("pn"),
					o.getInt("pflags"));
		}
			break;
		case __ID_closeTeeveeRecoder: {
			Log.i(TAG, "__ID_closeTeeveeRecoder");
			closeTeeveeRecoder();
			break;
		}
		case __ID_solveProblem:
			solveProblem();
			break;
		case __ID_enterCaApp:
			enterCaApp(json);
			break;
		case __ID_observeProgramGuide: {
			JSONObject o = (JSONObject) new JSONTokener(json).nextValue();
			ChannelKey key = ChannelKey.obten(o.getLong("freq"), o.getInt("program_number"));
			observeProgramGuide(key, o.getLong("focus"));
			break;
		}
		case __ID_syncSignalStatus:
			syncSignalStatus();
			break;
		case __ID_captureVideoFrame:
			captureVideoFrame(Integer.parseInt(json));
			break;
		default:
			Log.i(TAG, "default......");
			return super.onTransmit(code, json, p, b);
		}
		return null;
	}

	public void clearWidgetMessage() {
		mWidgetHandle.clearWidgetMessage();
		;
	}

	@Override
	public void clearCache(int flags) {
		// TODO Auto-generated method stub

	}
}
