package ipaneltv.toolkit.media;

import ipaneltv.toolkit.JsonParcelable;
import ipaneltv.toolkit.db.DatabaseObjectification.ChannelKey;
import ipaneltv.toolkit.media.MediaSessionInterface.LiveProgramPlayerInterface;

import org.json.JSONException;
import org.json.JSONObject;
import org.json.JSONStringer;
import org.json.JSONTokener;

import android.content.Context;
import android.graphics.Rect;
import android.os.Bundle;
import android.os.ParcelFileDescriptor;
import android.util.Log;

public class LiveProgramPlayer extends TeeveePlayerBase implements
		LiveProgramPlayerInterface, LiveProgramPlayerInterface.Callback {
	final static String TAG = LiveProgramPlayer.class.getSimpleName();
	public static final int WIDGET_FALG_TIPS = 0x01;
	static final int GROUP = 0, CHANNEL = 1, PRESENT = 2, FOLLOW = 3;

	public LiveProgramPlayer(Context context, String serviceName) {
		super(context, serviceName, LiveProgramPlayerInterface.class.getName());
	}

	public final void select(long freq, int fflags, int program, int pflags) {
		Log.i(TAG, "freq = " + freq + ";fflags = " + fflags + ";program = "
				+ program + ";pflags = " + pflags);
		try {
			JSONStringer str = new JSONStringer();
			str.object();
			str.key("freq").value(freq);
			str.key("fflags").value(fflags);
			str.key("program").value(program);
			str.key("pflags").value(pflags);
			str.endObject();
			Log.i(TAG, "str.toString() = " + str.toString());
			channel.transmit(__ID_select, str.toString(), null);/*-update version;*/
		} catch (JSONException e) {
			throw new RuntimeException(e.getMessage());
		}
	}

	public final void select(String furi, int fflags, String puri, int pflags) {
		try {
			if (furi == null) {
				furi = "null";
			}
			if (puri == null) {
				puri = "null";
			}
			JSONStringer str = new JSONStringer();
			str.object();
			str.key("furi").value(furi);
			str.key("fflags").value(fflags);
			str.key("puri").value(puri);
			str.key("pflags").value(pflags);
			str.endObject();
			channel.transmit(__ID_select_2, str.toString(), null);/*-update version;*/
		} catch (JSONException e) {
			throw new RuntimeException(e.getMessage());
		}
	}

	public final void setDisplayRect(Rect r) {
		setDisplay(r.left, r.top, r.right - r.left + 1, r.bottom - r.top + 1);
	}

	@Override
	public void checkPassword(String pwd) {
		channel.transmit(__ID_checkPassword, pwd);
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
		channel.transmitAsync(__ID_solveProblem);
	}

	@Override
	public final void enterCaApp(String uri) {
		channel.transmitAsync(__ID_enterCaApp, uri);
	}

	@Override
	public boolean pipOpenPlayers(int size, int flags) {
		try {
			JSONStringer str = new JSONStringer();
			str.object();
			str.key("size").value(size);
			str.key("flags").value(flags);
			str.endObject();
			String ret = channel.transmit(__ID_pipOpenPlayers, str.toString(),
					null);
			return (ret != null ? "true".equals(ret) : false);
		} catch (JSONException e) {
			throw new RuntimeException(e.getMessage());
		}
	}

	@Override
	public boolean pipClosePlayers() {
		String ret = channel.transmit(__ID_pipClosePlayers, null, null);
		return (ret != null ? "true".equals(ret) : false);
	}

	@Override
	public void pipSetFreqency(int index, long freq, int flags) {
		try {
			JSONStringer str = new JSONStringer();
			str.object();
			str.key("index").value(index);
			str.key("freq").value(freq);
			str.key("flags").value(flags);
			str.endObject();
			channel.transmit(__ID_pipSetFreqency, str.toString(), null);/*-update version;*/
		} catch (JSONException e) {
			throw new RuntimeException(e.getMessage());
		}
	}

	@Override
	public void pipSetProgram(int index, int prog, int x, int y, int w, int h,
			int flags) {
		try {
			JSONStringer str = new JSONStringer();
			str.object();
			str.key("index").value(index);
			str.key("prog").value(prog);
			str.key("x").value(x);
			str.key("y").value(y);
			str.key("w").value(w);
			str.key("h").value(h);
			str.key("flags").value(flags);
			str.endObject();
			channel.transmit(__ID_pipSetProgram, str.toString(), null);/*-update version;*/
		} catch (JSONException e) {
			throw new RuntimeException(e.getMessage());
		}
	}

	@Override
	public void pipLoadAnimation(ParcelFileDescriptor pfd) {
		Bundle b = new Bundle();
		b.putParcelable("pfd", pfd);
		Log.d(TAG, "pipLoadAnimation 2 jp = " + b + "; channel = " + channel);
		channel.transmit(__ID_pipLoadAnimation, null, null, b);/*-update version;*/
		Log.d(TAG, "pipLoadAnimation end");
	}

	@Override
	public void pipActAnimation(int action, int p1, int p2, int flags) {
		try {
			JSONStringer str = new JSONStringer();
			str.object();
			str.key("action").value(action);
			str.key("p1").value(p1);
			str.key("p2").value(p2);
			str.key("flags").value(flags);
			str.endObject();
			channel.transmit(__ID_pipActAnimation, str.toString(), null);/*-update version;*/
		} catch (JSONException e) {
			throw new RuntimeException(e.getMessage());
		}
	}

	@Override
	public boolean openTeeveeRecoder(int flags) {
		String ret = channel.transmit(__ID_openTeeveeRecoder, flags+"", null);
		Log.i(TAG, "openTeeveeRecoder transmit __ID_openTeeveeRecoder = "
				+ __ID_openTeeveeRecoder);
		return (ret != null ? "true".equals(ret) : false);
	}

	@Override
	public boolean setTeeveeRecoder(ParcelFileDescriptor pfd, long freq,int fflags,int pn, int pflags) {
		try {
			Bundle b = new Bundle();
			b.putParcelable("pfd", pfd);
			JSONStringer str = new JSONStringer();
			str.object();
			str.key("freq").value(freq);
			str.key("fflags").value(fflags);
			str.key("pn").value(pn);
			str.key("pflags").value(pflags);
			str.endObject();
			String ret = channel.transmit(__ID_setTeeveeRecoder,
					str.toString(), null, b);/*-update version;*/
			return (ret != null ? "true".equals(ret) : false);
		} catch (JSONException e) {
			throw new RuntimeException(e.getMessage());
		}
	}

	@Override
	public void closeTeeveeRecoder() {
		channel.transmit(__ID_closeTeeveeRecoder, null, null);
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
			channel.transmitAsync(__ID_observeProgramGuide, str.toString());
		} catch (JSONException e) {
			throw new RuntimeException(e.getMessage());
		}
	}

	@Override
	public final void captureVideoFrame(int id) {
		channel.transmitAsync(__ID_captureVideoFrame, id + "");
	}

	@Override
	protected final void onCallback(int code, String json, JsonParcelable a,
			Bundle b) {
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
			case __ID_onPlayError:
				onPlayError(json);
				break;
			case __ID_onDescramError:{
				JSONObject o = (JSONObject) new JSONTokener(json).nextValue();
				onDescramError(o.getLong("f"),o.getInt("pn"),o.getInt("code"),o.getString("err"));
				break;
			}
			case __ID_onProgramLost:
				onProgramLost();
				break;
			case __ID_onProgramReselected:
				onProgramReselected();
				break;
			case __ID_onLiveInfoUpdated:
				onLiveInfoUpdated(Integer.parseInt(json));
				break;
			case __ID_onCaModuleDispatched:
				onCaModuleDispatched(Integer.parseInt(json));
				break;
			case __ID_onResponseSelect:
				onResponseSelect(Boolean.parseBoolean(json));
				break;
			case __ID_onStreamLost:
				onStreamLost();
				break;
			case __ID_onRecordStart:
				onRecordStart(Integer.parseInt(json));
				break;
			case __ID_onRecordError: {
				JSONObject o = (JSONObject) new JSONTokener(json).nextValue();
				onRecordError(o.getInt("program_number"), o.getString("msg"));
				break;
			}
			case __ID_onChannelLocked: {
				JSONObject o = (JSONObject) new JSONTokener(json).nextValue();
				onChannelLocked(o.getLong("freq"), o.getInt("pn"));
				break;
			}
			case __ID_onPasswprdChecked:
				onPasswordChecked(Boolean.parseBoolean(json));
			case __ID_onRecordEnd:
				onRecordEnd(Integer.parseInt(json));
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
	public void onRecordStart(int program_number) {
		Log.d(TAG, "onRecordStart program_number = " + program_number);
	}

	@Override
	public void onRecordError(int program_number, String msg) {
		// TODO Auto-generated method stub

	}

	@Override
	public void onRecordEnd(int program_number) {
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
