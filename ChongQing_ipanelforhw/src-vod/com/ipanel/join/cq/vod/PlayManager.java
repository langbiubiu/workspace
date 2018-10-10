package com.ipanel.join.cq.vod;

import java.util.ArrayList;
import java.util.List;

import org.apache.http.Header;
import org.apache.http.message.BasicHeader;

import android.content.Context;
import android.content.Intent;
import android.util.Log;
import cn.ipanel.android.net.http.RequestParams;

import com.google.gson.Gson;
import com.ipanel.chongqing_ipanelforhw.R;
import com.ipanel.chongqing_ipanelforhw.hwstruct.GetHwResponse;
import com.ipanel.chongqing_ipanelforhw.hwstruct.GetHwResponse.Vod;
import com.ipanel.join.cq.vod.detail.DetailActivity;
import com.ipanel.join.cq.vod.jsondata.GlobalFilmData;
import com.ipanel.join.cq.vod.order.OrderDialog;
import com.ipanel.join.cq.vod.player.SimplePlayerActivity;
import com.ipanel.join.cq.vod.utils.Logger;
import com.ipanel.join.cq.vod.utils.Tools;
import com.ipanel.join.protocol.a7.ServiceHelper;
import com.ipanel.join.protocol.a7.ServiceHelper.ResponseHandlerT;
import com.ipanel.join.protocol.a7.ServiceHelper.SerializerType;
import com.ipanel.join.protocol.huawei.cqvod.RTSPResponse;

//只针对电影
public class PlayManager {
	private ResponseListener listener;
	private String vodId;
	private String vodName;
	private String posterUrl;
	private String typeId;
	private Context context;
	private boolean isFree;

	public interface ResponseListener {
		public void onResponse(Boolean success, RTSPResponse result);
	}
	
	public PlayManager(Context context, String vodId, String vodName, boolean isFree, String typeId) {
		this.vodId = vodId;
		this.vodName = vodName;
		this.typeId = typeId;
		this.context = context;
		this.isFree = isFree;
	}
	
	public PlayManager(Context context, String vodId,String vodName,String posterUrl, String typeId,
			ResponseListener listener) {
		this.listener = listener;
		this.vodId = vodId;
		this.vodName = vodName;
		this.posterUrl = posterUrl;
		this.typeId = typeId;
		this.context = context;
	}
	/**
	 * 获取播放地址
	 */
	public void requestPlayUrl() {
		Log.d("wuhd", "request url");
		ServiceHelper serviceHelper = ServiceHelper.getHelper();
		serviceHelper.setHeaders(new Header[] { new BasicHeader("Cookie",GlobalFilmData.getInstance().getCookieString()) });
		serviceHelper.setRootUrl(GlobalFilmData.getInstance().getEPGBaseURL() + "/go_authorization.jsp");
		serviceHelper.setSerializerType(SerializerType.JSON);
		RequestParams requestParamsfilm = new RequestParams();
		// requestParamsfilm.put("typeId", typeId);
		requestParamsfilm.put("typeId", "-1");
		requestParamsfilm.put("playType", "1");//电影传1
		requestParamsfilm.put("progId", vodId);
		requestParamsfilm.put("contentType", "0");
		requestParamsfilm.put("business", "1");
		Logger.d("requestPlayUrl isFree : " + isFree);
		if (isFree) {
			requestParamsfilm.put("baseFlag", "0");  // 为0时不验证基本产品包
		}
		serviceHelper.callServiceAsync(context, requestParamsfilm,
				RTSPResponse.class, new ResponseHandlerT<RTSPResponse>() {

					@Override
					public void onResponse(boolean success, RTSPResponse result) {
						if(!success){
							Tools.showToastMessage(context, context.getResources().getString(R.string.vod_msg_4026));
						}
						if(result.getPlayFlag().equals("0")){
							if ("1".equals(result.getAnCiFlag())) {
								new OrderDialog(context, context.getResources().
										getString(R.string.order_tip), true).show();
							}else {
								new OrderDialog(context, result.getMessage()).show();
							}
						}else{
							Intent intent = new Intent();
							intent.putExtra("name", vodName);
							intent.putExtra("params",result.getPlayUrl().substring(
											result.getPlayUrl().indexOf("rtsp"),
											result.getPlayUrl().length()));
							intent.putExtra("playType", "0");
							intent.putExtra("historyTime", 0);
							/**
							 * 构造一个hwResponse
							 */
//							GetHwResponse hwResponse = new GetHwResponse();
//							hwResponse.getWiki().setWikiCover(posterUrl);
//							hwResponse.getWiki().setWikiTitle(vodName);
//							List<GetHwResponse.Vod> vodList = new ArrayList<GetHwResponse.Vod>();
//							GetHwResponse.Vod vod = new GetHwResponse.Vod();
//							vod.setTitle(vodName);
//							vod.setId(vodId);
//							vod.setParent_id(vodId);
//							vod.setMark("1");
//							vodList.add(vod);
//							hwResponse.setVod(vodList);
//							/**
//							 * 设置到hwResponse中
//							 */
//							intent.putExtra("hwResponse", new Gson().toJson(hwResponse));
							intent.setClass(context, SimplePlayerActivity.class);
							context.startActivity(intent);
						}
					}
				});
	}
}
