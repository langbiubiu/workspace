package com.ipanel.chongqing_ipanelforhw;


import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.SocketException;
import java.util.Enumeration;

import org.apache.http.conn.util.InetAddressUtils;

import android.app.Application;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.database.Cursor;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.Handler;
import android.util.Log;
import android.widget.Toast;
import cn.ipanel.android.LogHelper;
import cn.ipanel.android.otto.OttoUtils;

import com.ipanel.chongqing_ipanelforhw.hwstruct.GetHwRequest;
import com.ipanel.chongqing_ipanelforhw.hwstruct.GetHwResponse;
import com.ipanel.chongqing_ipanelforhw.otto.OttoNetworkSate;
import com.ipanel.join.chongqing.live.LiveApp;
import com.ipanel.join.chongqing.portal.PortalDataManager;
import com.ipanel.join.cq.vod.utils.GlobalContext;
import com.ipanel.join.cq.vod.utils.Tools;
import com.ipanel.join.protocol.a7.ServiceHelper;
import com.ipanel.join.protocol.a7.ServiceHelper.ResponseHandlerT;
import com.ipanel.join.protocol.a7.ServiceHelper.SerializerType;
import com.squareup.otto.Produce;
import com.squareup.otto.Subscribe;

public class CQApplication extends Application {
	
	public static final String TAG = CQApplication.class.getSimpleName();
	
	private String cardID 		= "";
	private String uid 			= "";
	private String weibo 		= "";
	private boolean eds 		= false;
	private String epgUrl 		= "";
	private String icState 		= "";
	private String authToken 	= "";
	private String servicegroup = "";
	private String aaa_state 	= "";
	private String cookieString = "";
	private String IP = "";
	private String MAC = "";
	
	private static CQApplication mInstance;
	public static GetHwRequest getLiveRequest,getRecsRequest;
	
	public static OttoNetworkSate netState = new OttoNetworkSate();
	public static GetHwResponse hwRecResponseLive;//直播推荐
	public static GetHwResponse hwRecResponsePage2;//大家都在看
	public Handler mHandler = new Handler();
	int page = 1;
	public static Object ottoProducer = new Object() {

		@Produce
		public OttoNetworkSate getNetState() {
			return netState;
		}
//		@Produce
//		public GetHwResponse getPortalLiveHwResponse(){
//			return hwRecResponseLive;
//			
//		}
		@Produce
		public GetHwResponse getPortalRec2HwResponse(){
			return hwRecResponsePage2;
		}
	};
	@Override
	public void onCreate() {
		super.onCreate();
		LogHelper.LOGTAG = TAG;
		mInstance = this;
		OttoUtils.getBus().register(ottoProducer);
		OttoUtils.getBus().register(this);
		
		initLiveRecsData();//获得直播推荐
		getRecsPage2Data();//获得大家都在看
		
		GlobalContext.init(getApplicationContext());
		LiveApp.getInstance().init(this);
		registerReceiver(new BroadcastReceiver() {

			@Override
			public void onReceive(Context ctx, Intent intent) {
				// TODO Auto-generated method stub
				LogHelper.i("已经接收到更新广播CookieString = " + intent.getExtras().getString("CookieString"));
				setCookieString(intent.getExtras().getString("CookieString"));
				setEpgUrl(intent.getExtras().getString("EPG"));
				setServicegroup("" + intent.getExtras().getLong("ServiceGroupId"));
				setCardID(intent.getExtras().getString("smartcard"));
				setIcState(intent.getExtras().getString("icState"));
				setEds(intent.getBooleanExtra("eds",false));
				setWeibo(intent.getStringExtra("weibo"));
				setAaa_state(intent.getStringExtra("aaa_state"));
				setUid(intent.getStringExtra("unitUserId"));
				setAuthToken(intent.getStringExtra("authToken"));
			}
			
		}, new IntentFilter("com.ipanel.join.cq.vodauth.EPG_URL"));
		
		loadNetworkInfo();
		monitorNetwork();
	}
	
	@Subscribe
	public void onNetworkState(OttoNetworkSate state){
		if(state != null){
			Log.d(TAG, "onNetworkState connected = "+state.connected);
			if(state.connected){
				mHandler.removeCallbacks(updateLiveRecs);
				initLiveRecsData();
				getRecsPage2Data();
			} else {
				
			}
		}
	}
	private void monitorNetwork() {
		updateNetworkState();
		IntentFilter filter = new IntentFilter();
		filter.addAction(ConnectivityManager.CONNECTIVITY_ACTION);

		this.registerReceiver(new BroadcastReceiver() {

			@Override
			public void onReceive(Context context, Intent intent) {
				try {
					Log.d(TAG, "onReceive action=" + intent.getAction());
					boolean connected = netState.connected;
					updateNetworkState();
					loadNetworkInfo();
					if (connected != netState.connected) {
						Log.d(TAG, "post network state change connected=" + netState.connected);
						OttoUtils.getBus().post(netState);
					}
				} catch (Exception e) {
					e.printStackTrace();
				}
			}

		}, filter);
	}
	
	void updateNetworkState() {
		ConnectivityManager cm = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
		NetworkInfo info = cm.getActiveNetworkInfo();
		if (info != null && info.isConnected()) {
			netState.connected = true;
			netState.type = info.getType();
		} else {
			netState.connected = false;
			netState.type = -1;
		}
	}
	
	public void loadNetworkInfo(){
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
	
	public String getNetYype(Context context) {
		String result = "";
		try {
			Uri uri = Uri.parse("content://ipaneltv.chongqing.settings/net_type");
			Cursor cursor = context.getContentResolver().query(uri, null, null, null, null);
			while (cursor!=null&&cursor.moveToNext()) {
				result = cursor.getString(0);
				break;
			}
			if(cursor!=null){
				cursor.close();
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		LogHelper.i("get net type : " + result);
		return result;
	}
	
	public static CQApplication getInstance(){
		return mInstance;
	}
	
	public String getCardID() {
		return cardID;
	}

	public void setCardID(String cardID) {
		this.cardID = cardID;
	}

	public String getUid() {
		return uid;
	}

	public void setUid(String uid) {
		this.uid = uid;
	}

	public String getWeibo() {
		return weibo;
	}

	public void setWeibo(String weibo) {
		this.weibo = weibo;
	}

	public boolean isEds() {
		return eds;
	}

	public void setEds(boolean eds) {
		this.eds = eds;
	}

	public String getEpgUrl() {
		return epgUrl;
	}

	public void setEpgUrl(String epgUrl) {
		this.epgUrl = epgUrl;
	}

	public String getIcState() {
		return icState;
	}

	public void setIcState(String icState) {
		this.icState = icState;
	}

	public String getAuthToken() {
		return authToken;
	}

	public void setAuthToken(String authToken) {
		this.authToken = authToken;
	}

	public String getServicegroup() {
		return servicegroup;
	}

	public void setServicegroup(String servicegroup) {
		this.servicegroup = servicegroup;
	}

	public String getAaa_state() {
		return aaa_state;
	}

	public void setAaa_state(String aaa_state) {
		this.aaa_state = aaa_state;
	}

	public String getCookieString() {
		return cookieString;
	}

	public void setCookieString(String cookieString) {
		this.cookieString = cookieString;
	}
	
	public String getIP() {
		return IP;
	}
	
	public void setIP(String ipaddr) {
		this.IP = ipaddr;
	}
	
	public String getMAC() {
		return MAC;
	}
	
	public void setMAC(String macaddr) {
		this.MAC = macaddr;
	}

	/**
	 * 请求直播推荐频道
	 */
	public void initLiveRecsData() {
		
		getLiveRequest = new GetHwRequest();
		
		getLiveRequest.setAction("GetLiveProgramsByRecommend");
		
		getLiveRequest.getDevice().setDnum("123");
		getLiveRequest.getUser().setUserid("123");
		getLiveRequest.getParam().setPage(1);
		getLiveRequest.getParam().setPagesize(2);
		
		getLiveProgramsRequest(getLiveRequest);
		
		mHandler.postDelayed(updateLiveRecs, 5 * 60 * 1000);
	}
	
	Runnable updateLiveRecs = new Runnable() {
		
		@Override
		public void run() {
			initLiveRecsData();
		}
	};
	
	/**获取直播推荐
	 * @param requestEntity
	 */
	private void getLiveProgramsRequest(Object requestEntity) {
		
		ServiceHelper helper = ServiceHelper.getHelper();
		helper.setSerializerType(SerializerType.JSON);
		helper.setRootUrl(PortalDataManager.url);
		Log.d(TAG, PortalDataManager.gson.toJson(requestEntity));
		helper.callServiceAsync(getApplicationContext(), requestEntity,
				GetHwResponse.class, new ResponseHandlerT<GetHwResponse>() {					

					@Override
					public void onResponse(boolean success, GetHwResponse result) {
						if (!success) {
							Log.i(TAG, "request detail data failed");
							return;
						}
						if (result == null) {
							Log.i(TAG, "failed to parse JSON data");
							Toast.makeText(getApplicationContext(), "failed to parse JSON data", Toast.LENGTH_LONG).show();
							return;
						}
						if(result.getError() != null && result.getError().getCode() == 0){
							hwRecResponseLive = result;
							OttoUtils.getBus().post(hwRecResponseLive);
						}else{
							Tools.showToastMessage(getApplicationContext(), result.getError().getInfo());
						}
						
					}
				});
	}
	
	/**
	 * 请求"大家在看"列表
	 * @param page
	 */
	public void getRecsPage2Data() {
		getRecsRequest = new GetHwRequest();
		
		getRecsRequest.setAction(PortalDataManager.ACTION_GET_WIKIS_BY_HOT);
		
		getRecsRequest.getDevice().setDnum("123");
		getRecsRequest.getUser().setUserid("123");
		getRecsRequest.getParam().setPage(page++);
		getRecsRequest.getParam().setPagesize(5);
		
		getRecPage2Request(getRecsRequest);
	}
	/**获取大家在看
	 * @param requestEntity
	 */
	private void getRecPage2Request(Object requestEntity) {
		
		ServiceHelper helper = ServiceHelper.getHelper();
		helper.setSerializerType(SerializerType.JSON);
		helper.setRootUrl(PortalDataManager.url);
		Log.d(TAG, PortalDataManager.gson.toJson(requestEntity));
	
		helper.callServiceAsync(getApplicationContext(), requestEntity,
				GetHwResponse.class, new ResponseHandlerT<GetHwResponse>() {					

					@Override
					public void onResponse(boolean success, GetHwResponse result) {
						if (!success) {
							Log.i(TAG, "request detail data failed");
							return;
						}
						if (result == null) {
							Log.i(TAG, "failed to parse JSON data");
							Toast.makeText(getApplicationContext(), "failed to parse JSON data", Toast.LENGTH_LONG).show();
							return;
						}
						if(result.getError() != null && result.getError().getCode() == 0){
							hwRecResponsePage2 = result;
							OttoUtils.getBus().post(hwRecResponsePage2);
						}else{
							Tools.showToastMessage(getApplicationContext(), result.getError().getInfo());
						}
					}
				});
	}
}
