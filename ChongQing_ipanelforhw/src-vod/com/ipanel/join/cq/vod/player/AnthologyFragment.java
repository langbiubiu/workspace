package com.ipanel.join.cq.vod.player;

import java.text.SimpleDateFormat;

import android.provider.MediaStore.Video;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.VerticalViewPager2;
import android.text.TextUtils;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import cn.ipanel.android.Logger;
import cn.ipanel.android.net.imgcache.ImageFetcher;
import cn.ipanel.android.net.imgcache.SharedImageFetcher;
import cn.ipanel.android.widget.WeightGridLayout;
import cn.ipanel.android.widget.WeightGridLayout.WeightGridAdapter;

import com.ipanel.chongqing_ipanelforhw.R;
import com.ipanel.chongqing_ipanelforhw.hwstruct.GetHwResponse;
import com.ipanel.chongqing_ipanelforhw.hwstruct.GetHwResponse.Vod;
import com.ipanel.chongqing_ipanelforhw.hwstruct.GetHwResponse.Wiki;
import com.ipanel.chongqing_ipanelforhw.hwstruct.GetHwResponse.Wiki.Info.Star;
import com.ipanel.join.cq.vod.HWDataManager;
import com.ipanel.join.cq.vod.utils.Tools;
import com.ipanel.join.protocol.a7.ServiceHelper;
import com.ipanel.join.protocol.a7.ServiceHelper.SerializerType;
/**
 * 电视剧、综艺的选集界面
 * @author wuhd
 *
 */
public class AnthologyFragment extends BaseFragment {
	private SimplePlayerActivity mActivity;
	ImageView series_image;
	TextView series_name, series_numbers;
	LinearLayout series_language, series_type, series_actors;
//	WeightGridLayout series_grid;
	ImageFetcher mFetcher;
	ServiceHelper serviceHelper;
	
	String current_idx;
	String series_id;
//	SeriesInfo series;
	private GetHwResponse hwResponse;//电视剧实例
	VerticalViewPager2 series_viewpager;
	/**
	 * 一行显示多少影片
	 * */
	public int ROW_SIZE = 5;
	/**
	 * GridLayout一页显示几行
	 * */
	public static final int PAGE_ROW = 1;
	/**
	 * ViewPager一屏显示几行
	 * */
	public static final int MAX_PAGE_ROW =6;
	
	private Wiki wiki;
	
	SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMdd");
	
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container) {
		mActivity = (SimplePlayerActivity) getActivity();
		ViewGroup vg = (ViewGroup) inflater.inflate(R.layout.vod_fragment_anthology, container, false);
		series_image = (ImageView) vg.findViewById(R.id.series_image);
		series_name = (TextView) vg.findViewById(R.id.series_name);
		series_numbers = (TextView) vg.findViewById(R.id.series_numbers);
		series_language = (LinearLayout) vg.findViewById(R.id.series_language);
		series_type = (LinearLayout) vg.findViewById(R.id.series_type);
		series_actors = (LinearLayout) vg.findViewById(R.id.series_actors);
//		series_grid	 = (WeightGridLayout) vg.findViewById(R.id.series_grid);
		series_viewpager = (VerticalViewPager2) vg.findViewById(R.id.series_viewpager);
		
		mFetcher = SharedImageFetcher.getNewFetcher(mActivity, 1);
		serviceHelper = ServiceHelper.createOneHelper();
		serviceHelper.setSerializerType(SerializerType.JSON);
		Logger.d("series_id:"+series_id);
//		series_id = mActivity.seriesId;
		hwResponse = mActivity.getHwResponse();
		if(hwResponse == null){
			Tools.showToastMessage(mActivity, "电视剧集为空");
		}
		return vg;
	}
	
	@Override
	public void onResume() {
		super.onResume();
		if(hwResponse != null && hwResponse.getVod() != null && hwResponse.getVod().size()>0){
			updateUI();
		}else{
			Tools.showToastMessage(mActivity, "电视剧集数为0");
		}
	}
	
	private void getSeriesData(){/*
		String url = HomedAPI.MEDIA_SERIES_GET_INFO;
		RequestParams req = HomedDataManager.getRequestParams();
		req.put("seriesid", series_id);
		req.put("pageidx", "1");
		req.put("pagenum", "100");
		req.put("postersize","500x280");
		serviceHelper.setRootUrl(url);
		serviceHelper.callServiceAsync(mActivity, req, SeriesInfo.class,
				new ResponseHandlerT<SeriesInfo>() {

					@Override
					public void onResponse(boolean success, SeriesInfo result) {
						if (success && result != null && result.ret == 0 && result.video_list!= null && result.video_list.size()>0) {
							series = result;
							updateUI();
						} else {
							onDataError();
						}
					}
		});
	*/}
	
	private void onDataError() {
		Tools.showToastMessage(mActivity, "选集失败，请重试");
	}
	
	private void updateUI() {
		wiki = hwResponse.getWiki();
		String image_url = wiki.getCover();
		if (image_url != null)
			mFetcher.loadImage(image_url, series_image);
		else
			series_image.setImageResource(R.drawable.default_poster);
		series_name.setText(wiki.getTitle());
		if(hwResponse.getVod().size() > Integer.parseInt(wiki.getInfo().getEpisodes())){
			series_numbers.setText(wiki.getInfo().getEpisodes()+"集");
		}else{
			series_numbers.setText(hwResponse.getVod().size()+"集");
		}
		setSeriesLabels();
//		if (series.video_list.get(0).series_idx.length() >= 8)
//			ROW_SIZE = 1;
//		else 
		ROW_SIZE = 5;
		series_viewpager.setAdapter(new SeriesPagerAdapter());
		series_viewpager.requestFocus();
	}

	private void setSeriesLabels() {
		//设置语言
		if (wiki.getInfo().getLanguage() != null) {
			String[] languages = wiki.getInfo().getLanguage().split("\\|");
			series_language.removeAllViews();
			for(final String language : languages){
				if(language.equals(""))
					continue;
				final View convertView = LayoutInflater.from(mActivity).inflate(R.layout.vod_label_item, null,false);
				TextView direct_name = (TextView)convertView.findViewById(R.id.vod_label_name);
				direct_name.setText(language);
				if(convertView!=null){
					series_language.addView(convertView);
					LinearLayout.LayoutParams para = (LinearLayout.LayoutParams) convertView
							.getLayoutParams();
					para.leftMargin = 10;
					convertView.requestLayout();
				}
				convertView.setOnClickListener(new OnClickListener() {
					
					@Override
					public void onClick(View arg0) {
						HWDataManager.openVodTabActivity(mActivity, language);
					}
				});
			}
		}
		//设置类型
		if (wiki.getTags() != null && wiki.getTags().size() > 0) {
			series_type.removeAllViews();
			int width = 0;
			for(int i = 0; i < wiki.getTags().size() ; i ++ ){
				final String type = wiki.getTags().get(i);
				View convertView = LayoutInflater.from(mActivity).inflate(R.layout.vod_label_item, null,false);
				TextView type_name = (TextView)convertView.findViewById(R.id.vod_label_name);
				type_name.setText(type);
				if(convertView!=null){
					int w = View.MeasureSpec.makeMeasureSpec(0,View.MeasureSpec.UNSPECIFIED);  
					int h = View.MeasureSpec.makeMeasureSpec(0,View.MeasureSpec.UNSPECIFIED);  
					convertView.measure(w, h);
					width += (convertView.getMeasuredWidth() + 28);
					if(width >= 344){
						break;
					}
					series_type.addView(convertView);
					LinearLayout.LayoutParams para = (LinearLayout.LayoutParams) convertView
							.getLayoutParams();
					para.leftMargin = 10;
					convertView.requestLayout();
				}
				convertView.setOnClickListener(new OnClickListener() {
					
					@Override
					public void onClick(View arg0) {
						HWDataManager.openVodTabActivity(mActivity, type);
					}
				});
			}
		}
		//设置演员
		if (wiki.getInfo().getStarring() != null) {
			series_actors.removeAllViews();
			int width = 0;
			for(int i = 0 ; i < wiki.getInfo().getStarring().size() ; i++){
				final Star star = wiki.getInfo().getStarring().get(i);
				if(TextUtils.isEmpty(star.getId())){
					continue;
				}
				View convertView = LayoutInflater.from(mActivity).inflate(R.layout.vod_label_item, null,false);
				TextView actor_name = (TextView)convertView.findViewById(R.id.vod_label_name);
				actor_name.setText(star.getTitle());
				if(convertView!=null){
					int w = View.MeasureSpec.makeMeasureSpec(0,View.MeasureSpec.UNSPECIFIED);  
					int h = View.MeasureSpec.makeMeasureSpec(0,View.MeasureSpec.UNSPECIFIED);  
					convertView.measure(w, h);
					width += (convertView.getMeasuredWidth() + 28);
					if(width >= 344){
						break;
					}
					series_actors.addView(convertView);
					LinearLayout.LayoutParams para = (LinearLayout.LayoutParams) convertView
							.getLayoutParams();
					para.leftMargin = 10;
					convertView.requestLayout();
				}
				convertView.setOnClickListener(new OnClickListener() {
					
					@Override
					public void onClick(View arg0) {
						if(!TextUtils.isEmpty(star.getId())){
							HWDataManager.openStarShowActivity(mActivity, star.getId());
						}
					}
				});
			}
		}
	}
	
	class SeriesPagerAdapter extends PagerAdapter {
		
		@Override
		public float getPageWidth(int position) {
			return (PAGE_ROW + 0.0f) / MAX_PAGE_ROW;
		}

		@Override
		public int getCount() {
			int count = hwResponse.getVod().size();
			int rowDataCount = ROW_SIZE * PAGE_ROW;
			return count % rowDataCount == 0 ? count / rowDataCount : (count / rowDataCount + 1);
		}

		@Override
		public boolean isViewFromObject(View arg0, Object arg1) {
			return arg0 == arg1;
		}
		
		@Override
		public int getItemPosition(Object object) {
			return super.getItemPosition(object);
		}
		
		@Override
		public void destroyItem(ViewGroup container, int position, Object object) {
			View v = (View) object;
			container.removeView(v);
		}
		
		@Override
		public Object instantiateItem(ViewGroup container, int position) {
			View itemView = View.inflate(mActivity, R.layout.live_series_grid, null);
			WeightGridLayout grid = (WeightGridLayout) itemView.findViewById(R.id.series_grid);
			grid.setClipToPadding(false);
			grid.setTag(position);
			grid.setAdapter(new SeriesWeightGridAdapter(position,ROW_SIZE));
			itemView.setTag(position);
			container.addView(itemView);
			return itemView;
		}
		
	}
	/**
	 * 获得一页显示的总影片数
	 * */
	private int getPageDataCount() {
		return ROW_SIZE * PAGE_ROW;
	}
	private int getTotalFilmSize() {
		return hwResponse.getVod().size();
	}
	
	class SeriesWeightGridAdapter extends WeightGridAdapter {
		
		int xSize = 0;
		int row = 0;
		
		public SeriesWeightGridAdapter(int position, int rowSize) {
			this.row = position;
			this.xSize = rowSize;
		}

		@Override
		public int getCount() {
			return ((row+1) * getPageDataCount() > getTotalFilmSize()) ? (getTotalFilmSize() - row * getPageDataCount()) : getPageDataCount();
		}

		@Override
		public Object getItem(int position) {
			return position;
		}

		@Override
		public long getItemId(int position) {
			return position;
		}

		@Override
		public View getView(int position, View convertView, ViewGroup parent) {
			if (xSize == 5) {
				convertView = LayoutInflater.from(mActivity).inflate(R.layout.vod_item_grid, parent, false);
			} else if (xSize == 1){
				convertView = LayoutInflater.from(mActivity).inflate(R.layout.live_item_grid_1, parent, false);
			}
			final int number = row * getPageDataCount() + position;
			TextView index = (TextView) convertView.findViewById(R.id.series_index);
			final Vod event = hwResponse.getVod().get(number);
			String series_index = "";
//			if (event.series_idx.length() > 8) {
//				long time = Long.parseLong(event.series_idx);
//				series_index = sdf.format(new Date(time * 1000));
//			} else {
//				series_index = event.series_idx;
//			}
//			LogHelper.i("series index = " + series_index);
			index.setText((number+1)+"");
			convertView.setOnClickListener(new OnClickListener() {
				
				@Override
				public void onClick(View arg0) {
//					startToPlay(event);
					VodDataManager.getInstance(mActivity).requestTelevisionPlayUrl(hwResponse, number);
				}
			});
			return convertView;
		}

		@Override
		public int getChildXSize(int position) {
			return 1;
		}

		@Override
		public int getChildYSize(int position) {
			return 1;
		}

		@Override
		public int getXSize() {
			return xSize;
		}

		@Override
		public int getYSize() {
			return PAGE_ROW;
		}
		
		@Override
		public int getXSpace() {
			return 20;
		}
		
		@Override
		public int getYSpace() {
			return 20;
		}
		
	}
	
	public void startToPlay(Video item) {
//		mActivity.vodId = item.video_id+"";
//		mActivity.name = item.video_name;
//		mActivity.videoSurface.stopPlayback();//先停止当前播放，再开启新的播放
		mActivity.mFragmentFactory.showFragment(FragmentFactory.FRAGMENT_ID_LOADING, null);
	}
	
	@Override
	public boolean onKeyDown(int keyCode, KeyEvent event) {
		if(keyCode==KeyEvent.KEYCODE_DPAD_LEFT||keyCode==KeyEvent.KEYCODE_DPAD_RIGHT
				||keyCode==KeyEvent.KEYCODE_DPAD_UP||keyCode==KeyEvent.KEYCODE_DPAD_DOWN){
			changeFocus(keyCode);
			return true;
		}
		return super.onKeyDown(keyCode,event);
	}
}
