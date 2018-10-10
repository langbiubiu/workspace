package com.ipanel.join.cq.data;

import java.util.HashMap;
import java.util.Map;

import org.apache.http.Header;
import org.apache.http.message.BasicHeader;

import android.content.Context;
import android.util.Log;
import cn.ipanel.android.net.http.RequestParams;

import com.ipanel.join.chongqing.live.data.HuaweiGroup;
import com.ipanel.join.chongqing.live.data.HuaweiGroup.HuaweiChannel;
import com.ipanel.join.cq.vod.jsondata.GlobalFilmData;
import com.ipanel.join.protocol.a7.ServiceHelper;
import com.ipanel.join.protocol.a7.ServiceHelper.ResponseHandlerT;
import com.ipanel.join.protocol.a7.ServiceHelper.SerializerType;

public class ChannelDataManager {
	private static ChannelDataManager manager;
	private static Context context;
	private static Map<String, String> channelMap = new HashMap<String, String>();//seriveId，channelId
	private boolean flag = false;
	
	private ChannelDataManager(){
		requestAllChannel();
	}
	
	public static ChannelDataManager getInstance(Context ctx){
		if(manager == null){
			context = ctx;
			return new ChannelDataManager();
		}
		return manager;
	}
	
	public String getChannelId(String serviceId){
		Log.d("whd", "flag--"+flag);
		if(channelMap != null){
			Log.d("whd", "channelMap---"+channelMap.get(serviceId));
			return channelMap.get(serviceId);
		}
		return "";
	}
	//获取华为的频道列表
	private void requestAllChannel(){
		ServiceHelper mServiceHelper = ServiceHelper.getHelper();
		String url = GlobalFilmData.getInstance().getEpgUrl() + "/defaultHD/en/datajspHD/getAllChannelList.jsp";
		mServiceHelper.setRootUrl(url);
		mServiceHelper.setSerializerType(SerializerType.JSON);
		mServiceHelper.setHeaders(new Header[] { new BasicHeader("Cookie",
				 GlobalFilmData.getInstance().getCookieString()) });
		Log.d("whd", "request all channel");
		mServiceHelper.callServiceAsync(context, new RequestParams(), HuaweiGroup[].class, new ResponseHandlerT<HuaweiGroup[]>() {
			
			@Override
			public void onResponse(boolean success, HuaweiGroup[] result) {
				// TODO Auto-generated method stub
				if (success && result != null) {
					if(result.length > 0){
						HuaweiGroup group = result[0];
						Log.d("whd", "excute here----");
						if(group != null && group.chanelList!=null && group.chanelList.size() > 0){
							for (int i = 0; i < group.chanelList.size(); i++) {
								HuaweiChannel channel = group.chanelList.get(i);
								channelMap.put(channel.service+"", channel.channelID+"");
							}
							flag = true;//获取数据完成
						}
					}
				}
			}
		});
	}
	
}
