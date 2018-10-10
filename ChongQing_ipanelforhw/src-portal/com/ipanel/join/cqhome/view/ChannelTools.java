package com.ipanel.join.cqhome.view;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.List;

import com.ipanel.chongqing_ipanelforhw.hwstruct.GetHwRequest;
import com.ipanel.chongqing_ipanelforhw.hwstruct.GetHwResponse;
import com.ipanel.chongqing_ipanelforhw.hwstruct.GetHwResponse.Channel;
import com.ipanel.join.cq.vod.HWDataManager;
import com.ipanel.join.cq.vod.utils.Logger;
import com.ipanel.join.cq.vod.utils.Tools;
import com.ipanel.join.protocol.a7.ServiceHelper;
import com.ipanel.join.protocol.a7.ServiceHelper.ResponseHandlerT;
import com.ipanel.join.protocol.a7.ServiceHelper.SerializerType;

import android.content.Context;
import android.text.TextUtils;
import android.util.Log;

public class ChannelTools {
	
	public static final String TAG = ChannelTools.class.getSimpleName();
	
	/**将转码频道名称拼接成图片获取地址*/
	public static String getChannelUrl(String channelName,Context cxt){
		if(TextUtils.isEmpty(channelName)||"".equals(channelName.trim())){
			return getDefaultChannelUrl();
		}
		channelName = transformChannelName(channelName);
		if(isExistAssetChannelPic(channelName, cxt)){
			return "asset:///channel/"+channelName+".png";
		}else{
			return getDefaultChannelUrl();
		}
	}
	/**转码频道名称*/
	private static String transformChannelName(String channelName){
		try {
			return URLEncoder.encode(channelName, "GBK");
		} catch (UnsupportedEncodingException e) {
			e.printStackTrace();
		}catch(Exception e){
			e.printStackTrace();
		}
		return "";
	}
	public static String getDefaultChannelUrl(){
		return "asset:///config-new-1080P/chaLogo_cqws.png";
	}
	/**判断本地是否存在搜索的台标*/
	private static boolean isExistAssetChannelPic(String name,Context cxt){
		try {
			String[] s=cxt.getAssets().list("channel");			
			for (int i = 0; i < s.length; i++) {
				String LocalTaiBiaoName=s[i].substring(0, s[i].lastIndexOf("."));
				if (name.equals(LocalTaiBiaoName)) {
					return true;
				}
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
		return false;
	}
}
