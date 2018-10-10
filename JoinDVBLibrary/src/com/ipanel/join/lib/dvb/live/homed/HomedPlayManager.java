package com.ipanel.join.lib.dvb.live.homed;

import ipaneltv.toolkit.UriToolkit;
import ipaneltv.toolkit.db.DatabaseObjectification.ChannelKey;
import ipaneltv.toolkit.db.DatabaseObjectification.Program;
import ipaneltv.toolkit.dvb.DvbPfSearch;
import ipaneltv.toolkit.media.HomedHttpPlayer;
import ipaneltv.toolkit.media.HomedProgramPlayer;
import ipaneltv.toolkit.media.IpQamTsPlayer;
import ipaneltv.toolkit.media.LocalSockTsPlayer;
import ipaneltv.toolkit.media.MediaSessionInterface.TeeveePlayerBaseInterface;
import ipaneltv.toolkit.media.MediaSessionInterface.TsPlayerInetSourceInterface;
import ipaneltv.toolkit.media.TsPlayerInetSource;

import java.io.IOException;

import com.ipanel.join.lib.dvb.DVBConfig;
import com.ipanel.join.lib.dvb.live.PlayCallback;
import com.ipanel.join.lib.dvb.live.PlayInterface;

import android.net.Uri;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.ParcelFileDescriptor;
import android.util.Log;

public class HomedPlayManager implements PlayInterface {
	static final String TAG = HomedPlayManager.class.getName();
	HomedPlayFragment host;
	private HandlerThread procThread = new HandlerThread(HomedPlayManager.class.getName());
	PlayCallback callback;
	Handler procHandler, callbackHandler;
	private int shiftStartCount = 0, shiftResponsedStartount = 0;
	private boolean shiftMode = false, isSelecting = false, isShifting = false,
			shiftPaused = false;
	private Selection selection = null;
	protected long f = -1;
	protected int pn = -1;
	private Shiftration shiftration = null;
	private DvbPfSearch mDvbPfSearch = null;

	static final int FLAG_LOCAL_MODE = 1;
	static final int FLAG_QAM_MODE = 2;
	private int flag_mode = FLAG_LOCAL_MODE;

	HomedPlayManager(HomedPlayFragment f) {
		host = f;
		procThread.start();
		procHandler = new Handler(procThread.getLooper());
		callbackHandler = new Handler();
	}

	class Selection {
		long freq;
		int fflags, program, pflags;
		String http;

		Selection(long f, int ff, int p, int pf, String h) {
			freq = f;
			fflags = ff;
			program = p;
			pflags = pf;
			http = h;
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
		return true;// shiftStartCount == shiftResponsedStartount;
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
	}

	void suspend() {
		Log.d(TAG, "suspend 44");
		// HttpSource src = host.tryGetHttpSource();
		// if (src != null) {
		// src.stop();
		// }
		f = -1;
		pn = -1;
		shiftStartCount = 0;
		shiftResponsedStartount = 0;
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
//			if (flag_mode == FLAG_QAM_MODE)
//				p = host.tryGetQamShiftPlayer();
//			else if (flag_mode == FLAG_LOCAL_MODE)
//				p = host.tryGetShiftPlayer();
			p = host.tryGetLivePlayer();
		} else {
			p = host.tryGetLivePlayer();
		}
		return p;
	}

//	private final HttpPlayer  getShiftPlayer(int type) {
//		HttpPlayer  p = host.getHttpPlayer();
//		if (type == TsPlayerInetSourceInterface.STREAM_TYPE_IPQAM) {
//			p = host.getQamShiftPlayer();
//		} else if (type == TsPlayerInetSourceInterface.STREAM_TYPE_INET) {
//			p = host.getShiftPlayer();
//		}
//		return p;
//	}

	ParcelFileDescriptor pfds[];

	@Override
	public void select(final String http, final long freq, final int fflags, final int program,
			final int pflags) {
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
					HttpSource src = host.getHttpSource();
					Log.d(TAG, "select player  22 res is :" + p + "/" + src);
					if (p == null) {
						if(src != null){
							src.loosen(true);
						}
						procNotifyUIError("播放失败(没有播放器)");
						return;
					}
					step = 2;
					if (isSelecting) {
						selection = new Selection(freq, fflags, program, pflags, http);
						return;
					}
					step = 3;
					isSelecting = true;
					selection = null;
					int nflags = 0;
					if (src != null && http != null) {
						nflags = fflags;
						ParcelFileDescriptor pfd[] = ParcelFileDescriptor.createPipe();
						Log.d(TAG, "select player  33 http = " + http);
						src.stop();
						Log.d(TAG, "select player  44 pfd[0]:pfd[1] = " + pfd[0] + ":" + pfd[1]);
						p.startFd(-259000000, pfd[0], 0);
						Log.d(TAG, "select player  55 pfd[1] = " + pfd[1]);
						src.start(pfd[1], http, 4, TsPlayerInetSourceInterface.STREAM_TYPE_INET, 0);
						Log.d(TAG, "select player  66");
						pfd[0].close();
						pfd[1].close();
						shiftStartCount++;
						// FileOutputStream fos = new
						// FileOutputStream(pfd[1].getFileDescriptor());
						// byte b[] = {1,2,3,4,5,1,1,1,2,2,2,2,3,3,3,3,4,4,4,4};
						// while(true){
						// Log.d(TAG, "select player  write");
						// fos.write(b);
						// Thread.sleep(500);
						// }
					} else {
						nflags = fflags | 0x10000;
						p.startFd(-1, null, 0);
					}
					Log.d(TAG, "select player  55 nflags= " + nflags);
					Log.i(TAG, "play chanle time-----2---->" + System.currentTimeMillis());
					p.select(freq, nflags, program, pflags);
					f = freq;
					pn = program;
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

		Log.d(TAG, "  do shift   00");
		postProc(new Runnable() {
			@Override
			public void run() {
				int step = 0;
				if (uri == null) {
					Log.d(TAG, " shift uri is null");
					return;
				}
				Log.d(TAG, "  do shift   1");

				Log.d(TAG, "shift(" + uri.hashCode() + "," + off + ") in...");
				try {
					if (shiftMode && shiftPaused) {
						HttpSource s = host.tryGetHttpSource();
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
					HttpSource s = host.tryGetHttpSource();
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
					HttpSource s = host.tryGetHttpSource();
					if (isShifting) {
						step = 1;
						shiftration.seek_millis = millis;
						return;
					}
					// Log.d(TAG, "duration=" + shiftration.duration + ",t=" +
					// millis + ",start="+ shiftration.start);
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
					if (s != null) {
						ParcelFileDescriptor pfd[] = ParcelFileDescriptor.createPipe();
						s.seek(millis, pfd[1]);
						Log.d(TAG, "shiftSeek select player  555 pfd[1] = " + pfd[1]);
						DvbLivePlayer httpdPlayer = host.tryGetLivePlayer();
						httpdPlayer.redirect(-259000000, pfd[0], 0);
						Log.d(TAG, "shiftSeek select player  66");
						pfd[0].close();
						pfd[1].close();
					}
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
	public void loosenAllSession() {
		Log.d(TAG, "loosenAllSession");
		host.loosenAllSession();
		f = -1;
		pn = -1;
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
					HttpSource s = host.tryGetHttpSource();
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
	public void setLoosen(boolean b) {
		Log.d(TAG, "setLoosen b = " + b);
		host.setLoosenState(b);
	}

	@Override
	public void observeProgramGuide(final ChannelKey ch, final long focus) {
		postProc(new Runnable() {
			@Override
			public void run() {
				int step = 0;
				Log.d(TAG, "observeProgramGuide 1 (" + ch + "," + focus + ") in...");
				try {
					if (shiftMode) {
						step = 1;
						return;
					}
//					DvbLivePlayer p = host.tryGetLivePlayer();
//					if (p == null) {
//						step = 2;
//						return;
//					}
					step = 3;
					host.live.observeProgramGuide(ch, focus);
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
		logShiftState("procResponseShiftStart1(" + b + ")");
		shiftResponsedStartount++;
		if (isShiftMode() && !b) {
			procNotifyUIError("数据源连接失败");
		}	
	}

	void procShutdownShift() {
		Log.d(TAG, "procShutdownShift, shiftMode:" + shiftMode);
		if (shiftMode) {
			TeeveePlayerBaseInterface player = currentPlayer();
			HttpSource src = host.tryGetHttpSource();
			if (player != null) {
				player.stop(1);
//				player.loosen(true);
			}
			if (src != null) {
				src.stop();
//				src.loosen(true);
			}
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
			HttpSource src = host.tryGetHttpSource();
			if (player != null) {
				player.stop(1);
			}
			if (src != null) {
				src.stop();
			}
			shiftration = null;
			isShifting = false;
			shiftMode = false;
			shiftPaused = false;
		}
	}

	boolean procSetupShift(String uri, boolean endofshift, int type) {
		Log.d(TAG, "procSetupShift(" + uri + "," + endofshift + "), shiftMode:" + shiftMode);
		try {
			if (!shiftMode) {
				DvbLivePlayer  player = host.getLivePlayer();
				HttpSource src = host.getHttpSource();
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
				ParcelFileDescriptor pfd[] = ParcelFileDescriptor.createPipe();
				Log.d(TAG, "select player  33 uri = " + uri);
				src.stop();
				Log.d(TAG, "select player  44 pfd[0]:pfd[1] = " + pfd[0] + ":" + pfd[1]);
				player.startShift(-259000000, pfd[0], 0);
				Log.d(TAG, "select player  55 pfd[1] = " + pfd[1]);
				src.start(pfd[1], uri, 4, TsPlayerInetSourceInterface.STREAM_TYPE_INET, 0);
				Log.d(TAG, "select player  66");
				pfd[0].close();
				pfd[1].close();
				Log.d(TAG, "shift start end.");
				shiftStartCount++;
				isSelecting = false;
				selection = null;
				shiftMode = true;
			}		
		} catch (Exception e) {
			Log.e(TAG, "procSetupShift e = "+ e.getMessage());
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
		Log.d(TAG, "procUpdateSelectResult isLiveMode = " + isLiveMode() + ";isSelecting = "
				+ isSelecting + ";selection = " + selection);
		if (isLiveMode()) {
			if (isSelecting) {
				isSelecting = false;
				if (selection != null) {
					Log.d(TAG, "procUpdateSelectResult 111");
					DvbLivePlayer p = host.tryGetLivePlayer();
					HttpSource hSource = host.tryGetHttpSource();
					Log.d(TAG, "procUpdateSelectResult 333 hSource = " + hSource + ";p = " + p);
					int nflags = 0;
					if (hSource != null && selection.http != null) {
						ParcelFileDescriptor pfd[];
						try {
							nflags = selection.fflags;
							pfd = ParcelFileDescriptor.createPipe();
							Log.d(TAG, "procUpdateSelectResult  33 http = " + selection.http);
							hSource.stop();
							Log.d(TAG, "procUpdateSelectResult  44 pfd[0]:pfd[1] = " + pfd[0] + ":"
									+ pfd[1]);
							p.startFd(-259000000, pfd[0], 0);
							Log.d(TAG, "procUpdateSelectResult  55");
							hSource.start(pfd[1], selection.http, 4,
									TsPlayerInetSourceInterface.STREAM_TYPE_INET, 0);
							shiftStartCount++;
							Log.d(TAG, "procUpdateSelectResult  66");
							pfd[0].close();
							pfd[1].close();
						} catch (IOException e) {
							Log.e(TAG, "procUpdateSelectResult e = " + e.getMessage());
						}
					} else {
						nflags = selection.fflags | 0x10000;
					}
					Log.d(TAG, "procUpdateSelectResult 333");
					if (p != null) {
						p.select(selection.freq, nflags, selection.program, selection.pflags);
						f = selection.freq;
						pn = selection.program;
					} else {
						return;
					}
					Log.d(TAG, "procUpdateSelectResult 444");
					isSelecting = true;
					selection = null;
				}
			}
		}
	}

//	void procQamShiftPlay(String furi, String puri) {
//		Log.d(TAG, "procQamShiftPlay furi:" + furi + ",puri=" + puri);
//		IpqamShiftPlayer p = host.tryGetQamShiftPlayer();
//		if (p != null) {
//			p.start(furi, 0, puri, 0);
//		} else {
//			procNotifyUIError("找不到时移播放器");
//		}
//	}

//	void procLocalShiftPlay(String furi, String puri) {
//		LocalShiftPlayer p = host.tryGetShiftPlayer();
//		Uri uri = Uri.parse(furi);
//		String localSock = UriToolkit.makeLocalSockUri(uri.getAuthority(), uri.getQuery(),
//				-315000000);
//		Log.d(TAG, "procLocalShiftPlay localSock:" + localSock);
//		if (p != null) {
//			if (shiftration.first) {
//				shiftration.first = false;
//				p.start(localSock, 0, puri, shiftration.pflags);
//			} else {
//				p.redirect(localSock, 0);
//			}
//		} else {
//			procNotifyUIError("找不到时移播放器");
//		}
//	}

	void procShiftHttpPlay(String furi, String puri) {
		DvbLivePlayer  p = host.tryGetLivePlayer();
		if (furi == null) {
//			if (p != null) {
//				p.start("playerror", 0, "playerror", 1);
//			} else {
//				Log.d(TAG, "no DvbLivePlayer 1");
//			}
		} else {
			Uri uri = Uri.parse(furi);
			String localSock = UriToolkit.makeLocalSockUri(uri.getAuthority(), uri.getQuery(),
					-315000000);
			Log.d(TAG, "procHttpPlay localSock:" + localSock);
			if (p != null) {
				p.start(localSock, 0, puri, 1);
			} else {
				Log.d(TAG, "no DvbLivePlayer 2");
			}
		}
	}
	
	void procPlayShift(String furi, String puri) {
		logShiftState("procPlayShift(" + furi + "," + puri + ")");
		if (canProcShift()) {
//			switch (UriToolkit.getSchemaId(furi)) {
//			case UriToolkit.FREQUENCY_INFO_SCHEMA_ID:
//			case UriToolkit.PMT_SCHEMA_ID:
//				procQamShiftPlay(furi, puri);
//				flag_mode = FLAG_QAM_MODE;
//				break;
//			case UriToolkit.LOCALSOCK_SCHEMA_ID:
//				procLocalShiftPlay(furi, puri);
//				flag_mode = FLAG_LOCAL_MODE;
//				break;
//			default:
//				break;
//			}
			procShiftHttpPlay(furi, puri);
		}
	}

	void procLiveHttpPlay(String furi, String puri) {
		DvbLivePlayer p = host.tryGetLivePlayer();
		if (furi == null) {
			if (p != null) {
				p.start("playerror", 0, "playerror", 1);
			} else {
				Log.d(TAG, "no DvbLivePlayer 1");
			}
		} else {
			Uri uri = Uri.parse(furi);
			String localSock = UriToolkit.makeLocalSockUri(uri.getAuthority(), uri.getQuery(),
					-315000000);
			Log.d(TAG, "procHttpPlay localSock:" + localSock);
			if (p != null) {
				p.start(localSock, 0, puri, 1);
			} else {
				Log.d(TAG, "no DvbLivePlayer 2");
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
		try {
			if (canProcShift()) {
				if (isShifting) {
					isShifting = false;
					if (shiftration.seek_millis > 0 && !shiftPaused) {
						HttpSource s = host.tryGetHttpSource();
						if (s != null) {
							isShifting = true;
							Log.d(TAG, "procShiftSeekResponse seek auto...");
							ParcelFileDescriptor pfd[] = ParcelFileDescriptor.createPipe();
							s.seek(shiftration.seek_millis, pfd[1]);
							Log.d(TAG, "select player  55 pfd[1] = " + pfd[1]);
							DvbLivePlayer httpPlayer = host.tryGetLivePlayer();
							httpPlayer.redirect(-259000000, pfd[0], 0);
							Log.d(TAG, "select player  66");
							pfd[0].close();
							pfd[1].close();
						}
						shiftration.seek_millis = -1;
					}
				}
			}	
		} catch (Exception e) {
			Log.e(TAG, "procShiftSeekResponse e = "+ e.getMessage());
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
			mDvbPfSearch = new DvbPfSearch(host.getActivity().getApplicationContext(),
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
}

class DvbLivePlayer extends HomedProgramPlayer {
	HomedPlayManager manager;
	private boolean isLose = false;

	public DvbLivePlayer(HomedPlayManager manager) {
		super(manager.host.getActivity(), manager.host.getPlayServiceName());
		this.manager = manager;
	}

	@Override
	public void onIpStoped(final long f, final int pn) {
		super.onIpStoped(f, pn);
		Log.d("DvbLivePlayer", "onIpStoped f = "+ f +";pn = "+ pn);
		manager.postProc(new Runnable() {
			@Override
			public void run() {
				if (manager.isLiveMode()) {
					manager.postUI(new Runnable() {
						@Override
						public void run() {
							Log.d("DvbLivePlayer", "onIpStoped f = "+ f +";pn = "+ pn+";manager.f = "+ manager.f+";manager.pn = "+ manager.pn);
							if(manager.f == f && manager.pn == pn){
								HttpSource s = manager.host.tryGetHttpSource();
								if(s != null){
									s.stop();
									Log.d("DvbLivePlayer", "onIpStoped");
								}
							}
						}
					});
				}
			}
		});
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

	@Override
	public void onResponseStart(final boolean succ){
		Log.d("DvbLivePlayer", "onResponseStart succ = "+ succ);
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
	
	public boolean isLose() {
		return isLose;
	}

	@Override
	public void onServiceLost() {
		Log.d("DvbLivePlayer", "onServiceLost");
		super.onServiceLost();
		isLose = true;
	}

	@Override
	public void onServiceConnected() {
		Log.d("DvbLivePlayer", "onServiceConnected");
		super.onServiceConnected();
		isLose = false;
	}

	public void reConect() {

	}
}

class HttpPlayer extends HomedHttpPlayer {
	HomedPlayManager manager;
	private boolean isLose = false;
	
	public HttpPlayer(HomedPlayManager manager) {
		super(manager.host.getActivity(), manager.host.getPlayServiceName());
		this.manager = manager;
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
	public boolean isLose() {
		return isLose;
	}

	@Override
	public void onServiceLost() {
		Log.d("DvbLivePlayer", "onServiceLost");
		super.onServiceLost();
		isLose = true;
	}

	@Override
	public void onServiceConnected() {
		Log.d("DvbLivePlayer", "onServiceConnected");
		super.onServiceConnected();
		isLose = false;
	}

	public void reConect() {

	}
}

class LocalShiftPlayer extends LocalSockTsPlayer {
	HomedPlayManager manager;

	public LocalShiftPlayer(HomedPlayManager manager) {
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
	HomedPlayManager manager;

	public IpqamShiftPlayer(HomedPlayManager manager) {
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
	HomedPlayManager manager;

	public NgodShiftSource(HomedPlayManager manager) {
		super(manager.host.getActivity(), manager.host.getSourceServiceName(), Provider.iPanel_Ngod
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

class HttpSource extends TsPlayerInetSource {
	HomedPlayManager manager;

	public HttpSource(HomedPlayManager manager) {
		super(manager.host.getActivity(), manager.host.getSourceServiceName(), Provider.HomedHttp
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
		Log.d("HttpSource", "onResponseStart b = " + b);
		manager.postProc(new Runnable() {
			@Override
			public void run() {
				manager.procResponseShiftStart(b);
			}
		});
		if (!b) {
			manager.postProc(new Runnable() {
				@Override
				public void run() {
					manager.procLiveHttpPlay(null, null);
				}
			});
		}
	}

	@Override
	public void onSourceError(String msg) {
		super.onSourceError(msg);
		Log.d("HttpSource", "onSourceError msg = " + msg);
		manager.postProc(new Runnable() {
			@Override
			public void run() {
				manager.procLiveHttpPlay(null, null);
			}
		});
	}

	@Override
	public void onSourcePlayed(final String streamUri, final String programUri) {
		super.onSourcePlayed(streamUri, programUri);
		manager.postProc(new Runnable() {
			@Override
			public void run() {
				manager.procLiveHttpPlay(streamUri, programUri);
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
