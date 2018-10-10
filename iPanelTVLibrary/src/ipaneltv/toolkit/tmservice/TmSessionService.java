package ipaneltv.toolkit.tmservice;

import ipaneltv.toolkit.IPanelLog;
import ipaneltv.toolkit.JsonChannelService;
import ipaneltv.toolkit.JsonParcelable;
import ipaneltv.toolkit.tm.TmSessionInterface;

import java.util.HashMap;
import java.util.Map;

import org.json.JSONException;

import android.os.Bundle;

@SuppressWarnings("rawtypes")
public class TmSessionService extends JsonChannelService {

	private static final String TAG = TmSessionService.class.getSimpleName();
	Session session;
	Map<String, Session> sessions = new HashMap<String, JsonChannelService.Session>();

	HashMap<String, Class<? extends TmSessionBaseContext>> contextClass = new HashMap<String, Class<? extends TmSessionBaseContext>>();

	public void registerSessionType(Class<? extends TmSessionBaseContext> base,
			Class<? extends TmSessionBaseContext> impl) {
		if (!base.isAssignableFrom(impl))
			throw new RuntimeException("impl is not sub class of base");
		synchronized (contextClass) {
			contextClass.put(base.getName(), impl);
			IPanelLog.d(TAG, "registerSessionType sessionname=" + base.getName());
			IPanelLog.d(TAG, "registerSessionType impl=" + impl);
		}
	}

	public void registerSessionType(String name, Class<? extends TmSessionBaseContext> impl) {
		synchronized (contextClass) {
			contextClass.put(name, impl);
		}
	}

	@Override
	public final Session createSession(String interfaceName) {
		Session s = sessions.get(interfaceName);
		if (s != null) {
			return null;
		}

		synchronized (contextClass) {
			Class<? extends TmSessionBaseContext> clazz = contextClass.get(interfaceName);
			IPanelLog.d(TAG, "createSession clazz = " + clazz + ",interfaceName =" + interfaceName);
			if (clazz != null) {
				try {
					s = clazz.getConstructor(getClass()).newInstance(this);
					if (s != null) {
						sessions.put(interfaceName, s);
					}
					return s;
				} catch (Exception e) {
					e.printStackTrace();
					IPanelLog.d(TAG, "create play context failed:" + interfaceName + ", class is:"
							+ clazz);
				}
			}
		}
		return null;
	}
}

abstract class TmSessionBaseContext<T extends TmSessionService> extends JsonChannelService.Session
		implements TmSessionInterface {
	T service;

	public TmSessionBaseContext(T service) {
		this.service = service;
	}

	public T getService() {
		return service;
	}

	@Override
	public String onTransmit(int code, String json, JsonParcelable p, Bundle b)
			throws JSONException {
		switch (code) {
		case __ID_close:
			close();
			break;
		default:
			break;
		}
		return null;
	}

	@Override
	public void close() {
		onClose();
	}

}