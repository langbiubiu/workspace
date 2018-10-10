package com.ipanel.join.cqhome.view;

import java.util.List;

import org.json.JSONException;
import org.json.JSONObject;

import com.ipanel.chongqing_ipanelforhw.CQApplication;
import com.ipanel.chongqing_ipanelforhw.hwstruct.GetHwResponse;
import com.ipanel.chongqing_ipanelforhw.hwstruct.GetHwResponse.Wiki;
import com.ipanel.join.chongqing.portal.PortalDataManager;
import com.ipanel.join.cq.vod.utils.BlurBitmap;
import com.squareup.otto.Subscribe;

import cn.ipanel.android.LogHelper;
import cn.ipanel.android.net.imgcache.BaseImageFetchTask;
import cn.ipanel.android.net.imgcache.ImageFetchListener;
import cn.ipanel.android.net.imgcache.ImageFetchTask.TaskType;
import cn.ipanel.android.otto.OttoUtils;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.drawable.BitmapDrawable;
import android.text.TextUtils.TruncateAt;
import android.view.ViewTreeObserver;
import ipanel.join.configuration.Bind;
import ipanel.join.configuration.ConfigState;
import ipanel.join.configuration.View;
import ipanel.join.widget.AbsLayout;
import ipanel.join.widget.ImgView;
import ipanel.join.widget.TxtView;

public class ShowTextView extends AbsLayout{
    private TxtView tv,tvbg;
    private ImgView img;
    private MyRunable mRunable;
    private Wiki wiki;
    int position = -1;
    /**
	 * 推荐类型
	 * */
	public static final String RECOMMEND = "recommend";
	/**
	 * 推荐第二页-大家都在看
	 * */
	public static final String RECOMMEND_PAGE2 = "page2";
    /**
	 * 推荐位置
	 * */
	public static final String RECOMMEND_POSITION = "position";
    
	public ShowTextView(Context context, View data) {
		super(context, data);
		OttoUtils.getBus().register(this);
		Bind bind = data.getBindByName(RECOMMEND);
		if (bind != null) {
			try {
				JSONObject jobj = bind.getValue().getJsonValue();
				position = jobj.optInt(RECOMMEND_POSITION);
				setOnClickListener(new OnClickListener() {

					@Override
					public void onClick(android.view.View v) {
						try {
							if(wiki != null){
								PortalDataManager.openMovieDetail(getContext(), wiki);
							}
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
	
	@Override
	protected void onAttachedToWindow() {
		LogHelper.d("ShowTextView", "onAttachedToWindow");
		mRunable=new MyRunable();
		img = (ImgView) this.getChildAt(0);
		tvbg = (TxtView) this.getChildAt(1);
		tv=(TxtView) this.getChildAt(2);
		tv.setSingleLine(true);
		tv.setTextColor(Color.BLACK);
		tv.setBackgroundColor(Color.parseColor("#80ffffff"));
		tv.setHorizontallyScrolling(true);
		tv.setMarqueeRepeatLimit(6);
		tv.setEllipsize(TruncateAt.MARQUEE);
		tv.setVisibility(android.view.View.INVISIBLE);
		this.setOnFocusChangeListener(new OnFocusChangeListener() {
			
			@Override
			public void onFocusChange(android.view.View v, boolean hasFocus) {
				if (hasFocus) {
					tv.postDelayed(mRunable, 60);
				}
				if (!hasFocus) {
					tv.removeCallbacks(mRunable);
					tv.setVisibility(android.view.View.INVISIBLE);
					tvbg.setVisibility(android.view.View.INVISIBLE);
					tv.setSelected(false);
				}
			}
		});
		if(position == -1){//如果是配置的资源
			img.post(new Runnable() {
				
				@Override
				public void run() {
					BitmapDrawable bd = (BitmapDrawable) img.getDrawable();
					Bitmap bmp = bd.getBitmap();
				    BlurBitmap.blur(bmp, tvbg , 8); 
				}
			});
		}
		if(CQApplication.getInstance().hwRecResponsePage2 != null && position != -1){
			refreshData(CQApplication.getInstance().hwRecResponsePage2.getWikis().get(position));
		}
		super.onAttachedToWindow();
	}
	
	@Override
	protected void onDetachedFromWindow() {
		super.onDetachedFromWindow();
	}

	private void refreshData(Wiki wiki) {
		this.wiki = wiki;
		tv.setText(wiki.getTitle());
		if(position != -1 && img != null){
			img.setDrawingCacheEnabled(true);
			BaseImageFetchTask ivdtask = ConfigState.getInstance()
					.getImageFetcher(getContext()).getBaseTask(wiki.getCover());
			ivdtask.setTaskType(TaskType.IMAGE);
			ivdtask.setListener(new ImageFetchListener() {
				@Override
				public void OnComplete(int status) {
					if(img.getDrawingCache() != null)
//						BlurBitmap.blur(img.getDrawingCache(), tv);
						BlurBitmap.blur(img.getDrawingCache(), tvbg,8);
				}
			});
			ConfigState.getInstance().getImageFetcher(getContext())
					.loadImage(ivdtask, img);
		}
	}

	@Subscribe
	public void onPortalLiveHwResponse(GetHwResponse resp){
		if(resp != null && resp.getWikis() != null && resp.getWikis().size() > 0){
			if(position != -1){
				refreshData(resp.getWikis().get(position));
			}
		}
	}

	class MyRunable implements Runnable{

		@Override
		public void run() {
			tv.setVisibility(android.view.View.VISIBLE);
			tvbg.setVisibility(android.view.View.VISIBLE);
			tv.setSelected(true);
		}
	}
}

