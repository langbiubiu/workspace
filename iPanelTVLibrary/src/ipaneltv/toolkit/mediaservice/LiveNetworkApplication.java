package ipaneltv.toolkit.mediaservice;

import ipaneltv.toolkit.mediaservice.components.CaDescramblingManager;
import ipaneltv.toolkit.mediaservice.components.LiveDataManager;
import ipaneltv.toolkit.mediaservice.components.LiveWidgetPlayer;
import ipaneltv.toolkit.mediaservice.components.PlayResourceScheduler;
import ipaneltv.toolkit.mediaservice.components.PlayWidgetManager;
import android.app.Application;
import android.net.telecast.NetworkManager;
import android.net.telecast.SignalStatus;
import android.net.telecast.TransportManager;
import android.net.telecast.ca.CAManager;

public abstract class LiveNetworkApplication<//
T_CaDescramblingManager extends CaDescramblingManager, //
T_LiveDataManager extends LiveDataManager, //
T_LiveWidgetPlayer extends LiveWidgetPlayer, //
T_PlayResourceScheduler extends PlayResourceScheduler, //
T_PlayWidgetManager extends PlayWidgetManager> extends Application {
	private T_LiveDataManager liveDataManager;
	private T_PlayWidgetManager playWidgetManager;
	private T_PlayResourceScheduler resSchduler;
	private T_LiveWidgetPlayer widgetPlayer;
	private T_CaDescramblingManager descramblingManager;
	private NetworkManager networkManager;
	private TransportManager transport;
	private CAManager caManager;
	private TunerInfo info;

	public final String uuid, searchUUID;
	public final int deliveryType;

	public LiveNetworkApplication(String uuid, String searchUUID, int deliveryType) {
		this.uuid = uuid;
		this.searchUUID = searchUUID;
		this.deliveryType = deliveryType;
	}

	public final synchronized TransportManager getTransportManager() {
		if (transport == null)
			transport = TransportManager.getInstance(this);
		return transport;
	}

	public final synchronized NetworkManager getNetworkManager() {
		if (networkManager == null)
			networkManager = NetworkManager.getInstance(this);
		return networkManager;
	}

	public final synchronized CAManager getCAManager() {
		if (caManager == null)
			caManager = CAManager.createInstance(this);
		return caManager;
	}

	public final CAManager createCAManager() {
		return CAManager.createInstance(this);
	}

	public synchronized T_LiveDataManager getLiveDataManager() {
		if (liveDataManager == null) {
			liveDataManager = createLiveDataManager();
		}
		return liveDataManager;
	}

	public synchronized T_PlayWidgetManager getPlayWidgetManager() {
		if (playWidgetManager == null) {
			playWidgetManager = createPlayWidgetManager();
		}
		return playWidgetManager;
	}

	public synchronized T_PlayResourceScheduler getResourceScheduler() {
		if (resSchduler == null) {
			resSchduler = createPlayResourceScheduler();
		}
		return resSchduler;
	}

	public synchronized T_LiveWidgetPlayer getWidgetPlayer() {
		if (widgetPlayer == null) {
			widgetPlayer = createLiveWidgetPlayer();
		}
		return widgetPlayer;
	}

	public synchronized T_CaDescramblingManager getCaDescramblingManager() {
		if (descramblingManager == null)
			descramblingManager = createCaDescramblingManager();
		return descramblingManager;
	}

	public synchronized void setTunerInfo(TunerInfo info) {
		this.info = info;
	}

	public synchronized TunerInfo getTunerInfo() {
		return info;
	}

	protected String getWardshipRoot() {
		return null;
	}

	protected abstract T_CaDescramblingManager createCaDescramblingManager();

	protected abstract T_PlayWidgetManager createPlayWidgetManager();

	protected abstract T_LiveDataManager createLiveDataManager();

	protected abstract T_PlayResourceScheduler createPlayResourceScheduler();

	protected abstract T_LiveWidgetPlayer createLiveWidgetPlayer();

	@SuppressWarnings("rawtypes")
	public static class AppComponent {

		private final LiveNetworkApplication app;

		public AppComponent(LiveNetworkApplication app) {
			this.app = app;
		}

		public LiveNetworkApplication getApp() {
			return app;
		}

		public String getUUID() {
			return app.uuid;
		}

		public String getSearchUUID() {
			return app.searchUUID;
		}
	}

	public static class TunerInfo {
		public SignalStatus ss = null;;
		public long freq= -1;
		public String symbol_rate = null;
		public String modulation = null;
		public int status = -1;
	}
}
