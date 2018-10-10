package ipaneltv.toolkit.media;

import ipaneltv.toolkit.IPanelLog;
import ipaneltv.toolkit.JsonParcelable;
import ipaneltv.toolkit.media.MediaSessionInterface.TsPlayerFileSourceInterface;

import org.json.JSONException;
import org.json.JSONStringer;

import android.content.Context;
import android.os.Bundle;

/**
 * 将本地文件封装为LocalSocket的数据源
 */
public class TsPlayerFileSource extends TsLocalSockSourceBase implements
		TsPlayerFileSourceInterface, TsPlayerFileSourceInterface.Callback {
	final static String TAG = TsPlayerFileSource.class.getSimpleName();

	public TsPlayerFileSource(Context context, String serviceName) {
		super(context, serviceName, TsPlayerFileSourceInterface.class.getName());
	}

	private String path;
	int flags;
	String localSockName;

	public static TsPlayerFileSourceInterface createFromLocalFile(String path) {
		return null;
	}

	@Override
	public final void start(String url, int flags) {
		try {
			JSONStringer s = new JSONStringer();
			s.object();
			s.key("uri").value(url);
			s.key("flags").value(flags);
			s.endObject();
			channel.transmit(__ID_start, s.toString());
		} catch (JSONException e) {
		}
	}

	@Override
	public String getFilePath() {
		return path;
	}

	@Override
	protected final void onCallback(int code, String json, JsonParcelable p, Bundle b) {
		try {
			IPanelLog.d(TAG, "onCallback> code=" + code + ",json=" + json);
			switch (code) {
			case __ID_onSourceRewind:
				onSourceRewind();
				break;
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
	public void onSourceRewind() {
	}
}
