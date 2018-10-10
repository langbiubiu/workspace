package com.ipanel.join.cq.vod.vodhome;

import java.util.List;

import org.apache.http.Header;
import org.apache.http.message.BasicHeader;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewTreeObserver.OnGlobalFocusChangeListener;
import android.widget.FrameLayout;
import android.widget.ImageView;
import cn.ipanel.android.net.http.RequestParams;
import cn.ipanel.android.net.imgcache.SharedImageFetcher;
import cn.ipanel.android.widget.ViewFrameIndicator;

import com.ipanel.chongqing_ipanelforhw.R;
import com.ipanel.join.cq.huawei.data.HuaWeiResponse;
import com.ipanel.join.cq.huawei.data.VodProgram;
import com.ipanel.join.cq.vod.HWDataManager;
import com.ipanel.join.cq.vod.jsondata.GlobalFilmData;
import com.ipanel.join.cq.vod.jsondata.SpecialResponse;
import com.ipanel.join.cq.vod.jsondata.SpecialResponse.Special;
import com.ipanel.join.cq.vod.player.SimplePlayerActivity;
import com.ipanel.join.cq.vod.utils.Logger;
import com.ipanel.join.cq.vod.utils.Tools;
import com.ipanel.join.protocol.a7.ServiceHelper;
import com.ipanel.join.protocol.a7.ServiceHelper.ResponseHandlerT;
import com.ipanel.join.protocol.a7.ServiceHelper.SerializerType;
import com.ipanel.join.protocol.huawei.cqvod.ProgramaResponse;
import com.ipanel.join.protocol.huawei.cqvod.RTSPResponse;

/**
 * ���ר��
 * @author wuhd
 *
 */
public class FreeRegionActivity extends BaseActivity implements OnGlobalFocusChangeListener,OnClickListener{
	public static final String TAG = "FreeRegionActivity";
	public static final String ID_MOVIE = "10000100000000090000000000105956";//��Ӱ
	public static final String ID_TELEPLAY = "10000100000000090000000000105957";//���Ӿ�
	public static final String ID_ZONGYI = "10000100000000090000000000105958";//����
	public static final String ID_RECOMMEND = "10000100000000090000000000105959";//��ҳ����Ƽ�
	public static final String ID_FREE_REGION = "10000100000000090000000000105951";//��ҳ���ר��
	
	private ViewFrameIndicator indicator;
	private ImageView free_zongyi,free_movie,free_teleplay;// ����,��Ӱ�����Ӿ�
	private FrameLayout vod_zhuanti;// ר��
	private List<Special> specialUrl; // ר����תurl�б�
	private ImageView[] imgv = new ImageView[5];
	private VodProgram tagVod; // ����ӰƬ
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.vod_free_region_layout);
		indicator = new ViewFrameIndicator(this);
		indicator.setFrameResouce(R.drawable.focus);
		initViews();
		getRecommendData(ID_RECOMMEND);
		getSpecailURL();
	}

	private void initViews() {
		imgv[0] = (ImageView)this.findViewById(R.id.free_3d);
		imgv[0].getViewTreeObserver().addOnGlobalFocusChangeListener(this);
		free_movie = (ImageView)this.findViewById(R.id.free_movie);
		free_teleplay = (ImageView)this.findViewById(R.id.free_teleplay);
		
		imgv[1] = (ImageView)this.findViewById(R.id.free_recommend01);
		imgv[2] = (ImageView)this.findViewById(R.id.free_recommend02);
		vod_zhuanti = (FrameLayout)this.findViewById(R.id.vod_zhuanti);
		
		free_zongyi = (ImageView)this.findViewById(R.id.free_zongyi);
		imgv[3] = (ImageView)this.findViewById(R.id.free_zhuanti);
		imgv[4] = (ImageView)this.findViewById(R.id.free_ad);
		
		imgv[0].setOnClickListener(this);
		free_movie.setOnClickListener(this);
		free_teleplay.setOnClickListener(this);
		
		imgv[1].setOnClickListener(this);
		imgv[2].setOnClickListener(this);
		vod_zhuanti.setOnClickListener(this);
		
		free_zongyi.setOnClickListener(this);
		imgv[4].setOnClickListener(this);
		
	}

	@Override
	public void onGlobalFocusChanged(View oldFocus, View newFocus) {
		moveFocusFrameTo(newFocus);
	}
	
	public void moveFocusFrameTo(View v) {
		indicator.moveFrameTo(v, true, false);
	}

	@Override
	public void onClick(View arg0) {
		switch (arg0.getId()) {
		case R.id.free_movie:
			openFilmListActivity("��ѵ�Ӱ",ID_MOVIE, "1");
			break;
		case R.id.free_teleplay:
			//���Ӿ�
			openFilmListActivity("��ѵ��Ӿ�", ID_TELEPLAY, "1");
			break;
		case R.id.free_zongyi:
			//����
			openFilmListActivity("�������", ID_ZONGYI, "1");
			break;
		case R.id.free_3d:
		case R.id.free_recommend01:
		case R.id.free_recommend02:
		case R.id.vod_zhuanti:
//		case R.id.free_ad:
			tagVod = (VodProgram) arg0.getTag();
			Logger.d(TAG, "onclick tagVod---"+tagVod);
			pageJump();
			break;
		case R.id.free_ad:
			gotoIPanel30(FreeRegionActivity.this, "http://192.168.17.5/ilive-epg/i.do");
			break;
		default:
			break;
		}
	}
	
	public void openFilmListActivity(String name,String id,String isSub){
		Intent intent = new Intent(this,FilmListActivity.class);
		intent.putExtra("name", name);
		intent.putExtra("params", id);
		intent.putExtra("sub", isSub);
		startActivity(intent);
	}
	
	//��Ӱ�����Ӿ硢�������ݼ���
	public static ProgramaResponse[] initReqData(){
		ProgramaResponse[] responses = new ProgramaResponse[3];
		ProgramaResponse response1 = new ProgramaResponse();
		response1.setIsSubType("0");
		response1.setTypeId(ID_MOVIE);
		response1.setTypeName("��Ӱ");
		
		ProgramaResponse response2 = new ProgramaResponse();
		response2.setIsSubType("0");
		response2.setTypeId(ID_TELEPLAY);
		response2.setTypeName("���Ӿ�");
		
		ProgramaResponse response3 = new ProgramaResponse();
		response3.setIsSubType("0");
		response3.setTypeId(ID_ZONGYI);
		response3.setTypeName("����");
		
		responses[0] = response1;
		responses[1] = response2;
		responses[2] = response3;
		return responses;
	}
	/**
	 * ��ȡ��ҳ�Ƽ�������
	 */
	public void getRecommendData(String typeId){
		Logger.d(TAG, "getRecommend data---"+typeId);
		RequestParams requestParams = new RequestParams();
		requestParams.put("typeID", typeId); // ��Ŀid
		requestParams.put("start", "0"); // ��ʼλ��
		requestParams.put("size", "4"); // ÿ�η�����������
		requestParams.put("imgType", "0"); // ͼƬ���ͣ�0: ����ͼ,1: ����,2: ����,3: ͼ��,7: ����ͼ
		requestParams.put("tags", "1"); // �������� ;0�򲻴�:����ѯ,1����ѯ��
		requestParams.put("defaultImgPath", ""); // Ĭ��ͼƬ
		requestParams.put("icon", "0"); // ��ѯͼ�� ;0�򲻴�:����ѯ,1����ѯ��
		requestParams.put("intro", "0"); // ��Ŀ���,1��ʾ��ʾ��Ŀ���,����ֵ�򲻴���ʾ����ʾ��Ŀ���
		requestParams.put("platform", "android"); // ƽ̨��ʶipanel,android
		ServiceHelper serviceHelper = ServiceHelper.getHelper();
		String rootUrl = GlobalFilmData.getInstance().getEPGBaseURL();
		serviceHelper.setRootUrl(rootUrl + "/datajspHD/queryVodData.jsp");
		serviceHelper.setHeaders(new Header[] { new BasicHeader("Cookie", GlobalFilmData.getInstance()
				.getCookieString()) });
		serviceHelper.callServiceAsync(this, requestParams, HuaWeiResponse.class,
				new ResponseHandlerT<HuaWeiResponse>() {
					@Override
					public void onResponse(boolean success, HuaWeiResponse result) {
						if(success && result != null){
							List<VodProgram> list = result.getVod();
							if (list != null && list.size() > 0) {
								updateUI(list);
							}
						}
					}
				});
	}
	
	/**
	 * ���½�������
	 * @param list
	 */
	private void updateUI(List<VodProgram> list){
		for (int i = 0; i < list.size(); i++) {
			VodProgram move = list.get(i);
			if (move != null) {
				Logger.d(TAG, "updateUI move = "+move.toString());
				String url = Tools.replace(move.getImg(), "../..", GlobalFilmData.getInstance().getEpgUrl());
				SharedImageFetcher.getSharedFetcher(FreeRegionActivity.this).loadImage(url, imgv[i]);
				if (i == 3) {
					vod_zhuanti.setTag(move);
				}else {
					imgv[i].setTag(move);	
				}
			}
		}
	}
	
	private void getSpecailURL(){
		Logger.d(TAG, "getSpecailURL");
		RequestParams requestParams = new RequestParams();
		ServiceHelper serviceHelper = ServiceHelper.getHelper();
		String rootUrl = GlobalFilmData.getInstance().getEPGBaseURL();
		serviceHelper.setRootUrl(rootUrl + "/hddb/hddb_topic_andr.txt");
		serviceHelper.callServiceAsync(this, requestParams, SpecialResponse.class,
				new ResponseHandlerT<SpecialResponse>() {
					@Override
					public void onResponse(boolean success, SpecialResponse result) {
						if(success && result != null){
							specialUrl = result.getData();
							Logger.d("loadSpecailURL specialUrl = "+specialUrl);
						}
					}
				});
	}
	
	/**
	 * ҳ����ת�����ǵ���ӰƬֱ�Ӳ��ţ�����ר������תר��ҳ��
	 */
	private void pageJump(){
		Logger.d(TAG+"--->tagVod = "+tagVod);
		if (tagVod == null) {
			return;
		}
		Logger.d(TAG+"--->specialUrl = "+specialUrl);
		if (specialUrl != null && specialUrl.size() > 0) {
			int size = specialUrl.size();
			for (int i = 0; i < size; i++) {
				String vodName = tagVod.getName();
				String specialName = specialUrl.get(i) == null ? "" : specialUrl.get(i).getName();
				Logger.d(TAG+"--->vodName = "+vodName+";specialName = "+specialName);
				if (specialName.equals(vodName)) { // ר������תר��ҳ��
					String url = specialUrl.get(i).getUrl();
					Logger.d(TAG+"--->urls = "+url);
					if (url == null || "".equals(url)) {
						return;
					}
					if (url.startsWith("http")) { // http��ͷΪ����·��
						Logger.d(TAG+"--->http url = "+url);
						gotoIPanel30(FreeRegionActivity.this, url);
					}else if (url.startsWith("/EPG/jsp/")) {// /EPG/jsp/��ͷ����ȡ/EPG/jsp֮����EpgUrl�������url
						url = GlobalFilmData.getInstance().getEpgUrl()+url.substring(8, url.length());
						Logger.d(TAG+"--->/EPG/jsp/ url = "+url);
						gotoIPanel30(FreeRegionActivity.this, url);
					}
					return;
				}
			}
		}
		Logger.d(TAG+"--->go to play");
		if ("ר��".equals(tagVod.getTags())) {
			Logger.d(TAG+"--->ר��  = "+tagVod.getTags());
			return;
		}
		if ("1".equals(tagVod.getPlayType())) { // ���Ӿ������
			HWDataManager.openDetail(FreeRegionActivity.this, tagVod.getVodID(), 
					tagVod.getName(), tagVod.getPlayType(), true, true);
		}else { // ��Ӱֱ�Ӳ���
			getVodPlayUrl(tagVod.getVodID());
		}
	}
	
	/**
	 * 3.0apk��ת
	 * @param context
	 * @param url
	 */
	private void gotoIPanel30(Context context, String url){
		try {
			Intent intent = new Intent();
			intent.putExtra("url", url);
			intent.setClassName("com.ipanel.dtv.chongqing", "com.ipanel.dtv.chongqing.IPanel30PortalActivity");
			context.startActivity(intent);
		} catch (Exception e) {
			// TODO Auto-generated catch block
			Logger.d("gotoIPanel30 e = "+e);
			e.printStackTrace();
		}
	}
	
	//��ȡ���ŵ�ַ
	private void getVodPlayUrl(String vodId){
		Logger.d(TAG, "getVodplayUrl"+",vodId="+vodId);
		ServiceHelper serviceHelper = ServiceHelper.getHelper();
		serviceHelper.setHeaders(new Header[] { new BasicHeader("Cookie",
				GlobalFilmData.getInstance().getCookieString()) });
		serviceHelper.setRootUrl(GlobalFilmData.getInstance().getEPGBaseURL()
				+ "/go_authorization.jsp");
		serviceHelper.setSerializerType(SerializerType.JSON);
		RequestParams requestParamsfilm = new RequestParams();
		// requestParamsfilm.put("typeId", typeId);
		requestParamsfilm.put("typeId", "-1");
		requestParamsfilm.put("playType", "1");
		requestParamsfilm.put("progId", vodId);
		requestParamsfilm.put("baseFlag", "0");  // Ϊ0ʱ����֤������Ʒ��
		requestParamsfilm.put("contentType", "0");
		requestParamsfilm.put("business", "1");
		serviceHelper.callServiceAsync(FreeRegionActivity.this,
				requestParamsfilm, RTSPResponse.class,
				new ResponseHandlerT<RTSPResponse>() {

					@Override
					public void onResponse(boolean success,
							RTSPResponse result) {
						if (success) {
							if ("1".equals(result.getPlayFlag())) {
								startToMoviePlay(result, tagVod.getName());
							}else{
								if (result.getMessage() != null 
										&& result.getMessage().contains("��ȡIPQAM��Դʧ��")) {
									Tools.showToastMessage(getBaseContext(), getResources().getString(R.string.vod_msg_4020));
								}else {
									Tools.showToastMessage(getBaseContext(), result.getMessage());
								}
							}
						}else{
							Tools.showToastMessage(getBaseContext(), getResources().getString(R.string.vod_msg_4026));
						}
					}
				});
	}
		
	//��Ӱ����
	private void startToMoviePlay(RTSPResponse rtspResponse, String name) {
		Intent intent = new Intent();
		intent.putExtra("name", name);
		intent.putExtra(
				"params",
				rtspResponse.getPlayUrl().substring(
						rtspResponse.getPlayUrl().indexOf("rtsp"),
						rtspResponse.getPlayUrl().length()));
		intent.putExtra("playType", "0");
		intent.putExtra("historyTime", 0);
		intent.setClass(FreeRegionActivity.this, SimplePlayerActivity.class);
		FreeRegionActivity.this.startActivity(intent);
	}
}

/***************����Ϊʹ���Ͻӿڻ�ȡ����***************/
//public class FreeRegionActivity extends BaseActivity implements OnGlobalFocusChangeListener,OnClickListener{
//	public static final String TAG = "FreeRegionActivity";
//	public static final String ID_MOVIE = "10000100000000090000000000105956";//��Ӱ
//	public static final String ID_TELEPLAY = "10000100000000090000000000105957";//���Ӿ�
//	public static final String ID_ZONGYI = "10000100000000090000000000105958";//����
//	public static final String ID_RECOMMEND = "10000100000000090000000000105959";//��ҳ����Ƽ�
//	public static final String ID_FREE_REGION = "10000100000000090000000000105951";//��ҳ���ר��
//	
//	private ViewFrameIndicator indicator;
//	private ImageView free_zongyi,free_movie,free_teleplay;// ����,��Ӱ�����Ӿ�
//	private FrameLayout vod_zhuanti;// ר��
//	private List<Special> specialUrl; // ר����תurl�б�
//	private ImageView[] imgv = new ImageView[5];
//	private MovieData tagVod; // ����ӰƬ
//	
//	@Override
//	protected void onCreate(Bundle savedInstanceState) {
//		super.onCreate(savedInstanceState);
//		setContentView(R.layout.vod_free_region_layout);
//		indicator = new ViewFrameIndicator(this);
//		indicator.setFrameResouce(R.drawable.focus);
//		initViews();
//		getRecommendData(ID_RECOMMEND);
//		getSpecailURL();
//	}
//
//	private void initViews() {
//		imgv[0] = (ImageView)this.findViewById(R.id.free_3d);
//		imgv[0].getViewTreeObserver().addOnGlobalFocusChangeListener(this);
//		free_movie = (ImageView)this.findViewById(R.id.free_movie);
//		free_teleplay = (ImageView)this.findViewById(R.id.free_teleplay);
//		
//		imgv[1] = (ImageView)this.findViewById(R.id.free_recommend01);
//		imgv[2] = (ImageView)this.findViewById(R.id.free_recommend02);
//		vod_zhuanti = (FrameLayout)this.findViewById(R.id.vod_zhuanti);
//		
//		free_zongyi = (ImageView)this.findViewById(R.id.free_zongyi);
//		imgv[3] = (ImageView)this.findViewById(R.id.free_zhuanti);
//		imgv[4] = (ImageView)this.findViewById(R.id.free_ad);
//		
//		imgv[0].setOnClickListener(this);
//		free_movie.setOnClickListener(this);
//		free_teleplay.setOnClickListener(this);
//		
//		imgv[1].setOnClickListener(this);
//		imgv[2].setOnClickListener(this);
//		vod_zhuanti.setOnClickListener(this);
//		
//		free_zongyi.setOnClickListener(this);
//		imgv[4].setOnClickListener(this);
//		
//	}
//
//	@Override
//	public void onGlobalFocusChanged(View oldFocus, View newFocus) {
//		moveFocusFrameTo(newFocus);
//	}
//	
//	public void moveFocusFrameTo(View v) {
//		indicator.moveFrameTo(v, true, false);
//	}
//
//	@Override
//	public void onClick(View arg0) {
//		switch (arg0.getId()) {
//		case R.id.free_movie:
//			openFilmListActivity("��ѵ�Ӱ",ID_MOVIE, "1");
//			break;
//		case R.id.free_teleplay:
//			//���Ӿ�
//			openFilmListActivity("��ѵ��Ӿ�", ID_TELEPLAY, "1");
//			break;
//		case R.id.free_zongyi:
//			//����
//			openFilmListActivity("�������", ID_ZONGYI, "1");
//			break;
//		case R.id.free_3d:
//		case R.id.free_recommend01:
//		case R.id.free_recommend02:
//		case R.id.vod_zhuanti:
//		case R.id.free_ad:
//			tagVod = (MovieData) arg0.getTag();
//			Logger.d(TAG, "onclick tagVod---"+tagVod);
//			pageJump();
//			break;
//		default:
//			break;
//		}
//	}
//	
//	public void openFilmListActivity(String name,String id,String isSub){
//		Intent intent = new Intent(this,FilmListActivity.class);
//		intent.putExtra("name", name);
//		intent.putExtra("params", id);
//		intent.putExtra("sub", isSub);
//		startActivity(intent);
//	}
//	
//	//��Ӱ�����Ӿ硢�������ݼ���
//	public static ProgramaResponse[] initReqData(){
//		ProgramaResponse[] responses = new ProgramaResponse[3];
//		ProgramaResponse response1 = new ProgramaResponse();
//		response1.setIsSubType("0");
//		response1.setTypeId(ID_MOVIE);
//		response1.setTypeName("��Ӱ");
//		
//		ProgramaResponse response2 = new ProgramaResponse();
//		response2.setIsSubType("0");
//		response2.setTypeId(ID_TELEPLAY);
//		response2.setTypeName("���Ӿ�");
//		
//		ProgramaResponse response3 = new ProgramaResponse();
//		response3.setIsSubType("0");
//		response3.setTypeId(ID_ZONGYI);
//		response3.setTypeName("����");
//		
//		responses[0] = response1;
//		responses[1] = response2;
//		responses[2] = response3;
//		return responses;
//	}
//	/**
//	 * ��ȡ��ҳ�Ƽ�������
//	 */
//	public void getRecommendData(String typeId){
//		Logger.d(TAG, "getRecommend data---"+typeId);
//		RequestParams requestParams = new RequestParams();
//		requestParams.put("centerTypeId", typeId);
//		requestParams.put("pageNo", "1");
//		requestParams.put("showNums", "5");
//		ServiceHelper serviceHelper = ServiceHelper.getHelper();
//		String rootUrl = GlobalFilmData.getInstance().getEPGBaseURL();
//		serviceHelper.setRootUrl(rootUrl + "/datajspHD/android_getVodList_data.jsp");
//		serviceHelper.setHeaders(new Header[] { new BasicHeader("Cookie", GlobalFilmData.getInstance()
//				.getCookieString()) });
//		serviceHelper.callServiceAsync(this, requestParams, MovieListResponse.class,
//				new ResponseHandlerT<MovieListResponse>() {
//					@Override
//					public void onResponse(boolean success, MovieListResponse result) {
//						if(success && result != null){
//							int count = Integer.parseInt(result.getTotalNums());
//							Logger.d("count--"+count);
//							 List<MovieData> move =  result.getMovieLists();
//							 if (move != null && move.size() > 0) {
//								 updateUI(move);
//							}
//						}
//					}
//				});
//	}
//	
//	/**
//	 * ���½�������
//	 * @param list
//	 */
//	private void updateUI(List<MovieData> list){
//		for (int i = 0; i < list.size(); i++) {
//			MovieData move = list.get(i);
//			if (move != null) {
//				Logger.d(TAG, "updateUI move = "+move.toString());
//				String url = Tools.replace(move.getPicPath(), "../..", GlobalFilmData.getInstance().getEpgUrl());
//				SharedImageFetcher.getSharedFetcher(FreeRegionActivity.this).loadImage(url, imgv[i]);
//				if (i == 3) {
//					vod_zhuanti.setTag(move);
//				}else {
//					imgv[i].setTag(move);	
//				}
//			}
//		}
//	}
//	
//	private void getSpecailURL(){
//		Logger.d(TAG, "getSpecailURL");
//		RequestParams requestParams = new RequestParams();
//		ServiceHelper serviceHelper = ServiceHelper.getHelper();
//		String rootUrl = GlobalFilmData.getInstance().getEPGBaseURL();
//		serviceHelper.setRootUrl(rootUrl + "/hddb/hddb_topic_andr.txt");
//		serviceHelper.callServiceAsync(this, requestParams, SpecialResponse.class,
//				new ResponseHandlerT<SpecialResponse>() {
//					@Override
//					public void onResponse(boolean success, SpecialResponse result) {
//						if(success && result != null){
//							specialUrl = result.getData();
//							Logger.d("loadSpecailURL specialUrl = "+specialUrl);
//						}
//					}
//				});
//	}
//	
//	/**
//	 * ҳ����ת�����ǵ���ӰƬֱ�Ӳ��ţ�����ר������תר��ҳ��
//	 */
//	private void pageJump(){
//		Logger.d(TAG+"--->tagVod = "+tagVod);
//		if (tagVod == null) {
//			return;
//		}
//		Logger.d(TAG+"--->specialUrl = "+specialUrl);
//		if (specialUrl != null && specialUrl.size() > 0) {
//			int size = specialUrl.size();
//			for (int i = 0; i < size; i++) {
//				String vodName = tagVod.getVodName();
//				String specialName = specialUrl.get(i) == null ? "" : specialUrl.get(i).getName();
//				Logger.d(TAG+"--->vodName = "+vodName+";specialName = "+specialName);
//				if (specialName.equals(vodName)) { // ר������תר��ҳ��
//					String url = specialUrl.get(i).getUrl();
//					Logger.d(TAG+"--->urls = "+url);
//					if (url == null || "".equals(url)) {
//						return;
//					}
//					if (url.startsWith("http")) { // http��ͷΪ����·��
//						Logger.d(TAG+"--->http url = "+url);
//						gotoIPanel30(FreeRegionActivity.this, url);
//					}else if (url.startsWith("/EPG/jsp/")) {// /EPG/jsp/��ͷ����ȡ/EPG/jsp֮����EpgUrl�������url
//						url = GlobalFilmData.getInstance().getEpgUrl()+url.substring(8, url.length());
//						Logger.d(TAG+"--->/EPG/jsp/ url = "+url);
//						gotoIPanel30(FreeRegionActivity.this, url);
//					}
//					return;
//				}
//			}
//		}
//		Logger.d(TAG+"--->go to play");
//		if ("ר��".equals(tagVod.getTagType())) {
//			Logger.d(TAG+"--->ר��  = "+tagVod.getTagType());
//			return;
//		}
//		if ("1".equals(tagVod.getPlayType())) { // ���Ӿ������
//			HWDataManager.openDetail(FreeRegionActivity.this, tagVod.getVodId(), 
//					tagVod.getVodName(), tagVod.getPlayType(), true);
//		}else { // ��Ӱֱ�Ӳ���
//			getVodPlayUrl(tagVod.getVodId());
//		}
//	}
//	
//	/**
//	 * 3.0apk��ת
//	 * @param context
//	 * @param url
//	 */
//	private void gotoIPanel30(Context context, String url){
//		try {
//			Intent intent = new Intent();
//			intent.putExtra("url", url);
//			intent.setClassName("com.ipanel.dtv.chongqing", "com.ipanel.dtv.chongqing.IPanel30PortalActivity");
//			context.startActivity(intent);
//		} catch (Exception e) {
//			// TODO Auto-generated catch block
//			Logger.d("gotoIPanel30 e = "+e);
//			e.printStackTrace();
//		}
//	}
//	
//	//��ȡ���ŵ�ַ
//	private void getVodPlayUrl(String vodId){
//		Logger.d(TAG, "getVodplayUrl"+",vodId="+vodId);
//		ServiceHelper serviceHelper = ServiceHelper.getHelper();
//		serviceHelper.setHeaders(new Header[] { new BasicHeader("Cookie",
//				GlobalFilmData.getInstance().getCookieString()) });
//		serviceHelper.setRootUrl(GlobalFilmData.getInstance().getEPGBaseURL()
//				+ "/go_authorization.jsp");
//		serviceHelper.setSerializerType(SerializerType.JSON);
//		RequestParams requestParamsfilm = new RequestParams();
//		// requestParamsfilm.put("typeId", typeId);
//		requestParamsfilm.put("typeId", "-1");
//		requestParamsfilm.put("playType", "1");
//		requestParamsfilm.put("progId", vodId);
//		requestParamsfilm.put("baseFlag", "0");  // Ϊ0ʱ����֤������Ʒ��
//		requestParamsfilm.put("contentType", "0");
//		requestParamsfilm.put("business", "1");
//		serviceHelper.callServiceAsync(FreeRegionActivity.this,
//				requestParamsfilm, RTSPResponse.class,
//				new ResponseHandlerT<RTSPResponse>() {
//
//					@Override
//					public void onResponse(boolean success,
//							RTSPResponse result) {
//						if (success) {
//							if ("1".equals(result.getPlayFlag())) {
//								startToMoviePlay(result, tagVod.getVodName());
//							}else{
//								Tools.showToastMessage(getBaseContext(), "��ȡ���ŵ�ַʧ�ܣ����Ժ�����.");
//							}
//						}else{
//							Tools.showToastMessage(getBaseContext(), "��ȡ���ŵ�ַʧ�ܣ����Ժ�����.");
//						}
//					}
//				});
//	}
//		
//	//��Ӱ����
//	private void startToMoviePlay(RTSPResponse rtspResponse, String name) {
//		Intent intent = new Intent();
//		intent.putExtra("name", name);
//		intent.putExtra(
//				"params",
//				rtspResponse.getPlayUrl().substring(
//						rtspResponse.getPlayUrl().indexOf("rtsp"),
//						rtspResponse.getPlayUrl().length()));
//		intent.putExtra("playType", "0");
//		intent.putExtra("historyTime", 0);
//		intent.setClass(FreeRegionActivity.this, SimplePlayerActivity.class);
//		FreeRegionActivity.this.startActivity(intent);
//	}
//}
