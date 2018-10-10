package com.ipanel.join.cq.vod.detail;

import org.apache.http.Header;
import org.apache.http.message.BasicHeader;

import android.content.Context;
import cn.ipanel.android.net.http.RequestParams;

import com.ipanel.chongqing_ipanelforhw.hwstruct.GetHwResponse;
import com.ipanel.join.cq.vod.jsondata.GlobalFilmData;
import com.ipanel.join.cq.vod.utils.Logger;
import com.ipanel.join.protocol.a7.ServiceHelper;
import com.ipanel.join.protocol.a7.ServiceHelper.ResponseHandlerT;
import com.ipanel.join.protocol.a7.ServiceHelper.SerializerType;
import com.ipanel.join.protocol.huawei.cqvod.MovieDetailResponse;

/*
 * 获取华为vod的详情信息
 */
public class HwVodData {
	private final static String TAG = HwVodData.class.getSimpleName();
	private Context context;
	private ResponseCallBack responseCallBack;
	
	public interface ResponseCallBack{
		public void onDataResponse(boolean success,GetHwResponse result);
	}
	/*
	 * 设置回调
	 */
	public void setResponseCallBack(ResponseCallBack responseCallBack){
		this.responseCallBack = responseCallBack;
	}
	/*
	 * 构造函数
	 */
	public HwVodData(Context context){
		this.context = context;
	}
	/**
	 * 
	 * @param playType 播放类型：0电影，1电视剧
	 * @param vodId 影片ID
	 * @param typeId 栏目ID
	 */
	public void getDetailData(final GetHwResponse hwResponse,final int playType,String vodId,String typeId){
		Logger.d(TAG, "playType:"+playType+",vodId:"+vodId+",typeId:"+typeId);
		ServiceHelper serviceHelper = ServiceHelper.getHelper();
		serviceHelper.setSerializerType(SerializerType.JSON);
		if (playType==0) {
			serviceHelper.setRootUrl(GlobalFilmData.getInstance().getEPGBaseURL()
					+ "/datajspHD/android_getFilmDetail_data.jsp");
		} else {
			serviceHelper.setRootUrl(GlobalFilmData.getInstance().getEPGBaseURL()
					+ "/datajspHD/android_getTvDetail_data.jsp");
		}
		serviceHelper.setHeaders(new Header[] { new BasicHeader("Cookie",
				GlobalFilmData.getInstance().getCookieString()) });
		RequestParams requestParams = new RequestParams();
		requestParams.put("vodId", vodId);
		requestParams.put("typeId", typeId);
		
		serviceHelper.callServiceAsync(context, requestParams,
				MovieDetailResponse.class,
				new ResponseHandlerT<MovieDetailResponse>() {

					@Override
					public void onResponse(boolean success,
							MovieDetailResponse result) {
						responseCallBack.onDataResponse(success,getHuaweiVod(hwResponse,playType,result));
					}
				});
	}
	/**
	 * 
	 * @param hwResponse
	 * @return
	 */
	private GetHwResponse getHuaweiVod(GetHwResponse hwResponse,int playType,MovieDetailResponse result){
		if(playType == 1){
			//电视剧
			if(result!=null && result.getVodIdList()!=null && result.getVodIdList().size()>0){
				for (int i = 0; i < result.getVodIdList().size(); i++) {
					if(hwResponse.getVod().size() -1 >i)
						hwResponse.getVod().get(i).setId(result.getVodIdList().get(i));
				}
			}
		}
		return hwResponse;
	}
}
