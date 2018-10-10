package ipaneltv.toolkit.play;

import ipaneltv.toolkit.IPanelLog;
import ipaneltv.toolkit.UriToolkit;
import ipaneltv.toolkit.db.DatabaseObjectification.ChannelKey;
import ipaneltv.toolkit.media.IpQamTsPlayer;
import ipaneltv.toolkit.media.LiveProgramPlayer;
import ipaneltv.toolkit.media.LocalSockTsPlayer;
import ipaneltv.toolkit.media.MediaSessionInterface.TeeveePlayerBaseInterface;
import ipaneltv.toolkit.media.MediaSessionInterface.TsPlayerInetSourceInterface;
import ipaneltv.toolkit.media.TsPlayerInetSource;
import ipaneltv.toolkit.mediaservice.components.PFDataGetter;
import android.net.Uri;
import android.os.Handler;
import android.os.HandlerThread;
import android.util.Log;

public class PlayManager implements PlayInterface {
	static final String TAG = PlayManager.class.getName();
	PlayFragment host;
	private HandlerThread procThread = new HandlerThread(PlayManager.class.getName());
	PlayCallback callback;
	Handler procHandler, callbackHandler;
	private int shiftStartCount = 0, shiftResponsedStartount = 0;
	private boolean shiftMode = false, isSelecting = false, isShifting = false,
			shiftPaused = false;
	private Selection selection = null;
	private Shiftration shiftration = null;

	static final int FLAG_LOCAL_MODE = 1;
	static final int FLAG_QAM_MODE = 2;
	private int flag_mode = FLAG_LOCAL_MODE;

	private PFDataGetter livePFDatagetter = null;

	PlayManager(PlayFragment f) {
		host = f;
		procThread.start();
		procHandler = new Handler(procThread.getLooper());
		callbackHandler = new Handler();
	}

	class Selection {
		long freq;
		int fflags, program, pflags;
		String furi, puri;

		Selection(long f, int ff, int p, int pf) {
			freq = f;
			fflags = ff;
			program = p;
			pflags = pf;
			furi = null;
			puri = null;
		}

		Selection(String f, int ff, String p, int pf) {
			furi = f;
			fflags = ff;
			puri = p;
			pflags = pf;
			freq = -1;
			program = -1;
		}
	}

	class Shiftration {
		String uri;
		int pflags = 0;
		// long offset = -1;
		long seek_millis = -1;
		long start = -1;
		long duration = -1;
		boolean first = true;

		Shiftration(String s, int of) {
			uri = s;
			seek_millis = -1;
			// offset = of;
		}

		boolean sameUri(String s) {
			return s.equals(uri);
		}

		boolean canSeek() {
			if (duration > 0 || start > 0)
				return true;
			return false;
		}

		boolean validSeekOff(int off) {
			return (off <= (int) duration);
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

	void setCallback(final PlayCallback callback) {
		this.callback = callback;
	}

	void prepare() {
	}

	void resume() {
		init();
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

	private final TeeveePlayerBaseInterface currentPlayer() {
		TeeveePlayerBaseInterface p = null;
		if (shiftMode) {
			if (flag_mode == FLAG_QAM_MODE)
				p = host.tryGetQamShiftPlayer();
			else if (flag_mode == FLAG_LOCAL_MODE)
				p = host.tryGetShiftPlayer();
		} else {
			p = host.tryGetLivePlayer();
		}
		return p;
	}

	private final TeeveePlayerBaseInterface getShiftPlayer(int type) {
		TeeveePlayerBaseInterface p = null;
		if (type == TsPlayerInetSourceInterface.STREAM_TYPE_IPQAM) {
			p = host.getQamShiftPlayer();
		} else if (type == TsPlayerInetSourceInterface.STREAM_TYPE_INET) {
			p = host.getShiftPlayer();
		}
		return p;
	}

	@Override
	public void select(final long freq, final int fflags, final int program, final int pflags) {
		Log.d(this.toString(), "select 11");
		postProc(new Runnable() {
			@Override
			public void run() {
				Log.d(TAG, "select in(" + freq + "," + fflags + "," + program + "," + pflags + ")"
						+ shiftMode);
				int step = 0;
				try {
					if (shiftMode) {
						procShutdownShift();
						shiftMode = false;
					}
					step = 1;
					DvbLivePlayer p = host.getLivePlayer();
					Log.d(TAG, "select player res is :" + p);
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
					Log.d(TAG, "select out with:" + step);
				}
			}
		});
	}

	public void select(final String furi, final int fflags, final String puri, final int pflags) {
		Log.d(this.toString(), "select 11");
		postProc(new Runnable() {
			@Override
			public void run() {
				Log.d(TAG, "select in(" + furi + "," + fflags + "," + puri + "," + pflags + ")"
						+ shiftMode);
				int step = 0;
				try {
					if (shiftMode) {
						procShutdownShift();
						shiftMode = false;
					}
					step = 1;
					DvbLivePlayer p = host.getLivePlayer();
					Log.d(TAG, "select player res is :" + p);
					if (p == null) {
						procNotifyUIError("播放失败(没有播放器)");
						return;
					}
					step = 2;
					if (isSelecting) {
						selection = new Selection(furi, fflags, puri, pflags);
						return;
					}
					step = 3;
					isSelecting = true;
					selection = null;
					p.select(furi, fflags, puri, pflags);
					step = 4;
				} catch (Exception e) {
					e.printStackTrace();
				} finally {
					Log.d(TAG, "select out with:" + step);
				}
			}
		});
	}

	@Override
	public void shift(final String uri, final int off, final int pflags) {
		postProc(new Runnable() {
			@Override
			public void run() {
				int step = 0;
				if (uri == null) {
					Log.d(TAG, " shift uri is null");
					return;
				}
				Log.d(TAG, "shift(" + uri.hashCode() + "," + off + ") in...");
				try {
					if (shiftMode && shiftPaused) {
						NgodShiftSource s = host.tryGetShiftSource();
						TeeveePlayerBaseInterface p = currentPlayer();
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
						if (!procSetupShift(uri, off < 0, pflags)) {
							procNotifyUIError("启动时移失败");
							step = 1;
						} else {
							shiftration = new Shiftration(uri, off);
							step = 2;
						}
						return;// 不再继续
					}
					step = 3;
					NgodShiftSource s = host.tryGetShiftSource();
					if (s == null) {
						step = 4;
						return;
					}
					if (!shiftration.sameUri(uri) || off < 0) {
						step = 5;
						return;
					}
					step = 6;
				} catch (Exception e) {
					e.printStackTrace();
				} finally {
					Log.d(TAG, "shift out:" + step);
				}
			}
		});
	}

	@Override
	public void shiftSeek(final long millis) {
		postProc(new Runnable() {
			@Override
			public void run() {
				int step = 0;
				Log.d(TAG, "shiftSeek(" + millis + ") in...");
				try {
					NgodShiftSource s = host.tryGetShiftSource();
					if (isShifting) {
						step = 1;
						shiftration.seek_millis = millis;
						return;
					}
					Log.d(TAG, "duration=" + shiftration.duration + ",t=" + millis + ",start="
							+ shiftration.start);
					// if (!shiftration.canSeek()) {
					// step = 2;
					// shiftration.offset = off;
					// return;
					// }
					// if (!shiftration.validSeekOff(off)) {
					// step = 3;
					// shiftration.offset = off;
					// return;
					// }
					step = 4;
					isShifting = true;
					shiftration.seek_millis = -1;
					s.seek(millis);
					step = 5;
				} catch (Exception e) {
					e.printStackTrace();
				} finally {
					Log.d(TAG, "shiftSeek out:" + step);
				}
			}
		});
	}

	@Override
	public void shiftStop() {
		procStopShift();
	}

	@Override
	public void setDisplay(final int x, final int y, final int w, final int h) {
		postProc(new Runnable() {
			@Override
			public void run() {
				int step = 0;
				Log.d(TAG, "setDisplay(" + x + "," + y + "," + w + "," + h + ") in ...");
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
					Log.d(TAG, "setDisplay out:" + step);
				}
			}
		});
	}

	@Override
	public void checkPassword(final String pwd) {
		postProc(new Runnable() {
			@Override
			public void run() {
				int step = 0;
				Log.d(TAG, "checkPassword pwd = " + pwd);
				try {
					TeeveePlayerBaseInterface p = currentPlayer();
					if (p == null)
						return;
					step = 1;
					p.checkPassword(pwd);
					step = 2;
				} catch (Exception e) {
					e.printStackTrace();
				} finally {
					Log.d(TAG, "checkPassword out:" + step);
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
				Log.d(TAG, "setVolume(" + v + ") in...");
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
					Log.d(TAG, "setVolume out:" + step);
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
				Log.d(TAG, "setProgramFlags(" + flags + ") in...");
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
					Log.d(TAG, "setProgramFlags out:" + step);
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
				Log.d(TAG, "shiftPause(" + uri + ") in...");
				try {
					if (!shiftMode) {
						step = 1;
						return;
					}
					NgodShiftSource s = host.tryGetShiftSource();
					TeeveePlayerBaseInterface p = currentPlayer();
					if (p == null || s == null) {
						step = 2;
						return;
					}
					step = 3;
					Log.d(TAG, "shiftPaused: state=" + shiftPaused);
					if (!shiftPaused) {
						shiftPaused = true;
						s.pause();
						p.pause();
					}
					step = 4;
				} catch (Exception e) {
					e.printStackTrace();
				} finally {
					Log.d(TAG, "shiftPause out:" + step);
				}
			}
		});
	}

	@Override
	public void syncSignalStatus() {
		postProc(new Runnable() {
			@Override
			public void run() {
				int step = 0;
				Log.d(TAG, "syncSignalStatus in...");
				try {
					if (shiftMode) {
						step = 1;
						return;
					}
					DvbLivePlayer p = host.tryGetLivePlayer();
					if (p == null) {
						step = 2;
						return;
					}
					step = 3;
					p.syncSignalStatus();
					step = 4;
				} catch (Exception e) {
					e.printStackTrace();
				} finally {
					Log.d(TAG, "syncSignalStatus out:" + step);
				}
			}
		});
	}

	@Override
	public void loosenAllSession() {
		Log.d(TAG, "loosenAllSession");
		host.loosenAllSession();
	}

	void procSyncSignalStatus(final String msg) {
		postUI(new Runnable() {
			@Override
			public void run() {
				PlayCallback cb = callback;
				if (cb != null)
					cb.onSyncSignalStatus(msg);
			}
		});
	}

	@Override
	public void observeProgramGuide(final ChannelKey ch, final long focus) {
		postProc(new Runnable() {
			@Override
			public void run() {
				int step = 0;
				Log.d(TAG, "observeProgramGuide(" + ch + "," + focus + ") in...");
				try {
					if (shiftMode) {
						step = 1;
						return;
					}
					DvbLivePlayer p = host.tryGetLivePlayer();
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
					Log.d(TAG, "observeProgramGuide out:" + step);
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
		Log.d(TAG, "procShutdownShift, shiftMode:" + shiftMode);
		if (shiftMode) {
			TeeveePlayerBaseInterface player = currentPlayer();
			NgodShiftSource src = host.tryGetShiftSource();
			if (player != null) {
				player.stop(0);
				player.loosen(true);
			}
			if (src != null) {
				src.stop();
				src.loosen(true);
			}
			shiftStartCount = 0;
			shiftResponsedStartount = 0;
			shiftration = null;
			isShifting = false;
			shiftMode = false;
			shiftPaused = false;
		}
	}

	void procStopShift() {
		Log.d(TAG, "procStopShift, shiftMode:" + shiftMode);
		if (shiftMode) {
			TeeveePlayerBaseInterface player = currentPlayer();
			NgodShiftSource src = host.tryGetShiftSource();
			if (player != null) {
				player.stop(0);
			}
			if (src != null) {
				src.stop();
			}
			shiftStartCount = 0;
			shiftResponsedStartount = 0;
			shiftration = null;
			isShifting = false;
			shiftMode = false;
			shiftPaused = false;
		}
	}

	boolean procSetupShift(String uri, boolean endofshift, int type) {
		Log.d(TAG, "procSetupShift(" + uri + "," + endofshift + "), shiftMode:" + shiftMode);
		if (!shiftMode) {
			TeeveePlayerBaseInterface player = getShiftPlayer(type);
			NgodShiftSource src = host.getShiftSource();
			if (player == null || src == null) {
				if (player != null)
					player.loosen(true);
				if (src != null)
					src.loosen(true);
				return false;
			}
			Log.d(TAG, "player=" + player + ", src=" + src);
			int stype = endofshift ? NgodShiftSource.FLAGS_TIMESHIFT_FROM_END
					: NgodShiftSource.FLAGS_TIMESHIFT_FROM_SET;
			Log.d(TAG, "shift start... type=" + type + ",stype=" + stype);
			src.start(uri, NgodShiftSource.TYPE_TIMESHIFT, type, stype);
			Log.d(TAG, "shift start end.");
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
				PlayCallback cb = callback;
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
					DvbLivePlayer p = host.tryGetLivePlayer();
					if (p != null) {
						if (selection.furi != null) {
							p.select(selection.furi, selection.fflags, selection.puri,
									selection.pflags);
						} else {
							p.select(selection.freq, selection.fflags, selection.program,
									selection.pflags);
						}
					}
					isSelecting = true;
					selection = null;
				}
			}
		}
	}

	void procQamShiftPlay(String furi, String puri) {
		Log.d(TAG, "procQamShiftPlay furi:" + furi + ",puri=" + puri);
		IpqamShiftPlayer p = host.tryGetQamShiftPlayer();
		if (p != null) {
			p.start(furi, 0, puri, 0);
		} else {
			procNotifyUIError("找不到时移播放器");
		}
	}

	void procLocalShiftPlay(String furi, String puri) {
		LocalShiftPlayer p = host.tryGetShiftPlayer();
		Uri uri = Uri.parse(furi);
		String localSock = UriToolkit.makeLocalSockUri(uri.getAuthority(), uri.getQuery(),
				-315000000);
		Log.d(TAG, "procLocalShiftPlay localSock:" + localSock);
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

	void procPlayShift(String furi, String puri) {
		logShiftState("procPlayShift(" + furi + "," + puri + ")");
		if (canProcShift()) {
			switch (UriToolkit.getSchemaId(furi)) {
			case UriToolkit.FREQUENCY_INFO_SCHEMA_ID:
			case UriToolkit.PMT_SCHEMA_ID:
				procQamShiftPlay(furi, puri);
				flag_mode = FLAG_QAM_MODE;
				break;
			case UriToolkit.LOCALSOCK_SCHEMA_ID:
				procLocalShiftPlay(furi, puri);
				flag_mode = FLAG_LOCAL_MODE;
				break;
			default:
				break;
			}
		}
	}

	void procUpdateShiftStartTime(final long t) {
		logShiftState("procUpdateShiftStartTime(" + t + ")");
		if (canProcShift()) {
			shiftration.start = t;
			postUI(new Runnable() {
				public void run() {
					PlayCallback cb = callback;
					if (cb != null)
						cb.onShiftStartTimeUpdated(t);
				}
			});
		}
	}

	void procShiftDuration(final long d) {
	    IPanelLog.d(TAG, "hhh; onShiftDuration, duration =" +d);
		logShiftState("procShiftDuration(" + d + ")");
		if (canProcShift()) {
			shiftration.duration = d;
			postUI(new Runnable() {
				@Override
				public void run() {
					PlayCallback cb = callback;
					if (cb != null)
						cb.onShiftDuration(d);
				}
			});
		}
	}

	void procShiftSeekResponse(final long t) {
		logShiftState("procShiftSeekResponse(" + t + ")");
		if (canProcShift()) {
			if (isShifting) {
				isShifting = false;
				if (shiftration.seek_millis > 0 && !shiftPaused) {
					NgodShiftSource s = host.tryGetShiftSource();
					if (s != null) {
						isShifting = true;
						Log.d(TAG, "procShiftSeekResponse seek auto...");
						s.seek(t);
					}
					shiftration.seek_millis = -1;
				}
			}
		}
	}

	void procSyncShiftMediaTime(final long t) {
		logShiftState("procSyncShiftMediaTime(" + t + ")");
		if (canProcShift()) {
			postUI(new Runnable() {
				public void run() {
					PlayCallback cb = callback;
					if (cb != null)
						cb.onShiftMediaTimeSync(t);
				}
			});
		}
	}

	void notifyPlayContextReady(final String n) {
	    IPanelLog.d(TAG,"hhh; notifyPlayContextReady, group is "+n);
		postProc(new Runnable() {
			public void run() {
				PlayCallback cb = callback;
				if (cb != null)
					cb.onContextReady(n);
			}
		});
	}

	final void logShiftState(String p) {
		Log.d(TAG, p + "> shift mode =" + shiftMode + ",start count=" + shiftStartCount
				+ ", rep count=" + shiftResponsedStartount);
	}

	@Override
	public synchronized void getPresentAndFollow(final ChannelKey ch) {
		Log.d(TAG, "getPresentAndFollow freq=" + ch.getFrequency() + ",pn=" + ch.getProgram()
				+ ";livePFDatagetter = " + livePFDatagetter);
		Log.d(TAG, "LivePFDatagetter set Linstener end");
		postProc(new Runnable() {
			@Override
			public void run() {
				if (livePFDatagetter != null) {

					Log.d(TAG, "go on livePFDatagetter  getPresentAndFollow");
					livePFDatagetter.getPresentAndFollow(ch.getFrequency(), ch.getProgram(), 0);
				}
			}
		});

	}

	private void init() {
		if (livePFDatagetter == null) {
			Log.d(TAG, "getPresentAndFollow in 11");
			livePFDatagetter = new PFDataGetter(host.context, host.getNetworkUUID(), 0) {
				@Override
				protected void onProgramsFound(final PresentAndFollow pf, final int flags) {
					postUI(new Runnable() {
						@Override
						public void run() {
							PlayCallback cb = callback;
							if (cb != null) {
								Log.d(TAG, "go on cb  onPfInfoUpdated");
								cb.onPfInfoUpdated(pf);
								Log.d(TAG, "not go into cb onPfInfoUpdated");
							}
						}
					});
				}
			};
			Log.d(TAG, "getPresentAndFollow 11");
			livePFDatagetter.setEncoding("gbk");
			Log.d(TAG, "getPresentAndFollow 22");
			livePFDatagetter.setAttention(true, 0);
			Log.d(TAG, "getPresentAndFollow");
		}
	}
}

class DvbLivePlayer extends LiveProgramPlayer {
	PlayManager manager;

	public DvbLivePlayer(PlayManager manager) {
		super(manager.host.context, manager.host.getPlayServiceName());
		this.manager = manager;
		IPanelLog.d ( "DvbLivePlayer", "DvbLivePlayer..." );
	}

	@Override
	public void onSyncSignalStatus(final String signalStatus) {
		super.onSyncSignalStatus(signalStatus);
		manager.postProc(new Runnable() {
			@Override
			public void run() {
			    IPanelLog.d ( "DvbLivePlayer", "hhh; onSyncSignalStatus..."+signalStatus);
				if (manager.isLiveMode()) {
					manager.procSyncSignalStatus(signalStatus);
				}
			}
		});
	}

	@Override
	public void onLiveInfoUpdated(int mask) {
	    IPanelLog.d("DvbLivePlayer","hhh; onLiveInfoUpdated ---");
		super.onLiveInfoUpdated(mask);
		manager.postUI(new Runnable() {
			@Override
			public void run() {
				PlayCallback cb = manager.callback;
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
			    IPanelLog.d("DvbLivePlayer","hhh; onCaModuleDispatched ---");
				PlayCallback cb = manager.callback;
				if (cb != null) {
					cb.onCaModuleDispatched(moduleId);
				}
			}
		});
	}

	@Override
	public void onDescramError(final long f, final int pn, final int code, final String err) {
		Log.d("DvbLivePlayer", "onDescramError f = " + f + ";pn = " + pn + ";code = " + code);
		super.onDescramError(f, pn, code, err);
		manager.postUI(new Runnable() {
			@Override
			public void run() {
				PlayCallback cb = manager.callback;
				if (cb != null) {
					cb.onDescramError(f, pn, code, err);
				}
			}
		});
	};

	@Override
	public void onChannelLocked(final long freq, final int program_number) {
		Log.d("DvbLivePlayer", "onChannelLocked freq = " + freq + ";program_number = "
				+ program_number);
		super.onChannelLocked(freq, program_number);
		manager.postUI(new Runnable() {
			@Override
			public void run() {
				PlayCallback cb = manager.callback;
				if (cb != null) {
					cb.onChannelLocked(freq, program_number);
				}
			}
		});
	}

	@Override
	public void onPasswordChecked(final boolean succ) {
		Log.d("DvbLivePlayer", "onPasswordChecked succ = " + succ);
		super.onPasswordChecked(succ);
		manager.postUI(new Runnable() {
			@Override
			public void run() {
				PlayCallback cb = manager.callback;
				if (cb != null) {
					cb.onPasswordChecked(succ);
				}
			}
		});
	}

	@Override
	public void onPlayError(final String msg) {
		super.onPlayError(msg);
		IPanelLog.d("DvbLivePlayer","hhh; onPlayError ---");
		manager.postProc(new Runnable() {
			@Override
			public void run() {
				if (manager.isLiveMode()) {
					manager.postUI(new Runnable() {
						@Override
						public void run() {
							PlayCallback cb = manager.callback;
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

class LocalShiftPlayer extends LocalSockTsPlayer {
	PlayManager manager;

	public LocalShiftPlayer(PlayManager manager) {
		super(manager.host.context, manager.host.getPlayServiceName());
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
							PlayCallback cb = manager.callback;
							if (cb != null)
								cb.onSelectError(msg);
						}
					});
				}
			}
		});
	}

	@Override
	public void onResponseStart(final boolean succ) {
		super.onResponseStart(succ);
		manager.postProc(new Runnable() {
			@Override
			public void run() {
				if (manager.isShiftMode()) {
					manager.postUI(new Runnable() {
						@Override
						public void run() {
							PlayCallback cb = manager.callback;
							if (cb != null)
								cb.onShiftPlay(succ);
						}
					});
				}
			}
		});
	}
}

class IpqamShiftPlayer extends IpQamTsPlayer {
	PlayManager manager;

	public IpqamShiftPlayer(PlayManager manager) {
		super(manager.host.context, manager.host.getPlayServiceName());
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
							PlayCallback cb = manager.callback;
							if (cb != null)
								cb.onSelectError(msg);
						}
					});
				}
			}
		});
	}

	@Override
	public void onResponseStart(final boolean succ) {
		super.onResponseStart(succ);
		manager.postProc(new Runnable() {
			@Override
			public void run() {
				if (manager.isShiftMode()) {
					manager.postUI(new Runnable() {
						@Override
						public void run() {
							PlayCallback cb = manager.callback;
							if (cb != null)
								cb.onShiftPlay(succ);
						}
					});
				}
			}
		});
	}
}

class NgodShiftSource extends TsPlayerInetSource {
	PlayManager manager;

	public NgodShiftSource(PlayManager manager) {
		super(manager.host.context, manager.host.getSourceServiceName(), manager.host.provider);
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

	@Override
	public void onVodDuration(final long d) {
		super.onVodDuration(d);
		manager.postProc(new Runnable() {
			@Override
			public void run() {
				manager.procShiftDuration(d);
			}
		});
	}
}
