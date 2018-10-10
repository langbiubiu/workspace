package ipaneltv.toolkit.media;

import ipaneltv.toolkit.JsonParcelable;
import ipaneltv.toolkit.UriToolkit;
import ipaneltv.toolkit.media.MediaSessionInterface.IpQamTsPlayerInterface;

import org.json.JSONException;
import org.json.JSONObject;
import org.json.JSONStringer;
import org.json.JSONTokener;

import android.content.Context;
import android.net.Uri;
import android.os.Bundle;

public class IpQamTsPlayer extends TeeveePlayerBase implements IpQamTsPlayerInterface,
		IpQamTsPlayerInterface.Callback {
	final static String TAG = IpQamTsPlayer.class.getSimpleName();

	public IpQamTsPlayer(Context context, String serviceName) {
		super(context, serviceName, IpQamTsPlayerInterface.class.getName());
	}

	public final void start(String teeveeplayUri) {
		Uri uri = Uri.parse(teeveeplayUri);
		String furi = UriToolkit.splitFrequencyInfoUriInTeeveePlayUri(uri);
		String puri = UriToolkit.splitProgramInfoUriInTeeveePlayUri(uri);
		int fflags = UriToolkit.getFrequencyFlagsInTeeveePlayUri(uri);
		int pflags = UriToolkit.getProgramFlagsInTeeveePlayUri(uri);
		start(furi, fflags, puri, pflags);
	}

	public final void start(String furi, int fflags, int programNumber, int pflags) {
		start(furi, fflags, UriToolkit.DVB_SERVICE_SCHEMA + programNumber, pflags);
	}

	public final void start(String furi, int fflags, short pmtPID, int flags) {
		start(furi, fflags, UriToolkit.PMT_SCHEMA + pmtPID, flags);
	}

	@Override
	public void start(String furi, int fflags, String puri, int pflags) {
		try {
			JSONStringer str = new JSONStringer();
			str.object();
			str.key("furi").value(furi);
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
	public void setRate(float r) {
		channel.transmit(__ID_setRate, "" + r);
	}

	@Override
	public final void syncSignalStatus() {
		channel.transmitAsync(__ID_syncSignalStatus);
	}

	@Override
	public void clearCache(int flags) {
		channel.transmit(__ID_clearCache, "" + flags);
	}
	
	@Override
	protected void onCallback(int code, String json, JsonParcelable p, Bundle b) {
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
			case __ID_onSyncSignalStatus:
				onSyncSignalStatus(json);
				break;
//			case __ID_onPlayProcessing: {
//				JSONObject o = (JSONObject) new JSONTokener(json).nextValue();
//				onPlayProcessing(o.getInt("pn"), o.getLong("pts_time"));
//				break;
//			}
//			case __ID_onPlaySuspending:
//				onPlaySuspending(Integer.parseInt(json));
//				break;
			case __ID_onPlayerPTSChange: {
				JSONObject o = (JSONObject) new JSONTokener(json).nextValue();
				onPlayerPTSChange(o.getInt("pn"), o.getLong("pts_time"), o.getInt("state"));
				break;
			}
			case __ID_onSourceMediaChange: {
				JSONObject o = (JSONObject) new JSONTokener(json).nextValue();
				onSourceMediaChange(o.getLong("time"), o.getInt("pts"));
				break;
			}
			case __ID_onStreamResumed:
				onStreamResumed();
				break;
			case __ID_onStreamLost:
				onStreamLost();
				break;
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
	public void onSyncSignalStatus(String signalStatus) {
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
	public void onStreamResumed() {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void onStreamLost() {
		// TODO Auto-generated method stub
		
	}

//	@Override
//	public void onPlayProcessing(int pn, long pts_time) {
//	}
//
//	@Override
//	public void onPlaySuspending(int pn) {
//	}
	
}
