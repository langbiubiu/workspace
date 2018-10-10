package cn.ipanel.dlna;

import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.SocketException;
import java.util.Enumeration;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import org.apache.http.conn.util.InetAddressUtils;
import org.cybergarage.net.HostInterface;
import org.cybergarage.upnp.Action;
import org.cybergarage.upnp.Argument;
import org.cybergarage.upnp.ArgumentList;
import org.cybergarage.upnp.Device;
import org.cybergarage.upnp.DeviceList;
import org.cybergarage.upnp.ServiceList;
import org.cybergarage.upnp.UPnP;
import org.cybergarage.upnp.device.DeviceChangeListener;
import org.cybergarage.upnp.device.USN;
import org.cybergarage.upnp.event.EventListener;
import org.cybergarage.upnp.std.av.controller.MediaController;
import org.cybergarage.upnp.std.av.renderer.AVTransport;
import org.cybergarage.upnp.std.av.renderer.AVTransportInfo;
import org.cybergarage.upnp.std.av.renderer.Event;
import org.cybergarage.upnp.std.av.server.Directory;
import org.cybergarage.upnp.std.av.server.object.item.ItemNode;
import org.cybergarage.util.Debug;
import org.cybergarage.xml.Node;
import org.cybergarage.xml.Parser;
import org.cybergarage.xml.ParserException;
import org.json.JSONException;
import org.json.JSONObject;

import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.ConnectivityManager;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.Looper;
import android.os.RemoteException;
import android.text.TextUtils;
import android.util.Log;
import android.webkit.MimeTypeMap;

public class UPnPService extends Service implements DeviceChangeListener,
		EventListener {
	public static final String TAG = "UPnPService";
	
	public static final String ACTION_INFO_BROADCAST = "cn.ipanel.upnp.monitor.DEVICE_INFO";

	public interface PlayerControlListener {
		public void stop();

		/**
		 * 
		 * @return media duration in seconds
		 */
		public int getDuration();

		/**
		 * 
		 * @return media position in seconds
		 */
		public int getPosition();
		
		/**
		 * 
		 * @param position media position in seconds
		 */
		public void seek(int position);
		
		public void setPause(boolean pause);
	}

//	public static MediaServer sMediaServer;
//	public static MediaPlayer sMediaPlayer;
	
	//public static KeypadMouseDevice sKeypadMouseDevice;

	public static MediaController mControlPoint;

//	private static PlayerControlListener sPlayListener;

	public static DeviceChangeListener sDeviceListener;

	private static boolean running = false;
	
	private static Handler uiHandler = new Handler(Looper.getMainLooper());

	BroadcastReceiver networkStateReceiver = new BroadcastReceiver() {

		@Override
		public void onReceive(Context context, Intent intent) {
			ConnectivityManager cm = (ConnectivityManager) context.getSystemService(CONNECTIVITY_SERVICE);
			if(cm != null && cm.getActiveNetworkInfo() != null && cm.getActiveNetworkInfo().isConnected()){
				loadNetworkInfo();
				HostInterface.setInterface(IP);
				startServer(true);
			}
			
		}

	};

	public Upnp.Stub Ibind = new Upnp.Stub() {
		
		@Override
		public String getWifiApInfo() throws RemoteException {
			// TODO Auto-generated method stub
//			getWifiInfo();
			Log.d("wangkang", "start getwifiinfo");
			String result =getWifiInfo(); 
			
			Log.d("wangkang", "start getwifiinfo result");
			return result;
		}
		
		@Override
		public String getGateWayInfo() throws RemoteException {
			// TODO Auto-generated method stub
			Log.d("wangkang", "start getGatewayInfo");
			String result = getGateway();
			Log.d("wangkang", "start getGatewayInfo result");
			return result;
		}

		@Override
		public String setWifiAp(String data) throws RemoteException {
			// TODO Auto-generated method stub
			return SetWifiApInfo(data);
		}

		@Override
		public String setWifiApEnable(String enable) throws RemoteException {
			// TODO Auto-generated method stub
			Log.d("wangkang", "setWifiApEnable enable==="+enable);
			return setWifiEnable(enable);
		}

		@Override
		public int reset(int flag) throws RemoteException {
			// TODO Auto-generated method stub
			return 0;
		}
	}; 
	@Override
	public IBinder onBind(Intent intent) {
		Log.d(TAG, "onBind  Upnp.Stub()");
		return Ibind;
	}
	 @Override
	public boolean onUnbind(Intent intent) {
		System.out.println("Service解绑成功，onUnbind");
		return super.onUnbind(intent);
	}
	@Override
	public void onCreate() {
		Log.d(TAG, "onCreate()");
		Debug.on();
		registerNetworkMonitor();
		super.onCreate();
	}
	
//	ActionListener rendererListener = new ActionListener() {
//
//		@Override
//		public boolean actionControlReceived(Action action) {
//
//			String actionName = action.getName();
//			Logger.d("onRendererAction: " + actionName);
//			AVTransport transport = sMediaPlayer.getRenderer().getAVTransport();
//			Logger.d(AVTransport.TRANSPORTSTATE + " = "
//					+ transport.getStateVariable(AVTransport.TRANSPORTSTATE).getValue());
//			if(AVTransport.GETDEVICECAPABILITIES.equals(actionName)){
//				action.getArgument(AVTransport.PLAYMEDIA).setValue("NONE,NETWORK,UNKNOWN");
//				action.getArgument(AVTransport.RECMEDIA).setValue("NOT_IMPLEMENTED");
//				action.getArgument(AVTransport.RECQUALITYMODES).setValue("NOT_IMPLEMENTED");
//				return true;
//			} else if(AVTransport.GETTRANSPORTSETTINGS.equals(actionName)){
//				action.getArgument(AVTransport.PLAYMODE).setValue(AVTransport.NORMAL);
//				action.getArgument(AVTransport.RECQUALITYMODE).setValue("NOT_IMPLEMENTED");
//				return true;
//			} else if (AVTransport.STOP.equals(actionName)) {
//				stopPlay();
//				sMediaPlayer
//						.getRenderer()
//						.getAVTransport()
//						.updateStateVariable(AVTransport.TRANSPORTSTATE,
//								AVTransport.STOPPED);
//				return true;
//			} else if (AVTransport.PLAY.equals(actionName)) {
//				if (sPlayListener != null
//						&& AVTransport.PAUSED_PLAYBACK.equals(transport.getStateVariable(
//								AVTransport.TRANSPORTSTATE).getValue())) {
//					pausePlay(false);
//				} else {
//					play(transport.getCurrentAvTransInfo());
//				}
//				sMediaPlayer
//						.getRenderer()
//						.getAVTransport()
//						.updateStateVariable(AVTransport.TRANSPORTSTATE,
//								AVTransport.PLAYING);
//				return true;
//			} else if (AVTransport.PAUSE.equals(actionName)){
//				if (pausePlay(true)) {
//					sMediaPlayer
//							.getRenderer()
//							.getAVTransport()
//							.updateStateVariable(AVTransport.TRANSPORTSTATE,
//									AVTransport.PAUSED_PLAYBACK);
//				}
//				return true;
//			} else if(AVTransport.SEEK.equals(actionName)) {
//				String unit = action.getArgumentValue(AVTransport.UNIT);
//				if (AVTransport.REL_TIME.equals(unit) || AVTransport.ABS_TIME.equals(unit)
//						|| AVTransport.RELTIME.equals(unit) || AVTransport.ABSTIME.equals(unit)) {
//					final int seekTime = Util.parseMediaTime(action
//							.getArgumentValue(AVTransport.TARGET));
//					if (sPlayListener != null)
//						uiHandler.post(new Runnable() {
//
//							@Override
//							public void run() {
//								sPlayListener.seek(seekTime);
//							}
//						});
//				}
//				return true;
//			} else if (AVTransport.GETPOSITIONINFO.equals(actionName)) {
//				if (sPlayListener != null) {
//					action.getArgument(AVTransport.TRACK).setValue(1);
//					action.getArgument(AVTransport.TRACKDURATION).setValue(
//							Util.formatMediaTime(sPlayListener.getDuration()));
//					action.getArgument(AVTransport.RELTIME).setValue(
//							Util.formatMediaTime(sPlayListener.getPosition()));
//					action.getArgument(AVTransport.ABSTIME).setValue(
//							Util.formatMediaTime(sPlayListener.getPosition()));
//				} else {
//					action.getArgument(AVTransport.TRACK).setValue(0);
//					action.getArgument(AVTransport.TRACKDURATION).setValue("00:00:00");
//					action.getArgument(AVTransport.RELTIME).setValue("00:00:00");
//					action.getArgument(AVTransport.ABSTIME).setValue("00:00:00");
//				}
////				if (transport.getCurrentAvTransInfo() != null) {
////					action.getArgument(AVTransport.TRACKURI).setValue(
////							transport.getCurrentAvTransInfo().getURI());
////					action.getArgument(AVTransport.TRACKMETADATA).setValue(
////							transport.getCurrentAvTransInfo().getURIMetaData());
////				}
//				action.getArgument(AVTransport.RELCOUNT).setValue(2147483647);
//				action.getArgument(AVTransport.ABSCOUNT).setValue(2147483647);
//				return true;
//			} else if (AVTransport.GETTRANSPORTINFO.equals(actionName)) {
//				action.getArgument(AVTransport.CURRENTTRANSPORTSTATE)
//							.setValue(transport.getStateVariable(
//									AVTransport.TRANSPORTSTATE).getValue());
//				action.getArgument(AVTransport.CURRENTTRANSPORTSTATUS)
//						.setValue(AVTransport.OK);
//				action.getArgument(AVTransport.CURRENTSPEED).setValue("1");
//				return true;
//			} else if (RenderingControl.GETVOLUME.equals(actionName)){
//				AudioManager am = (AudioManager) getSystemService(AUDIO_SERVICE);
//				int max = am.getStreamMaxVolume(AudioManager.STREAM_MUSIC);
//				int current = am.getStreamVolume(AudioManager.STREAM_MUSIC);
//				action.getArgument(RenderingControl.CURRENTVOLUME).setValue(100*current/max);
//				return true;
//			} else if(RenderingControl.SETVOLUME.equals(actionName)){
//				AudioManager am = (AudioManager) getSystemService(AUDIO_SERVICE);
//				int max = am.getStreamMaxVolume(AudioManager.STREAM_MUSIC);
//				am.setStreamVolume(
//						AudioManager.STREAM_MUSIC,
//						action.getArgumentIntegerValue(RenderingControl.DESIREDVOLUME)
//								* max / 100, AudioManager.FLAG_SHOW_UI);
//				return true;
//			} else if(RenderingControl.SETMUTE.equals(actionName)){
//				AudioManager am = (AudioManager) getSystemService(AUDIO_SERVICE);
//				am.setStreamMute(AudioManager.STREAM_MUSIC, action.getArgumentIntegerValue(RenderingControl.DESIREDMUTE)>0);
//				return true;
//			}
//			return false;
//		}
//	};
	public static String IP;
	public static String MAC;
	
	public static void loadNetworkInfo(){
		try {
			Enumeration<NetworkInterface> enu = NetworkInterface.getNetworkInterfaces();
			while(enu.hasMoreElements()){
				NetworkInterface ni = enu.nextElement();
				Log.d(TAG, "Network interface "+ ni.getName());
				byte[] mac = ni.getHardwareAddress();
				String macStr = null;
				String ipStr = null;
                if (mac!=null){ 
	                StringBuilder buf = new StringBuilder();
	                for (int idx=0; idx<mac.length; idx++)
	                    buf.append(String.format("%02X:", mac[idx]));       
	                if (buf.length()>0) buf.deleteCharAt(buf.length()-1);
	                
	                macStr = buf.toString();
	                Log.d(TAG, "mac: "+macStr);
                }
                Enumeration<InetAddress> aenu = ni.getInetAddresses();
                while(aenu.hasMoreElements()){
                	InetAddress addr = aenu.nextElement();
                	if(!addr.isLoopbackAddress()){
                		String sAddr = addr.getHostAddress().toUpperCase();
                        boolean isIPv4 = InetAddressUtils.isIPv4Address(sAddr);
                        if(isIPv4){
	                		ipStr = addr.getHostAddress();
	                		Log.d(TAG, "ip:"+ipStr);
                        }
                	}
                }
                if(macStr != null && ipStr != null){
                	MAC = macStr;
                	IP = ipStr;
                }
			}
		} catch (SocketException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
	}
	protected void initServer() {
		loadNetworkInfo();
		HostInterface.setInterface(IP);
		
		UPnP.setEnable(UPnP.USE_ONLY_IPV4_ADDR);
		
//		Bitmap icon = BitmapFactory.decodeResource(getResources(),
//				R.drawable.ic_launcher);
//		ByteArrayOutputStream bao = new ByteArrayOutputStream();
//		icon.compress(CompressFormat.PNG, 100, bao);
//		byte[] iconData = bao.toByteArray();
//		if (sMediaPlayer == null)
//			UPnPService.sMediaPlayer = new MediaPlayer(
//					DeviceBuilder.createRenderer(getApplicationContext(),
//							Build.MODEL + " Renderer"),
		mControlPoint = new MediaController();//);
//		sMediaPlayer.getRenderer().setDeviceIcon(iconData);
		mControlPoint.addDeviceChangeListener(this);
		mControlPoint.addEventListener(this);

//		UPnPService.sMediaPlayer.getRenderer().setActionListener(
//				rendererListener);
//
//		if (sMediaServer == null) {
//			UPnPService.sMediaServer = DeviceBuilder.createServer(
//					getApplicationContext(), Build.MODEL + " Media Server");
//			UPnPService.sMediaServer.addPlugIn(new MPEGFormat());
//			UPnPService.sMediaServer.addPlugIn(new GIFFormat());
//			UPnPService.sMediaServer.addPlugIn(new PNGFormat());
//			UPnPService.sMediaServer.addPlugIn(new JPEGFormat());
//			UPnPService.sMediaServer.addPlugIn(new ID3Format());
//			UPnPService.sMediaServer.addPlugIn(new MusicFormat());
//			UPnPService.sMediaServer.addPlugIn(new MovieFormat());
//		}
//		sMediaServer.setDeviceIcon(iconData);
		
//		if(sKeypadMouseDevice == null)
//			sKeypadMouseDevice = new KeypadMouseDevice();
//		sKeypadMouseDevice.setDeviceIcon(iconData);
	}

	private void registerNetworkMonitor() {
		IntentFilter intentFilter = new IntentFilter();
		intentFilter.addAction(ConnectivityManager.CONNECTIVITY_ACTION);
		registerReceiver(networkStateReceiver, intentFilter);
	}

	Directory videos, musics, photos;

	protected void loadDMSContents() {
//		long start = SystemClock.elapsedRealtime();
//		if (videos == null)
//			videos = new ContentProviderDirectory(this, MediaType.Videos, getString(R.string.video));
//
//		if (musics == null)
//			musics = new ContentProviderDirectory(this, MediaType.Musics, getString(R.string.music));
//		if (photos == null)
//			photos = new ContentProviderDirectory(this, MediaType.Photos, getString(R.string.photos));
//		if (sMediaServer.getContentDirectory().getNDirectories() == 0) {
//			sMediaServer.addContentDirectory(videos);
//			sMediaServer.addContentDirectory(musics);
//			sMediaServer.addContentDirectory(photos);
//		}
//		//sMediaServer.getContentDirectory().getRootNode().print();
//		Logger.d("DMS loading time:" + (SystemClock.elapsedRealtime() - start));
	}

	public static boolean isRunning() {
		return running;
	}

	private static void stopPlay() {
//		if (sPlayListener != null) {
//			sPlayListener.stop();
//			sPlayListener = null;
//		}

	}
	
	static boolean pausePlay(final boolean pause){
//		if(sPlayListener != null) {
//			uiHandler.post(new Runnable() {
//				
//				@Override
//				public void run() {
//					sPlayListener.setPause(pause);
//				}
//			});
//			return true;
//		}
		return false;
	}

	public static void setPlayerListener(PlayerControlListener l) {
//		if (l != null)
//			stopPlay();
//		sPlayListener = l;
	}

	public static void clearPlayerListener(PlayerControlListener l) {
//		if (sPlayListener == l) {
//			mPool.submit(new Runnable() {
//				
//				@Override
//				public void run() {
//					sMediaPlayer.getRenderer().getAVTransport()
//					.updateStateVariable(AVTransport.TRANSPORTSTATE, AVTransport.STOPPED);
//				}
//			});
//			sPlayListener = null;
//		}
	}
	
	static ExecutorService mPool = Executors.newSingleThreadExecutor();

	protected void play(AVTransportInfo avTransportInfo) {

		String uri = avTransportInfo.getURI();
		String title = "";

		Parser xmlParser = UPnP.getXMLParser();

		boolean isImage = false;
		String mime = "";
		try {
			Node node = xmlParser.parse(avTransportInfo.getURIMetaData());
			Logger.d(node.toString());

			if (node.getNNodes() > 0) {
				Node child = node.getNode(0);
				if (ItemNode.isItemNode(child)) {
					ItemNode inode = new ItemNode();
					mime = inode.getMimeType();
					inode.set(child);
					if(inode.isAudioClass())
						mime="audio/*";
					isImage = inode.isImageClass();
					title = inode.getTitle();
					Logger.d(inode.toString());
				}
			}
		} catch (Exception e) {
			String ext = MimeTypeMap.getFileExtensionFromUrl(uri);

			mime = MimeTypeMap.getSingleton().getMimeTypeFromExtension(
					ext);
			isImage = mime != null && mime.startsWith("image");
		}

		stopPlay();

		Intent intent = new Intent(this, VideoViewer.class);
		if (isImage)
			intent.setClass(this, WebViewActivity.class);
		PlayEntry entry = new PlayEntry(title, uri, mime);
		intent.putExtra(PlayEntry.EXTRA_ENTRY, entry);
		intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
		startActivity(intent);

	}

	@Override
	public void onDestroy() {
		unregisterReceiver(networkStateReceiver);
		new Thread() {
			public void run() {
				stopServer();
			}
		}.start();
		super.onDestroy();
	}

	public static void stopServer() {
		if (running = false)
			return;
		Logger.d("stop UPnP servers");
//		if (sMediaPlayer != null)
//			sMediaPlayer.stop();
//		if (sMediaServer != null)
//			sMediaServer.stop();
//		if(sKeypadMouseDevice != null)
//			sKeypadMouseDevice.stop();
		mControlPoint.stop();
		running = false;
		Logger.d("UPnP servers stoped");
	}

	@Override
	public int onStartCommand(Intent intent, int flags, int startId) {
		handleIntent(intent);
		return Service.START_STICKY;
	}

	private void handleIntent(Intent intent) {
		String action = null;
		if (intent != null)
			action = intent.getAction();

		startServer(true);

	}

	Thread startThread;

	protected void startServer(boolean restart) {
		if ((!restart && running)
				|| (startThread != null && startThread.isAlive())) {
			Logger.d("Running: " + HostInterface.getIPv4Address());
			return;
		}
		startThread = new Thread() {
			public void run() {
				if (running)
					stopServer();

				Logger.d("start UPnP servers");
				initServer();

				mControlPoint.start();
//				if (sMediaPlayer != null)
//					sMediaPlayer.start();
//				loadDMSContents();
//				if (sMediaServer != null)
//					sMediaServer.start();
//				if (sKeypadMouseDevice != null)
//					sKeypadMouseDevice.start();
				mControlPoint.search(USN.ROOTDEVICE);
				running = true;
				Logger.d("UPnP server stared");
			}
		};
		startThread.start();
	}


	static final String CA_TYPE = "ManageableDevice:2";
	static final String IGD_TYPE = "InternetGatewayDevice:1";
	static final String HLM_TYPE = "LANDevice:1";
	
	Device sCADevice, sWanDevice, GatewayDevice, HomeLanManagementDevice,
			eRouterLANManageDevice, eMTALANManageDevice,eMTAWANConnectionLANManage,eRouterWANConnectionLANManage;
	String caid, stbip,caVendor;
	
	Runnable getDataTask = new Runnable() {
		
		@Override
		public void run() {
			try {
				Log.d(TAG, "getDeviceData begin");
				getCardNumber();
				getIP();
				getWifiInfo();
				getGateway();
//				if(!TextUtils.isEmpty(caid) || !TextUtils.isEmpty(stbip)){
					broadcastInfo();
//				}
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
	};
	
	@Override
	public void deviceAdded(Device dev) {
		processDevice(dev);
		
		if (sDeviceListener != null) {
			sDeviceListener.deviceAdded(dev);
		}
	}
	
	protected void broadcastInfo() {
		Log.d(TAG, "broadcast card and IP info");
		Intent intent = new Intent(ACTION_INFO_BROADCAST);
		intent.putExtra("caid", caid);
		intent.putExtra("stbip", stbip);
		intent.putExtra("WifiApInfo", WifiApInfo);	
		intent.putExtra("GateWayInfo", GateWayInfo);
		sendStickyBroadcast(intent);
		
	}

	
	
	void getDeviceData(){
		mPool.submit(getDataTask);
	}

	public void processDevice(Device dev) {
		if(dev == null)
			return;
		Log.d(TAG, "deviceAdded type="+dev.getDeviceType()+", name = "+dev.getFriendlyName());
		printServices(dev);
		if(dev.getDeviceType().endsWith(CA_TYPE)){
			Log.d(TAG, "found CA device");
			sCADevice = dev;
			getDeviceData();
		} else if (dev.getDeviceType().endsWith(IGD_TYPE)){
			Log.d(TAG, "found IGD device");
			GatewayDevice=dev;
			DeviceList dl = dev.getDeviceList();
			for(int i=0;i<dl.size();i++){
				Device sd = dl.getDevice(i);
				Log.d(TAG, "subdevice type="+sd.getDeviceType()+", model = "+sd.getModelName());
				if("eSTBLANManage".equals(sd.getModelName())){
					Log.d(TAG, "found eSTBLANManage device");
					DeviceList dll = sd.getDeviceList();
					for(int k=0;k<dll.size();k++){
						Device sdd = dll.getDevice(k);
						Log.d(TAG, "subdevice2 type="+sdd.getDeviceType()+", model = "+sdd.getModelName());
						if("eSTBWANConnectionLANManage".equals(sdd.getModelName())){
							sWanDevice = sdd;
							printServices(sdd);
							getDeviceData();
						}
					}
				}else if("HOME_LAN".equals(sd.getModelName())){
					HomeLanManagementDevice = sd ;
					getDeviceData();
				}else if("eRouterLANManage".equals(sd.getModelName())){
					eRouterLANManageDevice = sd;
					getDeviceData();
				}else if("eMTALANManage".equals(sd.getModelName())){
					eMTALANManageDevice = sd;
					getDeviceData();
				}
				
			}
			
		}
	}

	public void printServices(Device dev) {
		ServiceList sl = dev.getServiceList();
		for(int i=0;i<sl.size();i++){
			org.cybergarage.upnp.Service s = sl.getService(i);
			Log.d(TAG, "service type = "+s.getServiceType()+", control url = "+s.getControlURL());
		}
	}

	@Override
	public void deviceRemoved(Device dev) {
		if (sDeviceListener != null) {
			sDeviceListener.deviceRemoved(dev);
		}
	}

	@Override
	public void eventNotifyReceived(String uuid, long seq, String varName,
			String value) {
		if (AVTransport.LASTCHANGE.equals(varName)) {
			try {
				Node n = UPnP.getXMLParser().parse(value);
				if(n == null){
					Logger.e("Error parse: "+value);
					return;
				}
				Event e = new Event();
				e.set(n);
				Logger.d(e.toString());
			} catch (ParserException e1) {
				// TODO Auto-generated catch block
				e1.printStackTrace();
			}
		} else {
			Logger.d(varName + ": " + value);
		}

	}

	static final String PARAMS = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>"
			+ "<cms:ContentPathList xmlns:cms=\"urn:schemas-upnp-org:dm:cms\" "
			+ "xmlns:xsi=http://www.w3.org/2001/XMLSchema-instance "
			+ "xsi:schemaLocation=\"urn:schemas-upnp-org:dm:cms http://www.upnp.org/schemas/dm/cms.xsd\">"
			+ "<ContentPath>/BBF/SmartCardReaders/SmartCardReader/0/SmartCard/SerialNumber</ContentPath>"
			+ "</cms:ContentPathList>";
	
	static final String PARAMS_VONDOR = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>"
			+ "<cms:ContentPathList xmlns:cms=\"urn:schemas-upnp-org:dm:cms\" "
			+ "xmlns:xsi=http://www.w3.org/2001/XMLSchema-instance "
			+ "xsi:schemaLocation=\"urn:schemas-upnp-org:dm:cms http://www.upnp.org/schemas/dm/cms.xsd\">"
			+ "<ContentPath>/BBF/SmartCardReaders/SmartCardReader/0/SmartCard/Vendor</ContentPath>"
			+ "</cms:ContentPathList>";
	static final String PARAMS_CM = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>"
			+ "<cms:ContentPathList xmlns:cms=\"urn:schemas-upnp-org:dm:cms\" "
			+ "xmlns:xsi=http://www.w3.org/2001/XMLSchema-instance "
			+ "xsi:schemaLocation=\"urn:schemas-upnp-org:dm:cms http://www.upnp.org/schemas/dm/cms.xsd\">"
			+ "<ContentPath>/UPnP/CableModem/MACAddress</ContentPath>"
			+ "</cms:ContentPathList>";
	
	public void getCardNumber() {
		Log.d(TAG, "getCardNumber, CADevice = "+sCADevice);
		if (sCADevice != null) {
			org.cybergarage.upnp.Service s = sCADevice
					.getService("urn:schemas-upnp-org:service:ConfigurationManagement:2");
			if (s != null) {
				Log.d(TAG, "find ConfigurationManagement service");
				Action act = s.getAction("GetValues");
				Argument parameters = act.getArgument("Parameters");
				parameters.setValue(PARAMS);
				act.postControlAction();
				
//				Action act1 = s.getAction("GetValues");
//				Argument parameters1 = act.getArgument("Parameters");
//				parameters1.setValue(PARAMS_VONDOR);
//				act1.postControlAction();

				ArgumentList al = act.getArgumentList();
				for (int i = 0; i < al.size(); i++) {
					Argument arg = al.getArgument(i);
					Log.d(TAG,
							"argument name = " + arg.getName() + ", value="
									+ arg.getValue());
				}
				
//				ArgumentList al1 = act1.getArgumentList();
//				for (int i = 0; i < al1.size(); i++) {
//					Argument arg1 = al1.getArgument(i);
//					Log.d(TAG,
//							"argument name1 = " + arg1.getName() + ", value="
//									+ arg1.getValue());
//				}


				Argument arg = act
						.getArgument("ParameterValueList");
				
//				Argument arg1 = act1
//						.getArgument("ParameterValueList");
				
				if (arg != null) {
					try {
						Node node = UPnP.getXMLParser().parse(arg.getValue());
						Log.d(TAG, "arg.getValue() = " + arg.getValue());
						int count = node.getNNodes();
						for(int i=0; i< count; i++){
							Node para = node.getNode(i);
							Node path = para.getNode("ParameterPath");
							Node value = para.getNode("Value");
							if(path != null && value != null){
								Log.d(TAG, "path = "+path.getValue()+", value = "+value.getValue());
								caid = value.getValue();
								Log.d(TAG, "card number = " + caid);
							}
						}
					} catch (Exception e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
				}
				
//				if(arg1 != null){
//
//					try {
//						Node node = UPnP.getXMLParser().parse(arg1.getValue());
//						Log.d(TAG, "arg1.getValue() = " + arg1.getValue());
//						int count = node.getNNodes();
//						for(int i=0; i< count; i++){
//							Node para = node.getNode(i);
//							Node path = para.getNode("ParameterPath");
//							Node value = para.getNode("Value");
//							if(path != null && value != null){
//								Log.d(TAG, "path = "+path.getValue()+", value = "+value.getValue());
//								caVendor = value.getValue();
//								Log.d(TAG, "card caVendor = " + caVendor);
//							}
//						}
//					} catch (Exception e) {
//						// TODO Auto-generated catch block
//						e.printStackTrace();
//					}
//				
//				}
			}
		}
	}

	private String WifiApInfo ="";
	public String getWifiInfo(){
		Log.d("wangkang", "find device HomeLanManagementDevice====="+HomeLanManagementDevice);
		JSONObject wifiAp = null;
		if (HomeLanManagementDevice != null) {
			org.cybergarage.upnp.Service s = HomeLanManagementDevice
					.getServiceByControlURL("/ctl/LANDevice/HOME_LAN/2.4G_WLANC1/WLANConfiguration");
			if(s!=null){
				Action act = s.getAction("GetInfo");
				act.postControlAction();
				ArgumentList al = act.getArgumentList();
				for (int i = 0; i < al.size(); i++) {
					Argument arg = al.getArgument(i);
					Log.d("wangkang",
							"HomeLanManagementDevice argument name = " + arg.getName() + ", value="
									+ arg.getValue());
				}
				String enable ="",ssid="",SafeType ="",ip="";
				Argument argSSid = act
						.getArgument("NewSSID");
				if(argSSid!=null){
					Log.d("wangkang", "argSSid==="+argSSid);
					ssid=argSSid.getValue();
				}
				Argument argSafeTtype = act
						.getArgument("NewBeaconType");
				if(argSafeTtype!=null){
					Log.d("wangkang", "argSafeTtype==="+argSafeTtype);
					SafeType =argSafeTtype.getValue();
				}
				Argument argEnable = act
						.getArgument("NewEnable");
				if(argEnable!=null){
					Log.d("wangkang", "argEnable==="+argEnable);
					enable = argEnable.getValue();
				}
				
				wifiAp = new JSONObject();  
				try {
					wifiAp.put("enable", enable);
					wifiAp.put("ssid", ssid);
					wifiAp.put("safetype", SafeType);
					wifiAp.put("ip", stbip);
				} catch (JSONException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				
				WifiApInfo = wifiAp.toString();
				Log.d("wangkang", "return WifiApInfo==="+WifiApInfo);
				return WifiApInfo;
				
			}
		}
		if (wifiAp == null) {
			try {
				wifiAp = new JSONObject();  
				wifiAp.put("enable", "");
				wifiAp.put("ssid", "");
				wifiAp.put("safetype", "");
				wifiAp.put("ip", "");
			} catch (JSONException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		return wifiAp.toString();
	}
	
	String GateWayInfo = "";
	
	private String getGateway() {
		String cm_ip = "", cm_mac = "", erouter_ip = "", erouter_mac="", eMTA_ip = "", eMTA_mac = "";
		String gateway_Manufacturers = "", gateway_num = "", gateway_type = "", gateway_HD_version = "", gateway_sys_version = "";
		//获取cable_model信息
		Log.d("wangkang", "sCADevice=="+sCADevice);
		Log.d("wangkang", "eMTALANManageDevice=="+eMTALANManageDevice);
		Log.d("wangkang", "eRouterLANManageDevice=="+eRouterLANManageDevice);
		Log.d("wangkang", "eRouterWANConnectionLANManage=="+eRouterWANConnectionLANManage);
		Log.d("wangkang", "GatewayDevice=="+GatewayDevice);
		
		if (sWanDevice != null) {
			org.cybergarage.upnp.Service s = sWanDevice
					.getService("urn:dslforum-org:service:WANIPConnection:1");
			if (s != null) {
				Log.d(TAG, "find WANIPConnection:1 service");
				Action act = s.getAction("GetInfo");
				act.postControlAction();

				ArgumentList al = act.getArgumentList();
				for (int i = 0; i < al.size(); i++) {
					Argument arg = al.getArgument(i);
					Log.d(TAG,
							"argument name = " + arg.getName() + ", value="
									+ arg.getValue());
				}

				Argument arg = act
						.getArgument("NewExternalIPAddress");
				Argument arg1 = act
						.getArgument("NewMACAddress");
				 
				if (arg != null) {
					cm_ip = arg.getValue();
					Log.d("wangkang", "IP Address = " + stbip);
				}
				
				if(arg1!=null){
					cm_mac = arg1.getValue();
					Log.d("wangkang", "IP Address = " + stbip);
				}
			}
		}
		
		if (sCADevice != null) {
			org.cybergarage.upnp.Service s = sCADevice
					.getService("urn:schemas-upnp-org:service:ConfigurationManagement:2");
			if (s != null) {
				Log.d("wangkang", "find ConfigurationManagement service");
				
				Action act = s.getAction("GetValues");
				
				Argument parameters = act.getArgument("Parameters");
				parameters.setValue(PARAMS_CM);
				act.postControlAction();

				ArgumentList al = act.getArgumentList();
				for (int i = 0; i < al.size(); i++) {
					Argument arg = al.getArgument(i);
					Log.d("wangkang", "argument name = " + arg.getName()
							+ ", value=" + arg.getValue());
				}

				Argument arg = act.getArgument("ParameterValueList");

				if (arg != null) {
					try {
						Node node = UPnP.getXMLParser().parse(arg.getValue());
						Log.d("wangkang", "arg.getValue()===" + arg.getValue());
					} catch (Exception e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
				}
			}
		}
		//获取eMTA 信息
		if (eMTALANManageDevice != null) {
			DeviceList dl = eMTALANManageDevice.getDeviceList();

			for (int i = 0; i < dl.size(); i++) {
				Device sd = dl.getDevice(i);
				if ("eMTAWANConnectionLANManage".equals(sd.getModelName())) {
					eMTAWANConnectionLANManage = sd;
					break;
				}
			}
			if (eMTAWANConnectionLANManage != null) {
				org.cybergarage.upnp.Service s = eMTAWANConnectionLANManage
						.getService("urn:dslforum-org:service:WANIPConnection:1");
				Action act = s.getAction("GetInfo");
				act.postControlAction();
				ArgumentList al = act.getArgumentList();
				for (int i = 0; i < al.size(); i++) {
					Argument arg = al.getArgument(i);
					Log.d("wangkang",
							"eMTAWANConnectionLANManage argument name = "
									+ arg.getName() + ", value="
									+ arg.getValue());
				}
				Argument ip = act.getArgument("NewExternalIPAddress");
				if (ip != null) {
					eMTA_ip = ip.getValue();
				}
				Argument mac = act.getArgument("NewMACAddress");
				if (mac != null) {
					eMTA_mac = mac.getValue();
				}
			}
		}
		//获取eRouter信息
		if (eRouterLANManageDevice != null) {

			DeviceList dl = eRouterLANManageDevice.getDeviceList();

			for (int i = 0; i < dl.size(); i++) {
				Device sd = dl.getDevice(i);
				if ("eRouterWANConnectionLANManage".equals(sd.getModelName())) {
					eRouterWANConnectionLANManage = sd;
					break;
				}
			}
			if (eRouterWANConnectionLANManage != null) {
				org.cybergarage.upnp.Service s = eRouterWANConnectionLANManage
						.getService("urn:dslforum-org:service:WANIPConnection:1");
				Action act = s.getAction("GetInfo");
				act.postControlAction();
				ArgumentList al = act.getArgumentList();
				for (int i = 0; i < al.size(); i++) {
					Argument arg = al.getArgument(i);
					Log.d("wangkang",
							"eRouterWANConnectionLANManage argument name = "
									+ arg.getName() + ", value="
									+ arg.getValue());
				}
				Argument ip = act.getArgument("NewExternalIPAddress");
				if (ip != null) {
					erouter_ip = ip.getValue();
				}
				Argument mac = act.getArgument("NewMACAddress");
				if (mac != null) {
					erouter_mac = mac.getValue();
				}
			}
		}
		//获取网关的信息
		if(GatewayDevice!=null){
			org.cybergarage.upnp.Service s = GatewayDevice
					.getService("urn:dslforum-org:service:DeviceInfo:1");
			Action act = s.getAction("GetInfo");
			act.postControlAction();
			ArgumentList al = act.getArgumentList();
			for (int i = 0; i < al.size(); i++) {
				Argument arg = al.getArgument(i);
				Log.d("wangkang",
						"GatewayDevice argument name = "
								+ arg.getName() + ", value="
								+ arg.getValue());
			}
			Argument NewManufacturerName = act.getArgument("NewManufacturerName");
			if (NewManufacturerName != null) {
				gateway_Manufacturers = NewManufacturerName.getValue();
			}
			Argument NewSerialNumber = act.getArgument("NewSerialNumber");
			if (NewSerialNumber != null) {
				gateway_num = NewSerialNumber.getValue();
			}
			
			Argument NewProductClass = act.getArgument("NewProductClass");
			if (NewProductClass != null) {
				gateway_type = NewProductClass.getValue();
			}
			Argument NewHardwareVersion = act.getArgument("NewHardwareVersion");
			if (NewHardwareVersion != null) {
				gateway_HD_version = NewHardwareVersion.getValue();
			}
			Argument NewSoftwareVersion = act.getArgument("NewSoftwareVersion");
			if (NewSoftwareVersion != null) {
				gateway_sys_version = NewSoftwareVersion.getValue();
			}
		}
		//创建JSON对象
		JSONObject JSON = new JSONObject();
		try {
			JSON.put("cm_ip", cm_ip);
			JSON.put("cm_mac", cm_mac);
			JSON.put("eMTA_ip", eMTA_ip);
			JSON.put("eMTA_mac", eMTA_mac);
			JSON.put("erouter_ip", erouter_ip);
			JSON.put("erouter_mac", erouter_mac);
			JSON.put("gateway_Manufacturers", gateway_Manufacturers);
			JSON.put("gateway_num", gateway_num);
			JSON.put("gateway_type", gateway_type);
			JSON.put("gateway_HD_version", gateway_HD_version);
			JSON.put("gateway_sys_version", gateway_sys_version);
			Log.d("wangkang", "GateWayInfo===="+GateWayInfo);
			GateWayInfo = JSON.toString();
			Log.d("wangkang", "return GateWayInfo===="+GateWayInfo);
			return GateWayInfo;
			
		} catch (JSONException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return GateWayInfo;
	}
	
	private int resetFactory(int flag){
		if(GatewayDevice!=null){
			org.cybergarage.upnp.Service s = HomeLanManagementDevice
					.getService("urn:dslforum-org:service:DeviceConfig:1");
			if(s!=null){
				Action act = s.getAction("FactoryReset");
				ArgumentList al = act.getArgumentList();
				for (int i = 0; i < al.size(); i++) {
					Argument arg = al.getArgument(i);
					Log.d("wangkang",
							"eRouterWANConnectionLANManage argument name = "
									+ arg.getName() + ", value="
									+ arg.getValue());
				}
			}
		}
		
		return 0;
	}
	
	private String setWifiEnable(String enable){
		if (HomeLanManagementDevice != null) {
			org.cybergarage.upnp.Service s = HomeLanManagementDevice
					.getServiceByControlURL("/ctl/LANDevice/HOME_LAN/2.4G_WLANC1/WLANConfiguration");
			if (s != null) {
				Action SetEable = s.getAction("SetEnable");
				ArgumentList Eable = SetEable.getArgumentList();
				for (int i = 0; i < Eable.size(); i++) {
					Argument arg = Eable.getArgument(i);
					Log.d("wangkang", "SetEable argument name = " + arg.getName()
							+ ", value=" + arg.getValue());
				}
				Argument Switch = SetEable.getArgument("NewEnable");
				if(Switch!=null&&!TextUtils.isEmpty(enable)){
					Switch.setValue(enable);
					Log.d("wangkang", "setWifiEnable sucess");
				}
			}
		}
		return "false";
	}
	
	private String SetWifiApInfo(String data) {
		Log.d("wangkang", "SetWifiApInfo  data==="+data);
		if (HomeLanManagementDevice != null) {
			org.cybergarage.upnp.Service s = HomeLanManagementDevice
					.getServiceByControlURL("/ctl/LANDevice/HOME_LAN/2.4G_WLANC1/WLANConfiguration");
			if (s != null) {
				Action act1 = s.getAction("SetConfig");
				Action SetEable = s.getAction("SetEnable");
				act1.postControlAction();

				Action act2 = s.getAction("SetSecurityKeys");
				act2.postControlAction();
				ArgumentList al = act1.getArgumentList();
				for (int i = 0; i < al.size(); i++) {
					Argument arg = al.getArgument(i);
					Log.d("wangkang", "act1 argument name = " + arg.getName()
							+ ", value=" + arg.getValue());
				}

				ArgumentList al2 = act2.getArgumentList();
				for (int i = 0; i < al2.size(); i++) {
					Argument arg = al2.getArgument(i);
					Log.d("wangkang", "act2 argument name = " + arg.getName()
							+ ", value=" + arg.getValue());
				}
				try {
					if (!TextUtils.isEmpty(data)) {
						JSONObject wifiAp = new JSONObject(data);
						
						Argument Type = act1.getArgument("NewBeaconType");
						Argument ssid = act1.getArgument("NewSSID");
						Argument passwd = act2.getArgument("NewKeyPassphrase");
						Log.d("wangkang", "Type==="+Type);
						Log.d("wangkang", "ssid==="+ssid);
						Log.d("wangkang", "passwd==="+passwd);
						Log.d("wangkang", "01");
						if (passwd!=null&&Type != null && ssid != null) {
							Log.d("wangkang", "02");

							String safetypeValue = wifiAp.getString("safetype");
							String ssidValue = wifiAp.getString("ssid");
							String passwdValue = wifiAp.getString("passwd");
							if (!TextUtils.isEmpty(safetypeValue)
									&& !TextUtils.isEmpty(ssidValue)) {

								Type.setValue(safetypeValue);
								ssid.setValue(ssidValue);
								if (passwd != null) {
									if (!TextUtils.isEmpty(passwdValue)) {
										passwd.setValue(passwdValue);
									}
								}
								Log.d("wangkang", "03");
								return "true";
							}
						}
					}
				} catch (JSONException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}

			}
		}
		return "false";
	}
	
	public void getIP() {
		Log.d(TAG, "getIP, WanDevice = "+sWanDevice);
		if (sWanDevice != null) {
			org.cybergarage.upnp.Service s = sWanDevice
					.getService("urn:dslforum-org:service:WANIPConnection:1");
			if (s != null) {
				Log.d(TAG, "find WANIPConnection:1 service");
				Action act = s.getAction("GetInfo");
				act.postControlAction();

				ArgumentList al = act.getArgumentList();
				for (int i = 0; i < al.size(); i++) {
					Argument arg = al.getArgument(i);
					Log.d(TAG,
							"argument name = " + arg.getName() + ", value="
									+ arg.getValue());
				}

				Argument arg = act
						.getArgument("NewExternalIPAddress");
				if (arg != null) {
					stbip = arg.getValue();
					Log.d(TAG, "IP Address = " + stbip);
				}
			}
		}
	}
}
