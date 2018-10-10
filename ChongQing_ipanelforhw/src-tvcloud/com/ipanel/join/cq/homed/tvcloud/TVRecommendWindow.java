package com.ipanel.join.cq.homed.tvcloud;

import java.util.List;

import android.app.Activity;
import android.graphics.drawable.ColorDrawable;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnFocusChangeListener;
import android.widget.ImageView;
import android.widget.PopupWindow;
import android.widget.RelativeLayout;
import android.widget.TextView;
import cn.ipanel.android.net.http.RequestParams;
import cn.ipanel.android.net.imgcache.BaseImageFetchTask;
import cn.ipanel.android.net.imgcache.ImageFetcher;
import cn.ipanel.android.net.imgcache.SharedImageFetcher;

import com.ipanel.chongqing_ipanelforhw.R;
import com.ipanel.join.cq.vod.utils.Tools;
import com.ipanel.join.protocol.a7.ServiceHelper;
import com.ipanel.join.protocol.a7.ServiceHelper.ResponseHandlerT;
import com.ipanel.join.protocol.a7.ServiceHelper.SerializerType;

public class TVRecommendWindow {
	private Activity mActivity;
	private ServiceHelper serviceHelper;
	private ImageFetcher mImageFetcher;
	private PopupWindow popwindow;
	private RelativeLayout mScrollView_layout;
//	public List<Program> list;
	
	public TVRecommendWindow(Activity mActivity) {
		super();
		this.mActivity = mActivity;
		serviceHelper = ServiceHelper.getHelper();
		serviceHelper.setSerializerType(SerializerType.JSON);
		mImageFetcher = SharedImageFetcher.getNewFetcher(mActivity, 3);		
		//初始化视图
		View v = mActivity.getLayoutInflater().inflate(
				R.layout.back_recommend, null);
		initViews(v);
		popwindow = new PopupWindow(v);
		popwindow.setFocusable(true);
		ColorDrawable dw = new ColorDrawable(0x00000000);
		popwindow.setBackgroundDrawable(dw);
		//获取数据
		getRecommendData();
	}

	private void initViews(View v) {
		mScrollView_layout = (RelativeLayout)v.findViewById(R.id.back_scrollView_layout);
	}
	
	private void getRecommendData(){
		//String url = HomedAPI.RECOOMEND_GET_TOP_RECOMMEND;
		String url = "http://slave.homed.me/recommend/get_recommend_by_label";
		serviceHelper.setRootUrl(url);
//		RequestParams req = HomedDataManager.getRequestParams();
//		HomedDataManager.fillSizes(req);
//		req.put("label", "102");
//		req.put("num", "10");
//		serviceHelper.callServiceAsync(mActivity, req, RespRecommendList.class, new ResponseHandlerT<RespRecommendList>() {
//			
//			@Override
//			public void onResponse(boolean success, RespRecommendList result) {
//				if(result != null && result.recommend_list!=null && result.recommend_list.size()>0){
//					list = result.getMergedList();
//					updateUI();
//				}else{
//					Tools.showToastMessage(mActivity, "未获取到推荐数据");
//					popwindow.dismiss();
//				}
//			}
//		});
	}

	public PopupWindow getPop() {
		return popwindow;
	}
	
	private void updateUI() {
		mScrollView_layout.removeAllViews();
//		for (int i = 0; i < list.size(); i++) {
//			View convertView = mActivity.getLayoutInflater().inflate(R.layout.vod_item_recommend_list, mScrollView_layout,false);
//			final Program p = list.get(i % list.size());
//			final ViewHold hv = new ViewHold();
//			hv.image = (ImageView) convertView.findViewById(R.id.recommend_poster);
//			hv.text = (TextView) convertView.findViewById(R.id.recommend_name);
//			if (p.poster_list != null) {
//				String adUrl = p.poster_list.getUrl("375x210");
//				BaseImageFetchTask task = mImageFetcher.getBaseTask(adUrl);
//				mImageFetcher.setLoadingImage(R.drawable.default_poster);
//				mImageFetcher.loadImage(task, hv.image);
//			}else{
//				hv.image.setImageResource(R.drawable.default_poster);
//			}
//			hv.text.setText(p.name); 
//			convertView.setOnClickListener(new OnClickListener() {
//				@Override
//				public void onClick(View arg0) {
//					int total = p.series_total;
//					if(total > 1 || p.type == 4){
//						com.ipanel.join.cq.vod.HomedDataManager.openTVDetail(mActivity, p.id+"", p.name, p.series_id+"",p);
//					}else{
//						com.ipanel.join.cq.vod.HomedDataManager.openMovieDetail(mActivity, p.id+"", p.name,p);
//					}
//				}
//			});
//
//			if(convertView!=null){
//				mScrollView_layout.addView(convertView);
//				RelativeLayout.LayoutParams para = (RelativeLayout.LayoutParams) convertView
//						.getLayoutParams();
//				para.leftMargin = mActivity.getResources()
//						.getDimensionPixelSize(R.dimen.back_left_lenth) * i;
//				convertView.requestLayout();
//			}
//			convertView.setOnFocusChangeListener(new OnFocusChangeListener() {			
//				@Override
//				public void onFocusChange(View v, boolean hasFocus) {
//					if(hasFocus){
//						hv.text.setSelected(true);
//					}else{
//						hv.text.setSelected(false);
//					}
//				}
//			});
//		}
		mScrollView_layout.requestFocus();
	}
	
	class ViewHold {
		ImageView image;
		TextView text;
	}
}
