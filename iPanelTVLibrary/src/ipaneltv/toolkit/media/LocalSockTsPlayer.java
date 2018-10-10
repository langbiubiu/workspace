package ipaneltv.toolkit.media;

import ipaneltv.toolkit.IPanelLog;
import ipaneltv.toolkit.JsonParcelable;
import ipaneltv.toolkit.media.MediaSessionInterface.LocalSockTsPlayerInterface;

import org.json.JSONException;
import org.json.JSONObject;
import org.json.JSONStringer;
import org.json.JSONTokener;

import android.content.Context;
import android.os.Bundle;

public class LocalSockTsPlayer extends TeeveePlayerBase implements LocalSockTsPlayerInterface,
		LocalSockTsPlayerInterface.Callback {
	final static String TAG = LocalSockTsPlayer.class.getSimpleName();

	public LocalSockTsPlayer(Context context, String serviceName) {
		super(context, serviceName, LocalSockTsPlayerInterface.class.getName());
	}

	@Override
	public final void redirect(String uri, int flags) {
		try {
			JSONStringer s = new JSONStringer();
			s.object();
			s.key("redirect").value(uri);
			s.key("flags").value(flags);
			s.endObject();
			channel.transmit(__ID_redirect, s.toString());
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
		IPanelLog.d(TAG, "onCallback code = " + code + " json = " + json);
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
			// case __ID_onPlayProcessing: {
			// IPanelLog.d(TAG, "__ID_onPlayProcessing 1111111");
			// JSONObject o = (JSONObject) new JSONTokener(json).nextValue();
			// IPanelLog.d(TAG, "__ID_onPlayProcessing 2222222");
			// IPanelLog.d(TAG, "__ID_onPlayProcessing current object = " + this
			// + " o.getInt(\"pn\")= "
			// + o.getInt("pn") + "o.getLong(\"pts_time\") = " +
			// o.getLong("pts_time"));
			// onPlayProcessing(o.getInt("pn"), o.getLong("pts_time"));
			// break;
			// }
			// case __ID_onPlaySuspending:
			// onPlaySuspending(Integer.parseInt(json));
			// break;
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
