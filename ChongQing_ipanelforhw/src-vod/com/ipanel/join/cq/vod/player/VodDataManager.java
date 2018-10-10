package com.ipanel.join.cq.vod.player;

import org.apache.http.Header;
import org.apache.http.message.BasicHeader;

import android.content.Context;
import android.util.Log;
import cn.ipanel.android.net.http.RequestParams;

import com.google.gson.Gson;
import com.ipanel.chongqing_ipanelforhw.hwstruct.GetHwRequest;
import com.ipanel.chongqing_ipanelforhw.hwstruct.GetHwResponse;
import com.ipanel.join.cq.vod.HWDataManager;
import com.ipanel.join.cq.vod.jsondata.GlobalFilmData;
import com.ipanel.join.protocol.a7.LogcatUtils;
import com.ipanel.join.protocol.a7.ServiceHelper;
import com.ipanel.join.protocol.a7.ServiceHelper.ResponseHandlerT;
import com.ipanel.join.protocol.a7.ServiceHelper.SerializerType;
import com.ipanel.join.protocol.huawei.cqvod.MovieDetailResponse;
import com.ipanel.join.protocol.huawei.cqvod.RTSPResponse;

public class VodDataManager {
	private Context mContext;
	private static volatile VodDataManager vodDataManager = null;
	ResponseCallBack responseCallBack;
	HwResponseCallBack hwResponseCallBack;
	
	private VodDataManager() {
		
	}

	public static VodDataManager getInstance(Context context) {
		if (vodDataManager == null) {
			synchronized (VodDataManager.class) {
				if (vodDataManager == null) {
					vodDataManager = new VodDataManager(context);
				}
			}
		}
		return vodDataManager;
	}
	private VodDataManager(Context context){
		mContext= context.getApplicationContext();
	}
	/**
	 * 华为获取播放地址，弃用
	 * @param movieDetailResponse
	 * @param num
	 */
	public void requestTelevisionPlayUrl(MovieDetailResponse movieDetailResponse,final String num){
		LogcatUtils.splitAndLog("dzwillpower",String.format("movieDetailResponse: %s", movieDetailResponse.toString()));
		ServiceHelper serviceHelper = ServiceHelper.getHelper();
		serviceHelper.setHeaders(new Header[]{new BasicHeader("Cookie",GlobalFilmData.getInstance().getCookieString())});
		serviceHelper.setRootUrl(GlobalFilmData.getInstance().getEPGBaseURL()+"/go_authorization.jsp");
		serviceHelper.setSerializerType(SerializerType.JSON);
		RequestParams requestParams = new RequestParams();
//		requestParams.put("typeId", movieDetailResponse.getTypeId());
		requestParams.put("typeId", "-1");//电视剧所有的typeid请求都为-1
		requestParams.put("playType", "11");
		requestParams.put("progId", movieDetailResponse.getVodIdList().get(Integer.parseInt(num)-1));
		requestParams.put("parentVodId", movieDetailResponse.getVodId());
		requestParams.put("contentType", "0");
		requestParams.put("business", "1");
		serviceHelper.callServiceAsync(mContext,requestParams, RTSPResponse.class,
				new ResponseHandlerT<RTSPResponse>() {

			@Override
			public void onResponse(boolean success, RTSPResponse result) {
//				responseCallBack.onResponse(success, result,num);
			}
		});
	}
	/**
	 * 
	 * @param hwResponse
	 * @param num 第几集。从0开始
	 */
	public void requestTelevisionPlayUrl(final GetHwResponse hwResponse,final int num){
		Log.d("wuhd", "request tv play url-----");
		ServiceHelper serviceHelper = ServiceHelper.getHelper();
		serviceHelper.setHeaders(new Header[]{new BasicHeader("Cookie",GlobalFilmData.getInstance().getCookieString())});
		serviceHelper.setRootUrl(GlobalFilmData.getInstance().getEPGBaseURL()+"/go_authorization.jsp");
		serviceHelper.setSerializerType(SerializerType.JSON);
		RequestParams requestParams = new RequestParams();
		requestParams.put("typeId", "-1");//电视剧所有的typeid请求都为-1
		requestParams.put("playType", "11");
		requestParams.put("progId", hwResponse.getVod().get(num).getId());
		requestParams.put("parentVodId", hwResponse.getVod().get(num).getParent_id());
		requestParams.put("contentType", "0");
		requestParams.put("business", "1");
		Log.d("wuhd", "requestParams-->"+new Gson().toJson(requestParams));
		serviceHelper.callServiceAsync(mContext,requestParams, RTSPResponse.class,
				new ResponseHandlerT<RTSPResponse>() {

			@Override
			public void onResponse(boolean success, RTSPResponse result) {
				responseCallBack.onResponse(success,result,hwResponse.getVod().get(num).getTitle(),num);
			}
		});
	}
	
	/**
	 * 获取欢网数据
	 * @param request
	 */
	public void getHwData(final GetHwRequest request){
		ServiceHelper serviceHelper = ServiceHelper.getHelper();
		serviceHelper.setRootUrl(HWDataManager.ROOT_URL);
		serviceHelper.setSerializerType(SerializerType.JSON);
		
		serviceHelper.callServiceAsync(mContext, request, GetHwResponse.class, new ResponseHandlerT<GetHwResponse>() {
			
			@Override
			public void onResponse(boolean success, GetHwResponse result) {
				hwResponseCallBack.onResponse(success, result,request.getAction());
			}
		});
	}
	
	public interface ResponseCallBack{
		public void onResponse(boolean success, RTSPResponse result,String name,int num);
		
		public void onPlayDataReady(String url,String time,String flag,String name);
		
	}
	
	public interface HwResponseCallBack{
		public void onResponse(boolean success,GetHwResponse result,String action);
	}
	
	public void setResponseCallBack(ResponseCallBack responseCallBack) {
		this.responseCallBack = responseCallBack;
	}
	
	public void setHwResponseCallBack(HwResponseCallBack hwResponseCallBack) {
		this.hwResponseCallBack = hwResponseCallBack;
	}
}
