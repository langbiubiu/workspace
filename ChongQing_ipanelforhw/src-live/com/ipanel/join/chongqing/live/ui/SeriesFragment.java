package com.ipanel.join.chongqing.live.ui;

import java.lang.ref.SoftReference;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Stack;

import android.os.Handler;
import android.os.Message;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.VerticalViewPager2;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.RcKeyEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import cn.ipanel.android.LogHelper;
import cn.ipanel.android.net.imgcache.ImageFetcher;
import cn.ipanel.android.net.imgcache.SharedImageFetcher;
import cn.ipanel.android.widget.WeightGridLayout;
import cn.ipanel.android.widget.WeightGridLayout.WeightGridAdapter;

import com.ipanel.chongqing_ipanelforhw.R;
import com.ipanel.chongqing_ipanelforhw.hwstruct.GetHwRequest;
import com.ipanel.chongqing_ipanelforhw.hwstruct.GetHwResponse;
import com.ipanel.chongqing_ipanelforhw.hwstruct.GetHwResponse.Vod;
import com.ipanel.chongqing_ipanelforhw.hwstruct.GetHwResponse.Wiki;
import com.ipanel.chongqing_ipanelforhw.hwstruct.GetHwResponse.Wiki.Info.Star;
import com.ipanel.join.chongqing.live.base.BaseFragment;
import com.ipanel.join.chongqing.live.manager.impl.hw.HWDataManagerImpl;
import com.ipanel.join.chongqing.live.navi.DatabaseObjects.LiveChannel;
import com.ipanel.join.chongqing.portal.PortalDataManager;
import com.ipanel.join.protocol.a7.ServiceHelper;
import com.ipanel.join.protocol.a7.ServiceHelper.ResponseHandlerT;
import com.ipanel.join.protocol.a7.ServiceHelper.SerializerType;

public class SeriesFragment extends BaseFragment {
	
	private final static int REQUEST_SERIES_DATA_COMPLETE = 0;
	
	private final static int REQUEST_SERIES_DATA_FAILED = 1;
	
	ImageView series_image;
	TextView series_name, series_numbers;
	LinearLayout series_language, series_type, series_actors;
	VerticalViewPager2 series_viewpager;
	ImageFetcher mFetcher;
	ServiceHelper serviceHelper;
	TextView txt_01, txt_02, txt_03, txt_04, txt_05;
	
	Wiki wiki;
	List<Vod> vods;
	
	private Stack<SoftReference<View>> views = new Stack<SoftReference<View>>();
	/**
	 * 一行显示多少影片
	 * */
	public static int ROW_SIZE = 5;
	/**
	 * GridLayout一页显示几行
	 * */
	public static final int PAGE_ROW = 1;
	/**
	 * ViewPager一屏显示几行
	 * */
	public static final int MAX_PAGE_ROW =6;
	
	SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMdd");
	
	Handler mHandler = new Handler() {
		
		@Override
		public void handleMessage(Message msg) {
			// TODO Auto-generated method stub
			switch (msg.what) {
			case REQUEST_SERIES_DATA_COMPLETE:
				txt_01.setVisibility(View.VISIBLE);
				txt_02.setVisibility(View.VISIBLE);
				txt_03.setVisibility(View.VISIBLE);
				txt_04.setVisibility(View.VISIBLE);
				txt_05.setVisibility(View.VISIBLE);
				String image_url = wiki.getCover();
				if (image_url != null)
					mFetcher.loadImage(image_url, series_image);
				else
					series_image.setImageDrawable(null);
				series_name.setText(wiki.getTitle());
				if (wiki.getInfo().getEpisodes() != null && wiki.getInfo().getEpisodes() != "") {
					series_numbers.setText(wiki.getInfo().getEpisodes()+"集");
				}
				setSeriesLabels();
				if (vods != null && vods.size() > 0) {
					series_viewpager.setAdapter(new SeriesPagerAdapter());
					series_viewpager.requestFocus();
				}
				break;
			case REQUEST_SERIES_DATA_FAILED:
				series_image.setImageResource(R.drawable.live_default_series_poster);
				txt_01.setVisibility(View.INVISIBLE);
				txt_02.setVisibility(View.INVISIBLE);
				txt_03.setVisibility(View.INVISIBLE);
				txt_04.setVisibility(View.INVISIBLE);
				txt_05.setVisibility(View.INVISIBLE);
				break;
			default:
				break;
			}
		}
	};
	
	private void setSeriesLabels() {
		if (wiki.getInfo().getLanguage() != null && wiki.getInfo().getLanguage() != "") {
			series_language.removeAllViews();
			String[] languages = wiki.getInfo().getLanguage().split(",");
			for (final String language : languages) {
				View convertView = LayoutInflater.from(getLiveActivity()).inflate(R.layout.live_item_series, null,false);
				TextView direct_name = (TextView)convertView.findViewById(R.id.label_name);
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
						// TODO Auto-generated method stub
						
					}
				});
			}
		}
		if (wiki.getTags() != null && wiki.getTags().size() >0) {
			series_type.removeAllViews();
			int width = 0;
			for (final String tag : wiki.getTags()) {
				if(tag.equals(""))
					continue;
				View convertView = LayoutInflater.from(getLiveActivity()).inflate(R.layout.live_item_series, null,false);
				TextView type_name = (TextView)convertView.findViewById(R.id.label_name);
				type_name.setText(tag);
				if(convertView!=null){
					int w = View.MeasureSpec.makeMeasureSpec(0,View.MeasureSpec.UNSPECIFIED);  
					int h = View.MeasureSpec.makeMeasureSpec(0,View.MeasureSpec.UNSPECIFIED);  
					convertView.measure(w, h);
					width += (convertView.getMeasuredWidth() + 28);
					if(width >= series_type.getWidth()){
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
						// TODO Auto-generated method stub
						
					}
				});
			}
		}
		if (wiki.getInfo().getStarring() != null && wiki.getInfo().getStarring().size() > 0) {
			series_actors.removeAllViews();
			int width = 0;
			for (final Star actor : wiki.getInfo().getStarring()) {
				if(actor.getTitle().equals(""))
					continue;
				View convertView = LayoutInflater.from(getLiveActivity()).inflate(R.layout.live_item_series, null,false);
				TextView actor_name = (TextView)convertView.findViewById(R.id.label_name);
				actor_name.setText(actor.getTitle());
				if(convertView!=null){
					int w = View.MeasureSpec.makeMeasureSpec(0,View.MeasureSpec.UNSPECIFIED);  
					int h = View.MeasureSpec.makeMeasureSpec(0,View.MeasureSpec.UNSPECIFIED);  
					convertView.measure(w, h);
					width += (convertView.getMeasuredWidth() + 28);
					if(width >= series_actors.getWidth()){
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
						
					}
				});
			}
		}
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup root) {
		// TODO Auto-generated method stub
		ViewGroup vg = (ViewGroup) inflater.inflate(R.layout.live_fragment_series, root, false);
		series_image = (ImageView) vg.findViewById(R.id.series_image);
		series_name = (TextView) vg.findViewById(R.id.series_name);
		series_numbers = (TextView) vg.findViewById(R.id.series_numbers);
		series_language = (LinearLayout) vg.findViewById(R.id.series_language);
		series_type = (LinearLayout) vg.findViewById(R.id.series_type);
		series_actors = (LinearLayout) vg.findViewById(R.id.series_actors);
		series_viewpager = (VerticalViewPager2) vg.findViewById(R.id.series_viewpager);
		txt_01 = (TextView) vg.findViewById(R.id.series_txt_01);
		txt_02 = (TextView) vg.findViewById(R.id.series_txt_02);
		txt_03 = (TextView) vg.findViewById(R.id.series_txt_03);
		txt_04 = (TextView) vg.findViewById(R.id.series_txt_04);
		txt_05 = (TextView) vg.findViewById(R.id.series_txt_05);
		
		mFetcher = SharedImageFetcher.getNewFetcher(root.getContext(), 1);
		serviceHelper = ServiceHelper.createOneHelper();
		serviceHelper.setSerializerType(SerializerType.JSON);
		return vg;
	}

	@Override
	public void onShow() {
		// TODO Auto-generated method stub
		LiveChannel channel = getLiveActivity().getStationManager().getPlayChannel();
		String id = ((HWDataManagerImpl)getLiveActivity().getDataManager()).mHWMap.get(channel.getChannelKey().getProgram()+"");
		if (id != null) {
			requestProgramWiki(id);
		} else {
			mHandler.sendEmptyMessage(REQUEST_SERIES_DATA_FAILED);
		}
	}
	
	private void requestProgramWiki(String id) {
		GetHwRequest req = new GetHwRequest();
		req.setAction("GetWikiInfoByChannel");
		req.getDevice().setDnum("123");
		req.getUser().setUserid("123");
		req.getParam().setChannelId(id);
		serviceHelper.setRootUrl(PortalDataManager.url);
		serviceHelper.setSerializerType(SerializerType.JSON);
		serviceHelper.callServiceAsync(getLiveActivity(), req, GetHwResponse.class, new ResponseHandlerT<GetHwResponse>() {
			
			@Override
			public void onResponse(boolean success, GetHwResponse result) {
				// TODO Auto-generated method stub
				if (!success) {
					LogHelper.i("request detail data failed");
					mHandler.sendEmptyMessage(REQUEST_SERIES_DATA_FAILED);
					return;
				}
				if (result == null) {
					LogHelper.i("failed to parse JSON data");
					mHandler.sendEmptyMessage(REQUEST_SERIES_DATA_FAILED);
					return;
				}
				if (result.getError().getCode() != 0) {
					mHandler.sendEmptyMessage(REQUEST_SERIES_DATA_FAILED);
				} else {
					wiki = result.getWiki();
					vods = result.getVod();
					mHandler.sendEmptyMessage(REQUEST_SERIES_DATA_COMPLETE);
				}
			}
		});
	}

	@Override
	public void onRefresh() {
		// TODO Auto-generated method stub

	}

	@Override
	public void onHide() {
		// TODO Auto-generated method stub

	}

	@Override
	public void onDataChange(int type, Object data) {
		// TODO Auto-generated method stub

	}
	
	class SeriesPagerAdapter extends PagerAdapter {
		
		@Override
		public float getPageWidth(int position) {
			return (PAGE_ROW + 0.0f) / MAX_PAGE_ROW;
		}

		@Override
		public int getCount() {
			// TODO Auto-generated method stub
			int count = vods.size();
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
			ViewGroup v = (ViewGroup) object;
			views.push(new SoftReference<View>(v));
			container.removeView(v);
		}
		
		@Override
		public Object instantiateItem(ViewGroup container, int position) {
			// TODO Auto-generated method stub
			LogHelper.i("view pager adapter getCount() = " +this.getCount() + ", ROW_SIZE = " + ROW_SIZE + ", PAGE_ROW = " + PAGE_ROW);
			View itemView = getViewFromSoftReference();
			if (itemView == null) {
				itemView = View.inflate(getLiveActivity(), R.layout.live_series_grid, null);
			}
			WeightGridLayout grid = (WeightGridLayout) itemView.findViewById(R.id.series_grid);
			grid.setClipToPadding(false);
			grid.setTag(position);
			grid.setAdapter(new SeriesWeightGridAdapter(position,ROW_SIZE));
			itemView.setTag(position);
			container.addView(itemView);
			return itemView;
		}

		private View getViewFromSoftReference() {
			// TODO Auto-generated method stub
			View view = null;
			while (!views.empty()) {
				view = views.pop().get();
				if (view != null)
					return view;
			}
			return null;
		}
		
	}
	
	/**
	 * 获得一页显示的总影片数
	 * */
	private int getPageDataCount() {
		return ROW_SIZE * PAGE_ROW;
	}
	
	private int getTotalFilmSize() {
		return vods.size();
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
			// TODO Auto-generated method stub
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
			convertView = LayoutInflater.from(getLiveActivity()).inflate(R.layout.live_item_grid, parent, false);
			TextView index = (TextView) convertView.findViewById(R.id.series_index);
			Vod vod = vods.get(row * getPageDataCount() + position);
			index.setText(position);
			convertView.setOnClickListener(new OnClickListener() {
				
				@Override
				public void onClick(View arg0) {
					
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
	
	@Override
	public boolean onKeyDown(int keyCode, KeyEvent event) {
		// TODO Auto-generated method stub
		switch (keyCode) {
		case KeyEvent.KEYCODE_BACK:
		case RcKeyEvent.KEYCODE_QUIT:
		case KeyEvent.KEYCODE_MENU:
		case RcKeyEvent.KEYCODE_TV_ADD:
			return false;
		}
		return true;
	}

}
