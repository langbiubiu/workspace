package com.ipanel.join.chongqing.live.manager.impl.hw;

import java.util.ArrayList;
import java.util.List;

import android.content.Context;

import cn.ipanel.android.LogHelper;
import cn.ipanel.android.net.http.RequestParams;

import com.ipanel.join.chongqing.live.LiveActivity;
import com.ipanel.join.chongqing.live.data.HideChannel;
import com.ipanel.join.chongqing.live.navi.DatabaseObjects.LiveChannel;
import com.ipanel.join.protocol.a7.ServiceHelper;
import com.ipanel.join.protocol.a7.ServiceHelper.ResponseHandlerT;
import com.ipanel.join.protocol.a7.ServiceHelper.SerializerType;

/**
 * 隐藏频道管理类
 */
public class HideChannelManager {
	
	static LiveActivity activity;
	
	public static List<HideChannel> list = new ArrayList<HideChannel>();
	
	public static void init(Context context) {
		if (list != null && !list.isEmpty())
			return;
		activity = (LiveActivity)context;
		ServiceHelper.getHelper().setRootUrl("http://192.168.33.77:8181/ChannelManageSystem/getHide_ChannelListServlet");
		ServiceHelper.getHelper().setSerializerType(SerializerType.JSON);
		ServiceHelper.getHelper().callServiceAsync(context, new RequestParams(), HideChannel[].class, new ResponseHandlerT<HideChannel[]>() {
			
			@Override
			public void onResponse(boolean success, HideChannel[] result) {
				// TODO Auto-generated method stub
				if (result != null) {
					for (int i = 0; i < result.length; i++) {
						list.add(result[i]);
					}
				}
			}
		});
	}

	public static boolean isHideChannel(int channelNum) {
		for (HideChannel ch : list) {
			if (ch.getLogicChannel() == channelNum) {
				return true;
			}
		}
		return false;
	}
	
	public static LiveChannel getHideChannel(int channelNum) {
		for (HideChannel ch : list) {
			if (ch.getLogicChannel() == channelNum) {
//				LiveChannel channel = activity.getDataManager().getLiveChannelByNumber(channelNum);
				LogHelper.i("hide channel " + ch.getName() + ", number " + ch.getLogicChannel() +", serviceId " + ch.getServiceId());
				LiveChannel channel = activity.getDataManager().getLiveChannelByService(ch.getServiceId());
				if (channel == null) { //应用类隐藏频道
					channel = new LiveChannel();
					channel.setChannelNumber(ch.getLogicChannel());
					channel.setName(ch.getName());
				}
				return channel;
			}
		}
		return null;
	}
	
	public static String getUrl(int channelNum) {
		String url = "";
		for (HideChannel ch : list) {
			if (ch.getLogicChannel() == channelNum) {
				url = ch.getUrl();
			}
		}
		return url;
	}
	
}
