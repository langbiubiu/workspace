package ipaneltv.toolkit.mediaservice;

import ipaneltv.toolkit.IPanelLog;
import ipaneltv.toolkit.JsonParcelable;
import ipaneltv.toolkit.media.MediaSessionInterface.HomedHttpPlayerInterface;
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
import android.net.Uri;
import android.net.telecast.ProgramInfo;
import android.net.telecast.ProgramInfo.StreamTypeNameEnum;
import android.os.Bundle;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.ParcelFileDescriptor;
import android.util.Log;

public class HomedHttpPlayerContext<T extends MediaPlaySessionService> extends
		TeeveePlayerBaseContext<T> implements HomedHttpPlayerInterface {
	abstract class CB implements HomedHttpPlayerInterface.Callback {
	};

	public static final String TAG = HomedHttpPlayerContext.class.getSimpleName();

	protected final Object mutex = new Object();
	protected ResourcesState mResourceState;
	private boolean contextReady = false;

	protected int fflags = 0, pflags = 0;
//	protected String localSock = null;
//	protected String progUri = null;
	protected volatile int selecVersion = 0;
	private HandlerThread procThread = new HandlerThread("localSocket-proc");
	Handler procHandler = null;

	public HomedHttpPlayerContext(T service) {
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
		mResourceState.getPlayer().setListener(playerListener, playerListener,playerptslistener);
		IPanelLog.i(TAG, "onCreate 11");
		IPanelLog.i(TAG, "onCreate 22");
	}

	@Override
	public void onClose() {
		IPanelLog.i(TAG, "onClose 11");
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

	protected String getStreamType(int type,int flag) {
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
//					localSock = null;
//					progUri = null;
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
		Log.i(TAG, "stop IN");
		synchronized (mutex) {
			Log.i(TAG, "stop suspend = " + suspend);
			if (!suspend) {
				Log.i(TAG, "mResourceState.isReserved()= " + mResourceState.isReserved());
				if (mResourceState.isReserved()) {
					suspend = true;
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
				setVolume(volumeSelect);
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
					ProgramInfo pi = ProgramInfo.fromString(info);
					IPanelLog.d(TAG, "setProgramFlags flags = " + flags);
					if (!selectProgram(pi, flags)) {
						notifyError(L10n.SELECT_ERR_431);
						notifyWidgetSwitchEnd(L10n.SELECT_ERR_431);
					}
					setVolume(volumeSelect);
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


	@Override
	public void startFd(long vfreq, ParcelFileDescriptor pfd, int fflags) {
		synchronized (mutex) {
			boolean succ = false;
			try {
				if (mResourceState.isReserved() ) {
					IPanelLog.d(TAG, "startFd 44 pfd = pfd = "+ pfd);
					if(!selectHomedFd(vfreq, pfd.getFileDescriptor(), fflags)){
						return;
					}
					ProgramInfo pinfo = ProgramInfo.fromString(info);
					if(!startHomed(pinfo, 1)){
						return;
					}
					setVolume(volumeSelect);
					succ = true;
					suspend = false;
					IPanelLog.d(TAG, "startFd end");
				}	
			} catch (Exception e) {
				// TODO: handle exception
			}finally{
				notifyJson(CB.__ID_onResponseStart, succ + "");
				IPanelLog.d(TAG, "onProgramFoundPlay __ID_onResponseStart succ=" + succ);
			}
		}
	}
	
	@Override
	public void start(String sockname, int fflags, String puri, int pflags) {
		IPanelLog.d(TAG, "start 44 sockname = " + sockname + "; puri = " + puri +";pflags = "+ pflags);
		synchronized (mutex) {
			try {
				ProgramInfo pinfo = getProgramUri(puri);
				ProgramInfo defauletInfo = ProgramInfo.fromString(info);
				IPanelLog.d(TAG, "start cNumber pinfo = "+ pinfo);
				if (defauletInfo.getAudioPID() != pinfo.getAudioPID()
						|| !defauletInfo.getAudioStreamType().equals(pinfo.getAudioStreamType())
						|| defauletInfo.getVideoPID() != pinfo.getVideoPID()
						|| !defauletInfo.getVideoStreamType().equals(pinfo.getVideoStreamType())) {
					if(mResourceState.isReserved()){
						if(!startHomed(pinfo, 1|pflags)){
							return;
						}
						setVolume(volumeSelect);
						info = pinfo.toString();
					}
					suspend =false;
				}
			} catch (Exception e) {
				Log.e(TAG, "start e = "+ e.getMessage());
			}
			
		}
	}

	@Override
	public void setRate(float r) {
		synchronized (mutex) {
			try {
				if (mResourceState.isReserved()) {
					ProgramInfo pi = ProgramInfo.fromString(info);
					pi.setVideoSourceRate(r);
					selectProgram(pi, this.pflags);
					setVolume(volumeSelect);
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
	public void redirect(long vfreq, ParcelFileDescriptor pfd, int flags) {
		synchronized (mutex) {
			boolean succ = false;
			try {
				if (mResourceState.isReserved() ) {
					IPanelLog.d(TAG, "redirect 11 pfd = pfd = "+ pfd);
					if(!selectHomedFd(vfreq, pfd.getFileDescriptor(), fflags)){
						return;
					}
					ProgramInfo pinfo = ProgramInfo.fromString(info);
					if(!startHomed(pinfo, 1)){
						return;
					}
					setVolume(volumeSelect);
					succ = true;
					IPanelLog.d(TAG, "redirect end");
				}	
			} catch (Exception e) {
				// TODO: handle exception
			}finally{
				IPanelLog.d(TAG, "redirect  succ=" + succ);
			}
		}
	}

	@Override
	public String onTransmit(int code, String json, JsonParcelable p, Bundle b)
			throws JSONException {
		IPanelLog.d(TAG, "onTransmit> code=" + code + ",json=" + json);
		switch (code) {
		case __ID_start_fd: {
			ParcelFileDescriptor pfd = (ParcelFileDescriptor) p.getParcelable("pfd");
			Log.d(TAG, "__ID_start_fd pfd = " + pfd);
			JSONObject o = (JSONObject) new JSONTokener(json).nextValue();
			startFd(o.getLong("vfreq"), pfd, o.getInt("fflags"));
			break;
		}
		case __ID_start:
			JSONObject os = (JSONObject) new JSONTokener(json).nextValue();
			start(os.getString("localsock"), os.getInt("fflags"), os.getString("puri"),
					os.getInt("pflags"));
			break;
		case __ID_setRate:
			setRate(Float.parseFloat(json));
			break;
		case __ID_redirect:
			ParcelFileDescriptor pfd = (ParcelFileDescriptor) p.getParcelable("pfd");
			Log.d(TAG, "__ID_start_fd pfd = " + pfd);
			JSONObject o = (JSONObject) new JSONTokener(json).nextValue();
			redirect(o.getLong("vfreq"), pfd, o.getInt("fflags"));
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

	String info = "program://1?audio_stream_pid=1002&audio_stream_type=audio_aac&video_stream_pid=1001&video_stream_type=video_h264&pcr_stream_pid=1001&ca_required=false";

	private boolean selectHomedFd(long vfreq,FileDescriptor fd,int fflags){
		synchronized (mutex) {
			mResourceState.getSelector().select((FileDescriptor) null, 0);
			mResourceState.getSelector().setVirtualFrequency(vfreq);
			Log.d(TAG, "homedSelectFd> fd = " + fd);
			mResourceState.getSelector().clear();
			if (!mResourceState.getSelector().select(fd, fflags)) {
				Log.e(TAG, "homedSelectFd StreamSelector select failed");
				return false;
			}
		}
		Log.d(TAG, "homedSelectFd ok");
		return true;
	}
	private boolean startHomed(ProgramInfo pinfo,int pflags){
		Log.d(TAG, "select program 111111 = " + pinfo);
		synchronized (mutex) {
			mResourceState.getPlayer().start();
			Log.d(TAG, "select program 222");
			mResourceState.getPlayer().clearCache();
			if (!mResourceState.getPlayer().selectProgram(pinfo, pflags)) {
				Log.e(TAG, "TeeveePlayer select failed");
				return false;
			}
			Log.d(TAG, "select program end");
			return true;
		}
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
//		this.progUri = pi.toString();
		return true;
	}

	private void stopPlayer(int flag) {
		IPanelLog.d(TAG, "stopPlayer flag=" + flag);
		if ((flag & TeeveePlayer.FLAG_VIDEO_FRAME_FREEZE) == TeeveePlayer.FLAG_VIDEO_FRAME_BLACK) {
			mResourceState.getPlayer().setFreeze(true, TeeveePlayer.FLAG_VIDEO_FRAME_BLACK);
			mResourceState.getPlayer().stop();
			mResourceState.getPlayer().setFreeze(true, TeeveePlayer.FLAG_VIDEO_FRAME_BLACK);
			mResourceState.getPlayer().setVolume(0);
			playerStopped = true;
		} else if ((flag & TeeveePlayer.FLAG_VIDEO_FRAME_FREEZE) == TeeveePlayer.FLAG_VIDEO_FRAME_FREEZE) {
			mResourceState.getPlayer().setFreeze(true, TeeveePlayer.FLAG_VIDEO_FRAME_FREEZE);
			mResourceState.getPlayer().stop();
			mResourceState.getPlayer().setFreeze(true, TeeveePlayer.FLAG_VIDEO_FRAME_FREEZE);
			mResourceState.getPlayer().setVolume(0);
			playerStopped = true;
		}
	}

	protected ProgramInfo getProgramUri(String str) {
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
		pinfo.setAudioStreamType(getStreamType(Integer.valueOf(adec),0));
		pinfo.setVideoPID(Integer.valueOf(vpid));
		pinfo.setVideoStreamType(getStreamType(Integer.valueOf(vdec),1));
		pinfo.setPcrPID(Integer.valueOf(vpid));
		return pinfo;
	}
	
	private final void notifyWidgetSwitchEnd(String err) {
		// int code = err == null ? 0 : L10n.getErrorCode(err, 100000);
		// mWidget.notifySwitchingEnd(code, err);
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