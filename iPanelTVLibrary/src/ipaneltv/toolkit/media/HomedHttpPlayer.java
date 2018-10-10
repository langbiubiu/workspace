package ipaneltv.toolkit.media;

import ipaneltv.toolkit.IPanelLog;
import ipaneltv.toolkit.JsonParcelable;
import ipaneltv.toolkit.media.MediaSessionInterface.HomedHttpPlayerInterface;

import org.json.JSONException;
import org.json.JSONObject;
import org.json.JSONStringer;
import org.json.JSONTokener;

import android.content.Context;
import android.os.Bundle;
import android.os.ParcelFileDescriptor;

public class HomedHttpPlayer extends TeeveePlayerBase implements HomedHttpPlayerInterface,
HomedHttpPlayerInterface.Callback {
	final static String TAG = HomedHttpPlayer.class.getSimpleName();

	public HomedHttpPlayer(Context context, String serviceName) {
		super(context, serviceName, HomedHttpPlayerInterface.class.getName());
	}

	@Override
	public void redirect(long vfreq, ParcelFileDescriptor pfd, int flags){
		try {
			JSONStringer str = new JSONStringer();
			str.object();
			str.key("vfreq").value(vfreq);
			str.key("fflags").value(flags);
			str.endObject();
			JsonParcelable p = new JsonParcelable();
			p.put("pfd", pfd);
			channel.transmit(__ID_redirect, str.toString(),p);
		} catch (JSONException e) {
			throw new RuntimeException(e.getMessage());
		}
	}

	@Override
	public final void start(String sockname, int fflags, String puri, int pflags) {
		try {
			JSONStringer str = new JSONStringer();
			str.object();
			str.key("localsock").value(sockname);
			str.key("fflags").value(fflags);
			str.key("puri").value(puri);
			str.key("pflags").value(pflags);
			str.endObject();
			channel.transmit(__ID_start, str.toString(), null);/*-update version;*/
		} catch (JSONException e) {
			throw new RuntimeException(e.getMessage());
		}
	}
	@Override
	public void startFd(long vfreq, ParcelFileDescriptor pfd, int fflags) {
		try {
			JSONStringer str = new JSONStringer();
			str.object();
			str.key("vfreq").value(vfreq);
			str.key("fflags").value(fflags);
			str.endObject();
			JsonParcelable p = new JsonParcelable();
			p.put("pfd", pfd);
			channel.transmit(__ID_start_fd, str.toString(), p);/*-update version;*/
		} catch (JSONException e) {
			throw new RuntimeException(e.getMessage());
		}
	}

	@Override
	public void pause() {
		channel.transmit(__ID_pause);
	}

	@Override
	public void resume() {
		channel.transmit(__ID_resume);
	}

	@Override
	public final void setRate(float r) {
		channel.transmit(__ID_setRate, r + "");
	}

	@Override
	protected final void onCallback(int code, String json, JsonParcelable p, Bundle b) {
		IPanelLog.d(TAG, "onCallback 1 code = " + code + " json = " + json);
		try {
			switch (code) {
			case __ID_onResponseStop:
				onResponseStop();
				break;
			case __ID_onWidgetChecked:
				onWidgetChecked(Integer.parseInt(json));
				break;
			case __ID_onVolumeChange:
				onVolumeChange(Float.parseFloat(json));
				break;
			case __ID_onSyncMediaTime:
				onSyncMediaTime(Long.parseLong(json));
				break;
			case __ID_onPlayError:
				onPlayError(json);
				break;
			case __ID_onResponseStart:
				onResponseStart(Boolean.parseBoolean(json));
				break;
			case __ID_onRateChange:
				onRateChange(Float.parseFloat(json));
				break;
			case __ID_onResponseRedirect:
				onResponseRedirect(Boolean.parseBoolean(json));
				break;
			case __ID_onPlayerPTSChange: {
				IPanelLog.d(TAG, "__ID_onPlayerPTSChange 1111111");
				JSONObject o = (JSONObject) new JSONTokener(json).nextValue();
				onPlayerPTSChange(o.getInt("pn"), o.getLong("pts_time"), o.getInt("state"));
				break;
			}
			case __ID_onSourceMediaChange: {
				JSONObject o = (JSONObject) new JSONTokener(json).nextValue();
				onSourceMediaChange(o.getLong("time"), o.getLong("pts"));
				break;
			}
			default:
				break;
			}
		} catch (Exception e) {
			if (p != null)
				p.clean();
		}
	}

	@Override
	public void onResponseStop() {
	}

	@Override
	public void onWidgetChecked(int flags) {
	}

	@Override
	public void onVolumeChange(float v) {
	}

	@Override
	public void onSyncMediaTime(long t) {
	}

	@Override
	public void onPlayError(String msg) {
	}

	@Override
	public void onResponseStart(boolean succ) {
		IPanelLog.d(TAG, "onResponseStart 1 succ = " + succ);
	}

	@Override
	public void onRateChange(float r) {
	}

	@Override
	public void onResponseRedirect(boolean succ) {
	}

	@Override
	public void onPlayerPTSChange(int pn, long pts_time, int state) {
		// TODO Auto-generated method stub

	}

	@Override
	public void onSourceMediaChange(long time, long pts) {
		// TODO Auto-generated method stub

	}

	@Override
	public void onPlayProcessing(int pn, long pts_time) {
		IPanelLog.d(TAG, "onPlayProcessing 222222");
	}

	@Override
	public void onPlaySuspending(int pn) {
	}

	@Override
	public void checkPassword(String pwd) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void onChannelLocked(long freq, int program_number) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void onPasswordChecked(boolean succ) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void onDescramError(long f, int pn, int code,String err) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void clearCache(int flags) {
		// TODO Auto-generated method stub
		
	}

}
