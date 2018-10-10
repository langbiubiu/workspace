package com.ipanel.join.cqhome.view;


import org.json.JSONException;
import org.json.JSONObject;

import com.ipanel.chongqing_ipanelforhw.hwstruct.GetHwRequest;
import com.ipanel.chongqing_ipanelforhw.hwstruct.GetHwResponse;
import com.ipanel.chongqing_ipanelforhw.hwstruct.GetHwResponse.Wiki;
import com.ipanel.join.chongqing.portal.PortalDataManager;
import com.ipanel.join.cq.vod.rank.VodRankListActivity;
import com.ipanel.join.cq.vod.utils.BlurBitmap;
import com.ipanel.join.protocol.a7.ServiceHelper;
import com.ipanel.join.protocol.a7.ServiceHelper.ResponseHandlerT;
import com.ipanel.join.protocol.a7.ServiceHelper.SerializerType;

import cn.ipanel.android.LogHelper;
import cn.ipanel.android.net.imgcache.BaseImageFetchTask;
import cn.ipanel.android.net.imgcache.ImageFetchListener;
import cn.ipanel.android.net.imgcache.ImageFetchTask.TaskType;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.util.Log;
import android.widget.Toast;
import ipanel.join.configuration.Bind;
import ipanel.join.configuration.ConfigState;
import ipanel.join.configuration.View;
import ipanel.join.widget.AbsLayout;
import ipanel.join.widget.ImgView;
import ipanel.join.widget.TxtView;

public class RanksView extends AbsLayout{
    private ImgView img;
    private TxtView txt,txtbg;
    private Wiki wiki;
    int ranktype;
    /**
	 * 推荐类型-排行榜
	 * */
	public static final String RECOMMEND_RANK = "recommend_rank";
    /**
	 * 排行榜类型
	 * */
	public static final String RECOMMEND_RANK_TYPE = "ranktype";
	public static final int RANK_TYPE_NEW = 1; 		//最新榜
	public static final int RANK_TYPE_HOT = 2; 		//热播榜
	public static final int RANK_TYPE_STAR = 3; 	//明星榜
	public static final int RANK_TYPE_COLLECT = 4; 	//收藏榜
    
	public RanksView(Context context, View data) {
		super(context, data);
		Bind bind = data.getBindByName(RECOMMEND_RANK);
		if (bind != null) {
			try {
				JSONObject jobj = bind.getValue().getJsonValue();
				ranktype = jobj.optInt(RECOMMEND_RANK_TYPE);
				setOnClickListener(new OnClickListener() {

					@Override
					public void onClick(android.view.View v) {
						try {
							Intent intent = new Intent(getContext(),VodRankListActivity.class);
							intent.putExtra("type", ranktype);
							getContext().startActivity(intent);
						} catch (Exception e) {
							e.printStackTrace();
						}
					}
				});
			} catch (JSONException e) {
				e.printStackTrace();
			}
		}
	}
	
	private void getPosterByType(int type) {
		String action = "";
		switch(type){
		case RANK_TYPE_NEW:
			action = PortalDataManager.ACTION_GET_WIKIS_BY_NEW;
			break;
		case RANK_TYPE_HOT:
			action = PortalDataManager.ACTION_GET_WIKIS_BY_HIT;
			break;
		case RANK_TYPE_STAR:
			action = PortalDataManager.ACTION_GET_ACTORS_BY_Follow;
			break;
		case RANK_TYPE_COLLECT:
			action = PortalDataManager.ACTION_GET_WIKIS_BY_FAV;
			return;//收藏榜不用获取第一个海报
		default:
			return;
		}
		getRankPoster(action);
	}

	private void getRankPoster(String action) {
		GetHwRequest getRecsRequest = new GetHwRequest();
		
		getRecsRequest.setAction(action);
		
		getRecsRequest.getDevice().setDnum("123");
		getRecsRequest.getUser().setUserid("123");
		getRecsRequest.getParam().setPage(1);
		getRecsRequest.getParam().setPagesize(1);
		
		ServiceHelper helper = ServiceHelper.getHelper();
		helper.setSerializerType(SerializerType.JSON);
		helper.setRootUrl(PortalDataManager.url);
		Log.d("RanksView", PortalDataManager.gson.toJson(getRecsRequest));
	
		helper.callServiceAsync(getContext(), getRecsRequest,
				GetHwResponse.class, new ResponseHandlerT<GetHwResponse>() {					

					@Override
					public void onResponse(boolean success, GetHwResponse result) {
						if (!success) {
							Log.i("RanksView", "request detail data failed");
							return;
						}
						if (result == null) {
							Log.i("RanksView", "failed to parse JSON data");
							Toast.makeText(getContext(), "failed to parse JSON data", Toast.LENGTH_LONG).show();
							return;
						}
						if(result.getError().getCode() == 0 && result.getWikis() != null && result.getWikis().size() > 0){
							img.setDrawingCacheEnabled(true);
							wiki = result.getWikis().get(0);
							Log.d("RanksView", "cover:" + wiki.getCover());
							BaseImageFetchTask ivdtask = ConfigState.getInstance()
									.getImageFetcher(getContext()).getBaseTask(wiki.getCover());
							ivdtask.setTaskType(TaskType.IMAGE);
							ivdtask.setListener(new ImageFetchListener() {
								@Override
								public void OnComplete(int status) {
									if(img.getDrawingCache() != null)
										BlurBitmap.blur(img.getDrawingCache(), txtbg, 8);
								}
							});
							ConfigState.getInstance().getImageFetcher(getContext()).loadImage(ivdtask, img);
						}
					}
				});
	}
	
	@Override
	protected void onAttachedToWindow() {
		LogHelper.d("RanksView", "onAttachedToWindow");
		img = (ImgView) getChildAt(0);
		txtbg = (TxtView) getChildAt(1);
		txt = (TxtView) getChildAt(2);
		if(txt != null)
			txt.setBackgroundColor(Color.parseColor("#80ffffff"));
		getPosterByType(ranktype);
		super.onAttachedToWindow();
	}
	
	
}

