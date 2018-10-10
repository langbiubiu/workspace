package ipaneltv.toolkit.mediaservice;

import ipaneltv.toolkit.IPanelLog;
import ipaneltv.toolkit.JsonParcelable;
import ipaneltv.toolkit.db.DatabaseObjectification.ChannelKey;
import ipaneltv.toolkit.media.MediaSessionInterface.HomedProgramPlayerInterface2;
import ipaneltv.toolkit.mediaservice.components.CaDescramblingManager.ProgramDescramberCallback;
import ipaneltv.toolkit.mediaservice.components.CaDescramblingManager.ProgramDescrambler;
import ipaneltv.toolkit.mediaservice.components.L10n;
import ipaneltv.toolkit.mediaservice.components.LiveDataManager;
import ipaneltv.toolkit.mediaservice.components.LiveDataManager.LiveDataListener;
import ipaneltv.toolkit.mediaservice.components.PlayResourceScheduler;
import ipaneltv.toolkit.mediaservice.components.PlayResourceScheduler.ResourcesState;
import ipaneltv.toolkit.mediaservice.components.PlayWidgetManager.PlayWidgetControl;
import ipaneltv.toolkit.mediaservice.components.PlayWidgetManager.PlayWidgetControlCallback;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.Arrays;
import java.util.List;

import org.json.JSONException;
import org.json.JSONObject;
import org.json.JSONTokener;

import android.graphics.Rect;
import android.media.TeeveePlayer;
import android.media.TeeveePlayer.PlayStateListener;
import android.media.TeeveePlayer.ProgramStateListener;
import android.net.Uri;
import android.net.telecast.FrequencyInfo;
import android.net.telecast.ProgramInfo;
import android.net.telecast.ProgramInfo.StreamTypeNameEnum;
import android.net.telecast.SignalStatus;
import android.net.telecast.StreamSelector;
import android.net.telecast.StreamSelector.SelectionStateListener;
import android.net.telecast.ca.CAManager;
import android.os.Bundle;
import android.os.FileObserver;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.ParcelFileDescriptor;
import android.os.Parcelable;
import android.util.DisplayMetrics;

public class HomedPlayerContext2<T extends MediaPlaySessionService> extends
		TeeveePlayerBaseContext<T> implements HomedProgramPlayerInterface2 {
	abstract class CB implements HomedProgramPlayerInterface2.Callback {
	};

	private static final String TAG = HomedPlayerContext2.class.getSimpleName();
	String info = "program://1?audio_stream_pid=1002&audio_stream_type=audio_aac&video_stream_pid=1001&video_stream_type=video_h264&ca_required=false";
	protected final Object mutex = new Object();
	protected ResourcesState mPlayResource;

	protected PlayWidgetControl mWidgetHandle;
	protected ProgramDescrambler mDescrambler;
	private boolean contextReady = false;
	protected LiveDataManager mLiveData;
	private HandlerThread procThread = new HandlerThread("pipthread");
	Handler procHandler;
	CAManager caManager;
	Rect rect = new Rect();
	MyFileObserver mObserver;
	boolean cardVerified = false;
	private boolean network = false;
	Selection selection = new Selection();
	private boolean playerStopped = false;
	private boolean suspend = false;
	private float volumeSelect = 0.5f;
	/**
	 * 0 dvb 1 ip 2 
	 */
	private int playState = -1;
	/**
	 * dvb"u
	 */
	private static final int dvbPlay = 0;
	/**
	 * ip"u
	 */
	private static final int ipPlay = 1;
	/**
	 * "u
	 */
	private static final int shiftPlay = 2;

	public HomedPlayerContext2(T service) {
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
		procThread.start();
		procHandler = new Handler(procThread.getLooper());
		mPlayResource = app.getResourceScheduler().createLivePlayState(false, pri, soft, 2,
				StreamSelector.CREATE_FLAG_DEFAULT, TeeveePlayer.CREATE_FLAG_BASE_FOR_PIP);
		IPanelLog.d(TAG, "onCreate ret.selectorHandle = " + mPlayResource.getPlayer()
				+ ";ret.playerHandle = " + mPlayResource.getSelector());
		mPlayResource.getSelector().setSelectionStateListener(selectionStateListener);
		mPlayResource.getPlayer().setListener(null, programStateListener, playerptslistener);
		mPlayResource.setHomedPlayerListener(homedplayStateListener);
		mWidgetHandle = app.getPlayWidgetManager().createControl(widgetCallback);
		mDescrambler = app.getCaDescramblingManager().createDescrambler(descramblerCallback);
		mLiveData = app.getLiveDataManager();
		IPanelLog.d(TAG, "mLiveData = " + mLiveData + ";mLiveDataListener = " + mLiveDataListener);
		mLiveData.addLiveDataListener(mLiveDataListener);
		DisplayMetrics aDisplayMetrics = app.getResources().getDisplayMetrics();
		if (aDisplayMetrics != null) {
			rect.bottom = aDisplayMetrics.heightPixels;
			rect.right = aDisplayMetrics.widthPixels;
		}
		rect.left = 0;
		rect.top = 0;

		caManager = CAManager.createInstance(app);
		caManager.setCACardStateListener(cardStateListener);
		caManager.queryCurrentCAState();
		mObserver = new MyFileObserver("mnt/network.txt");

		mObserver.startWatching();
		int i = getNetworkState();
		IPanelLog.d(TAG, "onCreate2 i = " + i);
		if (i == 48) {
			network = false;
			IPanelLog.d(TAG, "onCreate 2222");
		} else {
			network = true;
			IPanelLog.d(TAG, "onCreate 3333");
		}
	}

	public class MyFileObserver extends FileObserver {

		/**
		 * path lß›l
		 */

		public MyFileObserver(String path) {
			super(path);

		}

		@Override
		public void onEvent(int event, String path) {
			IPanelLog.d(TAG, "onEvent event = " + event + ";path = " + path + ";network = "
					+ network);
			switch (event) {
			case android.os.FileObserver.CLOSE_WRITE:
				int i = getNetworkState();
				// l

				/**
				 * 
				 * 
				 */
				if (i == 48 && network) {
					network = false;
					if (mPlayResource.isReserved()) {
						postProcDvb(new DvbRunable(selection.getCount()) {

							@Override
							public void run() {
								synchronized (mutex) {
									IPanelLog.d(TAG, "postProcDvb onEvent count = " + count
											+ "; selection.getCount() = " + selection.getCount()
											+ ";playState = " + playState);
									if (count == selection.getCount() && playState != shiftPlay) {
										selectDvb(selection.freq, selection.fflags, selection.pn,
												selection.pflags, true, count);
									}
								}
							}
						}, false);
					}
				} else if (i == 49 && !network) {
					network = true;
				}

				break;
			}

		}

	}

	CAManager.CACardStateListener cardStateListener = new CAManager.CACardStateListener() {

		@Override
		public void onCardPresent(int readerIndex) {
			IPanelLog.d(TAG, "onCardPresent readerIndex = " + readerIndex);
		}

		@Override
		public void onCardAbsent(int readerIndex) {
			IPanelLog.d(TAG, "onCardAbsent readerIndex 2 = " + readerIndex);
			cardVerified = false;
		}

		@Override
		public void onCardMuted(int readerIndex) {
			IPanelLog.d(TAG, "onCardMuted readerIndex = " + readerIndex);
			cardVerified = false;
		}

		@Override
		public void onCardReady(int readerIndex) {
			IPanelLog.d(TAG, "onCardReady readerIndex = " + readerIndex);

		}

		@Override
		public void onCardVerified(int readerIndex, int moduleID) {
			IPanelLog.d(TAG, "onCardVerified 2 moduleID = " + moduleID);
			cardVerified = true;
		}

	};

	public int getNetworkState() {
		byte[] b = new byte[1];
		FileInputStream fis = null;
		try {
			fis = new FileInputStream("/mnt/network.txt");
			try {
				fis.read(b);
				IPanelLog.d(TAG, "onEvent b[0] = " + b[0]);
			} catch (IOException e) {
				e.printStackTrace();
			}
		} catch (FileNotFoundException e) {
			IPanelLog.e(TAG, "getNetworkState e = " + e.getMessage());
		} finally {
			if (fis != null) {
				try {
					fis.close();
					fis = null;
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		}
		return b[0];
	}

	final void postProcDvb(Runnable r, boolean clear) {
		if (clear)
			procHandler.removeCallbacksAndMessages(null);
		procHandler.post(r);
	}

	final void postProcDvbFront(Runnable r, boolean clear) {
		if (clear)
			procHandler.removeCallbacksAndMessages(null);
		procHandler.postAtFrontOfQueue(r);
	}

	final void postDelayedDvb(Runnable r, long l, boolean clear) {
		if (clear)
			procHandler.removeCallbacksAndMessages(null);
		procHandler.postDelayed(r, l);
	}

	/**
	 * 
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
		procThread.getLooper().quit();
		if (mObserver != null) {
			mObserver.stopWatching();
			mObserver = null;
		}
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
		if (clearState && isRelease()) {
			mPlayResource.destroy();
		} else {
			mPlayResource.loosen(clearState);
		}
		mPlayResource.closeHomedPipPlayer(clearState);
		mDescrambler.loosen(clearState);
		mWidgetHandle.loosen(clearState);
	}

	/**
	 * 
	 * 
	 * @return
	 */
	@Override
	public boolean reserve() {// 
		IPanelLog.d(TAG, "before reserve 11 cardVerified = " + cardVerified);
		synchronized (mutex) {
			IPanelLog.d(TAG, "reserve in contextReady = " + contextReady);
			if (contextReady ? false : reserveAllSafe()) {
				contextReady = true;
				mPlayResource.getPlayer().start();
				mPlayResource.getPlayer().setDisplay(0, 0, 1920, 1080);
			}
			IPanelLog.d(TAG, "reserve end");
		}
		IPanelLog.d(TAG, "end reserve");
		return contextReady;
	}

	/**
	 * sL,
	 */
	@Override
	public void loosen(boolean clearState) {
		IPanelLog.d(TAG, "before loosen");
		synchronized (mutex) {
			if (contextReady) {
				IPanelLog.d(TAG, "onLoosen 22(clearState=" + clearState + ")");
				contextReady = false;
				mWidgetHandle.clearWidgetMessage();
				if (!clearState) {
					selectDvb(selection.freq, selection.fflags | 0x10000, selection.pn, 0, true, -1);
				}
				loosenAll(clearState);
				selection.clearUri();
			}
		}
		IPanelLog.d(TAG, "end loosen");
	}

	@Override
	public void stop(final int flag) {
		IPanelLog.d(TAG, "stop flag = " + flag + ";suspend = " + suspend);
		synchronized (mutex) {
			if (!suspend) {
				if (mPlayResource.isReserved()) {
					suspend = true;
					selection.clearUri();
					stopPlayer(flag);
					if (mPlayResource.isHomedPipPlayerOpened()) {
						mPlayResource.homedStop(flag);
					}
				}
			}
		}
		IPanelLog.d(TAG, "end stop");
	}

	@Override
	public void pause() {
		IPanelLog.d(TAG, "before pause");
		synchronized (mutex) {
			if (mPlayResource.isHomedPipPlayerOpened()) {
				mPlayResource.homedPause();
			}
		}
		IPanelLog.d(TAG, "end pause");
	}

	@Override
	public void resume() {
		IPanelLog.d(TAG, "before resume");
		synchronized (mutex) {
			if (mPlayResource.isHomedPipPlayerOpened()) {
				mPlayResource.homedResume();
			}
		}
		IPanelLog.d(TAG, "end resume");
	}

	@Override
	public final void setVolume(float v) {
		IPanelLog.d(TAG, "before setVolume v = " + v);
		synchronized (mutex) {
			if (mPlayResource.isReserved() && volumeSelect != v) {
				volumeSelect = v < 0f ? 0f : v > 1f ? 1f : v;
				IPanelLog.d(TAG, "setVolume v= " + v + ";playState = " + playState);
				if (playState == ipPlay || playState == shiftPlay) {
					mPlayResource.homedSetVolume(v);
				} else {
					setDvbVolume(v);
				}
			}
		}
		IPanelLog.d(TAG, "end setVolume");
	}

	private final void setDvbVolume(float v) {
		IPanelLog.d(TAG, "before setVolume2 v = " + v);
		synchronized (mutex) {
			if (mPlayResource.isReserved()) {
				mPlayResource.getPlayer().setVolume(v);
			}
		}
		IPanelLog.d(TAG, "end setVolume2");
	}

	@Override
	public final void setDisplay(int x, int y, int w, int h) {
		IPanelLog.d(TAG, "before setDisplay 11 playState = " + playState);
		synchronized (mutex) {
			if (mPlayResource.isReserved()) {
				rect.left = x;
				rect.top = y;
				rect.right = w;
				rect.bottom = h;
				if (playState == ipPlay || playState == shiftPlay) {
					mPlayResource.homedSetDisplay(x, y, w, h);
				} else {
					setDvbDisplay(x, y, w, h);
				}
			}
		}
		IPanelLog.d(TAG, "end setDisplay");
	}

	private final void setDvbDisplay(int x, int y, int w, int h) {
		IPanelLog.d(TAG, "before setDvbDisplay 11");
		synchronized (mutex) {
			if (mPlayResource.isReserved()) {
				mPlayResource.getPlayer().setDisplay(x, y, w, h);
			}
		}
		IPanelLog.d(TAG, "end setDvbDisplay");
	}

	@Override
	public final void syncSignalStatus() {
		IPanelLog.d(TAG, "before syncSignalStatus");
		synchronized (mutex) {
			if (mPlayResource.isReserved()) {
				SignalStatus ss = mPlayResource.getSelector().getSignalStatus();
				notifyJson(CB.__ID_onSyncSignalStatus, ss.toString());
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
		IPanelLog.d(TAG, "end ceckTeeveeWidget");
	}

	@Override
	public void setProgramFlags(int flags) {
		IPanelLog.d(TAG, "before setProgramFlags");
		synchronized (mutex) {
			if (suspend) {
				IPanelLog.w(TAG, "is suspend, start first!");
				return;
			}
			String uri = selection.puri;
			if (uri != null) {
				ProgramInfo fi = ProgramInfo.fromString(uri);
				mPlayResource.getPlayer().selectProgram(fi, flags | selection.pflags);
			}
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
	public void observeProgramGuide(final ChannelKey ch, final long focusTime) {
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
	public void select(final long vfreq, final ParcelFileDescriptor pfd, final long freq,
			final int fflags, final int pn, final int pflags, final int delay) {
		IPanelLog.d(TAG, "before select 4 vfreq = " + vfreq + "pfd = " + pfd + " freq = " + freq
				+ "fflags = " + fflags + ";pn = " + pn + ";network = " + network
				+ ";cardVerified = " + cardVerified);
		synchronized (mutex) {
			try {
				selection.newCount();
				selection.fset(null, freq, fflags);
				selection.pset(null, pn, pflags);
				if (vfreq != 1 && pfd != null && network && cardVerified
						&& mPlayResource.openHomedPipPlayer()) {
					playState = ipPlay;
					ipPlay(vfreq, pfd, fflags, 1 | 0x20000);
					mPlayResource.getPlayer().setVolume(0);
					mPlayResource.homedSetDisplay(rect.left, rect.top, rect.right, rect.bottom);
					mPlayResource.homedSetVolume(volumeSelect);
					// TODO dvb
					postDelayedDvb(new DvbRunable(selection.getCount()) {

						@Override
						public void run() {
							synchronized (mutex) {
								IPanelLog.d(TAG, "postDelayedDvb selectDvb count = " + count
										+ ";selection.getCount() = " + selection.getCount());
								if (count == selection.getCount()) {
									selectDvb(freq, fflags, pn, pflags, false, count);
								}
							}
						}
					}, delay, true);
				} else {
					playState = dvbPlay;
					// ipdvb
					selectDvb(freq, fflags, pn, pflags, true, -1);
				}
			} catch (Exception e) {
				IPanelLog.e(TAG, "select base e = " + e.toString());
			} finally {
				notifyJson(CB.__ID_onResponseSelect, true + "");
			}
		}
	}

	public void selectDvb(long freq, int fflags, int pn, int pflags, boolean force, int count) {
		IPanelLog.d(TAG, "before selectDvb freq = " + freq + ";pn = " + pn + ";force = " + force);
		synchronized (mutex) {
			boolean done = false;
			boolean notFound = false;
			try {
				FrequencyInfo fi = null;
				ProgramInfo pi = null;
				if (freq == 0 && pn == 0) {
					return;
				}
				ChannelKey key = ChannelKey.obten(freq, pn);
				fi = mLiveData.getFrequencyInfo(freq);
				pi = mLiveData.getProgramInfo(key);
				if (fi != null) {
					onSelect(fi, fflags, pi, pflags);
					monitorProgramStream(freq, pn);
					if (force) {
						playState = dvbPlay;
						int flags = pi.getVideoPID() > 0 ? TeeveePlayer.FLAG_VIDEO_FRAME_FREEZE
								: TeeveePlayer.FLAG_VIDEO_FRAME_BLACK;
						showDvb(freq, pn, flags);
					} else {
						postDelayedDvb(new DvbRunable(count) {

							@Override
							public void run() {
								IPanelLog.d(TAG, "postDelayedDvb showDvb count = " + count
										+ ";selection.getCount() = " + selection.getCount());
								synchronized (mutex) {
									if (count == selection.getCount()) {
										playState = dvbPlay;
										ChannelKey key = ChannelKey.obten(selection.freq,
												selection.pn);
										ProgramInfo pi = mLiveData.getProgramInfo(key);
										int flags = pi.getVideoPID() > 0 ? TeeveePlayer.FLAG_VIDEO_FRAME_FREEZE
												: TeeveePlayer.FLAG_VIDEO_FRAME_BLACK;
										showDvb(selection.freq, selection.pn, flags);
									}
								}
							}
						}, 3000, false);
					}
					done = true;
				} else {
					notFound = true;
				}
			} catch (Exception e) {
				e.printStackTrace();
			} finally {
				if (!done) {
					if (mPlayResource.isReserved()) {
						IPanelLog.d(TAG, "end selectDvb no program");
						stopPlayer(0);
						mPlayResource.homedStop(1);
					}
				}
				if (notFound) {
					if (freq != 0 && pn != 0) {
						mWidgetHandle.notifyDecodeState(L10n.SELECT_ERR_433);
					}
				}
			}
		}
		IPanelLog.d(TAG, "end selectDvb");
	}

	@Override
	public void start(final String puri, final int pflags) {
		IPanelLog.d(TAG, "start puri = " + puri + ";pflags = " + pflags + ";playState = "
				+ playState);
		synchronized (mutex) {
			if (!puri.startsWith("playerror") && playState != dvbPlay) {
				try {
					ProgramInfo pinfo = getProgramUri(puri);
					ProgramInfo defauletInfo = ProgramInfo.fromString(info);
					IPanelLog.d(TAG, "start cNumber pinfo = " + pinfo);
					if (defauletInfo.getAudioPID() != pinfo.getAudioPID()
							|| !defauletInfo.getAudioStreamType()
									.equals(pinfo.getAudioStreamType())
							|| defauletInfo.getVideoPID() != pinfo.getVideoPID()
							|| !defauletInfo.getVideoStreamType()
									.equals(pinfo.getVideoStreamType())) {
						int flag = pflags | 1;
						if (playState != shiftPlay) {
							flag = flag | 0x20000;
						}
						if (mPlayResource.isReserved()) {
							if (!mPlayResource.homedstart(null, 0, pinfo, flag)) {
								return;
							}
							mPlayResource.homedSetVolume(volumeSelect);
							info = pinfo.toString();
						}
						suspend = false;
					}
				} catch (Exception e) {
					IPanelLog.e(TAG, "start e = " + e.getMessage());
				}
			} else if (puri.startsWith("playerror") && playState != shiftPlay) {
				selectDvb(selection.freq, selection.fflags, selection.pn, selection.pflags, true,
						-1);
			}

		}
	}

	public void startShift(final long vfreq, ParcelFileDescriptor pfd, final int fflags) {
		IPanelLog.d(TAG, "startShift pfd = " + pfd + ";vfreq = " + vfreq);
		synchronized (mutex) {
			try {
				selection.newCount();
				playState = shiftPlay;
				IPanelLog.d(TAG, "startShift 3333 pfd = " + pfd);
				if (mPlayResource.openHomedPipPlayer()) {
					ipPlay(vfreq, pfd, fflags, 1);
					mWidgetHandle.clearWidgetMessage(fflags);
					mPlayResource.getPlayer().setVolume(0);
					mPlayResource.homedSetDisplay(rect.left, rect.top, rect.right, rect.bottom);
					mPlayResource.homedSetVolume(volumeSelect);
				}
			} catch (Exception e) {
				IPanelLog.e(TAG, "startShift e = " + e.toString());
			} finally {
				notifyJson(CB.__ID_homed_onResponseStart, true + "");
				IPanelLog.d(TAG, "startShift onProgramFoundPlay __ID_onResponseStart");
			}

		}
	}

	@Override
	public void redirect(final long vfreq, ParcelFileDescriptor pfd, final int flags) {
		IPanelLog.d(TAG, "redirect 22");
		synchronized (mutex) {
			final int fd = pfd.detachFd();
			boolean succ = false;
			try {
				ParcelFileDescriptor ppfd = ParcelFileDescriptor.adoptFd(fd);
				IPanelLog.d(TAG, "redirect 11");
				if (mPlayResource.isHomedPipPlayerOpened()) {
					ipPlay(vfreq, ppfd, flags, 1);
				}
			} catch (Exception e) {
				IPanelLog.e(TAG, "redirect e = " + e.toString());
			} finally {
				IPanelLog.d(TAG, "redirect  succ=" + succ);
			}
		}
	}

	protected void ipPlay(long vfreq, ParcelFileDescriptor ppfd, int fflags, int pflags) {
		ipPlayQuick(vfreq, ppfd, fflags, pflags);
	}

	protected void ipPlayQuick(long vfreq, ParcelFileDescriptor ppfd, int fflags, int pflags) {
		ProgramInfo pinfo = ProgramInfo.fromString(info);
		mPlayResource.homedPlay(vfreq, ppfd.getFileDescriptor(), fflags, pinfo, pflags);
	}

	/**
	 * LcablesiiÅ£ß’°¬ß’
	 * 
	 * @param freq
	 *            ZL
	 * @param pn
	 */
	protected void monitorProgramStream(long freq, int pn) {

	}

	public void select(final String furi, final int fflags, final String puri, final int pflags) {
		synchronized (mutex) {
			try {
				selection.newCount();
				playState = dvbPlay;
				IPanelLog.d(TAG, "before select with furi 4");
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
						IPanelLog.d(TAG, "select pn = " + pn);
						if (pn > 0) {
							pi.setProgramNumber(pn);
						}
					}
				}
				if (!selection.isEquals(selection.furi, fi.toString())) {
					mWidgetHandle.clearWidgetMessage();
				} else {
					IPanelLog.d(TAG, "reselect same furi-------");
				}
				onSelect(fi, fflags, pi, pflags);
				int flags = pi.getVideoPID() > 0 ? TeeveePlayer.FLAG_VIDEO_FRAME_FREEZE
						: TeeveePlayer.FLAG_VIDEO_FRAME_BLACK;
				showDvb(fi.getBufSize(), pi.getProgramNumber(), flags);
				IPanelLog.d(TAG, "end select with furi");
			} catch (Exception e) {
				String err = "error:" + e.getMessage();
				notifyError(err);
				notifyWidgetSwitchEnd(err);
			}
		}

	}

	@Override
	public long getPlayTime() {
		return -1;
	}

	@Override
	public void captureVideoFrame(final int id) {
		synchronized (mutex) {
			mPlayResource.getPlayer().captureVideoFrame(id);
		}
	}

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
		if (pi.getVideoPID() < 0) {
			pflags = pflags & 0xFFFE;
		}
		IPanelLog.d(TAG, "onSelect pi.getVideoPID() = " + pi.getVideoPID() + ";pflags = " + pflags);
		mWidgetHandle.notifySwitchingStart(pi.getVideoPID() < 0);
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
		boolean d = mDescrambler.start(key, pi.getAudioPID(), pi.getVideoPID());
		IPanelLog.d(TAG, "after mDescrambler.start()");
		if (!d) {
			notifyError("select descrambling failed");
			notifyWidgetSwitchEnd(L10n.SELECT_ERR_432);
			return;
		}
		IPanelLog.d(TAG, "onSelect 44455");
		notifyWidgetSwitchEnd(null);
		// callback
	}

	private boolean onSelectProgram(ProgramInfo pi, int flags) {
		ensurePlayerStarted();
		if (mPlayResource.getPlayer().selectProgram(pi, flags)) {
			selection.pset(pi.toString(), pi.getProgramNumber(), flags);
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
		IPanelLog.d(TAG, "reselectProgram 33");
		synchronized (mutex) {
			selection.reselect(true);
		}
	}

	protected void showDvb(long f, int pn, int flags) {
		synchronized (mutex) {
			try {
				setDvbDisplay(rect.left, rect.top, rect.right, rect.bottom);
				setDvbVolume(volumeSelect);
				mPlayResource.homedStop(flags);
				JSONObject object = new JSONObject();
				object.put("f", f);
				object.put("pn", pn);
				notifyJson(CB.__ID_homed_onIpStoped, object.toString());
			} catch (Exception e) {
				IPanelLog.e(TAG, "showDvb e = " + e.toString());
			}
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

	private void reselect(final boolean force) {
		stopPlayer(0);
		selection.reselect(force);
	}

	/**
	 * Å£ß’°¬ß’?
	 * 
	 * @param program_number
	 *            
	 * @param newuri
	 *            uLLuri
	 */
	protected void onProgramInfoChanged(int program_number, String newuri) {

	}

	/**
	 * LcablesiiÅ£ß’°¬ß’
	 * 
	 * @param uri
	 *            ZLfuri
	 * @param ts2
	 */
	protected void monitorProgramStream(String furi, String puri) {

	}

	private void stopPlayer(int flag) {
		IPanelLog.d(TAG, "stopPlayer flag" + flag);
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

	private boolean ensurePlayerStarted() {
		if (mPlayResource.isReserved()) {
			boolean b = true;
			if (playerStopped) {
				b = mPlayResource.getPlayer().start();
				if (b) {
					mPlayResource.getPlayer().setFreeze(false, 0);
					// setVolume(volumeSelect);
					playerStopped = false;
				}
			}
			return b;
		}
		return false;
	}

	protected String getCurrentFuri() {
		return selection.furi;
	}

	protected void descramblingState(final int code, final String err) {
		IPanelLog.d(TAG, "descramblingState 22 err code = " + code + "err = " + err);
		mWidgetHandle.notifyDescramblingState(code, err);
		if (err == null) {
			reselectProgram();
			notifyJson(CB.__ID_onDescramError, true + "");
		} else if (code == 452) {
			mPlayResource.homedStop(0);
			stopPlayer(TeeveePlayer.FLAG_VIDEO_FRAME_BLACK);
			notifyJson(CB.__ID_onDescramError, false + "");
		} else if (code != 821 && code != 822) {
			notifyJson(CB.__ID_onDescramError, false + "");
			mPlayResource.homedStop(0);
			stopPlayer(TeeveePlayer.FLAG_VIDEO_FRAME_BLACK);
		}
	}

	class Selection {
		public final String FSCHEME = "frequency://";
		private String furi = null, puri = null;
		private int fflags = 0, pflags = 0;
		private long freq = 0;
		private int pn = 0;
		private int count;

		void clearUri() {
			puri = furi = null;
			pflags = fflags = count = 0;

		}

		int getCount() {
			return count;
		}

		int newCount() {
			return ++count;
		}

		void fset(String uri, long freq, int flags) {
			furi = uri;
			fflags = flags;
			this.freq = freq;
		}

		void pset(String uri, int pn, int flags) {
			puri = uri;
			pflags = flags;
			this.pn = pn;
		}

		void reselect(final boolean force) {
			selectDvb(freq, fflags, pn, pflags, force, count);
		}

		boolean preselect(int program, String npuri) {
			IPanelLog.d(TAG, "preselect program = " + program + "; npuri = " + npuri + ";pn = "
					+ pn);
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
			IPanelLog.d(TAG, "onSelectFailed playState = " + playState);
			postProcDvb(new Runnable() {
				public void run() {
					synchronized (mutex) {
						if (playState != shiftPlay) {
							reselect(true);
							String err = L10n.TRANSPORT_ERR_401;
							mWidgetHandle.notifyTransportState(err);
							mPlayResource.homedStop(0);
							IPanelLog.d(TAG, "onSelectFailed 22");
							notifyError(err);
							IPanelLog.d(TAG, "onSelectFailed 33");
						}
					}
				}
			}, false);
			notifyJson(CB.__ID_onStreamLost);
		}

		@Override
		public void onSelectSuccess(StreamSelector s) {
			IPanelLog.d(TAG, "onSelectSuccess:" + s.getSelectUri());
			mWidgetHandle.notifyTransportState(null);
			IPanelLog.d(TAG, "onSelectSuccess end ");
		}

		@Override
		public void onSelectionLost(StreamSelector s) {
			IPanelLog.d(TAG, "onSelectionLost 33 playState = " + playState);
			postProcDvb(new Runnable() {
				public void run() {
					synchronized (mutex) {
						stopPlayer(0);
						if (playState != shiftPlay) {
							mPlayResource.homedStop(0);
							String err = L10n.TRANSPORT_ERR_402;
							mWidgetHandle.notifyTransportState(err);
						}
					}
				}
			}, false);

			notifyJson(CB.__ID_onStreamLost);
		}

		@Override
		public void onSelectionResumed(StreamSelector s) {
			IPanelLog.d(TAG, "onSelectionResumed 11");
			postProcDvb(new Runnable() {

				@Override
				public void run() {
					synchronized (mutex) {
						if (playState != shiftPlay) {
							selection.reselect(true);
						}
					}
				}
			}, false);
			mWidgetHandle.notifyTransportState(null);
			notifyJson(CB.__ID_onStreamResumed);
		}
	};

	PlayStateListener homedplayStateListener = new PlayStateListener() {

		@Override
		public void onSelectionStart(TeeveePlayer player, int program_number) {/*-ignored ,ResourceScheduler done */
			IPanelLog.d(TAG, "hhh onSelectionStart");
		}

		@Override
		public void onPlayProcessing(int program_number) {
			IPanelLog.d(TAG, "onPlayProcessing: hhh program_number = " + program_number);
		}

		@Override
		public void onPlaySuspending(int program_number) {
		}

		@Override
		public void onPlayError(int program_number, String msg) {
			IPanelLog.d(TAG, "onPlayError hhh :" + msg);

		}
	};

	PlayerPTSListener playerptslistener = new PlayerPTSListener();

	class PlayerPTSListener implements PlayResourceScheduler.PlayerProcessPTSListener {

		@Override
		public void onPlayerPTSChange(final int program_number, long process_pts_time, int state) {

		}

		@Override
		public void onPlayProcess(int program_number, long process_pts_time) {
			try {
				IPanelLog.d(TAG, "onPlayProcessing program_number = " + program_number);
				// JSONStringer str = new JSONStringer();
				// str.object();
				// str.key("pn").value(program_number);
				// str.key("pts_time").value(process_pts_time);
				// str.endObject();
				// notifyJson(CB.__ID_onPlayProcessing, str.toString());
			} catch (Exception e) {
				IPanelLog.e(TAG, "onPlayProcessing failed e=" + e);
				e.printStackTrace();
			}
		}

		@Override
		public void onPlaySuspend(int program_number) {
			IPanelLog.d(TAG, "onPlaySuspending program_number=" + program_number);
			// try {
			// notifyJson(CB.__ID_onPlaySuspending, program_number + "");
			// } catch (Exception e) {
			// Log.e(TAG, "onPlaySuspending failed e=" + e);
			// e.printStackTrace();
			// }
			//
			// notifyJson(CB.__ID_onPlaySuspending, program_number + "");
		}
	}

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
		public void onDescramblingState(final int code, final String err) {
			IPanelLog.d(TAG, "onDescramblingState err code = " + code + "err = " + err);
			IPanelLog.d(TAG, "onDescramblingState selection.freq = " + selection.freq
					+ "selection.pn = " + selection.pn);
			postProcDvb(new Runnable() {

				@Override
				public void run() {
					synchronized (mutex) {
						IPanelLog.d(TAG, "postProcDvb onDescramblingState playState = " + playState);
						if (playState != shiftPlay) {
							descramblingState(code, err);
						}
					}
				}
			}, false);
		}

		@Override
		public void onCaCardState(int code, final String msg) {
			IPanelLog.d(TAG, "onCaCardState msg = " + msg + ";playState = " + playState);
			postProcDvb(new Runnable() {
				public void run() {
					synchronized (mutex) {
						if (playState != shiftPlay) {
							if (msg != null) {
								mPlayResource.homedStop(0);
								stopPlayer(0);
							} else {
								selection.reselect(true);
							}
						}
					}
				}
			}, false);
			mWidgetHandle.notifySmartcardState(code, msg);
		}
	};

	protected void onCaModule(int moduleId) {

	}

	@Override
	public String onTransmit(int code, String json, JsonParcelable p, Bundle b)
			throws JSONException {
		IPanelLog.i(TAG, "------>navigaton transmit json is=" + json);
		switch (code) {
		case __ID_homed_select: {
			ParcelFileDescriptor pfd = null;
			Parcelable parcelable = p.getParcelable("pfd");
			if (parcelable != null) {
				pfd = (ParcelFileDescriptor) parcelable;
			}
			JSONObject o = (JSONObject) new JSONTokener(json).nextValue();
			select(o.getLong("vfreq"), pfd, o.getLong("freq"), o.getInt("fflags"),
					o.getInt("pn"), o.getInt("pflags"), o.getInt("delay"));
			break;
		}
		case __ID_homed_start: {
			JSONObject o = (JSONObject) new JSONTokener(json).nextValue();
			start(o.getString("puri"), o.getInt("pflags"));
			break;
		}
		case __ID_homed_startShift: {
			ParcelFileDescriptor pfd = null;
			Parcelable parcelable = p.getParcelable("pfd");
			if (parcelable != null) {
				pfd = (ParcelFileDescriptor) parcelable;
			}
			IPanelLog.d(TAG, "__ID_homed_pfd_start pfd = " + pfd);
			JSONObject o = (JSONObject) new JSONTokener(json).nextValue();
			startShift(o.getLong("vfreq"), pfd, o.getInt("fflags"));
			break;
		}
		case __ID_homed_redirect: {
			ParcelFileDescriptor pfd = null;
			Parcelable parcelable = p.getParcelable("pfd");
			if (parcelable != null) {
				pfd = (ParcelFileDescriptor) parcelable;
			}
			IPanelLog.d(TAG, "__ID_homed_pfd_start pfd = " + pfd);
			JSONObject o = (JSONObject) new JSONTokener(json).nextValue();
			redirect(o.getLong("vfreq"), pfd, o.getInt("fflags"));
			break;
		}
		case __ID_pause:
			pause();
			break;
		case __ID_resume:
			resume();
		case __ID_homed_solveProblem:
			solveProblem();
			break;
		case __ID_homed_enterCaApp:
			enterCaApp(json);
			break;
		case __ID_homed_observeProgramGuide: {
			JSONObject o = (JSONObject) new JSONTokener(json).nextValue();
			ChannelKey key = ChannelKey.obten(o.getLong("freq"), o.getInt("program_number"));
			observeProgramGuide(key, o.getLong("focus"));
			break;
		}
		case __ID_syncSignalStatus:
			syncSignalStatus();
			break;
		case __ID_homed_captureVideoFrame:
			captureVideoFrame(Integer.parseInt(json));
			break;
		default:
			return super.onTransmit(code, json, p, b);
		}
		return null;
	}

	public void clearWidgetMessage() {
		mWidgetHandle.clearWidgetMessage();
		;
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

	protected ProgramInfo getProgramUri(String str) {
		IPanelLog.d(TAG, "getProgramUri str = " + str);
		Uri uri = Uri.parse(str);
		String apid = uri.getQueryParameter("apid");
		String vpid = uri.getQueryParameter("vpid");
		String adec = uri.getQueryParameter("adec");
		String vdec = uri.getQueryParameter("vdec");
		IPanelLog.d(TAG, "getProgramUri apid:vpid:adec:vdec = " + apid + ":" + vpid + ":" + adec
				+ ":" + vdec);
		ProgramInfo pinfo = new ProgramInfo();
		pinfo.setProgramNumber(1);
		pinfo.setAudioPID(Integer.valueOf(apid));
		pinfo.setAudioStreamType(getStreamType(Integer.valueOf(adec), 0));
		pinfo.setVideoPID(Integer.valueOf(vpid));
		pinfo.setVideoStreamType(getStreamType(Integer.valueOf(vdec), 1));
		pinfo.setPcrPID(Integer.valueOf(vpid));
		return pinfo;
	}

	protected String getStreamType(int type, int flag) {
		IPanelLog.d(TAG, "getStreamType type = " + type + ";flag = " + flag);
		switch (flag) {
		case 0:
			return getAudioType(type);
		case 1:
			return getVideoType(type);
		default:
			break;
		}
		return null;
	}

	// TsVodSourceServiceß÷K
	private String getVideoType(int type) {
		switch (type) {
		case 1:
			return "video_mpeg1";
		case 2:
			return "video_mpeg2";
		case 3:
			return "video_h264";
		case 21:
			return "video_h265";
		default:
			break;
		}
		return null;
	}

	private String getAudioType(int type) {
		switch (type) {
		case 1:
			return "audio_mpeg1";
		case 2:
			return "audio_mpeg2";
		case 4:
			return "audio_aac";
		case 6:
			return "audio_mpeg4_latm_aac";
		case 7:
		case 8:
			return "audio_ac3";
		case 9:
			return "audio_ac3_plus";

		default:
			break;
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

	abstract class DvbRunable implements Runnable {
		int count;

		public DvbRunable(int count) {
			this.count = count;
		}

		public synchronized int getCount() {
			return count;
		}
	}
}