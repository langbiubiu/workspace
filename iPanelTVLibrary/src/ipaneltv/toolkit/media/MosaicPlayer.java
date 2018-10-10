package ipaneltv.toolkit.media;

import ipaneltv.toolkit.JsonParcelable;
import ipaneltv.toolkit.media.MediaSessionInterface.MosaicPlayerInterface;

import org.json.JSONException;
import org.json.JSONStringer;

import android.content.Context;
import android.graphics.Rect;
import android.os.Bundle;

public class MosaicPlayer extends TeeveePlayerBase implements MosaicPlayerInterface,
	MosaicPlayerInterface.Callback {
	final static String TAG = MosaicPlayer.class.getSimpleName();
	public static final int WIDGET_FALG_TIPS = 0x01;
	static final int GROUP = 0, CHANNEL = 1, PRESENT = 2, FOLLOW = 3;

	public MosaicPlayer(Context context, String serviceName) {
		super(context, serviceName, MosaicPlayerInterface.class.getName());
	}

	@Override
	public final void select(long freq, int fflags, String puri, int pflags) {
		try {
			JSONStringer str = new JSONStringer();
			str.object();
			str.key("freq").value(freq);
			str.key("fflags").value(fflags);
			if(puri == null){
				puri ="no";
			}
			str.key("puri").value(puri);
			str.key("pflags").value(pflags);
			str.endObject();
			channel.transmit(__ID_select, str.toString(), null);/*-update version;*/
		} catch (JSONException e) {
			throw new RuntimeException(e.getMessage());
		}
	}
	public final void setDisplayRect(Rect r) {
		setDisplay(r.left, r.top, r.right - r.left + 1, r.bottom - r.top + 1);
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
	public final void syncSignalStatus() {
		channel.transmitAsync(__ID_syncSignalStatus);
	}

	@Override
	protected final void onCallback(int code, String json, JsonParcelable a, Bundle b) {
		try {
			switch (code) {
			case __ID_onWidgetChecked:
				onWidgetChecked(Integer.parseInt(json));
				break;
			case __ID_onResponseStop:
				onResponseStop();
				break;
			case __ID_onVolumeChange:
				onVolumeChange(Float.parseFloat(json));
				break;
			case __ID_onSyncMediaTime:
				onSyncMediaTime(Long.parseLong(json));
				break;
			case __ID_onLiveInfoUpdated:
				onLiveInfoUpdated(Integer.parseInt(json));
				break;
			case __ID_onPlayError:
				onPlayError(json);
				break;
			case __ID_onResponseSelect:
				onResponseSelect(Boolean.parseBoolean(json));
				break;
			case __ID_onStreamLost:
				onStreamLost();
				break;
			case __ID_onStreamResumed:
				onStreamResumed();
				break;
			case __ID_onSyncSignalStatus:
				onSyncSignalStatus(json);
				break;
			default:
				break;
			}
		} catch (Exception e) {
			if (a != null)
				a.clean();
		}
	}

	@Override
	public void onResponseSelect(boolean b) {
	}

	@Override
	public void onStreamLost() {
	}

	@Override
	public void onStreamResumed() {
	}

	@Override
	public void onSyncSignalStatus(String signalStatus) {
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
	public void onSourceMediaChange(long time, long pts) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void onLiveInfoUpdated(int mask) {
		
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
