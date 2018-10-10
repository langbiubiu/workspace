package ipaneltv.toolkit.mediaservice;

import ipaneltv.toolkit.IPanelLog;
import ipaneltv.toolkit.JsonChannelService;
import ipaneltv.toolkit.media.MediaSessionInterface;

import java.util.HashMap;

public abstract class MediaPlaySessionService extends MediaSessionService {
	@SuppressWarnings("rawtypes")
	public LiveNetworkApplication getApp() {
		return (LiveNetworkApplication) getApplicationContext();
	}
}

@SuppressWarnings("rawtypes")
abstract class MediaSessionService extends JsonChannelService {
	public static final String TAG = MediaPlaySessionService.class.getSimpleName();
	HashMap<String, Class<? extends MediaSessionContext>> contextClass = new HashMap<String, Class<? extends MediaSessionContext>>();

	public void registerSessionType(Class<? extends MediaSessionInterface> base,
			Class<? extends MediaSessionContext> impl) {
		if (!base.isAssignableFrom(impl))
			throw new RuntimeException("impl is not sub class of base");
		synchronized (contextClass) {
			contextClass.put(base.getName(), impl);
			IPanelLog.d(TAG, "registerSessionType sessionname=" + base.getName());
			IPanelLog.d(TAG, "registerSessionType impl=" + impl);
		}
	}

	public void registerSessionType(String name, Class<? extends MediaSessionContext> impl) {
		synchronized (contextClass) {
			contextClass.put(name, impl);
		}
	}

	@Override
	public final Session createSession() {
		return null;// yes!
	}

	@Override
	public final Session createSession(String interfaceName) {
		synchronized (contextClass) {
			Class<? extends MediaSessionContext> clazz = contextClass.get(interfaceName);
			IPanelLog.d(TAG, "createSession clazz = " + clazz + ",interfaceName =" + interfaceName);
			if (clazz != null) {
				try {
					return clazz.getConstructor(getClass()).newInstance(this);
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
