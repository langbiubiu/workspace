package com.ipanel.chongqing_ipanelforhw.hwstruct;


import com.google.gson.Gson;
import com.ipanel.chongqing_ipanelforhw.R;
import com.ipanel.join.protocol.a7.ServiceHelper;
import com.ipanel.join.protocol.a7.ServiceHelper.ResponseHandlerT;
import com.ipanel.join.protocol.a7.ServiceHelper.SerializerType;

import android.app.Activity;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.widget.TextView;
import android.widget.Toast;

public class HwstructTest extends Activity {

	private String url = "http://117.59.6.101/json";
	private final static String TAG="HwstructTest";
	
	GetHwRequest getChannelsRequest;//传参类
	GetHwResponse getChannelsResponse;//取值类
	Handler mHandler=new Handler();
	TextView responseText;
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
		responseText=(TextView) findViewById(R.id.response);
		
		//设置发送的Json数据模块
		getChannelsRequest=new GetHwRequest();
		getChannelsRequest.setAction("GetSearchKeyword");		
	
		getChannelsRequest.getDevice().setDnum("123");
		getChannelsRequest.getUser().setUserid("123");
		

//		getChannelsRequest.getParam().setPage(1);
//		getChannelsRequest.getParam().setPagesize(2);
//	
		getChannelsRequest.getParam().setWikiId("4d2ec8b32f2a241823000105");
		getHwRequest(getChannelsRequest);
	}
	Gson gson = new Gson();
	/**
	 * 欢网对接方法,对所有接口通用，传入的数据结构跟数据结构相同，只需要根据接口文档传入和获取对应的数值即可
	 **/
	private void getHwRequest(Object requestEntity) {
	
		ServiceHelper helper = ServiceHelper.getHelper();
		helper.setSerializerType(SerializerType.JSON);
		helper.setRootUrl(url);
		Log.d(TAG, gson.toJson(requestEntity));
		
		helper.callServiceAsync(getApplicationContext(), requestEntity,
				GetHwResponse.class, new ResponseHandlerT<GetHwResponse>() {					

					@Override
					public void onResponse(boolean success, GetHwResponse result) {
						// TODO Auto-generated method stub
						if (!success) {
							Log.i(TAG, "request detail data failed");

							
							return;
						}
						if (result == null) {
							Log.i(TAG, "failed to parse JSON data");
							Toast.makeText(HwstructTest.this, "failed to parse JSON data", 5555).show();
							return;
						}
						
						//这里获取到返回的Json模块结果
						getChannelsResponse = result;
						mHandler.post(new Runnable() {
							
							@Override
							public void run() {
								// TODO Auto-generated method stub
								responseText.setText(gson.toJson(getChannelsResponse));
							}
						});
					}
				});
	}

}
