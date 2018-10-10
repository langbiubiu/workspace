package com.ipanel.join.lib.dvb;

import ipaneltv.toolkit.entitlement.EntitlementObserver;
import ipaneltv.toolkit.media.MediaSessionInterface.TsPlayerInetSourceInterface;
import ipaneltv.toolkit.media.MediaSessionInterface.TsPlayerInetSourceInterface.Provider;
import android.content.Context;
import android.net.Uri;
import android.net.telecast.NetworkManager;
import android.net.telecast.TransportManager;
import android.net.telecast.ca.CAManager;

import com.ipanel.join.lib.dvb.live.LiveNavigator;
import com.ipanel.join.lib.dvb.live.NaviManager;

public class DVBConfig {
	public enum LivePlayerType {
		DVB, HOMED
	}

	private static String playService = "cn.ipanel.tvapps.network.NcPlayService";;
	private static String sourceService = "com.ipanel.apps.common.tsvodsrcservice";
	private static String tmService = "com.ipanel.apps.jx.tm.TmSessionService";
	private static String vodProvider = Provider.HomedHttp.getName();
	private static int vodStreamType = TsPlayerInetSourceInterface.STREAM_TYPE_INET;

	private static String uuid;

	private static NaviManager naviManager;

	private static LivePlayerType livePlayerType = LivePlayerType.DVB;

	public static void init(Context context, String uuid) {
		setUUID(uuid);
		ctx = context.getApplicationContext();
	}

	public static LivePlayerType getLivePlayerType() {
		return livePlayerType;
	}

	public static void setLivePlayerType(LivePlayerType type) {
		livePlayerType = type;
	}

	public static synchronized NaviManager getNaviManager() {
		if (naviManager == null) {
			naviManager = new NaviManager();
		}
		return naviManager;
	}

	public static Context getAppCtx() {
		return ctx;
	}

	public static void setUUID(String val) {
		uuid = val;
	}

	public static String getUUID() {
		return uuid;
	}

	public static String getPlayService() {
		return playService;
	}

	public static void setPlayService(String playService) {
		DVBConfig.playService = playService;
	}

	public static String getSourceService() {
		return sourceService;
	}

	public static void setSourceService(String sourceService) {
		DVBConfig.sourceService = sourceService;
	}

	public static String getVodProvider() {
		return vodProvider;
	}

	public static int getVodStreamType() {
		return vodStreamType;
	}

	public static void setVodProvider(Provider provider) {
		vodProvider = provider.getName();
		switch (provider) {
		case Sihua:
		case Huawei:
		case Seachange:
		case Ngod:
			vodStreamType = TsPlayerInetSourceInterface.STREAM_TYPE_IPQAM;
			break;

		default:
			vodStreamType = TsPlayerInetSourceInterface.STREAM_TYPE_INET;
			break;
		}
	}

	/**
	 * 
	 * @param type
	 *            TsPlayerInetSourceInterface.STREAM_TYPE_INET,
	 *            TsPlayerInetSourceInterface.STREAM_TYPE_IPQAM;
	 * 
	 */
	public static void setVodStreamType(int type) {
		vodStreamType = type;
	}

	public static String getTmService() {
		return tmService;
	}

	public static void setTmService(String tmService) {
		DVBConfig.tmService = tmService;
	}

	private static TransportManager transport;
	private static LiveNavigator navigator;
	private static NetworkManager netManager;
	private static CAManager caManager;
	private static EntitlementObserver entObserver;
	private static Uri dbUri = null;
	private static Context ctx;

	public static synchronized EntitlementObserver getEntitlementObserver() {
		if (entObserver == null) {
			entObserver = new EntitlementObserver(ctx);
			entObserver.prepare();
		}
		return entObserver;
	}

	public static synchronized TransportManager getTransportManager() {
		if (transport == null)
			transport = TransportManager.getInstance(ctx);
		return transport;
	}

	public static synchronized Uri getNetworkDatabaseUri() {
		if (dbUri == null)
			dbUri = getNetworkManager().getNetworkDatabaseUri(uuid);
		return dbUri;
	}

	public static synchronized LiveNavigator getLiveNavigator() {
		if (navigator == null) {
			navigator = new LiveNavigator(ctx, getNetworkDatabaseUri());
		}
		return navigator;
	}

	public static synchronized NetworkManager getNetworkManager() {
		if (netManager == null)
			netManager = NetworkManager.getInstance(ctx);
		return netManager;
	}

	public static synchronized CAManager getCAManager() {
		if (caManager == null)
			caManager = CAManager.createInstance(ctx);
		return caManager;
	}

	public static String getCACardId() {
		CAManager cam = getCAManager();
		try {
			int[] modid = cam.getCAModuleIDs(uuid);

			if (modid != null) {
				for (int mid : modid) {
					String cardNum = cam.getCAModuleProperty(mid, "CA_CARD_NUMBER");
					if (cardNum != null && cardNum.length() > 0) {
						return cardNum;
					}
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		return "";
	}

}
