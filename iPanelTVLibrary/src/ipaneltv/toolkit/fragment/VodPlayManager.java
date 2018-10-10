package ipaneltv.toolkit.fragment;

import ipaneltv.toolkit.IPanelLog;
import ipaneltv.toolkit.UriToolkit;
import ipaneltv.toolkit.media.IpQamTsPlayer;
import ipaneltv.toolkit.media.LocalSockTsPlayer;
import ipaneltv.toolkit.media.MediaSessionInterface.TeeveePlayerBaseInterface;
import ipaneltv.toolkit.media.TsPlayerInetSource;
import android.graphics.Rect;
import android.media.TeeveePlayer;
import android.net.Uri;
import android.os.Handler;
import android.os.HandlerThread;

public class VodPlayManager implements PlayActivityInterface.VodPlayBaseInterface {
	static final String TAG = VodPlayManager.class.getSimpleName();
	VodPlayFragment host;
	private HandlerThread procThread = new HandlerThread(LivePlayManager.class.getName());
	VodPlayBaseInterface.Callback callback;
	private Handler procHandler, callbackHandler;

	private static final int IPQAM_MODE = 1;
	private static final int INET_MODE = 2;
	private int playMode = 0;
	private int playStartCount = 0, playResponsedStartount = 0;
	private boolean playPaused = false;
	private Selection selection = null;

	VodPlayManager(VodPlayFragment host) {
		this.host = host;
		procThread.start();
		procHandler = new Handler(procThread.getLooper());
		callbackHandler = new Handler();
	}

	void setCallback(final VodPlayBaseInterface.Callback callback) {
		this.callback = callback;
	}

	void prepare() {
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

	class Selection {
		String furi, puri;
		int fflags, pflags;

		Selection() {
			this.furi = null;
			this.puri = null;
			this.fflags = 0;
			this.pflags = 0;
		}

		void setfuri(String furi) {
			this.furi = furi;
		}

		void setprui(String puri) {
			this.puri = puri;
		}

		void setfflag(int fflags) {
			this.fflags = fflags;
		}

		void setpflag(int pflags) {
			this.pflags = pflags;
		}

		private int checkUri(String f, String p) {
			if ((furi != null && puri == null) || (furi == null && puri != null))
				throw new RuntimeException("impl err");
			if (furi == null)
				return 1;
			boolean fb = furi.equals(f), pb = puri.equals(p);
			if (!fb && !pb) { // 源与节目都不相同
				furi = null;
				puri = null;
				return 1;
			} else if (fb && pb) { // 源与节目相同
				return 0;
			} else if (!fb && pb) { // 源不同而节目相同
				return 2;
			} else if (fb && !pb) { // 源相同而节目不同
				return 3;
			} else {
				throw new RuntimeException("impl err2");
			}
		}
	}

	final boolean matchPlayVersion() {
		return playStartCount == playResponsedStartount;
	}

	final boolean canProcMessage() {
		return matchPlayVersion();
	}

	private final TeeveePlayerBaseInterface currentPlayer() {
		if (playMode == IPQAM_MODE)
			host.tryGetIpqamPlayer();
		else if (playMode == INET_MODE)
			host.tryGetLocalsockPlayer();
		return null;
	}

	@Override
	public void start(String uri, int type, int streamType, int flags) {
		IPanelLog.d(TAG, "start uri=" + uri + ",type=" + type + ",streamType=" + streamType);
		TeeveePlayerBaseInterface player = currentPlayer();
		VodPlaySource src = host.getVodSource();
		if (player == null || src == null) {
			IPanelLog.d(TAG, "start loosen player&src");
			if (player != null)
				player.loosen(true);
			if (src != null)
				src.loosen(true);
			return;
		}
		IPanelLog.d(TAG, "vod player=" + player + ", src=" + src);
		IPanelLog.d(TAG, "vod start");
		src.start(uri, type, streamType, flags);
		IPanelLog.d(TAG, "vod start end.");
		playStartCount++;
		playPaused = false;
		selection = null;
	}

	@Override
	public void stop() {
		postProc(new Runnable() {
			@Override
			public void run() {
				int step = 0;
				IPanelLog.d(TAG, "stop in...");
				try {
					VodPlaySource s = host.getVodSource();
					TeeveePlayerBaseInterface p = currentPlayer();
					if (p == null || s == null) {
						step = 2;
						return;
					}
					step = 3;
					s.stop();
					p.stop(TeeveePlayer.FLAG_VIDEO_FRAME_BLACK);
					step = 4;
				} catch (Exception e) {
					e.printStackTrace();
				} finally {
					IPanelLog.d(TAG, "stop out:" + step);
				}
			}
		});
	}

	@Override
	public void pause() {
		postProc(new Runnable() {
			@Override
			public void run() {
				int step = 0;
				IPanelLog.d(TAG, "pause in...");
				try {
					VodPlaySource s = host.getVodSource();
					TeeveePlayerBaseInterface p = currentPlayer();
					if (p == null || s == null) {
						step = 2;
						return;
					}
					step = 3;
					IPanelLog.d(TAG, "pause: state=" + playPaused);
					if (!playPaused) {
						playPaused = true;
						s.pause();
						p.pause();
					}
					step = 4;
				} catch (Exception e) {
					e.printStackTrace();
				} finally {
					IPanelLog.d(TAG, "pause out:" + step);
				}
			}
		});
	}

	@Override
	public void resume() {
		postProc(new Runnable() {
			@Override
			public void run() {
				int step = 0;
				IPanelLog.d(TAG, "resume in...");
				try {
					VodPlaySource s = host.getVodSource();
					TeeveePlayerBaseInterface p = currentPlayer();
					if (p == null || s == null) {
						step = 2;
						return;
					}
					step = 3;
					IPanelLog.d(TAG, "resume: state=" + playPaused);
					if (playPaused) {
						playPaused = false;
						s.resume();
						p.resume();
					}
					step = 4;
				} catch (Exception e) {
					e.printStackTrace();
				} finally {
					IPanelLog.d(TAG, "resume out:" + step);
				}
			}
		});
	}

	@Override
	public void setDisplay(final Rect rect) {
		postProc(new Runnable() {
			@Override
			public void run() {
				int step = 0;
				int x = rect.left, y = rect.top;
				int w = rect.right - rect.left + 1, h = rect.bottom - rect.top + 1;
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

	private boolean seeking = false;
	private Long seekpos = null;

	@Override
	public void seek(final long time) {
		postProc(new Runnable() {
			@Override
			public void run() {
				int step = 0;
				IPanelLog.d(TAG, "seek time = " + time);
				try {
					VodPlaySource s = host.getVodSource();
					if (s == null)
						return;
					step = 1;
					if (seeking) {
						seekpos = time;
					} else {
						seeking = true;
						s.seek(time);
					}
					step = 2;
				} catch (Exception e) {
					e.printStackTrace();
				} finally {
					IPanelLog.d(TAG, "seek out:" + step);
				}
			}
		});
	}

	@Override
	public void setRate(final float rate) {
		postProc(new Runnable() {
			@Override
			public void run() {
				int step = 0;
				IPanelLog.d(TAG, "setRate rate=" + rate);
				try {
					TeeveePlayerBaseInterface p = currentPlayer();
					if (p == null)
						return;
					step = 1;
					if (playMode == IPQAM_MODE)
						host.tryGetIpqamPlayer().setRate(rate);
					else
						host.tryGetLocalsockPlayer().setRate(rate);
					step = 2;
				} catch (Exception e) {
					e.printStackTrace();
				} finally {
					IPanelLog.d(TAG, "setRate out:" + step);
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
	public void setProgramFlag(final int flags) {
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
					if (selection != null)
						selection.setpflag(flags);
				} catch (Exception e) {
					e.printStackTrace();
				} finally {
					IPanelLog.d(TAG, "setProgramFlags out:" + step);
				}
			}
		});
	}

	// ================
	void procResponseStart(final boolean b) {
		IPanelLog.d(TAG, "procResponseStart(" + b + ")");
		playResponsedStartount++;
		postUI(new Runnable() {
			public void run() {
				VodPlayBaseInterface.Callback cb = callback;
				if (cb != null)
					cb.onPlayStart(b);
			}
		});
	}

	void procSourceError(final String msg) {
		IPanelLog.d(TAG, "procSourceError(" + msg + ")");
		if (canProcMessage()) {
			postUI(new Runnable() {
				public void run() {
					VodPlayBaseInterface.Callback cb = callback;
					if (cb != null)
						cb.onPlayError(msg);
				}
			});
		}
	}

	void procSourceMessage(final String msg) {
		IPanelLog.d(TAG, "procSourceMessage(" + msg + ")");
		if (canProcMessage()) {
			postUI(new Runnable() {
				public void run() {
					VodPlayBaseInterface.Callback cb = callback;
					if (cb != null)
						cb.onPlayMsg(msg);
				}
			});
		}
	}

	void procEndOfSource(final float r) {
		IPanelLog.d(TAG, "procEndOfSource(" + r + ")");
		if (canProcMessage()) {
			postUI(new Runnable() {
				public void run() {
					VodPlayBaseInterface.Callback cb = callback;
					if (cb != null)
						cb.onPlayEnd(r);
				}
			});
		}
	}

	private void procPlayIpqam(String furi, String puri) {
		selection.setfuri(furi);
		selection.setprui(puri);
		playMode = IPQAM_MODE;
		host.getIpqamPlayer().start(furi, selection.fflags, puri, selection.pflags);
	}

	void procPlayLocalSock(String localSock, String puri) {
		Uri uri = Uri.parse(localSock);
		localSock = UriToolkit.makeLocalSockUri(uri.getAuthority(), uri.getQuery(), -259000000);

		playMode = INET_MODE;
		int result = selection.checkUri(localSock, puri);
		IPanelLog.d(TAG, "procPlayLocalSock>localSock=" + localSock + ",puri=" + puri);
		switch (result) {
		case 1:
			selection.setfuri(localSock);
			selection.setprui(puri);
			host.getLocalsockPlayer().start(localSock, selection.fflags, puri, selection.pflags);
			break;
		case 2:
			selection.setfuri(localSock);
			host.getLocalsockPlayer().redirect(localSock, selection.fflags);
			break;
		case 3:
			selection.setprui(puri);
			host.getLocalsockPlayer().start(localSock, selection.fflags, puri, selection.pflags);
			break;
		case 0:
		default:
			break;
		}
	}

	void procNotifyUIError(final String msg) {
		postUI(new Runnable() {
			@Override
			public void run() {
				VodPlayBaseInterface.Callback cb = callback;
				if (cb != null)
					cb.onPlayError(msg);
			}
		});
	}

	void procPlayPlayed(final String fUri, final String pUri) {
		IPanelLog.d(TAG, "procPlayPlayed furi=" + fUri + ",pUri=" + pUri);
		postProc(new Runnable() {
			@Override
			public void run() {
				if (canProcMessage()) {
					int step = 0;
					try {
						boolean succ = false;
						if (selection == null) {
							selection = new Selection();
						}
						IPanelLog.d(TAG, "procPlayPlayed start");
						switch (UriToolkit.getSchemaId(fUri)) {
						case UriToolkit.FREQUENCY_INFO_SCHEMA_ID:
							step = 1;
							if (UriToolkit.getSchemaId(pUri) == UriToolkit.DVB_SERVICE_SCHEMA_ID
									|| UriToolkit.getSchemaId(pUri) == UriToolkit.PMT_SCHEMA_ID) {
								procPlayIpqam(fUri, pUri);
								succ = true;
								step = 2;
							}
							step = 3;
							break;
						case UriToolkit.LOCALSOCK_SCHEMA_ID:
							step = 1;
							if (UriToolkit.getSchemaId(fUri) == UriToolkit.LOCALSOCK_SCHEMA_ID) {
								procPlayLocalSock(fUri, pUri);
								succ = true;
								step = 2;
							}
							step = 3;
							break;
						default:
							step = -1;
							break;
						}
						if (!succ)
							procNotifyUIError("onSourcePlayed failed");
					} catch (Exception e) {
						e.printStackTrace();
					} finally {
						IPanelLog.d(TAG, "procPlayPlayed out:" + step);
					}
				}
			}
		});
	}

	void procSeeBackPeriod(final long start, final long end) {
		IPanelLog.d(TAG, "procSeeBackPeriod(" + start + "," + end + ")");
		if (canProcMessage()) {
			postUI(new Runnable() {
				public void run() {
					VodPlayBaseInterface.Callback cb = callback;
					if (cb != null)
						cb.onSeeBackPeriod(start, end);
				}
			});
		}
	}

	void procSourceRate(final float r) {
		IPanelLog.d(TAG, "procSourceRate(" + r + ")");
		if (canProcMessage()) {
			postUI(new Runnable() {
				public void run() {
					VodPlayBaseInterface.Callback cb = callback;
					if (cb != null)
						cb.onSourceRate(r);
				}
			});
		}
	}

	void procVodDuration(final long d) {
		IPanelLog.d(TAG, "procVodDuration(" + d + ")");
		if (canProcMessage()) {
			postUI(new Runnable() {
				public void run() {
					VodPlayBaseInterface.Callback cb = callback;
					if (cb != null)
						cb.onVodDuration(d);
				}
			});
		}
	}

	void procSourceSeek(long t) {
		IPanelLog.d(TAG, "procSourceSeek t=" + t);
		if (canProcMessage()) {
			VodPlaySource s = host.getVodSource();
			if (s == null)
				return;
			if (seeking)
				seeking = false;
			if (seekpos != null) {
				s.seek(seekpos);
				seeking = true;
				seekpos = null;
			}
		}
	}

	void procSyncMediaTime(final long t) {
		IPanelLog.d(TAG, "procSyncShiftMediaTime(" + t + ")");
		if (canProcMessage()) {
			postUI(new Runnable() {
				public void run() {
					VodPlayBaseInterface.Callback cb = callback;
					if (cb != null)
						cb.onSyncMediaTime(t);
				}
			});
		}
	}

	void procPlayError(final String msg) {
		IPanelLog.d(TAG, "procPlayError(" + msg + ")");
		if (canProcMessage()) {
			postUI(new Runnable() {
				public void run() {
					VodPlayBaseInterface.Callback cb = callback;
					if (cb != null)
						cb.onPlayError(msg);
				}
			});
		}
	}

	void notifyPlayContextReady(final String n) {
		postUI(new Runnable() {
			public void run() {
				VodPlayBaseInterface.Callback cb = callback;
				if (cb != null)
					cb.onContextReady(n);
			}
		});
	}
}

class VodPlaySource extends TsPlayerInetSource {
	VodPlayManager manager;

	public VodPlaySource(VodPlayManager manager) {
		super(manager.host.getActivity(), manager.host.getSourceServiceName(), Provider.Sihua
				.getName());
		this.manager = manager;
	}

	@Override
	public void onResponseStart(final boolean b) {
		super.onResponseStart(b);
		manager.postProc(new Runnable() {
			@Override
			public void run() {
				manager.procResponseStart(b);
			}
		});
	}

	@Override
	public void onSourceError(final String msg) {
		super.onSourceError(msg);
		manager.postProc(new Runnable() {
			@Override
			public void run() {
				manager.procSourceError(msg);
			}
		});
	}

	@Override
	public void onSourceMessage(final String msg) {
		super.onSourceMessage(msg);
		manager.postProc(new Runnable() {
			@Override
			public void run() {
				manager.procSourceMessage(msg);
			}
		});
	}

	@Override
	public void onEndOfSource(final float rate) {
		super.onEndOfSource(rate);
		manager.postProc(new Runnable() {
			@Override
			public void run() {
				manager.procEndOfSource(rate);
			}
		});
	}

	@Override
	public void onSourcePlayed(final String streamUri, final String programUri) {
		super.onSourcePlayed(streamUri, programUri);
		manager.postProc(new Runnable() {
			@Override
			public void run() {
				manager.procPlayPlayed(streamUri, programUri);
			}
		});
	}

	@Override
	public void onSeeBackPeriod(final long start, final long end) {
		super.onSeeBackPeriod(start, end);
		manager.postProc(new Runnable() {
			@Override
			public void run() {
				manager.procSeeBackPeriod(start, end);
			}
		});
	}

	@Override
	public void onSourceRate(final float r) {
		super.onSourceRate(r);
		manager.postProc(new Runnable() {
			@Override
			public void run() {
				manager.procSourceRate(r);
			}
		});
	}

	@Override
	public void onVodDuration(final long d) {
		super.onVodDuration(d);
		manager.postProc(new Runnable() {
			@Override
			public void run() {
				manager.procVodDuration(d);
			}
		});
	}

	@Override
	public void onSourceSeek(final long t) {
		super.onSourceSeek(t);
		manager.postProc(new Runnable() {
			@Override
			public void run() {
				manager.procSourceSeek(t);
			}
		});
	}

	@Override
	public void onSyncMediaTime(final long t) {
		super.onSyncMediaTime(t);
		manager.postProc(new Runnable() {
			@Override
			public void run() {
				manager.procSyncMediaTime(t);
			}
		});
	}
}

class IpQamPlayer extends IpQamTsPlayer {
	VodPlayManager manager;

	public IpQamPlayer(VodPlayManager manager) {
		super(manager.host.getActivity(), manager.host.getPlayServiceName());
		this.manager = manager;
	}

	@Override
	public void onPlayError(final String msg) {
		super.onPlayError(msg);
		manager.postProc(new Runnable() {
			@Override
			public void run() {
				manager.procPlayError(msg);
			}
		});
	}

	@Override
	public void onResponseStart(boolean succ) {
		// TODO Auto-generated method stub
		super.onResponseStart(succ);
	}
}

class LocalSockPlayer extends LocalSockTsPlayer {
	VodPlayManager manager;

	public LocalSockPlayer(VodPlayManager manager) {
		super(manager.host.getActivity(), manager.host.getPlayServiceName());
		this.manager = manager;
	}

	@Override
	public void onPlayError(final String msg) {
		super.onPlayError(msg);
		manager.postProc(new Runnable() {
			@Override
			public void run() {
				manager.procPlayError(msg);
			}
		});
	}

	@Override
	public void onResponseStart(boolean succ) {
		// TODO Auto-generated method stub
		super.onResponseStart(succ);
	}
}
