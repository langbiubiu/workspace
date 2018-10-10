package com.ipanel.join.cq.vod.vodhome;

import java.lang.ref.SoftReference;
import java.util.ArrayList;
import java.util.List;
import java.util.Stack;

import android.app.Fragment;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.VerticalViewPager2;
import android.support.v4.view.VerticalViewPager2.OnPageChangeListener;
import android.support.v4.view.ViewPager2;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnFocusChangeListener;
import android.view.View.OnKeyListener;
import android.view.ViewGroup;
import android.view.ViewTreeObserver.OnGlobalFocusChangeListener;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.RatingBar;
import android.widget.RelativeLayout;
import android.widget.TextView;
import cn.ipanel.android.LogHelper;
import cn.ipanel.android.net.imgcache.BaseImageFetchTask;
import cn.ipanel.android.net.imgcache.ImageFetchTask;
import cn.ipanel.android.net.imgcache.ImageFetcher;
import cn.ipanel.android.net.imgcache.ImageWorker;
import cn.ipanel.android.net.imgcache.SharedImageFetcher;
import cn.ipanel.android.widget.WeightGridLayout;
import cn.ipanel.android.widget.WeightGridLayout.WeightGridAdapter;

import com.ipanel.chongqing_ipanelforhw.R;
import com.ipanel.chongqing_ipanelforhw.hwstruct.GetHwRequest;
import com.ipanel.chongqing_ipanelforhw.hwstruct.GetHwResponse;
import com.ipanel.chongqing_ipanelforhw.hwstruct.GetHwResponse.Filter;
import com.ipanel.chongqing_ipanelforhw.hwstruct.GetHwResponse.Filter.FilterItem;
import com.ipanel.chongqing_ipanelforhw.hwstruct.GetHwResponse.Wiki;
import com.ipanel.join.cq.vod.HWDataManager;
import com.ipanel.join.cq.vod.utils.Logger;
import com.ipanel.join.cq.vod.utils.Tools;
import com.ipanel.join.protocol.a7.ServiceHelper;
import com.ipanel.join.protocol.a7.ServiceHelper.ResponseHandlerT;
import com.ipanel.join.protocol.a7.ServiceHelper.SerializerType;
//电影&电视剧筛选界面
public class SortFragment extends Fragment implements OnClickListener {
	public static final String TAG = SortFragment.class.getSimpleName();
	private static final String FILM = "film";
	private static final String TELEPLAY = "teleplay";
	private static final int PAGE_SIZE = 20;
	private ServiceHelper serviceHelper;
	private ImageFetcher mImageFetcher;
	private FilmListActivity mActivity;
	private String model;
	private int sort = 1;//默认排序方式为最热
	private VerticalViewPager2 sortListView1;// 类型
	private VerticalViewPager2 sortListView2;// 地区
	private VerticalViewPager2 sortListView3;// 年代
	private VerticalViewPager2 viewPager;
	private Stack<SoftReference<View>> views = new Stack<SoftReference<View>>();
	/**
	 * 栏目数据加载完成消息
	 * */
	public static final int MSG_COLUME_DATA_LOADED = 0;
	/**
	 * 一页影片数据加载完成消息
	 * */
	public static final int MSG_ROW_DATA_LOAED = 1;
	/**
	 * 一屏显示几行
	 * */
	public static final int PAGE_ROW = 1;
	/**
	 * ViewPager一屏显示几行
	 * */
	public static final int MAX_PAGE_ROW = 2;
	private TextView title;//栏目名称
	private TextView sort1;
	private TextView sort2;
	private TextView sort3;//分别按热度、时间、评分排序
	private TextView page;
	private TextView totalPage;
	private int row_size = 4;//每行默认为4个
	private ImageView arrow_down1,arrow_down2,arrow_down3;
	private ImageView arrow_up1,arrow_up2,arrow_up3;
	private Wiki[] mData;
	private String cur_tag = "";
	private String cur_country = "";
	private String cur_released = "";
	private VODPagerAdapter pagerAdapter;
	private int totalMovieNumber = -1;
	private int sort_index1 = 0;//分别记录三种排序的位置
	private int sort_index2 = 0;
	private int sort_index3 = 0;
	
	Handler mHandler = new Handler() {
		@Override
		public void handleMessage(Message msg) {
			switch (msg.what) {
			//栏目数据加载完成
			case MSG_COLUME_DATA_LOADED:
				Logger.d("-->msg MSG_COLUME_DATA_LOADED");
				if(pagerAdapter == null){
					pagerAdapter = new VODPagerAdapter();
				}else{
					pagerAdapter.notifyDataSetChanged();
				}
				viewPager.setAdapter(pagerAdapter);
				refreshPage(1);
				break;
			//一页影片数据加载完成	
			case MSG_ROW_DATA_LOAED:
				if (msg.obj instanceof RowRequestParam) {
					RowRequestParam param = (RowRequestParam) msg.obj;
					if (viewPager.findViewWithTag(param.row) == null) {
						return;
					}
					Logger.d("-->msg 一页影片数据加载完成	");
					WeightGridLayout grid = (WeightGridLayout) viewPager.findViewWithTag(param.row).findViewById(
							R.id.hotgrid);
					if (grid != null) {
						refeshWeightLayoutChild(param, grid);
						Logger.d("-->handler刷新影片信息");
					}
				}
				break;
			}
		}
	};
	
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		View view = inflater.inflate(R.layout.vod_filmlist_layout_02, container,false);
		mActivity = (FilmListActivity) getActivity();
		serviceHelper = ServiceHelper.getHelper();
		serviceHelper.setSerializerType(SerializerType.JSON);
		serviceHelper.setRootUrl(HWDataManager.ROOT_URL);
		mImageFetcher = SharedImageFetcher.getSharedFetcher(mActivity);
		model = mActivity.getColumnName().equals("电影") ? FILM : TELEPLAY;
		Logger.d(TAG,"columnName-->"+model);
		initViews(view);
		initControl();
		initData();
		getCategory();
		return view;
	}
	
	@Override
	public void onResume() {
		super.onResume();
	}
	
	private void initViews(View view) {
		sortListView1 = (VerticalViewPager2)view.findViewById(R.id.listview1);
		sortListView2 = (VerticalViewPager2)view.findViewById(R.id.listview2);
		sortListView3 = (VerticalViewPager2)view.findViewById(R.id.listview3);
		
		sort1 = (TextView)view.findViewById(R.id.sort1);
		sort2 = (TextView)view.findViewById(R.id.sort2);
		sort3 = (TextView)view.findViewById(R.id.sort3);
		viewPager = (VerticalViewPager2)view.findViewById(R.id.sort_viewpager);
		title = (TextView)view.findViewById(R.id.sort_title);
		title.setText(mActivity.getColumnName());
		
		page = (TextView)view.findViewById(R.id.sort_page);
		totalPage = (TextView)view.findViewById(R.id.sort_totalpage);
		
		arrow_down1 = (ImageView)view.findViewById(R.id.arrow1down);
		arrow_down2 = (ImageView)view.findViewById(R.id.arrow2down);
		arrow_down3 = (ImageView)view.findViewById(R.id.arrow3down);
		arrow_up1 = (ImageView)view.findViewById(R.id.arrow1up);
		arrow_up2 = (ImageView)view.findViewById(R.id.arrow2up);
		arrow_up3 = (ImageView)view.findViewById(R.id.arrow3up);
	}
	
	@Override
	public void onPause() {
		super.onPause();
	}
	
	@Override
	public void onDestroy() {
		super.onDestroy();
	}
	
	private void initData() {
		getWikisByCategory(sort, model, cur_tag, cur_country, cur_released);
	}
	/**
	 * 获取wiki分类参数
	 */
	public void getCategory(){
		Logger.d(TAG, "getCategory");
		GetHwRequest request = HWDataManager.getHwRequest();
		request.setAction(HWDataManager.ACTION_GET_CATEGORY);
		serviceHelper.callServiceAsync(mActivity, request, GetHwResponse.class, new ResponseHandlerT<GetHwResponse>() {
			
			@Override
			public void onResponse(boolean success, GetHwResponse result) {
				if(!success || result==null){
					Tools.showToastMessage(mActivity, "获取参数失败");
					return;
				}
				List<Filter> filters = result.getFilters();
				if(filters!=null && filters.size() > 0){
					setCategoryList(filters);
				}
			}
		});
	}
	private void setCategoryList(List<Filter> filters) {
		if(filters.get(0).getFilter() != null && filters.get(0).getFilter().size() > 0){
			sortListView1.setAdapter(new SortAdapter(filters.get(0).getFilter(),1));
			refreshBackground(1);
			refreshArrow(1);
		}
		if(filters.get(1).getFilter() != null && filters.get(1).getFilter().size() > 0){
			sortListView2.setAdapter(new SortAdapter(filters.get(1).getFilter(),2));
			refreshBackground(2);
			refreshArrow(2);
		}
		if(filters.get(2).getFilter() != null && filters.get(2).getFilter().size() > 0){
			sortListView3.setAdapter(new SortAdapter(filters.get(2).getFilter(),3));
			refreshBackground(3);
			refreshArrow(3);
		}
	}
	
	/**
	 * 
	 * @param sort 排序方式，1最热   2最新   3评分
	 * @param model 类型： film电影， teleplay电视剧，television综艺节目
	 * @param tag 视频分类 不传则为全部
	 * @param country 地区分类
	 * @param released 上映年代
	 */
	public void getWikisByCategory(int sort,String model,String tag,String country,String released) {
		Logger.d(TAG, String.format("sort:%s,model:%s,tag:%s,country:%s,released:%s", sort,model,tag,country,released));
		GetHwRequest request = HWDataManager.getHwRequest();
		request.setAction(HWDataManager.ACTION_GET_WIKIS_BY_CATEGORY);
		request.getParam().setPage(1);
		request.getParam().setPagesize(PAGE_SIZE);
		request.getParam().setTag(tag);
		request.getParam().setSort(sort);
		request.getParam().setModel(model);
		request.getParam().setCountry(country);
		request.getParam().setReleased(released);
		
		serviceHelper.callServiceAsync(mActivity, request, GetHwResponse.class, new ResponseHandlerT<GetHwResponse>() {
			
			@Override
			public void onResponse(boolean success, GetHwResponse result) {
				if(success && result.getError().getCode() == 0){
					List<Wiki> list = result.getWikis();
					if(list != null && list.size() > 0){
						totalMovieNumber = result.getTotal();
						mData = new Wiki[totalMovieNumber];// 初始化数组
						for (int i = 0; i < list.size(); i++) {
							Wiki item = list.get(i);
							mData[i] = item;
						}
						mHandler.obtainMessage(MSG_COLUME_DATA_LOADED).sendToTarget();
					}else{
						viewPager.removeAllViews();
						page.setText(0+"");
						totalPage.setText("/"+0+"页");
					}
				}else{
					Tools.showToastMessage(mActivity, result.getError().getInfo());
				}
			}
		});
	}
	/**
	 * 设置监听
	 */
	protected void initControl() {
		sort1.setOnClickListener(this);
		sort2.setOnClickListener(this);
		sort3.setOnClickListener(this);
		setSortSelected(sort);
		viewPager.setMove_flag(false);
		viewPager.setOffscreenPageLimit(2);
		viewPager.setOnPageChangeListener(new OnPageChangeListener() {

			@Override
			public void onPageSelected(int position) {
				refreshPage(position+1);
			}

			@Override
			public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {

			}

			@Override
			public void onPageScrollStateChanged(int state) {
				if (state == ViewPager2.SCROLL_STATE_IDLE) {
//					moveIndicatorToFocus();
					mImageFetcher.setPauseWork(false);
				} else {
					mImageFetcher.setPauseWork(true);
				}
			}
		});
		viewPager.getViewTreeObserver().addOnGlobalFocusChangeListener(new OnGlobalFocusChangeListener() {
			@Override
			public void onGlobalFocusChanged(View oldFocus, View newFocus) {
				if (viewPager.getScrollState() == VerticalViewPager2.SCROLL_STATE_IDLE) {
					if (newFocus instanceof FrameLayout) {
//						moveIndicatorToFocus();
					}
				}
			}
		});
		
		//监听箭头的显示和隐藏
		sortListView1.setOnPageChangeListener(new OnPageChangeListener() {
			
			@Override
			public void onPageSelected(int position) {
				refreshArrow(1);
				refreshBackground(1);
			}
			
			@Override
			public void onPageScrolled(int position, float positionOffset,
					int positionOffsetPixels) {
				
				
			}
			
			@Override
			public void onPageScrollStateChanged(int state) {
				if(state == VerticalViewPager2.SCROLL_STATE_IDLE){
					refreshArrow(1);
					refreshBackground(1);
				}
			}
		});
		sortListView2.setOnPageChangeListener(new OnPageChangeListener() {
			
			@Override
			public void onPageSelected(int position) {
				refreshArrow(2);
				refreshBackground(2);
			}
			
			@Override
			public void onPageScrolled(int position, float positionOffset,
					int positionOffsetPixels) {
				
				
			}
			
			@Override
			public void onPageScrollStateChanged(int state) {
				if(state == VerticalViewPager2.SCROLL_STATE_IDLE){
					refreshArrow(2);
					refreshBackground(2);
				}
			}
		});
		sortListView3.setOnPageChangeListener(new OnPageChangeListener() {
			
			@Override
			public void onPageSelected(int position) {
				refreshArrow(3);
				refreshBackground(3);
			}
			
			@Override
			public void onPageScrolled(int position, float positionOffset,
					int positionOffsetPixels) {
				
				
			}
			
			@Override
			public void onPageScrollStateChanged(int state) {
				if(state == VerticalViewPager2.SCROLL_STATE_IDLE){
					refreshArrow(3);
					refreshBackground(3);
				}
			}
		});
	}
	/**
	 * 刷新上下箭头
	 */
	private void refreshArrow(int i) {
		switch (i) {
		case 1:
			arrow_down1.setVisibility(sortListView1.hasDownHideItem()?View.VISIBLE:View.INVISIBLE);
			arrow_up1.setVisibility(sortListView1.hasUpHideItem()?View.VISIBLE:View.INVISIBLE);
			break;
		case 2:
			arrow_down2.setVisibility(sortListView2.hasDownHideItem()?View.VISIBLE:View.INVISIBLE);
			arrow_up2.setVisibility(sortListView2.hasUpHideItem()?View.VISIBLE:View.INVISIBLE);
			break;
		case 3:
			arrow_down3.setVisibility(sortListView3.hasDownHideItem()?View.VISIBLE:View.INVISIBLE);
			arrow_up3.setVisibility(sortListView3.hasUpHideItem()?View.VISIBLE:View.INVISIBLE);
			break;

		default:
			break;
		}
	}
	/**
	 * 请求行数据
	 * @param row
	 */
	public void requestRowData(int row) {
		List<RowRequestParam> params = getRowRequestParam(row);
		final int count = params.size();
		LogHelper.i("request colume " + row);
		for (int i = 0; i < count; i++) {
			final RowRequestParam param = params.get(i);
			LogHelper.i(param.toString());
			if (checkRowData(param)) {
				noticyRowData(param);
			} else {
				Logger.d(TAG, "");
				GetHwRequest request = HWDataManager.getHwRequest();
				request.setAction(HWDataManager.ACTION_GET_WIKIS_BY_CATEGORY);
				request.getParam().setPage((param.data_start/row_size+1));
				request.getParam().setPagesize(row_size);
				request.getParam().setTag(cur_tag);
				request.getParam().setSort(sort);
				request.getParam().setModel(model);
				request.getParam().setCountry(cur_country);
				request.getParam().setReleased(cur_released);
				
				serviceHelper.callServiceAsync(mActivity, request, GetHwResponse.class, new ResponseHandlerT<GetHwResponse>() {
					
					@Override
					public void onResponse(boolean success, GetHwResponse result) {
						if(success && result.getError().getCode() == 0){
							List<Wiki> list = result.getWikis();
							if(list != null && list.size() > 0){
								fillColumeData(list, param);
							}
						}else{
							Tools.showToastMessage(mActivity, result.getError().getInfo());
						}
					}
				});
			}
		}	
	}
	
	/**
	 * 计算ViewPager一行的数据请求参数
	 * */
	private List<RowRequestParam> getRowRequestParam(int row) {
		List<RowRequestParam> params = new ArrayList<RowRequestParam>();
		int start = row * getPageDataCount();
		int end = start + getPageDataCount();
		int val = totalMovieNumber;

		RowRequestParam param = new RowRequestParam();
		param.data_start = start;
		param.view_start = 0;
		param.count = (end > val) ? val % getPageDataCount() : getPageDataCount();
		param.row = row;
		param.data = new Wiki[param.count];
		params.add(param);

		return params;
	}
	/**
	 * 判断某行的数据是否完整
	 * */
	private boolean checkRowData(RowRequestParam param) {
		boolean result = true;
		for (int i = param.data_start; i < param.data_start + param.count; i++) {
			if ((param.data[i - param.data_start] = mData[i]) == null) {
				result = false;
			}
		}
		return result;
	}
	/**
	 * 通知刷新界面
	 * */
	public void noticyRowData(RowRequestParam param) {
		LogHelper.i("nocity refresh row : " + param.row);
		mHandler.obtainMessage(MSG_ROW_DATA_LOAED, param).sendToTarget();
	}
	/**
	 * 填充栏目数据
	 * @param list
	 * @param param
	 */
	protected void fillColumeData(List<Wiki> list,RowRequestParam param) {
		LogHelper.i(String.format(
				"fill %s  ' colume film data %s  betwen %s and %s",
				param.columeID, list.size(), param.data_start, param.data_start
						+ param.count));
		int sub_count = list.size();
		int count = mData.length;
		for (int i = 0; i < param.count; i++) {
			mData[(param.data_start + i) % count] = list.get(i % sub_count);
		}
		if (checkRowData(param)) {
			noticyRowData(param);
		}
	}
	/**
	 * 刷新影片信息
	 * */
	private void refeshWeightLayoutChild(RowRequestParam param, WeightGridLayout layout) {
		int start = param.view_start;
		int end = Math.min(layout.getChildCount(), start + param.count);
		for (int i = param.view_start; i < end; i++) {
			View convertView = layout.getChildAt(i);
			final Wiki vodFilm = param.data[(i - param.view_start)
					% param.data.length];
			final ImageView image = (ImageView) convertView
					.findViewById(R.id.film_img);
			final TextView name = (TextView) convertView
					.findViewById(R.id.film_name);
			final TextView score = (TextView) convertView.findViewById(R.id.score_text);
			final RatingBar ratingbar = (RatingBar)convertView.findViewById(R.id.score_ratingbar);
			if (vodFilm == null) {
				image.setBackgroundResource(R.drawable.default_poster);
			} else {
				convertView.setOnClickListener(new OnClickListener() {
					@Override
					public void onClick(View arg0) {
						//TODO 跳转到详情
						if(vodFilm.getModel().equals(FILM) || vodFilm.getModel().equals(TELEPLAY)){
							HWDataManager.openDetail(mActivity, vodFilm.getId(), vodFilm.getTitle(), vodFilm);
						}
					}
				});
				image.setBackgroundResource(R.drawable.default_poster);
				name.setText(vodFilm.getTitle());
				score.setText(vodFilm.getInfo().getAverage()+"");
				ratingbar.setRating(Float.parseFloat(vodFilm.getInfo().getAverage())/2.0f);//设置评分
				
				if (vodFilm.getCover() != null
						&& image.getVisibility() == View.VISIBLE) {
					BaseImageFetchTask task = mImageFetcher.getBaseTask(vodFilm.getCover());
					mImageFetcher.setImageSize(241, 301);
					mImageFetcher.setLoadingImage(R.drawable.default_poster);
					mImageFetcher.loadImage(task, image);
				} else {
					image.setBackgroundResource(R.drawable.default_poster);
				}
			}
		}
	}
	
	/**
	 * 清除无效的图片加载任务
	 * */
	private void clearTasks(ViewGroup container) {
		int N = container.getChildCount();
		for (int i = 0; i < N; i++) {
			if (container.getChildAt(i).getTag() instanceof BaseImageFetchTask) {
				ImageWorker.cancelPotentialWork((ImageFetchTask) container.getChildAt(i).getTag(),
						container.getChildAt(i));
			} else {
				if (container.getChildAt(i) instanceof ViewGroup) {
					clearTasks((ViewGroup) container.getChildAt(i));
				}
			}
		}
	}
	
	private View getViewFromSoftReference() {
		View view = null;
		while (!views.empty()) {
			view = views.pop().get();
			if (view != null)
				return view;
		}
		return null;
	}
	//刷新页数
	private void refreshPage(int i) {
		page.setText(i+"");
		totalPage.setText("/"+getTotalPageSize()+"页");
	}
	
	private int getTotalFilmSize() {
		return totalMovieNumber;
	}
	
	
	/**
	 * 获得ViewPager的总行数
	 * */
	private int getTotalPageSize() {
		int count = getTotalFilmSize();
		return count % getPageDataCount() == 0 ? (count / getPageDataCount()) : (count / getPageDataCount() + 1);
	}
	
	/**
	 * 获得一页显示的总影片数
	 * */
	private int getPageDataCount() {
		return row_size * PAGE_ROW;
	}
	
	class VODPagerAdapter extends PagerAdapter {

		@Override
		public float getPageWidth(int position) {
			return (PAGE_ROW + 0.0f) / MAX_PAGE_ROW;  
		}

		@Override
		public int getCount() {
			return getTotalPageSize();
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
			clearTasks(v);
			views.push(new SoftReference<View>(v));
			container.removeView(v);
		}

		@Override
		public Object instantiateItem(ViewGroup container, final int position) {
			long time=System.currentTimeMillis();
			View itemView = getViewFromSoftReference();
			if (itemView == null) {
				itemView = View.inflate(mActivity, R.layout.vod_hotpageritem, null);
			}
			WeightGridLayout mWeightLayout = (WeightGridLayout) itemView.findViewById(R.id.hotgrid);
			mWeightLayout.setClipToPadding(false);
			mWeightLayout.setTag(position);
			mWeightLayout.setAdapter(new VODWeightGridAdapter(position));
			itemView.setTag(position);
			requestRowData(position);
			container.addView(itemView);
			LogHelper.e("instantiateItem view take time : "+(System.currentTimeMillis()-time));
			return itemView;
		}
	}
	
	class VODWeightGridAdapter extends WeightGridAdapter {
		
		int row;

		public VODWeightGridAdapter(int row) {
			this.row = row;
		}

		@Override
		public int getCount() {
			return getPageDataCount();
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
			if (convertView == null) {
				convertView = LayoutInflater.from(mActivity).inflate(R.layout.vod_hot_element3, parent, false);
			}
			final ImageView image = (ImageView) convertView.findViewById(R.id.film_img);
			final TextView name = (TextView) convertView.findViewById(R.id.film_name);
			final RelativeLayout score_layout = (RelativeLayout)convertView.findViewById(R.id.score_layout);
			convertView.setOnClickListener(new OnClickListener() {

				@Override
				public void onClick(View arg0) {
				}
			});
			convertView.setOnFocusChangeListener(new OnFocusChangeListener() {

				@Override
				public void onFocusChange(View v, boolean hasFocus) {
					if (hasFocus) {
						name.setSelected(true);
						score_layout.setVisibility(View.VISIBLE);
					} else {
						name.setSelected(false);
						score_layout.setVisibility(View.INVISIBLE);
					}
				}
			});
			convertView.setTag(row + "");
			if (row * getPageDataCount() + position >= getTotalFilmSize()) {
				convertView.setVisibility(View.GONE);
			} else {
				convertView.setVisibility(View.VISIBLE);
			}
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
			return row_size;
		}
		
		@Override
		public int getYSize() {
			return PAGE_ROW;
		}

		@Override
		public int getYSpace() {
			return 35;
		}

		@Override
		public int getXSpace() {
			return 45;
		}

	}
	
	/**
	 * 行请求参数
	 */
	public class RowRequestParam {
		public String columeID;
		public int count;
		public int data_start;
		public int view_start;
		public int row;
		public Wiki[] data;
		@Override
		public String toString() {
			return "RowRequestParam [columeID=" + columeID + ", count=" + count + ", data_start=" + data_start
					+ ", view_start=" + view_start + "]";
		}
	}
	@Override
	public void onClick(View v) {
		switch (v.getId()) {
		case R.id.sort1:
			sort = 1;
			initData();
			setSortSelected(1);
			break;
		case R.id.sort2:
			sort = 2;
			initData();
			setSortSelected(2);
			break;
		case R.id.sort3:
			sort = 3;
			initData();
			setSortSelected(3);
			break;

		default:
			break;
		}
	}
	
	private void setSortSelected(int i){
		switch (i) {
		case 1:
			sort1.setSelected(true);
			sort2.setSelected(false);
			sort3.setSelected(false);
			break;
		case 2:
			sort1.setSelected(false);
			sort2.setSelected(true);
			sort3.setSelected(false);
			break;
		case 3:
			sort1.setSelected(false);
			sort2.setSelected(false);
			sort3.setSelected(true);
			break;

		default:
			break;
		}
	}
	
	//按二级栏目分类筛选
	class SortAdapter extends PagerAdapter {
		private List<FilterItem> list; // 数据集合
		private int index;//表示第几行的数据
		public SortAdapter(List<FilterItem> list,int index) {
			Filter.FilterItem item = new GetHwResponse().new  Filter().new FilterItem();
			item.setName("全部");
			item.setValue("");
			list.add(0, item);
			this.list = list;
			this.index = index;
		}

		@Override
		public int getCount() {
			if (list != null) {
				return list.size();
			}
			return 0;
		}

		@Override
		public boolean isViewFromObject(View arg0, Object arg1) {
			return arg0 == arg1;
		}

		@Override
		public void destroyItem(ViewGroup container, int position, Object object) {
			View view = (View)object;
			container.removeView(view);
		}

		@Override
		public float getPageWidth(int position) {
			return 1.0f / 8;
		}

		@Override
		public Object instantiateItem(ViewGroup container, final int position) {
			View view = LayoutInflater.from(mActivity).inflate(R.layout.vod_textview_item, null);
			TextView tv = (TextView)view.findViewById(R.id.text);
			tv.setText(list.get(position).getName());
			view.setOnClickListener(new OnClickListener() {
				
				@Override
				public void onClick(View arg0) {
					
					switch (index) {
					case 1:
						cur_tag = list.get(position).getValue();
						initData();
						sort_index1 = position;
						refreshBackground(1);
						break;
					case 2:
						cur_country = list.get(position).getValue();
						initData();
						sort_index2 = position;
						refreshBackground(2);
						break;
					case 3:
						cur_released = list.get(position).getValue();
						initData();
						sort_index3 = position;
						refreshBackground(3);
						break;
					default:
						break;
					}
				}
			});
			view.setOnKeyListener(new OnKeyListener() {
				
				@Override
				public boolean onKey(View parent, int keyCode, KeyEvent event) {
					if(event.getAction()==KeyEvent.ACTION_DOWN){
						switch (index) {
						case 1:
							if(keyCode==KeyEvent.KEYCODE_DPAD_RIGHT){
								sortListView2.getChildAt(sortListView2.getCurrentItem()).requestFocus();
								return true;
							}
							break;
						case 2:
							if(keyCode==KeyEvent.KEYCODE_DPAD_LEFT){
								sortListView1.getChildAt(sortListView1.getCurrentItem()).requestFocus();
								return true;
							}else if(keyCode==KeyEvent.KEYCODE_DPAD_RIGHT){
								sortListView3.getChildAt(sortListView3.getCurrentItem()).requestFocus();
								return true;
							}
							break;
						case 3:
							if(keyCode==KeyEvent.KEYCODE_DPAD_LEFT){
								sortListView2.getChildAt(sortListView2.getCurrentItem()).requestFocus();
								return true;
							}else if(keyCode==KeyEvent.KEYCODE_DPAD_RIGHT){
								
							}
							break;

						default:
							break;
						}
					}
					return false;
				}
			});
			view.setTag(position);
			container.addView(view);
			return view;
		}

	}
	//选中其中一个。其他则不被选中
	private void refreshBackground(int index) {
		switch (index) {
		case 1:
			for (int i = 0; i < sortListView1.getChildCount(); i++) {
				if(i == sort_index1){
					sortListView1.getChildAt(i).setSelected(true);
				}else{
					sortListView1.getChildAt(i).setSelected(false);
				}
			}
			break;
		case 2:
			for (int i = 0; i < sortListView2.getChildCount(); i++) {
				if(i == sort_index2){
					sortListView2.getChildAt(i).setSelected(true);
				}else{
					sortListView2.getChildAt(i).setSelected(false);
				}
			}
			break;
		case 3:
			for (int i = 0; i < sortListView3.getChildCount(); i++) {
				if(i == sort_index3){
					sortListView3.getChildAt(i).setSelected(true);
				}else{
					sortListView3.getChildAt(i).setSelected(false);
				}
			}
			break;

		default:
			break;
		}
	}
}
