package ipaneltv.toolkit.media;

import ipaneltv.toolkit.JsonParcelable;
import ipaneltv.toolkit.db.DatabaseObjectification.ChannelKey;
import ipaneltv.toolkit.media.MediaSessionInterface.HomedProgramPlayerInterface2;

import org.json.JSONException;
import org.json.JSONObject;
import org.json.JSONStringer;
import org.json.JSONTokener;

import android.content.Context;
import android.graphics.Rect;
import android.os.Bundle;
import android.os.ParcelFileDescriptor;

public class HomedProgramPlayer2 extends TeeveePlayerBase implements HomedProgramPlayerInterface2,
		HomedProgramPlayerInterface2.Callback {
	final static String TAG = HomedProgramPlayer2.class.getSimpleName();
	public static final int WIDGET_FALG_TIPS = 0x01;
	static final int GROUP = 0, CHANNEL = 1, PRESENT = 2, FOLLOW = 3;

	public HomedProgramPlayer2(Context context, String serviceName) {
		super(context, serviceName, HomedProgramPlayerInterface.class.getName());
	}

	@Override
	public void select(long vfreq, ParcelFileDescriptor pfd, long freq, int fflags, int pn,
			int pflags, int delay) {
		try {
			JSONStringer str = new JSONStringer();
			str.object();
			str.key("vfreq").value(vfreq);
			str.key("freq").value(freq);
			str.key("fflags").value(fflags);
			str.key("pn").value(pn);
			str.key("pflags").value(pflags);
			str.key("delay").value(delay);
			str.endObject();
			JsonParcelable p = new JsonParcelable();
			if(pfd!= null){
				p.put("pfd", pfd);	
			}
			channel.transmit(__ID_homed_select, str.toString(), p);/*-update version;*/
		} catch (JSONException e) {
			throw new RuntimeException(e.getMessage());
		}
	}

	@Override
	public void start(String puri, int pflags) {
		try {
			JSONStringer str = new JSONStringer();
			str.object();
			str.key("puri").value(puri);
			str.key("pflags").value(pflags);
			str.endObject();
			channel.transmit(__ID_homed_start, str.toString(), null);/*-update version;*/
		} catch (JSONException e) {
			throw new RuntimeException(e.getMessage());
		}
	}

	@Override
	public void redirect(long vfreq, ParcelFileDescriptor pfd, int flags) {
		try {
			JSONStringer str = new JSONStringer();
			str.object();
			str.key("vfreq").value(vfreq);
			str.key("fflags").value(flags);
			str.endObject();
			JsonParcelable p = new JsonParcelable();
			if(pfd!= null){
				p.put("pfd", pfd);	
			}
			channel.transmit(__ID_homed_redirect, str.toString(), p);/*-update version;*/
		} catch (JSONException e) {
			throw new RuntimeException(e.getMessage());
		}
	}

	@Override
	public void startShift(long vfreq, ParcelFileDescriptor pfd, int fflags) {
		try {
			JSONStringer str = new JSONStringer();
			str.object();
			str.key("vfreq").value(vfreq);
			str.key("fflags").value(fflags);
			str.endObject();
			JsonParcelable p = new JsonParcelable();
			if(pfd!= null){
				p.put("pfd", pfd);	
			}
			channel.transmit(__ID_homed_startShift, str.toString(), p);/*-update version;*/
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
	public final void solveProblem() {
		channel.transmitAsync(__ID_homed_solveProblem);
	}

	@Override
	public final void enterCaApp(String uri) {
		channel.transmitAsync(__ID_homed_enterCaApp, uri);
	}

	@Override
	public final void observeProgramGuide(ChannelKey ch, long focus) {
		try {
			JSONStringer str = new JSONStringer();
			str.object();
			str.key("freq").value(ch.getFrequency());
			str.key("program_number").value(ch.getProgram());
			str.key("focus").value(focus);
			str.endObject();
			channel.transmitAsync(__ID_homed_observeProgramGuide, str.toString());
		} catch (JSONException e) {
			throw new RuntimeException(e.getMessage());
		}
	}

	@Override
	public final void captureVideoFrame(int id) {
		channel.transmitAsync(__ID_homed_captureVideoFrame, id + "");
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
			case __ID_homed_onIpStoped:{
				JSONObject o = (JSONObject) new JSONTokener(json).nextValue();
				onIpStoped(o.getLong("f"),o.getInt("pn"));
				break;
			}
			case __ID_onVolumeChange:
				onVolumeChange(Float.parseFloat(json));
				break;
			case __ID_onSyncMediaTime:
				onSyncMediaTime(Long.parseLong(json));
				break;
			case __ID_onPlayError:
				onPlayError(json);
				break;
			case __ID_onDescramError:{
				JSONObject o = (JSONObject) new JSONTokener(json).nextValue();
				onDescramError(o.getLong("f"),o.getInt("pn"),o.getInt("code"),o.getString("err"));
			}
			case __ID_homed_onProgramLost:
				onProgramLost();
				break;
			case __ID_homed_onProgramReselected:
				onProgramReselected();
				break;
			case __ID_homed_onResponseStart:
				onResponseStart(Boolean.parseBoolean(json));
				break;
			case __ID_homed_onLiveInfoUpdated:
				onLiveInfoUpdated(Integer.parseInt(json));
				break;
			case __ID_homed_onCaModuleDispatched:
				onCaModuleDispatched(Integer.parseInt(json));
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
	public void onProgramLost() {
	}

	@Override
	public void onProgramReselected() {
	}

	@Override
	public void onLiveInfoUpdated(int mask) {
	}

	@Override
	public void onCaModuleDispatched(int moduleId) {
	}

	@Override
	public void onSourceMediaChange(long time, long pts) {
		// TODO Auto-generated method stub

	}
	
	@Override
	public void onIpStoped(long f, int pn) {
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
	public void clearCache(int flags) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void onResponseStart(boolean succ) {
		// TODO Auto-generated method stub
		
	}

}
