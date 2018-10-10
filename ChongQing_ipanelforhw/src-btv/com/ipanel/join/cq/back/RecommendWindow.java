package com.ipanel.join.cq.back;

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
import cn.ipanel.android.net.imgcache.BaseImageFetchTask;
import cn.ipanel.android.net.imgcache.ImageFetcher;
import cn.ipanel.android.net.imgcache.SharedImageFetcher;

import com.ipanel.chongqing_ipanelforhw.R;
import com.ipanel.chongqing_ipanelforhw.hwstruct.GetHwRequest;
import com.ipanel.chongqing_ipanelforhw.hwstruct.GetHwResponse;
import com.ipanel.chongqing_ipanelforhw.hwstruct.GetHwResponse.Wiki;
import com.ipanel.join.cq.vod.HWDataManager;
import com.ipanel.join.cq.vod.utils.Logger;
import com.ipanel.join.cq.vod.utils.Tools;
import com.ipanel.join.protocol.a7.ServiceHelper;
import com.ipanel.join.protocol.a7.ServiceHelper.ResponseHandlerT;
import com.ipanel.join.protocol.a7.ServiceHelper.SerializerType;

public class RecommendWindow {
	private Activity mActivity;
	private ServiceHelper serviceHelper;
	private ImageFetcher mImageFetcher;
	private PopupWindow popwindow;
	private RelativeLayout mScrollView_layout;
	public List<Wiki> list;
	/**
	 * 推荐影片个数
	 */
	private static final int pagesize = 10;
	
	public RecommendWindow(Activity mActivity) {
		super();
		this.mActivity = mActivity;
		serviceHelper = ServiceHelper.getHelper();
		serviceHelper.setSerializerType(SerializerType.JSON);
		serviceHelper.setRootUrl(HWDataManager.ROOT_URL);
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
		Logger.d("wuhd", "getRecommendData--");
		GetHwRequest request = HWDataManager.getHwRequest();
		request.setAction(HWDataManager.ACTION_GET_WIKIS_BY_HOT);
		request.getParam().setPage(1);
		request.getParam().setPagesize(pagesize);
		
		serviceHelper.callServiceAsync(mActivity, request, GetHwResponse.class, new ResponseHandlerT<GetHwResponse>() {
			
			@Override
			public void onResponse(boolean success, GetHwResponse result) {
				if(!success){
					Tools.showToastMessage(mActivity, "获取数据失败");
					popwindow.dismiss();
					return;
				}
				if(result == null){
					Tools.showToastMessage(mActivity, "数据为空");
					popwindow.dismiss();
					return;
				}
				if(result.getError().getCode()==0){
					list = result.getWikis();
					if(list != null && list.size() > 0){
						updateUI();
					}
				}else{
					Tools.showToastMessage(mActivity,result.getError().getInfo());
					popwindow.dismiss();
				}
			}
		});
	}

	public PopupWindow getPop() {
		return popwindow;
	}
	
	private void updateUI() {
		mScrollView_layout.removeAllViews();
		for (int i = 0; i < list.size(); i++) {
			View convertView = mActivity.getLayoutInflater().inflate(R.layout.vod_item_recommend_list, mScrollView_layout,false);
			final Wiki item = list.get(i % list.size());
			final ViewHold hv = new ViewHold();
			hv.image = (ImageView) convertView.findViewById(R.id.recommend_poster);
			hv.text = (TextView) convertView.findViewById(R.id.recommend_name);
			
			if (item.getCover()!= null) {
				String adUrl = item.getCover();
				BaseImageFetchTask task = mImageFetcher.getBaseTask(adUrl);
				mImageFetcher.setLoadingImage(R.drawable.default_poster);
				mImageFetcher.loadImage(task, hv.image);
			}else{
				hv.image.setImageResource(R.drawable.default_poster);
			}
			hv.text.setText(item.getTitle()); 
			convertView.setOnClickListener(new OnClickListener() {

				@Override
				public void onClick(View arg0) {
					if(item.getModel().equals("teleplay")||item.getModel().equals("film")){
						HWDataManager.openDetail(mActivity, item.getWikiId(), item.getTitle(),item);
					}else{
						Tools.showToastMessage(mActivity, item.getModel());
					}
				}
			});

			if(convertView!=null){
				mScrollView_layout.addView(convertView);
				RelativeLayout.LayoutParams para = (RelativeLayout.LayoutParams) convertView
						.getLayoutParams();
				para.leftMargin = mActivity.getResources()
						.getDimensionPixelSize(R.dimen.back_left_lenth) * i;
				convertView.requestLayout();
			}
			convertView.setOnFocusChangeListener(new OnFocusChangeListener() {
				
				@Override
				public void onFocusChange(View v, boolean hasFocus) {
					if(hasFocus){
						hv.text.setSelected(true);
					}else{
						hv.text.setSelected(false);
					}
				}
			});
		}
		mScrollView_layout.requestFocus();
	}
	
	class ViewHold {
		ImageView image;
		TextView text;
	}
}
