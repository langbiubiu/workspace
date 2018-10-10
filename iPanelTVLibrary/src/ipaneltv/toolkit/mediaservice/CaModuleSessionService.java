package ipaneltv.toolkit.mediaservice;

import android.app.Application;

public class CaModuleSessionService extends MediaSessionService {

	public Application getApp() {
		return (Application) getApplicationContext();
	}
}
