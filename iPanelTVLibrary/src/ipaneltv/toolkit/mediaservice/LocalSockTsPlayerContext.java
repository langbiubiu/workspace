package ipaneltv.toolkit.mediaservice;

import ipaneltv.toolkit.IPanelLog;
import ipaneltv.toolkit.JsonParcelable;
import ipaneltv.toolkit.ProgramInfoFilter;
import ipaneltv.toolkit.UriToolkit;
import ipaneltv.toolkit.media.MediaSessionInterface.LocalSockTsPlayerInterface;
import ipaneltv.toolkit.mediaservice.components.L10n;
import ipaneltv.toolkit.mediaservice.components.PlayResourceScheduler;
import ipaneltv.toolkit.mediaservice.components.PlayResourceScheduler.ResourcesState;

import java.io.FileDescriptor;

import org.json.JSONException;
import org.json.JSONObject;
import org.json.JSONStringer;
import org.json.JSONTokener;

import android.media.TeeveePlayer;
import android.media.TeeveePlayer.PlayStateListener;
import android.media.TeeveePlayer.ProgramStateListener;
import android.net.LocalSocket;
import android.net.LocalSocketAddress;
import android.net.Uri;
import android.net.telecast.ProgramInfo;
import android.net.telecast.ProgramInfo.StreamTypeNameEnum;
import android.os.Bundle;
import android.os.Handler;
import android.os.HandlerThread;
import android.util.Log;

public class LocalSockTsPlayerContext<T extends MediaPlaySessionService> extends
		TeeveePlayerBaseContext<T> implements LocalSockTsPlayerInterface {
	abstract class CB implements LocalSockTsPlayerInterface.Callback {
	};

	public static final String TAG = LocalSockTsPlayerContext.class.getSimpleName();

	protected final Object mutex = new Object();
	protected ResourcesState mResourceState;
	// protected PlayWidgetControl mWidget;
	private boolean contextReady = false;

	protected int fflags = 0, pflags = 0;
	protected String localSock = null;
	protected String progUri = null;
	protected ProgramInfoFilter piFilter;
	protected volatile int selecVersion = 0;
	private HandlerThread procThread = new HandlerThread("localSocket-proc");
	private Handler procHandler = null;

	public LocalSockTsPlayerContext(T service) {
		super(service);
	}

	@SuppressWarnings("rawtypes")
	@Override
	public void onCreate() {
		IPanelLog.i(TAG, "onCreate getSessionService = " + getSessionService());
		procThread.start();
		procHandler = new Handler(procThread.getLooper());
		LiveNetworkApplication app = getSessionService().getApp();
		Bundle b = getBundle();
		int pri = PlayResourceScheduler.PRIORITY_DEFAULT;
		boolean soft = false;
		if (b != null) {
			pri = b.getInt("priority", pri);
			soft = b.getBoolean("soft", false);
		}
		IPanelLog.i(TAG, "onCreate app = " + app);
		PlayResourceScheduler ps = app.getResourceScheduler();
		IPanelLog.i(TAG, "onCreate ps = " + ps);
		mResourceState = ps.createPushPlayState(pri, soft);
		mResourceState.getPlayer().setListener(playerListener, playerListener);
		IPanelLog.i(TAG, "onCreate 11");
		piFilter = new ProgramInfoFilter(ps.getUUID(), ps.getTransportManager()) {

			@Override
			protected String getStreamTypeName(int stream_type) {
				IPanelLog.d(TAG, "getStreamTypeName stream_type = " + stream_type);
				return getStreamType(stream_type);
			}

		};
		IPanelLog.i(TAG, "onCreate 22");
	}

	@Override
	public void onClose() {
		IPanelLog.i(TAG, "onClose 11");
		if (piFilter != null) {
			piFilter.release();
			piFilter = null;
		}
		if (mResourceState != null) {
			mResourceState.loosen(true);
			mResourceState = null;
		}
		IPanelLog.i(TAG, "onClose 22");
		procThread.quit();
		IPanelLog.i(TAG, "onClose 33");
	}

	void ensureLiveStateListeners() {
		if (mResourceState.getTag() == null) {
			mResourceState.setTag(new Object());
			IPanelLog.d(TAG, "ensureLiveStateListeners playerListener =" + playerListener);
			mResourceState.getPlayer().setListener(playerListener, playerListener);
		}
	}

	protected boolean reserveAll() {
		return mResourceState.reserve();
	}

	protected void loosenAll(boolean clearState) {
		if(clearState && isRelease()){
			mResourceState.destroy();
		}else{
			mResourceState.loosen(clearState);	
		}
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

	private boolean reserveAllSafe() {
		boolean ret = false;
		try {
			return (ret = reserveAll());
		} finally {
			if (!ret)
				loosenAll(true);
			contextReady = ret;
		}
	}

	@Override
	public void loosen(boolean clearState) {
		IPanelLog.d(TAG, "loosen 1 contextReady = " + contextReady);
		synchronized (mutex) {
			if (contextReady) {
				IPanelLog.d(TAG, "onLoosen");
				contextReady = false;
				if (clearState) {
					localSock = null;
					progUri = null;
				}
				loosenAll(clearState);
			}
		}
	}

	@Override
	public final boolean reserve() {
		synchronized (mutex) {
			if (contextReady ? false : reserveAllSafe()) {
				ensureLiveStateListeners();
				mResourceState.getPlayer().start();
				try {
					contextReady = true;
					IPanelLog.e(TAG, "onReserve ok.");
				} catch (Exception e) {
					IPanelLog.e(TAG, "onReserve error:" + e);
					mResourceState.loosen(true);
				}
			}
		}
		return contextReady;
	}

	@Override
	public void stop(int flag) {
		Log.i(TAG, "stop IN 2");
		synchronized (mutex) {
			Log.i(TAG, "stop suspend = " + suspend);
			if (!suspend) {
				Log.i(TAG, "mResourceState.isReserved()= " + mResourceState.isReserved());
				if (mResourceState.isReserved()) {
					suspend = true;
					localSock = null;
					progUri = null;
					stopPlayer(flag);
				}
			}
		}
	}

	@Override
	public void pause() {
		synchronized (mutex) {
			if (mResourceState.isReserved()) {
				mResourceState.getPlayer().setFreeze(true, TeeveePlayer.FLAG_VIDEO_FRAME_FREEZE);
				mResourceState.getPlayer().pause();
			}
		}
	}

	@Override
	public void resume() {
		synchronized (mutex) {
			if (mResourceState.isReserved()) {
				mResourceState.getPlayer().resume();
				onSetVolume(volumeSelect);
			}
		}
	}

	@Override
	public void setVolume(float v) {
		synchronized (mutex) {
			if (mResourceState.isReserved()) {
				volumeSelect = v < 0f ? 0f : v > 1f ? 1f : v;
				IPanelLog.d(TAG, "setVolume volumeSelect= " + volumeSelect);
				mResourceState.getPlayer().setVolume(volumeSelect);
			}
		}
	}

	/**
	 * 得到当前节目的播放时间
	 * 
	 * @return 毫秒时间值
	 */
	public long getPlayTime() {
		synchronized (mutex) {
			if (mResourceState.isReserved()) {
				IPanelLog.d(TAG, "getPlayTime");
				return mResourceState.getPlayer().getPlayTime();
			}
			return -1;
		}
	}

	@Override
	public void setDisplay(int x, int y, int w, int h) {
		synchronized (mutex) {
			if (mResourceState.isReserved()) {
				mResourceState.getPlayer().setDisplay(x, y, w, h);
			}
		}
	}

	@Override
	public void setProgramFlags(int flags) {
		synchronized (mutex) {
			if (mResourceState.isReserved()) {
				try {
					ProgramInfo pi = ProgramInfo.fromString(progUri);
					IPanelLog.d(TAG, "setProgramFlags flags = " + flags);
					if (!selectProgram(pi, flags)) {
						notifyError(L10n.SELECT_ERR_431);
						notifyWidgetSwitchEnd(L10n.SELECT_ERR_431);
					}
				} catch (Exception e) {
					notifyError("error:" + e.getMessage());
				}
			}
		}
	}

	@Override
	public void setTeeveeWidget(int flags) {
		synchronized (mutex) {
			// mWidget.setTeeveeWidget(flags);
		}
	}

	@Override
	public void checkTeeveeWidget(int flags) {
		synchronized (mutex) {
			// mWidget.checkTeeveeWidget(flags);
		}
	}

	@Override
	public void syncMediaTime() {
		synchronized (mutex) {
			if (mResourceState.isReserved()) {
				notifyJson(CB.__ID_onSyncMediaTime, null);
			}
		}
	}

	void notifyError(String msg) {
		notifyJson(CB.__ID_onPlayError, msg);
	}

	class ProgFilter implements ProgramInfoFilter.ProgramHandler {
		int flags = 0;

		public ProgFilter(int flags) {
			this.flags = flags;
		}

		@Override
		public void onProgramFound(final int ver, final String err, final ProgramInfo info) {
			IPanelLog.d(TAG, "onProgramFound info=" + info);
			// 通过procHandler 发送消息播放，以免出现在其他的地方关闭filter时卡主的情况。
			procHandler.post(new Runnable() {
				@Override
				public void run() {
					onProgramFoundPlay(ver, err, info, flags);
				}
			});
			IPanelLog.d(TAG, "onProgramFound end");
		}
	};

	private void onProgramFoundPlay(int ver, String err, ProgramInfo info, int flag) {
		synchronized (mutex) {
			boolean succ = false;
			IPanelLog.d(TAG, "onProgramFoundPlay info=" + info + ";flag = " + flag);
			try {
				if (selecVersion == ver && err == null && info != null) {
					if (!selectProgram(info, flag)) {
						IPanelLog.e(TAG, "start>selectProgram failed");
						return;
					}
					piFilter.stop();
					succ = true;
				}
			} catch (Exception e) {
				e.printStackTrace();
			} finally {
				notifyJson(CB.__ID_onResponseStart, succ + "");
				IPanelLog.d(TAG, "onProgramFoundPlay __ID_onResponseStart succ=" + succ);
			}
		}
	}

	private int checkPlayedUri(String f, String p) {
		if ((localSock != null && progUri == null) || (localSock == null))
			return 1;
		boolean fb = localSock.equals(f), pb = progUri.equals(p);
		if (!fb && !pb) { // 源与节目都不相同
			localSock = null;
			progUri = null;
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

	@Override
	public void start(String sockname, int fflags, String puri, int pflags) {
		synchronized (mutex) {
			IPanelLog.d(TAG, "start sockname=" + sockname + ",fflags=" + fflags);
			IPanelLog.d(TAG, "start puri = " + puri + ",pflags=" + pflags);
			if (mResourceState.isReserved()) {
				Uri uri = Uri.parse(sockname);
				long freq = Long.parseLong(uri.getQueryParameter("vfrequency"));

				int result = checkPlayedUri(sockname, puri);
				IPanelLog.d(TAG, "check uri result 1 =" + result);
				if (result == 1) {
					if (!selectVFrequecy(sockname, fflags)) {
						IPanelLog.e(TAG, "start>selectVFrequecy failed");
						return;
					}
					piFilter.stop();
					if (UriToolkit.getSchemaId(puri) == UriToolkit.PMT_SCHEMA_ID) {
						// PMT PID
						final int pmtpid = UriToolkit.getProgramPmtId(puri);
						piFilter.start2(freq, pmtpid, ++selecVersion, new ProgFilter(pflags));
					} else if (UriToolkit.getSchemaId(puri) == UriToolkit.DVB_SERVICE_SCHEMA_ID) {
						// SERVICE ID
						int pn = UriToolkit.getProgramServiceId(puri);
						pn = (pn == 0 ? -1 : pn);
						piFilter.start(freq, pn, ++selecVersion, new ProgFilter(pflags));
					} else if (UriToolkit.getSchemaId(puri) == UriToolkit.PROGRAM_INFO_SCHEMA_ID) {
						ProgramInfo info = ProgramInfo.fromString(puri);
						boolean succ = false;
						if (selectProgram(info, pflags)) {
							IPanelLog.e(TAG, "start>selectProgram succ");
							succ = true;
						}
						notifyJson(CB.__ID_onResponseStart, succ + "");
						IPanelLog.d(TAG, "onProgramFoundPlay __ID_onResponseStart succ=" + succ);
					} else if (UriToolkit.getSchemaId(puri) == UriToolkit.FILE_SCHEMA_ID) {
						ProgramInfo info = getProgramUri(puri);
						boolean succ = false;
						if (selectProgram(info, pflags)) {
							IPanelLog.e(TAG, "start>selectProgram succ");
							succ = true;
						}
						notifyJson(CB.__ID_onResponseStart, succ + "");
						IPanelLog.d(TAG, "onProgramFoundPlay __ID_onResponseStart succ=" + succ);
					}
				} else if (result == 3) {
					piFilter.stop();
					if (UriToolkit.getSchemaId(puri) == UriToolkit.PMT_SCHEMA_ID) {
						// PMT PID
						final int pmtpid = UriToolkit.getProgramPmtId(puri);
						piFilter.start2(freq, pmtpid, ++selecVersion, new ProgFilter(pflags));
					} else if (UriToolkit.getSchemaId(puri) == UriToolkit.DVB_SERVICE_SCHEMA_ID) {
						// SERVICE ID
						int pn = UriToolkit.getProgramServiceId(puri);
						pn = (pn == 0 ? -1 : pn);
						piFilter.start(freq, pn, ++selecVersion, new ProgFilter(pflags));
					} else if (UriToolkit.getSchemaId(puri) == UriToolkit.PROGRAM_INFO_SCHEMA_ID) {
						ProgramInfo info = ProgramInfo.fromString(puri);
						boolean succ = false;
						if (selectProgram(info, pflags)) {
							IPanelLog.e(TAG, "start>selectProgram succ");
							succ = true;
						}
						notifyJson(CB.__ID_onResponseStart, succ + "");
						IPanelLog.d(TAG, "onProgramFoundPlay __ID_onResponseStart succ=" + succ);
					} else if (UriToolkit.getSchemaId(puri) == UriToolkit.FILE_SCHEMA_ID) {
						ProgramInfo info = getProgramUri(puri);
						boolean succ = false;
						if (selectProgram(info, pflags)) {
							IPanelLog.e(TAG, "start>selectProgram succ");
							succ = true;
						}
						notifyJson(CB.__ID_onResponseStart, succ + "");
						IPanelLog.d(TAG, "onProgramFoundPlay __ID_onResponseStart succ=" + succ);
					}
				} else if (result == 0) {
					// clear operation
					ensurePlayerStarted();
				} else {
					throw new RuntimeException("impl err");
				}
				suspend = false;
			} else {
				notifyJson(CB.__ID_onResponseStart, Boolean.FALSE + "");
				IPanelLog.d(TAG, "start __ID_onResponseStart failed");
			}
		}
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
		pinfo.setPcrPID(Integer.valueOf(vpid));
		return pinfo;
	}

	@Override
	public void setRate(float r) {
		synchronized (mutex) {
			try {
				if (mResourceState.isReserved()) {
					ProgramInfo pi = ProgramInfo.fromString(progUri);
					pi.setVideoSourceRate(r);
					selectProgram(pi, this.pflags);
				}
			} catch (Exception e) {
				IPanelLog.e(TAG, "setRate failed e" + e.getMessage());
			} finally {
				notifyJson(CB.__ID_onRateChange, "" + r);
			}
		}
	}

	protected void clearCache(boolean b) {
		mResourceState.getPlayer().setFreeze(b, TeeveePlayer.FLAG_VIDEO_FRAME_FREEZE);
		mResourceState.getPlayer().stop();
	}

	@Override
	public void redirect(String sockname, int flags) {
		synchronized (mutex) {
			boolean succ = false;
			try {
				IPanelLog.d(TAG, "redirect sockname=" + sockname + ",flags=" + flags);
				if (mResourceState.isReserved()) {
					try {
						IPanelLog.d(TAG, "redirect 1111112");
						clearCache(true);
						if (!selectVFrequecy(sockname, flags)) {
							IPanelLog.e(TAG, "redirect>selectVFrequecy failed");
							return;
						}
						IPanelLog.d(TAG, "redirect 222222 progUri = " + progUri);
						if (progUri == null) {
							Uri uri = Uri.parse(sockname);
							long freq = Long.parseLong(uri.getQueryParameter("vfrequency"));
							piFilter.stop();
							IPanelLog.d(TAG, "redirect filter stop end freq = " + freq);
							piFilter.start(freq, 0, ++selecVersion, new ProgFilter(pflags
									| TeeveePlayer.FLAG_VIDEO_FRAME_FREEZE));
							IPanelLog.d(TAG, "redirect filter start end");
						} else {
							ProgramInfo pi = ProgramInfo.fromString(progUri);
							if (!selectProgram(pi, pflags | TeeveePlayer.FLAG_VIDEO_FRAME_FREEZE)) {
								IPanelLog.e(TAG, "redirect>selectProgram failed");
								return;
							}
							onSetVolume(volumeSelect);
						}
						IPanelLog.d(TAG, "redirect 333333");
						succ = true;
					} finally {
						mResourceState.getPlayer().setFreeze(false, 0);
						// setVolume(volumeSelect);
					}
				}
			} catch (Exception e) {
				IPanelLog.e(TAG, "redirect error e:" + e);
			} finally {
				notifyJson(CB.__ID_onResponseRedirect, succ + "");
			}
		}
	}

	@Override
	public String onTransmit(int code, String json, JsonParcelable p, Bundle b)
			throws JSONException {
		IPanelLog.d(TAG, "onTransmit> code=" + code + ",json=" + json);
		switch (code) {
		case __ID_start:
			JSONObject os = (JSONObject) new JSONTokener(json).nextValue();
			start(os.getString("localsock"), os.getInt("fflags"), os.getString("puri"),
					os.getInt("pflags"));
			break;
		case __ID_setRate:
			setRate(Float.parseFloat(json));
			break;
		case __ID_redirect:
			JSONObject or = (JSONObject) new JSONTokener(json).nextValue();
			redirect(or.getString("redirect"), or.getInt("flags"));
			break;
		case __ID_pause:
			pause();
			break;
		case __ID_resume:
			resume();
			break;
		case __ID_getPlayTime:
			return getPlayTime() + "";
		default:
			return super.onTransmit(code, json, p, b);
		}
		return null;
	}

	// =====================
	private boolean playerStopped = false;
	private boolean suspend = false;
	private float volumeSelect = 0.5f;

	private void closeSocket(LocalSocket s) {
		if (s != null) {
			try {
				s.close();
			} catch (Exception e) {
				IPanelLog.e(TAG, "close socket failed");
			}
		}
	}

	private boolean selectVFrequecy(String localsock, int fflags) {
		LocalSocket sock = new LocalSocket();
		Log.d(TAG, "selectVFrequecy in");
		// 先进行数据注入停止，否则后面切换时会出错
		mResourceState.getSelector().select((FileDescriptor) null, 0);
		Log.d(TAG, "selectVFrequecy 11");
		try {
			Uri uri = Uri.parse(localsock);
			long freq = Long.parseLong(uri.getQueryParameter("vfrequency"));
			Log.d(TAG, "selectVFrequecy 22");
			String sockname = uri.getAuthority();
			IPanelLog.d(TAG, "selectVFrequecy> (sockname=" + sockname + ",freq=" + freq + ")");
			sock.connect(new LocalSocketAddress(sockname));
			FileDescriptor fd = sock.getFileDescriptor();
			mResourceState.getSelector().setVirtualFrequency(freq);
			IPanelLog.d(TAG, "selectVFrequecy> fd = " + fd);
			if (!mResourceState.getSelector().select(fd, fflags)) {
				IPanelLog.e(TAG, "StreamSelector select failed");
				return false;
			}
			IPanelLog.d(TAG, "selectVFrequecy ok");

			this.localSock = localsock;
			this.fflags = fflags;
			return true;
		} catch (Exception e) {
			IPanelLog.e(TAG, "selectVFrequecy e:" + e.getMessage());
			e.printStackTrace();
			return false;
		} finally {
			closeSocket(sock);
		}
	}

	private boolean ensurePlayerStarted() {
		if (mResourceState.isReserved()) {
			boolean b = false;
			IPanelLog.d(TAG, "ensurePlayerStarted playerStopped =" + playerStopped);
			if (playerStopped) {
				b = mResourceState.getPlayer().start();
				if (b) {
					mResourceState.getPlayer().setFreeze(false, 0);
					onSetVolume(volumeSelect);
					IPanelLog.d(TAG, "ensurePlayerStarted volumeSelect=" + volumeSelect);
					playerStopped = false;
				}
			}
			return b;
		}
		return false;
	}

	protected void onSetVolume(float f){
		mResourceState.getPlayer().setVolume(volumeSelect);
	}
	
	private boolean selectProgram(ProgramInfo pi, int pflags) {
		// ensurePlayerStarted();//不需要重启解码器
		IPanelLog.d(TAG, "select program 11 = " + pi);
		mResourceState.getPlayer().start();
		IPanelLog.d(TAG, "select program 22");
		if (!mResourceState.getPlayer().selectProgram(pi, pflags)) {
			IPanelLog.e(TAG, "TeeveePlayer select failed");
			return false;
		}

		this.pflags = pflags;
		this.progUri = pi.toString();
		return true;
	}

	private void stopPlayer(int flag) {
		IPanelLog.d(TAG, "stopPlayer flag=" + flag);
		if ((flag & TeeveePlayer.FLAG_VIDEO_FRAME_FREEZE) == TeeveePlayer.FLAG_VIDEO_FRAME_BLACK) {
			mResourceState.getPlayer().setFreeze(true, TeeveePlayer.FLAG_VIDEO_FRAME_BLACK);
			mResourceState.getPlayer().stop();
			mResourceState.getPlayer().setFreeze(true, TeeveePlayer.FLAG_VIDEO_FRAME_BLACK);
			playerStopped = true;
		} else if ((flag & TeeveePlayer.FLAG_VIDEO_FRAME_FREEZE) == TeeveePlayer.FLAG_VIDEO_FRAME_FREEZE) {
			mResourceState.getPlayer().setFreeze(true, TeeveePlayer.FLAG_VIDEO_FRAME_FREEZE);
			mResourceState.getPlayer().stop();
			mResourceState.getPlayer().setFreeze(true, TeeveePlayer.FLAG_VIDEO_FRAME_FREEZE);
			playerStopped = true;
		}
	}

	private final void notifyWidgetSwitchEnd(String err) {
		// int code = err == null ? 0 : L10n.getErrorCode(err, 100000);
		// mWidget.notifySwitchingEnd(code, err);
	}

	@SuppressWarnings("unused")
	private boolean onSelect(String localsock, int fflags, ProgramInfo pi, int pflags) {
		IPanelLog.d(TAG, "onSelect 1:(" + localsock + "," + fflags + "," + pi.toString() + ","
				+ pflags + ")");

		// mWidget.clearWidgetMessage();
		// mWidget.notifySwitchingStart(pi.getVideoPID() < 0);

		if (!selectVFrequecy(localsock, fflags)) {
			/*
			 * notifyError(L10n.SELECT_ERR_430);
			 * notifyWidgetSwitchEnd(L10n.SELECT_ERR_430);
			 */
			return false;
		}

		if (!selectProgram(pi, pflags)) {
			notifyError(L10n.SELECT_ERR_431);
			notifyWidgetSwitchEnd(L10n.SELECT_ERR_431);
			return false;
		}

		/** 动态更改参数时使用 **/
		this.localSock = localsock;
		this.progUri = pi.toString();
		this.fflags = fflags;
		this.pflags = pflags;
		// mWidget.notifySwitchingOver(null);
		return true;
	}

	PlayerListener playerListener = new PlayerListener();

	class PlayerListener implements PlayStateListener, ProgramStateListener {

		@Override
		public void onProgramReselect(int program_number, String newuri) {
			// TODO Auto-generated method stub

		}

		@Override
		public void onProgramDiscontinued(int program_number) {
			// TODO Auto-generated method stub

		}

		@Override
		public void onSelectionStart(TeeveePlayer player, int program_number) {
			// TODO Auto-generated method stub

		}

		@Override
		public void onPlayProcessing(int program_number) {
			// try {
			// IPanelLog.d(TAG, "onPlayProcessing program_number = " +
			// program_number);
			// JSONStringer str = new JSONStringer();
			// str.object();
			// str.key("pn").value(program_number);
			// long pts_time =
			// mResourceState.getPlayer().getPlayProcessPtsTime();
			// IPanelLog.d(TAG, "pts_time = " + pts_time);
			// str.key("pts_time").value(pts_time);
			// str.key("state").value(1);//1代表process消息
			// str.endObject();
			// //notifyJson(CB.__ID_onPlayProcessing,str.toString());
			// notifyJson(CB.__ID_onPlayerPTSChange,str.toString());
			// } catch (Exception e) {
			// IPanelLog.e(TAG, "onPlayProcessing failed e=" + e);
			// e.printStackTrace();
			// }
		}

		@Override
		public void onPlaySuspending(int program_number) {
			IPanelLog.d(TAG, "onPlaySuspending program_number=" + program_number);
			// try {
			// JSONStringer str = new JSONStringer();
			// str.object();
			// str.key("pn").value(program_number);
			// str.key("pts_time").value(0);
			// str.key("state").value(0);
			// str.endObject();
			// notifyJson(CB.__ID_onPlayerPTSChange,str.toString());
			// } catch (Exception e) {
			// IPanelLog.e(TAG, "onPlaySuspending failed e=" + e);
			// e.printStackTrace();
			// }
			//
			// notifyJson(CB.__ID_onPlaySuspending, program_number + "");
		}

		@Override
		public void onPlayError(int program_number, String msg) {
			// TODO Auto-generated method stub

		}
	}

	PlayerPTSListener playerptslistener = new PlayerPTSListener();

	class PlayerPTSListener implements PlayResourceScheduler.PlayerProcessPTSListener {

		@Override
		public void onPlayerPTSChange(int program_number, long process_pts_time, int state) {
			// TODO Auto-generated method stub
			try {
				IPanelLog.d(TAG, "onPlayerPTSChange program_number = " + program_number);
				JSONStringer str = new JSONStringer();
				str.object();
				str.key("pn").value(program_number);
				IPanelLog.d(TAG, "process_pts_time = " + process_pts_time);
				str.key("pts_time").value(process_pts_time);
				str.key("state").value(state);// 1代表process消息
				str.endObject();
				// notifyJson(CB.__ID_onPlayProcessing,str.toString());
				notifyJson(CB.__ID_onPlayerPTSChange, str.toString());
			} catch (Exception e) {
				IPanelLog.e(TAG, "onPlayProcessing failed e=" + e);
				e.printStackTrace();
			}
		}

		@Override
		public void onPlayProcess(int program_number, long process_pts_time) {
			// TODO Auto-generated method stub

		}

		@Override
		public void onPlaySuspend(int program_number) {
			// TODO Auto-generated method stub

		}
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