package cn.ipanel.smackui;

import java.io.BufferedReader;
import java.io.File;
import java.io.InputStreamReader;
import java.io.StringReader;
import java.io.StringWriter;
import java.util.Iterator;
import java.util.UUID;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import org.jivesoftware.smack.AndroidConnectionConfiguration;
import org.jivesoftware.smack.Chat;
import org.jivesoftware.smack.ChatManagerListener;
import org.jivesoftware.smack.Connection;
import org.jivesoftware.smack.ConnectionConfiguration;
import org.jivesoftware.smack.ConnectionConfiguration.SecurityMode;
import org.jivesoftware.smack.ConnectionListener;
import org.jivesoftware.smack.MessageListener;
import org.jivesoftware.smack.PacketListener;
import org.jivesoftware.smack.RosterEntry;
import org.jivesoftware.smack.XMPPConnection;
import org.jivesoftware.smack.XMPPException;
import org.jivesoftware.smack.filter.AndFilter;
import org.jivesoftware.smack.filter.PacketFilter;
import org.jivesoftware.smack.filter.PacketIDFilter;
import org.jivesoftware.smack.filter.PacketTypeFilter;
import org.jivesoftware.smack.packet.IQ;
import org.jivesoftware.smack.packet.IQ.Type;
import org.jivesoftware.smack.packet.Message;
import org.jivesoftware.smack.packet.Packet;
import org.jivesoftware.smack.packet.Registration;
import org.jivesoftware.smackx.ConfigureProviderManager;
import org.jivesoftware.smackx.InitStaticCode;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.os.Build;
import android.text.TextUtils;
import android.util.Log;
import cn.ipanel.android.LogHelper;
import cn.ipanel.android.util.JSONUtil;

import de.measite.smack.AndroidDebugger;

public class XMPPManager {

	static final String TAG = XMPPManager.class.getSimpleName();

	public static enum ConnState {
		DISCONNECT, CONNECTING, CONNECTED, ERROR
	}

	public interface SimpleResponse {
		public void onResponse(boolean success);
	}

	public interface SimpleTask {
		public boolean run();
	}

	public static class SimpleAsyncRunner extends AsyncTask<SimpleTask, Void, Boolean> {

		private SimpleResponse response;

		public SimpleAsyncRunner(SimpleResponse simpleResponse) {
			this.response = simpleResponse;
		}

		@Override
		protected Boolean doInBackground(SimpleTask... params) {

			return params[0].run();
		}

		@Override
		protected void onPostExecute(Boolean result) {
			if (response != null)
				response.onResponse(result);
		}

	}

	private static ExecutorService mExecutorService = Executors.newSingleThreadExecutor();

	public static String server = "192.168.1.202";

	public static Connection connection;

	private static ConnState connState = ConnState.DISCONNECT;
	
	private static boolean isRegistered = false;

	private final static Object stateLock = new Object();

	public static Context appContext;
	
	private static MessageListener messageListener; 

	// private static ExecutorService executor =
	// Executors.newSingleThreadExecutor();
	
	public static void setMessageListener(MessageListener lis){
		messageListener=lis;
	}
	
	public static void setIsRegistered(boolean isReg) {
		isRegistered = isReg;
	}

	private static ConnectionListener connectionListener = new ConnectionListener() {

		@Override
		public void reconnectionSuccessful() {
			synchronized (stateLock) {
				connState = ConnState.CONNECTED;
				stateLock.notifyAll();
			}

		}

		@Override
		public void reconnectionFailed(Exception e) {
			// TODO Auto-generated method stub

		}

		@Override
		public void reconnectingIn(int seconds) {
			// TODO Auto-generated method stub

		}

		@Override
		public void connectionClosedOnError(Exception e) {
			connState = ConnState.DISCONNECT;
			connection = null;
			initTask.run();

		}

		@Override
		public void connectionClosed() {
			connState = ConnState.DISCONNECT;

		}
	};

	static SimpleTask initTask = new SimpleTask() {

		@Override
		public boolean run() {
			try {
				if (connection == null)
					connection = createNewConnection();
				Connection.DEBUG_ENABLED = true;

				new AndroidDebugger(connection, new StringWriter(), new StringReader(""));
				if (!connection.isConnected()) {
					LogHelper.i("connection connect start");
					connection.connect();
				}
				LogHelper.i("set connect listener");
				connection.addConnectionListener(connectionListener);
				connState = ConnState.CONNECTED;
//				String user = PersistStore.getUser(appContext);
//				String pwd = PersistStore.getUserPwd(appContext);
//				if (!TextUtils.isEmpty(user) && !TextUtils.isEmpty(pwd))
//					connection.login(user, pwd, getResId());
				synchronized (stateLock) {
					stateLock.notifyAll();
				}
				return true;
			} catch (XMPPException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
				connState = ConnState.ERROR;
			}catch(NullPointerException ne){
				ne.printStackTrace();
				connState = ConnState.ERROR;
			}
			return false;
		}

	};
	
	public static boolean isConnected(){
		return connState == ConnState.CONNECTED;
	}
	public static boolean isConnecting(){
		return connState == ConnState.CONNECTING;
	}
	public static String getResId(){
		String id = null;
		if (appContext != null) {
			SharedPreferences sp = appContext.getSharedPreferences("XMPP", 0);
			id = sp.getString("resourceId", null);
			if (id == null) {
				id = UUID.randomUUID().toString().substring(0, 8);
				sp.edit().putString("resourceId", id).commit();
			}
		}
		if (id == null) {
			id = UUID.randomUUID().toString().substring(0, 8);
		}
		return id;
	}
	
	public static String getMACAddress() { //根据恒云要求，ResId为dongle的mac地址，
//		WifiManager wifi = (WifiManager) appContext.getSystemService(Context.WIFI_SERVICE);
//		 
//		WifiInfo info = wifi.getConnectionInfo();
//		 
//		return info.getMacAddress().replace(":", "");
		
		String result = "";     
	     String Mac = "";
	     result = callCmd("busybox ifconfig","HWaddr");
	      
	     //如果返回的result == null，则说明网络不可取
	     if(result==null){
	         return "网络出错，请检查网络";
	     }
	      
	     //对该行数据进行解析
	     //例如：eth0      Link encap:Ethernet  HWaddr 00:16:E8:3E:DF:67
	     if(result.length()>0 && result.contains("HWaddr")==true){
	         Mac = result.substring(result.indexOf("HWaddr")+6, result.length()-1);
	         Log.i("test","Mac:"+Mac+" Mac.length: "+Mac.length());
	          
	         if(Mac.length()>1){
	             Mac = Mac.replaceAll(" ", "");
	             result = "";
	             String[] tmp = Mac.split(":");
	             for(int i = 0;i<tmp.length;++i){
	                 result +=tmp[i];
	             }
	         }
	         Log.i("test",result+" result.length: "+result.length());            
	     }
	     return result;
	}
	
	 private static String callCmd(String cmd,String filter) {   
	     String result = "";   
	     String line = "";   
	     try {
	         Process proc = Runtime.getRuntime().exec(cmd);
	         InputStreamReader is = new InputStreamReader(proc.getInputStream());   
	         BufferedReader br = new BufferedReader (is);   
	          
	         //执行命令cmd，只取结果中含有filter的这一行
	         while ((line = br.readLine ()) != null && line.contains(filter)== false) {   
	             //result += line;
	             Log.i("test","line: "+line);
	         }
	          
	         result = line;
	         Log.i("test","result: "+result);
	     }   
	     catch(Exception e) {   
	         e.printStackTrace();   
	     }   
	     return result;   
	 }
	
//	public static void testPull(){
//		try{
//		StbPayload payload = JSONUtil.fromJSON("{serviceMode:'VOD',mediaParams:{id:32,startTime:615,scale:1}}", StbPayload.class);
//		if ("VOD".equals(payload.serviceMode) && payload.mediaParams != null) {
//			Intent intent = new Intent(appContext, VideoPlayActivity.class);
//			intent.putExtra(VideoPlayActivity.EXTRA_CID, ""+payload.mediaParams.id);
//			intent.putExtra(VideoPlayActivity.EXTRA_POSITION,
//					payload.mediaParams.startTime * 1000);
//
//			appContext.startActivity(intent);
//		}
//		}catch(Exception e){
//			e.printStackTrace();
//		}
//	}

	public static void init(Context ctx, SimpleResponse listner) {
		appContext = ctx;
		connState = ConnState.CONNECTING;
		ConfigureProviderManager.configureProviderManager();
		InitStaticCode.initStaticCode(ctx);
		server = PersistStore.getHost(ctx);
		SimpleAsyncRunner runner = new SimpleAsyncRunner(listner);
		runner.executeOnExecutor(mExecutorService, initTask);
	}

	public static void addFriend(final String jid, SimpleResponse listener) {
		SimpleAsyncRunner runner = new SimpleAsyncRunner(listener);
		runner.executeOnExecutor(mExecutorService, new SimpleTask() {

			@Override
			public boolean run() {
				try {
					if (!connection.getRoster().contains(jid)) {
						connection.getRoster().createEntry(jid, jid, null);
					}
					for (RosterEntry entry : connection.getRoster().getEntries()) {
						Log.d(TAG, entry.toString() + " " + entry.getStatus());
					}
					return true;
				} catch (Exception e) {
					e.printStackTrace();
				}
				return false;
			}

		});
	}

	public static void sendMessage(final String message, SimpleResponse listener) {
		
		LogHelper.i("message :"+message);
		SimpleAsyncRunner runner = new SimpleAsyncRunner(listener);
		runner.executeOnExecutor(mExecutorService, new SimpleTask() {

			@Override
			public boolean run() {
				try {
					Iterator<RosterEntry> it = connection.getRoster().getEntries().iterator();
					if (it.hasNext()) {
						Chat chat = connection.getChatManager().createChat(it.next().getUser(),
								null);
						chat.addMessageListener(pullListener);
						chat.sendMessage(message);
					}
					return true;
				} catch (Exception e) {
					e.printStackTrace();
				}
				return false;
			}

		});
	}
	public static void sendMessage(final String message,final String jid, SimpleResponse listener) {
		
		LogHelper.i("message :"+message);
		SimpleAsyncRunner runner = new SimpleAsyncRunner(listener);
		runner.executeOnExecutor(mExecutorService, new SimpleTask() {

			@Override
			public boolean run() {
				try {
					Iterator<RosterEntry> it = connection.getRoster().getEntries().iterator();
					while(it.hasNext()){
						RosterEntry entry=it.next();
						LogHelper.i("user :"+entry.getUser());

						if(jid.equals(entry.getUser())){
							LogHelper.i("success send to :"+jid);
							Chat chat = connection.getChatManager().createChat(entry.getUser(),
									null);
							chat.addMessageListener(pullListener);
							chat.sendMessage(message);
							break;
						}
					}
					return true;
				} catch (Exception e) {
					e.printStackTrace();
				}
				return false;
			}

		});
	}
	
	public static void register(final String user, final String password, SimpleResponse listener) {
		
		SimpleAsyncRunner runner = new SimpleAsyncRunner(listener);
		runner.executeOnExecutor(mExecutorService, new SimpleTask() {

			@Override
			public boolean run() {
				// TODO Auto-generated method stub
				try {
					synchronized (stateLock) {
						while (connState != ConnState.CONNECTED) {
							try {
								stateLock.wait();
							} catch (InterruptedException e) {
								return false;
							}
						}
					}
					Registration registration = new Registration();

	                PacketFilter packetFilter = new AndFilter(new PacketIDFilter(
	                        registration.getPacketID()), new PacketTypeFilter(
	                        IQ.class));
	                
	                PacketListener packetListener = new PacketListener() {

						@Override
						public void processPacket(Packet packet) {
							// TODO Auto-generated method stub
							Log.d("RegisterTask.PacketListener",
	                                "processPacket().....");

	                        if (packet instanceof IQ) {
	                            IQ response = (IQ) packet;
	                            if (response.getType() == IQ.Type.ERROR) {
	                                    Log.e(TAG, "Unknown error while registering XMPP account! " 
	                                    			+ response.getError().getCondition());
	                            } else if (response.getType() == IQ.Type.RESULT) {
	                            	isRegistered = true;
	                                Log.i(TAG, "Account registered successfully");
	                            }
	                        }
						}
	                };
	                
	                connection.addPacketListener(packetListener, packetFilter);
	                
	                registration.setType(Type.SET);
	                registration.addAttribute("username", user);
	                registration.addAttribute("password", password);
	                connection.sendPacket(registration);
	                
				} catch (Exception e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				return false;
			}
			
		});
	}
	
	public static void login(final String user, final String password, SimpleResponse listener) {

		SimpleAsyncRunner runner = new SimpleAsyncRunner(listener);
		runner.executeOnExecutor(mExecutorService, new SimpleTask() {

			@Override
			public boolean run() {
				try {
					synchronized (stateLock) {
						while (connState != ConnState.CONNECTED && !isRegistered) {
							try {
								stateLock.wait();
							} catch (InterruptedException e) {
								// TODO Auto-generated catch block
								return false;
							}
						}
					}
					if (!connection.isAuthenticated())
//						connection.login(user, password, getResId());
						connection.login(user, password, getMACAddress());
					connection.getChatManager().addChatListener(new ChatManagerListener() {

						@Override
						public void chatCreated(Chat chat, boolean createdLocally) {
							if (!createdLocally)
								chat.addMessageListener(pullListener);
						}
					});
					return true;
				} catch (Exception e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				return false;
			}

		});
	}

	public static void disconnect() {
		new Thread() {
			public void run() {
				if (connection != null)
					connection.disconnect();
				connection = null;
			}
		}.start();
	}
	
	public static void syncDisconnect() {
		if (connection != null)
			connection.disconnect();
		connection = null;
	}

	/**
	 * Parses the current preferences and returns an new unconnected
	 * XMPPConnection
	 * 
	 * @return
	 * @throws XMPPException
	 */
	private static XMPPConnection createNewConnection() throws XMPPException {
		ConnectionConfiguration conf;

		conf = new AndroidConnectionConfiguration(server, 5222, "message.localserver");
		
		conf.setDebuggerEnabled(true);
		conf.setSecurityMode(SecurityMode.enabled);
		conf.setSASLAuthenticationEnabled(true);
		// conf.setSocketFactory(new XmppSocketFactory());

		if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.ICE_CREAM_SANDWICH) {
			conf.setTruststoreType("AndroidCAStore");
			conf.setTruststorePassword(null);
			conf.setTruststorePath(null);
		} else {
			conf.setTruststoreType("BKS");
			String path = System.getProperty("javax.net.ssl.trustStore");
			if (path == null) {
				path = System.getProperty("java.home") + File.separator + "etc" + File.separator
						+ "security" + File.separator + "cacerts.bks";
			}
			conf.setTruststorePath(path);
		}

		// conf.setCompressionEnabled(true);

		// disable the built-in ReconnectionManager since we handle this
		conf.setReconnectionAllowed(false);
		// disable the build in keep alive, we handle this in Android way
		conf.setAutoKeepAlive(false);
		// conf.setSendPresence(false);

		XMPPConnection connection = new XMPPConnection(conf);
		return connection;
	}

	static MessageListener pullListener = new MessageListener() {

		@Override
		public void processMessage(Chat chat, Message message) {
			Log.d(TAG, message.getFrom() + " " + message.getBody());
			
			if(messageListener!=null){
				messageListener.processMessage(chat, message);
			}
		}
	};
	
}
