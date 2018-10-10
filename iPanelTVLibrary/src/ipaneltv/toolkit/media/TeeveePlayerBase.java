package ipaneltv.toolkit.media;

import ipaneltv.toolkit.IPanelLog;

import org.json.JSONStringer;

import android.content.Context;
import android.graphics.Rect;

public abstract class TeeveePlayerBase extends MediaSessionClient implements
		MediaSessionInterface.TeeveePlayerBaseInterface {

	public TeeveePlayerBase(Context context, String serviceName, String sessionName) {
		super(context, serviceName, sessionName);
	}

	@Override
	public final void stop(int flag) {
		channel.transmit(__ID_stop, flag + "");
	}

	@Override
	public final void setVolume(float v) {
		channel.transmit(__ID_setVolume, v + "");
	}
	
	@Override
	public long getPlayTime() {
		return Long.parseLong(channel.transmit(__ID_getPlayTime));
	}

	public final void setDisplay(Rect r) {
		int w = r.right - r.left;
		int h = r.bottom - r.top;
		IPanelLog.d(TAG, "setDisplay r.left = "+ r.left+";r.top = "+ r.top+";w = "+ w +";h = "+ h);
		setDisplay(r.left, r.top, w, h);
	}

	@Override
	public final void setDisplay(int x, int y, int w, int h) {
		try {
			JSONStringer str = new JSONStringer();
			str.object();
			str.key("x").value(x).key("y").value(y).key("w").value(w).key("h").value(h);
			str.endObject();
			channel.transmit(__ID_setDisplay, str.toString());
		} catch (Exception e) {
		}
	}

	@Override
	public final void setTeeveeWidget(int flags) {
		channel.transmit(__ID_setTeeveeWidget, flags + "");
	}

	@Override
	public final void checkTeeveeWidget(int flags) {
		channel.transmitAsync(__ID_checkTeeveeWidget, flags + "");
	}

	@Override
	public final void setProgramFlags(int flags) {
		channel.transmit(__ID_setProgramFlags, flags + "");
	}

	@Override
	public final void syncMediaTime() {
		channel.transmitAsync(__ID_syncMediaTime);
	}

}