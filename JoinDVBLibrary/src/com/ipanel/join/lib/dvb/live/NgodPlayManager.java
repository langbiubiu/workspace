package com.ipanel.join.lib.dvb.live;

import com.ipanel.join.lib.dvb.DVBConfig;

import ipaneltv.toolkit.UriToolkit;
import ipaneltv.toolkit.db.DatabaseObjectification.ChannelKey;
import ipaneltv.toolkit.db.DatabaseObjectification.Program;
import ipaneltv.toolkit.dvb.DvbPfSearch;
import ipaneltv.toolkit.media.IpQamTsPlayer;
import ipaneltv.toolkit.media.LiveProgramPlayer;
import ipaneltv.toolkit.media.LocalSockTsPlayer;
import ipaneltv.toolkit.media.MediaSessionInterface.TeeveePlayerBaseInterface;
import ipaneltv.toolkit.media.MediaSessionInterface.TsPlayerInetSourceInterface;
import ipaneltv.toolkit.media.TsPlayerInetSource;
import android.net.Uri;
import android.os.Handler;
import android.os.HandlerThread;
import android.util.Log;

public class NgodPlayManager implements PlayInterface {
	static final String TAG = NgodPlayManager.class.getName();
	PlayFragment host;
	private HandlerThread procThread = new HandlerThread(NgodPlayManager.class.getName());
	PlayCallback callback;
	Handler procHandler, callbackHandler;
	private int shiftStartCount = 0, shiftResponsedStartount = 0;
	private boolean shiftMode = false, isSelecting = false, isShifting = false,
			shiftPaused = false;
	private Selection selection = null;
	private Shiftration shiftration = null;
	private DvbPfSearch mDvbPfSearch = null;

	static final int FLAG_LOCAL_MODE = 1;
	static final int FLAG_QAM_MODE = 2;
	private int flag_mode = FLAG_QAM_MODE;

	NgodPlayManager(PlayFragment f) {
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
		isSelecting = false;
	}

	void resume() {
	}

	void suspend() {
		isSelecting = false;
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
	public void select(String url, final long freq, final int fflags, final int program, final int pflags) {
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

	@Override
	public void shift(final String uri, final int off, final int pflags) {
		postProc(new Runnable() {
			@Override
			public void run() {
				int step = 0;
				if (uri == null) {
					Log.d(TAG, " shift uri is null 2");
					return;
				}
				Log.d(TAG, "shift(" + uri.hashCode() + "," + off + ","+ pflags+" ) in...");
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
						return;// ???????
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
					TeeveePlayerBaseInterface p = currentPlayer();
					if (isShifting) {
						step = 1;
						shiftration.seek_millis = millis;
						return;
					}
					Log.d(TAG, "1 duration=" + shiftration.duration + ",t=" + millis + ",start="
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
					p.clearCache(0);
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
	public void setLoosen(boolean b) {
		Log.d(TAG, "setLoosen b = " + b);
		host.setLoosenState(b);
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
						p.select(selection.freq, selection.fflags, selection.program,
								selection.pflags);
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
	
	void procOnSourceError(final String err){
		logShiftState("procOnSourceError(" + err + ")");
		if (canProcShift()) {
			postUI(new Runnable() {
				public void run() {
					PlayCallback cb = callback;
					if (cb != null)
						cb.onSourceError(err);
				}
			});
		}
	}

	void procShiftDuration(final long d) {
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
					TeeveePlayerBaseInterface p = currentPlayer();
					if (s != null&&p!= null) {
						isShifting = true;
						Log.d(TAG, "procShiftSeekResponse seek auto...");
						s.seek(t);
						p.clearCache(0);
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
		postUI(new Runnable() {
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

	DvbPfSearch.OnPfInfoListener pfListen = new DvbPfSearch.OnPfInfoListener() {

		@Override
		public void onPfInfoUpdated(final Program present, final Program follow) {
			Log.d(TAG, "onPfInfoUpdated present = " + present + ",follow=" + follow);
			postUI(new Runnable() {
				@Override
				public void run() {
					PlayCallback cb = callback;
					if (cb != null)
						cb.onPfInfoUpdated(present, follow);
				}
			});
		}
	};

	@Override
	public void getPresentAndFollow(final ChannelKey ch) {
		Log.d(TAG, "getPresentAndFollow freq=" + ch.getFrequency() + ",pn=" + ch.getProgram());
		if (mDvbPfSearch == null) {
			mDvbPfSearch = new DvbPfSearch(DVBConfig.getAppCtx(),
					DVBConfig.getUUID());
			mDvbPfSearch.setOnPfInfoListener(pfListen);
		}

		postProc(new Runnable() {
			@Override
			public void run() {
				if (mDvbPfSearch != null) {
					mDvbPfSearch.getPresentAndFollow(ch.getFrequency(), ch.getProgram());
				}
			}
		});
	}

	@Override
	public void loosenAllSession() {
		// TODO Auto-generated method stub
		
	}
}

class DvbLivePlayer extends LiveProgramPlayer {
	NgodPlayManager manager;

	public DvbLivePlayer(NgodPlayManager manager) {
		super(DVBConfig.getAppCtx(), manager.host.getPlayServiceName());
		this.manager = manager;
	}

	@Override
	public void onLiveInfoUpdated(int mask) {
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
				PlayCallback cb = manager.callback;
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
	NgodPlayManager manager;

	public LocalShiftPlayer(NgodPlayManager manager) {
		super(DVBConfig.getAppCtx(), manager.host.getPlayServiceName());
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
	NgodPlayManager manager;

	public IpqamShiftPlayer(NgodPlayManager manager) {
		super(DVBConfig.getAppCtx(), manager.host.getPlayServiceName());
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
	NgodPlayManager manager;

	public NgodShiftSource(NgodPlayManager manager, String provider) {
		super(DVBConfig.getAppCtx(), manager.host.getSourceServiceName(), provider);
		this.manager = manager;
	}
	
	@Override
	public void onSourceError(final String msg) {
		super.onSourceError(msg);
		manager.postProc(new Runnable() {
			@Override
			public void run() {
				manager.procOnSourceError(msg);
			}
		});
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
