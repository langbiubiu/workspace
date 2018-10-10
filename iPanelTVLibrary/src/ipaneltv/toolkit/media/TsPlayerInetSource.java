package ipaneltv.toolkit.media;

import ipaneltv.toolkit.IPanelLog;
import ipaneltv.toolkit.JsonParcelable;
import ipaneltv.toolkit.media.MediaSessionInterface.TsPlayerInetSourceInterface;

import org.json.JSONObject;
import org.json.JSONStringer;
import org.json.JSONTokener;

import android.content.Context;
import android.os.Bundle;
import android.os.ParcelFileDescriptor;

public class TsPlayerInetSource extends TsLocalSockSourceBase implements
		TsPlayerInetSourceInterface, TsPlayerInetSourceInterface.Callback {
	final static String TAG = TsPlayerInetSource.class.getSimpleName();

	public TsPlayerInetSource(Context context, String serviceName, String sessionName) {
		super(context, serviceName, sessionName);
	}

	public TsPlayerInetSource(Context context, String serviceName) {
		this(context, serviceName, TsPlayerInetSourceInterface.class.getName());
	}

	@Override
	public void start(String uri, int type, int streamType, int flags) {
		try {
			JSONStringer s = new JSONStringer();
			s.object();
			s.key("uri").value(uri);
			s.key("type").value(type);
			s.key("stype").value(streamType);
			s.key("flags").value(flags);
			s.endObject();
			channel.transmit(__ID_start, s.toString());
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	@Override
	public void start(ParcelFileDescriptor pfd, String uri, int type, int streamType, int flags) {
		try {
			JSONStringer s = new JSONStringer();
			s.object();
			s.key("uri").value(uri);
			s.key("type").value(type);
			s.key("stype").value(streamType);
			s.key("flags").value(flags);
			s.endObject();
			JsonParcelable p = new JsonParcelable();
			IPanelLog.d(TAG, "start pfd " + pfd);
			p.put("pfd", pfd);
			channel.transmit(__ID_start_2, s.toString(),p);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	@Override
	public final void setRate(float rate) {
		channel.transmit(__ID_setRate, "" + rate);
	}

	@Override
	public void setCache(int bufsize) {
		channel.transmit(__ID_setCache, "" + bufsize);
	}

	@Override
	public void playCache() {
		channel.transmit(__ID_playCache);
	}

	@Override
	protected void onCallback(int code, String json, JsonParcelable p, Bundle b) {
		try {
			IPanelLog.d(TAG, "onCallback> code=" + code + ",json=" + json);
			switch (code) {
			case __ID_onSourceRate:
				onSourceRate(Float.parseFloat(json));
				break;
			case __ID_onShiftStartTime:
				onShiftStartTime(Long.parseLong(json));
				break;
			case __ID_onSeeBackPeriod: {
				JSONObject o = (JSONObject) new JSONTokener(json).nextValue();
				onSeeBackPeriod(o.getLong("start"), o.getLong("end"));
				break;
			}
			case __ID_onVodDuration:
				onVodDuration(Long.parseLong(json));
				break;
			case __ID_onCachingState:
				onCachingState(Float.parseFloat(json));
				break;
			case __ID_onSourceMediaChange: {
				JSONObject o = (JSONObject) new JSONTokener(json).nextValue();
				onSourceMediaChange(o.getLong("time"), o.getLong("pts"));
				break;
			}
			default:
				super.onCallback(code, json, p, b);
				break;
			}
		} catch (Exception e) {
			if (p != null)
				p.clean();
		}
	}

	@Override
	public void onSourceRate(float r) {
	}

	@Override
	public void onShiftStartTime(long start) {
	}

	@Override
	public void onSeeBackPeriod(long start, long end) {
	}

	@Override
	public void onVodDuration(long d) {
	}

	@Override
	public void onCachingState(float p) {
	}

	@Override
	public void onSourceMediaChange(long time, long pts) {
	}
}
