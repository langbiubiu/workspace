package ipaneltv.toolkit.mediaservice.components;

import ipaneltv.toolkit.IPanelLog;
import ipaneltv.toolkit.UriToolkit;
import ipaneltv.toolkit.db.DatabaseObjectification.ChannelKey;
import ipaneltv.toolkit.media.ReserveStateInterface;
import ipaneltv.toolkit.mediaservice.LiveNetworkApplication;
import ipaneltv.toolkit.mediaservice.LiveNetworkApplication.AppComponent;
import ipaneltv.toolkit.mediaservice.components.CaDescramblingManager.ProgramDescramberCallback;
import ipaneltv.toolkit.mediaservice.components.CaDescramblingManager.ProgramDescrambler;
import ipaneltv.toolkit.mediaservice.components.PlayResourceScheduler.ResourcesState;

import java.util.ArrayList;
import java.util.List;

import android.graphics.Rect;
import android.media.TeeveePlayer;
import android.media.TeeveePlayer.PlayStateListener;
import android.media.TeeveePlayer.ProgramStateListener;
import android.net.Uri;
import android.net.telecast.FrequencyInfo;
import android.net.telecast.ProgramInfo;
import android.net.telecast.StreamSelector;
import android.net.telecast.StreamSelector.SelectionStateListener;
import android.util.DisplayMetrics;
import android.util.Log;

public class LiveWidgetPlayer extends AppComponent implements ReserveStateInterface {
	static final String TAG = LiveWidgetPlayer.class.toString();
	Object mutex = new Object();
	private ResourcesState playState;
	private boolean reserved = false;
	private boolean started = false;
	protected ProgramDescrambler mDescrambler;
	FrequencyInfo currentFinfo;
	ProgramInfo currentPinfo;
	private int fFlags = 0;
	private int pFlags = 0;
	public static final int MSG_CLEAR_WIDGET = 0;
	public static final int MSG_TRANSPORT_STATE = 3;
	public static final int MSG_DESCRIBLING_STATE = 4;
	public static final int MSG_SMARTCARD_STATE = 5;

	public boolean descramblingState = true;

	@SuppressWarnings("rawtypes")
	public LiveWidgetPlayer(LiveNetworkApplication app) {
		super(app);
	}

	protected void toChannelTeeveePlay(String tvplayuri) {
		Uri uri = Uri.parse(tvplayuri);
		String fis = UriToolkit.splitFrequencyInfoUriInTeeveePlayUri(uri);
		String pis = UriToolkit.splitProgramInfoUriInTeeveePlayUri(uri);
		int ffalg = UriToolkit.getFrequencyFlagsInTeeveePlayUri(uri);
		int pflags = UriToolkit.getProgramFlagsInTeeveePlayUri(uri);
		Log.d(TAG, "toChannelTeeveePlay fis = " + fis + ";pis = " + pis);
		FrequencyInfo fi = FrequencyInfo.fromString(fis);
		ProgramInfo pi = ProgramInfo.fromString(pis);
		toChannel(fi, ffalg, pi, pflags);
	}

	protected synchronized void toChannel(FrequencyInfo fi, int fflags, ProgramInfo pi, int pflags) {
		IPanelLog.d(TAG, "toChannel");
		synchronized (mutex) {
			if (playState != null && mDescrambler != null) {
				try {
					IPanelLog.d(TAG, "toChannel sleep");
					onSwitchingStart();
					if (playState.reserve() && mDescrambler.reserve()) {
						Rect rect = r;
						IPanelLog.d(TAG, "toChannel rect = " + rect);
						if (rect != null) {
							playState.getPlayer().setDisplay(rect.left, rect.top, rect.right,
									rect.bottom);
						}
						playState.getSelector().select(fi, fflags);
						playState.getPlayer().stop();
						playState.getPlayer().start();
						playState.getPlayer().selectProgram(pi, pflags);
						currentFinfo = fi;
						currentPinfo = pi;
						fFlags = fflags;
						pFlags = pflags;
						started = true;
						ChannelKey key = ChannelKey.obten(fi.getFrequency(), pi.getProgramNumber());
						List<Integer> pids = new ArrayList<Integer>();
						if (pi.getAudioPID() > 0)
							pids.add(pi.getAudioPID());
						if (pi.getVideoPID() > 0)
							pids.add(pi.getVideoPID());
						IPanelLog.d(TAG, "before mDescrambler.stop()");
						mDescrambler.stop();
						IPanelLog.d(TAG, "before mDescrambler.start()");
						boolean d = mDescrambler.start(key, pi.getAudioPID(), pi.getVideoPID());
						IPanelLog.d(TAG, "end mDescrambler.start() d = " + d);
					} else {
						IPanelLog.d(TAG, "playState.reserve() or mDescrambler.reserve() falled");
					}
				} catch (Exception e) {
					e.printStackTrace();
				} finally {// 用完立即释放控制权,并请求保持状态
					loosen(false);
				}
			} else {
				IPanelLog.d(TAG, "playState or mDescrambler == null");
			}
		}
	}

	public void onSwitchingStart() {

	}

	public void onSwitchingEnd() {

	}

	protected void onPlayMessage(int msgType, String msg) {

	}

	Rect r = null;

	protected synchronized void setDisplay(Rect rect) {
		synchronized (mutex) {
			if (playState != null) {
				try {
					IPanelLog.d(TAG, "before setDisplay");
					if (playState.reserve()) {
						started = true;
						IPanelLog.d(TAG, "reserve setDisplay" + " rect.left =" + rect.left
								+ " rect.top = " + rect.top + " rect.right = " + rect.right
								+ " rect.bottom = " + rect.bottom);
						DisplayMetrics aDisplayMetrics = getApp().getResources()
								.getDisplayMetrics();
						if (aDisplayMetrics != null) {
							if(rect.right > aDisplayMetrics.widthPixels){
								rect.right = aDisplayMetrics.widthPixels;
							}
							if(rect.bottom > aDisplayMetrics.heightPixels){
								rect.bottom = aDisplayMetrics.heightPixels;
							}

						}
						boolean isSuccess = playState.getPlayer().setDisplay(rect.left, rect.top,
								rect.right, rect.bottom);
						IPanelLog.d(TAG, "setDisplay isSuccess = " + isSuccess);
						r = null;
					} else {
						IPanelLog.d(TAG, "else setDisplay 22");
						r = rect;
					}
				} catch (Exception e) {
					e.printStackTrace();
				} finally {// 用完立即释放控制权,并请求保持状态
					playState.loosen(false);
				}
			}
		}
	}

	protected synchronized void setVolume(float v) {
		synchronized (mutex) {
			if (playState != null) {
				try {
					IPanelLog.d(TAG, "before setVolume");
					if (playState.reserve()) {
						started = true;
						IPanelLog.d(TAG, "reserve setVolume");
						playState.getPlayer().setVolume(v);
					}
				} catch (Exception e) {
					e.printStackTrace();
				} finally {// 用完立即释放控制权,并请求保持状态
					playState.loosen(false);
				}
			}
		}
	}

	public synchronized void stop() {
		synchronized (mutex) {
			if (playState != null) {
				try {
					if (playState.reserve()) {
						playState.getPlayer().setFreeze(true, TeeveePlayer.FLAG_VIDEO_FRAME_BLACK);
						playState.getPlayer().stop();
						playState.getPlayer().setFreeze(true, TeeveePlayer.FLAG_VIDEO_FRAME_BLACK);
					}
					currentFinfo = null;
					currentPinfo = null;
					started = false;
				} catch (Exception e) {
					e.printStackTrace();
				} finally {// 用完立即释放控制权,并请求保持状态
					playState.loosen(true);
				}
			}
		}
	}

	public synchronized void destrory() {
		Log.d(TAG, "destrory");
		if (playState != null) {
			playState.destroy();
		}
		if (mDescrambler != null) {
			mDescrambler.loosen(true);
		}
	}

	@Override
	public synchronized boolean reserve() {
		if (playState == null) {
			// 优先级为4，低于普通的直播应用
			// 软引用，同优先级也可以被抢占,VOD Widget也是此级别，同样是soft
			playState = getApp().getResourceScheduler().createLivePlayState(false, 4, true);
			playState.getPlayer().setListener(playStateListener, programStateListener);
			playState.getSelector().setSelectionStateListener(selectListener);
		}// 这里并不实际保留播放资源
		if (mDescrambler == null) {
			mDescrambler = getApp().getCaDescramblingManager().createDescrambler(
					descramblerCallback);
		}
		return true;
	}

	public synchronized boolean homedReserve() {
		if (playState == null) {
			// 优先级为4，低于普通的直播应用
			// 软引用，同优先级也可以被抢占,VOD Widget也是此级别，同样是soft
			playState = getApp().getResourceScheduler().createLivePlayState(false, 4, true, 2,
					StreamSelector.CREATE_FLAG_DEFAULT, TeeveePlayer.CREATE_FLAG_BASE_FOR_PIP);
			playState.getPlayer().setListener(playStateListener, programStateListener);
			playState.getSelector().setSelectionStateListener(selectListener);
		}// 这里并不实际保留播放资源
		if (mDescrambler == null) {
			mDescrambler = getApp().getCaDescramblingManager().createDescrambler(
					descramblerCallback);
		}
		return true;
	}

	@Override
	public synchronized void loosen(boolean clearState) {
		if (playState != null) {
			playState.loosen(clearState);
		}
		if (mDescrambler != null) {
			mDescrambler.loosen(clearState);
		}
	}

	@Override
	public boolean isReserved() {
		return reserved;
	}

	public synchronized boolean isStarted() {
		return started;
	}

	protected void onSelectProgram(final ChannelKey key, final String puri, final int flags) {
		IPanelLog.d(TAG, "onSelectProgram key = " + key);
		new Thread(new Runnable() {
			@Override
			public void run() {
				synchronized (mutex) {
					ProgramInfo pi = ProgramInfo.fromString(puri);
					IPanelLog.d(TAG, "onSelectProgram currentPinfo = " + currentPinfo
							+ ";currentPinfo = " + currentPinfo);
					if (currentPinfo != null
							&& currentPinfo.getProgramNumber() == pi.getProgramNumber()) {
						if (playState != null && mDescrambler != null) {
							try {
								IPanelLog.d(TAG, "toChannel sleep");
								if (playState.reserve() && mDescrambler.reserve()) {
									playState.getPlayer().start();
									playState.getPlayer().selectProgram(pi, flags);
									currentPinfo = pi;
									pFlags = flags;
									List<Integer> pids = new ArrayList<Integer>();
									if (pi.getAudioPID() > 0)
										pids.add(pi.getAudioPID());
									if (pi.getVideoPID() > 0)
										pids.add(pi.getVideoPID());
									IPanelLog.d(TAG, "before mDescrambler.stop()");
									mDescrambler.stop();
									IPanelLog.d(TAG, "before mDescrambler.start()");
									boolean d = mDescrambler.start(key, pi.getAudioPID(),
											pi.getVideoPID());
									IPanelLog.d(TAG, "end mDescrambler.start() d = " + d);
								} else {
									IPanelLog.d(TAG,
											"playState.reserve() or mDescrambler.reserve() falled");
								}
							} catch (Exception e) {
								e.printStackTrace();
							} finally {// 用完立即释放控制权,并请求保持状态
								loosen(false);
							}
						} else {
							IPanelLog.d(TAG, "playState or mDescrambler == null");
						}
					}
				}
			}
		}).start();
	}

	protected void reselectProgram(ChannelKey key, String newuri, int flags) {

	}

	ProgramDescramberCallback descramblerCallback = new ProgramDescramberCallback() {
		@Override
		public void onCaModuleDispatched(int moduleId) {
		}

		@Override
		public void onDescramblingState(int code, String err) {
			IPanelLog.i(TAG, "--------onDescramblingState code=" + code + ";err=" + err);
			onPlayMessage(MSG_DESCRIBLING_STATE, err);
			if (code == 452) {
				IPanelLog.i(TAG, "--452------onDescramblingState go in");
				descramblingState = false;
				onSwitchingStart();
			}
			if (code == 0 && err != null) {
				onSwitchingStart();
			}
		}

		@Override
		public void onCaCardState(int code, String msg) {
			onPlayMessage(MSG_DESCRIBLING_STATE, msg);
			IPanelLog.i(TAG, "--------onCaCardState code=" + code + ";msg=" + msg);
			switch (code) {
			case 440:
				onSwitchingStart();
				break;
			case 441:
				onSwitchingStart();
				break;

			default:
				onSwitchingEnd();
				break;
			}
		}
	};

	SelectionStateListener selectListener = new SelectionStateListener() {

		@Override
		public void onSelectionResumed(StreamSelector selector) {
			IPanelLog.d(TAG, "onSelectionResumed :");
			onSwitchingEnd();
			onPlayMessage(MSG_TRANSPORT_STATE, null);
		}

		@Override
		public void onSelectionLost(StreamSelector selector) {
			IPanelLog.d(TAG, "onSelectionLost :");
			onSwitchingStart();
			String err = L10n.TRANSPORT_ERR_402;
			onPlayMessage(MSG_TRANSPORT_STATE, err);
		}

		@Override
		public void onSelectSuccess(StreamSelector selector) {
			IPanelLog.d(TAG, "onSelectSuccess :");
			onSwitchingEnd();
			onPlayMessage(MSG_TRANSPORT_STATE, null);
		}

		@Override
		public void onSelectStart(StreamSelector selector) {

			IPanelLog.d(TAG, "onSelectStart :");

		}

		@Override
		public void onSelectFailed(StreamSelector selector) {
			IPanelLog.d(TAG, "onSelectFailed :");
			onSwitchingStart();
			String err = L10n.TRANSPORT_ERR_401;
			onPlayMessage(MSG_TRANSPORT_STATE, err);
			toChannel(currentFinfo, fFlags, currentPinfo, pFlags);

		}
	};

	PlayStateListener playStateListener = new PlayStateListener() {

		@Override
		public void onSelectionStart(TeeveePlayer player, int program_number) {/*-ignored ,ResourceScheduler done */
			IPanelLog.d(TAG, " StartCount onSelectionStart program_number = " + program_number);
			// onSwitchingEnd();
		}

		@Override
		public void onPlayProcessing(int program_number) {
			IPanelLog.d(TAG, "onPlayProcessing program_number = " + program_number);
			onSwitchingEnd();
		}

		@Override
		public void onPlaySuspending(int program_number) {
			IPanelLog.d(TAG, "onPlaySuspending :" + program_number);
		}

		@Override
		public void onPlayError(int program_number, String msg) {
			IPanelLog.d(TAG, "onPlayError :" + msg);
		}
	};
	ProgramStateListener programStateListener = new ProgramStateListener() {

		@Override
		public void onProgramReselect(int program_number, String newuri) {
			IPanelLog.d(TAG, "onProgramReselect program_number=" + program_number + ";newuri = "
					+ newuri);
			if (currentFinfo != null) {
				reselectProgram(new ChannelKey(currentFinfo.getFrequency(), program_number),
						newuri, pFlags);
			}
		}

		@Override
		public void onProgramDiscontinued(int program_number) {
			IPanelLog.d(TAG, "onProgramDiscontinued program_number = " + program_number);
		}
	};
}
