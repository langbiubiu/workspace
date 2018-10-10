package ipaneltv.toolkit.mediaservice;

import ipaneltv.toolkit.IPanelLog;
import ipaneltv.toolkit.JsonParcelable;
import ipaneltv.toolkit.db.DatabaseObjectification.ChannelKey;
import ipaneltv.toolkit.media.MediaSessionInterface.HomedProgramPlayerInterface;
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
import android.util.Log;

public class HomedPlayerContext<T extends MediaPlaySessionService> extends
		TeeveePlayerBaseContext<T> implements HomedProgramPlayerInterface {
	abstract class CB implements HomedProgramPlayerInterface.Callback {
	};

	private static final String TAG = HomedPlayerContext.class.getSimpleName();
	protected final Object mutex = new Object();
	protected final Object homedMutex = new Object();
	protected ResourcesState mPlayResource;

	protected PlayWidgetControl mWidgetHandle;
	protected ProgramDescrambler mDescrambler;
	private boolean contextReady = false;
	protected LiveDataManager mLiveData;
	private HandlerThread procThread = new HandlerThread("pipthread");
	Handler procHandler;
	private HandlerThread dvbThread = new HandlerThread("dvbthread");
	CAManager caManager;
	Handler procDvbHandler;
	Rect rect = new Rect();
	MyFileObserver mObserver;
	boolean cardVerified = false;

	public HomedPlayerContext(T service) {
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
		dvbThread.start();
		procDvbHandler = new Handler(dvbThread.getLooper());
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
		// IntentFilter filter = new IntentFilter();
		// filter.addAction(ConnectivityManager.CONNECTIVITY_ACTION);
		// getSessionService().getApp().registerReceiver(receiver, filter);
		mObserver = new MyFileObserver("mnt/network.txt");

		mObserver.startWatching();
		int i = getNetworkState();
		Log.d(TAG, "onCreate2 i = " + i);
		if (i == 48) {
			network = false;
			Log.d(TAG, "onCreate 2222");
		} else {
			network = true;
			Log.d(TAG, "onCreate 3333");
		}
	}

	private boolean network = false;

	public class MyFileObserver extends FileObserver {

		/**
		 * path 是所监听的文件夹或者文件名。
		 */

		public MyFileObserver(String path) {
			super(path);

		}

		@Override
		public void onEvent(int event, String path) {
			Log.d(TAG, "onEvent event = " + event + ";path = " + path + ";network = " + network);
			switch (event) {
			case android.os.FileObserver.CLOSE_WRITE:
				int i = getNetworkState();
				// 编辑完文件，关闭

				/**
				 * 
				 * 相关操作
				 */
				if (i == 48 && network) {
					network = false;
					if (mPlayResource.isReserved()) {
						postProcDvb(new DvbRunable(selection.cfreq, selection.cpn, dvbCount) {

							@Override
							public void run() {
								IPanelLog.d(TAG, "start f = " + f + ";pn = " + pn
										+ ";selection.cfreq = " + selection.cfreq + ";pn = "
										+ selection.cpn + "+ pn" + selection.cpn);
								if (f == selection.cfreq && pn == selection.cpn) {
									select2(f, selection.cfflags, pn, selection.pflags);
								}
							}
						});
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
			Log.d(TAG, "onCardPresent readerIndex = " + readerIndex);
		}

		@Override
		public void onCardAbsent(int readerIndex) {
			Log.d(TAG, "onCardAbsent readerIndex 2 = " + readerIndex + ";shifted = " + shifted);
			cardVerified = false;
		}

		@Override
		public void onCardMuted(int readerIndex) {
			Log.d(TAG, "onCardMuted readerIndex = " + readerIndex);
			cardVerified = false;
		}

		@Override
		public void onCardReady(int readerIndex) {
			Log.d(TAG, "onCardReady readerIndex = " + readerIndex);

		}

		@Override
		public void onCardVerified(int readerIndex, int moduleID) {
			Log.d(TAG, "onCardVerified 2 moduleID = " + moduleID + ";shifted = " + shifted);
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
				Log.d(TAG, "onEvent b[0] = " + b[0]);
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		} catch (FileNotFoundException e) {
			Log.e(TAG, "getNetworkState e = " + e.getMessage());
		} finally {
			if (fis != null) {
				try {
					fis.close();
					fis = null;
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
		}
		return b[0];
	}

	// private BroadcastReceiver receiver = new BroadcastReceiver() {
	// @Override
	// public void onReceive(final Context context, Intent intent) {
	// String action = intent.getAction();
	// Log.i(TAG, "BroadcastReceiver：" + action);
	// synchronized (mutex) {
	// if (action.equals(ConnectivityManager.CONNECTIVITY_ACTION)) {
	// if (!intent.getBooleanExtra(ConnectivityManager.EXTRA_NO_CONNECTIVITY,
	// false)) {
	// Log.i(TAG, "network connected!");
	// NetworkInfo info = (NetworkInfo) intent.getExtras().get(
	// ConnectivityManager.EXTRA_NETWORK_INFO);
	// if (info != null) {
	// Log.i(TAG, "BroadcastReceiver:" + info.toString());
	// if (info.getType() == ConnectivityManager.TYPE_WIFI) {
	// } else if (info.getType() == ConnectivityManager.TYPE_ETHERNET) {
	//
	// } else if (info.getTypeName().equalsIgnoreCase("pppoe")) {
	//
	// } else {
	//
	// }
	// }
	// network = true;
	// } else {
	// Log.i(TAG, "netWork has lost");
	// network = false;
	// }
	// }
	// }
	// }
	// };

	final void postProcDvb(Runnable r) {
		procDvbHandler.post(r);
	}

	final void postProcDvbFront(Runnable r) {
		procDvbHandler.postAtFrontOfQueue(r);
	}

	final void postDelayedDvb(Runnable r, long l) {
		procDvbHandler.postDelayed(r, l);
	}

	final void postProcFront(Runnable r) {
		procHandler.postAtFrontOfQueue(r);
	}

	final void postProc(Runnable r) {
		procHandler.post(r);
	}

	final void postDelayed(Runnable r, long l) {
		procHandler.postDelayed(r, l);
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
		procThread.getLooper().quit();
		dvbThread.getLooper().quit();
		// getSessionService().getApp().unregisterReceiver(receiver);
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
		if(clearState && isRelease()){
			mPlayResource.destroy();
		}else{
			mPlayResource.loosen(clearState);	
		}
		mPlayResource.closeHomedPipPlayer(clearState);
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
	 * 客户端放开资源控制，除非因别的客户端请求资源而发生抢占,服务端对资源尽量保留
	 */
	@Override
	public void loosen(boolean clearState) {
		IPanelLog.d(TAG, "before loosen");
		synchronized (mutex) {
			if (contextReady) {
				IPanelLog.d(TAG, "onLoosen 22(clearState=" + clearState + ")");
				contextReady = false;
				mWidgetHandle.clearWidgetMessage();
				if(!clearState){
					select2(selection.cfreq, selection.fflags | 0x10000, selection.cpn, 0);
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
		postProcDvb(new Runnable() {
			public void run() {
				IPanelLog.d(TAG, "before stop");
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
		});
	}

	@Override
	public void pause() {
		postProc(new Runnable() {
			public void run() {
				IPanelLog.d(TAG, "before pause");
				synchronized (mutex) {
					if (mPlayResource.isHomedPipPlayerOpened()) {
						mPlayResource.homedPause();
					}
				}
				IPanelLog.d(TAG, "end pause");
			}
		});
	}

	@Override
	public void resume() {
		postProc(new Runnable() {
			public void run() {
				IPanelLog.d(TAG, "before resume");
				synchronized (mutex) {
					if (mPlayResource.isHomedPipPlayerOpened()) {
						mPlayResource.homedResume();
					}
				}
				IPanelLog.d(TAG, "end resume");
			}
		});
	}

	@Override
	public final void setVolume(final float v) {
		postProcDvb(new Runnable() {
			public void run() {
				IPanelLog.d(TAG, "before setVolume v = " + v);
				synchronized (mutex) {
					if (mPlayResource.isReserved() && volumeSelect != v) {
						volumeSelect = v < 0f ? 0f : v > 1f ? 1f : v;
						IPanelLog.d(TAG, "setVolume v= " + v + ";selection.cfreq = "
								+ selection.cfreq + ";selection.cdvbfreq = " + selection.cdvbfreq
								+ ";selection.cpn = " + selection.cpn + ";selection.cdvbpn = "
								+ selection.cdvbpn + ";shifted = " + shifted);
						synchronized (shiftMutex) {
							if (selection.cfreq == selection.cdvbfreq
									&& selection.cpn == selection.cdvbpn && selection.cdvbfreq != 0
									&& selection.cdvbpn != 0 && !shifted) {
								setVolume2(v);
							} else {
								mPlayResource.homedSetVolume(v);
							}
						}
					}
				}
				IPanelLog.d(TAG, "end setVolume");
			}
		});
	}

	public final void setVolume2(final float v) {
		IPanelLog.d(TAG, "before setVolume2 v = " + v);
		synchronized (mutex) {
			if (mPlayResource.isReserved()) {
				mPlayResource.getPlayer().setVolume(v);
			}
		}
		IPanelLog.d(TAG, "end setVolume2");
	}

	@Override
	public final void setDisplay(final int x, final int y, final int w, final int h) {
		postProcDvb(new Runnable() {
			public void run() {
				IPanelLog.d(TAG, "before setDisplay 11");
				synchronized (mutex) {
					if (mPlayResource.isReserved()) {
						// mPlayResource.getPlayer().setDisplay(x, y, w, h);
						rect.left = x;
						rect.top = y;
						rect.right = w;
						rect.bottom = h;
					}
				}
				IPanelLog.d(TAG, "end setDisplay");
			}
		});
	}

	public final void setDisplay2(final int x, final int y, final int w, final int h) {
		IPanelLog.d(TAG, "before setDisplay 11");
		synchronized (mutex) {
			if (mPlayResource.isReserved()) {
				mPlayResource.getPlayer().setDisplay(x, y, w, h);
			}
		}
		IPanelLog.d(TAG, "end setDisplay");
	}

	@Override
	public final void syncSignalStatus() {
		postProcDvb(new Runnable() {
			public void run() {
				IPanelLog.d(TAG, "before syncSignalStatus");
				synchronized (mutex) {
					if (mPlayResource.isReserved()) {
						SignalStatus ss = mPlayResource.getSelector().getSignalStatus();
						notifyJson(CB.__ID_onSyncSignalStatus, ss.toString());
					}
				}
				IPanelLog.d(TAG, "end syncSignalStatus");
			}
		});
	}

	@Override
	public final void solveProblem() {
		postProcDvb(new Runnable() {
			public void run() {
				IPanelLog.d(TAG, "before solveProblem");
				synchronized (mutex) {
					mDescrambler.solveProblem();
				}
				IPanelLog.d(TAG, "end solveProblem");
			}
		});
	}

	@Override
	public final void enterCaApp(final String uri) {
		postProcDvb(new Runnable() {
			public void run() {
				IPanelLog.d(TAG, "before enterCaApp");
				synchronized (mutex) {
					mDescrambler.enterCaApp(uri);
				}
				IPanelLog.d(TAG, "end enterCaApp");
			}
		});
	}

	@Override
	public void setTeeveeWidget(final int flags) {
		postProcDvb(new Runnable() {
			public void run() {
				IPanelLog.d(TAG, "before setTeeveeWidget");
				synchronized (mutex) {
					mWidgetHandle.setTeeveeWidget(flags);
				}
				IPanelLog.d(TAG, "end setTeeveeWidget");
			}
		});
	}

	@Override
	public void checkTeeveeWidget(final int flags) {
		postProcDvb(new Runnable() {
			public void run() {
				IPanelLog.d(TAG, "before checkTeeveeWidget");
				synchronized (mutex) {
					mWidgetHandle.checkTeeveeWidget(flags);
				}
				IPanelLog.d(TAG, "end ceckTeeveeWidget");
			}
		});
	}

	@Override
	public void setProgramFlags(final int flags) {
		postProcDvb(new Runnable() {
			public void run() {
				IPanelLog.d(TAG, "before setProgramFlags");
				synchronized (mutex) {
					if (suspend) {
						IPanelLog.w(TAG, "is suspend, start first!");
						return;
					}
					selection.trackFlags = flags;
					String uri = selection.puri;
					if (uri != null) {
						ProgramInfo fi = ProgramInfo.fromString(uri);
						mPlayResource.getPlayer().selectProgram(fi, flags | selection.pflags);
					}
				}
				IPanelLog.d(TAG, "end setProgramFlags");
			}
		});
	}

	@Override
	public void syncMediaTime() {
		postProcDvb(new Runnable() {
			public void run() {
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
		});
	}

	@Override
	public void observeProgramGuide(final ChannelKey ch, final long focusTime) {
		postProcDvb(new Runnable() {
			public void run() {
				IPanelLog.d(TAG, "before observeProgramGuide");
				synchronized (mutex) {
					mLiveData.observeProgramGuide(ch, focusTime);
				}
				IPanelLog.d(TAG, "end observeProgramGuide");
			}
		});
	}

	private final void notifyWidgetSwitchEnd(String err) {
		int code = err == null ? 0 : L10n.getErrorCode(err, 100000);
		mWidgetHandle.notifySwitchingEnd(code, err);
	}

	int dvbCount = 0;

	@Override
	public void select(final long freq, final int fflags, final int pn, final int pflags) {
		IPanelLog.d(TAG, "before select 4 freq = " + freq + "fflags = " + fflags + ";pn = " + pn
				+ ";network = " + network + ";cardVerified = " + cardVerified);
		postProcDvbFront(new Runnable() {

			@Override
			public void run() {
				try {
					if (freq == 0 && pn == 0) {
						return;
					}
					int time = 0;
					if (network && !((fflags & 0x10000) == 0x10000)
							&& !((fflags & 0x8000) == 0x8000) && cardVerified) {
						time = 7000;
					}
					dvbCount++;
					mWidgetHandle.clearWidgetMessage(fflags);
					IPanelLog.d(TAG, "before select dvbCount = " + dvbCount + ";time = " + time);
					selection.cfset(freq, fflags, pn, pflags);
					postDelayedDvb(new Runnable() {

						@Override
						public void run() {
							select2(freq, fflags, pn, pflags);
						}
					}, time);
				} catch (Exception e) {
					// TODO: handle exception
				} finally {
					notifyJson(CB.__ID_onResponseSelect, true + "");
				}
			}
		});
	}

	public void select2(final long freq, final int fflags, final int pn, final int pflags) {
		FrequencyInfo fi = null;
		ProgramInfo pi = null;
		ChannelKey key = ChannelKey.obten(freq, pn);
		IPanelLog.d(TAG, "before select2 3 freq = " + freq + ";pn = " + pn + ";selection.cfreq = "
				+ selection.cfreq + ";selection.cpn = " + selection.cpn + ";selection.cdvbfreq = "
				+ selection.cdvbfreq + ";selection.cdvbpn = " + selection.cdvbpn);
		synchronized (mutex) {
			synchronized (shiftMutex) {
				IPanelLog.d(TAG, "select2 shifted = " + shifted);
				if (!shifted) {
					String err = null;
					boolean done = false;
					boolean notFound = false;
					try {
						if (freq == 0 && pn == 0) {
							return;
						}
						if (selection.cfreq == selection.cdvbfreq && selection.cdvbfreq == freq
								&& selection.cpn == selection.cdvbpn && selection.cdvbpn == pn) {
							done = true;
							return;
						}
						if (freq != selection.cfreq || pn != selection.cpn) {
							done = true;
							return;
						}
						fi = mLiveData.getFrequencyInfo(freq);
						pi = mLiveData.getProgramInfo(key);
						if (fi != null) {
							onSelect(fi, fflags, pi, pflags);
							monitorProgramStream(freq, pn);
							if (!network || (fflags & 0x10000) == 0x10000) {
								setDisplay2(rect.left, rect.top, rect.right, rect.bottom);
								setVolume2(volumeSelect);
								selection.cdvbfreq = selection.cfreq;
								selection.cdvbpn = selection.cpn;
								if (pi.getVideoPID() == -1) {
									mPlayResource.homedStop(0);
								} else {
									mPlayResource.homedStop(1);
								}
								JSONObject object = new JSONObject();
								object.put("f", selection.cfreq);
								object.put("pn", selection.cpn);
								notifyJson(CB.__ID_homed_onIpStoped, object.toString());
							} else {
								int time = 4000;
								if ((fflags & 0x8000) == 0x8000) {
									time = 1500;
								}
								postDelayedDvb(new DvbRunable(selection.cfreq, selection.cpn,
										dvbCount) {

									@Override
									public void run() {
										IPanelLog.d(TAG, "start f = " + f + ";pn = " + pn
												+ ";selection.cfreq = " + selection.cfreq
												+ ";pn = " + selection.cpn + "+ pn" + selection.cpn
												+ ";count = " + count + ";dvbCount = " + dvbCount
												+ ";shifted = " + shifted);
										try {
											if (f == selection.cfreq && pn == selection.cpn
													&& count == dvbCount) {
												synchronized (shiftMutex) {
													if (!shifted) {
														setDisplay2(rect.left, rect.top,
																rect.right, rect.bottom);
														setVolume2(volumeSelect);
														selection.cdvbfreq = selection.cfreq;
														selection.cdvbpn = selection.cpn;
														mPlayResource.homedStop(1);
														JSONObject object = new JSONObject();
														object.put("f", selection.cfreq);
														object.put("pn", selection.cpn);
														notifyJson(CB.__ID_homed_onIpStoped,
																object.toString());
													}
												}
											}
										} catch (Exception e) {
											Log.e(TAG, "start f e = " + e.toString());
										}
									}
								}, time);
							}
							done = true;
						} else {
							notFound = true;
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
							if (mPlayResource.isReserved()) {
								IPanelLog.d(TAG, "end select no program 111");
								stopPlayer(0);
								mPlayResource.homedStop(1);
								selection.cdvbfreq = -1;
								selection.cdvbpn = -1;
								// R.string.no_program
							}
						}
						if (notFound) {
							if (freq != 0 && pn != 0) {
								mWidgetHandle.notifyDecodeState(L10n.SELECT_ERR_433);
							}
						}
						// notifyJson(CB.__ID_onResponseSelect, done + "");
					}
				}
			}
		}
		IPanelLog.d(TAG, "end select4");
	}

	int topCount = 0;

	@Override
	public void start(final String furi, final int fflags, final String puri, final int pflags) {
		IPanelLog.d(TAG, "start furi = " + furi + "; puri = " + puri + ";fflags = " + fflags
				+ ";pflags = " + pflags + ";cNumber = " + cNumber);
		synchronized (homedMutex) {
			postProc(new MyRunable(topCount) {
				public void run() {
					IPanelLog.d(TAG, "start cNumber = " + cNumber + ";topCount = " + topCount
							+ ";getCount() = " + getCount() + "network" + network);
					if (getCount() >= topCount && mPlayResource.isReserved()) {
						IPanelLog.d(TAG, "start 11 cNumber = " + cNumber + ";shifted = " + shifted);
						synchronized (shiftMutex) {
							if (furi.startsWith("playerror") && !shifted) {
								postProcDvb(new DvbRunable(selection.cfreq, selection.cpn, dvbCount) {

									@Override
									public void run() {
										IPanelLog
												.d(TAG, "start f = " + f + ";pn = " + pn
														+ ";selection.cfreq = " + selection.cfreq
														+ ";pn = " + selection.cpn + "+ pn"
														+ selection.cpn);
										if (f == selection.cfreq && pn == selection.cpn) {
											select(f, fflags | 0x8000, pn, pflags);
										}
									}
								});
							}
						}
					}
				}
			});
		}
	}

	String info = "program://1?audio_stream_pid=1002&audio_stream_type=audio_aac&video_stream_pid=1001&video_stream_type=video_h264&ca_required=false";
	Object shiftMutex = new Object();
	boolean shifted = false;

	@Override
	public void startFd(final long vfreq, ParcelFileDescriptor pfd, final int fflags) {
		IPanelLog.d(TAG, "startFd pfd = " + pfd + ";vfreq = " + vfreq);
		synchronized (homedMutex) {
			topCount++;
			int descriptor = -1;
			if (pfd != null) {
				descriptor = pfd.detachFd();
			}
			final int fd = descriptor;
			IPanelLog.d(TAG, "startFd fd = " + fd);
			postProc(new MyRunable(topCount) {
				public void run() {
					IPanelLog.d(TAG, "startFd 3 cNumber = " + cNumber + ";topCount = " + topCount
							+ ";getCount() = " + getCount() + "network = " + network
							+ ";cardVerified = " + cardVerified);
					synchronized (shiftMutex) {
						shifted = false;
						if (getCount() >= topCount && mPlayResource.isReserved()) {
							IPanelLog.d(TAG, "startFd 11 cNumber = " + cNumber);
							if (vfreq != -1) {
								ParcelFileDescriptor ppfd = null;
								ppfd = ParcelFileDescriptor.adoptFd(fd);
								IPanelLog.d(TAG, "startFd 3333 ppfd = " + ppfd);
								if (mPlayResource.openHomedPipPlayer()) {
									ipPlay(vfreq, ppfd, fflags, 1 | 0x20000);

									if (network) {
										synchronized (homedMutex) {
											Log.d(TAG, "startFd getCount() = " + getCount()
													+ ";topCount = " + topCount);
											if (getCount() == topCount) {
												selection.cdvbfreq = 0;
												selection.cdvbpn = 0;
												mPlayResource.getPlayer().setVolume(0);
												mPlayResource.homedSetDisplay(rect.left, rect.top,
														rect.right, rect.bottom);
												mPlayResource.homedSetVolume(volumeSelect);
											}
										}
									}
								}
							}
						}
						IPanelLog.d(TAG, "startFd end 1");
					}
				}
			});
		}
	}

	public void startShift(final long vfreq, ParcelFileDescriptor pfd, final int fflags) {
		IPanelLog.d(TAG, "startShift pfd = " + pfd + ";vfreq = " + vfreq);
		synchronized (homedMutex) {
			topCount++;
			if (vfreq != -1) {
				final int fd = pfd.detachFd();
				IPanelLog.d(TAG, "startShift fd = " + fd);
				postProc(new MyRunable(topCount) {
					public void run() {
						IPanelLog.d(TAG, "startShift 3 cNumber = " + cNumber + ";topCount = "
								+ topCount + ";getCount() = " + getCount() + "network = " + network
								+ ";cardVerified = " + cardVerified);
						try {
							if (getCount() >= topCount && mPlayResource.isReserved()) {
								IPanelLog.d(TAG, "startShift 11 cNumber = " + cNumber);
								synchronized (shiftMutex) {
									shifted = true;
									ParcelFileDescriptor ppfd = ParcelFileDescriptor.adoptFd(fd);
									IPanelLog.d(TAG, "startShift 3333 ppfd = " + ppfd);
									if (mPlayResource.openHomedPipPlayer()) {
										ipPlay(vfreq, ppfd, fflags, 1);
										synchronized (homedMutex) {
											Log.d(TAG, "startShift getCount() = " + getCount()
													+ ";topCount = " + topCount);
											if (getCount() == topCount) {
												mWidgetHandle.clearWidgetMessage(fflags);
												selection.cdvbfreq = 0;
												selection.cdvbpn = 0;
												mPlayResource.getPlayer().setVolume(0);
												mPlayResource.homedSetDisplay(rect.left, rect.top,
														rect.right, rect.bottom);
												mPlayResource.homedSetVolume(volumeSelect);
											}
										}
									}
								}
								IPanelLog.d(TAG, "startShift end 1");
							}
						} catch (Exception e) {
							// TODO: handle exception
						} finally {
							notifyJson(CB.__ID_homed_onResponseStart, true + "");
							IPanelLog.d(TAG, "startShift onProgramFoundPlay __ID_onResponseStart");
						}

					}
				});
			}
		}
	}

	@Override
	public void redirect(final long vfreq, ParcelFileDescriptor pfd, final int flags) {
		Log.d(TAG, "redirect 22");
		synchronized (homedMutex) {
			final int fd = pfd.detachFd();
			postProc(new Runnable() {

				@Override
				public void run() {
					boolean succ = false;
					try {
						ParcelFileDescriptor ppfd = ParcelFileDescriptor.adoptFd(fd);
						Log.d(TAG, "redirect 11");
						if (mPlayResource.isHomedPipPlayerOpened()) {
							ipPlay(vfreq, ppfd, flags, 1);
						}
					} catch (Exception e) {
						// TODO: handle exception
					} finally {
						IPanelLog.d(TAG, "redirect  succ=" + succ);
					}
				}
			});
		}
	}

	protected void ipPlay(long vfreq, ParcelFileDescriptor ppfd, int fflags, int pflags) {
		ipPlayNormal(vfreq, ppfd, fflags, pflags);
	}

	protected void ipPlayNormal(long vfreq, ParcelFileDescriptor ppfd, int fflags, int pflags) {
		mPlayResource.homedSelectFd(vfreq, ppfd.getFileDescriptor(), fflags);
		ProgramInfo pinfo = ProgramInfo.fromString(info);
		mPlayResource.homedstart(null, fflags, pinfo, pflags);
	}

	protected void ipPlayQuick(long vfreq, ParcelFileDescriptor ppfd, int fflags, int pflags) {
		mPlayResource.homedSelectFdQuick(vfreq, ppfd.getFileDescriptor(), fflags);
		ProgramInfo pinfo = ProgramInfo.fromString(info);
		mPlayResource.homedstartQuick(null, fflags, pinfo, pflags);
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

	abstract class MyRunable implements Runnable {
		int count;

		public MyRunable(int count) {
			this.count = count;
		}

		public synchronized int getCount() {
			return count;
		}
	}

	abstract class DvbRunable implements Runnable {
		public long f;
		public int pn;
		public int count;

		public DvbRunable(long f, int pn, int count) {
			this.f = f;
			this.pn = pn;
			this.count = count;
		}
	}

	@Override
	public void select(final String furi, final int fflags, final String puri, final int pflags) {
		postProcDvb(new Runnable() {
			public void run() {
				try {
					IPanelLog.d(TAG, "before select with furi 4");
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
								int pn = mLiveData.getProgramNum(fi.getFrequency(),
										pi.getAudioPID());
								Log.d(TAG, "select pn = " + pn);
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
					}
					IPanelLog.d(TAG, "end select with furi");
				} catch (Exception e) {
					String err = "error:" + e.getMessage();
					notifyError(err);
					notifyWidgetSwitchEnd(err);
				}
			}
		});
	}

	@Override
	public long getPlayTime() {
		return -1;
	}

	@Override
	public void captureVideoFrame(final int id) {
		postProcDvb(new Runnable() {
			public void run() {
				synchronized (mutex) {
					mPlayResource.getPlayer().captureVideoFrame(id);
				}
			}
		});
	}

	// =====================
	Selection selection = new Selection();
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
		if (mPlayResource.getPlayer().selectProgram(pi, flags | selection.trackFlags)) {
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
			selection.reselect(0x8000);
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

	private void reselect(final int flags) {
		postDelayedDvb(new Runnable() {

			@Override
			public void run() {
				stopPlayer(0);
				selection.cdvbfreq = -1;
				selection.cdvbpn = -1;
				selection.reselect(flags);
			}
		}, 0);
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
		IPanelLog.d(TAG, "descramblingState 22 err code = " + code + "err = " + err + ";shifted = "
				+ shifted);
		synchronized (shiftMutex) {
			if (!shifted) {
				mWidgetHandle.notifyDescramblingState(code, err);
				if (err == null) {
					reselectProgram();
					notifyJson(CB.__ID_onDescramError, true + "");
				} else if (code == 452) {
					mPlayResource.homedStop(0);
					selection.cdvbfreq = -1;
					selection.cdvbpn = -1;
					stopPlayer(TeeveePlayer.FLAG_VIDEO_FRAME_BLACK);
					// reselectProgram();
					notifyJson(CB.__ID_onDescramError, false + "");
				} else if (code != 821 && code != 822) {
					notifyJson(CB.__ID_onDescramError, false + "");
					mPlayResource.homedStop(0);
					stopPlayer(TeeveePlayer.FLAG_VIDEO_FRAME_BLACK);
					selection.cdvbfreq = -1;
					selection.cdvbpn = -1;
				}
			}
		}
	}

	class Selection {
		public final String FSCHEME = "frequency://";
		private String furi = null, puri = null;
		@SuppressWarnings("unused")
		private int fflags = 0, pflags = 0;
		private long freq = 0;
		private int pn = 0;

		private long cfreq = 0;
		public int cpn = 0, cfflags = 0, cpflags = 0;
		private long cdvbfreq = 0;
		public int cdvbpn = 0;
		private int trackFlags = 0;

		void clearUri() {
			puri = furi = null;
			pflags = fflags = 0;
			cfreq = cdvbfreq = 0;
			cpn = cdvbpn = 0;
			cfflags = cpflags = 0;

		}

		void cfset(long f, int fflags, int pn, int pflags) {
			cfreq = f;
			cpn = pn;
			cfflags = fflags;
			cpflags = pflags;
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

		void reselect(final int flags) {
			postDelayedDvb(new Runnable() {

				@Override
				public void run() {
					select2(cfreq, fflags | flags, cpn, pflags);
				}
			}, 0);
		}

		void reselectProgram() {
			IPanelLog.d(TAG, "Selection reselectProgram puri = " + puri);
			if (puri != null) {
				onSelectProgram(puri, pflags);
			}
		}

		boolean preselect(int program, String npuri) {
			IPanelLog.d(TAG, "preselect program = " + program + "; npuri = " + npuri + ";cpn = "
					+ cpn);
			if (cpn == program) {
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
			IPanelLog.d(TAG, "onSelectFailed");
			reselect(0x8000);
			IPanelLog.d(TAG, "onSelectFailed 22 shifted = " + shifted);
			synchronized (shiftMutex) {
				if (!shifted) {
					String err = L10n.TRANSPORT_ERR_401;
					mWidgetHandle.notifyTransportState(err);
					mPlayResource.homedStop(0);
					IPanelLog.d(TAG, "onSelectFailed 22");
					notifyError(err);
					IPanelLog.d(TAG, "onSelectFailed 33");
				}
			}
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
			IPanelLog.d(TAG, "onSelectionLost 33 shifted = " + shifted);
			postDelayedDvb(new Runnable() {
				@Override
				public void run() {
					stopPlayer(0);
					selection.cdvbfreq = -1;
					selection.cdvbpn = -1;
					synchronized (shiftMutex) {
						if (!shifted) {
							mPlayResource.homedStop(0);
							String err = L10n.TRANSPORT_ERR_402;
							mWidgetHandle.notifyTransportState(err);
						}
					}
				}
			}, 0);
			notifyJson(CB.__ID_onStreamLost);
		}

		@Override
		public void onSelectionResumed(StreamSelector s) {
			IPanelLog.d(TAG, "onSelectionResumed 11");
			selection.reselect(0x8000);
			mWidgetHandle.notifyTransportState(null);
			notifyJson(CB.__ID_onStreamResumed);
		}
	};

	private int cNumber = 0;

	PlayStateListener homedplayStateListener = new PlayStateListener() {

		@Override
		public void onSelectionStart(TeeveePlayer player, int program_number) {/*-ignored ,ResourceScheduler done */
			IPanelLog.d(TAG, "hhh onSelectionStart");
		}

		@Override
		public void onPlayProcessing(int program_number) {
			// IPanelLog.d(TAG, "onPlayProcessing: hhh program_number = " +
			// program_number);
			// postProc(new Runnable() {
			// public void run() {
			// if (mPlayResource.isReserved()) {
			// IPanelLog.d(TAG, "postProc: 111 cNumber = " + cNumber);
			// if (cNumber == 0&&!ipStarted) {
			// ipStarted = true;
			// IPanelLog.d(TAG, "postProc: 222");
			// mPlayResource.homedSetDisplay(rect.left, rect.top, rect.right,
			// rect.bottom);
			// IPanelLog.d(TAG, "postProc: 333");
			// }
			// }
			// }
			// });
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
			// try {
			// Log.d(TAG, "onPlayerPTSChange program_number = " + program_number
			// + ";process_pts_time = " + process_pts_time);
			// postDelayed(new Runnable() {
			// public void run() {
			// Log.d(TAG, " postProc: contextReady = " + contextReady
			// + "cNumber:program_number = " + cNumber + ":" + program_number
			// + ";selection.cpn = " + selection.cpn);
			// if (contextReady && cNumber != program_number && program_number
			// == selection.cpn) {
			// cNumber = program_number;
			// if (mPlayResource.isReserved()) {
			// IPanelLog.d(TAG, "postProc: 4444 cNumber = " + cNumber);
			// setVolume2();
			// setDisplay2(rect.left, rect.top, rect.right, rect.bottom);
			// IPanelLog.d(TAG, "postProc: 555");
			// }
			// }
			// }
			// }, 2000);
			//
			// } catch (Exception e) {
			// Log.e(TAG, "onPlayProcessing failed e=" + e);
			// e.printStackTrace();
			// }
			// Log.d("onPlayerPTSChange:", "end");
		}

		@Override
		public void onPlayProcess(int program_number, long process_pts_time) {
			try {
				Log.d(TAG, "onPlayProcessing program_number = " + program_number);
				// JSONStringer str = new JSONStringer();
				// str.object();
				// str.key("pn").value(program_number);
				// str.key("pts_time").value(process_pts_time);
				// str.endObject();
				// notifyJson(CB.__ID_onPlayProcessing, str.toString());
			} catch (Exception e) {
				Log.e(TAG, "onPlayProcessing failed e=" + e);
				e.printStackTrace();
			}
		}

		@Override
		public void onPlaySuspend(int program_number) {
			Log.d(TAG, "onPlaySuspending program_number=" + program_number);
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
					+ "selection.pn = " + selection.pn + "selection.cfreq = " + selection.cfreq
					+ "selection.cpn = " + selection.cpn);
			postProcDvb(new Runnable() {

				@Override
				public void run() {
					if (selection.freq == selection.cfreq && selection.pn == selection.cpn) {
						descramblingState(code, err);
					}
				}
			});
		}

		@Override
		public void onCaCardState(int code, String msg) {
			Log.d(TAG, "onCaCardState msg = " + msg + ";shifted = " + shifted);
			if (msg != null) {
				synchronized (shiftMutex) {
					if (!shifted) {
						mPlayResource.homedStop(0);
						stopPlayer(0);
					}
				}
			} else {
				Log.d(TAG, "onCaCardState selection.cfreq = " + selection.cfreq
						+ ";selection.cpn = " + selection.cpn);
				postProcDvb(new DvbRunable(selection.cfreq, selection.cpn, dvbCount) {

					@Override
					public void run() {
						IPanelLog.d(TAG, "start f = " + f + ";pn = " + pn + ";selection.cfreq = "
								+ selection.cfreq + ";pn = " + selection.cpn + "+ pn"
								+ selection.cpn);
						if (f == selection.cfreq && pn == selection.cpn) {
							select(f, selection.cfflags | 0x8000, pn, selection.pflags);
						}
					}
				});
			}
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
			JSONObject o = (JSONObject) new JSONTokener(json).nextValue();
			select(o.getLong("freq"), o.getInt("fflags"), o.getInt("program"), o.getInt("pflags"));
			break;
		}
		case __ID_homed_start: {
			JSONObject o = (JSONObject) new JSONTokener(json).nextValue();
			start(o.getString("furi"), o.getInt("fflags"), o.getString("puri"), o.getInt("pflags"));
			break;
		}
		case __ID_homed_pfd_start: {
			ParcelFileDescriptor pfd = null;
			Parcelable parcelable = p.getParcelable("pfd");
			if (parcelable != null) {
				pfd = (ParcelFileDescriptor) parcelable;
			}
			Log.d(TAG, "__ID_homed_pfd_start pfd = " + pfd);
			JSONObject o = (JSONObject) new JSONTokener(json).nextValue();
			startFd(o.getLong("vfreq"), pfd, o.getInt("fflags"));
			break;
		}
		case __ID_homed_startShift: {
			ParcelFileDescriptor pfd = null;
			Parcelable parcelable = p.getParcelable("pfd");
			if (parcelable != null) {
				pfd = (ParcelFileDescriptor) parcelable;
			}
			Log.d(TAG, "__ID_homed_pfd_start pfd = " + pfd);
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
			Log.d(TAG, "__ID_homed_pfd_start pfd = " + pfd);
			JSONObject o = (JSONObject) new JSONTokener(json).nextValue();
			redirect(o.getLong("vfreq"), pfd, o.getInt("fflags"));
			break;
		}

		case __ID_homed_select_2: {
			JSONObject o = (JSONObject) new JSONTokener(json).nextValue();
			select(o.getString("furi"), o.getInt("fflags"), o.getString("puri"), o.getInt("pflags"));
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

	private ProgramInfo getProgramUri(String str) {
		Log.d(TAG, "getProgramUri str = " + str);
		Uri uri = Uri.parse(str);
		String apid = uri.getQueryParameter("apid");
		String vpid = uri.getQueryParameter("vpid");
		String adec = uri.getQueryParameter("adec");
		String vdec = uri.getQueryParameter("vdec");
		Log.d(TAG, "getProgramUri apid:vpid:adec:vdec = " + apid + ":" + vpid + ":" + adec + ":"
				+ vdec);
		ProgramInfo pinfo = new ProgramInfo();
		pinfo.setProgramNumber(1);
		pinfo.setAudioPID(Integer.valueOf(apid));
		pinfo.setAudioStreamType(getStreamType(Integer.valueOf(adec)));
		pinfo.setVideoPID(Integer.valueOf(vpid));
		pinfo.setVideoStreamType(getStreamType(Integer.valueOf(vdec)));
		return pinfo;
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

	@Override
	public void checkPassword(String pwd) {
		// TODO Auto-generated method stub

	}

	@Override
	public void clearCache(int flags) {
		// TODO Auto-generated method stub

	}
}