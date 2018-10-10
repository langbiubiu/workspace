package ipaneltv.toolkit.fragment;

import ipaneltv.toolkit.IPanelLog;
import ipaneltv.toolkit.UriToolkit;
import ipaneltv.toolkit.db.DatabaseObjectification.ChannelKey;
import ipaneltv.toolkit.fragment.PlayActivityInterface.LivePlayBaseInterface;
import ipaneltv.toolkit.media.LiveProgramPlayer;
import ipaneltv.toolkit.media.LocalSockTsPlayer;
import ipaneltv.toolkit.media.MediaSessionInterface.TeeveePlayerBaseInterface;
import ipaneltv.toolkit.media.TsPlayerInetSource;
import android.net.Uri;
import android.os.Handler;
import android.os.HandlerThread;

class LivePlayManager implements PlayActivityInterface.LivePlayBaseInterface {
	static final String TAG = LivePlayManager.class.getSimpleName();
	LivePlayFragment host;
	private HandlerThread procThread = new HandlerThread(LivePlayManager.class.getName());
	LivePlayBaseInterface.Callback callback;
	private Handler procHandler, callbackHandler;
	private int shiftStartCount = 0, shiftResponsedStartount = 0;
	private boolean shiftMode = false, isSelecting = false;
	private boolean isShifting = false, shiftPaused = false;
	private Selection selection = null;
	private Shiftration shiftration = null;

	LivePlayManager(LivePlayFragment f) {
		host = f;
		procThread.start();
		procHandler = new Handler(procThread.getLooper());
		callbackHandler = new Handler();
	}

	class Selection {
		long freq;
		int fflags, program, pflags;

		Selection(long f, int ff, int p, int pf) {
			freq = f;
			fflags = ff;
			program = p;
			pflags = pf;
		}
	}

	class Shiftration {
		String uri;
		int offset = -1, pflags = 0;
		long start = -1;
		boolean first = true;

		Shiftration(String s, int of) {
			uri = s;
			offset = of;
		}

		boolean sameUri(String s) {
			return s.equals(uri);
		}

		boolean canSeek() {
			return start > 0;
		}
	}

	final boolean isShiftMode() {
		return shiftMode;
	}

	final boolean isLiveMode() {
		return !shiftMode;
	}

	final boolean matchShiftVersion() {
		return shiftStartCount == shiftResponsedStartount;
	}

	final boolean canProcShift() {
		return isShiftMode() && matchShiftVersion();
	}

	void setCallback(final LivePlayBaseInterface.Callback callback) {
		this.callback = callback;
	}

	void prepare() {
	}

	void resume() {
	}

	void suspend() {
	}

	void release() {
		callback = null;
		procThread.getLooper().quit();
	}

	final void postProc(Runnable r) {
		procHandler.post(r);
	}

	final void postUI(Runnable r) {
		callbackHandler.post(r);
	}

	@Override
	public void select(final long freq, final int fflags, final int program, final int pflags) {
		IPanelLog.d(this.toString(), "select 11");
		postProc(new Runnable() {
			@Override
			public void run() {
				IPanelLog.d(TAG, "select in(" + freq + "," + fflags + "," + program + "," + pflags + ")"
						+ shiftMode);
				int step = 0;
				try {
					if (shiftMode) {
						procShutdownShift();
						shiftMode = false;
					}
					step = 1;
					PlayLivePlayer p = host.getLivePlayer();
					IPanelLog.d(TAG, "select player res is :" + p);
					if (p == null) {
						procNotifyUIError("播放失败(没有播放器)");
						return;
					}
					step = 2;
					if (isSelecting) {
						selection = new Selection(freq, fflags, program, pflags);
						return;
					}
					step = 3;
					isSelecting = true;
					selection = null;
					p.select(freq, fflags, program, pflags);
					step = 4;
				} catch (Exception e) {
					e.printStackTrace();
				} finally {
					IPanelLog.d(TAG, "select out with:" + step);
				}
			}
		});
	}

	@Override
	public void shift(final String uri, final int off, int pflags) {
		postProc(new Runnable() {
			@Override
			public void run() {
				int step = 0;
				IPanelLog.d(TAG, "shift(" + uri.hashCode() + "," + off + ") in...");
				try {
					if (shiftMode && shiftPaused) {
						PlayShiftSource s = host.tryGetShiftSource();
						PlayShiftPlayer p = host.tryGetShiftPlayer();
						step = 20;
						if (s != null && p != null) {
							step = 21;
							shiftPaused = false;
							s.resume();
							p.resume();
						}
						step = 22;
						return;
					}
					if (!shiftMode) {
						if (!procSetupShift(uri, off < 0)) {
							procNotifyUIError("启动时移失败");
							step = 1;
						} else {
							shiftration = new Shiftration(uri, off);
							step = 2;
						}
						return;// 不再继续
					}
					step = 3;
					PlayShiftSource s = host.tryGetShiftSource();
					if (s == null) {
						step = 4;
						return;
					}
					if (!shiftration.sameUri(uri) || off < 0) {
						step = 5;
						return;
					}
					step = 6;
					if (isShifting) {
						step = 7;
						shiftration.offset = off;
						return;
					}
					if (!shiftration.canSeek()) {
						step = 8;
						shiftration.offset = off;
						return;
					}
					step = 9;
					isShifting = true;
					shiftration.offset = -1;
					s.seek(off);
					step = 10;
				} catch (Exception e) {
					e.printStackTrace();
				} finally {
					IPanelLog.d(TAG, "shift out:" + step);
				}
			}
		});
	}

	private final TeeveePlayerBaseInterface currentPlayer() {
		return shiftMode ? host.tryGetShiftPlayer() : host.tryGetLivePlayer();
	}

	@Override
	public void setDisplay(final int x, final int y, final int w, final int h) {
		postProc(new Runnable() {
			@Override
			public void run() {
				int step = 0;
				IPanelLog.d(TAG, "setDisplay(" + x + "," + y + "," + w + "," + h + ") in ...");
				try {
					TeeveePlayerBaseInterface p = currentPlayer();
					if (p == null)
						return;
					step = 1;
					p.setDisplay(x, y, w, h);
					step = 2;
				} catch (Exception e) {
					e.printStackTrace();
				} finally {
					IPanelLog.d(TAG, "setDisplay out:" + step);
				}
			}
		});
	}

	@Override
	public void setVolume(final float v) {
		postProc(new Runnable() {
			@Override
			public void run() {
				int step = 0;
				IPanelLog.d(TAG, "setVolume(" + v + ") in...");
				try {
					TeeveePlayerBaseInterface p = currentPlayer();
					if (p == null)
						return;
					step = 1;
					p.setVolume(v);
					step = 2;
				} catch (Exception e) {
					e.printStackTrace();
				} finally {
					IPanelLog.d(TAG, "setVolume out:" + step);
				}
			}
		});
	}

	@Override
	public void setProgramFlags(final int flags) {
		postProc(new Runnable() {
			@Override
			public void run() {
				int step = 0;
				IPanelLog.d(TAG, "setProgramFlags(" + flags + ") in...");
				try {
					TeeveePlayerBaseInterface p = currentPlayer();
					if (p == null)
						return;
					step = 1;
					p.setProgramFlags(flags);
					step = 2;
				} catch (Exception e) {
					e.printStackTrace();
				} finally {
					IPanelLog.d(TAG, "setProgramFlags out:" + step);
				}
			}
		});
	}

	@Override
	public void shiftPause(final String uri) {
		postProc(new Runnable() {
			@Override
			public void run() {
				int step = 0;
				IPanelLog.d(TAG, "shiftPause(" + uri + ") in...");
				try {
					if (!shiftMode) {
						step = 1;
						return;
					}
					PlayShiftSource s = host.tryGetShiftSource();
					PlayShiftPlayer p = host.tryGetShiftPlayer();
					if (p == null || s == null) {
						step = 2;
						return;
					}
					step = 3;
					IPanelLog.d(TAG, "shiftPaused: state=" + shiftPaused);
					if (!shiftPaused) {
						shiftPaused = true;
						s.pause();
						p.pause();
					}
					step = 4;
				} catch (Exception e) {
					e.printStackTrace();
				} finally {
					IPanelLog.d(TAG, "shiftPause out:" + step);
				}
			}
		});
	}

	@Override
	public void observeProgramGuide(final ChannelKey ch, final long focus) {
		postProc(new Runnable() {
			@Override
			public void run() {
				int step = 0;
				IPanelLog.d(TAG, "observeProgramGuide(" + ch + "," + focus + ") in...");
				try {
					if (shiftMode) {
						step = 1;
						return;
					}
					PlayLivePlayer p = host.tryGetLivePlayer();
					if (p == null) {
						step = 2;
						return;
					}
					step = 3;
					p.observeProgramGuide(ch, focus);
					step = 4;
				} catch (Exception e) {
					e.printStackTrace();
				} finally {
					IPanelLog.d(TAG, "observeProgramGuide out:" + step);
				}
			}
		});
	}

	// ================
	void procResponseShiftStart(boolean b) {
		logShiftState("procResponseShiftStart(" + b + ")");
		shiftResponsedStartount++;
		if (isShiftMode() && !b) {
			procNotifyUIError("时移数据源连接失败");
		}
	}

	void procShutdownShift() {
		IPanelLog.d(TAG, "procShutdownShift, shiftMode:" + shiftMode);
		if (shiftMode) {
			PlayShiftPlayer player = host.tryGetShiftPlayer();
			PlayShiftSource src = host.tryGetShiftSource();
			if (player != null) {
				player.stop(0);
				player.loosen(true);
			}
			if (src != null) {
				src.stop();
				src.loosen(true);
			}
			shiftration = null;
			isShifting = false;
			shiftMode = false;
			shiftPaused = false;
		}
	}

	boolean procSetupShift(String uri, boolean endofshift) {
		IPanelLog.d(TAG, "procSetupShift(" + uri + "," + endofshift + "), shiftMode:" + shiftMode);
		if (!shiftMode) {
			PlayShiftPlayer player = host.getShiftPlayer();
			PlayShiftSource src = host.getShiftSource();
			if (player == null || src == null) {
				if (player != null)
					player.loosen(true);
				if (src != null)
					src.loosen(true);
				return false;
			}
			IPanelLog.d(TAG, "player=" + player + ", src=" + src);
			int stype = endofshift ? PlayShiftSource.FLAGS_TIMESHIFT_FROM_END
					: PlayShiftSource.FLAGS_TIMESHIFT_FROM_SET;
			IPanelLog.d(TAG, "shift start...");
			src.start(uri, PlayShiftSource.TYPE_TIMESHIFT, PlayShiftSource.STREAM_TYPE_INET, stype);
			IPanelLog.d(TAG, "shift start end.");
			shiftStartCount++;
			isSelecting = false;
			selection = null;
			shiftMode = true;
		}
		return shiftMode;
	}

	void procNotifyUIError(final String msg) {
		postUI(new Runnable() {
			@Override
			public void run() {
				LivePlayBaseInterface.Callback cb = callback;
				if (cb != null)
					cb.onSelectError(msg);
			}
		});
	}

	void procUpdateSelectResult() {
		if (isLiveMode()) {
			if (isSelecting) {
				isSelecting = false;
				if (selection != null) {
					PlayLivePlayer p = host.tryGetLivePlayer();
					if (p != null) {
						p.select(selection.freq, selection.fflags, selection.program,
								selection.pflags);
					}
					isSelecting = true;
					selection = null;
				}
			}
		}
	}

	void procPlayShift(String furi, String puri) {
		logShiftState("procPlayShift(" + furi + "," + puri + ")");
		if (canProcShift()) {
			PlayShiftPlayer p = host.tryGetShiftPlayer();
			Uri uri = Uri.parse(furi);
			String localSock = UriToolkit.makeLocalSockUri(uri.getAuthority(), uri.getQuery(),
					-315000000);
			IPanelLog.d(TAG, "procPlayShift localSock:" + localSock);
			if (p != null) {
				if (shiftration.first) {
					shiftration.first = false;
					p.start(localSock, 0, puri, shiftration.pflags);
				} else {
					p.redirect(localSock, 0);
				}		
			} else {
				procNotifyUIError("找不到时移播放器");
			}
		}
	}

	void procUpdateShiftStartTime(final long t) {
		logShiftState("procUpdateShiftStartTime(" + t + ")");
		if (canProcShift()) {
			shiftration.start = t;
			postUI(new Runnable() {
				public void run() {
					LivePlayBaseInterface.Callback cb = callback;
					if (cb != null)
						cb.onShiftStartTimeUpdated(t);
				}
			});
		}
	}

	void procShiftSeekResponse(final long t) {
		logShiftState("procShiftSeekResponse(" + t + ")");
		if (canProcShift()) {
			if (isShifting) {
				isShifting = false;
				if (shiftration.offset > 0 && !shiftPaused) {
					PlayShiftSource s = host.tryGetShiftSource();
					if (s != null) {
						isShifting = true;
						IPanelLog.d(TAG, "procShiftSeekResponse seek auto...");
						s.seek(shiftration.offset);
					}
					shiftration.offset = -1;
				}
			}
		}
	}

	void procSyncShiftMediaTime(final long t) {
		logShiftState("procSyncShiftMediaTime(" + t + ")");
		if (canProcShift()) {
			postUI(new Runnable() {
				public void run() {
					LivePlayBaseInterface.Callback cb = callback;
					if (cb != null)
						cb.onShiftMediaTimeSync(t);
				}
			});
		}
	}

	void notifyPlayContextReady(final String n) {
		postUI(new Runnable() {
			public void run() {
				LivePlayBaseInterface.Callback cb = callback;
				if (cb != null)
					cb.onContextReady(n);
			}
		});
	}

	final void logShiftState(String p) {
		IPanelLog.d(TAG, p + "> shift mode =" + shiftMode + ",start count=" + shiftStartCount
				+ ", rep count=" + shiftResponsedStartount);
	}
}

class PlayLivePlayer extends LiveProgramPlayer {
	LivePlayManager manager;

	public PlayLivePlayer(LivePlayManager manager) {
		super(manager.host.getActivity(), manager.host.getPlayServiceName());
		this.manager = manager;
	}

	@Override
	public void onLiveInfoUpdated(int mask) {
		super.onLiveInfoUpdated(mask);
		manager.postUI(new Runnable() {
			@Override
			public void run() {
				LivePlayBaseInterface.Callback cb = manager.callback;
				if (cb != null) {
					cb.onLiveInfoUpdated();
				}
			}
		});
	}

	@Override
	public void onCaModuleDispatched(final int moduleId) {
		super.onCaModuleDispatched(moduleId);
		manager.postUI(new Runnable() {
			@Override
			public void run() {
				LivePlayBaseInterface.Callback cb = manager.callback;
				if (cb != null) {
					cb.onCaModuleDispatched(moduleId);
				}
			}
		});
	}

	@Override
	public void onPlayError(final String msg) {
		super.onPlayError(msg);
		manager.postProc(new Runnable() {
			@Override
			public void run() {
				if (manager.isLiveMode()) {
					manager.postUI(new Runnable() {
						@Override
						public void run() {
							LivePlayBaseInterface.Callback cb = manager.callback;
							if (cb != null) {
								cb.onSelectError(msg);
							}
						}
					});
				}
			}
		});
	}

	@Override
	public void onResponseSelect(final boolean b) {
		super.onResponseSelect(b);
		manager.postProc(new Runnable() {
			@Override
			public void run() {
				if (manager.isLiveMode()) {
					manager.procUpdateSelectResult();
				}
			}
		});
	}
}

class PlayShiftPlayer extends LocalSockTsPlayer {
	LivePlayManager manager;

	public PlayShiftPlayer(LivePlayManager manager) {
		super(manager.host.getActivity(), manager.host.getPlayServiceName());
		this.manager = manager;
	}

	@Override
	public void onPlayError(final String msg) {
		super.onPlayError(msg);
		manager.postProc(new Runnable() {
			@Override
			public void run() {
				if (manager.isShiftMode()) {
					manager.postUI(new Runnable() {
						@Override
						public void run() {
							LivePlayBaseInterface.Callback cb = manager.callback;
							if (cb != null)
								cb.onSelectError(msg);
						}
					});
				}
			}
		});
	}
}

class PlayShiftSource extends TsPlayerInetSource {
	LivePlayManager manager;

	public PlayShiftSource(LivePlayManager manager) {
		super(manager.host.getActivity(), manager.host.getSourceServiceName(), Provider.Sihua
				.getName());
		this.manager = manager;
	}

	@Override
	public void onSyncMediaTime(final long t) {
		super.onSyncMediaTime(t);
		manager.postProc(new Runnable() {
			@Override
			public void run() {
				manager.procSyncShiftMediaTime(t);
			}
		});
	}

	@Override
	public void onResponseStart(final boolean b) {
		super.onResponseStart(b);
		manager.postProc(new Runnable() {
			@Override
			public void run() {
				manager.procResponseShiftStart(b);
			}
		});
	}

	@Override
	public void onSourcePlayed(final String streamUri, final String programUri) {
		super.onSourcePlayed(streamUri, programUri);
		manager.postProc(new Runnable() {
			@Override
			public void run() {
				manager.procPlayShift(streamUri, programUri);
			}
		});
	}

	@Override
	public void onShiftStartTime(final long start) {
		super.onShiftStartTime(start);
		manager.postProc(new Runnable() {
			@Override
			public void run() {
				manager.procUpdateShiftStartTime(start);
			}
		});
	}

	@Override
	public void onSourceSeek(final long t) {
		super.onSourceSeek(t);
		manager.postProc(new Runnable() {
			@Override
			public void run() {
				manager.procShiftSeekResponse(t);
			}
		});
	}
}
